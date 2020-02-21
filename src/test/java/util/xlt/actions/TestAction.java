package util.xlt.actions;

import com.xceptance.xlt.api.actions.AbstractAction;

/**
 * A dummy specialization of {@link AbstractAction} that does nothing, to be used by unit tests
 * 
 * @author Deniz Altin
 */
public class TestAction extends AbstractAction
{
    public TestAction(AbstractAction previousAction, String timerName)
    {
        super(previousAction, timerName);
    }

    public TestAction(String timerName)
    {
        this(null, timerName);
    }

    public TestAction()
    {
        this(null, null);
    }

    @Override
    public void preValidate() throws Exception
    {
    }

    @Override
    protected void execute() throws Exception
    {
    }

    @Override
    protected void postValidate() throws Exception
    {
    }

    /**
     * First call {@link #preValidateSafe()}; if it returns {@code true}, call {@link #run()}, otherwise don't.
     * <p>
     * This is a commonly used pattern.
     * 
     * @return {@code false} if and only if {@link #preValidateSafe()} does
     * @throws Throwable
     *             if {@link #run()} does
     */
    public boolean runIfPossible() throws Throwable
    {
        if (preValidateSafe())
        {
            run();
            return true;
        }

        return false;
    }
}
