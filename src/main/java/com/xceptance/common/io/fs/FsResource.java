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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Represents a file or directory resource within a {@link VirtualFileSystem}.
 * Provides unified navigation, metadata inspection, streaming read access,
 * and subtree extraction across both plain directories and archives.
 *
 * [AI: Gemini]
 */
public interface FsResource
{
    /**
     * Returns the simple name of this resource (e.g. "timers.csv.gz" or "ac0028_us-west3_00").
     * For the root resource, this may return an empty string or the base directory/archive name.
     *
     * @return the simple name
     */
    String getName();

    /**
     * Returns the normalized path of this resource relative to the filesystem root,
     * using forward slash ('/') as the separator without leading or trailing slashes
     * (e.g. "ac0028_us-west3_00/TAddToCart/1582/timers.csv.gz").
     * The root resource returns an empty string ("").
     *
     * @return the relative normalized path
     */
    String getPath();

    /**
     * Returns the file extension of this resource without the leading dot (e.g. "gz" or "properties"),
     * or an empty string if there is no extension.
     *
     * @return file extension, or empty string if none
     */
    default String getExtension()
    {
        final String name = getName();
        final int dotIndex = name.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < name.length() - 1)
        {
            return name.substring(dotIndex + 1);
        }
        return "";
    }

    /**
     * Checks whether this resource exists in the filesystem.
     *
     * @return {@code true} if this resource exists, {@code false} otherwise
     */
    boolean exists();

    /**
     * Checks whether this resource is a directory.
     *
     * @return {@code true} if a directory, {@code false} otherwise
     */
    boolean isDirectory();

    /**
     * Checks whether this resource is a regular file.
     *
     * @return {@code true} if a regular file, {@code false} otherwise
     */
    boolean isFile();

    /**
     * Returns the size of this resource in bytes if it is a file, or 0 if a directory
     * or non-existent.
     *
     * @return size in bytes
     */
    long getSize();

    /**
     * Returns the last modified time of this resource in milliseconds since epoch,
     * or 0 if unknown or non-existent.
     *
     * @return last modified time in milliseconds
     */
    long getLastModified();

    /**
     * Opens an {@link InputStream} to read the contents of this resource.
     * Callers are responsible for closing the stream.
     *
     * @return an open {@link InputStream}
     * @throws IOException if this resource is not a file, does not exist, or cannot be read
     */
    InputStream openStream() throws IOException;

    /**
     * Lists immediate child resources contained within this directory.
     * Returns an empty list if this resource is not a directory or has no children.
     *
     * @return an unmodifiable list of child resources
     * @throws IOException if an I/O error occurs reading directory contents
     */
    List<FsResource> listChildren() throws IOException;

    /**
     * Lists immediate child resources contained within this directory that match the given filter.
     *
     * @param filter the filter predicate to apply
     * @return an unmodifiable list of matching child resources
     * @throws IOException if an I/O error occurs
     */
    default List<FsResource> listChildren(final Predicate<FsResource> filter) throws IOException
    {
        return listChildren().stream().filter(filter).toList();
    }

    /**
     * Resolves a child resource relative to this resource.
     *
     * @param relativePath the relative path to resolve
     * @return the resolved {@link FsResource} (may or may not exist)
     */
    FsResource resolve(String relativePath);

    /**
     * Returns the parent resource of this resource, or {@code null} if this is the root resource.
     *
     * @return the parent resource, or {@code null}
     */
    FsResource getParent();

    /**
     * Extracts this resource to the specified local disk destination path.
     * <p>
     * If this resource is a regular file, it is copied directly to {@code localDestination}.
     * If this resource is a directory, its entire subtree is recursively extracted/copied
     * into {@code localDestination}.
     * </p>
     *
     * @param localDestination the destination path on the local disk filesystem
     * @throws IOException if an I/O error occurs during extraction
     */
    void extractTo(Path localDestination) throws IOException;

    /**
     * Returns the underlying local {@link Path} if this resource is backed directly by a
     * plain directory on the local filesystem. Returns {@link Optional#empty()} if this resource
     * is backed by an archive.
     *
     * @return the local path, or {@link Optional#empty()}
     */
    Optional<Path> toLocalPath();

    /**
     * Returns the {@link VirtualFileSystem} that manages this resource.
     *
     * @return the owning virtual file system
     */
    VirtualFileSystem getFileSystem();
}
