package net.atlas.defaulted.fabric;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.Defaulted;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

public class FabricDefaultComponentPatchesManager extends DefaultComponentPatchesManager implements IdentifiableResourceReloadListener {

    private final Provider registries;

    public FabricDefaultComponentPatchesManager(Provider arg) {
        super();
        this.registries = arg;
    }

    @Override
    public ResourceLocation getFabricId() {
        return Defaulted.id("default_component_patches");
    }

    @Override
    public RegistryOps<JsonElement> makeOps() {
        return registries.createSerializationContext(JsonOps.INSTANCE);
    }
}
