package net.atlas.defaulted.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PatchedDataComponentMap.class)
public interface PatchedDataComponentMapAccessor {
	@Accessor
	DataComponentMap getPrototype();
}