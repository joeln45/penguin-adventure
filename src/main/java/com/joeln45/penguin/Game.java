package com.joeln45.penguin;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.joeln45.penguin.engine.*;

/**
 * Penguin Adventure - the main game class. Started as CSCU9N6 coursework at
 * Stirling and reworked into this portfolio version.
 *
 * @author Joel Nirmal
 */
@SuppressWarnings("serial")
public class Game extends GameCore {
    static int screenWidth = 1200;
    static int screenHeight = 505;

    InputHandler input = new InputHandler(this::stop);
    GameState state = new GameState();

    boolean horizontalCollision = false;
    boolean canJump = false;
    boolean paused = false;
    static final long FLICKER_DURATION_MS = 2000;

    Rectangle[] levelButtons = new Rectangle[3];
    Rectangle playAgain_Button;
    Rectangle restartGame_Button;
    Font menu_font;
    Font instruction_Font;

    Image heartPic;
    Image soundIcon;
    Font gameOverFont;
    Player playerObj;
    Sprite player = null;
    List<Tile> collidedTiles = Collections.emptyList();

    CollectibleManager collectibles = new CollectibleManager();
    EnemyManager enemyMgr = new EnemyManager();
    HawkManager hawks = new HawkManager();
    ShieldManager shields = new ShieldManager();
    ParticleManager particles = new ParticleManager();
    LevelManager levelMgr;
    TileMap tmap;
    Sprite igloo;

    ParallaxBackground background;

    public static void main(String[] args) {
        Sound backgroundMusic = AssetLoader.backgroundMusic();
        backgroundMusic.start();

        Game gct = new Game();
        gct.init();
        gct.setSize(screenWidth, screenHeight);
        gct.setVisible(true);
        gct.run(false, screenWidth, screenHeight);
    }

