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
package com.xceptance.xlt.engine.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Different tests for {@link IncludedFilesResolver}. The first test case has some explanatory comments as a very
 * similar structure with nearly the same steps is done in each test case. Notice, that the included directories in some
 * tests are existing directories in the same directory as this test class. So for this tests mocking is unnecessary.
 * 
 * @author Sebastian Oerding
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        IncludedFilesResolver.class
    })
public class IncludedFilesResolverTest
{
    /**
     * The absolute path to the test resources.
     */
    private final FileObject pathToTestResources;

    /**
     * Default constructor.
     */
    public IncludedFilesResolverTest()
    {
        try
        {
            pathToTestResources = VFS.getManager().toFileObject(new File(getPathToTestResources()));
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests a simple file which does not define any further includes.
     */
    @Test
    public void testSimpleInclude() throws Exception
    {
        /*
         * Prepare PowerMock for mocking the private method getFileInputStream of the class IncludedFilesResolver.
         */
        prepare();

        /*
         * Prepare PowerMock for a specific file with a specific content and the method invocation of
         * IncludedFilesResolver#getFileInputStream.
         */
        final FileObject root = VFS.getManager().toFileObject(new File("root.properties"));
        expectForGetFileInputStream(root, "");

        /* Switch to replay mode as required in the PowerMock documentation. */
        PowerMock.replay(IncludedFilesResolver.class);

        /* Get the results and check them. */
        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(root), root.getParent(), false);
        Assert.assertTrue("Root file not included", results.contains(root.getName().getPath()));
        Assert.assertEquals("Wrong number of entries for list of files to include", 1, results.size());
    }

    /**
     * Tests to include a file whose name does not ends with &quot;.properties&quot;.
     */
    @Test(expected = IllegalStateException.class)
    public void testIncludeFileWithWrongName() throws Exception
    {
        prepare();

        final FileObject root = VFS.getManager().toFileObject(new File("root.p"));
        expectForGetFileInputStream(root, "");

        PowerMock.replay(IncludedFilesResolver.class);

        // No check as we expect an Exception
        IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(root), root.getParent(), false);
    }

    /**
     * Tests to include a file via an include property with a a non-numeric index.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIncludeFileWithNonNumberIndex() throws Exception
    {
        prepare();

        final FileObject root = VFS.getManager().toFileObject(new File("root.properties"));
        expectForGetFileInputStream(root, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".aaa = first.properties\n");

        PowerMock.replay(IncludedFilesResolver.class);

        // No check as we expect an Exception
        IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(root), root.getParent(), false);
    }

    /**
     * Verifies that a transitive include is also included.
     */
    @Test
    public void testTransitiveInclude() throws Exception
    {
        prepare();

        /*
         * root.properties test1.properties
         */

        final FileObject root = VFS.getManager().toFileObject(new File("root.properties"));
        expectForGetFileInputStream(root, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=test1.properties");

        final FileObject test1 = computeNewFile(root, "test1.properties");
        /*
         * Just a dummy entry to also verify that selecting only the include properties is done correctly in
         * IncludedFilesResolver
         */
        expectForGetFileInputStream(test1, "no.include.property.defined = 1");

        PowerMock.replay(IncludedFilesResolver.class);

        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(root), root.getParent(), true);
        Assert.assertTrue("Root file not included", results.contains(root.getName().getPath()));
        Assert.assertTrue("Included file not included", results.contains(test1.getName().getPath()));
        Assert.assertEquals("Wrong number of entries for list of files to include", 2, results.size());
    }

