package net.icefighter.many_lights;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class ManyLightsClient implements ClientModInitializer {

    public final KarmaNbt karmaNbt = new KarmaNbt();

    @Override
    public void onInitializeClient() {

                new GameHud(karmaNbt);
        }

    }
