package org.chubby.github.mobtrinkets.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.skill.PrismaticBeamChannel;
import org.chubby.github.mobtrinkets.skill.TrinketSkills;
import org.chubby.github.mobtrinkets.trinket.TrinketSync;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class PlayerEvents {
    private PlayerEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        resync(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        resync(event.getEntity());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        resync(event.getEntity());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        TrinketSkills.clear(event.getEntity().getUUID());
        PrismaticBeamChannel.clear(event.getEntity().getUUID());
    }

    private static void resync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            TrinketSync.resync(serverPlayer);
        }
    }
}
