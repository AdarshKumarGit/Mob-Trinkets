package org.chubby.github.mobtrinkets.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.registry.ModAttachments;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;

public record SyncMasteryPayload(TrinketMastery mastery) implements CustomPacketPayload {
    public static final Type<SyncMasteryPayload> TYPE = new Type<>(MobTrinkets.id("sync_mastery"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMasteryPayload> CODEC = StreamCodec.composite(
            TrinketMastery.STREAM_CODEC, SyncMasteryPayload::mastery, SyncMasteryPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncMasteryPayload payload, IPayloadContext context) {
        context.player().setData(ModAttachments.MASTERY, payload.mastery());
    }
}
