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
package com.xceptance.xlt.engine.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.engine.util.PropertyIncludeResolver.PropertyInclude;
import com.xceptance.xlt.engine.util.PropertyIncludeResolver.PropertyIncludeResult;

/**
 * Different tests for {@link PropertyIncludeResolver}. The first test case has some explanatory comments as a very
 * similar structure with nearly the same steps is done in each test case. Notice, that the included directories in some
 * tests are existing directories in the same directory as this test class. So for this tests mocking is unnecessary.
 *
 * @author Rene Schwietzke
 */
public class PropertyIncludeResolverTest
{
    /**
     * The absolute path to the test resources. Simulates the boundary of the FS
     */
    private FileObject homeDir;

    /**
     * There the test resources are
     */
    private FileObject configDir;

    /**
     * Setup the base source
     */
    @Before
    public void setup() throws FileSystemException
    {
        var home = getClass().getResource("includeresolvertest").getFile();
        homeDir = VFS.getManager().toFileObject(new File(home));
        configDir = homeDir.resolveFile("configdir");
    }

    enum IsInclude
    {
        TRUE,
        FALSE;

        public boolean equals(boolean value)
        {
            if (this == TRUE && value)
            {
                return true;
            }
            if (this == FALSE && !value)
            {
                return true;
            }
            return false;
        }
    }

    /**
     * Detailed test result
     */
    static class IncludeResult
    {
        public final String name;
        public final boolean exists;
        public final boolean seenBefore;
        public final boolean outsideRootDirScope;
        public final IsInclude isInclude;
        public final Optional<String> alternativeName;

        private IncludeResult(String name, boolean exists, boolean seenBefore, boolean outsideRootDirScope, IsInclude isInclude)
        {
            this.name = name;
            this.exists = exists;
            this.seenBefore = seenBefore;
            this.outsideRootDirScope = outsideRootDirScope;
            this.isInclude = isInclude;
            this.alternativeName = Optional.empty();
        }

        private IncludeResult(String name, boolean exists, boolean seenBefore, boolean outsideRootDirScope, IsInclude isInclude, final String alternativeName)
        {
            this.name = name;
            this.exists = exists;
            this.seenBefore = seenBefore;
            this.outsideRootDirScope = outsideRootDirScope;
            this.isInclude = isInclude;
            this.alternativeName = Optional.of(alternativeName);
        }

        public static IncludeResult get(String name, boolean exists, boolean seenBefore, boolean outsideRootDirScope)
        {
            return new IncludeResult(name, exists, seenBefore, outsideRootDirScope, IsInclude.TRUE);
        }

        public static IncludeResult get(String name, boolean exists, boolean seenBefore, boolean outsideRootDirScope, IsInclude isInclude)
        {
            return new IncludeResult(name, exists, seenBefore, outsideRootDirScope, isInclude);
        }
        public static IncludeResult get(String name, boolean exists, boolean seenBefore, boolean outsideRootDirScope, IsInclude isInclude, String alternativeName)
        {
            return new IncludeResult(name, exists, seenBefore, outsideRootDirScope, isInclude, alternativeName);
        }
    }

