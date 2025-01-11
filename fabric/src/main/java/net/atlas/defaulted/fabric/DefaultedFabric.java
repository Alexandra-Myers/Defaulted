package net.atlas.defaulted.fabric;

import net.atlas.defaulted.component.ItemPatches;
import net.fabricmc.api.ModInitializer;

import net.atlas.defaulted.Defaulted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class DefaultedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Defaulted.init();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> Defaulted.EXECUTE_ON_RELOAD.add(itemPatches -> {
            if (!itemPatches.isEmpty())
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (ServerPlayNetworking.canSend(player, ClientboundDefaultComponentsSyncPacket.TYPE)) player.connection.send(ServerPlayNetworking.createS2CPacket(new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(itemPatches))));
                }
        }));
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if (ServerPlayNetworking.canSend(player, ClientboundDefaultComponentsSyncPacket.TYPE)) {
                MinecraftServer server = player.getServer();
                assert server != null;
                HolderGetter<ItemPatches> getter = server.reloadableRegistries().lookup().lookupOrThrow(Defaulted.ITEM_PATCHES);
                Collection<ItemPatches> reg = server.reloadableRegistries().getKeys(Defaulted.ITEM_PATCHES).stream()
                        .sorted(Comparator.nullsFirst(Comparator.naturalOrder())).filter(Objects::nonNull)
                        .map(resourceLocation -> getter.getOrThrow(ResourceKey.create(Defaulted.ITEM_PATCHES, resourceLocation)))
                        .map(Holder::value)
                        .toList();
                ServerPlayNetworking.send(player, new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(reg)));
            }
        });
        PayloadTypeRegistry.playS2C().register(ClientboundDefaultComponentsSyncPacket.TYPE, ClientboundDefaultComponentsSyncPacket.CODEC);
    }

    public record ClientboundDefaultComponentsSyncPacket(ArrayList<ItemPatches> list) implements CustomPacketPayload {
        public static final Type<ClientboundDefaultComponentsSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("defaulted", "update_status"));
        public static final StreamCodec<FriendlyByteBuf, ClientboundDefaultComponentsSyncPacket> CODEC = ByteBufCodecs.collection(ArrayList::new, ItemPatches.STREAM_CODEC).map(ClientboundDefaultComponentsSyncPacket::new, ClientboundDefaultComponentsSyncPacket::list).mapStream(buf -> (RegistryFriendlyByteBuf) buf);

        @Override
        public @NotNull Type<?> type() {
            return TYPE;
        }
    }
}
