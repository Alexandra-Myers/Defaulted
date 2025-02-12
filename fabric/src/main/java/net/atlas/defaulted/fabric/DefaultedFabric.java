package net.atlas.defaulted.fabric;

import net.atlas.defaulted.fabric.component.DefaultedRegistries;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.fabricmc.api.ModInitializer;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultedDataReloadListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.*;

public final class DefaultedFabric implements ModInitializer {
	public static final List<UUID> unmoddedPlayers = new ArrayList<>();
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Defaulted.init();
        DefaultedRegistries.init();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(Defaulted.id("default_component_patches"), holderLookup -> new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Defaulted.id("default_component_patches");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                DefaultedDataReloadListener.reload(holderLookup, manager);
            }
        });
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> DefaultedDataReloadListener.patch());

        PayloadTypeRegistry.playS2C().register(ClientboundDefaultComponentsSyncPacket.TYPE, ClientboundDefaultComponentsSyncPacket.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!ServerPlayNetworking.canSend(handler.getPlayer(), ClientboundDefaultComponentsSyncPacket.TYPE)) {
                unmoddedPlayers.add(handler.getPlayer().getUUID());
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (unmoddedPlayers.contains(handler.getPlayer().getUUID())) {
                unmoddedPlayers.remove(handler.getPlayer().getUUID());
            }
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if (ServerPlayNetworking.canSend(player, ClientboundDefaultComponentsSyncPacket.TYPE)) {
                ServerPlayNetworking.send(player, new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultedDataReloadListener.cached)));
            }
        });
    }
}
