/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.har.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * This object contains detailed info about the response.
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#response">specification</a>
 */
@JsonPropertyOrder(
    {
        "status", "statusText", "httpVersion", "cookies", "headers", "content", "redirectURL", "headersSize", "bodySize", "comment"
    })
public class HarResponse
{
    private final int status;

    private final String statusText;

    private final String httpVersion;

    private final List<HarCookie> cookies;

    private final List<HarHeader> headers;

    private final HarContent content;

    private final String redirectURL;

    private final long headersSize;

    private final long bodySize;

    private final String comment;

    @JsonCreator
    public HarResponse(@JsonProperty("status") int status, @JsonProperty("statusText") String statusText,
                       @JsonProperty("httpVersion") String httpVersion, @JsonProperty("cookies") List<HarCookie> cookies,
                       @JsonProperty("headers") List<HarHeader> headers, @JsonProperty("content") HarContent content,
                       @JsonProperty("redirectURL") String redirectURL, @JsonProperty("headersSize") long headersSize,
                       @JsonProperty("bodySize") long bodySize, @JsonProperty("comment") String comment)
    {
        this.status = status;
        this.statusText = statusText;
        this.httpVersion = httpVersion;
        this.cookies = cookies;
        this.headers = headers;
        this.content = content;
        this.redirectURL = redirectURL;
        this.headersSize = headersSize;
        this.bodySize = bodySize;
        this.comment = comment;
    }

    public HarContent getContent()
    {
        return content;
    }

    public List<HarHeader> getHeaders()
    {
        return headers;
    }

    public long getBodySize()
    {
        return bodySize;
    }

    public String getHttpVersion()
    {
        return httpVersion;
    }

    public int getStatus()
    {
        return status;
    }

    public String getRedirectURL()
    {
        return redirectURL;
    }

    public String getStatusText()
    {
        return statusText;
    }

    public String getComment()
    {
        return comment;
    }

    public List<HarCookie> getCookies()
    {
        return cookies;
    }

    public long getHeadersSize()
    {
        return headersSize;
    }

    @Override
    public String toString()
    {
        return "HarResponse [content = " + content + ", headers = " + headers + ", bodySize = " + bodySize + ", httpVersion = " +
               httpVersion + ", status = " + status + ", redirectURL = " + redirectURL + ", statusText = " + statusText + ", comment = " +
               comment + ", cookies = " + cookies + ", headersSize = " + headersSize + "]";
    }

    public static class Builder
    {
        private int status;

        private String statusText;

        private String httpVersion;

        private List<HarCookie> cookies = new ArrayList<>();

        private List<HarHeader> headers = new ArrayList<>();

        private HarContent content;

        private String redirectURL;

        private long headersSize = -1L;

        private long bodySize = -1L;

        private String comment;

        public Builder withStatus(int status)
        {
            this.status = status;
            return this;
        }

        public Builder withStatusText(String statusText)
        {
            this.statusText = statusText;
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

        public Builder withContent(HarContent content)
        {
            this.content = content;
            return this;
        }

        public Builder withRedirectURL(String redirectURL)
        {
            this.redirectURL = redirectURL;
            return this;
        }

        public Builder withHeadersSize(long headersSize)
        {
            this.headersSize = headersSize;
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

        public HarResponse build()
        {
            return new HarResponse(status, statusText, httpVersion, cookies, headers, content, redirectURL, headersSize, bodySize, comment);
        }
    }
}
