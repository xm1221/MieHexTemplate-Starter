package io.github.xm1221.miehexaddon.fabric.datagen

import io.github.xm1221.miehexaddon.datagen.MieHexAddonActionTags
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object FabricMieHexAddonDatagen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(gen: FabricDataGenerator) {
        val pack = gen.createPack()

        pack.addProvider(::MieHexAddonActionTags)
    }
}
