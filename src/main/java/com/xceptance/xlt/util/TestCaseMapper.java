package com.xceptance.xlt.util;

import com.xceptance.common.lang.StringUtils;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.mastercontroller.TestCaseLoadProfileConfiguration;
import com.xceptance.xlt.mastercontroller.TestLoadProfileConfiguration;
import javassist.bytecode.ClassFile;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.reflections.util.QueryBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class for getting test cases without a configured test class from a load profile and scanning for matching
 * test classes in the working directory.
 */
public class TestCaseMapper
{
    /**
     * Subdirectories to scan for ".class" files. These must be consistent with the classpath used by the agent
     * controller.
     */
    private static final String[] CLASS_DIRECTORIES =
        {
            "patches/classes", //
            "classes", //
            "target/classes", //
            "target/test-classes", //
            "build/classes/java/main", //
            "build/classes/kotlin/main", //
            "build/resources/main", //
            "build/classes/java/test", //
            "build/classes/kotlin/test", //
            "build/resources/test", //
            "bin" //
        };

    /**
     * Subdirectories to scan for ".jar" files. These must be consistent with the classpath used by the agent
     * controller.
     */
    private static final String[] JAR_DIRECTORIES =
        {
            "patches/lib", //
            "lib", //
            "target/dependency", //
            "build/dependency" //
        };

    private final Set<String> unmappedTestCaseNames;

    /**
     * Create a new TestCaseMapper object.
     *
     * @param loadProfile
     *            the load profile configuration that might contain unmapped test cases
     */
    public TestCaseMapper(final TestLoadProfileConfiguration loadProfile)
    {
        this.unmappedTestCaseNames = new HashSet<>();
        for (final TestCaseLoadProfileConfiguration config : loadProfile.getLoadTestConfiguration())
        {
            if (config.getTestCaseClassName() == null || config.getTestCaseClassName().isBlank())
            {
                this.unmappedTestCaseNames.add(config.getUserName());
            }
        }
    }

    /**
     * Get the names of all test cases from the load profile that don't have a test class configured.
     *
     * @return the names of all test cases without a configured test class
     */
    public Set<String> getUnmappedTestCaseNames()
    {
        return unmappedTestCaseNames;
    }

    /**
     * Scan the given working directory for classes whose simple class name matches the unmapped test case names.
     *
     * @param workDir
     *            the working directory to scan
     * @return a map of all unmapped test cases and the matching test classes in the working directory; if there are no
     *         unmapped test cases to begin with, an empty map is returned
     * @throws XltException
     *             if no or multiple matching test classes were found for any unmapped test case
     */
    public Map<String, String> scanForTestCaseClassMappings(final File workDir) throws XltException
    {
        final Map<String, String> testCaseClassNameMappings = new HashMap<>();

        if (!unmappedTestCaseNames.isEmpty())
        {
            final Set<String> matchingTestClassNames = scanForTestClasses(workDir);

            for (final String testCaseName : unmappedTestCaseNames)
            {
                testCaseClassNameMappings.put(testCaseName, getSingleMatchingTestClassName(testCaseName, matchingTestClassNames));
            }
        }

        return testCaseClassNameMappings;
    }

    /**
     * Scan the working directory to find all classes matching any of the unmapped test case names.
     *
     * @param workDir
     *            the working directory
     * @return the set with all matching class names
     */
    Set<String> scanForTestClasses(final File workDir)
    {
        final AllClassesScanner scanner = new AllClassesScanner();
        final Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(getUrlsToScan(workDir)).setScanners(scanner)
                                                                                  .setExpandSuperTypes(false)
                                                                                  .filterInputsBy(new FilterBuilder().includePattern(getTestClassesFilePattern(unmappedTestCaseNames))));

