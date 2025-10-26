package net.atlas.defaulted.component;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.Defaulted;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public record ToolMaterialWrapper(ToolMaterial toolMaterial, int weaponLevel, int speedLevel) {
	public static Codec<ToolMaterial> TOOL_MATERIAL_CODEC = Codec.STRING.validate(s -> Defaulted.baseTiers.containsKey(s) ? DataResult.success(s) : DataResult.error(() -> "Given base tier does not exist!")).xmap(s -> Defaulted.baseTiers.get(s.toLowerCase()), Defaulted.baseTiers.inverse()::get);
	public static MapCodec<ToolMaterialWrapper> BASE_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weapon_level").forGetter(ToolMaterialWrapper::weaponLevel),
						ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("speed_level").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.speedLevel())),
				ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("enchant_level").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.enchantmentValue())),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("uses").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.durability())),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("attack_damage_bonus").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.attackDamageBonus())),
				ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("mining_speed").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.speed())),
				TagKey.codec(Registries.ITEM).optionalFieldOf("repair_items").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.repairItems())),
				TagKey.codec(Registries.BLOCK).optionalFieldOf("incorrect_blocks").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.incorrectBlocksForDrops())),
				TOOL_MATERIAL_CODEC.fieldOf("base_tier").forGetter(ToolMaterialWrapper::toolMaterial))
			.apply(instance, ToolMaterialWrapper::create));

	public static MapCodec<ToolMaterialWrapper> FULL_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weapon_level").forGetter(ToolMaterialWrapper::weaponLevel),
						ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("speed_level").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.speedLevel())),
				ExtraCodecs.NON_NEGATIVE_INT.fieldOf("enchant_level").forGetter(ToolMaterialWrapper::enchantmentValue),
				ExtraCodecs.POSITIVE_INT.fieldOf("uses").forGetter(ToolMaterialWrapper::durability),
				ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("attack_damage_bonus").forGetter(ToolMaterialWrapper::attackDamageBonus),
				ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("mining_speed").forGetter(ToolMaterialWrapper::speed),
				TagKey.codec(Registries.ITEM).fieldOf("repair_items").forGetter(ToolMaterialWrapper::repairItems),
				TagKey.codec(Registries.BLOCK).fieldOf("incorrect_blocks").forGetter(ToolMaterialWrapper::incorrectBlocksForDrops))
			.apply(instance, ToolMaterialWrapper::create));

	public static MapCodec<ToolMaterialWrapper> CODEC = Codec.mapEither(FULL_CODEC, BASE_CODEC).xmap(
            Either::unwrap,
            Either::left
        );
	public static ToolMaterialWrapper create(Integer weaponLevel, Optional<Integer> speedLevel, Optional<Integer> enchantLevel, Optional<Integer> uses, Optional<Float> damage, Optional<Float> speed, Optional<TagKey<Item>> repairItems, Optional<TagKey<Block>> incorrect, ToolMaterial baseTier) {
		return create(weaponLevel, speedLevel, enchantLevel.orElse(baseTier.enchantmentValue()), uses.orElse(baseTier.durability()), damage.orElse(baseTier.attackDamageBonus()), speed.orElse(baseTier.speed()), repairItems.orElse(baseTier.repairItems()), incorrect.orElse(baseTier.incorrectBlocksForDrops()));
	}
	public static ToolMaterialWrapper create(int weaponLevel, Optional<Integer> speedLevel, int enchantLevel, int uses, float damage, float speed, TagKey<Item> repairItems, TagKey<Block> incorrect) {
		return new ToolMaterialWrapper(new ToolMaterial(incorrect, uses, speed, damage, enchantLevel, repairItems), weaponLevel, speedLevel.orElse(weaponLevel));
	}

	public TagKey<Block> incorrectBlocksForDrops() {
		return toolMaterial.incorrectBlocksForDrops();
	}

	public int durability() {
		return toolMaterial.durability();
	}

	public float speed() {
		return toolMaterial.speed();
	}

	public float attackDamageBonus() {
		return toolMaterial.attackDamageBonus();
	}

	public int enchantmentValue() {
		return toolMaterial.enchantmentValue();
	}

	public TagKey<Item> repairItems() {
		return toolMaterial.repairItems();
	}
}