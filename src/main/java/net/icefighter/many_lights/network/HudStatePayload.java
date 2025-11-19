package net.icefighter.many_lights.network;

import net.icefighter.many_lights.ManyLights;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HudStatePayload(boolean isPlaying) implements CustomPayload {
    public static final Id<HudStatePayload> ID = new Id<>(Identifier.of(ManyLights.MOD_ID, "hud_state"));

    public HudStatePayload(RegistryByteBuf buf) {
        this(buf.readBoolean());
    }

    //@Override
    public void write(RegistryByteBuf buf) {
        buf.writeBoolean(isPlaying);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}