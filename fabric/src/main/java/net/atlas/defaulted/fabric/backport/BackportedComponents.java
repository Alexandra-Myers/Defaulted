package net.atlas.defaulted.fabric.backport;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.backport.Enchantable;
import net.atlas.defaulted.component.backport.Repairable;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.function.UnaryOperator;

public class BackportedComponents {
    public static final Registry<DataComponentType<?>> PHANTOM_COMPONENT_TYPE_REG = FabricRegistryBuilder.createSimple(
            Defaulted.PHANTOM_COMPONENT_TYPE
    ).buildAndRegister();
    public static DataComponentType<Enchantable> ENCHANTABLE = register(
            "enchantable", builder -> builder.persistent(Enchantable.CODEC).networkSynchronized(ByteBufCodecs.INT.map(Enchantable::new, Enchantable::value))
    );
    public static DataComponentType<Repairable> REPAIRABLE = register(
            "repairable", builder -> builder.persistent(Repairable.CODEC).networkSynchronized(ByteBufCodecs.fromCodecWithRegistriesTrusted(Repairable.CODEC))
    );
    private static <T> DataComponentType<T> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
        return Registry.register(
                PHANTOM_COMPONENT_TYPE_REG, string, unaryOperator.apply(DataComponentType.builder()).build()
        );
    }
    public static void registerDataComponents() {

    }
}
