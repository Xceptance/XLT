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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The top log object according the HAR specification.
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#log">specification</a>
 */
@JsonPropertyOrder(
    {
        "version", "creator", "browser", "pages", "entries", "comment"
    })
public class HarLog
{
    private final String version;

    private final HarCreator creator;

    private final HarBrowser browser;

    private final List<HarPage> pages;

    private final List<HarEntry> entries;

    private final String comment;

    @JsonCreator
    public HarLog(@JsonProperty("version") String version, @JsonProperty("creator") HarCreator creator,
                  @JsonProperty("browser") HarBrowser browser, @JsonProperty("pages") List<HarPage> pages,
                  @JsonProperty("entries") List<HarEntry> entries, @JsonProperty("comment") String comment)
    {
        this.version = version;
        this.creator = creator;
        this.browser = browser;
        this.pages = pages;
        this.entries = entries;
        this.comment = comment;
    }

    public String getVersion()
    {
        return version;
    }

    public HarCreator getCreator()
    {
        return creator;
    }

    public HarBrowser getBrowser()
    {
        return browser;
    }

    public List<HarPage> getPages()
    {
        return pages;
    }

    public List<HarEntry> getEntries()
    {
        return entries;
    }

    public String getComment()
    {
        return comment;
    }

    @Override
    public String toString()
    {
        return "HarLog [pages = " + pages + ", browser = " + browser + ", entries = " + entries + ", comment = " + comment +
               ", creator = " + creator + ", version = " + version + "]";
    }

    public static class Builder
    {
        private String version = "1.2";

        private HarCreator creator;

        private HarBrowser browser;

        private List<HarPage> pages;

        private List<HarEntry> entries = new ArrayList<>();

        private String comment;

        public Builder withVersion(String version)
        {
            this.version = version;
            return this;
        }

        public Builder withCreator(HarCreator creator)
        {
            this.creator = creator;
            return this;
        }

        public Builder withBrowser(HarBrowser browser)
        {
            this.browser = browser;
            return this;
        }

        public Builder withPages(List<HarPage> pages)
        {
            this.pages = pages;
            return this;
        }

        public Builder withEntries(List<HarEntry> entries)
        {
            this.entries = entries;
            return this;
        }

        public Builder addEntry(HarEntry entry)
        {
            entries.add(entry);
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarLog build()
        {
            return new HarLog(version, creator, browser, pages, entries, comment);
        }
    }
}
