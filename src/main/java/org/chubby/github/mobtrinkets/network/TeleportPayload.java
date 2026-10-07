package org.chubby.github.mobtrinkets.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.handler.EnderTeleport;

public record TeleportPayload() implements CustomPacketPayload {
    public static final Type<TeleportPayload> TYPE = new Type<>(MobTrinkets.id("teleport"));
    public static final StreamCodec<FriendlyByteBuf, TeleportPayload> CODEC = StreamCodec.unit(new TeleportPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TeleportPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            EnderTeleport.tryTeleport(player);
        }
    }
}
