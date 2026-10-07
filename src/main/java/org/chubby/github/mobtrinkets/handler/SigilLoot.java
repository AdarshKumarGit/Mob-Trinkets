package org.chubby.github.mobtrinkets.handler;

import java.util.Set;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.registry.ModItems;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class SigilLoot {
    private static final Set<String> CHEST_TABLES = Set.of(
            "chests/ancient_city",
            "chests/stronghold_library",
            "chests/woodland_mansion",
            "chests/bastion_treasure",
            "chests/end_city_treasure",
            "chests/buried_treasure",
            "chests/desert_pyramid",
            "chests/jungle_temple",
            "chests/simple_dungeon",
            "chests/abandoned_mineshaft",
            "chests/trial_chambers/reward"
    );

    private SigilLoot() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!CHEST_TABLES.contains(event.getName().getPath())) {
            return;
        }
        LootPool pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(TrinketConfig.SIGIL_CHANCE.get().floatValue()))
                .add(LootItem.lootTableItem(ModItems.SIGIL.get()))
                .build();
        event.getTable().addPool(pool);
    }
}
