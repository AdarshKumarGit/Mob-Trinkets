package org.chubby.github.mobtrinkets.event;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.block.AssemblerBlockEntity;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.network.ModPayloads;
import org.chubby.github.mobtrinkets.registry.ModBlockEntities;
import org.chubby.github.mobtrinkets.trinket.TrinketSync;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEvents {
    private ModEvents() {
    }

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        ModPayloads.register(event);
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ASSEMBLER.get(),
                AssemblerBlockEntity::getItemHandler);
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != TrinketConfig.SPEC) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(() -> server.getPlayerList().getPlayers().forEach(TrinketSync::refresh));
        }
    }
}
