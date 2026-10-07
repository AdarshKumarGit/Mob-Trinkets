package org.chubby.github.mobtrinkets.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.menu.TrinketMenu;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;
import org.chubby.github.mobtrinkets.trinket.TrinketUnlocks;

public class TrinketScreen extends AbstractContainerScreen<TrinketMenu> {
    private static final ResourceLocation TEXTURE = MobTrinkets.id("textures/gui/trinket_menu.png");
    private static final int ITEM_SIZE = 16;
    private static final int LOCK_OVERLAY = 0xC0101010;
    private static final int TEXT_X = 62;
    private static final int TEXT_WIDTH = 104;
    private static final int FIRST_LINE_OFFSET = 1;
    private static final int SECOND_LINE_OFFSET = 10;
    private static final int TEXT_COLOR = 0xDDDDDD;
    private static final int MUTED_COLOR = 0x8A8A8A;
    private static final int HINT_COLOR = 0xE0B040;

    public TrinketScreen(TrinketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        TrinketData data = TrinketEquipment.data(minecraft.player);
        for (int slot = data.unlocked(); slot < TrinketData.MAX_SLOTS; slot++) {
            int x = leftPos + TrinketMenu.SLOT_X;
            int y = topPos + TrinketMenu.SLOT_Y + slot * TrinketMenu.SLOT_SPACING;
            graphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, LOCK_OVERLAY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        TrinketData data = TrinketEquipment.data(minecraft.player);
        for (int slot = 0; slot < TrinketData.MAX_SLOTS; slot++) {
            int y = TrinketMenu.SLOT_Y + slot * TrinketMenu.SLOT_SPACING + FIRST_LINE_OFFSET;
            if (!data.isUnlocked(slot)) {
                drawLocked(graphics, data, slot, y);
            } else {
                drawEquipped(graphics, data.stack(slot), y);
            }
        }
    }

    private void drawLocked(GuiGraphics graphics, TrinketData data, int slot, int y) {
        graphics.drawString(font, Component.translatable("gui.mobtrinkets.locked"), TEXT_X, y, MUTED_COLOR, true);
        if (slot == data.unlocked()) {
            Component hint = Component.translatable("gui.mobtrinkets.unlock_hint", TrinketUnlocks.nextCost(minecraft.player));
            graphics.drawString(font, font.plainSubstrByWidth(hint.getString(), TEXT_WIDTH), TEXT_X, y + SECOND_LINE_OFFSET, HINT_COLOR, true);
        }
    }

    private void drawEquipped(GuiGraphics graphics, ItemStack stack, int y) {
        TrinketDefinition definition = TrinketRegistry.forStack(stack);
        if (definition == null) {
            graphics.drawString(font, Component.translatable("gui.mobtrinkets.empty"), TEXT_X, y, MUTED_COLOR, true);
            return;
        }
        Component name = Component.translatable(stack.getDescriptionId()).withStyle(definition.rarity().formatting());
        graphics.drawString(font, name, TEXT_X, y, TEXT_COLOR, true);
        String description = definition.ability().description().getString();
        graphics.drawString(font, font.plainSubstrByWidth(description, TEXT_WIDTH), TEXT_X, y + SECOND_LINE_OFFSET, TEXT_COLOR, true);
    }
}
