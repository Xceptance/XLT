package com.xceptance.xlt.api.engine;

import java.util.List;

/**
 * The {@link CustomValue} can store a single 'double' value.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class CustomValue extends AbstractData
{
    /**
     * The type code ("V").
     */
    private static final String TYPE_CODE = "V";

    /**
     * The value.
     */
    private double value;

    /**
     * Creates a new {@link CustomValue} object and gives it the specified name. Furthermore, the start time attribute
     * is set to the current time.
     * 
     * @param name
     *            the statistics name
     */
    public CustomValue(final String name)
    {
        super(name, TYPE_CODE);
    }

    /**
     * Creates a new {@link CustomValue} object.
     */
    public CustomValue()
    {
        super(TYPE_CODE);
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            the value
     */
    public void setValue(final double value)
    {
        this.value = value;
    }

    /**
     * Returns the value.
     * 
     * @return the value
     */
    public double getValue()
    {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> addValues()
    {
        final List<String> values = super.addValues();
        values.add(Double.toString(value));
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getMinNoCSVElements()
    {
        return 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parseValues(final String[] values)
    {
        super.parseValues(values);
        value = Double.parseDouble(values[3]);
    }
}
