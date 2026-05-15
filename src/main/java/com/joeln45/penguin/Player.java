package com.joeln45.penguin;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.Sprite;

/**
 * The penguin. Handles physics, gravity and jump logic. The Game class still
 * runs collision detection and tells us whether we're allowed to jump.
 */
public final class Player {

    private static final float GRAVITY = 0.0006f;
    private static final float MOVE_SPEED = 0.15f;
    private static final float JUMP_SPEED = -0.32f;
    // max upward speed kept after an early jump release (short hop)
    private static final float SHORT_HOP_SPEED = 0.12f;
    private static final float TERMINAL_VELOCITY = 0.3f;
    private static final float ACCELERATION = 0.01f;
    private static final float DECELERATION = 0.008f;
    private static final float JUMP_BOOST_FACTOR = 1.1f;

    // jump can still fire 100ms after walking off a ledge
    private static final long COYOTE_TIME_MS = 100;
    // a jump press up to 150ms before landing still counts
    private static final long JUMP_BUFFER_MS = 150;

    private final Sprite sprite;
    private boolean usedAirJump = false;
    private boolean lastCanJump = false;
    private long    lastGroundedAt = 0;

    public Player(Animation idleAnim) {
        this.sprite = new Sprite(idleAnim);
    }

    public Sprite sprite() { return sprite; }

    public void respawn(float x, float y) {
        sprite.setPosition(x, y);
        sprite.setVelocity(0, 0);
        sprite.show();
        usedAirJump = false;
        lastCanJump = false;
        lastGroundedAt = 0;
    }

    /** Runs movement + jump for this frame. Returns true if a jump fired. */
    public boolean update(long elapsed, InputHandler input, boolean canJump) {
        long now = System.currentTimeMillis();

        // refill the mid-air jump when we land
        if (canJump && !lastCanJump) {
            usedAirJump = false;
        }
        if (canJump) {
            lastGroundedAt = now;
        }
        lastCanJump = canJump;

        // gravity, but keep vy at 0 on the ground so we don't drift through it
        if (canJump) {
            sprite.setVelocityY(0);
        } else {
            float newVy = sprite.getVelocityY() + (GRAVITY * elapsed);
            if (newVy > TERMINAL_VELOCITY) newVy = TERMINAL_VELOCITY;
            sprite.setVelocityY(newVy);
        }

        // jump = (on ground OR coyote window OR free mid-air jump) AND buffered press
        boolean jumped = false;
        boolean withinBuffer = (now - input.jumpPressedAt()) <= JUMP_BUFFER_MS;
        boolean withinCoyote = (now - lastGroundedAt) <= COYOTE_TIME_MS;

        if (input.isJump() && withinBuffer) {
            if (canJump || withinCoyote) {
                doJump(input);
                lastGroundedAt = 0; // don't let coyote re-fire mid-air
                jumped = true;
            } else if (!usedAirJump) {
                doJump(input);
                usedAirJump = true;
                jumped = true;
            }
        }

        // variable jump height: releasing UP while rising clips upward velocity
        if (sprite.getVelocityY() < -SHORT_HOP_SPEED && !input.isJumpHeld()) {
            sprite.setVelocityY(-SHORT_HOP_SPEED);
        }

        if (input.isMoveRight()) {
            float vx = sprite.getSpeedX() + (ACCELERATION * elapsed);
            if (vx > MOVE_SPEED) vx = MOVE_SPEED;
            sprite.setSpeedX(vx);
            sprite.setAnimationSpeed(1.0f);
            sprite.setScale(1.0f, 1.0f);
        } else if (input.isMoveLeft()) {
            float vx = sprite.getSpeedX() - (ACCELERATION * elapsed);
            if (vx < -MOVE_SPEED) vx = -MOVE_SPEED;
            sprite.setSpeedX(vx);
            sprite.setAnimationSpeed(1.0f);
            sprite.setScale(-1.0f, 1.0f);
        } else {
            float vx = sprite.getSpeedX();
            float dec = DECELERATION * elapsed;
            if (Math.abs(vx) < dec) {
                sprite.setSpeedX(0);
            } else if (vx > 0) {
                sprite.setSpeedX(vx - dec);
            } else if (vx < 0) {
                sprite.setSpeedX(vx + dec);
            }
            if (sprite.getSpeedX() == 0) {
                sprite.setAnimationSpeed(0);
            }
        }

        sprite.update(elapsed);
        return jumped;
    }

    private void doJump(InputHandler input) {
        sprite.setAnimationSpeed(1.8f);
        sprite.setVelocityY(JUMP_SPEED);
        input.consumeJump();
        // small horizontal boost when jumping mid-run
        if (input.isMoveRight()) {
            sprite.setSpeedX(MOVE_SPEED * JUMP_BOOST_FACTOR);
        } else if (input.isMoveLeft()) {
            sprite.setSpeedX(-MOVE_SPEED * JUMP_BOOST_FACTOR);
        }
    }
}
