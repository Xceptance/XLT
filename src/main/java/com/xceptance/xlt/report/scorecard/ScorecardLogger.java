package com.xceptance.xlt.report.scorecard;

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
