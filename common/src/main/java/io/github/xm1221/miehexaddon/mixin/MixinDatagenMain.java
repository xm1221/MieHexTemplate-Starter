package io.github.xm1221.miehexaddon.mixin;

import io.github.xm1221.miehexaddon.MieHexAddon;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

// scuffed workaround for https://github.com/architectury/architectury-loom/issues/189
@Mixin({
    net.minecraft.data.Main.class,
    net.minecraft.server.Main.class,
})
public abstract class MixinDatagenMain {
    @WrapMethod(method = "main", remap = false)
    private static void miehexaddon$systemExitAfterDatagenFinishes(String[] strings, Operation<Void> original) {
        try {
            original.call((Object) strings);
        } catch (Throwable throwable) {
            MieHexAddon.LOGGER.error("Datagen failed!", throwable);
            System.exit(1);
        }
        MieHexAddon.LOGGER.info("Datagen succeeded, terminating.");
        System.exit(0);
    }
}
