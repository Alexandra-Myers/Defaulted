package net.atlas.defaulted.fabric.client;

import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class DefaultedFabricClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundDefaultComponentsSyncPacket.TYPE, (clientboundDefaultComponentsSyncPacket, context) -> {
            if (!Minecraft.getInstance().hasSingleplayerServer()) DefaultComponentPatchesManager.loadClientCache(clientboundDefaultComponentsSyncPacket.list());
            else DefaultComponentPatchesManager.setClientCache();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> DefaultComponentPatchesManager.clearClient());
    }
}
