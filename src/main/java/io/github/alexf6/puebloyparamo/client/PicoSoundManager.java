package io.github.alexf6.puebloyparamo.client;

import io.github.alexf6.puebloyparamo.PuebloYParamoConfig;
import io.github.alexf6.puebloyparamo.network.PicoSoundPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Administrador cliente de pistas activas en bloques Picó.
 */
public final class PicoSoundManager {

    private static final Map<BlockPos, PicoSoundInstance> ACTIVE = new HashMap<>();

    private PicoSoundManager() {
    }

    public static void handlePayload(PicoSoundPayload payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            stopAt(payload.pos());
            if (!payload.playing()) {
                return;
            }
            SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(payload.soundId());
            if (sound == null) {
                return;
            }
            float volume = Math.max(1.0F, PuebloYParamoConfig.get().picoAudioRangeBlocks() / 16.0F);
            PicoSoundInstance instance = new PicoSoundInstance(payload.pos(), sound, volume);
            ACTIVE.put(payload.pos().immutable(), instance);
            Minecraft.getInstance().getSoundManager().play(instance);
        });
    }

    public static void stopAt(BlockPos pos) {
        PicoSoundInstance prev = ACTIVE.remove(pos);
        if (prev != null) {
            prev.stopNow();
            Minecraft.getInstance().getSoundManager().stop(prev);
        }
    }

    public static void stopAll() {
        for (PicoSoundInstance inst : ACTIVE.values()) {
            inst.stopNow();
            Minecraft.getInstance().getSoundManager().stop(inst);
        }
        ACTIVE.clear();
    }
}
