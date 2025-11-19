package net.icefighter.many_lights;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class GameHud {
    private final KarmaNbt karmaNbt;

    public GameHud(KarmaNbt karmaNbt) {
        this.karmaNbt = karmaNbt;
        HudRenderCallback.EVENT.register(this::onHudRender);
    }


    private void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftServer server = ManyLights.SERVER;
        if(server != null){
            ClientPlayerEntity currentPlayer = MinecraftClient.getInstance().player;
            if (currentPlayer == null) return;

        if (true) {
            final Identifier HUD = Identifier.of(ManyLights.MOD_ID, "textures/ui/hud.png");
            final Identifier HUD_BAR = Identifier.of(ManyLights.MOD_ID, "textures/ui/hud_bar.png");
            final Identifier HUD_BAR_RED = Identifier.of(ManyLights.MOD_ID, "textures/ui/hud_bar_red.png");
            //all assets of the lights
            String[] LIGHTS_LIST = {"textures/ui/green_light.png", "textures/ui/red_light.png", "textures/ui/orange_light.png", "textures/ui/blue_light.png"
                    , "textures/ui/pink_light.png", "textures/ui/yellow_light.png", "textures/ui/brown_light.png", "textures/ui/black_light.png"
                    , "textures/ui/white_light.png", "textures/ui/purple_light.png"};
            //all lights rules descriptions
            final String[] LIGHTS_DESCRIPTIONS = {"You can move freely.", "You can't move.", "You can't not move.", "You can't not be in water.", "You can't be hurt.",
                    "You can't be hungry.", "You can't not be in the overworld.", "You can't not be in the dark.", "You can't not be in the light", "You can't be within 20 blocks of a player."};

            int LIGHT = ManyLights.currentLight;


            MinecraftClient client = MinecraftClient.getInstance();
            if (currentPlayer == null) return; // no player, nothing to render


            //this is how big the bar on the ui is based on the game logic's ticks
            int time = (int) (GameLogic.tick / (GameLogic.interval * 20) * 94);

            //karama value 4 by default then fetched for the player
            int karma = 4;
            if (server.getPlayerManager().getPlayer(currentPlayer.getUuid()) != null
                    && KarmaNbt.getPlayerKarma(Objects.requireNonNull(server.getPlayerManager().getPlayer(currentPlayer.getUuid()))) > 0) {
                karma = KarmaNbt.getPlayerKarma(Objects.requireNonNull(server.getPlayerManager().getPlayer(currentPlayer.getUuid())));
            }

            //hud rendering
            ctx.drawTexture(HUD, 0, 0, 0, 0, 96, 48, 96, 48);
            if ((GameLogic.tick >= (GameLogic.interval * 20 - 300))) {
                ctx.drawTexture(HUD_BAR_RED, 1, 0, 0, 0, time, 4, 94, 4);
            } else {
                ctx.drawTexture(HUD_BAR, 1, 0, 0, 0, time, 4, 94, 4);
            }
            ctx.drawTexture(Identifier.of(ManyLights.MOD_ID, LIGHTS_LIST[LIGHT]), 5, 8, 0, 0, 32, 32, 32, 32);
            ctx.drawTexture(Identifier.of(ManyLights.MOD_ID, "textures/ui/karma_" + (karma) + ".png"), 100, 4, 0, 0, 24, 24, 24, 24);


            // Save the current transform
            ctx.getMatrices().push();

            float scale = 0.5f;
            ctx.getMatrices().scale(scale, scale, 1.0f);

            //splits the text so it fits in the hud
            String text1 = "";
            String text2 = "";
            if (LIGHTS_DESCRIPTIONS[LIGHT].length() > 20) {
                text1 = LIGHTS_DESCRIPTIONS[LIGHT].substring(0, 20);
                text2 = LIGHTS_DESCRIPTIONS[LIGHT].substring(20);
            } else {
                text1 = LIGHTS_DESCRIPTIONS[LIGHT];
            }

            //put the rule in red or green depending on if you're following it or not
            if (GameLogic.Rule(currentPlayer, LIGHT)) {
                ctx.drawTextWithShadow(client.textRenderer, text1, (int) (38 / scale), (int) (20 / scale), 0x0FFF0F);
                ctx.drawTextWithShadow(client.textRenderer, text2, (int) (38 / scale), (int) (24 / scale), 0x0FFF0F);
            } else {
                ctx.drawTextWithShadow(client.textRenderer, text1, (int) (38 / scale), (int) (20 / scale), 0xFF0F0F);
                ctx.drawTextWithShadow(client.textRenderer, text2, (int) (38 / scale), (int) (24 / scale), 0xFF0F0F);
            }


            // Restore the original transform
            ctx.getMatrices().pop();
        }
    }}
}
