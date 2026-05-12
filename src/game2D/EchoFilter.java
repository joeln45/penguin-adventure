package game2D;

import java.io.*;

/**
 * A filter that adds an echo effect to sound.
 */
public class EchoFilter extends SoundFilter {
    private short[] delayBuffer; // Buffer to store delayed samples
    private int delayBufferPos; // Current position in the delay buffer
    private float decay; // Decay factor for the echo
    private int delayInMs; // Delay duration in milliseconds
    private int sampleRate; // Sample rate of the audio

    /**
     * Constructor for creating an EchoFilter.
     *
     * @param in         The input stream of the audio.
     * @param delayInMs  The delay duration in milliseconds.
     * @param decay      The decay factor for the echo.
     * @param sampleRate The sample rate of the audio.
     */
    public EchoFilter(InputStream in, int delayInMs, float decay, int sampleRate) {
        super(in);
        this.delayInMs = delayInMs;
        this.decay = decay;
        this.sampleRate = sampleRate;

        int numSamples = (delayInMs * sampleRate) / 1000;
        delayBuffer = new short[numSamples];
        delayBufferPos = 0;
    }

    /**
     * Reads and processes audio samples,and then applying the echo effect.
     *
     * @param sample The audio sample buffer.
     * @param offset The offset in the buffer.
     * @param length The length of the buffer to read.
     * @return The number of bytes read.
     * @throws IOException If an I/O error occurs.
     */
    public int read(byte[] sample, int offset, int length) throws IOException {
        int bytesRead = super.read(sample, offset, length);

        for (int i = 0; i < bytesRead; i += 2) {
            short original = getSample(sample, offset + i);

            // Get the delayed sample
            short delayed = delayBuffer[delayBufferPos];

            // Mix the original with the delayed sample
            short mixed = (short) (original + (delayed * decay));
            setSample(sample, offset + i, mixed);

            // Store the original in the delay buffer
            delayBuffer[delayBufferPos] = original;
            delayBufferPos = (delayBufferPos + 1) % delayBuffer.length;
        }

        return bytesRead;
    }
}