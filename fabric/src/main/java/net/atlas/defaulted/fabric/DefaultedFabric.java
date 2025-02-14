package net.atlas.defaulted.fabric;

import net.atlas.defaulted.fabric.component.DefaultedRegistries;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.fabricmc.api.ModInitializer;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;

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
        Defaulted.hasOwo = FabricLoader.getInstance().isModLoaded("owo");
        DefaultedRegistries.init();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(Defaulted.id("default_component_patches"), holderLookup -> new FabricDefaultComponentPatchesManager(holderLookup));
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (!client) DefaultComponentPatchesManager.patch();
        });

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
                ServerPlayNetworking.send(player, new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultComponentPatchesManager.cached)));
            }
        });
    }
}
