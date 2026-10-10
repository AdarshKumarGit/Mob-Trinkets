package org.chubby.github.mobtrinkets.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.menu.AssemblerMenu;
import org.chubby.github.mobtrinkets.registry.ModItems;
import org.chubby.github.mobtrinkets.trinket.TrinketItem;

public class AssemblerScreen extends AbstractContainerScreen<AssemblerMenu> {
    private static final ResourceLocation TEXTURE = MobTrinkets.id("textures/gui/assembler.png");

    private static final int ARROW_X = 83;
    private static final int ARROW_Y = 39;
    private static final int ARROW_WIDTH = 40;
    private static final int ARROW_HEIGHT = 10;
    private static final int ARROW_U = 0;
    private static final int ARROW_V = 192;

    private static final int GLOW_X = 139;
    private static final int GLOW_Y = 30;
    private static final int GLOW_SIZE = 28;
    private static final int GLOW_U_FIRST = 50;
    private static final int GLOW_U_SECOND = 80;
    private static final int GLOW_V = 192;
    private static final long GLOW_PERIOD_MS = 350L;

    private static final int INPUT_CENTER_X = 53;
    private static final int OUTPUT_CENTER_X = 153;
    private static final int MACHINE_CENTER_X = 103;
    private static final int LABEL_Y = 22;
    private static final int CAPTION_Y = 59;
    private static final int PERCENT_Y = 50;
    private static final int INVENTORY_LABEL_X = 30;

    private static final int STRIP_X = 9;
    private static final int STRIP_WIDTH = 202;
    private static final int ICON_SIZE = 12;
    private static final int ICON_SPACING = 16;
    private static final int ICON_Y = 73;
    private static final float ICON_SCALE = 0.75F;
    private static final float SMALL_TEXT = 0.75F;

    private static final int TEXT_DARK = 0xFF404040;
    private static final int TEXT_PURPLE = 0xFF6B3FC0;
    private static final int TEXT_LIGHT = 0xFFB8BDCC;

    public AssemblerScreen(AssemblerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 220;
        this.imageHeight = 190;
        this.inventoryLabelY = 96;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        int progress = menu.getProgressScaled(ARROW_WIDTH);
        if (progress > 0) {
            graphics.blit(TEXTURE, leftPos + ARROW_X, topPos + ARROW_Y, ARROW_U, ARROW_V, progress, ARROW_HEIGHT);
        }

        if (!menu.slots.get(1).getItem().isEmpty()) {
            boolean first = (System.currentTimeMillis() / GLOW_PERIOD_MS) % 2L == 0L;
            graphics.blit(TEXTURE, leftPos + GLOW_X, topPos + GLOW_Y, first ? GLOW_U_FIRST : GLOW_U_SECOND, GLOW_V, GLOW_SIZE, GLOW_SIZE);
        }

        List<DeferredItem<TrinketItem>> trinkets = List.copyOf(ModItems.all());
        int startX = iconStartX(trinkets.size());
        for (int i = 0; i < trinkets.size(); i++) {
            drawIcon(graphics, new ItemStack(trinkets.get(i).get()), startX + i * ICON_SPACING, topPos + ICON_Y);
        }

        int percent = menu.getProgress() * 100 / menu.getMaxProgress();
        drawCentered(graphics, Component.translatable("screen.mobtrinkets.assembler.progress", percent),
                leftPos + MACHINE_CENTER_X, topPos + PERCENT_Y, TEXT_LIGHT, SMALL_TEXT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, TEXT_DARK, false);
        drawCentered(graphics, Component.translatable("screen.mobtrinkets.assembler.input"), INPUT_CENTER_X, LABEL_Y, TEXT_DARK, 1.0F);
        drawCentered(graphics, Component.translatable("screen.mobtrinkets.assembler.output"), OUTPUT_CENTER_X, LABEL_Y, TEXT_DARK, 1.0F);
        drawCentered(graphics, Component.translatable("screen.mobtrinkets.assembler.gamble"), MACHINE_CENTER_X, LABEL_Y, TEXT_PURPLE, 1.0F);
        drawCentered(graphics, Component.translatable("screen.mobtrinkets.assembler.rare"), imageWidth / 2, CAPTION_Y, TEXT_DARK, 1.0F);
        graphics.drawString(font, playerInventoryTitle, INVENTORY_LABEL_X, inventoryLabelY, TEXT_DARK, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        ItemStack hovered = hoveredTrinket(mouseX, mouseY);
        if (!hovered.isEmpty()) {
            graphics.renderTooltip(font, hovered, mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    private int iconStartX(int count) {
        int total = count * ICON_SIZE + (count - 1) * (ICON_SPACING - ICON_SIZE);
        return leftPos + STRIP_X + (STRIP_WIDTH - total) / 2;
    }

    private ItemStack hoveredTrinket(int mouseX, int mouseY) {
        int iconY = topPos + ICON_Y;
        if (mouseY < iconY || mouseY >= iconY + ICON_SIZE) {
            return ItemStack.EMPTY;
        }
        List<DeferredItem<TrinketItem>> trinkets = List.copyOf(ModItems.all());
        int startX = iconStartX(trinkets.size());
        for (int i = 0; i < trinkets.size(); i++) {
            int iconX = startX + i * ICON_SPACING;
            if (mouseX >= iconX && mouseX < iconX + ICON_SIZE) {
                return new ItemStack(trinkets.get(i).get());
            }
        }
        return ItemStack.EMPTY;
    }

    private void drawIcon(GuiGraphics graphics, ItemStack stack, int x, int y) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(ICON_SCALE, ICON_SCALE, 1.0F);
        graphics.renderItem(stack, 0, 0);
        graphics.pose().popPose();
    }

    private void drawCentered(GuiGraphics graphics, Component text, int centerX, int y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        int width = font.width(text);
        graphics.drawString(font, text, Math.round((centerX - width * scale / 2.0F) / scale), Math.round(y / scale), color, false);
        graphics.pose().popPose();
    }
}