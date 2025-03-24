package net.atlas.defaulted.extension;

import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import org.jetbrains.annotations.Nullable;

public interface ItemExtensions {
    <T> @Nullable T defaulted$get(DataComponentType<T> phantomType);

    <T> @Nullable T defaulted$getOrDefault(DataComponentType<T> phantomType, T defaultValue);

    boolean defaulted$has(DataComponentType<?> phantomType);

    <T> void defaulted$set(DataComponentType<T> phantomType, T phantom);

    PatchedDataComponentMap defaulted$getPhantomComponentMap();

    default ToolMaterialWrapper defaulted$getToolMaterial() {
      throw new IllegalStateException("Extension has not been applied");
    }
    default void defaulted$setToolMaterial(ToolMaterialWrapper newTier) {
      throw new IllegalStateException("Extension has not been applied");
    }
}
