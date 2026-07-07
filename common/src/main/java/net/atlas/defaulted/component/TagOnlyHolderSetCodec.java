package net.atlas.defaulted.component;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.Optional;
import java.util.function.Function;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public class TagOnlyHolderSetCodec<E> implements Codec<HolderSet<E>> {
	private final ResourceKey<? extends Registry<E>> registryKey;
	private final Codec<TagKey<E>> codec;

	public static <E> Codec<HolderSet<E>> create(
		final ResourceKey<? extends Registry<E>> registryKey
	) {
		return new TagOnlyHolderSetCodec<>(registryKey);
	}

	private TagOnlyHolderSetCodec(final ResourceKey<? extends Registry<E>> registryKey) {
		this.registryKey = registryKey;
		this.codec = TagKey.codec(registryKey);
	}

	@Override
	public <T> DataResult<Pair<HolderSet<E>, T>> decode(final DynamicOps<T> ops, final T input) {
		if (ops instanceof RegistryOps<T> registryOps) {
			Optional<HolderGetter<E>> registryOptional = registryOps.getter(this.registryKey);
			if (registryOptional.isPresent()) {
				HolderGetter<E> registry = registryOptional.get();
				return this.codec.decode(ops, input).flatMap(p -> {
					DataResult<HolderSet<E>> result = lookupTag(registry, p.getFirst());
					return result.map(holders -> Pair.of(holders, p.getSecond()));
				});
			}
		}

		return DataResult.error(() -> "Can't decode element " + input + " without registry");
	}

	private static <E> DataResult<HolderSet<E>> lookupTag(final HolderGetter<E> registry, final TagKey<E> key) {
		return registry.get(key)
				.map(DataResult::success)
				.orElseGet(() -> DataResult.error(() -> "Missing tag: '" + key.location() + "' in '" + key.registry().identifier() + "'"))
				.map(Function.identity());
	}

	public <T> DataResult<T> encode(final HolderSet<E> input, final DynamicOps<T> ops, final T prefix) {
		if (ops instanceof RegistryOps<T> registryOps) {
			Optional<HolderOwner<E>> maybeOwner = registryOps.owner(this.registryKey);
			if (maybeOwner.isPresent()) {
				if (!input.canSerializeIn(maybeOwner.get())) {
					return DataResult.error(() -> "HolderSet " + input + " is not valid in current registry set");
				}

				return this.codec.encode(input.unwrap().left().orElseThrow(), ops, prefix);
			}
		}

		return DataResult.error(() -> "Can't encode element " + input + " without registry");
	}
}
