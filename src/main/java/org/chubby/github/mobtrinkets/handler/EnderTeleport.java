package org.chubby.github.mobtrinkets.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.trinket.TrinketAbility;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;

public final class EnderTeleport {
    private static final double MIN_DISTANCE = 1.0;
    private static final double STEP = 0.5;
    private static final double GROUND_PROBE = 0.1;
    private static final int MAX_DROP = 4;
    private static final int TICKS_PER_SECOND = 20;
    private static final int PARTICLE_COUNT = 24;
    private static final double PARTICLE_SPREAD_XZ = 0.3;
    private static final double PARTICLE_SPREAD_Y = 0.6;
    private static final double PARTICLE_SPEED = 0.2;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    private EnderTeleport() {
    }

    public static void tryTeleport(ServerPlayer player) {
        if (!TrinketEquipment.has(player, TrinketAbility.SHORT_TELEPORT)) {
            return;
        }
        if (!player.isAlive() || player.isSpectator() || player.isPassenger() || player.isSleeping()) {
            return;
        }
        Item item = TrinketEquipment.get(player).getItem();
        if (player.getCooldowns().isOnCooldown(item)) {
            return;
        }
        Vec3 destination = findDestination(player);
        if (destination == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        burst(level, player.position());
        player.connection.teleport(destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
        player.resetFallDistance();
        burst(level, destination);
        player.getCooldowns().addCooldown(item, cooldownTicks());
    }

    private static int cooldownTicks() {
        return (int) Math.round(TrinketConfig.ENDER_COOLDOWN_SECONDS.get() * TICKS_PER_SECOND);
    }

    private static void burst(ServerLevel level, Vec3 position) {
        level.sendParticles(ParticleTypes.PORTAL, position.x, position.y + 1.0, position.z,
                PARTICLE_COUNT, PARTICLE_SPREAD_XZ, PARTICLE_SPREAD_Y, PARTICLE_SPREAD_XZ, PARTICLE_SPEED);
        level.playSound(null, position.x, position.y, position.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
    }

    private static Vec3 findDestination(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double range = TrinketConfig.ENDER_RANGE.get();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        HitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(range)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double reach = hit.getType() == HitResult.Type.MISS ? range : hit.getLocation().distanceTo(eye);
        double eyeHeight = player.getEyeHeight();
        for (double distance = reach; distance >= MIN_DISTANCE; distance -= STEP) {
            Vec3 point = eye.add(look.scale(distance));
            Vec3 feet = new Vec3(point.x, point.y - eyeHeight, point.z);
            if (isSafe(level, player, feet)) {
                return feet;
            }
        }
        return null;
    }

    private static boolean isSafe(ServerLevel level, ServerPlayer player, Vec3 feet) {
        AABB box = player.getBoundingBox().move(feet.x - player.getX(), feet.y - player.getY(), feet.z - player.getZ());
        if (box.minY < level.getMinBuildHeight() || box.maxY > level.getMaxBuildHeight()) {
            return false;
        }
        if (!level.hasChunkAt(BlockPos.containing(feet)) || !level.getWorldBorder().isWithinBounds(box)) {
            return false;
        }
        if (!level.noCollision(player, box)) {
            return false;
        }
        return !hasHazard(level, box) && hasGround(level, box);
    }

    private static boolean hasHazard(ServerLevel level, AABB box) {
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (isHazard(level.getBlockState(pos))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasGround(ServerLevel level, AABB box) {
        double centerX = (box.minX + box.maxX) / 2.0;
        double centerZ = (box.minZ + box.maxZ) / 2.0;
        for (int depth = 0; depth <= MAX_DROP; depth++) {
            BlockPos pos = BlockPos.containing(centerX, box.minY - GROUND_PROBE - depth, centerZ);
            BlockState state = level.getBlockState(pos);
            boolean solid = !state.getCollisionShape(level, pos).isEmpty();
            boolean fluid = !state.getFluidState().isEmpty();
            if (solid || fluid) {
                return !isHazard(state) && !state.is(Blocks.MAGMA_BLOCK);
            }
        }
        return false;
    }

    private static boolean isHazard(BlockState state) {
        return state.getFluidState().is(FluidTags.LAVA)
                || state.is(BlockTags.FIRE)
                || state.is(Blocks.CACTUS)
                || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SWEET_BERRY_BUSH);
    }
}
