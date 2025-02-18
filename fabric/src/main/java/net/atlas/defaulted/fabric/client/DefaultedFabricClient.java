package net.atlas.defaulted.fabric.client;

import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class DefaultedFabricClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundDefaultComponentsSyncPacket.TYPE, (clientboundDefaultComponentsSyncPacket, context) -> {
            DefaultComponentPatchesManager.loadClientCache(clientboundDefaultComponentsSyncPacket.list());
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DefaultComponentPatchesManager.clear(); 
        });
    }
}
