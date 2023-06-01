/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.api.util.XltRandom;
import com.xceptance.xlt.common.XltConstants;

/**
 * The {@link DataProvider} class provides convenient access to a fixed set of test data strings, which is backed by a
 * data file. Each line in the data file represents exactly one data item.
 * <p>
 * This class does a basic processing of comment lines, for example header lines that describe the data. All lines that
 * start with the configured line comment marker are filtered out.
 * <p>
 * The specified data file is searched for in the XLT data directory, which is "[testsuite]/config/data" by default. You
 * may change this directory by setting the XLT property "com.xceptance.xlt.data.directory" to an appropriate value.
 * <p>
 * Note: Be careful when creating instances of this class, as each instance loads the respective data file into memory.
 * Typically, data providers can/should be shared among test users, so you should have to create just one instance. A
 * simple way to ensure that there will be only one instance is to use the provided {@link #getInstance(String)} factory
 * method. But you are free to create (using the constructors) and manage the instances on your own.
 * 
 * @see GeneralDataProvider
 * @author René Schwietzke (Xceptance Software Technologies GmbH)
 */
public class DataProvider
{
    /**
     * Languages supported.
     */
    public static final String DEFAULT = "default";

    /**
     * The default file encoding ("UTF-8").
     */
    public static final String DEFAULT_FILE_ENCODING = XltConstants.UTF8_ENCODING;

    /**
     * The default line comment character ("#").
     */
    public static final String DEFAULT_LINE_COMMENT_MARKER = "#";

    /**
     * The global set of data providers, keyed by the data file names.
     */
    private static final Map<String, DataProvider> dataProviders = new ConcurrentHashMap<String, DataProvider>();

    /**
     * Returns the data provider responsible for the given file name. If a data provider has not been requested yet for
     * this file name, then a new data provider is created, otherwise the previously created provider will be returned.
     * Note that the data providers will be initialized using {@link #DEFAULT_FILE_ENCODING} and
     * {@link #DEFAULT_LINE_COMMENT_MARKER}.
     * <p>
     * Use this method to ensure, that only a single data provider instance is created.
     * 
     * @param fileName
     *            the file name/path of the data file
     * @return the data provider
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     */
    public static DataProvider getInstance(final String fileName) throws FileNotFoundException, IOException
    {
        DataProvider dataProvider = dataProviders.get(fileName);
        if (dataProvider == null)
        {
            synchronized (dataProviders)
            {
                // check again!
                dataProvider = dataProviders.get(fileName);
                if (dataProvider == null)
                {
                    dataProvider = new DataProvider(fileName);
                    dataProviders.put(fileName, dataProvider);
                }
            }
        }

        return dataProvider;
    }

    /**
     * The list of data lines.
     */
    private final List<String> dataRows;

    /**
     * The string that starts a line comment.
     */
    private final String lineCommentMarker;

    /**
     * Creates a new {@link DataProvider} instance and initializes it with the data loaded from the given data file. The
     * data file is expected to be saved using {@link #DEFAULT_FILE_ENCODING}. Lines in the data file that start with
     * the {@link #DEFAULT_LINE_COMMENT_MARKER} are considered as comment lines.
     * 
     * @param fileName
     *            the name/path of the data file
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     */
    public DataProvider(final String fileName) throws IOException, FileNotFoundException
    {
        this(fileName, DEFAULT_FILE_ENCODING, DEFAULT_LINE_COMMENT_MARKER);
    }

    /**
     * Creates a new {@link DataProvider} instance and initializes it with the data loaded from the given data file. The
     * data file is expected to be saved using the passed encoding, for example "UTF-8" or "ISO-8859-1". Lines in the
     * data file that start with the {@link #DEFAULT_LINE_COMMENT_MARKER} are considered as comment lines.
     * 
     * @param fileName
     *            the name/path of the data file
     * @param encoding
     *            the data file encoding
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     */
    public DataProvider(final String fileName, final String encoding) throws IOException, FileNotFoundException
    {
        this(fileName, encoding, DEFAULT_LINE_COMMENT_MARKER);
    }

    /**
     * Creates a new {@link DataProvider} instance and initializes it with the data loaded from the given data file. The
     * data file is expected to be saved using the passed encoding, for example "UTF-8" or "ISO-8859-1". Lines in the
     * data file that start with the given line comment marker are considered as comment lines.
     * 
     * @param fileName
     *            the name/path of the data file
     * @param encoding
     *            the data file encoding
     * @param lineCommentMarker
     *            the line comment marker to be used (may be <code>null</code>)
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     */
    public DataProvider(final String fileName, final String encoding, final String lineCommentMarker)
        throws IOException, FileNotFoundException
    {
        ParameterCheckUtils.isNotNullOrEmpty(fileName, "fileName");
        ParameterCheckUtils.isNotNullOrEmpty(encoding, "encoding");

        this.lineCommentMarker = lineCommentMarker;

        // load the data
        final String dataDirectory = XltProperties.getInstance().getProperty(XltConstants.XLT_PACKAGE_PATH + ".data.directory",
                                                                             "config" + File.separatorChar + "data");
        final File file = new File(dataDirectory, fileName);

        dataRows = loadData(file, encoding);
    }

