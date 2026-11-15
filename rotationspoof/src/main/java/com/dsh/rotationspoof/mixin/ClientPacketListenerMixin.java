package com.dsh.rotationspoof.mixin;

import com.dsh.rotationspoof.RotationSpoofManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Rewrites the yaw/pitch of outgoing movement packets so other players see the
 * body spin while the local camera keeps its real rotation.
 *
 * Implementation notes
 * --------------------
 * The angle fields (yRot/xRot) live on the abstract parent
 * ServerboundMovePlayerPacket, are declared `protected final`, and are shared by
 * every subclass (Rot, Pos, PosRot). Rewriting them in place is preferable to
 * cancelling and re-sending a replacement packet:
 *   - the packet count stays identical, so no burst of extra traffic;
 *   - position data is preserved untouched, so the server never rubber-bands
 *     the player back.
 *
 * The fields are reached by reflection rather than through an AccessTransformer.
 * AT needs a ForgeGradle `_at_<hash>` forge variant to be regenerated, which is
 * fragile across caches; the reflective lookup below runs once and is then
 * cached, and one field write per tick is not a hot path.
 *
 * Written in Java on purpose: Mixin's annotation processor and refmap
 * generation are reliable for Java sources and fragile for Kotlin ones.
 */
@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    private static Field yRotField;
    private static Field xRotField;
    private static boolean fieldsResolved;

    @Inject(method = "m_104955_", at = @At("HEAD"), remap = false)
    private void rotationspoof$rewriteRotation(Packet<?> packet, CallbackInfo ci) {
        if (!RotationSpoofManager.INSTANCE.isEnabled()) {
            return;
        }

        if (!(packet instanceof ServerboundMovePlayerPacket move)) {
            return;
        }

        // Only packets that actually carry an angle have something to fake.
        if (!move.hasRotation()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (!resolveFields()) {
            return;
        }

        try {
            yRotField.setFloat(move, RotationSpoofManager.INSTANCE.nextFakeYaw());
            xRotField.setFloat(move, RotationSpoofManager.INSTANCE.getPitch());
        } catch (Throwable ignored) {
            // Silently skip this packet rather than risk breaking movement.
        }
    }

    /**
     * Locates and un-finalises the two angle fields once per JVM.
     *
     * The lookup deliberately does NOT use the Mojang field names (`yRot` /
     * `xRot`): a reflective lookup takes a literal string that Mixin cannot
     * remap, so at runtime those names are the SRG ones (`f_134121_` /
     * `f_134122_`) and a name-based lookup would silently fail. Instead the two
     * `float` fields declared directly on ServerboundMovePlayerPacket are taken
     * in declaration order, which is stable across mappings:
     *
     *     f_134121_ yRot   <- first float declared on this class
     *     f_134122_ xRot   <- second float declared on this class
     *
     * The superclass also declares floats (and doubles), so only
     * getDeclaredFields() of the packet class itself is considered, and the
     * two nearest neighbours are confirmed to be adjacent `protected final
     * float` fields before being accepted.
     */
    private static synchronized boolean resolveFields() {
        if (fieldsResolved) {
            return yRotField != null && xRotField != null;
        }
        fieldsResolved = true;
        try {
            Field[] declared = ServerboundMovePlayerPacket.class.getDeclaredFields();

            Field first = null;
            Field second = null;
            for (Field f : declared) {
                if (f.getType() != float.class) {
                    continue;
                }
                if (first == null) {
                    first = f;
                } else {
                    second = f;
                    break;
                }
            }

            if (first == null || second == null) {
                return false;
            }

            yRotField = first;
            xRotField = second;

            makeWritable(yRotField);
            makeWritable(xRotField);

            return true;
        } catch (Throwable t) {
            yRotField = null;
            xRotField = null;
            return false;
        }
    }

    /**
     * Clears `final` on a field so it can be written. On Java 17 the modifiers
     * field of a class-backed Field is still writable in practice; if it is not,
     * setAccessible alone is attempted and the caller degrades gracefully.
     */
    private static void makeWritable(Field field) throws Exception {
        field.setAccessible(true);

        try {
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (Throwable ignored) {
            // Some JVMs refuse this; setAccessible above may already suffice.
        }
    }
}
