package io.github.xm1221.miehexaddon.networking

import dev.architectury.networking.NetworkChannel
import io.github.xm1221.miehexaddon.MieHexAddon
import io.github.xm1221.miehexaddon.networking.msg.MieHexAddonMessageCompanion

object MieHexAddonNetworking {
    val CHANNEL: NetworkChannel = NetworkChannel.create(MieHexAddon.id("networking_channel"))

    fun init() {
        for (subclass in MieHexAddonMessageCompanion::class.sealedSubclasses) {
            subclass.objectInstance?.register(CHANNEL)
        }
    }
}
