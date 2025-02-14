package net.atlas.defaulted.neoforge.event;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent.UpdateCause;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

public class DefaultedNeoForgeEventHandlers {
    @SubscribeEvent
    public static void onDatapackSync(final OnDatapackSyncEvent onDatapackSyncEvent) {
        onDatapackSyncEvent.getRelevantPlayers().forEach(player -> {
            PacketDistributor.sendToPlayer(player, new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultComponentPatchesManager.cached)));
        });
    }
    @SubscribeEvent
    public static void onDatapackReload(final AddServerReloadListenersEvent addReloadListenerEvent) {
        addReloadListenerEvent.addListener(Defaulted.id("default_component_patches"), new DefaultComponentPatchesManager(addReloadListenerEvent.getRegistryAccess()));
    }
    @SubscribeEvent
    public static void onTagsLoaded(final TagsUpdatedEvent tagsUpdatedEvent) {
        if (tagsUpdatedEvent.getUpdateCause() == UpdateCause.SERVER_DATA_LOAD) DefaultComponentPatchesManager.patch();
    }
}
