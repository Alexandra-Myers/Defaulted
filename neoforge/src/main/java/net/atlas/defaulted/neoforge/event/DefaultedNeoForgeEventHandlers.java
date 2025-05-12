package net.atlas.defaulted.neoforge.event;

import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

public class DefaultedNeoForgeEventHandlers {
    @SubscribeEvent
    public static void onDatapackSync(final OnDatapackSyncEvent onDatapackSyncEvent) {
        ClientboundDefaultComponentsSyncPacket packet = new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultComponentPatchesManager.getCached()));
        onDatapackSyncEvent.getRelevantPlayers().forEach(player -> {
            if (player.connection.hasChannel(packet)) PacketDistributor.sendToPlayer(player, packet);
        });
    }
    @SubscribeEvent
    public static void onDatapackReload(final AddReloadListenerEvent addReloadListenerEvent) {
        addReloadListenerEvent.addListener(new DefaultComponentPatchesManager(addReloadListenerEvent.getRegistryAccess()));
    }
    @SubscribeEvent
    public static void serverStart(final ServerStartedEvent event) {
        DefaultComponentPatchesManager.getInstance().load();
    }
}
