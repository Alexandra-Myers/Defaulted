package net.atlas.defaulted.fabric;

import net.atlas.defaulted.EnchantmentPatchesManager;
import net.atlas.defaulted.fabric.component.DefaultedRegistries;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.atlas.defaulted.networking.ClientboundEnchantmentsSyncPacket;
import net.fabricmc.api.ModInitializer;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class DefaultedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Defaulted.init();
        Defaulted.hasOwo = FabricLoader.getInstance().isModLoaded("owo");
        DefaultedRegistries.init();
        ResourceLocation defaultComponentPatches = Defaulted.id("default_component_patches");
        ResourceLocation enchantmentPatches = Defaulted.id("enchantment_patches");
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(defaultComponentPatches, FabricDefaultComponentPatchesManager::new);
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(enchantmentPatches, FabricEnchantmentPatchesManager::new);
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (!client) {
                DefaultComponentPatchesManager.getInstance().load();
                EnchantmentPatchesManager.getInstance().load(registries);
            }
        });
        PayloadTypeRegistry.playS2C().register(ClientboundDefaultComponentsSyncPacket.TYPE, ClientboundDefaultComponentsSyncPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundEnchantmentsSyncPacket.TYPE, ClientboundEnchantmentsSyncPacket.CODEC);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if (ServerPlayNetworking.canSend(player, ClientboundDefaultComponentsSyncPacket.TYPE))
                ServerPlayNetworking.send(player, new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultComponentPatchesManager.getCached())));
            if (ServerPlayNetworking.canSend(player, ClientboundEnchantmentsSyncPacket.TYPE))
                ServerPlayNetworking.send(player, new ClientboundEnchantmentsSyncPacket(new ArrayList<>(EnchantmentPatchesManager.getCached(player.registryAccess()))));
        });
    }

    public static Function<HolderLookup.Provider, IdentifiableResourceReloadListener> wrap(ResourceLocation id, Function<HolderLookup.Provider, PreparableReloadListener> listenerFactory, ResourceLocation... dependencies) {
        return provider -> {
            var unwrapped = listenerFactory.apply(provider);
            return new IdentifiableResourceReloadListener() {
                @Override
                public ResourceLocation getFabricId() {
                    return id;
                }

                @Override
                public Collection<ResourceLocation> getFabricDependencies() {
                    return List.of(dependencies);
                }

                @Override
                public @NotNull CompletableFuture<Void> reload(SharedState sharedState, Executor executor, PreparationBarrier preparationBarrier, Executor executor2) {
                    return unwrapped.reload(sharedState, executor, preparationBarrier, executor2);
                }

                @Override
                public void prepareSharedState(SharedState sharedState) {
                    unwrapped.prepareSharedState(sharedState);
                }

                @Override
                public @NotNull String getName() {
                    return unwrapped.getName();
                }
            };
        };
    }
}
