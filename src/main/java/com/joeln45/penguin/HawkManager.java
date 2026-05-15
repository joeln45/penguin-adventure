package com.joeln45.penguin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.Sprite;

/**
 * Hawks float through the air and chase the player once they're within
 * VISION_RANGE pixels. Outside that range they hover in place.
 */
public final class HawkManager {

    private static final float HAWK_SPEED  = 0.08f;
    private static final float VISION_RANGE = 600f;
    private static final int   FRAME_COUNT = 4;
    private static final int   FRAME_MS    = 150;

    private final List<Sprite> hawks = new ArrayList<>();
    private Animation hawkAnim;

    public void loadLevel(int level) {
        hawks.clear();
        hawkAnim = new Animation();
        hawkAnim.loadAnimationFromSheet("images/landbird.png", FRAME_COUNT, 1, FRAME_MS);

        if (level == 1) {
            spawnAt(1300, 130);
        } else if (level == 2) {
            spawnAt(1400, 100);
        } else if (level == 3) {
            spawnAt(1500, 100);
        }
    }

    /** Returns true if the player got hit this frame. */
    public boolean update(long elapsed, Sprite player) {
        boolean playerHit = false;
        for (Sprite hawk : hawks) {
            if (!hawk.isVisible()) continue;
            hawk.update(elapsed);

            float dx = (player.getX() + player.getWidth() / 2f) - (hawk.getX() + hawk.getWidth() / 2f);
            float dy = (player.getY() + player.getHeight() / 2f) - (hawk.getY() + hawk.getHeight() / 2f);
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist > 0 && dist < VISION_RANGE) {
                // chase: unit vector toward the player * HAWK_SPEED
                hawk.setSpeedX(HAWK_SPEED * dx / dist);
                hawk.setVelocityY(HAWK_SPEED * dy / dist);
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
