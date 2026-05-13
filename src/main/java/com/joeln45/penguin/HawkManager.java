package com.joeln45.penguin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.Sprite;

/**
 * Owns the airborne hawk enemies. Hawks ignore gravity and pursue the player
 * along both axes when within {@link #VISION_RANGE} pixels (Euclidean distance).
 * Outside their vision they hold position.
 *
 * <p>Functionally similar to {@link EnemyManager} but with chase behaviour
 * instead of wall-bounded patrol, so kept as a separate class for clarity.
 *
 * @author Joel Nirmal
 */
public final class HawkManager {

    private static final float HAWK_SPEED  = 0.08f;
    private static final float VISION_RANGE = 600f;
    private static final int   FRAME_COUNT = 4;
    private static final int   FRAME_MS    = 150;

    private final List<Sprite> hawks = new ArrayList<>();
    private Animation hawkAnim;

    /** Spawn the hawk(s) for the given level. */
    public void loadLevel(int level) {
        hawks.clear();
        hawkAnim = new Animation();
        hawkAnim.loadAnimationFromSheet("images/landbird.png", FRAME_COUNT, 1, FRAME_MS);

        if (level == 1) {
            spawnAt(1300, 130);
        } else if (level == 2) {
            spawnAt(1400, 100);
        }
    }

    /**
     * Per-frame hawk update: chase + collision. Returns true if the player
     * was hit this frame. The struck hawk is hidden so it can't damage twice
     * in a row.
     */
    public boolean update(long elapsed, Sprite player) {
        boolean playerHit = false;
        for (Sprite hawk : hawks) {
            if (!hawk.isVisible()) continue;
            hawk.update(elapsed);

            // Vector from hawk to player (sprite-center delta)
            float dx = (player.getX() + player.getWidth() / 2f) - (hawk.getX() + hawk.getWidth() / 2f);
            float dy = (player.getY() + player.getHeight() / 2f) - (hawk.getY() + hawk.getHeight() / 2f);
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist > 0 && dist < VISION_RANGE) {
                // Normalised pursuit vector scaled by hawk speed
                hawk.setSpeedX(HAWK_SPEED * dx / dist);
                hawk.setVelocityY(HAWK_SPEED * dy / dist);
                // Face the player horizontally
                hawk.setScale(dx >= 0 ? 1.0f : -1.0f, 1.0f);
            } else {
                hawk.setSpeedX(0);
                hawk.setVelocityY(0);
            }

            if (CollisionService.boundingBoxCollision(player, hawk)) {
                playerHit = true;
                hawk.hide();
            }
        }
        return playerHit;
    }

    public void draw(Graphics2D g, int xo, int yo, boolean debug) {
        for (Sprite hawk : hawks) {
            hawk.setOffsets(xo, yo);
            hawk.drawTransformed(g);
            if (debug) {
                g.setColor(Color.magenta);
                hawk.drawBoundingBox(g);
            }
        }
    }

    private void spawnAt(float x, float y) {
        Sprite hawk = new Sprite(hawkAnim);
        hawk.setPosition(x, y);
        hawk.show();
        hawks.add(hawk);
    }
}
