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

import static com.xceptance.xlt.engine.util.ScriptingUtils.DEFAULT_PACKAGE;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

import com.gargoylesoftware.htmlunit.WebClient;
import com.xceptance.common.collection.LRUHashMap;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.scripting.AbstractScriptTestCase;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.scripting.util.CommonScriptCommands;

/**
 * Test context.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class TestContext
{
    /**
     * Test contexts.
     */
    private static final Map<ThreadGroup, TestContext> CONTEXTS = new ConcurrentHashMap<ThreadGroup, TestContext>();

    /**
     * Default timeout property.
     */
    private static final String PROP_TIMEOUT = XltConstants.XLT_PACKAGE_PATH + ".scripting.defaultTimeout";

    /**
     * Default implicit timeout property.
     */
    private static final String PROP_IMPLICIT_TIMEOUT = XltConstants.XLT_PACKAGE_PATH + ".scripting.defaultImplicitWaitTimeout";

    /**
     * Default data cache size property.
     */
    private static final String DATA_CACHE_SIZE = XltConstants.XLT_PACKAGE_PATH + ".scripting.dataCacheSize";

    /**
     * Default command timeout.
     */
    private static final long defaultTimeout = XltProperties.getInstance().getProperty(PROP_TIMEOUT, 30000L);

    /**
     * Default implicit timeout used for element location.
     */
    private final long defaultImplicitTimeout = XltProperties.getInstance().getProperty(PROP_IMPLICIT_TIMEOUT, 1000L);

    /**
     * Current implicit timeout used for element location.
     */
    private final AtomicLong implicitTimeout = new AtomicLong(defaultImplicitTimeout);

    /**
     * Default data cache size.
     */
    private static final int dataCacheSize = XltProperties.getInstance().getProperty(DATA_CACHE_SIZE, 32);

    /**
     * Command timeout.
     */
    private final AtomicLong timeout = new AtomicLong(defaultTimeout);

    /**
     * Execution timer.
     */
    private Timer timer = new Timer();

    /**
     * Current scope.
     */
    private VariableScope _scope;

    private VariableScope _rScope;

    /**
     * Base URL as string.
     */
    private String _baseUrl = "";

    /**
     * Current web client.
     */
    private WeakReference<WebClient> _webClient;

    private WeakReference<CommonScriptCommands> _adapter;

    private volatile Map<String, String> _globalData;

    /**
     * Loaded test data.
     */
    private static final LRUHashMap<Object, Map<String, String>> LOADED_DATA = new LRUHashMap<Object, Map<String, String>>(dataCacheSize);

    /**
     * Loaded package data.
     */
    private static final LRUHashMap<String, Map<String, String>> LOADED_PKG_DATA = new LRUHashMap<String, Map<String, String>>(dataCacheSize);

    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private TestContext()
    {
        // Empty
    }

    /**
     * Returns the current context.
     * 
     * @return current test context
     */
    public static TestContext getCurrent()
    {
        final ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        TestContext c = CONTEXTS.get(threadGroup);
        if (c == null)
        {
            synchronized (threadGroup)
            {
                c = CONTEXTS.get(threadGroup);
                if (c == null)
                {
                    c = new TestContext();
                    CONTEXTS.put(threadGroup, c);
                }
            }
        }
        return c;
    }

    /**
     * Calls the given action and returns its result.
     * 
     * @param action
     *            the action to perform
     * @return result of the given action
     */
    public <T> T callAndWait(final Callable<T> action)
    {
        return getTimer().run(action);
    }

    /**
     * Returns the implicit timeout value as configured in the test suite settings.
     * 
     * @return the timeout [ms]
     */
    public long getDefaultImplicitTimeout()
    {
        return defaultImplicitTimeout;
    }

    /**
     * Returns the current implicit timeout.
     * 
     * @return the timeout [ms]
     */
    public long getImplicitTimeout()
    {
        return implicitTimeout.get();
    }

    /**
     * Sets the new implicit timeout.
     * 
     * @param timeout
     *            the new timeout [ms]
     */
    public void setImplicitTimeout(final long timeout)
    {
        final long to = Math.max(0L, timeout);
        implicitTimeout.set(to);
    }

    /**
     * Returns the command timeout value as configured in the test suite settings.
     * 
     * @return the timeout [ms]
     */
    public static long getDefaultTimeout()
    {
        return defaultTimeout;
    }

    /**
     * Returns the command timeout.
     * 
     * @return command timeout
     */
    public long getTimeout()
    {
        return timeout.get();
    }

    /**
     * Sets the command timeout.
     * 
     * @param timeout
     */
    public void setTimeout(final long timeout)
    {
        final long to = Math.max(0L, timeout);
        this.timeout.set(to);
    }

    /**
     * Shutdown.
     */
    public void shutDown()
    {
        synchronized (this)
        {
            if (timer != null)
            {
                timer.stop();
            }
        }
        setAdapter(null);
        setWebClient(null);
        setTimer(null);
        setScope(null);
    }

    /**
     * Returns the timer.
     * 
     * @return timer
     */
    private synchronized Timer getTimer()
    {
        if (timer == null)
        {
            timer = new Timer();
        }
        return timer;
    }

    /**
     * Sets the timer.
     * 
     * @param timer
     *            the timer
     */
    private synchronized void setTimer(final Timer timer)
    {
        this.timer = timer;
    }

    private class Timer
    {
        private final ExecutorService service = Executors.newSingleThreadExecutor();

        private <T> T run(final Callable<T> callable)
        {
            final Future<T> future = service.submit(new Callable<T>()
            {
                @Override
                public T call() throws Exception
                {
                    // set the thread's name again
                    Thread.currentThread().setName(Session.getCurrent().getUserID() + "-timer");

                    // delegate to the actual callable
                    return callable.call();
                }
            });
            try
            {
                return future.get(timeout.get(), TimeUnit.MILLISECONDS);
            }
            catch (final InterruptedException e)
            {
                throw new ScriptException("Interrupted while waiting for action to finish", e);
            }
            catch (final ExecutionException e)
            {
                final Throwable cause = e.getCause();
                if (cause instanceof RuntimeException)
                {
                    throw (RuntimeException) cause;
                }
                throw new RuntimeException(cause);
            }
            catch (final TimeoutException e)
            {
                throw new PageLoadTimeoutException("Timed out waiting for page to load", e);
            }
        }

        private void stop()
        {
            service.shutdownNow();
            try
            {
                service.awaitTermination(timeout.get(), TimeUnit.MILLISECONDS);
            }
            catch (final InterruptedException e)
            {
                // There is nothing sensible to do.
            }
        }
    }

    /**
     * Helper function used to retrieve the test data for the given object.
     * 
     * @param key
     *            the object to retrieve test data for
     * @return test data of given object
     */
    private Map<String, String> getData(final Object key)
    {
        if (key instanceof Script)
        {
            return getData((Script) key);
        }
        return getData(key.getClass());
    }

    /**
     * Helper function used to retrieve the test data for the given object.
     * 
     * @param key
     *            the object to retrieve test data for
     * @return test data of given object
     */
    private Map<String, String> getData(final Script script)
    {
        Map<String, String> data = LOADED_DATA.get(script);
        if (data == null)
        {
            synchronized (LOADED_DATA)
            {
                data = LOADED_DATA.get(script);
                if (data == null)
                {
                    data = new HashMap<String, String>(getPackageTestData(script));
                    data.putAll(TestDataUtils.getTestData(script));
                    LOADED_DATA.put(script, data);
                }
            }
        }
        return data;
    }

    /**
     * Helper function used to retrieve the test data for the given object.
     * 
     * @param key
     *            the object to retrieve test data for
     * @return test data of given object
     */
    private Map<String, String> getData(final Class<?> clazz)
    {
        Map<String, String> data = LOADED_DATA.get(clazz);
        if (data == null)
        {
            synchronized (LOADED_DATA)
            {
                data = LOADED_DATA.get(clazz);
                if (data == null)
                {
                    data = new HashMap<String, String>(getPackageTestData(clazz));
                    data.putAll(TestDataUtils.getTestData(clazz));
                    LOADED_DATA.put(clazz, data);
                }
            }
        }
        return data;
    }

    /**
     * Pops the current scope from the stack.
     */
    public void popScope()
    {
        VariableScope scope = getScope();
        if (scope != null)
        {
            scope = scope.getEnclosingScope();
        }
        setScope(scope);
    }

    /**
     * Pushes the scope for the given object onto the stack.
     * 
     * @param object
     */
    public void pushScope(final Object object)
    {
        if (object == null)
        {
            throw new IllegalArgumentException();
        }

        VariableScope scope = getScope();
        if (scope == null)
        {
            scope = new VariableScope(new HashMap<String, String>());
            getGlobalData();
        }

        if (object instanceof AbstractTestCase)
        {
            scope = new VariableScope(((AbstractTestCase) object).getTestDataSet(), scope);
        }

        if (!(object instanceof AbstractScriptTestCase))
        {
            scope = new VariableScope(getData(object), scope);
        }

        setScope(scope);
    }

    /**
     * Gets the package test data for the given object.
     * 
     * @param object
     *            the test object
     */
    private Map<String, String> getPackageTestData(final Script script)
    {
        final File baseDir = XlteniumScriptInterpreter.SCRIPTS_DIRECTORY;
        final String packageName;
        {
            final File scriptFile = script.getScriptFile();
            final ArrayList<String> parts = new ArrayList<String>();
            File dir = scriptFile;
            while ((dir = dir.getParentFile()) != null && !dir.equals(baseDir))
            {
                parts.add(0, dir.getName());
            }

            packageName = StringUtils.join(parts, '.');
        }

        return getPackageTestData(null, baseDir.getAbsolutePath(), packageName);
    }

    /**
     * Returns the package test data for the given script package.
     * 
     * @param clazz
     *            the context class to be used for resource lookup (pass {@code null} to force file lookup)
     * @param baseDir
     *            the base directory to be used for data file lookup
     * @param packageName
     *            the package name
     * @return the package test data
     */
    private static Map<String, String> getPackageTestData(final Class<?> clazz, final String baseDir, final String packageName)
    {
        final ArrayList<String> pkgs = new ArrayList<String>();
        String pkgName = packageName;
        int idx = pkgName.lastIndexOf('.');
        while (idx > -1)
        {
            pkgs.add(pkgName);
            pkgName = pkgName.substring(0, idx);
            idx = pkgName.lastIndexOf('.');
        }

        if (!DEFAULT_PACKAGE.equals(pkgName))
        {
            pkgs.add(pkgName);
        }

        final Map<String, String> m = new HashMap<String, String>(getOrLoadPackageData(clazz, baseDir, DEFAULT_PACKAGE));
        for (int i = pkgs.size() - 1; i >= 0; i--)
        {
            m.putAll(getOrLoadPackageData(clazz, baseDir, pkgs.get(i)));
        }

        return m;
    }

    /**
     * Returns the package test data from the internal cache or loads it from disk.
     * 
     * @param clazz
     *            the context class to be used for resource lookup (pass {@code null} to force file lookup)
     * @param baseDir
     *            the base directory
     * @param packageName
     *            the package name
     * @return the package test data
     */
    private static Map<String, String> getOrLoadPackageData(final Class<?> clazz, final String baseDir, final String packageName)
    {
        Map<String, String> data = LOADED_PKG_DATA.get(packageName);
        if (data == null)
        {
            synchronized (LOADED_PKG_DATA)
            {
                data = LOADED_PKG_DATA.get(packageName);
                if (data == null)
                {
                    data = TestDataUtils.getPackageTestData(clazz, baseDir, packageName);
                    LOADED_PKG_DATA.put(packageName, data);
                }
            }
        }
        return data;
    }

    /**
     * Returns the package test data for the given test class.
     * 
     * @param clazz
     *            the test class
     * @return package test data
     */
    private static Map<String, String> getPackageTestData(final Class<?> clazz)
    {
        final Package pkg = clazz.getPackage();
        final String packageName = pkg == null ? DEFAULT_PACKAGE : pkg.getName();

        try
        {
            final String baseDir;
            if (packageName.length() > 0)
            {
                final int nbPkgDelims = org.apache.commons.lang3.StringUtils.countMatches(packageName, ".");
                final StringBuilder sb = new StringBuilder();

                sb.append("..");
                for (int i = 0; i < nbPkgDelims; i++)
                {
                    sb.append('/').append("..");

                }

                baseDir = sb.toString();
            }
            else
            {
                baseDir = ".";
            }

            return getPackageTestData(clazz, baseDir, packageName);
        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.error("Failed to load test data for package '" + packageName + "'.", e);
        }
        return Collections.emptyMap();
    }

    /**
     * Sets the web client.
     * 
     * @param webClient
     *            the web client
     */
    public synchronized void setWebClient(final WebClient webClient)
    {
        _webClient = new WeakReference<WebClient>(webClient);
    }

    /**
     * Returns the web client.
     * 
     * @return web client
     */
    public synchronized WebClient getWebClient()
    {
        return _webClient.get();
    }

    /**
     * Sets the current scope.
     * 
     * @param scope
     *            the new current scope
     */
    private synchronized void setScope(final VariableScope scope)
    {
        _scope = scope;
        _rScope = null;
    }

    /**
     * Returns the current scope.
     * 
     * @return current scope
     */
    private synchronized VariableScope getScope()
    {
        return _scope;
    }

    private VariableScope getRScope()
    {
        final VariableScope scope = getScope();
        if (scope == null)
        {
            return null;
        }

        if (_rScope == null)
        {
            synchronized (this)
            {
                if (_rScope == null)
                {
                    _rScope = new VariableScope(_globalData, scope);
                }
            }
        }

        return _rScope;
    }

    /**
     * Returns the command adapter.
     * 
     * @return command adapter
     */
    public synchronized CommonScriptCommands getAdapter()
    {
        return _adapter.get();
    }

    /**
     * Sets the command adapter.
     * 
     * @param adapter
     *            the new command adapter
     */
    public synchronized void setAdapter(final CommonScriptCommands adapter)
    {
        _adapter = new WeakReference<CommonScriptCommands>(adapter);
    }

    /**
     * Returns the global test data.
     * 
     * @return global test data
     */
    public synchronized Map<String, String> getGlobalTestData()
    {
        return _globalData;
    }

    private void getGlobalData()
    {
        if (_globalData == null)
        {
            synchronized (this)
            {
                if (_globalData == null)
                {
                    _globalData = TestDataUtils.getGlobalTestData();
                }
            }
        }
    }

    private VariableScope getTopScope()
    {
        VariableScope scope = getScope();
        if (scope != null)
        {
            while (scope.hasEnclosingScope())
            {
                scope = scope.getEnclosingScope();
            }
        }
        return scope;
    }

    /**
     * Inserts a new test data mapping where the given key is mapped to the given value.
     * 
     * @param key
     *            the test data variable
     * @param value
     *            the value of the variable
     */
    public void storeValue(final String key, final String value)
    {
        final VariableScope scope = getTopScope();
        if (scope != null)
        {
            scope.storeValue(key, value);
        }
    }

    /**
     * Resolves the given string.
     * 
     * @param resolvable
     *            the string to be resolved
     * @return the resolved string
     */
    public String resolve(final String resolvable)
    {
        return getRScope().resolve(resolvable);
    }

    /**
     * Resolves the given test data key
     * 
     * @param key
     *            the key string containing only the name of a test data field
     * @return resolved string or <code>null</code> if not found
     */
    public String resolveKey(final String key)
    {
        return getRScope().resolveKey(key);
    }

    /**
     * Returns the configured base URL as string.
     * 
     * @return base URL as string (might be empty but is never null)
     */
    public synchronized String getBaseUrl()
    {
        return _baseUrl;
    }

    /**
     * Sets the base.
     * 
     * @param baseUrl
     *            the base URL as string
     */
    public synchronized void setBaseUrl(final String baseUrl)
    {
        _baseUrl = baseUrl == null ? "" : baseUrl;
    }
}
