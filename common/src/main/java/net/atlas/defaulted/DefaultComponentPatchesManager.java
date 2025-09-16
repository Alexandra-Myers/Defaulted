package net.atlas.defaulted;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.atlas.defaulted.component.ItemPatches;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class DefaultComponentPatchesManager extends SimpleJsonResourceReloadListener<ItemPatches> {
    public static List<ItemPatches> CLIENT_CACHED = null;
    private static DefaultComponentPatchesManager INSTANCE;
    private List<ItemPatches> cached = null;
    private Map<ResourceLocation, ItemPatches> intermediary = new HashMap<>();
    public DefaultComponentPatchesManager(HolderLookup.Provider arg) {
        super(arg, ItemPatches.DIRECT_CODEC, Defaulted.ITEM_PATCHES_TYPE);
        INSTANCE = this;
    }

    public void patch() {
        Defaulted.patchItemComponents(cached);
        Defaulted.EXECUTE_ON_RELOAD.forEach(collectionConsumer -> collectionConsumer.accept(cached));
    }

    @Override
    protected Map<ResourceLocation, ItemPatches> prepare(ResourceManager resourceManager,
            ProfilerFiller profilerFiller) {
        clear();
        return super.prepare(resourceManager, profilerFiller);
    }

    @Override
    protected void apply(Map<ResourceLocation, ItemPatches> patches, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        intermediary = patches;
    }

    public static DefaultComponentPatchesManager getInstance() {
        return INSTANCE;
    }

    public static List<ItemPatches> getCached() {
        if (INSTANCE == null) return null;
        INSTANCE.load();
        return INSTANCE.cached;
    }

    public void load() {
        if (cached == null) {
            cached = intermediary.entrySet().stream().map(entry -> new ItemPatchesEntry(entry.getKey(), entry.getValue())).sorted(Comparator.naturalOrder()).map(ItemPatchesEntry::itemPatches).toList();
            patch();
        }
    }

    public static void clear() {
        if (INSTANCE != null) INSTANCE.cached = null;

        if (CLIENT_CACHED != null) CLIENT_CACHED = null;
    }

    public static void loadClientCache(List<ItemPatches> cached) {
        DefaultComponentPatchesManager.CLIENT_CACHED = cached;
        Defaulted.patchItemComponents(cached);
    }
    public static void setClientCache() {
        DefaultComponentPatchesManager.CLIENT_CACHED = getCached();
    }
    
    public record ItemPatchesEntry(ResourceLocation id, ItemPatches itemPatches) implements Comparable<ItemPatchesEntry> {
        @Override
        public int compareTo(ItemPatchesEntry other) {
            int priority = itemPatches.compareTo(other.itemPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }
}
