package net.atlas.defaulted.neoforge;

import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.neoforge.component.DefaultedRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;

@SuppressWarnings("unused")
public class DefaultedExpectPlatformImpl {
    public static Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry() {
        return DefaultedRegistries.PATCH_GENERATOR_TYPE_REG;
    }
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
    public static boolean isOnClientNetworkingThread() {
        if (!FMLEnvironment.dist.isClient()) return false;
        return Defaulted.isOnClientNetworkingThread();
    }
    public static DataComponentMap createDerivedMap(ItemStack itemStack, DataComponentMap prototype) {
        return prototype;
    }
}
