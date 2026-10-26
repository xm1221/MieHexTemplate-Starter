package io.github.xm1221.miehexaddon.fabric

import io.github.xm1221.miehexaddon.MieHexAddonClient
import net.fabricmc.api.ClientModInitializer

object FabricMieHexAddonClient : ClientModInitializer {
    override fun onInitializeClient() {
        MieHexAddonClient.init()
    }
}
