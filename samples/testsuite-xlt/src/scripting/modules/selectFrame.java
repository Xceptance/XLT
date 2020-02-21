package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class selectFrame extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {

        //
        // ~~~ open_frame_page ~~~
        //
        startAction("open_frame_page");
        open("testpages/frame.html");

        //
        // ~~~ frame_1_byIndex ~~~
        //
        startAction("frame_1_byIndex");
        selectWindow("title=frame parent");
        selectFrame("index=0");
        assertTextPresent("This is frame 1.");

        //
        // ~~~ frame_2_byIndex ~~~
        //
        startAction("frame_2_byIndex");
        selectWindow("title=frame parent");
        selectFrame("index=1");
        assertTextPresent("This is frame 2.");

        //
        // ~~~ frame_1_byDom ~~~
        //
        startAction("frame_1_byDom");
        selectWindow("title=frame parent");
        selectFrame("dom=frames[\"frame_1\"]");
        assertTextPresent("This is frame 1.");

        //
        // ~~~ frame_2_byDom ~~~
        //
        startAction("frame_2_byDom");
        selectWindow("title=frame parent");
        selectFrame("dom=frames[\"frame_2\"]");
        assertTextPresent("This is frame 2.");

    }
}