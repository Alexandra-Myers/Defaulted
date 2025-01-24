package net.atlas.defaulted.component;

import java.util.Iterator;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.atlas.defaulted.mixin.PatchedDataComponentMapAccessor;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;

public class DefaultedDataComponentMap implements DataComponentMap {
	private final PatchedDataComponentMap core;

	public DefaultedDataComponentMap(DataComponentMap prototype, DataComponentPatch patch) {
		core = new PatchedDataComponentMap(prototype);
		core.applyPatch(patch);
	}

    public DefaultedDataComponentMap(PatchedDataComponentMap core) {
		this.core = core;
	}

	@Nullable
	public <T> T get(DataComponentType<? extends T> dataComponentType) {
		return core.get(dataComponentType);
	}

	public Set<DataComponentType<?>> keySet() {
		return core.keySet();
	}

	public Iterator<TypedDataComponent<?>> iterator() {
		return core.iterator();
	}

	public int size() {
		return core.size();
	}

	public DataComponentPatch asPatch() {
		return core.asPatch();
	}

	public DataComponentMap getPrototype() {
		return PatchedDataComponentMapAccessor.class.cast(core).getPrototype();
	}

	public boolean equals(Object object) {
		if (this == object) return true;
		if (object instanceof DefaultedDataComponentMap defaultedDataComponentMap) return this.core.equals(defaultedDataComponentMap.core);
		return false;
	}

	public int hashCode() {
		return this.core.hashCode();
	}

	public String toString() {
		return "Defaulted DataComponentMap" + core.toString();
	}
}
