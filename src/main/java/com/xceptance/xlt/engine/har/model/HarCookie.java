/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Cookie used in request and/or response.
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#cookies">specification</a>
 */
@JsonPropertyOrder(
    {
        "name", "value", "path", "domain", "expires", "httpOnly", "secure", "comment"
    })
public class HarCookie
{
    private final String name;

    private final String value;

    private final String path;

    private final String domain;

    private final String expires;

    private final boolean httpOnly;

    private final boolean secure;

    private final String comment;

    @JsonCreator
    public HarCookie(@JsonProperty("name") String name, @JsonProperty("value") String value, @JsonProperty("path") String path,
                     @JsonProperty("domain") String domain, @JsonProperty("expires") String expires,
                     @JsonProperty("httpOnly") boolean httpOnly, @JsonProperty("secure") boolean secure,
                     @JsonProperty("comment") String comment)
    {
        this.name = name;
        this.value = value;
        this.path = path;
        this.domain = domain;
        this.expires = expires;
        this.httpOnly = httpOnly;
        this.secure = secure;
        this.comment = comment;
    }

    public String getExpires()
    {
        return expires;
    }

    public String getName()
    {
        return name;
    }

    public boolean getSecure()
    {
        return secure;
    }

    public String getDomain()
    {
        return domain;
    }

    public String getPath()
    {
        return path;
    }

    public String getValue()
    {
        return value;
    }

    public boolean getHttpOnly()
    {
        return httpOnly;
    }

    public String getComment()
    {
        return comment;
    }

    @Override
    public String toString()
    {
        return "HarCookie [expires = " + expires + ", name = " + name + ", secure = " + secure + ", domain = " + domain + ", path = " +
               path + ", value = " + value + ", httpOnly = " + httpOnly + ", comment = " + comment + "]";
    }

    public static class Builder
    {
        private String name;

        private String value;

        private String path;

        private String domain;

        private String expires;

        private boolean httpOnly;

        private boolean secure;

        private String comment;

        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withValue(String value)
        {
            this.value = value;
            return this;
        }

        public Builder withPath(String path)
        {
            this.path = path;
            return this;
        }

        public Builder withDomain(String domain)
        {
            this.domain = domain;
            return this;
        }

        public Builder withExpires(String expires)
        {
            this.expires = expires;
            return this;
        }

        public Builder withHttpOnly(boolean httpOnly)
        {
            this.httpOnly = httpOnly;
            return this;
        }

        public Builder withSecure(boolean secure)
        {
            this.secure = secure;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarCookie build()
        {
            return new HarCookie(name, value, path, domain, expires, httpOnly, secure, comment);
        }
    }
}
