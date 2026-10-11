package org.chubby.github.mobtrinkets.trinket;

import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.network.PacketDistributor;
import org.chubby.github.mobtrinkets.network.ModPayloads.SyncMasteryPayload;
import org.chubby.github.mobtrinkets.network.ModPayloads.SyncTrinketPayload;
import org.chubby.github.mobtrinkets.registry.ModAttachments;

public final class TrinketSync {
    private TrinketSync() {
    }

    public static void resync(ServerPlayer player) {
        refresh(player);
        PacketDistributor.sendToPlayer(player, new SyncTrinketPayload(TrinketEquipment.data(player)));
        PacketDistributor.sendToPlayer(player, new SyncMasteryPayload(player.getData(ModAttachments.MASTERY)));
    }

    public static void refresh(ServerPlayer player) {
        Set<TrinketAbility> active = TrinketEquipment.data(player).abilities();
        for (TrinketAbility ability : TrinketAbility.attributeAbilities()) {
            AttributeInstance instance = player.getAttribute(ability.attribute());
            if (instance == null) {
                continue;
            }
            instance.removeModifier(ability.modifierId());
            if (active.contains(ability)) {
                instance.addTransientModifier(new AttributeModifier(
                        ability.modifierId(), ability.strength(TrinketMastery.scale(player, ability)), ability.operation()));
            }
        }
    }
}
