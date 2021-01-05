/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.scripting;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.openqa.selenium.WebDriver;

import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.scripting.WebDriverCustomModule;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.scripting.util.AbstractCommandAdapter;
import com.xceptance.xlt.engine.scripting.util.ReplayUtils;

/**
 * The basic XLT script interpreter. It knows how to execute commands, but does not know anything about the script
 * syntax.
 */
public abstract class AbstractScriptInterpreter
{
    /**
     * The log facility.
     */
    private static final Log LOG = AbstractCommandAdapter.LOGGER;

    /**
     * The module scripts already loaded so far, keyed by their name.
     */
    private static final Map<String, Script> moduleCache = new HashMap<String, Script>();

    /**
     * The test case scripts already loaded so far, keyed by their name.
     */
    private static final Map<String, Script> testCaseCache = new HashMap<String, Script>();

    /**
     * The command processor which executes commands.
     */
    private WebDriverCommandProcessor commandProcessor;

    /**
     * Indicates whether an action has not been started yet. Used to detect whether a default start action should be
     * generated.
     */
    private boolean noActionStartedYet = true;

    /**
     * The WebDriver instance.
     */
    private final WebDriver webDriver;

    private final LineNumberType linenumberType;

    /**
     * Constructor.
     * 
     * @param baseUrl
     *            the base URL to use for relative URLs in the script
     * @param webDriver
     *            the WebDriver instance to use
     */
    protected AbstractScriptInterpreter(final WebDriver webDriver)
    {
        this.webDriver = webDriver;

        // get the line numbering type
        final String linenumber_tmp = XltProperties.getInstance().getProperty(XltConstants.LINE_NUMBER_TYPE_PROPERTY);
        LineNumberType type_tmp = null;
        if (StringUtils.isNotBlank(linenumber_tmp))
        {
            try
            {
                type_tmp = LineNumberType.get(linenumber_tmp);
            }
            catch (final Exception e)
            {
                type_tmp = LineNumberType.scriptdeveloper;
            }
            linenumberType = type_tmp;
        }
        else
        {
            linenumberType = LineNumberType.scriptdeveloper;
        }
    }

    /**
     * Closes this script interpreter.
     */
    public void close()
    {
        if (commandProcessor != null)
        {
            commandProcessor.close();
        }
    }

    /**
     * Executes the script with the given name.
     * 
     * @param scriptName
     *            the name of the script to execute
     * @throws Exception
     *             in case of errors during script execution
     */
    public void executeScript(final String scriptName) throws Exception
    {
        final TestCase testCaseScript = (TestCase) getTestCaseScript(scriptName);
        if (testCaseScript.isDisabled())
        {
            if (LOG.isWarnEnabled())
            {
                LOG.warn(String.format("Script test case '%s' is disabled but hasn't been annotated with @org.junit.Ignore", scriptName));
            }

            return;
        }

        TestContext.getCurrent().pushScope(testCaseScript);

        String baseUrl = TestContext.getCurrent().getBaseUrl();
        // get the base URL from the test case script if not overridden in the test case class
        if (StringUtils.isBlank(baseUrl))
        {
            baseUrl = testCaseScript.getBaseUrl();
            if (!StringUtils.isBlank(baseUrl))
            {
                TestContext.getCurrent().setBaseUrl(baseUrl);
            }
        }

        final AtomicInteger sdLinenumber = new AtomicInteger();
        try
        {
            Throwable throwable = null;

            try
            {
                executeScript(testCaseScript, null, sdLinenumber);
            }
            catch (final Throwable t)
            {
                throwable = t;
            }

            try
            {
                executePostSteps(testCaseScript, sdLinenumber);
            }
            catch (final Throwable t2)
            {
                if (throwable != null)
                {
                    throwable.addSuppressed(t2);
                }
                else
                {
                    throwable = t2;
                }
            }

            if (throwable != null)
            {
                // Check what we have (exceptions and errors can be re-thrown just as they are)
                if (!(throwable instanceof Exception))
                {
                    if (throwable instanceof Error)
                    {
                        throw (Error) throwable;
                    }
                    // -> create a new exception with the same details (message, cause, stack-trace and suppressed
                    // exception).
                    else
                    {
                        final Throwable[] suppressed = throwable.getSuppressed();
                        final StackTraceElement[] trace = throwable.getStackTrace();
                        throwable = new ScriptException(throwable.getMessage(), throwable.getCause());
                        if (trace != null)
                        {
                            throwable.setStackTrace(trace);
                        }
                        if (suppressed != null)
                        {
                            for (final Throwable suppressedThrowable : suppressed)
                            {
                                throwable.addSuppressed(suppressedThrowable);
                            }
                        }
                    }
                }
                throw (Exception) throwable;
            }

        }
        finally
        {
            // finish the last action and take a last screenshot
            finishCurrentAction();

            TestContext.getCurrent().popScope();
        }
    }

