package com.dsh.baritoneui

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import org.slf4j.LoggerFactory

/**
 * Reflective bridge to Baritone.
 *
 * Baritone is an OPTIONAL runtime dependency, so every access goes through
 * reflection. If Baritone is missing or its API shape differs, the bridge
 * reports `available == false` instead of throwing, letting the UI degrade
 * gracefully rather than crashing the game.
 *
 * Baritone API surface used (1.10.x):
 *   baritone.api.BaritoneAPI.getProvider().getPrimaryBaritone() -> IBaritone
 *   IBaritone.getCustomGoalProcess().setGoalAndPath(Goal)
 *   IBaritone.getMineProcess().mine(BlockOptionalMetaLookup) / .cancel()
 *   IBaritone.getFollowProcess().follow(EntityPredicate) / .cancel()
 *   IBaritone.getExploreProcess().explore(x, z) / .cancel()
 *   IBaritone.getFarmProcess().farm() / .cancel()
 *   IBaritone.getPathingBehavior().isPathing()
 */
object BaritoneBridge {
    private val LOGGER = LoggerFactory.getLogger("BaritoneUI/Bridge")!!

    /** Null when Baritone is absent; resolved lazily on first use. */
    private var provider: Any? = null
    private var baritone: Any? = null
    private var resolveAttempted = false
    private var failedReason: String? = null

    /** True when Baritone is present and its core API bound successfully. */
    val available: Boolean
        get() {
            resolve()
            return baritone != null
        }

    /** Human-readable reason when [available] is false. */
    val unavailableReason: String?
        get() {
            resolve()
            return failedReason
        }

    private fun resolve() {
        if (resolveAttempted) return
        resolveAttempted = true
        try {
            val apiClass = Class.forName("baritone.api.BaritoneAPI")
            val getProvider = apiClass.getMethod("getProvider")
            provider = getProvider.invoke(null)
            val getPrimary = provider!!.javaClass.getMethod("getPrimaryBaritone")
            baritone = getPrimary.invoke(provider)
            LOGGER.info("Baritone bound successfully.")
        } catch (t: Throwable) {
            failedReason = t.message ?: t.javaClass.simpleName
            LOGGER.warn("Baritone not available: {}", failedReason)
            baritone = null
        }
    }

    /** Forces a re-resolve; used after a resource reload in case Baritone loaded late. */
    fun invalidate() {
        resolveAttempted = false
        provider = null
        baritone = null
        failedReason = null
    }

    // ---------------------------------------------------------------- process access

    private fun process(name: String): Any? {
        if (!available) return null
        return try {
            baritone!!.javaClass.getMethod(name).invoke(baritone)
        } catch (t: Throwable) {
            LOGGER.warn("Baritone process '{}' unavailable: {}", name, t.message)
            null
        }
    }

    // ---------------------------------------------------------------- state queries

    /** True while Baritone is actively computing or executing a path. */
    fun isPathing(): Boolean {
        val behavior = process("getPathingBehavior") ?: return false
        return try {
            behavior.javaClass.getMethod("isPathing").invoke(behavior) as? Boolean ?: false
        } catch (t: Throwable) {
            false
        }
    }

    /** True when any Baritone process is currently active. */
    fun isActive(): Boolean {
        val behavior = process("getPathingBehavior") ?: return false
        return try {
            val hasPath = behavior.javaClass.getMethod("hasPath").invoke(behavior) as? Boolean ?: false
            hasPath || isPathing()
        } catch (t: Throwable) {
            isPathing()
        }
    }

    /** Coarse classification of what Baritone is doing, for the HUD label. */
    fun currentTask(): BaritoneTask {
        if (!available) return BaritoneTask.NOT_INSTALLED
        for (candidate in BaritoneTask.entries) {
            if (candidate.processName == null) continue
            val p = process("get${candidate.processName}Process")
            if (p != null && isProcessActive(p)) return candidate
        }
        return if (isPathing()) BaritoneTask.PATHING else BaritoneTask.IDLE
    }

    private fun isProcessActive(process: Any): Boolean {
        return try {
            process.javaClass.getMethod("isActive").invoke(process) as? Boolean ?: false
        } catch (t: Throwable) {
            false
        }
    }

    /** Live player position from the client, used for distance/ETA readouts. */
    fun playerPos(): BlockPos? = Minecraft.getInstance().player?.blockPosition()

    // ---------------------------------------------------------------- actions

