package org.chubby.github.mobtrinkets.trinket;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.registry.ModAttachments;

public final class TrinketMastery {
    public static final int MAX_TIER = 3;

    public static final TrinketMastery EMPTY = new TrinketMastery(Map.of());

    public static final Codec<TrinketMastery> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT)
            .xmap(TrinketMastery::new, TrinketMastery::points);

    public static final StreamCodec<ByteBuf, TrinketMastery> STREAM_CODEC = ByteBufCodecs
            .<ByteBuf, String, Integer, Map<String, Integer>>map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT)
            .map(TrinketMastery::new, TrinketMastery::points);

    private final Map<String, Integer> points;

    public TrinketMastery(Map<String, Integer> points) {
        this.points = Map.copyOf(points);
    }

    public Map<String, Integer> points() {
        return points;
    }

    public int points(String id) {
        return points.getOrDefault(id, 0);
    }

    public TrinketMastery with(String id, int value) {
        Map<String, Integer> updated = new HashMap<>(points);
        updated.put(id, value);
        return new TrinketMastery(updated);
    }

    public static int tierOf(int points) {
        if (points >= TrinketConfig.TIER3_POINTS.get()) {
            return 3;
        }
        return points >= TrinketConfig.TIER2_POINTS.get() ? 2 : 1;
    }

    public static int tierStart(int tier) {
        return switch (tier) {
            case 3 -> TrinketConfig.TIER3_POINTS.get();
            case 2 -> TrinketConfig.TIER2_POINTS.get();
            default -> 0;
        };
    }

    public static int tierEnd(int tier) {
        return tier >= MAX_TIER ? TrinketConfig.TIER3_POINTS.get() : tierStart(tier + 1);
    }

    public static double scaleForTier(int tier) {
        return switch (tier) {
            case 3 -> TrinketConfig.TIER3_SCALE.get();
            case 2 -> TrinketConfig.TIER2_SCALE.get();
            default -> 1.0;
        };
    }

    public static int pointsOf(Player player, TrinketDefinition definition) {
        return player.getData(ModAttachments.MASTERY).points(definition.id());
    }

    public static int tier(Player player, TrinketDefinition definition) {
        return tierOf(pointsOf(player, definition));
    }

    public static double scale(Player player, TrinketAbility ability) {
        TrinketDefinition definition = TrinketEquipment.equippedDefinition(player, ability);
        return definition == null ? 1.0 : scaleForTier(tier(player, definition));
    }
}