    /**
     * Returns all rows as an unmodifiable list to protect the data.
     * 
     * @return all rows or an empty list, if no data is available
     */
    public synchronized List<String> getAllRows()
    {
        return Collections.unmodifiableList(new ArrayList<>(dataRows));
    }

    /**
     * Returns a randomly-chosen data row.
     * 
     * @return a row, or <code>null</code> if the data set is empty
     */
    public String getRandomRow()
    {
        return getRandomRow(false);
    }

    /**
     * Returns a randomly-chosen data row.
     * 
     * @param removeWhitespace
     *            whether all whitespace is to be removed from the data
     * @return a row, or <code>null</code> if the data set is empty
     */
    public synchronized String getRandomRow(final boolean removeWhitespace)
    {
        final int r = XltRandom.nextInt(dataRows.size());

        return getRow(removeWhitespace, r);
    }

    /**
     * Returns the specified data row.
     * 
     * @param removeWhitespace
     *            whether all whitespace is to be removed from the data
     * @param rowNumber
     *            the number of the row to read
     * @return the row, or <code>null</code> if the specified row number exceeds the size of the data set
     */
    public synchronized String getRow(final boolean removeWhitespace, final int rowNumber)
    {
        if (rowNumber < 0 || rowNumber >= dataRows.size())
        {
            return null;
        }

        final String s = dataRows.get(rowNumber);

        return removeWhitespace ? StringUtils.deleteWhitespace(s) : s;
    }

    /**
     * Returns the specified data row.
     * 
     * @param rowNumber
     *            the number of the row to read
     * @return the row, or <code>null</code> if the specified row number exceeds the size of the data set
     */
    public String getRow(final int rowNumber)
    {
        return getRow(false, rowNumber);
    }

    /**
     * Adds a new data row at the specified row index to the internal store.
     * 
     * @param rowNumber
     *            the index of the new row
     * @param row
     *            the new row
     */
    public synchronized void addRow(final int rowNumber, String row)
    {
        dataRows.add(rowNumber, row);
    }

    /**
     * Adds a new data row as the last element to the internal store.
     * 
     * @param row
     *            the new row
     */
    public synchronized void addRow(final String row)
    {
        dataRows.add(row);
    }

    /**
     * Removes the data row with the specified row number from the internal store.
     * 
     * @param rowNumber
     *            the index of the row to remove
     * @return the row just removed, or <code>null</code> if the specified row number exceeds the size of the data set
     */
    public synchronized String removeRow(final int rowNumber)
    {
        if (rowNumber < 0 || rowNumber >= dataRows.size())
        {
            return null;
        }

        return dataRows.remove(rowNumber);
    }

    /**
     * Removes the first occurrence of the specified data row from the internal store.
     * 
     * @param row
     *            the row to remove
     * @return <code>true</code> if the row was present, <code>false</code> otherwise
     */
    public synchronized boolean removeRow(final String row)
    {
        return dataRows.remove(row);
    }

    /**
     * Returns the size of the data set.
     * 
     * @return the data set size
     */
    public int getSize()
    {
        return dataRows.size();
    }

    /**
     * Loads the data lines from the given file into a list.
     * 
     * @param file
     *            the data file to load
     * @param encoding
     *            the data file encoding
     * @return a list with the data lines
     * @throws FileNotFoundException
     *             if the data file cannot be found
     * @throws IOException
     *             if the data file cannot be opened or read
     */
    protected List<String> loadData(final File file, final String encoding) throws IOException, FileNotFoundException
    {
        if (XltLogger.runTimeLogger.isDebugEnabled())
        {
            XltLogger.runTimeLogger.debug("Loading data from " + file.getAbsolutePath());
        }

        final List<String> lines = FileUtils.readLines(file, encoding);

        // post-process the lines
        return processLines(lines);
    }

    /**
     * Post-processes the data lines just read. This method is responsible for comment line processing.
     * 
     * @param lines
     *            the data lines
     * @return the processed data lines
     */
    protected List<String> processLines(final List<String> lines)
    {
        final Iterator<String> iterator = lines.iterator();

        while (iterator.hasNext())
        {
            final String line = iterator.next();

            if (isCommentLine(line))
            {
                iterator.remove();
            }
        }

        return lines;
    }

    /**
     * Checks whether the given line is a comment line.
     * 
     * @param line
     *            the line
     * @return <code>true</code> if this line is a comment line, <code>false</code> otherwise
     */
    private boolean isCommentLine(final String line)
    {
        if (lineCommentMarker == null || lineCommentMarker.length() == 0)
        {
            return false;
        }
        else
        {
            return line.trim().startsWith(lineCommentMarker);
        }
    }
}
