package org.chubby.github.mobtrinkets.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.block.AssemblerBlock;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MobTrinkets.MOD_ID);

    public static final Supplier<Block> ASSEMBLER = BLOCKS.register("assembler",
            () -> new AssemblerBlock(BlockBehaviour.Properties.of().strength(4.0F).requiresCorrectToolForDrops()));

    private ModBlocks() {
    }
}
