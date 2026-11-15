package com.dsh.baritoneui.config

import com.dsh.baritoneui.BaritoneUiMod
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig

/**
 * Client config for the HUD and panel.
 *
 * Kept intentionally small: the panel itself has no persistent state, and
 * Baritone owns its own settings (config/baritone/settings.txt).
 */
object UiConfig {
    private val spec: ForgeConfigSpec
    val hudEnabled: ForgeConfigSpec.BooleanValue
    val hudX: ForgeConfigSpec.IntValue
    val hudY: ForgeConfigSpec.IntValue
    val hudOpacity: ForgeConfigSpec.DoubleValue
    val showDistance: ForgeConfigSpec.BooleanValue
    val showTaskName: ForgeConfigSpec.BooleanValue

    init {
        val b = ForgeConfigSpec.Builder()

        b.comment("Status HUD settings").push("hud")
        hudEnabled = b
            .comment("Show the Baritone status HUD.")
            .define("enabled", true)
        hudX = b
            .comment("HUD X offset in pixels from the left edge.")
            .defineInRange("x", 4, 0, 10000)
        hudY = b
            .comment("HUD Y offset in pixels from the top edge.")
            .defineInRange("y", 4, 0, 10000)
        hudOpacity = b
            .comment("Background opacity, 0.0 = transparent, 1.0 = opaque.")
            .defineInRange("opacity", 0.5, 0.0, 1.0)
        showDistance = b
            .comment("Show distance to the current target when available.")
            .define("show_distance", true)
        showTaskName = b
            .comment("Show the active Baritone task name.")
            .define("show_task_name", true)
        b.pop()

        spec = b.build()
    }

    fun register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, spec)
    }

    /** Called from client setup; tolerates a missing config file. */
    fun load() {
        // ForgeConfigSpec values are read lazily; nothing to do here.
        // Kept as a hook so the mod class has an explicit init point.
        if (spec.isLoaded) {
            // Touch a value to force config file creation early.
            hudEnabled.get()
        }
    }
}
