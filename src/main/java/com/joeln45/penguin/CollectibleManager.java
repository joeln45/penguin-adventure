package com.joeln45.penguin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.Sprite;

/**
 * Owns the collectible stars: spawning per level, animating, drawing, and
 * detecting pickup against the player.
 *
 * @author Joel Nirmal
 */
public final class CollectibleManager {

    private final List<Sprite> stars = new ArrayList<>();
    private Animation starAnim;
    private int starsCollected;

    /** Spawn the stars for the given level (clears any previous ones). */
    public void loadLevel(int level) {
        stars.clear();
        starsCollected = 0;
        starAnim = new Animation();
        starAnim.loadAnimationFromSheet("images/star.png", 5, 1, 200);

        if (level == 1) {
            addStar(275, 115);
            addStar(900, 400);
            addStar(1620, 370);
        } else if (level == 2) {
            addStar(1020, 300);
            addStar(1050, 70);
            addStar(1380, 375);
        } else if (level == 3) {
            addStar(960, 224);    // easy — centre of long row-8 mid platform
            addStar(384, 160);    // medium — centre of left row-6 platform
            addStar(1184, 160);   // hard — centre of right row-6 platform
        }
    }

    public void resetCollectedCount() { starsCollected = 0; }

    public int getStarsCollected() { return starsCollected; }

    public int total() { return 3; }

    public boolean allCollected() { return starsCollected >= total(); }

    /**
     * Per-frame animation + pickup detection. Plays the coin sound on pickup.
     * @return the number of stars picked up this frame
     */
    public int update(long elapsed, Sprite player) {
        int picked = 0;
        for (int i = stars.size() - 1; i >= 0; i--) {
            Sprite star = stars.get(i);
            star.update(elapsed);
            if (CollisionService.boundingBoxCollision(player, star)) {
                stars.remove(i);
                starsCollected++;
                picked++;
                AssetLoader.coinSound().start();
            }
        }
        return picked;
    }

    public void draw(Graphics2D g, int xo, int yo, boolean debug) {
        for (Sprite star : stars) {
            star.setOffsets(xo, yo);
            star.draw(g);
            if (debug) {
                g.setColor(Color.black);
                star.drawBoundingCircle(g);
            }
        }
    }

    private void addStar(float x, float y) {
        Sprite star = new Sprite(starAnim);
        star.setPosition(x, y);
        star.setScale(0.7f);
        star.show();
        stars.add(star);
    }
}
