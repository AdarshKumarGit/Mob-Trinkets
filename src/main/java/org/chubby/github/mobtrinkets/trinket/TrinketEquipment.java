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

    public static TrinketData data(Player player) {
        return player.getData(ModAttachments.TRINKETS);
    }

    public static ItemStack get(Player player, int slot) {
        return data(player).stack(slot);
    }

    public static int unlocked(Player player) {
        return data(player).unlocked();
    }

    public static boolean has(Player player, TrinketAbility ability) {
        return data(player).abilities().contains(ability);
    }

    public static boolean canEquip(Player player, int slot, ItemStack stack) {
        TrinketData data = data(player);
        if (!data.isUnlocked(slot)) {
            return false;
        }
        TrinketDefinition definition = TrinketRegistry.forStack(stack);
        if (definition == null) {
            return false;
        }
        for (int other = 0; other < TrinketData.MAX_SLOTS; other++) {
            if (other != slot && TrinketRegistry.forStack(data.stack(other)) == definition) {
                return false;
            }
        }
        return true;
    }

    public static void setSlot(Player player, int slot, ItemStack stack) {
        TrinketData data = data(player);
        if (!data.isUnlocked(slot)) {
            return;
        }
        if (!stack.isEmpty() && !canEquip(player, slot, stack)) {
            return;
        }
        ItemStack previous = data.stack(slot);
        TrinketData updated = data.withStack(slot, stack);
        store(player, updated);
        if (player instanceof ServerPlayer serverPlayer && !stack.isEmpty() && !ItemStack.matches(previous, updated.stack(slot))) {
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, EQUIP_VOLUME, EQUIP_PITCH);
        }
    }

    public static void unlockNext(Player player) {
        TrinketData data = data(player);
        if (data.unlocked() < TrinketData.MAX_SLOTS) {
            store(player, data.withUnlocked(data.unlocked() + 1));
        }
    }

    private static void store(Player player, TrinketData data) {
        player.setData(ModAttachments.TRINKETS, data);
        if (player instanceof ServerPlayer serverPlayer) {
            TrinketHandler.resync(serverPlayer);
        }
    }
}
