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
package com.xceptance.xlt.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xceptance.common.util.ParameterCheckUtils;
import com.xceptance.xlt.api.data.DataProvider;
import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.common.XltConstants;

public class LineProvider
{
    /** Line providers mapped by the file name */
    private static final Map<Key, LineProvider> LINE_PROVIDERS = new ConcurrentHashMap<Key, LineProvider>();

    /** the read lines */
    private final List<String> lines;

    /**
     * Get an instance of the line provider for the given file.
     * 
     * @param fileName
     *            file to read
     * @param encoding
     *            file encoding
     * @param ignoreLineComments
     *            whether line comments should be ignored
     * @return the line reader for the given file
     * @throws IOException
     *             if reading the file has failed
     * @throws FileNotFoundException
     *             if there's no such file
     * @see DataProvider#DEFAULT_LINE_COMMENT_MARKER
     */
    public static LineProvider getInstance(final String fileName, final String encoding, final boolean ignoreLineComments)
        throws FileNotFoundException, IOException
    {
        final Key key = new Key(fileName, encoding, ignoreLineComments);
        LineProvider lineProvider = LINE_PROVIDERS.get(key);
        if (lineProvider == null)
        {
            synchronized (LINE_PROVIDERS)
            {
                // check again!
                lineProvider = LINE_PROVIDERS.get(key);
                if (lineProvider == null)
                {
                    lineProvider = new LineProvider(key);
                    LINE_PROVIDERS.put(key, lineProvider);
                }
            }
        }

        return lineProvider;
    }

    /**
     * Read the given file with the given encoding
     * 
     * @param key
     *            the key
     * @throws IOException
     *             if reading the file has failed
     * @throws FileNotFoundException
     *             if there's no such file
     */
    private LineProvider(final Key key) throws IOException, FileNotFoundException
    {
        ParameterCheckUtils.isNotNull(key, "key");
        ParameterCheckUtils.isNotNullOrEmpty(key.fileName, "fileName");
        ParameterCheckUtils.isNotNullOrEmpty(key.encoding, "encoding");

        // load the data
        final String dataDirectory = XltProperties.getInstance().getProperty(XltConstants.XLT_PACKAGE_PATH + ".data.directory",
                                                                             "config" + File.separatorChar + "data");
        final File file = new File(dataDirectory, key.fileName);

        lines = new ArrayList<String>();

        if (XltLogger.runTimeLogger.isDebugEnabled())
        {
            XltLogger.runTimeLogger.debug("Loading data from " + file.getAbsolutePath());
        }

        try (final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), key.encoding)))
        {
            String line = null;
            while ((line = br.readLine()) != null)
            {
                if (!line.startsWith(DataProvider.DEFAULT_LINE_COMMENT_MARKER) || !key.ignoreLineComments)
                {
                    lines.add(line);
                }
            }
        }
    }

    /**
     * Get the read lines.
     * 
     * @return the read lines
     */
    public List<String> getLines()
    {
        return Collections.unmodifiableList(lines);
    }

    /**
     * Combined key for file name and encoding
     */
    private static class Key
    {
        private final String fileName;

        private final String encoding;

        private final boolean ignoreLineComments;

        private Key(final String fileName, final String encoding, final boolean ignoreLineComments)
        {
            this.fileName = fileName;
            this.encoding = encoding;
            this.ignoreLineComments = ignoreLineComments;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object o)
        {
            if (o instanceof Key)
            {
                Key k = (Key) o;
                if (this.fileName.equals(k.fileName) && this.encoding.equals(k.encoding) && ignoreLineComments == k.ignoreLineComments)
                {
                    return true;
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
            final int prime = 17;
            int hash = fileName.hashCode();
            hash = hash * prime + encoding.hashCode();
            hash = hash * prime + (ignoreLineComments ? 37 : 31);
            return hash;
        }
    }
}
