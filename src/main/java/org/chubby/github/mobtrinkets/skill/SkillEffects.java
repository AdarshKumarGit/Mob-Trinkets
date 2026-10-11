package org.chubby.github.mobtrinkets.skill;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
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
import org.chubby.github.mobtrinkets.trinket.TrinketSkill;

public final class SkillEffects {
    private static final double AIM_TOLERANCE = 0.95;

    private SkillEffects() {
    }

    public static boolean cast(TrinketSkill skill, ServerPlayer player) {
        return switch (skill) {
            case POLLEN_DASH -> pollenDash(player);
            case HONEY_SHIELD -> honeyShield(player);
            case BONE_ARMOR -> boneArmor(player);
            case RATTLE -> rattle(player);
            case RAM_CHARGE -> ramCharge(player);
            case HORN_BLAST -> hornBlast(player);
            case TIDAL_SURGE -> tidalSurge(player);
            case RIPTIDE_BURST -> riptideBurst(player);
            case SLIME_BOUNCE -> slimeBounce(player);
            case SLIME_WAVE -> slimeWave(player);
            case GLIDE -> glide(player);
            case HAUNT -> haunt(player);
            case GUARDIANS_WARD -> guardiansWard(player);
            case VOLATILE_BURST -> volatileBurst(player);
            case CHAIN_DETONATION -> chainDetonation(player);
            case FLAME_LASH -> flameLash(player);
            case INFERNO_RING -> infernoRing(player);
            case VENOM_STRIKE -> venomStrike(player);
            case SILK_SNARE -> silkSnare(player);
            case MAGMA_SHIELD -> magmaShield(player);
            case ERUPTION -> eruption(player);
            case VOID_PULL -> voidPull(player);
            case PHASE_SHIFT -> phaseShift(player);
            case PRISMATIC_BEAM -> throw new IllegalArgumentException("Prismatic Beam is channelled, not cast");
        };
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
        entity.addEffect(new MobEffectInstance(effect, seconds * SharedConstants.TICKS_PER_SECOND, amplifier));
    }

