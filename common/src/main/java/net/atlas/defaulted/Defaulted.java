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
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

import java.util.*;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.MapCodec;

public final class Defaulted {
    public static boolean hasOwo = false;
    public static final BiMap<String, Tier> baseTiers = HashBiMap.create();
    public static final ToolMaterialWrapper DEFAULT_WRAPPER = new ToolMaterialWrapper(Tiers.DIAMOND, 3);
    public static final Map<Holder<Item>, DataComponentMap> originalComponents = new HashMap<>();
    public static final String MOD_ID = "defaulted";
    public static final Logger LOGGER = LogManager.getLogger("defaulted");
	public static final ResourceKey<Registry<MapCodec<? extends PatchGenerator>>> PATCH_GENERATOR_TYPE = ResourceKey.createRegistryKey(id("patch_generator"));
    public static final ResourceKey<Registry<DataComponentType<?>>> PHANTOM_COMPONENT_TYPE = ResourceKey.createRegistryKey(id("phantom_data_components"));
	public static final ResourceKey<Registry<ItemPatches>> ITEM_PATCHES_TYPE = ResourceKey.createRegistryKey(id("default_component_patches"));
    /**
     * {@link ArrayList} of {@link Consumer}s to run on the sorted collection of {@link ItemPatches} after a reload or resource loading.
     */
    public static final List<Consumer<Collection<ItemPatches>>> EXECUTE_ON_RELOAD = new ArrayList<>();
    /**
     * {@link ArrayList} of {@link Consumer}s for the initial map of all item patches, empty by default, and will be overridden if data is loaded for these.
     */
    public static final List<Consumer<Map<ResourceLocation, ItemPatches>>> ADD_DEFAULT_PATCHES = new ArrayList<>();
	public static final Set<ItemStack> ALL_STACKS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static void init() {
        // Write common init code here.
        baseTiers.put("wood", Tiers.WOOD);
        baseTiers.put("stone", Tiers.STONE);
        baseTiers.put("gold", Tiers.GOLD);
        baseTiers.put("iron", Tiers.IRON);
        baseTiers.put("diamond", Tiers.DIAMOND);
        baseTiers.put("netherite", Tiers.NETHERITE);
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
