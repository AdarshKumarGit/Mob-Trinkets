package org.chubby.github.mobtrinkets.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.client.BeamClientState;
import org.chubby.github.mobtrinkets.menu.TrinketMenu;
import org.chubby.github.mobtrinkets.registry.ModAttachments;
import org.chubby.github.mobtrinkets.skill.EnderTeleport;
import org.chubby.github.mobtrinkets.skill.PrismaticBeamChannel;
import org.chubby.github.mobtrinkets.skill.TrinketSkills;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;

public final class ModPayloads {
    private static final String PROTOCOL_VERSION = "1";

    private ModPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(SyncTrinketPayload.TYPE, SyncTrinketPayload.CODEC, SyncTrinketPayload::handle);
        registrar.playToClient(SyncMasteryPayload.TYPE, SyncMasteryPayload.CODEC, SyncMasteryPayload::handle);
        registrar.playToClient(BeamStatePayload.TYPE, BeamStatePayload.CODEC, BeamStatePayload::handle);

        registrar.playToServer(OpenTrinketMenuPayload.TYPE, OpenTrinketMenuPayload.CODEC, OpenTrinketMenuPayload::handle);
        registrar.playToServer(UseSkillPayload.TYPE, UseSkillPayload.CODEC, UseSkillPayload::handle);
        registrar.playToServer(TeleportPayload.TYPE, TeleportPayload.CODEC, TeleportPayload::handle);
        registrar.playToServer(BeamReleasePayload.TYPE, BeamReleasePayload.CODEC, BeamReleasePayload::handle);
    }


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

    public record BeamStatePayload(int state, int ticks, int total) implements CustomPacketPayload {
        public static final int NONE = 0;
        public static final int CHARGING = 1;
        public static final int FIRING = 2;

        public static final Type<BeamStatePayload> TYPE = new Type<>(MobTrinkets.id("beam_state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BeamStatePayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BeamStatePayload::state,
                ByteBufCodecs.VAR_INT, BeamStatePayload::ticks,
                ByteBufCodecs.VAR_INT, BeamStatePayload::total,
                BeamStatePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(BeamStatePayload payload, IPayloadContext context) {
            BeamClientState.set(payload.state(), payload.ticks(), payload.total());
        }
    }

    public record OpenTrinketMenuPayload() implements CustomPacketPayload {
        public static final Type<OpenTrinketMenuPayload> TYPE = new Type<>(MobTrinkets.id("open_trinket_menu"));
        public static final StreamCodec<FriendlyByteBuf, OpenTrinketMenuPayload> CODEC = StreamCodec.unit(new OpenTrinketMenuPayload());

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

    /** A skill key was pressed; {@code secondary} is set when Shift was held. */
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

    public record BeamReleasePayload() implements CustomPacketPayload {
        public static final Type<BeamReleasePayload> TYPE = new Type<>(MobTrinkets.id("beam_release"));
        public static final StreamCodec<FriendlyByteBuf, BeamReleasePayload> CODEC = StreamCodec.unit(new BeamReleasePayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(BeamReleasePayload payload, IPayloadContext context) {
            if (context.player() instanceof ServerPlayer player) {
                PrismaticBeamChannel.release(player);
            }
        }
    }
}
