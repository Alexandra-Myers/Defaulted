package net.atlas.defaulted.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.DefaultedExpectPlatform;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentPatchGenerator {
	Codec<EnchantmentPatchGenerator> CODEC = DefaultedExpectPlatform.getEnchantmentPatchGenRegistry()
        .byNameCodec()
        .dispatch("generator", EnchantmentPatchGenerator::codec, mapCodec -> mapCodec);
    void patchDataComponentMap(Holder<Enchantment> enchantment, EnchantmentBuilder builder);
    MapCodec<? extends EnchantmentPatchGenerator> codec();
}
