package net.atlas.defaulted.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.defaulted.fabric.DefaultedExpectPlatformImpl;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RegistrySyncManager.class)
public class RegistrySyncManagerMixin {
    @WrapOperation(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/event/registry/RegistryAttributeHolder;hasAttribute(Lnet/fabricmc/fabric/api/event/registry/RegistryAttribute;)Z"), require = 0, remap = false)
    private static boolean skipDefaultedRegistries(RegistryAttributeHolder instance, RegistryAttribute registryAttribute, Operation<Boolean> original, @Local(ordinal = 0) Registry<?> reg) {
        if (registryAttribute == RegistryAttribute.MODDED && reg.key().location().getNamespace().equals("defaulted")) {
            return false;
        }

        return original.call(instance, registryAttribute);
    }

    @WrapOperation(method = "createAndPopulateRegistryMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"), require = 0)
    private static ResourceLocation polymer_registry_sync$skipServerEntries(Registry<?> instance, Object t, Operation<ResourceLocation> original) {
        ResourceLocation loc = original.call(instance, t);
        if (loc.getNamespace().equals("defaulted")) {
            return null;
        }
        return loc;
    }
}
