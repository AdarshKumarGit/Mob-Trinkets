package org.chubby.github.mobtrinkets.handler;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;
import org.chubby.github.mobtrinkets.trinket.TrinketSkill;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class TrinketSkills {
    public static final int PRIMARY_TIER = 2;
    public static final int SECONDARY_TIER = 3;

    private static final int TICKS_PER_SECOND = 20;
    private static final Map<UUID, Map<TrinketSkill, Long>> READY_AT = new HashMap<>();

    private TrinketSkills() {
    }

    public static void use(ServerPlayer player, int slot, boolean secondary) {
        TrinketData data = TrinketEquipment.data(player);
        if (!data.isUnlocked(slot) || !player.isAlive() || player.isSpectator()) {
            return;
        }
        TrinketDefinition definition = TrinketRegistry.forStack(data.stack(slot));
        if (definition == null) {
            return;
        }
        int required = secondary ? SECONDARY_TIER : PRIMARY_TIER;
        if (TrinketMastery.tier(player, definition) < required) {
            player.displayClientMessage(Component.translatable("message.mobtrinkets.skill_locked", required), true);
            return;
        }
        TrinketSkill skill = secondary ? definition.secondary() : definition.primary();
        long now = player.level().getGameTime();
        Map<TrinketSkill, Long> ready = READY_AT.computeIfAbsent(player.getUUID(), id -> new EnumMap<>(TrinketSkill.class));
        long readyAt = ready.getOrDefault(skill, 0L);
        if (now < readyAt) {
            long seconds = (readyAt - now + TICKS_PER_SECOND - 1) / TICKS_PER_SECOND;
            player.displayClientMessage(Component.translatable("message.mobtrinkets.skill_cooldown", skill.displayName(), seconds), true);
            return;
        }
        if (!skill.activate(player)) {
            player.displayClientMessage(Component.translatable("message.mobtrinkets.no_target"), true);
            return;
        }
        long cooldown = Math.round(skill.cooldownSeconds() * TICKS_PER_SECOND * TrinketConfig.SKILL_COOLDOWN_MULTIPLIER.get());
        ready.put(skill, now + cooldown);
        player.displayClientMessage(skill.displayName(), true);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        READY_AT.remove(event.getEntity().getUUID());
    }
}
