/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.util.ExclusiveUtils;
import com.xceptance.xlt.util.LineProvider;

/**
 * Provides exclusive data access. The given file is partitioned so that every agent has an exclusive set of lines.
 * These lines get parsed into the dates to manage by this provider.
 *
 * @param <T>
 *            type of managed data
 */
public class ExclusiveDataProvider<T>
{
    /**
     * The exclusive data providers, mapped by the corresponding data file name and the used parser.
     */
    private static final Map<Key, ExclusiveDataProvider<?>> EXCLUSIVE_DATA_PROVIDERS = new ConcurrentHashMap<Key, ExclusiveDataProvider<?>>();

    /**
     * Parser that just returns the received lines.
     */
    public static final Parser<String> DEFAULT_PARSER = new Parser<String>()
    {
        @Override
        public List<String> parse(final List<String> lines)
        {
            return lines;
        }
    };

    /** Managed items */
    private final List<T> dataItems;

    /**
     * Creates a new {@link ExclusiveDataProvider} instance for String data and initializes it with the agent's
     * exclusive data partition loaded from the given data file. The data file is expected to be saved using
     * {@link DataProvider#DEFAULT_FILE_ENCODING}.
     * <p>
     * Line comments won't be handled in any way.
     * 
     * @param fileName
     *            the name/path of the data file
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     */
    public static ExclusiveDataProvider<String> getNewInstance(final String fileName) throws FileNotFoundException, IOException
    {
        return getNewInstance(fileName, DataProvider.DEFAULT_FILE_ENCODING);
    }

    /**
     * Creates a new {@link ExclusiveDataProvider} instance for String data and initializes it with the agent's
     * exclusive data partition loaded from the given data file. The data file is expected to be saved using
     * {@link DataProvider#DEFAULT_FILE_ENCODING}.
     * <p>
     * Line comments won't be handled in any way.
     *
     * @param fileName
     *            the name/path of the data file
     * @param encoding
     *            the encoding the file was saved with
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     */
    public static ExclusiveDataProvider<String> getNewInstance(final String fileName, final String encoding)
        throws FileNotFoundException, IOException
    {
        return new ExclusiveDataProvider<String>(fileName, encoding, false, getDefaultParser());
    }

