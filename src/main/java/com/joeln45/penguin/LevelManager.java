package com.joeln45.penguin;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.GameCore;
import com.joeln45.penguin.engine.Sprite;
import com.joeln45.penguin.engine.TileMap;

/**
 * Tracks which level is active and owns the per-level world: tile map, igloo
 * goal sprite, and screen boundaries.
 *
 * <p>Coordinates with {@link CollectibleManager} and {@link EnemyManager} when
 * advancing levels, but does not own them — they are passed in for reloading.
 *
 * @author Joel Nirmal
 */
public final class LevelManager {

    public static final int FIRST_LEVEL = 1;
    public static final int LAST_LEVEL = 3;

    private final TileMap tmap = new TileMap();
    private final Sprite igloo;

    private int currentLevel = FIRST_LEVEL;
    private float leftBoundary  = 0;
    private float rightBoundary = 1200;
    private float topBoundary   = 43;
    private float bottomBoundary = 505;

    public LevelManager(GameCore gameCore) {
        Animation iglooAnim = new Animation();
        iglooAnim.addFrame(gameCore.loadImage("images/igloo.png"), 1000);
        this.igloo = new Sprite(iglooAnim);
    }

    public TileMap tileMap()      { return tmap; }
    public Sprite igloo()         { return igloo; }
    public int currentLevel()     { return currentLevel; }
    public float leftBoundary()   { return leftBoundary; }
    public float rightBoundary()  { return rightBoundary; }
    public float topBoundary()    { return topBoundary; }
    public float bottomBoundary() { return bottomBoundary; }
    public boolean isLastLevel()  { return currentLevel >= LAST_LEVEL; }

    /** Load a specific level number, resetting all per-level state. */
    public void loadLevel(int level, CollectibleManager collectibles, EnemyManager enemies,
                          PowerupManager powerups, HawkManager hawks) {
        currentLevel = level;
        tmap.loadMap("maps", "map" + level + ".txt");
        setBoundaries(20, 2035, 65, 480);
        positionIgloo();
        collectibles.loadLevel(level);
        enemies.loadLevel(level);
        powerups.loadLevel(level);
        powerups.deactivate();
        hawks.loadLevel(level);
    }

    /** Load the very first level (called on a new game). */
    public void loadFirstLevel(CollectibleManager collectibles, EnemyManager enemies,
                               PowerupManager powerups, HawkManager hawks) {
        loadLevel(FIRST_LEVEL, collectibles, enemies, powerups, hawks);
    }

    /** Advance to the next level. */
    public void loadNextLevel(CollectibleManager collectibles, EnemyManager enemies,
                              PowerupManager powerups, HawkManager hawks) {
        loadLevel(currentLevel + 1, collectibles, enemies, powerups, hawks);
    }

    /** Reload the current level (used by the Play-Again button). */
    public void reloadCurrent(CollectibleManager collectibles, EnemyManager enemies,
                              PowerupManager powerups, HawkManager hawks) {
        loadLevel(currentLevel, collectibles, enemies, powerups, hawks);
    }

    private void positionIgloo() {
        if (currentLevel == 1) {
            igloo.setPosition(1920, 160);
        } else if (currentLevel == 2) {
            igloo.setPosition(1910, 319);
        } else if (currentLevel == 3) {
            igloo.setPosition(1920, 160);  // top of the right-side stair, like level 1
        }
        igloo.show();
    }

    private void setBoundaries(float left, float right, float top, float bottom) {
        this.leftBoundary = left;
        this.rightBoundary = right;
        this.topBoundary = top;
        this.bottomBoundary = bottom;
    }
}
