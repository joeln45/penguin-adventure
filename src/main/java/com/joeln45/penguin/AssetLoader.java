package com.joeln45.penguin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.joeln45.penguin.engine.EchoFilter;
import com.joeln45.penguin.engine.FadeInFilter;
import com.joeln45.penguin.engine.Sound;
import com.joeln45.penguin.engine.VolumeBoostFilter;

/**
 * Loads bundled images/sounds from the classpath so the game runs the same
 * from an IDE and from a packaged JAR.
 */
public final class AssetLoader {

    private AssetLoader() {}

    public static InputStream openResource(String name) throws FileNotFoundException {
        InputStream in = AssetLoader.class.getClassLoader().getResourceAsStream(name);
        if (in != null) {
            return in;
        }
        return new FileInputStream(name);
    }

    public static Sound backgroundMusic() {
        Sound s = new Sound("sounds/background_music.mid");
        s.setVolume(0.5f);
        return s;
    }

    public static Sound jumpSound() throws FileNotFoundException {
        return new Sound("sounds/jump.wav",
                new EchoFilter(openResource("sounds/jump.wav"), 100, 0.5f, 44100));
    }

    public static Sound coinSound() {
        return new Sound("sounds/coin.wav");
    }

    public static Sound attackSound() throws FileNotFoundException {
        return new Sound("sounds/attack.wav",
                new VolumeBoostFilter(openResource("sounds/attack.wav"), 0.25f));
    }

    public static Sound gameOverSound() {
        return new Sound("sounds/game_over.wav");
    }

    // 2 second fade-in so the sting doesn't blow your ears off
    public static Sound levelCompleteSound() throws FileNotFoundException {
        return new Sound("sounds/level_complete.wav",
                new FadeInFilter(openResource("sounds/level_complete.wav"), 2000, 44100));
    }
}
