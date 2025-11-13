package net.atlas.defaulted.component.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.generators.handler.ArmourVariable;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record ArmourStatsGenerator(ArmourVariable<Integer> durability, ArmourVariable<Integer> protection, ArmourVariable<Double> armourToughness, ArmourVariable<Double> armourKbRes, boolean persistPrevious) implements PatchGenerator {
    public static final Codec<Double> POSITIVE_DOUBLE = doubleMiniumumExclusiveWithMessage(0, double_ -> "Value must be positive: " + double_);
    public static final MapCodec<ArmourStatsGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(ArmourVariable.codec(ExtraCodecs.POSITIVE_INT).optionalFieldOf("max_damage", ArmourVariable.empty()).forGetter(ArmourStatsGenerator::durability),
            ArmourVariable.codec(ExtraCodecs.POSITIVE_INT).optionalFieldOf("armor", ArmourVariable.empty()).forGetter(ArmourStatsGenerator::protection),
            ArmourVariable.codec(POSITIVE_DOUBLE).optionalFieldOf("armor_toughness", ArmourVariable.empty()).forGetter(ArmourStatsGenerator::armourToughness),
            ArmourVariable.codec(POSITIVE_DOUBLE).optionalFieldOf("knockback_resistance", ArmourVariable.empty()).forGetter(ArmourStatsGenerator::armourKbRes),
            Codec.BOOL.fieldOf("persist_previous").forGetter(ArmourStatsGenerator::persistPrevious))
        .apply(instance, ArmourStatsGenerator::new));
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        Integer maxDamage = durability.getValue(item);
        if (maxDamage != null) Defaulted.setDurability(maxDamage, patchedDataComponentMap);
        Integer armor = protection.getValue(item);
        Double toughness = armourToughness.getValue(item);
        Double kbRes = armourKbRes.getValue(item);
        if (armor == null && toughness == null && kbRes == null) return;
        ItemAttributeModifiers oldModifiers = patchedDataComponentMap.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers defaultModifiers = item.getDefaultAttributeModifiers();
        if (oldModifiers == ItemAttributeModifiers.EMPTY && defaultModifiers != ItemAttributeModifiers.EMPTY) oldModifiers = defaultModifiers;
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        List<Holder<Attribute>> addedEntries = new ArrayList<>();
		ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("armor.any");
		EquipmentSlotGroup slotGroup = EquipmentSlotGroup.ARMOR;
		if (item instanceof Equipable equipable)
			slotGroup = EquipmentSlotGroup.bySlot(equipable.getEquipmentSlot());
		resourceLocation = switch (slotGroup) {
			case HEAD -> ResourceLocation.withDefaultNamespace("armor.helmet");
			case CHEST -> ResourceLocation.withDefaultNamespace("armor.chestplate");
			case LEGS -> ResourceLocation.withDefaultNamespace("armor.leggings");
			case FEET -> ResourceLocation.withDefaultNamespace("armor.boots");
			case BODY -> ResourceLocation.withDefaultNamespace("armor.body");
			default -> resourceLocation;
		};

		if (armor != null) {
            addedEntries.add(Attributes.ARMOR);
			builder.add(Attributes.ARMOR,
                new AttributeModifier(resourceLocation, armor, AttributeModifier.Operation.ADD_VALUE),
                slotGroup);
		}
		if (toughness != null) {
            addedEntries.add(Attributes.ARMOR_TOUGHNESS);
			if (toughness > 0) builder.add(Attributes.ARMOR_TOUGHNESS,
				new AttributeModifier(resourceLocation, toughness, AttributeModifier.Operation.ADD_VALUE),
				slotGroup);
		}
		if (kbRes != null) {
			addedEntries.add(Attributes.KNOCKBACK_RESISTANCE);
			if (kbRes > 0) builder.add(Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(resourceLocation, kbRes, AttributeModifier.Operation.ADD_VALUE),
				slotGroup);
		}
        
        if (persistPrevious && oldModifiers != ItemAttributeModifiers.EMPTY)
            for (ItemAttributeModifiers.Entry entry : oldModifiers.modifiers()) {
                innerloop: {
                    for (Holder<Attribute> attribute : addedEntries) if (entry.matches(attribute, resourceLocation)) break innerloop;
                    builder.add(entry.attribute(), entry.modifier(), entry.slot());
                }
            }

        patchedDataComponentMap.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }
    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
    public static Codec<Double> doubleMiniumumExclusiveWithMessage(double min, Function<Double, String> function) {
        return Codec.DOUBLE
            .validate(
                double_ -> double_.compareTo(min) > 0 ? DataResult.success(double_) : DataResult.error(() -> (String)function.apply(double_))
            );
    }
}
