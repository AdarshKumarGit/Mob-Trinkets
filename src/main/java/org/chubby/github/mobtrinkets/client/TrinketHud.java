package org.chubby.github.mobtrinkets.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.config.HudConfig;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;

public final class TrinketHud {
    private static final int CELL = 20;
    private static final int GAP = 2;
    private static final int ITEM_OFFSET = 2;
    private static final int BACKGROUND = 0xB0101018;
    private static final int LOCKED_BACKGROUND = 0x50101018;
    private static final int EMPTY_BORDER = 0xFF3A3A46;
    private static final int LOCKED_BORDER = 0x803A3A46;
    private static final int PIP_OFF = 0xFF2A2A34;
    private static final int PIP_WIDTH = 4;
    private static final int PIP_HEIGHT = 2;
    private static final int PIP_GAP = 1;
    private static final int[] TIER_COLORS = {0xFF8A8A9A, 0xFFE8B040, 0xFFB070E8};

    private TrinketHud() {
    }

    public static int footprint() {
        return TrinketData.MAX_SLOTS * CELL + (TrinketData.MAX_SLOTS - 1) * GAP;
    }

    public static int width() {
        return Math.round((HudConfig.VERTICAL.get() ? CELL : footprint()) * scale());
    }

    public static int height() {
        return Math.round((HudConfig.VERTICAL.get() ? footprint() : CELL) * scale());
    }

    public static int originX(int screenWidth) {
        return (int) Math.round(HudConfig.X.get() * Math.max(0, screenWidth - width()));
    }

    public static int originY(int screenHeight) {
        return (int) Math.round(HudConfig.Y.get() * Math.max(0, screenHeight - height()));
    }

    public static void renderLayer(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !HudConfig.ENABLED.get()) {
            return;
        }
        if (minecraft.screen instanceof HudEditScreen || TrinketEquipment.unlocked(minecraft.player) == 0) {
            return;
        }
        draw(graphics, minecraft, originX(graphics.guiWidth()), originY(graphics.guiHeight()), false);
    }

    public static void draw(GuiGraphics graphics, Minecraft minecraft, int x, int y, boolean preview) {
        TrinketData data = TrinketEquipment.data(minecraft.player);
        boolean vertical = HudConfig.VERTICAL.get();
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale(), scale(), 1.0F);
        for (int slot = 0; slot < TrinketData.MAX_SLOTS; slot++) {
            boolean unlocked = data.isUnlocked(slot);
            if (!unlocked && !preview) {
                continue;
            }
            int offset = slot * (CELL + GAP);
            drawCell(graphics, minecraft, vertical ? 0 : offset, vertical ? offset : 0, data.stack(slot), unlocked);
        }
        graphics.pose().popPose();
    }

    private static void drawCell(GuiGraphics graphics, Minecraft minecraft, int x, int y, ItemStack stack, boolean unlocked) {
        TrinketDefinition definition = TrinketRegistry.forStack(stack);
        int tier = definition == null ? 0 : TrinketMastery.tier(minecraft.player, definition);
        int border = !unlocked ? LOCKED_BORDER : definition == null ? EMPTY_BORDER : 0xFF000000 | (TIER_COLORS[tier - 1] & 0xFFFFFF);
        graphics.fill(x, y, x + CELL, y + CELL, unlocked ? BACKGROUND : LOCKED_BACKGROUND);
        outline(graphics, x, y, x + CELL, y + CELL, border);
        if (definition == null) {
            return;
        }
        graphics.renderItem(stack, x + ITEM_OFFSET, y + ITEM_OFFSET);
        int pipsWidth = TrinketMastery.MAX_TIER * PIP_WIDTH + (TrinketMastery.MAX_TIER - 1) * PIP_GAP;
        int pipX = x + (CELL - pipsWidth) / 2;
        int pipY = y + CELL - PIP_HEIGHT - 1;
        for (int i = 0; i < TrinketMastery.MAX_TIER; i++) {
            int color = i < tier ? 0xFF000000 | (TIER_COLORS[tier - 1] & 0xFFFFFF) : PIP_OFF;
            graphics.fill(pipX + i * (PIP_WIDTH + PIP_GAP), pipY, pipX + i * (PIP_WIDTH + PIP_GAP) + PIP_WIDTH, pipY + PIP_HEIGHT, color);
        }
    }

    private static float scale() {
        return HudConfig.SCALE.get().floatValue();
    }

    private static void outline(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        graphics.fill(x0, y0, x1, y0 + 1, color);
        graphics.fill(x0, y1 - 1, x1, y1, color);
        graphics.fill(x0, y0, x0 + 1, y1, color);
        graphics.fill(x1 - 1, y0, x1, y1, color);
    }
}
