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

    @Test
    public void testConstantColumnRleAndDecompressTo()
    {
        final int size = 64_000;
        final int[] constantCol = new int[size];
        java.util.Arrays.fill(constantCol, 200);

        final int[] compressed = FastIntegerCodec.compress(constantCol);
        Assert.assertEquals("Constant column RLE must compress to 3 words", 3, compressed.length);
        Assert.assertTrue(FastIntegerCodec.isConstant(compressed));
        Assert.assertEquals(200, FastIntegerCodec.getConstantValue(compressed));
        Assert.assertEquals(size, FastIntegerCodec.getUncompressedLength(compressed));

        // Decompress into a fresh array
        final int[] decompressed = FastIntegerCodec.decompress(compressed);
        Assert.assertArrayEquals(constantCol, decompressed);

        // Zero-allocation decompressTo with a reusable scratch buffer
        final int[] scratch = new int[size + 128];
        final int[] reused = FastIntegerCodec.decompressTo(compressed, scratch);
        Assert.assertSame("decompressTo must reuse caller buffer when capacity is sufficient", scratch, reused);
        for (int i = 0; i < size; i++)
        {
            Assert.assertEquals(200, reused[i]);
        }

        // Also test decompressTo on a non-constant SIMD-packed array
        final int[] nonConstant = new int[size];
        for (int i = 0; i < size; i++)
        {
            nonConstant[i] = i % 300;
        }
        final int[] compNonConst = FastIntegerCodec.compress(nonConstant);
        Assert.assertFalse(FastIntegerCodec.isConstant(compNonConst));
        Assert.assertEquals(size, FastIntegerCodec.getUncompressedLength(compNonConst));

        final int[] reusedNonConst = FastIntegerCodec.decompressTo(compNonConst, scratch);
        Assert.assertSame(scratch, reusedNonConst);
        for (int i = 0; i < size; i++)
        {
            Assert.assertEquals(nonConstant[i], reusedNonConst[i]);
        }

        // Edge cases: null, empty, and undersized reuseBuffer
        Assert.assertEquals(0, FastIntegerCodec.compress(null).length);
        Assert.assertEquals(0, FastIntegerCodec.compress(new int[0]).length);
        Assert.assertFalse(FastIntegerCodec.isConstant(null));
        Assert.assertEquals(0, FastIntegerCodec.getUncompressedLength(null));
        Assert.assertEquals(0, FastIntegerCodec.decompressTo(new int[0], scratch).length);
        final int[] reallocated = FastIntegerCodec.decompressTo(compressed, new int[10]);
        Assert.assertEquals(size, reallocated.length);
        Assert.assertEquals(200, reallocated[size - 1]);
    }

    @Test
    public void testGlobalDictionariesFacetBitmapsSerialization() throws Exception
    {
        final GlobalDictionaries dict = new GlobalDictionaries();
        final int methodId = dict.getOrCreateStringId("PATCH");
        final int contentTypeId = dict.getOrCreateStringId("application/x-protobuf");
        final int usedIpId = dict.getOrCreateStringId("192.168.10.42");

        dict.recordHttpMethodId(methodId);
        dict.recordContentTypeId(contentTypeId);
        dict.recordUsedIpId(usedIpId);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dict.writeTo(new DataOutputStream(baos));

        final GlobalDictionaries restored = GlobalDictionaries.readFrom(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
        Assert.assertTrue(restored.getHttpMethodIds().contains(methodId));
        Assert.assertTrue(restored.getContentTypeIds().contains(contentTypeId));
        Assert.assertTrue(restored.getUsedIpIds().contains(usedIpId));
        Assert.assertFalse(restored.getUsedIpIds().contains(methodId));
    }
}
