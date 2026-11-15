package com.dsh.baritoneui.ui

import com.dsh.baritoneui.BaritoneBridge
import com.dsh.baritoneui.BaritoneTask
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

/**
 * The visual control panel.
 *
 * Layout:
 *   - Left column:  task buttons (Stop / Go To / Mine / Follow / Explore / Farm)
 *   - Middle:       parameter fields for the selected action
 *   - Right column: category tabs + multi-select preset buttons (Mine mode only)
 *
 * Mining presets are multi-select across categories: each preset toggles its own
 * selected state, and "Start mining" merges every selected preset's block list
 * into one Baritone `#mine` request.
 */
class ControlScreen : Screen(Component.translatable("baritoneui.title")) {

    private enum class Mode(val labelKey: String) {
        GOTO("baritoneui.button.goto"),
        MINE("baritoneui.button.mine"),
        FOLLOW("baritoneui.button.follow"),
        EXPLORE("baritoneui.button.explore"),
        FARM("baritoneui.button.farm")
    }

    /** Which preset list the Mine tab is showing. */
    private enum class PresetCategory(val labelKey: String, val shortKey: String, val presets: List<MinePreset>) {
        ORES("baritoneui.preset.cat.ores", "baritoneui.preset.cat.ores.short", MinePreset.ORES),
        MODDED("baritoneui.preset.cat.moddded", "baritoneui.preset.cat.moddded.short", MinePreset.MODDED_ORES),
        MATERIALS("baritoneui.preset.cat.materials", "baritoneui.preset.cat.materials.short", MinePreset.MATERIALS),
        WOOD("baritoneui.preset.cat.wood", "baritoneui.preset.cat.wood.short", MinePreset.WOOD)
    }

    /** A preset button that keeps a selected/unselected appearance. */
    private inner class PresetButton(
        x: Int, y: Int, w: Int, h: Int,
        private val preset: MinePreset
    ) : AbstractWidget(x, y, w, h, Component.translatable(preset.labelKey)) {

        override fun renderWidget(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
            val selected = preset in selectedPresets
            val hovered = isHovered && active

            val bg = when {
                selected && hovered -> 0xFF3A7A3A.toInt()
                selected -> 0xFF2A6A2A.toInt()
                hovered -> 0xFF4A4A4A.toInt()
                else -> 0xFF2B2B2B.toInt()
            }
            val border = if (selected) 0xFF55FF55.toInt() else 0xFF555555.toInt()

            g.fill(x, y, x + width, y + height, bg)
            // 1px border
            g.fill(x, y, x + width, y + 1, border)
            g.fill(x, y + height - 1, x + width, y + height, border)
            g.fill(x, y, x + 1, y + height, border)
            g.fill(x + width - 1, y, x + width, y + height, border)

            val label = if (selected) "\u2714 " + message.string else message.string
            val color = if (selected) 0xFFFFFF else 0xCCCCCC
            g.drawCenteredString(
                Minecraft.getInstance().font,
                label,
                x + width / 2,
                y + (height - 8) / 2,
                color
            )
        }

        override fun onClick(mouseX: Double, mouseY: Double) {
            if (preset in selectedPresets) selectedPresets.remove(preset)
            else selectedPresets.add(preset)
            syncParamBox()
        }

        override fun updateWidgetNarration(output: NarrationElementOutput) {
            defaultButtonNarrationText(output)
        }
    }

    private var mode = Mode.GOTO
    private var category = PresetCategory.ORES
    private var statusLine: Component? = null

    /** Multi-select state, shared across categories. */
    private val selectedPresets = linkedSetOf<MinePreset>()

    private lateinit var xBox: EditBox
    private lateinit var yBox: EditBox
    private lateinit var zBox: EditBox
    private lateinit var paramBox: EditBox

    /** Widgets rebuilt when the Mine tab's category changes. */
    private val presetWidgets = mutableListOf<AbstractWidget>()

