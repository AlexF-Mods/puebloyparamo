package io.github.alexf6.puebloyparamo.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Reglas puras de emparejamiento para la Olla de barro (sin dependencias de tipos de Minecraft)
 * para permitir pruebas unitarias rápidas en JUnit 5.
 */
public final class OllaLogic {

    public static final String AIR_ID = "minecraft:air";

    private static final Map<String, String> STANDARD_CONTAINERS = Map.of(
            "puebloyparamo:agua_pura", "minecraft:glass_bottle",
            "minecraft:potion", "minecraft:glass_bottle",
            "minecraft:honey_bottle", "minecraft:glass_bottle",
            "minecraft:milk_bucket", "minecraft:bucket",
            "minecraft:water_bucket", "minecraft:bucket"
    );

    private OllaLogic() {
    }

    private static boolean isEmptyId(String id) {
        return id == null || id.isEmpty() || AIR_ID.equals(id);
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
     * Estado actual de la ranura de salida para comprobar compatibilidad y apilado.
     */
    public record OutputSlotState(String itemId, int count, int maxStackSize) {
        public OutputSlotState {
            itemId = itemId == null ? "" : itemId;
            count = Math.max(0, count);
            maxStackSize = Math.max(1, maxStackSize);
        }

        public static OutputSlotState empty() {
            return new OutputSlotState("", 0, 64);
        }

        public boolean isEmpty() {
            return isEmptyId(itemId) || count <= 0;
        }
    }

    /**
     * Definición pura de receta de olla: ingredientes sin orden fijo, ítem resultante,
     * cantidad y mapa opcional de recipientes devueltos por ingrediente consumido.
     */
    public record RecipeSpec(
            String recipeId,
            List<IngredientGroup> requirements,
            String resultItemId,
            int resultCount,
            Map<String, String> containerLeftovers
    ) {
        public RecipeSpec {
            recipeId = Objects.requireNonNull(recipeId, "recipeId");
            requirements = List.copyOf(Objects.requireNonNull(requirements, "requirements"));
            resultItemId = Objects.requireNonNull(resultItemId, "resultItemId");
            containerLeftovers = Map.copyOf(Objects.requireNonNull(containerLeftovers, "containerLeftovers"));
            if (requirements.isEmpty() || requirements.size() > 4) {
                throw new IllegalArgumentException("Una receta de olla requiere entre 1 y 4 ingredientes.");
            }
            if (resultCount < 1) {
                throw new IllegalArgumentException("resultCount debe ser >= 1.");
            }
        }

        public RecipeSpec(String recipeId, List<IngredientGroup> requirements, String resultItemId, int resultCount) {
            this(recipeId, requirements, resultItemId, resultCount, Map.of());
        }
    }

    /**
     * Resultado de buscar una receta válida para los 4 slots de entrada y el estado del slot de salida.
     */
    public static Optional<RecipeSpec> findMatchingRecipe(
            List<String> inputSlotIds,
            List<RecipeSpec> recipes,
            OutputSlotState outputState
    ) {
        List<String> nonEmptyInputs = new ArrayList<>();
        for (String id : inputSlotIds) {
            if (!isEmptyId(id)) {
                nonEmptyInputs.add(id);
            }
        }
        if (nonEmptyInputs.isEmpty()) {
            return Optional.empty();
        }

        for (RecipeSpec spec : recipes) {
            if (matchesUnordered(nonEmptyInputs, spec.requirements())
                    && canFitOutput(outputState, spec.resultItemId(), spec.resultCount())) {
                return Optional.of(spec);
            }
        }
        return Optional.empty();
    }

    public static Optional<RecipeSpec> findMatchingRecipe(
            List<String> inputSlotIds,
            List<RecipeSpec> recipes,
            String currentOutputId,
            int currentOutputCount,
            int maxStackSize
    ) {
        return findMatchingRecipe(
                inputSlotIds,
                recipes,
                new OutputSlotState(currentOutputId, currentOutputCount, maxStackSize)
        );
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

    public static boolean canFitOutput(OutputSlotState currentOutput, String resultItemId, int resultCount) {
        if (currentOutput.isEmpty()) {
            return resultCount <= currentOutput.maxStackSize();
        }
        return currentOutput.itemId().equals(resultItemId)
                && currentOutput.count() + resultCount <= currentOutput.maxStackSize();
    }

    /**
     * Indica qué recipiente devuelve un ingrediente líquido al consumirse en la olla.
     */
    public static Optional<String> leftoverContainerFor(RecipeSpec recipe, String consumedItemId) {
        if (recipe != null) {
            String recipeSpecific = recipe.containerLeftovers().get(consumedItemId);
            if (recipeSpecific != null) {
                return Optional.of(recipeSpecific);
            }
        }
        return leftoverContainerFor(consumedItemId);
    }

    public static Optional<String> leftoverContainerFor(String consumedItemId) {
        return Optional.ofNullable(STANDARD_CONTAINERS.get(consumedItemId));
    }
}
