package net.atlas.defaulted.mixin;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultedExpectPlatform;
import net.atlas.defaulted.extension.ItemStackExtensions;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;

import java.lang.reflect.Field;

import org.jetbrains.annotations.NotNull;
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

    @WrapMethod(method = "createOptionalStreamCodec")
    private static StreamCodec<RegistryFriendlyByteBuf, ItemStack> wrapCodec(StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> streamCodec, Operation<StreamCodec<RegistryFriendlyByteBuf, ItemStack>> original) {
        StreamCodec<RegistryFriendlyByteBuf, ItemStack> result = original.call(streamCodec);
        return defaulted$wrapStreamCodec(result);
    }

    @Unique
    private static StreamCodec<RegistryFriendlyByteBuf, ItemStack> defaulted$wrapStreamCodec(StreamCodec<RegistryFriendlyByteBuf, ItemStack> original) {
        return new StreamCodec<>() {
            @Override
            public @NotNull ItemStack decode(RegistryFriendlyByteBuf buffer) {
                ItemStack stack = original.decode(buffer);
                DataComponentPatch patch = stack.getComponentsPatch();
                if (stack.getComponents() instanceof PatchedDataComponentMap patchedDataComponentMap) {
                    patchedDataComponentMap.clearPatch();
                    patchedDataComponentMap.applyPatch(patch);
                }
                return stack;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, ItemStack stack) {
                ItemStack newStack = stack.copy();
                if (!stack.isEmpty()) {
                    DataComponentMap prototype = PatchedDataComponentMapAccessor.class.cast(stack.getComponents()).getPrototype();
                    if (prototype instanceof PatchedDataComponentMap prototypeDataComponentMap) {
                        if (newStack.getComponents() instanceof PatchedDataComponentMap patchedDataComponentMap) {
                            patchedDataComponentMap.restorePatch(prototypeDataComponentMap.asPatch());
                            patchedDataComponentMap.applyPatch(stack.getComponentsPatch());
                        }
                    }
                }
                original.encode(buffer, newStack);
            }
        };
    }

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
}
