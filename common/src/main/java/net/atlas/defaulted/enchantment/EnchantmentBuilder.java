package net.atlas.defaulted.enchantment;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EnchantmentBuilder implements DataComponentHolder {
    Component description;
    HolderSet<Item> supportedItems;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<HolderSet<Item>> primaryItems;
    int weight;
    int maxLevel;
    Enchantment.Cost minCost;
    Enchantment.Cost maxCost;
    int anvilCost;
    List<EquipmentSlotGroup> slots;
    HolderSet<Enchantment> exclusiveSet;
    PatchedDataComponentMap effects;
    boolean getsChanged = false;
    public EnchantmentBuilder(Component description, Enchantment.EnchantmentDefinition definition, HolderSet<Enchantment> exclusiveSet, DataComponentMap effects) {
        this.description = description;
        this.supportedItems = definition.supportedItems();
        this.primaryItems = definition.primaryItems();
        this.weight = definition.weight();
        this.maxLevel = definition.maxLevel();
        this.minCost = definition.minCost();
        this.maxCost = definition.maxCost();
        this.anvilCost = definition.anvilCost();
        this.slots = new ArrayList<>(definition.slots());
        this.exclusiveSet = exclusiveSet;
        this.effects = new PatchedDataComponentMap(effects);
    }

    public static EnchantmentBuilder of(Enchantment enchantment) {
        return new EnchantmentBuilder(enchantment.description(), enchantment.definition(), enchantment.exclusiveSet(), enchantment.effects());
    }

    public void setChanged() {
        this.getsChanged = true;
    }

    public void setDescription(Component description) {
        setChanged();
        this.description = description;
    }

    public void setSupportedItems(HolderSet<Item> supportedItems) {
        setChanged();
        this.supportedItems = supportedItems;
    }

    public void setPrimaryItems(HolderSet<Item> primaryItems) {
        setChanged();
        this.primaryItems = Optional.ofNullable(primaryItems);
    }

    public void setWeight(int weight) {
        setChanged();
        this.weight = Mth.clamp(weight, 1, 1024);
    }

    public void setMaxLevel(int maxLevel) {
        setChanged();
        this.maxLevel = Mth.clamp(maxLevel, 1, 255);
    }

    public void setMinCost(Enchantment.Cost minCost) {
        setChanged();
        this.minCost = minCost;
    }

    public void setMaxCost(Enchantment.Cost maxCost) {
        setChanged();
        this.maxCost = maxCost;
    }

    public void setAnvilCost(int anvilCost) {
        setChanged();
        this.anvilCost = Math.max(anvilCost, 0);
    }

    public void setSlots(List<EquipmentSlotGroup> slots) {
        setChanged();
        this.slots = new ArrayList<>(slots);
    }

    public void setExclusiveSet(HolderSet<Enchantment> exclusiveSet) {
        setChanged();
        this.exclusiveSet = exclusiveSet;
    }

    public void modifySlots(List<EquipmentSlotGroup> added, List<EquipmentSlotGroup> removed) {
        if (added.isEmpty() && removed.isEmpty()) return;
        setChanged();
        this.slots.removeAll(removed);
        this.slots.addAll(added);
    }

    public void applyEffects(DataComponentPatch effects) {
        if (effects.isEmpty()) return;
        setChanged();
        this.effects.applyPatch(effects);
    }

    public <T> void set(DataComponentType<T> type, T value) {
        setChanged();
        this.effects.set(type, value);
    }

    public void apply(EnchantmentPatches.EnchantmentOverrides overrides) {
        overrides.description().ifPresent(this::setDescription);
        apply(overrides.definition());
        overrides.definition().slots().ifPresentOrElse(this::setSlots, () -> this.modifySlots(overrides.addedSlots(), overrides.removedSlots()));
        overrides.exclusiveSet().ifPresent(this::setExclusiveSet);
        applyEffects(overrides.effectsPatch());
    }

    public void apply(EnchantmentPatches.EnchantmentDefinition definition) {
        definition.supportedItems().ifPresent(this::setSupportedItems);
        definition.primaryItems().ifPresent(this::setPrimaryItems);
        definition.weight().ifPresent(weight -> this.setWeight(weight.roundedValue(Optional.of(this.weight))));
        definition.maxLevel().ifPresent(maxLevel -> this.setMaxLevel(maxLevel.roundedValue(Optional.of(this.maxLevel))));
        definition.anvilCost().ifPresent(anvilCost -> this.setAnvilCost(anvilCost.roundedValue(Optional.of(this.anvilCost))));
        definition.minCost().ifPresent(cost -> this.setMinCost(cost.toCost(this.minCost)));
        definition.maxCost().ifPresent(cost -> this.setMaxCost(cost.toCost(this.maxCost)));
    }

    public boolean isChanged() {
        return this.getsChanged;
    }

    public Enchantment build() {
        return new Enchantment(this.description,
                new Enchantment.EnchantmentDefinition(this.supportedItems,
                        this.primaryItems,
                        this.weight,
                        this.maxLevel,
                        this.minCost,
                        this.maxCost,
                        this.anvilCost,
                        Collections.unmodifiableList(this.slots)),
                this.exclusiveSet,
                this.effects);
    }

    @Override
    public @NonNull DataComponentMap getComponents() {
        return this.effects;
    }
}
