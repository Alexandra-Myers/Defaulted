package net.atlas.defaulted;

import java.util.*;

import net.atlas.defaulted.component.ItemPatches;
//? <1.21.11 && fabric {
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
//?}
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

//? <1.21.11 && fabric {
public class DefaultComponentPatchesManager extends SimpleJsonResourceReloadListener<ItemPatches> implements IdentifiableResourceReloadListener {
//?} else {
/*public class DefaultComponentPatchesManager extends SimpleJsonResourceReloadListener<ItemPatches> {
*///?}
    public static List<ItemPatches> CLIENT_CACHED = null;
    private static DefaultComponentPatchesManager INSTANCE;
    private List<ItemPatches> cached = null;
    private Map<ResourceLocation, ItemPatches> intermediary = new HashMap<>();
    public DefaultComponentPatchesManager(HolderLookup.Provider arg) {
        super(arg, ItemPatches.CODEC, Defaulted.ITEM_PATCHES_TYPE);
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

    public static List<ItemPatches> getCached(RegistryAccess registryAccess) {
        if (INSTANCE == null) return null;
        INSTANCE.load(registryAccess);
        return getCached();
    }

    public static @Nullable List<ItemPatches> getCached() {
        if (INSTANCE == null) return null;
        return INSTANCE.cached;
    }

    public void load(RegistryAccess registryAccess) {
        if (cached == null) {
            Defaulted.ADD_DEFAULT_PATCHES.forEach(collectionConsumer -> collectionConsumer.accept(registryAccess, intermediary));
            cached = intermediary.entrySet().stream().map(entry -> new ItemPatchesEntry(entry.getKey(), entry.getValue())).sorted(Comparator.naturalOrder()).map(ItemPatchesEntry::itemPatches).toList();
            patch();
        }
    }

    public static void clear() {
        if (INSTANCE != null) INSTANCE.cached = null;

        if (CLIENT_CACHED != null) CLIENT_CACHED = null;
    }

    public static void clearClient() {
        clear();
    }

    public static void loadClientCache(List<ItemPatches> cached) {
        DefaultComponentPatchesManager.CLIENT_CACHED = cached;
        Defaulted.patchItemComponents(cached);
    }
    public static void setClientCache(RegistryAccess registryAccess) {
        DefaultComponentPatchesManager.CLIENT_CACHED = getCached(registryAccess);
    }

    //? <1.21.11 && fabric {
    @Override
    public ResourceLocation getFabricId() {
        return Defaulted.id("default_component_patches");
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return List.of(Defaulted.id("enchantment_patches"));
    }
    //?}

    public record ItemPatchesEntry(ResourceLocation id, ItemPatches itemPatches) implements Comparable<ItemPatchesEntry> {
        @Override
        public int compareTo(ItemPatchesEntry other) {
            int priority = itemPatches.compareTo(other.itemPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }
}
