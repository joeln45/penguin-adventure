package com.joeln45.penguin;

import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * Tracks live keyboard input state for the game.
 *
 * <p>Maintains per-frame "is this key held?" flags so the update loop can poll
 * them without coupling to AWT events directly. The {@link Game} forwards its
 * {@code keyPressed}/{@code keyReleased} callbacks here.
 *
 * <p>Two side-effects can't naturally live as flags so they're delegated to
 * callbacks supplied at construction: pressing <kbd>Esc</kbd> exits the game,
 * pressing <kbd>B</kbd> toggles debug mode.
 *
 * @author Joel Nirmal
 */
public final class InputHandler {

    private boolean moveLeft;
    private boolean moveRight;
    private boolean jump;          // pending unconsumed jump request
    private boolean jumpHeld;      // true while the jump key is physically held
    private long    jumpPressedAt; // wall-clock ms of the most recent jump press
    private boolean debug;

    private final Runnable onQuit;

    public InputHandler(Runnable onQuit) {
        this.onQuit = onQuit;
    }

    public boolean isMoveLeft()  { return moveLeft;  }
    public boolean isMoveRight() { return moveRight; }
    public boolean isJump()      { return jump;      }
    public boolean isJumpHeld()  { return jumpHeld;  }
    public long    jumpPressedAt() { return jumpPressedAt; }
    public boolean isDebug()     { return debug;     }

    /** Consume the current jump request (returns true and clears it if one was pending). */
    public boolean consumeJump() {
        if (jump) { jump = false; return true; }
        return false;
    }

    public void handleKeyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> {
                // Treat each fresh keyboard press as a new press event (auto-repeat
                // generates pressed+released pairs on Windows, but we only timestamp
                // the first one).
                if (!jumpHeld) jumpPressedAt = System.currentTimeMillis();
                jump = true;
                jumpHeld = true;
            }
            case KeyEvent.VK_RIGHT -> moveRight = true;
            case KeyEvent.VK_LEFT  -> moveLeft = true;
            case KeyEvent.VK_ESCAPE -> onQuit.run();
            case KeyEvent.VK_B     -> debug = !debug;
            default -> { /* ignored */ }
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> onQuit.run();
            case KeyEvent.VK_UP, KeyEvent.VK_SPACE -> jumpHeld = false;
            case KeyEvent.VK_RIGHT -> moveRight = false;
            case KeyEvent.VK_LEFT  -> moveLeft = false;
            default -> { /* ignored */ }
        }
    }

    // Unused helper kept for completeness in case the future Pause feature wants
    // a one-shot callback per press.
    public static <T> Consumer<T> noop() { return t -> {}; }
}
