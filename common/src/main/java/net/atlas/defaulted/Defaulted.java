package net.atlas.defaulted;

import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.atlas.defaulted.component.generators.WeaponLevelBasedValue;
import net.atlas.defaulted.component.generators.condition.PatchConditions;
import net.atlas.defaulted.mixin.ItemAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.storage.loot.LootDataType;

import java.util.*;
import java.util.function.Consumer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.MapCodec;

public final class Defaulted {
    public static final BiMap<String, ToolMaterial> baseTiers = HashBiMap.create();
    public static final ToolMaterialWrapper DEFAULT_WRAPPER = new ToolMaterialWrapper(ToolMaterial.DIAMOND, 3);
    public static final Map<Holder<Item>, DataComponentMap> originalComponents = new HashMap<>();
    public static final String MOD_ID = "defaulted";
	public static final ResourceKey<Registry<MapCodec<? extends PatchGenerator>>> PATCH_GENERATOR_TYPE = ResourceKey.createRegistryKey(id("patch_generator"));
    public static final ResourceKey<Registry<ItemPatches>> ITEM_PATCHES = ResourceKey.createRegistryKey(id("default_component_patches"));
    public static final LootDataType<ItemPatches> PATCHES_LOOT_DATA_TYPE = DefaultedExpectPlatform.createLootDataType(ITEM_PATCHES, ItemPatches.DIRECT_CODEC, (validationContext, resourceKey, object) -> {});
    /**
     * {@link ArrayList} of {@link Consumer}s to run on the sorted collection of {@link ItemPatches} after a reload or resource loading.
     * NOTE: All consumers must be triggered on **server start**.
     */
    public static final List<Consumer<Collection<ItemPatches>>> EXECUTE_ON_RELOAD = new ArrayList<>();
	public static final Set<ItemStack> ALL_STACKS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static void init() {
        // Write common init code here.
        baseTiers.put("wood", ToolMaterial.WOOD);
        baseTiers.put("stone", ToolMaterial.STONE);
        baseTiers.put("gold", ToolMaterial.GOLD);
        baseTiers.put("iron", ToolMaterial.IRON);
        baseTiers.put("diamond", ToolMaterial.DIAMOND);
        baseTiers.put("netherite", ToolMaterial.NETHERITE);
        PatchConditions.bootstrap();
        WeaponLevelBasedValue.bootstrap();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void setDurability(int maxDamage, PatchedDataComponentMap patchedDataComponentMap) {
        patchedDataComponentMap.set(DataComponents.MAX_DAMAGE, maxDamage);
        patchedDataComponentMap.set(DataComponents.DAMAGE, 0);
        patchedDataComponentMap.set(DataComponents.MAX_STACK_SIZE, 1);
    }

    @SuppressWarnings("deprecation")
    public static void patchItemComponents(Iterable<ItemPatches> reg) {
        for (Item item : BuiltInRegistries.ITEM) {
            Holder<Item> itemHolder = item.builtInRegistryHolder();
            DataComponentMap originalPrototype = item.components();
            if (Defaulted.originalComponents.containsKey(itemHolder)) {
                originalPrototype = Defaulted.originalComponents.get(itemHolder);
                ((ItemAccessor) item).setComponents(originalPrototype);
            }
            else Defaulted.originalComponents.put(itemHolder, originalPrototype);
            PatchedDataComponentMap newMap = new PatchedDataComponentMap(originalPrototype);
            reg.forEach(itemPatches -> itemPatches.apply(item, newMap));
            reg.forEach(itemPatches -> itemPatches.applyGenerators(item, newMap));
            if (newMap.asPatch().isEmpty()) continue;
            ((ItemAccessor) item).setComponents(newMap);
        }
        synchronized (ALL_STACKS) {
			ALL_STACKS.forEach(ItemStack::defaulted$updatePrototype);
		}
    }
}
