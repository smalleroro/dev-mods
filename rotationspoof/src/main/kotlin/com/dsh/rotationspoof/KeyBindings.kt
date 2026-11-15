package com.dsh.rotationspoof

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraftforge.client.settings.KeyConflictContext
import org.lwjgl.glfw.GLFW

/** Hotkey for toggling the spoof. Default R; rebindable in Options > Controls. */
object KeyBindings {
    val toggleKey: KeyMapping = KeyMapping(
        "key.rotationspoof.toggle",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "key.categories.rotationspoof"
    )
}