        /*
         * the reflections object stores the found classes mapped to their parent class. For the AllClassesScanner, the
         * parent is always "Object.class"
         */
        return reflections.get(scanner.of(Object.class));
    }

    /**
     * Get the URLs for all subdirectories and JAR files in the given working directory to scan for test classes.
     *
     * @param workDir
     *            the working directory
     * @return the set of URLs to scan
     */
    static Set<URL> getUrlsToScan(final File workDir) throws XltException
    {
        final Set<File> filesToScan = new HashSet<>();

        for (final String directoryPath : CLASS_DIRECTORIES)
        {
            final File dir = new File(workDir, directoryPath);

            if (dir.isDirectory())
            {
                filesToScan.add(dir);
            }
        }

        for (final String directoryPath : JAR_DIRECTORIES)
        {
            final File dir = new File(workDir, directoryPath);

            if (dir.isDirectory())
            {
                // add all ".jar" or ".JAR" files located directly in the directory
                for (final File file : dir.listFiles())
                {
                    if (file.getName().endsWith(".jar") || file.getName().endsWith(".JAR"))
                    {
                        filesToScan.add(file);
                    }
                }
            }
        }

        if (filesToScan.isEmpty())
        {
            throw new XltException("Auto-mapping test cases failed because no valid test class directories were found in '" + workDir +
                                   "'.");
        }

        return filesToScan.stream().map(f -> {
            try
            {
                return f.toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                throw new XltException("Failed to convert file '" + f + "' to URL.", e);
            }
        }).collect(Collectors.toSet());
    }

    /**
     * Check if the given set of test class names contains exactly one class name that matches the given test case name;
     * if so, return that class name.
     *
     * @param testCaseName
     *            the test case name
     * @param testClassNames
     *            the set of test class names
     * @return the single test class name matching the test case name
     * @throws XltException
     *             if no or multiple class names in the set match the given test case name
     */
    private String getSingleMatchingTestClassName(final String testCaseName, final Set<String> testClassNames) throws XltException
    {
        final Pattern testClassNamePattern = Pattern.compile(getTestClassNamePattern(testCaseName));
        final List<String> matchingClassNames = testClassNames.stream().filter(testClassNamePattern.asPredicate())
                                                              .collect(Collectors.toList());

        if (matchingClassNames.isEmpty())
        {
            throw new XltException("Auto-mapping test cases failed because no matching test class was found for test case '" +
                                   testCaseName + "'.");
        }

        if (matchingClassNames.size() > 1)
        {
            throw new XltException("Auto-mapping test cases failed because multiple matching test classes were found for test case '" +
                                   testCaseName + "': [ " + StringUtils.join(", ", matchingClassNames, 5, ", ...") + " ].");
        }

        return matchingClassNames.get(0);
    }

    /**
     * Get the RegEx pattern for class files that match any of the given test case names.
     *
     * @param testCaseNames
     *            the test case names
     * @return the RegEx pattern
     */
    static String getTestClassesFilePattern(final Set<String> testCaseNames)
    {
        final Set<String> escapedTestCaseNames = testCaseNames.stream().map(Pattern::quote).collect(Collectors.toSet());
        return "(^|(.*(\\/|\\$)))(" + String.join("|", escapedTestCaseNames) + ")\\.class$";
    }

    /**
     * Get the RegEx pattern for class names that match the given test case name.
     *
     * @param testCaseName
     *            the test case name
     * @return the RegEx pattern
     */
    static String getTestClassNamePattern(final String testCaseName)
    {
        return "(^|(.*(\\.|\\$)))" + Pattern.quote(testCaseName) + "$";
    }

    /**
     * Implementation of {@link Scanner} to scan for all classes in a directory.
     */
    private class AllClassesScanner implements Scanner, QueryBuilder
    {
        @Override
        public List<Map.Entry<String, String>> scan(ClassFile classFile)
        {
            // classes are stored with a mapping to their parent classes using the Reflections library
            // since we want all classes regardless of parent we set the parent to "Object.class"
            return List.of(entry(Object.class.getName(), classFile.getName()));
        }

        @Override
        public String index()
        {
            return getClass().getSimpleName();
        }
    }
}
