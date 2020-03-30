/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.data.DataSetProviderException;

/**
 * @author Sebastian Oerding
 */
public class DomXmlDataSetProviderTest
{
    private final static String fileContents;

    static
    {
        final StringBuilder file = new StringBuilder(512);
        file.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        file.append("<bla-sets>\n<bla-set>\n");
        file.append("<userName>fred</userName>\n<password>topsecret</password>\n");
        file.append("</bla-set>\n<bla-set>\n");
        file.append("<userName>wilma</userName>\n<unknown>cantremember</unknown>\n<add> whatToDo  </add>\n");
        file.append("</bla-set>\n</bla-sets>\n");

        fileContents = file.toString();
    }

    @Test
    public void testGetAllDataSetsWithWellformedData()
    {
        File dataFile = null;
        try
        {
            dataFile = DummyDataSetProvider.writeTestDataSetFile(fileContents);
            final DomXmlDataSetProvider provider = new DomXmlDataSetProvider();
            final List<Map<String, String>> values = provider.getAllDataSets(dataFile);
            Assert.assertEquals("Wrong number of entries!", 2, values.size());
            Assert.assertEquals("Wrong value!", "fred", values.get(0).get("userName"));
            Assert.assertEquals("Wrong number of entries!", 3, values.get(1).size());
            Assert.assertEquals("Wrong value!", " whatToDo  ", values.get(1).get("add"));
        }
        finally
        {
            dataFile.delete();
        }
    }

    @Test
    public void testGetAllDataSetsWithMalformedData()
    {
        File dataFile = null;
        try
        {
            dataFile = DummyDataSetProvider.writeTestDataSetFile(fileContents + "bla");
            final DomXmlDataSetProvider provider = new DomXmlDataSetProvider();
            provider.getAllDataSets(dataFile);
            Assert.assertTrue("This code should not be reached as an exception is expected!", false);
        }
        catch (final DataSetProviderException e)
        {
            Assert.assertEquals("Unexpected error message!", "Failed to parse XML data file: " + dataFile.getName(), e.getMessage());
        }
        finally
        {
            dataFile.delete();
        }
    }
}
