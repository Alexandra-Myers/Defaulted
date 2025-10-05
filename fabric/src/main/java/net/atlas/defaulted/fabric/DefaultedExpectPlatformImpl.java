package net.atlas.defaulted.fabric;

import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.backport.Enchantable;
import net.atlas.defaulted.component.backport.Repairable;
import net.atlas.defaulted.fabric.backport.BackportedComponents;
import net.atlas.defaulted.fabric.component.DefaultedRegistries;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;

@SuppressWarnings("unused")
public class DefaultedExpectPlatformImpl {
    public static Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry() {
        return DefaultedRegistries.PATCH_GENERATOR_TYPE_REG;
    }
    public static Registry<DataComponentType<?>> getPhantomDataComponentTypeRegistry() {
        return BackportedComponents.PHANTOM_COMPONENT_TYPE_REG;
    }
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
    public static boolean isOnClientNetworkingThread() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return false;
        return Defaulted.isOnClientNetworkingThread();
    }
    public static DataComponentType<Enchantable> getEnchantable() {
        return BackportedComponents.ENCHANTABLE;
    }
    public static DataComponentType<Repairable> getRepairable() {
        return BackportedComponents.REPAIRABLE;
    }
}
