/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package org.htmlunit;

import java.nio.charset.StandardCharsets;

import org.htmlunit.util.UrlUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.xceptance.common.lang.ReflectionUtils;

/**
 * @see https://lab.xceptance.de/issues/3054
 */
@RunWith(Parameterized.class)
public class _3054_EscapePercentSignTest
{

    private final String input;

    private final String output;

    public _3054_EscapePercentSignTest(final String aInput, final String aOutput)
    {
        input = aInput;
        output = aOutput;
    }

    @Parameters(name = "enc({0})={1}")
    public static Object[][] data()
    {
        return new Object[][]
        {
          {
            "foo%%20bar", "foo%25%20bar"
          },
          {
            "foo%20bar", "foo%20bar"
          },
          {
            "foo%ar", "foo%25ar"
          },
          {
            "foo%%xyz", "foo%25%25xyz"
          },
          {
            "foo%20%xyz", "foo%20%25xyz"
          },
          {
            "foo%2x%bar", "foo%252x%bar"
          }
        };
    }

    @Test
    public void testEncPrcnt() throws Throwable
    {
        final byte[] bytes = input.getBytes(StandardCharsets.US_ASCII);
        final String encodedString = ReflectionUtils.callStaticMethod(UrlUtils.class, "encodePercentSign", bytes);
        Assert.assertEquals(output, encodedString);
    }

}
