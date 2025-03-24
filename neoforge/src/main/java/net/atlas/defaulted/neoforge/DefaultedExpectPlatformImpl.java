package net.atlas.defaulted.neoforge;

import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.backport.Enchantable;
import net.atlas.defaulted.component.backport.Repairable;
import net.atlas.defaulted.neoforge.backport.BackportedComponents;
import net.atlas.defaulted.neoforge.component.DefaultedRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

@SuppressWarnings("unused")
public class DefaultedExpectPlatformImpl {
    public static boolean isSyncingPlayerUnmodded() {
        return false;
    }
    public static Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry() {
        return DefaultedRegistries.PATCH_GENERATOR_TYPE_REG;
    }
    public static Registry<DataComponentType<?>> getPhantomDataComponentTypeRegistry() {
        return BackportedComponents.PHANTOM_COMPONENT_TYPE_REG;
    }
    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
    public static DataComponentMap createDerivedMap(ItemStack itemStack, DataComponentMap prototype) {
        return prototype;
    }
    public static DataComponentType<Enchantable> getEnchantable() {
        return BackportedComponents.ENCHANTABLE.get();
    }
    public static DataComponentType<Repairable> getRepairable() {
        return BackportedComponents.REPAIRABLE.get();
    }
}
