package io.github.xm1221.miehexaddon.fabric

import io.github.xm1221.miehexaddon.MieHexAddon
import net.fabricmc.api.ModInitializer

object FabricMieHexAddon : ModInitializer {
    override fun onInitialize() {
        MieHexAddon.init()
    }
}
