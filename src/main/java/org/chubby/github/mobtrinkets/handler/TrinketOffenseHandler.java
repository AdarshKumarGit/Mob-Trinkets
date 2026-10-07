package org.chubby.github.mobtrinkets.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.trinket.TrinketAbility;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class TrinketOffenseHandler {
    private static final float DEGREES_TO_RADIANS = (float) Math.PI / 180.0F;
    private static final double KNOCKBACK_PER_LEVEL = 0.5;

    private TrinketOffenseHandler() {
    }

    @SubscribeEvent
    public static void onArrowJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk()) {
            return;
        }
        if (!(event.getEntity() instanceof AbstractArrow arrow) || arrow instanceof ThrownTrident) {
            return;
        }
        if (arrow.getOwner() instanceof ServerPlayer player && TrinketEquipment.has(player, TrinketAbility.ARROW_BOOST)) {
            arrow.setBaseDamage(arrow.getBaseDamage() * (1.0 + TrinketConfig.ARROW_DAMAGE_BONUS.get()));
        }
    }

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypes.PLAYER_ATTACK) || !(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!TrinketEquipment.has(player, TrinketAbility.KNOCKBACK_BOOST)) {
            return;
        }
        float yaw = player.getYRot() * DEGREES_TO_RADIANS;
        event.getEntity().knockback(TrinketConfig.KNOCKBACK_LEVELS.get() * KNOCKBACK_PER_LEVEL, Mth.sin(yaw), -Mth.cos(yaw));
    }
}
