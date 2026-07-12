package net.atlas.defaulted.extension;

import net.atlas.defaulted.component.ToolMaterialWrapper;

public interface ItemExtensions {
    default ToolMaterialWrapper defaulted$getToolMaterial() {
      throw new IllegalStateException("Extension has not been applied");
    }
    default void defaulted$setToolMaterial(ToolMaterialWrapper newTier) {
      throw new IllegalStateException("Extension has not been applied");
    }
}
