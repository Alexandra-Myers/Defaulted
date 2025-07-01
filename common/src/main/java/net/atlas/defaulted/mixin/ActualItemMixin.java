package net.atlas.defaulted.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.atlas.defaulted.extension.WrapHashedStreamCodec;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HashedStack.ActualItem.class)
public class ActualItemMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> patchCodec(StreamCodec<RegistryFriendlyByteBuf, HashedStack.ActualItem> original) {
        return new WrapHashedStreamCodec(original);
    }
}
