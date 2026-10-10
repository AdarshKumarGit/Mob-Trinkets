package org.chubby.github.mobtrinkets.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HudConfig {
    public static final double DEFAULT_X = 0.01;
    public static final double DEFAULT_Y = 0.4;
    public static final double DEFAULT_SCALE = 1.0;
    public static final double MIN_SCALE = 0.5;
    public static final double MAX_SCALE = 3.0;

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue VERTICAL;
    public static final ModConfigSpec.DoubleValue X;
    public static final ModConfigSpec.DoubleValue Y;
    public static final ModConfigSpec.DoubleValue SCALE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("hud");
        ENABLED = builder.comment("Show the equipped trinkets overlay").define("enabled", true);
        VERTICAL = builder.comment("Stack the trinkets vertically instead of in a row").define("vertical", true);
        X = builder.comment("Horizontal position of the overlay, 0 is the left edge and 1 the right edge")
                .defineInRange("positionX", DEFAULT_X, 0.0, 1.0);
        Y = builder.comment("Vertical position of the overlay, 0 is the top edge and 1 the bottom edge")
                .defineInRange("positionY", DEFAULT_Y, 0.0, 1.0);
        SCALE = builder.comment("Size multiplier of the overlay")
                .defineInRange("scale", DEFAULT_SCALE, MIN_SCALE, MAX_SCALE);
        builder.pop();
        SPEC = builder.build();
    }

    private HudConfig() {
    }

    public static void resetPosition() {
        X.set(DEFAULT_X);
        Y.set(DEFAULT_Y);
        SCALE.set(DEFAULT_SCALE);
    }

    public static void save() {
        SPEC.save();
    }
}
