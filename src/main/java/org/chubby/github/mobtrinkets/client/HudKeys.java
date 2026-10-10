package org.chubby.github.mobtrinkets.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class HudKeys {
    public static final KeyMapping EDIT = new KeyMapping("key.mobtrinkets.hud_edit", InputConstants.KEY_K, ClientKeys.CATEGORY);

    private HudKeys() {
    }
}
