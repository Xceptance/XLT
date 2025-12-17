package com.xceptance.xlt.report.util;

import static org.junit.Assert.*;
import org.junit.Test;

import com.xceptance.xlt.report.util.misc.BitCompression;

/**
 * Unit tests for {@link BitCompression}.
 * All test values use binary number formats for clarity.
 */
public class BitCompressionTest
{
    // --- Tests for combineAdjacentBits ---

    @Test
    public void testCombineAdjacentBits_AllZeros()
    {
        // All bits are 0
        assertEquals(0b0000000000000000000000000000000000000000000000000000000000000000L,
                     BitCompression.combineAdjacentBits(0b0000000000000000000000000000000000000000000000000000000000000000L));
    }

    @Test
    public void testCombineAdjacentBits_AllOnes()
    {
        // All od bits are 1
        assertEquals(0b1010101010101010101010101010101010101010101010101010101010101010L,
                     BitCompression.combineAdjacentBits(0b1111111111111111111111111111111111111111111111111111111111111111L));
    }

    @Test
    public void testCombineAdjacentBits_AlternatingBits()
    {
        // Alternating 1s and 0s: 1010...
        assertEquals(0b1010101010101010101010101010101010101010101010101010101010101010L,
                     BitCompression.combineAdjacentBits(0b1010101010101010101010101010101010101010101010101010101010101010L));
        // Alternating 0s and 1s: 0101...
        assertEquals(0b1010101010101010101010101010101010101010101010101010101010101010L,
                     BitCompression.combineAdjacentBits(0b0101010101010101010101010101010101010101010101010101010101010101L));
    }

    @Test
    public void testCombineAdjacentBits_SingleBit()
    {
        // we always have the odd bit set when combining
        
        // Only bit 1 set
        assertEquals(0b0000000000000000000000000000000000000000000000000000000000000010L,
                     BitCompression.combineAdjacentBits(0b0000000000000000000000000000000000000000000000000000000000000010L));
        // Only bit 0 set
        assertEquals(0b0000000000000000000000000000000000000000000000000000000000000010L,
                     BitCompression.combineAdjacentBits(0b0000000000000000000000000000000000000000000000000000000000000001L));
        // Only highest bit set
        assertEquals(0b1000000000000000000000000000000000000000000000000000000000000000L,
                     BitCompression.combineAdjacentBits(0b100000000000000000000000000000000000000000000000000000000000000L));
        assertEquals(0b1000000000000000000000000000000000000000000000000000000000000000L,
                     BitCompression.combineAdjacentBits(0b1100000000000000000000000000000000000000000000000000000000000000L));
        // Only highest bit set
        assertEquals(0b10L,
                     BitCompression.combineAdjacentBits(0b11L));
    }

    // --- Tests for compressAndShiftOddBits ---

    @Test
    public void testCompressAndShiftOddBits_AllZeros()
    {
        // All bits are 0
        assertEquals(0b0L, BitCompression.compressAndShiftOddBits(0b0L));
    }

    @Test
    public void testCompressAndShiftOddBits_AllOnes()
    {
        // All bits are 1
        assertEquals(0b11111111111111111111111111111111L,
                     BitCompression.compressAndShiftOddBits(0b1111111111111111111111111111111111111111111111111111111111111111L));
    }

    @Test
    public void testCompressAndShiftOddBits_AlternatingBits()
    {
        // Only odd bits set: 1010...
        assertEquals(0b11111111111111111111111111111111L,
                     BitCompression.compressAndShiftOddBits(0b1010101010101010101010101010101010101010101010101010101010101010L));
        // Only even bits set: 0101...
        assertEquals(0b00000000000000000000000000000000L,
                     BitCompression.compressAndShiftOddBits(0b0101010101010101010101010101010101010101010101010101010101010101L));
    }

    @Test
    public void testCompressAndShiftOddBits()
    {
        assertEquals(0b01L, BitCompression.compressAndShiftOddBits(0b10L));
        assertEquals(0b00L, BitCompression.compressAndShiftOddBits(0b01L));
        assertEquals(0b11L, BitCompression.compressAndShiftOddBits(0b1010L));
        assertEquals(0b101L, BitCompression.compressAndShiftOddBits(0b100010L));
    }
    @Test

    public void testCompressAndShiftUpper()
    {
        // we end always with 32 bits in the lower part and empty upper bits
        assertEquals(0b0000000000000000000000000000000011111111111111111111111111111111L, 
                     BitCompression.compressAndShiftOddBits(0b1111111111111111111111111111111111111111111111111111111111111111L));
        assertEquals(0b0000000000000000000000000000000010000000000000000000000000000000L, 
                     BitCompression.compressAndShiftOddBits(0b1000000000000000000000000000000000000000000000000000000000000000L));
    }

    @Test
    public void testCompressAndShiftOddBits_Example()
    {
        // Example from documentation
        long input    = 0b0000000000000000000000000000000010000110100111001101010110101111L;
        long expected = 0b00000000000000001011111011111111L;
        assertEquals(expected, BitCompression.compressAndShiftOddBits(input));
    }
}
