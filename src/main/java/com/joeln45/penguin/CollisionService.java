package com.joeln45.penguin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.joeln45.penguin.engine.Sprite;
import com.joeln45.penguin.engine.Tile;
import com.joeln45.penguin.engine.TileMap;

/**
 * Collision helpers. {@link #checkTileCollision} mutates the sprite to resolve
 * the collision; everything else is read-only.
 */
public final class CollisionService {

    private CollisionService() {}

    public static final class TileCollisionResult {
        public final List<Tile> collidedTiles;
        public final boolean horizontalCollision;
        /** True if the sprite actually hit a tile while falling this frame. */
        public final boolean landedOnGround;
        /** True if the sprite is already resting on a tile (vy == 0). */
        public final boolean onGround;

        TileCollisionResult(List<Tile> tiles, boolean hCol, boolean landed, boolean onGround) {
            this.collidedTiles = Collections.unmodifiableList(tiles);
            this.horizontalCollision = hCol;
            this.landedOnGround = landed;
            this.onGround = onGround;
        }
    }

    /** AABB overlap test with a small margin so the hitboxes feel fair. */
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

    /** True if the enemy would walk into a wall this frame (used to turn around). */
    public static boolean checkEnemyTileCollision(Sprite enemy, TileMap tmap, long elapsed) {
        float ex = enemy.getX();
        float ey = enemy.getY();
        float ew = enemy.getWidth();
        float eh = enemy.getHeight();
        float evx = enemy.getSpeedX();

        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        // velocity is px/ms, project a full frame ahead
        float nextX = ex + evx * elapsed;

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
     * Resolves tile collisions for one frame. Snaps the sprite to the tile edge
     * and zeroes the relevant velocity component when it hits.
     */
    public static TileCollisionResult checkTileCollision(Sprite s, TileMap tmap, long elapsed) {
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

        // velocity is px/ms, so project a full frame ahead before checking
        float nextX = sx + velocityX * elapsed;
        float nextY = sy + velocityY * elapsed;

        // X axis
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
                // snap to int pixels so the sprite doesn't jitter when its width isn't whole
                if (velocityX > 0) {
                    s.setX((float) Math.floor(tileX * tileWidth - sw));
                } else {
                    s.setX((float) Math.ceil((tileX + 1) * tileWidth));
                }
                s.setSpeedX(0);
                horizontalCollision = true;
            }
        }

        sx = s.getX();
        velocityY = s.getVelocityY();
        nextY = sy + velocityY * elapsed;

        // Y axis
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
                // snap to int pixels so the sprite rests cleanly on ledges
                if (velocityY > 0) {
                    s.setY((float) Math.floor(tileY * tileHeight - sh));
                    s.setVelocityY(0);
                    landed = true;
                } else {
                    s.setY((float) Math.ceil((tileY + 1) * tileHeight));
                    s.setVelocityY(0);
                }
            }
            return new TileCollisionResult(collidedTiles, horizontalCollision, landed, false);
        } else {
            boolean onGround = isOnGround(s, tmap);
            return new TileCollisionResult(collidedTiles, horizontalCollision, false, onGround);
        }
    }

    /** True if there's a solid tile one pixel below the sprite. */
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
