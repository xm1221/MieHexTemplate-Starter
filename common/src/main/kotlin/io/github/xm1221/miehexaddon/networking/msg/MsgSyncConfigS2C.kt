package io.github.xm1221.miehexaddon.networking.msg

import io.github.xm1221.miehexaddon.config.MieHexAddonServerConfig
import net.minecraft.network.FriendlyByteBuf

data class MsgSyncConfigS2C(val serverConfig: MieHexAddonServerConfig.ServerConfig) : MieHexAddonMessageS2C {
    companion object : MieHexAddonMessageCompanion<MsgSyncConfigS2C> {
        override val type = MsgSyncConfigS2C::class.java

        override fun decode(buf: FriendlyByteBuf) = MsgSyncConfigS2C(
            serverConfig = MieHexAddonServerConfig.ServerConfig().decode(buf),
        )

        override fun MsgSyncConfigS2C.encode(buf: FriendlyByteBuf) {
            serverConfig.encode(buf)
        }
    }
}
