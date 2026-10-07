package org.chubby.github.mobtrinkets.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.chubby.github.mobtrinkets.network.OpenTrinketMenuPayload;
import org.chubby.github.mobtrinkets.registry.ModItems;

public class TrinketButton extends Button {
    private static final int SIZE = 18;
    private static final int ICON_OFFSET = 1;

    private final ItemStack icon = new ItemStack(ModItems.byId("ender_eye").get());

    public TrinketButton(int x, int y) {
        super(x, y, SIZE, SIZE, Component.empty(), button -> PacketDistributor.sendToServer(new OpenTrinketMenuPayload()), DEFAULT_NARRATION);
        setTooltip(Tooltip.create(Component.translatable("gui.mobtrinkets.open")));
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        graphics.renderItem(icon, getX() + ICON_OFFSET, getY() + ICON_OFFSET);
    }
}
