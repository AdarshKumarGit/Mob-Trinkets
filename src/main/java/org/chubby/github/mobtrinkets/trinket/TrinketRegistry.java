package org.chubby.github.mobtrinkets.trinket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

public final class TrinketRegistry {
    public static final String ENDER_EYE_ID = "ender_eye";

    private static final List<TrinketDefinition> ALL = List.of(
            new TrinketDefinition("bee_wing", TrinketRarity.COMMON, TrinketAbility.SWIFTNESS, EntityType.BEE),
            new TrinketDefinition("skeleton_bone_charm", TrinketRarity.COMMON, TrinketAbility.ARROW_BOOST, EntityType.SKELETON),
            new TrinketDefinition("goat_horn_charm", TrinketRarity.COMMON, TrinketAbility.KNOCKBACK_BOOST, EntityType.GOAT),
            new TrinketDefinition("drowned_pearl", TrinketRarity.COMMON, TrinketAbility.SWIM_SPEED, EntityType.DROWNED),
            new TrinketDefinition("slime_heart", TrinketRarity.UNCOMMON, TrinketAbility.HIGH_JUMP, EntityType.SLIME),
            new TrinketDefinition("phantom_wing", TrinketRarity.UNCOMMON, TrinketAbility.SLOW_FALL, EntityType.PHANTOM),
            new TrinketDefinition("guardian_eye", TrinketRarity.UNCOMMON, TrinketAbility.WATER_BREATHING, EntityType.GUARDIAN),
            new TrinketDefinition("creeper_heart", TrinketRarity.UNCOMMON, TrinketAbility.EXPLOSION_GUARD, EntityType.CREEPER),
            new TrinketDefinition("blaze_core", TrinketRarity.RARE, TrinketAbility.FIRE_IMMUNITY, EntityType.BLAZE),
            new TrinketDefinition("spider_fang", TrinketRarity.RARE, TrinketAbility.SPIDER_CLIMB, EntityType.SPIDER),
            new TrinketDefinition("magma_core", TrinketRarity.RARE, TrinketAbility.FIRE_GUARD, EntityType.MAGMA_CUBE),
            new TrinketDefinition("ender_eye", TrinketRarity.EPIC, TrinketAbility.SHORT_TELEPORT, EntityType.ENDERMAN)
    );

    private static final Map<EntityType<?>, TrinketDefinition> BY_ENTITY = new HashMap<>();
    private static final Map<String, TrinketDefinition> BY_ID = new HashMap<>();

    static {
        for (TrinketDefinition definition : ALL) {
            BY_ENTITY.put(definition.source(), definition);
            BY_ID.put(definition.id(), definition);
        }
    }

    private TrinketRegistry() {
    }

    public static List<TrinketDefinition> all() {
        return ALL;
    }

    public static TrinketDefinition forEntity(EntityType<?> type) {
        return BY_ENTITY.get(type);
    }

    public static TrinketDefinition byId(String id) {
        return BY_ID.get(id);
    }

    public static TrinketDefinition forStack(ItemStack stack) {
        return stack.getItem() instanceof TrinketItem item ? item.definition() : null;
    }
}