    /** `#goto x z` -- walks to the given XZ coordinate at any Y level. */
    fun gotoXZ(x: Int, z: Int): Boolean {
        return try {
            val goalXZ = Class.forName("baritone.api.pathing.goals.GoalXZ")
                .getConstructor(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                .newInstance(x, z)
            setGoalAndPath(goalXZ)
        } catch (t: Throwable) {
            LOGGER.warn("gotoXZ failed: {}", t.message)
            false
        }
    }

    /** `#goto x y z` -- walks to the exact block. */
    fun gotoBlock(x: Int, y: Int, z: Int): Boolean {
        return try {
            val goalBlock = Class.forName("baritone.api.pathing.goals.GoalBlock")
                .getConstructor(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                .newInstance(x, y, z)
            setGoalAndPath(goalBlock)
        } catch (t: Throwable) {
            LOGGER.warn("gotoBlock failed: {}", t.message)
            false
        }
    }

    private fun setGoalAndPath(goal: Any): Boolean {
        val customGoal = process("getCustomGoalProcess") ?: return false
        return try {
            customGoal.javaClass
                .getMethod("setGoalAndPath", Class.forName("baritone.api.pathing.goals.Goal"))
                .invoke(customGoal, goal)
            true
        } catch (t: Throwable) {
            LOGGER.warn("setGoalAndPath failed: {}", t.message)
            false
        }
    }

    /**
     * `#mine <block>` -- mines every matching block.
     * Accepts comma-separated block ids, e.g. "diamond_ore,deepslate_diamond_ore".
     */
    fun mine(blocks: String): Boolean {
        val mineProcess = process("getMineProcess") ?: return false
        return try {
            val ids = blocks.split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { if (it.contains(':')) it else "minecraft:$it" }

            // BlockOptionalMetaLookup(BlockOptionalMeta...) or (String...)
            val bomClass = Class.forName("baritone.api.utils.BlockOptionalMeta")
            val lookupClass = Class.forName("baritone.api.utils.BlockOptionalMetaLookup")
            val lookup = try {
                val boms = ids.map { id -> bomClass.getConstructor(String::class.java).newInstance(id) }
                val arr = java.lang.reflect.Array.newInstance(bomClass, boms.size)
                boms.forEachIndexed { i, b -> java.lang.reflect.Array.set(arr, i, b) }
                lookupClass.getConstructor(arr.javaClass).newInstance(arr)
            } catch (t: Throwable) {
                // Fall back to the String... constructor used by newer builds.
                val arr = java.lang.reflect.Array.newInstance(String::class.java, ids.size)
                ids.forEachIndexed { i, s -> java.lang.reflect.Array.set(arr, i, s) }
                lookupClass.getConstructor(arr.javaClass).newInstance(arr)
            }

            mineProcess.javaClass
                .getMethod("mine", lookupClass)
                .invoke(mineProcess, lookup)
            true
        } catch (t: Throwable) {
            LOGGER.warn("mine failed: {}", t.message)
            false
        }
    }

    /** `#follow player <name>` -- follows the named player. */
    fun follow(playerName: String): Boolean {
        val followProcess = process("getFollowProcess") ?: return false
        return try {
            // EntityPredicate via Baritone's factory; falls back to a name filter.
            val predicateClass = Class.forName("java.util.function.Predicate")
            val predicate = java.util.function.Predicate<Any> { entity ->
                try {
                    val name = entity.javaClass.getMethod("getName").invoke(entity)
                    name?.javaClass?.getMethod("getString")?.invoke(name) == playerName
                } catch (t: Throwable) {
                    false
                }
            }
            followProcess.javaClass
                .getMethod("follow", predicateClass)
                .invoke(followProcess, predicate)
            true
        } catch (t: Throwable) {
            LOGGER.warn("follow failed: {}", t.message)
            false
        }
    }

    /** `#explore` -- explores outward from the current position. */
    fun explore(): Boolean {
        val exploreProcess = process("getExploreProcess") ?: return false
        val pos = playerPos() ?: return false
        return try {
            exploreProcess.javaClass
                .getMethod("explore", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                .invoke(exploreProcess, pos.x, pos.z)
            true
        } catch (t: Throwable) {
            LOGGER.warn("explore failed: {}", t.message)
            false
        }
    }

    /** `#farm` -- runs Baritone's automatic farm routine. */
    fun farm(): Boolean {
        val farmProcess = process("getFarmProcess") ?: return false
        return try {
            farmProcess.javaClass.getMethod("farm").invoke(farmProcess)
            true
        } catch (t: Throwable) {
            LOGGER.warn("farm failed: {}", t.message)
            false
        }
    }

    /** `#stop` -- cancels every Baritone process. */
    fun stop(): Boolean {
        if (!available) return false
        var any = false
        for (name in listOf(
            "getCustomGoalProcess", "getMineProcess", "getFollowProcess",
            "getExploreProcess", "getFarmProcess", "getBuilderProcess"
        )) {
            val p = process(name) ?: continue
            try {
                p.javaClass.getMethod("onLostControl").invoke(p)
                any = true
            } catch (t: Throwable) {
                try {
                    p.javaClass.getMethod("cancel").invoke(p)
                    any = true
                } catch (_: Throwable) {
                    // Process exposes neither; skip it.
                }
            }
        }
        return any
    }
}

/** Baritone activity, mapped to a translatable HUD label. */
enum class BaritoneTask(val processName: String?, val translationKey: String) {
    NOT_INSTALLED(null, "baritoneui.status.not_installed"),
    IDLE(null, "baritoneui.status.idle"),
    PATHING(null, "baritoneui.status.pathing"),
    MINING("Mine", "baritoneui.status.mining"),
    FOLLOWING("Follow", "baritoneui.status.following"),
    EXPLORING("Explore", "baritoneui.status.exploring"),
    BUILDING("Builder", "baritoneui.status.building"),
    FARMING("Farm", "baritoneui.status.farming");

    companion object {
        val entries: List<BaritoneTask> = listOf(MINING, FOLLOWING, EXPLORING, BUILDING, FARMING)
    }
}
