package org.chubby.github.mobtrinkets.network;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.menu.TrinketMenu;

public record OpenTrinketMenuPayload() implements CustomPacketPayload {
    public static final Type<OpenTrinketMenuPayload> TYPE = new Type<>(MobTrinkets.id("open_trinket_menu"));
    public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, OpenTrinketMenuPayload> CODEC = StreamCodec.unit(new OpenTrinketMenuPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenTrinketMenuPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player && player.isAlive()) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, ignored) -> new TrinketMenu(containerId, inventory),
                    Component.translatable("gui.mobtrinkets.title")));
        }
    }
}
