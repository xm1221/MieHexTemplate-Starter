package io.github.xm1221.miehexaddon.networking.msg

import dev.architectury.networking.NetworkChannel
import dev.architectury.networking.NetworkManager.PacketContext
import io.github.xm1221.miehexaddon.MieHexAddon
import io.github.xm1221.miehexaddon.networking.MieHexAddonNetworking
import io.github.xm1221.miehexaddon.networking.handler.applyOnClient
import io.github.xm1221.miehexaddon.networking.handler.applyOnServer
import net.fabricmc.api.EnvType
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import java.util.function.Supplier

sealed interface MieHexAddonMessage

sealed interface MieHexAddonMessageC2S : MieHexAddonMessage {
    fun sendToServer() {
        MieHexAddonNetworking.CHANNEL.sendToServer(this)
    }
}

sealed interface MieHexAddonMessageS2C : MieHexAddonMessage {
    fun sendToPlayer(player: ServerPlayer) {
        MieHexAddonNetworking.CHANNEL.sendToPlayer(player, this)
    }

    fun sendToPlayers(players: Iterable<ServerPlayer>) {
        MieHexAddonNetworking.CHANNEL.sendToPlayers(players, this)
    }
}

sealed interface MieHexAddonMessageCompanion<T : MieHexAddonMessage> {
    val type: Class<T>

    fun decode(buf: FriendlyByteBuf): T

    fun T.encode(buf: FriendlyByteBuf)

    fun apply(msg: T, supplier: Supplier<PacketContext>) {
        val ctx = supplier.get()
        when (ctx.env) {
            EnvType.SERVER, null -> {
                MieHexAddon.LOGGER.debug("Server received packet from {}: {}", ctx.player.name.string, this)
                when (msg) {
                    is MieHexAddonMessageC2S -> msg.applyOnServer(ctx)
                    else -> MieHexAddon.LOGGER.warn("Message not handled on server: {}", msg::class)
                }
            }
            EnvType.CLIENT -> {
                MieHexAddon.LOGGER.debug("Client received packet: {}", this)
                when (msg) {
                    is MieHexAddonMessageS2C -> msg.applyOnClient(ctx)
                    else -> MieHexAddon.LOGGER.warn("Message not handled on client: {}", msg::class)
                }
            }
        }
    }

    fun register(channel: NetworkChannel) {
        channel.register(type, { msg, buf -> msg.encode(buf) }, ::decode, ::apply)
    }
}