    /**
     * Creates a new {@link ExclusiveDataProvider} instance and initializes it with the agent's exclusive data partition
     * loaded from the given data file. The data file is expected to be saved using
     * {@link DataProvider#DEFAULT_FILE_ENCODING}.
     *
     * @param fileName
     *            the name/path of the data file
     * @param filterLineComments
     *            whether line comments in given file should be automatically filtered out before content is handed over
     *            to given parser
     * @param parser
     *            parser to parse the content of the given file with
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    public ExclusiveDataProvider(final String fileName, final boolean filterLineComments, final Parser<T> parser)
        throws FileNotFoundException, IOException
    {
        this(fileName, DataProvider.DEFAULT_FILE_ENCODING, filterLineComments, parser);
    }

    /**
     * Creates a new {@link ExclusiveDataProvider} instance and initializes it with the agent's exclusive data partition
     * loaded from the given data file. The data file is expected to be saved using
     * {@link DataProvider#DEFAULT_FILE_ENCODING}.
     * <p>
     * Line comments won't be automatically filtered out and need to be handled by the given parser.
     * 
     * @param fileName
     *            the name/path of the data file
     * @param parser
     *            parser to parse the content of given file with
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    public ExclusiveDataProvider(final String fileName, final Parser<T> parser) throws FileNotFoundException, IOException
    {
        this(fileName, DataProvider.DEFAULT_FILE_ENCODING, false, parser);
    }

    /**
     * Creates a new {@link ExclusiveDataProvider} instance and initializes it with the agent's exclusive data partition
     * loaded from the given data file.
     * <p>
     * Line comments won't be automatically filtered out and need to be handled by the given parser.
     * 
     * @param fileName
     *            the name/path of the data file
     * @param encoding
     *            the file's encoding
     * @param parser
     *            parser to parse the content of given file with
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    public ExclusiveDataProvider(final String fileName, final String encoding, final Parser<T> parser)
        throws FileNotFoundException, IOException
    {
        this(fileName, encoding, false, parser);
    }

    /**
     * Creates a new {@link ExclusiveDataProvider} instance and initializes it with the agent's exclusive data partition
     * loaded from the given data file.
     *
     * @param fileName
     *            the name/path of the data file
     * @param encoding
     *            the file's encoding
     * @param filterLineComments
     *            whether line comments in given file should be automatically filtered out before content is handed over
     *            to given parser
     * @param parser
     *            parser to parse the content of the given file with
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    public ExclusiveDataProvider(final String fileName, final String encoding, final boolean filterLineComments, final Parser<T> parser)
        throws FileNotFoundException, IOException
    {
        super();

        final List<T> lines = loadData(fileName, encoding, filterLineComments, parser);
        dataItems = new ArrayList<T>(lines);
    }

    /**
     * Get the agent's exclusive data partition loaded from the given data file and parse this partition using the given
     * parser.
     *
     * @param fileName
     *            the name/path of the data file
     * @param encoding
     *            the file's encoding
     * @param filterLineComments
     *            whether line comments in given file should be automatically filtered out before content is handed over
     *            to given parser
     * @param parser
     *            parser to parse the content of the given file with
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    protected static <T> List<T> loadData(final String fileName, final String encoding, final boolean filterLineComments,
                                          final Parser<T> parser)
        throws FileNotFoundException, IOException
    {
        final LineProvider p = LineProvider.getInstance(fileName, encoding, filterLineComments);
        final List<String> exclusiveLines = ExclusiveUtils.getExclusiveAgentPart(p.getLines());
        return parse(exclusiveLines, parser);
    }

    /**
     * Get the agent's exclusive data partition loaded from the given data file and parse this partition using the given
     * parser.
     * <p>
     * Line comments won't be automatically filtered out and need to be handled by the given parser.
     * 
     * @param fileName
     *            the name/path of the data file
     * @param encoding
     *            the file's encoding
     * @param parser
     *            parser to parse the content of the given file with
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    protected static <T> List<T> loadData(final String fileName, final String encoding, final Parser<T> parser)
        throws FileNotFoundException, IOException
    {
        return loadData(fileName, encoding, false, parser);
    }

    /**
     * Parse the given lines and return the resulting data objects.
     *
     * @param lines
     *            the lines to parse
     * @param parser
     *            parser to convert the read lines to the desired object
     */
    protected static <T> List<T> parse(final List<String> lines, final Parser<T> parser)
    {
        return parser.parse(lines);
    }

    /**
     * Returns the exclusive data provider responsible for the given file name. If an exclusive data provider has not
     * been requested yet for this file name, then a new exclusive data provider is created, otherwise the previously
     * created provider will be returned. Note that the exclusive data providers will be initialized using
     * {@link DataProvider#DEFAULT_FILE_ENCODING} and the default parser ({@link #getDefaultParser()}).
     * <p>
     * Use this method to ensure, that only a single exclusive data provider instance is created.
     * <p>
     * Line comments won't be handled in any way.
     * 
     * @param fileName
     *            the file name/path of the data file
     * @return the exclusive data provider
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     */
    public static ExclusiveDataProvider<String> getInstance(final String fileName) throws FileNotFoundException, IOException
    {
        return getInstance(fileName, false, getDefaultParser());
    }

