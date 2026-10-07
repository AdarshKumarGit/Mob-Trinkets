package org.chubby.github.mobtrinkets.trinket;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.chubby.github.mobtrinkets.config.TrinketConfig;

public class TrinketSigilItem extends Item {
    public TrinketSigilItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Component failure = TrinketUnlocks.failure(player);
        if (failure != null) {
            if (!level.isClientSide()) {
                player.displayClientMessage(failure, true);
            }
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            TrinketUnlocks.unlock(serverPlayer, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.mobtrinkets.sigil").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.mobtrinkets.sigil_cost",
                TrinketConfig.unlockLevels(0), TrinketConfig.unlockLevels(1), TrinketConfig.unlockLevels(2)).withStyle(ChatFormatting.DARK_GRAY));
    }
}
