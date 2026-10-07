package org.chubby.github.mobtrinkets.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.menu.TrinketMenu;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;

public class TrinketScreen extends AbstractContainerScreen<TrinketMenu> {
    private static final ResourceLocation TEXTURE = MobTrinkets.id("textures/gui/trinket_menu.png");
    private static final int ICON_X = 60;
    private static final int ICON_Y = 19;
    private static final int NAME_X = 80;
    private static final int NAME_Y = 20;
    private static final int RARITY_Y = 29;
    private static final int TEXT_X = 60;
    private static final int TEXT_Y = 39;
    private static final int TEXT_WIDTH = 106;
    private static final int MAX_LINES = 4;
    private static final int TEXT_COLOR = 0xDDDDDD;
    private static final int HINT_COLOR = 0xAAAAAA;
    private static final int BUTTON_X = 8;
    private static final int BUTTON_Y = 56;
    private static final int BUTTON_WIDTH = 46;
    private static final int BUTTON_HEIGHT = 14;

    private Button removeButton;

    public TrinketScreen(TrinketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        removeButton = addRenderableWidget(Button.builder(Component.translatable("gui.mobtrinkets.remove"),
                        button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, TrinketMenu.REMOVE_BUTTON))
                .bounds(leftPos + BUTTON_X, topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        removeButton.active = !TrinketEquipment.get(minecraft.player).isEmpty();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        ItemStack stack = TrinketEquipment.get(minecraft.player);
        TrinketDefinition definition = TrinketRegistry.forStack(stack);
        if (definition == null) {
            drawLines(graphics, Component.translatable("gui.mobtrinkets.empty"), ICON_Y, HINT_COLOR);
            return;
        }
        graphics.renderItem(stack, ICON_X, ICON_Y);
        graphics.drawString(font, Component.translatable(stack.getDescriptionId()).withStyle(definition.rarity().formatting()), NAME_X, NAME_Y, TEXT_COLOR, true);
        graphics.drawString(font, definition.rarity().displayName(), NAME_X, RARITY_Y, TEXT_COLOR, true);
        drawLines(graphics, definition.ability().description(), TEXT_Y, TEXT_COLOR);
    }

    private void drawLines(GuiGraphics graphics, Component text, int startY, int color) {
        int y = startY;
        int count = 0;
        for (FormattedCharSequence line : font.split(text, TEXT_WIDTH)) {
            if (count++ >= MAX_LINES) {
                break;
            }
            graphics.drawString(font, line, TEXT_X, y, color, true);
            y += font.lineHeight + 1;
        }
    }
}
