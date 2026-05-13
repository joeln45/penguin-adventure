package com.joeln45.penguin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.Sprite;

/**
 * Owns the double-jump powerup pickups and tracks the active-effect timer.
 *
 * <p>One powerup per level at a fixed position. Picking it up grants the
 * player a single extra mid-air jump for the next {@link #DURATION_MS}
 * milliseconds. The sprite is drawn programmatically so the feature ships
 * without needing any new image asset.
 *
 * @author Joel Nirmal
 */
public final class PowerupManager {

    /** How long the double-jump effect lasts after pickup. */
    public static final long DURATION_MS = 10_000;

    private static final int SPRITE_SIZE = 32;

    private final BufferedImage powerupImage = createPowerupImage();
    private final List<Sprite> powerups = new ArrayList<>();
    private long doubleJumpExpiresAt = 0;

    /** Spawn the powerup(s) for the given level. */
    public void loadLevel(int level) {
        powerups.clear();
        // Positions chosen to sit in open areas and be reachable with a single
        // jump from ground level (~85px max jump height).
        if (level == 1) {
            spawnAt(450, 400);
        } else if (level == 2) {
            spawnAt(1200, 400);
        } else if (level == 3) {
            spawnAt(650, 415);
        }
    }

    /** Clear any active timer (called on game reset / new game). */
    public void deactivate() {
        doubleJumpExpiresAt = 0;
    }

    /** Per-frame update: animate, detect pickup, activate timer on contact. */
    public void update(long elapsed, Sprite player) {
        for (int i = powerups.size() - 1; i >= 0; i--) {
            Sprite p = powerups.get(i);
            p.update(elapsed);
            if (CollisionService.boundingBoxCollision(player, p)) {
                powerups.remove(i);
                doubleJumpExpiresAt = System.currentTimeMillis() + DURATION_MS;
                AssetLoader.coinSound().start();
            }
        }
    }

    public boolean isDoubleJumpActive() {
        return System.currentTimeMillis() < doubleJumpExpiresAt;
    }

    /** Milliseconds remaining (clamped at 0). */
    public long remainingMs() {
        return Math.max(0, doubleJumpExpiresAt - System.currentTimeMillis());
    }

    public void draw(Graphics2D g, int xo, int yo) {
        for (Sprite p : powerups) {
            p.setOffsets(xo, yo);
            p.draw(g);
        }
    }

    private void spawnAt(float x, float y) {
        Animation anim = new Animation();
        anim.addFrame(powerupImage, 1000);
        Sprite s = new Sprite(anim);
        s.setPosition(x, y);
        s.show();
        powerups.add(s);
    }

    /** Draws a glowing yellow disc with a "2x" label, used as the pickup sprite. */
    private static BufferedImage createPowerupImage() {
        BufferedImage img = new BufferedImage(SPRITE_SIZE, SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Outer glow
            g.setColor(new Color(255, 220, 50, 120));
            g.fillOval(0, 0, SPRITE_SIZE, SPRITE_SIZE);

            // Inner disc
            g.setColor(new Color(255, 195, 0));
            g.fillOval(4, 4, SPRITE_SIZE - 8, SPRITE_SIZE - 8);

            // Border
            g.setColor(new Color(180, 120, 0));
            g.setStroke(new BasicStroke(2));
            g.drawOval(4, 4, SPRITE_SIZE - 8, SPRITE_SIZE - 8);

            // "2x" text
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            String text = "2x";
            int tx = (SPRITE_SIZE - fm.stringWidth(text)) / 2;
            int ty = (SPRITE_SIZE + fm.getAscent()) / 2 - 3;
            g.drawString(text, tx, ty);
        } finally {
            g.dispose();
        }
        return img;
    }
}
