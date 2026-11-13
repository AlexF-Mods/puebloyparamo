package io.github.alexf6.puebloyparamo.logic;

/**
 * Reglas puras para descanso nocturno/en tormenta mediante Hamaca sin alterar el spawn del jugador.
 */
public final class HammockLogic {

    public static final long NIGHT_START_TICK = 12541L;
    public static final long NIGHT_END_TICK = 23458L;
    public static final long DAY_LENGTH_TICKS = 24000L;

    private HammockLogic() {
    }

    public enum RestCheckResult {
        ALLOW,
        WRONG_DIMENSION,
        NOT_NIGHT_OR_STORM,
        MONSTERS_NEARBY
    }

    /**
     * Evalúa si el jugador puede descansar en la hamaca bajo las condiciones actuales del mundo.
     */
    public static RestCheckResult evaluateRest(
            boolean overworldDimension,
            long totalDayTime,
            boolean thundering,
            boolean monstersNearby
    ) {
        if (!overworldDimension) {
            return RestCheckResult.WRONG_DIMENSION;
        }
        if (monstersNearby) {
            return RestCheckResult.MONSTERS_NEARBY;
        }
        long timeOfDay = Math.floorMod(totalDayTime, DAY_LENGTH_TICKS);
        boolean night = timeOfDay >= NIGHT_START_TICK && timeOfDay <= NIGHT_END_TICK;
        if (!night && !thundering) {
            return RestCheckResult.NOT_NIGHT_OR_STORM;
        }
        return RestCheckResult.ALLOW;
    }

    /**
     * Calcula el tick absoluto del amanecer del día siguiente al descanso.
     */
    public static long computeNextDawnTick(long currentDayTime) {
        long safeTime = Math.max(0L, currentDayTime);
        return ((safeTime / DAY_LENGTH_TICKS) + 1L) * DAY_LENGTH_TICKS;
    }
}
