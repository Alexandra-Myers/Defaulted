package net.atlas.defaulted.extension;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Objects;
import java.util.function.Function;

public class LateBoundIdMapper<I, V> {
	public static <I, E> Codec<E> idResolverCodec(Codec<I> codec, Function<I, E> function, Function<E, I> function2) {
		return codec.flatXmap(object -> {
			E object2 = (E)function.apply(object);
			return object2 == null ? DataResult.error(() -> "Unknown element id: " + object) : DataResult.success(object2);
		}, object -> {
			I object2 = (I)function2.apply(object);
			return object2 == null ? DataResult.error(() -> "Element with unknown id: " + object) : DataResult.success(object2);
		});
	}
	private final BiMap<I, V> idToValue = HashBiMap.create();

	public Codec<V> codec(Codec<I> codec) {
		BiMap<V, I> biMap = this.idToValue.inverse();
		return idResolverCodec(codec, this.idToValue::get, biMap::get);
	}

	public LateBoundIdMapper<I, V> put(I object, V object2) {
		Objects.requireNonNull(object2, () -> "Value for " + object + " is null");
		this.idToValue.put(object, object2);
		return this;
	}
}