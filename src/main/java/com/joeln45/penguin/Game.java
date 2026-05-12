package com.joeln45.penguin;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.joeln45.penguin.engine.*;

/**
 * The Game class extends the GameCore class to create a custom game.
 * It handles the initialization, game logic, rendering, and user input.
 * This class includes features such as parallax scrolling, collision detection,
 * and level progression.
 */
// Student ID: 3141744

@SuppressWarnings("serial")
public class Game extends GameCore {
    // Useful game constants
    static int screenWidth = 1200;
    static int screenHeight = 505;

    // Screen edge boundaries 
    float leftBoundary = 0;
    float rightBoundary = screenWidth;
    float topBoundary = 43;
    float bottomBoundary = screenHeight;

    // Game constants
    float gravity = 0.0006f;        
    float moveSpeed = 0.15f;
    float jumpSpeed = -0.32f;       
    float terminalVelocity = 0.3f;   

    // Game state flags
    boolean moveRight = false;
    boolean moveLeft = false;
    boolean debug = false;
    boolean horizontalCollision = false;
    boolean jump = false;
    boolean canJump = false;
    boolean wasOnGround = false;    // Track if player was on ground last frame
    boolean levelCompleted = false;
    boolean gameOver = false;       // Track if game is over
    boolean isFlickering = false;   // Track if the player is flickering
    boolean inMenu = true;          // Track if the game is on the menu page
    long flickerStartTime = 0;      // flickering start
    long flickerDuration = 2000;    // Duration of flickering 
    int currentLevel = 1;
    int lives = 2;                  // Player starts with 2 lives

    // Menu button variables
    Rectangle startButton;
    Rectangle playAgain_Button; 
    Rectangle restartGame_Button; 
    Font menu_font;
    Font instruction_Font;

    // Game resources
    Animation onGround;
    Image heartPic;               // Heart image for lives display
    Font gameOverFont;              // Font for game over screen
    boolean gameCompleted = false;   // Track if all levels are completed

    Sprite player = null;
    Sprite igloo = null;
    ArrayList<Tile> collidedTiles = new ArrayList<Tile>();

    ArrayList<Sprite> stars = new ArrayList<Sprite>();
    Animation starAnim;
    int starsCollected = 0;

    // Enemy sprite
    ArrayList<Sprite> enemies = new ArrayList<Sprite>();
    Animation enemyAnim;
    float enemySpeed = 0.05f; // Speed for enemy movement
    boolean[] enemyMovingRight; // Track direction for each enemy

    TileMap tmap = new TileMap();

    // Background images and parallax scrolling using 4 layers
    Image[] backgroundLayers = new Image[4];
    float[] backgroundX = new float[4];
    float[] backgroundX2 = new float[4];
    float[] layerSpeeds = {0.01f, 0.02f, 0.04f, 0.08f};

    // Add a new boolean to track mute state
    boolean isMuted = false;

    
    /**
     * The main method serves as the entry point for the game.
     * It initializes the game, sets up the window, and starts the game loop.
     *
     * @param args Command-line arguments (not used in this program)
     */
    public static void main(String[] args) {

        Sound backgroundMusic = new Sound("sounds/background_music.mid");
        backgroundMusic.setVolume(0.5f); // Sets volume to 50%
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

        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("maps", "map1.txt");

        setSize(screenWidth, screenHeight);
        setVisible(true);

        // Initialize menu components
        menu_font = new Font("Arial", Font.BOLD, 48);
        instruction_Font = new Font("Arial", Font.PLAIN, 20);
        
        // Create start button (position: left-middle of the screen)
        int buttonWidth = 200;
        int buttonHeight = 60;
        int buttonX = screenWidth / 4;  // Positioned at 1/6 of screen width
        int buttonY = 300;  // Centered vertically
        startButton = new Rectangle(buttonX, buttonY, buttonWidth, buttonHeight);

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

        // Load the background images
        backgroundLayers[0] = loadImage("images/Desert mountains - Layer 1.png");
        backgroundLayers[1] = loadImage("images/Desert mountains - Layer 2.png");
        backgroundLayers[2] = loadImage("images/Desert mountains - Layer 3.png");
        backgroundLayers[3] = loadImage("images/Desert mountains - Layer 4.png");

        // Load heart image
        heartPic = loadImage("images/heart.png");

        // Initialize game over font
        gameOverFont = new Font("Arial", Font.BOLD, 48);

        // Initialize background positions
        for (int i = 0; i < 4; i++) {
            backgroundX[i] = 0;
            backgroundX2[i] = screenWidth;
        }

        // Create animation for the main player
        onGround = new Animation();
        onGround.loadAnimationFromSheet("images/penguin.png", 4, 1, 120);

        // Initialise the player with an animation
        player = new Sprite(onGround);
        
        // Load the igloo image and create a sprite for initialization
        Animation iglooAnim = new Animation();
        iglooAnim.addFrame(loadImage("images/igloo.png"), 1000); 
        igloo = new Sprite(iglooAnim);

        initialiseGame();

        
        System.out.println(tmap);
    }

