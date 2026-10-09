package org.chubby.github.mobtrinkets.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.handler.TrinketSkills;

public record UseSkillPayload(int slot, boolean secondary) implements CustomPacketPayload {
    public static final Type<UseSkillPayload> TYPE = new Type<>(MobTrinkets.id("use_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UseSkillPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, UseSkillPayload::slot,
            ByteBufCodecs.BOOL, UseSkillPayload::secondary,
            UseSkillPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseSkillPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            TrinketSkills.use(player, payload.slot(), payload.secondary());
        }
    }
}