    /**
     * Parses the module script file with the specified name and returns the corresponding {@link Script} object.
     * 
     * @param scriptName
     *            the name of the script
     * @return the script object
     * @throws Exception
     *             if an error occurred
     */
    protected abstract Script parseModuleScriptFile(String scriptName) throws Exception;

    /**
     * Parses the test case script file with the specified name and returns the corresponding {@link Script} object.
     * 
     * @param scriptName
     *            the name of the script
     * @return the script object
     * @throws Exception
     *             if an error occurred
     */
    protected abstract Script parseTestCaseScriptFile(String scriptName) throws Exception;

    /**
     * Returns the command processor, lazily created.
     * 
     * @return the command processor
     */
    private WebDriverCommandProcessor getCommandProcessor()
    {
        if (commandProcessor == null)
        {
            commandProcessor = new WebDriverCommandProcessor(webDriver);
        }

        return commandProcessor;
    }

    /**
     * Executes the given script using the specified global and local variables.
     * 
     * @param script
     *            the script to execute
     * @param context
     *            the execution context
     * @param parameters
     *            the set of local variables
     * @param sdLinenumber
     *            script developer line number of test case
     * @throws Exception
     *             in case of errors during script execution
     */
    private void executeScript(final Script script, final Map<String, String> parameters, final AtomicInteger sdLinenumber) throws Exception
    {
        // remember the start value to calculate the local line number
        final int baseLineNumber = sdLinenumber.get();

        // resolve parameter values
        final HashMap<String, String> params;
        if (parameters != null)
        {
            params = new HashMap<String, String>();
            for (final Map.Entry<String, String> entry : parameters.entrySet())
            {
                params.put(entry.getKey(), resolve(entry.getValue()));
            }
        }
        else
        {
            params = null;
        }

        // execute the script
        if (script instanceof CommandScript)
        {
            final CommandScript commandScript = (CommandScript) script;
            for (final ScriptElement scriptElement : commandScript.getScriptElements(params))
            {
                executeScriptElement(commandScript, scriptElement, baseLineNumber, sdLinenumber);
            }
        }
        else if (script instanceof JavaModule)
        {
            final JavaModule javaModule = (JavaModule) script;
            final String javaModuleClassName = javaModule.getClassName();

            try
            {
                final Class<?> javaModuleClass = Class.forName(javaModuleClassName);
                final WebDriverCustomModule javaModuleObject = (WebDriverCustomModule) javaModuleClass.newInstance();

                if (LOG.isDebugEnabled())
                {
                    LOG.debug(String.format("Executing Java module: %s", javaModuleClassName));
                }

                final List<String> formalParameters = javaModule.getParameterNames();

                final String[] arguments = new String[formalParameters.size()];
                for (int i = 0; i < arguments.length; i++)
                {
                    arguments[i] = params.get(formalParameters.get(i));
                }
                javaModuleObject.execute(webDriver, arguments);
            }
            catch (final Exception e)
            {
                throw new ScriptException("Failed to execute Java module: " + javaModuleClassName, e);
            }
        }
        else
        {
            throw new ScriptException("Don't know how to execute script: " + script);
        }
    }

