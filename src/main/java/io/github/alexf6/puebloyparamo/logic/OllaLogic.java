package io.github.alexf6.puebloyparamo.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Reglas puras de emparejamiento para la Olla de barro (sin dependencias de tipos de Minecraft)
 * para permitir pruebas unitarias rápidas en JUnit 5.
 */
public final class OllaLogic {

    private OllaLogic() {
    }

    /**
     * Predicado de ingrediente que acepta uno o más IDs de ítems equivalentes
     * (por ejemplo, carne cruda o cocida).
     */
    public record IngredientGroup(Set<String> acceptedIds) {
        public IngredientGroup {
            acceptedIds = Set.copyOf(Objects.requireNonNull(acceptedIds, "acceptedIds"));
            if (acceptedIds.isEmpty()) {
                throw new IllegalArgumentException("IngredientGroup debe tener al menos un ítem.");
            }
        }

        public static IngredientGroup of(String... itemIds) {
            return new IngredientGroup(Set.of(itemIds));
        }

        public boolean matches(String itemId) {
            return itemId != null && acceptedIds.contains(itemId);
        }
    }

    /**
     * Definición pura de receta de olla: ingredientes sin orden fijo, ítem resultante y cantidad.
     */
    public record RecipeSpec(
            String recipeId,
            List<IngredientGroup> requirements,
            String resultItemId,
            int resultCount
    ) {
        public RecipeSpec {
            recipeId = Objects.requireNonNull(recipeId, "recipeId");
            requirements = List.copyOf(Objects.requireNonNull(requirements, "requirements"));
            resultItemId = Objects.requireNonNull(resultItemId, "resultItemId");
            if (requirements.isEmpty() || requirements.size() > 4) {
                throw new IllegalArgumentException("Una receta de olla requiere entre 1 y 4 ingredientes.");
            }
            if (resultCount < 1) {
                throw new IllegalArgumentException("resultCount debe ser >= 1.");
            }
        }
    }

    /**
     * Resultado de buscar una receta válida para los 4 slots de entrada y el estado del slot de salida.
     */
    public static Optional<RecipeSpec> findMatchingRecipe(
            List<String> inputSlotIds,
            List<RecipeSpec> recipes,
            String currentOutputId,
            int currentOutputCount,
            int maxStackSize
    ) {
        List<String> nonEmptyInputs = new ArrayList<>();
        for (String id : inputSlotIds) {
            if (id != null && !id.isEmpty() && !"minecraft:air".equals(id)) {
                nonEmptyInputs.add(id);
            }
        }
        if (nonEmptyInputs.isEmpty()) {
            return Optional.empty();
        }

        for (RecipeSpec spec : recipes) {
            if (matchesUnordered(nonEmptyInputs, spec.requirements())
                    && canFitOutput(currentOutputId, currentOutputCount, spec.resultItemId(), spec.resultCount(), maxStackSize)) {
                return Optional.of(spec);
            }
        }
        return Optional.empty();
    }

    /**
     * Comprueba si la lista exacta de ítems presentes satisface los requisitos en cualquier orden.
     */
    public static boolean matchesUnordered(List<String> presentIds, List<IngredientGroup> requirements) {
        if (presentIds.size() != requirements.size()) {
            return false;
        }
        boolean[] used = new boolean[requirements.size()];
        return backtrackMatch(presentIds, 0, requirements, used);
    }

    private static boolean backtrackMatch(
            List<String> presentIds,
            int idx,
            List<IngredientGroup> requirements,
            boolean[] used
    ) {
        if (idx == presentIds.size()) {
            return true;
        }
        String itemId = presentIds.get(idx);
        for (int r = 0; r < requirements.size(); r++) {
            if (!used[r] && requirements.get(r).matches(itemId)) {
                used[r] = true;
                if (backtrackMatch(presentIds, idx + 1, requirements, used)) {
                    return true;
                }
                used[r] = false;
            }
        }
        return false;
    }

    public static boolean canFitOutput(
            String currentOutputId,
            int currentOutputCount,
            String resultItemId,
            int resultCount,
            int maxStackSize
    ) {
        if (currentOutputId == null || currentOutputId.isEmpty() || "minecraft:air".equals(currentOutputId) || currentOutputCount <= 0) {
            return resultCount <= maxStackSize;
        }
        return currentOutputId.equals(resultItemId) && currentOutputCount + resultCount <= maxStackSize;
    }

    /**
     * Indica qué recipiente devuelve un ingrediente líquido al consumirse en la olla.
     */
    public static Optional<String> leftoverContainerFor(String consumedItemId) {
        if ("puebloyparamo:agua_pura".equals(consumedItemId)
                || "minecraft:potion".equals(consumedItemId)
                || "minecraft:honey_bottle".equals(consumedItemId)
                || "mitosyfauna:cafe_campesino".equals(consumedItemId)) {
            return Optional.of("minecraft:glass_bottle");
        }
        if ("minecraft:milk_bucket".equals(consumedItemId) || "minecraft:water_bucket".equals(consumedItemId)) {
            return Optional.of("minecraft:bucket");
        }
        return Optional.empty();
    }
}
