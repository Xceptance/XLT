package com.xceptance.xlt.report.scorecard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public List<String> getLogs()
    {
        return Collections.unmodifiableList(logs);
    }
}
