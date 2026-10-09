package org.chubby.github.mobtrinkets.handler;

import java.util.Set;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.trinket.TrinketAbility;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class TrinketDefenseHandler {
    private TrinketDefenseHandler() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Set<TrinketAbility> abilities = TrinketEquipment.data(player).abilities();
        if (abilities.isEmpty()) {
            return;
        }
        DamageSource source = event.getSource();
        if (abilities.contains(TrinketAbility.EXPLOSION_GUARD) && source.is(DamageTypeTags.IS_EXPLOSION)) {
            event.setAmount((float) (event.getAmount() * (1.0 - TrinketAbility.EXPLOSION_GUARD.strength(TrinketMastery.scale(player, TrinketAbility.EXPLOSION_GUARD)))));
        }
        if (source.is(DamageTypeTags.IS_FIRE)) {
            if (abilities.contains(TrinketAbility.FIRE_IMMUNITY)) {
                event.setCanceled(true);
            } else if (abilities.contains(TrinketAbility.FIRE_GUARD)) {
                event.setAmount((float) (event.getAmount() * (1.0 - TrinketAbility.FIRE_GUARD.strength(TrinketMastery.scale(player, TrinketAbility.FIRE_GUARD)))));
            }
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && TrinketEquipment.has(player, TrinketAbility.SLOW_FALL)) {
            event.setDamageMultiplier(0.0F);
        }
    }

    @SubscribeEvent
    public static void onBreathe(LivingBreatheEvent event) {
        if (event.getEntity() instanceof Player player && TrinketEquipment.has(player, TrinketAbility.WATER_BREATHING)) {
            event.setCanBreathe(true);
        }
    }
}
