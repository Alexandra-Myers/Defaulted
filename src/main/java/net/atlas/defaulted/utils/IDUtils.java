package net.atlas.defaulted.utils;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;

public class IDUtils {
	public static <T> ResourceLocation identifier(ResourceKey<T> key) {
		//? >= 1.21.11 {
		/*return key.identifier();
		*///?} <1.21.11 {
		return key.location();
		//?}
	}

	public static <S> ResourceLocation getId(CommandContext<S> context, String name) {
		return context.getArgument(name, ResourceLocation.class);
	}

    public static ArgumentType<ResourceLocation> id() {
        return ResourceLocationArgument.id();
    }
}