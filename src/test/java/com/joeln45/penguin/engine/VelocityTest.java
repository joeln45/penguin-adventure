package com.joeln45.penguin.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Velocity}.
 *
 * <p>Velocity stores a (speed, angle°) polar vector and exposes (dx, dy) in
 * cartesian form. These tests cover construction, mutation, and composition.
 */
class VelocityTest {

    private static final double EPS = 1e-9;

    @Test
    @DisplayName("default constructor yields zero velocity")
    void defaultIsZero() {
        Velocity v = new Velocity();
        assertEquals(0.0, v.getSpeed(), EPS);
        assertEquals(0.0, v.getdx(), EPS);
        assertEquals(0.0, v.getdy(), EPS);
    }

    @Test
    @DisplayName("0° points along +x")
    void zeroDegreesPointsAlongPositiveX() {
        Velocity v = new Velocity(10, 0);
        assertEquals(10.0, v.getdx(), EPS);
        assertEquals(0.0, v.getdy(), EPS);
    }

    @Test
    @DisplayName("90° points along +y (screen-space convention)")
    void ninetyDegreesPointsAlongPositiveY() {
        Velocity v = new Velocity(10, 90);
        assertEquals(0.0, v.getdx(), EPS);
        assertEquals(10.0, v.getdy(), EPS);
    }

    @Test
    @DisplayName("180° points along -x")
    void oneEightyPointsAlongNegativeX() {
        Velocity v = new Velocity(5, 180);
        assertEquals(-5.0, v.getdx(), EPS);
        assertEquals(0.0, v.getdy(), EPS);
    }

    @Test
    @DisplayName("setSpeed scales dx and dy while preserving direction")
    void setSpeedScalesComponents() {
        Velocity v = new Velocity(10, 0);
        v.setSpeed(7);
        assertEquals(7.0, v.getdx(), EPS);
        assertEquals(0.0, v.getdy(), EPS);
        assertEquals(7.0, v.getSpeed(), EPS);
    }

    @Test
    @DisplayName("setAngle re-projects components for the new angle")
    void setAngleReprojects() {
        Velocity v = new Velocity(10, 0);
        v.setAngle(90);
        assertEquals(0.0, v.getdx(), EPS);
        assertEquals(10.0, v.getdy(), EPS);
        assertEquals(90.0, v.getAngle(), EPS);
    }

    @Test
    @DisplayName("adding a perpendicular velocity composes pythagorean magnitude")
    void addPerpendicularCombines() {
        Velocity v = new Velocity(3, 0);  // (3, 0)
        Velocity w = new Velocity(4, 90); // (0, 4)
        v.add(w);
        // Resultant magnitude should be sqrt(3² + 4²) = 5
        assertEquals(5.0, v.getSpeed(), 1e-9);
        assertEquals(3.0, v.getdx(), 1e-9);
        assertEquals(4.0, v.getdy(), 1e-9);
    }

    @Test
    @DisplayName("subtracting same velocity yields zero magnitude")
    void subtractSameYieldsZero() {
        Velocity v = new Velocity(10, 45);
        Velocity w = new Velocity(10, 45);
        v.subtract(w);
        assertEquals(0.0, v.getdx(), 1e-9);
        assertEquals(0.0, v.getdy(), 1e-9);
    }
}