    /**
     * Sets up or resets the game state to its initial values.
     * This method is called when starting a new game or restarting a level.
     */
    public void initialiseGame() {
        currentLevel = 1;           
        
        // Loads the map1.txt file for level 1
        tmap.loadMap("maps", "map1.txt");
        
        player.setPosition(100, 475);
        player.setVelocity(0, 0);
        player.show();
        
        canJump = false;
        wasOnGround = false;
        levelCompleted = false;
        starsCollected = 0;
        lives = 2;                  // this variable is to reset lives when starting new game
        gameOver = false;           
        gameCompleted = false;      
        gameOverSoundPlayed = false;
    
        // seting up the screen boundaries
        setScreenBoundaries(20, 2035, 65, 480);
        
        initializeStars(currentLevel);
        initializeIgloo(currentLevel);
        initializeEnemies(currentLevel);
    }

    /**
     * Initializes the igloo for the current level with predefined positions.
     *
     * @param level The current level number
     */
    public void initializeIgloo(int level) {
        if (igloo == null) {
            Animation iglooAnim = new Animation();
            iglooAnim.addFrame(loadImage("images/igloo.png"), 1000); // Static image
            igloo = new Sprite(iglooAnim);
        }

        if (level == 1) {
            iglooPosition(1920, 160); 
        } else if (level == 2) {
            iglooPosition(1910, 319);
        }
        
        igloo.show();
    }

    /**
     * Initializes the stars for the current level.
     * Clears any existing stars and sets up new ones based on the level.
     *
     * @param level The current level number
     */
    public void initializeStars(int level) {
        // Clears the existing stars
        stars.clear();
        
        // Creates star animation
        starAnim = new Animation();
        starAnim.loadAnimationFromSheet("images/star.png", 5, 1, 200);
        
        if (level == 1) {
            // Level 1 stars positions
            addStar(275, 115);
            addStar(900, 400);
            addStar(1620, 370);
        } else if (level == 2) {
            // Level 2 stars positions
            addStar(1020, 300);
            addStar(1050, 70);
            addStar(1380, 375);
        }
    }

    /**
     * Adds a star at the specified position.
     *
     * @param x The x-coordinate of the star
     * @param y The y-coordinate of the star
     */
    public void addStar(float x, float y) {
        Sprite star = new Sprite(starAnim);
        star.setPosition(x, y);
        star.setScale(0.7f); // makes it a bit smaller
        star.show();
        stars.add(star);
    }

    /**
     * Initializes the enemies for the current level.
     * Clears any existing enemies and sets up new ones based on the level.
     *
     * @param level The current level number
     */
    public void initializeEnemies(int level) {
        // Clears the existing enemies
        enemies.clear();
        
        // Create enemy animation
        enemyAnim = new Animation();
        enemyAnim.loadAnimationFromSheet("images/enemy.png", 7, 1, 150);
        
        // Different positions for different levels
        if (level == 1) {
            // Level 1 enemy positions
            addEnemy(550, 452);
            addEnemy(400, 163);
            addEnemy(1350, 228);
        } else if (level == 2) {
            // Level 2 enemy positions
            addEnemy(600, 355);
            addEnemy(1000, 130);
            addEnemy(1500, 451);
        }
        
        // Initialize direction for the movement of the enemy, towards right
        enemyMovingRight = new boolean[enemies.size()];
        for (int i = 0; i < enemyMovingRight.length; i++) {
            enemyMovingRight[i] = true; 
        }
    }

   /**
     * Adds an enemy at the specified position.
     *
     * @param x The x-coordinate of the enemy
     * @param y The y-coordinate of the enemy
     */
    public void addEnemy(float x, float y) {
        Sprite enemy = new Sprite(enemyAnim);
        enemy.setPosition(x, y);
        enemy.setSpeedX(enemySpeed);
        enemy.show();
        enemies.add(enemy);
    }

