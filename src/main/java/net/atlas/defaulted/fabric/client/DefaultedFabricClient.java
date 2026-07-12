package net.atlas.defaulted.fabric.client;

//? fabric {
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.EnchantmentPatchesManager;
import net.atlas.defaulted.fabric.util.FabricUtils;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.atlas.defaulted.networking.ClientboundEnchantmentsSyncPacket;
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
            else DefaultComponentPatchesManager.setClientCache(FabricUtils.getRegistryAccess(context));
        });
        ClientPlayNetworking.registerGlobalReceiver(ClientboundEnchantmentsSyncPacket.TYPE, (clientboundEnchantmentsSyncPacket, context) -> {
            if (!Minecraft.getInstance().hasSingleplayerServer()) EnchantmentPatchesManager.loadClientCache(FabricUtils.getRegistryAccess(context), clientboundEnchantmentsSyncPacket.list());
            else EnchantmentPatchesManager.setClientCache(FabricUtils.getRegistryAccess(context));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            DefaultComponentPatchesManager.clearClient();
            EnchantmentPatchesManager.clearClient();
        });
    }
}
//?}