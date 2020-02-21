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
 * Note that this conversion to XML is done via the XStream library. This means, by using the XStream annotations @XStreamAlias
 * and @XStreamImplicit with the report object, one has some control how the object's state is represented in XML.</li>
 * </ol>
 * Report provider implementations must be registered with the framework by listing them in the file
 * "xlt/config/reportgenerator.properties". This file may also be used to hold additional implementation specific
 * configuration values. Use {@link ReportProviderConfiguration#getProperties()} to get access to these values.
 * 
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
     * Sets the report provider's configuration. Use the configuration object to get access to general as well as
     * provider-specific properties stored in the global configuration file.
     * 
     * @param config
     *            the report provider configuration
     */
    public void setConfiguration(ReportProviderConfiguration config);
}
