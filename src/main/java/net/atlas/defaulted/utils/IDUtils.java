package net.atlas.defaulted.utils;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class IDUtils {
	public static <T> Identifier identifier(ResourceKey<T> key) {
		//? >= 1.21.11 {
		return key.identifier();
		//?} <1.21.11 {
		/*return key.location();
		*///?}
	}
}