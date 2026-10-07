package org.chubby.github.mobtrinkets.handler;

import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.trinket.TrinketAbility;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class TrinketTicker {
    private static final double CLIMB_SPEED = 0.2;
    private static final double CLIMB_HORIZONTAL_LIMIT = 0.15;
    private static final double SLOW_FALL_OFFSET = 0.07;
    private static final int FLAME_INTERVAL = 40;
    private static final int FLAME_COUNT = 2;
    private static final double FLAME_SPREAD_XZ = 0.3;
    private static final double FLAME_SPREAD_Y = 0.5;
    private static final double FLAME_SPEED = 0.01;

    private TrinketTicker() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        Set<TrinketAbility> abilities = TrinketEquipment.data(player).abilities();
        if (abilities.isEmpty() || player.isSpectator()) {
            return;
        }
        if (abilities.contains(TrinketAbility.SPIDER_CLIMB)) {
            climb(player);
        }
        if (abilities.contains(TrinketAbility.SLOW_FALL)) {
            slowFall(player);
        }
        if (abilities.contains(TrinketAbility.FIRE_IMMUNITY)) {
            keepExtinguished(player);
        }
    }

    private static void climb(Player player) {
        if (!player.horizontalCollision || player.onGround() || player.isInFluidType() || player.isPassenger()
                || player.isFallFlying() || player.getAbilities().flying || player.onClimbable()) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        double vertical = player.isShiftKeyDown() ? 0.0 : CLIMB_SPEED;
        player.setDeltaMovement(
                Mth.clamp(motion.x, -CLIMB_HORIZONTAL_LIMIT, CLIMB_HORIZONTAL_LIMIT),
                vertical,
                Mth.clamp(motion.z, -CLIMB_HORIZONTAL_LIMIT, CLIMB_HORIZONTAL_LIMIT));
        player.resetFallDistance();
    }

    private static void slowFall(Player player) {
        Vec3 motion = player.getDeltaMovement();
        if (motion.y >= 0.0 || player.onGround() || player.isInFluidType() || player.isPassenger()
                || player.isFallFlying() || player.getAbilities().flying) {
            return;
        }
        player.setDeltaMovement(motion.x, Math.min(motion.y + SLOW_FALL_OFFSET, 0.0), motion.z);
    }

    private static void keepExtinguished(Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (player.getRemainingFireTicks() > 0) {
            player.setRemainingFireTicks(0);
        }
        if (player.tickCount % FLAME_INTERVAL == 0) {
            level.sendParticles(ParticleTypes.SMALL_FLAME, player.getX(), player.getY() + 1.0, player.getZ(),
                    FLAME_COUNT, FLAME_SPREAD_XZ, FLAME_SPREAD_Y, FLAME_SPREAD_XZ, FLAME_SPEED);
        }
    }
}
