package net.atlas.defaulted.neoforge.event;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

public class DefaultedNeoForgeEventHandlers {
    @SubscribeEvent
    public static void onDatapackSync(final OnDatapackSyncEvent onDatapackSyncEvent) {
        onDatapackSyncEvent.getRelevantPlayers().forEach(player -> {
            MinecraftServer server = player.getServer();
            assert server != null;
            HolderGetter<ItemPatches> getter = server.reloadableRegistries().lookup().lookupOrThrow(Defaulted.ITEM_PATCHES);
            Collection<ItemPatches> reg = server.reloadableRegistries().getKeys(Defaulted.ITEM_PATCHES).stream()
                    .sorted(Comparator.nullsFirst(Comparator.naturalOrder())).filter(Objects::nonNull)
                    .map(resourceLocation -> getter.getOrThrow(ResourceKey.create(Defaulted.ITEM_PATCHES, resourceLocation)))
                    .map(Holder::value)
                    .toList();
            PacketDistributor.sendToPlayer(player, new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(reg)));
        });
    }
    @SubscribeEvent
    public static void onServerStarted(final ServerStartedEvent serverStartedEvent) {
        Defaulted.EXECUTE_ON_RELOAD.add(itemPatches -> {
            if (!itemPatches.isEmpty()) PacketDistributor.sendToAllPlayers(new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(itemPatches)));
        });
    }
}
