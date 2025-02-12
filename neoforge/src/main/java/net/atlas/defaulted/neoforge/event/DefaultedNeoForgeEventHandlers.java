package net.atlas.defaulted.neoforge.event;

import net.atlas.defaulted.DefaultedDataReloadListener;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DefaultedNeoForgeEventHandlers {
    @SubscribeEvent
    public static void onDatapackSync(final OnDatapackSyncEvent onDatapackSyncEvent) {
        onDatapackSyncEvent.getRelevantPlayers().forEach(player -> {
            PacketDistributor.sendToPlayer(player, new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultedDataReloadListener.cached)));
        });
    }
    @SubscribeEvent
    public static void onDatapackReload(final AddReloadListenerEvent addReloadListenerEvent) {
        addReloadListenerEvent.addListener(new PreparableReloadListener() {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier arg, ResourceManager arg2, Executor executor,
                    Executor executor2) {
                return CompletableFuture.runAsync(() -> DefaultedDataReloadListener.reload(addReloadListenerEvent.getRegistryAccess(), arg2));
            }
            
        });
    }
}
