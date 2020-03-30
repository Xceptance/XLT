/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableMap;

/**
 * Tests the module parameter resolution capability of {@link CommandScript}.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
@RunWith(Parameterized.class)
public class CommandScriptTest
{
    private CommandScript script;

    private Method m;

    @Parameter(0)
    public String description;

    @Parameter(1)
    public String nameOrValue;

    @Parameter(2)
    public Map<String, String> args;

    @Parameter(3)
    public String expected;

    @Parameters(name = "{index}:{0}")
    public static Iterable<Object[]> data()
    {
        return Arrays.asList(new Object[]
                                 {
                                     "<three simple params>", "css=.@{a} a.@{b}:nth-child(@{c})",
                                     ImmutableMap.of("a", "detail", "b", "notice", "c", "1"), "css=.detail a.notice:nth-child(1)"
                                 },
                             new Object[]
                                 {
                                     "<two params used multiple times>",
                                     "xpath=//a[@class='table_detail_link'][contains(@href, '=@{priceBookId}')][starts-with(text(), '@{priceBookId}')]/td[contains(text(), '@{price}')]",
                                     ImmutableMap.<String, String>of("priceBookId", "AutomatedPriceBook_CreatePriceBook", "price", "25.40"),
                                     "xpath=//a[@class='table_detail_link'][contains(@href, '=AutomatedPriceBook_CreatePriceBook')][starts-with(text(), 'AutomatedPriceBook_CreatePriceBook')]/td[contains(text(), '25.40')]"
                                 },
                             new Object[]
                                 {
                                     "<two unknown params>",
                                     "xpath=//a[@class='table_detail_link'][contains(@href, '=@{priceBookId}')][starts-with(text(), '@{priceBookId}')]/td[contains(text(), '@{price}')]",
                                     Collections.emptyMap(),
                                     "xpath=//a[@class='table_detail_link'][contains(@href, '=@{priceBookId}')][starts-with(text(), '@{priceBookId}')]/td[contains(text(), '@{price}')]"
                                 }, new Object[]
                                 {
                                     "<self-resolving param>", "xpath=//div[@class='@{myClass}']",
                                     ImmutableMap.of("myClass", "@{myClass}"), "xpath=//div[@class='@{myClass}']"
                                 });
    }

    @Before
    public void prepare() throws Throwable
    {
        final List<ScriptElement> commands = Collections.emptyList();
        final List<String> parms = Collections.emptyList();

        script = new CommandScript(new File("foobar.xml"), commands, parms);
        m = script.getClass().getDeclaredMethod("resolve", String.class, Map.class);
        m.setAccessible(true);
    }

    @Test
    public void testResolve() throws Throwable
    {
        final String expected = (String) m.invoke(script, nameOrValue, args);
        Assert.assertEquals(this.expected, expected);
    }
}
