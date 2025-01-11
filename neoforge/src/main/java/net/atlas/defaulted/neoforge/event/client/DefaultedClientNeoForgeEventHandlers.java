package net.atlas.defaulted.neoforge.event.client;

import net.atlas.defaulted.Defaulted;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = "defaulted")
public class DefaultedClientNeoForgeEventHandlers {
    @SubscribeEvent
    public static void onClientDisconnect(final ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        Defaulted.EXECUTE_ON_RELOAD.clear();
    }
}
