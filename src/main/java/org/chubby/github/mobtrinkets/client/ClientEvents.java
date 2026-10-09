package org.chubby.github.mobtrinkets.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.network.TeleportPayload;
import org.chubby.github.mobtrinkets.network.UseSkillPayload;
import org.chubby.github.mobtrinkets.registry.ModItems;
import org.chubby.github.mobtrinkets.trinket.TrinketAbility;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private static final int BUTTON_X = 76;
    private static final int BUTTON_Y = 42;

    private static TrinketButton button;

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean active = minecraft.player != null && minecraft.screen == null;
        for (int slot = 0; slot < ClientKeys.SLOT_KEYS.length; slot++) {
            while (ClientKeys.SLOT_KEYS[slot].consumeClick()) {
                if (active) {
                    PacketDistributor.sendToServer(new UseSkillPayload(slot, Screen.hasShiftDown()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen) {
            button = new TrinketButton(screen.getGuiLeft() + BUTTON_X, screen.getGuiTop() + BUTTON_Y);
            event.addListener(button);
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        if (button != null && event.getScreen() instanceof InventoryScreen screen) {
            button.setPosition(screen.getGuiLeft() + BUTTON_X, screen.getGuiTop() + BUTTON_Y);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return;
        }
        if (!TrinketEquipment.has(player, TrinketAbility.SHORT_TELEPORT)) {
            return;
        }
        if (player.getCooldowns().isOnCooldown(ModItems.byId(TrinketRegistry.ENDER_EYE_ID).get())) {
            return;
        }
        PacketDistributor.sendToServer(new TeleportPayload());
    }
}
