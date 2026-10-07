package org.chubby.github.mobtrinkets.trinket;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Rarity;

public enum TrinketRarity {
    COMMON("common", Rarity.COMMON, ChatFormatting.WHITE),
    UNCOMMON("uncommon", Rarity.UNCOMMON, ChatFormatting.YELLOW),
    RARE("rare", Rarity.RARE, ChatFormatting.AQUA),
    EPIC("epic", Rarity.EPIC, ChatFormatting.LIGHT_PURPLE);

    private final String key;
    private final Rarity vanilla;
    private final ChatFormatting formatting;

    TrinketRarity(String key, Rarity vanilla, ChatFormatting formatting) {
        this.key = key;
        this.vanilla = vanilla;
        this.formatting = formatting;
    }

    public Rarity vanilla() {
        return vanilla;
    }

    public ChatFormatting formatting() {
        return formatting;
    }

    public MutableComponent displayName() {
        return Component.translatable("rarity.mobtrinkets." + key).withStyle(formatting);
    }
}
