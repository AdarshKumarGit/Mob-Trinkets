package org.chubby.github.mobtrinkets.skill;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.slf4j.Logger;

/**
 * Server-side particle choreography for trinket skills.
 *
 * <p>Every skill gets its own colour palette and motion: spirals that climb, rings that expand,
 * orbiting motes, trails that follow the caster, beams that travel to their target and burst on impact.
 * Animated effects are driven by a tiny tick scheduler so they play out over several ticks instead of
 * appearing as one flat puff.</p>
 *
 * <p>Notes on the particle tricks used here:
 * a particle sent with count 0 uses the offset as its velocity, which lets us fling particles in a
 * direction. Dust particles only keep a tenth of that velocity, so dust "speeds" look large on purpose.
 * Portal / enchant / nautilus particles do the opposite and fly <em>into</em> their spawn point from the
 * offset, which gives us cheap implosions.</p>
 */
public final class SkillFx {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double TAU = Math.PI * 2.0;

    private static final Vec3 AXIS_X = new Vec3(1.0, 0.0, 0.0);
    private static final Vec3 AXIS_Z = new Vec3(0.0, 0.0, 1.0);

    private static final DustParticleOptions GOLD = dust(1.00F, 0.82F, 0.18F, 1.1F);
    private static final DustParticleOptions AMBER = dust(1.00F, 0.55F, 0.08F, 0.9F);
    private static final DustParticleOptions BONE = dust(0.94F, 0.92F, 0.80F, 1.2F);
    private static final DustParticleOptions CHALK = dust(0.92F, 0.92F, 0.97F, 1.3F);
    private static final DustParticleOptions WATER = dust(0.20F, 0.60F, 1.00F, 1.1F);
    private static final DustParticleOptions AQUA = dust(0.35F, 0.95F, 0.95F, 1.0F);
    private static final DustParticleOptions PRISMARINE = dust(0.45F, 0.85F, 0.70F, 1.0F);
    private static final DustParticleOptions SLIME = dust(0.45F, 0.92F, 0.30F, 1.2F);
    private static final DustParticleOptions CREEPER = dust(0.25F, 1.00F, 0.25F, 1.3F);
    private static final DustParticleOptions VENOM = dust(0.60F, 0.95F, 0.20F, 1.0F);
    private static final DustParticleOptions TOXIN = dust(0.55F, 0.15F, 0.75F, 1.0F);
    private static final DustParticleOptions WEB = dust(0.95F, 0.95F, 1.00F, 0.7F);
    private static final DustParticleOptions SPIRIT = dust(0.55F, 0.95F, 1.00F, 1.0F);
    private static final DustParticleOptions VOID = dust(0.55F, 0.15F, 0.90F, 1.1F);
    private static final DustColorTransitionOptions FIRE = fade(1.00F, 0.50F, 0.05F, 0.80F, 0.08F, 0.00F, 1.2F);
    private static final DustColorTransitionOptions MAGMA = fade(1.00F, 0.35F, 0.05F, 0.35F, 0.04F, 0.00F, 1.2F);
    private static final DustColorTransitionOptions WING = fade(0.35F, 0.95F, 0.85F, 0.55F, 0.30F, 1.00F, 1.2F);
    private static final DustColorTransitionOptions ABYSS = fade(0.70F, 0.30F, 1.00F, 0.10F, 0.00F, 0.30F, 1.2F);

    private SkillFx() {
    }

    // ------------------------------------------------------------------------------------------
    // Tiny tick scheduler
    // ------------------------------------------------------------------------------------------

    private static final class Task {
        private final int delay;
        private final int duration;
        private final IntConsumer frame;
        private final BooleanSupplier valid;
        private int age;

        private Task(int delay, int duration, IntConsumer frame, BooleanSupplier valid) {
            this.delay = delay;
            this.duration = duration;
            this.frame = frame;
            this.valid = valid;
        }
    }

    @FunctionalInterface
    private interface Frame {
        void run(ServerLevel level, int tick);
    }

    private static final List<Task> ACTIVE = new ArrayList<>();
    private static final List<Task> QUEUED = new ArrayList<>();

