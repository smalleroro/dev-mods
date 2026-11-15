package com.dsh.baritoneui.ui

import com.dsh.baritoneui.config.UiConfig

/**
 * Runtime HUD toggle state.
 *
 * Deliberately kept OUT of ForgeConfigSpec: writing a config value every time
 * the user presses the hotkey would both hit the disk and throw
 * "Cannot set config value without assigned Config object present" whenever the
 * config has not been loaded yet. The config value is only read once, as the
 * initial state.
 */
object HudState {
    @Volatile
    private var initialised = false
    private var enabled = true

    /** Reads the initial value from config on first access. */
    fun isEnabled(): Boolean {
        if (!initialised) {
            initialised = true
            enabled = runCatching { UiConfig.hudEnabled.get() }.getOrDefault(true)
        }
        return enabled
    }

    fun toggle(): Boolean {
        val next = !isEnabled()
        setEnabled(next)
        return next
    }

    fun setEnabled(value: Boolean) {
        initialised = true
        enabled = value
    }
}
