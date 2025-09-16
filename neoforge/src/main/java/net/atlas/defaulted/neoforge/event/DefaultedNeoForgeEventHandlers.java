package net.atlas.defaulted.neoforge.event;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.minecraft.resources.RegistryOps;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Optional;

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
        addReloadListenerEvent.addListener(new DefaultComponentPatchesManager() {
            @Override
            public Codec<Optional<ItemPatches>> getCodec() {
                return ConditionalOps.createConditionalCodecWithConditions(ItemPatches.DIRECT_CODEC).xmap(optionalWithConditions -> optionalWithConditions.map(WithConditions::carrier), patches -> patches.map(itemPatches -> WithConditions.builder(itemPatches).build()));
            }

            @Override
            public RegistryOps<JsonElement> makeOps() {
                return makeConditionalOps();
            }
        });
    }
    @SubscribeEvent
    public static void serverStart(final ServerStartedEvent event) {
        DefaultComponentPatchesManager.getInstance().load();
    }
}
