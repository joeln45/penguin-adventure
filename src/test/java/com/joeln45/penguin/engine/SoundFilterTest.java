package com.joeln45.penguin.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SoundFilter}'s 16-bit little-endian sample helpers.
 *
 * <p>Uses a no-op concrete subclass since {@code SoundFilter} is abstract.
 */
class SoundFilterTest {

    /** Minimal concrete subclass for accessing the protected helpers. */
    private static class TestFilter extends SoundFilter {
        TestFilter(InputStream in) { super(in); }
    }

    private static TestFilter newFilter() {
        return new TestFilter(new ByteArrayInputStream(new byte[0]));
    }

    @Test
    @DisplayName("getSample decodes a small positive little-endian value")
    void getSampleDecodesPositive() {
        // 0x0001 little-endian = bytes {0x01, 0x00} -> 1
        byte[] buf = {0x01, 0x00};
        assertEquals((short) 1, newFilter().getSample(buf, 0));
    }

    @Test
    @DisplayName("getSample decodes the maximum 16-bit signed value")
    void getSampleDecodesMaxValue() {
        // 0x7FFF little-endian = bytes {0xFF, 0x7F} -> 32767
        byte[] buf = {(byte) 0xFF, (byte) 0x7F};
        assertEquals(Short.MAX_VALUE, newFilter().getSample(buf, 0));
    }

    @Test
    @DisplayName("getSample decodes a negative two's-complement value")
    void getSampleDecodesNegative() {
        // -1 two's complement = 0xFFFF little-endian = {0xFF, 0xFF}
        byte[] buf = {(byte) 0xFF, (byte) 0xFF};
        assertEquals((short) -1, newFilter().getSample(buf, 0));
    }

    @Test
    @DisplayName("setSample writes back exactly what getSample decoded")
    void setSampleIsInverseOfGetSample() {
        TestFilter f = newFilter();
        byte[] buf = new byte[2];
        for (short value : new short[] {0, 1, -1, 1234, -1234, Short.MAX_VALUE, Short.MIN_VALUE}) {
            f.setSample(buf, 0, value);
            assertEquals(value, f.getSample(buf, 0),
                    "round-trip failed for " + value);
        }
    }

    @Test
    @DisplayName("getSample reads at an offset within a larger buffer")
    void getSampleRespectsPosition() {
        // Three back-to-back samples: 100, -100, 200
        TestFilter f = newFilter();
        byte[] buf = new byte[6];
        f.setSample(buf, 0, (short) 100);
        f.setSample(buf, 2, (short) -100);
        f.setSample(buf, 4, (short) 200);

        assertEquals((short) 100,  f.getSample(buf, 0));
        assertEquals((short) -100, f.getSample(buf, 2));
        assertEquals((short) 200,  f.getSample(buf, 4));
    }
}
