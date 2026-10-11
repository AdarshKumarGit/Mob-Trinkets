package org.chubby.github.mobtrinkets.block;

import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import org.chubby.github.mobtrinkets.item.TrinketItem;
import org.chubby.github.mobtrinkets.registry.ModItems;

final class AssemblerRewards {
    private static final int ROLL_RANGE = 10_000;

    private record Tier(int below, List<Item> items, int maxCount) {
    }

    private static final List<Tier> TIERS = List.of(
            new Tier(5_000, List.of(Items.FLINT, Items.COAL, Items.COBBLESTONE, Items.GRAVEL, Items.ANDESITE, Items.DIORITE), 4),
            new Tier(8_000, List.of(Items.RAW_IRON, Items.RAW_COPPER, Items.REDSTONE, Items.LAPIS_LAZULI, Items.QUARTZ), 3),
            new Tier(9_500, List.of(Items.DIAMOND, Items.EMERALD, Items.RAW_GOLD, Items.AMETHYST_SHARD), 2),
            new Tier(9_900, List.of(Items.ECHO_SHARD, Items.ANCIENT_DEBRIS), 1)
    );

    private AssemblerRewards() {
    }

    static ItemStack roll(RandomSource random) {
        int roll = random.nextInt(ROLL_RANGE);
        for (Tier tier : TIERS) {
            if (roll < tier.below()) {
                Item item = tier.items().get(random.nextInt(tier.items().size()));
                int count = tier.maxCount() == 1 ? 1 : 1 + random.nextInt(tier.maxCount());
                return new ItemStack(item, count);
            }
        }
        List<DeferredItem<TrinketItem>> trinkets = List.copyOf(ModItems.all());
        return new ItemStack(trinkets.get(random.nextInt(trinkets.size())).get());
    }
}
