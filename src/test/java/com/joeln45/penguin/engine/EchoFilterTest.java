package com.joeln45.penguin.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EchoFilter}.
 *
 * <p>The filter mixes each incoming 16-bit sample with a delayed copy of an
 * earlier sample, scaled by the {@code decay} factor. With a delay sized to
 * exactly N samples, sample number {@code i + N} should equal the original
 * sample {@code i} plus {@code decay * sample[i - N]} (initial delay buffer
 * is zero-filled, so the first N samples are unaffected).
 */
class EchoFilterTest {

    /** Helper: write a 16-bit little-endian sample to a byte array. */
    private static void writeSample(byte[] buf, int pos, short value) {
        buf[pos]     = (byte) (value & 0xFF);
        buf[pos + 1] = (byte) ((value >> 8) & 0xFF);
    }

    /** Helper: read a 16-bit little-endian sample from a byte array. */
    private static short readSample(byte[] buf, int pos) {
        return (short) (((buf[pos + 1] & 0xff) << 8) | (buf[pos] & 0xff));
    }

    @Test
    @DisplayName("first N samples pass through unchanged (delay buffer starts at zero)")
    void firstSamplesPassThrough() throws IOException {
        // sampleRate=1000, delay=2ms -> 2-sample delay
        // Send 4 samples; first 2 should be untouched.
        byte[] in = new byte[8];
        writeSample(in, 0, (short) 100);
        writeSample(in, 2, (short) 200);
        writeSample(in, 4, (short) 300);
        writeSample(in, 6, (short) 400);

        EchoFilter echo = new EchoFilter(new ByteArrayInputStream(in), 2, 0.5f, 1000);
        byte[] out = new byte[8];
        int read = echo.read(out, 0, 8);
        assertEquals(8, read);

        // sample[0] = 100 + 0*0.5 = 100
        // sample[1] = 200 + 0*0.5 = 200
        assertEquals((short) 100, readSample(out, 0));
        assertEquals((short) 200, readSample(out, 2));
    }

    @Test
    @DisplayName("samples beyond the delay window receive an echo of an earlier sample")
    void laterSamplesIncludeEcho() throws IOException {
        // 2-sample delay, decay 0.5
        byte[] in = new byte[8];
        writeSample(in, 0, (short) 100);
        writeSample(in, 2, (short) 200);
        writeSample(in, 4, (short) 0);
        writeSample(in, 6, (short) 0);

        EchoFilter echo = new EchoFilter(new ByteArrayInputStream(in), 2, 0.5f, 1000);
        byte[] out = new byte[8];
        echo.read(out, 0, 8);

        // sample[2] = 0   + 100 * 0.5 = 50
        // sample[3] = 0   + 200 * 0.5 = 100
        assertEquals((short) 50,  readSample(out, 4));
        assertEquals((short) 100, readSample(out, 6));
    }

    @Test
    @DisplayName("decay = 0 means no echo and samples pass through unchanged")
    void zeroDecayMeansNoEcho() throws IOException {
        byte[] in = new byte[8];
        writeSample(in, 0, (short) 1000);
        writeSample(in, 2, (short) 2000);
        writeSample(in, 4, (short) 3000);
        writeSample(in, 6, (short) 4000);

        EchoFilter echo = new EchoFilter(new ByteArrayInputStream(in), 2, 0.0f, 1000);
        byte[] out = new byte[8];
        echo.read(out, 0, 8);

        assertEquals((short) 1000, readSample(out, 0));
        assertEquals((short) 2000, readSample(out, 2));
        assertEquals((short) 3000, readSample(out, 4));
        assertEquals((short) 4000, readSample(out, 6));
    }
}
