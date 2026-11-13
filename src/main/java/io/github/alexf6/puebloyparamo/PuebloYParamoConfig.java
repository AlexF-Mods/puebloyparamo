package io.github.alexf6.puebloyparamo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuración persistente de {@code puebloyparamo} guardada en {@code config/puebloyparamo.json}.
 * Acota todos los valores numéricos directamente en el constructor canónico del registro.
 */
public record PuebloYParamoConfig(
        boolean enableFrailejonWorldgen,
        boolean enableFrailejonMistParticles,
        boolean enableRimaxExplosionImmunity,
        boolean enablePicoPartyBuff,
        boolean enableCrossModOllaRecipes,
        int ollaCookTimeTicks,
        int picoAudioRangeBlocks
) {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = PuebloYParamoMod.MOD_ID + ".json";

    public PuebloYParamoConfig {
        ollaCookTimeTicks = clampInt(ollaCookTimeTicks, 20, 1200);
        picoAudioRangeBlocks = clampInt(picoAudioRangeBlocks, 16, 96);
    }

    public static PuebloYParamoConfig defaults() {
        return new PuebloYParamoConfig(
                true,
                true,
                true,
                true,
                true,
                160,
                48
        );
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static PuebloYParamoConfig get() {
        return Holder.INSTANCE;
    }

    public static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        Holder.INSTANCE = loadOrCreate(configPath);
    }

    static PuebloYParamoConfig loadOrCreate(Path path) {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                Raw raw = GSON.fromJson(reader, Raw.class);
                if (raw != null) {
                    PuebloYParamoConfig merged = raw.toConfig(defaults());
                    write(path, merged);
                    return merged;
                }
                PuebloYParamoMod.LOGGER.warn("Archivo de configuración vacío en {}; usando valores por defecto.", path);
                return defaults();
            } catch (Exception e) {
                PuebloYParamoMod.LOGGER.warn(
                        "No se pudo leer {}; usando valores por defecto sin sobrescribir el archivo.", path, e
                );
                return defaults();
            }
        }
        PuebloYParamoConfig defaults = defaults();
        write(path, defaults);
        return defaults;
    }

    private static void write(Path path, PuebloYParamoConfig config) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            PuebloYParamoMod.LOGGER.warn("No se pudo guardar configuración en {}", path, e);
        }
    }

    private static final class Holder {
        private static volatile PuebloYParamoConfig INSTANCE = defaults();
    }

    private static final class Raw {
        Boolean enableFrailejonWorldgen;
        Boolean enableFrailejonMistParticles;
        Boolean enableRimaxExplosionImmunity;
        Boolean enablePicoPartyBuff;
        Boolean enableCrossModOllaRecipes;
        Integer ollaCookTimeTicks;
        Integer picoAudioRangeBlocks;

        PuebloYParamoConfig toConfig(PuebloYParamoConfig d) {
            return new PuebloYParamoConfig(
                    enableFrailejonWorldgen != null ? enableFrailejonWorldgen : d.enableFrailejonWorldgen(),
                    enableFrailejonMistParticles != null ? enableFrailejonMistParticles : d.enableFrailejonMistParticles(),
                    enableRimaxExplosionImmunity != null ? enableRimaxExplosionImmunity : d.enableRimaxExplosionImmunity(),
                    enablePicoPartyBuff != null ? enablePicoPartyBuff : d.enablePicoPartyBuff(),
                    enableCrossModOllaRecipes != null ? enableCrossModOllaRecipes : d.enableCrossModOllaRecipes(),
                    ollaCookTimeTicks != null ? ollaCookTimeTicks : d.ollaCookTimeTicks(),
                    picoAudioRangeBlocks != null ? picoAudioRangeBlocks : d.picoAudioRangeBlocks()
            );
        }
    }
}
