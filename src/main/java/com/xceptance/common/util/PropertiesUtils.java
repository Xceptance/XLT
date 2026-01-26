/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;

/**
 * The PropertiesUtils helps in dealing with properties files.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class PropertiesUtils
{
    /**
     * Default constructor. Declared private to prevent external instantiation.
     */
    private PropertiesUtils()
    {
    }

    /**
     * Start delimiter for variable name.
     */
    private static final String DELIMITER_START = "${";

    /**
     * End delimiter for variable name.
     */
    private static final String DELIMITER_STOP = "}";

    /**
     * Loads the properties from the given file and returns them as a Properties object.
     *
     * @param file
     *                 the properties file
     * @return the resulting Properties object
     * @throws IOException
     *                         if an I/O error occurs
     */
    public static Properties loadProperties(final File file) throws IOException
    {
        ParameterCheckUtils.isReadableFile(file, "file");

        return loadProperties(VFS.getManager().toFileObject(file));
    }

    /**
     * Loads the properties from the given file and puts them into the specified Properties object.
     *
     * @param file
     *                  the properties file
     * @param props
     *                  the properties object to load the properties into
     * @throws IOException
     *                         if an I/O error occurs
     */
    public static void loadProperties(final File file, final Properties props) throws IOException
    {
        ParameterCheckUtils.isReadableFile(file, "file");
        ParameterCheckUtils.isNotNull(props, "props");

        loadProperties(VFS.getManager().toFileObject(file), props);
    }

    /**
     * Loads the properties from the given file and returns them as a Properties object.
     *
     * @param file
     *                 the properties file
     * @return the resulting Properties object
     * @throws IOException
     *                         if an I/O error occurs
     */
    public static Properties loadProperties(final FileObject file) throws IOException
    {
        final Properties props = new Properties();

        loadProperties(file, props);

        return props;
    }

    /**
     * Loads the properties from the given file and puts them into the specified Properties object.
     *
     * @param file
     *                  the properties file
     * @param props
     *                  the properties object to load the properties into
     * @throws IOException
     *                         if an I/O error occurs
     */
    public static void loadProperties(final FileObject file, final Properties props) throws IOException
    {
        try
        {
            ParameterCheckUtils.isReadableFile(file, "file");
        }
        catch (IllegalArgumentException e)
        {
            throw new FileNotFoundException(file.toString());
        }
        ParameterCheckUtils.isNotNull(props, "props");

        try (final InputStream is = file.getContent().getInputStream())
        {
            props.load(is);
        }
    }

    /**
     * Perform variable substitution in string <code>value</code> from the values of keys found in the system properties.
     * <p>
     * The variable substitution delimiters are <b>${</b> and <b>}</b>.
     * <p>
     * For example, if the System properties contains "key=value", then the call
     *
     * <pre>
     * String s = OptionConverter.substituteVars(&quot;Value of key is ${key}.&quot;);
     * </pre>
     *
     * will set the variable <code>s</code> to "Value of key is value.".
     * <p>
     * If no value could be found for the specified key, then the <code>props</code> parameter is searched, if the value
     * could not be found there, then substitution defaults to the empty string.
     * <p>
     * For example, if system properties contains no value for the key "inexistentKey", then the call
     *
     * <pre>
     * String s = OptionConverter.subsVars(&quot;Value of nonexistentKey is [${nonexistentKey}]&quot;);
     * </pre>
     *
     * will set <code>s</code> to "Value of nonexistentKey is []"
     * <p>
     * Additionally, Groovy expressions in the format <b>#{...}</b> are evaluated after variable substitution. Multi-line
     * scripts are supported.
     * </p>
     * <p>
     * An {@link java.lang.IllegalArgumentException} is thrown if <code>value</code> contains a start delimiter "${" which
     * is not balanced by a stop delimiter "}".
     * </p>
     *
     * @param value
     *                       the string on which variable substitution is performed
     * @param properties
     *                       properties object to be used for variable lookup
     * @return argument string where variable have been substituted
     * @throws IllegalArgumentException
     *                                      if <code>value</code> is malformed
     */
    public static String substituteVariables(final String value, final Properties properties) throws IllegalArgumentException
    {
        return substituteVariables(value, properties, null);
    }

    /**
     * Perform variable substitution in string <code>value</code> from the values of keys found in the system properties,
     * with support for Groovy expressions and shared context.
     * <p>
     * Variable substitution uses <b>${...}</b> syntax. Groovy expressions use <b>#{...}</b> syntax and are evaluated after
     * variable substitution. Groovy scripts can access:
     * <ul>
     * <li><code>props</code> - read-only access to property values</li>
     * <li><code>ctx</code> - shared Map for storing data between script evaluations</li>
     * </ul>
     * </p>
     *
     * @param value
     *                       the string on which variable substitution is performed
     * @param properties
     *                       properties object to be used for variable lookup
     * @param ctx
     *                       shared context map for Groovy scripts (may be null, in which case an empty map is used)
     * @return argument string where variables and Groovy expressions have been substituted
     * @throws IllegalArgumentException
     *                                      if <code>value</code> is malformed or Groovy evaluation fails
     * @since 8.0.0
     */
    public static String substituteVariables(final String value, final Properties properties, final Map<String, Object> ctx)
        throws IllegalArgumentException
    {
        // parameter validation
        ParameterCheckUtils.isNotNull(value, "value");
        ParameterCheckUtils.isNotNull(properties, "props");

        if (value.isEmpty())
        {
            return value;
        }

        // Step 1: Resolve ${...} variable references
        String result = value;
        if (properties.size() > 0)
        {
            result = resolveVariables(value, properties, new HashSet<String>());
        }

        // Step 2: Evaluate #{...} Groovy expressions
        if (result.contains("#{"))
        {
            final Map<String, Object> contextMap = ctx != null ? ctx : new java.util.concurrent.ConcurrentHashMap<>();
            result = GroovyPropertyEvaluator.evaluateGroovyExpressions(result, properties, contextMap);
        }

        return result;
    }

    /**
     * Resolves the given value as variable reference using the given properties object and set of already known variables.
     *
     * @param value
     *                      variable reference to be resolved
     * @param props
     *                      properties object to be used to resolve the variable reference
     * @param variables
     *                      set of already known variables in current lookup path
     * @return resolved variable reference
     */
    private static String resolveVariables(final String value, final Properties props, final Set<String> variables)
    {
        String result = value;

        // list of all variable reference matches
        final List<String> matches = RegExUtils.getAllMatches(value, RegExUtils.escape(DELIMITER_START) + "(.*?)" +
                                                                     RegExUtils.escape(DELIMITER_STOP),
                                                              1);

        // loop through variable references
        for (final String key : matches)
        {
            if (variables.contains(key))
            {
                continue;
            }

            // add found variable to set of known variables in order to
            // prevent cyclic lookup paths
            variables.add(key);

            // resolve variable
            // 1st: Try to get value using system properties
            String substitution = System.getProperty(key, null);
            if (substitution == null)
            {
                // 2nd: Try to get value using passed properties
                substitution = props.getProperty(key);
                if (substitution == null)
                {
                    // 3rd: Try to get value using system environment
                    substitution = System.getenv(key);
                }
            }

            if (substitution != null)
            {
                // recursively replace any variable in the substitution string
                substitution = resolveVariables(substitution, props, new HashSet<String>(variables));

                // replace variable reference with its substitution value
                result = RegExUtils.replaceAll(result, RegExUtils.escape(DELIMITER_START + key + DELIMITER_STOP),
                                               Matcher.quoteReplacement(substitution));
            }
        }

        return result;
    }

    /**
     * Returns all properties for this domain key, strips the key from the property name, e.g. ClassName.Testproperty=ABC
     * --> TestProperty=ABC Attention: Properties without a domain (e.g. foobar=test) or domain only properties are invalid
     * and will be ignored. A property has to have at least this form: domain.propertyname=value
     *
     * @param domainKey
     *                       domain for the properties
     * @param properties
     *                       the properties from which to return the matching entries
     * @return map with all key value pairs of properties
     */
    public static Map<String, String> getPropertiesForKey(final String domainKey, final Properties properties)
    {
        // initialize map
        final Map<String, String> result = new HashMap<String, String>();

        // maybe we are finished yet
        if (domainKey == null || domainKey.isEmpty())
        {
            return result;
        }

        // assemble prefix: append a dot to the domainKey if it does not end
        // with a dot
        final String prefix = domainKey.endsWith(".") ? domainKey : domainKey + ".";

        // go through all property entries
        for (final Entry<Object, Object> entry : properties.entrySet())
        {
            // get property name
            final String fullKey = (String) entry.getKey();
            // get property value
            final String propVal = (String) entry.getValue();
            // check if there is something to do at all
            if (propVal == null || propVal.length() == 0)
            {
                continue;
            }

            // if property name starts with the prefix and if property name is
            // not only the prefix...
            if (fullKey.startsWith(prefix) && fullKey.length() > prefix.length())
            {
                // put its name remainder into the map
                result.put(fullKey.substring(prefix.length()), properties.getProperty(fullKey));
            }

        }

        return result;
    }
}
