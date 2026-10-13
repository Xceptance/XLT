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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A lightweight virtual file system abstraction providing unified, high-performance,
 * low-memory access to plain disk directories and compressed archives (e.g. ZIP).
 *
 * [AI: Gemini]
 */
public interface VirtualFileSystem extends Closeable
{
    /**
     * Returns the root {@link FsResource} of this file system.
     *
     * @return the root resource
     */
    FsResource getRoot();

    /**
     * Resolves a path relative to the root of this file system.
     *
     * @param relativePath the relative path to resolve
     * @return the resolved {@link FsResource}
     */
    FsResource resolve(String relativePath);

    /**
     * Checks whether this file system is backed by a plain local directory on disk.
     *
     * @return {@code true} if backed by a local directory, {@code false} if backed by an archive
     */
    boolean isPlainDirectory();

    /**
     * Returns the local disk {@link Path} of the root directory if this file system is backed
     * by a plain directory, or {@link Optional#empty()} if backed by an archive.
     *
     * @return the local root path, or {@link Optional#empty()}
     */
    Optional<Path> getLocalRootPath();

    /**
     * Returns the base {@link Path} on the local filesystem that this virtual file system was opened from.
     * For directory file systems, this is the root directory path.
     * For zip file systems, this is the zip archive file path.
     *
     * @return the base path
     */
    Path getBasePath();

    /**
     * Closes this file system and releases any open system resources or file handles.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    void close() throws IOException;

    /**
     * Opens a {@link VirtualFileSystem} for the given local {@link Path}.
     * If the path points to a directory, a directory-backed file system is returned.
     * If the path points to a .zip archive, a zip-backed file system is returned.
     *
     * @param path the path to open
     * @return the opened file system
     * @throws IOException if the file system cannot be opened
     */
    static VirtualFileSystem open(final Path path) throws IOException
    {
        return VirtualFileSystemFactory.open(path);
    }

    /**
     * Opens a {@link VirtualFileSystem} for the given local {@link File}.
     *
     * @param file the file or directory to open
     * @return the opened file system
     * @throws IOException if the file system cannot be opened
     */
    static VirtualFileSystem open(final File file) throws IOException
    {
        return VirtualFileSystemFactory.open(file.toPath());
    }

    /**
     * Opens a {@link VirtualFileSystem} for the given path string or URI.
     * Handles plain filesystem paths, file: URIs, and zip:file: URIs.
     *
     * @param pathOrUri the path string or URI
     * @return the opened file system
     * @throws IOException if the file system cannot be opened
     */
    static VirtualFileSystem open(final String pathOrUri) throws IOException
    {
        return VirtualFileSystemFactory.open(pathOrUri);
    }
}
