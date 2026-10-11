package org.chubby.github.mobtrinkets.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.skill.PrismaticBeamChannel;
import org.chubby.github.mobtrinkets.skill.SkillFx;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class ServerEvents {
    private ServerEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        SkillFx.tick();
        PrismaticBeamChannel.tickAll(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        SkillFx.clear();
    }
}