    /**
     * Returns the exclusive data provider responsible for the given file name. If an exclusive data provider has not
     * been requested yet for this file name, then a new exclusive data provider is created, otherwise the previously
     * created provider will be returned. Note that the exclusive data providers will be initialized using the default
     * parser {@link #getDefaultParser()}.
     * <p>
     * Use this method to ensure, that only a single exclusive data provider instance is created.
     *
     * @param fileName
     *            the file name/path of the data file
     * @param filterLineComments
     *            whether line comments in given file should be automatically filtered out before content is handed over
     *            to given parser
     * @return the exclusive data provider
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     */
    public static ExclusiveDataProvider<String> getInstance(final String fileName, final boolean filterLineComments)
        throws FileNotFoundException, IOException
    {
        return getInstance(fileName, filterLineComments, getDefaultParser());
    }

    /**
     * Returns the exclusive data provider responsible for the given file name. If an exclusive data provider has not
     * been requested yet for this file name, then a new exclusive data provider is created, otherwise the previously
     * created provider will be returned. Note that the exclusive data providers will be initialized using the default
     * parser {@link #getDefaultParser()}.
     * <p>
     * Use this method to ensure, that only a single exclusive data provider instance is created.
     * <p>
     * Line comments won't be handled in any way.
     *
     * @param fileName
     *            the file name/path of the data file
     * @param encoding
     *            file encoding
     * @return the exclusive data provider
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     */
    public static ExclusiveDataProvider<String> getInstance(final String fileName, final String encoding)
        throws FileNotFoundException, IOException
    {
        return getInstance(fileName, encoding, false, getDefaultParser());
    }

    /**
     * Returns the exclusive data provider responsible for the given file name. If an exclusive data provider has not
     * been requested yet for this file name, then a new exclusive data provider is created, otherwise the previously
     * created provider will be returned.
     * <p>
     * Use this method to ensure, that only a single exclusive data provider instance is created.
     * <p>
     * Line comments won't be automatically filtered out and need to be handled by the given parser.
     * 
     * @param fileName
     *            the file name/path of the data file
     * @param encoding
     *            file encoding
     * @param parser
     *            parser to parse the lines with
     * @return the exclusive data provider
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    public static <T> ExclusiveDataProvider<T> getInstance(final String fileName, final String encoding, final Parser<T> parser)
        throws FileNotFoundException, IOException
    {
        return getInstance(fileName, encoding, false, parser);
    }

    /**
     * Returns the exclusive data provider responsible for the given file name. If an exclusive data provider has not
     * been requested yet for this file name, then a new exclusive data provider is created, otherwise the previously
     * created provider will be returned. Note that the exclusive data providers will be initialized using
     * {@link DataProvider#DEFAULT_FILE_ENCODING}.
     * <p>
     * Use this method to ensure, that only a single exclusive data provider instance is created.
     *
     * @param fileName
     *            the file name/path of the data file
     * @param parser
     *            parser to parse the lines with
     * @param filterLineComments
     *            whether line comments in given file should be automatically filtered out before content is handed over
     *            to given parser
     * @return the exclusive data provider
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    public static <T> ExclusiveDataProvider<T> getInstance(final String fileName, final boolean filterLineComments, final Parser<T> parser)
        throws FileNotFoundException, IOException
    {
        return getInstance(fileName, DataProvider.DEFAULT_FILE_ENCODING, filterLineComments, parser);
    }

    /**
     * Returns the exclusive data provider responsible for the given file name. If an exclusive data provider has not
     * been requested yet for this file name, then a new exclusive data provider is created, otherwise the previously
     * created provider will be returned. Note that the exclusive data providers will be initialized using
     * {@link DataProvider#DEFAULT_FILE_ENCODING}.
     * <p>
     * Use this method to ensure, that only a single exclusive data provider instance is created.
     * <p>
     * Line comments won't be automatically filtered out and need to be handled by the given parser.
     *
     * @param fileName
     *            the file name/path of the data file
     * @param parser
     *            parser to parse the lines with
     * @return the exclusive data provider
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    public static <T> ExclusiveDataProvider<T> getInstance(final String fileName, final Parser<T> parser)
        throws FileNotFoundException, IOException
    {
        return getInstance(fileName, DataProvider.DEFAULT_FILE_ENCODING, false, parser);
    }

    /**
     * Returns the exclusive data provider responsible for the given file name. If an exclusive data provider has not
     * been requested yet for this file name, then a new exclusive data provider is created, otherwise the previously
     * created provider will be returned.
     * <p>
     * Use this method to ensure, that only a single exclusive data provider instance is created.
     *
     * @param fileName
     *            the file name/path of the data file
     * @param encoding
     *            file encoding
     * @param filterLineComments
     *            whether line comments in given file should be automatically filtered out before content is handed over
     *            to given parser
     * @param parser
     *            parser to parse the lines with
     * @return the exclusive data provider
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    @SuppressWarnings("unchecked")
    public static <T> ExclusiveDataProvider<T> getInstance(final String fileName, final String encoding, final boolean filterLineComments,
                                                           final Parser<T> parser)
        throws FileNotFoundException, IOException
    {
        final Key key = new Key(fileName, parser);
        ExclusiveDataProvider<T> edp = (ExclusiveDataProvider<T>) EXCLUSIVE_DATA_PROVIDERS.get(key);
        if (edp == null)
        {
            synchronized (EXCLUSIVE_DATA_PROVIDERS)
            {
                // check again!
                edp = (ExclusiveDataProvider<T>) EXCLUSIVE_DATA_PROVIDERS.get(key);
                if (edp == null)
                {
                    edp = new ExclusiveDataProvider<T>(fileName, encoding, filterLineComments, parser);
                    EXCLUSIVE_DATA_PROVIDERS.put(key, edp);
                }
            }
        }

        return edp;
    }

    /**
     * Allocate the next available item for exclusive use.
     *
     * @return the first item or <code>null</code> if no item is available
     */
    public synchronized T get()
    {
        if (!dataItems.isEmpty())
        {
            return dataItems.remove(0);
        }

        return null;
    }

