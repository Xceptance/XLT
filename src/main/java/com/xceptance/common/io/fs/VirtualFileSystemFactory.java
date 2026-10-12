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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Factory for creating {@link VirtualFileSystem} instances from paths, files, and URIs.
 *
 * [AI: Gemini]
 */
final class VirtualFileSystemFactory
{
    private VirtualFileSystemFactory()
    {
    }

    /**
     * Opens a {@link VirtualFileSystem} for the given local {@link Path}.
     *
     * @param path the path to open
     * @return the opened file system
     * @throws IOException if the path does not exist or cannot be opened
     */
    static VirtualFileSystem open(final Path path) throws IOException
    {
        Objects.requireNonNull(path, "path must not be null");

        if (!Files.exists(path))
        {
            throw new FileNotFoundException("Path does not exist: " + path);
        }

        if (Files.isDirectory(path))
        {
            return new DirectoryFileSystem(path);
        }

        return new ZipFileSystem(path, null);
    }

    /**
     * Opens a {@link VirtualFileSystem} for the given path string or URI.
     *
     * @param pathOrUri the path string or URI
     * @return the opened file system
     * @throws IOException if the file system cannot be opened
     */
    static VirtualFileSystem open(final String pathOrUri) throws IOException
    {
        Objects.requireNonNull(pathOrUri, "pathOrUri must not be null");
        final String trimmed = pathOrUri.trim();

        if (trimmed.isEmpty())
        {
            throw new IllegalArgumentException("pathOrUri must not be empty");
        }

        // Handle zip: URIs (e.g. zip:file:///tmp/results.zip!/subfolder)
        if (trimmed.startsWith("zip:"))
        {
            String nested = trimmed.substring(4);
            String innerPrefix = null;

            final int exclamationIdx = nested.indexOf('!');
            if (exclamationIdx >= 0)
            {
                innerPrefix = nested.substring(exclamationIdx + 1);
                nested = nested.substring(0, exclamationIdx);
            }

            final Path zipPath = resolveToPath(nested);
            return new ZipFileSystem(zipPath, innerPrefix);
        }

        // Handle exclamation mark in plain zip path (e.g. /tmp/results.zip!/subfolder)
        final int exclamationIdx = trimmed.indexOf("!/");
        if (exclamationIdx >= 0 && trimmed.substring(0, exclamationIdx).endsWith(".zip"))
        {
            final String filePathPart = trimmed.substring(0, exclamationIdx);
            final String innerPrefix = trimmed.substring(exclamationIdx + 2);
            final Path zipPath = resolveToPath(filePathPart);
            return new ZipFileSystem(zipPath, innerPrefix);
        }

        final Path path = resolveToPath(trimmed);
        return open(path);
    }

    private static Path resolveToPath(final String spec)
    {
        if (spec.startsWith("file:"))
        {
            try
            {
                return Paths.get(URI.create(spec));
            }
            catch (final IllegalArgumentException e)
            {
                // Fallback for non-strictly formatted file: paths
                String raw = spec.substring(5);
                while (raw.startsWith("//"))
                {
                    raw = raw.substring(1);
                }
                return Path.of(raw);
            }
        }
        return Path.of(spec);
    }
}
