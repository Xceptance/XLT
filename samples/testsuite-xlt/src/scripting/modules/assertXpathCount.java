package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;
import scripting.modules.Open_ExamplePage;

/**
 * TODO: Add class description
 */
public class assertXpathCount extends AbstractWebDriverModule
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
        // ~~~ existing_correctCount ~~~
        //
        startAction("existing_correctCount");
        assertXpathCount("id(\"xpath_count\")/input[@type=\"checkbox\" and @name=\"xpath_count\"]", 5);
        assertXpathCount("id(\"xyz\")", 0);

        //
        // ~~~ iframe ~~~
        //
        startAction("iframe");
        selectWindow("title=example page");
        selectFrame("index=0");
        selectFrame("index=0");
        selectFrame("index=0");
        assertXpathCount("/html/body/div[@id=\"f3_i\"]/input[@id=\"f3_ia\"]", 1);

    }
}