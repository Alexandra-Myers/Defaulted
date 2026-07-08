package net.atlas.defaulted;

import com.mojang.datafixers.util.Pair;
import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.atlas.defaulted.component.generators.WeaponLevelBasedValue;
import net.atlas.defaulted.component.generators.condition.PatchConditions;
import net.atlas.defaulted.enchantment.EnchantmentBuilder;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.atlas.defaulted.enchantment.EnchantmentPatches;
import net.atlas.defaulted.enchantment.value_provider.ValueProvider;
import net.atlas.defaulted.mixin.MappedRegistryAccessor;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.MapCodec;

public final class Defaulted {
    public static boolean hasOwo = false;
    public static final BiMap<String, ToolMaterial> baseTiers = HashBiMap.create();
    public static final ToolMaterialWrapper DEFAULT_WRAPPER = new ToolMaterialWrapper(ToolMaterial.DIAMOND, 3, 3);
    public static final String MOD_ID = "defaulted";
    public static final Logger LOGGER = LogManager.getLogger("defaulted");
    public static final Map<ResourceKey<Enchantment>, Enchantment> ORIGINAL_ENCHANTMENTS = new HashMap<>();
    public static final ResourceKey<Registry<MapCodec<? extends PatchGenerator>>> PATCH_GENERATOR_TYPE = ResourceKey.createRegistryKey(id("patch_generator"));
    public static final ResourceKey<Registry<MapCodec<? extends EnchantmentPatchGenerator>>> ENCHANTMENT_PATCH_GENERATOR_TYPE = ResourceKey.createRegistryKey(id("enchantment_patch_generator"));
    public static final ResourceKey<Registry<ItemPatches>> ITEM_PATCHES_TYPE = ResourceKey.createRegistryKey(id("default_component_patches"));
    public static final ResourceKey<Registry<EnchantmentPatches>> ENCHANTMENT_PATCHES_TYPE = ResourceKey.createRegistryKey(id("enchantment_patches"));
    /**
     * {@link ArrayList} of {@link Consumer}s to run on the sorted collection of {@link ItemPatches} after a reload or resource loading.
     */
    public static final List<Consumer<Collection<ItemPatches>>> EXECUTE_ON_RELOAD = new ArrayList<>();
    /**
     * {@link ArrayList} of {@link Consumer}s for the initial map of all item patches, empty by default, and will be overridden if data is loaded for these.
     */
    static final List<BiConsumer<RegistryAccess, Map<Identifier, ItemPatches>>> ADD_DEFAULT_PATCHES = new ArrayList<>();
    /**
     * {@link ArrayList} of {@link Consumer}s to run on the sorted collection of {@link EnchantmentPatches} after a reload or resource loading.
     */
    public static final List<Consumer<Collection<EnchantmentPatches>>> EXECUTE_ON_ENCHANT_RELOAD = new ArrayList<>();
    /**
     * {@link ArrayList} of {@link Consumer}s for the initial map of all enchantment patches, empty by default, and will be overridden if data is loaded for these.
     */
    static final List<BiConsumer<RegistryAccess, Map<Identifier, EnchantmentPatches>>> ADD_DEFAULT_ENCHANT_PATCHES = new ArrayList<>();
	public static final Set<ItemStack> ALL_STACKS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public static void init() {
        // Write common init code here.
        baseTiers.put("wood", ToolMaterial.WOOD);
        baseTiers.put("stone", ToolMaterial.STONE);
        baseTiers.put("gold", ToolMaterial.GOLD);
        baseTiers.put("copper", ToolMaterial.COPPER);
        baseTiers.put("iron", ToolMaterial.IRON);
        baseTiers.put("diamond", ToolMaterial.DIAMOND);
        baseTiers.put("netherite", ToolMaterial.NETHERITE);
        PatchConditions.bootstrap();
        net.atlas.defaulted.enchantment.generators.condition.PatchConditions.bootstrap();
        ValueProvider.bootstrap();
        WeaponLevelBasedValue.bootstrap();
    }
    
