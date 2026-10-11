package org.chubby.github.mobtrinkets.skill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.chubby.github.mobtrinkets.network.ModPayloads.BeamStatePayload;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;
import org.chubby.github.mobtrinkets.trinket.TrinketSkill;
import org.joml.Vector3f;

public final class PrismaticBeamChannel {
    private static final int CHARGE_TICKS = 36;
    private static final int MAX_FIRE_TICKS = 60;
    private static final double RANGE = 24.0;
    private static final int DAMAGE_INTERVAL = 5;
    private static final float DAMAGE = 4.0F;
    private static final int SYNC_INTERVAL = 4;
    private static final int SLOW_REFRESH = 10;
    private static final int SLOW_DURATION = 14;
    private static final int SLOW_AMPLIFIER = 2;
    private static final int CHARGE_FX_INTERVAL = 2;
    private static final int CHIME_INTERVAL = 6;
    private static final int HUM_INTERVAL = 10;
    private static final int RING_POINTS = 8;
    private static final double RING_START_RADIUS = 1.4;
    private static final double RING_END_RADIUS = 0.15;
    private static final double BEAM_DUST_SPACING = 1.0;
    private static final double BEAM_ROD_SPACING = 2.0;
    private static final double HIT_INFLATE = 0.35;
    private static final DustParticleOptions BEAM_DUST = new DustParticleOptions(new Vector3f(0.35F, 1.0F, 0.9F), 1.5F);

    private static final Map<UUID, Channel> CHANNELS = new HashMap<>();

    private PrismaticBeamChannel() {
    }

    private static final class Channel {
        private final int slot;
        private final boolean secondary;
        private int ticks;
        private boolean firing;
        private int firingTicks;

        private Channel(int slot, boolean secondary) {
            this.slot = slot;
            this.secondary = secondary;
        }
    }

    public static void start(ServerPlayer player, int slot, boolean secondary) {
        if (CHANNELS.containsKey(player.getUUID())) {
            return;
        }
        CHANNELS.put(player.getUUID(), new Channel(slot, secondary));
        sendState(player, BeamStatePayload.CHARGING, 0, CHARGE_TICKS);
        play(player, SoundEvents.AMETHYST_BLOCK_CHIME, 0.6F, 0.6F);
    }

    public static void release(ServerPlayer player) {
        Channel channel = CHANNELS.get(player.getUUID());
        if (channel != null) {
            end(player, channel.firing);
        }
    }

    public static void tickAll(MinecraftServer server) {
        if (CHANNELS.isEmpty()) {
            return;
        }
        for (UUID id : List.copyOf(CHANNELS.keySet())) {
            Channel channel = CHANNELS.get(id);
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (channel == null) {
                continue;
            }
            if (player == null) {
                CHANNELS.remove(id);
                continue;
            }
            tick(player, channel);
        }
    }

    public static void clear(UUID playerId) {
        CHANNELS.remove(playerId);
    }

