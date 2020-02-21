package com.xceptance.xlt.engine.junit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runner.Runner;

import com.xceptance.xlt.api.data.DataSetProvider;

/**
 * A JUnit {@link Runner} implementation for arbitrary Java test cases. This runner runs a certain test case possibly
 * multiple times if there is a {@link DataSetProvider} with more than one data set (data-driven test). To use this
 * runner, annotate your JUnit test class with this class using {@link RunWith}.
 */
public class JavaTestCaseRunner extends AbstractTestCaseRunner
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
    }

    /**
     * Constructor. Only called reflectively from a JUnit runtime environment. Do not use programmatically.
     * 
     * @param testCaseClass
     *            the test case class
     */
    public JavaTestCaseRunner(final Class<?> testCaseClass) throws Throwable
    {
        super(testCaseClass, testCaseClass.getName(), null, dataSetFileDirs);
    }
}
