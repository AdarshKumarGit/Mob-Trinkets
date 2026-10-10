package org.chubby.github.mobtrinkets.handler;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.registry.ModItems;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class FragmentDropHandler {
    private static final double STONE_FRAGMENT_CHANCE = 0.05D;

    private FragmentDropHandler() {
    }

    @SubscribeEvent
    public static void onStoneBroken(BlockEvent.BreakEvent event) {
        if (event.getPlayer().level().isClientSide || event.getPlayer().getAbilities().instabuild) {
            return;
        }

        if (!event.getState().is(BlockTags.BASE_STONE_OVERWORLD)) {
            return;
        }

        if (event.getPlayer().getRandom().nextDouble() < STONE_FRAGMENT_CHANCE) {
            BlockPos pos = event.getPos();
            Block.popResource(event.getPlayer().level(), pos, new ItemStack(ModItems.DUSTY_FRAGMENT.get()));
        }
    }
}
