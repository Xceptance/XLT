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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.xceptance.xlt.api.data.DataSetProvider;
import com.xceptance.xlt.api.data.DataSetProviderException;

/**
 * A {@link DataSetProvider} implementation that reads data sets from an XML file. The structure of the XML file is
 * expected to be as follows:
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="utf-8"?>
 * <data-sets>
 *     <data-set>
 *         <userName>fred</userName>
 *         <password>topsecret</password>
 *     </data-set>
 *     <data-set>
 *         <userName>wilma</userName>
 *         <password>cantremember</password>
 *     </data-set>
 * </data-sets>
 * }
 * </pre>
 * 
 * Note that the tag name of the root element and the elements at the second level do not matter, but the tag names at
 * the third level define the names of the test data parameters.
 */
public class DomXmlDataSetProvider implements DataSetProvider
{
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, String>> getAllDataSets(final File dataFile) throws DataSetProviderException
    {
        // parse the XML data file
        final Document document;

        try (final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(dataFile)))
        {
            final DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = parser.parse(inputStream);
        }
        catch (final Exception e)
        {
            throw new DataSetProviderException("Failed to parse XML data file: " + dataFile, e);
        }

        // create the data sets from the XML document
        final List<Map<String, String>> dataSets = new ArrayList<Map<String, String>>();

        Element dataSetElement = getFirstChildElement(document.getDocumentElement());
        while (dataSetElement != null)
        {
            final Map<String, String> dataSet = new LinkedHashMap<String, String>();
            dataSets.add(dataSet);

            Element valueElement = getFirstChildElement(dataSetElement);
            while (valueElement != null)
            {
                final String key = valueElement.getTagName();
                final String value = valueElement.getTextContent();

                dataSet.put(key, value);

                // advance to next value
                valueElement = getNextSiblingElement(valueElement);
            }

            // advance to next data set
            dataSetElement = getNextSiblingElement(dataSetElement);
        }

        return dataSets;
    }

    /**
     * Returns the first child element of the specified element.
     * 
     * @param element
     *            the element
     * @return the first child element, or <code>null</code> if there is none
     */
    private Element getFirstChildElement(final Element element)
    {
        Node node = element.getFirstChild();

        while (node != null)
        {
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                return (Element) node;
            }

            node = node.getNextSibling();
        }

        return null;
    }

    /**
     * Returns the next sibling element of the specified element.
     * 
     * @param element
     *            the element
     * @return the next sibling element, or <code>null</code> if there is none
     */
    private Element getNextSiblingElement(final Element element)
    {
        Node node = element.getNextSibling();

        while (node != null)
        {
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                return (Element) node;
            }

            node = node.getNextSibling();
        }

        return null;
    }
}
