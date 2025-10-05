package net.atlas.defaulted.fabric;

import net.atlas.defaulted.fabric.backport.BackportedComponents;
import net.atlas.defaulted.fabric.component.DefaultedRegistries;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.fabricmc.api.ModInitializer;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.*;

public final class DefaultedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Defaulted.init();
        DefaultedRegistries.init();
        BackportedComponents.registerDataComponents();
        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if (!client) DefaultComponentPatchesManager.getInstance().load();
        });

        PayloadTypeRegistry.playS2C().register(ClientboundDefaultComponentsSyncPacket.TYPE, ClientboundDefaultComponentsSyncPacket.CODEC);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if (ServerPlayNetworking.canSend(player, ClientboundDefaultComponentsSyncPacket.TYPE)) {
                ServerPlayNetworking.send(player, new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultComponentPatchesManager.getCached())));
            }
        });
    }
}
