package org.chubby.github.mobtrinkets.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.block.AssemblerBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MobTrinkets.MOD_ID);

    public static final Supplier<BlockEntityType<AssemblerBlockEntity>> ASSEMBLER = BLOCK_ENTITIES.register("assembler",
            () -> BlockEntityType.Builder.of(AssemblerBlockEntity::new, ModBlocks.ASSEMBLER.get()).build(null));

    private ModBlockEntities() {
    }
}
