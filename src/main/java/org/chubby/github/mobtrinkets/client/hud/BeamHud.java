package org.chubby.github.mobtrinkets.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.chubby.github.mobtrinkets.client.BeamClientState;
import org.chubby.github.mobtrinkets.network.ModPayloads.BeamStatePayload;

public final class BeamHud {
    private static final int BAR_WIDTH = 62;
    private static final int BAR_HEIGHT = 6;
    private static final int OFFSET_Y = 16;
    private static final int LABEL_GAP = 10;
    private static final int BACKGROUND = 0xFF14141C;
    private static final int CHARGE_FILL = 0xFF40E0D0;
    private static final int CHARGE_SHINE = 0xFFB8FFF4;
    private static final int FIRE_FILL = 0xFFB070E8;
    private static final int FIRE_SHINE = 0xFFE0C0FF;
    private static final int BORDER_A = 0xFFE8B040;
    private static final int BORDER_B = 0xFFFFE9B0;
    private static final long PULSE_MS = 150L;
    private static final float READY_THRESHOLD = 0.95F;

    private BeamHud() {
    }

    public static void renderLayer(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!BeamClientState.active() || minecraft.options.hideGui || minecraft.player == null) {
            return;
        }
        boolean firing = BeamClientState.state() == BeamStatePayload.FIRING;
        float progress = BeamClientState.progress();
        float fill = firing ? 1.0F - progress : progress;
        int x = graphics.guiWidth() / 2 - BAR_WIDTH / 2;
        int y = graphics.guiHeight() / 2 + OFFSET_Y;

        boolean flash = firing || progress > READY_THRESHOLD;
        int border = flash && (System.currentTimeMillis() / PULSE_MS) % 2L == 0L ? BORDER_B : BORDER_A;
        graphics.fill(x - 2, y - 2, x + BAR_WIDTH + 2, y + BAR_HEIGHT + 2, border);
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xFF000000);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BACKGROUND);

        int width = Math.round(BAR_WIDTH * fill);
        if (width > 0) {
            graphics.fill(x, y, x + width, y + BAR_HEIGHT, firing ? FIRE_FILL : CHARGE_FILL);
            graphics.fill(x, y, x + width, y + 2, firing ? FIRE_SHINE : CHARGE_SHINE);
        }
        for (int i = 1; i < 4; i++) {
            int tick = x + BAR_WIDTH * i / 4;
            graphics.fill(tick, y, tick + 1, y + BAR_HEIGHT, 0x80000000);
        }

        Component label = Component.translatable(firing ? "gui.mobtrinkets.beam_firing" : "gui.mobtrinkets.beam_charging");
        graphics.drawCenteredString(minecraft.font, label, graphics.guiWidth() / 2, y - LABEL_GAP, firing ? FIRE_SHINE : CHARGE_SHINE);
    }
}
