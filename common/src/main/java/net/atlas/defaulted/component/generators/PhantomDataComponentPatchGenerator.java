package net.atlas.defaulted.component.generators;

import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.DefaultedExpectPlatform;
import net.atlas.defaulted.component.PatchGenerator;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;

public record PhantomDataComponentPatchGenerator(DataComponentMap map) implements PatchGenerator {
    public static final MapCodec<PhantomDataComponentPatchGenerator> CODEC = DataComponentMap.makeCodec(DefaultedExpectPlatform.getPhantomDataComponentTypeRegistry().byNameCodec()).fieldOf("patch").xmap(PhantomDataComponentPatchGenerator::new, PhantomDataComponentPatchGenerator::map);
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        item.defaulted$getPhantomComponentMap().setAll(map);
    }

    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
}
