package com.xceptance.common.util;

/**
 * Some standard exit codes to use when quitting a program via {@link System#exit(int)}.
 */
public interface ProcessExitCodes
{
    /**
     * Success.
     */
    public static final int SUCCESS = 0;

    /**
     * General error.
     */
    public static final int GENERAL_ERROR = 1;

    /**
     * The parameters given on the command line are either unknown, incomplete, or not valid.
     */
    public static final int PARAMETER_ERROR = 2;
}
