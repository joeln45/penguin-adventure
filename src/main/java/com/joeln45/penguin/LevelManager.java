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
    public static final int LAST_LEVEL = 2;

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

    /** Load the very first level (called on a new game). */
    public void loadFirstLevel(CollectibleManager collectibles, EnemyManager enemies,
                               PowerupManager powerups) {
        currentLevel = FIRST_LEVEL;
        tmap.loadMap("maps", "map1.txt");
        setBoundaries(20, 2035, 65, 480);
        positionIgloo();
        collectibles.loadLevel(currentLevel);
        enemies.loadLevel(currentLevel);
        powerups.loadLevel(currentLevel);
        powerups.deactivate();
    }

    /** Advance to the next level (currently a fixed level-2 transition). */
    public void loadNextLevel(CollectibleManager collectibles, EnemyManager enemies,
                              PowerupManager powerups) {
        currentLevel++;
        tmap.loadMap("maps", "map" + currentLevel + ".txt");
        setBoundaries(20, 2035, 65, 480);
        positionIgloo();
        collectibles.loadLevel(currentLevel);
        enemies.loadLevel(currentLevel);
        powerups.loadLevel(currentLevel);
    }

    /** Reload the current level's collectibles/enemies/igloo without bumping the counter. */
    public void reloadCurrent(CollectibleManager collectibles, EnemyManager enemies,
                              PowerupManager powerups) {
        positionIgloo();
        collectibles.loadLevel(currentLevel);
        enemies.loadLevel(currentLevel);
        powerups.loadLevel(currentLevel);
        powerups.deactivate();
    }

    private void positionIgloo() {
        if (currentLevel == 1) {
            igloo.setPosition(1920, 160);
        } else if (currentLevel == 2) {
            igloo.setPosition(1910, 319);
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
