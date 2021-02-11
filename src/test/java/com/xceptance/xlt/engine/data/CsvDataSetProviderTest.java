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
package com.xceptance.xlt.engine.data;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.data.DataSetProviderException;

/**
 * @author Sebastian Oerding
 */
public class CsvDataSetProviderTest
{
    private final static String fileContents;

    static
    {
        final StringBuilder file = new StringBuilder(512);
        file.append("time,first, second, third \n");
        file.append("123000,Go, Chess,Backgammon\n");
        file.append("246000,Roy,    Ina,Ola\n");
        file.append("#12345,Rene,Ronny,Ika\n");
        file.append("369000,Martin Odersky,Josh Bloch,Robert C. Martin");
        fileContents = file.toString();
    }

    @Test
    public void testGetAllDataSets()
    {
        File dataFile = null;
        try
        {
            dataFile = DummyDataSetProvider.writeTestDataSetFile(fileContents);

            final CsvDataSetProvider cdsp = new CsvDataSetProvider();
            final List<Map<String, String>> dataSets = cdsp.getAllDataSets(dataFile);

            Assert.assertEquals("Wrong number of entries", 3, dataSets.size());
            Assert.assertEquals("Wrong number of entries", 4, dataSets.get(0).size());
            Assert.assertEquals("Wrong number of entries", 4, dataSets.get(1).size());
            Assert.assertEquals("Wrong number of entries", 4, dataSets.get(2).size());
            Assert.assertEquals("Wrong entry", "123000", dataSets.get(0).get("time"));
            Assert.assertEquals("Wrong entry", " Chess", dataSets.get(0).get(" second"));
            Assert.assertEquals("Wrong entry", "    Ina", dataSets.get(1).get(" second"));
            Assert.assertEquals("Wrong entry", "Robert C. Martin", dataSets.get(2).get(" third "));
        }
        finally
        {
            dataFile.delete();
        }
    }

    @Test
    public void testGetAllDataSetsForNonExistingFile()
    {
        try
        {
            new CsvDataSetProvider().getAllDataSets(new File("Huhu.csv"));
            Assert.fail("This code should not be reached as an exception is expected!");
        }
        catch (final DataSetProviderException e)
        {
            Assert.assertTrue("Unexpected error message : " + e.getMessage(),
                              e.getMessage().startsWith("Failed to read data sets from CSV file: "));
        }
    }

    @Test
    public void testGetAllDataSetsWithMalformedFile()
    {
        final String contents = fileContents + ",James Gosling";
        final File dataFile = DummyDataSetProvider.writeTestDataSetFile(contents);
        try
        {
            new CsvDataSetProvider().getAllDataSets(dataFile);
            Assert.fail("This code should not be reached as an exception is expected!");
        }
        catch (final DataSetProviderException e)
        {
            Assert.assertEquals("Unexpected error message!", "Invalid data record in line 5: Expected 4 fields, but found 5",
                                e.getMessage());
        }
        finally
        {
            dataFile.delete();
        }
    }

    @Test
    public void testGetAllDataSetsWithEmptyFile() throws IOException
    {
        final File dataFile = new File("dataSetTestFile.csv");
        try
        {
            dataFile.createNewFile();
            final List<Map<String, String>> dataSets = new CsvDataSetProvider().getAllDataSets(dataFile);
            
            Assert.assertEquals("Wrong number of entries", 0, dataSets.size());
        }
        finally
        {
            dataFile.delete();
        }
    }
}
