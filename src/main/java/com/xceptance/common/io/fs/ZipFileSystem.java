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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A high-performance, memory-efficient {@link VirtualFileSystem} implementation backed by a ZIP archive.
 * <p>
 * Employs a compact directory navigation tree with string deduplication to minimize heap footprint
 * (typically single-digit MBs even for archives containing millions of files).
 * Deep dump and screenshot directories are excluded from tree indexing and streamed directly to disk
 * on extraction without in-memory entry overhead.
 * </p>
 *
 * [AI: Gemini]
 */
final class ZipFileSystem implements VirtualFileSystem
{
    private final Path zipFilePath;
    private final String rootPrefix;
    private final ZipFile zipFile;
    private final Map<String, String[]> dirTree;
    private final ZipFsResource rootResource;
    private volatile boolean closed = false;

    /**
     * Constructs a new {@link ZipFileSystem} for the specified zip archive.
     *
     * @param zipFilePath the path to the zip archive
     * @param rootPrefix  an optional root prefix inside the archive (e.g. for single-root archives),
     *                    or {@code null}/empty for archive root
     * @throws IOException if the zip file cannot be opened or is invalid
     */
    ZipFileSystem(final Path zipFilePath, final String rootPrefix) throws IOException
    {
        Objects.requireNonNull(zipFilePath, "zipFilePath must not be null");
        if (!Files.exists(zipFilePath))
        {
            throw new FileNotFoundException("Zip file does not exist: " + zipFilePath);
        }
        if (Files.isDirectory(zipFilePath))
        {
            throw new IOException("Path is a directory, not a zip file: " + zipFilePath);
        }

        this.zipFilePath = zipFilePath.toAbsolutePath().normalize();
        this.rootPrefix = DirectoryFileSystem.normalizeRelativePath(rootPrefix);
        this.zipFile = new ZipFile(this.zipFilePath.toFile(), ZipFile.OPEN_READ);

        // Build ultra-compact navigation index
        this.dirTree = buildCompactDirectoryTree();
        this.rootResource = new ZipFsResource(this, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FsResource getRoot()
    {
        return rootResource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FsResource resolve(final String relativePath)
    {
        final String normalized = DirectoryFileSystem.normalizeRelativePath(relativePath);
        if (normalized.isEmpty())
        {
            return rootResource;
        }
        return new ZipFsResource(this, normalized);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPlainDirectory()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Path> getLocalRootPath()
    {
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getBasePath()
    {
        return zipFilePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws IOException
    {
        if (!closed)
        {
            closed = true;
            zipFile.close();
        }
    }

    void checkOpen() throws IOException
    {
        if (closed)
        {
            throw new IOException("FileSystem is closed: " + zipFilePath);
        }
    }

    ZipFile getZipFile()
    {
        return zipFile;
    }

    String getFullEntryPath(final String relativePath)
    {
        final String norm = DirectoryFileSystem.normalizeRelativePath(relativePath);
        if (rootPrefix.isEmpty())
        {
            return norm;
        }
        if (norm.isEmpty())
        {
            return rootPrefix;
        }
        return rootPrefix + "/" + norm;
    }

    String[] getChildNames(final String relativePath)
    {
        return dirTree.get(relativePath);
    }

    boolean isKnownDirectory(final String relativePath)
    {
        return dirTree.containsKey(relativePath);
    }

    /**
     * Scans the zip central directory and builds a compact parent -> childNames mapping.
     */
    private Map<String, String[]> buildCompactDirectoryTree()
    {
        final Map<String, LinkedHashSet<String>> tempTree = new HashMap<>();
        final Map<String, String> stringPool = new HashMap<>();

        final String prefixWithSlash = rootPrefix.isEmpty() ? "" : rootPrefix + "/";
        final int prefixLen = prefixWithSlash.length();

        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements())
        {
            final ZipEntry entry = entries.nextElement();
            String name = entry.getName();

            // Handle root prefix scoping if specified
            if (!prefixWithSlash.isEmpty())
            {
                if (!name.startsWith(prefixWithSlash))
                {
                    continue;
                }
                name = name.substring(prefixLen);
            }

            if (name.endsWith("/"))
            {
                name = name.substring(0, name.length() - 1);
            }
            if (name.isEmpty())
            {
                continue;
            }

            // Exclude deeply nested dump/screenshot subdirectories from navigation index
            // to keep memory minimal (~8 MB). They are streamed directly on extractTo().
            final int outputIdx = name.indexOf("/output/");
            if (outputIdx >= 0)
            {
                name = name.substring(0, outputIdx + "/output".length());
                tempTree.computeIfAbsent(internString(name, stringPool), k -> new LinkedHashSet<>(0));
            }
            else
            {
                final int dumpIdx = name.indexOf("/dump/");
                if (dumpIdx >= 0)
                {
                    name = name.substring(0, dumpIdx + "/dump".length());
                    tempTree.computeIfAbsent(internString(name, stringPool), k -> new LinkedHashSet<>(0));
                }
                else if (entry.isDirectory())
                {
                    tempTree.computeIfAbsent(internString(name, stringPool), k -> new LinkedHashSet<>(0));
                }
            }

            // Record ancestors in directory tree
            String current = name;
            while (!current.isEmpty())
            {
                final int lastSlash = current.lastIndexOf('/');
                final String parent = internString(lastSlash >= 0 ? current.substring(0, lastSlash) : "", stringPool);
                final String child = internString(lastSlash >= 0 ? current.substring(lastSlash + 1) : current, stringPool);

                final LinkedHashSet<String> children = tempTree.computeIfAbsent(parent, k -> new LinkedHashSet<>(4));
                final boolean added = children.add(child);
                if (!added)
                {
                    // If child was already recorded, its ancestors were already processed
                    break;
                }
                current = parent;
            }
        }

        // Convert LinkedHashSet to compact sorted String[]
        final Map<String, String[]> compactTree = new HashMap<>(tempTree.size());
        for (final Map.Entry<String, LinkedHashSet<String>> entry : tempTree.entrySet())
        {
            final String[] arr = entry.getValue().toArray(new String[0]);
            Arrays.sort(arr);
            compactTree.put(entry.getKey(), arr);
        }

        return compactTree;
    }

    private static String internString(final String s, final Map<String, String> pool)
    {
        final String existing = pool.putIfAbsent(s, s);
        return existing != null ? existing : s;
    }

    /**
     * Implementation of {@link FsResource} for zip archive entries.
     */
    private static final class ZipFsResource implements FsResource
    {
        private final ZipFileSystem fs;
        private final String relativePath;

        private ZipFsResource(final ZipFileSystem fs, final String relativePath)
        {
            this.fs = fs;
            this.relativePath = relativePath;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName()
        {
            if (relativePath.isEmpty())
            {
                return "";
            }
            final int lastSlash = relativePath.lastIndexOf('/');
            if (lastSlash >= 0)
            {
                return relativePath.substring(lastSlash + 1);
            }
            return relativePath;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getPath()
        {
            return relativePath;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean exists()
        {
            if (relativePath.isEmpty())
            {
                return true;
            }
            if (fs.isKnownDirectory(relativePath))
            {
                return true;
            }

            final String fullPath = fs.getFullEntryPath(relativePath);
            final ZipFile zf = fs.getZipFile();
            if (zf.getEntry(fullPath) != null || zf.getEntry(fullPath + "/") != null)
            {
                return true;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDirectory()
        {
            if (relativePath.isEmpty())
            {
                return true;
            }
            if (fs.isKnownDirectory(relativePath))
            {
                return true;
            }

            final String fullPath = fs.getFullEntryPath(relativePath);
            final ZipFile zf = fs.getZipFile();
            if (zf.getEntry(fullPath + "/") != null)
            {
                return true;
            }
            final ZipEntry entry = zf.getEntry(fullPath);
            return entry != null && entry.isDirectory();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isFile()
        {
            if (relativePath.isEmpty() || fs.isKnownDirectory(relativePath))
            {
                return false;
            }

            final String fullPath = fs.getFullEntryPath(relativePath);
            final ZipEntry entry = fs.getZipFile().getEntry(fullPath);
            return entry != null && !entry.isDirectory();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getSize()
        {
            if (relativePath.isEmpty() || fs.isKnownDirectory(relativePath))
            {
                return 0L;
            }

            final String fullPath = fs.getFullEntryPath(relativePath);
            final ZipEntry entry = fs.getZipFile().getEntry(fullPath);
            return entry != null && !entry.isDirectory() ? entry.getSize() : 0L;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getLastModified()
        {
            final String fullPath = fs.getFullEntryPath(relativePath);
            final ZipFile zf = fs.getZipFile();
            ZipEntry entry = zf.getEntry(fullPath);
            if (entry == null)
            {
                entry = zf.getEntry(fullPath + "/");
            }
            return entry != null ? entry.getTime() : 0L;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream openStream() throws IOException
        {
            fs.checkOpen();

            if (isDirectory())
            {
                throw new IOException("Cannot open InputStream for a directory: " + relativePath);
            }

            final String fullPath = fs.getFullEntryPath(relativePath);
            final ZipEntry entry = fs.getZipFile().getEntry(fullPath);
            if (entry == null || entry.isDirectory())
            {
                throw new FileNotFoundException("Entry not found in zip archive: " + fullPath);
            }

            final InputStream in = fs.getZipFile().getInputStream(entry);
            if (in == null)
            {
                throw new IOException("Failed to open stream for entry: " + fullPath);
            }
            return in;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<FsResource> listChildren() throws IOException
        {
            fs.checkOpen();

            final String[] children = fs.getChildNames(relativePath);
            if (children == null || children.length == 0)
            {
                return Collections.emptyList();
            }

            final List<FsResource> list = new ArrayList<>(children.length);
            for (final String child : children)
            {
                final String childPath = relativePath.isEmpty() ? child : relativePath + "/" + child;
                list.add(new ZipFsResource(fs, childPath));
            }
            return Collections.unmodifiableList(list);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FsResource resolve(final String subPath)
        {
            final String norm = DirectoryFileSystem.normalizeRelativePath(subPath);
            if (norm.isEmpty())
            {
                return this;
            }
            final String newPath = relativePath.isEmpty() ? norm : relativePath + "/" + norm;
            return fs.resolve(newPath);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FsResource getParent()
        {
            if (relativePath.isEmpty())
            {
                return null;
            }

            final int lastSlash = relativePath.lastIndexOf('/');
            if (lastSlash >= 0)
            {
                return fs.resolve(relativePath.substring(0, lastSlash));
            }
            return fs.getRoot();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void extractTo(final Path localDestination) throws IOException
        {
            Objects.requireNonNull(localDestination, "localDestination must not be null");
            fs.checkOpen();

            if (!exists())
            {
                throw new FileNotFoundException("Source resource does not exist in archive: " + relativePath);
            }

            if (isFile())
            {
                final Path parent = localDestination.getParent();
                if (parent != null)
                {
                    Files.createDirectories(parent);
                }
                try (final InputStream in = openStream())
                {
                    Files.copy(in, localDestination, StandardCopyOption.REPLACE_EXISTING);
                }
                return;
            }

            if (isDirectory())
            {
                Files.createDirectories(localDestination);

                // Stream matching entries directly from the zip without holding memory
                final String prefix = fs.getFullEntryPath(relativePath);
                final String prefixWithSlash = prefix.isEmpty() ? "" : prefix + "/";
                final int prefixLen = prefixWithSlash.length();

                final Enumeration<? extends ZipEntry> entries = fs.getZipFile().entries();
                while (entries.hasMoreElements())
                {
                    final ZipEntry entry = entries.nextElement();
                    final String name = entry.getName();

                    if (!prefixWithSlash.isEmpty() && !name.startsWith(prefixWithSlash))
                    {
                        continue;
                    }

                    final String subPath = prefixWithSlash.isEmpty() ? name : name.substring(prefixLen);
                    if (subPath.isEmpty())
                    {
                        continue;
                    }

                    final Path targetPath = localDestination.resolve(subPath);
                    if (entry.isDirectory())
                    {
                        Files.createDirectories(targetPath);
                    }
                    else
                    {
                        final Path parent = targetPath.getParent();
                        if (parent != null)
                        {
                            Files.createDirectories(parent);
                        }
                        try (final InputStream in = fs.getZipFile().getInputStream(entry))
                        {
                            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Path> toLocalPath()
        {
            return Optional.empty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public VirtualFileSystem getFileSystem()
        {
            return fs;
        }

        @Override
        public String toString()
        {
            return "ZipFsResource[" + fs.getFullEntryPath(relativePath) + "]";
        }
    }
}
