package net.atlas.defaulted.enchantment;

import com.mojang.serialization.Codec;
import net.atlas.defaulted.DefaultedPlatform;
import net.atlas.defaulted.base.BasePatchGenerator;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentPatchGenerator extends BasePatchGenerator<EnchantmentPatchGenerator> {
	Codec<EnchantmentPatchGenerator> CODEC = DefaultedPlatform.INSTANCE.getEnchantmentPatchGenRegistry()
        .byNameCodec()
        .dispatch("generator", EnchantmentPatchGenerator::codec, mapCodec -> mapCodec);
    void patchDataComponentMap(Holder<Enchantment> enchantment, EnchantmentBuilder builder);
}
