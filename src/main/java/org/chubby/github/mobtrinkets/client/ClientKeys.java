package org.chubby.github.mobtrinkets.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class ClientKeys {
    public static final String CATEGORY = "key.categories.mobtrinkets";

    public static final KeyMapping[] SLOT_KEYS = {
            new KeyMapping("key.mobtrinkets.skill_slot_1", InputConstants.KEY_R, CATEGORY),
            new KeyMapping("key.mobtrinkets.skill_slot_2", InputConstants.KEY_G, CATEGORY),
            new KeyMapping("key.mobtrinkets.skill_slot_3", InputConstants.KEY_V, CATEGORY)
    };

    private ClientKeys() {
    }
}
