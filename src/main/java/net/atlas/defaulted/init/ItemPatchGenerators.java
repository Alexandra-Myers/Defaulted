package net.atlas.defaulted.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.generators.*;
import net.atlas.defaulted.init.registry.Bootstrapper;

public class ItemPatchGenerators extends Bootstrapper<MapCodec<? extends PatchGenerator>> {
    public static final ItemPatchGenerators INSTANCE = new ItemPatchGenerators();
    public static final Codec<PatchGenerator> CODEC = INSTANCE.getRegistry().byNameCodec()
            .dispatch("generator", PatchGenerator::codec, mapCodec -> mapCodec);

    public ItemPatchGenerators() {
        super(Defaulted.PATCH_GENERATOR_TYPE, "defaulted");
    }
    @Override
    protected void bootstrap(Registrar<MapCodec<? extends PatchGenerator>> registrar) {
        registrar.register("armor_stats", () -> ArmourStatsGenerator.CODEC);
        registrar.register("conditional", () -> ConditionalPatch.CODEC);
        registrar.register("modify_attribute_modifiers", () -> AttributeModifiersGenerator.CODEC);
        registrar.register("modify_blocks_attacks", () -> BlocksAttacksGenerator.CODEC);
        registrar.register("modify_enchantments", () -> EnchantmentModifierGenerator.CODEC);
        registrar.register("modify_from_tool_material", () -> ModifyTierStatsGenerator.CODEC);
        registrar.register("modify_use_seconds", () -> EditUseDurationGenerator.CODEC);
        registrar.register("tool_material", () -> ChangeTierGenerator.CODEC);
        registrar.register("vanilla_weapon_stats", () -> WeaponStatsGenerator.CODEC);
    }
}
