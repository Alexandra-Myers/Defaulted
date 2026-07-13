package net.atlas.defaulted.neoforge.event;

//? neoforge {
/*import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.atlas.defaulted.EnchantmentPatchesManager;
import net.atlas.defaulted.command.DefaultedCommand;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.atlas.defaulted.networking.ClientboundEnchantmentsSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;

public class DefaultedNeoForgeEventHandlers {
    @SubscribeEvent
    public static void onCommandRegistration(RegisterCommandsEvent event) {
        DefaultedCommand.register(event.getDispatcher(), event.getBuildContext());
    }
    @SubscribeEvent
    public static void onDatapackSync(final OnDatapackSyncEvent onDatapackSyncEvent) {
        ClientboundDefaultComponentsSyncPacket[] defaultComponentsSyncPacket = {null};
        ClientboundEnchantmentsSyncPacket[] enchantmentsSyncPacket = {null};
        onDatapackSyncEvent.getRelevantPlayers().forEach(player -> {
            if (defaultComponentsSyncPacket[0] == null) defaultComponentsSyncPacket[0] = new ClientboundDefaultComponentsSyncPacket(new ArrayList<>(DefaultComponentPatchesManager.getCached(player.registryAccess())));
            if (enchantmentsSyncPacket[0] == null) enchantmentsSyncPacket[0] = new ClientboundEnchantmentsSyncPacket(new ArrayList<>(EnchantmentPatchesManager.getCached(player.registryAccess())));
            if (player.connection.hasChannel(enchantmentsSyncPacket[0])) PacketDistributor.sendToPlayer(player, enchantmentsSyncPacket[0]);
            if (player.connection.hasChannel(defaultComponentsSyncPacket[0])) PacketDistributor.sendToPlayer(player, defaultComponentsSyncPacket[0]);
        });
    }
    @SubscribeEvent
    public static void onDatapackReload(final AddServerReloadListenersEvent addReloadListenerEvent) {
        ResourceLocation defaultComponentPatches = Defaulted.id("default_component_patches");
        ResourceLocation enchantmentPatches = Defaulted.id("enchantment_patches");
        addReloadListenerEvent.addListener(defaultComponentPatches, new DefaultComponentPatchesManager(addReloadListenerEvent.getRegistryAccess()));
        addReloadListenerEvent.addListener(enchantmentPatches, new EnchantmentPatchesManager(addReloadListenerEvent.getRegistryAccess()));
        addReloadListenerEvent.addDependency(enchantmentPatches, defaultComponentPatches);
    }
    @SubscribeEvent
    public static void serverStart(final ServerStartedEvent event) {
        DefaultComponentPatchesManager.getInstance().load(event.getServer().registryAccess());
        EnchantmentPatchesManager.getInstance().load(event.getServer().registryAccess());
    }
}
*///?}