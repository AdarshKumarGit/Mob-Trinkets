package org.chubby.github.mobtrinkets;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.registry.ModAttachments;
import org.chubby.github.mobtrinkets.registry.ModCreativeTabs;
import org.chubby.github.mobtrinkets.registry.ModItems;
import org.chubby.github.mobtrinkets.registry.ModMenus;

@Mod(MobTrinkets.MOD_ID)
public final class MobTrinkets {
    public static final String MOD_ID = "mobtrinkets";

    public MobTrinkets(IEventBus modEventBus, ModContainer container) {
        ModItems.ITEMS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        container.registerConfig(ModConfig.Type.COMMON, TrinketConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
