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
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A {@link VirtualFileSystem} implementation backed directly by a directory on the local disk filesystem.
 *
 * [AI: Gemini]
 */
final class DirectoryFileSystem implements VirtualFileSystem
{
    private final Path rootPath;
    private final DirectoryFsResource rootResource;

    /**
     * Constructs a new {@link DirectoryFileSystem} rooted at the given local directory path.
     *
     * @param rootPath the root directory path
     * @throws IOException if rootPath does not exist or is not a directory
     */
    DirectoryFileSystem(final Path rootPath) throws IOException
    {
        Objects.requireNonNull(rootPath, "rootPath must not be null");
        if (!Files.exists(rootPath))
        {
            throw new FileNotFoundException("Directory does not exist: " + rootPath);
        }
        if (!Files.isDirectory(rootPath))
        {
            throw new IOException("Path is not a directory: " + rootPath);
        }

        this.rootPath = rootPath.toAbsolutePath().normalize();
        this.rootResource = new DirectoryFsResource(this, this.rootPath, "");
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
        if (relativePath == null)
        {
            return rootResource;
        }
        if (relativePath.startsWith("/"))
        {
            final Path targetPath = Path.of(relativePath).normalize();
            return new DirectoryFsResource(this, targetPath, relativePath);
        }

        final String normalized = normalizeRelativePath(relativePath);
        if (normalized.isEmpty())
        {
            return rootResource;
        }

        final Path targetPath = rootPath.resolve(normalized).normalize();
        return new DirectoryFsResource(this, targetPath, normalized);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPlainDirectory()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Path> getLocalRootPath()
    {
        return Optional.of(rootPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getBasePath()
    {
        return rootPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException
    {
        // No system resources to close for a plain directory
    }

    /**
     * Normalizes a relative path string by stripping leading and trailing slashes and replacing backslashes.
     *
     * @param path the path to normalize
     * @return the normalized path
     */
    static String normalizeRelativePath(final String path)
    {
        if (path == null)
        {
            return "";
        }

        String p = path.replace('\\', '/').trim();
        while (p.startsWith("/"))
        {
            p = p.substring(1);
        }
        while (p.endsWith("/"))
        {
            p = p.substring(0, p.length() - 1);
        }
        while (p.contains("//"))
        {
            p = p.replace("//", "/");
        }
        if (p.isEmpty())
        {
            return "";
        }

        final String[] segments = p.split("/");
        final List<String> list = new ArrayList<>(segments.length);
        for (final String seg : segments)
        {
            if (seg.isEmpty() || seg.equals("."))
            {
                continue;
            }
            if (seg.equals(".."))
            {
                if (!list.isEmpty() && !list.get(list.size() - 1).equals(".."))
                {
                    list.remove(list.size() - 1);
                }
                else
                {
                    list.add("..");
                }
            }
            else
            {
                list.add(seg);
            }
        }
        return String.join("/", list);
    }

    /**
     * Implementation of {@link FsResource} for local filesystem entries.
     */
    private static final class DirectoryFsResource implements FsResource
    {
        private final DirectoryFileSystem fs;
        private final Path localPath;
        private final String relativePath;

        private DirectoryFsResource(final DirectoryFileSystem fs, final Path localPath, final String relativePath)
        {
            this.fs = fs;
            this.localPath = localPath;
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
                final Path fileName = localPath.getFileName();
                return fileName != null ? fileName.toString() : "";
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
            return Files.exists(localPath);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDirectory()
        {
            return Files.isDirectory(localPath);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isFile()
        {
            return Files.isRegularFile(localPath);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getSize()
        {
            try
            {
                if (Files.isRegularFile(localPath))
                {
                    return Files.size(localPath);
                }
            }
            catch (final IOException e)
            {
                // Fall through
            }
            return 0L;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getLastModified()
        {
            try
            {
                if (Files.exists(localPath))
                {
                    return Files.getLastModifiedTime(localPath).toMillis();
                }
            }
            catch (final IOException e)
            {
                // Fall through
            }
            return 0L;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream openStream() throws IOException
        {
            if (!Files.exists(localPath))
            {
                throw new FileNotFoundException("File not found: " + localPath);
            }
            if (Files.isDirectory(localPath))
            {
                throw new IOException("Cannot open InputStream for a directory: " + localPath);
            }
            return Files.newInputStream(localPath);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<FsResource> listChildren() throws IOException
        {
            if (!Files.isDirectory(localPath))
            {
                return Collections.emptyList();
            }

            final List<FsResource> children = new ArrayList<>();
            try (final DirectoryStream<Path> stream = Files.newDirectoryStream(localPath))
            {
                for (final Path childPath : stream)
                {
                    final String childName = childPath.getFileName().toString();
                    final String childRelativePath = relativePath.isEmpty() ? childName : relativePath + "/" + childName;
                    children.add(new DirectoryFsResource(fs, childPath, childRelativePath));
                }
            }

            children.sort(Comparator.comparing(FsResource::getName));
            return Collections.unmodifiableList(children);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FsResource resolve(final String subPath)
        {
            if (subPath != null && subPath.startsWith("/"))
            {
                return fs.resolve(subPath);
            }

            final String normalizedSub = normalizeRelativePath(subPath);
            if (normalizedSub.isEmpty())
            {
                return this;
            }

            final String newRelative = relativePath.isEmpty() ? normalizedSub : relativePath + "/" + normalizedSub;
            return fs.resolve(newRelative);
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

            if (relativePath.startsWith("/"))
            {
                final Path parentLocal = localPath.getParent();
                if (parentLocal == null)
                {
                    return null;
                }
                return fs.resolve(parentLocal.toString());
            }

            final int lastSlash = relativePath.lastIndexOf('/');
            if (lastSlash >= 0)
            {
                final String parentRel = relativePath.substring(0, lastSlash);
                return fs.resolve(parentRel);
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

            if (!Files.exists(localPath))
            {
                throw new FileNotFoundException("Source resource does not exist: " + localPath);
            }

            if (Files.isRegularFile(localPath))
            {
                final Path parent = localDestination.getParent();
                if (parent != null)
                {
                    Files.createDirectories(parent);
                }
                Files.copy(localPath, localDestination, StandardCopyOption.REPLACE_EXISTING);
                return;
            }

            if (Files.isDirectory(localPath))
            {
                Files.createDirectories(localDestination);
                final Path sourceBase = localPath;

                Files.walkFileTree(sourceBase, new SimpleFileVisitor<Path>()
                {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
                    {
                        final Path rel = sourceBase.relativize(dir);
                        final Path targetDir = localDestination.resolve(rel.toString());
                        Files.createDirectories(targetDir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
                    {
                        final Path rel = sourceBase.relativize(file);
                        final Path targetFile = localDestination.resolve(rel.toString());
                        final Path parent = targetFile.getParent();
                        if (parent != null)
                        {
                            Files.createDirectories(parent);
                        }
                        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Path> toLocalPath()
        {
            return Optional.of(localPath);
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
            return "DirectoryFsResource[" + localPath + "]";
        }
    }
}
