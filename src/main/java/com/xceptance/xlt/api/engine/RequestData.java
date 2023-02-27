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

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.lang.ParseNumbers;
import com.xceptance.common.lang.StringHasher;
import com.xceptance.xlt.api.util.XltCharBuffer;
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
    public final static XltCharBuffer UNKNOWN_HOST = XltCharBuffer.valueOf("(unknown)");
    
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
    private XltCharBuffer contentType;

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
    private XltCharBuffer requestId;

    /**
     * The response ID that was sent back by the server.
     */
    private XltCharBuffer responseId;

    /**
     * The request URL.
     */
    private XltCharBuffer url;

    /**
     * The hash code of a url without fragment, needed downstream
     */
    private int hashCodeOfUrlWithoutFragment;
    
    /**
     * The host, parsed from the url early in the process
     */
    private XltCharBuffer host;
    
    /**
     * The HTTP-Method of this request.
     */
    private XltCharBuffer httpMethod;

    /**
     * The form data encoding.
     */
    private XltCharBuffer formDataEncoding;

    /**
     * The form data.
     */
    private XltCharBuffer formData;

    /**
     * The list of IP addresses reported by DNS for the host name used when making the request. If there is more than
     * one IP address, they will be stored separated by IP_ADDRESSES_SEPARATOR.
     */
    private String ipAddresses;
    
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
    public XltCharBuffer getContentType()
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
     * @deprecated Use {@link #getRequestId()} instead.
     */
    @Deprecated
    public String getId()
    {
        return getRequestId();
    }

    /**
     * Returns the request ID that was sent to the server.
     * 
     * @return the request ID
     */
    public String getRequestId()
    {
        return requestId == null ? null : requestId.toString();
    }

    /**
     * Returns the response ID that was sent back by the server.
     * 
     * @return the response ID
     */
    public String getResponseId()
    {
        return responseId == null ? null : responseId.toString();
    }

    /**
     * Returns the request's URL.
     * 
     * @return the URL
     */
    public XltCharBuffer getUrl()
    {
        return url;
    }

    /**
     * Returns the hashcode of the fragment free version of the url
     * 
     * @return the hashcode of the fragment free url 
     */
    public int hashCodeOfUrlWithoutFragment()
    {
        return hashCodeOfUrlWithoutFragment;
    }
    
    /**
     * Returns the host parsed from the url or
     * UNKNOWN_HOST if it does not exist. Never null or empty.
     * 
     * @return the host from the url
     */
    public XltCharBuffer getHost()
    {
        return host;
    }
    
    /**
     * Returns the HTTP method of the request.
     * 
     * @return the HTTP method.
     */
    public XltCharBuffer getHttpMethod()
    {
        return httpMethod;
    }

    /**
     * Returns the encoding of the form data.
     * 
     * @return the data encoding.
     */
    public XltCharBuffer getFormDataEncoding()
    {
        return formDataEncoding;
    }

    /**
     * Returns the form data.
     * 
     * @return the form data.
     */
    public XltCharBuffer getFormData()
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
        return StringUtils.split(ipAddresses, IP_ADDRESSES_SEPARATOR);
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
        this.contentType = XltCharBuffer.valueOf(contentType);
    }
    
    public void setContentType(final XltCharBuffer contentType)
    {
        this.contentType = contentType;
        this.contentType.hashCode();
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
     * @deprecated Use {@link #setRequestId(String)} instead.
     */
    @Deprecated
    public void setId(final XltCharBuffer id)
    {
        setRequestId(id);
    }

    /**
     * Sets the request ID that was sent to the server.
     * 
     * @param id
     *            the request ID
     */
    public void setRequestId(final XltCharBuffer id)
    {
        this.requestId = id;
    }

    /**
     * Sets the request ID that was sent to the server.
     * 
     * @param id
     *            the request ID
     */
    public void setRequestId(final String id)
    {
        this.requestId = XltCharBuffer.valueOf(id);
    }
    
    /**
     * Sets the response ID that was sent back by the server.
     * 
     * @param id
     *            the response ID
     */
    public void setResponseId(final XltCharBuffer id)
    {
        this.responseId = id;
    }

    /**
     * Sets the response ID that was sent back by the server.
     * 
     * @param id
     *            the response ID
     */
    public void setResponseId(final String id)
    {
        this.responseId = XltCharBuffer.valueOf(id);
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
     * Sets the request's URL. This is for encding!
     * 
     * @param url
     *            the URL
     */
    public void setUrl(final String url)
    {
        this.url = XltCharBuffer.valueOf(url);
    }
    
    /**
     * Sets the request's URL. Uses a char buffer for efficiency. 
     * This is for decoding. We do it here because it is more efficient
     * because the data is hotter and we have more cpu available
     * than later in the providers.
     * 
     * @param url
     *            the URL
     */
    public void setUrl(final XltCharBuffer url)
    {
        // remove the fragment if any and compute the hash
        this.hashCodeOfUrlWithoutFragment = StringHasher.hashCodeWithLimit(url, '#');

        final XltCharBuffer hostName = UrlHostParser.retrieveHostFromUrl(url);
        
        if (hostName.length() == 0)
        {
            host = UNKNOWN_HOST;
        }
        else
        {   
            host = hostName;
            hostName.hashCode(); // get the hashcode while it is in the cache
        }
        
        this.url = url;
    }

    /**
     * Set the httpMethod value
     * 
     * @param httpMethod
     *            the new httpMethod value
     */
    public void setHttpMethod(XltCharBuffer httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    /**
     * Set the httpMethod value
     * 
     * @param httpMethod
     *            the new httpMethod value
     */
    public void setHttpMethod(String httpMethod)
    {
        this.httpMethod = XltCharBuffer.valueOf(httpMethod);
    }
    
    /**
     * Set the form data encoding.
     * 
     * @param encoding
     *            the new encoding
     */
    public void setFormDataEncoding(XltCharBuffer encoding)
    {
        this.formDataEncoding = encoding;
    }

    /**
     * Set the form data encoding.
     * 
     * @param encoding
     *            the new encoding
     */
    public void setFormDataEncoding(String encoding)
    {
        this.formDataEncoding = XltCharBuffer.valueOf(encoding);
    }
    
    /**
     * Set the form data.
     * 
     * @param formData
     *            the new data
     */
    public void setFormData(XltCharBuffer formData)
    {
        this.formData = formData;
    }

    /**
     * Set the form data.
     * 
     * @param formData
     *            the new data
     */
    public void setFormData(String formData)
    {
        this.formData = XltCharBuffer.valueOf(formData);
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
     * 
     * @param ipAddresses
     *            the list of IP addresses
     */
    public void setIpAddresses(final String[] ipAddresses)
    {
        this.ipAddresses = StringUtils.join(ipAddresses, IP_ADDRESSES_SEPARATOR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> addValues()
    {
        final List<String> fields = super.addValues();

        fields.add(Integer.toString(bytesSent));
        fields.add(Integer.toString(bytesReceived));
        fields.add(Integer.toString(responseCode));
        fields.add(XltCharBuffer.emptyWhenNull(url).toString());
        fields.add(XltCharBuffer.emptyWhenNull(contentType).toString());
        fields.add(String.valueOf(connectTime));
        fields.add(String.valueOf(sendTime));
        fields.add(String.valueOf(serverBusyTime));
        fields.add(String.valueOf(receiveTime));
        fields.add(String.valueOf(timeToFirstBytes));
        fields.add(String.valueOf(timeToLastBytes));
        fields.add(XltCharBuffer.emptyWhenNull(requestId).toString());

        fields.add(XltCharBuffer.emptyWhenNull(httpMethod).toString());
        fields.add(XltCharBuffer.emptyWhenNull(formDataEncoding).toString());
        fields.add(XltCharBuffer.emptyWhenNull(formData).toString());

        fields.add(String.valueOf(dnsTime));
        fields.add(StringUtils.defaultString(ipAddresses));

        fields.add(XltCharBuffer.emptyWhenNull(responseId).toString());

        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getMinNoCSVElements()
    {
        return 8;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void parseValues(final List<XltCharBuffer> values)
    {
        super.parseValues(values);

        setBytesSent(ParseNumbers.parseInt(values.get(5)));
        setBytesReceived(ParseNumbers.parseInt(values.get(6)));
        setResponseCode(ParseNumbers.parseInt(values.get(7)));

        if (values.size() > 22)
        {
            setUrl(values.get(8));
            setContentType(values.get(9));

            setConnectTime(ParseNumbers.parseInt(values.get(10)));
            setSendTime(ParseNumbers.parseInt(values.get(11)));
            setServerBusyTime(ParseNumbers.parseInt(values.get(12)));
            setReceiveTime(ParseNumbers.parseInt(values.get(13)));
            setTimeToFirstBytes(ParseNumbers.parseInt(values.get(14)));
            setTimeToLastBytes(ParseNumbers.parseInt(values.get(15)));

            setRequestId(values.get(16));
            setHttpMethod(values.get(17));

            setFormDataEncoding(values.get(18));
            setFormData(values.get(19));
            setDnsTime(ParseNumbers.parseInt(values.get(20)));
            ipAddresses = values.get(21).toString();
            setResponseId(values.get(22));
        }
        else
        {
            // do legacy, translate to array because it does rarely happen
            parseLegacyValues(values);
        }
    }

    /**
     * Deal with legacy data of older version
     * 
     * @param values
     *            parsed data
     */
    private void parseLegacyValues(final List<XltCharBuffer> values)
    {
        // be defensive so older reports can be re-generated
        final int length = values.size();
        if (length > 8)
        {
            url = values.get(8);
        }

        if (length > 9)
        {
            contentType = values.get(9);
        }

        if (length > 10)
        {
            setConnectTime(ParseNumbers.parseInt(values.get(10)));
            setSendTime(ParseNumbers.parseInt(values.get(11)));
            setServerBusyTime(ParseNumbers.parseInt(values.get(12)));
            setReceiveTime(ParseNumbers.parseInt(values.get(13)));
            setTimeToFirstBytes(ParseNumbers.parseInt(values.get(14)));
            setTimeToLastBytes(ParseNumbers.parseInt(values.get(15)));
        }

        if (length > 16)
        {
            setRequestId(values.get(16));
        }

        // XLT 4.6.0
        if (length > 17)
        {
            setHttpMethod(values.get(17));
            setFormDataEncoding(values.get(18));
            setFormData(values.get(19));
        }

        // XLT 4.7.0
        if (length > 20)
        {
            setDnsTime(ParseNumbers.parseInt(values.get(20)));
        }
    }
}
