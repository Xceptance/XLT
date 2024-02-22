/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package scripting.testcases;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;
import scripting.modules.Open_ExamplePage;
import scripting.modules.AttributeLocatorTest_1;
import scripting.modules.AttributeLocatorTest_2;
import scripting.modules.AttributeLocatorTest_3;
import scripting.modules.AttributeLocatorTest_4;

/**
 * TODO: Add class description
 */
public class assertAttribute extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public assertAttribute()
    {
        super("http://localhost:8080");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        //
        // ~~~ complete ~~~
        //
        startAction("complete");
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "foobar");
        //
        // ~~~ implicit_glob ~~~
        //
        startAction("implicit_glob");
        // substring (starting with)
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "foo*");
        // substring (ending with)
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "*bar");
        // substring (contains)
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "*oo*");
        // single char wildcard
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "?oo?ar");
        //
        // ~~~ whitespaces ~~~
        //
        startAction("whitespaces");
        // contains whitespace
        assertAttribute("xpath=id('ws8_a')/input[2]@value", "foo bar");
        // start with whitespace
        assertAttribute("xpath=id('ws8_a')/input[3]@value", " foobar");
        // ends with whitespace
        assertAttribute("xpath=id('ws8_a')/input[4]@value", "foobar ");
        // whitespaces all around und in between
        assertAttribute("xpath=id('ws8_a')/input[5]@value", " foo bar ");
        // attribute consits of whitespaces only
        assertAttribute("xpath=id('select_17')/option[@title='2 spaces']@value", "  ");
        //
        // ~~~ matching_strategies ~~~
        //
        startAction("matching_strategies");
        // exact
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "exact:foobar");
        // explicit glob
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "glob:foo*");
        // regexp
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "regexp:fo{2}ba\\w");
        // regexp
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "regexp:.+");
        // regexpi
        assertAttribute("xpath=id('ws8_a')/input[1]@value", "regexpi:fo{2}BA\\w");
        // empty attribute value
        assertAttribute("xpath=id('in_txt_13')@value", "");
        // special chars: keyspace characters
        assertAttribute("xpath=id('special_char_set4_1')@value", "glob:1234567890 qwertzuiop asdfghjkl yxcvbnm QWERTZUIOP ASDFGHJKL YXCVBNM äöü ÄÖÜ ß !\"§$%&/()=? `´^° <> ,;.:-_ #'+*²³{[]}\\ @€~ |µ ©«» ¼×");
        // hidden element
        assertAttribute("xpath=id('special_char_set4_2')@value", "special_char_set4_2");
        //
        // ~~~ attribute_vs_module_parameter ~~~
        //
        startAction("attribute_vs_module_parameter");
        final AttributeLocatorTest_1 _attributeLocatorTest_1 = new AttributeLocatorTest_1();
        _attributeLocatorTest_1.execute();

        final AttributeLocatorTest_2 _attributeLocatorTest_2 = new AttributeLocatorTest_2();
        _attributeLocatorTest_2.execute("foobar");

        final AttributeLocatorTest_3 _attributeLocatorTest_3 = new AttributeLocatorTest_3();
        _attributeLocatorTest_3.execute("foobar");

        final AttributeLocatorTest_4 _attributeLocatorTest_4 = new AttributeLocatorTest_4();
        _attributeLocatorTest_4.execute("value");


    }

}