    public void init() {

        levelMgr = new LevelManager(this);
        tmap = levelMgr.tileMap();
        igloo = levelMgr.igloo();

        setSize(screenWidth, screenHeight);
        setVisible(true);

        menu_font = new Font("Arial", Font.BOLD, 48);
        instruction_Font = new Font("Arial", Font.PLAIN, 20);

        // 3 level-select buttons stacked vertically on the left side of the menu
        int buttonWidth = 240;
        int buttonHeight = 56;
        int buttonX = screenWidth / 4 - buttonWidth / 2 + 60;
        int firstButtonY = 260;
        int buttonGap = 16;
        for (int i = 0; i < 3; i++) {
            levelButtons[i] = new Rectangle(buttonX,
                                            firstButtonY + i * (buttonHeight + buttonGap),
                                            buttonWidth, buttonHeight);
        }

        playAgain_Button = new Rectangle((screenWidth - buttonWidth) / 2,
                                   (screenHeight - buttonHeight) / 2 + 50,
                                   buttonWidth, buttonHeight);

        restartGame_Button = new Rectangle((screenWidth - buttonWidth) / 2,
                                   (screenHeight - buttonHeight) / 2 + 50,
                                   buttonWidth, buttonHeight);

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClick(evt);
            }
        });

        // pause when window loses focus (e.g. alt-tab)
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent e) { paused = false; }
            public void windowLostFocus(java.awt.event.WindowEvent e)   { paused = true; }
        });

        Image[] backgroundLayers = new Image[] {
            loadImage("images/Desert mountains - Layer 1.png"),
            loadImage("images/Desert mountains - Layer 2.png"),
            loadImage("images/Desert mountains - Layer 3.png"),
            loadImage("images/Desert mountains - Layer 4.png"),
        };
        background = new ParallaxBackground(backgroundLayers, screenWidth, screenHeight);

        heartPic = loadImage("images/heart.png");
        soundIcon = loadImage("images/sound.png");

        gameOverFont = new Font("Arial", Font.BOLD, 48);

        Animation onGround = new Animation();
        onGround.loadAnimationFromSheet("images/penguin.png", 4, 1, 120);

        playerObj = new Player(onGround);
        player = playerObj.sprite();

        initialiseGame();

        System.out.println(tmap);
    }

    public void initialiseGame() {
        initialiseGame(1);
    }

    public void initialiseGame(int level) {
        playerObj.respawn(100, 475);
        canJump = false;
        state.resetForNewGame();
        levelMgr.loadLevel(level, collectibles, enemyMgr, hawks);
        shields.loadLevel(level);
        particles.reset();
    }

    public void draw(Graphics2D g) {
        if (state.inMenu) {
            drawMenu(g);
            return;
        }

        background.draw(g);

        // view offset = follow the player horizontally, keep them around x=300
        int xo = -(int) player.getX() + 300;
        int yo = 0;

        tmap.draw(g, xo, yo);

        player.setOffsets(xo, yo);
        if (state.isFlickering) {
            // flash every 100ms during invincibility
            if ((System.currentTimeMillis() - state.flickerStartTime) % 200 < 100) {
                player.drawTransformed(g);
            }
        } else {
            player.drawTransformed(g);
        }

        igloo.setOffsets(xo, yo);
        igloo.draw(g);

        collectibles.draw(g, xo, yo, input.isDebug());
        enemyMgr.draw(g, xo, yo, input.isDebug());
        hawks.draw(g, xo, yo, input.isDebug());
        shields.draw(g, xo, yo);
        particles.draw(g, xo, yo);

        HudRenderer.drawStarCounter(g, collectibles.getStarsCollected(), collectibles.total());
        HudRenderer.drawShieldIndicator(g, state.shields);
        HudRenderer.drawLives(g, heartPic, state.lives, getWidth());
        HudRenderer.drawSoundIcon(g, soundIcon, state.isMuted, getWidth());

        if (state.gameOver) {
            HudRenderer.drawGameOverScreen(g, gameOverFont, state.gameCompleted,
                    playAgain_Button, restartGame_Button, getWidth(), getHeight());
        } else if (paused) {
            HudRenderer.drawPausedOverlay(g, gameOverFont, getWidth(), getHeight());
        }
    
        if (input.isDebug()) {
            // hitboxes + debug info
            g.setColor(Color.red);
            player.drawBoundingCircle(g);

            g.setColor(Color.orange);
            igloo.drawBoundingBox(g);

            g.setColor(Color.darkGray);
            g.drawString(String.format("Level: %d", levelMgr.currentLevel()), getWidth() - 200, 150);
            g.drawString(String.format("Can Jump: %s", canJump ? "Yes" : "No"),
                    getWidth() - 200, 110);

            g.setColor(Color.green);
            g.drawRect((int)levelMgr.leftBoundary() + xo, (int)levelMgr.topBoundary() + yo,
                      (int)(levelMgr.rightBoundary() - levelMgr.leftBoundary()),
                      (int)(levelMgr.bottomBoundary() - levelMgr.topBoundary()));

            drawCollidedTiles(g, tmap, xo, yo);
        }
    }

    private void drawMenu(Graphics2D g) {
        background.draw(g);

        // dim the background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, screenWidth, screenHeight);

        // title with a drop-shadow effect
        g.setFont(menu_font);
        g.setColor(new Color(0, 0, 0, 150));
        String title = "Penguin Adventure";
        FontMetrics fontMetric = g.getFontMetrics();
        int titleX = screenWidth / 6;
        int titleY = 200;
        g.drawString(title, titleX, titleY);
        g.setColor(Color.WHITE);
        g.drawString(title, titleX - 2, titleY - 2);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        fontMetric = g.getFontMetrics();
        for (int i = 0; i < 3; i++) {
            Rectangle btn = levelButtons[i];

            g.setColor(new Color(70, 130, 180, 200));
            g.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);

            g.setColor(new Color(255, 255, 255, 150));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);

            g.setColor(Color.WHITE);
            String label = "Level " + (i + 1);
            int lx = btn.x + (btn.width - fontMetric.stringWidth(label)) / 2;
            int ly = btn.y + (btn.height + fontMetric.getAscent()) / 2;
            g.drawString(label, lx, ly);
        }

        // instructions box on the right
        int boxWidth = 350;
        int boxHeight = 350;
        int instructionBoxX = screenWidth - boxWidth - 50;
        int instructionBoxY = screenHeight / 5;

        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(instructionBoxX, instructionBoxY, boxWidth, boxHeight, 20, 20);

        g.setColor(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(instructionBoxX, instructionBoxY, boxWidth, boxHeight, 20, 20);

        g.setFont(instruction_Font);
        g.setColor(Color.WHITE);
        String[] instructions = {
            "How to Play:",
            "• Use LEFT, RIGHT and UP arrow",
                "  keys to move and jump",
            "• Collect all stars to complete each",
                "  level",
            "• Avoid enemies to stay alive",
            "• Reach the igloo to complete the",
                "  level",
            "• Press ESC to quit"
        };

        int instructionY = instructionBoxY + 40;
        for (String instruction : instructions) {
            fontMetric = g.getFontMetrics();
            int instructionX = instructionBoxX + 20;
            g.drawString(instruction, instructionX, instructionY);
            instructionY += 35;
        }
    }

    public void drawCollidedTiles(Graphics2D g, TileMap map, int xOffset, int yOffset) {
        if (collidedTiles.size() > 0) {
            int tileWidth = map.getTileWidth();
            int tileHeight = map.getTileHeight();

            g.setColor(Color.blue);
            for (Tile t : collidedTiles) {
                g.drawRect(t.getXC() + xOffset, t.getYC() + yOffset, tileWidth, tileHeight);
            }
        }
    }

    public void update(long elapsed) {
        if (paused) return;
        if (state.gameOver) {
            if (!state.gameOverSoundPlayed) {
                AssetLoader.gameOverSound().start();
                state.gameOverSoundPlayed = true;
            }
            return;
        }

        horizontalCollision = false;

        CollisionService.TileCollisionResult tileResult =
                CollisionService.checkTileCollision(player, tmap, elapsed);
        collidedTiles = tileResult.collidedTiles;
        if (tileResult.horizontalCollision) horizontalCollision = true;
        if (tileResult.landedOnGround) {
            particles.spawnLandingDust(player.getX() + player.getWidth() / 2f,
                                       player.getY() + player.getHeight());
        }
        // recompute every frame so walking off a ledge drops the flag cleanly
        canJump = tileResult.landedOnGround || tileResult.onGround;

        if (state.isFlickering) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - state.flickerStartTime >= FLICKER_DURATION_MS) {
                state.isFlickering = false;
                player.show();
            }
        }

        background.update(elapsed, input.isMoveRight(), input.isMoveLeft(), horizontalCollision);

        boolean jumped = playerObj.update(elapsed, input, canJump);
        if (jumped) {
            canJump = false;
            try {
                AssetLoader.jumpSound().start();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        collectibles.update(elapsed, player);
        shields.update(elapsed, player, state);
        particles.update(elapsed);

        boolean enemyHit = enemyMgr.update(elapsed, player, tmap);
        boolean hawkHit = hawks.update(elapsed, player);
        if (enemyHit || hawkHit) {
            if (state.shields > 0) {
                // shield absorbs the hit, no life lost
                state.shields--;
                state.isFlickering = true;
                state.flickerStartTime = System.currentTimeMillis();
            } else {
                state.lives--;
                try {
                    AssetLoader.attackSound().start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (state.lives <= 0) {
                    state.gameOver = true;
                    AssetLoader.gameOverSound().start();
                    state.gameOverSoundPlayed = true;
                } else {
                    state.isFlickering = true;
                    state.flickerStartTime = System.currentTimeMillis();
                }
            }
        }

        handleScreenEdge(player, tmap, elapsed);

        // reached the igloo with all stars -> level complete
        if (!state.levelCompleted && CollisionService.boundingBoxCollision(player, igloo)) {
            if (collectibles.allCollected()) {
                try {
                    AssetLoader.levelCompleteSound().start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (levelMgr.isLastLevel()) {
                    state.gameOver = true;
                    state.gameCompleted = true;
                } else {
                    state.levelCompleted = true;
                    playerObj.respawn(100, 475);
                    canJump = false;
                    state.resetForNewLevel();
                    levelMgr.loadNextLevel(collectibles, enemyMgr, hawks);
                    shields.loadLevel(levelMgr.currentLevel());
                    particles.reset();
                    state.levelCompleted = false;
                }
            }
        }
    }

    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed) {
        float sx = s.getX();
        float sy = s.getY();
        float sw = s.getWidth();
        float sh = s.getHeight();

        if (sy + sh > levelMgr.bottomBoundary()) {
            s.setY(levelMgr.bottomBoundary() - sh);
            s.setVelocityY(0);
            canJump = true;
        }
        if (sy < levelMgr.topBoundary()) {
            s.setY(levelMgr.topBoundary());
            s.setVelocityY(0);
        }
        if (sx < levelMgr.leftBoundary()) {
            s.setX(levelMgr.leftBoundary());
            s.setSpeedX(0);
        }
        if (sx + sw > levelMgr.rightBoundary()) {
            s.setX(levelMgr.rightBoundary() - sw);
            s.setSpeedX(0);
        }
    }

    public void keyPressed(KeyEvent e) { input.handleKeyPressed(e); }

    public void keyReleased(KeyEvent e) { input.handleKeyReleased(e); }

    private void handleMouseClick(java.awt.event.MouseEvent evt) {
        if (state.inMenu) {
            for (int i = 0; i < 3; i++) {
                if (levelButtons[i].contains(evt.getPoint())) {
                    state.inMenu = false;
                    initialiseGame(i + 1);
                    return;
                }
            }
        } else if (state.gameOver) {
            if (state.gameCompleted) {
                if (restartGame_Button.contains(evt.getPoint())) {
                    state.gameOver = false;
                    state.gameCompleted = false;
                    state.inMenu = true;
                }
            } else {
                if (playAgain_Button.contains(evt.getPoint())) {
                    state.gameOver = false;
                    state.levelCompleted = false;
                    playerObj.respawn(100, 475);
                    state.resetForNewLevel();
                    levelMgr.reloadCurrent(collectibles, enemyMgr, hawks);
                }
            }
        }

        // click on the sound icon toggles mute
        Rectangle soundIconBounds = HudRenderer.soundIconBounds(getWidth());
        if (soundIconBounds.contains(evt.getPoint())) {
            state.isMuted = !state.isMuted;
            Sound.setGlobalMute(state.isMuted);
        }
    }
}