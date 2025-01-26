package net.atlas.defaulted.component.generators;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record WeaponStatsGenerator(List<WeaponLevelBasedValue> damage, List<WeaponLevelBasedValue> speed, Optional<ResourceLocation> damageIdOverride, Optional<ResourceLocation> speedIdOverride, List<ItemAttributeModifiers.Entry> additionalModifiers, boolean tieredDamage, boolean persistPrevious) implements PatchGenerator {
    public static final MapCodec<WeaponStatsGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(WeaponLevelBasedValue.CODEC.optionalFieldOf("attack_damage", Collections.emptyList()).forGetter(WeaponStatsGenerator::damage),
            WeaponLevelBasedValue.CODEC.optionalFieldOf("attack_speed", Collections.emptyList()).forGetter(WeaponStatsGenerator::speed),
            ResourceLocation.CODEC.optionalFieldOf("damage_id_override").forGetter(WeaponStatsGenerator::damageIdOverride),
            ResourceLocation.CODEC.optionalFieldOf("speed_id_override").forGetter(WeaponStatsGenerator::speedIdOverride),
            ItemAttributeModifiers.Entry.CODEC.listOf().optionalFieldOf("additional_modifiers", Collections.emptyList()).forGetter(WeaponStatsGenerator::additionalModifiers),
            Codec.BOOL.optionalFieldOf("apply_tier_to_damage", true).forGetter(WeaponStatsGenerator::tieredDamage),
            Codec.BOOL.fieldOf("persist_previous").forGetter(WeaponStatsGenerator::persistPrevious)).apply(instance, WeaponStatsGenerator::new));

    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        ItemAttributeModifiers oldModifiers = patchedDataComponentMap.get(DataComponents.ATTRIBUTE_MODIFIERS);
        ToolMaterialWrapper toolMaterialWrapper = item.defaulted$getToolMaterial();
        if (toolMaterialWrapper == null) toolMaterialWrapper = Defaulted.DEFAULT_WRAPPER;
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        AttributeModifier attackDamage = new AttributeModifier(damageIdOverride.orElse(Item.BASE_ATTACK_DAMAGE_ID), getTierModifier(toolMaterialWrapper, true), AttributeModifier.Operation.ADD_VALUE);
        AttributeModifier attackSpeed = new AttributeModifier(speedIdOverride.orElse(Item.BASE_ATTACK_SPEED_ID), getTierModifier(toolMaterialWrapper, false), AttributeModifier.Operation.ADD_VALUE);
        
		for (ItemAttributeModifiers.Entry entry : additionalModifiers)
			if (!(entry.matches(Attributes.ATTACK_DAMAGE, attackDamage.id()) || entry.matches(Attributes.ATTACK_SPEED, attackSpeed.id()))) builder.add(entry.attribute(), entry.modifier(), entry.slot());
        if (persistPrevious && oldModifiers != null)
            for (ItemAttributeModifiers.Entry entry : oldModifiers.modifiers())
                if (!(entry.matches(Attributes.ATTACK_DAMAGE, attackDamage.id()) || entry.matches(Attributes.ATTACK_SPEED, attackSpeed.id()))) builder.add(entry.attribute(), entry.modifier(), entry.slot());

        builder.add(Attributes.ATTACK_DAMAGE, attackDamage, EquipmentSlotGroup.MAINHAND);
        builder.add(Attributes.ATTACK_SPEED, attackSpeed, EquipmentSlotGroup.MAINHAND);
        patchedDataComponentMap.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
    
    private double getTierModifier(ToolMaterialWrapper tier, boolean forDamage) {
        if (forDamage)
            for (WeaponLevelBasedValue value : damage) {
                if (value instanceof WeaponLevelBasedValue.Unconditional unconditional) return unconditional.value() + (tieredDamage ? tier.attackDamageBonus() : 0);
                else {
                    float res = value.getResult(tier.weaponLevel(), tieredDamage);
                    if (res != 0) return res;
                }
            }
        else for (WeaponLevelBasedValue value : speed) {
            float res = value.getResult(tier.weaponLevel(), true);
            if (res != 0) return res;
        }
        return 0;
    }
    
    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
}
