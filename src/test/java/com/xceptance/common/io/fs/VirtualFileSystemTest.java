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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link VirtualFileSystem} factory methods and URI resolution.
 *
 * [AI: Gemini]
 */
public class VirtualFileSystemTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private Path testDir;
    private Path testZip;

    @Before
    public void setUp() throws Exception
    {
        testDir = tempFolder.newFolder("vfs_dir").toPath();
        Files.writeString(testDir.resolve("test.txt"), "hello");

        testZip = tempFolder.newFile("vfs_test.zip").toPath();
        try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(testZip.toFile())))
        {
            zos.putNextEntry(new ZipEntry("test.txt"));
            zos.write("hello from zip".getBytes());
            zos.closeEntry();
        }
    }

    @Test
    public void testOpenDirectoryPath() throws IOException
    {
        try (final VirtualFileSystem fs = VirtualFileSystem.open(testDir))
        {
            assertTrue(fs.isPlainDirectory());
            assertTrue(fs.getLocalRootPath().isPresent());
            assertTrue(fs.resolve("test.txt").exists());
        }
    }

    @Test
    public void testOpenDirectoryFile() throws IOException
    {
        try (final VirtualFileSystem fs = VirtualFileSystem.open(testDir.toFile()))
        {
            assertTrue(fs.isPlainDirectory());
            assertTrue(fs.resolve("test.txt").exists());
        }
    }

    @Test
    public void testOpenZipPath() throws IOException
    {
        try (final VirtualFileSystem fs = VirtualFileSystem.open(testZip))
        {
            assertFalse(fs.isPlainDirectory());
            assertFalse(fs.getLocalRootPath().isPresent());
            assertTrue(fs.resolve("test.txt").exists());
        }
    }

    @Test
    public void testOpenZipFile() throws IOException
    {
        try (final VirtualFileSystem fs = VirtualFileSystem.open(testZip.toFile()))
        {
            assertFalse(fs.isPlainDirectory());
            assertTrue(fs.resolve("test.txt").exists());
        }
    }

    @Test
    public void testOpenUriStringPlainPath() throws IOException
    {
        try (final VirtualFileSystem fs = VirtualFileSystem.open(testDir.toString()))
        {
            assertTrue(fs.isPlainDirectory());
            assertTrue(fs.resolve("test.txt").exists());
        }
    }

    @Test
    public void testOpenUriStringFileScheme() throws IOException
    {
        final String uriStr = testDir.toUri().toString();
        try (final VirtualFileSystem fs = VirtualFileSystem.open(uriStr))
        {
            assertTrue(fs.isPlainDirectory());
            assertTrue(fs.resolve("test.txt").exists());
        }
    }

    @Test
    public void testOpenUriStringZipFileScheme() throws IOException
    {
        final String uriStr = "zip:" + testZip.toUri().toString();
        try (final VirtualFileSystem fs = VirtualFileSystem.open(uriStr))
        {
            assertFalse(fs.isPlainDirectory());
            assertTrue(fs.resolve("test.txt").exists());
        }
    }

    @Test
    public void testOpenUriStringZipFileSchemeWithExclamation() throws IOException
    {
        final String uriStr = "zip:" + testZip.toUri().toString() + "!/";
        try (final VirtualFileSystem fs = VirtualFileSystem.open(uriStr))
        {
            assertFalse(fs.isPlainDirectory());
            assertTrue(fs.resolve("test.txt").exists());
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testOpenNonExistentThrows() throws IOException
    {
        VirtualFileSystem.open(tempFolder.getRoot().toPath().resolve("non_existent_path"));
    }
}
