package org.chubby.github.mobtrinkets.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.chubby.github.mobtrinkets.MobTrinkets;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";

    private ModNetworking() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(SyncTrinketPayload.TYPE, SyncTrinketPayload.CODEC, SyncTrinketPayload::handle);
        registrar.playToServer(OpenTrinketMenuPayload.TYPE, OpenTrinketMenuPayload.CODEC, OpenTrinketMenuPayload::handle);
        registrar.playToClient(SyncMasteryPayload.TYPE, SyncMasteryPayload.CODEC, SyncMasteryPayload::handle);
        registrar.playToServer(UseSkillPayload.TYPE, UseSkillPayload.CODEC, UseSkillPayload::handle);
        registrar.playToServer(TeleportPayload.TYPE, TeleportPayload.CODEC, TeleportPayload::handle);
    }
}
