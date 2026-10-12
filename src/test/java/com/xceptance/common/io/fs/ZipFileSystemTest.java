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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link ZipFileSystem} and its {@link FsResource} implementation.
 *
 * [AI: Gemini]
 */
public class ZipFileSystemTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private Path zipFile;
    private VirtualFileSystem fs;

    @Before
    public void setUp() throws Exception
    {
        zipFile = tempFolder.newFile("test_archive.zip").toPath();

        // Create test zip archive:
        // root.txt
        // config/default.properties
        // ac01/TLogin/0/timers.csv.gz
        // ac01/TLogin/0/output/1750000000000/dump.html
        // ac01/TLogin/1/timers.csv
        // ac02/TCheckout/0/timers.csv

        try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.toFile())))
        {
            addZipEntry(zos, "root.txt", "Hello Zip".getBytes(StandardCharsets.UTF_8));
            addZipEntry(zos, "config/default.properties", "key1=val1\nkey2=val2".getBytes(StandardCharsets.UTF_8));

            // Add gzipped entry
            final byte[] gzippedTimers = gzip("1000,T,Login,250,true,0".getBytes(StandardCharsets.UTF_8));
            addZipEntry(zos, "ac01/TLogin/0/timers.csv.gz", gzippedTimers);

            // Add nested dump
            addZipEntry(zos, "ac01/TLogin/0/output/1750000000000/dump.html", "<html>Dump Content</html>".getBytes(StandardCharsets.UTF_8));

            // Add plain timers
            addZipEntry(zos, "ac01/TLogin/1/timers.csv", "2000,T,Login,350,true,0".getBytes(StandardCharsets.UTF_8));
            addZipEntry(zos, "ac02/TCheckout/0/timers.csv", "3000,T,Checkout,500,true,0".getBytes(StandardCharsets.UTF_8));
        }

        fs = VirtualFileSystem.open(zipFile);
    }

    @After
    public void tearDown() throws Exception
    {
        if (fs != null)
        {
            fs.close();
        }
    }

    private static void addZipEntry(final ZipOutputStream zos, final String name, final byte[] content) throws IOException
    {
        final ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(content);
        zos.closeEntry();
    }

    private static byte[] gzip(final byte[] data) throws IOException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final GZIPOutputStream gzos = new GZIPOutputStream(baos))
        {
            gzos.write(data);
        }
        return baos.toByteArray();
    }

    @Test
    public void testIsPlainDirectory()
    {
        assertFalse(fs.isPlainDirectory());
        assertFalse(fs.getLocalRootPath().isPresent());
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
        assertFalse(root.toLocalPath().isPresent());
    }

    @Test
    public void testResolveFile()
    {
        final FsResource rootFile = fs.resolve("root.txt");
        assertNotNull(rootFile);
        assertTrue(rootFile.exists());
        assertTrue(rootFile.isFile());
        assertFalse(rootFile.isDirectory());
        assertEquals("root.txt", rootFile.getName());
        assertEquals("root.txt", rootFile.getPath());
        assertEquals("Hello Zip".getBytes(StandardCharsets.UTF_8).length, rootFile.getSize());
        assertFalse(rootFile.toLocalPath().isPresent());
    }

    @Test
    public void testResolveImplicitDirectory()
    {
        // Directory "config" was not explicitly added as "config/", only via "config/default.properties"
        final FsResource configDir = fs.resolve("config");
        assertNotNull(configDir);
        assertTrue(configDir.exists());
        assertTrue(configDir.isDirectory());
        assertFalse(configDir.isFile());
        assertEquals("config", configDir.getName());
        assertEquals("config", configDir.getPath());
    }

    @Test
    public void testListChildrenRoot() throws IOException
    {
        final FsResource root = fs.getRoot();
        final List<FsResource> children = root.listChildren();
        final List<String> names = children.stream().map(FsResource::getName).sorted().toList();

        assertEquals(List.of("ac01", "ac02", "config", "root.txt"), names);
    }

    @Test
    public void testListChildrenAgentDir() throws IOException
    {
        final FsResource agentDir = fs.resolve("ac01");
        final List<FsResource> children = agentDir.listChildren();
        assertEquals(1, children.size());
        assertEquals("TLogin", children.get(0).getName());
        assertTrue(children.get(0).isDirectory());
    }

    @Test
    public void testListChildrenTestCaseDir() throws IOException
    {
        final FsResource tcDir = fs.resolve("ac01/TLogin");
        final List<FsResource> children = tcDir.listChildren();
        final List<String> names = children.stream().map(FsResource::getName).sorted().toList();

        assertEquals(List.of("0", "1"), names);
    }

    @Test
    public void testListChildrenUserDir() throws IOException
    {
        final FsResource userDir0 = fs.resolve("ac01/TLogin/0");
        final List<FsResource> children0 = userDir0.listChildren();
        final List<String> names0 = children0.stream().map(FsResource::getName).sorted().toList();

        assertEquals(List.of("output", "timers.csv.gz"), names0);

        final FsResource userDir1 = fs.resolve("ac01/TLogin/1");
        final List<FsResource> children1 = userDir1.listChildren();
        assertEquals(1, children1.size());
        assertEquals("timers.csv", children1.get(0).getName());
    }

    @Test
    public void testReadStream() throws IOException
    {
        final FsResource configFile = fs.resolve("config/default.properties");
        try (final InputStream in = configFile.openStream())
        {
            final String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertEquals("key1=val1\nkey2=val2", content);
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testReadNonExistentThrowsException() throws IOException
    {
        final FsResource missing = fs.resolve("does_not_exist.txt");
        missing.openStream();
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
        final FsResource rootFile = fs.resolve("root.txt");
        final Path targetFile = tempFolder.newFolder("extract_zip_file").toPath().resolve("extracted_root.txt");

        rootFile.extractTo(targetFile);

        assertTrue(Files.exists(targetFile));
        assertEquals("Hello Zip", Files.readString(targetFile, StandardCharsets.UTF_8));
    }

    @Test
    public void testExtractSubdirectory() throws IOException
    {
        // Extract the error dump subtree: ac01/TLogin/0/output
        final FsResource outputDir = fs.resolve("ac01/TLogin/0/output");
        final Path targetDir = tempFolder.newFolder("extract_zip_dir").toPath().resolve("dump_dest");

        outputDir.extractTo(targetDir);

        assertTrue(Files.isDirectory(targetDir));
        final Path extractedDump = targetDir.resolve("1750000000000/dump.html");
        assertTrue(Files.exists(extractedDump));
        assertEquals("<html>Dump Content</html>", Files.readString(extractedDump, StandardCharsets.UTF_8));
    }

    @Test
    public void testExtractAgentSubtree() throws IOException
    {
        final FsResource agentDir = fs.resolve("ac01");
        final Path targetDir = tempFolder.newFolder("extract_agent_dir").toPath().resolve("ac01");

        agentDir.extractTo(targetDir);

        assertTrue(Files.isDirectory(targetDir));
        assertTrue(Files.exists(targetDir.resolve("TLogin/0/timers.csv.gz")));
        assertTrue(Files.exists(targetDir.resolve("TLogin/0/output/1750000000000/dump.html")));
        assertTrue(Files.exists(targetDir.resolve("TLogin/1/timers.csv")));
        assertEquals("2000,T,Login,350,true,0", Files.readString(targetDir.resolve("TLogin/1/timers.csv"), StandardCharsets.UTF_8));
    }

    @Test
    public void testConcurrentReads() throws Exception
    {
        final int numThreads = 16;
        final int readsPerThread = 50;
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        final List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < numThreads; i++)
        {
            final int threadIdx = i;
            tasks.add(() -> {
                for (int r = 0; r < readsPerThread; r++)
                {
                    final String path = (threadIdx % 2 == 0) ? "root.txt" : "config/default.properties";
                    final FsResource res = fs.resolve(path);
                    try (final InputStream in = res.openStream())
                    {
                        final byte[] bytes = in.readAllBytes();
                        if (bytes.length == 0)
                        {
                            return false;
                        }
                    }
                }
                return true;
            });
        }

        final List<Future<Boolean>> futures = executor.invokeAll(tasks);
        for (final Future<Boolean> f : futures)
        {
            assertTrue(f.get());
        }

        executor.shutdown();
    }

    @Test(expected = IOException.class)
    public void testAccessAfterCloseFails() throws IOException
    {
        fs.close();
        final FsResource res = fs.resolve("root.txt");
        res.openStream();
    }
}
