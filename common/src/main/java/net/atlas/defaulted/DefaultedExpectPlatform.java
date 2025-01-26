package net.atlas.defaulted;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootDataType;

public interface DefaultedExpectPlatform {
    @ExpectPlatform
    static <T> LootDataType<T> createLootDataType(ResourceKey<Registry<T>> registryKey, Codec<T> codec, LootDataType.Validator<T> validator) {
        throw new AssertionError();
    }
    @ExpectPlatform
    static boolean isSyncingPlayerUnmodded() {
        throw new AssertionError();
    }
    @ExpectPlatform
    static Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry() {
        throw new AssertionError();
    }
    @ExpectPlatform
    static DataComponentType<ToolMaterialWrapper> getToolMaterialComponentType() {
        throw new AssertionError();
    }
    @ExpectPlatform
    static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }
}
