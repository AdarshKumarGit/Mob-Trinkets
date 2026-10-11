package org.chubby.github.mobtrinkets.client;

import net.minecraft.client.Minecraft;
import org.chubby.github.mobtrinkets.network.ModPayloads.BeamStatePayload;

public final class BeamClientState {
    private static final int STALE_TICKS = 40;

    private static int state;
    private static int ticks;
    private static int total = 1;
    private static long receivedAt;

    private BeamClientState() {
    }

    public static void set(int newState, int newTicks, int newTotal) {
        state = newState;
        ticks = newTicks;
        total = Math.max(1, newTotal);
        receivedAt = now();
    }

    public static boolean active() {
        return state != BeamStatePayload.NONE && now() - receivedAt < STALE_TICKS;
    }

    public static int state() {
        return state;
    }

    public static float progress() {
        float value = (ticks + (now() - receivedAt)) / (float) total;
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static long now() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }
}