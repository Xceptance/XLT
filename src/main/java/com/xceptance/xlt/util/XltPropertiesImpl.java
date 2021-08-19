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
package com.xceptance.xlt.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.common.util.PropertiesUtils;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.engine.util.IncludedFilesResolver;

/**
 * The property keeper. Loads and stores the properties of the entire tool. Single instance implementation.
 * <p>
 * The process of looking up a property uses multiple fall-backs. When resolving the value for the key "foo.bar", for
 * instance, the following effective keys are tried, in this order:
 * <ol>
 * <li>the test user name plus simple key, e.g. "TOrder.foo.bar"</li>
 * <li>the test class name plus simple key, e.g. "posters.loadtest.tests.TOrder.foo.bar"</li>
 * <li>the simple key, i.e. "foo.bar"</li>
 * </ol>
 * This multi-step process allows for test-user-specific or test-class-specific overrides of certain settings, while
 * falling back to the globally defined values if such specific settings are absent.
 */
public class XltPropertiesImpl extends XltProperties
{
    /**
     * The properties object holding the current configuration.
     */
    private final Properties properties;

    /**
     * The start time of the test. It will be set when the singleton instance is created.
     */
    private long startTime = -1L;

    /**
     * The XLT version string.
     */
    private String version;

    /**
     * Contains the absolute paths to the resolved property files. This means the property files which are there by
     * default and the property files transitively included by &quot;includes&quot; in these property files.
     */
    private final List<String> resolvedPropertyFiles = new ArrayList<String>();

    /**
     * Singleton instance. Created on demand by calling {@link #getInstance()} or {@link #getInstance(boolean)}.
     */
    private static volatile XltPropertiesImpl _instance;

    /**
     * Gate used to avoid recursive creations of the singleton. Necessary due to static initializer of XltLogger.
     */
    private static final ThreadLocal<ThreadLocal<?>> Gate = new ThreadLocal<>();

    /**
     * Returns the singleton instance which is initialized on demand.
     *
     * @param ignoreMissingIncludes
     *            whether or not missing property includes should be ignored
     * @return the singleton instance
     */
    public static XltPropertiesImpl getInstance(final boolean ignoreMissingIncludes)
    {
        if (_instance == null)
        {
            synchronized (XltPropertiesImpl.class)
            {
                if (_instance == null)
                {
                    _instance = _createInstance(ignoreMissingIncludes);
                }

            }
        }
        return _instance;
    }

    /**
     * Creates a new instance of XltPropertiesImpl using the testsuite's home and configuration directories as currently
     * set at XltExecutionContext. This method uses a thread-local gate to detect and avoid recursive attempts to create
     * an instance.
     *
     * @param ignoreMissingInclude
     *            whether or not missing properties includes should be ignored
     * @return new instance of XltPropertiesImpl or {@code null} if this method is called recursively
     */
    private static XltPropertiesImpl _createInstance(final boolean ignoreMissingInclude)
    {
        if (Gate.get() != null)
        {
            return null;
        }

        try
        {
            Gate.set(Gate);

            return new XltPropertiesImpl(null, null, ignoreMissingInclude);
        }
        finally
        {
            Gate.set(null);
        }
    }

    /**
     * Returns the one and only XltProperties instance.
     *
     * @return the XltProperties singleton
     */
    public static XltPropertiesImpl getInstance()
    {
        return getInstance(false);
    }

    /**
     * Resets the properties framework. This is mainly needed for testing.
     */
    public static synchronized void reset()
    {
        getInstance().initialize(null, null, false);
    }

    /**
     * Creates an XltProperties instance using the given parameters.
     *
     * @param homeDirectory
     *            the home directory
     * @param configDirectory
     *            the configuration directory
     * @param ignoreMissing
     *            whether to ignore any missing property file include
     */
    public XltPropertiesImpl(final FileObject homeDirectory, final FileObject configDirectory, final boolean ignoreMissing)
    {
        properties = new VarSubstitutionSupportedProperties();
        initialize(homeDirectory, configDirectory, ignoreMissing);
    }

    /**
     * Checks whether there is a mapping for the specified key in this property list.
     *
     * @param key
     *            the property key
     * @return <code>true</code> if there is a mapping, <code>false</code> otherwise
     */
    public boolean containsKey(final String key)
    {
        return properties.containsKey(XltConstants.SECRET_PREFIX + key) || properties.containsKey(key);
    }

