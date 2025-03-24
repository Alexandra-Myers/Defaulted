package net.atlas.defaulted.neoforge.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.atlas.defaulted.DefaultedExpectPlatform;
import net.atlas.defaulted.component.backport.Enchantable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
    @ModifyExpressionValue(method = "getEnchantmentCost", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentValue()I"))
    private static int getEnchantmentValue(int original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
        if (!stack.getItem().defaulted$has(DefaultedExpectPlatform.getEnchantable())) stack.getItem().defaulted$set(DefaultedExpectPlatform.getEnchantable(), new Enchantable(original));
        return stack.getItem().defaulted$getOrDefault(DefaultedExpectPlatform.getEnchantable(), Enchantable.EMPTY).value();
    }

    @ModifyExpressionValue(method = "selectEnchantment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentValue()I"))
    private static int getEnchantmentValue1(int original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
        if (!stack.getItem().defaulted$has(DefaultedExpectPlatform.getEnchantable())) stack.getItem().defaulted$set(DefaultedExpectPlatform.getEnchantable(), new Enchantable(original));
        return stack.getItem().defaulted$getOrDefault(DefaultedExpectPlatform.getEnchantable(), Enchantable.EMPTY).value();
    }
}
