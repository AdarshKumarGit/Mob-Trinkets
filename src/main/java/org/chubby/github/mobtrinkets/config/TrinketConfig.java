package org.chubby.github.mobtrinkets.config;

import java.util.HashMap;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;

public final class TrinketConfig {
    private static final double DEFAULT_DROP_CHANCE = 0.02;
    private static final Map<String, ModConfigSpec.DoubleValue> DROP_CHANCES = new HashMap<>();

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue DROP_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue LOOTING_BONUS;
    public static final ModConfigSpec.DoubleValue EXPLOSION_REDUCTION;
    public static final ModConfigSpec.DoubleValue FIRE_REDUCTION;
    public static final ModConfigSpec.DoubleValue JUMP_BONUS;
    public static final ModConfigSpec.DoubleValue SPEED_BONUS;
    public static final ModConfigSpec.DoubleValue SWIM_EFFICIENCY;
    public static final ModConfigSpec.DoubleValue KNOCKBACK_LEVELS;
    public static final ModConfigSpec.DoubleValue ARROW_DAMAGE_BONUS;
    public static final ModConfigSpec.DoubleValue ENDER_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue ENDER_RANGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("drops");
        DROP_MULTIPLIER = builder.comment("Global multiplier applied to every trinket drop chance")
                .defineInRange("dropMultiplier", 1.0, 0.0, 100.0);
        LOOTING_BONUS = builder.comment("Extra drop chance added per Looting level")
                .defineInRange("lootingBonus", 0.01, 0.0, 1.0);
        builder.push("chances");
        for (TrinketDefinition definition : TrinketRegistry.all()) {
            DROP_CHANCES.put(definition.id(), builder.comment("Base drop chance for " + definition.id())
                    .defineInRange(definition.id(), DEFAULT_DROP_CHANCE, 0.0, 1.0));
        }
        builder.pop(2);

        builder.push("abilities");
        EXPLOSION_REDUCTION = builder.comment("Creeper Heart: fraction of explosion damage removed")
                .defineInRange("creeperHeartExplosionReduction", 0.25, 0.0, 1.0);
        FIRE_REDUCTION = builder.comment("Magma Core: fraction of fire damage removed")
                .defineInRange("magmaCoreFireReduction", 0.5, 0.0, 1.0);
        JUMP_BONUS = builder.comment("Slime Heart: bonus to jump strength")
                .defineInRange("slimeHeartJumpBonus", 0.2, 0.0, 1.0);
        SPEED_BONUS = builder.comment("Bee Wing: bonus to movement speed")
                .defineInRange("beeWingSpeedBonus", 0.05, 0.0, 1.0);
        SWIM_EFFICIENCY = builder.comment("Drowned Pearl: added water movement efficiency")
                .defineInRange("drownedPearlWaterEfficiency", 0.5, 0.0, 1.0);
        KNOCKBACK_LEVELS = builder.comment("Goat Horn Charm: extra knockback in Knockback enchantment levels")
                .defineInRange("goatHornKnockbackLevels", 0.75, 0.0, 5.0);
        ARROW_DAMAGE_BONUS = builder.comment("Skeleton Bone Charm: bonus to arrow damage")
                .defineInRange("skeletonBoneCharmArrowBonus", 0.1, 0.0, 2.0);
        builder.pop();

        builder.push("ender_eye");
        ENDER_COOLDOWN_SECONDS = builder.comment("Teleport cooldown in seconds")
                .defineInRange("cooldownSeconds", 8.0, 0.0, 600.0);
        ENDER_RANGE = builder.comment("Maximum teleport range in blocks")
                .defineInRange("range", 16.0, 1.0, 64.0);
        builder.pop();

        SPEC = builder.build();
    }

    private TrinketConfig() {
    }

    public static double dropChance(TrinketDefinition definition) {
        return DROP_CHANCES.get(definition.id()).get();
    }
}
