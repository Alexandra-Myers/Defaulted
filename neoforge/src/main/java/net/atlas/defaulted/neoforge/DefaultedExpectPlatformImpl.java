package net.atlas.defaulted.neoforge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.atlas.defaulted.neoforge.component.DefaultedDataComponents;
import net.atlas.defaulted.neoforge.component.DefaultedRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

@SuppressWarnings("unused")
public class DefaultedExpectPlatformImpl {
    public static <T> LootDataType<T> createLootDataType(ResourceKey<Registry<T>> registryKey, Codec<T> codec, LootDataType.Validator<T> validator) {
        return new LootDataType<>(registryKey, codec, validator, null, ConditionalOps.createConditionalCodec(codec), (it, id) -> {});
    }
    public static boolean isSyncingPlayerUnmodded() {
        return false;
    }
    public static Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry() {
        return DefaultedRegistries.PATCH_GENERATOR_TYPE_REG;
    }
    public static DataComponentType<ToolMaterialWrapper> getToolMaterialComponentType() {
        return DefaultedDataComponents.TOOL_MATERIAL.get();
    }
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
