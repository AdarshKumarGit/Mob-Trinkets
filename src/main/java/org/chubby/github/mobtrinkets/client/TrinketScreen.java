package org.chubby.github.mobtrinkets.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.handler.TrinketSkills;
import org.chubby.github.mobtrinkets.menu.TrinketMenu;
import org.chubby.github.mobtrinkets.menu.TrinketSlot;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;
import org.chubby.github.mobtrinkets.trinket.TrinketSkill;
import org.chubby.github.mobtrinkets.trinket.TrinketUnlocks;

public class TrinketScreen extends AbstractContainerScreen<TrinketMenu> {
    private static final ResourceLocation TEXTURE = MobTrinkets.id("textures/gui/trinket_menu.png");
    private static final String[] ROMAN = {"I", "II", "III"};

    private static final int ITEM_SIZE = 16;
    private static final int LOCK_OVERLAY = 0xC0101010;
    private static final int PANEL_X = 34;
    private static final int PANEL_Y = 15;
    private static final int PANEL_W = 136;
    private static final int PANEL_H = 64;
    private static final int PAD = 7;
    private static final int BAR_Y = PANEL_Y + 18;
    private static final int BAR_H = 7;
    private static final int CHIP_H = 12;
    private static final int CHIP_FIRST_Y = PANEL_Y + 38;
    private static final int CHIP_GAP = 13;
    private static final int GEM_SIZE = 8;
    private static final float SMALL = 0.75F;
    private static final int SELECT_COLOR = 0xFFE8B040;
    private static final int TEXT_MUTED = 0xFF9090A0;

    private int viewSlot;

    public TrinketScreen(TrinketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        TrinketData data = TrinketEquipment.data(minecraft.player);
        viewSlot = 0;
        for (int slot = 0; slot < TrinketData.MAX_SLOTS; slot++) {
            if (!data.stack(slot).isEmpty()) {
                viewSlot = slot;
                break;
            }
        }
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
            int y = topPos + TrinketMenu.slotY(slot);
            graphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, LOCK_OVERLAY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        TrinketData data = TrinketEquipment.data(minecraft.player);
        if (hoveredSlot instanceof TrinketSlot slot && slot.isUnlocked()) {
            viewSlot = slot.trinketIndex();
        }
        if (data.isUnlocked(viewSlot)) {
            int x = TrinketMenu.SLOT_X - 2;
            int y = TrinketMenu.slotY(viewSlot) - 2;
            outline(graphics, x, y, x + ITEM_SIZE + 4, y + ITEM_SIZE + 4, SELECT_COLOR);
        }
        ItemStack stack = data.stack(viewSlot);
        TrinketDefinition definition = TrinketRegistry.forStack(stack);
        if (!data.isUnlocked(viewSlot)) {
            drawMessage(graphics, Component.translatable("gui.mobtrinkets.unlock_hint", TrinketUnlocks.nextCost(minecraft.player)));
        } else if (definition == null) {
            drawMessage(graphics, Component.translatable("gui.mobtrinkets.empty_panel"));
        } else {
            drawMastery(graphics, stack, definition);
        }
    }

    private void drawMessage(GuiGraphics graphics, Component text) {
        int y = PANEL_Y + 20;
        for (FormattedCharSequence line : font.split(text, PANEL_W - 2 * PAD)) {
            int width = font.width(line);
            graphics.drawString(font, line, PANEL_X + (PANEL_W - width) / 2, y, TEXT_MUTED, false);
            y += font.lineHeight + 1;
        }
    }

    private void drawMastery(GuiGraphics graphics, ItemStack stack, TrinketDefinition definition) {
        int points = TrinketMastery.pointsOf(minecraft.player, definition);
        int tier = TrinketMastery.tierOf(points);
        int accent = 0xFF000000 | colorOf(definition);
        graphics.fill(PANEL_X + 1, PANEL_Y + 1, PANEL_X + 3, PANEL_Y + PANEL_H - 1, accent);
        Component name = Component.translatable(stack.getDescriptionId()).withStyle(definition.rarity().formatting());
        graphics.drawString(font, name, PANEL_X + PAD, PANEL_Y + 5, 0xFFFFFFFF, true);
        for (int i = 0; i < TrinketMastery.MAX_TIER; i++) {
            gem(graphics, PANEL_X + PANEL_W - PAD - (TrinketMastery.MAX_TIER - i) * (GEM_SIZE + 1) + 1, PANEL_Y + 4, i < tier);
        }
        drawBar(graphics, points, tier);
        drawProgressText(graphics, points, tier);
        drawChip(graphics, CHIP_FIRST_Y, definition.primary(), TrinketSkills.PRIMARY_TIER, tier, keyLabel(false));
        drawChip(graphics, CHIP_FIRST_Y + CHIP_GAP, definition.secondary(), TrinketSkills.SECONDARY_TIER, tier, keyLabel(true));
    }

    private void drawBar(GuiGraphics graphics, int points, int tier) {
        int x = PANEL_X + PAD;
        int width = PANEL_W - 2 * PAD;
        graphics.fill(x - 1, BAR_Y - 1, x + width + 1, BAR_Y + BAR_H + 1, 0xFF000000);
        graphics.fill(x, BAR_Y, x + width, BAR_Y + BAR_H, 0xFF23232E);
        double fraction = 1.0;
        if (tier < TrinketMastery.MAX_TIER) {
            int start = TrinketMastery.tierStart(tier);
            fraction = (points - start) / (double) (TrinketMastery.tierEnd(tier) - start);
        }
        int filled = (int) Math.round(width * Math.max(0.0, Math.min(1.0, fraction)));
        if (filled > 0) {
            graphics.fill(x, BAR_Y, x + filled, BAR_Y + BAR_H, tier == TrinketMastery.MAX_TIER ? 0xFFB070E8 : 0xFFD99A2B);
            graphics.fill(x, BAR_Y, x + filled, BAR_Y + 2, tier == TrinketMastery.MAX_TIER ? 0xFFD9B0FF : 0xFFFFD66B);
        }
    }

