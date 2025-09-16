package net.atlas.defaulted;

import java.util.*;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import net.atlas.defaulted.component.ItemPatches;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public abstract class DefaultComponentPatchesManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    public static List<ItemPatches> CLIENT_CACHED = null;
    private static DefaultComponentPatchesManager INSTANCE;
    private List<ItemPatches> cached = null;
    private Map<ResourceLocation, ItemPatches> intermediary = new HashMap<>();
    public DefaultComponentPatchesManager() {
        super(GSON, "defaulted/default_component_patches");
        INSTANCE = this;
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
        RegistryOps<JsonElement> registryOps = makeOps();

        for(Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation loc = entry.getKey();

            try {
                Optional<ItemPatches> optionalItemPatches = getCodec().parse(registryOps, entry.getValue()).getOrThrow(JsonParseException::new);
                optionalItemPatches.ifPresentOrElse(itemPatches -> patchesMap.put(loc, itemPatches), () -> LOGGER.debug("Skipping loading item components patch {} as its conditions were not met", loc));
            } catch (IllegalArgumentException | JsonParseException runtimeException) {
                LOGGER.error("Parsing error loading item patches {}", loc, runtimeException);
            }
        }

        intermediary = patchesMap;
    }

    public Codec<Optional<ItemPatches>> getCodec() {
        return ItemPatches.DIRECT_CODEC.xmap(Optional::of, Optional::get);
    }

    public abstract RegistryOps<JsonElement> makeOps();

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
