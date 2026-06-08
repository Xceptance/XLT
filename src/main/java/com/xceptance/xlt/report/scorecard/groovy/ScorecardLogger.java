/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.scorecard.groovy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Simple logger to be used within Groovy scorecard scripts.
 */
public class ScorecardLogger
{
    private final List<String> logs = new ArrayList<>();

    public void info(final String message)
    {
        logs.add("[INFO] " + message);
    }

    public void warn(final String message)
    {
        logs.add("[WARN] " + message);
    }

    public void error(final String message)
    {
        logs.add("[ERROR] " + message);
    }

    public void error(final Throwable t)
    {
        error(null, t);
    }

    public void error(final String message, final Throwable t)
    {
        final StringBuilder sb = new StringBuilder("[ERROR] ");
        if (message != null)
        {
            sb.append(message).append("\n");
        }
        sb.append(ExceptionUtils.getStackTrace(t));
        logs.add(sb.toString().trim());
    }

    public List<String> getLogs()
    {
        return Collections.unmodifiableList(logs);
    }
}
