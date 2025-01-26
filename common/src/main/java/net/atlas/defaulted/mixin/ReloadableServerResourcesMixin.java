package net.atlas.defaulted.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.ItemPatches;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @WrapMethod(method = "loadResources")
    private static CompletableFuture<ReloadableServerResources> applyComponentsOnLoad(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, List<Registry.PendingTags<?>> list, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, int i, Executor executor, Executor executor2, Operation<CompletableFuture<ReloadableServerResources>> original) {
        CompletableFuture<ReloadableServerResources> res = original.call(resourceManager, layeredRegistryAccess, list, featureFlagSet, commandSelection, i, executor, executor2);
        return res.thenApplyAsync(reloadableServerResources -> {
            HolderGetter<ItemPatches> getter = reloadableServerResources.fullRegistries().lookup().lookupOrThrow(Defaulted.ITEM_PATCHES);
            Collection<ItemPatches> reg = reloadableServerResources.fullRegistries().getKeys(Defaulted.ITEM_PATCHES).stream()
                    .sorted(Comparator.nullsFirst(Comparator.naturalOrder())).filter(Objects::nonNull)
                    .map(resourceLocation -> getter.getOrThrow(ResourceKey.create(Defaulted.ITEM_PATCHES, resourceLocation)))
                    .map(Holder::value)
                    .sorted(Comparator.naturalOrder())
                    .toList();
            Defaulted.patchItemComponents(reg);
            Defaulted.EXECUTE_ON_RELOAD.forEach(collectionConsumer -> collectionConsumer.accept(reg));
            return reloadableServerResources;
        });
    }
}
