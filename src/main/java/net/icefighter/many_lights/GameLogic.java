package net.icefighter.many_lights;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

import java.util.concurrent.ThreadLocalRandom;

public class GameLogic {
    //current time between each iteration of the loop
    public static float tick = 0;

    //time in seconds of the loop
    public static final int interval = 60;

    //boolean so I don't play the sound of the rules being applied twice
    public static boolean rulesApplied = false;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(GameLogic::onTick);

        //resets player karma when they die
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            KarmaNbt.setPlayerKarma(newPlayer, 4);
        });

    }

    private static void onTick(MinecraftServer server) {
        //updates tick if the game is playing
        tick += ManyLights.playing ? 1 : 0;

        //applies karma
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Karma(player, KarmaNbt.getPlayerKarma(player));
        }

        //end of loop
        if (tick >= interval * 20) {
            //plays a sound to all players and gives +1 karma to all players that followed the light
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                //plays either a Chime or Cow Bell depending on if you failed or not
                player.getWorld().playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        (!KarmaNbt.getPlayerRuled(player) && Rule(player, ManyLights.currentLight)) ? SoundEvents.BLOCK_NOTE_BLOCK_CHIME : SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL,
                        SoundCategory.RECORDS, 1.0f, 1.0f
                );

                //adds +1 karma if you succeeded
                if (!KarmaNbt.getPlayerRuled(player) && Rule(player, ManyLights.currentLight)) {
                    int amount = KarmaNbt.getPlayerKarma(player) + 1;
                    if (amount > 7) {
                        amount = 7;
                    }
                    KarmaNbt.setPlayerKarma(player, amount);
                }
                KarmaNbt.setPlayerRuled(player, false);
            }

            //resets loop and light
            tick = 0;
            ManyLights.setCurrentLight(0);
            rulesApplied = false;

        }
        //sets a new random light and plays a sound to indicate it
        else if (tick == (interval * 20 - 300)) {
            ManyLights.setCurrentLight(ThreadLocalRandom.current().nextInt(1, 10));
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.getWorld().playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.RECORDS, 1.0f, 1.0f
                );
            }

        }
        //this is when the rules are applied (there's also an audio queue for that)
        else if (tick >= (((float) 87 / 94) * (interval * 20))) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!rulesApplied) {
                    player.getWorld().playSound(
                            null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.RECORDS, 1.0f, 1.0f
                    );
                    rulesApplied = true;
                }
                //-1 karma if you fail
                if (!KarmaNbt.getPlayerRuled(player) && !Rule(player, ManyLights.currentLight)) {
                    int amount = KarmaNbt.getPlayerKarma(player) - 1;
                    if (amount < 0) {
                        amount = 0;
                    }
                    KarmaNbt.setPlayerKarma(player, amount);
                    KarmaNbt.setPlayerRuled(player, true);
                    player.getWorld().playSound(
                            null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.RECORDS, 1.0f, 0.5f
                    );
                }
            }
        }
    }


    private static void Karma(PlayerEntity player, int karma) {
        if (karma == 1) {
            if (!player.getActiveStatusEffects().containsKey(StatusEffects.POISON)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0, false, false, false));
            }
            if (!player.getActiveStatusEffects().containsKey(StatusEffects.WEAKNESS)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1, false, false, false));
            }
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10);
        } else if (karma == 2) {
            if (!player.getActiveStatusEffects().containsKey(StatusEffects.WEAKNESS)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1, false, false, false));
            }
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10);
        } else if (karma == 3) {
            if (!player.getActiveStatusEffects().containsKey(StatusEffects.WEAKNESS)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1, false, false, false));
            }
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20);
        } else if (karma == 4) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20);
        } else if (karma == 5) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(24);
        } else if (karma == 6) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(24);
            if (!player.getActiveStatusEffects().containsKey(StatusEffects.HASTE)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, 0, false, false, false));
            }
        } else if (karma == 7) {
            if (!player.getActiveStatusEffects().containsKey(StatusEffects.HASTE)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 60, 0, false, false, false));
            }
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(26);
        }
    }
    public static boolean Rule(PlayerEntity player, int light) {
        boolean moved = player.getX() != player.prevX || player.getY() != player.prevY || player.getZ() != player.prevZ;
        if (light == 1) {
            return !moved;
        }
        if (light == 2) {
            return moved;
        }
        if (light == 3) {
            return player.isSubmergedInWater();
        }
        if (light == 4) {
            double current = player.getHealth();
            double max = player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
            return max == current;
        }
        if (light == 5) {
            return player.getHungerManager().getFoodLevel() == 20;
        }
        if (light == 6) {
            return player.getWorld().getRegistryKey() == World.OVERWORLD;
        }
        if (light == 7) {
            return player.getWorld().getLightLevel(player.getBlockPos()) < 8;
        }
        if (light == 8) {
            return player.getWorld().getLightLevel(player.getBlockPos()) > 7;
        }
        if (light == 9) {
            if (player.getServer() != null) {
                for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
                    if (other == player) continue; // skip self

                    double distance = player.squaredDistanceTo(other);

                    if (distance <= 20 * 20) { // squared distance for performance
                        // Found a player within 20 blocks
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
