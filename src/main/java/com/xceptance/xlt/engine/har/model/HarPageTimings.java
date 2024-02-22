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
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(
    {
        "onContentLoad", "onLoad", "comment"
    })
public class HarPageTimings
{
    private final Long onContentLoad;

    private final Long onLoad;

    private final String comment;

    private final Long firstPaint;

    private final Long firstContentfulPaint;

    @JsonCreator
    public HarPageTimings(@JsonProperty("onContentLoad") Long onContentLoad, @JsonProperty("onLoad") Long onLoad,
                          @JsonProperty("comment") String comment, @JsonProperty("_firstPaint") final Long firstPaint,
                          @JsonProperty("_firstContentfulPaint") final Long firstContentfulPaint)
    {
        this.onContentLoad = onContentLoad;
        this.onLoad = onLoad;
        this.comment = comment;
        this.firstPaint = firstPaint;
        this.firstContentfulPaint = firstContentfulPaint;
    }

    public Long getOnContentLoad()
    {
        return onContentLoad;
    }

    public Long getOnLoad()
    {
        return onLoad;
    }

    public String getComment()
    {
        return comment;
    }

    @JsonGetter("_firstPaint")
    public Long getFirstPaint()
    {
        return firstPaint;
    }

    @JsonGetter("_firstContentfulPaint")
    public Long getFirstContentfulPaint()
    {
        return firstContentfulPaint;
    }

    @Override
    public String toString()
    {
        return "HarPageTimings [onLoad = " + onLoad + ", onContentLoad = " + onContentLoad + ", comment = " + comment + ", firstPaint = " +
               firstPaint + ", firstContentfulPaint = " + firstContentfulPaint + "]";
    }

    public static class Builder
    {
        private Long onContentLoad;

        private Long onLoad;

        private String comment;

        private Long firstPaint;

        private Long firstContentfulPaint;

        public Builder withOnContentLoad(Long onContentLoad)
        {
            this.onContentLoad = onContentLoad;
            return this;
        }

        public Builder withOnLoad(Long onLoad)
        {
            this.onLoad = onLoad;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public Builder withFirstPaint(Long firstPaint)
        {
            this.firstPaint = firstPaint;
            return this;
        }

        public Builder withFirstContentfulPaint(Long firstContentfulPaint)
        {
            this.firstContentfulPaint = firstContentfulPaint;
            return this;
        }

        public HarPageTimings build()
        {
            return new HarPageTimings(onContentLoad, onLoad, comment, firstPaint, firstContentfulPaint);
        }

    }
}
