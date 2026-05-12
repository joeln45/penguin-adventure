package com.joeln45.penguin.engine;

import java.io.*;

/**
 * An abstract base class for sound filters that modify audio streams.
 *
 * @author Joel Nirmal
 */
public abstract class SoundFilter extends FilterInputStream {

    /**
     * Constructor for creating a SoundFilter.
     *
     * @param in The input stream of the audio.
     */
    public SoundFilter(InputStream in) {
        super(in);
    }

    /**
     * Takes a 16-bit audio sample from the buffer.
     *
     * @param buffer   The audio sample buffer.
     * @param position The position in the buffer.
     * @return The 16-bit audio sample.
     */
    public short getSample(byte[] buffer, int position) {
        return (short) (((buffer[position + 1] & 0xff) << 8) | (buffer[position] & 0xff));
    }

    /**
     * Sets a 16-bit audio sample into the buffer.
     *
     * @param buffer   The audio sample buffer.
     * @param position The position in the buffer.
     * @param sample   The 16-bit audio sample to set.
     */
    public void setSample(byte[] buffer, int position, short sample) {
        buffer[position] = (byte) (sample & 0xFF);
        buffer[position + 1] = (byte) ((sample >> 8) & 0xFF);
    }
}
