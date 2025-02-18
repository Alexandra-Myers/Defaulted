package net.atlas.defaulted.mixin;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultedExpectPlatform;
import net.atlas.defaulted.extension.ItemStackExtensions;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.lang.reflect.Field;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtensions {
    @Shadow public abstract boolean isEmpty();

    @Mutable
    @Shadow
    @Final
    PatchedDataComponentMap components;

    @Shadow public abstract DataComponentPatch getComponentsPatch();

    @Shadow public abstract DataComponentMap getPrototype();

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
    public void appendStack(ItemLike itemLike, int count, PatchedDataComponentMap patchedDataComponentMap, CallbackInfo ci) {
        Defaulted.ALL_STACKS.add(ItemStack.class.cast(this));
    }

    @Override
    public void defaulted$updatePrototype() {
        if (!isEmpty()) {
            DataComponentMap prototype = PatchedDataComponentMapAccessor.class.cast(components).getPrototype(); // Safe dw gang
            DataComponentMap newPrototype = getPrototype();
            if (Defaulted.hasOwo) newPrototype = defaulted$wrapAsDerivedComponentMap(newPrototype);
            if (prototype.equals(newPrototype)) return;
            PatchedDataComponentMap newMap = new PatchedDataComponentMap(newPrototype);
            newMap.applyPatch(getComponentsPatch());
            components = newMap;
        }
    }

	@Unique
	private DataComponentMap defaulted$wrapAsDerivedComponentMap(DataComponentMap prototype) {
		try {
			Field field = ItemStack.class.getDeclaredField("owo$derivedMap");
			DataComponentMap derived = DefaultedExpectPlatform.createDerivedMap(ItemStack.class.cast(this), prototype);
			field.set(this, derived);
			return derived;
		} catch (Exception e) {
			Defaulted.LOGGER.error("Failed to wrap as a DerivedComponentMap: ", e);
			throw new RuntimeException(e);
		}
	}
    @Mixin(targets = {"net.minecraft.world.item.ItemStack$1"})
    public static class StreamCodecMixin {
        @WrapMethod(method = "encode")
        public void wrapEncode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack itemStack, Operation<Void> original) {
            if (!itemStack.isEmpty()) {
                DataComponentMap prototype = PatchedDataComponentMapAccessor.class.cast(itemStack.getComponents()).getPrototype();
                if (DefaultedExpectPlatform.isSyncingPlayerUnmodded() && prototype instanceof PatchedDataComponentMap prototypeDataComponentMap) {
                    ItemStack newStack = itemStack.copy();
                    if (newStack.getComponents() instanceof PatchedDataComponentMap patchedDataComponentMap) {
                        patchedDataComponentMap.restorePatch(prototypeDataComponentMap.asPatch());
                        patchedDataComponentMap.applyPatch(itemStack.getComponentsPatch());
                    }
                    itemStack = newStack;
                }
            }
            original.call(registryFriendlyByteBuf, itemStack);
        }
    }
}
