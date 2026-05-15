package com.joeln45.penguin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Tiny particle system, currently used for the dust puff when the player
 * lands. Particles are just coloured circles with velocity, gravity and an
 * alpha that fades to zero. They don't collide with anything.
 */
public final class ParticleManager {

    private static final int   DUST_COUNT      = 6;
    private static final long  DUST_LIFE_MS    = 400;
    private static final float DUST_GRAVITY    = 0.0005f;
    private static final Color DUST_COLOR      = new Color(170, 130, 80);

    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    public void reset() { particles.clear(); }

    /** Spawn a small puff of dust at the given world position. */
    public void spawnLandingDust(float x, float y) {
        for (int i = 0; i < DUST_COUNT; i++) {
            // random sideways drift + tiny upward kick
            float vx = (rng.nextFloat() - 0.5f) * 0.20f;
            float vy = -rng.nextFloat() * 0.10f;
            float radius = 3f + rng.nextFloat() * 2f;
            particles.add(new Particle(x, y, vx, vy, radius));
        }
    }

    public void update(long elapsed) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.ageMs += elapsed;
            if (p.ageMs >= DUST_LIFE_MS) {
                particles.remove(i);
                continue;
            }
            p.vy += DUST_GRAVITY * elapsed;
            p.x  += p.vx * elapsed;
            p.y  += p.vy * elapsed;
        }
    }

    public void draw(Graphics2D g, int xo, int yo) {
        for (Particle p : particles) {
            float t = 1f - (p.ageMs / (float) DUST_LIFE_MS); // fades from 1 to 0
            int alpha = Math.max(0, Math.min(255, (int) (t * 200)));
            g.setColor(new Color(DUST_COLOR.getRed(), DUST_COLOR.getGreen(),
                                 DUST_COLOR.getBlue(), alpha));
            int drawX = (int) (p.x + xo - p.radius);
            int drawY = (int) (p.y + yo - p.radius);
            int size  = (int) (p.radius * 2);
            g.fillOval(drawX, drawY, size, size);
        }
    }

    private static final class Particle {
        float x, y, vx, vy, radius;
        long ageMs;
        Particle(float x, float y, float vx, float vy, float radius) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.radius = radius;
        }
    }
}
