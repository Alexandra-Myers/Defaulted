package net.atlas.defaulted.component;

import com.mojang.serialization.Codec;

import net.atlas.defaulted.DefaultedPlatform;
import net.atlas.defaulted.base.BasePatchGenerator;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;

public interface PatchGenerator extends BasePatchGenerator<PatchGenerator> {
	Codec<PatchGenerator> CODEC = DefaultedPlatform.INSTANCE.getPatchGenRegistry()
        .byNameCodec()
        .dispatch("generator", PatchGenerator::codec, mapCodec -> mapCodec);
    void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap);
}