    /**
     * Verifies that a cycle / duplicate is detected and a RuntimeException is thrown in that case.
     */
    @Test(expected = RuntimeException.class)
    public void testResolveFileIncludedByItself() throws Exception
    {
        prepare();

        /*
         * root.properties test1.properties test1.properties
         */

        final FileObject root = VFS.getManager().toFileObject(new File("root.properties"));
        expectForGetFileInputStream(root, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=test1.properties");

        final FileObject test1 = computeNewFile(root, "test1.properties");
        expectForGetFileInputStream(test1, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=test1.properties");

        PowerMock.replay(IncludedFilesResolver.class);

        // No check as we expect an Exception
        IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(root), root.getParent(), false);
    }

    /**
     * Verifies that a cycle / duplicate is detected and a RuntimeException is thrown in that case.
     */
    @Test(expected = RuntimeException.class)
    public void testResolveFileIncludeRoot() throws Exception
    {
        prepare();

        /*
         * root.properties test1.properties root.properties
         */

        final FileObject root = VFS.getManager().toFileObject(new File("root.properties"));
        expectForGetFileInputStream(root, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=test1.properties");

        final FileObject test1 = computeNewFile(root, "test1.properties");
        expectForGetFileInputStream(test1, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=root.properties");

        PowerMock.replay(IncludedFilesResolver.class);

        // No check as we expect an Exception
        IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(root), root.getParent(), false);
    }

    /**
     * Verifies that files which are included more than once by different files are also included.
     */
    @Test
    public void testResolveFileIncludedMoreThanOnce() throws Exception
    {
        prepare();

        /*
         * first.properties second.properties third.properties fourth.properties second.properties
         */

        final FileObject first = VFS.getManager().toFileObject(new File("first.properties"));
        expectForGetFileInputStream(first, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=second.properties\n" +
                                           IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".2=third.properties");

        final FileObject fourth = VFS.getManager().toFileObject(new File("fourth.properties"));
        expectForGetFileInputStream(fourth, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=second.properties");

        final FileObject second = computeNewFile(first, "second.properties");
        expectForGetFileInputStream(second, "no.include.property.defined = 1");
        expectForGetFileInputStream(second, "no.include.property.defined = 1");

        final FileObject third = computeNewFile(first, "third.properties");
        expectForGetFileInputStream(third, "no.include.property.defined = 1");

        PowerMock.replay(IncludedFilesResolver.class);

        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(first, fourth), first.getParent(),
                                                                                       true);

        Assert.assertEquals("Unexpected first element", first.getName().getPath(), results.get(0));
        Assert.assertEquals("Unexpected second element", second.getName().getPath(), results.get(1));
        Assert.assertEquals("Unexpected third element", third.getName().getPath(), results.get(2));
        Assert.assertEquals("Unexpected fourth element", fourth.getName().getPath(), results.get(3));
        // The second property file is included twice
        Assert.assertEquals("Unexpected fifth element", second.getName().getPath(), results.get(4));
        Assert.assertEquals("Wrong number of entries for list of files to include", 5, results.size());
    }

    /**
     * Verifies that the includes are resolved in the correct order.
     */
    @Test
    public void testCorrectResolveOrder() throws Exception
    {
        prepare();

        /*
         * root.properties first.properties second.properties third.properties fourth.properties
         */

        final FileObject root = VFS.getManager().toFileObject(new File("root.properties"));
        expectForGetFileInputStream(root, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=first.properties\n" +
                                          IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".2=fourth.properties");

        final FileObject first = computeNewFile(root, "first.properties");
        expectForGetFileInputStream(first, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=second.properties\n" +
                                           IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".3=third.properties");

        final FileObject second = computeNewFile(root, "second.properties");
        expectForGetFileInputStream(second, "no.include.property.defined = 1");

        final FileObject third = computeNewFile(root, "third.properties");
        expectForGetFileInputStream(third, "no.include.property.defined = 1");

        final FileObject fourth = computeNewFile(root, "fourth.properties");
        expectForGetFileInputStream(fourth, "no.include.property.defined = 1");

        PowerMock.replay(IncludedFilesResolver.class);

        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(root), root.getParent(), true);

        Assert.assertEquals("Unexpected root element", root.getName().getPath(), results.get(0));
        Assert.assertEquals("Unexpected first element", first.getName().getPath(), results.get(1));
        Assert.assertEquals("Unexpected second element", second.getName().getPath(), results.get(2));
        Assert.assertEquals("Unexpected third element", third.getName().getPath(), results.get(3));
        Assert.assertEquals("Unexpected fourth element", fourth.getName().getPath(), results.get(4));
        Assert.assertEquals("Wrong number of entries for list of files to include", 5, results.size());
    }

    /**
     * Verifies that the includes are resolved in the correct numeric order.
     */
    @Test
    public void testCorrectResolveOrderNumeric() throws Exception
    {
        prepare();

        /*
         * root.properties first.properties second.properties third.properties fourth.properties
         */

        final FileObject root = VFS.getManager().toFileObject(new File("root.properties"));
        expectForGetFileInputStream(root,
                                    IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".10=fourth.properties\n" +
                                          IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".5=third.properties\n" +
                                          IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".03=second.properties\n" +
                                          IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".0=first.properties");

        final FileObject first = computeNewFile(root, "first.properties");
        expectForGetFileInputStream(first, "");

        final FileObject second = computeNewFile(root, "second.properties");
        expectForGetFileInputStream(second, "");

        final FileObject third = computeNewFile(root, "third.properties");
        expectForGetFileInputStream(third, "");

        final FileObject fourth = computeNewFile(root, "fourth.properties");
        expectForGetFileInputStream(fourth, "");

        PowerMock.replay(IncludedFilesResolver.class);

        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(root), root.getParent(), true);

        Assert.assertEquals("Unexpected root element", root.getName().getPath(), results.get(0));
        Assert.assertEquals("Unexpected first element", first.getName().getPath(), results.get(1));
        Assert.assertEquals("Unexpected second element", second.getName().getPath(), results.get(2));
        Assert.assertEquals("Unexpected third element", third.getName().getPath(), results.get(3));
        Assert.assertEquals("Unexpected fourth element", fourth.getName().getPath(), results.get(4));
        Assert.assertEquals("Wrong number of entries for list of files to include", 5, results.size());
    }