    /**
     * Returns a copy of all the internally stored properties, with any placeholder resolved.
     *
     * @return the properties
     */
    public final Properties getProperties()
    {
        final Properties copy = new Properties();

        // resolve and copy all properties
        for (final Object k : properties.keySet())
        {
            final String key = (String) k;
            copy.setProperty(key, properties.getProperty(key));
        }

        return copy;
    }

    /**
     * Convenience method. Calls {@link #getPropertiesForKey(String, Properties)} with the member properties of this
     * instance.
     *
     * @see #getPropertiesForKey(String, Properties)
     */
    public Map<String, String> getPropertiesForKey(final String domainKey)
    {
        return PropertiesUtils.getPropertiesForKey(domainKey, properties);
    }

    /**
     * Returns the effective key to be used for property lookup via one of the getProperty(...) methods.
     * <p>
     * When looking up a key, "password" for example, the following effective keys are tried, in this order:
     * <ol>
     * <li>the prefix "secret." plus the simple key to ensure precedence of secret properties over public ones</li>
     * <li>the test user name plus simple key, e.g. "TAuthor.password"</li>
     * <li>the test class name plus simple key, e.g. "com.xceptance.xlt.samples.tests.TAuthor.password"</li>
     * <li>the simple key, e.g. "password"</li>
     * </ol>
     *
     * @param bareKey
     *            the bare property key, i.e. without any prefixes
     * @return the first key that produces a result
     */
    private String getEffectiveKey(final String bareKey)
    {
        final String nonPrefixedKey = bareKey.startsWith(XltConstants.SECRET_PREFIX) ? bareKey.substring(XltConstants.SECRET_PREFIX.length())
                                                                                     : bareKey;

        final SessionImpl session = SessionImpl.getCurrent();
        if (session != null)
        {
            // if we have a session, user and class specific props may take precedence

            // 1.0 use the current user name as prefix for a secret property
            final String userNameQualifiedSecretKey = XltConstants.SECRET_PREFIX + session.getUserName() + "." + nonPrefixedKey;
            if (properties.containsKey(userNameQualifiedSecretKey))
            {
                return userNameQualifiedSecretKey;
            }

            // 1.1 use the current user name as prefix
            final String userNameQualifiedKey = session.getUserName() + "." + bareKey; // do not return public props if
                                                                                       // the test case requested a
                                                                                       // secret
            if (properties.containsKey(userNameQualifiedKey))
            {
                return userNameQualifiedKey;
            }

            // 2.0 use the current class name as prefix for a secret property
            final String classNameQualifiedSecretKey = XltConstants.SECRET_PREFIX + session.getTestCaseClassName() + "." + nonPrefixedKey;
            if (properties.containsKey(classNameQualifiedSecretKey))
            {
                return classNameQualifiedSecretKey;
            }

            // 2.1 use the current class name as prefix
            final String classNameQualifiedKey = session.getTestCaseClassName() + "." + bareKey; // do not return public
                                                                                                 // props if the test
                                                                                                 // case requested a
                                                                                                 // secret
            if (properties.containsKey(classNameQualifiedKey))
            {
                return classNameQualifiedKey;
            }
        }
        // 3.0. Check whether the given key is available as a secret property, in which case it takes precedence
        final String secretKey = XltConstants.SECRET_PREFIX + nonPrefixedKey;
        if (properties.containsKey(secretKey))
        {
            return secretKey;
        }

        // 3.1 use the bare key
        return bareKey;
    }

    /**
     * Searches for the property with the specified key in this property list. The method returns null if the property
     * is not found.
     *
     * @param key
     *            the property key
     * @return the value of the key
     */
    public String getProperty(final String key)
    {
        // get value of property and return it
        final String effectiveKey = getEffectiveKey(key);
        return properties.getProperty(effectiveKey);
    }

    /**
     * Searches for the property with the specified key in this property list. The method returns the default value
     * argument if the property is not found.
     *
     * @param key
     *            the property key
     * @param defaultValue
     *            the defaultValue if key not found
     * @return the value of the key as a boolean
     */
    public boolean getProperty(final String key, final boolean defaultValue)
    {
        // get value of property
        final String valueString = getProperty(key);
        // if property is set, parse its boolean value and return it
        if (valueString != null)
        {
            return Boolean.valueOf(valueString);
        }

        // property not set so far -> return defaultValue
        return defaultValue;
    }

