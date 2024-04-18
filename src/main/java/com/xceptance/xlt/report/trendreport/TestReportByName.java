/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
