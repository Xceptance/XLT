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
package com.xceptance.common.io.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link DirectoryFileSystem} and its {@link FsResource} implementation.
 *
 * [AI: Gemini]
 */
public class DirectoryFileSystemTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private Path rootDir;
    private VirtualFileSystem fs;

    @Before
    public void setUp() throws Exception
    {
        rootDir = tempFolder.newFolder("fs_test_dir").toPath();

        // Populate test structure:
        // root/
        //   agent1/
        //     tc1/
        //       user0/
        //         timers.csv
        //         output/
        //           page.html
        //   config/
        //     test.properties
        //   readme.txt

        Files.createDirectories(rootDir.resolve("agent1/tc1/user0/output"));
        Files.createDirectories(rootDir.resolve("config"));

        Files.writeString(rootDir.resolve("readme.txt"), "Root Readme", StandardCharsets.UTF_8);
        Files.writeString(rootDir.resolve("config/test.properties"), "prop=val", StandardCharsets.UTF_8);
        Files.writeString(rootDir.resolve("agent1/tc1/user0/timers.csv"), "100,T,200", StandardCharsets.UTF_8);
        Files.writeString(rootDir.resolve("agent1/tc1/user0/output/page.html"), "<html>Hello</html>", StandardCharsets.UTF_8);

        fs = VirtualFileSystem.open(rootDir);
    }

    @Test
    public void testIsPlainDirectory()
    {
        assertTrue(fs.isPlainDirectory());
        final Optional<Path> localRoot = fs.getLocalRootPath();
        assertTrue(localRoot.isPresent());
        assertEquals(rootDir.toAbsolutePath().normalize(), localRoot.get().toAbsolutePath().normalize());
    }

    @Test
    public void testRootResource()
    {
        final FsResource root = fs.getRoot();
        assertNotNull(root);
        assertTrue(root.exists());
        assertTrue(root.isDirectory());
        assertFalse(root.isFile());
        assertEquals("", root.getPath());
        assertNull(root.getParent());
        assertTrue(root.toLocalPath().isPresent());
    }

    @Test
    public void testResolveFile()
    {
        final FsResource readme = fs.resolve("readme.txt");
        assertNotNull(readme);
        assertTrue(readme.exists());
        assertTrue(readme.isFile());
        assertFalse(readme.isDirectory());
        assertEquals("readme.txt", readme.getName());
        assertEquals("readme.txt", readme.getPath());
        assertTrue(readme.getSize() > 0);
        assertTrue(readme.getLastModified() > 0);
        assertTrue(readme.toLocalPath().isPresent());
    }

    @Test
    public void testResolveNestedDirectory()
    {
        final FsResource userDir = fs.resolve("agent1/tc1/user0");
        assertNotNull(userDir);
        assertTrue(userDir.exists());
        assertTrue(userDir.isDirectory());
        assertFalse(userDir.isFile());
        assertEquals("user0", userDir.getName());
        assertEquals("agent1/tc1/user0", userDir.getPath());

        final FsResource parent = userDir.getParent();
        assertNotNull(parent);
        assertEquals("tc1", parent.getName());
        assertEquals("agent1/tc1", parent.getPath());
    }

    @Test
    public void testListChildren() throws IOException
    {
        final FsResource root = fs.getRoot();
        final List<FsResource> children = root.listChildren();
        assertEquals(3, children.size());

        final List<String> names = children.stream().map(FsResource::getName).sorted().toList();
        assertEquals(List.of("agent1", "config", "readme.txt"), names);
    }

    @Test
    public void testListChildrenWithFilter() throws IOException
    {
        final FsResource root = fs.getRoot();
        final List<FsResource> dirs = root.listChildren(FsResource::isDirectory);
        assertEquals(2, dirs.size());

        final List<String> names = dirs.stream().map(FsResource::getName).sorted().toList();
        assertEquals(List.of("agent1", "config"), names);
    }

    @Test
    public void testReadStream() throws IOException
    {
        final FsResource file = fs.resolve("config/test.properties");
        try (final InputStream in = file.openStream())
        {
            final String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("prop=val", content);
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testReadNonExistentThrowsException() throws IOException
    {
        final FsResource nonExistent = fs.resolve("missing.txt");
        nonExistent.openStream();
    }

    @Test(expected = IOException.class)
    public void testReadDirectoryThrowsException() throws IOException
    {
        final FsResource dir = fs.resolve("config");
        dir.openStream();
    }

    @Test
    public void testExtractSingleFile() throws IOException
    {
        final FsResource readme = fs.resolve("readme.txt");
        final Path targetFile = tempFolder.newFolder("extract_file_dest").toPath().resolve("copied_readme.txt");

        readme.extractTo(targetFile);

        assertTrue(Files.exists(targetFile));
        assertEquals("Root Readme", Files.readString(targetFile, StandardCharsets.UTF_8));
    }

    @Test
    public void testExtractDirectoryTree() throws IOException
    {
        final FsResource userDir = fs.resolve("agent1/tc1/user0");
        final Path targetDir = tempFolder.newFolder("extract_dir_dest").toPath().resolve("user0");

        userDir.extractTo(targetDir);

        assertTrue(Files.isDirectory(targetDir));
        final Path extractedTimer = targetDir.resolve("timers.csv");
        final Path extractedHtml = targetDir.resolve("output/page.html");

        assertTrue(Files.exists(extractedTimer));
        assertTrue(Files.exists(extractedHtml));
        assertEquals("100,T,200", Files.readString(extractedTimer, StandardCharsets.UTF_8));
        assertEquals("<html>Hello</html>", Files.readString(extractedHtml, StandardCharsets.UTF_8));
    }

    @Test
    public void testClose() throws IOException
    {
        fs.close();
        // Closing a directory file system should be a clean no-op
    }
}