    /**
     * Searches for the property with the specified key in this property list. The method returns the default value
     * argument if the property is not found.
     *
     * @param key
     *            the property key
     * @param defaultValue
     *            the defaultValue if key not found
     * @return the value of the key as an int
     */
    public int getProperty(final String key, final int defaultValue)
    {
        // get property value
        final String valueString = getProperty(key);
        // if property is set, parse its integer value and return it
        if (valueString != null)
        {
            try
            {
                return Integer.parseInt(valueString);
            }
            catch (final NumberFormatException e)
            {
            }
        }

        // property is not set so far or its integer value does not seem to be a
        // string representation of an integer value -> return defaultValue
        return defaultValue;
    }

    /**
     * Searches for the property with the specified key in this property list. The method returns the default value
     * argument if the property is not found.
     *
     * @param key
     *            the property key
     * @param defaultValue
     *            the defaultValue if key not found
     * @return the value of the key as a long
     */
    public long getProperty(final String key, final long defaultValue)
    {
        // get property value
        final String valueString = getProperty(key);
        // if property is set, parse its long value and return it
        if (valueString != null)
        {
            try
            {
                return Long.parseLong(valueString);
            }
            catch (final NumberFormatException e)
            {
            }
        }

        // property is not set so far or its value does not seem to be a string
        // representation of a long value -> return defaultValue
        return defaultValue;
    }

    /**
     * Searches for the property with the specified key in this property list. The method returns the default value
     * argument if the property is not found. The key is upper-cased before the property will be searched.
     *
     * @param key
     *            the property key
     * @param defaultValue
     *            the defaultValue if key not found
     * @return the value of the key
     */
    public String getProperty(final String key, final String defaultValue)
    {
        // get property value
        final String value = getProperty(key);
        // if property is set, return its value, else return defaultValue
        return (value != null) ? value : defaultValue;
    }

    /**
     * Returns one value of the given multi-value property. Multiple values are separated by comma, semicolon, or space.
     * The returned value is chosen randomly from the set of values.
     *
     * @param key
     *            the name of the property
     * @param defaultValue
     *            the default property value (a multi-value)
     * @return one of the values, chosen randomly
     */
    public String getPropertyRandomValue(final String key, final String defaultValue)
    {
        // get all values
        final String value = getProperty(key, defaultValue);
        if (value == null)
        {
            return XltConstants.EMPTYSTRING;
        }

        // split multi-value string into its values by various delimiters
        final String[] values = com.xceptance.common.lang.StringUtils.split(value, "[ ,;]");

        // return a random value
        return values[XltRandom.nextInt(values.length)];
    }

    /**
     * Returns the start time of the test in milliseconds since 1970.
     *
     * @return the start time of the test in milliseconds
     */
    public long getStartTime()
    {
        return startTime;
    }

    /**
     * Returns the product version.
     *
     * @return the version string, e.g. "1.1.0"
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Removes the property with the given key from the internal properties store.
     *
     * @param key
     *            the property key
     */
    public void removeProperty(final String key)
    {
        ParameterCheckUtils.isNotNull(key, "key");

        properties.remove(key);
    }

    /**
     * Defines a source for property data. If properties are already loaded, these new properties will be added. If a
     * property already exists it will be overwritten. Last one wins. Automatically adds java system properties
     * afterwards.
     *
     * @param file
     *            the file that contains the properties to be loaded
     * @throws IOException
     *             thrown when opening the file or reading from the file failed
     */
    public void setProperties(final File file) throws IOException
    {
        ParameterCheckUtils.isNotNull(file, "file");

        setProperties(VFS.getManager().resolveFile(file.getAbsolutePath()));
    }

    /**
     * Defines a source for property data. If properties are already loaded, these new properties will be added. If a
     * property already exists it will be overwritten. Last one wins. Automatically adds java system properties
     * afterwards.
     *
     * @param file
     *            the file that contains the properties to be loaded
     * @throws IOException
     *             thrown when opening the file or reading from the file failed
     */
    public void setProperties(final FileObject file) throws IOException
    {
        ParameterCheckUtils.isNotNull(file, "file");

        if (XltLogger.runTimeLogger.isInfoEnabled())
        {
            XltLogger.runTimeLogger.info("Loading properties from file: " + file.getName().getURI());
        }

        final Properties properties = PropertiesUtils.loadProperties(file);

        setProperties(properties);
    }

