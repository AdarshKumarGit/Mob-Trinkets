package org.chubby.github.mobtrinkets.trinket;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.config.TrinketConfig;

public enum TrinketAbility {
    SPIDER_CLIMB("spider_climb"),
    EXPLOSION_GUARD("explosion_guard", () -> TrinketConfig.EXPLOSION_REDUCTION.get(), true),
    FIRE_IMMUNITY("fire_immunity"),
    SLOW_FALL("slow_fall"),
    HIGH_JUMP("high_jump", Attributes.JUMP_STRENGTH, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, () -> TrinketConfig.JUMP_BONUS.get(), true),
    WATER_BREATHING("water_breathing"),
    SHORT_TELEPORT("short_teleport"),
    ARROW_BOOST("arrow_boost", () -> TrinketConfig.ARROW_DAMAGE_BONUS.get(), true),
    KNOCKBACK_BOOST("knockback_boost", () -> TrinketConfig.KNOCKBACK_LEVELS.get(), false),
    SWIFTNESS("swiftness", Attributes.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, () -> TrinketConfig.SPEED_BONUS.get(), true),
    SWIM_SPEED("swim_speed", Attributes.WATER_MOVEMENT_EFFICIENCY, AttributeModifier.Operation.ADD_VALUE, () -> TrinketConfig.SWIM_EFFICIENCY.get(), false),
    FIRE_GUARD("fire_guard", () -> TrinketConfig.FIRE_REDUCTION.get(), true);

    private static final List<TrinketAbility> ATTRIBUTE_ABILITIES = Arrays.stream(values()).filter(ability -> ability.attribute != null).toList();
    private static final double PERCENT = 100.0;

    private final String key;
    private final Holder<Attribute> attribute;
    private final AttributeModifier.Operation operation;
    private final DoubleSupplier strength;
    private final boolean percentage;
    private final ResourceLocation modifierId;

    TrinketAbility(String key) {
        this(key, null, null, () -> 0.0, false);
    }

    TrinketAbility(String key, DoubleSupplier strength, boolean percentage) {
        this(key, null, null, strength, percentage);
    }

    TrinketAbility(String key, Holder<Attribute> attribute, AttributeModifier.Operation operation, DoubleSupplier strength, boolean percentage) {
        this.key = key;
        this.attribute = attribute;
        this.operation = operation;
        this.strength = strength;
        this.percentage = percentage;
        this.modifierId = MobTrinkets.id("trinket_" + key);
    }

    public static List<TrinketAbility> attributeAbilities() {
        return ATTRIBUTE_ABILITIES;
    }

    public Holder<Attribute> attribute() {
        return attribute;
    }

    public AttributeModifier.Operation operation() {
        return operation;
    }

    public ResourceLocation modifierId() {
        return modifierId;
    }

    public double strength() {
        return strength.getAsDouble();
    }

    public MutableComponent description() {
        double value = strength();
        Object argument = percentage ? (Object) Math.round(value * PERCENT) : (Object) (Math.round(value * PERCENT) / PERCENT);
        return Component.translatable("tooltip.mobtrinkets." + key, argument);
    }
}
