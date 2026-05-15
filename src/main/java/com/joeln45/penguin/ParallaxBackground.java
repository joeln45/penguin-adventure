package com.joeln45.penguin;

import java.awt.Graphics2D;
import java.awt.Image;

/**
 * Four-layer parallax background. Each layer has its own scroll speed (the
 * back layers move slower so you get a depth effect) and tracks two copies
 * (x1, x2) so it can wrap around the viewport without a visible seam.
 */
public final class ParallaxBackground {

    private static final float[] LAYER_SPEEDS = {0.01f, 0.02f, 0.04f, 0.08f};

    private final Image[] layers;
    private final float[] x1;
    private final float[] x2;
    private final int screenWidth;
    private final int screenHeight;

    public ParallaxBackground(Image[] layers, int screenWidth, int screenHeight) {
        if (layers.length != 4) {
            throw new IllegalArgumentException("Expected 4 parallax layers, got " + layers.length);
        }
        this.layers = layers;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.x1 = new float[4];
        this.x2 = new float[4];
        for (int i = 0; i < 4; i++) {
            x1[i] = 0;
            x2[i] = screenWidth;
        }
    }

    public void update(long elapsed, boolean moveRight, boolean moveLeft, boolean blocked) {
        for (int i = 0; i < 4; i++) {
            if (!blocked) {
                if (moveRight) {
                    x1[i] -= LAYER_SPEEDS[i] * elapsed;
                    x2[i] -= LAYER_SPEEDS[i] * elapsed;
                } else if (moveLeft) {
                    x1[i] += LAYER_SPEEDS[i] * elapsed;
                    x2[i] += LAYER_SPEEDS[i] * elapsed;
                }
            }
            // wrap when one copy goes off-screen
            if (x1[i] + screenWidth <= 0) {
                x1[i] = x2[i] + screenWidth;
            } else if (x2[i] + screenWidth <= 0) {
                x2[i] = x1[i] + screenWidth;
            } else if (x1[i] >= screenWidth) {
                x1[i] = x2[i] - screenWidth;
            } else if (x2[i] >= screenWidth) {
                x2[i] = x1[i] - screenWidth;
            }
        }
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < 4; i++) {
            g.drawImage(layers[i], (int) x1[i], 0, screenWidth, screenHeight, null);
            g.drawImage(layers[i], (int) x2[i], 0, screenWidth, screenHeight, null);
        }
    }
}
