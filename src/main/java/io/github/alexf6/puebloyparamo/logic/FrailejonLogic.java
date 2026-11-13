package io.github.alexf6.puebloyparamo.logic;

/**
 * Utilidades puras para mecánicas del frailejón: recarga de agua pura en páramos
 * e intensidad de neblina según altura y humedad climática.
 */
public final class FrailejonLogic {

    private FrailejonLogic() {
    }

    /**
     * Determina si la planta recupera su reserva de agua pura en este tick aleatorio.
     * En lluvia o altura de páramo (>= 100) la condensación es más rápida.
     */
    public static boolean shouldRechargeWater(boolean currentlyMoist, boolean raining, int blockY, double randomUnit) {
        if (currentlyMoist) {
            return false;
        }
        double chance = 0.25;
        if (raining) {
            chance += 0.45;
        }
        if (blockY >= 96) {
            chance += 0.20;
        }
        return randomUnit >= 0.0 && randomUnit < Math.min(0.95, chance);
    }

    /**
     * Cantidad de partículas de bruma ambiental por tick de animación alrededor de un frailejón.
     */
    public static int computeMistParticleCount(boolean mistEnabled, boolean raining, int blockY, boolean nightOrDawn) {
        if (!mistEnabled) {
            return 0;
        }
        int base = blockY >= 95 ? 2 : 1;
        if (raining || nightOrDawn) {
            base += 1;
        }
        return base;
    }
}
