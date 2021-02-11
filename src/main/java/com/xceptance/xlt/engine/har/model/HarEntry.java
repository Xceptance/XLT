/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

/**
 * Container for all information about an exported request (and its response if any).
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#entries">specification</a>
 */
@JsonPropertyOrder(
    {
        "pageref", "startedDateTime", "time", "request", "response", "cache", "timings", "serverIPAddress", "connection", "comment"
    })
public class HarEntry
{
    private final String pageref;

    private final String startedDateTime;

    private final long time;

    private final HarRequest request;

    private HarResponse response;

    private final HarCache cache;

    private final HarTimings timings;

    private final String serverIPAddress;

    private final String connection;

    private final String comment;

    @JsonCreator
    public HarEntry(@JsonProperty("pageref") String pageref, @JsonProperty("startedDateTime") String startedDateTime,
                    @JsonProperty("time") long time, @JsonProperty("request") HarRequest request,
                    @JsonProperty("response") HarResponse response, @JsonProperty("cache") HarCache cache,
                    @JsonProperty("timings") HarTimings timings, @JsonProperty("serverIPAddress") String serverIPAddress,
                    @JsonProperty("connection") String connection, @JsonProperty("comment") String comment)
    {
        this.pageref = pageref;
        this.startedDateTime = startedDateTime;
        this.time = time;
        this.request = request;
        this.response = response;
        this.cache = cache;
        this.timings = timings;
        this.serverIPAddress = serverIPAddress;
        this.connection = connection;
        this.comment = comment;
    }

    public String getPageref()
    {
        return pageref;
    }

    public String getStartedDateTime()
    {
        return startedDateTime;
    }

    public long getTime()
    {
        return time;
    }

    public HarRequest getRequest()
    {
        return request;
    }

    public HarResponse getResponse()
    {
        return response;
    }

    public HarCache getCache()
    {
        return cache;
    }

    public HarTimings getTimings()
    {
        return timings;
    }

    public String getServerIPAddress()
    {
        return serverIPAddress;
    }

    public String getConnection()
    {
        return connection;
    }

    public String getComment()
    {
        return comment;
    }

    @Override
    public String toString()
    {
        return "HarEntry [response = " + response + ", connection = " + connection + ", time = " + time + ", pageref = " + pageref +
               ", cache = " + cache + ", timings = " + timings + ", request = " + request + ", comment = " + comment +
               ", serverIPAddress = " + serverIPAddress + ", startedDateTime = " + startedDateTime + "]";
    }

    public static class Builder
    {
        private String pageref;

        private String startedDateTime;

        private long time;

        private HarRequest request;

        private HarResponse response;

        private HarCache cache = new HarCache(null, null, null);

        private HarTimings timings;

        private String serverIPAddress;

        private String connection;

        private String comment;

        public Builder withPageref(String pageref)
        {
            this.pageref = pageref;
            return this;
        }

        public Builder withStartedDateTime(String startedDateTime)
        {
            this.startedDateTime = startedDateTime;
            return this;
        }

        public Builder withStartedDateTime(Date startedDateTime)
        {
            this.startedDateTime = ISO8601Utils.format(startedDateTime, true);
            return this;
        }

        public Builder withTime(long time)
        {
            this.time = time;
            return this;
        }

        public Builder withRequest(HarRequest request)
        {
            this.request = request;
            return this;
        }

        public Builder withResponse(HarResponse response)
        {
            this.response = response;
            return this;
        }

        public Builder withCache(HarCache cache)
        {
            this.cache = cache;
            return this;
        }

        public Builder withTimings(HarTimings timings)
        {
            this.timings = timings;
            return this;
        }

        public Builder withServerIPAddress(String serverIPAddress)
        {
            this.serverIPAddress = serverIPAddress;
            return this;
        }

        public Builder withConnection(String connection)
        {
            this.connection = connection;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarEntry build()
        {
            return new HarEntry(pageref, startedDateTime, time, request, response, cache, timings, serverIPAddress, connection, comment);
        }
    }
}
