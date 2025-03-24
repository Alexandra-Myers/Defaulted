package net.atlas.defaulted.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.atlas.defaulted.DefaultedExpectPlatform;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {
    @WrapOperation(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isValidRepairItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    public boolean useComponent(Item instance, ItemStack itemStack, ItemStack itemStack2, Operation<Boolean> original) {
        return itemStack2.getItem().defaulted$has(DefaultedExpectPlatform.getRepairable()) ? itemStack2.getItem().defaulted$get(DefaultedExpectPlatform.getRepairable()).isValidRepairItem(itemStack2) : original.call(instance, itemStack, itemStack2);
    }
}
