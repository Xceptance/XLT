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
package com.xceptance.xlt.engine.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import com.xceptance.xlt.api.data.DataSetProvider;
import com.xceptance.xlt.api.data.DataSetProviderException;

/**
 * Dummy implementation of {@link DataSetProvider} for tests. Externalized cause reflectively instantiating this class
 * would otherwise fail.
 */
public class DummyDataSetProvider implements DataSetProvider
{
    @Override
    public List<Map<String, String>> getAllDataSets(final File dataFile) throws DataSetProviderException
    {
        return null;
    }

    /**
     * Convenience method to write a file.
     *
     * @return the file object for the written file
     * @throws IllegalStateException
     *             if anything goes wrong when writing the file
     */
    static File writeTestDataSetFile(final String contents) throws IllegalStateException
    {
        final File dataFile = new File("dataSetTestFile.csv");
        try (var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile))))
        {
            writer.write(contents); // flushing is done when invoking close()
        }
        catch (final FileNotFoundException e)
        {
            throw new IllegalStateException("Failed to create testFile on disk! Aborting test with exception!");
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("Failed to write to testFile on disk! Aborting test with exception!");
        }

        return dataFile;
    }
}
