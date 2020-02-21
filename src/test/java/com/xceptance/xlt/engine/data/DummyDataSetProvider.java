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
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile)));
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
        finally
        {
            if (writer != null)
            {
                try
                {
                    writer.close();
                }
                catch (final IOException e)
                {
                    throw new IllegalStateException("Failed to close stream to testFile on disk! Aborting test with exception!");
                }
            }
        }
        return dataFile;
    }
}
