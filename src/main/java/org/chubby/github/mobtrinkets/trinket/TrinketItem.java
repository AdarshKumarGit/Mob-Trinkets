package org.chubby.github.mobtrinkets.trinket;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class TrinketItem extends Item {
    private final TrinketDefinition definition;

    public TrinketItem(TrinketDefinition definition, Properties properties) {
        super(properties);
        this.definition = definition;
    }

    public TrinketDefinition definition() {
        return definition;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(definition.ability().description().withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.mobtrinkets.dropped_by", definition.source().getDescription()).withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.mobtrinkets.mob_trinket").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(definition.rarity().displayName());
    }
}
