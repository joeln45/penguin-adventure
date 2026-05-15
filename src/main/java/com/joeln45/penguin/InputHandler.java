package com.joeln45.penguin;

import java.awt.event.KeyEvent;

/**
 * Holds the current keyboard state so the game loop can poll it instead of
 * reacting to AWT key events directly. Esc quits, B toggles debug overlay.
 */
public final class InputHandler {

    private boolean moveLeft;
    private boolean moveRight;
    private boolean jump;
    private boolean jumpHeld;
    private long    jumpPressedAt;
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

    /** Clears the pending jump flag and returns true if a jump was queued. */
    public boolean consumeJump() {
        if (jump) { jump = false; return true; }
        return false;
    }

    public void handleKeyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> {
                // only timestamp the first press; key auto-repeat fires this again
                if (!jumpHeld) jumpPressedAt = System.currentTimeMillis();
                jump = true;
                jumpHeld = true;
            }
            case KeyEvent.VK_RIGHT -> moveRight = true;
            case KeyEvent.VK_LEFT  -> moveLeft = true;
            case KeyEvent.VK_ESCAPE -> onQuit.run();
            case KeyEvent.VK_B     -> debug = !debug;
            default -> {}
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> onQuit.run();
            case KeyEvent.VK_UP, KeyEvent.VK_SPACE -> jumpHeld = false;
            case KeyEvent.VK_RIGHT -> moveRight = false;
            case KeyEvent.VK_LEFT  -> moveLeft = false;
            default -> {}
        }
    }
}
