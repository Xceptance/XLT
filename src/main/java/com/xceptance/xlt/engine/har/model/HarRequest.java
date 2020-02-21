package com.xceptance.xlt.engine.har.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This object contains detailed info about performed request.
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#request">specification</a>
 */
@JsonPropertyOrder(
    {
        "method", "url", "httpVersion", "cookies", "headers", "queryString", "postData", "headersSize", "bodySize", "comment"
    })
public class HarRequest
{
    private final String method;

    private final String url;

    private final String httpVersion;

    private final List<HarCookie> cookies;

    private final List<HarHeader> headers;

    private final List<HarQueryString> queryString;

    private final HarPostData postData;

    private final long headersSize;

    private final long bodySize;

    private final String comment;

    @JsonCreator
    public HarRequest(@JsonProperty("headersSize") long headersSize, @JsonProperty("method") String method, @JsonProperty("url") String url,
                      @JsonProperty("httpVersion") String httpVersion, @JsonProperty("cookies") List<HarCookie> cookies,
                      @JsonProperty("headers") List<HarHeader> headers, @JsonProperty("queryString") List<HarQueryString> queryString,
                      @JsonProperty("postData") HarPostData postData, @JsonProperty("bodySize") long bodySize,
                      @JsonProperty("comment") String comment)
    {
        this.headersSize = headersSize;
        this.method = method;
        this.url = url;
        this.httpVersion = httpVersion;
        this.cookies = cookies;
        this.headers = headers;
        this.queryString = queryString;
        this.postData = postData;
        this.bodySize = bodySize;
        this.comment = comment;
    }

    public List<HarHeader> getHeaders()
    {
        return headers;
    }

    public List<HarQueryString> getQueryString()
    {
        return queryString;
    }

    public long getBodySize()
    {
        return bodySize;
    }

    public HarPostData getPostData()
    {
        return postData;
    }

    public String getHttpVersion()
    {
        return httpVersion;
    }

    public String getMethod()
    {
        return method;
    }

    public String getComment()
    {
        return comment;
    }

    public List<HarCookie> getCookies()
    {
        return cookies;
    }

    public String getUrl()
    {
        return url;
    }

    public long getHeadersSize()
    {
        return headersSize;
    }

    @Override
    public String toString()
    {
        return "HarRequest [headers = " + headers + ", queryString = " + queryString + ", bodySize = " + bodySize + ", postData = " +
               postData + ", httpVersion = " + httpVersion + ", method = " + method + ", comment = " + comment + ", cookies = " + cookies +
               ", url = " + url + ", headersSize = " + headersSize + "]";
    }

    public static class Builder
    {
        private String method;

        private String url;

        private String httpVersion;

        private List<HarCookie> cookies = new ArrayList<>();

        private List<HarHeader> headers = new ArrayList<>();

        private List<HarQueryString> queryString = new ArrayList<>();

        private HarPostData postData;

        private long bodySize = -1L;

        private long headersSize = -1L;

        private String comment;

        public Builder withHeadersSize(long headersSize)
        {
            this.headersSize = headersSize;
            return this;
        }

        public Builder withMethod(String method)
        {
            this.method = method;
            return this;
        }

        public Builder withUrl(String url)
        {
            this.url = url;
            return this;
        }

        public Builder withHttpVersion(String httpVersion)
        {
            this.httpVersion = httpVersion;
            return this;
        }

        public Builder withCookies(List<HarCookie> cookies)
        {
            this.cookies = cookies;
            return this;
        }

        public Builder withHeaders(List<HarHeader> headers)
        {
            this.headers = headers;
            return this;
        }

        public Builder withQueryString(List<HarQueryString> queryString)
        {
            this.queryString = queryString;
            return this;
        }

        public Builder withQueryString(String queryString) throws UnsupportedEncodingException
        {
            final List<HarQueryString> queryStrings = new ArrayList<>();

            final String[] parameters = queryString.split("&");
            for (String parameter : parameters)
            {
                final int idx = parameter.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(parameter.substring(0, idx), "UTF-8") : parameter;
                final String value = idx > 0 && parameter.length() > idx + 1 ? URLDecoder.decode(parameter.substring(idx + 1), "UTF-8")
                                                                             : "";
                if (!key.isEmpty())
                {
                    queryStrings.add(new HarQueryString(key, value, ""));
                }
            }
            this.queryString = queryStrings;
            return this;
        }

        public Builder withPostData(HarPostData postData)
        {
            this.postData = postData;
            return this;
        }

        public Builder withBodySize(long bodySize)
        {
            this.bodySize = bodySize;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarRequest build()
        {
            return new HarRequest(headersSize, method, url, httpVersion, cookies, headers, queryString, postData, bodySize, comment);
        }
    }
}
