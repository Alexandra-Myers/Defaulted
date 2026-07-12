package net.atlas.defaulted;

import com.mojang.serialization.MapCodec;

//? fabric {
/*import net.atlas.defaulted.fabric.DefaultedPlatformFabric;
*///?}
//? neoforge {
import net.atlas.defaulted.neoforge.DefaultedPlatformNeoForge;
 //?}
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.enchantment.EnchantmentPatchGenerator;
import net.minecraft.core.Registry;

public interface DefaultedPlatform {
    //? fabric {
    /*DefaultedPlatform INSTANCE = new DefaultedPlatformFabric();
    *///?}
    //? neoforge {
    DefaultedPlatform INSTANCE = new DefaultedPlatformNeoForge();
     //?}
    Registry<MapCodec<? extends PatchGenerator>> getPatchGenRegistry();
    Registry<MapCodec<? extends EnchantmentPatchGenerator>> getEnchantmentPatchGenRegistry();

    boolean isOnClientNetworkingThread();
}
