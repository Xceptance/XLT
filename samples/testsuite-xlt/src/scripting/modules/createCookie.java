/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package scripting.modules;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.AssertCookie;

/**
 * TODO: Add class description
 */
public class createCookie extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {
        final Open_ExamplePage _open_ExamplePage = new Open_ExamplePage();
        _open_ExamplePage.execute();

        //
        // ~~~ cleanup ~~~
        //
        startAction("cleanup");
        deleteCookie("x_1");
        deleteCookie("x_2");
        deleteCookie("x_3");
        deleteCookie("x_4");
        deleteCookie("x_5");
        deleteCookie("x_6");
        deleteCookie("x_7");
        //
        // ~~~ create ~~~
        //
        startAction("create");
        createCookie("x_1=create");
        final AssertCookie _assertCookie = new AssertCookie();
        _assertCookie.execute("x_1", "create");

        //
        // ~~~ overwrite ~~~
        //
        startAction("overwrite");
        createCookie("x_2=value_a");
        createCookie("x_2=value_b");
        final AssertCookie _assertCookie0 = new AssertCookie();
        _assertCookie0.execute("x_2", "value_b");

        //
        // ~~~ empty_cookie_value ~~~
        //
        startAction("empty_cookie_value");
        createCookie("x_3=");
        final AssertCookie _assertCookie1 = new AssertCookie();
        _assertCookie1.execute("x_3", "");

        //
        // ~~~ optionsString ~~~
        //
        startAction("optionsString");
        createCookie("x_4=create_with_option_string", "path=/,max_age=10");
        final AssertCookie _assertCookie2 = new AssertCookie();
        _assertCookie2.execute("x_4", "create_with_option_string");

        //
        // ~~~ specialChars ~~~
        //
        // startAction("specialChars");
        // createCookie("x_5=^°!§$%&`´|üöäÜÖÄß+*~#'-_.\\");
        // final AssertCookie _assertCookie3 = new AssertCookie();
        // _assertCookie3.execute("x_5","^°!§$%&`´|üöäÜÖÄß+*~#'-_.\\");
        //
        // ~~~ quotedString ~~~
        //
        startAction("quotedString");
        createCookie("x_6=\"( ){ }[ ]< >:@?/=,\"");
        final AssertCookie _assertCookie4 = new AssertCookie();
        _assertCookie4.execute("x_6", "\"( ){ }[ ]< >:@?/=,\"");

        //
        // ~~~ create_without_open_page ~~~
        //
        // startAction("create_without_open_page");
        // close();
        // createCookie("x_7=create","path=/testpages/examplePage_1.html");
        // final Open_ExamplePage _open_ExamplePage0 = new Open_ExamplePage();
        // _open_ExamplePage0.execute();
        // final AssertCookie _assertCookie5 = new AssertCookie();
        // _assertCookie5.execute("x_7","create");
        //
        // ~~~ cleanup ~~~
        //
        startAction("cleanup");
        deleteCookie("x_1");
        deleteCookie("x_2");
        deleteCookie("x_3");
        deleteCookie("x_4");
        deleteCookie("x_5");
        deleteCookie("x_6");
        deleteCookie("x_7");

    }
}