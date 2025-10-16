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
package com.xceptance.xlt.engine.scripting.docgen;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.xceptance.common.util.RegExUtils;

/**
 * Tests the markdown-to-HTML conversion.
 */
@RunWith(Parameterized.class)
public class MarkedTest
{
    private final String source;

    private final String html;

    public MarkedTest(final String aSource, final String aHtml)
    {
        source = aSource;
        html = aHtml;
    }

    @Test
    public void testMe()
    {
        Assert.assertEquals(html, minimize(Marked.getInstance().markdownToHTML(source)));
    }

    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]
            {
                new String[]
                    {
                        "__Caution:__This is some text", "<p><strong>Caution:</strong>This is some text</p>"
                    },
                new String[]
                    {
                        "(_Caution:_This is some text)", "<p>(<em>Caution:</em>This is some text)</p>"
                    },
                new String[]
                    {
                        "A sample list\n\n- 1st item\n- 2nd item", "<p>A sample list</p><ul><li>1st item</li><li>2nd item</li></ul>"
                    },
                new String[]
                    {
                        "A sample list with asterisks\n\n* 1st item\n* 2nd item", "<p>A sample list with asterisks</p><ul><li>1st item</li><li>2nd item</li></ul>"
                    },
                new String[]
                    {
                        "Another list\n- 1st item\n- 2nd item", "<p>Another list</p><ul><li>1st item</li><li>2nd item</li></ul>"
                    },
                new String[]
                    {
                        "This is some `inline code with entities &, <, >` inside a text.",
                        "<p>This is some <code>inline code with entities &amp;, &lt;, &gt;</code> inside a text.</p>"
                    },
                new String[]
                    {
                        "Some text <div>with some inline <b>HTML code</b> that should be sanitized in output</div>.",
                        "<p>Some text &lt;div&gt;with some inline &lt;b&gt;HTML code&lt;/b&gt; that should be sanitized in output&lt;/div&gt;.</p>"
                    },
                new String[]
                    {
                        "| Left-Aligned  | Center Aligned  | Right Aligned |\n| :------------ |:---------------:| -----:|\n"
                            + "| col 3 is      | some wordy text | $1600 |\n| col 2 is      | centered        |   $12 |\n"
                            + "| zebra stripes | are neat        |    $1 |",
                        "<table><thead><tr><th align=\"left\">Left-Aligned </th><th align=\"center\">Center Aligned </th>"
                            + "<th align=\"right\">Right Aligned </th></tr></thead><tbody><tr><td align=\"left\">col 3 is </td>"
                            + "<td align=\"center\">some wordy text </td><td align=\"right\">$1600 </td></tr>"
                            + "<tr><td align=\"left\">col 2 is </td><td align=\"center\">centered </td><td align=\"right\">$12 </td>"
                            + "</tr><tr><td align=\"left\">zebra stripes </td><td align=\"center\">are neat </td>"
                            + "<td align=\"right\">$1 </td></tr></tbody></table>"

                    }
            });
    }

    private static String minimize(final String str)
    {
        if (StringUtils.isNotBlank(str))
        {
            return RegExUtils.replaceAll(str, ">\\s+<", "><");
        }
        return str;
    }
}
