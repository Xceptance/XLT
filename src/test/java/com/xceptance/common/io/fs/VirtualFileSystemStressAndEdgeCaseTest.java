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
import static org.junit.Assert.assertTrue;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Stress and edge-case tests for {@link VirtualFileSystem}, {@link DirectoryFileSystem}, and {@link ZipFileSystem}.
 *
 * [AI: Gemini]
 */
public class VirtualFileSystemStressAndEdgeCaseTest
{
    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testSpecialCharactersInFileAndDirNames() throws IOException
    {
        final Path dir = tempFolder.newFolder("special_chars_dir").toPath();
        final String specialName = "test file with spaces & umlauts äöü #1.csv";
        final String specialDirName = "special dir (test) #42";

        final Path nestedDir = dir.resolve(specialDirName);
        Files.createDirectories(nestedDir);
        final Path filePath = nestedDir.resolve(specialName);
        Files.writeString(filePath, "special content", StandardCharsets.UTF_8);

        // Test with DirectoryFileSystem
        try (final VirtualFileSystem dirFs = VirtualFileSystem.open(dir))
        {
            final FsResource resDir = dirFs.resolve(specialDirName);
            assertTrue(resDir.exists());
            assertTrue(resDir.isDirectory());

            final FsResource resFile = dirFs.resolve(specialDirName + "/" + specialName);
            assertTrue(resFile.exists());
            assertTrue(resFile.isFile());
            assertEquals(specialName, resFile.getName());

            try (final InputStream in = resFile.openStream())
            {
                assertEquals("special content", new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }

        // Test with ZipFileSystem
        final Path zipPath = tempFolder.newFile("special.zip").toPath();
        try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile())))
        {
            zos.putNextEntry(new ZipEntry(specialDirName + "/" + specialName));
            zos.write("special content in zip".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        try (final VirtualFileSystem zipFs = VirtualFileSystem.open(zipPath))
        {
            final FsResource resDir = zipFs.resolve(specialDirName);
            assertTrue(resDir.exists());
            assertTrue(resDir.isDirectory());

            final FsResource resFile = zipFs.resolve(specialDirName + "/" + specialName);
            assertTrue(resFile.exists());
            assertTrue(resFile.isFile());
            assertEquals(specialName, resFile.getName());

            try (final InputStream in = resFile.openStream())
            {
                assertEquals("special content in zip", new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
    }

    @Test
    public void testPathNormalizationWithMultipleSlashesAndTrailingSlashes() throws IOException
    {
        final Path dir = tempFolder.newFolder("norm_dir").toPath();
        Files.createDirectories(dir.resolve("a/b/c"));
        Files.writeString(dir.resolve("a/b/c/data.txt"), "data", StandardCharsets.UTF_8);

        try (final VirtualFileSystem fs = VirtualFileSystem.open(dir))
        {
            final FsResource r1 = fs.resolve("///a//b///c///data.txt///");
            assertTrue(r1.exists());
            assertTrue(r1.isFile());
            assertEquals("data.txt", r1.getName());
            assertEquals("a/b/c/data.txt", r1.getPath());

            final FsResource rDir = fs.resolve("a/b/");
            assertTrue(rDir.exists());
            assertTrue(rDir.isDirectory());
            assertEquals("b", rDir.getName());
            assertEquals("a/b", rDir.getPath());
        }
    }

    @Test
    public void testSingleRootArchiveScoping() throws IOException
    {
        final Path zipPath = tempFolder.newFile("single_root.zip").toPath();
        try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile())))
        {
            zos.putNextEntry(new ZipEntry("report-run-2026/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("report-run-2026/config/report.properties"));
            zos.write("threads=8".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("report-run-2026/ac01/timers.csv"));
            zos.write("1,2,3".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        // Open scoped to the inner directory via !/report-run-2026
        final String uri = "zip:" + zipPath.toUri().toString() + "!/report-run-2026";
        try (final VirtualFileSystem scopedFs = VirtualFileSystem.open(uri))
        {
            final FsResource root = scopedFs.getRoot();
            final List<String> childNames = root.listChildren().stream().map(FsResource::getName).sorted().toList();
            assertEquals(List.of("ac01", "config"), childNames);

            final FsResource configProp = scopedFs.resolve("config/report.properties");
            assertTrue(configProp.exists());
            assertTrue(configProp.isFile());

            try (final InputStream in = configProp.openStream())
            {
                assertEquals("threads=8", new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
    }

    @Test
    public void testStressHighFileCountAndParallelExtraction() throws Exception
    {
        final Path zipPath = tempFolder.newFile("stress.zip").toPath();
        final int agentCount = 10;
        final int testCasesPerAgent = 5;
        final int usersPerTestCase = 100; // 10 * 5 * 100 = 5,000 user directories

        try (final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile())))
        {
            for (int a = 0; a < agentCount; a++)
            {
                for (int tc = 0; tc < testCasesPerAgent; tc++)
                {
                    for (int u = 0; u < usersPerTestCase; u++)
                    {
                        final String entryName = "ac" + a + "/TTestCase" + tc + "/" + u + "/timers.csv";
                        zos.putNextEntry(new ZipEntry(entryName));
                        zos.write("100,T,Test,50,true,0\n".getBytes(StandardCharsets.UTF_8));
                        zos.closeEntry();
                    }
                }
            }
        }

        final long startOpen = System.currentTimeMillis();
        try (final VirtualFileSystem fs = VirtualFileSystem.open(zipPath))
        {
            final long openDuration = System.currentTimeMillis() - startOpen;
            assertTrue("Opening 5,000 entries took too long: " + openDuration + "ms", openDuration < 3000);

            // Verify navigation
            final FsResource root = fs.getRoot();
            final List<FsResource> agents = root.listChildren();
            assertEquals(agentCount, agents.size());

            final FsResource tc0 = fs.resolve("ac0/TTestCase0");
            final List<FsResource> users = tc0.listChildren();
            assertEquals(usersPerTestCase, users.size());

            // Parallel extraction test: extract 20 user directories concurrently
            final int parallelExtractions = 20;
            final ExecutorService executor = Executors.newFixedThreadPool(8);
            final List<Callable<Boolean>> extractionTasks = new ArrayList<>();

            for (int i = 0; i < parallelExtractions; i++)
            {
                final int userIndex = i;
                final Path extractDest = tempFolder.newFolder("extract_user_" + userIndex).toPath();

                extractionTasks.add(() -> {
                    final FsResource userRes = fs.resolve("ac0/TTestCase0/" + userIndex);
                    userRes.extractTo(extractDest);

                    final Path extractedFile = extractDest.resolve("timers.csv");
                    if (!Files.exists(extractedFile))
                    {
                        return false;
                    }
                    final String content = Files.readString(extractedFile, StandardCharsets.UTF_8);
                    return "100,T,Test,50,true,0\n".equals(content);
                });
            }

            final List<Future<Boolean>> futures = executor.invokeAll(extractionTasks);
            for (final Future<Boolean> future : futures)
            {
                assertTrue(future.get());
            }

            executor.shutdown();
        }
    }
}
