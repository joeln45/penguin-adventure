package game2D;

import java.io.*;

/**
 * A filter that boosts or decreases the volume of audio samples.
 */
public class VolumeBoostFilter extends SoundFilter {
    private float boostFactor; // The factor by which to boost the volume

    /**
     * Constructor for creating a VolumeBoostFilter.
     *
     * @param in         The input stream of the audio.
     * @param boostFactor The factor by which to boost the volume.
     */
    public VolumeBoostFilter(InputStream in, float boostFactor) {
        super(in);
        this.boostFactor = boostFactor;
    }

    /**
     * Reads and processes audio samples, boosting their volume.
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
            short amp = getSample(sample, offset + i);

            // Apply volume boost with clipping protection
            float boosted = amp * boostFactor;
            if (boosted > Short.MAX_VALUE) boosted = Short.MAX_VALUE;
            if (boosted < Short.MIN_VALUE) boosted = Short.MIN_VALUE;

            setSample(sample, offset + i, (short) boosted);
        }

        return bytesRead;
    }
}