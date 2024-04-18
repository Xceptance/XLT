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

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.util.ISO8601Utils;

@JsonPropertyOrder(
    {
        "startedDateTime", "id", "title", "pageTimings", "comment"
    })
public class HarPage
{

    private final String startedDateTime;

    private final String id;

    private final String title;

    private final HarPageTimings pageTimings;

    private final String comment;

    @JsonCreator
    public HarPage(@JsonProperty("startedDateTime") String startedDateTime, @JsonProperty("id") String id,
                   @JsonProperty("title") String title, @JsonProperty("pageTimings") HarPageTimings pageTimings,
                   @JsonProperty("comment") String comment)
    {
        this.startedDateTime = startedDateTime;
        this.id = id;
        this.title = title;
        this.pageTimings = pageTimings;
        this.comment = comment;
    }

    public String getStartedDateTime()
    {
        return startedDateTime;
    }

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public HarPageTimings getPageTimings()
    {
        return pageTimings;
    }

    public String getComment()
    {
        return comment;
    }

    @Override
    public String toString()
    {
        return "HarPage [id = " + id + ", title = " + title + ", pageTimings = " + pageTimings + ", comment = " + comment +
               ", startedDateTime = " + startedDateTime + "]";
    }

    public static class Builder
    {
        private String startedDateTime;

        private String id;

        private String title;

        private HarPageTimings pageTimings;

        private String comment;

        public Builder withStartedDateTime(Date startedDateTime)
        {
            this.startedDateTime = ISO8601Utils.format(startedDateTime, true);
            return this;
        }

        public Builder withStartedDateTime(String startedDateTime)
        {
            this.startedDateTime = startedDateTime;
            return this;
        }

        public Builder withId(String id)
        {
            this.id = id;
            return this;
        }

        public Builder withTitle(String title)
        {
            this.title = title;
            return this;
        }

        public Builder withPageTimings(HarPageTimings pageTimings)
        {
            this.pageTimings = pageTimings;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarPage build()
        {
            return new HarPage(startedDateTime, id, title, pageTimings, comment);
        }
    }
}
