package net.atlas.defaulted.mixin;

//? <1.21.5 {
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.defaulted.DefaultedPlatform;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
//?}
//? >=1.21.5
//import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(/*? <1.21.5 {*/ targets = {"net.minecraft.world.item.ItemStack$1"} /*?} else {*/ /*ResourceLocation.class*/ /*?}*/)
public class ItemStackStreamCodecMixin {
    //? <1.21.5 {
    @WrapMethod(method = "decode")
    public ItemStack wrapDecode(RegistryFriendlyByteBuf buffer, Operation<ItemStack> original) {
        return original.call(buffer);
    }
    @WrapMethod(method = "encode")
    public void wrapEncode(RegistryFriendlyByteBuf buffer, ItemStack stack, Operation<Void> original) {
        if (DefaultedPlatform.INSTANCE.isOnClientNetworkingThread()) {
            original.call(buffer, stack);
            return;
        }
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
        original.call(buffer, newStack);
    }
    //?}
}