package net.icefighter.many_lights;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KarmaNbt {

    private static final String KARMA_KEY = "playerKarma";
    private static final ConcurrentHashMap<UUID, Integer> playerKarma = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Boolean> hasBeenRuled = new ConcurrentHashMap<>();

    public  static void setPlayerKarma(ServerPlayerEntity player, int karma) {playerKarma.put(player.getUuid(), karma);}
    public  static void setPlayerRuled(ServerPlayerEntity player, boolean ruled) {hasBeenRuled.put(player.getUuid(), ruled);}

    public static int getPlayerKarma(ServerPlayerEntity player) {
        return playerKarma.getOrDefault(player.getUuid(),4);
    }
    public static boolean getPlayerRuled(ServerPlayerEntity player) {return hasBeenRuled.getOrDefault(player.getUuid(),false);}
}
