package net.atlas.defaulted.component.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.generators.handler.ArmourVariable;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
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
        ItemAttributeModifiers oldModifiers = patchedDataComponentMap.get(DataComponents.ATTRIBUTE_MODIFIERS);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        List<Holder<Attribute>> addedEntries = new ArrayList<>();
		String uuid = "2AD3F246-C118-495F-4726-6020A9A58B6B";
		EquipmentSlotGroup slotGroup = EquipmentSlotGroup.ARMOR;
		if (item instanceof Equipable equipable)
			slotGroup = EquipmentSlotGroup.bySlot(equipable.getEquipmentSlot());
		uuid = switch (slotGroup) {
			case HEAD -> "2AD3F246-FEE1-4E67-B886-69FD380BB150";
			case CHEST -> "9F3D476D-C118-4544-8365-64846904B48E";
			case LEGS -> "D8499B04-0E66-4726-AB29-64469D734E0D";
			case FEET -> "845DB27C-C624-495F-8C9F-6020A9A58B6B";
			case BODY -> "C1C72771-8B8E-BA4A-ACE0-81A93C8928B2";
			default -> uuid;
		};

		if (armor != null) {
            addedEntries.add(Attributes.ARMOR);
			builder.add(Attributes.ARMOR,
                new AttributeModifier(uuid, armor, AttributeModifier.Operation.ADD_VALUE),
                slotGroup);
		}
		if (toughness != null) {
            addedEntries.add(Attributes.ARMOR_TOUGHNESS);
			if (toughness > 0) builder.add(Attributes.ARMOR_TOUGHNESS,
				new AttributeModifier(uuid, toughness, AttributeModifier.Operation.ADD_VALUE),
				slotGroup);
		}
		if (kbRes != null) {
			addedEntries.add(Attributes.KNOCKBACK_RESISTANCE);
			if (kbRes > 0) builder.add(Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(uuid, kbRes, AttributeModifier.Operation.ADD_VALUE),
				slotGroup);
		}
        
        if (persistPrevious && oldModifiers != null)
            for (ItemAttributeModifiers.Entry entry : oldModifiers.modifiers()) {
                innerloop: {
                    for (Holder<Attribute> attribute : addedEntries) {
                        if (ItemPatches.matches(entry, attribute, UUID.fromString(uuid))) break innerloop;
                    }
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
