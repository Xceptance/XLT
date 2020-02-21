package com.xceptance.xlt.api.report;

/**
 * The {@link ReportCreator} defines the interface that report providers must implement to take part in report
 * generation.
 * 
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public interface ReportCreator
{
    /**
     * Creates a report fragment to be added to the test report. The fragment is generated from the statistics generated
     * during processing the data records. The statistics are encapsulated by some object which forms the record
     * fragment.
     * 
     * @return the report fragment
     */
    public Object createReportFragment();
}
