package io.github.alexf6.puebloyparamo;

import io.github.alexf6.puebloyparamo.logic.OllaLogic;
import io.github.alexf6.puebloyparamo.logic.OllaLogic.IngredientGroup;
import io.github.alexf6.puebloyparamo.logic.OllaLogic.RecipeSpec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OllaLogicTest {

    private static final List<RecipeSpec> SAMPLE_RECIPES = List.of(
            new RecipeSpec(
                    "aguapanela",
                    List.of(
                            IngredientGroup.of("puebloyparamo:agua_pura"),
                            IngredientGroup.of("minecraft:sugar", "minecraft:sugar_cane")
                    ),
                    "puebloyparamo:aguapanela_caliente",
                    1
            ),
            new RecipeSpec(
                    "sancocho",
                    List.of(
                            IngredientGroup.of("puebloyparamo:agua_pura"),
                            IngredientGroup.of("minecraft:beef", "minecraft:cooked_beef", "minecraft:chicken"),
                            IngredientGroup.of("minecraft:potato", "minecraft:baked_potato"),
                            IngredientGroup.of("minecraft:carrot")
                    ),
                    "puebloyparamo:sancocho",
                    1
            )
    );

    @Test
    void matchesIngredientsRegardlessOfSlotOrder() {
        Optional<RecipeSpec> match = OllaLogic.findMatchingRecipe(
                List.of("minecraft:sugar_cane", "", "puebloyparamo:agua_pura", ""),
                SAMPLE_RECIPES,
                "",
                0,
                16
        );
        assertTrue(match.isPresent());
        assertEquals("aguapanela", match.get().recipeId());
    }

    @Test
    void rejectsExtraUnrelatedIngredients() {
        Optional<RecipeSpec> match = OllaLogic.findMatchingRecipe(
                List.of("minecraft:sugar", "puebloyparamo:agua_pura", "minecraft:cobblestone", ""),
                SAMPLE_RECIPES,
                "",
                0,
                16
        );
        assertTrue(match.isEmpty());
    }

    @Test
    void enforcesOutputStackCompatibilityAndCapacity() {
        List<String> sancochoInputs = List.of(
                "minecraft:carrot", "minecraft:potato", "minecraft:cooked_beef", "puebloyparamo:agua_pura"
        );
        assertTrue(OllaLogic.findMatchingRecipe(
                sancochoInputs, SAMPLE_RECIPES, "puebloyparamo:sancocho", 15, 16
        ).isPresent());
        assertTrue(OllaLogic.findMatchingRecipe(
                sancochoInputs, SAMPLE_RECIPES, "puebloyparamo:sancocho", 16, 16
        ).isEmpty());
        assertTrue(OllaLogic.findMatchingRecipe(
                sancochoInputs, SAMPLE_RECIPES, "puebloyparamo:ajiaco", 1, 16
        ).isEmpty());
    }

    @Test
    void returnsExpectedContainerLeftovers() {
        assertEquals(Optional.of("minecraft:glass_bottle"), OllaLogic.leftoverContainerFor("puebloyparamo:agua_pura"));
        assertEquals(Optional.of("minecraft:bucket"), OllaLogic.leftoverContainerFor("minecraft:milk_bucket"));
        assertTrue(OllaLogic.leftoverContainerFor("minecraft:potato").isEmpty());
    }
}
