package net.atlas.defaulted.fabric;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
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
}
