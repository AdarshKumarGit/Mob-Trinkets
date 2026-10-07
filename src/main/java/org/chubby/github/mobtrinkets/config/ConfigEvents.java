package org.chubby.github.mobtrinkets.config;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.handler.TrinketHandler;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ConfigEvents {
    private ConfigEvents() {
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != TrinketConfig.SPEC) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(() -> server.getPlayerList().getPlayers().forEach(TrinketHandler::refresh));
        }
    }
}
