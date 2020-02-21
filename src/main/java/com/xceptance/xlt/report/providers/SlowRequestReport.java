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
