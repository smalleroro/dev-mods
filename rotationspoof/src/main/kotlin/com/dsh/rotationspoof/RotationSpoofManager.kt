package com.dsh.rotationspoof

/**
 * Holds the spoof state and produces the fake angles.
 *
 * `nextFakeYaw()` advances the spin by [degreesPerPacket] every time a rotation
 * packet goes out. Advancing per packet (rather than per client tick) keeps the
 * spin speed consistent whether the player is standing still (one Rot packet
 * per tick) or moving (one PosRot packet per tick) — in both cases exactly one
 * rotation-carrying packet is sent per tick.
 *
 * No jitter, no rate limiting, no evasion: this is for a private server where
 * anti-cheat is explicitly out of scope.
 */
object RotationSpoofManager {
    @Volatile
    var isEnabled: Boolean = false
        private set

    /** Degrees of fake yaw added per outgoing rotation packet. */
    @Volatile
    var degreesPerPacket: Float = 20.0f

    /** Fake pitch; 0 keeps the body level while it spins. */
    @Volatile
    var pitch: Float = 0.0f

    private var fakeYaw: Float = 0.0f

    fun toggle(): Boolean {
        isEnabled = !isEnabled
        return isEnabled
    }

    fun setEnabled(value: Boolean) {
        isEnabled = value
    }

    /**
     * Advances and returns the yaw to send. Wraps into [0, 360).
     * Only called from the packet path, so it stays cheap.
     */
    fun nextFakeYaw(): Float {
        fakeYaw += degreesPerPacket
        if (fakeYaw >= 360.0f) fakeYaw -= 360.0f
        else if (fakeYaw < 0.0f) fakeYaw += 360.0f
        return fakeYaw
    }

    /** Drops the accumulated yaw; called when toggled off or on disconnect. */
    fun reset() {
        fakeYaw = 0.0f
    }
}