    /**
     * Allocate a random item for exclusive use.
     *
     * @return a random item or <code>null</code> if no item is available
     */
    public synchronized T getRandom()
    {
        final int size = dataItems.size();
        if (size > 0)
        {
            return dataItems.remove(XltRandom.nextInt(size));
        }

        return null;
    }

    /**
     * Add a new or release a previously received exclusive item.
     *
     * @param item
     *            the item to add
     */
    public synchronized void add(final T item)
    {
        dataItems.add(item);
    }

    /**
     * Get the number of available items.
     *
     * @return number of available items
     */
    public synchronized int size()
    {
        return dataItems.size();
    }

    /**
     * Default parser that just returns the lines it receives.
     *
     * @return list of lines
     */
    public static Parser<String> getDefaultParser()
    {
        return DEFAULT_PARSER;
    }

    /**
     * Implement this parser to use parsed
     *
     * @param <T>
     *            parsing result type
     */
    public static abstract class Parser<T> extends Object
    {
        public abstract List<T> parse(final List<String> data);
    }

    /**
     * Combined key for file name and parser.
     */
    private static final class Key
    {
        private final String fileName;

        private final Parser<?> parser;

        public Key(final String fileName, final Parser<?> parser)
        {
            if (StringUtils.isBlank(fileName) || parser == null)
            {
                throw new IllegalArgumentException("The file name must not be blank and parser must not be NULL.");
            }
            this.fileName = fileName;
            this.parser = parser;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object o)
        {
            if (o != null)
            {
                if (o == this)
                {
                    return true;
                }
                else if (o.getClass() == this.getClass())
                {
                    final Key k = (Key) o;
                    if (k.fileName.equals(this.fileName) && k.parser == this.parser)
                    {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            int hash = 17;
            hash = hash * 13 + fileName.hashCode();
            hash = hash * 39 + parser.hashCode();
            return hash;
        }
    }
}
