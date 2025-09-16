package net.atlas.defaulted.neoforge;

import net.atlas.defaulted.neoforge.component.DefaultedRegistries;
import net.atlas.defaulted.neoforge.event.DefaultedNeoForgeEventHandlers;
import net.atlas.defaulted.networking.ClientboundDefaultComponentsSyncPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.DefaultComponentPatchesManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Defaulted.MOD_ID)
public final class DefaultedNeoForge {
    public DefaultedNeoForge(IEventBus modBus) {
        // Run our common setup.
        Defaulted.init();
        DefaultedRegistries.init(modBus);
        modBus.register(this);
        NeoForge.EVENT_BUS.register(DefaultedNeoForgeEventHandlers.class);
    }
    @SubscribeEvent
    public void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(
                ClientboundDefaultComponentsSyncPacket.TYPE,
                ClientboundDefaultComponentsSyncPacket.CODEC,
                DefaultedNeoForge::receiveDefaults
        );
    }
    public static void receiveDefaults(final ClientboundDefaultComponentsSyncPacket payload, final IPayloadContext payloadContext) {
        if (!Minecraft.getInstance().hasSingleplayerServer()) DefaultComponentPatchesManager.loadClientCache(payload.list());
        else DefaultComponentPatchesManager.setClientCache();
    }
}
