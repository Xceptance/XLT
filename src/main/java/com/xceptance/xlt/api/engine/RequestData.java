/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.common.lang.StringHasher;
import com.xceptance.common.util.CsvByteColumns;
import com.xceptance.xlt.report.util.UrlHostParser;

/**
 * <p>
 * The {@link RequestData} class holds any data measured for a request. Typically, a request represents one call to a
 * (remote) server.
 * </p>
 * <p>
 * The values stored include not only the request's start and run time, but also an indicator whether or not the request
 * was executed successfully. Data gathered for the same type of request may be correlated via the name attribute.
 * </p>
 * <p style="color:green">
 * Note that {@link RequestData} objects have an "R" as their type code.
 * </p>
 *
 * @see ActionData
 * @see TransactionData
 * @see CustomData
 * @see EventData
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class RequestData extends TimerData
{
    /**
     * The type code.
     */
    private static final char TYPE_CODE = 'R';

    /**
     * The character used to separate multiple IP addresses.
     */
    private static final char IP_ADDRESSES_SEPARATOR = '|';

    /**
     * The value to show if the host could not be determined from a URL.
     */
    public final static String UNKNOWN_HOST = "(unknown)";
    
    /**
     * No response code available
     */
    public final static String NO_RESPONSE_CODE = "0";

    /**
     * Pre-allocated and cached String instances for common HTTP response codes (0..599).
     * Eliminates millions of string allocations during high-throughput report generation.
     */
    private static final String[] COMMON_RESPONSE_CODE_STRINGS = new String[600];
    static
    {
        for (int i = 0; i < COMMON_RESPONSE_CODE_STRINGS.length; i++)
        {
            COMMON_RESPONSE_CODE_STRINGS[i] = Integer.toString(i);
        }
    }

    /**
     * The size of the response message in bytes.
     */
    private int bytesReceived;

    /**
     * The size of the request message in bytes.
     */
    private int bytesSent;

    /**
     * The time it took to connect to the server.
     */
    private int connectTime;

    /**
     * The content type of the response.
     */
    private String contentType;

    /**
     * The time it took to receive the response from the server.
     */
    private int receiveTime;

    /**
     * The response code.
     */
    private int responseCode;

    /**
     * The time it took to send the request to the server.
     */
    private int sendTime;

    /**
     * The time it took the server the process the request.
     */
    private int serverBusyTime;

    /**
     * The total time till the first server bytes arrived including connect and server busy time.
     */
    private int timeToFirstBytes;

    /**
     * The total time to read everything including connect, server busy, and full read time.
     */
    private int timeToLastBytes;

    /**
     * The time it took to look up the IP address for a host name using a name server.
     */
    private int dnsTime;

    /**
     * The value to identify a request.
     */
    private String requestId;

    /**
     * The response ID that was sent back by the server.
     */
    private String responseId;

    /**
     * The request URL.
     */
    private String url;

    /**
     * Raw URL bytes when ingested directly from byte stream.
     */
    private byte[] urlBytes;
    private int urlOffset;
    private int urlLength;

    /**
     * We need this for a later efficient search using urlText when reporting
     */
    private String originalUrl;

    /**
     * The hash code of a url without fragment, needed downstream
     */
    private int hashCodeOfUrlWithoutFragment;

    /**
     * The host, parsed from the url early in the process
     */
    private String host;

    /**
     * The HTTP-Method of this request.
     */
    private String httpMethod;

    /**
     * The form data encoding.
     */
    private String formDataEncoding;

    /**
     * The form data.
     */
    private String formData;

    /**
     * The list of IP addresses reported by DNS for the host name used when making the request. If there is more than
     * one IP address, they will be stored separated by IP_ADDRESSES_SEPARATOR. Will not be set if the request did not trigger
     * a DNS address resolution, for example, in case of keep-alive connections.
     */
    private String ipAddresses;

    /**
     * Pre-split array representation of IP addresses, cached to eliminate millions of join/split conversions.
     */
    private String[] ipAddressesArray;

    /**
     * The target IP address of the system under test that was used when making the request. This info is useful only if
     * the target system has multiple IP addresses, for example, if it is located behind a CDN. Diverging IP address
     * usage counts might be a sign of traffic distribution problems.
     */
    private String usedIpAddress;

    /**
     * Creates a new RequestData object.
     */
    public RequestData()
    {
        super(TYPE_CODE);
    }

    /**
     * Creates a new RequestData object and gives it the specified name. Furthermore, the start time attribute is set to
     * the current time.
     *
     * @param name
     *            the request name
     */
    public RequestData(final String name)
    {
        super(name, TYPE_CODE);
    }

    /**
     * Returns the size of the response message.
     *
     * @return the bytes received
     */
    public int getBytesReceived()
    {
        return bytesReceived;
    }

    /**
     * Returns the size of the request message.
     *
     * @return the bytes sent
     */
    public int getBytesSent()
    {
        return bytesSent;
    }

    /**
     * Returns the time it took to connect to the server.
     *
     * @return the connect time
     */
    public int getConnectTime()
    {
        return connectTime;
    }

    /**
     * Returns the response's content type.
     *
     * @return the content type
     */
    public String getContentType()
    {
        return contentType;
    }

    /**
     * Returns the time it took to receive the response from the server.
     *
     * @return the receive time
     */
    public int getReceiveTime()
    {
        return receiveTime;
    }

    /**
     * Returns the request's response code.
     *
     * @return the response code
     */
    public int getResponseCode()
    {
        return responseCode;
    }

    /**
     * Returns the time it took to send the request to the server.
     *
     * @return the send time
     */
    public int getSendTime()
    {
        return sendTime;
    }

    /**
     * Returns the time it took the server the process the request.
     *
     * @return the server busy time
     */
    public int getServerBusyTime()
    {
        return serverBusyTime;
    }

    /**
     * Returns the time until the first response bytes arrived, including connect time and server busy time.
     *
     * @return the time to first bytes
     */
    public int getTimeToFirstBytes()
    {
        return timeToFirstBytes;
    }

    /**
     * Returns the time needed to read all response bytes, including connect time and server busy time.
     *
     * @return the time to last bytes
     */
    public int getTimeToLastBytes()
    {
        return timeToLastBytes;
    }

    /**
     * Returns the request ID that was sent to the server.
     *
     * @return the request ID
     */
    public String getRequestId()
    {
        return requestId;
    }

    /**
     * Returns the response ID that was sent back by the server.
     *
     * @return the response ID
     */
    public String getResponseId()
    {
        return responseId;
    }

    /**
     * Returns the request's URL.
     *
     * @return the URL
     */
    public String getUrl()
    {
        if (url == null && urlBytes != null)
        {
            this.url = new String(urlBytes, urlOffset, urlLength, StandardCharsets.UTF_8);
            this.originalUrl = this.url;
        }
        return url;
    }

    /**
     * Returns the request's original URL. Lazily computes the string representation from the
     * underlying URL char buffer or raw byte slice if it has not already been populated, eliminating
     * millions of premature heap allocations during report generation.
     *
     * @return the original URL string
     */
    public String getOriginalUrl()
    {
        return getUrl();
    }

    /**
     * Returns the hashcode of the fragment free version of the url
     *
     * @return the hashcode of the fragment free url
     */
    public int hashCodeOfUrlWithoutFragment()
    {
        if (hashCodeOfUrlWithoutFragment == 0)
        {
            final String u = getUrl();
            if (u != null)
            {
                this.hashCodeOfUrlWithoutFragment = StringHasher.hashCodeWithLimit(u, '#');
            }
        }
        return hashCodeOfUrlWithoutFragment;
    }

    /**
     * Returns the host parsed from the url or
     * UNKNOWN_HOST if it does not exist. Never null or empty.
     *
     * @return the host from the url
     */
    public String getHost()
    {
        if (host == null)
        {
            final String u = getUrl();
            if (u != null)
            {
                final String hostName = UrlHostParser.retrieveHostFromUrl(u);
                this.host = (hostName == null || hostName.length() == 0) ? UNKNOWN_HOST : hostName;
            }
            else
            {
                this.host = UNKNOWN_HOST;
            }
        }
        return host != null ? host : UNKNOWN_HOST;
    }

    /**
     * Returns the HTTP method of the request.
     *
     * @return the HTTP method.
     */
    public String getHttpMethod()
    {
        return httpMethod;
    }

    /**
     * Returns the encoding of the form data.
     *
     * @return the data encoding.
     */
    public String getFormDataEncoding()
    {
        return formDataEncoding;
    }

    /**
     * Returns the form data.
     *
     * @return the form data.
     */
    public String getFormData()
    {
        return formData;
    }

    /**
     * Returns the time it took to look up the IP address for a host name.
     *
     * @return the look-up time
     */
    public int getDnsTime()
    {
        return dnsTime;
    }

    /**
     * Returns the list of IP addresses reported by DNS for the host name used when making the request.
     *
     * @return the list of IP addresses
     */
    public String[] getIpAddresses()
    {
        if (ipAddressesArray == null && ipAddresses != null)
        {
            ipAddressesArray = StringUtils.split(ipAddresses, IP_ADDRESSES_SEPARATOR);
        }
        return ipAddressesArray;
    }

    /**
     * Returns the pipe-delimited string of IP addresses reported by DNS.
     *
     * @return the delimited IP address string, or null
     */
    public String getIpAddressesAsString()
    {
        if (ipAddresses == null && ipAddressesArray != null)
        {
            ipAddresses = StringUtils.join(ipAddressesArray, IP_ADDRESSES_SEPARATOR);
        }
        return ipAddresses;
    }

    /**
     * Returns the target IP address of the system under test that was used when making the request.
     *
     * @return the used IP address
     */
    public String getUsedIpAddress()
    {
        return usedIpAddress;
    }

    /**
     * Sets the size of the response message
     *
     * @param responseSize
     *            the response size
     */
    public void setBytesReceived(final int responseSize)
    {
        if (responseSize >= 0)
        {
            bytesReceived = responseSize;
        }
        else
        {
            throw new IllegalArgumentException("Response size must not be negative: '" + responseSize + "'.");
        }
    }

    /**
     * Sets the size of the request message
     *
     * @param requestSize
     *            the request size
     */
    public void setBytesSent(final int requestSize)
    {
        if (requestSize >= 0)
        {
            bytesSent = requestSize;
        }
        else
        {
            throw new IllegalArgumentException("Request size must not be negative: '" + requestSize + "'.");
        }
    }

    /**
     * Sets The time it took to connect to the server.
     *
     * @param connectTime
     *            the connect time
     */
    public void setConnectTime(final int connectTime)
    {
        this.connectTime = connectTime;
    }

    /**
     * Sets the response's content type.
     *
     * @param contentType
     *            the contentType
     */
    public void setContentType(final String contentType)
    {
        this.contentType = contentType;
    }

    /**
     * Sets the time it took to receive the response from the server.
     *
     * @param receiveTime
     *            the receive time
     */
    public void setReceiveTime(final int receiveTime)
    {
        this.receiveTime = receiveTime;
    }

    /**
     * Sets the request ID that was sent to the server.
     *
     * @param id
     *            the request ID
     */
    public void setRequestId(final String id)
    {
        this.requestId = id;
    }

    /**
     * Sets the response ID that was sent back by the server.
     *
     * @param id
     *            the response ID
     */
    public void setResponseId(final String id)
    {
        this.responseId = id;
    }

    /**
     * Sets the request's response code.
     *
     * @param responseCode
     *            the response code
     */
    public void setResponseCode(final int responseCode)
    {
        if (responseCode >= 0)
        {
            this.responseCode = responseCode;
        }
        else
        {
            throw new IllegalArgumentException("Response code must not be negative: " + responseCode + "'.");
        }
    }

    /**
     * Sets the request's response code.
     *
     * @param responseCode
     *            the response code
     */
    public void setResponseCode(final String responseCode)
    {
        if (responseCode != null && !responseCode.isEmpty())
        {
            final int code = ParseNumbers.parseInt(responseCode);
            if (code >= 0)
            {
                this.responseCode = code;
            }
            else
            {
                throw new IllegalArgumentException("Response code must not be negative: " + responseCode + "'.");
            }
        }
        else
        {
            throw new IllegalArgumentException("Response code must not be null or empty");
        }
    }

    /**
     * Get the request's response code as a cached string.
     *
     * @return the response code string
     */
    public String getResponseCodeAsString()
    {
        final int code = this.responseCode;
        if (code >= 0 && code < COMMON_RESPONSE_CODE_STRINGS.length)
        {
            return COMMON_RESPONSE_CODE_STRINGS[code];
        }
        return Integer.toString(code);
    }

    /**
     * Get the request's response code as originally recorded.
     *
     * @return responseCode the response code as chars
     */
    public CharSequence getResponseCodeAsChars()
    {
        return getResponseCodeAsString();
    }

    /**
     * Sets the time it took to send the request to the server.
     *
     * @param sendTime
     *            the send time
     */
    public void setSendTime(final int sendTime)
    {
        this.sendTime = sendTime;
    }

    /**
     * Sets the time it took the server the process the request.
     *
     * @param serverBusyTime
     *            the server busy time
     */
    public void setServerBusyTime(final int serverBusyTime)
    {
        this.serverBusyTime = serverBusyTime;
    }

    /**
     * Set the timeToFirstBytes attribute
     *
     * @param timeToFirstBytes
     *            the new timeToFirstBytes value
     */
    public void setTimeToFirstBytes(final int timeToFirstBytes)
    {
        this.timeToFirstBytes = timeToFirstBytes;
    }

    /**
     * Set the timeToLastBytes attribute
     *
     * @param timeToLastBytes
     *            the new timeToLastBytes value
     */
    public void setTimeToLastBytes(final int timeToLastBytes)
    {
        this.timeToLastBytes = timeToLastBytes;
    }

    /**
     * Sets the request's URL.
     *
     * @param url
     *            the URL
     */
    public void setUrl(final String url)
    {
        this.url = url;
        this.originalUrl = url;
        this.urlBytes = null;
        this.urlOffset = 0;
        this.urlLength = 0;
        if (url != null)
        {
            this.hashCodeOfUrlWithoutFragment = StringHasher.hashCodeWithLimit(url, '#');
            final String hostName = UrlHostParser.retrieveHostFromUrl(url);
            this.host = (hostName == null || hostName.length() == 0) ? UNKNOWN_HOST : hostName;
        }
        else
        {
            this.hashCodeOfUrlWithoutFragment = 0;
            this.host = UNKNOWN_HOST;
        }
    }

    /**
     * Sets the request's URL directly from a raw byte buffer slice.
     * Eliminates premature String allocations during CSV ingestion.
     *
     * @param buffer
     *            the byte buffer
     * @param offset
     *            the offset within the buffer
     * @param length
     *            the length of the URL slice
     */
    public void setUrl(final byte[] buffer, final int offset, final int length)
    {
        this.urlBytes = buffer;
        this.urlOffset = offset;
        this.urlLength = length;
        this.url = null;
        this.originalUrl = null;
        this.hashCodeOfUrlWithoutFragment = 0;
        this.host = null;
    }

    /**
     * Returns whether this request has an unmaterialized raw byte URL.
     */
    public boolean hasUrlBytes()
    {
        return this.urlBytes != null && this.urlLength > 0;
    }

    /**
     * Returns the raw URL byte buffer.
     */
    public byte[] getUrlBytes()
    {
        return this.urlBytes;
    }

    /**
     * Returns the start offset of the raw URL bytes.
     */
    public int getUrlOffset()
    {
        return this.urlOffset;
    }

    /**
     * Returns the length of the raw URL bytes.
     */
    public int getUrlLength()
    {
        return this.urlLength;
    }

    /**
     * Fast-path URL setter for high-throughput columnar scanning where the host and fragment-free
     * hash code have been precomputed across sample URLs. Avoids per-row host parsing, string hashing,
     * and string allocation.
     *
     * @param url
     *            the pre-allocated URL string
     * @param host
     *            the precomputed host string
     * @param hashCodeOfUrlWithoutFragment
     *            the precomputed URL hash code without fragment
     */
    public void setUrlFast(final String url, final String host, final int hashCodeOfUrlWithoutFragment)
    {
        this.url = url;
        this.host = (host != null && host.length() > 0) ? host : UNKNOWN_HOST;
        this.hashCodeOfUrlWithoutFragment = hashCodeOfUrlWithoutFragment;
        this.originalUrl = url;
        this.urlBytes = null;
        this.urlOffset = 0;
        this.urlLength = 0;
    }

    /**
     * Sets the host.
     *
     * @param host
     *            the host
     */
    public void setHost(final String host)
    {
        this.host = host;
    }

    /**
     * Set the httpMethod value
     *
     * @param httpMethod
     *            the new httpMethod value
     */
    public void setHttpMethod(final String httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    /**
     * Set the form data encoding.
     *
     * @param encoding
     *            the new encoding
     */
    public void setFormDataEncoding(final String encoding)
    {
        this.formDataEncoding = encoding;
    }

    /**
     * Set the form data.
     *
     * @param formData
     *            the new data
     */
    public void setFormData(final String formData)
    {
        this.formData = formData;
    }

    /**
     * Sets the time it took to look up the IP address for a host name.
     *
     * @param dnsTime
     *            the look-up time
     */
    public void setDnsTime(final int dnsTime)
    {
        this.dnsTime = dnsTime;
    }

    /**
     * Sets the list of IP addresses reported by DNS for the host name used when making the request.
     * Caches the array directly without joining into a pipe-delimited string, eliminating millions
     * of string allocations in high-throughput loops.
     *
     * @param ipAddresses
     *            the list of IP addresses
     */
    public void setIpAddresses(final String[] ipAddresses)
    {
        this.ipAddressesArray = ipAddresses;
        this.ipAddresses = null; // Lazily computed on demand if getIpAddressesAsString() is called
    }

    /**
     * Sets the target IP address of the system under test that was used when making the request.
     *
     * @param ipAddress
     *            the used IP address
     */
    public void setUsedIpAddress(final String ipAddress)
    {
        this.usedIpAddress = ipAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toList()
    {
        final List<String> fields = super.toList();

        fields.add(Integer.toString(bytesSent));
        fields.add(Integer.toString(bytesReceived));
        fields.add(Integer.toString(responseCode));
        fields.add(StringUtils.defaultString(getUrl()));
        fields.add(StringUtils.defaultString(contentType));
        fields.add(String.valueOf(connectTime));
        fields.add(String.valueOf(sendTime));
        fields.add(String.valueOf(serverBusyTime));
        fields.add(String.valueOf(receiveTime));
        fields.add(String.valueOf(timeToFirstBytes));
        fields.add(String.valueOf(timeToLastBytes));
        fields.add(StringUtils.defaultString(requestId));

        fields.add(StringUtils.defaultString(httpMethod));
        fields.add(StringUtils.defaultString(formDataEncoding));
        fields.add(StringUtils.defaultString(formData));

        fields.add(String.valueOf(dnsTime));
        fields.add(StringUtils.defaultString(getIpAddressesAsString()));

        fields.add(StringUtils.defaultString(responseId));

        fields.add(StringUtils.defaultString(usedIpAddress));

        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRemainingValues(final CsvByteColumns values)
    {
        super.setRemainingValues(values);

        setBytesSent(values.parseInt(5));
        setBytesReceived(values.parseInt(6));
        setResponseCode(values.parseInt(7));

        if (values.size() > 23)
        {
            setUrl(values.getBuffer(), values.getOffset(8), values.getLength(8));
            setContentType(values.toString(9));

            setConnectTime(values.parseInt(10));
            setSendTime(values.parseInt(11));
            setServerBusyTime(values.parseInt(12));
            setReceiveTime(values.parseInt(13));
            setTimeToFirstBytes(values.parseInt(14));
            setTimeToLastBytes(values.parseInt(15));

            setRequestId(values.toString(16));

            // XLT 4.6.6 (as hidden feature, officially released in XLT 4.7.0)
            setHttpMethod(values.toString(17));
            setFormDataEncoding(values.toString(18));
            setFormData(values.toString(19));

            // XLT 4.7.0
            setDnsTime(values.parseInt(20));

            // XLT 4.12.0
            ipAddresses = values.toString(21);
            ipAddressesArray = null;
            setResponseId(values.toString(22));

            // XLT 7.0.0
            setUsedIpAddress(values.toString(23));
        }
        else
        {
            // do legacy parsing which is a bit slower
            parseLegacyValues(values);
        }
    }

    /**
     * Deal with legacy data of older version
     *
     * @param values
     *            parsed data
     */
    private void parseLegacyValues(final CsvByteColumns values)
    {
        // be defensive so older reports can be re-generated
        final int length = values.size();
        if (length > 8)
        {
            setUrl(values.getBuffer(), values.getOffset(8), values.getLength(8));
        }

        if (length > 9)
        {
            setContentType(values.toString(9));
        }

        if (length > 10)
        {
            setConnectTime(values.parseInt(10));
            setSendTime(values.parseInt(11));
            setServerBusyTime(values.parseInt(12));
            setReceiveTime(values.parseInt(13));
            setTimeToFirstBytes(values.parseInt(14));
            setTimeToLastBytes(values.parseInt(15));
        }

        if (length > 16)
        {
            setRequestId(values.toString(16));
        }

        // XLT 4.6.6 (as hidden feature, officially released in XLT 4.7.0)
        if (length > 17)
        {
            setHttpMethod(values.toString(17));
            setFormDataEncoding(values.toString(18));
            setFormData(values.toString(19));
        }

        // XLT 4.7.0
        if (length > 20)
        {
            setDnsTime(values.parseInt(20));
        }

        // XLT 4.12.0
        if (length > 21)
        {
            ipAddresses = values.toString(21);
            setResponseId(values.toString(22));
        }

        // XLT 7.0.0
        if (length > 23)
        {
            setUsedIpAddress(values.toString(23));
        }
    }
}
