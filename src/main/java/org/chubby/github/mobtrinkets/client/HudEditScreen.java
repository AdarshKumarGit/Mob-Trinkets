package org.chubby.github.mobtrinkets.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.chubby.github.mobtrinkets.config.HudConfig;

public class HudEditScreen extends Screen {
    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int FIRST_ROW_OFFSET = 52;
    private static final int SECOND_ROW_OFFSET = 28;
    private static final double SCALE_STEP = 0.25;
    private static final int TITLE_Y = 20;
    private static final int SCALE_Y = 32;
    private static final int HIGHLIGHT = 0xFFE8B040;
    private static final int FRAME_PADDING = 2;

    private final Screen parent;
    private boolean dragging;
    private double grabX;
    private double grabY;

    public HudEditScreen(Screen parent) {
        super(Component.translatable("gui.mobtrinkets.hud_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = width / 2 - (3 * BUTTON_WIDTH + 2 * BUTTON_GAP) / 2;
        int first = height - FIRST_ROW_OFFSET;
        int second = height - SECOND_ROW_OFFSET;
        addRenderableWidget(Button.builder(toggleLabel(), button -> {
            HudConfig.ENABLED.set(!HudConfig.ENABLED.get());
            button.setMessage(toggleLabel());
        }).bounds(left, first, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(layoutLabel(), button -> {
            HudConfig.VERTICAL.set(!HudConfig.VERTICAL.get());
            button.setMessage(layoutLabel());
        }).bounds(left + BUTTON_WIDTH + BUTTON_GAP, first, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mobtrinkets.hud_reset"), button -> HudConfig.resetPosition())
                .bounds(left + 2 * (BUTTON_WIDTH + BUTTON_GAP), first, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mobtrinkets.hud_smaller"), button -> changeScale(-SCALE_STEP))
                .bounds(left, second, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mobtrinkets.hud_larger"), button -> changeScale(SCALE_STEP))
                .bounds(left + BUTTON_WIDTH + BUTTON_GAP, second, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.mobtrinkets.hud_done"), button -> onClose())
                .bounds(left + 2 * (BUTTON_WIDTH + BUTTON_GAP), second, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private static Component toggleLabel() {
        Component state = Component.translatable(HudConfig.ENABLED.get() ? "gui.mobtrinkets.hud_on" : "gui.mobtrinkets.hud_off");
        return Component.translatable("gui.mobtrinkets.hud_toggle", state);
    }

    private static Component layoutLabel() {
        Component state = Component.translatable(HudConfig.VERTICAL.get() ? "gui.mobtrinkets.hud_vertical" : "gui.mobtrinkets.hud_horizontal");
        return Component.translatable("gui.mobtrinkets.hud_layout", state);
    }

    private static void changeScale(double delta) {
        double value = Math.max(HudConfig.MIN_SCALE, Math.min(HudConfig.MAX_SCALE, HudConfig.SCALE.get() + delta));
        HudConfig.SCALE.set(value);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, TITLE_Y, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("gui.mobtrinkets.hud_scale", String.format("%.2f", HudConfig.SCALE.get())), width / 2, SCALE_Y, 0xFFB8B8C8);
        int x = TrinketHud.originX(width);
        int y = TrinketHud.originY(height);
        TrinketHud.draw(graphics, minecraft, x, y, true);
        boolean hovered = mouseX >= x && mouseX < x + TrinketHud.width() && mouseY >= y && mouseY < y + TrinketHud.height();
        if (hovered || dragging) {
            int x0 = x - FRAME_PADDING;
            int y0 = y - FRAME_PADDING;
            int x1 = x + TrinketHud.width() + FRAME_PADDING;
            int y1 = y + TrinketHud.height() + FRAME_PADDING;
            graphics.fill(x0, y0, x1, y0 + 1, HIGHLIGHT);
            graphics.fill(x0, y1 - 1, x1, y1, HIGHLIGHT);
            graphics.fill(x0, y0, x0 + 1, y1, HIGHLIGHT);
            graphics.fill(x1 - 1, y0, x1, y1, HIGHLIGHT);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        int x = TrinketHud.originX(width);
        int y = TrinketHud.originY(height);
        if (button == 0 && mouseX >= x && mouseX < x + TrinketHud.width() && mouseY >= y && mouseY < y + TrinketHud.height()) {
            dragging = true;
            grabX = mouseX - x;
            grabY = mouseY - y;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        int rangeX = width - TrinketHud.width();
        int rangeY = height - TrinketHud.height();
        if (rangeX > 0) {
            HudConfig.X.set(Math.max(0.0, Math.min(1.0, (mouseX - grabX) / rangeX)));
        }
        if (rangeY > 0) {
            HudConfig.Y.set(Math.max(0.0, Math.min(1.0, (mouseY - grabY) / rangeY)));
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            HudConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        HudConfig.save();
        minecraft.setScreen(parent);
    }
}
