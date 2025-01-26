package net.atlas.defaulted.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.DefaultedExpectPlatform;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;

public interface PatchGenerator {
	Codec<PatchGenerator> CODEC = DefaultedExpectPlatform.getPatchGenRegistry()
        .byNameCodec()
        .dispatch(PatchGenerator::codec, mapCodec -> mapCodec);
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap);
    public MapCodec<? extends PatchGenerator> codec();
}
