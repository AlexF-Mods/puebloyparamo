package io.github.alexf6.puebloyparamo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PuebloYParamoConfigTest {

    @Test
    void defaultsMatchExpectedBalancedValues() {
        PuebloYParamoConfig cfg = PuebloYParamoConfig.defaults();
        assertTrue(cfg.enableFrailejonWorldgen());
        assertTrue(cfg.enableFrailejonMistParticles());
        assertTrue(cfg.enableRimaxExplosionImmunity());
        assertTrue(cfg.enablePicoPartyBuff());
        assertTrue(cfg.enableCrossModOllaRecipes());
        assertEquals(160, cfg.ollaCookTimeTicks());
        assertEquals(48, cfg.picoAudioRangeBlocks());
    }

    @Test
    void canonicalConstructorClampsOutOfRangeValues() {
        PuebloYParamoConfig clamped = new PuebloYParamoConfig(false, true, false, true, false, 5, 250);
        assertFalse(clamped.enableFrailejonWorldgen());
        assertEquals(20, clamped.ollaCookTimeTicks());
        assertEquals(96, clamped.picoAudioRangeBlocks());

        PuebloYParamoConfig highCookLowRange = new PuebloYParamoConfig(true, true, true, true, true, 9999, 2);
        assertEquals(1200, highCookLowRange.ollaCookTimeTicks());
        assertEquals(16, highCookLowRange.picoAudioRangeBlocks());
    }

    @Test
    void loadOrCreateMergesPartialConfigAndPreservesCorruptFile(@TempDir Path tempDir) throws IOException {
        Path configPath = tempDir.resolve("puebloyparamo.json");

        PuebloYParamoConfig created = PuebloYParamoConfig.loadOrCreate(configPath);
        assertEquals(PuebloYParamoConfig.defaults(), created);
        assertTrue(Files.exists(configPath));

        Files.writeString(configPath, "{\"ollaCookTimeTicks\": 240, \"enablePicoPartyBuff\": false}");
        PuebloYParamoConfig merged = PuebloYParamoConfig.loadOrCreate(configPath);
        assertEquals(240, merged.ollaCookTimeTicks());
        assertFalse(merged.enablePicoPartyBuff());
        assertEquals(48, merged.picoAudioRangeBlocks());

        Files.writeString(configPath, "{not valid json");
        PuebloYParamoConfig recovered = PuebloYParamoConfig.loadOrCreate(configPath);
        assertEquals(PuebloYParamoConfig.defaults(), recovered);
        assertEquals("{not valid json", Files.readString(configPath));
    }
}
