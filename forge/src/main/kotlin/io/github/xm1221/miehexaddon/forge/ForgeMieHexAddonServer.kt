package io.github.xm1221.miehexaddon.forge

import io.github.xm1221.miehexaddon.MieHexAddon
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent

object ForgeMieHexAddonServer {
    @Suppress("UNUSED_PARAMETER")
    fun init(event: FMLDedicatedServerSetupEvent) {
        MieHexAddon.initServer()
    }
}
