package net.atlas.defaulted.component.generators;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record WeaponStatsGenerator(List<WeaponLevelBasedValue> damage, List<WeaponLevelBasedValue> speed, Optional<UUID> damageIdOverride, Optional<UUID> speedIdOverride, List<ItemAttributeModifiers.Entry> additionalModifiers, boolean tieredDamage, boolean persistPrevious) implements PatchGenerator {
    public static final MapCodec<WeaponStatsGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(WeaponLevelBasedValue.CODEC.optionalFieldOf("attack_damage", Collections.emptyList()).forGetter(WeaponStatsGenerator::damage),
            WeaponLevelBasedValue.CODEC.optionalFieldOf("attack_speed", Collections.emptyList()).forGetter(WeaponStatsGenerator::speed),
            UUIDUtil.CODEC.optionalFieldOf("damage_uuid_override").forGetter(WeaponStatsGenerator::damageIdOverride),
            UUIDUtil.CODEC.optionalFieldOf("speed_uuid_override").forGetter(WeaponStatsGenerator::speedIdOverride),
            ItemAttributeModifiers.Entry.CODEC.listOf().optionalFieldOf("additional_modifiers", Collections.emptyList()).forGetter(WeaponStatsGenerator::additionalModifiers),
            Codec.BOOL.optionalFieldOf("apply_tier_to_damage", true).forGetter(WeaponStatsGenerator::tieredDamage),
            Codec.BOOL.fieldOf("persist_previous").forGetter(WeaponStatsGenerator::persistPrevious)).apply(instance, WeaponStatsGenerator::new));

    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        ItemAttributeModifiers oldModifiers = patchedDataComponentMap.get(DataComponents.ATTRIBUTE_MODIFIERS);
        ToolMaterialWrapper toolMaterialWrapper = item.defaulted$getToolMaterial();
        if (toolMaterialWrapper == null) toolMaterialWrapper = Defaulted.DEFAULT_WRAPPER;
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        UUID damageID = damageIdOverride.orElse(Item.BASE_ATTACK_DAMAGE_UUID);
        UUID speedID = speedIdOverride.orElse(Item.BASE_ATTACK_SPEED_UUID);
        AttributeModifier attackDamage = null;
        boolean hasDamage = false;
        if (!damage.isEmpty()) {
            hasDamage = true;
            attackDamage = new AttributeModifier(damageID, "Weapon stats", getTierModifier(toolMaterialWrapper, true), AttributeModifier.Operation.ADD_VALUE);
        }
        AttributeModifier attackSpeed = null;
        boolean hasSpeed = false;
        if (!speed.isEmpty()) {
            hasSpeed = true;
            attackSpeed = new AttributeModifier(speedID, "Weapon stats", getTierModifier(toolMaterialWrapper, false), AttributeModifier.Operation.ADD_VALUE);
        }
        if (!(hasDamage || hasSpeed)) return;
        
		for (ItemAttributeModifiers.Entry entry : additionalModifiers)
			if (!((hasDamage && ItemPatches.matches(entry, Attributes.ATTACK_DAMAGE, damageID)) || (hasSpeed && ItemPatches.matches(entry, Attributes.ATTACK_SPEED, speedID)))) builder.add(entry.attribute(), entry.modifier(), entry.slot());
        if (persistPrevious && oldModifiers != null)
            for (ItemAttributeModifiers.Entry entry : oldModifiers.modifiers())
                if (!((hasDamage && ItemPatches.matches(entry, Attributes.ATTACK_DAMAGE, damageID)) || (hasSpeed && ItemPatches.matches(entry, Attributes.ATTACK_SPEED, speedID)))) builder.add(entry.attribute(), entry.modifier(), entry.slot());

        if (hasDamage) builder.add(Attributes.ATTACK_DAMAGE, attackDamage, EquipmentSlotGroup.MAINHAND);
        if (hasSpeed) builder.add(Attributes.ATTACK_SPEED, attackSpeed, EquipmentSlotGroup.MAINHAND);
        patchedDataComponentMap.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
    
    private double getTierModifier(ToolMaterialWrapper tier, boolean forDamage) {
        if (forDamage)
            for (WeaponLevelBasedValue value : damage) {
                if (value instanceof WeaponLevelBasedValue.Unconditional unconditional) return unconditional.value() + (tieredDamage ? tier.attackDamageBonus() : 0);
                else {
                    Float res = value.getResult(tier.weaponLevel(), tieredDamage);
                    if (res != null) return res.doubleValue();
                }
            }
        else for (WeaponLevelBasedValue value : speed) {
            Float res = value.getResult(tier.weaponLevel(), true);
            if (res != null) return res.doubleValue();
        }
        return 0;
    }
    
    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
}
