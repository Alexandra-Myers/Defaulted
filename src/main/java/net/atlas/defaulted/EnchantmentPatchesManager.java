package net.atlas.defaulted;

//? <1.21.11 && fabric {
/*import net.atlas.defaulted.Defaulted;
 *///?}
import net.atlas.defaulted.enchantment.EnchantmentPatches;
//? <1.21.11 && fabric {
/*import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
 *///?}
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//? <1.21.11 && fabric {
/*public class EnchantmentPatchesManager extends SimpleJsonResourceReloadListener<EnchantmentPatches> implements IdentifiableResourceReloadListener {
*///?} else {
public class EnchantmentPatchesManager extends SimpleJsonResourceReloadListener<EnchantmentPatches> {
//?}
    public static List<EnchantmentPatches> CLIENT_CACHED = null;
    private static EnchantmentPatchesManager INSTANCE;
    private List<EnchantmentPatches> cached = null;
    private Map<Identifier, EnchantmentPatches> intermediary = new HashMap<>();
    public EnchantmentPatchesManager(HolderLookup.Provider arg) {
        super(arg, EnchantmentPatches.CODEC, Defaulted.ENCHANTMENT_PATCHES_TYPE);
        INSTANCE = this;
    }

    public void patch(RegistryAccess registryAccess) {
        Defaulted.patchEnchantments(registryAccess, cached);
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
    }

    public static EnchantmentPatchesManager getInstance() {
        return INSTANCE;
    }

    public static List<EnchantmentPatches> getCached(RegistryAccess registryAccess) {
        if (INSTANCE == null) return null;
        INSTANCE.load(registryAccess);
        return getCached();
    }

    public static @Nullable List<EnchantmentPatches> getCached() {
        if (INSTANCE == null) return null;
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

    //? <1.21.11 && fabric {
    /*@Override
    public Identifier getFabricId() {
        return Defaulted.id("enchantment_patches");
    }
    *///?}

    public record EnchantmentPatchesEntry(Identifier id, EnchantmentPatches enchantmentPatches) implements Comparable<EnchantmentPatchesEntry> {
        @Override
        public int compareTo(EnchantmentPatchesEntry other) {
            int priority = enchantmentPatches.compareTo(other.enchantmentPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }
    }
}
