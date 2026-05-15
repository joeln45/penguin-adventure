package com.joeln45.penguin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.Sprite;
import com.joeln45.penguin.engine.TileMap;

/** Patrolling dino enemies. They walk left/right and turn around at walls. */
public final class EnemyManager {

    private static final float ENEMY_SPEED = 0.05f;

    private final List<Sprite> enemies = new ArrayList<>();
    private Animation enemyAnim;
    private boolean[] movingRight;

    public void loadLevel(int level) {
        enemies.clear();
        enemyAnim = new Animation();
        enemyAnim.loadAnimationFromSheet("images/enemy.png", 7, 1, 150);

        if (level == 1) {
            addEnemy(550, 452);
            addEnemy(400, 163);
            addEnemy(1350, 228);
        } else if (level == 2) {
            addEnemy(600, 355);
            addEnemy(1000, 130);
            addEnemy(1500, 451);
        } else if (level == 3) {
            // left platform, long mid platform, ground
            addEnemy(384, 160);
            addEnemy(1050, 224);
            addEnemy(550, 452);
        }

        movingRight = new boolean[enemies.size()];
        for (int i = 0; i < movingRight.length; i++) movingRight[i] = true;
    }

    /**
     * Returns true if the player was hit this frame. The enemy that hit is
     * hidden so it can't hit again on the next frame.
     */
    public boolean update(long elapsed, Sprite player, TileMap tmap) {
        boolean playerHit = false;
        for (int i = 0; i < enemies.size(); i++) {
            Sprite enemy = enemies.get(i);
            if (!enemy.isVisible()) continue;

            enemy.update(elapsed);

            if (CollisionService.checkEnemyTileCollision(enemy, tmap, elapsed)) {
                enemy.setSpeedX(-enemy.getSpeedX());
                movingRight[i] = !movingRight[i];
                enemy.setScale(movingRight[i] ? 1.0f : -1.0f, 1.0f);
            }

            if (CollisionService.boundingBoxCollision(player, enemy)) {
                playerHit = true;
                enemy.hide();
            }
        }
        return playerHit;
    }

    public void draw(Graphics2D g, int xo, int yo, boolean debug) {
        for (Sprite enemy : enemies) {
            enemy.setOffsets(xo, yo);
            enemy.drawTransformed(g);
            if (debug) {
                g.setColor(Color.red);
                enemy.drawBoundingBox(g);
            }
        }
    }

    private void addEnemy(float x, float y) {
        Sprite enemy = new Sprite(enemyAnim);
        enemy.setPosition(x, y);
        enemy.setSpeedX(ENEMY_SPEED);
        enemy.show();
        enemies.add(enemy);
    }
}
