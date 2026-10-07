package org.chubby.github.mobtrinkets.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketSigilItem;
import org.chubby.github.mobtrinkets.trinket.TrinketItem;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MobTrinkets.MOD_ID);

    public static final DeferredItem<TrinketSigilItem> SIGIL = ITEMS.registerItem("trinket_sigil",
            properties -> new TrinketSigilItem(properties.stacksTo(16).rarity(Rarity.EPIC)));

    private static final Map<String, DeferredItem<TrinketItem>> TRINKETS = new LinkedHashMap<>();

    static {
        for (TrinketDefinition definition : TrinketRegistry.all()) {
            DeferredItem<TrinketItem> item = ITEMS.registerItem(definition.id(),
                    properties -> new TrinketItem(definition, properties.stacksTo(1).rarity(definition.rarity().vanilla())));
            TRINKETS.put(definition.id(), item);
        }
    }

    private ModItems() {
    }

    public static DeferredItem<TrinketItem> get(TrinketDefinition definition) {
        return TRINKETS.get(definition.id());
    }

    public static DeferredItem<TrinketItem> byId(String id) {
        return TRINKETS.get(id);
    }

    public static Collection<DeferredItem<TrinketItem>> all() {
        return TRINKETS.values();
    }
}
