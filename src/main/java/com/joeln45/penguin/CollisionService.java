package com.joeln45.penguin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.joeln45.penguin.engine.Sprite;
import com.joeln45.penguin.engine.Tile;
import com.joeln45.penguin.engine.TileMap;

/**
 * Stateless collision math for the platformer.
 *
 * <p>Methods are pure with one exception: {@link #checkTileCollision(Sprite, TileMap)}
 * mutates the sprite's position and velocity to resolve the collision (the standard
 * "detect + respond" pattern). It does <em>not</em> mutate any game state — the caller
 * receives a {@link TileCollisionResult} and applies the consequences itself.
 *
 * @author Joel Nirmal
 */
public final class CollisionService {

    private CollisionService() {}

    /** Outcome of a per-frame tile-collision check. */
    public static final class TileCollisionResult {
        public final List<Tile> collidedTiles;
        public final boolean horizontalCollision;
        /** Set when the sprite was actively moving down and contacted a tile this frame. */
        public final boolean landedOnGround;
        /** Set when the sprite was already standing on a tile (no vertical motion). */
        public final boolean onGround;

        TileCollisionResult(List<Tile> tiles, boolean hCol, boolean landed, boolean onGround) {
            this.collidedTiles = Collections.unmodifiableList(tiles);
            this.horizontalCollision = hCol;
            this.landedOnGround = landed;
            this.onGround = onGround;
        }
    }

    /**
     * Axis-aligned bounding-box overlap test between two sprites, with a small
     * hitbox margin to feel less punishing.
     */
    public static boolean boundingBoxCollision(Sprite s1, Sprite s2) {
        float s1x = s1.getX();
        float s1y = s1.getY();
        float s1w = s1.getWidth();
        float s1h = s1.getHeight();

        float s2x = s2.getX();
        float s2y = s2.getY();
        float s2w = s2.getWidth();
        float s2h = s2.getHeight();

        float hitboxMargin = 5.0f;

        return (s1x + hitboxMargin < s2x + s2w - hitboxMargin &&
                s1x + s1w - hitboxMargin > s2x + hitboxMargin &&
                s1y + hitboxMargin < s2y + s2h - hitboxMargin &&
                s1y + s1h - hitboxMargin > s2y + hitboxMargin);
    }

    /**
     * Checks whether the sprite, given its current horizontal velocity, would
     * collide with any solid tile on the side it is moving toward. Used for
     * enemy turn-around logic.
     */
    public static boolean checkEnemyTileCollision(Sprite enemy, TileMap tmap) {
        float ex = enemy.getX();
        float ey = enemy.getY();
        float ew = enemy.getWidth();
        float eh = enemy.getHeight();
        float evx = enemy.getSpeedX();

        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        float nextX = ex + evx;

        int tileX = evx > 0
                ? (int) ((nextX + ew - 2) / tileWidth)
                : (int) (nextX / tileWidth);

        int topTileY = (int) (ey / tileHeight);
        int bottomTileY = (int) ((ey + eh - 3) / tileHeight);

        for (int tileY = topTileY; tileY <= bottomTileY; tileY++) {
            Tile tile = tmap.getTile(tileX, tileY);
            if (tile != null && tile.getCharacter() != '.') {
                return true;
            }
        }
        return false;
    }

    /**
     * Detects and resolves tile collisions for a sprite this frame, returning
     * a result describing what happened. Mutates the sprite's position/velocity
     * as part of collision response; does not touch any other state.
     */
    public static TileCollisionResult checkTileCollision(Sprite s, TileMap tmap) {
        List<Tile> collidedTiles = new ArrayList<>();
        boolean horizontalCollision = false;
        boolean landed = false;

        float sx = s.getX();
        float sy = s.getY();
        float sw = s.getWidth();
        float sh = s.getHeight();
        float velocityX = s.getSpeedX();
        float velocityY = s.getVelocityY();

        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        if (velocityX == 0 && velocityY == 0) {
            boolean onGround = isOnGround(s, tmap);
            return new TileCollisionResult(collidedTiles, false, false, onGround);
        }

        float nextX = sx + velocityX;
        float nextY = sy + velocityY;

        // Horizontal sweep
        if (velocityX != 0) {
            int tileX = velocityX > 0
                    ? (int) ((nextX + sw - 2) / tileWidth)
                    : (int) (nextX / tileWidth);

            int topTileY = (int) (sy / tileHeight);
            int bottomTileY = (int) ((sy + sh - 3) / tileHeight);

            boolean localHorizontalCollision = false;
            for (int tileY = topTileY; tileY <= bottomTileY; tileY++) {
                Tile tile = tmap.getTile(tileX, tileY);
                if (tile != null && tile.getCharacter() != '.') {
                    collidedTiles.add(tile);
                    localHorizontalCollision = true;
                }
            }

            if (localHorizontalCollision) {
                if (velocityX > 0) {
                    s.setX(tileX * tileWidth - sw);
                } else {
                    s.setX((tileX + 1) * tileWidth);
                }
                s.setSpeedX(0);
                horizontalCollision = true;
            }
        }

        sx = s.getX();
        velocityY = s.getVelocityY();
        nextY = sy + velocityY;

        // Vertical sweep
        if (velocityY != 0) {
            int tileY = velocityY > 0
                    ? (int) ((nextY + sh - 1) / tileHeight)
                    : (int) (nextY / tileHeight);

            int leftTileX = (int) ((sx + 2) / tileWidth);
            int rightTileX = (int) ((sx + sw - 2) / tileWidth);

            boolean verticalCollision = false;
            for (int tileX = leftTileX; tileX <= rightTileX; tileX++) {
                Tile tile = tmap.getTile(tileX, tileY);
                if (tile != null && tile.getCharacter() != '.') {
                    collidedTiles.add(tile);
                    verticalCollision = true;
                }
            }

            if (verticalCollision) {
                if (velocityY > 0) {
                    s.setY(tileY * tileHeight - sh);
                    s.setVelocityY(0);
                    landed = true;
                } else {
                    s.setY((tileY + 1) * tileHeight);
                    s.setVelocityY(0);
                }
            }
            return new TileCollisionResult(collidedTiles, horizontalCollision, landed, false);
        } else {
            boolean onGround = isOnGround(s, tmap);
            return new TileCollisionResult(collidedTiles, horizontalCollision, false, onGround);
        }
    }

    /**
     * Returns true if the sprite is resting on a solid tile (checks one pixel
     * below its bottom edge).
     */
    public static boolean isOnGround(Sprite s, TileMap tmap) {
        float sx = s.getX();
        float sy = s.getY();
        float sw = s.getWidth();
        float sh = s.getHeight();
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        int groundTileY = (int) ((sy + sh + 1) / tileHeight);
        int leftTileX = (int) ((sx + 2) / tileWidth);
        int rightTileX = (int) ((sx + sw - 2) / tileWidth);

        for (int tileX = leftTileX; tileX <= rightTileX; tileX++) {
            Tile tile = tmap.getTile(tileX, groundTileY);
            if (tile != null && tile.getCharacter() != '.') {
                return true;
            }
        }
        return false;
    }
}
