package net.atlas.defaulted;

import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Registry;
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
}
