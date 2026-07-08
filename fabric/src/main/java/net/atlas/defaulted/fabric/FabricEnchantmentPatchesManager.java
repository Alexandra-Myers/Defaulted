package net.atlas.defaulted.fabric;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.EnchantmentPatchesManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

public class FabricEnchantmentPatchesManager extends EnchantmentPatchesManager implements IdentifiableResourceReloadListener {

    private final Provider registries;

    public FabricEnchantmentPatchesManager(Provider arg) {
        super();
        this.registries = arg;
    }

    @Override
    public ResourceLocation getFabricId() {
        return Defaulted.id("enchantment_patches");
    }

    @Override
    public RegistryOps<JsonElement> makeOps() {
        return registries.createSerializationContext(JsonOps.INSTANCE);
    }
    
}
