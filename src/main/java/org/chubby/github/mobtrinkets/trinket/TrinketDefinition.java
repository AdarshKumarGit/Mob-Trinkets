package org.chubby.github.mobtrinkets.trinket;

import net.minecraft.world.entity.EntityType;

public record TrinketDefinition(String id, TrinketRarity rarity, TrinketAbility ability, EntityType<?> source) {
}
