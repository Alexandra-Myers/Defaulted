package net.atlas.defaulted;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import net.atlas.defaulted.enchantment.EnchantmentPatches;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public abstract class EnchantmentPatchesManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger LOGGER = LogManager.getLogger();
    public static List<EnchantmentPatches> CLIENT_CACHED = null;
    private static EnchantmentPatchesManager INSTANCE;
    private List<EnchantmentPatches> cached = null;
    private Map<ResourceLocation, EnchantmentPatches> intermediary = new HashMap<>();
    public EnchantmentPatchesManager() {
        super(GSON, "defaulted/enchantment_patches");
        INSTANCE = this;
    }

    public void patch(RegistryAccess registryAccess) {
        Defaulted.patchEnchantments(registryAccess, cached);
        Defaulted.EXECUTE_ON_ENCHANT_RELOAD.forEach(collectionConsumer -> collectionConsumer.accept(cached));
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager,
            ProfilerFiller profilerFiller) {
        clear();
        return super.prepare(resourceManager, profilerFiller);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, EnchantmentPatches> patchesMap = new HashMap<>();
        RegistryOps<JsonElement> registryOps = makeOps();

        for(Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation loc = entry.getKey();

            try {
                Optional<EnchantmentPatches> optionalEnchantmentPatches = getCodec().parse(registryOps, entry.getValue()).getOrThrow(JsonParseException::new);
                optionalEnchantmentPatches.ifPresentOrElse(enchantmentPatches -> patchesMap.put(loc, enchantmentPatches), () -> LOGGER.debug("Skipping loading enchantment patch {} as its conditions were not met", loc));
            } catch (IllegalArgumentException | JsonParseException runtimeException) {
                LOGGER.error("Parsing error loading enchantment patches {}", loc, runtimeException);
            }
        }

        intermediary = patchesMap;
    }

    public Codec<Optional<EnchantmentPatches>> getCodec() {
        return EnchantmentPatches.CODEC.xmap(Optional::of, Optional::get);
    }

    public abstract RegistryOps<JsonElement> makeOps();

    public static EnchantmentPatchesManager getInstance() {
        return INSTANCE;
    }

    public static List<EnchantmentPatches> getCached(RegistryAccess registryAccess) {
        if (INSTANCE == null) return null;
        INSTANCE.load(registryAccess);
        return INSTANCE.cached;
    }

    public void load(RegistryAccess registryAccess) {
        if (cached == null) {
            Defaulted.ADD_DEFAULT_ENCHANT_PATCHES.forEach(collectionConsumer -> collectionConsumer.accept(registryAccess, intermediary));
            cached = intermediary.entrySet().stream().map(entry -> new EnchantmentPatchesEntry(entry.getKey(), entry.getValue())).sorted(Comparator.naturalOrder()).map(EnchantmentPatchesEntry::enchantmentPatches).toList();
            patch(registryAccess);
        }
    }

    public static void clear() {
        if (INSTANCE != null) INSTANCE.cached = null;

        if (CLIENT_CACHED != null) CLIENT_CACHED = null;
    }

    public static void clearClient() {
        clear();
        Defaulted.ORIGINAL_ENCHANTMENTS.clear();
    }

    public static void loadClientCache(RegistryAccess registryAccess, List<EnchantmentPatches> cached) {
        EnchantmentPatchesManager.CLIENT_CACHED = cached;
        Defaulted.patchEnchantments(registryAccess, cached);
    }
    public static void setClientCache(RegistryAccess registryAccess) {
        EnchantmentPatchesManager.CLIENT_CACHED = getCached(registryAccess);
    }
    
    public record EnchantmentPatchesEntry(ResourceLocation id, EnchantmentPatches enchantmentPatches) implements Comparable<EnchantmentPatchesEntry> {
        @Override
        public int compareTo(EnchantmentPatchesEntry other) {
            int priority = enchantmentPatches.compareTo(other.enchantmentPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }
}
