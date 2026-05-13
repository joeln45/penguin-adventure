package com.joeln45.penguin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.joeln45.penguin.engine.EchoFilter;
import com.joeln45.penguin.engine.FadeInFilter;
import com.joeln45.penguin.engine.Sound;
import com.joeln45.penguin.engine.VolumeBoostFilter;

/**
 * Centralised asset access for the game.
 *
 * <p>Resolves bundled resources via the classpath (so the game runs identically
 * from an IDE or a packaged JAR), and exposes named factory methods for each
 * sound effect so call sites stay readable.
 *
 * @author Joel Nirmal
 */
public final class AssetLoader {

    private AssetLoader() {}

    /**
     * Opens a bundled resource (under {@code src/main/resources}) as a stream.
     * Falls back to the filesystem only if the classpath lookup fails.
     */
    public static InputStream openResource(String name) throws FileNotFoundException {
        InputStream in = AssetLoader.class.getClassLoader().getResourceAsStream(name);
        if (in != null) {
            return in;
        }
        return new FileInputStream(name);
    }

    // ── Sound factories ─────────────────────────────────────────────────────

    /** Looping MIDI background music, played at 50% volume. */
    public static Sound backgroundMusic() {
        Sound s = new Sound("sounds/background_music.mid");
        s.setVolume(0.5f);
        return s;
    }

    /** Jump sound effect with a short echo. */
    public static Sound jumpSound() throws FileNotFoundException {
        return new Sound("sounds/jump.wav",
                new EchoFilter(openResource("sounds/jump.wav"), 100, 0.5f, 44100));
    }

    /** Coin pickup sound (no filter). */
    public static Sound coinSound() {
        return new Sound("sounds/coin.wav");
    }

    /** Player-damage sound, slightly boosted. */
    public static Sound attackSound() throws FileNotFoundException {
        return new Sound("sounds/attack.wav",
                new VolumeBoostFilter(openResource("sounds/attack.wav"), 0.25f));
    }

    /** Game-over jingle (no filter). */
    public static Sound gameOverSound() {
        return new Sound("sounds/game_over.wav");
    }

    /** Level-complete sting with a 2-second fade-in. */
    public static Sound levelCompleteSound() throws FileNotFoundException {
        return new Sound("sounds/level_complete.wav",
                new FadeInFilter(openResource("sounds/level_complete.wav"), 2000, 44100));
    }
}
