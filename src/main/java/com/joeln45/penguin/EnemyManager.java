package com.joeln45.penguin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.Sprite;
import com.joeln45.penguin.engine.TileMap;

/**
 * Owns the patrolling-dino enemies: spawning per level, movement (including
 * turn-around on wall hit), drawing, and detecting hits against the player.
 *
 * @author Joel Nirmal
 */
public final class EnemyManager {

    private static final float ENEMY_SPEED = 0.05f;

    private final List<Sprite> enemies = new ArrayList<>();
    private Animation enemyAnim;
    private boolean[] movingRight;

    /** Spawn the enemies for the given level (clears any previous ones). */
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
            addEnemy(384, 160);   // patrols the row-6 left long platform
            addEnemy(1184, 160);  // patrols the row-6 right long platform
            addEnemy(1050, 224);  // patrols the row-8 long platform
        }

        movingRight = new boolean[enemies.size()];
        for (int i = 0; i < movingRight.length; i++) movingRight[i] = true;
    }

    /**
     * Per-frame enemy update: animation, wall-turn-around, player hit detection.
     * The hit enemy is hidden so the same one can't damage the player twice in
     * a row. Returns true if the player was struck this frame.
     */
    public boolean update(long elapsed, Sprite player, TileMap tmap) {
        boolean playerHit = false;
        for (int i = 0; i < enemies.size(); i++) {
            Sprite enemy = enemies.get(i);
            if (!enemy.isVisible()) continue;

            enemy.update(elapsed);

            if (CollisionService.checkEnemyTileCollision(enemy, tmap)) {
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
