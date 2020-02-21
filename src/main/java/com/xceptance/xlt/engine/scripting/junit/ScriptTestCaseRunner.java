package com.xceptance.xlt.engine.scripting.junit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.model.FrameworkMethod;

import com.xceptance.xlt.api.data.DataSetProvider;
import com.xceptance.xlt.engine.junit.AbstractTestCaseRunner;
import com.xceptance.xlt.engine.scripting.XlteniumScriptInterpreter;
import com.xceptance.xlt.engine.util.ScriptingUtils;

/**
 * A JUnit {@link Runner} implementation for script-based test cases. This runner runs a certain test case possibly
 * multiple times if there is a {@link DataSetProvider} with more than one data set (data-driven test). To use this
 * runner, annotate your JUnit test class with this class using {@link RunWith}.
 */
public class ScriptTestCaseRunner extends AbstractTestCaseRunner
{
    /**
     * The list of directories to be searched for data set files.
     */
    private static final List<File> dataSetFileDirs = new ArrayList<File>();

    static
    {
        // 1. the current directory
        dataSetFileDirs.add(CURRENT_DIR);

        // 2. the data sets directory if available
        if (DATA_SETS_DIR != null)
        {
            dataSetFileDirs.add(DATA_SETS_DIR);
        }

        // 3. the scripts directory
        dataSetFileDirs.add(XlteniumScriptInterpreter.SCRIPTS_DIRECTORY);
    }

    /**
     * The name of the test script.
     */
    private final String scriptName;

    /**
     * Constructor. Only called reflectively from a JUnit runtime environment. Do not use programmatically.
     * 
     * @param testCaseClass
     *            the script wrapper test case class
     */
    public ScriptTestCaseRunner(final Class<?> testCaseClass) throws Throwable
    {
        this(testCaseClass, ScriptingUtils.getScriptName(testCaseClass));
    }

    /**
     * Constructor. Only called from {@link ScriptTestCaseSuiteRunner}.
     * 
     * @param scriptName
     *            the name of the test script
     */
    public ScriptTestCaseRunner(final String scriptName) throws Throwable
    {
        this(GenericScriptTestCase.class, scriptName);
    }

    /**
     * Constructor.
     * 
     * @param testCaseClass
     *            the script wrapper test case class
     * @param scriptName
     *            the name of the test script
     */
    private ScriptTestCaseRunner(final Class<?> testCaseClass, final String scriptName) throws Throwable
    {
        super(testCaseClass, scriptName, ScriptingUtils.getScriptBaseName(scriptName), dataSetFileDirs);

        this.scriptName = scriptName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUpTest(final FrameworkMethod method, final Object test)
    {
        super.setUpTest(method, test);

        if (test instanceof GenericScriptTestCase)
        {
            // generic test case needs the name
            ((GenericScriptTestCase) test).setScriptName(scriptName);
        }
    }
}
