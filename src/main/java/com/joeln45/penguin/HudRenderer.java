package com.joeln45.penguin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Stateless HUD/overlay rendering for in-game UI: star counter, lives, sound
 * icon (with mute overlay) and the game-over / level-complete screen.
 *
 * @author Joel Nirmal
 */
public final class HudRenderer {

    private static final int STAR_PANEL_X = 10;
    private static final int STAR_PANEL_Y = 60;
    private static final int STAR_PANEL_W = 130;
    private static final int STAR_PANEL_H = 40;
    private static final int HEART_SIZE = 30;
    private static final int HEART_GAP = 35;
    private static final int HEART_Y = 70;
    public static final int SOUND_ICON_SIZE = 30;
    public static final int SOUND_ICON_Y = 70;

    private HudRenderer() {}

    /** Star counter pill (top-left). */
    public static void drawStarCounter(Graphics2D g, int starsCollected, int total) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(STAR_PANEL_X, STAR_PANEL_Y, STAR_PANEL_W, STAR_PANEL_H, 10, 10);
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(STAR_PANEL_X, STAR_PANEL_Y, STAR_PANEL_W, STAR_PANEL_H, 10, 10);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.YELLOW);
        g.drawString("Stars:", 20, 85);
        String numbersText = String.format("%d/%d", starsCollected, total);
        int numbersX = 20 + fm.stringWidth("Stars: ");
        g.drawString(numbersText, numbersX, 85);
    }

    /** Hearts row (top-right area), anchored just left of the sound icon. */
    public static void drawLives(Graphics2D g, Image heartPic, int lives, int containerWidth) {
        // rightmost heart ends 10 px left of the sound icon
        int heartX = containerWidth - 50 - 10 - HEART_SIZE - (lives - 1) * HEART_GAP;
        for (int i = 0; i < lives; i++) {
            g.drawImage(heartPic, heartX, HEART_Y, HEART_SIZE, HEART_SIZE, null);
            heartX += HEART_GAP;
        }
    }

    /** Sound icon plus mute overlay. */
    public static void drawSoundIcon(Graphics2D g, Image soundIcon, boolean isMuted, int containerWidth) {
        int x = containerWidth - 50;
        g.drawImage(soundIcon, x, SOUND_ICON_Y, SOUND_ICON_SIZE, SOUND_ICON_SIZE, null);
        if (isMuted) {
            g.setColor(new Color(255, 0, 0, 150));
            g.fillOval(x, SOUND_ICON_Y, SOUND_ICON_SIZE, SOUND_ICON_SIZE);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("X", x + 10, SOUND_ICON_Y + 20);
        }
    }

    /** Returns the screen-space bounds of the sound icon for hit-testing clicks. */
    public static Rectangle soundIconBounds(int containerWidth) {
        return new Rectangle(containerWidth - 50, SOUND_ICON_Y, SOUND_ICON_SIZE, SOUND_ICON_SIZE);
    }

    /** Pill showing "2x JUMP — Ns" while the double-jump powerup is active. */
    public static void drawDoubleJumpIndicator(Graphics2D g, long remainingMs) {
        if (remainingMs <= 0) return;
        int x = STAR_PANEL_X;
        int y = STAR_PANEL_Y + STAR_PANEL_H + 10;
        int w = 160;
        int h = 36;

        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(new Color(255, 195, 0, 200));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, w, h, 10, 10);

        g.setColor(new Color(255, 220, 80));
        g.setFont(new Font("Arial", Font.BOLD, 18));
        long seconds = (remainingMs + 999) / 1000; // round up so "1s" shows for the final tick
        g.drawString(String.format("2x JUMP - %ds", seconds), x + 12, y + 24);
    }

    /** Dimmed overlay with a "PAUSED" caption — shown while the window has lost focus. */
    public static void drawPausedOverlay(Graphics2D g, Font titleFont, int containerWidth, int containerHeight) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, containerWidth, containerHeight);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(titleFont);
        g.setColor(Color.WHITE);
        String text = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        int x = (containerWidth - fm.stringWidth(text)) / 2;
        int y = containerHeight / 2;
        g.drawString(text, x, y);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String hint = "Click the window to resume";
        fm = g.getFontMetrics();
        g.drawString(hint, (containerWidth - fm.stringWidth(hint)) / 2, y + 40);
    }

    /** Full-screen game-over / level-complete overlay with a styled action button. */
    public static void drawGameOverScreen(Graphics2D g, Font gameOverFont, boolean gameCompleted,
                                          Rectangle playAgainBtn, Rectangle restartBtn,
                                          int containerWidth, int containerHeight) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, containerWidth, containerHeight);

        g.setFont(gameOverFont);
        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        String gameOverText = gameCompleted ? "Congratulations!" : "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int gameOverX = (containerWidth - fm.stringWidth(gameOverText)) / 2;
        int gameOverY = containerHeight / 2 - 100;
        g.drawString(gameOverText, gameOverX, gameOverY);

        if (gameCompleted) {
            g.setFont(new Font("Arial", Font.BOLD, 32));
            String completionText = "You completed all levels!";
            fm = g.getFontMetrics();
            int x = (containerWidth - fm.stringWidth(completionText)) / 2;
            int y = gameOverY + 60;
            g.drawString(completionText, x, y);
        }

        Rectangle btn = gameCompleted ? restartBtn : playAgainBtn;
        String btnText = gameCompleted ? "Restart Game" : "Play Again";

        g.setColor(new Color(70, 130, 180, 200));
        g.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        fm = g.getFontMetrics();
        int bx = btn.x + (btn.width - fm.stringWidth(btnText)) / 2;
        int by = btn.y + (btn.height + fm.getAscent()) / 2;
        g.drawString(btnText, bx, by);
    }
}