    private static void sound(ServerPlayer player, SoundEvent event, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), event, SoundSource.PLAYERS, 1.0F, pitch);
    }

    private static void sound(ServerPlayer player, Holder<SoundEvent> event, float pitch) {
        sound(player, event.value(), pitch);
    }

    private static void dash(ServerPlayer player, double speed, double lift) {
        Vec3 look = player.getLookAngle();
        player.setDeltaMovement(look.x * speed, look.y * speed * 0.5 + lift, look.z * speed);
        player.hurtMarked = true;
        player.resetFallDistance();
    }

    private static boolean pollenDash(ServerPlayer player) {
        effect(player, MobEffects.MOVEMENT_SPEED, 8, 1);
        SkillFx.pollenDash(player);
        sound(player, SoundEvents.BEE_POLLINATE, 1.2F);
        return true;
    }

    private static boolean honeyShield(ServerPlayer player) {
        effect(player, MobEffects.ABSORPTION, 15, 1);
        SkillFx.honeyShield(player);
        sound(player, SoundEvents.HONEY_BLOCK_PLACE, 1.0F);
        return true;
    }

    private static boolean boneArmor(ServerPlayer player) {
        effect(player, MobEffects.DAMAGE_RESISTANCE, 12, 1);
        SkillFx.boneArmor(player);
        sound(player, SoundEvents.SKELETON_AMBIENT, 0.8F);
        return true;
    }

    private static boolean rattle(ServerPlayer player) {
        List<LivingEntity> victims = enemies(player, 6.0);
        for (LivingEntity victim : victims) {
            effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 6, 1);
            effect(victim, MobEffects.WEAKNESS, 6, 0);
            magic(player, victim, 3.0F);
        }
        SkillFx.rattle(player, victims);
        sound(player, SoundEvents.SKELETON_HURT, 0.7F);
        return true;
    }

    private static boolean ramCharge(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        dash(player, 1.7, 0.15);
        List<LivingEntity> hit = new ArrayList<>();
        for (LivingEntity victim : enemies(player, 3.5)) {
            Vec3 toward = victim.position().subtract(player.position()).normalize();
            if (toward.dot(look) > 0.3) {
                melee(player, victim, 6.0F);
                push(player, victim, 1.5);
                hit.add(victim);
            }
        }
        SkillFx.ramCharge(player, hit);
        sound(player, SoundEvents.GOAT_RAM_IMPACT, 1.0F);
        return true;
    }

    private static boolean hornBlast(ServerPlayer player) {
        List<LivingEntity> victims = enemies(player, 7.0);
        for (LivingEntity victim : victims) {
            magic(player, victim, 6.0F);
            push(player, victim, 1.8);
        }
        SkillFx.hornBlast(player, victims);
        sound(player, SoundEvents.RAVAGER_ROAR, 1.2F);
        return true;
    }

    private static boolean tidalSurge(ServerPlayer player) {
        effect(player, MobEffects.DOLPHINS_GRACE, 20, 0);
        effect(player, MobEffects.WATER_BREATHING, 20, 0);
        SkillFx.tidalSurge(player);
        sound(player, SoundEvents.DOLPHIN_SPLASH, 1.0F);
        return true;
    }

    private static boolean riptideBurst(ServerPlayer player) {
        dash(player, 2.0, 0.1);
        List<LivingEntity> victims = enemies(player, 3.5);
        for (LivingEntity victim : victims) {
            magic(player, victim, 5.0F);
        }
        SkillFx.riptideBurst(player, victims);
        sound(player, SoundEvents.TRIDENT_RIPTIDE_1, 1.0F);
        return true;
    }

    private static boolean slimeBounce(ServerPlayer player) {
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, 1.2, motion.z);
        player.hurtMarked = true;
        effect(player, MobEffects.SLOW_FALLING, 8, 0);
        SkillFx.slimeBounce(player);
        sound(player, SoundEvents.SLIME_JUMP, 1.0F);
        return true;
    }

    private static boolean slimeWave(ServerPlayer player) {
        List<LivingEntity> victims = enemies(player, 6.0);
        for (LivingEntity victim : victims) {
            push(player, victim, 1.3);
            lift(victim, 0.5);
            effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 4, 1);
        }
        SkillFx.slimeWave(player, victims);
        sound(player, SoundEvents.SLIME_SQUISH, 0.8F);
        return true;
    }

    private static boolean glide(ServerPlayer player) {
        dash(player, 1.3, 0.7);
        effect(player, MobEffects.SLOW_FALLING, 12, 0);
        SkillFx.glide(player);
        sound(player, SoundEvents.PHANTOM_FLAP, 1.0F);
        return true;
    }

    private static boolean haunt(ServerPlayer player) {
        List<LivingEntity> victims = enemies(player, 8.0);
        for (LivingEntity victim : victims) {
            effect(victim, MobEffects.GLOWING, 10, 0);
            effect(victim, MobEffects.WEAKNESS, 8, 0);
            effect(victim, MobEffects.BLINDNESS, 3, 0);
        }
        SkillFx.haunt(player, victims);
        sound(player, SoundEvents.PHANTOM_SWOOP, 0.8F);
        return true;
    }

    private static boolean guardiansWard(ServerPlayer player) {
        effect(player, MobEffects.ABSORPTION, 20, 2);
        SkillFx.guardiansWard(player);
        sound(player, SoundEvents.GUARDIAN_AMBIENT, 1.0F);
        return true;
    }

    private static boolean volatileBurst(ServerPlayer player) {
        List<LivingEntity> victims = enemies(player, 4.5);
        for (LivingEntity victim : victims) {
            magic(player, victim, 8.0F);
            push(player, victim, 1.6);
        }
        SkillFx.volatileBurst(player, victims);
        sound(player, SoundEvents.CREEPER_PRIMED, 1.0F);
        return true;
    }

    private static boolean chainDetonation(ServerPlayer player) {
        List<LivingEntity> victims = enemies(player, 7.0);
        for (LivingEntity victim : victims) {
            magic(player, victim, 14.0F);
            push(player, victim, 2.2);
        }
        SkillFx.chainDetonation(player, victims);
        sound(player, SoundEvents.CREEPER_PRIMED, 0.7F);
        return true;
    }

    private static boolean flameLash(ServerPlayer player) {
        LivingEntity victim = target(player, 12.0);
        if (victim == null) {
            return false;
        }
        victim.setRemainingFireTicks(6 * SharedConstants.TICKS_PER_SECOND);
        magic(player, victim, 4.0F);
        SkillFx.flameLash(player, victim);
        sound(player, SoundEvents.BLAZE_SHOOT, 1.0F);
        return true;
    }

    private static boolean infernoRing(ServerPlayer player) {
        List<LivingEntity> victims = enemies(player, 6.0);
        for (LivingEntity victim : victims) {
            victim.setRemainingFireTicks(8 * SharedConstants.TICKS_PER_SECOND);
            magic(player, victim, 4.0F);
            push(player, victim, 0.8);
        }
        SkillFx.infernoRing(player, victims);
        sound(player, SoundEvents.BLAZE_SHOOT, 0.7F);
        return true;
    }

    private static boolean venomStrike(ServerPlayer player) {
        LivingEntity victim = target(player, 7.0);
        if (victim == null) {
            return false;
        }
        effect(victim, MobEffects.POISON, 6, 1);
        effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 3, 0);
        magic(player, victim, 4.0F);
        SkillFx.venomStrike(player, victim);
        sound(player, SoundEvents.SPIDER_AMBIENT, 1.0F);
        return true;
    }

    private static boolean silkSnare(ServerPlayer player) {
        List<LivingEntity> victims = enemies(player, 6.0);
        for (LivingEntity victim : victims) {
            effect(victim, MobEffects.MOVEMENT_SLOWDOWN, 5, 4);
            effect(victim, MobEffects.WEAKNESS, 5, 0);
        }
        SkillFx.silkSnare(player, victims);
        sound(player, SoundEvents.COBWEB_PLACE, 1.0F);
        return true;
    }

    private static boolean magmaShield(ServerPlayer player) {
        effect(player, MobEffects.FIRE_RESISTANCE, 15, 0);
        effect(player, MobEffects.DAMAGE_RESISTANCE, 10, 0);
        SkillFx.magmaShield(player);
        sound(player, SoundEvents.MAGMA_CUBE_SQUISH, 1.0F);
        return true;
    }

    private static boolean eruption(ServerPlayer player) {
        List<LivingEntity> victims = enemies(player, 5.0);
        for (LivingEntity victim : victims) {
            magic(player, victim, 8.0F);
            victim.setRemainingFireTicks(6 * SharedConstants.TICKS_PER_SECOND);
            lift(victim, 0.7);
        }
        SkillFx.eruption(player, victims);
        sound(player, SoundEvents.FIRECHARGE_USE, 0.8F);
        return true;
    }

    private static boolean voidPull(ServerPlayer player) {
        LivingEntity victim = target(player, 14.0);
        if (victim == null) {
            return false;
        }
        Vec3 pull = player.position().subtract(victim.position()).normalize().scale(1.4);
        victim.setDeltaMovement(pull.x, 0.3, pull.z);
        victim.hurtMarked = true;
        SkillFx.voidPull(player, victim);
        sound(player, SoundEvents.ENDERMAN_TELEPORT, 0.8F);
        return true;
    }

    private static boolean phaseShift(ServerPlayer player) {
        effect(player, MobEffects.INVISIBILITY, 6, 0);
        effect(player, MobEffects.MOVEMENT_SPEED, 6, 1);
        SkillFx.phaseShift(player);
        sound(player, SoundEvents.ENDERMAN_TELEPORT, 1.2F);
        return true;
    }
}