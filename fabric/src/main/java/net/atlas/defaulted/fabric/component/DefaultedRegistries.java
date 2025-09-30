package net.atlas.defaulted.fabric.component;

import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.generators.*;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class DefaultedRegistries {
	public static final Registry<MapCodec<? extends PatchGenerator>> PATCH_GENERATOR_TYPE_REG = FabricRegistryBuilder.createSimple(
		Defaulted.PATCH_GENERATOR_TYPE
	).attribute(RegistryAttribute.OPTIONAL).buildAndRegister();

    public static void registerPatchGenerator(String path, MapCodec<? extends PatchGenerator> mapCodec) {
        Registry.register(PATCH_GENERATOR_TYPE_REG, ResourceKey.create(Defaulted.PATCH_GENERATOR_TYPE, Defaulted.id(path)), mapCodec);
    }

    public static void init() {
        registerPatchGenerator("armor_stats", ArmourStatsGenerator.CODEC);
        registerPatchGenerator("conditional", ConditionalPatch.CODEC);
        registerPatchGenerator("modify_attribute_modifiers", AttributeModifiersGenerator.CODEC);
        registerPatchGenerator("modify_blocks_attacks", BlocksAttacksGenerator.CODEC);
        registerPatchGenerator("modify_enchantments", EnchantmentModifierGenerator.CODEC);
        registerPatchGenerator("modify_from_tool_material", ModifyTierStatsGenerator.CODEC);
        registerPatchGenerator("modify_use_seconds", EditUseDurationGenerator.CODEC);
        registerPatchGenerator("tool_material", ChangeTierGenerator.CODEC);
        registerPatchGenerator("vanilla_weapon_stats", WeaponStatsGenerator.CODEC);
    }
}
