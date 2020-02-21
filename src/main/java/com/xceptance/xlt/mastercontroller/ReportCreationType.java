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
