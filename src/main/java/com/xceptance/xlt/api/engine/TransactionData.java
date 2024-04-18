/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.engine;

import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.util.XltCharBuffer;
import com.xceptance.xlt.common.XltConstants;

/**
 * <p>
 * The {@link TransactionData} class holds any data measured for a transaction. Typically, a transaction spans exactly
 * one execution of a certain test case, which itself comprises one or more actions.
 * </p>
 * <p>
 * The values stored include not only the transaction's start and run time, but also an indicator whether or not the
 * transaction was executed successfully and, if it has failed, what was the cause. Data gathered for the same type of
 * transaction may be correlated via the name attribute.
 * </p>
 * <p style="color:green">
 * Note that {@link TransactionData} objects have a "T" as their type code.
 * </p>
 *
 * @see ActionData
 * @see RequestData
 * @see CustomData
 * @see EventData
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class TransactionData extends TimerData
{
    /**
     * The type code ("T").
     */
    private static final char TYPE_CODE = 'T';

    /**
     * The last part of the path to the directory where dumped pages can be found. Kept separate to make as much use of
     * String.intern() as possible to save memory. The final path is constructed on request in
     * {@link TransactionData#getDumpDirectoryPath()}.
     */
    private String directoryName;

    /**
     * The name of the action that caused the transaction to fail. Will be empty if the transaction was successful or
     * the transaction failed outside of an action.
     */
    private String failedActionName;

    /**
     * The stack trace of the throwable that caused this transaction to fail, if any.
     */
    private String stackTrace;

    /**
     * The second last part of the path to the directory where dumped pages can be found. Kept separate to make as much
     * use of String.intern() as possible to save memory. The final path is constructed on request in
     * {@link TransactionData#getDumpDirectoryPath()}.
     */
    private String testUserNumber;

    /**
     * Creates a new TransactionData object.
     */
    public TransactionData()
    {
        super(TYPE_CODE);
    }

    /**
     * Creates a new TransactionData object and gives it the specified name. Furthermore, the time attribute is set to
     * the current time.
     *
     * @param name
     *            the transaction name
     */
    public TransactionData(final String name)
    {
        super(name, TYPE_CODE);
    }

    /**
     * Returns the name of the directory where the result browser for this transaction is stored.
     *
     * @return the directory name
     */
    public String getDirectoryName()
    {
        return directoryName;
    }

    /**
     * Sets the name of the directory where the result browser for this transaction is stored.
     *
     * @param directoryName
     *            the directory name
     */
    public void setDirectoryName(final String directoryName)
    {
        this.directoryName = directoryName;
    }

    /**
     * Returns the number (or index, [0..N]) of the test user that produced this transaction data.
     *
     * @return the test user number as a string
     */
    public String getTestUserNumber()
    {
        return testUserNumber;
    }

    /**
     * Sets the number (or index, [0..N]) of the test user that produced this transaction data.
     *
     * @param testUserNumber
     *            the test user number as a string
     */
    public void setTestUserNumber(final String testUserNumber)
    {
        this.testUserNumber = testUserNumber;
    }

    /**
     * Returns the name of the action that caused the transaction to fail. Will be empty if the transaction was
     * successful or the transaction failed outside of an action.
     *
     * @return the action name
     */
    public String getFailedActionName()
    {
        return failedActionName;
    }

    /**
     * Sets the name of the action that caused the transaction to fail.
     *
     * @param actionName
     *            the action name
     */
    public void setFailedActionName(final String actionName)
    {
        this.failedActionName = actionName;
    }

    /**
     * Returns the path to the directory where dumped pages can be found if this transaction failed. The path is meant
     * to be relative to the results directory of the respective load test. Typically, it looks like
     * "ac1/TAuthor/1/output/1216803080255".
     *
     * @return the dump directory path, or <code>null</code> if this transaction did not fail or no directory
     *         information was available
     */
    public String getDumpDirectoryPath()
    {
        if (StringUtils.isAnyEmpty(testUserNumber, directoryName))
        {
            return null;
        }

        return getAgentName() + "/" + getName() + "/" + testUserNumber + "/" + XltConstants.DUMP_OUTPUT_DIR + "/" + directoryName;
    }

    /**
     * Returns the message of the throwable that caused this transaction to fail.
     *
     * @return the message (may be null)
     */
    public String getFailureMessage()
    {
        // do NOT replace "\n" with IOUtils.LINE_SEPARATOR, it won't work on Windows
        final String messageWithClassPrefix = StringUtils.substringBefore(stackTrace, "\n");

        String plainMessage = StringUtils.substringAfter(messageWithClassPrefix, ": ");
        if (plainMessage.isEmpty())
        {
            // report at least the class name
            plainMessage = messageWithClassPrefix;
        }

        return plainMessage;
    }

    /**
     * Returns the stack trace of the throwable that caused this transaction to fail.
     *
     * @return the trace (may be null)
     */
    public String getFailureStackTrace()
    {
        return stackTrace;
    }

    /**
     * Sets the stack trace of the throwable that caused this transaction to fail.
     *
     * @param trace
     *            the trace
     */
    public void setFailureStackTrace(final String trace)
    {
        stackTrace = trace;
    }

    /**
     * Sets the stack trace attribute retrieved from the given throwable.
     *
     * @param throwable
     *            the throwable
     */
    public void setFailureStackTrace(final Throwable throwable)
    {
        if (throwable == null)
        {
            stackTrace = null;
        }
        else
        {
            stackTrace = ThrowableUtils.getMinifiedStackTrace(throwable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> toList()
    {
        final List<String> fields = super.toList();

        // process and add the stack trace
        String t = stackTrace;

        if (t == null)
        {
            t = "";
        }
        else
        {
            // translate any EOL to '\' so that the trace fits on one line
            t = t.replace("\n", "\\");
            t = t.replace("\r", "");
        }

        fields.add(t);

        // add the other fields
        fields.add(StringUtils.defaultString(failedActionName));
        fields.add(StringUtils.defaultString(testUserNumber));
        fields.add(StringUtils.defaultString(directoryName));

        return fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRemainingValues(final List<XltCharBuffer> values)
    {
        super.setRemainingValues(values);

        // process the stack trace
        // TODO performance
        stackTrace = values.get(5).toString().trim();
        if (stackTrace.length() == 0)
        {
            stackTrace = null;
        }
        else
        {
            // undo any "quoted" character
            // TODO performance
            stackTrace = stackTrace.replace("\\", "\n");
        }

        // be defensive so a report can be generated also for older results
        final int length = values.size();
        if (length > 6)
        {
            setFailedActionName(values.get(6).toString());
        }

        // test user number and directory name (since XLT 4.13.2)
        if (length > 7)
        {
            setTestUserNumber(values.get(7).toString());
            setDirectoryName(values.get(8).toString());
        }
        else
        {
            // fallback for older results: test user number and directory name may be present in the stack trace
            if (stackTrace != null)
            {
                // find the directory hint in the trace
                final Matcher matcher = RegExUtils.getPattern(ThrowableUtils.DIRECTORY_HINT_REGEX).matcher(stackTrace);
                if (matcher.find())
                {
                    // remove all hints
                    stackTrace = StringUtils.remove(stackTrace, matcher.group());

                    // keep some values so we can reconstruct the hint on request
                    final String testUserId = matcher.group(1);
                    final int i = testUserId.lastIndexOf('-');
                    testUserNumber = StringUtils.substring(testUserId, i + 1);
                    directoryName = matcher.group(2);
                }
            }
        }
    }
}