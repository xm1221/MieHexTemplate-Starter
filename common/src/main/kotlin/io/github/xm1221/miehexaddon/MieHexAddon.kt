package io.github.xm1221.miehexaddon

import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import io.github.xm1221.miehexaddon.config.MieHexAddonServerConfig
import io.github.xm1221.miehexaddon.networking.MieHexAddonNetworking
import io.github.xm1221.miehexaddon.registry.MieHexAddonActions

object MieHexAddon {
    const val MODID = "miehexaddon"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MODID)

    @JvmStatic
    fun id(path: String) = ResourceLocation(MODID, path)

    fun init() {
        MieHexAddonServerConfig.init()
        initRegistries(
            MieHexAddonActions,
        )
        MieHexAddonNetworking.init()
    }

    fun initServer() {
        MieHexAddonServerConfig.initServer()
    }
}
