package net.atlas.defaulted;

import com.mojang.serialization.MapCodec;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.backport.Enchantable;
import net.atlas.defaulted.component.backport.Repairable;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

public interface DefaultedExpectPlatform {
    @ExpectPlatform
    static boolean isSyncingPlayerUnmodded() {
        throw new AssertionError();
    }
    @ExpectPlatform
    static Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry() {
        throw new AssertionError();
    }
    @ExpectPlatform
    static Registry<DataComponentType<?>> getPhantomDataComponentTypeRegistry() {
        throw new AssertionError();
    }
    @ExpectPlatform
    static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }
    @ExpectPlatform
    static DataComponentMap createDerivedMap(ItemStack itemStack, DataComponentMap prototype) {
        throw new AssertionError();
    }
    @ExpectPlatform
    static DataComponentType<Enchantable> getEnchantable() {
        throw new AssertionError();
    }
    @ExpectPlatform
    static DataComponentType<Repairable> getRepairable() {
        throw new AssertionError();
    }
}
