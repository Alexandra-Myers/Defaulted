package net.atlas.defaulted.mixin;

import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.atlas.defaulted.extension.ItemExtensions;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public class ItemMixin implements ItemExtensions {
  @Unique
  public ToolMaterialWrapper toolMaterialWrapper = null;
  
  @Override
  public ToolMaterialWrapper defaulted$getToolMaterial() {
	  return toolMaterialWrapper;
  }

  @Override
  public void defaulted$setToolMaterial(ToolMaterialWrapper newTier) {
	  toolMaterialWrapper = newTier;
  }
}
