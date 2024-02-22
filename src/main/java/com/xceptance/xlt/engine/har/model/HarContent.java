/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
 * Details about response content (embedded in response object).
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#content">specification</a>
 */
@JsonPropertyOrder(
    {
        "size", "compression", "mimeType", "text", "comment"
    })
public class HarContent
{
    private final long size;

    private final Long compression;

    private final String mimeType;

    private final String text;

    private final String comment;

    @JsonCreator
    public HarContent(@JsonProperty("size") Long size, @JsonProperty("compression") Long compression,
                      @JsonProperty("mimeType") String mimeType, @JsonProperty("text") String text, @JsonProperty("comment") String comment)
    {
        this.size = size;
        this.compression = compression;
        this.mimeType = mimeType;
        this.text = text;
        this.comment = comment;
    }

    public String getText()
    {
        return text;
    }

    public String getComment()
    {
        return comment;
    }

    public Long getCompression()
    {
        return compression;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public long getSize()
    {
        return size;
    }

    @Override
    public String toString()
    {
        return "HarContent [text = " + text + ", comment = " + comment + ", compression = " + compression + ", mimeType = " + mimeType +
               ", size = " + size + "]";
    }

    public static class Builder
    {
        private long size = -1L;

        private Long compression;

        private String mimeType;

        private String text;

        private String comment;

        public Builder withSize(long size)
        {
            this.size = size;
            return this;
        }

        public Builder withCompression(Long compression)
        {
            this.compression = compression;
            return this;
        }

        public Builder withMimeType(String mimeType)
        {
            this.mimeType = mimeType;
            return this;
        }

        public Builder withText(String text)
        {
            this.text = text;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarContent build()
        {
            return new HarContent(size, compression, mimeType, text, comment);
        }
    }
}
