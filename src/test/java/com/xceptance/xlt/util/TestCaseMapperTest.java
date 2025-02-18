package com.xceptance.xlt.util;

import com.xceptance.common.util.zip.ZipUtils;
import com.xceptance.xlt.api.util.XltException;
import com.xceptance.xlt.mastercontroller.TestCaseLoadProfileConfiguration;
import com.xceptance.xlt.mastercontroller.TestLoadProfileConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestCaseMapperTest
{
    private static final String TEST_CASE_TBROWSE = "TBrowse";

    private static final String TEST_CASE_TORDER = "TOrder";

    private static final String TEST_CASE_TVISIT = "TVisit";

    private static final String TEST_CASE_TSEARCH = "TSearch";

    private static final String TEST_CASE_OTHER = "TOther";

    private static final String CLASS_TBROWSE = "com.xceptance.TBrowse";

    private static final String CLASS_TORDER = "com.xceptance.TOrder";

    private static final String CLASS_TORDER_DUPLICATE = "com.xceptance.test.TOrder";

    private static final String CLASS_TVISIT = "com.xceptance.test.TVisit";

    private static final String CLASS_TVISIT_NESTED = "com.xceptance.test.ParentClass$TVisit";

    private static final String CLASS_TSEARCH = "com.xceptance.test.search.TSearch";

    private static final String CLASS_OTHER = "com.xceptance.test.OtherClass";

    private static final String CLASS_IGNORE = "com.xceptance.ClassToIgnore";

    private static final File TEST_PROJECT_ZIP = new File(TestCaseMapperTest.class.getResource("/testCaseMappingTestFiles.zip").getFile());

    /**
     * Class names from the test project zip file that match test case "TBrowse"
     */
    private static final Set<String> TEST_PROJECT_MATCHES_TBROWSE = Set.of(
                                                                           // test classes in directory
                                                                           // "/target/classes/"
                                                                           "TBrowse", "com.xceptance.TBrowse",
                                                                           "com.xceptance.packagedoesnotmatchdirectory.TBrowse",
                                                                           "com.xceptance.inheritance.TBrowse",
                                                                           "com.xceptance.interfaces.testclassisimplementation.TBrowse",
                                                                           "com.xceptance.interfaces.testclassisinterface.TBrowse",
                                                                           "com.xceptance.nestedclasses.TBrowse",
                                                                           "com.xceptance.nestedclasses.ParentClass$TBrowse",
                                                                           // test classes in directory
                                                                           // "/build/classes/java/main/"
                                                                           "com.xceptance.otherclassdirectory.TBrowse",
                                                                           // test classes in a JAR in directory "/lib/"
                                                                           "com.xceptance.jarcontent.TBrowse",
                                                                           "com.xceptance.jarcontent.packagedoesnotmatchdirectory.TBrowse",
                                                                           "com.xceptance.jarcontent.inheritance.TBrowse",
                                                                           "com.xceptance.jarcontent.interfaces.testclassisimplementation.TBrowse",
                                                                           "com.xceptance.jarcontent.interfaces.testclassisinterface.TBrowse",
                                                                           "com.xceptance.jarcontent.nestedclasses.TBrowse",
                                                                           "com.xceptance.jarcontent.nestedclasses.ParentClass$TBrowse");

    /**
     * Class names from the test project zip file that match test case "TVisit"
     */
    private static final Set<String> TEST_PROJECT_MATCHES_TVISIT = Set.of(
                                                                          // test classes in directory
                                                                          // "/target/classes/"
                                                                          "com.xceptance.TVisit",
                                                                          "com.xceptance.nestedclasses.ParentClass$TVisit",
                                                                          // test classes in a JAR in directory "/lib/"
                                                                          "TVisit",
                                                                          "com.xceptance.jarcontent.nestedclasses.ParentClass$TVisit");

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File workDir;

    private TestLoadProfileConfiguration loadProfile;

    private List<TestCaseLoadProfileConfiguration> testCaseProfiles;

    private Set<String> testClassMatches;

    @Before
    public void init() throws IOException
    {
        workDir = tempFolder.newFolder("workDir");

        loadProfile = Mockito.mock(TestLoadProfileConfiguration.class);
        testCaseProfiles = new ArrayList<>();
        Mockito.doReturn(testCaseProfiles).when(loadProfile).getLoadTestConfiguration();

        testClassMatches = new HashSet<>();
    }

    @Test
    public void testGetUnmappedTestCaseNames_NoUnmappedTestCases()
    {
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TBROWSE, CLASS_TBROWSE));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TVISIT, CLASS_TVISIT_NESTED));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TSEARCH, CLASS_OTHER));

        final Set<String> unmappedTestCaseNames = new TestCaseMapper(loadProfile).getUnmappedTestCaseNames();

        Assert.assertNotNull(unmappedTestCaseNames);
        Assert.assertTrue(unmappedTestCaseNames.isEmpty());
    }

    @Test
    public void testGetUnmappedTestCaseNames_WithUnmappedTestCases()
    {
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TBROWSE, CLASS_OTHER));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TORDER, null));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TVISIT, ""));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TSEARCH, "   "));

        final Set<String> unmappedTestCaseNames = new TestCaseMapper(loadProfile).getUnmappedTestCaseNames();

        Assert.assertEquals(3, unmappedTestCaseNames.size());
        Assert.assertTrue(unmappedTestCaseNames.contains(TEST_CASE_TORDER));
        Assert.assertTrue(unmappedTestCaseNames.contains(TEST_CASE_TVISIT));
        Assert.assertTrue(unmappedTestCaseNames.contains(TEST_CASE_TSEARCH));
    }

    @Test
    public void testScanForMatchingTestCases_NoUnmappedTestClassesProvided()
    {
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TBROWSE, CLASS_TBROWSE));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TVISIT, CLASS_TVISIT_NESTED));

        final Map<String, String> mappings = getSpyTestCaseMapper().scanForTestCaseClassMappings(workDir);

        Assert.assertNotNull(mappings);
        Assert.assertTrue(mappings.isEmpty());
    }

    @Test
    public void testScanForTestCaseClassMappings_MappingsFound()
    {
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TBROWSE, CLASS_TBROWSE));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TORDER, null));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TVISIT, ""));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TSEARCH, "   "));

        testClassMatches.add(CLASS_TORDER);
        testClassMatches.add(CLASS_TVISIT_NESTED);
        testClassMatches.add(CLASS_TSEARCH);
        testClassMatches.add(CLASS_IGNORE);

        final Map<String, String> mappings = getSpyTestCaseMapper().scanForTestCaseClassMappings(workDir);

        Assert.assertEquals(3, mappings.size());
        Assert.assertEquals(CLASS_TORDER, mappings.get(TEST_CASE_TORDER));
        Assert.assertEquals(CLASS_TVISIT_NESTED, mappings.get(TEST_CASE_TVISIT));
        Assert.assertEquals(CLASS_TSEARCH, mappings.get(TEST_CASE_TSEARCH));
    }

    @Test(expected = XltException.class)
    public void testScanForTestCaseClassMappings_NoMappingFound()
    {
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TBROWSE, CLASS_TBROWSE));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TORDER, null));

        testClassMatches.add(CLASS_OTHER);

        getSpyTestCaseMapper().scanForTestCaseClassMappings(workDir);
    }

    @Test(expected = XltException.class)
    public void testScanForTestCaseClassMappings_TooManyMappingsFound()
    {
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TBROWSE, CLASS_TBROWSE));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TORDER, null));

        testClassMatches.add(CLASS_TORDER);
        testClassMatches.add(CLASS_TORDER_DUPLICATE);
        testClassMatches.add(CLASS_OTHER);

        getSpyTestCaseMapper().scanForTestCaseClassMappings(workDir);
    }

    @Test(expected = XltException.class)
    public void testScanForTestCaseClassMappings_TooManyMappingsIncludingNestedClass()
    {
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TBROWSE, CLASS_TBROWSE));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TVISIT, null));

        testClassMatches.add(CLASS_TVISIT);
        testClassMatches.add(CLASS_TVISIT_NESTED);
        testClassMatches.add(CLASS_OTHER);

        getSpyTestCaseMapper().scanForTestCaseClassMappings(workDir);
    }

    @Test
    public void testGetUrlsToScan() throws IOException
    {
        tempFolder.newFolder("workDir", "target", "classes");
        tempFolder.newFolder("workDir", "build", "resources", "main");

        final File jarDirectory = tempFolder.newFolder("workDir", "build", "dependency");
        new File(jarDirectory, "test1.jar").createNewFile();
        new File(jarDirectory, "test2.JAR").createNewFile();

        // directories that don't have the expected names should be ignored
        tempFolder.newFolder("workDir", "directoryToIgnore");

        // in JAR directories, files that don't end with ".jar" or ".JAR" should be ignored
        new File(jarDirectory, "fileToIgnore.txt").createNewFile();
        new File(jarDirectory, "jarToIgnore.jaR").createNewFile();

        // subdirectories in JAR directories should be ignored
        final File ignoredJarDirectory = tempFolder.newFolder("workDir", "build", "dependency", "directoryToIgnore");
        new File(ignoredJarDirectory, "jarToIgnore.jar").createNewFile();

        final Set<URL> urls = TestCaseMapper.getUrlsToScan(workDir);

        Assert.assertEquals(4, urls.size());
        Assert.assertTrue(urls.stream().anyMatch(u -> u.toString().endsWith("/workDir/target/classes/")));
        Assert.assertTrue(urls.stream().anyMatch(u -> u.toString().endsWith("/workDir/build/resources/main/")));
        Assert.assertTrue(urls.stream().anyMatch(u -> u.toString().endsWith("/workDir/build/dependency/test1.jar")));
        Assert.assertTrue(urls.stream().anyMatch(u -> u.toString().endsWith("/workDir/build/dependency/test2.JAR")));
    }

    @Test(expected = XltException.class)
    public void testGetUrlsToScan_NoValidDirectoriesExist() throws IOException
    {
        tempFolder.newFolder("workDir", "directoryToIgnore");

        TestCaseMapper.getUrlsToScan(workDir);
    }

    @Test
    public void testScanForTestClasses_SingleTestCase()
    {
        prepareTestProjectDirectory();

        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TBROWSE, null));

        final Set<String> matches = new TestCaseMapper(loadProfile).scanForTestClasses(workDir);

        Assert.assertEquals(TEST_PROJECT_MATCHES_TBROWSE.size(), matches.size());
        Assert.assertTrue(matches.containsAll(TEST_PROJECT_MATCHES_TBROWSE));
    }

    @Test
    public void testScanForTestClasses_MultipleTestCases()
    {
        prepareTestProjectDirectory();

        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TBROWSE, null));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_TVISIT, null));
        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_OTHER, null));

        final Set<String> matches = new TestCaseMapper(loadProfile).scanForTestClasses(workDir);

        Assert.assertEquals(TEST_PROJECT_MATCHES_TBROWSE.size() + TEST_PROJECT_MATCHES_TVISIT.size(), matches.size());
        Assert.assertTrue(matches.containsAll(TEST_PROJECT_MATCHES_TBROWSE));
        Assert.assertTrue(matches.containsAll(TEST_PROJECT_MATCHES_TVISIT));
    }

    @Test
    public void testScanForTestClasses_NoMatchesFound()
    {
        prepareTestProjectDirectory();

        testCaseProfiles.add(createTestCaseProfile(TEST_CASE_OTHER, null));

        Assert.assertTrue(new TestCaseMapper(loadProfile).scanForTestClasses(workDir).isEmpty());
    }

    @Test
    public void testGetTestClassesFilePattern_SingleTestCaseName()
    {
        final String p = TestCaseMapper.getTestClassesFilePattern(Set.of(TEST_CASE_TBROWSE));

        Assert.assertTrue("TBrowse.class".matches(p));
        Assert.assertTrue("test/TBrowse.class".matches(p));
        Assert.assertTrue("com/xceptance/TBrowse.class".matches(p));
        Assert.assertTrue("com/xceptance/ParentClass$TBrowse.class".matches(p));

        Assert.assertFalse("TBrowse".matches(p));
        Assert.assertFalse("com/xceptance/TBrowse".matches(p));
        Assert.assertFalse("com/xceptance/ParentClass$TBrowse".matches(p));
        Assert.assertFalse("com/xceptance/TBrowse$NestedClass.class".matches(p));
        Assert.assertFalse("com/xceptance/TBrowse/Test.class".matches(p));
        Assert.assertFalse("TBrowse.txt".matches(p));
        Assert.assertFalse("com/xceptance/TBrowse.txt".matches(p));
        Assert.assertFalse("com/xceptance/ParentClass$TBrowse.txt".matches(p));
        Assert.assertFalse("com/xceptance/tbrowse.class".matches(p));
        Assert.assertFalse("com/xceptance/AbcTBrowse.class".matches(p));
        Assert.assertFalse("com/xceptance/TBrowseXyz.class".matches(p));
        Assert.assertFalse("com/xceptance/AbcTBrowseXyz.class".matches(p));

        Assert.assertFalse("TVisit.class".matches(p));
        Assert.assertFalse("com/xceptance/TVisit.class".matches(p));
        Assert.assertFalse("com/xceptance/ParentClass$TVisit.class".matches(p));
    }

    @Test
    public void testGetTestClassesFilePattern_MultipleTestCaseNames()
    {
        final String p = TestCaseMapper.getTestClassesFilePattern(Set.of(TEST_CASE_TBROWSE, TEST_CASE_TVISIT));

        Assert.assertTrue("TBrowse.class".matches(p));
        Assert.assertTrue("TVisit.class".matches(p));
        Assert.assertTrue("test/TBrowse.class".matches(p));
        Assert.assertTrue("test/TVisit.class".matches(p));
        Assert.assertTrue("com/xceptance/TBrowse.class".matches(p));
        Assert.assertTrue("com/xceptance/TVisit.class".matches(p));
        Assert.assertTrue("com/xceptance/ParentClass$TBrowse.class".matches(p));
        Assert.assertTrue("com/xceptance/ParentClass$TVisit.class".matches(p));

        Assert.assertFalse("TBrowse".matches(p));
        Assert.assertFalse("com/xceptance/TVisit".matches(p));
        Assert.assertFalse("test/TBrowse.txt".matches(p));
        Assert.assertFalse("TVisit.txt".matches(p));
        Assert.assertFalse("com/xceptance/ParentClass$tbrowse.class".matches(p));
        Assert.assertFalse("tvisit.class".matches(p));
        Assert.assertFalse("com/xceptance/TBrowseXyz.class".matches(p));
        Assert.assertFalse("AbcTvisit.class".matches(p));

        Assert.assertFalse("Other.class".matches(p));
        Assert.assertFalse("com/xceptance/Other.class".matches(p));
        Assert.assertFalse("com/xceptance/ParentClass$Other.class".matches(p));
    }

    @Test
    public void testGetTestClassesFilePattern_TestCaseNameWithSpecialCharacters()
    {
        final String p = TestCaseMapper.getTestClassesFilePattern(Set.of("$Test_"));

        Assert.assertTrue("$Test_.class".matches(p));
        Assert.assertTrue("com/xceptance/$Test_.class".matches(p));
        Assert.assertTrue("com/xceptance/ParentClass$$Test_.class".matches(p));

        // check that "$" symbol is not interpreted as the RegEx end-of-string symbol
        Assert.assertFalse("".matches(p));
        Assert.assertFalse("com/xceptance/".matches(p));
        Assert.assertFalse("com/xceptance/ParentClass$".matches(p));

        Assert.assertFalse("com/xceptance/$Test_".matches(p));
        Assert.assertFalse("com/xceptance/$Test_.txt".matches(p));
        Assert.assertFalse("com/xceptance/Test.class".matches(p));
        Assert.assertFalse("com/xceptance/$test_.class".matches(p));
    }

    @Test
    public void testGetTestClassNamePattern_SingleTestCaseName()
    {
        final String p = TestCaseMapper.getTestClassNamePattern(TEST_CASE_TBROWSE);

        Assert.assertTrue("TBrowse".matches(p));
        Assert.assertTrue("test.TBrowse".matches(p));
        Assert.assertTrue("com.xceptance.TBrowse".matches(p));
        Assert.assertTrue("com.xceptance.ParentClass$TBrowse".matches(p));

        Assert.assertFalse("TBrowse.class".matches(p));
        Assert.assertFalse("com.xceptance.TBrowse.class".matches(p));
        Assert.assertFalse("com.xceptance.ParentClass$TBrowse.class".matches(p));
        Assert.assertFalse("com.xceptance.TBrowse$NestedClass".matches(p));
        Assert.assertFalse("com.xceptance.TBrowse.Test".matches(p));
        Assert.assertFalse("TBrowse.txt".matches(p));
        Assert.assertFalse("com.xceptance.TBrowse.txt".matches(p));
        Assert.assertFalse("com.xceptance.ParentClass$TBrowse.txt".matches(p));
        Assert.assertFalse("com.xceptance.tbrowse".matches(p));
        Assert.assertFalse("com.xceptance.AbcTBrowse".matches(p));
        Assert.assertFalse("com.xceptance.TBrowseXyz".matches(p));
        Assert.assertFalse("com.xceptance.AbcTBrowseXyz".matches(p));

        Assert.assertFalse("TVisit".matches(p));
        Assert.assertFalse("com.xceptance.TVisit".matches(p));
        Assert.assertFalse("com.xceptance.ParentClass$TVisit".matches(p));
    }

    @Test
    public void testGetTestClassNamePattern_TestCaseNameWithSpecialCharacters()
    {
        final String p = TestCaseMapper.getTestClassNamePattern("$Test_");

        Assert.assertTrue("$Test_".matches(p));
        Assert.assertTrue("com.xceptance.$Test_".matches(p));
        Assert.assertTrue("com.xceptance.ParentClass$$Test_".matches(p));

        // check that "$" symbol is not interpreted as the RegEx end-of-string symbol
        Assert.assertFalse("".matches(p));
        Assert.assertFalse("com.xceptance.".matches(p));
        Assert.assertFalse("com.xceptance.ParentClass$".matches(p));

        Assert.assertFalse("com.xceptance.$Test_.class".matches(p));
        Assert.assertFalse("com.xceptance.$Test_.txt".matches(p));
        Assert.assertFalse("com.xceptance.Test".matches(p));
        Assert.assertFalse("com.xceptance.$test_".matches(p));
    }

    private TestCaseLoadProfileConfiguration createTestCaseProfile(final String testCaseName, final String testClassName)
    {
        final TestCaseLoadProfileConfiguration testCaseProfile = new TestCaseLoadProfileConfiguration();
        testCaseProfile.setUserName(testCaseName);
        testCaseProfile.setTestCaseClassName(testClassName);
        return testCaseProfile;
    }

    private TestCaseMapper getSpyTestCaseMapper()
    {
        final TestCaseMapper testCaseMapper = Mockito.spy(new TestCaseMapper(loadProfile));
        Mockito.doReturn(testClassMatches).when(testCaseMapper).scanForTestClasses(Mockito.any());

        return testCaseMapper;
    }

    private void prepareTestProjectDirectory()
    {
        try
        {
            ZipUtils.unzipFile(TEST_PROJECT_ZIP, workDir);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
