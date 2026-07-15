package net.atlas.defaulted.mixin;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.compat.OwoCompat;
import net.atlas.defaulted.extension.ItemStackExtensions;
import net.atlas.defaulted.utils.ReferentialDataComponentMap;
//? >=26.1 {
import net.minecraft.core.Holder;
//?}
import net.minecraft.core.component.DataComponentMap;
//? >=1.21.5
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
//? >=1.21.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
//?}
//? >=26.1 {
import net.minecraft.world.item.Item;
//?}
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;

//? <26.1 {
/*import net.minecraft.world.level.ItemLike;
*///?}
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? >=1.21.5 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//?}
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtensions {
    @Shadow public abstract boolean isEmpty();

    @Mutable
    @Shadow
    @Final
    PatchedDataComponentMap components;

    @Shadow public abstract DataComponentMap getPrototype();

    //? >=1.21.5 {
    @WrapMethod(method = "createOptionalStreamCodec")
    private static StreamCodec<RegistryFriendlyByteBuf, ItemStack> wrapCodec(StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> streamCodec, Operation<StreamCodec<RegistryFriendlyByteBuf, ItemStack>> original) {
        StreamCodec<RegistryFriendlyByteBuf, ItemStack> result = original.call(streamCodec);
        return Defaulted.wrapStreamCodec(result);
    }
    //?}

    //? >=26.1 {
    @Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
    private void appendStack(Holder<Item> itemHolder, int count, PatchedDataComponentMap components, CallbackInfo ci) {
    //?} <26.1 {
    /*@Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
    public void appendStack(ItemLike itemLike, int count, PatchedDataComponentMap patchedDataComponentMap, CallbackInfo ci) {
    *///?}
        PatchedDataComponentMapAccessor accessor = PatchedDataComponentMapAccessor.class.cast(this.components);
        boolean mustWrap = false;
        if (Defaulted.hasOwo) {
            DataComponentMap prototype = accessor.defaulted$getPrototype();
            mustWrap = OwoCompat.isDerived(prototype) && !(OwoCompat.unwrapDerivedComponentMap(prototype) instanceof ReferentialDataComponentMap); // If this is false, OwO is going to do this for us, as it is running after us.
        }
        ReferentialDataComponentMap newPrototype = new ReferentialDataComponentMap(this::getPrototype);
        newPrototype.setOriginal(this.components);
        accessor.defaulted$setPrototype(mustWrap ? defaulted$wrapAsDerivedComponentMap(newPrototype) : newPrototype);
    }

    @Inject(method = "getComponents", at = @At(value = "HEAD"))
    public void validateReferentialPrototype0(CallbackInfoReturnable<DataComponentMap> cir) {
        this.defaulted$updatePrototype();
    }

    @Override
    public void defaulted$updatePrototype() {
        if (!isEmpty()) {
            PatchedDataComponentMapAccessor accessor = PatchedDataComponentMapAccessor.class.cast(this.components);
            DataComponentMap prototype = accessor.defaulted$getPrototype();
            if (Defaulted.hasOwo) prototype = OwoCompat.unwrapDerivedComponentMap(prototype);
            if (!(prototype instanceof ReferentialDataComponentMap)) {
                ReferentialDataComponentMap newPrototype = new ReferentialDataComponentMap(this::getPrototype);
                newPrototype.setOriginal(this.components);
                accessor.defaulted$setPrototype(Defaulted.hasOwo ? defaulted$wrapAsDerivedComponentMap(newPrototype) : newPrototype);
            }
        }
    }

	@Unique
	private DataComponentMap defaulted$wrapAsDerivedComponentMap(DataComponentMap prototype) {
		try {
			Field field = ItemStack.class.getDeclaredField("owo$derivedMap");
            field.setAccessible(true);
			DataComponentMap derived = OwoCompat.deriveComponentMap(ItemStack.class.cast(this), prototype);
			field.set(this, derived);
			return derived;
		} catch (Exception e) {
			Defaulted.LOGGER.error("Failed to wrap as a DerivedComponentMap: ", e);
			throw new RuntimeException(e);
		}
	}
}
