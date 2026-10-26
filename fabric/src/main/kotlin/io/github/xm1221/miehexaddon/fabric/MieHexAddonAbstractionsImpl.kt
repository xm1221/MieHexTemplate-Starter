@file:JvmName("MieHexAddonAbstractionsImpl")

package io.github.xm1221.miehexaddon.fabric

import io.github.xm1221.miehexaddon.registry.MieHexAddonRegistrar
import net.minecraft.core.Registry

fun <T : Any> initRegistry(registrar: MieHexAddonRegistrar<T>) {
    val registry = registrar.registry
    registrar.init { id, value -> Registry.register(registry, id, value) }
}
