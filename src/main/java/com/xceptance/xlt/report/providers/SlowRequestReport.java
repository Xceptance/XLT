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
package com.xceptance.xlt.report.providers;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents URL and runtime of a slow request.
 */
@XStreamAlias("request")
public class SlowRequestReport implements Comparable<SlowRequestReport>
{
    /**
     * The request's URL.
     */
    public String url;

    /**
     * The request's runtime.
     */
    public long runtime;

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(SlowRequestReport o)
    {
        // first reverse-compare by runtime
        int result = Long.compare(o.runtime, runtime);

        if (result == 0)
        {
            // compare by URL
            result = (url == null) ? -1 : (o.url == null) ? 1 : url.compareTo(o.url);
        }

        return result;
    }
}