    private static void tick(ServerPlayer player, Channel channel) {
        if (!stillValid(player, channel)) {
            end(player, channel.firing);
            return;
        }
        if (player.tickCount % SLOW_REFRESH == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_DURATION, SLOW_AMPLIFIER, false, false, false));
        }
        if (!channel.firing) {
            tickCharge(player, channel);
            return;
        }
        channel.firingTicks++;
        fireBeam(player, channel.firingTicks);
        if (channel.firingTicks >= MAX_FIRE_TICKS) {
            end(player, true);
        } else if (channel.firingTicks % SYNC_INTERVAL == 0) {
            sendState(player, BeamStatePayload.FIRING, channel.firingTicks, MAX_FIRE_TICKS);
        }
    }

    private static void tickCharge(ServerPlayer player, Channel channel) {
        channel.ticks++;
        double progress = channel.ticks / (double) CHARGE_TICKS;
        if (channel.ticks % CHARGE_FX_INTERVAL == 0) {
            chargeParticles(player, progress);
        }
        if (channel.ticks % CHIME_INTERVAL == 0) {
            play(player, SoundEvents.AMETHYST_BLOCK_CHIME, 0.7F, (float) (0.6 + 1.4 * progress));
        }
        if (channel.ticks >= CHARGE_TICKS) {
            channel.firing = true;
            channel.firingTicks = 0;
            play(player, SoundEvents.WARDEN_SONIC_BOOM, 0.8F, 1.4F);
            sendState(player, BeamStatePayload.FIRING, 0, MAX_FIRE_TICKS);
        } else if (channel.ticks % SYNC_INTERVAL == 0) {
            sendState(player, BeamStatePayload.CHARGING, channel.ticks, CHARGE_TICKS);
        }
    }

    private static boolean stillValid(ServerPlayer player, Channel channel) {
        if (!player.isAlive() || player.isSpectator()) {
            return false;
        }
        TrinketData data = TrinketEquipment.data(player);
        TrinketDefinition definition = TrinketRegistry.forStack(data.stack(channel.slot));
        if (definition == null || !data.isUnlocked(channel.slot)) {
            return false;
        }
        TrinketSkill skill = channel.secondary ? definition.secondary() : definition.primary();
        int required = channel.secondary ? TrinketSkills.SECONDARY_TIER : TrinketSkills.PRIMARY_TIER;
        return skill == TrinketSkill.PRISMATIC_BEAM && TrinketMastery.tier(player, definition) >= required;
    }

    private static void end(ServerPlayer player, boolean startCooldown) {
        CHANNELS.remove(player.getUUID());
        sendState(player, BeamStatePayload.NONE, 0, 1);
        if (startCooldown) {
            TrinketSkills.startCooldown(player, TrinketSkill.PRISMATIC_BEAM);
        }
    }

    private static void sendState(ServerPlayer player, int state, int ticks, int total) {
        PacketDistributor.sendToPlayer(player, new BeamStatePayload(state, ticks, total));
    }

    private static void play(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static Vec3 hand(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        return player.getEyePosition().add(right.scale(0.28)).add(0.0, -0.3, 0.0).add(look.scale(0.5));
    }

    private static void chargeParticles(ServerPlayer player, double progress) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();
        Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        Vec3 up = right.cross(look).normalize();
        Vec3 center = hand(player).add(look.scale(0.4));
        double radius = RING_START_RADIUS + (RING_END_RADIUS - RING_START_RADIUS) * progress;
        for (int i = 0; i < RING_POINTS; i++) {
            double angle = i * Math.PI * 2.0 / RING_POINTS + progress * 6.0;
            Vec3 offset = right.scale(Math.cos(angle) * radius).add(up.scale(Math.sin(angle) * radius));
            Vec3 point = center.add(offset);
            Vec3 inward = offset.normalize().scale(-1.0);
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 0, inward.x, inward.y, inward.z, 0.12);
        }
        ParticleOptions orb = new DustParticleOptions(new Vector3f(0.35F, 1.0F, 0.9F), (float) (0.8 + 1.2 * progress));
        level.sendParticles(orb, center.x, center.y, center.z, 2, 0.05, 0.05, 0.05, 0.0);
    }

    private static void fireBeam(ServerPlayer player, int firingTicks) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        HitResult block = level.clip(new ClipContext(eye, eye.add(look.scale(RANGE)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double length = block.getType() == HitResult.Type.MISS ? RANGE : block.getLocation().distanceTo(eye);
        Vec3 stop = eye.add(look.scale(length));
        Vec3 origin = hand(player);

        beamParticles(level, origin, stop, firingTicks);

        List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class, new AABB(eye, stop).inflate(1.0),
                candidate -> isValidTarget(player, candidate) && candidate.getBoundingBox().inflate(HIT_INFLATE).clip(eye, stop).isPresent());
        if (firingTicks % DAMAGE_INTERVAL == 0) {
            for (LivingEntity victim : hits) {
                victim.invulnerableTime = 0;
                victim.hurt(player.damageSources().indirectMagic(player, player), DAMAGE);
                Vec3 center = victim.getBoundingBox().getCenter();
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 8, 0.25, 0.3, 0.25, 0.3);
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, stop.x, stop.y, stop.z, 3, 0.15, 0.15, 0.15, 0.08);
        if (firingTicks % 2 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, stop.x, stop.y, stop.z, 6, 0.2, 0.2, 0.2, 0.25);
            level.sendParticles(ParticleTypes.SMOKE, stop.x, stop.y, stop.z, 2, 0.1, 0.1, 0.1, 0.02);
        }
        if (firingTicks % HUM_INTERVAL == 1) {
            play(player, SoundEvents.BEACON_AMBIENT, 1.0F, 1.6F);
        }
    }

    private static void beamParticles(ServerLevel level, Vec3 origin, Vec3 stop, int firingTicks) {
        double length = origin.distanceTo(stop);
        int rods = Math.max(2, (int) (length / BEAM_ROD_SPACING));
        for (int i = 0; i <= rods; i++) {
            Vec3 point = origin.lerp(stop, i / (double) rods);
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.03, 0.03, 0.03, 0.0);
        }
        if (firingTicks % 2 == 0) {
            int dust = Math.max(2, (int) (length / BEAM_DUST_SPACING));
            for (int i = 0; i <= dust; i++) {
                Vec3 point = origin.lerp(stop, i / (double) dust);
                level.sendParticles(BEAM_DUST, point.x, point.y, point.z, 2, 0.06, 0.06, 0.06, 0.0);
            }
        }
        level.sendParticles(BEAM_DUST, origin.x, origin.y, origin.z, 3, 0.1, 0.1, 0.1, 0.0);
    }

    private static boolean isValidTarget(ServerPlayer player, LivingEntity candidate) {
        if (candidate == player || !candidate.isAlive() || candidate instanceof Player) {
            return false;
        }
        return !(candidate instanceof OwnableEntity ownable && ownable.getOwnerUUID() != null);
    }
}
