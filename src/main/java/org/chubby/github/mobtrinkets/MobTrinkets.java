package org.chubby.github.mobtrinkets;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.chubby.github.mobtrinkets.command.SetTrinketMasteryCommand;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.registry.*;

@Mod(MobTrinkets.MOD_ID)
public final class MobTrinkets {
    public static final String MOD_ID = "mobtrinkets";

    public MobTrinkets(IEventBus modEventBus, ModContainer container) {
        ModItems.ITEMS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        container.registerConfig(ModConfig.Type.COMMON, TrinketConfig.SPEC);
    }
    public  void registerCommands(RegisterCommandsEvent event)
    {
        SetTrinketMasteryCommand.registerCommands(event);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
