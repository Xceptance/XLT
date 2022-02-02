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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(
    {
        "name", "version", "comment"
    })
public class HarBrowser
{
    private final String name;

    private final String version;

    private final String comment;

    @JsonCreator
    public HarBrowser(@JsonProperty("name") String name, @JsonProperty("version") String version, @JsonProperty("comment") String comment)
    {
        this.name = name;
        this.version = version;
        this.comment = comment;
    }

    public String getName()
    {
        return name;
    }

    public String getComment()
    {
        return comment;
    }

    public String getVersion()
    {
        return version;
    }

    @Override
    public String toString()
    {
        return "HarBrowser [name = " + name + ", comment = " + comment + ", version = " + version + "]";
    }

    public static class Builder
    {
        private String name;

        private String version;

        private String comment;

        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        public Builder withVersion(String version)
        {
            this.version = version;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarBrowser build()
        {
            return new HarBrowser(name, version, comment);
        }

    }
}
