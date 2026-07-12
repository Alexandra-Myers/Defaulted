package net.atlas.defaulted.fabric;

//? fabric {
import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultedPlatform;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.atlas.defaulted.fabric.component.DefaultedRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;

@SuppressWarnings("unused")
public class DefaultedPlatformFabric implements DefaultedPlatform {
    public Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry() {
        return DefaultedRegistries.PATCH_GENERATOR_TYPE_REG;
    }
    public Registry<MapCodec<? extends EnchantmentPatchGenerator>> getEnchantmentPatchGenRegistry() {
        return DefaultedRegistries.ENCHANTMENT_PATCH_GENERATOR_TYPE_REG;
    }
    public boolean isOnClientNetworkingThread() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return false;
        return Defaulted.isOnClientNetworkingThread();
    }
}
//?}