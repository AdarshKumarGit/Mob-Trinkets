package org.chubby.github.mobtrinkets.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.registry.ModAttachments;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class TrinketMasteryHandler {
    private TrinketMasteryHandler() {
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) {
            return;
        }
        TrinketData data = TrinketEquipment.data(player);
        if (data.unlocked() == 0) {
            return;
        }
        TrinketMastery mastery = player.getData(ModAttachments.MASTERY);
        boolean changed = false;
        for (ItemStack stack : data.stacks()) {
            TrinketDefinition definition = TrinketRegistry.forStack(stack);
            if (definition == null) {
                continue;
            }
            int gain = victim.getType() == definition.source() ? TrinketConfig.SOURCE_KILL_POINTS.get() : victim instanceof Enemy ? 1 : 0;
            if (gain == 0) {
                continue;
            }
            int before = mastery.points(definition.id());
            int after = before + gain;
            mastery = mastery.with(definition.id(), after);
            changed = true;
            if (TrinketMastery.tierOf(after) > TrinketMastery.tierOf(before)) {
                announce(player, stack, TrinketMastery.tierOf(after));
            }
        }
        if (changed) {
            player.setData(ModAttachments.MASTERY, mastery);
            TrinketHandler.resync(player);
        }
    }

    private static void announce(ServerPlayer player, ItemStack stack, int tier) {
        Component name = Component.translatable(stack.getDescriptionId());
        player.displayClientMessage(Component.translatable("message.mobtrinkets.mastery_up", name, tier), false);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.0F);
    }
}
