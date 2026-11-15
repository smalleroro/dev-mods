package com.dsh.baritoneui.ui

import com.dsh.baritoneui.BaritoneBridge
import com.dsh.baritoneui.BaritoneTask
import com.dsh.baritoneui.config.UiConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraftforge.client.gui.overlay.IGuiOverlay
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Status HUD drawn in the top-left corner: current Baritone task, target and
 * distance. Renders a short "not installed" notice when Baritone is absent
 * instead of hiding entirely, so users understand why nothing happens.
 */
object StatusHud : IGuiOverlay {
    private const val PADDING = 3
    private const val LINE_HEIGHT = 10

    override fun render(
        forgeGui: net.minecraftforge.client.gui.overlay.ForgeGui,
        guiGraphics: GuiGraphics,
        partialTick: Float,
        screenWidth: Int,
        screenHeight: Int
    ) {
        if (!HudState.isEnabled()) return
        val mc = Minecraft.getInstance()
        if (mc.player == null || mc.options.hideGui) return
        if (mc.screen != null) return // keep the HUD out of menus

        val lines = buildLines()
        if (lines.isEmpty()) return

        val font = mc.font
        val width = lines.maxOf { font.width(it.first) } + PADDING * 2
        val height = lines.size * LINE_HEIGHT + PADDING * 2
        val x = UiConfig.hudX.get()
        val y = UiConfig.hudY.get()

        val alpha = (UiConfig.hudOpacity.get() * 255.0).roundToInt().coerceIn(0, 255)
        guiGraphics.fill(x, y, x + width, y + height, alpha shl 24)

        lines.forEachIndexed { i, (text, color) ->
            guiGraphics.drawString(
                font,
                text,
                x + PADDING,
                y + PADDING + i * LINE_HEIGHT,
                color,
                true
            )
        }
    }

    private fun buildLines(): List<Pair<String, Int>> {
        val out = mutableListOf<Pair<String, Int>>()

        if (!BaritoneBridge.available) {
            out += Component.translatable("baritoneui.status.not_installed").string to 0xFF5555
            return out
        }

        val task = BaritoneBridge.currentTask()

        if (UiConfig.showTaskName.get()) {
            val color = if (task == BaritoneTask.IDLE) 0xAAAAAA else 0x55FF55
            out += Component.translatable(task.translationKey).string to color
        }

        val pos = BaritoneBridge.playerPos()
        if (pos != null) {
            out += "${Component.translatable("baritoneui.hud.target").string}: " +
                "${pos.x} ${pos.y} ${pos.z}" to 0xFFFFFF

            if (UiConfig.showDistance.get() && lastTarget != null) {
                val t = lastTarget!!
                val dist = kotlin.math.sqrt(
                    ((t.first - pos.x) * (t.first - pos.x) +
                        (t.second - pos.z) * (t.second - pos.z)).toDouble()
                )
                out += Component.translatable("baritoneui.label.distance").string +
                    ": ${dist.roundToInt()}m" to 0xFFFF55
            }
        }

        return out
    }

    /**
     * Last XZ target set from the panel, used for the distance readout.
     * Baritone's own goal API is not queried here to keep the HUD cheap.
     */
    var lastTarget: Pair<Int, Int>? = null
        private set

    fun setTarget(x: Int, z: Int) {
        lastTarget = x to z
    }

    fun clearTarget() {
        lastTarget = null
    }
}
