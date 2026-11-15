package com.dsh.rotationspoof

import com.mojang.logging.LogUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.slf4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Rotation Spoof -- client-only.
 *
 * Press the hotkey to toggle; while enabled the server (and therefore every
 * other player) sees the body spin, while the local camera is unaffected
 * because only outgoing packets are rewritten.
 */
@Mod(RotationSpoofMod.MOD_ID)
object RotationSpoofMod {
    const val MOD_ID: String = "rotationspoof"
    private val LOGGER: Logger = LogUtils.getLogger()

    init {
        MOD_BUS.addListener(::onRegisterKeys)
        MinecraftForge.EVENT_BUS.register(TickHandler)
        LOGGER.info("Rotation Spoof loading.")
    }

    private fun onRegisterKeys(event: net.minecraftforge.client.event.RegisterKeyMappingsEvent) {
        event.register(KeyBindings.toggleKey)
    }

    /** Polls the hotkey on the client tick and handles disconnect cleanup. */
    private object TickHandler {
        private var lastDown = false
        private var wasInWorld = false

        @SubscribeEvent
        fun onClientTick(event: TickEvent.ClientTickEvent) {
            if (event.phase != TickEvent.Phase.END) return

            val mc = Minecraft.getInstance()
            val inWorld = mc.player != null

            // Leaving a world: drop the state so the next world starts clean.
            if (wasInWorld && !inWorld) {
                RotationSpoofManager.setEnabled(false)
                RotationSpoofManager.reset()
            }
            wasInWorld = inWorld

            if (!inWorld) {
                lastDown = false
                return
            }

            val down = KeyBindings.toggleKey.isDown
            if (down && !lastDown) {
                val nowOn = RotationSpoofManager.toggle()
                if (!nowOn) RotationSpoofManager.reset()
                mc.player?.displayClientMessage(
                    Component.translatable(
                        if (nowOn) "rotationspoof.message.enabled"
                        else "rotationspoof.message.disabled"
                    ),
                    true
                )
            }
            lastDown = down
        }
    }
}
