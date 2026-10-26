package io.github.xm1221.miehexaddon.forge

import dev.architectury.platform.forge.EventBuses
import io.github.xm1221.miehexaddon.MieHexAddon
import io.github.xm1221.miehexaddon.forge.datagen.ForgeMieHexAddonDatagen
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(MieHexAddon.MODID)
class ForgeMieHexAddon {
    init {
        MOD_BUS.apply {
            EventBuses.registerModEventBus(MieHexAddon.MODID, this)
            addListener(ForgeMieHexAddonClient::init)
            addListener(ForgeMieHexAddonDatagen::init)
            addListener(ForgeMieHexAddonServer::init)
        }
        MieHexAddon.init()
    }
}
