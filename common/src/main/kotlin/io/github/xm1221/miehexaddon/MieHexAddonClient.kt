package io.github.xm1221.miehexaddon

import io.github.xm1221.miehexaddon.config.MieHexAddonClientConfig
import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.gui.screens.Screen

object MieHexAddonClient {
    fun init() {
        MieHexAddonClientConfig.init()
    }

    fun getConfigScreen(parent: Screen): Screen {
        return AutoConfig.getConfigScreen(MieHexAddonClientConfig.GlobalConfig::class.java, parent).get()
    }
}
