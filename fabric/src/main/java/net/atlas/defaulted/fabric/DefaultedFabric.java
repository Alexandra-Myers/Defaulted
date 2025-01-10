package net.atlas.defaulted.fabric;

import net.atlas.defaulted.component.ItemPatches;
import net.fabricmc.api.ModInitializer;

import net.atlas.defaulted.Defaulted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;

public final class DefaultedFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Defaulted.init();

        DynamicRegistries.register(Defaulted.ITEM_PATCHES, ItemPatches.DIRECT_CODEC);
        ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
            Registry<ItemPatches> reg = minecraftServer.registryAccess().lookupOrThrow(Defaulted.ITEM_PATCHES);
            Defaulted.patchItemComponents(reg.stream()
                    .sorted(Comparator.comparing(reg::getKey, Comparator.nullsFirst(Comparator.naturalOrder()))).toList());
        });
        ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (ServerPlayNetworking.canSend(player, ClientboundDefaultComponentsSyncPacket.TYPE)) {
                Registry<ItemPatches> reg = server.registryAccess().lookupOrThrow(Defaulted.ITEM_PATCHES);
                packetSender.sendPacket(new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(reg.stream()
                        .sorted(Comparator.comparing(reg::getKey, Comparator.nullsFirst(Comparator.naturalOrder()))).toList())));
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
