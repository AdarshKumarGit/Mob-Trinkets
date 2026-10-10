package org.chubby.github.mobtrinkets.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.blocks.entity.AssemblerBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MobTrinkets.MOD_ID);

    public static final Supplier<BlockEntityType<AssemblerBlockEntity>> ASSEMBLER_BLOCK_ENTITY = BLOCK_ENTITIES.register("assembler", () -> BlockEntityType.Builder.of(AssemblerBlockEntity::new, ModBlocks.ASSEMBLER_BLOCK.get()).build(null));

}
