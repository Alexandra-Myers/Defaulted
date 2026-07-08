package net.atlas.defaulted.enchantment;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.component.HeterogeneousHolderSetListCodec;
import net.atlas.defaulted.enchantment.value_provider.ValueProvider;
import net.atlas.defaulted.utils.DataComponentPatchUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record EnchantmentPatches(List<HolderSet<Enchantment>> elements, EnchantmentOverrides overrides, List<EnchantmentPatchGenerator> patchGenerators, int priority) implements Comparable<EnchantmentPatches> {
    public static final Codec<EnchantmentPatches> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance ->
            instance.group(HeterogeneousHolderSetListCodec.create(Registries.ENCHANTMENT, RegistryFixedCodec.create(Registries.ENCHANTMENT), false).fieldOf("elements").forGetter(EnchantmentPatches::elements))
                    .and(additionalDetails(instance))
                    .apply(instance, EnchantmentPatches::new)));

    @Override
    public int compareTo(EnchantmentPatches o) {
        return priority - o.priority;
    }

    public void apply(Holder<Enchantment> enchantment, EnchantmentBuilder enchantmentBuilder) {
        if (!matchEnchantment(enchantment)) return;
        enchantmentBuilder.apply(this.overrides());
    }

    public void applyGenerators(Holder<Enchantment> enchantment, EnchantmentBuilder enchantmentBuilder) {
        if (!matchEnchantment(enchantment)) return;
        this.patchGenerators().forEach(patchGenerator -> patchGenerator.patchDataComponentMap(enchantment, enchantmentBuilder));
    }

    public boolean matchEnchantment(Holder<Enchantment> enchantment) {
        if (!(this.elements.isEmpty() || this.elements.stream().allMatch(holders -> holders.size() <= 0)))
            return this.elements.stream().anyMatch(holders -> holders.contains(enchantment));
        return true;
    }

    public static Products.P3<RecordCodecBuilder.Mu<EnchantmentPatches>, EnchantmentOverrides, List<EnchantmentPatchGenerator>, Integer> additionalDetails(RecordCodecBuilder.Instance<EnchantmentPatches> instance) {
        return instance.group(EnchantmentOverrides.CODEC.forGetter(EnchantmentPatches::overrides),
                EnchantmentPatchGenerator.CODEC.listOf().optionalFieldOf("patch_generators", Collections.emptyList()).forGetter(EnchantmentPatches::patchGenerators),
                ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("priority", 1000).forGetter(EnchantmentPatches::priority));
    }

    public record EnchantmentOverrides(Optional<Component> description,
                                       EnchantmentDefinition definition,
                                       Optional<HolderSet<Enchantment>> exclusiveSet,
                                       List<EquipmentSlotGroup> addedSlots,
                                       List<EquipmentSlotGroup> removedSlots,
                                       DataComponentPatch effectsPatch) {
        public static final MapCodec<EnchantmentOverrides> CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(
                                ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(EnchantmentOverrides::description),
                                EnchantmentDefinition.CODEC.forGetter(EnchantmentOverrides::definition),
                                RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("exclusive_set").forGetter(EnchantmentOverrides::exclusiveSet),
                                EquipmentSlotGroup.CODEC.listOf().optionalFieldOf("added_slots", Collections.emptyList()).forGetter(EnchantmentOverrides::addedSlots),
                                EquipmentSlotGroup.CODEC.listOf().optionalFieldOf("removed_slots", Collections.emptyList()).forGetter(EnchantmentOverrides::removedSlots),
                                DataComponentPatchUtils.codec(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE).optionalFieldOf("patch", DataComponentPatch.EMPTY).forGetter(EnchantmentOverrides::effectsPatch)
                        )
                        .apply(i, EnchantmentOverrides::new)
        );
    }

    public record EnchantmentDefinition(
            Optional<HolderSet<Item>> supportedItems,
            Optional<HolderSet<Item>> primaryItems,
            Optional<ValueProvider> weight,
            Optional<ValueProvider> maxLevel,
            Optional<Cost> minCost,
            Optional<Cost> maxCost,
            Optional<ValueProvider> anvilCost,
            Optional<List<EquipmentSlotGroup>> slots
    ) {
        public static final MapCodec<EnchantmentDefinition> CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(
                                RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("supported_items").forGetter(EnchantmentDefinition::supportedItems),
                                RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("primary_items").forGetter(EnchantmentDefinition::primaryItems),
                                ValueProvider.CODEC.optionalFieldOf("weight").forGetter(EnchantmentDefinition::weight),
                                ValueProvider.CODEC.optionalFieldOf("max_level").forGetter(EnchantmentDefinition::maxLevel),
                                Cost.CODEC.optionalFieldOf("min_cost").forGetter(EnchantmentDefinition::minCost),
                                Cost.CODEC.optionalFieldOf("max_cost").forGetter(EnchantmentDefinition::maxCost),
                                ValueProvider.CODEC.optionalFieldOf("anvil_cost").forGetter(EnchantmentDefinition::anvilCost),
                                EquipmentSlotGroup.CODEC.listOf().optionalFieldOf("slots").forGetter(EnchantmentDefinition::slots)
                        )
                        .apply(i, EnchantmentDefinition::new)
        );
    }

    public record Cost(ValueProvider base, ValueProvider perLevelAboveFirst) {
        public static final Codec<Cost> CODEC = RecordCodecBuilder.create(
                i -> i.group(
                                ValueProvider.CODEC.fieldOf("base").forGetter(Cost::base), ValueProvider.CODEC.fieldOf("per_level_above_first").forGetter(Cost::perLevelAboveFirst)
                        )
                        .apply(i, Cost::new)
        );

        public Enchantment.Cost toCost(Enchantment.Cost original) {
            return new Enchantment.Cost(this.base().roundedValue(Optional.of(original.base())),
                    this.perLevelAboveFirst().roundedValue(Optional.of(original.perLevelAboveFirst())));
        }
    }
}
