package org.chubby.github.mobtrinkets.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.chubby.github.mobtrinkets.MobTrinkets;

@Mod(value = MobTrinkets.MOD_ID, dist = Dist.CLIENT)
public final class MobTrinketsClient {
    public MobTrinketsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}