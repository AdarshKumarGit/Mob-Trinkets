package org.chubby.github.mobtrinkets.event;

import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.registry.ModItems;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class LootEvents {
    private static final int FULL_SIZE_SLIME = 4;
    private static final double STONE_FRAGMENT_CHANCE = 0.05D;

    private static final Set<String> SIGIL_CHEST_TABLES = Set.of(
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

    private LootEvents() {
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        TrinketDefinition definition = TrinketRegistry.forEntity(victim.getType());
        if (definition == null || isSplitSlime(victim)) {
            return;
        }
        ServerPlayer killer = findKiller(event);
        if (killer == null) {
            return;
        }
        double chance = (TrinketConfig.dropChance(definition) + lootingLevel(killer) * TrinketConfig.LOOTING_BONUS.get())
                * TrinketConfig.DROP_MULTIPLIER.get();
        if (victim.getRandom().nextDouble() >= chance) {
            return;
        }
        ItemEntity drop = new ItemEntity(victim.level(), victim.getX(), victim.getY(), victim.getZ(),
                new ItemStack(ModItems.get(definition).get()));
        drop.setDefaultPickUpDelay();
        event.getDrops().add(drop);
    }

    private static boolean isSplitSlime(LivingEntity victim) {
        return victim instanceof Slime slime && slime.getSize() < FULL_SIZE_SLIME;
    }

    private static ServerPlayer findKiller(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && !(attacker instanceof FakePlayer)) {
            return attacker;
        }
        if (event.isRecentlyHit() && event.getEntity().getKillCredit() instanceof ServerPlayer credited && !(credited instanceof FakePlayer)) {
            return credited;
        }
        return null;
    }

    private static int lootingLevel(ServerPlayer player) {
        Holder<Enchantment> looting = player.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.LOOTING);
        return EnchantmentHelper.getItemEnchantmentLevel(looting, player.getMainHandItem());
    }

    // ---------------------------------------------------------------------------------------
    // Trinket Sigils in rare structure chests
    // ---------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!SIGIL_CHEST_TABLES.contains(event.getName().getPath())) {
            return;
        }
        LootPool pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(LootItemRandomChanceCondition.randomChance(TrinketConfig.SIGIL_CHANCE.get().floatValue()))
                .add(LootItem.lootTableItem(ModItems.SIGIL.get()))
                .build();
        event.getTable().addPool(pool);
    }

    // ---------------------------------------------------------------------------------------
    // Dusty Fragments from breaking stone
    // ---------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onStoneBroken(BlockEvent.BreakEvent event) {
        if (event.getPlayer().level().isClientSide || event.getPlayer().getAbilities().instabuild) {
            return;
        }
        if (!event.getState().is(BlockTags.BASE_STONE_OVERWORLD)) {
            return;
        }
        if (event.getPlayer().getRandom().nextDouble() < STONE_FRAGMENT_CHANCE) {
            Block.popResource(event.getPlayer().level(), event.getPos(), new ItemStack(ModItems.DUSTY_FRAGMENT.get()));
        }
    }
}
