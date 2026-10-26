package io.github.xm1221.miehexaddon.fabric

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import io.github.xm1221.miehexaddon.MieHexAddonClient

object FabricMieHexAddonModMenu : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory(MieHexAddonClient::getConfigScreen)
}
