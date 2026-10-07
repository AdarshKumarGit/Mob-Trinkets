package org.chubby.github.mobtrinkets.handler;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.trinket.TrinketAbility;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class TrinketDefenseHandler {
    private TrinketDefenseHandler() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        TrinketDefinition definition = TrinketEquipment.active(player);
        if (definition == null) {
            return;
        }
        DamageSource source = event.getSource();
        switch (definition.ability()) {
            case EXPLOSION_GUARD -> {
                if (source.is(DamageTypeTags.IS_EXPLOSION)) {
                    event.setAmount((float) (event.getAmount() * (1.0 - TrinketConfig.EXPLOSION_REDUCTION.get())));
                }
            }
            case FIRE_IMMUNITY -> {
                if (source.is(DamageTypeTags.IS_FIRE)) {
                    event.setCanceled(true);
                }
            }
            case FIRE_GUARD -> {
                if (source.is(DamageTypeTags.IS_FIRE)) {
                    event.setAmount((float) (event.getAmount() * (1.0 - TrinketConfig.FIRE_REDUCTION.get())));
                }
            }
            default -> {
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
