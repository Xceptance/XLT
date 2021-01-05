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
public class deleteCookie extends AbstractWebDriverModule
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
        deleteCookie("testsuite-xlt");
        // deleteCookie("^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
        //
        // ~~~ delete ~~~
        //
        startAction("delete");
        createCookie("testsuite-xlt=xlt-testsuite");
        final AssertCookie _assertCookie = new AssertCookie();
        _assertCookie.execute("testsuite-xlt", "xlt-testsuite");

        deleteCookie("testsuite-xlt");
        _assertCookie.execute("testsuite-xlt", "");


        //
        // ~~~ delete_twice ~~~
        //
        startAction("delete_twice");
        createCookie("testsuite-xlt=xlt-testsuite");
        _assertCookie.execute("testsuite-xlt", "xlt-testsuite");

        deleteCookie("testsuite-xlt");
        deleteCookie("testsuite-xlt");
        _assertCookie.execute("testsuite-xlt", "");


        //
        // ~~~ delete_non_existing ~~~
        //
        startAction("delete_non_existing");
        createCookie("testsuite-xlt=xlt-testsuite");
        _assertCookie.execute("testsuite-xlt", "xlt-testsuite");

        deleteCookie("xyz");
        _assertCookie.execute("testsuite-xlt", "xlt-testsuite");

        deleteCookie("testsuite-xlt");
        _assertCookie.execute("testsuite-xlt", "");


        //
        // ~~~ specialChars ~~~
        //
        // startAction("specialChars");
        // createCookie("^°!§$%&`´|üöäÜÖÄ+*~#'-_.=^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
        // _assertCookie.execute("^°!§$%&`´|üöäÜÖÄ+*~#'-_.","^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
        // deleteCookie("^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
        // _assertCookie.execute("testsuite-xlt","");

        //
        // ~~~ delete_without_open_page ~~~
        //
        startAction("delete_without_open_page");
        createCookie("testsuite-xlt=xlt-testsuite");
        _assertCookie.execute("testsuite-xlt", "xlt-testsuite");

        close();
        deleteCookie("testsuite-xlt", "path=/testpages/examplePage_1.html");
        _open_ExamplePage.execute();

        _assertCookie.execute("testsuite-xlt", "");


        //
        // ~~~ cleanup ~~~
        //
        startAction("cleanup");
        deleteCookie("testsuite-xlt");
        // deleteCookie("^°!§$%&`´|üöäÜÖÄ+*~#'-_.");
    }
}