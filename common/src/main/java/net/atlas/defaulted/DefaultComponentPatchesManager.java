package net.atlas.defaulted;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.atlas.defaulted.component.ItemPatches;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class DefaultComponentPatchesManager extends SimpleJsonResourceReloadListener<ItemPatches> {
    public static List<ItemPatches> cached = new ArrayList<>();
    public DefaultComponentPatchesManager(HolderLookup.Provider arg) {
        super(arg, ItemPatches.DIRECT_CODEC, Defaulted.ITEM_PATCHES_TYPE);
    }

    public static void patch() {
        Defaulted.patchItemComponents(cached);
        Defaulted.EXECUTE_ON_RELOAD.forEach(collectionConsumer -> collectionConsumer.accept(cached));
    }

    @Override
    protected void apply(Map<ResourceLocation, ItemPatches> patches, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        cached = patches.entrySet().stream().map(entry -> new ItemPatchesEntry(entry.getKey(), entry.getValue())).sorted(Comparator.naturalOrder()).map(ItemPatchesEntry::itemPatches).toList();
    }
    
    public record ItemPatchesEntry(ResourceLocation id, ItemPatches itemPatches) implements Comparable<ItemPatchesEntry> {
        @Override
        public int compareTo(ItemPatchesEntry other) {
            int priority = itemPatches.compareTo(other.itemPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }
}
