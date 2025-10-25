package net.icefighter.many_lights;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class CoinCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("coins")
            // version with argument
            .then(CommandManager.argument("target", EntityArgumentType.player())
                .executes(context -> {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                    int coins = getCoinAmount(target);
                    context.getSource().sendFeedback(() ->
                            Text.literal(target.getName().getString() + " has " + coins + " coins."), false);
                    return 1;
                }))
            .executes(context -> {
                ServerPlayerEntity player = context.getSource().getPlayer();
                int coins = getCoinAmount(player);
                player.sendMessage(Text.literal("You have " + coins + " coins."), false);
                return 1;
            })
        );
    }

    // Example placeholder function
    private static int getCoinAmount(ServerPlayerEntity player) {
        return 42; // Replace with your real logic
    }
}
