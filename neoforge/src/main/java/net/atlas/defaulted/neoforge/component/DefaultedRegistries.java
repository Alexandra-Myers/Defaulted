package net.atlas.defaulted.neoforge.component;

import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.generators.ArmourStatsGenerator;
import net.atlas.defaulted.component.generators.AttributeModifiersGenerator;
import net.atlas.defaulted.component.generators.ChangeTierGenerator;
import net.atlas.defaulted.component.generators.ConditionalPatch;
import net.atlas.defaulted.component.generators.EditUseDurationGenerator;
import net.atlas.defaulted.component.generators.EnchantmentModifierGenerator;
import net.atlas.defaulted.component.generators.ModifyTierStatsGenerator;
import net.atlas.defaulted.component.generators.WeaponStatsGenerator;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DefaultedRegistries {
    private static final DeferredRegister<MapCodec<? extends PatchGenerator>> PATCH_GENERATOR_TYPES = DeferredRegister.create(Defaulted.PATCH_GENERATOR_TYPE, "defaulted");

    public static final Registry<MapCodec<? extends PatchGenerator>> PATCH_GENERATOR_TYPE_REG =
            PATCH_GENERATOR_TYPES.makeRegistry(builder -> builder.sync(false));

    public static void init(IEventBus modBus) {
        PATCH_GENERATOR_TYPES.register("armor_stats", () -> ArmourStatsGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("conditional", () -> ConditionalPatch.CODEC);
        PATCH_GENERATOR_TYPES.register("modify_attribute_modifiers", () -> AttributeModifiersGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("modify_enchantments", () -> EnchantmentModifierGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("modify_from_tool_material", () -> ModifyTierStatsGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("modify_use_seconds", () -> EditUseDurationGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("tool_material", () -> ChangeTierGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register("vanilla_weapon_stats", () -> WeaponStatsGenerator.CODEC);
        PATCH_GENERATOR_TYPES.register(modBus);
    }
}