    /**
     * Executes a single step (the given script element) in context of the given script.
     * 
     * @param script
     *            the script
     * @param scriptElement
     *            the step to execute
     * @param baseLineNumber
     *            the base line-number
     * @param sdLinenumber
     *            the line-number counter
     * @throws Exception
     *             thrown on error
     */
    private void executeScriptElement(final CommandScript script, final ScriptElement scriptElement, final int baseLineNumber,
                                      final AtomicInteger sdLinenumber)
        throws Exception
    {
        // update line number
        final int lineNumber = sdLinenumber.incrementAndGet();

        if (scriptElement.isDisabled())
        {
            if (LOG.isInfoEnabled())
            {
                LOG.info(String.format("Skipping disabled script element: %s", scriptElement));
            }

            // increment line-number by the size of the called module
            if (scriptElement instanceof ModuleCall)
            {
                sdLinenumber.set(lineNumber + getModuleScript(scriptElement.getName()).getSize());
            }
        }
        else
        {
            final String name = scriptElement.getName();

            if (scriptElement instanceof Command)
            {
                // check whether we need to implicitly open a start action
                if (noActionStartedYet)
                {
                    // set a default start action name
                    Session.getCurrent().startAction("OpenStartPage");
                    noActionStartedYet = false;
                }

                final Command command = (Command) scriptElement;
                try
                {
                    doCommand(command);
                }
                catch (final Throwable t)
                {
                    final int linenumber = linenumberType.equals(LineNumberType.file) ? scriptElement.getLineNumber()
                                                                                      : lineNumber - baseLineNumber;
                    t.setStackTrace(new StackTraceElement[]
                        {
                            getTraceElement(script, command, linenumber)
                        });
                    if (t instanceof Error)
                    {
                        throw (Error) t;
                    }
                    throw (Exception) t;
                }
            }
            else if (scriptElement instanceof Action)
            {
                if (!noActionStartedYet)
                {
                    finishCurrentAction();
                }

                Session.getCurrent().startAction(name);
                noActionStartedYet = false;
            }
            else if (scriptElement instanceof CodeComment)
            {
                if (LOG.isInfoEnabled())
                {
                    LOG.info("Skipping comment: " + RegExUtils.replaceAll(name, "\\s+", " "));
                }
            }
            else
            {
                final ModuleCall moduleCall = (ModuleCall) scriptElement;
                if (needToCallModule(moduleCall))
                {
                    // get the script module
                    if (LOG.isInfoEnabled())
                    {
                        LOG.info("Calling module: " + name);
                    }

                    final Script moduleScript = getModuleScript(name);

                    // execute the module call
                    TestContext.getCurrent().pushScope(moduleScript);
                    try
                    {
                        executeScript(moduleScript, moduleCall.getParameters(), sdLinenumber);
                    }
                    catch (final Throwable t)
                    {
                        final int linenumber = linenumberType.equals(LineNumberType.file) ? scriptElement.getLineNumber()
                                                                                          : lineNumber - baseLineNumber;
                        t.setStackTrace(appendLineNumberToStackTrace(t.getStackTrace(), script, moduleCall, linenumber));
                        if (t instanceof Error)
                        {
                            throw (Error) t;
                        }
                        throw (Exception) t;
                    }
                    finally
                    {
                        TestContext.getCurrent().popScope();
                    }

                    if (LOG.isInfoEnabled())
                    {
                        LOG.info("Returned from module: " + name);
                    }
                }
                else
                {
                    if (LOG.isInfoEnabled())
                    {
                        LOG.info("Skipping module: " + name);
                    }
                }
            }
        }

    }

    /**
     * Returns whether or not the given module call shall be executed.
     * 
     * @param moduleCall
     *            the module call
     * @return <code>true</code> if and only if the given module call shall be executed, <code>false</code> otherwise.
     */
    protected boolean needToCallModule(final ModuleCall moduleCall)
    {
        if (moduleCall != null)
        {
            final CallCondition cond = moduleCall.getCondition();
            if (cond != null && !cond.isDisabled())
            {
                return evaluateCondition(resolve(cond.getConditionExpression()));
            }
            return true;
        }

        return false;
    }

