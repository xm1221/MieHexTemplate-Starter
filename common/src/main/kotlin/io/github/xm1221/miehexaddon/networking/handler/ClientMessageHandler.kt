package io.github.xm1221.miehexaddon.networking.handler

import dev.architectury.networking.NetworkManager.PacketContext
import io.github.xm1221.miehexaddon.config.MieHexAddonServerConfig
import io.github.xm1221.miehexaddon.networking.msg.*

fun MieHexAddonMessageS2C.applyOnClient(ctx: PacketContext) = ctx.queue {
    when (this) {
        is MsgSyncConfigS2C -> {
            MieHexAddonServerConfig.onSyncConfig(serverConfig)
        }

        // add more client-side message handlers here
    }
}
