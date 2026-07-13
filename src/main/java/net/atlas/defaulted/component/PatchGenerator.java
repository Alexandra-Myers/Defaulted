package net.atlas.defaulted.component;

import com.mojang.serialization.Codec;

import net.atlas.defaulted.base.BasePatchGenerator;
import net.atlas.defaulted.init.ItemPatchGenerators;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;

public interface PatchGenerator extends BasePatchGenerator<PatchGenerator> {
	Codec<PatchGenerator> CODEC = ItemPatchGenerators.CODEC;
    void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap);
}
