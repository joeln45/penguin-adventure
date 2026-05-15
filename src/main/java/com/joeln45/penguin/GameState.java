package com.joeln45.penguin;

/** Flags + counters for the menu/playing/game-over flow. Plain data, no logic. */
public final class GameState {

    public boolean inMenu = true;
    public boolean gameOver = false;
    public boolean gameCompleted = false;
    public boolean gameOverSoundPlayed = false;
    public boolean levelCompleted = false;
    public boolean isFlickering = false;
    public boolean isMuted = false;
    public static final int LIVES_PER_LEVEL = 3;

    public int lives = LIVES_PER_LEVEL;
    public int shields = 0;
    public long flickerStartTime = 0;

    public void resetForNewGame() {
        levelCompleted = false;
        lives = LIVES_PER_LEVEL;
        shields = 0;
        gameOver = false;
        gameCompleted = false;
        gameOverSoundPlayed = false;
        isFlickering = false;
    }

    public void resetForNewLevel() {
        lives = LIVES_PER_LEVEL;
        shields = 0;
        gameOverSoundPlayed = false;
        isFlickering = false;
    }
}
