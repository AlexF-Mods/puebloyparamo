package io.github.alexf6.puebloyparamo.client;

import io.github.alexf6.puebloyparamo.block.ModBlocks;
import io.github.alexf6.puebloyparamo.block.PicoBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Instancia de audio espacial vinculada a un bloque Picó activo.
 * Se detiene automáticamente si el bloque se apaga, se rompe o cambia de mundo.
 */
public class PicoSoundInstance extends AbstractTickableSoundInstance {

    private final BlockPos picoPos;

    public PicoSoundInstance(BlockPos picoPos, SoundEvent soundEvent, float volume) {
        super(soundEvent, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
        this.picoPos = picoPos.immutable();
        this.looping = false;
        this.delay = 0;
        this.volume = volume;
        this.pitch = 1.0F;
        this.x = picoPos.getX() + 0.5;
        this.y = picoPos.getY() + 0.5;
        this.z = picoPos.getZ() + 0.5;
    }

    public void stopNow() {
        this.stop();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            this.stop();
            return;
        }
        BlockState state = level.getBlockState(picoPos);
        if (!state.is(ModBlocks.PICO) || !state.getValue(PicoBlock.PLAYING)) {
            this.stop();
        }
    }
}