    /**
     * Test non-include
     * @throws IOException
     *
     * @throws FileSystemException
     */
    public void testIt(List<String> source, List<IncludeResult> expected) throws IOException
    {
        var srcFiles = source.stream().map(s ->
        {
            try
            {
                return new PropertyInclude(configDir.resolveFile(s), s);
            }
            catch (FileSystemException e)
            {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        /* Get the results and check them. */
        var results = PropertyIncludeResolver.resolve(homeDir, configDir, srcFiles);

        // count right
        Assert.assertEquals("Incorrect file count", expected.size(), results.size());

        // order and content
        for (int i = 0; i < expected.size(); i++)
        {
            final IncludeResult e = expected.get(i);
            final PropertyIncludeResult r = results.get(i);

            // correct path
            try
            {
                var resolved = configDir.resolveFile(e.name);
                assertEquals(String.format("expected: %s and actual: %s", resolved.toString(), r.file.toString()),
                             0,
                             resolved.compareTo(r.file));
            }
            catch (Exception e1)
            {
                fail("Could not resolve path");
            }

            assertEquals(e.exists, r.exists);
            assertEquals(e.outsideRootDirScope, r.outsideORootDirScope);
            assertEquals(e.seenBefore, r.seenBefore);
            assertTrue(e.isInclude.equals(r.isInclude));
            assertEquals(e.alternativeName.orElse(e.name), r.name);
        }
    }

    /**
     * Nothing to resolve
     * @throws IOException
     */
    @Test
    public void nothingToResolve() throws IOException
    {
        testIt(
               List.of("nothingToResolve.properties"),
               List.of(IncludeResult.get("nothingToResolve.properties", true, false, false, IsInclude.FALSE)));
    }

    /**
     * Nothing to resolve, two files
     * @throws IOException
     */
    @Test
    public void twoFilesNothingToResolve() throws IOException
    {
        testIt(
               List.of("nothingToResolve.properties", "nothingToResolve2.properties"),
               List.of(
                       IncludeResult.get("nothingToResolve.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("nothingToResolve2.properties",true, false, false, IsInclude.FALSE)));
    }


    /**
     * One include
     * @throws IOException
     */
    @Test
    public void oneIncludeInRoot_HappyPath() throws IOException
    {
        testIt(
               List.of("oneFileRoot.properties"),
               List.of(
                       IncludeResult.get("oneFileRoot.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("include1.properties", true, false, false, IsInclude.TRUE)));
    }

    /**
     * Resolve by name not just number, will fail because there is no order
     * @throws IOException
     */
    @Test(expected=IllegalArgumentException.class)
    public void includesByNumberAndName() throws IOException
    {
        testIt(
               List.of("includeByNumberOrName.properties"),
               List.of(
                       IncludeResult.get("a.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("include1.properties", true, false, false)));
    }

    /**
     * One include has one include
     * @throws IOException
     */
    @Test
    public void oneIncludeWithIncludeInRoot_HappyPath() throws IOException
    {
        testIt(
               List.of("oneFileRootIncludeWithInclude.properties"),
               List.of(
                       IncludeResult.get("oneFileRootIncludeWithInclude.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("includeWithInclude.properties", true, false, false),
                       IncludeResult.get("include1.properties", true, false, false)));
    }

    /**
     * One include but does not exist
     * @throws IOException
     */
    @Test
    public void oneFileRootNonExistent() throws IOException
    {
        testIt(List.of("oneFileRootNonExistent.properties"),
               List.of(
                       IncludeResult.get("oneFileRootNonExistent.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("notthere.properties", false, false, false)));
    }

    /**
     * Two files in the same dir
     * @throws IOException
     */
    @Test
    public void twoIncludesRoot() throws IOException
    {
        testIt(
               List.of("twoIncludesRoot.properties"),
               List.of(
                       IncludeResult.get("twoIncludesRoot.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("include1.properties", true, false, false),
                       IncludeResult.get("include2.properties", true, false, false)));
    }

    /**
     * Three files in the same dir, different odering in the file
     * @throws IOException
     */
    @Test
    public void threeIncludesRoot() throws IOException
    {
        testIt(
               List.of("threeIncludesRoot.properties"),
               List.of(
                       IncludeResult.get("threeIncludesRoot.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("include1.properties", true, false, false),
                       IncludeResult.get("include2.properties", true, false, false),
                       IncludeResult.get("include3.properties", true, false, false)));
    }

    /**
     * Two files but one does not exist
     * @throws IOException
     */
    @Test
    public void twoFilesRootOneNonExistent() throws IOException
    {
        testIt(
               List.of("twoFilesRooOneDoesNotExist.properties"),
               List.of(
                       IncludeResult.get("twoFilesRooOneDoesNotExist.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("include1.properties", true, false, false),
                       IncludeResult.get("notthereorhere.properties", false, false, false)));
    }

    /**
     * Two files with includes, ensure correct order
     * @throws IOException
     */
    @Test
    public void includeTwoFilesWithIncludes() throws IOException
    {
        testIt(
               List.of("ordertest1.properties", "ordertest2.properties"),
               List.of(
                       IncludeResult.get("ordertest1.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("a.properties", true, false, false),
                       IncludeResult.get("b.properties", true, false, false),
                       IncludeResult.get("ordertest2.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("c.properties", true, false, false),
                       IncludeResult.get("d.properties", true, false, false)
                       ));
    }

    /**
     * Include a dir without properties
     * @throws IOException
     */
    @Test
    public void includeADirWithoutProps() throws IOException
    {
        testIt(
               List.of("includeADir1.properties"),
               List.of(
                       IncludeResult.get("includeADir1.properties", true, false, false, IsInclude.FALSE)));
    }

    /**
     * Include a dir with properties, files are sorted by name
     * @throws IOException
     */
    @Test
    public void includeADirWithProps() throws IOException
    {
        testIt(
               List.of("includeADir2.properties"),
               List.of(
                       IncludeResult.get("includeADir2.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("includeADir2/d.properties", true, false, false),
                       IncludeResult.get("includeADir2/x.properties", true, false, false),
                       IncludeResult.get("includeADir2/y.properties", true, false, false)
                       ));
    }

    /**
     * Include a dir that does not exist.
     * @throws IOException
     */
    @Test
    public void includeNonExistingDir() throws IOException
    {
        testIt(
               List.of("includeADir3.properties"),
               List.of(
                       IncludeResult.get("includeADir3.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("includeADir3", false, false, false)));
    }

    /**
     * Include with gaps in the numbering
     * @throws IOException
     */
    @Test
    public void includeWithGaps() throws IOException
    {
        testIt(
               List.of("includeWithGaps.properties"),
               List.of(
                       IncludeResult.get("includeWithGaps.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("a.properties", true, false, false),
                       IncludeResult.get("b.properties", true, false, false),
                       IncludeResult.get("c.properties", true, false, false)));
    }

    /**
     * Reused number within a file. Last wins
     *
     * @throws IOException
     */
    @Test
    public void includeWithSameNumber() throws IOException
    {
        testIt(
               List.of("includeReuseNumber.properties"),
               List.of(
                       IncludeResult.get("includeReuseNumber.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("a.properties", true, false, false),
                       IncludeResult.get("c.properties", true, false, false)));
    }

    /**
     * Reference in itself
     */
    @Test
    public void referenceItself() throws IOException
    {
        testIt(
               List.of("referenceMe.properties"),
               List.of(
                       IncludeResult.get("referenceMe.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("referenceMe.properties", false, true, false, IsInclude.TRUE)));
    }

    /**
     * Reference is cyclic over several files
     */
    @Test
    public void referenceOverSeveralFiles() throws IOException
    {
        testIt(
               List.of("referenceSeveralFiles0.properties"),
               List.of(
                       IncludeResult.get("referenceSeveralFiles0.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("referenceSeveralFiles1.properties", true, false, false),
                       IncludeResult.get("referenceSeveralFiles2.properties", true, false, false),
                       IncludeResult.get("referenceSeveralFiles0.properties", false, true, false),
                       IncludeResult.get("referenceSeveralFiles1.properties", false, true, false)
                       ));
    }

    /**
     * Include a dir with props that include another dir
     */
    @Test
    public void includeADirWhichIncludesOfADir() throws IOException
    {
        testIt(
               List.of("includeDirAndADir.properties"),
               List.of(
                       IncludeResult.get("includeDirAndADir.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("includeLevel1/includeAnotherDir.properties", true, false, false),
                       IncludeResult.get("includeLevel1/justsome.properties", true, false, false),
                       IncludeResult.get("includeLevel1/includeLevel2/justsome.properties", true, false, false)
                       ));
    }

    /**
     * Include a dir and reference back
     */
    @Test
    public void includeDirAndReferenceUp() throws IOException
    {
        testIt(
               List.of("includeDirAndBack.properties"),
               List.of(
                       IncludeResult.get("includeDirAndBack.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("includeDirAndBack/include.properties", true, false, false),
                       IncludeResult.get("a.properties", true, false, false, IsInclude.TRUE, "../a.properties"),
                       IncludeResult.get("includeADir2/d.properties", true, false, false, IsInclude.TRUE, "../includeADir2/d.properties")
                       ));
    }

    /**
     * Bad references
     */
    @Test
    public void outsideOfRoot() throws IOException
    {
        testIt(
               List.of("outsideOfRoot.properties"),
               List.of(
                       IncludeResult.get("outsideOfRoot.properties", true, false, false, IsInclude.FALSE),
                       IncludeResult.get("/tmp", false, false, true, IsInclude.TRUE),
                       IncludeResult.get("..", false, false, true, IsInclude.TRUE),
                       IncludeResult.get("/etc", false, false, true, IsInclude.TRUE),
                       IncludeResult.get("a.properties", true, false, false, IsInclude.TRUE, "../configdir/a.properties"),
                       IncludeResult.get("outsideOfRoot.properties", false, true, false, IsInclude.TRUE, "../configdir/outsideOfRoot.properties")
                       ));
    }

    /**
     * Extract name test. Just the name of a shared root
     * @throws FileSystemException
     */
    @Test
    public void extractNameFileNameOnly() throws FileSystemException
    {
        final var NAME = "a.properties";
        var s = configDir.resolveFile(NAME);
        assertEquals(NAME, PropertyIncludeResolver.extractName(configDir, s, NAME));
    }

    /**
     * Extract name test. Just the and some path
     * @throws FileSystemException
     */
    @Test
    public void extractNameFilenameAndPath() throws FileSystemException
    {
        final var NAME = "includeADir2/d.properties";
        var s = configDir.resolveFile(NAME);
        assertEquals(NAME, PropertyIncludeResolver.extractName(configDir, s, NAME));
    }

    /**
     * Extract name test. Just the name of a shared root
     * @throws IOException
     */
    @Test
    public void extractNameNoMatch() throws IOException
    {
        var temp = File.createTempFile("testing", ".test");
        var s = VFS.getManager().toFileObject(temp);
        assertEquals(temp.getName(), PropertyIncludeResolver.extractName(configDir, s, temp.getName()));
    }

}
