package net.atlas.defaulted.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.atlas.defaulted.mixin.DataComponentPatchAccessor;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;

import java.util.Map;
import java.util.Optional;

import static net.minecraft.core.component.DataComponentPatch.EMPTY;

public class DataComponentPatchUtils {
    public static Codec<DataComponentPatch> codec(Registry<DataComponentType<?>> registry) {
        return Codec.dispatchedMap(PatchKey.codec(registry), PatchKey::valueCodec).xmap((data) -> {
            if (data.isEmpty()) {
                return EMPTY;
            } else {
                Reference2ObjectMap<DataComponentType<?>, Optional<?>> map = new Reference2ObjectArrayMap<>(data.size());

                for(Map.Entry<PatchKey, ?> entry : data.entrySet()) {
                    PatchKey key = entry.getKey();
                    if (key.removed()) {
                        map.put(key.type(), Optional.empty());
                    } else {
                        map.put(key.type(), Optional.of(entry.getValue()));
                    }
                }

                return DataComponentPatchAccessor.create(map);
            }
        }, (patch) -> {
            Reference2ObjectMap<PatchKey, Object> map = new Reference2ObjectArrayMap<>(getMap(patch).size());

            for (Reference2ObjectMap.Entry<DataComponentType<?>, Optional<?>> entry : Reference2ObjectMaps.fastIterable(getMap(patch))) {
                DataComponentType<?> type = entry.getKey();
                if (!type.isTransient()) {
                    Optional<?> value = entry.getValue();
                    if (value.isPresent()) {
                        map.put(new PatchKey(type, false), value.get());
                    } else {
                        map.put(new PatchKey(type, true), Unit.INSTANCE);
                    }
                }
            }

            //noinspection rawtypes,unchecked
            return (Map) map;
        });
    }
    public static Reference2ObjectMap<DataComponentType<?>, Optional<?>> getMap(DataComponentPatch patch) {
        return ((DataComponentPatchAccessor) (Object) patch).getMap();
    }
    private record PatchKey(DataComponentType<?> type, boolean removed) {
        public static Codec<PatchKey> codec(Registry<DataComponentType<?>> registry) {
            return Codec.STRING.flatXmap((string) -> {
                boolean removed = string.startsWith("!");
                if (removed) {
                    string = string.substring("!".length());
                }

                ResourceLocation id = ResourceLocation.tryParse(string);
                DataComponentType<?> type = registry.get(id);
                if (type == null) {
                    return DataResult.error(() -> "No component with type: '" + id + "'");
                } else {
                    return type.isTransient() ? DataResult.error(() -> "'" + id + "' is not a persistent component") : DataResult.success(new PatchKey(type, removed));
                }
            }, (key) -> {
                DataComponentType<?> type = key.type();
                ResourceLocation id = registry.getKey(type);
                return id == null ? DataResult.error(() -> "Unregistered component: " + type) : DataResult.success(key.removed() ? "!" + id : id.toString());
            });
        }

        public Codec<?> valueCodec() {
            return this.removed ? Codec.EMPTY.codec() : this.type.codecOrThrow();
        }
    }
}
