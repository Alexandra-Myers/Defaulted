package net.atlas.defaulted.fabric;

import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.Defaulted;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.ResourceLocation;

public class FabricDefaultComponentPatchesManager extends DefaultComponentPatchesManager implements IdentifiableResourceReloadListener {

    public FabricDefaultComponentPatchesManager(Provider arg) {
        super(arg);
    }

    @Override
    public ResourceLocation getFabricId() {
        return Defaulted.id("default_component_patches");
    }
    
}
