package net.atlas.defaulted.fabric;

import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.fabric.compat.OwoCompat;
import net.atlas.defaulted.fabric.component.DefaultedRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import xyz.nucleoid.packettweaker.PacketContext;

@SuppressWarnings("unused")
public class DefaultedExpectPlatformImpl {
    public static boolean isSyncingPlayerUnmodded() {
        PacketContext context = PacketContext.get();
        return context != null && context.getPlayer() != null && DefaultedFabric.unmoddedPlayers.contains(context.getPlayer().getUUID());
    }
    public static Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry() {
        return DefaultedRegistries.PATCH_GENERATOR_TYPE_REG;
    }
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
    public static DataComponentMap createDerivedMap(ItemStack itemStack, DataComponentMap prototype) {
        return OwoCompat.deriveComponentMap(itemStack, prototype);
    }
}
