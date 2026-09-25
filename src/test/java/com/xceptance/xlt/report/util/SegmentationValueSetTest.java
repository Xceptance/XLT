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
package com.xceptance.xlt.report.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link SegmentationValueSet} class.
 */
public class SegmentationValueSetTest
{
    @Test
    public void testBasics()
    {
        final int[] boundaries = { 10, 50, 100 };
        final SegmentationValueSet set = new SegmentationValueSet(boundaries);

        set.addValue(5);
        set.addValue(30);
        set.addValue(80);
        set.addValue(150);

        final int[] counts = set.getCountPerSegment();
        Assert.assertEquals(4, counts.length);
        Assert.assertEquals(1, counts[0]); // <= 10
        Assert.assertEquals(2, counts[1]); // <= 50 (5 and 30)
        Assert.assertEquals(3, counts[2]); // <= 100 (5, 30, 80) // <= 50
        Assert.assertEquals(1, counts[3]); // > 100
    }

    @Test
    public void testMergeNull()
    {
        final int[] boundaries = { 10, 50, 100 };
        final SegmentationValueSet set = new SegmentationValueSet(boundaries);
        set.addValue(5);
        set.merge(null);
        Assert.assertEquals(1, set.getCountPerSegment()[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeDifferentBoundariesThrows()
    {
        final SegmentationValueSet set1 = new SegmentationValueSet(new int[] { 10, 50 });
        final SegmentationValueSet set2 = new SegmentationValueSet(new int[] { 20, 50 });
        set1.merge(set2);
    }

    @Test
    public void testMergeEquivalence()
    {
        final int[] boundaries = { 10, 50, 100 };
        final SegmentationValueSet seq = new SegmentationValueSet(boundaries);
        final SegmentationValueSet s1 = new SegmentationValueSet(boundaries);
        final SegmentationValueSet s2 = new SegmentationValueSet(boundaries);

        final int[] values = { 5, 10, 15, 50, 51, 99, 100, 101, 200 };
        for (int i = 0; i < values.length; i++)
        {
            seq.addValue(values[i]);
            if (i % 2 == 0)
            {
                s1.addValue(values[i]);
            }
            else
            {
                s2.addValue(values[i]);
            }
        }

        s1.merge(s2);
        Assert.assertArrayEquals(seq.getCountPerSegment(), s1.getCountPerSegment());
    }
}
