package net.atlas.defaulted.neoforge.component;

import java.util.function.UnaryOperator;

import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DefaultedDataComponents {
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, "defaulted");
	public static DeferredHolder<DataComponentType<?>, DataComponentType<ToolMaterialWrapper>> TOOL_MATERIAL = register(
		"tool_material", builder -> builder.persistent(ToolMaterialWrapper.CODEC).networkSynchronized(ByteBufCodecs.fromCodecTrusted(ToolMaterialWrapper.CODEC))
	);
	private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return DATA_COMPONENTS.register(
			string, () -> unaryOperator.apply(DataComponentType.builder()).build()
		);
	}
	public static void registerDataComponents(IEventBus modBus) {
		DATA_COMPONENTS.register(modBus);
	}
}