    override fun init() {
        val cx = width / 2
        val top = height / 4

        // ---- action buttons (left column) ----
        val btnW = 90
        val btnH = 20
        val leftX = cx - 175
        var by = top

        addRenderableWidget(
            Button.builder(Component.translatable("baritoneui.button.stop")) {
                BaritoneBridge.stop()
                StatusHud.clearTarget()
                statusLine = Component.literal("Stopped.")
            }.bounds(leftX, by, btnW, btnH).build()
        )
        by += btnH + 4

        for (m in Mode.entries) {
            addRenderableWidget(
                Button.builder(Component.translatable(m.labelKey)) {
                    mode = m
                    rebuildPresets()
                    applyModeVisibility()
                }.bounds(leftX, by, btnW, btnH).build()
            )
            by += btnH + 4
        }

        // ---- parameter fields (middle) ----
        val fieldX = cx - 75
        var fy = top

        xBox = EditBox(font, fieldX, fy, 90, 18, Component.literal("X")).apply {
            setMaxLength(12); value = "0"
        }
        addRenderableWidget(xBox); fy += 22

        yBox = EditBox(font, fieldX, fy, 90, 18, Component.literal("Y")).apply {
            setMaxLength(12); value = "64"
        }
        addRenderableWidget(yBox); fy += 22

        zBox = EditBox(font, fieldX, fy, 90, 18, Component.literal("Z")).apply {
            setMaxLength(12); value = "0"
        }
        addRenderableWidget(zBox); fy += 22

        paramBox = EditBox(font, fieldX, fy, 90, 18, Component.literal("Param")).apply {
            setMaxLength(400); value = ""
        }
        addRenderableWidget(paramBox); fy += 28

        // In Mine mode this button starts mining; otherwise it runs the action.
        addRenderableWidget(
            Button.builder(Component.literal("Run")) { runCurrent() }
                .bounds(fieldX, fy, 90, 20).build()
        )

        addRenderableWidget(
            Button.builder(Component.translatable("gui.done")) { onClose() }
                .bounds(cx - 50, height - 30, 100, 20).build()
        )

        rebuildPresets()
        applyModeVisibility()
    }

    /** Builds the Mine-mode preset column: category tabs plus preset buttons. */
    private fun rebuildPresets() {
        presetWidgets.forEach { removeWidget(it) }
        presetWidgets.clear()
        if (mode != Mode.MINE) return

        val cx = width / 2
        val top = height / 4
        val gridX = cx + 42
        val btnW = 104
        val btnH = 14
        val gapX = 2
        val gapY = 1

        // ---- category tabs (wrap onto a second row if needed) ----
        var ty = top - 12
        val tabW = 52
        var tabX = gridX
        PresetCategory.entries.forEach { c ->
            if (tabX + tabW > width - 8) {
                tabX = gridX
                ty += btnH + 2
            }
            val b = Button.builder(Component.translatable(c.shortKey)) {
                category = c
                rebuildPresets()
            }.bounds(tabX, ty, tabW, btnH).build()
            b.active = c != category
            addRenderableWidget(b)
            presetWidgets += b
            tabX += tabW + gapX
        }
        ty += btnH + 4

        // ---- preset buttons, laid out in as many columns as fit ----
        val avail = width - gridX - 8
        val cols = ((avail + gapX) / (btnW + gapX)).coerceAtLeast(1)
        val bottomLimit = height - 52

        category.presets.forEachIndexed { i, preset ->
            val col = i % cols
            val row = i / cols
            val px = gridX + col * (btnW + gapX)
            val py = ty + row * (btnH + gapY)
            if (py + btnH > bottomLimit) return@forEachIndexed
            val b = PresetButton(px, py, btnW, btnH, preset)
            addRenderableWidget(b)
            presetWidgets += b
        }

        // ---- convenience row: clear the current selection ----
        val clrY = (bottomLimit + 4).coerceAtMost(height - 44)
        val clr = Button.builder(Component.translatable("baritoneui.preset.clear")) {
            selectedPresets.clear()
            syncParamBox()
            rebuildPresets()
        }.bounds(gridX, clrY, 74, btnH + 2).build()
        addRenderableWidget(clr)
        presetWidgets += clr
    }

    /** Mirrors the current selection into the editable block list. */
    private fun syncParamBox() {
        paramBox.value = selectedPresets
            .flatMap { it.blocks.split(',') }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(",")
    }

    private fun applyModeVisibility() {
        val pos = Minecraft.getInstance().player?.blockPosition()
        if (pos != null && xBox.value == "0" && zBox.value == "0") {
            xBox.value = pos.x.toString()
            yBox.value = pos.y.toString()
            zBox.value = pos.z.toString()
        }

        when (mode) {
            Mode.GOTO -> {
                xBox.visible = true; yBox.visible = true; zBox.visible = true
                paramBox.visible = false
            }
            Mode.MINE -> {
                xBox.visible = true; yBox.visible = false; zBox.visible = true
                paramBox.visible = true
            }
            Mode.FOLLOW -> {
                xBox.visible = false; yBox.visible = false; zBox.visible = false
                paramBox.visible = true
                paramBox.value = ""
            }
            Mode.EXPLORE, Mode.FARM -> {
                xBox.visible = false; yBox.visible = false; zBox.visible = false
                paramBox.visible = false
            }
        }
    }

