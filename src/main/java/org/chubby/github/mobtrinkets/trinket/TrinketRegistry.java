package org.chubby.github.mobtrinkets.trinket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

public final class TrinketRegistry {
    public static final String ENDER_EYE_ID = "ender_eye";

    private static final List<TrinketDefinition> ALL = List.of(
            new TrinketDefinition("bee_wing", TrinketRarity.COMMON, TrinketAbility.SWIFTNESS, EntityType.BEE, TrinketSkill.POLLEN_DASH, TrinketSkill.HONEY_SHIELD),
            new TrinketDefinition("skeleton_bone_charm", TrinketRarity.COMMON, TrinketAbility.ARROW_BOOST, EntityType.SKELETON, TrinketSkill.BONE_ARMOR, TrinketSkill.RATTLE),
            new TrinketDefinition("goat_horn_charm", TrinketRarity.COMMON, TrinketAbility.KNOCKBACK_BOOST, EntityType.GOAT, TrinketSkill.RAM_CHARGE, TrinketSkill.HORN_BLAST),
            new TrinketDefinition("drowned_pearl", TrinketRarity.COMMON, TrinketAbility.SWIM_SPEED, EntityType.DROWNED, TrinketSkill.TIDAL_SURGE, TrinketSkill.RIPTIDE_BURST),
            new TrinketDefinition("slime_heart", TrinketRarity.UNCOMMON, TrinketAbility.HIGH_JUMP, EntityType.SLIME, TrinketSkill.SLIME_BOUNCE, TrinketSkill.SLIME_WAVE),
            new TrinketDefinition("phantom_wing", TrinketRarity.UNCOMMON, TrinketAbility.SLOW_FALL, EntityType.PHANTOM, TrinketSkill.GLIDE, TrinketSkill.HAUNT),
            new TrinketDefinition("guardian_eye", TrinketRarity.UNCOMMON, TrinketAbility.WATER_BREATHING, EntityType.GUARDIAN, TrinketSkill.PRISMATIC_BEAM, TrinketSkill.GUARDIANS_WARD),
            new TrinketDefinition("creeper_heart", TrinketRarity.UNCOMMON, TrinketAbility.EXPLOSION_GUARD, EntityType.CREEPER, TrinketSkill.VOLATILE_BURST, TrinketSkill.CHAIN_DETONATION),
            new TrinketDefinition("blaze_core", TrinketRarity.RARE, TrinketAbility.FIRE_IMMUNITY, EntityType.BLAZE, TrinketSkill.FLAME_LASH, TrinketSkill.INFERNO_RING),
            new TrinketDefinition("spider_fang", TrinketRarity.RARE, TrinketAbility.SPIDER_CLIMB, EntityType.SPIDER, TrinketSkill.VENOM_STRIKE, TrinketSkill.SILK_SNARE),
            new TrinketDefinition("magma_core", TrinketRarity.RARE, TrinketAbility.FIRE_GUARD, EntityType.MAGMA_CUBE, TrinketSkill.MAGMA_SHIELD, TrinketSkill.ERUPTION),
            new TrinketDefinition("ender_eye", TrinketRarity.EPIC, TrinketAbility.SHORT_TELEPORT, EntityType.ENDERMAN, TrinketSkill.VOID_PULL, TrinketSkill.PHASE_SHIFT)
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
