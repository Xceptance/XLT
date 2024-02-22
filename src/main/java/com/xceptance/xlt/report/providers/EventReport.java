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

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.xceptance.common.collection.FastHashMap;

/**
 * Represents the statistics for a certain event in a test report.
 */
@XStreamAlias("event")
public class EventReport
{
    /**
     * The number how often a certain event has occurred.
     */
    public int totalCount = 0;

    /**
     * How many messages details have been counted but not stored.
     */
    public int droppedCount;

    /**
     * The name of the event.
     */
    public final String name;

    /**
     * The name of the test case that generated the event.
     */
    public String testCaseName;

    /**
     * The list of associated messages and their respective count keyed by message.
     */
    public transient FastHashMap<String, EventMessageInfo> messageMap = new FastHashMap<>(23, 0.5f);

    /**
     * The later list of data points, will be populated before we need it.
     */
    public List<EventMessageInfo> messages;

    /**
     * Constructor.
     */
    public EventReport(final String testCaseName, final String name)
    {
        this.testCaseName = testCaseName;
        this.name = name;
    }

    /**
     * Add a new message, drop it when the size is too large
     *
     * @param message
     *            the event message
     * @param limit
     *            limit the number of messages collected
     */
    public void addMessage(final String message, final int limit)
    {
        totalCount++;

        var info = messageMap.get(message);
        if (info == null)
        {
            if (messageMap.size() >= limit)
            {
                droppedCount++;
                return;
            }

            info = new EventMessageInfo();
            info.info = message;

            messageMap.put(message, info);
        }

        info.count++;
    }

    /**
     * Transform the data for serialization.
     */
    public void prepareSerialization()
    {
        messages = messageMap.values();

        // drop to save memory
        messageMap = null;
    }

    /**
     * Set the test case name if needed.
     *
     * @param name
     *            new name of the test case, mainly needed to reset the name
     */
    public void setTestCaseName(final String testCaseName)
    {
        this.testCaseName = testCaseName;
    }
}
