package org.chubby.github.mobtrinkets;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.chubby.github.mobtrinkets.config.HudConfig;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.registry.ModRegistries;

@Mod(MobTrinkets.MOD_ID)
public final class MobTrinkets {
    public static final String MOD_ID = "mobtrinkets";

    public MobTrinkets(IEventBus modEventBus, ModContainer container) {
        ModRegistries.register(modEventBus);
        container.registerConfig(ModConfig.Type.COMMON, TrinketConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, HudConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
