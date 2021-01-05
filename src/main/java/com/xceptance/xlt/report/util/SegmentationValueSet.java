/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

/**
 *
 */
public class SegmentationValueSet
{
    private final int countPerBin[];

    private final int[] boundaries;

    public SegmentationValueSet(final int[] boundaries)
    {
        this.boundaries = boundaries;
        countPerBin = new int[boundaries.length + 1];
    }

    public void addValue(final int value)
    {
        int i = boundaries.length - 1;

        if (value > boundaries[i])
        {
            countPerBin[i + 1]++;
        }
        else
        {
            for (i = boundaries.length - 1; i >= 0; i--)
            {
                if (value <= boundaries[i])
                {
                    countPerBin[i]++;
                }
                else
                {
                    break;
                }
            }
        }
    }

    public int[] getCountPerSegment()
    {
        return countPerBin;
    }
}
