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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import com.xceptance.common.util.ParseUtils;
import com.xceptance.common.util.PropertiesUtils;

/**
 * This class is a convenience to get files which should be included. This is especially due to the feature of property
 * files included by include... properties. This class also resolves cycles as described in the specification for ticket
 * #1650. It can also handle reference on directories and will load all property files there alphabetically.
 *
 * @author Sebastian Oerding
 * @author René Schwietzke
 */
public final class PropertyIncludeResolver
{
    /**
     * The name prefix for our special "include" properties.
     */
    static final String PROP_INCLUDE_NAME_PREFIX = "com.xceptance.xlt.propertiesInclude";

    /**
     * Returns a list with the canonical paths to all files which should be included. This includes transitive includes.
     * Furthermore the order in the list (as given by the index) defines the order in which the files have to be loaded
     * to get the correct results. The order is important in the case that properties are overwritten as the last one
     * wins.
     *
     * @param propertyFiles
     *            the property files to use as starting point to resolve includes
     * @param homeDirectory
     *            the root directory that acts as file-system boundary
     * @param configDirectory
     *            our config route and base we determine all data relative to that
     * @return a list with the absolute paths to the files to include as described above
     */
    public static List<PropertyIncludeResult> resolve(final FileObject homeDirectory, final FileObject configDirectory, final List<PropertyInclude> propertyFiles)
    {
        /*
         * We use an ArrayList to store the absolute file names of the files we have already read in. We use the
         * absolute names as we otherwise would have to recompute the files to the same directory to properly handle the
         * case of files with the same name in different directories. We use a list as we need later on the order when
         * applying the files (different orders may give different resulting properties if properties are overwritten)
         *
         * We have two input queues, the main input and the local input, because we want to load each file, resolve the includes and
         * if this is done, we load the next, we need two queues to maintain the input, resolved, input, resolved order
         */
        final Deque<PropertyInclude> inputFiles = new ArrayDeque<>(propertyFiles);
        final Deque<PropertyInclude> toProcess = new ArrayDeque<>();

        final List<PropertyIncludeResult> processed = new ArrayList<>();
        final Set<String> previousFiles = new HashSet<>();

        while (!toProcess.isEmpty() || !inputFiles.isEmpty())
        {
            final boolean isInclude;
            final PropertyInclude propertyFile;
            if (toProcess.isEmpty())
            {
                propertyFile = inputFiles.pollFirst();
                isInclude = false;
            }
            else
            {
                propertyFile = toProcess.pollFirst();
                isInclude = true;
            }

            // did we see that before?
            if (previousFiles.contains(propertyFile.file.getPublicURIString()))
            {
                processed.add(new PropertyIncludeResult(propertyFile.file, propertyFile.name, configDirectory, false, true, false, isInclude));
                continue;
            }
            else
            {
                previousFiles.add(propertyFile.file.getPublicURIString());
            }

            // ok, sanity check... ensure it is within the root hierarchy
            if (verifyRootDir(homeDirectory, propertyFile.file) == false)
            {
                processed.add(new PropertyIncludeResult(propertyFile.file, propertyFile.name, configDirectory, false, false, true, isInclude));
                continue; // we, we won't check that anymore because it outside of what is ours
            }

            try
            {
                // do we have that?
                if (propertyFile.file.exists() == false)
                {
                    // no, add and mark
                    processed.add(new PropertyIncludeResult(propertyFile.file, propertyFile.name, configDirectory, false, false, false, isInclude));

                    continue;
                }

                // yes
                if (propertyFile.file.isFile())
                {
                    processed.add(new PropertyIncludeResult(propertyFile.file, propertyFile.name, configDirectory, true, false, false, isInclude));

                    // resolve includes
                    List<PropertyInclude> includes = resolveIncludes(propertyFile.file);
                    toProcess.addAll(includes);
                }
                else if (propertyFile.file.isFolder())
                {
                    // get all files
                    final List<PropertyInclude> includes = getFilesOrderedByName(configDirectory, propertyFile.file);
                    toProcess.addAll(includes);
                }
            }
            catch (FileSystemException e)
            {
                // ignore
            }
        }

        return processed;
    }

    /**
     * Try to find out if our file is below rootDir or not
     *
     * @param toCheckFile the file to verify
     * @param homeDirectory our base dir
     * @return true if this is a valid subdir or subFile, false otherwise
     */
    @SuppressWarnings("resource")
    private static boolean verifyRootDir(final FileObject homeDirectory, final FileObject toCheckFile)
    {
        boolean isPartOfRootDir = false;

        var file = toCheckFile;
        try
        {
            if (toCheckFile.isFile() == false)
            {
                file = toCheckFile.getParent();
            }

            do
            {
                if (file.equals(homeDirectory))
                {
                    isPartOfRootDir = true;
                    break;
                }
            }
            while ((file = file.getParent()) != null);
        }
        catch (IOException e)
        {
            // ignore
        }

        return isPartOfRootDir;
    }

    /**
     * Extracts the relative name
     *
     * @param target the file to extract the name and path from
     * @param rootDirectory our base dir
     * @return a string with the name and path (excluding root) or just the filename of the root and the target have nothing in common
     * @throws FileSystemException
     * @throws
     */
    public static String extractName(final FileObject rootDirectory, final FileObject target, final String originalName)
    {
        // subtract to base name
        var targetName = target.getName();
        var rootName = rootDirectory.getName();

        if (!target.equals(rootDirectory) && verifyRootDir(rootDirectory, target))
        {
            try
            {
                return rootName.getRelativeName(targetName);
            }
            catch (FileSystemException e)
            {
                // not interested
            }
        }

        return originalName;
    }

