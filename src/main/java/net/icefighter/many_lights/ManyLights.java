package net.icefighter.many_lights;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.icefighter.many_lights.network.HudStatePayload;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.core.jmx.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ManyLights implements ModInitializer {
	public static final String MOD_ID = "many_lights";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer SERVER;
	public static int currentLight = 0;
	public static boolean playing;

	@Override
	public void onInitialize() {

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			SERVER = server;
		});

		GameLogic.register();


		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(
					CommandManager.literal("lights").then(CommandManager.literal("karma")
							.then(CommandManager.argument("karma", IntegerArgumentType.integer(-3,3)).then(CommandManager.argument("target", EntityArgumentType.players())
									.executes(ManyLights::setPlayersKarma))))

			);
			dispatcher.register(
					CommandManager.literal("lights").then(CommandManager.literal("set")
							.then(CommandManager.argument("light", IntegerArgumentType.integer(0,9))
									.executes(ManyLights::setLight)))

			);
			dispatcher.register(
					CommandManager.literal("lights").then(CommandManager.literal("playing")
							.then(CommandManager.argument("playing", BoolArgumentType.bool())
									.executes(ManyLights::setPlaying)))

			);
		});
	}
	private static int setPlayersKarma(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "target");
		int amount = IntegerArgumentType.getInteger(context,"karma") + 4;
		for(ServerPlayerEntity player : players){
			KarmaNbt.setPlayerKarma(player,amount);
		}
		return Command.SINGLE_SUCCESS;
	}

	private static int setLight(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		setCurrentLight(IntegerArgumentType.getInteger(context,"light"));
		return Command.SINGLE_SUCCESS;
	}
	public static void setCurrentLight(int x){
		currentLight = x;
	}

	private static int setPlaying(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		setCurrentPlaying(BoolArgumentType.getBool(context,"playing"),context.getSource().getServer());
		return Command.SINGLE_SUCCESS;
	}
	public static void setCurrentPlaying(boolean x, MinecraftServer server){
		playing = x;
		GameLogic.tick = 0;
		for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
			KarmaNbt.setPlayerKarma(player,4);
		}
	}


	public static final Identifier HUD_STATE_PACKET_ID = Identifier.of(ManyLights.MOD_ID, "hud_state");

	public static boolean isPlaying = false;

	public static void setPlaying2(ServerWorld world, boolean playing) {
		isPlaying = playing;

		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeBoolean(playing);

		for (ServerPlayerEntity player : world.getPlayers()) {
			ServerPlayNetworking.send(player, new HudStatePayload(true));
		}
	}

	public static boolean readPlaying(ServerWorld world, ServerPlayerEntity player){

		return false;
	}

}