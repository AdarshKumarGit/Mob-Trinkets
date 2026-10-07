package org.chubby.github.mobtrinkets.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;

public class TrinketContainer implements Container {
    private final Player player;

    public TrinketContainer(Player player) {
        this.player = player;
    }

    @Override
    public int getContainerSize() {
        return TrinketData.MAX_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < TrinketData.MAX_SLOTS; slot++) {
            if (!getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return TrinketEquipment.get(player, slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return take(slot);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return take(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        TrinketEquipment.setSlot(player, slot, stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player viewer) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < TrinketData.MAX_SLOTS; slot++) {
            TrinketEquipment.setSlot(player, slot, ItemStack.EMPTY);
        }
    }

    private ItemStack take(int slot) {
        ItemStack current = getItem(slot);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = current.copy();
        TrinketEquipment.setSlot(player, slot, ItemStack.EMPTY);
        return removed;
    }
}
