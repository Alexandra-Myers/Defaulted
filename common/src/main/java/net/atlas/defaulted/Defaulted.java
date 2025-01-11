package net.atlas.defaulted;

import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.mixin.ItemAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootDataType;

import java.util.*;
import java.util.function.Consumer;

public final class Defaulted {
    public static final Map<Holder<Item>, DataComponentMap> originalComponents = new HashMap<>();
    public static final String MOD_ID = "defaulted";
    public static final ResourceKey<Registry<ItemPatches>> ITEM_PATCHES = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "default_component_patches"));
    public static final LootDataType<ItemPatches> PATCHES_LOOT_DATA_TYPE = DefaultedExpectPlatform.createLootDataType(ITEM_PATCHES, ItemPatches.DIRECT_CODEC, (validationContext, resourceKey, object) -> {});
    /**
     * {@link ArrayList} of {@link Consumer}s to run on the sorted collection of {@link ItemPatches} after a reload or resource loading.
     * NOTE: All consumers must be triggered on **server start**.
     */
    public static final List<Consumer<Collection<ItemPatches>>> EXECUTE_ON_RELOAD = new ArrayList<>();

    public static void init() {
        // Write common init code here.
    }

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
            if (newMap.asPatch().isEmpty()) continue;
            ((ItemAccessor) item).setComponents(newMap);
        }
    }
}
