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