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