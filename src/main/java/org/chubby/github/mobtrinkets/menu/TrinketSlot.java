package org.chubby.github.mobtrinkets.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.trinket.TrinketItem;

public class TrinketSlot extends Slot {
    public TrinketSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof TrinketItem;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
