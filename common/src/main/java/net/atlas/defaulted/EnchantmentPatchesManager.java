package net.atlas.defaulted;

import net.atlas.defaulted.enchantment.EnchantmentPatches;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentPatchesManager extends SimpleJsonResourceReloadListener<EnchantmentPatches> {
    public static List<EnchantmentPatches> CLIENT_CACHED = null;
    private static EnchantmentPatchesManager INSTANCE;
    private List<EnchantmentPatches> cached = null;
    private Map<Identifier, EnchantmentPatches> intermediary = new HashMap<>();
    public EnchantmentPatchesManager(HolderLookup.Provider arg) {
        super(arg, EnchantmentPatches.CODEC, Defaulted.ENCHANTMENT_PATCHES_TYPE);
        INSTANCE = this;
    }

    public void patch(HolderLookup.Provider provider) {
        Defaulted.patchEnchantments(provider, cached);
        Defaulted.EXECUTE_ON_ENCHANT_RELOAD.forEach(collectionConsumer -> collectionConsumer.accept(cached));
    }

    @Override
    protected Map<Identifier, EnchantmentPatches> prepare(ResourceManager resourceManager,
            ProfilerFiller profilerFiller) {
        clear();
        return super.prepare(resourceManager, profilerFiller);
    }

    @Override
    protected void apply(Map<Identifier, EnchantmentPatches> patches, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        intermediary = patches;
        Defaulted.ADD_DEFAULT_ENCHANT_PATCHES.forEach(collectionConsumer -> collectionConsumer.accept(intermediary));
    }

    public static EnchantmentPatchesManager getInstance() {
        return INSTANCE;
    }

    public static List<EnchantmentPatches> getCached(HolderLookup.Provider provider) {
        if (INSTANCE == null) return null;
        INSTANCE.load(provider);
        return INSTANCE.cached;
    }

    public void load(HolderLookup.Provider provider) {
        if (cached == null) {
            cached = intermediary.entrySet().stream().map(entry -> new EnchantmentPatchesEntry(entry.getKey(), entry.getValue())).sorted(Comparator.naturalOrder()).map(EnchantmentPatchesEntry::enchantmentPatches).toList();
            patch(provider);
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

    public static void loadClientCache(HolderLookup.Provider provider, List<EnchantmentPatches> cached) {
        EnchantmentPatchesManager.CLIENT_CACHED = cached;
        Defaulted.patchEnchantments(provider, cached);
    }
    public static void setClientCache(HolderLookup.Provider provider) {
        EnchantmentPatchesManager.CLIENT_CACHED = getCached(provider);
    }
    
    public record EnchantmentPatchesEntry(Identifier id, EnchantmentPatches enchantmentPatches) implements Comparable<EnchantmentPatchesEntry> {
        @Override
        public int compareTo(EnchantmentPatchesEntry other) {
            int priority = enchantmentPatches.compareTo(other.enchantmentPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }
}
