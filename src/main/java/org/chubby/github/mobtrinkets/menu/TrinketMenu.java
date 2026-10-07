package org.chubby.github.mobtrinkets.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.registry.ModMenus;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;

public class TrinketMenu extends AbstractContainerMenu {
    public static final int REMOVE_BUTTON = 0;

    private static final int TRINKET_SLOT = 0;
    private static final int INVENTORY_START = 1;
    private static final int INVENTORY_END = 37;
    private static final int SLOT_SIZE = 18;
    private static final int TRINKET_X = 18;
    private static final int TRINKET_Y = 36;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;

    public TrinketMenu(int containerId, Inventory inventory) {
        super(ModMenus.TRINKET.get(), containerId);
        addSlot(new TrinketSlot(new TrinketContainer(inventory.player), 0, TRINKET_X, TRINKET_Y));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, INVENTORY_X + column * SLOT_SIZE, INVENTORY_Y + row * SLOT_SIZE));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, INVENTORY_X + column * SLOT_SIZE, HOTBAR_Y));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != REMOVE_BUTTON) {
            return false;
        }
        ItemStack current = TrinketEquipment.get(player);
        if (current.isEmpty()) {
            return false;
        }
        if (!player.getInventory().add(current.copy())) {
            return false;
        }
        TrinketEquipment.set(player, ItemStack.EMPTY);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == TRINKET_SLOT) {
            ItemStack moving = stack.copy();
            if (!moveItemStackTo(moving, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.setByPlayer(ItemStack.EMPTY);
            return original;
        }
        if (!moveItemStackTo(stack, TRINKET_SLOT, INVENTORY_START, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }
}
