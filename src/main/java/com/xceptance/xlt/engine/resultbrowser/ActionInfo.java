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
