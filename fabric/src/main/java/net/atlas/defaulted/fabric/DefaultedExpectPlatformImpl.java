package net.atlas.defaulted.fabric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.atlas.defaulted.fabric.component.DefaultedDataComponents;
import net.atlas.defaulted.fabric.component.DefaultedRegistries;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootDataType;
import xyz.nucleoid.packettweaker.PacketContext;

@SuppressWarnings("unused")
public class DefaultedExpectPlatformImpl {
    public static <T> LootDataType<T> createLootDataType(ResourceKey<Registry<T>> registryKey, Codec<T> codec, LootDataType.Validator<T> validator) {
        return new LootDataType<>(registryKey, codec, validator);
    }
    public static boolean isSyncingPlayerUnmodded() {
        PacketContext context = PacketContext.get();
        return context != null && context.getPlayer() != null && DefaultedFabric.unmoddedPlayers.contains(context.getPlayer().getUUID());
    }
    public static Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry() {
        return DefaultedRegistries.PATCH_GENERATOR_TYPE_REG;
    }
    public static DataComponentType<ToolMaterialWrapper> getToolMaterialComponentType() {
        return DefaultedDataComponents.TOOL_MATERIAL;
    }
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