    /**
     * Method for changing the properties during runtime. Can be called multiple times to add additional properties.
     * Automatically adds java system properties afterwards.
     *
     * @param newProperties
     *            complete new set of properties, will be added to existing properties and overwrites already defined
     *            properties with new values. None existing properties will be added.
     */
    public void setProperties(final Properties newProperties)
    {
        ParameterCheckUtils.isNotNull(newProperties, "newProperties");

        // store the new properties in synchronized block since we need to iterate over them
        synchronized (newProperties)
        {
            properties.putAll(newProperties);
        }
    }

    /**
     * Sets a property during runtime. Overwrites an existing property with the same name. Does not re-apply any java
     * system settings.
     *
     * @param key
     *            new property key
     * @param value
     *            new property value
     */
    public void setProperty(final String key, final String value)
    {
        ParameterCheckUtils.isNotNull(key, "key");
        ParameterCheckUtils.isNotNull(value, "value");

        properties.setProperty(key, value);
    }

    /**
     * Updates the properties.
     */
    public void update()
    {
        update(null, null, false);
    }

    public void update(final FileObject homeDirectory, final FileObject configDirectory, final boolean ignoreMissing)
    {
        properties.clear();

        // load the properties from the statically configured property files
        loadProperties(homeDirectory, configDirectory, ignoreMissing);

    }

    /**
     * Initializes the instance.
     */
    private void initialize(final FileObject homeDirectory, final FileObject configDirectory, boolean ignoreMissing)
    {
        update(homeDirectory, configDirectory, ignoreMissing);

        // get version and start time
        version = ProductInformation.getProductInformation().getVersion();
        startTime = GlobalClock.getInstance().getTime();
    }

    /**
     * Loads the properties from different files that are supposed to be located in the XLT configuration directory:
     * <ol>
     * <li>default.properties</li>
     * <li>project.properties</li>
     * <li>file referred to by com.xceptance.xlt.testPropertiesFile in project.properties</li>
     * </ol>
     * Properties will be loaded and overwritten in that order. Last defined property value wins. Furthermore property
     * files includes via &quot;xlt.com.xceptance.loadtest.include.NUMBER&quot; properties are loaded (see ticket 1650
     * for details).
     * <p>
     * Finally, the Java system properties are loaded because they are the last instances where some property value can
     * be overridden.
     * </p>
     */
    private void loadProperties(FileObject homeDirectory, FileObject configDirectory, boolean ignoreMissing)
    {
        if (homeDirectory == null)
        {
            homeDirectory = XltExecutionContext.getCurrent().getTestSuiteHomeDir();
        }
        if (configDirectory == null)
        {
            configDirectory = XltExecutionContext.getCurrent().getTestSuiteConfigDir();
        }

        /*
         * We first have to get the basic roots as we do not know if the name for the "test.properties" has been reset
         * in the properties.
         */
        final List<FileObject> roots = getRoots(configDirectory);
        final List<String> files = new ArrayList<>();
        for (final String file : IncludedFilesResolver.resolveIncludePropertyFiles(roots, homeDirectory, ignoreMissing))
        {
            files.add(makeRelativeTo(file, configDirectory));
        }

        /* Load the properties resolved so far as we have to check for a renamed "test.properties". */
        loadPropertyFiles(files, 0, configDirectory);

        /*
         * We collect the remaining roots in the same list as we need all roots in one list to get the correct order for
         * the resolved includes (important in case of duplicate includes).
         */

        final int alreadyLoadedFiles = files.size();
        files.clear();

        roots.addAll(getAdditionalRoots(configDirectory));

        /* Now, resolve all includes. */

        for (final String file : IncludedFilesResolver.resolveIncludePropertyFiles(roots, homeDirectory, ignoreMissing))
        {
            final String path = makeRelativeTo(file, configDirectory);
            resolvedPropertyFiles.add(path);
            files.add(path);
        }

        /*
         * Just load the resolved files. There is no need to clear anything as we overwrite the properties. Thus the
         * properties that are already have been read are overwritten by themselves but this time in correct order.
         */
        loadPropertyFiles(files, alreadyLoadedFiles, configDirectory);

        /* load the secret properties file, if any */
        loadSecretProperties(configDirectory);

        // system properties always overwrite properties from files
        setProperties(System.getProperties());

        // finally log the properties
        logProperties();
    }

