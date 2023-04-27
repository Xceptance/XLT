/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.engine;

import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.common.util.CsvUtils;
import com.xceptance.xlt.api.util.SimpleArrayList;
import com.xceptance.xlt.api.util.XltCharBuffer;

/**
 * Test the implementation of {@link RequestData}.
 *
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class RequestDataTest extends TimerDataTest
{
    /**
     * Decouple from XLT
     */
    private static final Random random = new Random();

    /**
     * Request id.
     */
    protected final String requestId = String.valueOf(random.nextInt(1024));

    /**
     * Response id.
     */
    protected final String responseId = String.valueOf(random.nextInt(1024));

    /**
     * Request HTTP method type.
     */
    protected final String httpMethod = "POST";

    /**
     * Request form data encoding type.
     */
    protected final String formDataEncoding = "application/x-www-form-urlencoded";

    /**
     * Request form data.
     */
    protected final String formData = "foo=1&bar=2";

    /**
     * No. bytes sent.
     */
    protected final int bytesSent = random.nextInt(1024);

    /**
     * No. bytes received.
     */
    protected final int bytesReceived = random.nextInt(1024);

    /**
     * Response status code.
     */
    protected final int responseCode = random.nextInt(501);

    /**
     * Target URL.
     */
    protected final String url = "http://localhost";

    /**
     * Content type of response.
     */
    protected final String contentType = "text/html";

    /**
     * Common CSV representation (equal to {@link TimerData#toCSV()}).
     */
    private final String commonCSV = getCommonCSV();

    /**
     * Default timing values.
     */
    private final int dnsTime = 70;

    private final int connectTime = 10;

    private final int sendTime = 20;

    private final int busyTime = 30;

    private final int receiveTime = 40;

    private final int firstByteTime = 50;

    private final int lastByteTime = 60;

    private final String ipAddresses = "127.0.0.1";

    private final String usedIpAddresses = "127.0.0.1";

    private final String name = "AReqest";

    private final long time = 1654632508330L;

    private final boolean failed = false;

    private final int runTime = 412;


    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * Passes CVS string misses all additional fields maintained by this class.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvMissesAllClassSpecificFields()
    {
        // read in common CSV string (no bytesSent, bytesReceived and
        // responseCode
        // values)
        var instance = fromCsv(commonCSV);
    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * Passed CSV string misses the values for the number of bytes received and for the response code.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvMissesBytesReceivedAndResponseCode()
    {


        // read in CSV string
        var instance = fromCsv(StringUtils.join(new Object[]
            {
                commonCSV, bytesSent
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * Passed CSV string misses the value for the response code.
     * </p>
     */
    @Test(expected = IllegalArgumentException.class)
    public void csvMissesResponseCode()
    {


        // read in CSV string
        var instance = fromCsv(StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * The value of the field <code>bytesSent</code> is not a valid string representation of an integer value. Expecting
     * a NumberFormatException.
     * </p>
     */
    @Test(expected = NumberFormatException.class)
    public void bytesSentInCSVNotInt()
    {


        // read in CSV string
        var instance = fromCsv(StringUtils.join(new Object[]
            {
                commonCSV, "notanInt", bytesReceived, responseCode, url, contentType
            }, Data.DELIMITER));

    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * The value of the field <code>bytesSent</code> is negative. Expecting a RuntimeException.
     * </p>
     */
    @Test(expected = RuntimeException.class)
    public void bytesSentInCSVNegative()
    {


        // read in CSV string
        var instance = fromCsv(StringUtils.join(new Object[]
            {
                commonCSV, -bytesSent, bytesReceived, responseCode, url, contentType
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * The value of the field <code>bytesReceived</code> is not a valid string representation of an integer value.
     * Expecting a NumberFormatException.
     * </p>
     */
    @Test(expected = NumberFormatException.class)
    public void bytesReceivedInCSVNotInt()
    {


        // read in CSV string
        var instance = fromCsv(StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, "NotAnInt", responseCode, url, contentType
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * The value of the field <code>bytesReceived</code> is negative. Expecting a RuntimeException.
     * </p>
     */
    @Test(expected = RuntimeException.class)
    public void bytesReceivedInCSVNegative()
    {


        // read in CSV string
        var instance = fromCsv(StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, -bytesReceived, responseCode, url, contentType
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * The value of the field <code>responseCode</code> is not a valid string representation of an integer value.
     * Expecting a NumberFormatException.
     * </p>
     */
    @Test(expected = NumberFormatException.class)
    public void responseCodeInCVSNotInt()
    {


        // read in CSV string
        var instance = fromCsv(StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived, "NotInt", url, contentType
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * The value of the field <code>responseCode</code> is negative.
     * </p>
     */
    @Test(expected = RuntimeException.class)
    public void responseCodeInCVSNegative()
    {


        // read in CSV string
        var instance = fromCsv(StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived, -1, url, contentType
            }, Data.DELIMITER));
    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * The value of the field <code>url</code> contains the CSV delimiter. This test actually fails and is tracked as
     * bug #101.
     * </p>
     */
    @Test
    public void urlContainsCVSDelimiter()
    {
        // target URL
        final String url = "http://www.jny.com/Sweaters/22962157,default,sc.html";

        final ArrayList<String> elements = new ArrayList<String>();
        elements.addAll(CsvUtils.decodeToList(commonCSV));
        elements.add(Integer.toString(bytesSent));
        elements.add(Integer.toString(bytesReceived));
        elements.add(Integer.toString(responseCode));
        elements.add(url);
        elements.add(contentType);

        // read in CSV string
        var instance = fromCsv(CsvUtils.encode(elements).toString());

        // validate data record
        Assert.assertEquals(name, instance.getName());
        Assert.assertEquals(time + runTime, instance.getEndTime());
        Assert.assertEquals(failed, instance.hasFailed());
        Assert.assertEquals(bytesSent, instance.getBytesSent());
        Assert.assertEquals(bytesReceived, instance.getBytesReceived());
        Assert.assertEquals(responseCode, instance.getResponseCode());
        Assert.assertEquals(XltCharBuffer.valueOf(url), instance.getUrl());
        Assert.assertEquals(XltCharBuffer.valueOf(contentType), instance.getContentType());
    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)}.
     * <p>
     * The value of the field <code>contentType</code> contains the CSV delimiter.
     * </p>
     */
    @Test
    public void contentTypeContainsCVSDelimiter()
    {
        // content type of response
        final String contentType = "application/x-java-applet;version=1.4.1,1.4.2";

        final ArrayList<String> elements = new ArrayList<String>();
        elements.addAll(CsvUtils.decodeToList(commonCSV));
        elements.add(Integer.toString(bytesSent));
        elements.add(Integer.toString(bytesReceived));
        elements.add(Integer.toString(responseCode));
        elements.add(url);
        elements.add(contentType);

        // read in CSV string
        var instance = fromCsv(CsvUtils.encode(elements).toString());

        // validate data record
        Assert.assertEquals(name, instance.getName());
        Assert.assertEquals(time + runTime, instance.getEndTime());
        Assert.assertEquals(failed, instance.hasFailed());
        Assert.assertEquals(bytesSent, instance.getBytesSent());
        Assert.assertEquals(bytesReceived, instance.getBytesReceived());
        Assert.assertEquals(responseCode, instance.getResponseCode());
        Assert.assertEquals(XltCharBuffer.valueOf(url), instance.getUrl());
        Assert.assertEquals(XltCharBuffer.valueOf(contentType), instance.getContentType());
    }

    /**
     * Tests the implementation of {@link RequestData#toCSV()}.
     * <p>
     * Instance doesn't contain valid values for the <code>url</code> and <code>contentType</code> fields.
     * </p>
     */
    @Test
    public void testToCSV_NoURLNoContentType()
    {
        // construct CSV string
        final String csvLine = StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived, responseCode, "", "", 0, 0, 0, 0, 0, 0, "", "", "", "", 0, "", "", ""
            }, Data.DELIMITER);

        // read in CSV string
        var instance = fromCsv(csvLine);

        // validate output of toCSV()
        Assert.assertEquals(csvLine, instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link RequestData#toCSV()}.
     * <p>
     * Instance doesn't contain a valid value for the <code>url</code> field.
     * </p>
     */
    @Test
    public void testToCSV_NoURL()
    {
        // construct CSV string
        final String csvLine = StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived, responseCode, "", contentType, 0, 0, 0, 0, 0, 0, "", "", "", "", 0, "", "", ""
            }, Data.DELIMITER);

        // read in CSV string
        var instance = fromCsv(csvLine);

        // validate output of toCSV()
        Assert.assertEquals(csvLine, instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link RequestData#toCSV()}.
     * <p>
     * Instance doesn't contain a valid value for the <code>contentType</code> field.
     * </p>
     */
    @Test
    public void testToCSV_NoContentType()
    {
        // construct CSV string
        final String csvLine = StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived, responseCode, url, "", 0, 0, 0, 0, 0, 0, "", "", "", "", 0, "", "", ""
            }, Data.DELIMITER);

        // read in CSV string
        var instance = fromCsv(csvLine);

        // validate output of toCSV()
        Assert.assertEquals(csvLine, instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link RequestData#toCSV()}.
     * <p>
     * Test parsing of pre-3.3.3 request data
     * </p>
     */
    @Test
    public void testParsingCompatibility_XLT3_3_3()
    {
        // construct CSV string
        final String csvLine = StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived, responseCode, url, contentType
            }, Data.DELIMITER);

        // read in CSV string
        var instance = fromCsv(csvLine);

        Assert.assertEquals(0, instance.getConnectTime());
        Assert.assertEquals(0, instance.getSendTime());
        Assert.assertEquals(0, instance.getServerBusyTime());
        Assert.assertEquals(0, instance.getReceiveTime());
        Assert.assertEquals(0, instance.getTimeToFirstBytes());
        Assert.assertEquals(0, instance.getTimeToLastBytes());

        // validate output of toCSV()
        Assert.assertEquals(csvLine + ",0,0,0,0,0,0,,,,,0,,,", instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link RequestData#toCSV()}.
     * <p>
     * Test parsing of request data before XLT 4.6.6 with disabled hidden firefox client performance form data feature.
     * When this feature was enabled there were additional form data values in the CSV but when disabled these columns
     * were missing. There was also no DNS time.
     * </p>
     */
    @Test
    public void testParsingCompatibility_XLT_before_4_6_6_noHiddenFormData()
    {
        // construct CSV string
        final String csvLine = getBeforeXLT4_6_6_CSVLine(false);

        // read in CSV string
        var instance = fromCsv(csvLine);

        validateBeforeXLT4_6_6_RequestData(instance, false);

        // validate output of toCSV()
        Assert.assertEquals(csvLine + ",,,,0,,,", instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link RequestData#toCSV()}.
     * <p>
     * Test parsing of request data before XLT 4.6.6 with disabled hidden firefox client performance form data feature.
     * When this feature was enabled there were additional form data values in the CSV but when disabled these columns
     * were missing. There was also no DNS time.
     * </p>
     */
    @Test
    public void testParsingCompatibility_XLT_before_4_6_6_withHiddenFormData()
    {
        // construct CSV string
        final String csvLine = getBeforeXLT4_6_6_CSVLine(true);

        // read in CSV string
        var instance = fromCsv(csvLine);

        validateBeforeXLT4_6_6_RequestData(instance, true);

        // validate output of toCSV()
        Assert.assertEquals(csvLine + ",0,,,", instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link RequestData#toCSV()}.
     * <p>
     * Test parsing of request data for XLT 4.6.6 with disabled hidden firefox client performance form data feature.
     * Since now when this feature was enabled there were additional form data values in the CSV and when disabled these
     * columns were just left blank which matches the behavior of XLT 4.7.0. There was also no DNS time.
     * </p>
     */
    @Test
    public void testParsingCompatibility_XLT_4_6_6_noHiddenFormData()
    {
        // construct CSV string
        final String csvLine = getXLT4_6_6_CSVLine(false);

        // read in CSV string
        var instance = fromCsv(csvLine);

        validateXLT4_6_6_RequestData(instance, false);

        // validate output of toCSV()
        Assert.assertEquals(csvLine + ",0,,,", instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link RequestData#toCSV()}.
     * <p>
     * Test parsing of request data for XLT 4.6.6 with enabled hidden firefox client performance form data feature.
     * Since now when this feature was enabled there were additional form data values in the CSV and when disabled these
     * columns were just left blank which matches the behavior of XLT 4.7.0. There was also no DNS time.
     * </p>
     */
    @Test
    public void testParsingCompatibility_XLT_4_6_6_withHiddenFormData()
    {
        // construct CSV string
        final String csvLine = getXLT4_6_6_CSVLine(true);

        // read in CSV string
        var instance = fromCsv(csvLine);

        validateXLT4_6_6_RequestData(instance, true);

        // validate output of toCSV()
        Assert.assertEquals(csvLine + ",0,,,", instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link RequestData#toCSV()}.
     * <p>
     * Test parsing of request data for XLT 4.6.6 with enabled hidden firefox client performance form data feature.
     * Since now when this feature was enabled there were additional form data values in the CSV and when disabled these
     * columns were just left blank which matches the behavior of XLT 4.7.0. There was also no DNS time.
     * </p>
     */
    @Test
    public void testParsingCompatibility_XLT_4_7_0()
    {
        // construct CSV string
        final String csvLine = getXLT4_7_0_CSVLine(true);

        // read in CSV string
        var instance = fromCsv(csvLine);

        validateXLT4_7_0_RequestData(instance);

        // validate output of toCSV()
        Assert.assertEquals(csvLine + ",,,", instance.toCSV().toString());
    }

    /**
     * Tests the implementation of {@link RequestData#remainingValuesFromCSV(String)} and {@link RequestData#toCSV()}.
     */
    @Test
    public void testParsingCompatibility_XLT_4_12_0()
    {
        final String csvLine = getXLT4_12_0_CSVLine(true);

        var instance = fromCsv(csvLine);
        validateXLT4_12_0_RequestData(instance);

        Assert.assertEquals(csvLine + ",", instance.toCSV().toString());
    }

    /**
     * Returns the common CSV string.
     *
     * @return common CSV string
     */
    private String getCommonCSV()
    {
        final TimerData stat = new TimerData(new RequestData().getTypeCode())
        {
        };

        stat.setTime(time);
        stat.setName(name);
        stat.setFailed(failed);
        stat.setRunTime(runTime);

        return stat.toCSV().toString();

    }
//
//    private void setFormDataOutputEnabled(boolean newEnabledValue)
//    {
//        try
//        {
//            ReflectionUtils.writeStaticField(SessionImpl.class, "COLLECT_ADDITIONAL_REQUEST_DATA", Boolean.valueOf(newEnabledValue));
//        }
//        catch (Exception e)
//        {
//            throw new Error("Failed to set field", e);
//        }
//    }

    /**
     * Get a new CSV line containing all default with or without additional form data values.
     *
     * @param includeformData
     *            if true then the CSV line contains form data values otherwise they are just left blank
     * @return a CSV line with or without additional form data values
     */
    private String getXLT4_12_0_CSVLine(boolean includeformData)
    {
        String httpMethod = "";
        String formDataEncoding = "";
        String formData = "";

        if (includeformData)
        {
            httpMethod = this.httpMethod;
            formDataEncoding = this.formDataEncoding;
            formData = this.formData;
        }
        return StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived, responseCode, url, contentType, connectTime, sendTime, busyTime, receiveTime,
                firstByteTime, lastByteTime, requestId, httpMethod, formDataEncoding, formData, dnsTime, ipAddresses, responseId
            }, Data.DELIMITER);
    }

    /**
     * Get a new CSV line containing all default with or without additional form data values.
     *
     * @param includeformData
     *            if true then the CSV line contains form data values otherwise they are just left blank
     * @return a CSV line with or without additional form data values
     */
    private String getXLT4_7_0_CSVLine(boolean includeformData)
    {
        String httpMethod = "";
        String formDataEncoding = "";
        String formData = "";

        if (includeformData)
        {
            httpMethod = this.httpMethod;
            formDataEncoding = this.formDataEncoding;
            formData = this.formData;
        }
        return StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived, responseCode, url, contentType, connectTime, sendTime, busyTime, receiveTime,
                firstByteTime, lastByteTime, requestId, httpMethod, formDataEncoding, formData, dnsTime
            }, Data.DELIMITER);
    }

    /**
     * Get a new CSV line with all default values valid for XLT 4.6.6. So we don't have a DNS time and for the firefox
     * client performance there was a hidden feature to output additional post data.
     *
     * @param includeformData
     *            if true then the CSV line contains form data values otherwise they are just left blank
     * @return a CSV line with or without additional form data values
     */
    private String getXLT4_6_6_CSVLine(boolean includeformData)
    {
        String httpMethod = "";
        String formDataEncoding = "";
        String formData = "";

        if (includeformData)
        {
            httpMethod = this.httpMethod;
            formDataEncoding = this.formDataEncoding;
            formData = this.formData;
        }
        return StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived, responseCode, url, contentType, connectTime, sendTime, busyTime, receiveTime,
                firstByteTime, lastByteTime, requestId, httpMethod, formDataEncoding, formData
            }, Data.DELIMITER);
    }

    /**
     * Get a new CSV line with all default values before XLT 4.6.6. So we don't have a DNS time and for the firefox
     * client performance there was a hidden feature to output additional post data but if the feature was disabled
     * there were no post data columns. Since 4.6.6 the behavior was like for 4.7.0 where the columns are left blank.
     *
     * @param includeformData
     *            if true then the CSV line contains form data values otherwise they are not present
     * @return a CSV line with or without additional form data values
     */
    private String getBeforeXLT4_6_6_CSVLine(boolean includeformData)
    {
        if (includeformData)
        {
            // with form data values
            return StringUtils.join(new Object[]
                {
                    commonCSV, bytesSent, bytesReceived, responseCode, url, contentType, connectTime, sendTime, busyTime, receiveTime,
                    firstByteTime, lastByteTime, requestId, httpMethod, formDataEncoding, formData
                }, Data.DELIMITER);
        }
        // no form data columns in csv
        return StringUtils.join(new Object[]
            {
                commonCSV, bytesSent, bytesReceived, responseCode, url, contentType, connectTime, sendTime, busyTime, receiveTime,
                firstByteTime, lastByteTime, requestId
            }, Data.DELIMITER);
    }

    /**
     * Assert that the given request data instance contains all the default values for XLT 7.0.0
     *
     * @param instance
     *            the request data instance to validate
     */
    private void validateXLT7_0_0_RequestData(RequestData instance)
    {
        validateRequestData(instance, true, 70000);
    }

    /**
     * Assert that the given request data instance contains all the default values for XLT 4.12.0
     *
     * @param instance
     *            the request data instance to validate
     */
    private void validateXLT4_12_0_RequestData(RequestData instance)
    {
        validateRequestData(instance, true, 4_12_00);
    }

    /**
     * Assert that the given request data instance contains all the default values for XLT 4.7.0
     *
     * @param instance
     *            the request data instance to validate
     */
    private void validateXLT4_7_0_RequestData(RequestData instance)
    {
        validateRequestData(instance, true, 4_07_00);
    }

    /**
     * Assert that the given request data instance contains all the default values for XLT 4.6.6
     *
     * @param instance
     *            the request data instance to validate
     * @param hiddenFormDataFeatureEnabled
     *            if true then the form data should be present otherwise the should be just left blank
     */
    private void validateXLT4_6_6_RequestData(RequestData instance, boolean hiddenFormDataFeatureEnabled)
    {
        validateRequestData(instance, hiddenFormDataFeatureEnabled, 4_06_06);
    }

    /**
     * Assert that the given request data instance contains all the default values before XLT 4.6.6
     *
     * @param instance
     *            the request data instance to validate
     * @param hiddenFormDataFeatureEnabled
     *            if true then the form data should be present otherwise the should be null
     */
    private void validateBeforeXLT4_6_6_RequestData(RequestData instance, boolean hiddenFormDataFeatureEnabled)
    {
        validateRequestData(instance, hiddenFormDataFeatureEnabled, 4_06_00);
    }

    /**
     * A common request data validation to assert that the given request data instance contains all the default values
     *
     * @param instance
     *            the request data instance to validate
     * @param testFormData
     *            if true the also validate the formData otherwise the request data values should be empty
     * @param testDnsTime
     *            if true the also validate the dns time otherwise the request data value should be 0
     */
    private void validateRequestData(RequestData instance, boolean hasFormData, int xltVersion)
    {
        Assert.assertEquals(time, instance.getTime());
        Assert.assertEquals(name, instance.getName());
        Assert.assertEquals(failed, instance.hasFailed());
        Assert.assertEquals(runTime, instance.getRunTime());

        Assert.assertEquals(bytesSent, instance.getBytesSent());
        Assert.assertEquals(bytesReceived, instance.getBytesReceived());
        Assert.assertEquals(responseCode, instance.getResponseCode());
        Assert.assertEquals(url, instance.getUrl().toString());
        Assert.assertEquals(contentType, instance.getContentType().toString());

        Assert.assertEquals(connectTime, instance.getConnectTime());
        Assert.assertEquals(sendTime, instance.getSendTime());
        Assert.assertEquals(busyTime, instance.getServerBusyTime());
        Assert.assertEquals(receiveTime, instance.getReceiveTime());
        Assert.assertEquals(firstByteTime, instance.getTimeToFirstBytes());
        Assert.assertEquals(lastByteTime, instance.getTimeToLastBytes());
        Assert.assertEquals(requestId, instance.getRequestId());

        // XLT 4.6.6 (as hidden feature, officially released in XLT 4.7.0)
        String httpMethod = xltVersion < 4_06_06 ? null : "";
        String formDataEncoding = xltVersion < 4_06_06 ? null : "";
        String formData = xltVersion < 4_06_06 ? null : "";
        if (hasFormData)
        {
            httpMethod = this.httpMethod;
            formDataEncoding = this.formDataEncoding;
            formData = this.formData;
        }
        Assert.assertEquals(XltCharBuffer.valueOf(httpMethod), instance.getHttpMethod());
        Assert.assertEquals(XltCharBuffer.valueOf(formDataEncoding), instance.getFormDataEncoding());
        Assert.assertEquals(XltCharBuffer.valueOf(formData), instance.getFormData());
        
        // XLT 4.7.0
        int dnsTime = (xltVersion < 40700) ? 0 : this.dnsTime;
        Assert.assertEquals(dnsTime, instance.getDnsTime());

        // XLT 4.12.0
        String responseId = (xltVersion < 4_12_00) ? null : this.responseId;
        Assert.assertEquals(responseId, instance.getResponseId());

        // XLT 7.0.0
        String usedIpAddress = (xltVersion < 7_00_00) ? null : this.usedIpAddresses;
        Assert.assertEquals(usedIpAddress, instance.getUsedIpAddress());
    }

    /**
     * Just a helper to keep the old test cases alive
     * @param csv
     * @return
     */
    private static RequestData fromCsv(final String csv)
    {
        var instance = new RequestData();
        var result = new SimpleArrayList<XltCharBuffer>(10);

        instance.baseValuesFromCSV(result, XltCharBuffer.valueOf(csv));
        instance.remainingValuesFromCSV(result);

        return instance;
    }
}
