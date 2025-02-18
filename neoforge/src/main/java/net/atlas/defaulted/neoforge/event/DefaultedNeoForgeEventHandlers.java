package net.atlas.defaulted.neoforge.event;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

public class DefaultedNeoForgeEventHandlers {
    @SubscribeEvent
    public static void onDatapackSync(final OnDatapackSyncEvent onDatapackSyncEvent) {
        onDatapackSyncEvent.getRelevantPlayers().forEach(player -> {
            PacketDistributor.sendToPlayer(player, new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultComponentPatchesManager.getCached())));
        });
    }
    @SubscribeEvent
    public static void onDatapackReload(final AddServerReloadListenersEvent addReloadListenerEvent) {
        addReloadListenerEvent.addListener(Defaulted.id("default_component_patches"), new DefaultComponentPatchesManager(addReloadListenerEvent.getRegistryAccess()));
    }
    @SubscribeEvent
    public static void serverStart(final ServerStartedEvent event) {
        DefaultComponentPatchesManager.getInstance().load();
    }
}
