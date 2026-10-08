package org.chubby.github.mobtrinkets.client;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.menu.TrinketMenu;
import org.chubby.github.mobtrinkets.menu.TrinketSlot;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketUnlocks;

public class TrinketScreen extends AbstractContainerScreen<TrinketMenu> {
    private static final ResourceLocation TEXTURE = MobTrinkets.id("textures/gui/trinket_menu.png");
    private static final int ITEM_SIZE = 16;
    private static final int LOCK_OVERLAY = 0xC0101010;

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
            int x = leftPos + TrinketMenu.slotX(slot);
            int y = topPos + TrinketMenu.SLOT_Y;
            graphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, LOCK_OVERLAY);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredSlot instanceof TrinketSlot slot && !slot.isUnlocked() && menu.getCarried().isEmpty()) {
            graphics.renderComponentTooltip(font, lockedTooltip(slot), mouseX, mouseY);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private List<Component> lockedTooltip(TrinketSlot slot) {
        Component title = Component.translatable("gui.mobtrinkets.locked").withStyle(ChatFormatting.GRAY);
        if (slot.trinketIndex() != TrinketEquipment.unlocked(minecraft.player)) {
            return List.of(title);
        }
        Component hint = Component.translatable("gui.mobtrinkets.unlock_hint", TrinketUnlocks.nextCost(minecraft.player)).withStyle(ChatFormatting.GOLD);
        return List.of(title, hint);
    }
}
