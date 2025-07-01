package net.atlas.defaulted.extension;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.HashCode;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.component.*;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.HashOps;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class WrapHashedStreamCodec implements StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> {
        private final StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> original;
        private DynamicOps<HashCode> registryHashOps;

        private LoadingCache<TypedDataComponent<?>, Integer> cache = CacheBuilder.newBuilder().maximumSize(256L).build(new CacheLoader<>() {
            public Integer load(TypedDataComponent<?> typedDataComponent) {
                return typedDataComponent.encodeValue(WrapHashedStreamCodec.this.registryHashOps).getOrThrow((string) -> {
                    String var10002 = String.valueOf(typedDataComponent);
                    return new IllegalArgumentException("Failed to hash " + var10002 + ": " + string);
                }).asInt();
            }
        });

        public WrapHashedStreamCodec(StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> original) {
            this.original = original;
        }

        @Override
        public HashedStack.@NotNull ActualItem decode(RegistryFriendlyByteBuf buffer) {
            if (this.registryHashOps == null) this.registryHashOps = buffer.registryAccess().createSerializationContext(HashOps.CRC32C_INSTANCE);
            HashedStack.ActualItem stack = original.decode(buffer);
            HashedPatchMap components = stack.components();
            DataComponentMap prototype = stack.item().value().components();
            if (prototype instanceof PatchedDataComponentMap prototypeDataComponentMap) {
                DataComponentPatch.SplitResult splitResult = prototypeDataComponentMap.asPatch().split();
                Map<DataComponentType<?>, Integer> added = new IdentityHashMap<>(splitResult.added().size());
                added.putAll(components.addedComponents());
                splitResult.added().forEach((typedDataComponent) -> {
                    if (added.containsKey(typedDataComponent.type()) && added.get(typedDataComponent.type()).equals(cache.getUnchecked(typedDataComponent))) added.remove(typedDataComponent.type());
                });
                Set<DataComponentType<?>> removed = new HashSet<>(components.removedComponents());
                removed.removeAll(splitResult.removed());
                stack = new HashedStack.ActualItem(stack.item(), stack.count(), new HashedPatchMap(added, removed));
            }
            return stack;
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, HashedStack.ActualItem stack) {
            if (this.registryHashOps == null) this.registryHashOps = buffer.registryAccess().createSerializationContext(HashOps.CRC32C_INSTANCE);
            DataComponentMap prototype = stack.item().value().components();
            if (prototype instanceof PatchedDataComponentMap prototypeDataComponentMap) {
                DataComponentPatch.SplitResult splitResult = prototypeDataComponentMap.asPatch().split();
                Map<DataComponentType<?>, Integer> added = new IdentityHashMap<>(splitResult.added().size());
                splitResult.added().forEach((typedDataComponent) -> added.put(typedDataComponent.type(), cache.getUnchecked(typedDataComponent)));
                added.putAll(stack.components().addedComponents());
                Set<DataComponentType<?>> removed = new HashSet<>(splitResult.removed());
                removed.addAll(stack.components().removedComponents());
                stack = new HashedStack.ActualItem(stack.item(), stack.count(), new HashedPatchMap(added, removed));
            }
            original.encode(buffer, stack);
        }
    }