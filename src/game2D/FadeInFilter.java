package game2D;

import java.io.*;

/**
 * A filter that gradually increases the volume of audio samples (fade-in effect).
 */
public class FadeInFilter extends SoundFilter {
    private float currentVolume = 0.0f; // Current volume level
    private float fadeStep; // Step size for increasing the volume
    private boolean fadeComplete = false; // Indicates if the fade-in filter is complete

    /**
     * Constructor for creating a FadeInFilter.
     *
     * @param in            The input stream of the audio.
     * @param fadeDurationMs The duration of the fade-in effect in milliseconds.
     * @param sampleRate     The sample rate of the audio.
     */
    public FadeInFilter(InputStream in, int fadeDurationMs, int sampleRate) {
        super(in);
        float totalSamples = (sampleRate * fadeDurationMs) / 1000f;
        this.fadeStep = 1.0f / totalSamples;
    }

    /**
     * Reads and processes audio samples, applying the fade-in effect.
     *
     * @param sample The audio sample buffer.
     * @param offset The offset in the buffer.
     * @param length The length of the buffer to read.
     * @return The number of bytes read.
     * @throws IOException If an I/O error occurs.
     */
    public int read(byte[] sample, int offset, int length) throws IOException {
        int bytesRead = super.read(sample, offset, length);

        if (fadeComplete) {
            return bytesRead;
        }

        for (int i = 0; i < bytesRead; i += 2) {
            short amp = getSample(sample, offset + i);
            amp = (short) (amp * currentVolume);
            setSample(sample, offset + i, amp);

            // Increase volume
            currentVolume += fadeStep;
            if (currentVolume >= 1.0f) {
                currentVolume = 1.0f;
                fadeComplete = true;
            }
        }

        return bytesRead;
    }
}