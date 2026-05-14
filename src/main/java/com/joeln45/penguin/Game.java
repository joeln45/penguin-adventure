package com.joeln45.penguin;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.joeln45.penguin.engine.*;

/**
 * Penguin Adventure — the main game class.
 *
 * <p>Extends {@link com.joeln45.penguin.engine.GameCore} to implement a 2D
 * platformer: parallax-scrolling backgrounds, tile-based levels, animated
 * sprites, collision detection, a lives system, and filtered sound effects.
 *
 * <p>Originally written as coursework for the CSCU9N6 module at the
 * University of Stirling and since modernised as a portfolio project.
 *
 * @author Joel Nirmal
 */
@SuppressWarnings("serial")
public class Game extends GameCore {
    // Useful game constants
    static int screenWidth = 1200;
    static int screenHeight = 505;

    // Input state (delegated to InputHandler)
    InputHandler input = new InputHandler(this::stop);

    // Top-level game-mode flags and counters
    GameState state = new GameState();

    // Per-frame physics flags (transient — not part of GameState)
    boolean horizontalCollision = false;
    boolean canJump = false;
    boolean paused = false;          // true while the window has lost focus
    static final long FLICKER_DURATION_MS = 2000;

    // Menu button variables
    Rectangle[] levelButtons = new Rectangle[3]; // index 0 = level 1, etc.
    Rectangle playAgain_Button;
    Rectangle restartGame_Button;
    Font menu_font;
    Font instruction_Font;

    // Game resources
    Image heartPic;               // Heart image for lives display
    Image soundIcon;              // Sound icon image for HUD
    Font gameOverFont;              // Font for game over screen
    Player playerObj;
    Sprite player = null;         // alias for playerObj.sprite() to minimise call-site churn
    List<Tile> collidedTiles = Collections.emptyList();

    CollectibleManager collectibles = new CollectibleManager();
    EnemyManager enemyMgr = new EnemyManager();
    HawkManager hawks = new HawkManager();
    ShieldManager shields = new ShieldManager();
    ParticleManager particles = new ParticleManager();
    LevelManager levelMgr;
    // Aliases (set after levelMgr is constructed) so existing call sites keep compiling
    TileMap tmap;
    Sprite igloo;

    // Background images and parallax scrolling using 4 layers
    ParallaxBackground background;

    /**
     * The main method serves as the entry point for the game.
     * It initializes the game, sets up the window, and starts the game loop.
     *
     * @param args Command-line arguments (not used in this program)
     */
    public static void main(String[] args) {

        Sound backgroundMusic = AssetLoader.backgroundMusic();
        backgroundMusic.start(); // This will start playing the bg music

        Game gct = new Game();
        gct.init();
        gct.setSize(screenWidth, screenHeight);
        gct.setVisible(true);
        gct.run(false, screenWidth, screenHeight);
        
    }

