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
package scripting.modules;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;
import scripting.modules.AssertCookie;

/**
 * TODO: Add class description
 */
public class deleteAllVisibleCookies extends AbstractWebDriverModule
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
        // ~~~ createCookies ~~~
        //
        startAction("createCookies");
        createCookie("testsuite-xlt_1=xlt-testsuite_1");
        createCookie("testsuite-xlt_2=xlt-testsuite_2");
        createCookie("testsuite-xlt_3=xlt-testsuite_3");
        //
        // ~~~ checkPresence ~~~
        //
        startAction("checkPresence");
        final AssertCookie _assertCookie = new AssertCookie();
        _assertCookie.execute("testsuite-xlt_1", "xlt-testsuite_1");

        final AssertCookie _assertCookie0 = new AssertCookie();
        _assertCookie0.execute("testsuite-xlt_2", "xlt-testsuite_2");

        final AssertCookie _assertCookie1 = new AssertCookie();
        _assertCookie1.execute("testsuite-xlt_3", "xlt-testsuite_3");

        //
        // ~~~ deleteAll ~~~
        //
        startAction("deleteAll");
        deleteAllVisibleCookies();
        //
        // ~~~ checkAbsence ~~~
        //
        startAction("checkAbsence");
        final AssertCookie _assertCookie2 = new AssertCookie();
        _assertCookie2.execute("testsuite-xlt_1", "");

        final AssertCookie _assertCookie3 = new AssertCookie();
        _assertCookie3.execute("testsuite-xlt_2", "");

        final AssertCookie _assertCookie4 = new AssertCookie();
        _assertCookie4.execute("testsuite-xlt_3", "");


    }
}