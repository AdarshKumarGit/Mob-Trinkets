package org.chubby.github.mobtrinkets.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.registry.ModAttachments;
import org.chubby.github.mobtrinkets.trinket.TrinketData;

public record SyncTrinketPayload(TrinketData data) implements CustomPacketPayload {
    public static final Type<SyncTrinketPayload> TYPE = new Type<>(MobTrinkets.id("sync_trinkets"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTrinketPayload> CODEC = StreamCodec.composite(
            TrinketData.STREAM_CODEC, SyncTrinketPayload::data, SyncTrinketPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTrinketPayload payload, IPayloadContext context) {
        context.player().setData(ModAttachments.TRINKETS, payload.data());
    }
}
