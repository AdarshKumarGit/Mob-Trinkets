package org.chubby.github.mobtrinkets.event;

import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.trinket.TrinketAbility;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class AbilityEvents {
    private static final float DEGREES_TO_RADIANS = (float) Math.PI / 180.0F;
    private static final double KNOCKBACK_PER_LEVEL = 0.5;

    private static final double CLIMB_SPEED = 0.2;
    private static final double CLIMB_HORIZONTAL_LIMIT = 0.15;
    private static final double SLOW_FALL_OFFSET = 0.07;
    private static final int FLAME_INTERVAL = 40;
    private static final int FLAME_COUNT = 2;
    private static final double FLAME_SPREAD_XZ = 0.3;
    private static final double FLAME_SPREAD_Y = 0.5;
    private static final double FLAME_SPEED = 0.01;

    private AbilityEvents() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Set<TrinketAbility> abilities = TrinketEquipment.data(player).abilities();
        if (abilities.isEmpty()) {
            return;
        }
        DamageSource source = event.getSource();
        if (abilities.contains(TrinketAbility.EXPLOSION_GUARD) && source.is(DamageTypeTags.IS_EXPLOSION)) {
            event.setAmount(reduced(event.getAmount(), player, TrinketAbility.EXPLOSION_GUARD));
        }
        if (source.is(DamageTypeTags.IS_FIRE)) {
            if (abilities.contains(TrinketAbility.FIRE_IMMUNITY)) {
                event.setCanceled(true);
            } else if (abilities.contains(TrinketAbility.FIRE_GUARD)) {
                event.setAmount(reduced(event.getAmount(), player, TrinketAbility.FIRE_GUARD));
            }
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && TrinketEquipment.has(player, TrinketAbility.SLOW_FALL)) {
            event.setDamageMultiplier(0.0F);
        }
    }

    @SubscribeEvent
    public static void onBreathe(LivingBreatheEvent event) {
        if (event.getEntity() instanceof Player player && TrinketEquipment.has(player, TrinketAbility.WATER_BREATHING)) {
            event.setCanBreathe(true);
        }
    }

    private static float reduced(float amount, Player player, TrinketAbility ability) {
        return (float) (amount * (1.0 - ability.strength(TrinketMastery.scale(player, ability))));
    }


    @SubscribeEvent
    public static void onArrowJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()) {
            return;
        }
        if (!(event.getEntity() instanceof AbstractArrow arrow) || arrow instanceof ThrownTrident) {
            return;
        }
        if (arrow.getOwner() instanceof ServerPlayer player && TrinketEquipment.has(player, TrinketAbility.ARROW_BOOST)) {
            double bonus = TrinketAbility.ARROW_BOOST.strength(TrinketMastery.scale(player, TrinketAbility.ARROW_BOOST));
            arrow.setBaseDamage(arrow.getBaseDamage() * (1.0 + bonus));
        }
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypes.PLAYER_ATTACK) || !(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!TrinketEquipment.has(player, TrinketAbility.KNOCKBACK_BOOST)) {
            return;
        }
        float yaw = player.getYRot() * DEGREES_TO_RADIANS;
        double levels = TrinketAbility.KNOCKBACK_BOOST.strength(TrinketMastery.scale(player, TrinketAbility.KNOCKBACK_BOOST));
        event.getEntity().knockback(levels * KNOCKBACK_PER_LEVEL, Mth.sin(yaw), -Mth.cos(yaw));
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
