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
 * This object describes posted data, if any (embedded in request object).
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/#postData">specification</a>
 */
@JsonPropertyOrder(
    {
        "mimeType", "params", "text", "comment"
    })
public class HarPostData
{

    private final String mimeType;

    private final List<HarParam> params;

    private final String text;

    private final String comment;

    @JsonCreator
    public HarPostData(@JsonProperty("mimeType") String mimeType, @JsonProperty("params") List<HarParam> params,
                       @JsonProperty("text") String text, @JsonProperty("comment") String comment)
    {
        this.mimeType = mimeType;
        this.params = params;
        this.text = text;
        this.comment = comment;
    }

    public String getText()
    {
        return text;
    }

    public List<HarParam> getParams()
    {
        return params;
    }

    public String getComment()
    {
        return comment;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    @Override
    public String toString()
    {
        return "HarPostData [text = " + text + ", params = " + params + ", comment = " + comment + ", mimeType = " + mimeType + "]";
    }

    public static class Builder
    {
        private String mimeType;

        private List<HarParam> params;

        private String text;

        private String comment;

        public Builder withMimeType(String mimeType)
        {
            this.mimeType = mimeType;
            return this;
        }

        public Builder withParams(List<HarParam> params)
        {
            if (params != null)
            {
                // post-body is either text or form-data
                this.text = null;
            }
            this.params = params;
            return this;
        }

        public Builder addParam(HarParam param)
        {
            if (this.params == null)
            {
                this.params = new ArrayList<>();
            }
            this.params.add(param);
            // post-body is either text or form-data
            this.text = null;
            return this;
        }

        public Builder withText(String text)
        {
            if (text != null)
            {
                // post-body is either text or form-data
                this.params = null;
            }
            this.text = text;
            return this;
        }

        public Builder withComment(String comment)
        {
            this.comment = comment;
            return this;
        }

        public HarPostData build()
        {
            return new HarPostData(mimeType, params, text, comment);
        }
    }
}
