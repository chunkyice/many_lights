package net.icefighter.many_lights;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

import java.util.concurrent.ThreadLocalRandom;

public class GameLogic {
    public static float tick= 0;
    public static final int interval = 60;
    public static boolean rulesApplied=false;

    public static void register(){
        ServerTickEvents.END_SERVER_TICK.register(GameLogic::onTick);


        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            KarmaNbt.setPlayerKarma(newPlayer, 4);
        });

    }

    private static void onTick(MinecraftServer server){
        tick+= ManyLights.playing? 1 : 0;
        for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
            Karma(player,KarmaNbt.getPlayerKarma(player));
        }

        if(tick>=interval*20){
            for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
                player.getWorld().playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL, SoundCategory.RECORDS, 1.0f, 1.0f
                );
                if(!KarmaNbt.getPlayerRuled(player) && Rule(player, ManyLights.currentLight)){
                    ManyLights.LOGGER.info("karma before logic: " + KarmaNbt.getPlayerKarma(player));
                    ManyLights.LOGGER.info("karma math: " + (KarmaNbt.getPlayerKarma(player) + 1));
                    int amount = KarmaNbt.getPlayerKarma(player) + 1;
                    if(amount>7){amount=7;}
                    KarmaNbt.setPlayerKarma(player, amount);
                    ManyLights.LOGGER.info("karma after logic: " + KarmaNbt.getPlayerKarma(player));
                }
                KarmaNbt.setPlayerRuled(player,false);
            }
            tick = 0;
            ManyLights.setCurrentLight(0);
            rulesApplied = false;
        }
        else if(tick == (interval*20 - 300)){
           ManyLights.setCurrentLight(ThreadLocalRandom.current().nextInt(1, 10));
            for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
                player.getWorld().playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_BIT, SoundCategory.RECORDS, 1.0f, 1.0f
                );
            }

        }
        else if(tick >= (((float) 87 /94)*(interval*20))){
            for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
                if(!rulesApplied){
                player.getWorld().playSound(
                        null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_NOTE_BLOCK_FLUTE, SoundCategory.RECORDS, 1.0f, 1.0f
                );
                rulesApplied = true;
                }
                if(!KarmaNbt.getPlayerRuled(player) && !Rule(player, ManyLights.currentLight)){
                    ManyLights.LOGGER.info("karma before logic: " + KarmaNbt.getPlayerKarma(player));
                    ManyLights.LOGGER.info("karma math: " + (KarmaNbt.getPlayerKarma(player) - 1));
                    int amount = KarmaNbt.getPlayerKarma(player) - 1;
                    if(amount<0){amount=0;}
                    KarmaNbt.setPlayerKarma(player, amount);
                    ManyLights.LOGGER.info("karma after logic: " + KarmaNbt.getPlayerKarma(player));
                    KarmaNbt.setPlayerRuled(player,true);
                    player.getWorld().playSound(
                            null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.RECORDS, 1.0f, 0.5f
                    );
                }
            }
        }
    }

    private static void Karma(PlayerEntity player, int karma){
        if(karma == 1){
            if(!player.getActiveStatusEffects().containsKey(StatusEffects.POISON)){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON,60,0,false,false,false));
            }
            if(!player.getActiveStatusEffects().containsKey(StatusEffects.WEAKNESS)){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS,60,1,false,false,false));
            }
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10);
        }
        else if(karma == 2){
            if(!player.getActiveStatusEffects().containsKey(StatusEffects.WEAKNESS)){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS,60,1,false,false,false));
            }
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10);
        }
        else if(karma == 3){
            if(!player.getActiveStatusEffects().containsKey(StatusEffects.WEAKNESS)){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS,60,1,false,false,false));
            }
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20);
        } else if (karma == 4) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20);
        } else if (karma == 5) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(24);
        } else if (karma == 6) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(24);
            if(!player.getActiveStatusEffects().containsKey(StatusEffects.HASTE)){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE,60,0,false,false,false));
            }
        }
        else if(karma == 7){
            if(!player.getActiveStatusEffects().containsKey(StatusEffects.HASTE)){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE,60,0,false,false,false));
            }
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(26);
        }
    }
    public static boolean Rule(PlayerEntity player, int light){
        boolean moved = player.getX() != player.prevX || player.getY() != player.prevY || player.getZ() != player.prevZ;
        if(light == 1){return !moved;}
        if(light == 2){return moved;}
        if(light == 3){return player.isSubmergedInWater();}
        if(light == 4){double current = player.getHealth();
            double max = player.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
            return max == current;}
        if(light == 5){return player.getHungerManager().getFoodLevel() == 20;}
        if(light == 6){return player.getWorld().getRegistryKey() == World.OVERWORLD;}
        if(light == 7){return player.getWorld().getLightLevel(player.getBlockPos())<8;}
        if(light == 8){return player.getWorld().getLightLevel(player.getBlockPos())>7;}
        if(light == 9){if(player.getServer() != null){
            for (ServerPlayerEntity other : player.getServer().getPlayerManager().getPlayerList()) {
            if (other == player) continue; // skip self

            double distance = player.squaredDistanceTo(other);

            if (distance <= 20 * 20) { // squared distance for performance
                // Found a player within 20 blocks
                return false;
                }
            }
        }}
        return true;
    }
}
