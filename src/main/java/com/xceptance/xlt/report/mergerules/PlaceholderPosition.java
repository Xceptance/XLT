package com.xceptance.xlt.report.mergerules;

/**
 * Container for placeholder information within a pattern. It is thread safe and can be shared.
 *
 * @author rschwietzke
 */
public class PlaceholderPosition
{
    public final String typeCode;

    public final int typeCodeHashCode;

    public final int index;

    public final int start;

    public final int end;

    public final int length;

    public PlaceholderPosition(final String typeCode, final int index, final int start, final int end, final int length)
    {
        this.typeCode = typeCode;
        typeCodeHashCode = typeCode.hashCode();

        this.index = index;
        this.start = start;
        this.end = end;
        this.length = length;
    }
}
