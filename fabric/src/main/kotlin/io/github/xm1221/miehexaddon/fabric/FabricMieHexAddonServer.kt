package io.github.xm1221.miehexaddon.fabric

import io.github.xm1221.miehexaddon.MieHexAddon
import net.fabricmc.api.DedicatedServerModInitializer

object FabricMieHexAddonServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        MieHexAddon.initServer()
    }
}
