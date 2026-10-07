package org.chubby.github.mobtrinkets.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.network.SyncTrinketPayload;
import org.chubby.github.mobtrinkets.trinket.TrinketAbility;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class TrinketHandler {
    private TrinketHandler() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resync(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resync(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resync(player);
        }
    }

    public static void resync(ServerPlayer player) {
        refresh(player);
        PacketDistributor.sendToPlayer(player, new SyncTrinketPayload(TrinketEquipment.get(player)));
    }

    public static void refresh(ServerPlayer player) {
        TrinketDefinition active = TrinketEquipment.active(player);
        for (TrinketAbility ability : TrinketAbility.attributeAbilities()) {
            AttributeInstance instance = player.getAttribute(ability.attribute());
            if (instance == null) {
                continue;
            }
            instance.removeModifier(ability.modifierId());
            if (active != null && active.ability() == ability) {
                instance.addTransientModifier(new AttributeModifier(ability.modifierId(), ability.strength(), ability.operation()));
            }
        }
    }
}
