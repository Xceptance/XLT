package com.xceptance.xlt.report.external;

/**
 * Parse and create report fragment of command line output e.g. <code>iostat -x 1 -t</code> or
 * <code>iostat -x 1 -t</code>.
 */
public class IostatDeviceParser extends AbstractIostatParser
{
    /**
     * Section identifier of Device section.
     */
    protected static final String SECTION_DEVICE = "Device:";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSectionIdentifier()
    {
        return SECTION_DEVICE;
    }
}
