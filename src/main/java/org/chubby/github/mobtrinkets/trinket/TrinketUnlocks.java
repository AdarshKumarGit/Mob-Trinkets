package org.chubby.github.mobtrinkets.trinket;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.config.TrinketConfig;

public final class TrinketUnlocks {
    private static final int PARTICLE_COUNT = 40;
    private static final double PARTICLE_SPREAD_XZ = 0.5;
    private static final double PARTICLE_SPREAD_Y = 0.8;
    private static final double PARTICLE_SPEED = 0.5;

    private TrinketUnlocks() {
    }

    public static int nextCost(Player player) {
        return TrinketConfig.unlockLevels(TrinketEquipment.unlocked(player));
    }

    public static Component failure(Player player) {
        int unlocked = TrinketEquipment.unlocked(player);
        if (unlocked >= TrinketData.MAX_SLOTS) {
            return Component.translatable("message.mobtrinkets.all_unlocked");
        }
        int cost = TrinketConfig.unlockLevels(unlocked);
        if (!player.getAbilities().instabuild && player.experienceLevel < cost) {
            return Component.translatable("message.mobtrinkets.not_enough_levels", cost);
        }
        return null;
    }

    public static void unlock(ServerPlayer player, ItemStack sigil) {
        int cost = nextCost(player);
        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-cost);
        }
        sigil.consume(1, player);
        TrinketEquipment.unlockNext(player);
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.2F);
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(),
                PARTICLE_COUNT, PARTICLE_SPREAD_XZ, PARTICLE_SPREAD_Y, PARTICLE_SPREAD_XZ, PARTICLE_SPEED);
        player.displayClientMessage(Component.translatable("message.mobtrinkets.unlocked", TrinketEquipment.unlocked(player)), true);
    }
}
