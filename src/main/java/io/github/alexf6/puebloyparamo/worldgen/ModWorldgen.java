package io.github.alexf6.puebloyparamo.worldgen;

import io.github.alexf6.puebloyparamo.PuebloYParamoConfig;
import io.github.alexf6.puebloyparamo.PuebloYParamoMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Inyecta la generación natural de frailejones en biomas altos y fríos vanilla
 * (cumbres, laderas nevadas, praderas altas y colinas ventosas) sin añadir biomas nuevos.
 */
public final class ModWorldgen {

    public static final ResourceKey<PlacedFeature> FRAILEJON_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE,
            PuebloYParamoMod.id("frailejon_placed")
    );

    private ModWorldgen() {
    }

    public static void init() {
        if (!PuebloYParamoConfig.get().enableFrailejonWorldgen()) {
            return;
        }
        BiomeModifications.addFeature(
                BiomeSelectors.includeByKey(
                                Biomes.MEADOW,
                                Biomes.WINDSWEPT_HILLS,
                                Biomes.WINDSWEPT_GRAVELLY_HILLS,
                                Biomes.WINDSWEPT_FOREST,
                                Biomes.STONY_PEAKS,
                                Biomes.JAGGED_PEAKS,
                                Biomes.SNOWY_SLOPES,
                                Biomes.GROVE
                        )
                        .or(BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN)),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                FRAILEJON_PLACED
        );
    }
}
