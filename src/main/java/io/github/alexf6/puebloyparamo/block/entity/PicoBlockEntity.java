package io.github.alexf6.puebloyparamo.block.entity;

import io.github.alexf6.puebloyparamo.PuebloYParamoConfig;
import io.github.alexf6.puebloyparamo.block.PicoBlock;
import io.github.alexf6.puebloyparamo.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

/**
 * Entidad de bloque del Picó: guarda el disco insertado, controla duración de reproducción,
 * envía paquetes de audio espacial al cliente y ambienta el pueblo con un buff alegre.
 */
public class PicoBlockEntity extends BlockEntity {

    private static final Identifier STOP_SENTINEL = Identifier.withDefaultNamespace("empty");

    private final NonNullList<ItemStack> recordSlot = NonNullList.withSize(1, ItemStack.EMPTY);
    private int ticksPlayed;
    private int songLengthTicks;
    private int partyCooldown;

    public PicoBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PICO, pos, state);
    }

    public boolean hasRecord() {
        return !recordSlot.get(0).isEmpty();
    }

    public void insertRecord(ServerLevel level, ItemStack disc, Player player) {
        Optional<JukeboxSong> song = resolveSong(level, disc);
        recordSlot.set(0, disc);
        ticksPlayed = 0;
        songLengthTicks = song
                .map(s -> Math.max(20, (int) Math.ceil(s.lengthInSeconds() * 20.0F)))
                .orElse(20 * 30);
        setChanged();

        level.setBlock(worldPosition, getBlockState().setValue(PicoBlock.PLAYING, true), Block.UPDATE_ALL);
        level.playSound(null, worldPosition, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.7F, 1.2F);

        Identifier soundId = song.map(PicoBlockEntity::extractSoundId).orElse(STOP_SENTINEL);
        ModNetworking.sendPicoSound(level, worldPosition, soundId, true);
    }

    public void ejectRecord(ServerLevel level, Player player) {
        ItemStack disc = recordSlot.get(0).copy();
        recordSlot.set(0, ItemStack.EMPTY);
        ticksPlayed = 0;
        songLengthTicks = 0;
        setChanged();

        if (!disc.isEmpty()) {
            if (!player.getInventory().add(disc)) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, disc);
            }
        }
        level.setBlock(worldPosition, getBlockState().setValue(PicoBlock.PLAYING, false), Block.UPDATE_ALL);
        ModNetworking.sendPicoSound(level, worldPosition, STOP_SENTINEL, false);
    }

    public void stopAndDrop(ServerLevel level) {
        ItemStack disc = recordSlot.get(0);
        if (!disc.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, disc.copy());
            recordSlot.set(0, ItemStack.EMPTY);
        }
        ModNetworking.sendPicoSound(level, worldPosition, STOP_SENTINEL, false);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PicoBlockEntity pico) {
        if (!(level instanceof ServerLevel serverLevel) || !pico.hasRecord()) {
            return;
        }
        if (pico.songLengthTicks > 0 && pico.ticksPlayed < pico.songLengthTicks) {
            pico.ticksPlayed++;
            if (pico.ticksPlayed >= pico.songLengthTicks && state.getValue(PicoBlock.PLAYING)) {
                level.setBlock(pos, state.setValue(PicoBlock.PLAYING, false), Block.UPDATE_ALL);
                pico.setChanged();
            }
        }

        if (state.getValue(PicoBlock.PLAYING) && PuebloYParamoConfig.get().enablePicoPartyBuff()) {
            pico.partyCooldown++;
            if (pico.partyCooldown >= 60) {
                pico.partyCooldown = 0;
                double radius = Math.min(20.0, PuebloYParamoConfig.get().picoAudioRangeBlocks() * 0.4);
                AABB area = new AABB(pos).inflate(radius);
                for (Player listener : serverLevel.getEntitiesOfClass(Player.class, area)) {
                    listener.addEffect(new MobEffectInstance(MobEffects.LUCK, 120, 0, true, false));
                }
            }
        }
    }

    private static Optional<JukeboxSong> resolveSong(ServerLevel level, ItemStack stack) {
        JukeboxPlayable playable = stack.get(DataComponents.JUKEBOX_PLAYABLE);
        if (playable == null) {
            return Optional.empty();
        }
        return playable.song().unwrap(level.registryAccess()).map(Holder::value);
    }

    private static Identifier extractSoundId(JukeboxSong song) {
        return song.soundEvent()
                .unwrapKey()
                .map(ResourceKey::identifier)
                .or(() -> Optional.ofNullable(BuiltInRegistries.SOUND_EVENT.getKey(song.soundEvent().value())))
                .orElse(STOP_SENTINEL);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, recordSlot);
        this.ticksPlayed = input.getIntOr("TicksPlayed", 0);
        this.songLengthTicks = input.getIntOr("SongLengthTicks", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, recordSlot);
        output.putInt("TicksPlayed", this.ticksPlayed);
        output.putInt("SongLengthTicks", this.songLengthTicks);
    }
}