    /**
     * Initialise the game by setting up variables, loading resources,
     * creating animations, and registering event handlers.
     * This method is called once at the start of the game.
     */
    public void init() {

        // Build the level manager (loads the tile map and igloo sprite)
        levelMgr = new LevelManager(this);
        tmap = levelMgr.tileMap();
        igloo = levelMgr.igloo();

        setSize(screenWidth, screenHeight);
        setVisible(true);

        // Initialize menu components
        menu_font = new Font("Arial", Font.BOLD, 48);
        instruction_Font = new Font("Arial", Font.PLAIN, 20);
        
        // Three level-select buttons stacked vertically on the left half of the menu
        int buttonWidth = 240;
        int buttonHeight = 56;
        int buttonX = screenWidth / 4 - buttonWidth / 2 + 60; // left-of-centre block
        int firstButtonY = 260;
        int buttonGap = 16;
        for (int i = 0; i < 3; i++) {
            levelButtons[i] = new Rectangle(buttonX,
                                            firstButtonY + i * (buttonHeight + buttonGap),
                                            buttonWidth, buttonHeight);
        }

        // play again button
        playAgain_Button = new Rectangle((screenWidth - buttonWidth) / 2,
                                   (screenHeight - buttonHeight) / 2 + 50,
                                   buttonWidth, buttonHeight);

        // restart game button
        restartGame_Button = new Rectangle((screenWidth - buttonWidth) / 2,
                                   (screenHeight - buttonHeight) / 2 + 50,
                                   buttonWidth, buttonHeight);

        // Add mouse listener
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleMouseClick(evt);
            }
        });

        // Pause gameplay when the window loses focus (e.g. alt-tab)
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent e) { paused = false; }
            public void windowLostFocus(java.awt.event.WindowEvent e)   { paused = true; }
        });

        // Load the background images and create the parallax background
        Image[] backgroundLayers = new Image[] {
            loadImage("images/Desert mountains - Layer 1.png"),
            loadImage("images/Desert mountains - Layer 2.png"),
            loadImage("images/Desert mountains - Layer 3.png"),
            loadImage("images/Desert mountains - Layer 4.png"),
        };
        background = new ParallaxBackground(backgroundLayers, screenWidth, screenHeight);

        // Load heart image and sound icon for HUD
        heartPic = loadImage("images/heart.png");
        soundIcon = loadImage("images/sound.png");

        // Initialize game over font
        gameOverFont = new Font("Arial", Font.BOLD, 48);

        // Create animation for the main player
        Animation onGround = new Animation();
        onGround.loadAnimationFromSheet("images/penguin.png", 4, 1, 120);

        // Initialise the player with an animation
        playerObj = new Player(onGround);
        player = playerObj.sprite();

        initialiseGame();

        System.out.println(tmap);
    }

    /**
     * Sets up or resets the game state to its initial values.
     * This method is called when starting a new game or restarting a level.
     */
    public void initialiseGame() {
        initialiseGame(1);
    }

    /** Start (or restart) the game at a specific level number. */
    public void initialiseGame(int level) {
        playerObj.respawn(100, 475);
        canJump = false;
        state.resetForNewGame();
        levelMgr.loadLevel(level, collectibles, enemyMgr, hawks);
        shields.loadLevel(level);
        particles.reset();
    }

    /**
     * Draws the current state of the game, including the background,
     * player, enemies, stars, and UI elements.
     *
     * @param g The Graphics2D object used for rendering
     */
    public void draw(Graphics2D g) {
        if (state.inMenu) {
            drawMenu(g);
            return;
        }

        // Draws the background layers with parallax scrolling
        background.draw(g);
    
        // Calculate view offset based on player position
        int xo = -(int) player.getX() + 300;
        int yo = 0;
    
        // Apply offsets to tile map and draws it
        tmap.draw(g, xo, yo);
    
        // Apply offsets to player and draw with flickering effect
        player.setOffsets(xo, yo);
        if (state.isFlickering) {
            // Toggle visibility every 100ms for flickering effect
            if ((System.currentTimeMillis() - state.flickerStartTime) % 200 < 100) {
                player.drawTransformed(g);
            }
        } else {
            player.drawTransformed(g);
        }
    
        // Applying offsets to igloo and then drawing it
        igloo.setOffsets(xo, yo);
        igloo.draw(g);
    
        // Draw stars and enemies via their managers
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
            // When in debug mode, you could draw borders around objects
            // and write messages to the screen with useful information.
            g.setColor(Color.red);
            player.drawBoundingCircle(g);
        
            // Draw bounding box for igloo in debug mode
            g.setColor(Color.orange);
            igloo.drawBoundingBox(g);

            // Display level info 
            g.setColor(Color.darkGray);
            g.drawString(String.format("Level: %d", levelMgr.currentLevel()), getWidth() - 200, 150);
                    
            g.drawString(String.format("Can Jump: %s", canJump ? "Yes" : "No"),
                    getWidth() - 200, 110);

            // Draw screen boundaries in debug mode
            g.setColor(Color.green);
            g.drawRect((int)levelMgr.leftBoundary() + xo, (int)levelMgr.topBoundary() + yo,
                      (int)(levelMgr.rightBoundary() - levelMgr.leftBoundary()),
                      (int)(levelMgr.bottomBoundary() - levelMgr.topBoundary()));
            
            drawCollidedTiles(g, tmap, xo, yo);
        }
    }

    /**
     * Draws the menu screen with title, buttons, and instructions.
     *
     * @param g The Graphics2D object used for rendering
     */
    private void drawMenu(Graphics2D g) {
        // Draw background layers with parallax scrolling
        background.draw(g);

        // Added a semi-transparent black overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, screenWidth, screenHeight);

        // Draw title with shadow effect - positioned on the left side
        g.setFont(menu_font);
        // Draw shadow
        g.setColor(new Color(0, 0, 0, 150));
        String title = "Penguin Adventure";
        FontMetrics fontMetric = g.getFontMetrics();
        int titleX = screenWidth / 6; 
        int titleY = 200;  
        g.drawString(title, titleX, titleY);
        g.setColor(Color.WHITE);
        g.drawString(title, titleX - 2, titleY - 2);

        // Drawing three level-select buttons
        g.setFont(new Font("Arial", Font.BOLD, 24));
        fontMetric = g.getFontMetrics();
        for (int i = 0; i < 3; i++) {
            Rectangle btn = levelButtons[i];

            // Button background
            g.setColor(new Color(70, 130, 180, 200));
            g.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);

            // Button border
            g.setColor(new Color(255, 255, 255, 150));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 15, 15);

            // Button label
            g.setColor(Color.WHITE);
            String label = "Level " + (i + 1);
            int lx = btn.x + (btn.width - fontMetric.stringWidth(label)) / 2;
            int ly = btn.y + (btn.height + fontMetric.getAscent()) / 2;
            g.drawString(label, lx, ly);
        }

        // Drawing an instructions box 
        int boxWidth = 350;  
        int boxHeight = 350; 
        int instructionBoxX = screenWidth - boxWidth - 50; 
        int instructionBoxY = screenHeight / 5; 
        
        // Draw semi-transparent over the instruction box background
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(instructionBoxX, instructionBoxY, boxWidth, boxHeight, 20, 20);
        
        // Draw box border
        g.setColor(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(instructionBoxX, instructionBoxY, boxWidth, boxHeight, 20, 20);

        // Putting the instruction text
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

        int instructionY = instructionBoxY + 40;  // Top padding
        for (String instruction : instructions) {
            fontMetric = g.getFontMetrics();
            int instructionX = instructionBoxX + 20; // Left padding
            g.drawString(instruction, instructionX, instructionY);
            instructionY += 35;  // Spacing between lines
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

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
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
            // Puff of dust at the player's feet on touchdown.
            particles.spawnLandingDust(player.getX() + player.getWidth() / 2f,
                                       player.getY() + player.getHeight());
        }
        // Recompute canJump every frame so walking off a ledge cleanly drops the
        // grounded flag (otherwise gravity-hold in Player would let us air-walk).
        canJump = tileResult.landedOnGround || tileResult.onGround;

        // Update flickering state
        if (state.isFlickering) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - state.flickerStartTime >= FLICKER_DURATION_MS) {
                state.isFlickering = false;
                player.show();
            }
        }

        // Updates the background position for parallax scrolling
        background.update(elapsed, input.isMoveRight(), input.isMoveLeft(), horizontalCollision);

        // Delegate physics + movement + jump to Player
        boolean jumped = playerObj.update(elapsed, input, canJump);
        if (jumped) {
            canJump = false;
            try {
                AssetLoader.jumpSound().start();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Update collectibles (animation + pickup)
        collectibles.update(elapsed, player);
        shields.update(elapsed, player, state);
        particles.update(elapsed);

        // Update enemies (movement + player hit detection)
        boolean enemyHit = enemyMgr.update(elapsed, player, tmap);
        boolean hawkHit = hawks.update(elapsed, player);
        if (enemyHit || hawkHit) {
            if (state.shields > 0) {
                // Shield absorbs the hit — no life lost, brief flicker only.
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

        // Check for collisions with screen edges
        handleScreenEdge(player, tmap, elapsed);

        // Checks for collision with igloo
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
                    state.resetForNewLevel();   // restore lives to 3 for the new level
                    levelMgr.loadNextLevel(collectibles, enemyMgr, hawks);
                    shields.loadLevel(levelMgr.currentLevel());
                    particles.reset();
                    state.levelCompleted = false;
                }
            }
        }
    }

    /**
     * Checks and handles collisions with the edge of the screen.
     */
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

    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     * 
     *  @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) { input.handleKeyPressed(e); }

    /**
     * Handles keyboard input when a key is released.
     *
     * @param e The KeyEvent object representing the key release
     */
    public void keyReleased(KeyEvent e) { input.handleKeyReleased(e); }

    /**
     * Handles mouse click events, such as starting the game or toggling sound.
     *
     * @param evt The MouseEvent object representing the mouse click
     */
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
                    state.inMenu = true;  // Returns to the menu when restarting
                }
            } else {
                if (playAgain_Button.contains(evt.getPoint())) {
                    state.gameOver = false;
                    state.levelCompleted = false;
                    playerObj.respawn(100, 475);
                    state.resetForNewLevel();   // 3 fresh lives
                    levelMgr.reloadCurrent(collectibles, enemyMgr, hawks);
                }
            }
        }

        // Check if click is within the sound icon
        Rectangle soundIconBounds = HudRenderer.soundIconBounds(getWidth());
        if (soundIconBounds.contains(evt.getPoint())) {
            state.isMuted = !state.isMuted;
            Sound.setGlobalMute(state.isMuted);
        }
    }
}