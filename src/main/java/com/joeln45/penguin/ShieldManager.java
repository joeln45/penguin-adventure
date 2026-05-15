package com.joeln45.penguin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.joeln45.penguin.engine.Animation;
import com.joeln45.penguin.engine.Sprite;

/**
 * Shield pickups. Each one collected adds a charge to GameState.shields,
 * which absorbs the next hit. The icon is drawn in code so the game doesn't
 * need an extra PNG.
 */
public final class ShieldManager {

    private static final int SPRITE_SIZE = 32;

    private final BufferedImage image = createImage();
    private final List<Sprite> pickups = new ArrayList<>();

    public void loadLevel(int level) {
        pickups.clear();
        if (level == 3) {
            // sits on top of the staircase so the player walks into it on the way up
            spawnAt(352, 320);
        }
    }

    public void update(long elapsed, Sprite player, GameState state) {
        for (int i = pickups.size() - 1; i >= 0; i--) {
            Sprite s = pickups.get(i);
            s.update(elapsed);
            if (CollisionService.boundingBoxCollision(player, s)) {
                pickups.remove(i);
                state.shields++;
                AssetLoader.coinSound().start();
            }
        }
    }

    public void draw(Graphics2D g, int xo, int yo) {
        for (Sprite s : pickups) {
            s.setOffsets(xo, yo);
            s.draw(g);
        }
    }

    private void spawnAt(float x, float y) {
        Animation anim = new Animation();
        anim.addFrame(image, 1000);
        Sprite s = new Sprite(anim);
        s.setPosition(x, y);
        s.show();
        pickups.add(s);
    }

    // builds the blue disc with an "S" on it
    private static BufferedImage createImage() {
        BufferedImage img = new BufferedImage(SPRITE_SIZE, SPRITE_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(80, 160, 255, 120));
            g.fillOval(0, 0, SPRITE_SIZE, SPRITE_SIZE);
            g.setColor(new Color(60, 130, 230));
            g.fillOval(4, 4, SPRITE_SIZE - 8, SPRITE_SIZE - 8);
            g.setColor(new Color(20, 60, 130));
            g.setStroke(new BasicStroke(2));
            g.drawOval(4, 4, SPRITE_SIZE - 8, SPRITE_SIZE - 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            FontMetrics fm = g.getFontMetrics();
            String text = "S";
            int tx = (SPRITE_SIZE - fm.stringWidth(text)) / 2;
            int ty = (SPRITE_SIZE + fm.getAscent()) / 2 - 3;
            g.drawString(text, tx, ty);
        } finally {
            g.dispose();
        }
        return img;
    }
}
