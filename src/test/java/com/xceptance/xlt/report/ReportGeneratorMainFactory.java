package com.xceptance.xlt.report;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Creates and initializes {@link ReportGenerator} instances with custom arguments plus some default arguments.
 */
class ReportGeneratorMainFactory
{
    static ReportGeneratorMain create(final String[] args) throws Exception
    {
        // add some default arguments
        final String[] finalArgs = ArrayUtils.addAll(args, "-timezone", "GMT", ".");

        ReportGeneratorMain rgm = new ReportGeneratorMain();
        rgm.init(finalArgs);

        return rgm;
    }
}
