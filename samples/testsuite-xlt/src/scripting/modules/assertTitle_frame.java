package scripting.modules;

import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverModule;

/**
 * TODO: Add class description
 */
public class assertTitle_frame extends AbstractWebDriverModule
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCommands(final String...parameters) throws Exception
    {

        //
        // ~~~ OpenFramePage ~~~
        //
        startAction("OpenFramePage");
        open("testpages/frame.html");

        //
        // ~~~ assertTitle ~~~
        //
        startAction("assertTitle");
        assertTitle("frame parent");

    }
}