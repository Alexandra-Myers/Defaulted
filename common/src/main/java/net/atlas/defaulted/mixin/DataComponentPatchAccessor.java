package net.atlas.defaulted.mixin;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(DataComponentPatch.class)
public interface DataComponentPatchAccessor {
    @Invoker("<init>")
    static DataComponentPatch create(final Reference2ObjectMap<DataComponentType<?>, Optional<?>> map) {
        throw new AssertionError();
    }
    @Accessor
    Reference2ObjectMap<DataComponentType<?>, Optional<?>> getMap();
}
