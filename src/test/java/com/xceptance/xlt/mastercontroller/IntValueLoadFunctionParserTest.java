/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the implementation of {@link IntValueLoadFunctionParser}.
 */
public class IntValueLoadFunctionParserTest
{
    @Test
    public void testParseLoadFunction_separatorAndWhitespaceFormats_absoluteFunction() throws ParseException
    {
        final IntValueLoadFunctionParser parser = new IntValueLoadFunctionParser();
        final int[][] expected = new int[][]
            {
                {
                    LoadFunctionUtils.START_TIME, LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR
                },
                {
                    3600, 1
                },
                {
                    1800, 2
                },
                {
                    15, 3
                },
                {
                    10, 4
                }
            };

        // load function is parsed correctly with different styles of separating entries and additional whitespaces
        Assert.assertArrayEquals(expected, parser.parse(" 1h/1 30m/2,15/3;10s/4 "));
        Assert.assertArrayEquals(expected, parser.parse(" 1h//1   30m/2,,,15///3;;;10s///////4 "));
        Assert.assertArrayEquals(expected,
                                 parser.parse(" 1h  /  /  1     30m   /  2 ,  ; \t  ; , 15  /  / \t /  3  , ,  , ; 10s  /  //  /  4 \t "));
    }

    @Test
    public void testParseLoadFunction_separatorAndWhitespaceFormats_relativeFunction() throws ParseException
    {
        final IntValueLoadFunctionParser parser = new IntValueLoadFunctionParser();
        final int[][] expected = new int[][]
            {
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR)
                },
                {
                    (LoadFunctionUtils.START_TIME + 3600), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 1)
                },
                {
                    (LoadFunctionUtils.START_TIME + 3600 + 1800), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 1 + 2)
                },
                {
                    (LoadFunctionUtils.START_TIME + 3600 + 1800 + 15), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 1 + 2 + 3)
                },
                {
                    (LoadFunctionUtils.START_TIME + 3600 + 1800 + 15 + 10), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 1 + 2 + 3 - 4)
                }
            };

        // load function is parsed correctly with different styles of separating values and additional whitespaces
        Assert.assertArrayEquals(expected, parser.parse(" +1h/+1 +30m/+2,+15/+3;+10s/-4 "));
        Assert.assertArrayEquals(expected, parser.parse(" +1h/+1   +30m//+2,,,+15///+3;;;+10s///////-4 "));
        Assert.assertArrayEquals(expected,
                                 parser.parse(" +  1h  /  /  +  1     +  30m   /  +  2 ,  ; \t  ; , +  15  /  / \t /  +  3  , ,  , ; +  10s  /  //  /  -  4 \t "));
    }

    @Test
    public void testParseLoadFunction_timeFormats_absoluteFunction() throws ParseException
    {
        final IntValueLoadFunctionParser parser = new IntValueLoadFunctionParser();
        final int[][] expected = new int[][]
            {
                {
                    0, 0
                },
                {
                    3602, 1
                },
                {
                    108945, 2
                }
            };

        // different time formats in load function are parsed correctly
        Assert.assertArrayEquals(expected, parser.parse("0/0 3602/1 108945/2"));
        Assert.assertArrayEquals(expected, parser.parse("  0  /  0  3602  /  1  108945  /  2  "));
        Assert.assertArrayEquals(expected, parser.parse("  0  /  //  0  3602  /\t  /  1  108945  /  /  /  /  2  "));

        Assert.assertArrayEquals(expected, parser.parse("0s/0 3602s/1 108945s/2"));
        Assert.assertArrayEquals(expected, parser.parse("  0  s  /  0  3602  s  /  1  108945  s  /  2  "));
        Assert.assertArrayEquals(expected, parser.parse("  0  s  /  //  0  3602  s  /\t  /  1  108945  s  /  /  /  /  2  "));

        Assert.assertArrayEquals(expected, parser.parse("0h0m0s/0 1h2s/1 30h15m45s/2"));
        Assert.assertArrayEquals(expected, parser.parse("  0h  0  s  /  0  60  m  2  s  / 1 27  h  135  m  3645  s  /  2  "));
        Assert.assertArrayEquals(expected,
                                 parser.parse("  0h  0  s  /  //  0  60  m  2  s  /\t  / 1 27  h  135  m  3645  s  /  /  /  /  2  "));

        Assert.assertArrayEquals(expected, parser.parse("0:0:0/0 01:00:2/1  30:15:45/2"));
        Assert.assertArrayEquals(expected, parser.parse("  0:0:0  /  0  01:00:2  /  1  30:15:45  /  2"));
        Assert.assertArrayEquals(expected, parser.parse("  0:0:0  /  //  0  01:00:2  /\t  /  1  30:15:45  /  /  /  /  2"));
    }

    @Test
    public void testParseLoadFunction_timeFormats_relativeFunction() throws ParseException
    {
        final IntValueLoadFunctionParser parser = new IntValueLoadFunctionParser();
        final int[][] expected = new int[][]
            {
                {
                    (0), (0)
                },
                {
                    (0 + 3602), (0 + 1)
                },
                {
                    (0 + 3602 - 108945), (0 + 1 + 2)
                }
            };

        // different time formats in load function are parsed correctly
        Assert.assertArrayEquals(expected, parser.parse("0/0 +3602/+1 -108945/+2"));
        Assert.assertArrayEquals(expected, parser.parse("  0   /  0  +  3602  /  +  1  -  108945  /  +  2  "));
        Assert.assertArrayEquals(expected, parser.parse("  0   /  //  0  +  3602  /\t  /  +  1  -  108945  /  /  /  /  +  2  "));

        Assert.assertArrayEquals(expected, parser.parse("0s/0 +3602s/+1 -108945s/+2"));
        Assert.assertArrayEquals(expected, parser.parse("  0  s  /  0  +  3602  s  /  +  1  -  108945  s  /  +  2  "));
        Assert.assertArrayEquals(expected, parser.parse("  0  s  /  //  0  +  3602  s  /\t  /  +  1  -  108945  s  /  /  /  /  +  2  "));

        Assert.assertArrayEquals(expected, parser.parse("0h0m0s/0 +1h2s/+1 -30h15m45s/+2"));
        Assert.assertArrayEquals(expected,
                                 parser.parse("  0  h  0  s  /  0  +  60  m  2  s  / +  1  -  27  h  135  m  3645  s  /  +  2  "));
        Assert.assertArrayEquals(expected,
                                 parser.parse("  0  h  0  s  /  //  0  +  60  m  2  s  /\t  / +  1  -  27  h  135  m  3645  s  /  /  /  /  +  2  "));

        Assert.assertArrayEquals(expected, parser.parse("0:0:0/0 +01:00:2/+1  -30:15:45/+2"));
        Assert.assertArrayEquals(expected, parser.parse("  0:0:0  /  0  +  01:00:2  /  +  1  -  30:15:45  /  +  2  "));
        Assert.assertArrayEquals(expected, parser.parse("  0:0:0  /  //  0  +  01:00:2  /\t  /  +  1  -  30:15:45  /  /  /  /  +  2  "));
    }

    @Test
    public void testParseLoadFunction_mixRelativeAndAbsoluteValues() throws ParseException
    {
        final String loadFunctionProperty = "0/0 15m/+2 +1h/5 +1s/-2 2h/+1 +30m/1 3h/6";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    (0), (0)
                },
                {
                    (900), (0 + 2)
                },
                {
                    (900 + 3600), (5)
                },
                {
                    (900 + 3600 + 1), (5 - 2)
                },
                {
                    (7200), (5 - 2 + 1)
                },
                {
                    (7200 + 1800), (1)
                },
                {
                    (10800), (6)
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_firstPairIsValidStartingPoint() throws ParseException
    {
        // first entry is absolute and has time LoadFunctionUtils.START_TIME which is a valid starting point, so we
        // don't insert the default starting point
        final String loadFunctionProperty = LoadFunctionUtils.START_TIME + "/0 +15m/+1";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    (LoadFunctionUtils.START_TIME), (0)
                },
                {
                    (LoadFunctionUtils.START_TIME + 900), (0 + 1)
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_firstPairHasRelativeTime() throws ParseException
    {
        // first pair has relative time, so the default starting point is added at the start of the function
        final String loadFunctionProperty = "+15m/2 16m/4";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR)
                },
                {
                    (LoadFunctionUtils.START_TIME + 900), (2)
                },
                {
                    (960), (4)
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_firstPairHasRelativeValue() throws ParseException
    {
        // first pair has relative value, so the default starting point is added at the start of the function
        final String loadFunctionProperty = "0/+2 16m/4";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR)
                },
                {
                    (0), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 2)
                },
                {
                    (960), (4)
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_firstPairHasRelativeTimeAndLoadFactor() throws ParseException
    {
        // first pair has relative time and value, so the default starting point is added at the start of the function
        final String loadFunctionProperty = "+15m/+2 16m/4";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR)
                },
                {
                    (LoadFunctionUtils.START_TIME + 900), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 2)
                },
                {
                    (960), (4)
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_firstPairHasRelativeTimeZero() throws ParseException
    {
        // first pair has relative time "+0", in which case the default starting point is still added at the start of
        // the function
        final String loadFunctionProperty = "+0/2 16m/4";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR)
                },
                {
                    (LoadFunctionUtils.START_TIME + 0), (2)
                },
                {
                    (960), (4)
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_oneAbsolutePair_isValidStartingPoint() throws ParseException
    {
        // resulting function contains only the one pair
        final String loadFunctionProperty = " " + LoadFunctionUtils.START_TIME + "/15 ";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    LoadFunctionUtils.START_TIME, 15
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_oneAbsolutePair_isNotValidStartingPoint() throws ParseException
    {
        // resulting function contains the default starting point followed by the one pair
        final String loadFunctionProperty = " 1/15 ";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    LoadFunctionUtils.START_TIME, LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR
                },
                {
                    1, 15
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_oneRelativePair_timeIsZero() throws ParseException
    {
        // default starting point is added
        final String loadFunctionProperty = " +0/+15 ";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR)
                },
                {
                    (LoadFunctionUtils.START_TIME + 0), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 15)
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_oneRelativePair_timeIsNotZero() throws ParseException
    {
        // default starting point is added
        final String loadFunctionProperty = " +1/+15 ";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR)
                },
                {
                    (LoadFunctionUtils.START_TIME + 1), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 15)
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_duplicateTimeValues_absoluteFunction() throws ParseException
    {
        final String loadFunctionProperty = "0/0 0/2 0/1 1h/1 1h/1 1h/4";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    0, 0
                },
                {
                    0, 2
                },
                {
                    0, 1
                },
                {
                    3600, 1
                },
                {
                    3600, 1
                },
                {
                    3600, 4
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test
    public void testParseLoadFunction_duplicateTimeValues_relativeFunction() throws ParseException
    {
        final String loadFunctionProperty = "+0/+0 +0/+2 +0/+2 +1m/+0 +1m/-0 +0m/+0 +0/+3";
        final int[][] function = new IntValueLoadFunctionParser().parse(loadFunctionProperty);
        final int[][] expected = new int[][]
            {
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR)
                },
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR)
                },
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 2)
                },
                {
                    (LoadFunctionUtils.START_TIME), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 2 + 2)
                },
                {
                    (LoadFunctionUtils.START_TIME + 60), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 2 + 2)
                },
                {
                    (LoadFunctionUtils.START_TIME + 60 + 60), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 2 + 2)
                },
                {
                    (LoadFunctionUtils.START_TIME + 60 + 60), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 2 + 2)
                },
                {
                    (LoadFunctionUtils.START_TIME + 60 + 60), (LoadFunctionUtils.DEFAULT_INITIAL_LOAD_FACTOR + 2 + 2 + 3)
                }
            };
        Assert.assertArrayEquals(expected, function);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunction_incomplete_missingSeparator() throws ParseException
    {
        final String loadFunctionProperty = " 1h ";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunction_incomplete_missingLoadFactor() throws ParseException
    {
        final String loadFunctionProperty = " 1h / ";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunction_incomplete_missingTimeAndLoadFactor() throws ParseException
    {
        final String loadFunctionProperty = " 1h / 3;/";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunction_incomplete_missingTime() throws ParseException
    {
        final String loadFunctionProperty = " 1h / 3;/1";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLoadFunction_incomplete_noPairs() throws ParseException
    {
        final String loadFunctionProperty = ",,,,,";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseLoadFunction_invalid_emptyFunction() throws ParseException
    {
        final String loadFunctionProperty = "";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunction_invalid_invalidTimeFormat_01() throws ParseException
    {
        final String loadFunctionProperty = "0/0 2x/1";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunction_invalid_invalidTimeFormat_02() throws ParseException
    {
        final String loadFunctionProperty = "0/0 1:60:59/1";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunction_invalid_invalidValue_01() throws ParseException
    {
        // int parser doesn't support double values
        final String loadFunctionProperty = "0/0 2h/1.2";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }

    @Test(expected = ParseException.class)
    public void testParseLoadFunction_invalid_invalidValue_02() throws ParseException
    {
        // int parser can't parse values greater than Integer.MAX_VALUE
        final String loadFunctionProperty = "0/0 2h/999999999999";
        new IntValueLoadFunctionParser().parse(loadFunctionProperty);
    }
}
