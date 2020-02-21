package com.xceptance.xlt.report.mergerules;

import com.xceptance.xlt.api.engine.RequestData;

/**
 * Base class for all request filters.
 */
public abstract class AbstractRequestFilter
{
    /**
     * The type code of this request filter.
     */
    private final String typeCode;
    
    /**
     * The hash of the string for faster comparison of the type code
     */
    private final int typeCodeHashCode;

    /**
     * Constructor.
     * 
     * @param typeCode
     *            the type code of this request filter
     */
    public AbstractRequestFilter(final String typeCode)
    {
        this.typeCode = typeCode;
        this.typeCodeHashCode = typeCode.hashCode();
    }

    /**
     * Returns the replacement text derived from the passed request data object.
     * 
     * @param requestData
     *            the request data object
     * @param capturingGroupIndex
     *            the capturing group index specified in the placeholder
     * @param filterState
     *            the filter state object returned by {@link #appliesTo(RequestData)}
     * @return the replacement text
     */
    public abstract String getReplacementText(RequestData requestData, int capturingGroupIndex, Object filterState);

    /**
     * Whether or not the passed request data object is accepted by this request filter.
     * 
     * @param requestData
     *            the request data object
     * @return in case the filter accepted the passed request data: a state object representing the filter state (can be
     *         a dummy object), otherwise: <code>null</code>
     */
    public abstract Object appliesTo(RequestData requestData);

    /**
     * Returns the type code of this request filter.
     * 
     * @return the type code
     */
    public String getTypeCode()
    {
        return typeCode;
    }

    /**
     * Compares two types codes efficiently
     * 
     * @param typeCode the type code to compare to
     * @param typeCodeHashCode the hash of the type code for performance reason
     * @return true, if the type codes match, false otherwise
     */
    public boolean isSameTypeCode(final String typeCode, final int typeCodeHashCode)
    {
        if (this.typeCodeHashCode == typeCodeHashCode && this.typeCode.length() == typeCode.length())
        {
            return this.typeCode.equals(typeCode);
        }
        else
        {
            return false;
        }
    }
}
