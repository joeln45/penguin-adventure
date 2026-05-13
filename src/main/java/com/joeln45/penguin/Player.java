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
    private static final float TERMINAL_VELOCITY = 0.3f;
    private static final float ACCELERATION = 0.01f;
    private static final float DECELERATION = 0.008f;
    private static final float JUMP_BOOST_FACTOR = 1.1f;

    private final Sprite sprite;

    public Player(Animation idleAnim) {
        this.sprite = new Sprite(idleAnim);
    }

    public Sprite sprite() { return sprite; }

    public void respawn(float x, float y) {
        sprite.setPosition(x, y);
        sprite.setVelocity(0, 0);
        sprite.show();
    }

    /**
     * Applies gravity, jump, and horizontal movement for this frame. Caller
     * passes in the latest input snapshot and whether a jump is permitted
     * (the player just touched ground).
     *
     * @return true if a jump was initiated (caller should play the sfx)
     */
    public boolean update(long elapsed, InputHandler input, boolean canJump) {
        // Gravity with terminal velocity
        float newVy = sprite.getVelocityY() + (GRAVITY * elapsed);
        if (newVy > TERMINAL_VELOCITY) newVy = TERMINAL_VELOCITY;
        sprite.setVelocityY(newVy);

        // Jump
        boolean jumped = false;
        if (input.isJump() && canJump) {
            sprite.setAnimationSpeed(1.8f);
            sprite.setVelocityY(JUMP_SPEED);
            input.consumeJump();
            jumped = true;

            // Small horizontal boost when jumping while moving
            if (input.isMoveRight()) {
                sprite.setSpeedX(MOVE_SPEED * JUMP_BOOST_FACTOR);
            } else if (input.isMoveLeft()) {
                sprite.setSpeedX(-MOVE_SPEED * JUMP_BOOST_FACTOR);
            }
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
}
