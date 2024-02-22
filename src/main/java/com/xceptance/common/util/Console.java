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
package com.xceptance.common.util;

/**
 * Just a simple class to help with the output to the console and at the same time to the logs. It can also do some
 * pretty printing of needed and do empty lines and such which is not what a log needs.
 * <p>
 * This could be a real terminal or console with colors maybe or things like that... but for now, it is way too much.
 * 
 * @author rschwietzke
 */
public class Console
{
    private static final int LENGTH = 80;

    public static String startSection(final String topic)
    {
        return topic;
    }

    private static String bar(final boolean nlAfter)
    {
        final StringBuilder s = new StringBuilder();

        for (int i = 0; i < LENGTH; i++)
        {
            s.append("-");
        }

        return nlAfter ? s.append("\n").toString() : s.toString();
    }

    public static String horizontalBar()
    {
        return bar(false);
    }

    public static String endSection()
    {
        return bar(true);
    }
}
