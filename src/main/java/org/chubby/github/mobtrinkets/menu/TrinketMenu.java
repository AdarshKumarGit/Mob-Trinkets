package org.chubby.github.mobtrinkets.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.registry.ModMenus;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;

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

    /** A slot showing one equipped trinket; it only accepts trinkets that {@link TrinketEquipment} allows. */
    public static class TrinketSlot extends Slot {
        private final Player player;
        private final int trinketIndex;

        private TrinketSlot(Container container, Player player, int index, int x, int y) {
            super(container, index, x, y);
            this.player = player;
            this.trinketIndex = index;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return TrinketEquipment.canEquip(player, trinketIndex, stack);
        }

        public boolean isUnlocked() {
            return TrinketEquipment.data(player).isUnlocked(trinketIndex);
        }

        public int trinketIndex() {
            return trinketIndex;
        }

        @Override
        public boolean mayPickup(Player picker) {
            return isUnlocked();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /** Exposes the player's trinket data as a vanilla container so the menu's slots can read and write it. */
    private static final class TrinketContainer implements Container {
        private final Player player;

        private TrinketContainer(Player player) {
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
}
