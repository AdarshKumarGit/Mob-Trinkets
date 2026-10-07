package org.chubby.github.mobtrinkets.trinket;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class TrinketData {
    public static final int MAX_SLOTS = 3;

    public static final Codec<TrinketData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("unlocked").forGetter(TrinketData::unlocked),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("stacks").forGetter(TrinketData::stacks)
    ).apply(instance, TrinketData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TrinketData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TrinketData::unlocked,
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, TrinketData::stacks,
            TrinketData::new);

    public static final TrinketData EMPTY = new TrinketData(0, List.of());

    private final int unlocked;
    private final List<ItemStack> stacks;
    private final Set<TrinketAbility> abilities;

    public TrinketData(int unlocked, List<ItemStack> stacks) {
        this.unlocked = Mth.clamp(unlocked, 0, MAX_SLOTS);
        List<ItemStack> normalized = new ArrayList<>(MAX_SLOTS);
        Set<TrinketAbility> active = EnumSet.noneOf(TrinketAbility.class);
        for (int slot = 0; slot < MAX_SLOTS; slot++) {
            ItemStack stack = slot < this.unlocked && slot < stacks.size() ? stacks.get(slot) : ItemStack.EMPTY;
            TrinketDefinition definition = TrinketRegistry.forStack(stack);
            if (definition == null) {
                stack = ItemStack.EMPTY;
            } else {
                active.add(definition.ability());
            }
            normalized.add(stack);
        }
        this.stacks = Collections.unmodifiableList(normalized);
        this.abilities = Collections.unmodifiableSet(active);
    }

    public int unlocked() {
        return unlocked;
    }

    public List<ItemStack> stacks() {
        return stacks;
    }

    public Set<TrinketAbility> abilities() {
        return abilities;
    }

    public ItemStack stack(int slot) {
        return slot >= 0 && slot < MAX_SLOTS ? stacks.get(slot) : ItemStack.EMPTY;
    }

    public boolean isUnlocked(int slot) {
        return slot >= 0 && slot < unlocked;
    }

    public TrinketData withStack(int slot, ItemStack stack) {
        List<ItemStack> updated = new ArrayList<>(stacks);
        updated.set(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        return new TrinketData(unlocked, updated);
    }

    public TrinketData withUnlocked(int count) {
        return new TrinketData(count, stacks);
    }
}
