package com.xceptance.xlt.report.trendreport;

import org.w3c.dom.Document;

/**
 * This class represents a test report document keyed by the test report name.
 * 
 * @author sebastianloob
 */
public class TestReportByName
{
    /**
     * The test report name of the test report.
     */
    private String reportName;

    /**
     * The test report document.
     */
    private Document testReport;

    /**
     * Constructor.
     * 
     * @param reportName
     * @param testReport
     */
    public TestReportByName(String reportName, Document testReport)
    {
        this.setReportName(reportName);
        this.setTestReport(testReport);
    }

    /**
     * Returns the test report name.
     * 
     * @return the test report name
     */
    public String getReportName()
    {
        return reportName;
    }

    /**
     * Sets the test report name.
     * 
     * @param reportName
     *            the test report name
     */
    public void setReportName(String reportName)
    {
        this.reportName = reportName;
    }

    /**
     * Returns the test report document.
     * 
     * @return the test report document
     */
    public Document getTestReport()
    {
        return testReport;
    }

    /**
     * Sets the test report document.
     * 
     * @param testReport
     *            the test report document
     */
    public void setTestReport(Document testReport)
    {
        this.testReport = testReport;
    }
}
