package net.atlas.defaulted.fabric.client;

import net.atlas.defaulted.Defaulted;
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
        ClientPlayNetworking.registerGlobalReceiver(ClientboundDefaultComponentsSyncPacket.TYPE, (clientboundDefaultComponentsSyncPacket, context) -> Defaulted.patchItemComponents(clientboundDefaultComponentsSyncPacket.list()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> Defaulted.EXECUTE_ON_RELOAD.clear());
    }
}
