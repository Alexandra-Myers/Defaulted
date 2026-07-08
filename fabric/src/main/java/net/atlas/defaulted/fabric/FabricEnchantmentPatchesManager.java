package net.atlas.defaulted.fabric;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.EnchantmentPatchesManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;

public class FabricEnchantmentPatchesManager extends EnchantmentPatchesManager implements IdentifiableResourceReloadListener {

    public FabricEnchantmentPatchesManager(Provider arg) {
        super(arg);
    }

    @Override
    public ResourceLocation getFabricId() {
        return Defaulted.id("enchantment_patches");
    }
    
}