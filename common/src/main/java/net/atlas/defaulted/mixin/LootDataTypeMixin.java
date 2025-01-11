package net.atlas.defaulted.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.atlas.defaulted.Defaulted;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Mixin(LootDataType.class)
public class LootDataTypeMixin {
    @ModifyReturnValue(method = "values", at = @At("RETURN"))
    private static Stream<LootDataType<?>> append(Stream<LootDataType<?>> original) {
        List<LootDataType<?>> modifiable = new ArrayList<>(original.toList());
        modifiable.add(Defaulted.PATCHES_LOOT_DATA_TYPE);
        return modifiable.stream();
    }
}
