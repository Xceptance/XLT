/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.report;

import com.xceptance.xlt.api.engine.Data;

/**
 * The {@link ReportProvider} defines the interface that custom report providers must implement to take part in report
 * generation.
 * <p>
 * The process of generating the test report comprises of two steps:
 * <ol>
 * <li>Data processing: Each data record logged during the test is passed to each report provider. If a report provider
 * is not interested in a certain data record, it simply ignores it. Otherwise, it updates internal statistics. Note
 * that the passed data records should not be stored internally as this may cause memory problems.</li>
 * <li>Report fragment generation: After all data records have been processed, each report provider is asked to generate
 * its share of the test report from the information gathered during data processing.
 * <p>
 * The final test report is an XML file. Each report provider creates a section of the test report. However, the
 * fragment returned need not be an XML snippet, but can be an ordinary structured Java object, for example:
 * 
 * <pre>
 * &#064;XStreamAlias(&quot;general&quot;)
 * public class GeneralReport
 * {
 *     public long bytesSent;
 * 
 *     public long bytesReceived;
 * 
 *     public long hits;
 * 
 *     public Date startTime;
 * 
 *     public Date endTime;
 * 
 *     public int duration;
 * }
 * </pre>
 * 
 * This object and its attributes are automatically converted to XML by the report generator framework:
 * 
 * <pre>
 * &lt;general&gt;
 *     &lt;bytesSent&gt;3911638522&lt;/bytesSent&gt;
 *     &lt;bytesReceived&gt;368679396567&lt;/bytesReceived&gt;
 *     &lt;hits&gt;23463398&lt;/hits&gt;
 *     &lt;startTime&gt;2009-02-18 00:15:19.632 CET&lt;/startTime&gt;
 *     &lt;endTime&gt;2009-02-18 08:20:57.951 CET&lt;/endTime&gt;
 *     &lt;duration&gt;29138&lt;/duration&gt;
 *   &lt;/general&gt;
 * </pre>
 * 
 * This XML snippet is finally inserted into the test report.
 * <p>
 * Note that this conversion to XML is done via the XStream library. This means, by using the XStream annotations
 * <code>@XStreamAlias</code> and <code>@XStreamImplicit</code> with the report object, one has some control how the
 * object's state is represented in XML.</li>
 * </ol>
 * Report provider implementations must be registered with the framework by listing them in the file
 * "xlt/config/reportgenerator.properties". This file may also be used to hold additional implementation specific
 * configuration values. Use {@link ReportProviderConfiguration#getProperties()} to get access to these values.
 * 
 * @see AbstractReportProvider
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public interface ReportProvider extends ReportCreator
{
    /**
     * Processes the passed data record to gather information needed for the test report. Typically, only some internal
     * statistics will be updated.
     *
     * @param data
     *            the data record to process
     */
    public void processDataRecord(Data data);

    /**
     * Processes all data records in the passed container to gather information needed for the test report. Typically,
     * only some internal statistics will be updated.
     * <p>
     * This method should call {@link #processDataRecord(Data)} for each data record in the container.
     *
     * @param dataContainer
     *            the data records to process
     */
    public void processAll(final PostProcessedDataContainer dataContainer);

    /**
     * Sets the report provider's configuration. Use the configuration object to get access to general as well as
     * provider-specific properties stored in the global configuration file.
     *
     * @param config
     *            the report provider configuration
     */
    public void setConfiguration(ReportProviderConfiguration config);

    /**
     * Announce that we want to actually see data for processDataRecord because it might happen that we have some report
     * providers which are using other data
     *
     * @return true if it needs data false otherwise
     */
    public default boolean wantsDataRecords()
    {
        return true;
    }

    /**
     * Tries to lock this provider for data record processing.
     *
     * @return <code>true</code> if the lock could be acquired, <code>false</code> if this provider is already locked by
     *         another thread
     */
    public boolean lock();

    /**
     * Unlocks this provider after data record processing has finished.
     */
    public void unlock();
}
