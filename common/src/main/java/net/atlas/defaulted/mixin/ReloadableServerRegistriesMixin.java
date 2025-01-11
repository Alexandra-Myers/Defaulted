package net.atlas.defaulted.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Lifecycle;
import net.atlas.defaulted.Defaulted;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ReloadableServerRegistries.class)
public class ReloadableServerRegistriesMixin {
    @Unique
    private static final RegistrationInfo STABLE_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.stable());
    @WrapOperation(method = "method_61240", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Lifecycle;experimental()Lcom/mojang/serialization/Lifecycle;"))
    private static Lifecycle makeStable(Operation<Lifecycle> original, @Local(ordinal = 0, argsOnly = true) LootDataType<?> lootDataType) {
        return lootDataType.equals(Defaulted.PATCHES_LOOT_DATA_TYPE) ? Lifecycle.stable() : original.call();
    }
    @WrapOperation(method = "method_61246", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/WritableRegistry;register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;"))
    private static Holder.Reference<?> useStableLifecycle(WritableRegistry<?> instance, ResourceKey<?> tResourceKey, Object t, RegistrationInfo registrationInfo, Operation<Holder.Reference<?>> original) {
        if (tResourceKey.isFor(Defaulted.ITEM_PATCHES)) registrationInfo = STABLE_REGISTRATION_INFO;
        return original.call(instance, tResourceKey, t, registrationInfo);
    }
}
