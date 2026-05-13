package com.joeln45.penguin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.Sprite;

/**
 * Unit tests for {@link CollisionService}.
 *
 * <p>Covers {@code boundingBoxCollision} with the small hitbox margin the
 * service uses. Tile-based collision (which needs a real {@code TileMap})
 * is left for integration testing.
 */
class CollisionServiceTest {

    /** Smallest factory: build a sprite of given size with an in-memory blank frame. */
    private static Sprite makeSprite(int width, int height, float x, float y) {
        Animation anim = new Animation();
        anim.addFrame(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), 1000);
        Sprite s = new Sprite(anim);
        s.setPosition(x, y);
        return s;
    }

    @Test
    @DisplayName("two sprites that fully overlap collide")
    void fullyOverlappingSpritesCollide() {
        Sprite a = makeSprite(40, 40, 100, 100);
        Sprite b = makeSprite(40, 40, 100, 100);
        assertTrue(CollisionService.boundingBoxCollision(a, b));
    }

    @Test
    @DisplayName("far-apart sprites do not collide")
    void disjointSpritesDoNotCollide() {
        Sprite a = makeSprite(40, 40, 0, 0);
        Sprite b = makeSprite(40, 40, 500, 500);
        assertFalse(CollisionService.boundingBoxCollision(a, b));
    }

    @Test
    @DisplayName("partially overlapping sprites collide")
    void partiallyOverlappingSpritesCollide() {
        Sprite a = makeSprite(40, 40, 100, 100);
        Sprite b = makeSprite(40, 40, 120, 120); // overlaps by 20 px on each axis
        assertTrue(CollisionService.boundingBoxCollision(a, b));
    }

    @Test
    @DisplayName("touching edges do NOT collide (5px hitbox margin)")
    void edgeTouchingSpritesDoNotCollide() {
        // The service uses a 5px hitbox margin, so sprites flush against
        // each other should not register as a collision.
        Sprite a = makeSprite(40, 40, 100, 100);
        Sprite b = makeSprite(40, 40, 140, 100); // a's right edge meets b's left edge
        assertFalse(CollisionService.boundingBoxCollision(a, b));
    }

    @Test
    @DisplayName("small overlap below margin does NOT collide")
    void tinyOverlapBelowMarginDoesNotCollide() {
        // The 5px margin on each side means a 9px overlap (4.5px each side)
        // still falls inside the slack and shouldn't register.
        Sprite a = makeSprite(40, 40, 100, 100);
        Sprite b = makeSprite(40, 40, 132, 100); // 8px overlap, less than 2*5px margin
        assertFalse(CollisionService.boundingBoxCollision(a, b));
    }

    @Test
    @DisplayName("vertical-only separation prevents collision")
    void verticalSeparationPreventsCollision() {
        Sprite a = makeSprite(40, 40, 100, 100);
        Sprite b = makeSprite(40, 40, 100, 200); // same X, very different Y
        assertFalse(CollisionService.boundingBoxCollision(a, b));
    }
}
