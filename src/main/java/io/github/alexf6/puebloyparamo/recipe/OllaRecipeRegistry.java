package io.github.alexf6.puebloyparamo.recipe;

import io.github.alexf6.puebloyparamo.PuebloYParamoConfig;
import io.github.alexf6.puebloyparamo.PuebloYParamoMod;
import io.github.alexf6.puebloyparamo.logic.OllaLogic.IngredientGroup;
import io.github.alexf6.puebloyparamo.logic.OllaLogic.RecipeSpec;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Catálogo de recetas de la Olla de barro, incluyendo recetas propias e integración
 * blanda opcional con los mods hermanos {@code gastronomia} y {@code mitosyfauna}.
 */
public final class OllaRecipeRegistry {

    private static final List<RecipeSpec> RECIPES = new ArrayList<>();

    private OllaRecipeRegistry() {
    }

    public static void init() {
        RECIPES.clear();

        // 1. Aguapanela caliente: agua pura + azúcar o caña de azúcar
        RECIPES.add(new RecipeSpec(
                "aguapanela_caliente",
                List.of(
                        IngredientGroup.of("puebloyparamo:agua_pura"),
                        IngredientGroup.of("minecraft:sugar", "minecraft:sugar_cane")
                ),
                "puebloyparamo:aguapanela_caliente",
                1
        ));

        // 2. Infusión de frailejón: agua pura + fibra de frailejón + miel o azúcar
        RECIPES.add(new RecipeSpec(
                "infusion_frailejon",
                List.of(
                        IngredientGroup.of("puebloyparamo:agua_pura"),
                        IngredientGroup.of("puebloyparamo:frailejon_fiber"),
                        IngredientGroup.of("minecraft:honey_bottle", "minecraft:sugar")
                ),
                "puebloyparamo:infusion_frailejon",
                1
        ));

        // 3. Caldo de costilla: agua pura + carne de res + papa
        RECIPES.add(new RecipeSpec(
                "caldo_costilla",
                List.of(
                        IngredientGroup.of("puebloyparamo:agua_pura"),
                        IngredientGroup.of("minecraft:beef", "minecraft:cooked_beef"),
                        IngredientGroup.of("minecraft:potato", "minecraft:baked_potato")
                ),
                "puebloyparamo:caldo_costilla",
                1
        ));

        // 4. Ajiaco santafereño: agua pura + pollo + papa + leche o betarraga/zanahoria
        RECIPES.add(new RecipeSpec(
                "ajiaco",
                List.of(
                        IngredientGroup.of("puebloyparamo:agua_pura"),
                        IngredientGroup.of("minecraft:chicken", "minecraft:cooked_chicken"),
                        IngredientGroup.of("minecraft:potato", "minecraft:baked_potato"),
                        IngredientGroup.of("minecraft:milk_bucket", "minecraft:carrot", "minecraft:sweet_berries")
                ),
                "puebloyparamo:ajiaco",
                1
        ));

        // 5. Sancocho trifásico: carne (res/cerdo/pollo) + papa + zanahoria + agua pura
        RECIPES.add(new RecipeSpec(
                "sancocho",
                List.of(
                        IngredientGroup.of("puebloyparamo:agua_pura"),
                        IngredientGroup.of(
                                "minecraft:beef", "minecraft:cooked_beef",
                                "minecraft:porkchop", "minecraft:cooked_porkchop",
                                "minecraft:chicken", "minecraft:cooked_chicken"
                        ),
                        IngredientGroup.of("minecraft:potato", "minecraft:baked_potato"),
                        IngredientGroup.of("minecraft:carrot", "minecraft:beetroot")
                ),
                "puebloyparamo:sancocho",
                1
        ));

        registerOptionalCrossModRecipes();
    }

    private static void registerOptionalCrossModRecipes() {
        if (!PuebloYParamoConfig.get().enableCrossModOllaRecipes()) {
            return;
        }
        if (FabricLoader.getInstance().isModLoaded("gastronomia")) {
            addIfItemsExist(
                    "compat_tinto_en_olla",
                    List.of(
                            IngredientGroup.of("puebloyparamo:agua_pura"),
                            IngredientGroup.of("gastronomia:roasted_coffee_beans"),
                            IngredientGroup.of("minecraft:sugar", "minecraft:sugar_cane")
                    ),
                    "gastronomia:tinto",
                    2
            );
            addIfItemsExist(
                    "compat_empanadas_olla",
                    List.of(
                            IngredientGroup.of("gastronomia:chili_pepper"),
                            IngredientGroup.of("minecraft:wheat"),
                            IngredientGroup.of("minecraft:beef", "minecraft:porkchop"),
                            IngredientGroup.of("minecraft:potato")
                    ),
                    "gastronomia:spicy_empanada",
                    2
            );
        }
        if (FabricLoader.getInstance().isModLoaded("mitosyfauna")) {
            addIfItemsExist(
                    "compat_cafe_campesino_olla",
                    List.of(
                            IngredientGroup.of("puebloyparamo:agua_pura"),
                            IngredientGroup.of("minecraft:cocoa_beans"),
                            IngredientGroup.of("minecraft:sugar")
                    ),
                    "mitosyfauna:cafe_campesino",
                    2
            );
            addIfItemsExist(
                    "compat_caldo_tropical",
                    List.of(
                            IngredientGroup.of("puebloyparamo:agua_pura"),
                            IngredientGroup.of("mitosyfauna:lulo", "mitosyfauna:maracuya", "mitosyfauna:mango_biche"),
                            IngredientGroup.of("minecraft:sugar")
                    ),
                    "puebloyparamo:infusion_frailejon",
                    2
            );
        }
    }

    private static void addIfItemsExist(
            String id,
            List<IngredientGroup> reqs,
            String resultId,
            int count
    ) {
        if (!BuiltInRegistries.ITEM.containsKey(Identifier.parse(resultId))) {
            PuebloYParamoMod.LOGGER.debug("Omitiendo receta compat {} porque {} no está registrado.", id, resultId);
            return;
        }
        for (IngredientGroup group : reqs) {
            boolean anyPresent = group.acceptedIds().stream().map(Identifier::parse).anyMatch(BuiltInRegistries.ITEM::containsKey);
            if (!anyPresent) {
                PuebloYParamoMod.LOGGER.debug("Omitiendo receta compat {} por falta de ingredientes opcionales.", id);
                return;
            }
        }
        RECIPES.add(new RecipeSpec(id, reqs, resultId, count));
    }

    public static List<RecipeSpec> getRecipes() {
        return List.copyOf(RECIPES);
    }
}
