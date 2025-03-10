package net.atlas.defaulted;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.atlas.defaulted.component.ItemPatches;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class DefaultComponentPatchesManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    public static List<ItemPatches> CLIENT_CACHED = null;
    private static DefaultComponentPatchesManager INSTANCE;
    private List<ItemPatches> cached = null;
    private Map<ResourceLocation, ItemPatches> intermediary = new HashMap<>();
    private final HolderLookup.Provider registries;
    public DefaultComponentPatchesManager(HolderLookup.Provider arg) {
        super(GSON, "defaulted/default_component_patches");
        INSTANCE = this;
        registries = arg;
    }

    public void patch() {
        Defaulted.patchItemComponents(cached);
        Defaulted.EXECUTE_ON_RELOAD.forEach(collectionConsumer -> collectionConsumer.accept(cached));
    }

    @Override
    protected @NotNull Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        clear();
        return super.prepare(resourceManager, profilerFiller);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, ItemPatches> patchesMap = new HashMap<>();
        RegistryOps<JsonElement> registryOps = this.registries.createSerializationContext(JsonOps.INSTANCE);

        for(Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation loc = entry.getKey();

            try {
                ItemPatches patches = ItemPatches.DIRECT_CODEC.parse(registryOps, entry.getValue()).getOrThrow(JsonParseException::new);
                patchesMap.put(loc, patches);
            } catch (IllegalArgumentException | JsonParseException runtimeException) {
                LOGGER.error("Parsing error loading item patches {}", loc, runtimeException);
            }
        }

        intermediary = patchesMap;
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
        if (INSTANCE != null) {
            INSTANCE.cached = null;
        }
    }

    public static void loadClientCache(List<ItemPatches> cached) {
        DefaultComponentPatchesManager.CLIENT_CACHED = cached;
        Defaulted.patchItemComponents(cached);
    }
    
    public record ItemPatchesEntry(ResourceLocation id, ItemPatches itemPatches) implements Comparable<ItemPatchesEntry> {
        @Override
        public int compareTo(ItemPatchesEntry other) {
            int priority = itemPatches.compareTo(other.itemPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }
}
