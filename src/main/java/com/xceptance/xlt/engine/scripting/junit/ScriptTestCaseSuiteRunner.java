package com.xceptance.xlt.engine.scripting.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;

import com.xceptance.xlt.api.data.DataSetProvider;

/**
 * A JUnit {@link Runner} implementation for a set of script test cases. This runner runs all test scripts configured in
 * the system property "com.xceptance.xlt.api.junit.ScriptTestCaseSuite.testCases", each possibly multiple times if
 * there is a {@link DataSetProvider} with more than one data set (data-driven test).
 */
public class ScriptTestCaseSuiteRunner extends ParentRunner<Runner>
{
    /**
     * The JUnit child runners, in this case {@link ScriptTestCaseRunner}.
     */
    private final List<Runner> runners = new ArrayList<Runner>();

    /**
     * Only called reflectively. Do not use programmatically.
     */
    public ScriptTestCaseSuiteRunner(final Class<?> klass) throws Throwable
    {
        super(klass);

        // get the script test cases to run from the suite test case class
        @SuppressWarnings("unchecked")
        final List<String> testCaseNames = (List<String>) klass.getMethod("getTestCases").invoke(null);

        // create a JUnit child runner for each test case
        for (final String testCaseName : testCaseNames)
        {
            runners.add(new ScriptTestCaseRunner(testCaseName));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Runner> getChildren()
    {
        return runners;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Description describeChild(final Runner child)
    {
        return child.getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runChild(final Runner child, final RunNotifier notifier)
    {
        child.run(notifier);
    }
}
