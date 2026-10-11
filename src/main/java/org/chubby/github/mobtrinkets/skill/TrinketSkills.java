package org.chubby.github.mobtrinkets.skill;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;
import org.chubby.github.mobtrinkets.trinket.TrinketSkill;

public final class TrinketSkills {
    public static final int PRIMARY_TIER = 2;
    public static final int SECONDARY_TIER = 3;

    private static final Map<UUID, Map<TrinketSkill, Long>> READY_AT = new HashMap<>();
    private static final Set<UUID> COOLDOWN_DISABLED = new HashSet<>();

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
        long readyAt = readyMap(player).getOrDefault(skill, 0L);
        if (!isCooldownDisabled(player) && now < readyAt) {
            long seconds = (readyAt - now + SharedConstants.TICKS_PER_SECOND - 1) / SharedConstants.TICKS_PER_SECOND;
            player.displayClientMessage(Component.translatable("message.mobtrinkets.skill_cooldown", skill.displayName(), seconds), true);
            return;
        }
        if (skill == TrinketSkill.PRISMATIC_BEAM) {
            PrismaticBeamChannel.start(player, slot, secondary);
            return;
        }
        if (!SkillEffects.cast(skill, player)) {
            player.displayClientMessage(Component.translatable("message.mobtrinkets.no_target"), true);
            return;
        }
        startCooldown(player, skill);
        player.displayClientMessage(skill.displayName(), true);
    }

    public static void startCooldown(ServerPlayer player, TrinketSkill skill) {
        if (isCooldownDisabled(player)) {
            return;
        }
        long now = player.level().getGameTime();
        long cooldown = Math.round(skill.cooldownSeconds() * SharedConstants.TICKS_PER_SECOND * TrinketConfig.SKILL_COOLDOWN_MULTIPLIER.get());
        readyMap(player).put(skill, now + cooldown);
    }

    public static void setCooldownDisabled(ServerPlayer player, boolean disabled) {
        if (disabled) {
            COOLDOWN_DISABLED.add(player.getUUID());
        } else {
            COOLDOWN_DISABLED.remove(player.getUUID());
        }
    }

    public static boolean isCooldownDisabled(ServerPlayer player) {
        return COOLDOWN_DISABLED.contains(player.getUUID());
    }

    public static void clear(UUID playerId) {
        READY_AT.remove(playerId);
        COOLDOWN_DISABLED.remove(playerId);
    }

    private static Map<TrinketSkill, Long> readyMap(ServerPlayer player) {
        return READY_AT.computeIfAbsent(player.getUUID(), id -> new EnumMap<>(TrinketSkill.class));
    }
}
