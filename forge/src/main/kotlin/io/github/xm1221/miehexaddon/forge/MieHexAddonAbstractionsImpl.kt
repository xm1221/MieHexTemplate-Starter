@file:JvmName("MieHexAddonAbstractionsImpl")

package io.github.xm1221.miehexaddon.forge

import io.github.xm1221.miehexaddon.registry.MieHexAddonRegistrar
import net.minecraftforge.registries.RegisterEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS

fun <T : Any> initRegistry(registrar: MieHexAddonRegistrar<T>) {
    MOD_BUS.addListener { event: RegisterEvent ->
        event.register(registrar.registryKey) { helper ->
            registrar.init(helper::register)
        }
    }
}
