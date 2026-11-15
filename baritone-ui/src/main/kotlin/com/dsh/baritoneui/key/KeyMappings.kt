package com.dsh.baritoneui.key

import com.dsh.baritoneui.ui.ControlScreen
import com.dsh.baritoneui.ui.HudState
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.client.settings.KeyConflictContext
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.lwjgl.glfw.GLFW

/**
 * Key bindings for opening the panel and toggling the HUD.
 *
 * Polling happens on the client tick rather than through Forge's
 * InputEvent.Key because this mod is client-only and the tick hook
 * is simpler to reason about across mappings.
 */
object KeyMappings {
    val openPanel: KeyMapping = KeyMapping(
        "key.baritoneui.open_panel",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_B,
        "key.categories.baritoneui"
    )

    val toggleHud: KeyMapping = KeyMapping(
        "key.baritoneui.toggle_hud",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_H,
        "key.categories.baritoneui"
    )

    fun register(event: RegisterKeyMappingsEvent) {
        event.register(openPanel)
        event.register(toggleHud)
        MinecraftForge.EVENT_BUS.register(TickHandler)
    }

    private object TickHandler {
        private var lastOpen = false
        private var lastToggle = false

        @SubscribeEvent
        fun onClientTick(event: TickEvent.ClientTickEvent) {
            if (event.phase != TickEvent.Phase.END) return
            val mc = Minecraft.getInstance()
            if (mc.player == null) return

            val open = openPanel.isDown
            if (open && !lastOpen && mc.screen == null) {
                mc.setScreen(ControlScreen())
            }
            lastOpen = open

            val toggle = toggleHud.isDown
            if (toggle && !lastToggle) {
                // In-memory toggle only: writing ForgeConfigSpec here throws when
                // the config is not loaded and would hit the disk every press.
                HudState.toggle()
            }
            lastToggle = toggle
        }
    }
}
