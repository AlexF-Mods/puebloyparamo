package io.github.alexf6.puebloyparamo;

import io.github.alexf6.puebloyparamo.block.ModBlocks;
import io.github.alexf6.puebloyparamo.block.entity.ModBlockEntities;
import io.github.alexf6.puebloyparamo.entity.ModEntities;
import io.github.alexf6.puebloyparamo.item.ModItems;
import io.github.alexf6.puebloyparamo.menu.ModMenus;
import io.github.alexf6.puebloyparamo.network.ModNetworking;
import io.github.alexf6.puebloyparamo.recipe.OllaRecipeRegistry;
import io.github.alexf6.puebloyparamo.sound.ModSounds;
import io.github.alexf6.puebloyparamo.worldgen.ModWorldgen;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada principal de Pueblo y Páramo.
 * Registra bloques coloniales y de páramo, entidades de asiento, olla de barro,
 * picó con discos musicales, recetas y generación natural de frailejones.
 */
public class PuebloYParamoMod implements ModInitializer {

    public static final String MOD_ID = "puebloyparamo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PuebloYParamoConfig.load();
        ModSounds.init();
        ModBlocks.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModMenus.init();
        ModItems.init();
        ModNetworking.initServer();
        OllaRecipeRegistry.init();
        ModWorldgen.init();
        LOGGER.info("Pueblo y Páramo inicializado correctamente.");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
