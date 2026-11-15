package com.dsh.baritoneui

import com.dsh.baritoneui.config.UiConfig
import com.dsh.baritoneui.key.KeyMappings
import com.dsh.baritoneui.ui.StatusHud
import com.mojang.logging.LogUtils
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.slf4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Baritone UI -- a visual control panel and status HUD for Baritone.
 *
 * Client-only: Baritone is a client-side automation mod, so this mod registers
 * no server behaviour. `mods.toml` declares side="CLIENT" for the Baritone dep.
 */
@Mod(BaritoneUiMod.MOD_ID)
object BaritoneUiMod {
    const val MOD_ID: String = "baritoneui"
    private val LOGGER: Logger = LogUtils.getLogger()

    init {
        MOD_BUS.addListener(::onClientSetup)
        MOD_BUS.addListener(::onRegisterKeys)
        MOD_BUS.addListener(::onRegisterOverlays)

        MinecraftForge.EVENT_BUS.register(StatusHud)

        LOGGER.info("Baritone UI loading.")
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        UiConfig.register()
        UiConfig.load()
        LOGGER.info("Baritone UI client setup. Baritone available = {}", BaritoneBridge.available)
    }

    private fun onRegisterKeys(event: RegisterKeyMappingsEvent) {
        KeyMappings.register(event)
    }

    private fun onRegisterOverlays(event: RegisterGuiOverlaysEvent) {
        event.registerAboveAll("baritoneui_status", StatusHud)
    }
}
