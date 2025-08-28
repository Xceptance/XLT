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

import org.junit.Assert;
import org.junit.Test;

public class JUnitParamsUtilsTest
{
    @Test
    public void wrapParams_SingleParam()
    {
        final String param = " abc,123 ";
        Assert.assertArrayEquals(new Object[]
            {
                param
            }, JUnitParamsUtils.wrapParams(param));
    }

    @Test
    public void wrapParams_MultipleParams()
    {
        final String param1 = " abc,123 ";
        final String param2 = "xyz|";
        Assert.assertArrayEquals(new Object[]
            {
                param1, param2
            }, JUnitParamsUtils.wrapParams(param1, param2));
    }

    @Test
    public void wrapEachParam_SingleParam()
    {
        final Object[] params =
            {
                " abc,123 "
            };
        final Object[][] wrappedParams = JUnitParamsUtils.wrapEachParam(params);

        Assert.assertEquals(1, wrappedParams.length);
        Assert.assertEquals(1, wrappedParams[0].length);
        Assert.assertEquals(params[0], wrappedParams[0][0]);
    }

    @Test
    public void wrapEachParam_MultipleParams()
    {
        final Object[] params =
            {
                " abc,123 ", "xyz|"
            };
        final Object[][] wrappedParams = JUnitParamsUtils.wrapEachParam(params);

        Assert.assertEquals(2, wrappedParams.length);
        Assert.assertEquals(1, wrappedParams[0].length);
        Assert.assertEquals(1, wrappedParams[1].length);
        Assert.assertEquals(params[0], wrappedParams[0][0]);
        Assert.assertEquals(params[1], wrappedParams[1][0]);
    }

}