    /**
     * Load the secret properties (if any) from the given config directory
     *
     * @param configDirectory
     *            The directory where to look for the secret properties file
     */
    private void loadSecretProperties(final FileObject configDirectory)
    {
        try
        {
            final FileObject secretFile = configDirectory.resolveFile(XltConstants.SECRET_PROPERTIES_FILENAME);
            if (secretFile.isReadable())
            {
                final Properties props = PropertiesUtils.loadProperties(secretFile);
                resolvedPropertyFiles.add(secretFile.getName().getBaseName());

                for (final Entry<Object, Object> entry : props.entrySet())
                {
                    final String name = (String) entry.getKey();
                    final String value = (String) entry.getValue();

                    if (name.startsWith(XltConstants.SECRET_PREFIX))
                    {
                        properties.setProperty(name, value);
                    }
                    else
                    {
                        properties.setProperty(XltConstants.SECRET_PREFIX + name, value);
                    }
                }
            }
        }
        catch (FileNotFoundException _e)
        {
            XltLogger.runTimeLogger.trace("Could not load secret properties. File does not exist.");
        }
        catch (IOException e)
        {
            XltLogger.runTimeLogger.error("Could not load secret properties.", e);
        }
    }

    /**
     * Convenience method to load all files from the argument collection.
     *
     * @param files
     * @param alreadyLoadedFiles
     *            the number of files that were already loaded to avoid duplicate attempts on them
     */
    private void loadPropertyFiles(final List<String> files, final int alreadyLoadedFiles, final FileObject configDirectory)
    {
        for (int i = alreadyLoadedFiles; i < files.size(); i++)
        {
            loadPropertiesFile(files.get(i), false, configDirectory);
        }
    }

    /**
     * Collects the roots for the property files, but just for the &quot; default.properties&quot; and the &quot;
     * project.properties&quot;. However the returned list may be empty or contain just a single file object as these
     * files seem to be optional judging from the current implementation.
     *
     * @param configDir
     *            the configuration directory to use
     * @return a list containing file objects for the basic configuration files
     * @see #getAdditionalRoots()
     */
    private List<FileObject> getRoots(final FileObject configDir)
    {
        final List<FileObject> roots = new ArrayList<FileObject>();

        addFile(configDir, XltConstants.DEFAULT_PROPERTY_FILENAME, true, roots);
        addFile(configDir, XltConstants.PROJECT_PROPERTY_FILENAME, true, roots);

        return roots;
    }

    /**
     * Collects the roots for the additional (&quot;test.properties&quot; and &quot;dev.properties&quot; if in
     * development mode) property files. We first have to collect the properties to be read earlier as someone might
     * have reconfigured the name for the expected &quot;test.properties&quot; in the properties. And the
     * &quot;dev.properties&quot; has to be read as last as the order is important or it must not be read in at all.
     *
     * @param configDir
     *            the configuration directory to use
     * @see #getRoots()
     */
    private List<FileObject> getAdditionalRoots(final FileObject configDir)
    {
        final List<FileObject> roots = new ArrayList<FileObject>();

        // get the test properties file (use properties directly to break loop between SessionImpl and XltProperties)
        final String testPropertiesFile = properties.getProperty(XltConstants.TEST_PROPERTIES_FILE_PATH_PROPERTY);

        // load the properties files
        addFile(configDir, testPropertiesFile, false, roots);

        // guess whether we are in development mode
        final boolean isDevMode = XltEngine.getInstance().isDevMode();
        if (isDevMode)
        {
            addFile(configDir, XltConstants.DEV_PROPERTY_FILENAME, true, roots);
        }

        return roots;
    }

    /**
     * Adds the file with the absolute path made of the argument path with a slash and the argument file name appended
     * to the argument files. May log a warning about a missing file.
     *
     * @param configDir
     *            the configuration directory to use
     * @param fileName
     *            the name of the file for which to create a file object
     * @param logNotExistingFile
     *            indicating whether to log a FATAL if the searched file does not exist
     * @param files
     *            the list of files to which to add the created file object
     */
    private void addFile(final FileObject configDir, final String fileName, final boolean logNotExistingFile, final List<FileObject> files)
    {
        if (StringUtils.isNotBlank(fileName))
        {
            boolean exists = false;
            try
            {
                final FileObject child = configDir.resolveFile(fileName);
                exists = child.exists();
                if (exists)
                {
                    files.add(child);
                }
            }
            catch (final FileSystemException fse)
            {
            }

            if (!exists && logNotExistingFile)
            {
                if (XltLogger.runTimeLogger.isInfoEnabled())
                {
                    XltLogger.runTimeLogger.info("No such property file: " + configDir.getName().getPath() + "/" + fileName);
                }
            }

        }
    }