    // Identifier is renamed to Identifier as of 26.1
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void setDurability(int maxDamage, PatchedDataComponentMap patchedDataComponentMap) {
        patchedDataComponentMap.set(DataComponents.MAX_DAMAGE, maxDamage);
        patchedDataComponentMap.set(DataComponents.DAMAGE, 0);
        patchedDataComponentMap.set(DataComponents.MAX_STACK_SIZE, 1);
    }

    @SuppressWarnings("deprecation")
    public static void patchItemComponents(Iterable<ItemPatches> reg) {
        for (Item item : BuiltInRegistries.ITEM) {
            DataComponentMap originalPrototype = item.components();
            PatchedDataComponentMap newMap = new PatchedDataComponentMap(originalPrototype);
            reg.forEach(itemPatches -> itemPatches.apply(item, newMap));
            reg.forEach(itemPatches -> itemPatches.applyGenerators(item, newMap));
            if (newMap.asPatch().isEmpty()) continue;
            item.builtInRegistryHolder().bindComponents(newMap);
        }
        synchronized (ALL_STACKS) {
			ALL_STACKS.forEach(ItemStack::defaulted$updatePrototype);
		}
    }
    public static boolean isOnClientNetworkingThread() {
        return Thread.currentThread().getName().startsWith("Netty") && Thread.currentThread().getName().contains("Client");
    }
    /**
     * Attaches a {@link Consumer} to run on the intermediary map of item patches (before they get resorted and applied).
     * @param patchApplier The {@link Consumer} to apply onto the intermediary patches.
     */
    public static void builtinPatchCreator(BiConsumer<RegistryAccess, Map<Identifier, ItemPatches>> patchApplier) {
        ADD_DEFAULT_PATCHES.add(patchApplier);
    }
    /**
     * Attaches a {@link Consumer} to run on the intermediary map of enchantment patches (before they get resorted and applied).
     * @param patchApplier The {@link Consumer} to apply onto the intermediary patches.
     */
    public static void builtinEnchantmentPatchCreator(BiConsumer<RegistryAccess, Map<Identifier, EnchantmentPatches>> patchApplier) {
        ADD_DEFAULT_ENCHANT_PATCHES.add(patchApplier);
    }

    public static void patchEnchantments(RegistryAccess registryAccess, List<EnchantmentPatches> reg) {
        var enchantmentLookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        Map<Pair<Enchantment, Enchantment>, Holder.Reference<Enchantment>> modified = new HashMap<>();
        enchantmentLookup.listElements().forEach(enchantment -> {
            ResourceKey<Enchantment> key = enchantment.key();
            Enchantment original = enchantment.value();
            Enchantment base = original;
            if (ORIGINAL_ENCHANTMENTS.containsKey(key))
                base = ORIGINAL_ENCHANTMENTS.get(key);
            EnchantmentBuilder enchantmentBuilder = EnchantmentBuilder.of(base);
            reg.forEach(enchantmentPatches -> enchantmentPatches.apply(enchantment, enchantmentBuilder));
            reg.forEach(enchantmentPatches -> enchantmentPatches.applyGenerators(enchantment, enchantmentBuilder));
            if (!enchantmentBuilder.isChanged()) {
                if (ORIGINAL_ENCHANTMENTS.containsKey(key)) enchantment.defaulted$forceBind(ORIGINAL_ENCHANTMENTS.get(key));
                else return;
            } else enchantment.defaulted$forceBind(enchantmentBuilder.build());
            if (!ORIGINAL_ENCHANTMENTS.containsKey(key)) ORIGINAL_ENCHANTMENTS.put(key, base);
            modified.put(Pair.of(original, enchantment.value()), enchantment);
        });
        if (!(enchantmentLookup instanceof MappedRegistry<Enchantment> enchantmentRegistry)) return;
        MappedRegistryAccessor<Enchantment> accessor = (MappedRegistryAccessor<Enchantment>) enchantmentRegistry;
        modified.forEach((originalAndNew, holder) -> {
            accessor.getByValue().remove(originalAndNew.getFirst());
            accessor.getByValue().put(originalAndNew.getSecond(), holder);
            int id = accessor.getToId().removeInt(originalAndNew.getFirst());
            accessor.getToId().put(originalAndNew.getSecond(), id);
        });
    }
}
