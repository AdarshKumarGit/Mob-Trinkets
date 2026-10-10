
package org.chubby.github.mobtrinkets.command;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import org.chubby.github.mobtrinkets.MobTrinkets;
import org.chubby.github.mobtrinkets.registry.ModAttachments;
import org.chubby.github.mobtrinkets.trinket.TrinketData;
import org.chubby.github.mobtrinkets.trinket.TrinketDefinition;
import org.chubby.github.mobtrinkets.trinket.TrinketEquipment;
import org.chubby.github.mobtrinkets.trinket.TrinketMastery;
import org.chubby.github.mobtrinkets.trinket.TrinketRegistry;
import org.chubby.github.mobtrinkets.handler.TrinketHandler;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = MobTrinkets.MOD_ID)
public final class SetTrinketMasteryCommand {

    private static final int MAX_MASTERY_POINTS = 1_000_000;

    private SetTrinketMasteryCommand() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("trinketmastery")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("max")
                                .executes(context -> {
                                    if (!(context.getSource().getEntity()
                                            instanceof ServerPlayer player)) {
                                        context.getSource().sendFailure(
                                                Component.literal("This command requires a player.")
                                        );
                                        return 0;
                                    }

                                    return setMastery(player);
                                })
                        )
        );
    }

    public static int setMastery(ServerPlayer player) {
        TrinketData data = TrinketEquipment.data(player);

        if (data.unlocked() == 0 || data.stacks().isEmpty()) {
            player.sendSystemMessage(
                    Component.literal("You have no equipped trinkets.")
            );
            return 0;
        }

        TrinketMastery mastery = player.getData(ModAttachments.MASTERY);
        Set<String> mastered = new HashSet<>();

        for (ItemStack stack : data.stacks()) {
            TrinketDefinition definition = TrinketRegistry.forStack(stack);

            if (definition == null) {
                continue;
            }

            String id = definition.id().toString();

            if (!mastered.add(id)) {
                continue;
            }

            mastery = mastery.with(
                    definition.id(),
                    MAX_MASTERY_POINTS
            );
        }

        if (mastered.isEmpty()) {
            player.sendSystemMessage(
                    Component.literal("No valid equipped trinkets were found.")
            );
            return 0;
        }

        player.setData(ModAttachments.MASTERY, mastery);
        TrinketHandler.resync(player);

        player.sendSystemMessage(
                Component.literal(
                        "Mastery maxed for " + mastered.size()
                                + " equipped trinket(s)!"
                )
        );

        return Command.SINGLE_SUCCESS;
    }
}
