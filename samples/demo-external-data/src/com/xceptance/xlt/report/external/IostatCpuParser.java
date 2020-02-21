package com.xceptance.xlt.report.external;

/**
 * Parse CPU section of iostat output <code>iostat -c 1 -t</code>.
 */
public class IostatCpuParser extends AbstractIostatParser
{
    /**
     * Section identifier of CPU section.
     */
    protected static final String SECTION_DEVICE = "avg-cpu:";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parseHeader(final String line)
    {
        // you can replace the real head by your own names like ('tps', 'read', 'write' or 'read/s', 'write/s').
        // feel free but take care of the config file.
        final String[] heads = line.substring(SECTION_DEVICE.length(), line.length()).trim().split("\\s+");
        for (int index = 0; index < heads.length; index++)
        {
            final String headline = heads[index].replace("%", "");
            if (getValueNames().contains(headline))
            {
                getHeadlines().put(headline, index);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getSectionIdentifier()
    {
        return SECTION_DEVICE;
    }
}