    /**
     * Evaluates the given expression.
     * 
     * @param expression
     *            the JavaScript expression to evaluate.
     * @return whether or not the given expression evaluates to <code>true</code
     */
    protected boolean evaluateCondition(final String expression)
    {
        return getCommandProcessor().getAdapter().evaluatesToTrue(expression);
    }

    /**
     * Computes the size of the given command script. Java modules have a size of zero so there is no need to compute
     * the size for them. <br />
     * As for command scripts, their size is computed as the number of commands plus the size of each called module in
     * order of occurrence.
     * 
     * @param script
     *            the script whose size shall be computed
     * @return the size of the given script
     */
    private int computeScriptSize(CommandScript script) throws Exception
    {
        int size = 0;
        for (final ScriptElement el : script.getScriptElements())
        {
            ++size;
            if (el instanceof ModuleCall)
            {
                size += getModuleScript(el.getName()).getSize();
            }
        }
        return size;
    }

    /**
     * Finishes the current action and takes a screenshot.
     */
    private void finishCurrentAction()
    {
        Session.getCurrent().stopAction();
    }

    /**
     * Build stack trace element containing test case / module, parameters, line number and reason.
     * 
     * @param commandScript
     *            script that failed
     * @param scriptElement
     *            script element that failed
     * @param params
     *            script parameters
     * @param elementPosition
     *            position of script element
     * @return built stack trace element
     * @throws Exception
     */
    private StackTraceElement getTraceElement(final CommandScript commandScript, final ScriptElement scriptElement,
                                              final int elementPosition)
        throws Exception
    {
        final String scriptsDir = getScriptsDirectory().getCanonicalPath();
        final String scriptPath = commandScript.getScriptFile().getCanonicalPath();
        //
        final String relativeScriptPath = scriptPath.substring(scriptsDir.length());
        // script name with leading package path
        final String scriptPackagePath = relativeScriptPath.replaceAll("[\\\\/]", ".");
        // get script package
        String scriptPackage = scriptPackagePath.startsWith(".") ? scriptPackagePath.substring(1, scriptPackagePath.length())
                                                                 : scriptPackagePath;
        scriptPackage = scriptPackage.contains(".") ? scriptPackage.substring(0, scriptPackage.lastIndexOf(".")) : StringUtils.EMPTY;

        String scriptName = scriptElement.getName();
        scriptName = scriptName.contains(".") ? scriptName.substring(scriptName.lastIndexOf(".") + 1) : scriptName;

        return new StackTraceElement(scriptPackage, scriptName, commandScript.getScriptFile().getName(), elementPosition);
    }

    /**
     * Appends the line number information to the given input stack trace and returns the new trace.
     * 
     * @param origTrace
     *            the original stack trace
     * @param script
     *            the script
     * @param element
     *            the script element
     * @param elementPosition
     *            position of the input script element
     * @return new stack trace which contains the same information as the given input stack trace except for the last
     *         element that holds the line number information
     * @throws Exception
     */
    private StackTraceElement[] appendLineNumberToStackTrace(final StackTraceElement[] origTrace, final CommandScript script,
                                                             final ScriptElement element, final int elementPosition)
        throws Exception
    {
        final int traceLength = origTrace.length;
        final StackTraceElement[] newTrace = new StackTraceElement[traceLength + 1];
        System.arraycopy(origTrace, 0, newTrace, 0, traceLength);
        newTrace[traceLength] = getTraceElement(script, element, elementPosition);

        return newTrace;
    }

    /**
     * Executes the post-steps of the given test.
     * 
     * @param testCase
     *            the test case currently executing
     * @param sdLineNumber
     *            the line-number counter
     * @throws Exception
     *             thrown on error
     */
    private void executePostSteps(final TestCase testCase, final AtomicInteger sdLineNumber) throws Exception
    {
        final List<Throwable> throwables = new ArrayList<>();

        // script size is the number of all commands before the 1st post-step
        final int afterStart = testCase.getSize();
        int offset = 0;

        for (final ScriptElement scriptElement : testCase.getPostSteps())
        {
            sdLineNumber.set(afterStart + offset);
            try
            {
                executeScriptElement(testCase, scriptElement, 0, sdLineNumber);
            }
            catch (final Throwable t)
            {
                throwables.add(t);
            }

            ++offset;
            if (scriptElement instanceof ModuleCall)
            {
                offset += getModuleScript(scriptElement.getName()).getSize();
            }
        }

        if (!throwables.isEmpty())
        {
            final ScriptException ex = new ScriptException("Failed to execute post-steps of testcase: " +
                                                           testCase.getScriptFile().getAbsolutePath());
            for (final Throwable t : throwables)
            {
                ex.addSuppressed(t);
            }
            throw ex;
        }
    }

