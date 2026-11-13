package io.github.alexf6.puebloyparamo.network;

import io.github.alexf6.puebloyparamo.PuebloYParamoMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Paquete servidor -> cliente para iniciar o detener la reproducción de un disco en un Picó.
 */
public record PicoSoundPayload(BlockPos pos, Identifier soundId, boolean playing) implements CustomPacketPayload {

    public static final Type<PicoSoundPayload> TYPE = new Type<>(PuebloYParamoMod.id("pico_sound"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PicoSoundPayload> CODEC =
            StreamCodec.of(PicoSoundPayload::encode, PicoSoundPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, PicoSoundPayload payload) {
        buf.writeLong(payload.pos().asLong());
        buf.writeUtf(payload.soundId().toString());
        buf.writeBoolean(payload.playing());
    }

    private static PicoSoundPayload decode(RegistryFriendlyByteBuf buf) {
        BlockPos pos = BlockPos.of(buf.readLong());
        Identifier soundId = Identifier.parse(buf.readUtf());
        boolean playing = buf.readBoolean();
        return new PicoSoundPayload(pos, soundId, playing);
    }

    @Override
    public Type<PicoSoundPayload> type() {
        return TYPE;
    }
}
