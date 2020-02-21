package com.xceptance.xlt.engine.resultbrowser;

import java.util.ArrayList;
import java.util.List;

/**
 * Container that holds all information about an action necessary to be processed by the results browser.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class ActionInfo
{
    public String fileName;

    public String name;

    public final List<RequestInfo> requests = new ArrayList<RequestInfo>();

    /**
     * Page load event timings (used for HAR export)
     */
    public transient final List<PageLoadEventInfo> events = new ArrayList<>();

    public static class PageLoadEventInfo
    {
        public final String name;

        public final long startTime;

        public final long duration;

        public PageLoadEventInfo(final String aName, final long aStartTime, final long aDuration)
        {
            name = aName;
            startTime = aStartTime;
            duration = aDuration;
        }

    }
}