    /**
     * Loads the properties from the file identified by the given file name.
     *
     * @param fileName
     *            name of property file to be loaded
     * @param ignoreMissingFile
     *            does silently skip not existing given property file
     */
    private void loadPropertiesFile(final String fileName, final boolean ignoreMissingFile, final FileObject configDir)
    {
        // parameter validation
        if (fileName == null || fileName.length() == 0)
        {
            return;
        }

        try
        {
            // create file handle
            if (configDir == null)
            {
                throw new IOException("Unable to access configuration directory");
            }

            final FileObject file = configDir.resolveFile(fileName);

            // try to load the properties from the given file if it exists
            if (XltLogger.runTimeLogger.isInfoEnabled())
            {
                XltLogger.runTimeLogger.info("Trying to load property file '" + file.getName().getURI() + "'.");
            }

            if (!ignoreMissingFile || (file.exists() && file.getType() == FileType.FILE))
            {
                setProperties(file);
            }
        }
        catch (final IOException e)
        {
            // log the error, but continue
            XltLogger.runTimeLogger.fatal("Failed to load properties file: " + fileName, e);
        }
    }

    /**
     * Logs the properties as a sorted list.
     */
    private void logProperties()
    {
        if (XltLogger.runTimeLogger.isDebugEnabled())
        {
            XltLogger.runTimeLogger.debug("----------------------------------------------------------------");

            final Map<Object, Object> sortedProperties = new TreeMap<Object, Object>(properties);
            for (final Entry<Object, Object> entry : sortedProperties.entrySet())
            {
                final String maskedValue = ((String) entry.getKey()).startsWith(XltConstants.SECRET_PREFIX) ? XltConstants.MASK_PROPERTIES_HIDETEXT
                                                                                                            : (String) entry.getValue();
                XltLogger.runTimeLogger.debug("| " + entry.getKey() + " = " + maskedValue);
            }

            XltLogger.runTimeLogger.debug("----------------------------------------------------------------");
        }
    }

    /**
     * Returns the absolute paths to the resolved property files. This means the property files which are there by
     * default and the property files transitively included by &quot;includes&quot; in these property files. However
     * note that some of the default files are optional (as &quot;dev.properties&quot;) and the returned list only
     * contains existing files.
     *
     * @return the resolved property files as described above
     */
    public List<String> getResolvedPropertyFiles()
    {
        return resolvedPropertyFiles;
    }

    private static String makeRelativeTo(final String path, final FileObject target)
    {
        try
        {

            final FileObject fo = target.resolveFile(path);
            final ArrayList<String> pathSegments = new ArrayList<>();

            FileObject tmp = target;
            FileObject tmp2 = fo;
            int depthResolved = fo.getName().getDepth();
            int depthPivot = target.getName().getDepth() + 1;
            if (depthResolved < depthPivot)
            {
                while (depthResolved < depthPivot)
                {
                    tmp = tmp.getParent();
                    pathSegments.add("..");
                    --depthPivot;

                }
            }
            else if (depthResolved > depthPivot)
            {
                while (depthResolved > depthPivot)
                {
                    tmp2 = tmp2.getParent();
                    pathSegments.add(0, tmp2.getName().getBaseName());
                    --depthResolved;
                }
            }

            if (!tmp2.getParent().equals(tmp))
            {
                throw new IllegalArgumentException(String.format("Paths '%s' and '%s' do not have a common ancestor.",
                                                                 fo.getName().getPath(), target.getName().getPath()));
            }
            pathSegments.add(fo.getName().getBaseName());
            return StringUtils.join(pathSegments, '/').toString();

        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.warn(e.getMessage());
            return path;
        }
    }

    /**
     * Subclass of {@link Properties} which enables substitution of variables used in property values. Additionally all
     * property values will be trimmed on write access.
     */
    private static class VarSubstitutionSupportedProperties extends Properties
    {
        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = -9202819207114231133L;

        /**
         * {@inheritDoc}
         */
        @Override
        public String getProperty(final String key)
        {
            final String val = super.getProperty(key);
            if (val == null || val.length() == 0)
            {
                return val;
            }

            return PropertiesUtils.substituteVariables(val, this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized Object put(final Object key, final Object value)
        {
            if (null == key || null == value)
            {
                return null;
            }

            return super.put(key, ((String) value).trim());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized void putAll(final Map<? extends Object, ? extends Object> map)
        {
            // #3031: make sure our put() method is used when someone calls putAll()

            for (final Entry<?, ?> entry : map.entrySet())
            {
                put(entry.getKey(), entry.getValue());
            }
        }
    }
}
