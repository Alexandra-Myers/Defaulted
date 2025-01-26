package net.atlas.defaulted.fabric.component;

import java.util.function.UnaryOperator;

import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public class DefaultedDataComponents {
	public static DataComponentType<ToolMaterialWrapper> TOOL_MATERIAL = register(
		"defaulted:tool_material", builder -> builder.persistent(ToolMaterialWrapper.CODEC).networkSynchronized(ByteBufCodecs.fromCodecTrusted(ToolMaterialWrapper.CODEC))
	);
	private static <T> DataComponentType<T> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE, string, unaryOperator.apply(DataComponentType.builder()).build()
		);
	}
	public static void registerDataComponents() {

	}
}
