package org.chubby.github.mobtrinkets.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;

public class TrinketSlot extends Slot {
    private final Player player;
    private final int trinketIndex;

    public TrinketSlot(TrinketContainer container, Player player, int index, int x, int y) {
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
