package com.xceptance.xlt.api.report.external;

/**
 * A {@link NamedData} holds a value and its abstract name.
 *
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 * @deprecated As of XLT 4.6.0: This class is not used at all in the XLT API and will therefore be removed soon.
 */
@Deprecated
public class NamedData
{
    /**
     * The value.
     */
    private final double value;

    /**
     * Abstract name of the value.
     */
    private final String name;

    /**
     * Creates an instance of {@link NamedData}.
     *
     * @param name
     *            the name of the value
     * @param value
     *            the value
     */
    public NamedData(final String name, final double value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the value.
     *
     * @return value
     */
    public double getValue()
    {
        return value;
    }
}
