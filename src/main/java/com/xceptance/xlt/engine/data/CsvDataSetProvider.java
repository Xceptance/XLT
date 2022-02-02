/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.xceptance.common.util.CsvUtils;
import com.xceptance.xlt.api.data.DataSetProvider;
import com.xceptance.xlt.api.data.DataSetProviderException;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

/**
 * A {@link DataSetProvider} implementation that reads data sets from a CSV file. The structure of the data file is as
 * follows:
 * 
 * <pre>
 * userName,password
 * fred,topsecret
 * wilma,cantremember
 * </pre>
 * 
 * The first line defines the names of the parameters, while the following lines specify the parameter values.
 */
public class CsvDataSetProvider implements DataSetProvider
{
    /**
     * The prefix for all CSV data provider properties.
     */
    private static final String PROP_PREFIX = XltConstants.XLT_PACKAGE_PATH + ".data.dataSetProviders.csv.";

    /**
     * Name of property specifying the line comment marker string.
     */
    private static final String PROP_COMMENT_MARKER = PROP_PREFIX + "lineCommentMarker";

    /**
     * Name of property specifying the CSV field separator character.
     */
    private static final String PROP_FIELD_SEPARATOR = PROP_PREFIX + "separator";

    /**
     * Name of property specifying the CSV file encoding.
     */
    private static final String PROP_FILE_ENCODING = PROP_PREFIX + "encoding";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, String>> getAllDataSets(final File dataFile) throws DataSetProviderException
    {
        // get the settings from the configuration
        final XltProperties props = XltProperties.getInstance();

        final String encoding = props.getProperty(PROP_FILE_ENCODING, "UTF-8");
        final char fieldSeparator = props.getProperty(PROP_FIELD_SEPARATOR, ",").trim().charAt(0);
        final String commentMarker = props.getProperty(PROP_COMMENT_MARKER, "#");

        // process the data file
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), encoding)))
        {
            final List<Map<String, String>> dataSets = new ArrayList<>();
            String[] keys = null;

            String line;
            int lineNo = 1;

            while ((line = in.readLine()) != null)
            {
                if (!commentMarker.isEmpty() && line.startsWith(commentMarker))
                {
                    // ignore comment line
                }
                else
                {
                    final String[] dataRecord = CsvUtils.decode(line, fieldSeparator);

                    if (keys == null)
                    {
                        // the first data record read determines the parameter names
                        keys = dataRecord;
                    }
                    else
                    {
                        if (keys.length != dataRecord.length)
                        {
                            throw new DataSetProviderException(String.format("Invalid data record in line %d: Expected %d fields, but found %d",
                                                                             lineNo, keys.length, dataRecord.length));
                        }

                        final Map<String, String> dataSet = new LinkedHashMap<>();
                        dataSets.add(dataSet);

                        for (int i = 0; i < keys.length; i++)
                        {
                            dataSet.put(keys[i], dataRecord[i]);
                        }
                    }
                }

                lineNo++;
            }

            return dataSets;
        }
        catch (final IOException ex)
        {
            throw new DataSetProviderException("Failed to read data sets from CSV file: " + dataFile, ex);
        }
    }
}
