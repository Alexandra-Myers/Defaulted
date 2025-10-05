package net.atlas.defaulted.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.fabric.FabricDefaultComponentPatchesManager;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Unique
    private DefaultComponentPatchesManager defaultComponentPatchesManager;
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void addDefaultComponentPatchesManager(RegistryAccess.Frozen frozen, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, int i, CallbackInfo ci) {
        this.defaultComponentPatchesManager = new FabricDefaultComponentPatchesManager(frozen);
    }
    @WrapMethod(method = "listeners")
    public List<PreparableReloadListener> withDefaultComponentPatches(Operation<List<PreparableReloadListener>> original) {
        List<PreparableReloadListener> preparableReloadListeners = new ArrayList<>(original.call());
        preparableReloadListeners.add(defaultComponentPatchesManager);
        return preparableReloadListeners;
    }
}
