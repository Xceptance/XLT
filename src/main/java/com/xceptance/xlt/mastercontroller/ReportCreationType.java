/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.mastercontroller;

/**
 * The {@link ReportCreationType} specifies the time range to create the report of.
 */
public enum ReportCreationType
{
    ALL("Yes", "y"), NO_RAMPUP("No", "n"), ABORT("Cancel", "c");

    private static final String[] displayNames;

    private static final String[] shortcuts;

    static
    {
        // build the list of display names and shortcuts
        final ReportCreationType[] values = values();
        displayNames = new String[values.length];
        shortcuts = new String[values.length];

        for (int i = 0; i < values.length; i++)
        {
            displayNames[i] = values[i].displayName;
            shortcuts[i] = values[i].shortcut;
        }
    }

    public static String[] displayNames()
    {
        return displayNames;
    }

    public static String[] shortcuts()
    {
        return shortcuts;
    }

    private final String displayName;

    private final String shortcut;

    private ReportCreationType(final String displayName, final String shortcut)
    {
        this.displayName = displayName;
        this.shortcut = shortcut;
    }
}
