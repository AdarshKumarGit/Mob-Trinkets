package org.chubby.github.mobtrinkets.registry;


import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.blocks.custom.AssemblerBlock;

import java.util.function.Supplier;

public class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MobTrinkets.MOD_ID);

    public static final Supplier<Block> ASSEMBLER_BLOCK = BLOCKS.register("assembler", () -> new AssemblerBlock(BlockBehaviour.Properties.of().strength(4.0F).requiresCorrectToolForDrops()));

}
