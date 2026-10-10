package org.chubby.github.mobtrinkets.blocks.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.Containers;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.server.level.ServerLevel;

import org.chubby.github.mobtrinkets.menu.AssemblerMenu;
import org.chubby.github.mobtrinkets.registry.ModBlockEntities;
import org.chubby.github.mobtrinkets.registry.ModItems;
import org.chubby.github.mobtrinkets.trinket.TrinketItem;
import net.neoforged.neoforge.registries.DeferredItem;

public class AssemblerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int PROCESS_TIME = 100;

    private int progress;

    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && stack.is(ModItems.DUSTY_FRAGMENT.get());
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

    public AssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ASSEMBLER_BLOCK_ENTITY.get(), pos, state);
    }

    private final IItemHandler inputHandler = new SidedHandler(true);
    private final IItemHandler outputHandler = new SidedHandler(false);

    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        return side == Direction.DOWN ? outputHandler : inputHandler;
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(slot));
        }
    }

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
            if (!insertOnly || slot != 0) {
                return stack;
            }
            return inventory.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (insertOnly || slot != 1) {
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
            return insertOnly && slot == 0 && inventory.isItemValid(slot, stack);
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public ContainerData getContainerData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mobtrinkets.assembler");
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory playerInventory,
            Player player
    ) {
        return new AssemblerMenu(
                containerId,
                playerInventory,
                this,
                ContainerLevelAccess.create(level, worldPosition)
        );
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            AssemblerBlockEntity blockEntity
    ) {
        if (level.isClientSide) {
            return;
        }

        ItemStack input = blockEntity.inventory.getStackInSlot(0);

        if (input.isEmpty() || !blockEntity.inventory.getStackInSlot(1).isEmpty()) {
            if (blockEntity.progress != 0) {
                blockEntity.progress = 0;
                blockEntity.setChanged();
            }
            return;
        }

        blockEntity.progress++;

        if (blockEntity.progress >= PROCESS_TIME) {
            blockEntity.progress = 0;

            ItemStack remaining = input.copy();
            remaining.shrink(1);

            blockEntity.inventory.setStackInSlot(0, remaining);
            blockEntity.inventory.setStackInSlot(1, rollReward(level.random));

            level.playSound(
                    null,
                    pos,
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS,
                    0.8F,
                    0.75F + level.random.nextFloat() * 0.5F
            );

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,
                        pos.getX() + 0.5,
                        pos.getY() + 1.0,
                        pos.getZ() + 0.5,
                        8,
                        0.25,
                        0.25,
                        0.25,
                        0.01
                );
            }
        }

        blockEntity.setChanged();
    }

    private static ItemStack rollReward(RandomSource random) {
        int roll = random.nextInt(10_000);
        Item reward;
        int count;

        if (roll < 5_000) {
            Item[] rewards = {
                    Items.FLINT,
                    Items.COAL,
                    Items.COBBLESTONE,
                    Items.GRAVEL,
                    Items.ANDESITE,
                    Items.DIORITE
            };
            reward = rewards[random.nextInt(rewards.length)];
            count = 1 + random.nextInt(4);
        } else if (roll < 8_000) {
            Item[] rewards = {
                    Items.RAW_IRON,
                    Items.RAW_COPPER,
                    Items.REDSTONE,
                    Items.LAPIS_LAZULI,
                    Items.QUARTZ
            };
            reward = rewards[random.nextInt(rewards.length)];
            count = 1 + random.nextInt(3);
        } else if (roll < 9_500) {
            Item[] rewards = {
                    Items.DIAMOND,
                    Items.EMERALD,
                    Items.RAW_GOLD,
                    Items.AMETHYST_SHARD
            };
            reward = rewards[random.nextInt(rewards.length)];
            count = 1 + random.nextInt(2);
        } else if (roll < 9_900) {
            Item[] rewards = {
                    Items.ECHO_SHARD,
                    Items.ANCIENT_DEBRIS
            };
            reward = rewards[random.nextInt(rewards.length)];
            count = 1;
        } else {
            List<Item> trinkets = findTrinkets();

            if (trinkets.isEmpty()) {
                Item[] fallbackRewards = {
                        Items.DIAMOND,
                        Items.EMERALD,
                        Items.ECHO_SHARD
                };
                reward = fallbackRewards[random.nextInt(fallbackRewards.length)];
            } else {
                reward = trinkets.get(random.nextInt(trinkets.size()));
            }

            count = 1;
        }

        return new ItemStack(reward, count);
    }

    private static List<Item> findTrinkets() {
        List<Item> trinkets = new ArrayList<>();

        for (DeferredItem<TrinketItem> deferredItem : ModItems.all()) {
            Item item = deferredItem.get();

            if (BuiltInRegistries.ITEM.getKey(item) != null) {
                trinkets.add(item);
            }
        }

        return trinkets;
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }
}