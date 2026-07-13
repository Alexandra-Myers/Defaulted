package net.atlas.defaulted.init;

import net.atlas.defaulted.component.generators.*;
import net.atlas.defaulted.component.generators.condition.PatchConditions;
import net.atlas.defaulted.enchantment.generators.condition.EnchantmentPatchConditions;
import net.atlas.defaulted.enchantment.value_provider.ValueProviders;
//? fabric {
import net.atlas.defaulted.fabric.util.FabricUtils;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
//?} neoforge {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}
import net.minecraft.core.Registry;
//? fabric
import net.minecraft.resources.ResourceKey;


public class DefaultedRegistries {

    //? neoforge {
    /*public static void init(IEventBus modBus) {
    *///?} fabric {
    public static void init() {
    //?}
        ItemPatchGenerators.INSTANCE.bootstrap(/*? neoforge {*/ /*modBus *//*?}*/);
        EnchantmentPatchGenerators.INSTANCE.bootstrap(/*? neoforge {*/ /*modBus *//*?}*/);
        PatchConditions.INSTANCE.bootstrap(/*? neoforge {*/ /*modBus *//*?}*/);
        EnchantmentPatchConditions.INSTANCE.bootstrap(/*? neoforge {*/ /*modBus *//*?}*/);
        ValueProviders.INSTANCE.bootstrap(/*? neoforge {*/ /*modBus *//*?}*/);
        WeaponLevelBasedValues.INSTANCE.bootstrap(/*? neoforge {*/ /*modBus *//*?}*/);
        LevelConditions.INSTANCE.bootstrap(/*? neoforge {*/ /*modBus *//*?}*/);
    }

    //? fabric {
    public static <T> Registry<T> createRegistry(ResourceKey<Registry<T>> key) {
    //?} neoforge {
    /*public static <T> Registry<T> createRegistry(DeferredRegister<T> register) {
    *///?}
        //? fabric {
        return FabricUtils.createRegistry(
                key
        ).attribute(RegistryAttribute.OPTIONAL).buildAndRegister();
        //?} neoforge {
        /*return register.makeRegistry(builder -> builder.sync(false));
        *///?}
    }
}