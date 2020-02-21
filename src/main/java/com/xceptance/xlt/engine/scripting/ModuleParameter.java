package com.xceptance.xlt.engine.scripting;

/**
 * Represents a module parameter in a script file.
 */
public class ModuleParameter
{
    /**
     * The parameter's name.
     */
    private final String name;

    /**
     * The parameter's value.
     */
    private final String value;

    /**
     * Constructor.
     * 
     * @param name
     *            the parameter name
     * @param value
     *            the parameter value
     */
    public ModuleParameter(final String name, final String value)
    {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the value of the 'name' attribute.
     * 
     * @return the value of name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the value of the 'value' attribute.
     * 
     * @return the value of value
     */
    public String getValue()
    {
        return value;
    }
}
