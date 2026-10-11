package org.chubby.github.mobtrinkets.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.client.hud.BeamHud;
import org.chubby.github.mobtrinkets.client.hud.TrinketHud;
import org.chubby.github.mobtrinkets.client.screen.AssemblerScreen;
import org.chubby.github.mobtrinkets.client.screen.TrinketScreen;
import org.chubby.github.mobtrinkets.registry.ModMenus;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        for (var key : ClientKeys.SLOT_KEYS) {
            event.register(key);
        }
        event.register(ClientKeys.HUD_EDIT);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.TRINKET.get(), TrinketScreen::new);
        event.register(ModMenus.ASSEMBLER.get(), AssemblerScreen::new);
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(MobTrinkets.id("trinket_hud"), (graphics, deltaTracker) -> TrinketHud.renderLayer(graphics));
        event.registerAboveAll(MobTrinkets.id("prismatic_beam_bar"), (graphics, deltaTracker) -> BeamHud.renderLayer(graphics));
    }
}
