package net.atlas.defaulted.fabric.client;

import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.EnchantmentPatchesManager;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.atlas.defaulted.networking.ClientboundEnchantmentsSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
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
        ClientPlayNetworking.registerGlobalReceiver(ClientboundEnchantmentsSyncPacket.TYPE, (clientboundEnchantmentsSyncPacket, context) -> {
            if (!Minecraft.getInstance().hasSingleplayerServer()) EnchantmentPatchesManager.loadClientCache(context.packetContext().get(PacketContext.REGISTRY_ACCESS), clientboundEnchantmentsSyncPacket.list());
            else EnchantmentPatchesManager.setClientCache(context.packetContext().get(PacketContext.REGISTRY_ACCESS));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DefaultComponentPatchesManager.clearClient();
            EnchantmentPatchesManager.clearClient();
        });
    }
}