    /** Advances every running effect by one tick. Called once per server tick. */
    public static void tick() {
        if (!QUEUED.isEmpty()) {
            ACTIVE.addAll(QUEUED);
            QUEUED.clear();
        }
        Iterator<Task> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            if (!task.valid.getAsBoolean()) {
                iterator.remove();
                continue;
            }
            if (task.age >= task.delay) {
                try {
                    task.frame.accept(task.age - task.delay);
                } catch (RuntimeException exception) {
                    LOGGER.error("Skill particle effect failed and was cancelled", exception);
                    iterator.remove();
                    continue;
                }
            }
            task.age++;
            if (task.age - task.delay >= task.duration) {
                iterator.remove();
            }
        }
    }

    /** Drops all running effects, e.g. when the server stops. */
    public static void clear() {
        ACTIVE.clear();
        QUEUED.clear();
    }

    /** Runs {@code frame} once per tick for {@code duration} ticks, starting after {@code delay} ticks. */
    private static void run(ServerLevel level, int delay, int duration, IntConsumer frame) {
        QUEUED.add(new Task(delay, duration, frame, () -> true));
    }

    /** Like {@link #run} but follows an entity and stops early if it dies or disappears. */
    private static void follow(LivingEntity entity, int duration, Frame frame) {
        follow(entity, 0, duration, frame);
    }

    private static void follow(LivingEntity entity, int delay, int duration, Frame frame) {
        QUEUED.add(new Task(delay, duration, tick -> {
            if (entity.level() instanceof ServerLevel level) {
                frame.run(level, tick);
            }
        }, () -> !entity.isRemoved() && entity.isAlive()));
    }

    private static void later(int delay, Runnable action) {
        QUEUED.add(new Task(delay, 1, tick -> action.run(), () -> true));
    }

    // ------------------------------------------------------------------------------------------
    // Building blocks
    // ------------------------------------------------------------------------------------------

    private static DustParticleOptions dust(float r, float g, float b, float scale) {
        return new DustParticleOptions(new Vector3f(r, g, b), scale);
    }

    private static DustColorTransitionOptions fade(float r1, float g1, float b1, float r2, float g2, float b2, float scale) {
        return new DustColorTransitionOptions(new Vector3f(r1, g1, b1), new Vector3f(r2, g2, b2), scale);
    }

    private static Vec3 mid(Entity entity) {
        return entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
    }

    private static Vec3 base(Entity entity) {
        return entity.position().add(0.0, 0.12, 0.0);
    }

    private static Vec3 forward(Entity entity) {
        double yaw = Math.toRadians(entity.getYRot());
        return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
    }

    private static Vec3 right(Entity entity) {
        double yaw = Math.toRadians(entity.getYRot());
        return new Vec3(-Math.cos(yaw), 0.0, -Math.sin(yaw));
    }

    private static Vec3 perpendicular(Vec3 direction) {
        Vec3 reference = Math.abs(direction.y) > 0.95 ? AXIS_X : new Vec3(0.0, 1.0, 0.0);
        return direction.cross(reference).normalize();
    }

    private static Vec3 randomUnit(RandomSource random) {
        double y = random.nextDouble() * 2.0 - 1.0;
        double angle = random.nextDouble() * TAU;
        double horizontal = Math.sqrt(1.0 - y * y);
        return new Vec3(Math.cos(angle) * horizontal, y, Math.sin(angle) * horizontal);
    }

    private static void put(ServerLevel level, ParticleOptions type, Vec3 at) {
        level.sendParticles(type, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    private static void put(ServerLevel level, ParticleOptions type, Vec3 at, int count, double spread, double speed) {
        level.sendParticles(type, at.x, at.y, at.z, count, spread, spread, spread, speed);
    }

    /** Sends one particle with an explicit velocity (count 0 turns the offset into a velocity). */
    private static void fling(ServerLevel level, ParticleOptions type, Vec3 at, Vec3 velocity) {
        level.sendParticles(type, at.x, at.y, at.z, 0, velocity.x, velocity.y, velocity.z, 1.0);
    }

    /** A burst of particles flying out in every direction. */
    private static void sphere(ServerLevel level, ParticleOptions type, Vec3 center, int count, double speed) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < count; i++) {
            fling(level, type, center, randomUnit(random).scale(speed * (0.45 + 0.55 * random.nextDouble())));
        }
    }

    /** Evenly spread points on a sphere surface, handy for domes and bubbles. */
    private static void shell(ServerLevel level, Vec3 center, double radius, int count, ParticleOptions type, double twist) {
        double golden = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < count; i++) {
            double y = 1.0 - (i + 0.5) * 2.0 / count;
            double ringRadius = Math.sqrt(1.0 - y * y);
            double angle = i * golden + twist;
            put(level, type, center.add(Math.cos(angle) * ringRadius * radius, y * radius, Math.sin(angle) * ringRadius * radius));
        }
    }

    private static void circle(ServerLevel level, ParticleOptions type, Vec3 center, Vec3 u, Vec3 v, double radius, int count, double phase) {
        for (int i = 0; i < count; i++) {
            double angle = phase + i * TAU / count;
            put(level, type, center.add(u.scale(Math.cos(angle) * radius)).add(v.scale(Math.sin(angle) * radius)));
        }
    }

    /** A ground ring that rushes outward and decelerates, throwing accent particles as it goes. */
    private static void shockwave(ServerLevel level, Vec3 center, double maxRadius, int ticks, int delay,
                                  ParticleOptions main, ParticleOptions accent, double lift) {
        run(level, delay, ticks, t -> {
            double progress = (t + 1) / (double) ticks;
            double radius = maxRadius * (1.0 - (1.0 - progress) * (1.0 - progress));
            int count = Math.max(14, (int) (radius * 4.5));
            for (int i = 0; i < count; i++) {
                double angle = i * TAU / count + t * 0.2;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                Vec3 point = center.add(cos * radius, 0.0, sin * radius);
                put(level, main, point);
                if (accent != null && i % 3 == 0) {
                    fling(level, accent, point, new Vec3(cos * 0.06, lift, sin * 0.06));
                }
            }
        });
    }

    /** Motes circling the entity at several heights; neighbouring rings turn in opposite directions. */
    private static void orbit(ServerLevel level, Entity entity, int tick, double radius, double[] heights, double spin,
                              ParticleOptions main, ParticleOptions accent) {
        if (tick % 2 != 0) {
            return;
        }
        Vec3 origin = entity.position();
        for (int j = 0; j < heights.length; j++) {
            double direction = (j % 2 == 0) ? 1.0 : -1.0;
            for (int k = 0; k < 3; k++) {
                double angle = direction * tick * spin + k * TAU / 3.0 + j * 0.9;
                double bob = Math.sin(tick * 0.25 + j + k) * 0.08;
                Vec3 point = origin.add(Math.cos(angle) * radius, heights[j] + bob, Math.sin(angle) * radius);
                put(level, k == 0 ? accent : main, point);
            }
        }
    }

    /** A point (or several) that climbs a spiral around the entity, leaving a helix of particles. */
    private static void helix(ServerLevel level, Entity entity, int tick, double radius, double height, int strands, double climb,
                              ParticleOptions main, ParticleOptions accent) {
        Vec3 origin = entity.position();
        double lift = (tick * climb) % height;
        for (int s = 0; s < strands; s++) {
            double angle = tick * 0.5 + s * TAU / strands;
            Vec3 point = origin.add(Math.cos(angle) * radius, lift, Math.sin(angle) * radius);
            put(level, s == 0 ? accent : main, point);
        }
    }

    @FunctionalInterface
    private interface BeamStyle {
        void draw(ServerLevel level, Vec3 core, Vec3 swirl, double progress, int index);
    }

    /**
     * A beam that visibly travels from the caster to the target over a few ticks, optionally arcing
     * upward (sag) and wrapped in a spiral, then calls {@code impact} on arrival.
     */
    private static void beam(ServerPlayer player, Vec3 to, int travelTicks, double sag, double swirlRadius, double swirlTurns,
                             BeamStyle style, Consumer<Vec3> impact) {
        ServerLevel level = player.serverLevel();
        Vec3 from = player.getEyePosition().add(0.0, -0.35, 0.0).add(right(player).scale(0.25));
        Vec3 span = to.subtract(from);
        double length = span.length();
        if (length < 0.1) {
            impact.accept(to);
            return;
        }
        Vec3 direction = span.scale(1.0 / length);
        Vec3 u = perpendicular(direction);
        Vec3 v = direction.cross(u);
        int total = Math.max(4, (int) (length * 3.0));
        int[] cursor = {0};
        run(level, 0, travelTicks, t -> {
            int end = (int) Math.round(total * (t + 1) / (double) travelTicks);
            for (int i = cursor[0]; i <= end && i <= total; i++) {
                double progress = i / (double) total;
                Vec3 core = from.add(span.scale(progress)).add(0.0, Math.sin(progress * Math.PI) * sag, 0.0);
                double angle = progress * swirlTurns * TAU;
                Vec3 swirl = core.add(u.scale(Math.cos(angle) * swirlRadius)).add(v.scale(Math.sin(angle) * swirlRadius));
                style.draw(level, core, swirl, progress, i);
            }
            cursor[0] = end + 1;
        });
        later(travelTicks, () -> impact.accept(to));
    }

    /** Lingering sparkle on a victim after they were hit. */
    private static void afterglow(LivingEntity victim, int ticks, int every, ParticleOptions type, int count, double spread) {
        follow(victim, ticks, (level, t) -> {
            if (t % every == 0) {
                put(level, type, mid(victim), count, spread, 0.0);
            }
        });
    }

    // ------------------------------------------------------------------------------------------
    // Skills
    // ------------------------------------------------------------------------------------------

    /** Bee: a golden pollen cloud that weaves behind the caster like a bee's flight path. */
    public static void pollenDash(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        sphere(l, GOLD, mid(p), 16, 1.5);
        put(l, ParticleTypes.HAPPY_VILLAGER, mid(p), 10, 0.6, 0.0);
        follow(p, 45, (lv, t) -> {
            double wobble = t * 0.55;
            Vec3 point = base(p)
                    .add(right(p).scale(Math.sin(wobble) * 0.5))
                    .add(0.0, 0.9 + Math.sin(wobble * 2.0) * 0.3, 0.0)
                    .subtract(forward(p).scale(0.3));
            put(lv, GOLD, point, 2, 0.04, 0.0);
            if (t % 2 == 0) {
                put(lv, ParticleTypes.WAX_ON, point, 1, 0.12, 0.0);
            }
            if (t % 4 == 0) {
                put(lv, AMBER, mid(p), 3, 0.5, 0.0);
            }
        });
    }

    /** Bee: a shimmering honey shell with slow drips and golden motes circling the caster. */
    public static void honeyShield(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        shockwave(l, base(p), 2.6, 6, 0, GOLD, ParticleTypes.WAX_ON, 0.04);
        shell(l, mid(p), 1.3, 36, GOLD, 0.0);
        follow(p, 50, (lv, t) -> {
            orbit(lv, p, t, 0.95, new double[] {0.3, 1.0, 1.7}, 0.22, GOLD, AMBER);
            if (t % 3 == 0) {
                put(lv, ParticleTypes.FALLING_HONEY, p.position().add(0.0, 2.1, 0.0), 1, 0.45, 0.0);
            }
            if (t % 12 == 0 && t > 0) {
                shell(lv, mid(p), 1.3, 18, GOLD, t * 0.3);
            }
            if (t == 49) {
                sphere(lv, GOLD, mid(p), 16, 1.2);
            }
        });
    }

    /** Skeleton: ribs of bone-white motes circling the caster with the odd crit spark. */
    public static void boneArmor(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        sphere(l, BONE, mid(p), 18, 1.6);
        put(l, ParticleTypes.CRIT, mid(p), 14, 0.7, 0.2);
        shockwave(l, base(p), 2.2, 5, 0, BONE, ParticleTypes.CRIT, 0.1);
        follow(p, 50, (lv, t) -> {
            orbit(lv, p, t, 0.85, new double[] {0.35, 0.95, 1.55}, 0.28, BONE, CHALK);
            if (t % 6 == 0) {
                put(lv, ParticleTypes.CRIT, mid(p).add(randomUnit(lv.getRandom()).scale(0.9)));
            }
            if (t % 8 == 0) {
                put(lv, ParticleTypes.WHITE_ASH, mid(p), 3, 0.7, 0.0);
            }
        });
    }

    /** Skeleton: a bony shockwave that rattles every enemy it passes through. */
    public static void rattle(ServerPlayer p, List<LivingEntity> victims) {
        ServerLevel l = p.serverLevel();
        Vec3 floor = base(p);
        put(l, ParticleTypes.CRIT, mid(p), 20, 1.0, 0.3);
        shockwave(l, floor, 6.0, 8, 0, BONE, ParticleTypes.CRIT, 0.08);
        shockwave(l, floor, 6.0, 8, 3, BONE, ParticleTypes.WHITE_ASH, 0.04);
        for (LivingEntity victim : victims) {
            sphere(l, BONE, mid(victim), 8, 1.2);
            put(l, ParticleTypes.CRIT, mid(victim), 10, 0.4, 0.2);
            afterglow(victim, 20, 2, BONE, 2, 0.35);
        }
    }

    /** Goat: a dust-kicking charge with streaking crits and a swoosh at the horns. */
    public static void ramCharge(ServerPlayer p, List<LivingEntity> hit) {
        ServerLevel l = p.serverLevel();
        RandomSource random = l.getRandom();
        Vec3 look = p.getLookAngle();
        Vec3 behind = look.scale(-0.35);
        for (int i = 0; i < 14; i++) {
            fling(l, ParticleTypes.CLOUD, mid(p), behind.add((random.nextDouble() - 0.5) * 0.2, (random.nextDouble() - 0.5) * 0.15, (random.nextDouble() - 0.5) * 0.2));
        }
        follow(p, 14, (lv, t) -> {
            put(lv, ParticleTypes.CLOUD, mid(p), 3, 0.25, 0.01);
            put(lv, CHALK, base(p).add(0.0, 0.2, 0.0), 2, 0.2, 0.0);
            fling(lv, ParticleTypes.CRIT, mid(p), look.scale(-0.5));
            if (t % 3 == 0) {
                put(lv, ParticleTypes.SWEEP_ATTACK, mid(p).add(look.scale(1.3)));
            }
        });
        for (LivingEntity victim : hit) {
            sphere(l, ParticleTypes.CRIT, mid(victim), 14, 0.5);
            put(l, ParticleTypes.POOF, mid(victim), 8, 0.3, 0.05);
            put(l, ParticleTypes.EXPLOSION, mid(victim));
        }
    }

    /** Goat: a booming horn - sonic crack, two rolling shockwaves and a ring of coloured notes. */
    public static void hornBlast(ServerPlayer p, List<LivingEntity> victims) {
        ServerLevel l = p.serverLevel();
        Vec3 center = mid(p);
        put(l, ParticleTypes.SONIC_BOOM, center);
        shockwave(l, base(p), 7.0, 8, 0, CHALK, ParticleTypes.CLOUD, 0.05);
        shockwave(l, base(p), 7.0, 8, 2, GOLD, ParticleTypes.END_ROD, 0.03);
        for (int i = 0; i < 12; i++) {
            double angle = i * TAU / 12.0;
            // Note particles read their colour from the x velocity (0..1).
            fling(l, ParticleTypes.NOTE, center.add(Math.cos(angle) * 1.2, 0.3, Math.sin(angle) * 1.2), new Vec3(i / 12.0, 0.0, 0.0));
        }
        for (LivingEntity victim : victims) {
            sphere(l, ParticleTypes.CLOUD, mid(victim), 12, 0.2);
            put(l, ParticleTypes.CRIT, mid(victim), 8, 0.4, 0.2);
        }
    }

    /** Drowned: a rising ribbon of water with bubbles and swirls drawn into the caster. */
    public static void tidalSurge(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        shockwave(l, base(p), 3.2, 7, 0, WATER, ParticleTypes.SPLASH, 0.12);
        put(l, ParticleTypes.SPLASH, mid(p), 30, 0.7, 0.2);
        follow(p, 45, (lv, t) -> {
            helix(lv, p, t, 0.85, 2.3, 2, 0.11, WATER, AQUA);
            if (t % 3 == 0) {
                put(lv, ParticleTypes.BUBBLE, mid(p), 3, 0.6, 0.02);
            }
            if (t % 4 == 0) {
                fling(lv, ParticleTypes.NAUTILUS, mid(p), randomUnit(lv.getRandom()).scale(1.8));
            }
            if (t % 6 == 0) {
                put(lv, ParticleTypes.DOLPHIN, mid(p), 3, 0.7, 0.0);
            }
        });
    }

    /** Drowned: a torpedo of water - a twin-strand corkscrew wake and a splash on every enemy hit. */
    public static void riptideBurst(ServerPlayer p, List<LivingEntity> victims) {
        ServerLevel l = p.serverLevel();
        Vec3 direction = p.getLookAngle();
        Vec3 u = perpendicular(direction);
        Vec3 v = direction.cross(u);
        put(l, ParticleTypes.SPLASH, mid(p), 40, 0.8, 0.2);
        sphere(l, WATER, mid(p), 16, 1.4);
        follow(p, 16, (lv, t) -> {
            Vec3 center = mid(p);
            for (int s = 0; s < 2; s++) {
                double angle = t * 0.9 + s * Math.PI;
                Vec3 point = center.add(u.scale(Math.cos(angle) * 0.75)).add(v.scale(Math.sin(angle) * 0.75));
                put(lv, s == 0 ? WATER : AQUA, point, 2, 0.04, 0.0);
                put(lv, ParticleTypes.BUBBLE, point, 1, 0.05, 0.0);
            }
            put(lv, ParticleTypes.SPLASH, center, 3, 0.3, 0.1);
            put(lv, ParticleTypes.FISHING, center, 2, 0.3, 0.0);
        });
        for (LivingEntity victim : victims) {
            put(l, ParticleTypes.SPLASH, mid(victim), 20, 0.4, 0.2);
            put(l, ParticleTypes.BUBBLE_POP, mid(victim), 10, 0.4, 0.1);
        }
    }

    /** Slime: a squelchy ground splat, then a springy trail while airborne. */
    public static void slimeBounce(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        shockwave(l, base(p), 2.8, 6, 0, SLIME, ParticleTypes.ITEM_SLIME, 0.12);
        put(l, ParticleTypes.ITEM_SLIME, base(p).add(0.0, 0.2, 0.0), 24, 0.45, 0.1);
        follow(p, 18, (lv, t) -> {
            put(lv, SLIME, base(p).add(0.0, 0.3, 0.0), 3, 0.25, 0.0);
            if (t % 2 == 0) {
                put(lv, ParticleTypes.ITEM_SLIME, mid(p), 2, 0.3, 0.0);
            }
            if (t % 4 == 0) {
                circle(lv, SLIME, mid(p), AXIS_X, AXIS_Z, 0.7 + t * 0.04, 10, t * 0.5);
            }
        });
    }

    /** Slime: two wobbling goo waves rolling outward, splattering the enemies they shove. */
    public static void slimeWave(ServerPlayer p, List<LivingEntity> victims) {
        ServerLevel l = p.serverLevel();
        shockwave(l, base(p), 6.0, 8, 0, SLIME, ParticleTypes.ITEM_SLIME, 0.14);
        shockwave(l, base(p), 6.0, 8, 3, SLIME, ParticleTypes.ITEM_SLIME, 0.2);
        for (LivingEntity victim : victims) {
            put(l, ParticleTypes.ITEM_SLIME, mid(victim), 16, 0.5, 0.1);
            sphere(l, SLIME, mid(victim), 10, 1.0);
        }
    }

    /** Phantom: spectral wings that flap open on both sides, then a ghostly feather trail. */
    public static void glide(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        sphere(l, WING, mid(p), 14, 1.2);
        put(l, ParticleTypes.CLOUD, mid(p), 12, 0.5, 0.05);
        follow(p, 36, (lv, t) -> {
            Vec3 chest = mid(p).add(0.0, 0.2, 0.0);
            Vec3 side = right(p);
            Vec3 front = forward(p);
            if (t < 16 && t % 2 == 0) {
                double flap = Math.sin(t * 0.5);
                for (int sign = -1; sign <= 1; sign += 2) {
                    for (int i = 1; i <= 6; i++) {
                        double s = i / 6.0;
                        Vec3 point = chest
                                .add(side.scale(sign * (0.35 + 1.7 * s)))
                                .add(0.0, flap * 0.8 * s + Math.sin(s * Math.PI) * 0.3, 0.0)
                                .subtract(front.scale(0.25 + 0.5 * s * s));
                        put(lv, WING, point);
                    }
                    put(lv, ParticleTypes.END_ROD, chest.add(side.scale(sign * 2.05)).add(0.0, flap * 0.8, 0.0).subtract(front.scale(0.75)));
                }
            }
            Vec3 tail = base(p).add(0.0, 0.9, 0.0).subtract(front.scale(0.5));
            put(lv, WING, tail, 2, 0.25, 0.0);
            if (t % 3 == 0) {
                fling(lv, ParticleTypes.CLOUD, tail, front.scale(-0.05));
            }
        });
    }

    /** Phantom: a haunting wave of spirits, with a ghostly spiral wrapped around each cursed enemy. */
    public static void haunt(ServerPlayer p, List<LivingEntity> victims) {
        ServerLevel l = p.serverLevel();
        shockwave(l, base(p), 8.0, 10, 0, SPIRIT, ParticleTypes.SOUL, 0.08);
        shockwave(l, base(p), 8.0, 10, 4, SPIRIT, ParticleTypes.SCULK_SOUL, 0.05);
        for (LivingEntity victim : victims) {
            sphere(l, SPIRIT, mid(victim), 10, 1.0);
            follow(victim, 36, (lv, t) -> {
                double angle = t * 0.5;
                double lift = (t * 0.1) % (victim.getBbHeight() + 0.4);
                Vec3 origin = victim.position();
                for (int s = 0; s < 2; s++) {
                    put(lv, SPIRIT, origin.add(Math.cos(angle + s * Math.PI) * 0.6, lift, Math.sin(angle + s * Math.PI) * 0.6));
                }
                if (t % 3 == 0) {
                    put(lv, ParticleTypes.SOUL, origin.add(0.0, 0.2, 0.0), 1, 0.3, 0.0);
                }
                if (t == 0) {
                    put(lv, ParticleTypes.SCULK_SOUL, mid(victim), 6, 0.3, 0.05);
                }
            });
        }
    }

    /** Guardian: a pulsing crystalline dome with eye-shards orbiting in opposite directions. */
    public static void guardiansWard(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        shockwave(l, base(p), 3.2, 7, 0, AQUA, ParticleTypes.BUBBLE_POP, 0.05);
        shell(l, mid(p), 1.5, 44, AQUA, 0.0);
        follow(p, 56, (lv, t) -> {
            orbit(lv, p, t, 1.1, new double[] {1.0}, 0.18, AQUA, PRISMARINE);
            orbit(lv, p, t, 0.75, new double[] {0.45, 1.6}, -0.3, PRISMARINE, AQUA);
            if (t % 14 == 0 && t > 0) {
                shell(lv, mid(p), 1.5, 24, AQUA, t * 0.2);
            }
            if (t % 5 == 0) {
                put(lv, ParticleTypes.BUBBLE_POP, mid(p), 2, 0.9, 0.0);
            }
            if (t == 55) {
                sphere(lv, AQUA, mid(p), 18, 1.2);
            }
        });
    }

    /** Creeper: a single sharp, green-tinged blast with smoke and sparks. */
    public static void volatileBurst(ServerPlayer p, List<LivingEntity> victims) {
        ServerLevel l = p.serverLevel();
        Vec3 center = mid(p);
        sphere(l, CREEPER, center, 36, 1.7);
        sphere(l, ParticleTypes.POOF, center, 14, 0.25);
        sphere(l, ParticleTypes.LARGE_SMOKE, center, 10, 0.15);
        sphere(l, ParticleTypes.FIREWORK, center, 10, 0.3);
        put(l, ParticleTypes.EXPLOSION, center, 2, 0.8, 0.0);
        shockwave(l, base(p), 4.5, 6, 0, CREEPER, ParticleTypes.POOF, 0.06);
        for (LivingEntity victim : victims) {
            sphere(l, CREEPER, mid(victim), 10, 0.9);
            put(l, ParticleTypes.POOF, mid(victim), 6, 0.3, 0.05);
        }
    }

    /** Creeper: explosions that ripple outward - along the enemies, around the caster, then one big finale. */
    public static void chainDetonation(ServerPlayer p, List<LivingEntity> victims) {
        ServerLevel l = p.serverLevel();
        Vec3 origin = mid(p);
        Vec3 floor = base(p);
        List<LivingEntity> ordered = new ArrayList<>(victims);
        ordered.sort(Comparator.comparingDouble(victim -> victim.distanceToSqr(p)));
        int delay = 0;
        for (LivingEntity victim : ordered.subList(0, Math.min(8, ordered.size()))) {
            delay += 2;
            later(delay, () -> blast(l, mid(victim)));
        }
        for (int k = 0; k < 8; k++) {
            double angle = k * TAU / 8.0;
            double radius = (k % 2 == 0) ? 2.5 : 5.0;
            Vec3 at = origin.add(Math.cos(angle) * radius, -0.4, Math.sin(angle) * radius);
            later(1 + k * 2, () -> blast(l, at));
        }
        later(18, () -> {
            put(l, ParticleTypes.EXPLOSION_EMITTER, origin);
            shockwave(l, floor, 6.5, 8, 0, CREEPER, ParticleTypes.POOF, 0.08);
        });
    }

    private static void blast(ServerLevel level, Vec3 center) {
        sphere(level, CREEPER, center, 14, 1.2);
        sphere(level, ParticleTypes.POOF, center, 6, 0.2);
        put(level, ParticleTypes.EXPLOSION, center);
        put(level, ParticleTypes.CRIT, center, 6, 0.4, 0.3);
    }

    /** Blaze: a flaming whip that arcs from the hand to the target, trailing sparks and lava pops. */
    public static void flameLash(ServerPlayer p, LivingEntity victim) {
        ServerLevel l = p.serverLevel();
        beam(p, mid(victim), 3, 0.9, 0.22, 4.0, (lv, core, swirl, progress, index) -> {
            put(lv, FIRE, swirl);
            put(lv, ParticleTypes.FLAME, core, 1, 0.05, 0.01);
            if (index % 3 == 0) {
                put(lv, ParticleTypes.SMALL_FLAME, swirl, 1, 0.1, 0.0);
            }
            if (index % 5 == 0) {
                put(lv, ParticleTypes.LAVA, core);
            }
        }, target -> {
            scorch(l, target);
            put(l, ParticleTypes.SMOKE, target, 5, 0.3, 0.02);
            afterglow(victim, 20, 2, ParticleTypes.SMALL_FLAME, 2, 0.3);
        });
    }

    /** Blaze: a ring of fire that races outward, then keeps burning with leaping flames. */
    public static void infernoRing(ServerPlayer p, List<LivingEntity> victims) {
        ServerLevel l = p.serverLevel();
        Vec3 center = base(p);
        double radius = 5.5;
        shockwave(l, center, radius, 7, 0, FIRE, ParticleTypes.FLAME, 0.06);
        run(l, 7, 26, t -> {
            RandomSource random = l.getRandom();
            for (int k = 0; k < 6; k++) {
                double angle = random.nextDouble() * TAU;
                Vec3 point = center.add(Math.cos(angle) * radius, 0.1, Math.sin(angle) * radius);
                fling(l, ParticleTypes.SMALL_FLAME, point, new Vec3(0.0, 0.04 + random.nextDouble() * 0.04, 0.0));
                if (k % 2 == 0) {
                    fling(l, ParticleTypes.FLAME, point, new Vec3(0.0, 0.08 + random.nextDouble() * 0.06, 0.0));
                }
            }
            for (int h = 0; h < 3; h++) {
                double angle = t * 0.35 + h * TAU / 3.0;
                put(l, ParticleTypes.FLAME, center.add(Math.cos(angle) * radius, 0.3, Math.sin(angle) * radius), 2, 0.1, 0.02);
            }
            if (t % 4 == 0) {
                circle(l, FIRE, center.add(0.0, 0.1, 0.0), AXIS_X, AXIS_Z, radius, 30, t * 0.1);
            }
        });
        for (LivingEntity victim : victims) {
            scorch(l, mid(victim));
        }
    }

    private static void scorch(ServerLevel level, Vec3 center) {
        sphere(level, ParticleTypes.FLAME, center, 14, 0.25);
        sphere(level, FIRE, center, 10, 1.4);
        put(level, ParticleTypes.LAVA, center, 4, 0.4, 0.0);
    }

    /** Spider: a toxic purple-and-green dart, leaving the victim in a dripping poison cloud. */
    public static void venomStrike(ServerPlayer p, LivingEntity victim) {
        ServerLevel l = p.serverLevel();
        beam(p, mid(victim), 3, 0.0, 0.25, 4.0, (lv, core, swirl, progress, index) -> {
            put(lv, VENOM, core);
            put(lv, TOXIN, swirl);
            if (index % 3 == 0) {
                put(lv, ParticleTypes.WITCH, swirl);
            }
        }, target -> {
            sphere(l, VENOM, target, 16, 1.2);
            put(l, ParticleTypes.WITCH, target, 12, 0.4, 0.1);
            follow(victim, 24, (lv, t) -> {
                RandomSource random = lv.getRandom();
                Vec3 at = mid(victim).add((random.nextDouble() - 0.5) * 0.7, (random.nextDouble() - 0.5) * 0.7, (random.nextDouble() - 0.5) * 0.7);
                fling(lv, VENOM, at, new Vec3(0.0, -0.6, 0.0));
                if (t % 2 == 0) {
                    put(lv, ParticleTypes.WITCH, mid(victim), 1, 0.3, 0.0);
                }
            });
        });
    }

    /** Spider: a spiderweb spun across the ground - spokes first, then the rings - that lingers briefly. */
    public static void silkSnare(ServerPlayer p, List<LivingEntity> victims) {
        ServerLevel l = p.serverLevel();
        Vec3 center = base(p);
        double radius = 5.5;
        double[] rings = {1.6, 3.2, 4.8};
        int spokes = 8;
        int grow = 8;
        double[] reached = {0.0};
        run(l, 0, grow, t -> {
            double now = radius * (t + 1) / grow;
            webSpokes(l, center, spokes, reached[0], now, 0.45);
            for (double ring : rings) {
                if (ring > reached[0] && ring <= now) {
                    webRing(l, center, spokes, ring, 0.55);
                }
            }
            reached[0] = now;
        });
        for (int k = 0; k < 2; k++) {
            later(14 + k * 10, () -> {
                webSpokes(l, center, spokes, 0.0, radius, 1.0);
                for (double ring : rings) {
                    webRing(l, center, spokes, ring, 1.0);
                }
            });
        }
        for (LivingEntity victim : victims) {
            put(l, WEB, mid(victim), 16, 0.5, 0.0);
            follow(victim, 20, (lv, t) -> {
                double angle = t * 0.8;
                double lift = (t * 0.12) % victim.getBbHeight();
                Vec3 origin = victim.position();
                put(lv, WEB, origin.add(Math.cos(angle) * 0.55, lift, Math.sin(angle) * 0.55));
                put(lv, WEB, origin.add(Math.cos(angle + Math.PI) * 0.55, victim.getBbHeight() - lift, Math.sin(angle + Math.PI) * 0.55));
            });
        }
    }

    private static void webSpokes(ServerLevel level, Vec3 center, int spokes, double from, double to, double step) {
        for (int s = 0; s < spokes; s++) {
            double angle = s * TAU / spokes + 0.2;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            for (double d = from; d <= to; d += step) {
                put(level, WEB, center.add(cos * d, 0.05, sin * d));
            }
        }
    }

    /** Draws one ring of the web as straight threads between neighbouring spokes, like a real web. */
    private static void webRing(ServerLevel level, Vec3 center, int spokes, double radius, double step) {
        for (int s = 0; s < spokes; s++) {
            double a1 = s * TAU / spokes + 0.2;
            double a2 = (s + 1) * TAU / spokes + 0.2;
            Vec3 start = center.add(Math.cos(a1) * radius, 0.05, Math.sin(a1) * radius);
            Vec3 end = center.add(Math.cos(a2) * radius, 0.05, Math.sin(a2) * radius);
            int points = Math.max(1, (int) (start.distanceTo(end) / step));
            for (int i = 0; i < points; i++) {
                put(level, WEB, start.lerp(end, i / (double) points));
            }
        }
    }

    /** Magma cube: a cracked, glowing shell of embers with lava dripping off it. */
    public static void magmaShield(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        shockwave(l, base(p), 2.8, 6, 0, MAGMA, ParticleTypes.FLAME, 0.05);
        sphere(l, MAGMA, mid(p), 18, 1.3);
        put(l, ParticleTypes.LAVA, mid(p), 6, 0.6, 0.0);
        follow(p, 56, (lv, t) -> {
            orbit(lv, p, t, 0.9, new double[] {0.3, 1.0, 1.7}, 0.2, MAGMA, MAGMA);
            if (t % 2 == 0) {
                circle(lv, MAGMA, base(p), AXIS_X, AXIS_Z, 1.1, 4, t * 0.35);
                put(lv, ParticleTypes.SMALL_FLAME, mid(p).add(randomUnit(lv.getRandom()).scale(0.9)));
            }
            if (t % 4 == 0) {
                put(lv, ParticleTypes.DRIPPING_LAVA, p.position().add(0.0, 2.0, 0.0), 1, 0.4, 0.0);
            }
            if (t % 9 == 0) {
                put(lv, ParticleTypes.ASH, mid(p), 4, 0.8, 0.0);
            }
        });
    }

    /** Magma cube: a volcano - a spitting fountain of fire, smoke and lava from the caster, ringed by flame pillars. */
    public static void eruption(ServerPlayer p, List<LivingEntity> victims) {
        ServerLevel l = p.serverLevel();
        Vec3 center = base(p);
        shockwave(l, center, 4.8, 7, 0, FIRE, ParticleTypes.FLAME, 0.1);
        put(l, ParticleTypes.EXPLOSION, center.add(0.0, 0.4, 0.0));
        run(l, 0, 14, t -> {
            RandomSource random = l.getRandom();
            for (int k = 0; k < 6; k++) {
                double angle = random.nextDouble() * TAU;
                double reach = random.nextDouble() * 1.2;
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                Vec3 point = center.add(cos * reach, 0.1, sin * reach);
                fling(l, ParticleTypes.FLAME, point, new Vec3(cos * 0.05, 0.35 + random.nextDouble() * 0.35, sin * 0.05));
                if (k % 2 == 0) {
                    fling(l, FIRE, point, new Vec3(cos * 0.4, 3.0 + random.nextDouble() * 2.5, sin * 0.4));
                }
            }
            put(l, ParticleTypes.LAVA, center.add(0.0, 0.5, 0.0), 3, 0.8, 0.0);
            if (t % 2 == 0) {
                fling(l, ParticleTypes.LARGE_SMOKE, center.add(0.0, 0.5, 0.0), new Vec3(0.0, 0.25, 0.0));
            }
            for (int k = 0; k < 3; k++) {
                double angle = random.nextDouble() * TAU;
                Vec3 point = center.add(Math.cos(angle) * 4.4, 0.1, Math.sin(angle) * 4.4);
                fling(l, ParticleTypes.FLAME, point, new Vec3(0.0, 0.3 + random.nextDouble() * 0.2, 0.0));
            }
        });
        for (LivingEntity victim : victims) {
            scorch(l, mid(victim));
        }
    }

    /** Ender: a violet stream pours from the target toward the caster while the victim's space collapses. */
    public static void voidPull(ServerPlayer p, LivingEntity victim) {
        ServerLevel l = p.serverLevel();
        RandomSource random = l.getRandom();
        beam(p, mid(victim), 3, 0.0, 0.3, 3.0, (lv, core, swirl, progress, index) -> {
            put(lv, ABYSS, swirl);
            if (index % 2 == 0) {
                // Portal particles fly toward their spawn point, so the stream flows to the caster.
                fling(lv, ParticleTypes.PORTAL, core, mid(victim).subtract(core).scale(0.35));
            }
        }, target -> {
            for (int i = 0; i < 20; i++) {
                fling(l, ParticleTypes.PORTAL, target, randomUnit(random).scale(1.8));
            }
            run(l, 0, 7, t -> circle(l, ABYSS, target, AXIS_X, AXIS_Z, 2.4 * (1.0 - (t + 1) / 7.0) + 0.2, 20, t * 0.4));
        });
        follow(victim, 14, (lv, t) -> put(lv, ABYSS, mid(victim), 2, 0.3, 0.0));
        Vec3 gather = mid(p).add(p.getLookAngle().scale(0.8));
        for (int i = 0; i < 14; i++) {
            fling(l, ParticleTypes.PORTAL, gather, randomUnit(random).scale(1.2));
        }
    }

    /** Ender: the caster implodes into the void, dissolves in a violet column, and shimmers faintly while hidden. */
    public static void phaseShift(ServerPlayer p) {
        ServerLevel l = p.serverLevel();
        RandomSource random = l.getRandom();
        Vec3 center = mid(p);
        for (int i = 0; i < 36; i++) {
            fling(l, ParticleTypes.PORTAL, center, randomUnit(random).scale(1.2 + random.nextDouble() * 1.6));
        }
        sphere(l, ParticleTypes.END_ROD, center, 8, 0.12);
        run(l, 0, 8, t -> circle(l, ABYSS, base(p), AXIS_X, AXIS_Z, 2.2 * (1.0 - (t + 1) / 8.0) + 0.2, 18, t * 0.5));
        follow(p, 20, (lv, t) -> {
            helix(lv, p, t, 0.65, 2.0, 2, 0.12, ABYSS, VOID);
            put(lv, ParticleTypes.PORTAL, mid(p), 2, 0.4, 0.1);
            if (t == 19) {
                sphere(lv, VOID, mid(p), 24, 1.5);
                sphere(lv, ParticleTypes.REVERSE_PORTAL, mid(p), 12, 0.15);
            }
        });
        follow(p, 20, 100, (lv, t) -> {
            if (t % 8 == 0) {
                put(lv, ParticleTypes.PORTAL, mid(p), 3, 0.35, 0.05);
            }
        });
    }
}