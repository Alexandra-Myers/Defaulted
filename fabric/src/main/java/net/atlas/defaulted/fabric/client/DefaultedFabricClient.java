package net.atlas.defaulted.fabric.client;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.fabric.DefaultedFabric;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class DefaultedFabricClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(DefaultedFabric.ClientboundDefaultComponentsSyncPacket.TYPE, (clientboundDefaultComponentsSyncPacket, context) -> Defaulted.patchItemComponents(clientboundDefaultComponentsSyncPacket.list()));
    }
}
