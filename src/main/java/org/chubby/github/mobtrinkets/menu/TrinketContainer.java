package org.chubby.github.mobtrinkets.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;

public class TrinketContainer implements Container {
    private final Player player;

    public TrinketContainer(Player player) {
        this.player = player;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return TrinketEquipment.get(player).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return TrinketEquipment.get(player);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return take();
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return take();
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        TrinketEquipment.set(player, stack);
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
        TrinketEquipment.set(player, ItemStack.EMPTY);
    }

    private ItemStack take() {
        ItemStack current = TrinketEquipment.get(player);
        if (current.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = current.copy();
        TrinketEquipment.set(player, ItemStack.EMPTY);
        return removed;
    }
}
