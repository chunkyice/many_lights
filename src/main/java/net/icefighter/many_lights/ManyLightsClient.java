package net.icefighter.many_lights;

import net.fabricmc.api.ClientModInitializer;

public class ManyLightsClient implements ClientModInitializer {

    private final KarmaNbt karmaNbt = new KarmaNbt();

    @Override
    public void onInitializeClient() {


        new GameHud(karmaNbt);

    }
}
