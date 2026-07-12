package net.atlas.defaulted.neoforge;

//? neoforge {
import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultedPlatform;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.atlas.defaulted.neoforge.component.DefaultedRegistries;
import net.minecraft.core.Registry;
import net.neoforged.fml.loading.FMLEnvironment;

@SuppressWarnings("unused")
public class DefaultedPlatformNeoForge implements DefaultedPlatform {
    public Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry() {
        return DefaultedRegistries.PATCH_GENERATOR_TYPE_REG;
    }
    public Registry<MapCodec<? extends EnchantmentPatchGenerator>> getEnchantmentPatchGenRegistry() {
        return DefaultedRegistries.ENCHANTMENT_PATCH_GENERATOR_TYPE_REG;
    }
    public boolean isOnClientNetworkingThread() {
        if (!FMLEnvironment.getDist().isClient()) return false;
        return Defaulted.isOnClientNetworkingThread();
    }
}
//?}