package com.xceptance.xlt.report.util;

public class BitCompression 
{
    /**
     * Combine adjacent bits of a long by compressing the "odd" bits to the right.
     */
    public static long combineAdjacentBits(long n) 
    {
        // System.out.println(String.format("%64s", Long.toBinaryString(n)).replace(' ', '0'));
      
        // Mask to select even bits (positions 0, 2, 4, ...)
        long even = (n & 0b0101010101010101010101010101010101010101010101010101010101010101L);
        
        // Mask to select odd bits (positions 1, 3, 5, ...)
        long odd  = (n & 0b1010101010101010101010101010101010101010101010101010101010101010L);
        
        // Shift even bits right to align with odd bits, then OR to combine each pair
        n = odd | (even << 1);
        
        // System.out.println(String.format("%64s", Long.toBinaryString(n)).replace(' ', '0'));

        return n;
    }
    
    /**
     * Compresses the "odd" bits of a long to the right.
     * For example, given an 8-bit number b7 b6 b5 b4 b3 b2 b1 b0,
     * this function extracts the bits at odd positions (b7, b5, b3, b1)
     * and packs them into the rightmost bits (b3, b2, b1, b0 of the result).
     *
     * @param n The long to compress.
     * @return An long with the odd bits of n packed to the right.
     */
    public static long compressAndShiftOddBits(long n) {
        // Step 1: Isolate the odd bits and shift them into even positions.
        // Mask 0xAAAAAAAA selects bits 1, 3, 5, ...
        // The shift moves them to positions 0, 2, 4, ...
        // e.g., ... b5 0 b3 0 b1 0  ->  ... 0 b5 0 b3 0 b1
        n = (n & 0xAAAAAAAAAAAAAAAAL) >>> 1;

        // Step 2: In parallel, merge pairs of bits.
        // e.g., ... 0 X 0 Y -> ... 0 0 X Y
        n = (n | (n >>> 1)) & 0x3333333333333333L;

        // Step 3: In parallel, merge pairs of bit-pairs (nibbles).
        // e.g., ... 00XX 00YY -> ... 0000XXYY
        n = (n | (n >>> 2)) & 0x0F0F0F0F0F0F0F0FL;

        // Step 4: In parallel, merge pairs of nibbles (bytes).
        n = (n | (n >>> 4)) & 0x00FF00FF00FF00FFL;

        // Step 5: In parallel, merge pairs of bytes.
        n = (n | (n >>> 8)) & 0x0000FFFF0000FFFFL;

        // Use logical shift (>>>) for longs and 64-bit masks (L suffix).
        // New step for 64-bit: merge pairs of 16-bit chunks.
        n = (n | (n >>> 16)) & 0x00000000FFFFFFFFL;

        // Only lower 32 bits are relevant after packing
        return n;
    }
}

