package net.atlas.defaulted.extension;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

import java.util.Optional;

public interface DataComponentUpdateConsumer {
    <T> void set(DataComponentType<T> type, T value);
    <T> void remove(DataComponentType<T> type);
    void applyPatch(DataComponentPatch patch);
    void applyPatch(DataComponentType<?> type, Optional<?> value);
    void restorePatch(DataComponentPatch patch);
    void clearPatch();
    void setAll(DataComponentMap components);
}
