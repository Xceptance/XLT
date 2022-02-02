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
package com.xceptance.xlt.engine.har.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(
    {
        "expires", "lastAccess", "eTag", "hitCount", "comment"
    })
public class HarCacheRequest
{
    private final Date expires;

    private final Date lastAccess;

    private final String eTag;

    private final long hitCount;

    private final String comment;

    @JsonCreator
    public HarCacheRequest(@JsonProperty("expires") Date expires, @JsonProperty("lastAccess") Date lastAccess,
                           @JsonProperty("eTag") String eTag, @JsonProperty("hitCount") long hitCount,
                           @JsonProperty("comment") String comment)
    {
        this.expires = expires;
        this.lastAccess = lastAccess;
        this.eTag = eTag;
        this.hitCount = hitCount;
        this.comment = comment;
    }

    public String getETag()
    {
        return eTag;
    }

    public Date getExpires()
    {
        return expires;
    }

    public long getHitCount()
    {
        return hitCount;
    }

    public Date getLastAccess()
    {
        return lastAccess;
    }

    public String getComment()
    {
        return comment;
    }

    @Override
    public String toString()
    {
        return "HarCacheRequest [eTag = " + eTag + ", expires = " + expires + ", hitCount = " + hitCount + ", lastAccess = " + lastAccess +
               ", comment = " + comment + "]";
    }

    public static class Builder
    {
        private Date expires;

        private Date lastAccess;

        private String eTag = "";

        private long hitCount;

        private String comment;

        public Builder withExpires(Date expires)
        {
            this.expires = expires;
            return this;
        }

        public Builder withLastAccess(Date lastAccess)
        {
            this.lastAccess = lastAccess;
            return this;
        }

        public Builder withEtag(String eTag)
        {
            this.eTag = eTag;
            return this;
        }

        public Builder withHitCount(long hitCount)
        {
            this.hitCount = hitCount;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarCacheRequest build()
        {
            return new HarCacheRequest(expires, lastAccess, eTag, hitCount, comment);
        }

    }
}