    /**
     * Draws the current state of the game, including the background,
     * player, enemies, stars, and UI elements.
     *
     * @param g The Graphics2D object used for rendering
     */
    public void draw(Graphics2D g) {
        if (inMenu) {
            drawMenu(g);
            return;
        }

        // Draws the background layers with parallax scrolling
        for (int i = 0; i < 4; i++) {
            g.drawImage(backgroundLayers[i], (int) backgroundX[i], 0, screenWidth, screenHeight, null);
            g.drawImage(backgroundLayers[i], (int) backgroundX2[i], 0, screenWidth, screenHeight, null);
        }
    
        // Calculate view offset based on player position
        int xo = -(int) player.getX() + 300;
        int yo = 0;
    
        // Apply offsets to tile map and draws it
        tmap.draw(g, xo, yo);
    
        // Apply offsets to player and draw with flickering effect
        player.setOffsets(xo, yo);
        if (isFlickering) {
            // Toggle visibility every 100ms for flickering effect
            if ((System.currentTimeMillis() - flickerStartTime) % 200 < 100) {
                player.drawTransformed(g);
            }
        } else {
            player.drawTransformed(g);
        }
    
        // Applying offsets to igloo and then drawing it
        igloo.setOffsets(xo, yo);
        igloo.draw(g);
    
        // Drawing stars with proper offsets
        for (Sprite star : stars) {
            star.setOffsets(xo, yo);
            star.draw(g);
            
            // Drawing bounding box for stars in debug mode
            if (debug) {
                g.setColor(Color.black);
                star.drawBoundingCircle(g);  //from Sprite class
            }
        }

        for (Sprite enemy : enemies) {
            enemy.setOffsets(xo, yo);
            enemy.drawTransformed(g); // Use drawTransformed for proper facing direction
            
            // Draw bounding box for enemies in debug mode
            if (debug) {
                g.setColor(Color.red);
                enemy.drawBoundingBox(g);
            }
        }
    
        // Show no. of stars collected with a background panel
        g.setColor(new Color(0, 0, 0, 120)); 
        g.fillRoundRect(10, 60, 130, 40, 10, 10); 
        
        // Draw border for the panel
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(10, 60, 130, 40, 10, 10);
        
        // Draw star counter text with "Stars:" 
        g.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g.getFontMetrics();
        
        // color "Stars:" in yellow
        g.setColor(Color.YELLOW);
        g.drawString("Stars:", 20, 85);
        
        // Draw the number of stars in yellow
        g.setColor(Color.YELLOW);
        String numbersText = String.format("%d/%d", starsCollected, 3);
        int numbersX = 20 + fm.stringWidth("Stars: ");
        g.drawString(numbersText, numbersX, 85);

        // Drawong hearts
        int heartSize = 30;
        int heartGap = 35;
        int heartX = getWidth() - 130; //placing the hearts to the right side of the screen
        int heartY = 70;  // Y position for hearts
        for (int i = 0; i < lives; i++) {
            g.drawImage(heartPic, heartX, heartY, heartSize, heartSize, null);
            heartX += heartGap;
        }

        // Draw sound icon
        int soundIconSize = 30;
        int soundIconX = getWidth() - 50; 
        int soundIconY = 70;  
        Image soundIcon = loadImage("images/sound.png");
        g.drawImage(soundIcon, soundIconX, soundIconY, soundIconSize, soundIconSize, null);

        // Draw mute overlay if sound is muted
        if (isMuted) {
            g.setColor(new Color(255, 0, 0, 150)); // drawing a semi-transparent red overlay
            g.fillOval(soundIconX, soundIconY, soundIconSize, soundIconSize);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("X", soundIconX + 10, soundIconY + 20); // Draw "X" on top
        }

        // Drawing game over screen if game is over
        if (gameOver) {
            // Create semi-transparent black overlay over the entire game
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(0, 0, getWidth(), getHeight());

            // Set up text rendering for game over screen
            g.setFont(gameOverFont);
            g.setColor(Color.WHITE);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Drawing text based on game state
            String gameOverText = gameCompleted ? "Congratulations!" : "GAME OVER";
            fm = g.getFontMetrics();
            int gameOverX = (getWidth() - fm.stringWidth(gameOverText)) / 2;
            int gameOverY = getHeight() / 2 - 100;  
            g.drawString(gameOverText, gameOverX, gameOverY);

            // Draw completion message if game is completed
            if (gameCompleted) {
                g.setFont(new Font("Arial", Font.BOLD, 32));
                String completionText = "You completed all levels!";
                fm = g.getFontMetrics();
                int gameCompleteX = (getWidth() - fm.stringWidth(completionText)) / 2;
                int gameCompleteY = gameOverY + 60;
                g.drawString(completionText, gameCompleteX, gameCompleteY);
            }

            // Draw appropriate button based on game state (Game over or restart game)
            Rectangle currentGameButton = gameCompleted ? restartGame_Button : playAgain_Button;
            String buttonText = gameCompleted ? "Restart Game" : "Play Again";
            
            // Draw button for the above
            g.setColor(new Color(70, 130, 180, 200)); 
            g.fillRoundRect(currentGameButton.x, currentGameButton.y, currentGameButton.width, currentGameButton.height, 15, 15);
            
            // Button border
            g.setColor(new Color(255, 255, 255, 150));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(currentGameButton.x, currentGameButton.y, currentGameButton.width, currentGameButton.height, 15, 15);
            
            // Button text
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 28));
            fm = g.getFontMetrics();
            int buttonX = currentGameButton.x + (currentGameButton.width - fm.stringWidth(buttonText)) / 2;
            int buttonY = currentGameButton.y + (currentGameButton.height + fm.getAscent()) / 2;
            g.drawString(buttonText, buttonX, buttonY);
        }
    
        if (debug) {
            // When in debug mode, you could draw borders around objects
            // and write messages to the screen with useful information.
            g.setColor(Color.red);
            player.drawBoundingCircle(g);
        
            // Draw bounding box for igloo in debug mode
            g.setColor(Color.orange);
            igloo.drawBoundingBox(g);

            // Display level info 
            g.setColor(Color.darkGray);
            g.drawString(String.format("Level: %d", currentLevel), getWidth() - 200, 150);
                    
            g.drawString(String.format("Can Jump: %s", canJump ? "Yes" : "No"),
                    getWidth() - 200, 110);

            // Draw screen boundaries in debug mode
            g.setColor(Color.green);
            g.drawRect((int)leftBoundary + xo, (int)topBoundary + yo, 
                      (int)(rightBoundary - leftBoundary), (int)(bottomBoundary - topBoundary));
            
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
        for (int i = 0; i < 4; i++) {
            g.drawImage(backgroundLayers[i], (int) backgroundX[i], 0, screenWidth, screenHeight, null);
            g.drawImage(backgroundLayers[i], (int) backgroundX2[i], 0, screenWidth, screenHeight, null);
        }

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

        // Drawing start button with hover effect 
        g.setColor(new Color(70, 130, 180, 200)); 
        g.fillRoundRect(startButton.x, startButton.y, startButton.width, startButton.height, 15, 15);
        // Button border
        g.setColor(new Color(255, 255, 255, 150));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(startButton.x, startButton.y, startButton.width, startButton.height, 15, 15);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        String startText = "Start Game";
        fontMetric = g.getFontMetrics();
        int startX = startButton.x + (startButton.width - fontMetric.stringWidth(startText)) / 2;
        int startY = startButton.y + (startButton.height + fontMetric.getAscent()) / 2;
        g.drawString(startText, startX, startY);

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

    boolean gameOverSoundPlayed = false;

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void update(long elapsed) {
        if (gameOver) {
            if (!gameOverSoundPlayed) {
                Sound gameOverSound = new Sound("sounds/game_over.wav");
                    gameOverSound.start();
                gameOverSoundPlayed = true;
            }
            return;
        }

        wasOnGround = false;
        
        horizontalCollision = false;
        checkTileCollision(player, tmap);

        // Update flickering state
        if (isFlickering) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - flickerStartTime >= flickerDuration) {
                isFlickering = false;
                player.show();
            }
        }

        // Updates the background position for parallax scrolling
        for (int i = 0; i < 4; i++) {
            if (!horizontalCollision) {
                if (moveRight) {
                    backgroundX[i] -= layerSpeeds[i] * elapsed;
                    backgroundX2[i] -= layerSpeeds[i] * elapsed;
                } else if (moveLeft) {
                    backgroundX[i] += layerSpeeds[i] * elapsed;
                    backgroundX2[i] += layerSpeeds[i] * elapsed;
                }
            }
    
            // Ensures that the background layers loop correctly
            if (backgroundX[i] + screenWidth <= 0) {
                backgroundX[i] = backgroundX2[i] + screenWidth;
            } else if (backgroundX2[i] + screenWidth <= 0) {
                backgroundX2[i] = backgroundX[i] + screenWidth;
            } else if (backgroundX[i] >= screenWidth) {
                backgroundX[i] = backgroundX2[i] - screenWidth;
            } else if (backgroundX2[i] >= screenWidth) {
                backgroundX2[i] = backgroundX[i] - screenWidth;
            }
        }
    
        // Apply gravity with terminal velocity limit and with smooth acceleration
        float newSpeedY = player.getVelocityY() + (gravity * elapsed);
        if (newSpeedY > terminalVelocity) {
            newSpeedY = terminalVelocity;
        }
        player.setVelocityY(newSpeedY);
    
        // the jump logic
        if (jump && canJump) {
            // Play the jump sound here with EchoFilter
            try {
                Sound jumpSound = new Sound("sounds/jump.wav", new EchoFilter(new FileInputStream("sounds/jump.wav"), 100, 0.5f, 44100)); // 100ms delay, 0.5 decay
                jumpSound.start();
            } catch (FileNotFoundException e) {
                e.printStackTrace(); 
            }

            player.setAnimationSpeed(1.8f);
            player.setVelocityY(jumpSpeed);
            jump = false;
            canJump = false;

            
            // Add a small horizontal boost when jumping for better control
            if (moveRight) {
                player.setSpeedX(moveSpeed * 1.1f);
            } else if (moveLeft) {
                player.setSpeedX(-moveSpeed * 1.1f);
            }
        }
    
        // Horizontal movement with smoother acceleration/deceleration
        if (moveRight) {
            // Apply gradual acceleration for smoother movement to the right
            float currentSpeedX = player.getSpeedX();
            float targetSpeedX = moveSpeed;
            float acceleration = 0.01f;
            float newSpeedX = currentSpeedX + (acceleration * elapsed);
            
            // Capping the velocity at the moveSpeed
            if (newSpeedX > targetSpeedX) {
                newSpeedX = targetSpeedX;
            }
            
            player.setSpeedX(newSpeedX);
            player.setAnimationSpeed(1.0f); // Set animation speed to normal when moving
            player.setScale(1.0f, 1.0f); // Face right
        } else if (moveLeft) {
            // Apply gradual acceleration for smoother movement to the left
            float currentSpeedX = player.getSpeedX();
            float targetSpeedX = -moveSpeed;
            float acceleration = 0.01f;
            float newSpeedX = currentSpeedX - (acceleration * elapsed);
            
            // Cap the speed at -moveSpeed
            if (newSpeedX < targetSpeedX) {
                newSpeedX = targetSpeedX;
            }
            
            player.setSpeedX(newSpeedX);
            player.setAnimationSpeed(1.0f); // Set animation speed to normal when moving
            player.setScale(-1.0f, 1.0f); // Face left
        } else {
            // Apply deceleration for smoother stopping
            float currentSpeedX = player.getSpeedX();
            float deceleration = 0.008f * elapsed;
            
            if (Math.abs(currentSpeedX) < deceleration) {
                player.setSpeedX(0); // Stop completely if very slow
            } else if (currentSpeedX > 0) {
                player.setSpeedX(currentSpeedX - deceleration);
            } else if (currentSpeedX < 0) {
                player.setSpeedX(currentSpeedX + deceleration);
            }
            
            // Only stop animation when completely stopped
            if (player.getSpeedX() == 0) {
                player.setAnimationSpeed(0);
            }
        }
    
        // Update sprite animation and position
        player.update(elapsed);
        
        // Update all stars
        for (int i = stars.size() - 1; i >= 0; i--) {
            Sprite star = stars.get(i);
            star.update(elapsed);
            
            // Check for collision with player
            if (boundingBoxCollision(player, star)) {
                // Remove the star and increase counter
                stars.remove(i);
                starsCollected++;

                Sound coinSound = new Sound("sounds/coin.wav");
                coinSound.start();
            }
        }

        for (int i = 0; i < enemies.size(); i++) {
            Sprite enemy = enemies.get(i);
            
            // if enemies are hidden, there is no collision
            if (!enemy.isVisible()) continue;
            
            // Update enemy position
            enemy.update(elapsed);
            
            // Check for collision with tiles
            boolean collision = checkEnemyTileCollision(enemy, tmap);
            if (collision) {
                // Reverse the direction
                enemy.setSpeedX(-enemy.getSpeedX());
                enemyMovingRight[i] = !enemyMovingRight[i];
                
                // Flip the sprite to face the right way
                if (enemyMovingRight[i]) {
                    enemy.setScale(1.0f, 1.0f); // right side
                } else {
                    enemy.setScale(-1.0f, 1.0f); // left side
                }
            }
            
            // Check for collision with player
            if (boundingBoxCollision(player, enemy)) {
                lives--;
                // Plays attack sound when player gets damaged
                try {
                    Sound attackSound = new Sound("sounds/attack.wav", new VolumeBoostFilter(new FileInputStream("sounds/attack.wav"), 0.25f)); // 50% volume boost
                    attackSound.start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace(); 
                }
                if (lives <= 0) {
                    // Game over
                    gameOver = true;
                    Sound gameOverSound = new Sound("sounds/game_over.wav");
                    gameOverSound.start();
                    gameOverSoundPlayed = true;
                    
                } else {
                    // Start flickering effect
                    isFlickering = true;
                    flickerStartTime = System.currentTimeMillis();
                    // Hide the enemy that caused the collision
                    enemy.hide();
                }
            }
        }
    
        // Check for collisions with screen edges
        handleScreenEdge(player, tmap, elapsed);
        
        // Checks for collision with igloo
        if (!levelCompleted && boundingBoxCollision(player, igloo)) {
            if (starsCollected >= 3) {
                if (currentLevel == 2) {
                    gameOver = true;
                    gameCompleted = true;
                    // Play level complete sound 
                    try {
                        Sound levelCompleteSound = new Sound("sounds/level_complete.wav", new FadeInFilter(new FileInputStream("sounds/level_complete.wav"), 2000, 44100)); // 1 second fade-in
                        levelCompleteSound.start();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace(); 
                    }
                } else {
                    // Play level complete sound 
                    try {
                        Sound levelCompleteSound = new Sound("sounds/level_complete.wav", new FadeInFilter(new FileInputStream("sounds/level_complete.wav"), 2000, 44100)); // 1 second fade-in
                        levelCompleteSound.start();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    levelCompleted = true;
                    loadLevel2();
                }
            }
        }
    }

    /**
     * Check if an enemy has collided with a tile
     */
    public boolean checkEnemyTileCollision(Sprite enemy, TileMap tmap) {
        float ex = enemy.getX(); //  x-coordinate of the enemy's position
        float ey = enemy.getY(); // y-coordinate of the enemy's position
        float ew = enemy.getWidth(); // width of the enemy sprite
        float eh = enemy.getHeight(); // height of the enemy sprite
        float evx = enemy.getSpeedX(); //horizontal velocity of the enemy
        
        // Find out tile dimensions
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();
        
        // Calculate the new position
        float nextX = ex + evx;
        
        // Determine which tile the enemy is moving into horizontally
        int tileX = evx > 0 ? 
            (int)((nextX + ew - 2) / tileWidth) : 
            (int)(nextX / tileWidth);
        
        // Check the corners on that side
        int topTileY = (int)(ey / tileHeight);
        int bottomTileY = (int)((ey + eh - 3) / tileHeight);
        
        // Check each potential collision along the side
        for (int tileY = topTileY; tileY <= bottomTileY; tileY++) {
            Tile tile = tmap.getTile(tileX, tileY);
            if (tile != null && tile.getCharacter() != '.') {
                return true; // Collision detected
            }
        }
        
        return false; // No collision
    }
    
    /**
     * Load level 2
     */
    private void loadLevel2() {
        currentLevel = 2;
        
        // Resets the player position
        player.setPosition(100, 475);
        player.setVelocity(0, 0);
        
        // Load the map2.txt file
        tmap.loadMap("maps", "map2.txt");
        
        // Reset game state
        canJump = false;
        wasOnGround = false;
        levelCompleted = false;
        starsCollected = 0;
        lives = 2;                  // Reset lives to 2 for level 2
        gameOverSoundPlayed = false; // Reset the flag to allow sound to play again
        
        // Set screen boundaries appropriate for the new map
        setScreenBoundaries(20, 2035, 65, 480);
        
        // Initialize igloo and stars for level 2
        initializeIgloo(currentLevel);
        initializeStars(currentLevel);
        initializeEnemies(currentLevel);
        
        System.out.println("Level 2 loaded");
    }

    /**
     * Checks and handles collisions with the edge of the screen. 
     * 
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check 
     * @param elapsed	How much time has gone by since the last call
     */
    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed) {
        float sx = s.getX(); //  x-coordinate of the sprite's position
        float sy = s.getY(); //y-coordinate of the sprite's position
        float sw = s.getWidth(); // width of the sprite
        float sh = s.getHeight(); // height of the sprite
        
        // Handle bottom edge - it is also used for ground collision
        if (sy + sh > bottomBoundary) {
            s.setY(bottomBoundary - sh);
            s.setVelocityY(0);
            canJump = true;
            wasOnGround = true;
        }
    
        // Handle top edge
        if (sy < topBoundary) {
            s.setY(topBoundary);
            s.setVelocityY(0);
        }
    
        // Handle left edge
        if (sx < leftBoundary) {
            s.setX(leftBoundary);
            s.setSpeedX(0);
        }
        
        // Handle right edge
        if (sx + sw > rightBoundary) {
            s.setX(rightBoundary - sw);
            s.setSpeedX(0);
        }
    }

    /**
     * Sets the screen boundaries
     * @param left Left boundary
     * @param right Right boundary
     * @param top Top boundary
     * @param bottom Bottom boundary
     */
    public void setScreenBoundaries(float left, float right, float top, float bottom) {
        this.leftBoundary = left;
        this.rightBoundary = right;
        this.topBoundary = top;
        this.bottomBoundary = bottom;
    }
    
    /**
     * Set igloo position 
     * @param x X position
     * @param y Y position
     */
    public void iglooPosition(float x, float y) {
        if (igloo != null) {
            igloo.setPosition(x, y);
            igloo.show();
        } else {
            System.out.println("Warning: Igloo sprite is null, cannot set position");
        }
    }

    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     * 
     *  @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        switch (key) {
            case KeyEvent.VK_UP:
                jump = true; 
                break;
            case KeyEvent.VK_RIGHT:
                moveRight = true;
                break;
            case KeyEvent.VK_LEFT:
                moveLeft = true;
                break;
            case KeyEvent.VK_ESCAPE:
                stop();
                break;
            case KeyEvent.VK_B:
                debug = !debug;  // Toggle debug mode
                break;
            default:
                break;
        }
    }

    /** 
     * Detects a bounding box collision between sprites s1 and s2.
     * Uses a slightly smaller hitbox for better feel.
     * 
     * @param s1 First sprite
     * @param s2 Second sprite
     * @return true if a collision has occurred, false if it has not
     */
    public boolean boundingBoxCollision(Sprite s1, Sprite s2) {
        float s1x = s1.getX(); // x-coordinate of the sprite's position
        float s1y = s1.getY(); //y-coordinate of the sprite's position
        float s1w = s1.getWidth(); //width of the sprite
        float s1h = s1.getHeight(); // height of the sprite
        
        float s2x = s2.getX();
        float s2y = s2.getY();
        float s2w = s2.getWidth();
        float s2h = s2.getHeight();
        
        float hitboxMargin = 5.0f;
        
        // Check if the bounding boxes overlap with adjusted margins
        return (s1x + hitboxMargin < s2x + s2w - hitboxMargin && 
                s1x + s1w - hitboxMargin > s2x + hitboxMargin &&
                s1y + hitboxMargin < s2y + s2h - hitboxMargin && 
                s1y + s1h - hitboxMargin > s2y + hitboxMargin);
    }
    
   /**
     * Checks for collisions between the player and tiles in the tile map.
     *
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check 
     */
    public void checkTileCollision(Sprite s, TileMap tmap) {
        // Clears out the current set of collided tiles
        collidedTiles.clear();
    
        // Get sprite dimensions and position
        float sx = s.getX();
        float sy = s.getY();
        float sw = s.getWidth();
        float sh = s.getHeight();
        float velocityX = s.getSpeedX();
        float velocityY = s.getVelocityY();
        
        // Find out tile dimensions
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();
        
        // Avoids doing unnecessary collision checks if velocity is zero
        if (velocityX == 0 && velocityY == 0) {
            checkIfOnGround(s, tmap);
            return;
        }
        
        // Calculate the potential new position
        float nextX = sx + velocityX;
        float nextY = sy + velocityY;
        
        // Check horizontal movement 
        if (velocityX != 0) {
            // Determine which tile the sprite is moving into horizontally
            int tileX = velocityX > 0 ? 
                (int)((nextX + sw - 2) / tileWidth) : // Slight offset to prevent sticking
                (int)(nextX / tileWidth);
            
            // Checks the corners on that side
            int topTileY = (int)(sy / tileHeight);
            int bottomTileY = (int)((sy + sh - 3) / tileHeight); // Slight offset for smoother landing
            
            boolean localHorizontalCollision = false;
            
            // Check each potential collision along the side
            for (int tileY = topTileY; tileY <= bottomTileY; tileY++) {
                Tile tile = tmap.getTile(tileX, tileY);
                if (tile != null && tile.getCharacter() != '.') {
                    collidedTiles.add(tile);
                    localHorizontalCollision = true;
                }
            }
            
            // Handle horizontal collision
            if (localHorizontalCollision) {
                if (velocityX > 0) {
                    // Moving right
                    s.setX(tileX * tileWidth - sw);
                } else {
                    // Moving left
                    s.setX((tileX + 1) * tileWidth);
                }
                s.setSpeedX(0);
                horizontalCollision = true;
            }
        }
        
        sx = s.getX();
        velocityY = s.getVelocityY(); 
        nextY = sy + velocityY;
        
        if (velocityY != 0) {
            // Determine which tile the sprite is moving into vertically
            int tileY = velocityY > 0 ? 
                (int)((nextY + sh - 1) / tileHeight) : // Slight offset for smoother landing
                (int)(nextY / tileHeight);
            
            // Check the corners on that side
            int leftTileX = (int)((sx + 2) / tileWidth); 
            int rightTileX = (int)((sx + sw - 2) / tileWidth); 
            
            boolean verticalCollision = false;
            
            // Check each potential collision along the top or the bottom edge of the tiles
            for (int tileX = leftTileX; tileX <= rightTileX; tileX++) {
                Tile tile = tmap.getTile(tileX, tileY);
                if (tile != null && tile.getCharacter() != '.') {
                    collidedTiles.add(tile);
                    verticalCollision = true;
                }
            }
            
            // Handle vertical collision
            if (verticalCollision) {
                if (velocityY > 0) {
                    // Moving down, hit ground
                    s.setY(tileY * tileHeight - sh);
                    s.setVelocityY(0);
                    canJump = true;
                    wasOnGround = true;
                } else {
                    // Moving up, hit the top
                    s.setY((tileY + 1) * tileHeight);
                    s.setVelocityY(0);
                }
            }
        } else {
            // If we're not moving vertically, it check if we're on ground
            checkIfOnGround(s, tmap);
        }
    }
    
    /**
     * Check if the sprite is standing on ground (for jump handling)
     */
    private void checkIfOnGround(Sprite s, TileMap tmap) {
        float sx = s.getX();
        float sy = s.getY();
        float sw = s.getWidth();
        float sh = s.getHeight();
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();
        
        int groundTileY = (int)((sy + sh + 1) / tileHeight);
        
        int leftTileX = (int)((sx + 2) / tileWidth);
        int rightTileX = (int)((sx + sw - 2) / tileWidth);
        
        // Checks for tiles along the bottom edge
        for (int tileX = leftTileX; tileX <= rightTileX; tileX++) {
            Tile tile = tmap.getTile(tileX, groundTileY);
            if (tile != null && tile.getCharacter() != '.') {
                // if there's ground just below
                canJump = true;
                return;
            }
        }
    }

    /**
     * Handles keyboard input when a key is released.
     *
     * @param e The KeyEvent object representing the key release
     */
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_ESCAPE:
                stop();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_SPACE:
                jump = false;
                break;
            case KeyEvent.VK_RIGHT:
                moveRight = false;
                break;
            case KeyEvent.VK_LEFT:
                moveLeft = false;
                break;
            default:
                break;
        }
    }

    /**
     * Handles mouse click events, such as starting the game or toggling sound.
     *
     * @param evt The MouseEvent object representing the mouse click
     */
    private void handleMouseClick(java.awt.event.MouseEvent evt) {
        if (inMenu) {
            // Check if click is within start button
            if (startButton.contains(evt.getPoint())) {
                inMenu = false;
                initialiseGame();
            }
        } else if (gameOver) {
            if (gameCompleted) {
                // Check if click is within restart game button
                if (restartGame_Button.contains(evt.getPoint())) {
                    gameOver = false;
                    gameCompleted = false;
                    inMenu = true;  // Returns to the menu when restarting
                }
            } else {
                // Check if click is within play again button
                if (playAgain_Button.contains(evt.getPoint())) {
                    gameOver = false;
                    levelCompleted = false; // Resest levelCompleted flag
                    starsCollected = 0; // Resets stars collected
                    // Resets player position and state for current level
                    player.setPosition(100, 475);
                    player.setVelocity(0, 0);
                    lives = 2;
                    // Reinitialize current level
                    initializeStars(currentLevel);
                    initializeIgloo(currentLevel);
                    initializeEnemies(currentLevel);
                }
            }
        }

        // Check if click is within the sound icon
        int soundIconSize = 30;
        int soundIconX = getWidth() - 50;
        int soundIconY = 70;
        Rectangle soundIconBounds = new Rectangle(soundIconX, soundIconY, soundIconSize, soundIconSize);
        if (soundIconBounds.contains(evt.getPoint())) {
            isMuted = !isMuted; // Toggle mute state
            Sound.setGlobalMute(isMuted); // Applys mute state globally except for the background music
        }
    }
}