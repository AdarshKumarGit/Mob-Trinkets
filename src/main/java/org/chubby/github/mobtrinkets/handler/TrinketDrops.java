package org.chubby.github.mobtrinkets.handler;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.config.TrinketConfig;
import org.chubby.github.mobtrinkets.registry.ModItems;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class TrinketDrops {
    private static final int FULL_SIZE_SLIME = 4;

    private TrinketDrops() {
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
}
