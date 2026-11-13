package io.github.alexf6.puebloyparamo.sound;

import io.github.alexf6.puebloyparamo.PuebloYParamoMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;

/**
 * Registro de eventos de sonido personalizados y claves de {@link JukeboxSong}
 * para las pistas de cumbia, vallenato, salsa y rap local del mod.
 */
public final class ModSounds {

    public static final SoundEvent MUSIC_CUMBIA = register("music_disc.cumbia");
    public static final SoundEvent MUSIC_VALLENATO = register("music_disc.vallenato");
    public static final SoundEvent MUSIC_SALSA = register("music_disc.salsa");
    public static final SoundEvent MUSIC_RAP_LOCAL = register("music_disc.rap_local");

    public static final ResourceKey<JukeboxSong> SONG_CUMBIA = songKey("cumbia");
    public static final ResourceKey<JukeboxSong> SONG_VALLENATO = songKey("vallenato");
    public static final ResourceKey<JukeboxSong> SONG_SALSA = songKey("salsa");
    public static final ResourceKey<JukeboxSong> SONG_RAP_LOCAL = songKey("rap_local");

    private static SoundEvent register(String path) {
        Identifier id = PuebloYParamoMod.id(path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    private static ResourceKey<JukeboxSong> songKey(String name) {
        return ResourceKey.create(Registries.JUKEBOX_SONG, PuebloYParamoMod.id(name));
    }

    public static void init() {
        // Dispara inicialización estática de SoundEvents y claves de JukeboxSong.
    }

    private ModSounds() {
    }
}
