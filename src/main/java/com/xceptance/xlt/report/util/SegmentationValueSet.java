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
