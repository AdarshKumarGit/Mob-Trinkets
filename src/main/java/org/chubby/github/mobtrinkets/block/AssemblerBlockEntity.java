package org.chubby.github.mobtrinkets.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.chubby.github.mobtrinkets.menu.AssemblerMenu;
import org.chubby.github.mobtrinkets.registry.ModBlockEntities;
import org.chubby.github.mobtrinkets.registry.ModItems;

public class AssemblerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int PROCESS_TIME = 100;

    private int progress;

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT && stack.is(ModItems.DUSTY_FRAGMENT.get());
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : PROCESS_TIME;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private final IItemHandler inputHandler = new SidedHandler(true);
    private final IItemHandler outputHandler = new SidedHandler(false);

    public AssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLER.get(), pos, state);
    }

    /** Item handler exposed to hoppers and pipes: inputs from every side but the bottom, outputs from the bottom. */
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        return side == Direction.DOWN ? outputHandler : inputHandler;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getContainerData() {
        return data;
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(slot));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobtrinkets.assembler");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AssemblerMenu(containerId, playerInventory, this, ContainerLevelAccess.create(level, worldPosition));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AssemblerBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        ItemStack input = blockEntity.inventory.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty() || !blockEntity.inventory.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            if (blockEntity.progress != 0) {
                blockEntity.progress = 0;
                blockEntity.setChanged();
            }
            return;
        }

        blockEntity.progress++;
        if (blockEntity.progress >= PROCESS_TIME) {
            blockEntity.progress = 0;
            blockEntity.finishCraft(level, pos, input);
        }
        blockEntity.setChanged();
    }

    private void finishCraft(Level level, BlockPos pos, ItemStack input) {
        ItemStack remaining = input.copy();
        remaining.shrink(1);
        inventory.setStackInSlot(INPUT_SLOT, remaining);
        inventory.setStackInSlot(OUTPUT_SLOT, AssemblerRewards.roll(level.random));

        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 0.75F + level.random.nextFloat() * 0.5F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 8, 0.25, 0.25, 0.25, 0.01);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }

    /** Restricted view of the inventory: either insert-only into the input slot or extract-only from the output slot. */
    private final class SidedHandler implements IItemHandler {
        private final boolean insertOnly;

        private SidedHandler(boolean insertOnly) {
            this.insertOnly = insertOnly;
        }

        @Override
        public int getSlots() {
            return inventory.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!insertOnly || slot != INPUT_SLOT) {
                return stack;
            }
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (insertOnly || slot != OUTPUT_SLOT) {
                return ItemStack.EMPTY;
            }
            return inventory.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return insertOnly && slot == INPUT_SLOT && inventory.isItemValid(slot, stack);
        }
    }
}
