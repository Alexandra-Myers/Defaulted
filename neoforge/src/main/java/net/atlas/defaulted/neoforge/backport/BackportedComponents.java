package net.atlas.defaulted.neoforge.backport;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.backport.Enchantable;
import net.atlas.defaulted.component.backport.Repairable;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class BackportedComponents {
    private static final DeferredRegister<DataComponentType<?>> PHANTOM_COMPONENT_TYPES = DeferredRegister.create(Defaulted.PHANTOM_COMPONENT_TYPE, "minecraft");

    public static final Registry<DataComponentType<?>> PHANTOM_COMPONENT_TYPE_REG =
            PHANTOM_COMPONENT_TYPES.makeRegistry(builder -> builder.sync(false));
    public static DeferredHolder<DataComponentType<?>, DataComponentType<Enchantable>> ENCHANTABLE = BackportedComponents.register(
            "enchantable", builder -> builder.persistent(Enchantable.CODEC).networkSynchronized(ByteBufCodecs.INT.map(Enchantable::new, Enchantable::value))
    );
    public static DeferredHolder<DataComponentType<?>, DataComponentType<Repairable>> REPAIRABLE = register(
            "repairable", builder -> builder.persistent(Repairable.CODEC).networkSynchronized(ByteBufCodecs.fromCodecWithRegistriesTrusted(Repairable.CODEC))
    );

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
        return PHANTOM_COMPONENT_TYPES.register(
                string, () -> unaryOperator.apply(DataComponentType.builder()).build()
        );
    }
    public static void init(IEventBus modBus) {
        PHANTOM_COMPONENT_TYPES.register(modBus);
    }
}
