/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.common.util.ProductInformation;
import com.xceptance.common.util.PropertiesUtils;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.engine.XltEngine;
import com.xceptance.xlt.engine.XltExecutionContext;
import com.xceptance.xlt.engine.util.PropertyIncludeResolver;
import com.xceptance.xlt.engine.util.PropertyIncludeResolver.PropertyInclude;
import com.xceptance.xlt.engine.util.PropertyIncludeResolver.PropertyIncludeResult;

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
     * The properties object holding the current configuration. This is a merged version
     * of the buckets.
     */
    private final VarSubstitutionSupportedProperties mergedProperties = new VarSubstitutionSupportedProperties();

    /**
     * Holding all properties by source, so we can later expose this to callers to add properties
     * at a certain position if needed. This also holds the order of the properties
     */
    private final LinkedHashMap<String, DetailedProperties> propertyBuckets = new LinkedHashMap<>();

    /**
     * A cached version for external purposes
     */
    private final LinkedHashMap<String, Properties> cachedPropertyBuckets = new LinkedHashMap<>();

    /**
     * The start time of the test. It will be set when the singleton instance is created.
     * This is reset when you call reset during testing.
     */
    private long startTime = -1L;

    /**
     * The XLT version string.
     */
    private String version;

    // ********************************************************************
    /**
     * Global flag that controls whether or not additional request information should be collected and dumped to CSV.
     */
    private final boolean collectAdditonalRequestData;

    /**
     * Global flag that controls whether or not the IP address used for a request should be collected and dumped to CSV.
     */
    private final boolean collectUsedIpAddress;

    /**
     * Global flag that controls whether or not to remove user-info from request URLs.
     */
    private final boolean removeUserInfoFromRequestUrl;

    /**
     * Whether or not XLT is running in "dev mode".
     */
    private final boolean devMode;

    /**
     * Creates a new instance of XltPropertiesImpl using the testsuite's home and configuration directories as currently
     * set at XltExecutionContext. This method uses a thread-local gate to detect and avoid recursive attempts to create
     * an instance.
     *
     * @param ignoreMissingIncludes
     *            whether or not missing include property files should be ignored
     * @param staySilent
     *            shall we complain about missing things or stay silent, useful for startup
     *
     * @return new instance of XltPropertiesImpl or {@code null} if this method is called recursively
     */
    public static XltPropertiesImpl createInstance(final boolean ignoreMissing, boolean staySilent)
    {
        return new XltPropertiesImpl(null, null, ignoreMissing, staySilent);
    }

    /**
     * Returns the one and only XltProperties instance.
     *
     * @return the XltProperties singleton
     */
    public static XltPropertiesImpl getInstance()
    {
        return XltEngine.get().xltProperties;
    }

    /**
     * Creates an XltProperties instance using the given parameters. It is absolutely legal to create
     * your very own instance if needed.
     *
     * @param homeDirectory
     *            the home directory
     * @param configDirectory
     *            the configuration directory
     * @param ignoreMissingIncludes
     *            whether to ignore any missing property file include
     * @param staySilent
     *            shall we complain about missing things or stay silent, useful for startup
     */
    public XltPropertiesImpl(final FileObject homeDirectory, final FileObject configDirectory,
                             final boolean ignoreMissingIncludes, final boolean staySilent)
    {
        this(homeDirectory, configDirectory,
             (System.getenv("XLT_HOME") == null && System.getProperty(XltConstants.XLT_PACKAGE_PATH + ".home") == null),
             ignoreMissingIncludes, staySilent);
    }

    /**
     * Creates an XltProperties instance using the given parameters. It is absolutely legal to create
     * your very own instance if needed.
     *
     * @param homeDirectory
     *            the home directory
     * @param configDirectory
     *            the configuration directory
     * @param devMode
     *          true if we are running in dev mode, mainly needed when we bring up the properties
     *          as a standalone instance
     * @param ignoreMissingIncludes
     *            whether to ignore any missing property file include
     * @param staySilent
     *            shall we complain about missing things or stay silent, useful for startup
     */
    public XltPropertiesImpl(final FileObject homeDirectory, final FileObject configDirectory,
                             final boolean devMode, final boolean ignoreMissingIncludes, final boolean staySilent)
    {
        this.devMode = devMode;

        initialize(homeDirectory, configDirectory, ignoreMissingIncludes, staySilent);

        // we work with fake data here to avoid pulling up SessionImpl
        this.collectAdditonalRequestData = getProperty("XltPropertiesImpl", "XLTNoSuchUser-00000", XltConstants.PROP_COLLECT_ADDITIONAL_REQUEST_DATA).map(Boolean::valueOf).orElse(false);
        this.collectUsedIpAddress = getProperty("XltPropertiesImpl", "XLTNoSuchUser-00000", XltConstants.PROP_COLLECT_USED_IP_ADDRESS).map(Boolean::valueOf).orElse(false);
        this.removeUserInfoFromRequestUrl = getProperty("XltPropertiesImpl", "XLTNoSuchUser-00000", XltConstants.PROP_REMOVE_USERINFO_FROM_REQUEST_URL).map(Boolean::valueOf).orElse(true);

    }

    /**
     * Creates an empty XltProperties. This is useful for testing as well as when we don't want to load anything but need
     * the logic of the property lookup. Attention: This does not provide any bucket data or source data.
     */
    public XltPropertiesImpl()
    {
        this(Optional.empty());
    }

    /**
     * Creates an empty XltProperties. This is useful for testing as well as when we don't want to load anything but need
     * the logic of the property lookup. Attention: This does not provide any bucket data or source data.
     *
     * @param properties start a new instance with this set of properties
     */
    public XltPropertiesImpl(final Properties properties)
    {
        this(Optional.ofNullable(properties));
    }

    /**
     * Creates an empty XltProperties. This is useful for testing as well as when we don't want to load anything but need
     * the logic of the property lookup. Attention: This does not provide any bucket data or source data.
     *
     * @param properties start a new instance with this set of properties as optional for more flexible handling
     */
    public XltPropertiesImpl(final Optional<Properties> properties)
    {
        // get version and start time
        this.devMode = (System.getenv("XLT_HOME") == null && System.getProperty(XltConstants.XLT_PACKAGE_PATH + ".home") == null);
        this.version = ProductInformation.getProductInformation().getVersion();
        this.startTime = GlobalClock.millis();

        this.mergedProperties.putAll(properties.orElse(new Properties()));

        this.collectAdditonalRequestData = false;
        this.collectUsedIpAddress = false;
        this.removeUserInfoFromRequestUrl = true;
    }

    /**
     * Initializes the instance from scratch, ensure that only one is doing it. That should already been taken care
     * of higher in the chain, just for safety. Won't do a thing if this is the same thread.
     */
    private synchronized void initialize(final FileObject homeDirectory, final FileObject configDirectory, boolean ignoreMissingIncludes, boolean staySilent)
    {
        clear();

        // load the properties from the statically configured property files
        var hd = homeDirectory == null ? XltExecutionContext.getCurrent().getTestSuiteHomeDir() : homeDirectory;
        var cd = configDirectory == null ? XltExecutionContext.getCurrent().getTestSuiteConfigDir() : configDirectory;
        loadProperties(hd, cd, ignoreMissingIncludes, staySilent);

        // get version and start time
        version = ProductInformation.getProductInformation().getVersion();
        startTime = GlobalClock.millis();
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
     * If in development mode, dev.properties is loaded. If secret.properties exists, it is loaded last and its property keys
     * are transformed from key to secret.key if they are not yet named secret.
     * <p>
     * Finally, the Java system properties are loaded because they are the last instances where some property value can
     * be overridden.
     * </p>
     *
     * @param homeDirectory where is our home
     * @param configDirectory what is the config dir
     * @param ignoreMissingIncludes shall we ignore missing includes
     * @param staySilent shall we complain about missing things or stay silent, useful for startup
     */
    private void loadProperties(final FileObject homeDirectory, final FileObject configDirectory, boolean ignoreMissingIncludes, boolean staySilent)
    {
        /*
         * Load default.properties and project, this is not longer optional
         */
        process(homeDirectory, configDirectory, XltConstants.DEFAULT_PROPERTY_FILENAME, XltProperties.DEFAULT_PROPERTIES, true, ignoreMissingIncludes, s -> s);
        process(homeDirectory, configDirectory, XltConstants.PROJECT_PROPERTY_FILENAME, XltProperties.PROJECT_PROPERTIES, true, ignoreMissingIncludes, s -> s);

        // apply system props temporary here in case someone has the test props defined there
        final Properties temp = new Properties();
        temp.putAll(this.mergedProperties);
        temp.putAll(System.getProperties());

        // get the test properties file (use properties directly to break loop between SessionImpl and XltProperties)
        final String testPropertiesFile = temp.getProperty(XltConstants.TEST_PROPERTIES_FILE_PATH_PROPERTY);

        // Collects additional (&quot;test.properties&quot; and &quot;dev.properties&quot; if in
        // development mode) property files. We first have to collect the properties to be read earlier as someone might
        // have reconfigured the name for the expected &quot;test.properties&quot; in the properties. And the
        // &quot;dev.properties&quot; has to be read as last as the order is important or it must not be read in at all.
        if (testPropertiesFile != null)
        {
            // if we specified it, we have to be able to load it !!!
            process(homeDirectory, configDirectory, testPropertiesFile, XltProperties.TEST_PROPERTIES, false, ignoreMissingIncludes, s -> s);
        }
        else
        {
            // warn at least, if wanted
            if (!staySilent)
            {
                XltLogger.runTimeLogger.warn("No test property file was referenced.", XltConstants.TEST_PROPERTIES_FILE_PATH_PROPERTY);
            }
        }

        // guess whether we are in development mode, try to load it when in dev mode, loading is optional
        if (this.devMode)
        {
            process(homeDirectory, configDirectory, XltConstants.DEV_PROPERTY_FILENAME, XltProperties.DEVELOPMENT_PROPERTIES, true,ignoreMissingIncludes,  s -> s);
        }

        // ok, finally put the secrets into the mix

        // load the secrets and any includes they might have, secrets get a key transformation
        process(homeDirectory, configDirectory, XltConstants.SECRET_PROPERTIES_FILENAME, XltProperties.SECRET_PROPERTIES, true, ignoreMissingIncludes, s ->
        {
            return s.startsWith(XltConstants.SECRET_PREFIX) ?  s : XltConstants.SECRET_PREFIX + s;
        });

        // system properties always overwrite properties from files
        this.mergedProperties.putAll(System.getProperties());
        this.cachedPropertyBuckets.put(XltProperties.SYSTEM_PROPERTIES, System.getProperties());

        // finally log the properties
        if (XltLogger.runTimeLogger.isTraceEnabled())
        {
            XltLogger.runTimeLogger.trace("--- >>> Final Properties ---------------------------------------------");
            dumpAllProperties().forEach(s -> XltLogger.runTimeLogger.trace("| " + s));
            XltLogger.runTimeLogger.trace("--- <<< --------------------------------------------------------------");
        }
    }

    /**
     * The handling of a single property load, includes will be handled automatically. This fully
     * handles the loading including and update of the merged properties. Hence after this call, the merged properties will have
     * mostly a new state.
     *
     * @param homeDirectory the context in which we can read files without complaining
     * @param configDirectory where is the file located
     * @param fileName what is the file we want to load from, this is relative to config
     * @param bucketName for keeping the data by file name sorted for later sharing with others for more dedicated processing
     * @param ignoreMissing shall we just continue when a file is missing?
     * @param ignoreMissingIncludes shall we be forgiving when an incldue is missing?
     * @param keyTransformer a function applied to the later loading to be able to modify keys of properties if needed
     */
    private void process(final FileObject homeDirectory, final FileObject configDirectory,
                         final String fileName, final String bucketName,
                         boolean ignoreMissing,
                         boolean ignoreMissingIncludes,
                         final Function<String, String> keyTransformer)
    {
        // get us the file
        final Optional<PropertyInclude> propFile = getFile(configDirectory, fileName);

        if (propFile.isEmpty() && !ignoreMissing)
        {
            throw new PropertyFileNotFoundException(String.format("Unable to locate property file %s.", fileName));
        }

        // yeah, we have to deal with the exception somehow
        boolean exists = false;
        try
        {
            exists = propFile.get().file.exists();
        }
        catch (FileSystemException e1)
        {
        }

        if (!exists && !ignoreMissing)
        {
            throw new PropertyFileNotFoundException(String.format("Property file %s does not exist",
                                                                  makeRelativeTo(propFile.get().file, homeDirectory, fileName)));
        }
        if (!exists && ignoreMissing)
        {
            // not there and we don't care
            return;
        }

        // load all properties
        if (XltLogger.runTimeLogger.isDebugEnabled())
        {
            XltLogger.runTimeLogger.debug(String.format("Trying to evaluate property file: %s ...", propFile.get().file));
        }

        // resolve includes but don't load them, just check
        List<PropertyIncludeResult> includeResult = PropertyIncludeResolver.resolve(homeDirectory, configDirectory, List.of(propFile.get()));

        // warn or fail, filter out what we don't want
        includeResult = verifyFiles(includeResult, ignoreMissing, ignoreMissingIncludes);

        // load all properties
        Optional<String> firstName = Optional.empty();
        final VarSubstitutionSupportedProperties newProperties = new VarSubstitutionSupportedProperties();

        // collect all in order and put them into the new props
        for (PropertyIncludeResult include : includeResult)
        {
            // load all properties
            if (XltLogger.runTimeLogger.isDebugEnabled())
            {
                var msg = firstName.isEmpty() ?
                                               String.format("Loading from property file: %s ...", include.name) :
                                                   String.format("Loading from include file %s of %s ...", include.name, firstName.get());

                XltLogger.runTimeLogger.debug(msg);

                // update main name if empty
                firstName = firstName.or(() -> Optional.of(include.name));
            }

            try
            {
                // load
                final Properties p = PropertiesUtils.loadProperties(include.file);
                newProperties.putAll(p, keyTransformer);
            }
            catch (IOException e)
            {
                XltLogger.runTimeLogger.error(String.format("Issues loading properties from %s", fileName), e);

                throw new PropertiesIOException(String.format("Issues loading properties from %s", fileName));
            }
        }

        // add to buckets
        this.propertyBuckets.put(bucketName, new DetailedProperties(fileName, newProperties, includeResult));
        this.cachedPropertyBuckets.put(bucketName, newProperties);

        if (XltLogger.runTimeLogger.isTraceEnabled())
        {
            var l = fileName + " ------------------------------------------------";

            XltLogger.runTimeLogger.trace("--- >>> " + l);
            dumpProperties(newProperties).forEach(s -> XltLogger.runTimeLogger.trace("| " + s));
            XltLogger.runTimeLogger.trace("--- <<< " + Stream.generate(() -> "-").limit(l.length()).collect(Collectors.joining()));
        }

        // update the mergeProperties
        mergedProperties.putAll(newProperties);
    }

    /**
     * Just check that the files have the right state and break if needed. Will filter out everything not correct, so
     * we can rely later on a clean list.
     *
     * @param files a list of loaded files
     * @param ignoreMissing shall we ignore files that are not available, ignoreMissing is a general handle for all missing files, if this is false, we can still ignore missing includes
     * @param ignoreMissingIncludes shall we ignore missing includes?
     *
     * @return an updated list
     *
     * @exception will raise {@link PropertiesConfigurationException} if something is wrong, will also write an error log entry
     */
    private List<PropertyIncludeResult> verifyFiles(final List<PropertyIncludeResult> files, final boolean ignoreMissing, final boolean ignoreMissingIncludes)
    {
        // clean new list
        final List<PropertyIncludeResult> newFiles = new ArrayList<>();

        for (final PropertyIncludeResult file : files)
        {
            if (file.outsideORootDirScope)
            {
                var msg = String.format("File %s is outside of the permitted scope. This error cannot be ignored.", file.name);
                XltLogger.runTimeLogger.error(msg);

                // abort
                throw new PropertiesConfigurationException(msg);
            }

            if (file.seenBefore)
            {
                if (ignoreMissingIncludes && file.isInclude)
                {
                    var msg = String.format("File %s has been seen multiple times when resolving properties, this can indicate a cyclic include pattern but also just be a repeated reference. Ignoring for the moment.", file.name);
                    XltLogger.runTimeLogger.warn(msg);

                    continue;
                }
                else
                {
                    var msg = String.format("File %s has been seen multiple times when resolving properties, this can indicate a cyclic include pattern but also just be a repeated reference.", file.name);
                    XltLogger.runTimeLogger.error(msg);

                    // abort
                    throw new PropertiesConfigurationException(msg);
                }
            }

            if (!file.exists)
            {
                if (file.isInclude && ignoreMissingIncludes)
                {
                    // ok, we don't want to keep the missing includes
                    var msg = String.format("Property include file %s does not exist. Ignoring.", file.name);
                    XltLogger.runTimeLogger.warn(msg);

                    continue;
                }

                if (ignoreMissing && !file.isInclude)
                {
                    // we don't care at all
                    var msg = String.format("Property file %s does not exist. Ignoring.", file.name);
                    XltLogger.runTimeLogger.warn(msg);

                    continue;
                }

                // ok, complain now!
                var msg = String.format("File %s does not exist", file.name);
                XltLogger.runTimeLogger.error(msg);

                // abort
                throw new PropertyFileNotFoundException(msg);
            }

            newFiles.add(file);
        }

        return newFiles;
    }

    /**
     * Resolves the file and returns the file object
     *
     * @param configDir
     *            the configuration directory to use
     * @param fileName
     *            the name of the file for which to create a file object
     *
     * @return resolved file object or empty optional
     */
    private Optional<PropertyInclude> getFile(final FileObject configDir, final String fileName)
    {
        try
        {
            final FileObject file = configDir.resolveFile(fileName);

            return Optional.of(new PropertyInclude(file, fileName));
        }
        catch (final FileSystemException fse)
        {
            XltLogger.runTimeLogger.error("Unable to read or open property file", fse);
        }

        return Optional.empty();
    }

    /**
     * Logs the properties as a sorted list. This is public to allow to log it when needed again and to
     * aid testing. Secret properties will be masked.
     *
     * @return returns the properties as formatted object of lines
     */
    public List<String> dumpAllProperties()
    {
        return dumpProperties(mergedProperties);
    }

    /**
     * Logs the properties as a sorted list. This is public to allow to log it when needed again and to
     * aid testing. Secret properties will be masked.
     *
     * @return a dump of the selected properties
     */
    protected List<String> dumpProperties(final Properties source)
    {
        final List<String> result = new ArrayList<>(500);
        for (final var entry : source.entrySet())
        {
            // mask anything that is a secret (source does not matter)
            final String k = (String) entry.getKey();
            final Object v = k.startsWith(XltConstants.SECRET_PREFIX) ?
                                                                       XltConstants.MASK_PROPERTIES_HIDETEXT : entry.getValue();

            result.add(k + " = " + v);
        }
        Collections.sort(result);

        return result;
    }

    private static String makeRelativeTo(final FileObject file, final FileObject targetDirectory, final String fallback)
    {
        try
        {
            final ArrayList<String> pathSegments = new ArrayList<>();

            FileObject tmp = targetDirectory;
            FileObject tmp2 = file;
            int depthResolved = file.getName().getDepth();
            int depthPivot = targetDirectory.getName().getDepth() + 1;
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
                                                                 file.getName().getPath(), targetDirectory.getName().getPath()));
            }
            pathSegments.add(file.getName().getBaseName());

            return pathSegments.stream().collect(Collectors.joining("/"));
        }
        catch (final Exception e)
        {
            XltLogger.runTimeLogger.warn(e.getMessage());
            return fallback;
        }
    }

    /**
     * Checks whether there is a mapping for the specified key in this property list.
     *
     * @param key
     *            the property key.
     *
     */
    @Override
    public boolean containsKey(final String key)
    {
        return mergedProperties.containsKey(XltConstants.SECRET_PREFIX + key) || mergedProperties.containsKey(key);
    }

    /**
     * Returns a copy of all the internally stored properties, with any placeholder resolved.
     *
     * @return the properties
     */
    @Override
    public final Properties getCopyOfProperties()
    {
        final Properties copy = new Properties();

        // resolve and copy all properties
        for (final Object k : mergedProperties.keySet())
        {
            final String key = (String) k;
            copy.setProperty(key, mergedProperties.getProperty(key));
        }

        return copy;
    }

    /**
     * Returns all properties. This instance of the properties is not protected against write access. Don't modify it!
     * It is here for speed in case you don't want to write to it instead of using {@link #getCopyOfProperties()}.
     *
     * @return the properties
     */
    @Override
    public Properties getProperties()
    {
        return mergedProperties;
    }

    /**
     * Convenience method. Calls {@link #getPropertiesForKey(String, Properties)} with the member properties of this
     * instance.
     *
     * @see #getPropertiesForKey(String, Properties)
     */
    @Override
    public Map<String, String> getPropertiesForKey(final String domainKey)
    {
        return PropertiesUtils.getPropertiesForKey(domainKey, mergedProperties);
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
     * @param session the session to get utility data from
     * @param bareKey
     *            the bare property key, i.e. without any prefixes
     * @return the first key that produces a result
     */
    @Override
    public String getEffectiveKey(final Session session, final String bareKey)
    {
        // if we have a session, user and class specific props may take precedence
        if (session != null)
        {
            return getEffectiveKey(session.getTestCaseClassName(), session.getUserName(), bareKey);
        }
        else
        {
            final String nonPrefixedKey = getNonPrefixedKey(bareKey);

            return getEffectiveKey_Step3(nonPrefixedKey, bareKey);
        }
    }

    private String getNonPrefixedKey(final String bareKey)
    {
        return bareKey.startsWith(XltConstants.SECRET_PREFIX) ? bareKey.substring(XltConstants.SECRET_PREFIX.length())
                                                              : bareKey;
    }

    /**
     * Internal version of {@link #getEffectiveKey(Session, String)} to avoid session usage. Comes in handy in some areas
     *
     * @param testCaseClassName the test class'es name
     * @param userName the session user name
     * @param bareKey the bare property key, i.e. without any prefixes

     * @return the first key that produces a result
     */
    @Override
    public String getEffectiveKey(final String testCaseClassName, final String userName, final String bareKey)
    {
        final String nonPrefixedKey = getNonPrefixedKey(bareKey);

        // 1.0 use the current user name as prefix for a secret property
        final String userNameQualifiedSecretKey = XltConstants.SECRET_PREFIX + userName + "." + nonPrefixedKey;
        if (mergedProperties.containsKey(userNameQualifiedSecretKey))
        {
            return userNameQualifiedSecretKey;
        }

        // 1.1 use the current user name as prefix
        final String userNameQualifiedKey = userName + "." + bareKey; // do not return public props if
        // the test case requested a
        // secret
        if (mergedProperties.containsKey(userNameQualifiedKey))
        {
            return userNameQualifiedKey;
        }

        // 2.0 use the current class name as prefix for a secret property
        final String classNameQualifiedSecretKey = XltConstants.SECRET_PREFIX + testCaseClassName + "." + nonPrefixedKey;
        if (mergedProperties.containsKey(classNameQualifiedSecretKey))
        {
            return classNameQualifiedSecretKey;
        }

        // 2.1 use the current class name as prefix
        final String classNameQualifiedKey = testCaseClassName + "." + bareKey; // do not return public
        // props if the test
        // case requested a
        // secret
        if (mergedProperties.containsKey(classNameQualifiedKey))
        {
            return classNameQualifiedKey;
        }

        // to avoid code duplication, we moved that into its own method
        return getEffectiveKey_Step3(nonPrefixedKey, bareKey);
    }

    /**
     * Part of the previous code, put here to make it reusable
     * @param nonPrefixedKey
     * @param bareKey
     * @return
     */
    private String getEffectiveKey_Step3(final String nonPrefixedKey, final String bareKey)
    {
        // 3.0. Check whether the given key is available as a secret property, in which case it takes precedence
        final String secretKey = XltConstants.SECRET_PREFIX + nonPrefixedKey;
        if (mergedProperties.containsKey(secretKey))
        {
            return secretKey;
        }

        // 3.1 use the bare key
        return bareKey;
    }

    /**
     * Searches for the property with the specified key in this property list. The method returns null if the property
     * is not found. This method might have its value for testing because it allows you to pass in a Session context
     * rather have an indirect look up.
     *
     * @param key
     *            the property key
     * @param session
     *            the session information to use to enhance the lookup
     * @return the value of the key
     */
    @Override
    public Optional<String> getProperty(final Session session, final String key)
    {
        return getProperty(session.getTestCaseClassName(), session.getUserName(), key);
    }

    /**
     * Looks up a key in the properties without a sessin context but still with paying
     * attention to secure keys.
     *
     * @param key
     *            the property key
     * @return the value of the key as optional or an empty optional otherwise
     */
    public Optional<String> getPropertySessionLess(final String key)
    {
        final String nonPrefixedKey = getNonPrefixedKey(key);
        final String finalKey = getEffectiveKey_Step3(nonPrefixedKey, key);

        return Optional.ofNullable(mergedProperties.getProperty(finalKey));
    }

    /**
     * Searches for the property with the specified key in this property list. The method returns null if the property
     * is not found. Will automatically pick a session context
     *
     * @param key
     *            the property key
     * @return the value of the key
     */
    @Override
    public String getProperty(final String key)
    {
        return getProperty(Session.getCurrent(), key).orElse(null);
    }

    /**
     * Internal: Searches for the property with the specified key in this property list. The method returns an optional
     * for easier handling of fallbacks.
     *
     * @param key
     *            the property key
     * @return returns an optional with the value when set, an empty optional otherwise
     */
    public Optional<String> getProperty(final String testCaseClassName, final String userName, final String key)
    {
        // get value of property and return it
        final String effectiveKey = getEffectiveKey(testCaseClassName, userName, key);

        return Optional.ofNullable(mergedProperties.getProperty(effectiveKey));
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public long getStartTime()
    {
        return startTime;
    }

    /**
     * Returns the product version.
     *
     * @return the version string, e.g. "1.1.0"
     */
    @Override
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
    @Override
    public void removeProperty(final String key)
    {
        ParameterCheckUtils.isNotNull(key, "key");

        mergedProperties.remove(key);
    }

    /**
     * Method for changing the properties during runtime. Can be called multiple times to add additional properties.
     *
     *
     * @param newProperties
     *            complete new set of properties, will be added to existing properties and overwrites already defined
     *            properties with new values. None existing properties will be added.
     */
    @Override
    public void setProperties(final Properties newProperties)
    {
        ParameterCheckUtils.isNotNull(newProperties, "newProperties");
        mergedProperties.putAll(newProperties);
    }

    /**
     * Sets a property during runtime. Overwrites an existing property with the same name. Does not re-apply any java
     * system settings. This impacts all of the propert
     *
     * @param key
     *            new property key
     * @param value
     *            new property value
     */
    @Override
    public void setProperty(final String key, final String value)
    {
        ParameterCheckUtils.isNotNull(key, "key");
        ParameterCheckUtils.isNotNull(value, "value");

        mergedProperties.setProperty(key, value);
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
            return val == null ? null : PropertiesUtils.substituteVariables(val, this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object put(final Object key, final Object value)
        {
            // this does not require a synchronized anymore, because Properties uses a ConcurrentHashMap
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
        public void putAll(final Map<? extends Object, ? extends Object> map)
        {
            // #3031: make sure our put() method is used when someone calls putAll()
            // this does not require a synchronized anymore, because Properties uses a ConcurrentHashMap
            for (final Entry<?, ?> entry : map.entrySet())
            {
                put(entry.getKey(), entry.getValue());
            }
        }

        /**
         * Allows to apply a transformation of the key before storing. It is important that the key is a String, otherwise
         * we will fail.
         */
        public void putAll(final Map<? extends Object, ? extends Object> map, final Function<String, String> keyTransformer)
        {
            // #3031: make sure our put() method is used when someone calls putAll()
            // this does not require a synchronized anymore, because Properties uses a ConcurrentHashMap
            for (final Entry<?, ?> entry : map.entrySet())
            {
                put(keyTransformer.apply((String) entry.getKey()), entry.getValue());
            }
        }
    }

    /**
     * Holder for details of file, name and properties
     */
    public static class DetailedProperties
    {
        /**
         * Relative name to the config directory
         */
        public final String relativeName;
        public final Properties properties;
        public final List<PropertyIncludeResult> propertyChain;

        public DetailedProperties(final String relativeName, final Properties properties, final List<PropertyIncludeResult> propertyChain)
        {
            this.relativeName = relativeName;
            this.properties = properties;
            this.propertyChain = propertyChain;
        }
    }

    @Override
    public LinkedHashMap<String, Properties> getPropertyBuckets()
    {
        return cachedPropertyBuckets;
    }

    public FileObject getTestPropertyFile()
    {
        return this.propertyBuckets.get(XltProperties.TEST_PROPERTIES).propertyChain.get(0).file;
    }

    /**
     * Empties the properties! Might never be needed except for testing.
     */
    @Override
    public synchronized XltProperties clear()
    {
        mergedProperties.clear();
        cachedPropertyBuckets.clear();
        propertyBuckets.clear();

        return this;
    }

    /**
     * Returns the absolute paths of the resolved property files. This means the property files which are there by
     * default and the property files transitively included by &quot;includes&quot; in these property files. However
     * note that some of the default files are optional (as &quot;dev.properties&quot;) and the returned list only
     * contains existing files. For internal use only!
     *
     * @return the resolved property files as described above as absolute file object
     */
    public List<FileObject> getUsedPropertyFiles()
    {
        final List<FileObject> r = new ArrayList<>();
        propertyBuckets.values().forEach(p ->
        {
            p.propertyChain.forEach(i -> r.add(i.file));
        });

        return r;
    }

    /**
     * Returns the relative paths of the resolved property files. This means the property files which are there by
     * default and the property files transitively included by &quot;includes&quot; in these property files. However
     * note that some of the default files are optional (as &quot;dev.properties&quot;) and the returned list only
     * contains existing files. For internal use only!
     *
     * The relative path is meant to  be relative to config as base.
     *
     * @return the resolved property files as relative name
     */
    public List<String> getUsedPropertyFilesByRelativeName()
    {
        final List<String> r = new ArrayList<>();
        propertyBuckets.values().forEach(p ->
        {
            p.propertyChain.forEach(i -> r.add(i.name));
        });

        return r;
    }

    /**
     * Do we run in dev mode such as Maven or Eclipse or similar?
     *
     * @return true if this instance is running a dev mode, false otherwise
     */
    @Override
    public boolean isDevMode()
    {
        return devMode;
    }

    /**
     * Do we run in load test mode?
     *
     * @return true if this instance is running a load test aka this is executed by an agent
     */
    @Override
    public boolean isLoadTest()
    {
        return !devMode;
    }

    public boolean collectAdditonalRequestData()
    {
        return XltPropertiesImpl.getInstance().collectAdditonalRequestData;
    }

    public boolean collectUsedIpAddress()
    {
        return XltPropertiesImpl.getInstance().collectUsedIpAddress;
    }

    public boolean removeUserInfoFromRequestUrl()
    {
        return XltPropertiesImpl.getInstance().removeUserInfoFromRequestUrl;
    }
}
