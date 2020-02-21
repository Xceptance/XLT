package com.xceptance.xlt.report;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ReportGeneratorMain_TimeParsingFailsTest
{
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data()
    {
        /**
         * description
         * args
         */
        return Arrays.asList(new Object[][]
            {
                {
                    "'from' offset without positive-negative marker", new String[] {"-from", "1s"}
                },
                {
                    "'to' offset without positive-negative marker", new String[] {"-to", "1s"}
                },
                {
                    "'from' missing time unit", new String[] {"-from", "+1"}
                },
                {
                    "'to' missing time unit", new String[] {"-to", "+1"}
                },
                {
                    "'from' invalid value", new String[] {"-from", "a"}
                },
                {
                    "'to' invalid value", new String[] {"-to", "a"}
                },
                {
                    "'from' empty", new String[] {"-from", ""}
                },
                {
                    "'to' empty", new String[] {"-to", ""}
                },
                {
                    "'from' whitespace", new String[] {"-from", " "}
                },
                {
                    "'to' whitespace", new String[] {"-to", " "}
                },
                {
                    "'from' missing value", new String[] {"-from"}
                },
                {
                    "'to' missing value", new String[] {"-to"}
                },
                {
                    "duration invalid", new String[] {"-from", "+1s", "-l", "a"}
                },
                {
                    "duration empty", new String[] {"-from", "+1s", "-l", ""}
                },
                {
                    "duration whitespace", new String[] {"-from", "+1s", "-l", " "}
                },
                {
                    "duration missing", new String[] {"-from", "+1s", "-l"}
                },
                {
                    "duration parameter lacks 'from' or 'to'", new String[] {"-l", "10s"}
                },
                {
                    "either 'from' or 'to' are allowed, one is mandatory", new String[] {"-from", "+1s", "-to", "+3600s", "-l", "10s"}
                },
                {
                    "duration value is negative but must not be", new String[] {"-from", "+1s", "-l", "-10s"}
                }
            });
    }
    
    @Parameter(value = 0)
    public String description;
    
    @Parameter(value = 1)
    public String[] args;
    
    /**
     * Invokes {@link ReportGeneratorMain} with the given arguments.
     * 
     * @throws Exception
     *             as an indirect indicator for the invalid command line value
     */
    @Test(expected = Exception.class)
    public void check() throws Exception
    {
        // try to start with given arguments
        ReportGeneratorMainFactory.create(args);
    }
}
