package org.chubby.github.mobtrinkets.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.registry.ModAttachments;
import org.chubby.github.mobtrinkets.skill.TrinketSkills;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;
import org.chubby.github.mobtrinkets.trinket.TrinketSync;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class TrinketCommands {
    private static final int MAX_MASTERY_POINTS = 1_000_000;

    private TrinketCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("trinketmastery")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("max").executes(context -> {
                            if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
                                context.getSource().sendFailure(Component.literal("This command requires a player."));
                                return 0;
                            }
                            return maxMastery(player);
                        })));

        event.getDispatcher().register(
                Commands.literal("trinketcooldown")
                        .requires(source -> source.hasPermission(2))
                        .then(cooldownSwitch("off", true))
                        .then(cooldownSwitch("on", false)));
    }

    private static int maxMastery(ServerPlayer player) {
        TrinketData data = TrinketEquipment.data(player);
        if (data.unlocked() == 0) {
            player.sendSystemMessage(Component.literal("You have no equipped trinkets."));
            return 0;
        }

        TrinketMastery mastery = player.getData(ModAttachments.MASTERY);
        Set<String> mastered = new HashSet<>();
        for (ItemStack stack : data.stacks()) {
            TrinketDefinition definition = TrinketRegistry.forStack(stack);
            if (definition != null && mastered.add(definition.id())) {
                mastery = mastery.with(definition.id(), MAX_MASTERY_POINTS);
            }
        }

        if (mastered.isEmpty()) {
            player.sendSystemMessage(Component.literal("No valid equipped trinkets were found."));
            return 0;
        }

        player.setData(ModAttachments.MASTERY, mastery);
        TrinketSync.resync(player);
        player.sendSystemMessage(Component.literal("Mastery maxed for " + mastered.size() + " equipped trinket(s)!"));
        return Command.SINGLE_SUCCESS;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> cooldownSwitch(String name, boolean disabled) {
        return Commands.literal(name)
                .executes(context -> setCooldowns(context.getSource(), List.of(context.getSource().getPlayerOrException()), disabled))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> setCooldowns(context.getSource(), EntityArgument.getPlayers(context, "targets"), disabled)));
    }

    private static int setCooldowns(CommandSourceStack source, Collection<ServerPlayer> players, boolean disabled) {
        for (ServerPlayer player : players) {
            TrinketSkills.setCooldownDisabled(player, disabled);
        }
        String state = disabled ? "disabled" : "enabled";
        source.sendSuccess(() -> Component.literal("Trinket cooldowns " + state + " for " + players.size() + " player(s)."), true);
        return players.size();
    }
}
