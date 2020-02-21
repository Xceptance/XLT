package com.xceptance.xlt.agentcontroller;

/**
 * The {@link TestResultAmount} specifies the amount of test result data to be downloaded.
 */
public enum TestResultAmount
{
    ALL("Measurements, result browser data, and logs [all]", "1"),
    MEASUREMENTS_AND_RESULTBROWSER("Measurements and result browser data", "2"),
    MEASUREMENTS_ONLY("Measurements only", "3"),
    CANCEL("Cancel", "c");

    private static final String[] displayNames;

    private static final String[] shortcuts;

    static
    {
        // build the list of display names
        final TestResultAmount[] values = values();
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

    private TestResultAmount(final String displayName, final String shortcut)
    {
        this.displayName = displayName;
        this.shortcut = shortcut;
    }
}
