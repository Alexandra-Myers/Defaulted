package net.atlas.defaulted.fabric;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootDataType;

@SuppressWarnings("unused")
public class DefaultedExpectPlatformImpl {
    public static <T> LootDataType<T> createLootDataType(ResourceKey<Registry<T>> registryKey, Codec<T> codec, LootDataType.Validator<T> validator) {
        return new LootDataType<>(registryKey, codec, validator);
    }
}
