@file:JvmName("MieHexAddonAbstractions")

package io.github.xm1221.miehexaddon

import dev.architectury.injectables.annotations.ExpectPlatform
import io.github.xm1221.miehexaddon.registry.MieHexAddonRegistrar

fun initRegistries(vararg registries: MieHexAddonRegistrar<*>) {
    for (registry in registries) {
        initRegistry(registry)
    }
}

@ExpectPlatform
fun <T : Any> initRegistry(registrar: MieHexAddonRegistrar<T>) {
    throw AssertionError()
}
