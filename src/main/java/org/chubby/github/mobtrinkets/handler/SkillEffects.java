package org.chubby.github.mobtrinkets.handler;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SkillEffects {
    private static final double AIM_TOLERANCE = 0.95;
    private static final int TICKS_PER_SECOND = 20;
    private static final double RING_STEPS = 28.0;

    private SkillEffects() {
    }

    private static List<LivingEntity> enemies(ServerPlayer player, double radius) {
        double limit = radius * radius;
        return player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                candidate -> candidate != player && candidate.isAlive() && candidate instanceof Enemy && candidate.distanceToSqr(player) <= limit);
    }

    private static LivingEntity target(ServerPlayer player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : player.serverLevel().getEntitiesOfClass(LivingEntity.class, box, other -> other != player && other.isAlive())) {
            Vec3 toward = candidate.getBoundingBox().getCenter().subtract(eye);
            double distance = toward.length();
            if (distance > range || toward.normalize().dot(look) < AIM_TOLERANCE || !player.hasLineOfSight(candidate)) {
                continue;
            }
            if (distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static void magic(ServerPlayer player, LivingEntity victim, float amount) {
        victim.hurt(player.damageSources().indirectMagic(player, player), amount);
    }

    private static void melee(ServerPlayer player, LivingEntity victim, float amount) {
        victim.hurt(player.damageSources().playerAttack(player), amount);
    }

    private static void push(ServerPlayer player, LivingEntity victim, double strength) {
        victim.knockback(strength, player.getX() - victim.getX(), player.getZ() - victim.getZ());
    }

    private static void lift(LivingEntity victim, double amount) {
        victim.setDeltaMovement(victim.getDeltaMovement().add(0.0, amount, 0.0));
        victim.hurtMarked = true;
    }

    private static void effect(LivingEntity entity, Holder<MobEffect> effect, int seconds, int amplifier) {
        entity.addEffect(new MobEffectInstance(effect, seconds * TICKS_PER_SECOND, amplifier));
    }

    private static void particles(ServerPlayer player, ParticleOptions type, int count, double spread) {
        player.serverLevel().sendParticles(type, player.getX(), player.getY() + 1.0, player.getZ(), count, spread, spread, spread, 0.05);
    }

    private static void sound(ServerPlayer player, SoundEvent event, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), event, SoundSource.PLAYERS, 1.0F, pitch);
    }

    private static void dash(ServerPlayer player, double speed, double lift) {
        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.x * speed, look.y * speed * 0.5 + lift, look.z * speed);
        player.hurtMarked = true;
        player.resetFallDistance();
    }

    private static void beam(ServerPlayer player, Vec3 to, ParticleOptions type) {
        ServerLevel level = player.serverLevel();
        Vec3 from = player.getEyePosition().add(0.0, -0.3, 0.0);
        int steps = Math.max(2, (int) (from.distanceTo(to) * 2.0));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = from.lerp(to, i / (double) steps);
            level.sendParticles(type, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static void ring(ServerPlayer player, ParticleOptions type, double radius) {
        ServerLevel level = player.serverLevel();
        for (int i = 0; i < RING_STEPS; i++) {
            double angle = i / RING_STEPS * Math.PI * 2.0;
            level.sendParticles(type, player.getX() + Math.cos(angle) * radius, player.getY() + 0.3, player.getZ() + Math.sin(angle) * radius, 1, 0.0, 0.05, 0.0, 0.0);
        }
    }

    public static boolean pollenDash(ServerPlayer player) {
        effect(player, MobEffects.MOVEMENT_SPEED, 8, 1);
        particles(player, ParticleTypes.HAPPY_VILLAGER, 20, 0.6);
        sound(player, SoundEvents.BEE_POLLINATE, 1.2F);
        return true;
    }

    public static boolean honeyShield(ServerPlayer player) {
        effect(player, MobEffects.ABSORPTION, 15, 1);
        particles(player, ParticleTypes.FALLING_HONEY, 24, 0.6);
        sound(player, SoundEvents.HONEY_BLOCK_PLACE, 1.0F);
        return true;
    }

    public static boolean boneArmor(ServerPlayer player) {
        effect(player, MobEffects.DAMAGE_RESISTANCE, 12, 1);
        particles(player, ParticleTypes.CRIT, 24, 0.6);
        sound(player, SoundEvents.SKELETON_AMBIENT, 0.8F);
        return true;
    }

    public static boolean rattle(ServerPlayer player) {
        for (LivingEntity victim : enemies(player, 6.0)) {
            effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 6, 1);
            effect(victim, MobEffects.WEAKNESS, 6, 0);
            magic(player, victim, 3.0F);
        }
        ring(player, ParticleTypes.CRIT, 5.0);
        sound(player, SoundEvents.SKELETON_HURT, 0.7F);
        return true;
    }

    public static boolean ramCharge(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        dash(player, 1.7, 0.15);
        for (LivingEntity victim : enemies(player, 3.5)) {
            Vec3 toward = victim.position().subtract(player.position()).normalize();
            if (toward.dot(look) > 0.3) {
                melee(player, victim, 6.0F);
                push(player, victim, 1.5);
            }
        }
        sound(player, SoundEvents.GOAT_RAM_IMPACT, 1.0F);
        return true;
    }

    public static boolean hornBlast(ServerPlayer player) {
        for (LivingEntity victim : enemies(player, 7.0)) {
            magic(player, victim, 6.0F);
            push(player, victim, 1.8);
        }
        ring(player, ParticleTypes.CLOUD, 6.0);
        sound(player, SoundEvents.RAVAGER_ROAR, 1.2F);
        return true;
    }

    public static boolean tidalSurge(ServerPlayer player) {
        effect(player, MobEffects.DOLPHINS_GRACE, 20, 0);
        effect(player, MobEffects.WATER_BREATHING, 20, 0);
        particles(player, ParticleTypes.BUBBLE, 30, 0.7);
        sound(player, SoundEvents.DOLPHIN_SPLASH, 1.0F);
        return true;
    }

    public static boolean riptideBurst(ServerPlayer player) {
        dash(player, 2.0, 0.1);
        for (LivingEntity victim : enemies(player, 3.5)) {
            magic(player, victim, 5.0F);
        }
        particles(player, ParticleTypes.SPLASH, 40, 0.8);
        sound(player, SoundEvents.TRIDENT_RIPTIDE_1, 1.0F);
        return true;
    }

    public static boolean slimeBounce(ServerPlayer player) {
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, 1.2, motion.z);
        player.hurtMarked = true;
        effect(player, MobEffects.SLOW_FALLING, 8, 0);
        particles(player, ParticleTypes.ITEM_SLIME, 24, 0.5);
        sound(player, SoundEvents.SLIME_JUMP, 1.0F);
        return true;
    }

    public static boolean slimeWave(ServerPlayer player) {
        for (LivingEntity victim : enemies(player, 6.0)) {
            push(player, victim, 1.3);
            lift(victim, 0.5);
            effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 4, 1);
        }
        ring(player, ParticleTypes.ITEM_SLIME, 5.0);
        sound(player, SoundEvents.SLIME_SQUISH, 0.8F);
        return true;
    }

    public static boolean glide(ServerPlayer player) {
        dash(player, 1.3, 0.7);
        effect(player, MobEffects.SLOW_FALLING, 12, 0);
        particles(player, ParticleTypes.CLOUD, 20, 0.5);
        sound(player, SoundEvents.PHANTOM_FLAP, 1.0F);
        return true;
    }

    public static boolean haunt(ServerPlayer player) {
        for (LivingEntity victim : enemies(player, 8.0)) {
            effect(victim, MobEffects.GLOWING, 10, 0);
            effect(victim, MobEffects.WEAKNESS, 8, 0);
            effect(victim, MobEffects.BLINDNESS, 3, 0);
        }
        ring(player, ParticleTypes.SOUL, 7.0);
        sound(player, SoundEvents.PHANTOM_SWOOP, 0.8F);
        return true;
    }

    public static boolean prismaticBeam(ServerPlayer player) {
        LivingEntity victim = target(player, 16.0);
        if (victim == null) {
            return false;
        }
        magic(player, victim, 8.0F);
        effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 3, 1);
        beam(player, victim.getBoundingBox().getCenter(), ParticleTypes.ELECTRIC_SPARK);
        sound(player, SoundEvents.GUARDIAN_ATTACK, 1.0F);
        return true;
    }

    public static boolean guardiansWard(ServerPlayer player) {
        effect(player, MobEffects.ABSORPTION, 20, 2);
        particles(player, ParticleTypes.BUBBLE_POP, 30, 0.7);
        sound(player, SoundEvents.GUARDIAN_AMBIENT, 1.0F);
        return true;
    }

    public static boolean volatileBurst(ServerPlayer player) {
        for (LivingEntity victim : enemies(player, 4.5)) {
            magic(player, victim, 8.0F);
            push(player, victim, 1.6);
        }
        particles(player, ParticleTypes.EXPLOSION, 3, 1.5);
        sound(player, SoundEvents.CREEPER_PRIMED, 1.0F);
        return true;
    }

    public static boolean chainDetonation(ServerPlayer player) {
        for (LivingEntity victim : enemies(player, 7.0)) {
            magic(player, victim, 14.0F);
            push(player, victim, 2.2);
        }
        particles(player, ParticleTypes.EXPLOSION, 8, 3.0);
        ring(player, ParticleTypes.POOF, 6.5);
        sound(player, SoundEvents.CREEPER_PRIMED, 0.7F);
        return true;
    }

    public static boolean flameLash(ServerPlayer player) {
        LivingEntity victim = target(player, 12.0);
        if (victim == null) {
            return false;
        }
        victim.setRemainingFireTicks(6 * TICKS_PER_SECOND);
        magic(player, victim, 4.0F);
        beam(player, victim.getBoundingBox().getCenter(), ParticleTypes.FLAME);
        sound(player, SoundEvents.BLAZE_SHOOT, 1.0F);
        return true;
    }

    public static boolean infernoRing(ServerPlayer player) {
        for (LivingEntity victim : enemies(player, 6.0)) {
            victim.setRemainingFireTicks(8 * TICKS_PER_SECOND);
            magic(player, victim, 4.0F);
            push(player, victim, 0.8);
        }
        ring(player, ParticleTypes.FLAME, 5.5);
        sound(player, SoundEvents.BLAZE_SHOOT, 0.7F);
        return true;
    }

    public static boolean venomStrike(ServerPlayer player) {
        LivingEntity victim = target(player, 7.0);
        if (victim == null) {
            return false;
        }
        effect(victim, MobEffects.POISON, 6, 1);
        effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 3, 0);
        magic(player, victim, 4.0F);
        beam(player, victim.getBoundingBox().getCenter(), ParticleTypes.WITCH);
        sound(player, SoundEvents.SPIDER_AMBIENT, 1.0F);
        return true;
    }

    public static boolean silkSnare(ServerPlayer player) {
        for (LivingEntity victim : enemies(player, 6.0)) {
            effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 5, 4);
            effect(victim, MobEffects.WEAKNESS, 5, 0);
        }
        ring(player, ParticleTypes.POOF, 5.5);
        sound(player, SoundEvents.COBWEB_PLACE, 1.0F);
        return true;
    }

    public static boolean magmaShield(ServerPlayer player) {
        effect(player, MobEffects.FIRE_RESISTANCE, 15, 0);
        effect(player, MobEffects.DAMAGE_RESISTANCE, 10, 0);
        particles(player, ParticleTypes.LAVA, 14, 0.6);
        sound(player, SoundEvents.MAGMA_CUBE_SQUISH, 1.0F);
        return true;
    }

    public static boolean eruption(ServerPlayer player) {
        for (LivingEntity victim : enemies(player, 5.0)) {
            magic(player, victim, 8.0F);
            victim.setRemainingFireTicks(6 * TICKS_PER_SECOND);
            lift(victim, 0.7);
        }
        particles(player, ParticleTypes.LAVA, 30, 1.8);
        ring(player, ParticleTypes.FLAME, 4.5);
        sound(player, SoundEvents.FIRECHARGE_USE, 0.8F);
        return true;
    }

    public static boolean voidPull(ServerPlayer player) {
        LivingEntity victim = target(player, 14.0);
        if (victim == null) {
            return false;
        }
        Vec3 pull = player.position().subtract(victim.position()).normalize().scale(1.4);
        victim.setDeltaMovement(pull.x, 0.3, pull.z);
        victim.hurtMarked = true;
        beam(player, victim.getBoundingBox().getCenter(), ParticleTypes.PORTAL);
        sound(player, SoundEvents.ENDERMAN_TELEPORT, 0.8F);
        return true;
    }

    public static boolean phaseShift(ServerPlayer player) {
        effect(player, MobEffects.INVISIBILITY, 6, 0);
        effect(player, MobEffects.MOVEMENT_SPEED, 6, 1);
        particles(player, ParticleTypes.PORTAL, 40, 0.7);
        sound(player, SoundEvents.ENDERMAN_TELEPORT, 1.2F);
        return true;
    }
}
