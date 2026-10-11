package org.chubby.github.mobtrinkets.client;

import net.minecraft.client.gui.GuiGraphics;

public final class GuiHelper {
    private GuiHelper() {
    }

    public static void outline(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        graphics.fill(x0, y0, x1, y0 + 1, color);
        graphics.fill(x0, y1 - 1, x1, y1, color);
        graphics.fill(x0, y0, x0 + 1, y1, color);
        graphics.fill(x1 - 1, y0, x1, y1, color);
    }
}
