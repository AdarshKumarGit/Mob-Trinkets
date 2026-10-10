package org.chubby.github.mobtrinkets.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID, value = Dist.CLIENT)
public final class HudInputEvents {
    private HudInputEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean active = minecraft.player != null && minecraft.screen == null;
        while (HudKeys.EDIT.consumeClick()) {
            if (active) {
                minecraft.setScreen(new HudEditScreen(null));
            }
        }
    }
}
