package com.xceptance.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.xceptance.common.util.CsvByteRow.ByteStringCache;

public class CsvByteRowTest
{
    @Test
    public void testByteStringCache_HitAndMiss()
    {
        final ByteStringCache cache = new ByteStringCache();
        
        final String s1 = "SampleTransaction";
        final byte[] d1 = s1.getBytes(StandardCharsets.UTF_8);

        // First access should be a miss, creating a new string
        final String result1 = cache.get(d1, 0, d1.length, false);
        assertEquals(s1, result1);

        // Second access should hit, returning the EXACT same object instance
        final String result2 = cache.get(d1, 0, d1.length, false);
        assertSame("Cache should return the exact same String instance on hit", result1, result2);
    }

    @Test
    public void testByteStringCache_DifferentOffsets()
    {
        final ByteStringCache cache = new ByteStringCache();
        
        final String payload = "Prefix_Transaction_Suffix";
        final byte[] data = payload.getBytes(StandardCharsets.UTF_8);

        // We want to extract "Transaction" (length 11, offset 7)
        final int offset = 7;
        final int length = 11;
        
        final String result1 = cache.get(data, offset, length, false);
        assertEquals("Transaction", result1);

        // Extract again, must be identical reference
        final String result2 = cache.get(data, offset, length, false);
        assertSame(result1, result2);
        
        // Extract a DIFFERENT "Transaction" that happens to be identically byte-wise, but different offset
        final String payload2 = "OtherPrefix_Transaction_X";
        final byte[] data2 = payload2.getBytes(StandardCharsets.UTF_8);
        final String result3 = cache.get(data2, 12, length, false);
        
        // It should match the previous "Transaction" caching
        assertSame(result1, result3);
    }

    @Test
    public void testByteStringCache_CollisionEviction()
    {
        final ByteStringCache cache = new ByteStringCache();
        
        // Let's create two DIFFERENT strings that happen to hash to the exact same slot.
        // Hash formula inside cache:
        // int h = 0;
        // for (int i = 0; i < length; i++) h = 31 * h + data[offset + i];
        // ptr = (h ^ (h >>> 16)) & 1023;
        
        // "A"  -> h = 65 -> slot = 65
        // "qD" -> h = 31 * 'q' + 'D' = 31 * 113 + 68 = 3503 + 68 = 3571. 
        // 3571 ^ (3571 >>> 16) = 3571
        // 3571 & 1023 (0x3FF) = 501. 
        // "A" is slot 65.
        // Let's just generate strings until we find a collision.
        
        String s1 = null, s2 = null;
        for (int i = 0; i < 5000; i++)
        {
            String cand1 = "A" + i;
            int hash1 = getCacheSlot(cand1);
            
            for (int j = i + 1; j < 5000; j++)
            {
                String cand2 = "B" + j;
                if (getCacheSlot(cand2) == hash1 && cand1.length() == cand2.length())
                {
                    s1 = cand1;
                    s2 = cand2;
                    break;
                }
            }
            if (s1 != null)
            {
                break;
            }
        }
        
        final byte[] d1 = s1.getBytes(StandardCharsets.UTF_8);
        final byte[] d2 = s2.getBytes(StandardCharsets.UTF_8);

        // Put s1 in cache
        final String r1 = cache.get(d1, 0, d1.length, false);
        assertEquals(s1, r1);
        
        // Put s2 in cache (collides, evicts s1)
        final String r2 = cache.get(d2, 0, d2.length, false);
        assertEquals(s2, r2);
        
        // Put s1 in cache again (misses, recreates s1)
        final String r3 = cache.get(d1, 0, d1.length, false);
        assertEquals(s1, r3);
        assertNotSame("s1 should have been evicted and recreated", r1, r3);
    }
    @Test
    public void testByteStringCache_CollisionEviction_DifferentLengths()
    {
        final ByteStringCache cache = new ByteStringCache();
        
        // Find a collision between a short string and a LONG string.
        String shortStr = null;
        String longStr = null;
        
        for (int i = 0; i < 5000; i++)
        {
            String cand1 = "A" + i;
            int hash1 = getCacheSlot(cand1);
            
            for (int j = i + 1; j < 5000; j++)
            {
                // Create a string that exceeds the Math.max(64, length) safety buffer if cand1 is short
                String cand2 = "VeryLongStringThatForcesAReallocationWhenItCollidesWithAShortString_" + j;
                if (getCacheSlot(cand2) == hash1)
                {
                    shortStr = cand1;
                    longStr = cand2;
                    break;
                }
            }
            if (shortStr != null)
            {
                break;
            }
        }
        
        final byte[] dShort = shortStr.getBytes(StandardCharsets.UTF_8);
        final byte[] dLong = longStr.getBytes(StandardCharsets.UTF_8);

        // Put short in cache (allocates keyBuffer of size 64)
        final String r1 = cache.get(dShort, 0, dShort.length, false);
        assertEquals(shortStr, r1);
        
        // Put long in cache (must resize keyBuffer to > 64)
        final String r2 = cache.get(dLong, 0, dLong.length, false);
        assertEquals(longStr, r2);
        
        // Put short in cache again (reuses larger buffer, only overrides prefix)
        final String r3 = cache.get(dShort, 0, dShort.length, false);
        assertEquals(shortStr, r3);
    }
    
    private int getCacheSlot(String s)
    {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        int h = 0;
        for (int i = 0; i < b.length; i++)
        {
            h = 31 * h + b[i];
        }
        return (h ^ (h >>> 16)) & 1023;
    }

    @Test
    public void testByteStringCache_EmptyString()
    {
        final ByteStringCache cache = new ByteStringCache();
        assertEquals("", cache.get(new byte[0], 0, 0, false));
    }
}
