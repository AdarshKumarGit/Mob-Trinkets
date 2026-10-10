package org.chubby.github.mobtrinkets.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;

public final class TrinketConfig {
    private static final double DEFAULT_DROP_CHANCE = 0.02;
    private static final Map<String, ModConfigSpec.DoubleValue> DROP_CHANCES = new HashMap<>();

    private static final int[] DEFAULT_UNLOCK_LEVELS = {10, 20, 30};
    private static final List<ModConfigSpec.IntValue> UNLOCK_LEVELS = new ArrayList<>();

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue TIER2_POINTS;
    public static final ModConfigSpec.IntValue TIER3_POINTS;
    public static final ModConfigSpec.IntValue SOURCE_KILL_POINTS;
    public static final ModConfigSpec.DoubleValue TIER2_SCALE;
    public static final ModConfigSpec.DoubleValue TIER3_SCALE;
    public static final ModConfigSpec.DoubleValue SKILL_COOLDOWN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue SIGIL_CHANCE;
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

        builder.push("unlocking");
        for (int slot = 0; slot < DEFAULT_UNLOCK_LEVELS.length; slot++) {
            UNLOCK_LEVELS.add(builder.comment("Experience levels needed to unlock trinket slot " + (slot + 1))
                    .defineInRange("slot" + (slot + 1) + "Levels", DEFAULT_UNLOCK_LEVELS[slot], 0, 100));
        }
        SIGIL_CHANCE = builder.comment("Chance that a rare structure chest contains a Trinket Sigil")
                .defineInRange("sigilChestChance", 0.15, 0.0, 1.0);
        builder.pop();

        builder.push("mastery");
        TIER2_POINTS = builder.comment("Mastery points needed for Tier II (unlocks the first skill)")
                .defineInRange("tier2Points", 40, 1, 100000);
        TIER3_POINTS = builder.comment("Mastery points needed for Tier III (unlocks the second skill)")
                .defineInRange("tier3Points", 150, 1, 100000);
        SOURCE_KILL_POINTS = builder.comment("Points for killing the mob a trinket drops from (other hostile mobs give 1)")
                .defineInRange("sourceKillPoints", 3, 1, 100);
        TIER2_SCALE = builder.comment("Passive strength multiplier at Tier II")
                .defineInRange("tier2Scale", 1.5, 1.0, 5.0);
        TIER3_SCALE = builder.comment("Passive strength multiplier at Tier III")
                .defineInRange("tier3Scale", 2.0, 1.0, 5.0);
        SKILL_COOLDOWN_MULTIPLIER = builder.comment("Multiplier applied to every skill cooldown")
                .defineInRange("skillCooldownMultiplier", 1.0, 0.0, 10.0);
        builder.pop();

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

    public static int unlockLevels(int unlockedCount) {
        return UNLOCK_LEVELS.get(unlockedCount).get();
    }

    public static double dropChance(TrinketDefinition definition) {
        return DROP_CHANCES.get(definition.id()).get();
    }
}
