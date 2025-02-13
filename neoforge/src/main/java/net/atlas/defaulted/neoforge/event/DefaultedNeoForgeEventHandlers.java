package net.atlas.defaulted.neoforge.event;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultedDataReloadListener;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent.UpdateCause;
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
    public static void onDatapackReload(final AddServerReloadListenersEvent addReloadListenerEvent) {
        addReloadListenerEvent.addListener(Defaulted.id("default_component_patches"), new PreparableReloadListener() {
            @Override
            public CompletableFuture<Void> reload(PreparationBarrier arg, ResourceManager arg2, Executor executor,
                    Executor executor2) {
                return CompletableFuture.runAsync(() -> DefaultedDataReloadListener.reload(addReloadListenerEvent.getRegistryAccess(), arg2));
            }
            
        });
    }
    @SubscribeEvent
    public static void onTagsLoaded(final TagsUpdatedEvent tagsUpdatedEvent) {
        if (tagsUpdatedEvent.getUpdateCause() == UpdateCause.SERVER_DATA_LOAD) DefaultedDataReloadListener.patch();
    }
}
