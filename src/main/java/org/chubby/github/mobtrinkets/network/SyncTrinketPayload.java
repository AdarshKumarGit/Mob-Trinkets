package org.chubby.github.mobtrinkets.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.registry.ModAttachments;

public record SyncTrinketPayload(ItemStack stack) implements CustomPacketPayload {
    public static final Type<SyncTrinketPayload> TYPE = new Type<>(MobTrinkets.id("sync_trinket"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncTrinketPayload> CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, SyncTrinketPayload::stack, SyncTrinketPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncTrinketPayload payload, IPayloadContext context) {
        context.player().setData(ModAttachments.EQUIPPED, payload.stack());
    }
}
