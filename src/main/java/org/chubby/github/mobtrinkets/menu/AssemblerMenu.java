package org.chubby.github.mobtrinkets.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import org.chubby.github.mobtrinkets.blocks.entity.AssemblerBlockEntity;
import org.chubby.github.mobtrinkets.registry.ModBlocks;
import org.chubby.github.mobtrinkets.registry.ModItems;
import org.chubby.github.mobtrinkets.registry.ModMenus;

public class AssemblerMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public AssemblerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, new ItemStackHandler(2), new SimpleContainerData(2), ContainerLevelAccess.NULL);
        buffer.readBlockPos();
    }

    public AssemblerMenu(int containerId, Inventory playerInventory, AssemblerBlockEntity blockEntity, ContainerLevelAccess access) {
        this(containerId, playerInventory, blockEntity.getInventory(), blockEntity.getContainerData(), access);
    }

    private AssemblerMenu(int containerId, Inventory playerInventory, IItemHandler inventory, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.ASSEMBLER.get(), containerId);
        this.access = access;
        this.data = data;
        checkContainerDataCount(data, 2);

        addSlot(new SlotItemHandler(inventory, 0, 45, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.DUSTY_FRAGMENT.get());
            }
        });
        addSlot(new SlotItemHandler(inventory, 1, 145, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 30 + column * 18, 108 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 30 + column * 18, 166));
        }

        addDataSlots(data);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return Math.max(1, data.get(1));
    }

    public int getProgressScaled(int pixels) {
        return getProgress() * pixels / getMaxProgress();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index == 1) {
            if (!moveItemStackTo(stack, 2, 38, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, result);
        } else if (index >= 2) {
            if (stack.is(ModItems.DUSTY_FRAGMENT.get())) {
                if (!moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 29) {
                if (!moveItemStackTo(stack, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 2, 29, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 2, 38, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ASSEMBLER_BLOCK.get());
    }
}
