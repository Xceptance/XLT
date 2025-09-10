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
package util;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

/**
 * Util class to assist in running tests with JUnitParams. Contains helper methods and common test parameter providers.
 */
public class JUnitParamsUtils
{
    /**
     * Test parameter provider class for different blank String parameters.
     */
    public static class BlankStringParamProvider
    {
        @SuppressWarnings("unused")
        public static Object[] provideBlankStringParams()
        {
            return wrapEachParam(new Object[]
                {
                    "", //
                    " ", //
                    " \t " //
                });
        }
    }

    /**
     * Test parameter provider class for different blank String parameters as well as "null". Extends
     * {@link BlankStringParamProvider} and returns the blank Strings provided by that class.
     */
    public static class BlankStringOrNullParamProvider extends BlankStringParamProvider
    {
        @SuppressWarnings("unused")
        public static Object[] provideNullParam()
        {
            return new Object[]
                {
                    null
                };
        }
    }

    /**
     * Helper method to wrap the given parameters into an Object array.
     * <p>
     * If String parameters aren't provided in an Object array, JUnitParams will typically trim leading and trailing
     * whitespaces, and interpret commas or pipes as parameter separators (e.g. " abc,123 " will be read as two
     * parameters, "abc" and "123"). Wrapping the parameter in an Object array avoids this behavior and will use the
     * String parameter exactly as provided.
     * </p>
     * <p>
     * Usage examples:
     * </p>
     * <ul>
     * <li><code>new Object[] { wrapParams(" abc,123 "), wrapParams("xyz|"), wrapParams(" ") }</code></li>
     * <li><code>new Object[] { wrapParams(" input1 ", "expected1"), wrapParams(" input2 ", "expected2") }</code></li>
     * </ul>
     *
     * @param params
     *            the parameters to collect in an Object array
     * @return an Object array containing the given parameters
     */
    public static Object[] wrapParams(final Object... params)
    {
        return params;
    }

    /**
     * Helper method to go through the given Object array and wrap each parameter in an individual Object array.
     * <p>
     * If String parameters aren't provided in an Object array, JUnitParams will typically trim leading and trailing
     * whitespaces, and interpret commas or pipes as parameter separators (e.g. " abc,123 " will be read as two
     * parameters, "abc" and "123"). Wrapping the parameter in an Object array avoids this behavior and will use the
     * String parameter exactly as provided.
     * </p>
     * <p>
     * Usage example:
     * </p>
     * <p>
     * <code>wrapEachParam(new Object[] { " abc,123 ", "xyz|", " " })</code>
     * </p>
     *
     * @param params
     *            the array of parameters
     * @return an Object array containing one Object array for each parameter; each of the inner Object arrays contains
     *         a single parameter
     */
    public static Object[][] wrapEachParam(final Object[] params)
    {
        return Arrays.stream(params).map(JUnitParamsUtils::wrapParams).toArray(Object[][]::new);
    }

    /**
     * Parses the given parameter set strings into individual parameter sets and returns them as an array of Object
     * arrays. In contrast to JUnitParams, this method does NOT trim parameter values. For example:
     * <p>
     * <code>convertParamSets("foo|bar", " baz , bum")</code>
     * <p>
     * will return:
     * <p>
     * <code>new Object[]{new Object[]{ "foo", "bar"}, new Object[]{" baz ", " bum"}}</code>
     *
     * @param paramSets
     *            the parameter sets, with the parameters separated by '|' or ',' in each set
     * @return an Object array containing one Object array for each parameter set
     */
    public static Object[][] parseParamSets(final String... paramSets)
    {
        return Arrays.stream(paramSets).map(JUnitParamsUtils::parseParamSet).toArray(Object[][]::new);
    }

    /**
     * Parses the given parameter set string into individual parameters and returns them as an Object array. In contrast
     * to JUnitParams, this method does NOT trim parameter values.
     *
     * @param paramSet
     *            the parameter set, with the parameters separated by '|' or ','
     * @return an Object array containing the parameters
     */
    static Object[] parseParamSet(final String paramSet)
    {
        return StringUtils.splitPreserveAllTokens(paramSet, "|,");
    }
}