    private fun runCurrent() {
        if (!BaritoneBridge.available) {
            statusLine = Component.translatable("baritoneui.status.not_installed")
            return
        }

        statusLine = when (mode) {
            Mode.GOTO -> {
                val x = xBox.value.toIntOrNull()
                val y = yBox.value.toIntOrNull()
                val z = zBox.value.toIntOrNull()
                if (x == null || z == null) {
                    Component.literal("Invalid coordinates.")
                } else {
                    val ok = if (y != null) BaritoneBridge.gotoBlock(x, y, z)
                    else BaritoneBridge.gotoXZ(x, z)
                    if (ok) {
                        StatusHud.setTarget(x, z)
                        Component.literal("Going to $x ${y ?: "~"} $z")
                    } else {
                        Component.literal("Baritone rejected the goal.")
                    }
                }
            }

            Mode.MINE -> {
                // Fold any manual edits back into the selection view first.
                val blocks = paramBox.value.trim()
                if (blocks.isEmpty()) {
                    Component.literal("Select at least one preset.")
                } else {
                    if (BaritoneBridge.mine(blocks)) {
                        val count = blocks.split(',').count { it.isNotBlank() }
                        Component.literal("Mining $count block type(s).")
                    } else {
                        Component.literal("Mine request failed.")
                    }
                }
            }

            Mode.FOLLOW -> {
                val name = paramBox.value.trim()
                if (name.isEmpty()) {
                    Component.literal("Enter a player name.")
                } else {
                    if (BaritoneBridge.follow(name)) Component.literal("Following $name")
                    else Component.literal("Follow request failed.")
                }
            }

            Mode.EXPLORE ->
                if (BaritoneBridge.explore()) Component.literal("Exploring...")
                else Component.literal("Explore request failed.")

            Mode.FARM ->
                if (BaritoneBridge.farm()) Component.literal("Farming...")
                else Component.literal("Farm request failed.")
        }
    }

    override fun render(g: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(g)
        g.drawCenteredString(font, title, width / 2, height / 4 - 24, 0xFFFFFF)

        val cx = width / 2
        val top = height / 4
        val labelX = cx - 75 - 22

        when (mode) {
            Mode.GOTO -> {
                g.drawString(font, Component.translatable("baritoneui.label.target_x").string, labelX, top + 5, 0xAAAAAA)
                g.drawString(font, Component.translatable("baritoneui.label.target_y").string, labelX, top + 27, 0xAAAAAA)
                g.drawString(font, Component.translatable("baritoneui.label.target_z").string, labelX, top + 49, 0xAAAAAA)
            }
            Mode.MINE -> {
                g.drawString(font, Component.translatable("baritoneui.label.target_x").string, labelX, top + 5, 0xAAAAAA)
                g.drawString(font, Component.translatable("baritoneui.label.target_z").string, labelX, top + 49, 0xAAAAAA)
                g.drawString(font, Component.translatable("baritoneui.label.mine_block").string, labelX, top + 71, 0xAAAAAA)
                g.drawString(font, Component.translatable("baritoneui.preset.header").string, cx + 42, top - 26, 0xFFFF55)
                val selectedCount = selectedPresets.size
                g.drawString(
                    font,
                    Component.translatable("baritoneui.preset.selected").string + ": $selectedCount",
                    cx + 42, height - 40, 0x55FF55
                )
            }
            Mode.FOLLOW ->
                g.drawString(font, Component.translatable("baritoneui.label.follow_player").string, labelX, top + 71, 0xAAAAAA)
            else -> {}
        }

        val stateText = if (!BaritoneBridge.available) {
            Component.translatable("baritoneui.status.not_installed").string
        } else {
            Component.translatable(BaritoneBridge.currentTask().translationKey).string
        }
        val stateColor = if (!BaritoneBridge.available) 0xFF5555
        else if (BaritoneBridge.currentTask() == BaritoneTask.IDLE) 0xAAAAAA else 0x55FF55
        g.drawCenteredString(font, stateText, cx, height / 4 - 10, stateColor)

        statusLine?.let {
            g.drawCenteredString(font, it, cx, height - 56, 0xFFFF55)
        }

        super.render(g, mouseX, mouseY, partialTick)
    }

    override fun isPauseScreen(): Boolean = false
}
