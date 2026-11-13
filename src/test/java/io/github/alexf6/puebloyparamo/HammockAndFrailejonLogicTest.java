package io.github.alexf6.puebloyparamo;

import io.github.alexf6.puebloyparamo.logic.FrailejonLogic;
import io.github.alexf6.puebloyparamo.logic.HammockLogic;
import io.github.alexf6.puebloyparamo.logic.HammockLogic.RestCheckResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HammockAndFrailejonLogicTest {

    @Test
    void hammockAllowsNightOrStormOnlyInOverworldWithoutMonsters() {
        assertEquals(RestCheckResult.ALLOW, HammockLogic.evaluateRest(true, 13000L, false, false));
        assertEquals(RestCheckResult.ALLOW, HammockLogic.evaluateRest(true, 5000L, true, false));
        assertEquals(RestCheckResult.NOT_NIGHT_OR_STORM, HammockLogic.evaluateRest(true, 5000L, false, false));
        assertEquals(RestCheckResult.MONSTERS_NEARBY, HammockLogic.evaluateRest(true, 15000L, false, true));
        assertEquals(RestCheckResult.WRONG_DIMENSION, HammockLogic.evaluateRest(false, 15000L, false, false));
    }

    @Test
    void nextDawnAdvancesToMorningOfNextDayTick() {
        assertEquals(24000L, HammockLogic.computeNextDawnTick(15000L));
        assertEquals(72000L, HammockLogic.computeNextDawnTick(62000L));
    }

    @Test
    void frailejonWaterRechargeAndMistRespondToAltitudeAndRain() {
        assertFalse(FrailejonLogic.shouldRechargeWater(true, true, 120, 0.01));
        assertTrue(FrailejonLogic.shouldRechargeWater(false, true, 110, 0.80));
        assertFalse(FrailejonLogic.shouldRechargeWater(false, false, 64, 0.80));

        assertEquals(0, FrailejonLogic.computeMistParticleCount(false, true, 130, true));
        assertEquals(3, FrailejonLogic.computeMistParticleCount(true, true, 110, false));
        assertEquals(1, FrailejonLogic.computeMistParticleCount(true, false, 70, false));
    }
}
