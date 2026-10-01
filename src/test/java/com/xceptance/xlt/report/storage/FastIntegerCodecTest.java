/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.report.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.report.storage.compression.FastIntegerCodec;
import com.xceptance.xlt.report.storage.dictionary.GlobalDictionaries;

public class FastIntegerCodecTest
{
    @Test
    public void testGeneralIntegerCompressionRoundTrip()
    {
        final int size = 64_000;
        final int[] original = new int[size];
        final Random random = new Random(42);

        // Typical runtimes between 10ms and 1500ms
        for (int i = 0; i < size; i++)
        {
            original[i] = random.nextInt(1500);
        }

        final int[] compressed = FastIntegerCodec.compress(original);
        Assert.assertTrue("Compressed size (" + compressed.length + ") should be significantly smaller than original (" + original.length + ")",
                          compressed.length < original.length / 2);

        final int[] decompressed = FastIntegerCodec.decompress(compressed);
        Assert.assertArrayEquals(original, decompressed);
    }

    @Test
    public void testDeltaTimelineCompressionRoundTrip()
    {
        final int size = 64_000;
        final int[] timestamps = new int[size];
        final Random random = new Random(1337);

        // Monotonically increasing timestamps (delays 0..50ms)
        int current = 0;
        for (int i = 0; i < size; i++)
        {
            current += random.nextInt(50);
            timestamps[i] = current;
        }

        final int[] compressed = FastIntegerCodec.compressDelta(timestamps);
        // Integrated Delta + SIMD packing should compress smoothly
        Assert.assertTrue("Compressed delta size should be much smaller", compressed.length < timestamps.length / 2);

        final int[] decompressed = FastIntegerCodec.decompressDelta(compressed);
        Assert.assertArrayEquals(timestamps, decompressed);
    }

    @Test
    public void testShortArrayCompressionRoundTrip()
    {
        final int size = 64_000;
        final short[] original = new short[size];
        final Random random = new Random(77);

        for (int i = 0; i < size; i++)
        {
            original[i] = (short) random.nextInt(500);
        }

        final int[] compressed = FastIntegerCodec.compressShorts(original);
        final short[] decompressed = FastIntegerCodec.decompressShorts(compressed);
        Assert.assertArrayEquals(original, decompressed);
    }

    @Test
    public void testGlobalDictionariesSerialization() throws Exception
    {
        final GlobalDictionaries dict = new GlobalDictionaries();

        final int timer1 = dict.getOrCreateTimerNameId("Homepage");
        final int timer2 = dict.getOrCreateTimerNameId("Search");
        final int timer3 = dict.getOrCreateTimerNameId("Homepage");
        Assert.assertEquals(timer1, timer3);
        Assert.assertNotEquals(timer1, timer2);

        final int pair1 = dict.getOrCreateAgentTestCaseId("agent-001", "TOrder");
        final int pair2 = dict.getOrCreateAgentTestCaseId("agent-001", "TOrder");
        Assert.assertEquals(pair1, pair2);

        final int str1 = dict.getOrCreateStringId("GET");
        final int str2 = dict.getOrCreateStringId("POST");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dict.writeTo(new DataOutputStream(baos));

        final GlobalDictionaries restored = GlobalDictionaries.readFrom(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));

        Assert.assertEquals("Homepage", restored.getTimerName(timer1));
        Assert.assertEquals("Search", restored.getTimerName(timer2));
        Assert.assertEquals("agent-001", restored.getAgentName(pair1));
        Assert.assertEquals("TOrder", restored.getTestCaseName(pair1));
        Assert.assertEquals("GET", restored.getString(str1));
        Assert.assertEquals("POST", restored.getString(str2));
    }
}
