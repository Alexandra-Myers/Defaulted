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

import java.util.HashMap;
import java.util.Map;

public final class Defaulted {
    public static final Map<Holder<Item>, DataComponentMap> originalComponents = new HashMap<>();
    public static final String MOD_ID = "defaulted";
    public static final ResourceKey<Registry<ItemPatches>> ITEM_PATCHES = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(MOD_ID, "default_component_patches"));

    public static void init() {
        // Write common init code here.
    }

    public static void patchItemComponents(Iterable<ItemPatches> reg) {
        for (Item item : BuiltInRegistries.ITEM) {
            Holder<Item> itemHolder = item.builtInRegistryHolder();
            DataComponentMap originalPrototype = item.components();
            if (Defaulted.originalComponents.containsKey(itemHolder)) originalPrototype = Defaulted.originalComponents.get(itemHolder);
            else Defaulted.originalComponents.put(itemHolder, originalPrototype);
            PatchedDataComponentMap newMap = new PatchedDataComponentMap(originalPrototype);
            reg.forEach(itemPatches -> itemPatches.apply(item, newMap));
            ((ItemAccessor) item).setComponents(newMap);
        }
    }
}
