package org.chubby.github.mobtrinkets.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.registry.ModMenus;
import org.chubby.github.mobtrinkets.trinket.TrinketData;

public class TrinketMenu extends AbstractContainerMenu {
    public static final int SLOT_X = 9;
    public static final int SLOT_START_Y = 18;
    public static final int SLOT_SPACING = 21;

    private static final int INVENTORY_START = TrinketData.MAX_SLOTS;
    private static final int INVENTORY_END = INVENTORY_START + 36;
    private static final int SLOT_SIZE = 18;
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 84;
    private static final int HOTBAR_Y = 142;

    public TrinketMenu(int containerId, Inventory inventory) {
        super(ModMenus.TRINKET.get(), containerId);
        TrinketContainer container = new TrinketContainer(inventory.player);
        for (int slot = 0; slot < TrinketData.MAX_SLOTS; slot++) {
            addSlot(new TrinketSlot(container, inventory.player, slot, SLOT_X, slotY(slot)));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, INVENTORY_X + column * SLOT_SIZE, INVENTORY_Y + row * SLOT_SIZE));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, INVENTORY_X + column * SLOT_SIZE, HOTBAR_Y));
        }
    }

    public static int slotY(int slot) {
        return SLOT_START_Y + slot * SLOT_SPACING;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < INVENTORY_START) {
            ItemStack moving = stack.copy();
            if (!moveItemStackTo(moving, INVENTORY_START, INVENTORY_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.setByPlayer(ItemStack.EMPTY);
            return original;
        }
        if (!moveItemStackTo(stack, 0, INVENTORY_START, false)) {
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
