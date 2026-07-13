package net.atlas.defaulted.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.atlas.defaulted.enchantment.generators.AddEffectGenerator;
import net.atlas.defaulted.enchantment.generators.ConditionalPatch;
import net.atlas.defaulted.utils.Bootstrapper;

public class EnchantmentPatchGenerators extends Bootstrapper<MapCodec<? extends EnchantmentPatchGenerator>> {
    public static final EnchantmentPatchGenerators INSTANCE = new EnchantmentPatchGenerators();
    public static final Codec<EnchantmentPatchGenerator> CODEC = INSTANCE.getRegistry().byNameCodec()
            .dispatch("generator", EnchantmentPatchGenerator::codec, mapCodec -> mapCodec);

    public EnchantmentPatchGenerators() {
        super(Defaulted.ENCHANTMENT_PATCH_GENERATOR_TYPE, "defaulted");
    }
    @Override
    protected void bootstrap(Registrar<MapCodec<? extends EnchantmentPatchGenerator>> registrar) {
        registrar.register("conditional", () -> ConditionalPatch.CODEC);
        registrar.register("modify_list_effect", () -> AddEffectGenerator.CODEC);
    }
}