    private static List<PropertyInclude> resolveIncludes(final FileObject file)
    {
        try
        {
            var properties = getPropertiesFromFile(file);

            final List<PropertyInclude> includes = new ArrayList<>();

            final FileObject baseDir = file.getParent();

            for (final String path : getOrderedIncludes(properties))
            {
                final var f = baseDir.resolveFile(path);
                final var s = extractName(file, f, path);
                includes.add(new PropertyInclude(f, s));
            }

            return includes;
        }
        catch (IOException e)
        {
        }

        return List.of();
    }

    /**
     * Returns all files contained in the argument directory which are not itself a directory and whose name ends with
     * &quot;.properties&quot;.
     *
     * @param configDirectory
     *                  the base directory to search from, going up is ok as long as we are not leaving the later
     *                  checked base dir (mostly the test suite home)
     * @param directory the directory to resolve and pull the files from
     * @return a list of file names sorted alphabetically
     */
    private static List<PropertyInclude> getFilesOrderedByName(final FileObject configDirectory, final FileObject directory)
    {
        final List<PropertyInclude> orderedIncludes = new ArrayList<>();

        final FileObject[] files;
        try
        {
            files = directory.getChildren();
        }
        catch (final FileSystemException fse)
        {
            throw new IllegalStateException("Failed to get children of directory '" + directory.getName().getPath() + "'");
        }

        if (files != null)
        {
            for (final FileObject child : files)
            {
                try
                {
                    if (!child.isFolder() && child.getName().getExtension().equals("properties"))
                    {
                        final var originalName = child.getName().getBaseName();
                        orderedIncludes.add(new PropertyInclude(child,
                                                                extractName(configDirectory, child, originalName)));
                    }

                }
                catch (final FileSystemException fse)
                {
                    throw new IllegalStateException("Failed to determine type of file '" + child.getName().getPath() + "'");
                }
            }
        }

        // sort them
        Collections.sort(orderedIncludes);

        return orderedIncludes;
    }

    /**
     * Loads and returns the properties from the given file.
     *
     * @param current
     *            the file which to load into properties to get its includes
     * @return a properties object with the contents of the argument file loaded into it
     * @throws IOException
     * @throws IllegalStateException
     *             if the argument file does not exist, is a directory or can not be read
     */
    private static Properties getPropertiesFromFile(final FileObject current) throws IOException
    {
        return PropertiesUtils.loadProperties(current);
    }

    /**
     * Gets the includes from the argument properties and returns them sorted numerically according to the number of the
     * include.
     *
     * @param properties
     *            the properties from which to get the includes
     * @return a sorted list with the includes from the argument properties
     */
    private static List<String> getOrderedIncludes(final Properties properties)
    {
        // get the include entries
        final Map<String, String> unsortedIncludes = PropertiesUtils.getPropertiesForKey(PROP_INCLUDE_NAME_PREFIX, properties);

        // now sort the include entries numerically by their index
        final TreeMap<Integer, String> sortedIncludes = new TreeMap<Integer, String>();
        for (final Entry<String, String> entry : unsortedIncludes.entrySet())
        {
            final String key = entry.getKey();

            int index;
            try
            {
                index = ParseUtils.parseInt(key);
            }
            catch (final ParseException e)
            {
                throw new IllegalArgumentException(String.format("Failed to parse the suffix '%s' of property '%s' as an integer", key,
                                                                 PROP_INCLUDE_NAME_PREFIX + "." + key));
            }

            sortedIncludes.put(index, entry.getValue());
        }

        // finally return the file names as list
        return new ArrayList<String>(sortedIncludes.values());
    }

    public static class PropertyIncludeResult
    {
        /**
         * The file system object, might point to something non-existent
         */
        public final FileObject file;

        /**
         * The human version of the name without any locked up file system
         */
        public final String name;

        /**
         * To avoid that we have to check file again, we store the result if this is truly a
         * file and exists
         */
        public final boolean exists;

        /**
         * Have we already dealt with it? Could prevent cyclic includes and
         * double includes with unexpected results. Deal with it later at your
         * own discretion.
         */
        public final boolean seenBefore;

        /**
         * Indicate that this file is not within the root dir and should not be used
         */
        public final boolean outsideORootDirScope;

        /**
         * Mark as include if any
         */
        public final boolean isInclude;

        /**
         * Constructor
         * @param file
         * @param name
         * @param exists
         * @param seenBefore
         * @param outsideORootDirScope
         */
        public PropertyIncludeResult(final FileObject file, final String name, final FileObject rootDir, boolean exists, boolean seenBefore, boolean outsideORootDirScope, boolean isInclude)
        {
            this.file = file;
            this.name = name;
            this.exists = exists;
            this.seenBefore = seenBefore;
            this.outsideORootDirScope = outsideORootDirScope;
            this.isInclude = isInclude;
        }
    }

    public static class PropertyInclude implements Comparable<PropertyInclude>
    {
        /**
         * The file system object, might point to something non-existent
         */
        public final FileObject file;

        /**
         * The human version of the name without any looked up file system
         */
        public final String name;

        /**
         * Constructor
         * @param file the file object from the FS
         * @param name the name we used to look that up initially
         */
        public PropertyInclude(final FileObject file, final String name)
        {
            this.file = file;
            this.name = name;
        }

        @Override
        public int compareTo(PropertyInclude o)
        {
            return file.compareTo(o.file);
        }
    }
}
