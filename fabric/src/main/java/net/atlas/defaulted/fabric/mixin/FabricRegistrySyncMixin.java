package net.atlas.defaulted.fabric.mixin;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.atlas.defaulted.fabric.DefaultedExpectPlatformImpl;
import net.atlas.defaulted.fabric.component.DefaultedDataComponents;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

@Mixin(RegistrySyncManager.class)
public class FabricRegistrySyncMixin {
    @WrapOperation(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"), require = 0)
    private static ResourceLocation defaulted$skipToolMaterialForUnmodded(Registry<?> instance, Object t, Operation<ResourceLocation> original) {
        if (DefaultedExpectPlatformImpl.isSyncingPlayerUnmodded() && Objects.equals(t, DefaultedDataComponents.TOOL_MATERIAL)) {
            return null;
        }
        return original.call(instance, t);
    }
}