    /**
     * Verifies that the includes are resolved in the correct order.
     */
    @Test
    public void testCorrectResolveOrderTwoRootFilesNoTransitiveIncludes() throws Exception
    {
        prepare();

        /*
         * default.properties project.properties
         */

        final FileObject defaultProp = VFS.getManager().toFileObject(new File("default.properties"));
        expectForGetFileInputStream(defaultProp, "no.include.property.defined = 1");

        final FileObject projectProp = VFS.getManager().toFileObject(new File("project.properties"));
        expectForGetFileInputStream(projectProp, "no.include.property.defined = 1");

        PowerMock.replay(IncludedFilesResolver.class);

        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(defaultProp, projectProp),
                                                                                       defaultProp.getParent(), false);

        Assert.assertEquals("Unexpected first root element", defaultProp.getName().getPath(), results.get(0));
        Assert.assertEquals("Unexpected second root element", projectProp.getName().getPath(), results.get(1));
        Assert.assertEquals("Wrong number of entries for list of files to include", 2, results.size());
    }

    /**
     * Verifies that the includes are resolved in the correct order.
     */
    @Test
    public void testCorrectResolveOrderTwoRootFilesTransitiveIncludes() throws Exception
    {
        prepare();

        /*
         * default.properties defaultFirst.properties defaultSecond.properties defaultThird.properties
         * defaultFourth.properties project.properties projectFirst.properties projectSecond.properties
         * projectThird.properties projectFourth.properties
         */

        // set default properties
        final FileObject defaultProp = VFS.getManager().toFileObject(new File("default.properties"));
        expectForGetFileInputStream(defaultProp, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=defaultFirst.properties\n" +
                                                 IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".2=defaultFourth.properties");

        final FileObject defaultFirst = computeNewFile(defaultProp, "defaultFirst.properties");
        expectForGetFileInputStream(defaultFirst, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=defaultSecond.properties\n" +
                                                  IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".3=defaultThird.properties");

        final FileObject defaultSecond = computeNewFile(defaultProp, "defaultSecond.properties");
        expectForGetFileInputStream(defaultSecond, "no.include.property.defined = 1");

        final FileObject defaultThird = computeNewFile(defaultProp, "defaultThird.properties");
        expectForGetFileInputStream(defaultThird, "no.include.property.defined = 1");

        final FileObject defaultFourth = computeNewFile(defaultProp, "defaultFourth.properties");
        expectForGetFileInputStream(defaultFourth, "no.include.property.defined = 1");

        // set project properties
        final FileObject projectProp = VFS.getManager().toFileObject(new File("project.properties"));
        expectForGetFileInputStream(projectProp, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=projectFirst.properties\n" +
                                                 IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".2=projectFourth.properties");

        final FileObject projectFirst = computeNewFile(projectProp, "projectFirst.properties");
        expectForGetFileInputStream(projectFirst, IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".1=projectSecond.properties\n" +
                                                  IncludedFilesResolver.PROP_INCLUDE_NAME_PREFIX + ".2=projectThird.properties");

        final FileObject projectSecond = computeNewFile(projectProp, "projectSecond.properties");
        expectForGetFileInputStream(projectSecond, "no.include.property.defined = 1");

        final FileObject projectThird = computeNewFile(projectProp, "projectThird.properties");
        expectForGetFileInputStream(projectThird, "no.include.property.defined = 1");

        final FileObject projectFourth = computeNewFile(projectProp, "projectFourth.properties");
        expectForGetFileInputStream(projectFourth, "no.include.property.defined = 1");

        PowerMock.replay(IncludedFilesResolver.class);

        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(defaultProp, projectProp),
                                                                                       defaultFirst.getParent(), true);

        Assert.assertEquals("Unexpected first root element", defaultProp.getName().getPath(), results.get(0));
        Assert.assertEquals("Unexpected first default element", defaultFirst.getName().getPath(), results.get(1));
        Assert.assertEquals("Unexpected second default element", defaultSecond.getName().getPath(), results.get(2));
        Assert.assertEquals("Unexpected third default element", defaultThird.getName().getPath(), results.get(3));
        Assert.assertEquals("Unexpected fourth default element", defaultFourth.getName().getPath(), results.get(4));
        Assert.assertEquals("Unexpected second root element", projectProp.getName().getPath(), results.get(5));
        Assert.assertEquals("Unexpected first project element", projectFirst.getName().getPath(), results.get(6));
        Assert.assertEquals("Unexpected second project element", projectSecond.getName().getPath(), results.get(7));
        Assert.assertEquals("Unexpected third project element", projectThird.getName().getPath(), results.get(8));
        Assert.assertEquals("Unexpected fourth project element", projectFourth.getName().getPath(), results.get(9));
        Assert.assertEquals("Wrong number of entries for list of files to include", 10, results.size());
    }

    /**
     * Verifies that an exception is thrown if the include refers to a missing file.
     */
    @Test(expected = IllegalStateException.class)
    public void testIncludeMissingFile() throws FileSystemException
    {
        final FileObject notExisting = VFS.getManager().toFileObject(new File("IDontExist.properties"));
        if (notExisting.exists())
        {
            throw new RuntimeException("File " + notExisting.getName().getPath() +
                                       " is expected not to exist for testing purposes. Nevertheless it exists!");
        }

        // No check as we expect an Exception
        IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(notExisting), notExisting.getParent(), false);
    }

    /**
     * Verifies that a directory can be included by just giving the directory name in an include the same way as for a
     * regular file. However note that this includes only files having their name ending with .properties to avoid
     * conflicts with tools such as SVN which add their own files into each stored directory.
     * 
     * @throws Exception
     */
    @Test
    public void testIncludeWholeDirectory() throws Exception
    {
        /*
         * test1 test1a.properties test1b.properties
         */

        final FileObject test1 = pathToTestResources.resolveFile("test1");
        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(test1), pathToTestResources, false);

        Assert.assertEquals("Wrong number of entries for list of files to include", 2, results.size());
        Assert.assertTrue("Unexpected first element", results.get(0).endsWith("test1/test1a.properties"));
        Assert.assertTrue("Unexpected second element", results.get(1).endsWith("test1/test1b.properties"));
    }

    /**
     * Verifies that a sub-directory can be included. This feature is not implemented yet, so it can be ignored!
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void testIncludeWholeDirectoryAndSubDirectory() throws Exception
    {
        /*
         * test2 test2sub1 test2sub1a.properties test2sub2 test2sub2a.properties test2a.properties
         */

        final FileObject test2 = pathToTestResources.resolveFile("test2");

        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(test2), pathToTestResources, false);

        Assert.assertEquals("Wrong number of entries for list of files to include", 3, results.size());
        Assert.assertTrue("Unexpected first element", results.get(0).endsWith("test2/test2sub1/test2sub1a.properties"));
        Assert.assertTrue("Unexpected second element", results.get(1).endsWith("test2/test2sub2/test2sub2a.properties"));
        Assert.assertTrue("Unexpected third element", results.get(2).endsWith("test2/test2a.properties"));
    }

    /**
     * Verifies that a directory can be included by a properties file.
     * 
     * @throws Exception
     */
    @Test
    public void testIncludeWholeDirectoryByPropertiesFile() throws Exception
    {
        /*
         * root.properties test1 test1a.properties test1b.properties a.properties
         */

        final FileObject root = pathToTestResources.resolveFile("root.properties");

        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(root), pathToTestResources, false);

        Assert.assertEquals("Wrong number of entries for list of files to include", 4, results.size());

        Assert.assertEquals("Unexpected first element", root.getName().getPath(), results.get(0));
        Assert.assertTrue("Unexpected second element", results.get(1).endsWith("test1/test1a.properties"));
        Assert.assertTrue("Unexpected third element", results.get(2).endsWith("test1/test1b.properties"));
        Assert.assertTrue("Unexpected fourth element", results.get(3).endsWith("a.properties"));
    }

    /**
     * Verifies that a properties file in a sub-directory can be included.
     */
    @Test
    public void testIncludeWholeSubDirectoryAsRoot() throws Exception
    {
        /*
         * test2sub1a.properties
         */

        final FileObject test2sub1a = pathToTestResources.resolveFile("test2/test2sub1/test2sub1a.properties");
        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(test2sub1a), pathToTestResources,
                                                                                       false);

        Assert.assertEquals("Wrong number of entries for list of files to include", 1, results.size());
        Assert.assertTrue("Unexpected first element", results.get(0).endsWith("test2/test2sub1/test2sub1a.properties"));
    }

    /**
     * Verifies that the includes are resolved in the correct order.
     */
    @Test
    public void testIncludeWholeSubDirectoryIncludedByFile() throws Exception
    {
        /*
         * test3 test3a.properties test3sub1 test3sub1a.properties test3sub1b.properties test3b.properties a.properties
         */

        final FileObject test3 = pathToTestResources.resolveFile("test3");
        final FileObject a = pathToTestResources.resolveFile("a.properties");

        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(test3, a), pathToTestResources, false);

        Assert.assertEquals("Wrong number of entries for list of files to include", 5, results.size());
        Assert.assertTrue("Unexpected first element", results.get(0).endsWith("test3/test3a.properties"));
        Assert.assertTrue("Unexpected second element", results.get(1).endsWith("test3/test3sub1/test3sub1a.properties"));
        Assert.assertTrue("Unexpected third element", results.get(2).endsWith("test3/test3sub1/test3sub1b.properties"));
        Assert.assertTrue("Unexpected fourth element", results.get(3).endsWith("test3/test3b.properties"));
        Assert.assertTrue("Unexpected fifth element", results.get(4).endsWith("a.properties"));
    }

    /**
     * Verifies, that files in a parent folder can be included.
     * 
     * @throws Exception
     */
    @Test
    public void testIncludeFileInParentFolder() throws Exception
    {
        /*
         * test4 test4a.properties ../a.properties
         */

        final FileObject test4 = pathToTestResources.resolveFile("test4");

        final List<String> results = IncludedFilesResolver.resolveIncludePropertyFiles(Arrays.asList(test4), pathToTestResources, false);

        Assert.assertEquals("Wrong number of entries for list of files to include", 2, results.size());
        Assert.assertTrue("Unexpected first element", results.get(0).endsWith("test4/test4a.properties"));
        Assert.assertTrue("Unexpected second element", results.get(1).endsWith("a.properties"));
    }

    /**
     * Convenience method to prepare the mock for IncludedFilesResolver appropriately. May be changed to use the @Before
     * annotation from JUnit (method must made public in this case).
     */
    private void prepare()
    {
        PowerMock.mockStaticPartial(IncludedFilesResolver.class, "getFileInputStream");
    }

    /**
     * Convenience method. Computes a new file by taking the absolute path of the argument file, removing the file name
     * and appending the argument includeValue afterwards.
     * 
     * @throws FileSystemException
     */
    private FileObject computeNewFile(final FileObject current, final String includeValue) throws FileSystemException
    {
        return current.getParent().resolveFile(includeValue);
    }

    /**
     * Convenience method. Prepares PowerMock to expect one invocation of IncludedFilesResolver#getFileInputStream and
     * to return a new InputStream filled with the argument contents.
     */
    private void expectForGetFileInputStream(final FileObject file, final String content) throws Exception
    {
        final ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
        PowerMock.expectPrivate(IncludedFilesResolver.class, "getFileInputStream", file).andReturn(bais);
    }

    /**
     * Returns the canonical path name to the test resources.
     * 
     * @return canonical path name to test resources
     */
    private String getPathToTestResources()
    {
        return getClass().getResource("include").getFile();

    }
}
