package net.atlas.defaulted.fabric.compat;

import io.wispforest.owo.ext.DerivedComponentMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;

public class OwoCompat {
	public static DataComponentMap deriveComponentMap(ItemStack itemStack, DataComponentMap prototype) {
		DerivedComponentMap derived = new DerivedComponentMap(prototype);
		derived.derive(itemStack);
		return derived;
    }
}
