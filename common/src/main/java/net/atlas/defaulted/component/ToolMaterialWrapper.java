package net.atlas.defaulted.component;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.Defaulted;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public record ToolMaterialWrapper(TagKey<Block> incorrectBlocksForDrops, int uses, float speed, float attackDamageBonus, int enchantmentValue, TagKey<Item> repairItems, int weaponLevel, int speedLevel) implements Tier {
	public static Codec<Tier> TOOL_MATERIAL_CODEC = Codec.STRING.validate(s -> Defaulted.baseTiers.containsKey(s) ? DataResult.success(s) : DataResult.error(() -> "Given base tier does not exist!")).xmap(s -> Defaulted.baseTiers.get(s.toLowerCase()), Defaulted.baseTiers.inverse()::get);
	public static MapCodec<ToolMaterialWrapper> BASE_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weapon_level").forGetter(ToolMaterialWrapper::weaponLevel),
				ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("speed_level").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.speedLevel())),
				ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("enchant_level").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.enchantmentValue())),
				ExtraCodecs.POSITIVE_INT.optionalFieldOf("uses").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.uses())),
				Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("attack_damage_bonus").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.attackDamageBonus())),
				Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("mining_speed").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.speed())),
				TagKey.codec(Registries.ITEM).optionalFieldOf("repair_items").forGetter(toolMaterialWrapper -> Optional.ofNullable(toolMaterialWrapper.repairItems())),
				TagKey.codec(Registries.BLOCK).optionalFieldOf("incorrect_blocks").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.incorrectBlocksForDrops())),
				TOOL_MATERIAL_CODEC.fieldOf("base_tier").forGetter(ToolMaterialWrapper::asTier))
			.apply(instance, ToolMaterialWrapper::create));

	public static MapCodec<ToolMaterialWrapper> FULL_CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weapon_level").forGetter(ToolMaterialWrapper::weaponLevel),
				ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("speed_level").forGetter(toolMaterialWrapper -> Optional.of(toolMaterialWrapper.speedLevel())),
				ExtraCodecs.NON_NEGATIVE_INT.fieldOf("enchant_level").forGetter(ToolMaterialWrapper::enchantmentValue),
				ExtraCodecs.POSITIVE_INT.fieldOf("uses").forGetter(ToolMaterialWrapper::uses),
				Codec.floatRange(0, Float.MAX_VALUE).fieldOf("attack_damage_bonus").forGetter(ToolMaterialWrapper::attackDamageBonus),
				Codec.floatRange(0, Float.MAX_VALUE).fieldOf("mining_speed").forGetter(ToolMaterialWrapper::speed),
				TagKey.codec(Registries.ITEM).fieldOf("repair_items").forGetter(ToolMaterialWrapper::repairItems),
				TagKey.codec(Registries.BLOCK).fieldOf("incorrect_blocks").forGetter(ToolMaterialWrapper::incorrectBlocksForDrops))
			.apply(instance, ToolMaterialWrapper::create));

	public static MapCodec<ToolMaterialWrapper> CODEC = Codec.mapEither(FULL_CODEC, BASE_CODEC).xmap(
            Either::unwrap,
            Either::left
        );
	public Tier asTier() {
		return this;
	}
	public ToolMaterialWrapper(Tier tier, int weaponLevel, int speedLevel) {
		this(tier.getIncorrectBlocksForDrops(), tier.getUses(), tier.getSpeed(), tier.getAttackDamageBonus(), tier.getEnchantmentValue(), null, weaponLevel, speedLevel);
	}
	public static ToolMaterialWrapper create(Integer weaponLevel, Optional<Integer> speedLevel, Optional<Integer> enchantLevel, Optional<Integer> uses, Optional<Float> damage, Optional<Float> speed, Optional<TagKey<Item>> repairItems, Optional<TagKey<Block>> incorrect, Tier baseTier) {
		return create(weaponLevel, speedLevel, enchantLevel.orElse(baseTier.getEnchantmentValue()), uses.orElse(baseTier.getUses()), damage.orElse(baseTier.getAttackDamageBonus()), speed.orElse(baseTier.getSpeed()), repairItems.orElse(null), incorrect.orElse(baseTier.getIncorrectBlocksForDrops()));
	}
	public static ToolMaterialWrapper create(int weaponLevel, Optional<Integer> speedLevel, int enchantLevel, int uses, float damage, float speed, TagKey<Item> repairItems, TagKey<Block> incorrect) {
		return new ToolMaterialWrapper(incorrect, uses, speed, damage, enchantLevel, repairItems, weaponLevel, speedLevel.orElse(weaponLevel));
	}

	@Override
	public int getUses() {
		return uses();
	}

	@Override
	public float getSpeed() {
		return speed();
	}

	@Override
	public float getAttackDamageBonus() {
		return attackDamageBonus();
	}

	@Override
	public TagKey<Block> getIncorrectBlocksForDrops() {
		return incorrectBlocksForDrops();
	}

	@Override
	public int getEnchantmentValue() {
		return enchantmentValue();
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.of(repairItems());
	}

	@Override
	public TagKey<Item> repairItems() {
		if (repairItems == null) return ItemTags.PLANKS;
		return repairItems;
	}
}