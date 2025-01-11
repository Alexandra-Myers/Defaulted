package net.atlas.defaulted.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(targets = {"net/minecraft/core/Registry$1"})
public abstract class RegistryMixin {
    @Shadow @Final
    Registry<?> field_40939;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @WrapOperation(method = "getId(Lnet/minecraft/core/Holder;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;value()Ljava/lang/Object;"))
    public Object modValue(Holder<?> instance, Operation<?> original) {
        Optional<? extends ResourceKey<?>> resourceKey = instance.unwrapKey();
        Object result = original.call(instance);
        if (resourceKey.isPresent() && this.field_40939.containsKey((ResourceKey) resourceKey.get()))
            return field_40939.getValue((ResourceKey) resourceKey.get());
        return result;
    }
}
