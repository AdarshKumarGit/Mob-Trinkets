package org.chubby.github.mobtrinkets.registry;

import net.neoforged.bus.api.IEventBus;
public final class ModRegistries {
    private ModRegistries() {
    }

    public static void register(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
    }
}
