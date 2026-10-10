package org.chubby.github.mobtrinkets.registry;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.blocks.entity.AssemblerBlockEntity;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {
    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.ASSEMBLER_BLOCK_ENTITY.get(),
                AssemblerBlockEntity::getItemHandler);
    }
}