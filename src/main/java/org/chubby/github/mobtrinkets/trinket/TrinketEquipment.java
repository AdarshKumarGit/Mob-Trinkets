package org.chubby.github.mobtrinkets.trinket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.handler.TrinketHandler;
import org.chubby.github.mobtrinkets.registry.ModAttachments;

public final class TrinketEquipment {
    private static final float EQUIP_VOLUME = 0.3F;
    private static final float EQUIP_PITCH = 1.6F;

    private TrinketEquipment() {
    }

    public static ItemStack get(Player player) {
        return player.getData(ModAttachments.EQUIPPED);
    }

    public static TrinketDefinition active(Player player) {
        return TrinketRegistry.forStack(get(player));
    }

    public static boolean has(Player player, TrinketAbility ability) {
        TrinketDefinition definition = active(player);
        return definition != null && definition.ability() == ability;
    }

    public static void set(Player player, ItemStack stack) {
        if (!stack.isEmpty() && TrinketRegistry.forStack(stack) == null) {
            return;
        }
        ItemStack previous = get(player);
        ItemStack value = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        player.setData(ModAttachments.EQUIPPED, value);
        if (player instanceof ServerPlayer serverPlayer) {
            TrinketHandler.resync(serverPlayer);
            if (!value.isEmpty() && !ItemStack.matches(previous, value)) {
                serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, EQUIP_VOLUME, EQUIP_PITCH);
            }
        }
    }
}