    /**
     * Executes the given command using the given context.
     * 
     * @param command
     *            the command to execute
     * @param context
     *            the execution context
     */
    public void doCommand(final Command command)
    {
        // resolve variable references
        final String target = resolve(command.getTarget());
        final String value = resolve(command.getValue());

        final String name = command.getName();

        // execute the command
        try
        {
            String realTarget = target;
            String realValue = value;

            // for value-only commands: make the value the new target
            if (ReplayUtils.isValueOnlyCommand(name))
            {
                realTarget = value;
                realValue = null;
            }

            // execute the command
            getCommandProcessor().doCommand(name, realTarget, realValue);
        }
        catch (final AssertionError e)
        {
            // throw as-is
            throw e;
        }
        catch (final Throwable t)
        {
            // throw as ScriptException
            throw new ScriptException(t.getMessage(), t.getCause());
        }
    }

    /**
     * Returns the module script with the given name. If the script is requested for the first time, the corresponding
     * script file is read and parsed and the resulting script is cached in memory, so any subsequent lookup will return
     * the cached script.
     * 
     * @param moduleName
     *            the name of the script
     * @return the corresponding script object
     * @throws Exception
     *             if an error occurred during loading/parsing the script file
     */
    private Script getModuleScript(final String moduleName) throws Exception
    {
        synchronized (moduleCache)
        {
            Script script = moduleCache.get(moduleName);
            if (script == null)
            {
                script = parseModuleScriptFile(moduleName);
                moduleCache.put(moduleName, script);

                loadCalledModules(script);
            }

            return script;
        }
    }

    /**
     * Returns the test case script with the given name. If the script is requested for the first time, the
     * corresponding script file is read and parsed and the resulting script is cached in memory, so any subsequent
     * lookup will return the cached script.
     * 
     * @param testCaseName
     *            the name of the script
     * @return the corresponding script object
     * @throws Exception
     *             if an error occurred during loading/parsing the script file
     */
    private Script getTestCaseScript(final String testCaseName) throws Exception
    {
        synchronized (testCaseCache)
        {
            Script script = testCaseCache.get(testCaseName);
            if (script == null)
            {
                script = parseTestCaseScriptFile(testCaseName);
                testCaseCache.put(testCaseName, script);

                loadCalledModules(script);
            }

            return script;
        }
    }

    /**
     * Loads all module scripts which are called from the given script.
     * 
     * @param script
     *            the script
     * @throws Exception
     *             if an error occurred during loading the module script files
     */
    private void loadCalledModules(final Script script) throws Exception
    {
        if (script instanceof CommandScript)
        {
            final CommandScript commandScript = (CommandScript) script;

            // recursively load/parse any called modules
            for (final ScriptElement scriptElement : commandScript.getScriptElements())
            {
                if (scriptElement instanceof ModuleCall)
                {
                    final ModuleCall moduleCall = (ModuleCall) scriptElement;
                    getModuleScript(moduleCall.getName());
                }
            }

            // all called modules have been loaded -> compute and store size
            script.setSize(computeScriptSize(commandScript));
        }
    }

    /**
     * Resolves all placeholders in the given string and returns the result.
     * 
     * @param resolvable
     *            the input string
     * @return the input string where all placeholders have been resolved
     */
    private String resolve(final String resolvable)
    {
        return TestContext.getCurrent().resolve(resolvable);
    }

    /**
     * Get scripts directory
     * 
     * @return scripts directory
     */
    protected abstract File getScriptsDirectory();
}
