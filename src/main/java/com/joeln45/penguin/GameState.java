package com.joeln45.penguin;

/**
 * Mutable bag of top-level game-mode flags and counters.
 *
 * <p>This is intentionally a plain data holder — no behaviour — so the
 * orchestrator ({@link Game}) can still drive transitions between menu /
 * playing / game-over states explicitly. Grouping these fields here makes
 * Game's field section readable and signals "these change together".
 *
 * @author Joel Nirmal
 */
public final class GameState {

    public boolean inMenu = true;
    public boolean gameOver = false;
    public boolean gameCompleted = false;
    public boolean gameOverSoundPlayed = false;
    public boolean levelCompleted = false;
    public boolean isFlickering = false;
    public boolean isMuted = false;
    public int lives = 2;
    public long flickerStartTime = 0;

    /** Reset the per-run flags to the start-of-game defaults (called on new game / restart). */
    public void resetForNewGame() {
        levelCompleted = false;
        lives = 2;
        gameOver = false;
        gameCompleted = false;
        gameOverSoundPlayed = false;
        isFlickering = false;
    }
}
