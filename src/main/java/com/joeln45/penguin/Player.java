package com.joeln45.penguin;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.Sprite;

/**
 * The penguin player: holds the sprite, physics constants and the per-frame
 * movement logic (gravity, jumping, horizontal acceleration/deceleration).
 *
 * <p>The owning {@link Game} is still responsible for collision detection and
 * for telling the player whether it's allowed to jump this frame.
 *
 * @author Joel Nirmal
 */
public final class Player {

    private static final float GRAVITY = 0.0006f;
    private static final float MOVE_SPEED = 0.15f;
    private static final float JUMP_SPEED = -0.32f;
    /** Short-hop ceiling: max upward speed retained when jump is released early. */
    private static final float SHORT_HOP_SPEED = 0.12f;
    private static final float TERMINAL_VELOCITY = 0.3f;
    private static final float ACCELERATION = 0.01f;
    private static final float DECELERATION = 0.008f;
    private static final float JUMP_BOOST_FACTOR = 1.1f;

    /** Grace window after walking off a ledge during which a jump still fires. */
    private static final long COYOTE_TIME_MS = 100;
    /** Window before landing during which a jump press is remembered and auto-fires. */
    private static final long JUMP_BUFFER_MS = 150;

    private final Sprite sprite;
    private boolean usedAirJump = false;  // air-jump consumed this airborne span
    private boolean lastCanJump = false;  // ground-state from previous frame
    private long    lastGroundedAt = 0;   // wall-clock ms when we last touched ground

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

    /**
     * Applies gravity, jump, and horizontal movement for this frame. Caller
     * passes in the latest input snapshot, whether a jump is permitted
     * (the player just touched ground), and whether the double-jump powerup
     * is currently active.
     *
     * @return true if a jump was initiated (caller should play the sfx)
     */
    public boolean update(long elapsed, InputHandler input, boolean canJump,
                          boolean doubleJumpAvailable) {
        long now = System.currentTimeMillis();

        // Reset the air-jump credit whenever we transition onto solid ground,
        // and stamp the time so coyote-jump can use it.
        if (canJump && !lastCanJump) {
            usedAirJump = false;
        }
        if (canJump) {
            lastGroundedAt = now;
        }
        lastCanJump = canJump;

        // Gravity with terminal velocity — but when standing on solid ground
        // hold vy at zero so the body doesn't slowly drift through the floor
        // between landing frames (which used to cause horizontal-sweep snags).
        if (canJump) {
            sprite.setVelocityY(0);
        } else {
            float newVy = sprite.getVelocityY() + (GRAVITY * elapsed);
            if (newVy > TERMINAL_VELOCITY) newVy = TERMINAL_VELOCITY;
            sprite.setVelocityY(newVy);
        }

        // Jump trigger combines three game-feel tricks:
        //   - coyote time: 100 ms grace after walking off a ledge
        //   - jump buffer: 150 ms remembered press window before landing
        //   - air jump (with double-jump powerup): one extra mid-air jump
        boolean jumped = false;
        boolean withinBuffer = (now - input.jumpPressedAt()) <= JUMP_BUFFER_MS;
        boolean withinCoyote = (now - lastGroundedAt) <= COYOTE_TIME_MS;

        if (input.isJump() && withinBuffer) {
            if (canJump || withinCoyote) {
                doJump(input);
                lastGroundedAt = 0; // burn coyote so we can't re-use it mid-air
                jumped = true;
            } else if (doubleJumpAvailable && !usedAirJump) {
                doJump(input);
                usedAirJump = true;
                jumped = true;
            }
        }

        // Variable jump height: releasing jump while rising clips the upward
        // velocity to SHORT_HOP_SPEED, so a quick tap = small hop.
        if (sprite.getVelocityY() < -SHORT_HOP_SPEED && !input.isJumpHeld()) {
            sprite.setVelocityY(-SHORT_HOP_SPEED);
        }

        // Horizontal movement with acceleration / deceleration
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

        // Advance sprite animation + apply velocity
        sprite.update(elapsed);
        return jumped;
    }

    private void doJump(InputHandler input) {
        sprite.setAnimationSpeed(1.8f);
        sprite.setVelocityY(JUMP_SPEED);
        input.consumeJump();
        // Small horizontal boost when jumping while moving
        if (input.isMoveRight()) {
            sprite.setSpeedX(MOVE_SPEED * JUMP_BOOST_FACTOR);
        } else if (input.isMoveLeft()) {
            sprite.setSpeedX(-MOVE_SPEED * JUMP_BOOST_FACTOR);
        }
    }
}
