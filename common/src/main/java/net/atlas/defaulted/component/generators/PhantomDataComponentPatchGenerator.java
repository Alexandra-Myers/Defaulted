package net.atlas.defaulted.component.generators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import net.atlas.defaulted.DefaultedExpectPlatform;
import net.atlas.defaulted.component.PatchGenerator;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.item.Item;

import java.util.Map;

public record PhantomDataComponentPatchGenerator(DataComponentMap map) implements PatchGenerator {
    static Codec<DataComponentType<?>> PHANTOM_DATA_COMPONENT_CODEC = Codec.lazyInitialized(() -> DefaultedExpectPlatform.getPhantomDataComponentTypeRegistry().byNameCodec());
    static Codec<DataComponentType<?>> PERSISTENT_CODEC = PHANTOM_DATA_COMPONENT_CODEC.validate((dataComponentType) -> dataComponentType.isTransient() ? DataResult.error(() -> "Encountered transient component " + DefaultedExpectPlatform.getPhantomDataComponentTypeRegistry().getKey(dataComponentType)) : DataResult.success(dataComponentType));
    static Codec<Map<DataComponentType<?>, Object>> VALUE_MAP_CODEC = Codec.dispatchedMap(PERSISTENT_CODEC, DataComponentType::codecOrThrow);
    public static final MapCodec<PhantomDataComponentPatchGenerator> CODEC = VALUE_MAP_CODEC.flatComapMap(DataComponentMap.Builder::buildFromMapTrusted, (dataComponentMap) -> {
        int i = dataComponentMap.size();
        if (i == 0) {
            return DataResult.success(Reference2ObjectMaps.emptyMap());
        } else {
            Reference2ObjectMap<DataComponentType<?>, Object> reference2ObjectMap = new Reference2ObjectArrayMap(i);

            for(TypedDataComponent<?> typedDataComponent : dataComponentMap) {
                if (!typedDataComponent.type().isTransient()) {
                    reference2ObjectMap.put(typedDataComponent.type(), typedDataComponent.value());
                }
            }

            return DataResult.success(reference2ObjectMap);
        }
    }).fieldOf("patch").xmap(PhantomDataComponentPatchGenerator::new, PhantomDataComponentPatchGenerator::map);
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        item.defaulted$getPhantomComponentMap().setAll(map);
    }

    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
}
