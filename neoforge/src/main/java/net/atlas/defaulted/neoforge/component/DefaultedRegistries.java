package net.atlas.defaulted.neoforge.component;

import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.generators.*;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.atlas.defaulted.enchantment.generators.AddEffectGenerator;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DefaultedRegistries {
    private static final DeferredRegister<MapCodec<? extends PatchGenerator>> PATCH_GENERATOR_TYPES = DeferredRegister.create(Defaulted.PATCH_GENERATOR_TYPE, "defaulted");

    public static final Registry<MapCodec<? extends PatchGenerator>> PATCH_GENERATOR_TYPE_REG =
            PATCH_GENERATOR_TYPES.makeRegistry(builder -> builder.sync(false));
    private static final DeferredRegister<MapCodec<? extends EnchantmentPatchGenerator>> ENCHANTMENT_PATCH_GENERATOR_TYPES = DeferredRegister.create(Defaulted.ENCHANTMENT_PATCH_GENERATOR_TYPE, "defaulted");

    public static final Registry<MapCodec<? extends EnchantmentPatchGenerator>> ENCHANTMENT_PATCH_GENERATOR_TYPE_REG =
            ENCHANTMENT_PATCH_GENERATOR_TYPES.makeRegistry(builder -> builder.sync(false));

    public static void init(IEventBus modBus) {
        PATCH_GENERATOR_TYPES.register("armor_stats", () -> ArmourStatsGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("conditional", () -> ConditionalPatch.CODEC);
        PATCH_GENERATOR_TYPES.register("modify_attribute_modifiers", () -> AttributeModifiersGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("modify_blocks_attacks", () -> BlocksAttacksGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("modify_enchantments", () -> EnchantmentModifierGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("modify_from_tool_material", () -> ModifyTierStatsGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("modify_use_seconds", () -> EditUseDurationGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("tool_material", () -> ChangeTierGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("vanilla_weapon_stats", () -> WeaponStatsGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register(modBus);
        ENCHANTMENT_PATCH_GENERATOR_TYPES.register("conditional", () -> net.atlas.defaulted.enchantment.generators.ConditionalPatch.CODEC);
        ENCHANTMENT_PATCH_GENERATOR_TYPES.register("modify_list_effect", () -> AddEffectGenerator.CODEC);
        ENCHANTMENT_PATCH_GENERATOR_TYPES.register(modBus);
    }
}
