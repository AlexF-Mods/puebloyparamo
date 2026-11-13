package io.github.alexf6.puebloyparamo.network;

import io.github.alexf6.puebloyparamo.PuebloYParamoConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registro de paquetes personalizados del lado del servidor y utilidades de envío del Picó.
 */
public final class ModNetworking {

    private ModNetworking() {
    }

    public static void initServer() {
        PayloadTypeRegistry.clientboundPlay().register(PicoSoundPayload.TYPE, PicoSoundPayload.CODEC);
    }

    public static void sendPicoSound(ServerLevel level, BlockPos pos, Identifier soundId, boolean playing) {
        int range = PuebloYParamoConfig.get().picoAudioRangeBlocks() + 16;
        double rangeSq = (double) range * range;
        PicoSoundPayload payload = new PicoSoundPayload(pos, soundId, playing);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= rangeSq) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
