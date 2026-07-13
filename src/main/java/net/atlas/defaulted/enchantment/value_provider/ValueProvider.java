package net.atlas.defaulted.enchantment.value_provider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Optional;

public interface ValueProvider {
    Codec<ValueProvider> CODEC = ValueProviders.CODEC;
    double extractValue(Optional<Integer> input);
    default int roundedValue(Optional<Integer> value) {
        return Math.toIntExact(Math.round(extractValue(value)));
    }
    MapCodec<? extends ValueProvider> type();
}