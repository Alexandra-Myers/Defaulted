package net.atlas.defaulted.enchantment;

import com.mojang.serialization.Codec;
import net.atlas.defaulted.base.BasePatchGenerator;
import net.atlas.defaulted.init.EnchantmentPatchGenerators;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentPatchGenerator extends BasePatchGenerator<EnchantmentPatchGenerator> {
	Codec<EnchantmentPatchGenerator> CODEC = EnchantmentPatchGenerators.CODEC;
    void patchDataComponentMap(Holder<Enchantment> enchantment, EnchantmentBuilder builder);
}
