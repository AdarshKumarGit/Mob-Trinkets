package org.chubby.github.mobtrinkets.registry;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.chubby.github.mobtrinkets.MobTrinkets;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MobTrinkets.MOD_ID);

    public static final Supplier<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mobtrinkets"))
                    .icon(() -> new ItemStack(ModItems.byId("ender_eye").get()))
                    .displayItems((parameters, output) -> ModItems.all().forEach(item -> output.accept(item.get())))
                    .build());

    private ModCreativeTabs() {
    }
}
