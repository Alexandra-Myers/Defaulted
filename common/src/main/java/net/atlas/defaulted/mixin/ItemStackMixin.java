package net.atlas.defaulted.mixin;

import net.atlas.defaulted.DefaultedExpectPlatform;
import net.atlas.defaulted.component.DefaultedDataComponentMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract boolean isEmpty();

    @Mutable
    @Shadow
    @Final
    PatchedDataComponentMap components;

    @Shadow public abstract DataComponentPatch getComponentsPatch();

    @Shadow public abstract DataComponentMap getPrototype();

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void updatePrototypeTick(Level level, Entity entity, int i, boolean bl, CallbackInfo ci) {
        if (entity.tickCount % 20 == 0) {
            defaulted$updatePrototype();
        }
    }
    @Inject(method = "getTooltipLines", at = @At(value = "HEAD"))
    public void updatePrototypeBeforeTooltip(Item.TooltipContext tooltipContext, Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        defaulted$updatePrototype();
    }

    @Unique
    public void defaulted$updatePrototype() {
        if (!isEmpty()) {
            DataComponentMap prototype = PatchedDataComponentMapAccessor.class.cast(components).getPrototype(); // Safe dw gang
            if (prototype.equals(getPrototype())) return;
            PatchedDataComponentMap newMap = new PatchedDataComponentMap(getPrototype());
            newMap.applyPatch(getComponentsPatch());
            components = newMap;
        }
    }
    @Mixin(targets = {"net.minecraft.world.item.ItemStack$1"})
    public static class StreamCodecMixin {
        @WrapMethod(method = "encode")
        public void wrapEncode(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack itemStack, Operation<Void> original) {
            DataComponentMap prototype = PatchedDataComponentMapAccessor.class.cast(itemStack.getComponents()).getPrototype();
            if (DefaultedExpectPlatform.isSyncingPlayerUnmodded() && prototype instanceof DefaultedDataComponentMap defaultedDataComponentMap) {
                ItemStack newStack = itemStack.copy();
                if (newStack.getComponents() instanceof PatchedDataComponentMap patchedDataComponentMap) {
                    patchedDataComponentMap.restorePatch(defaultedDataComponentMap.asPatch());
                    patchedDataComponentMap.applyPatch(itemStack.getComponentsPatch());
                }
            }
            original.call(registryFriendlyByteBuf, itemStack);
        }
    }
}