    private void drawProgressText(GuiGraphics graphics, int points, int tier) {
        String scale = String.format("%.1f", TrinketMastery.scaleForTier(tier));
        Component text = tier >= TrinketMastery.MAX_TIER
                ? Component.translatable("gui.mobtrinkets.progress_max", scale)
                : Component.translatable("gui.mobtrinkets.progress", points, TrinketMastery.tierEnd(tier), scale);
        drawSmall(graphics, text, PANEL_X + PAD, BAR_Y + BAR_H + 3, 0xFFB8B8C8);
    }

    private void drawChip(GuiGraphics graphics, int y, TrinketSkill skill, int required, int tier, Component key) {
        boolean unlocked = tier >= required;
        int x = PANEL_X + PAD;
        int width = PANEL_W - 2 * PAD;
        graphics.fill(x, y, x + width, y + CHIP_H, unlocked ? 0xFF2A2230 : 0xFF1A1A22);
        outline(graphics, x, y, x + width, y + CHIP_H, unlocked ? 0xFFB8862E : 0xFF3A3A46);
        graphics.fill(x + 2, y + 2, x + CHIP_H - 2, y + CHIP_H - 2, unlocked ? 0xFFE8B040 : 0xFF505060);
        Component label = unlocked
                ? Component.empty().append(key).append(" ").append(skill.displayName())
                : Component.translatable("gui.mobtrinkets.requires_tier", ROMAN[required - 1]);
        drawSmall(graphics, label, x + CHIP_H + 2, y + 3, unlocked ? 0xFFFFE9B0 : 0xFF808090);
    }

    private Component keyLabel(boolean secondary) {
        MutableComponent label = Component.literal("[");
        if (secondary) {
            label.append("Shift+");
        }
        label.append(ClientKeys.SLOT_KEYS[viewSlot].getTranslatedKeyMessage()).append("]");
        return label;
    }

    private void drawSmall(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().scale(SMALL, SMALL, 1.0F);
        graphics.drawString(font, text, Math.round(x / SMALL), Math.round(y / SMALL), color, false);
        graphics.pose().popPose();
    }

    private static void gem(GuiGraphics graphics, int x, int y, boolean on) {
        int body = on ? 0xFFFFD25A : 0xFF3A3A48;
        int light = on ? 0xFFFFF0B0 : 0xFF4A4A58;
        int dark = on ? 0xFFC8861C : 0xFF2A2A34;
        graphics.fill(x + 2, y, x + 6, y + 1, light);
        graphics.fill(x + 1, y + 1, x + 7, y + 2, body);
        graphics.fill(x, y + 2, x + 8, y + 4, body);
        graphics.fill(x + 1, y + 4, x + 7, y + 5, dark);
        graphics.fill(x + 2, y + 5, x + 6, y + 6, dark);
    }

    private static void outline(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        graphics.fill(x0, y0, x1, y0 + 1, color);
        graphics.fill(x0, y1 - 1, x1, y1, color);
        graphics.fill(x0, y0, x0 + 1, y1, color);
        graphics.fill(x1 - 1, y0, x1, y1, color);
    }

    private static int colorOf(TrinketDefinition definition) {
        Integer color = definition.rarity().formatting().getColor();
        return color == null ? 0xFFFFFF : color;
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredSlot instanceof TrinketSlot slot && !slot.isUnlocked() && menu.getCarried().isEmpty()) {
            graphics.renderComponentTooltip(font, lockedTooltip(slot), mouseX, mouseY);
            return;
        }
        List<Component> chip = chipTooltip(mouseX - leftPos, mouseY - topPos);
        if (chip != null) {
            graphics.renderComponentTooltip(font, chip, mouseX, mouseY);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    private List<Component> chipTooltip(int x, int y) {
        TrinketData data = TrinketEquipment.data(minecraft.player);
        TrinketDefinition definition = TrinketRegistry.forStack(data.stack(viewSlot));
        if (definition == null || x < PANEL_X + PAD || x > PANEL_X + PANEL_W - PAD) {
            return null;
        }
        boolean secondary;
        if (y >= CHIP_FIRST_Y && y < CHIP_FIRST_Y + CHIP_H) {
            secondary = false;
        } else if (y >= CHIP_FIRST_Y + CHIP_GAP && y < CHIP_FIRST_Y + CHIP_GAP + CHIP_H) {
            secondary = true;
        } else {
            return null;
        }
        TrinketSkill skill = secondary ? definition.secondary() : definition.primary();
        int required = secondary ? TrinketSkills.SECONDARY_TIER : TrinketSkills.PRIMARY_TIER;
        List<Component> lines = new ArrayList<>();
        lines.add(skill.displayName().withStyle(ChatFormatting.GOLD));
        lines.add(skill.description().withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("gui.mobtrinkets.cooldown", skill.cooldownSeconds()).withStyle(ChatFormatting.DARK_GRAY));
        if (TrinketMastery.tier(minecraft.player, definition) < required) {
            lines.add(Component.translatable("gui.mobtrinkets.requires_tier", ROMAN[required - 1]).withStyle(ChatFormatting.RED));
        }
        return lines;
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
