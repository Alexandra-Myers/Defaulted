package net.atlas.defaulted.neoforge;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

@SuppressWarnings("unused")
public class DefaultedExpectPlatformImpl {
    public static <T> LootDataType<T> createLootDataType(ResourceKey<Registry<T>> registryKey, Codec<T> codec, LootDataType.Validator<T> validator) {
        return new LootDataType<>(registryKey, codec, validator, null, ConditionalOps.createConditionalCodec(codec), (it, id) -> {});
    }
}
