package org.chubby.github.mobtrinkets.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.menu.AssemblerMenu;
import org.chubby.github.mobtrinkets.menu.TrinketMenu;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MobTrinkets.MOD_ID);

    public static final Supplier<MenuType<TrinketMenu>> TRINKET = MENUS.register("trinket",
            () -> new MenuType<>(TrinketMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final Supplier<MenuType<AssemblerMenu>> ASSEMBLER  = MENUS.register("assembler", () -> IMenuTypeExtension.create(AssemblerMenu::new));

    private ModMenus() {
    }
}
