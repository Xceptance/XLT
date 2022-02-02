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
package com.xceptance.common.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;

import com.xceptance.common.util.ParameterCheckUtils;

/**
 * Utility class that provides a convenient XSL transform method.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public final class XSLTUtils
{

    private static final XSLTUtils instance = new XSLTUtils();

    /**
     * Factory instance to use for obtaining a proper transformer.
     */
    private TransformerFactory factory = TransformerFactory.newInstance();

    /**
     * Default constructor. Private to prevent external instantiation.
     */
    private XSLTUtils()
    {
    }

    /**
     * Sets the transformer factory internally used to the given one.
     * 
     * @param factory
     *            new transformer factory
     */
    public static void setTransformerFactory(final TransformerFactory factory)
    {
        if (factory != null)
        {
            instance.setFactory(factory);
        }
    }

    /**
     * Sets the transformer factory.
     * 
     * @param factory
     *            transformer factory
     */
    private synchronized void setFactory(final TransformerFactory factory)
    {
        this.factory = factory;
    }

    /**
     * Performs the XML transformation.
     * 
     * @param in
     *            input file
     * @param out
     *            output file
     * @param styleSheet
     *            style sheet file
     * @param parameters
     *            the key/value pairs to be passed as parameters to the style sheet
     * @throws FileNotFoundException
     *             if the output file cannot be (re-)created
     * @throws TransformerException
     *             if a transformation error occurs
     */
    private void doTransform(final File in, final File out, final File styleSheet, final Map<String, Object> parameters)
        throws FileNotFoundException, TransformerException
    {
        // parameter check
        ParameterCheckUtils.isReadableFile(in, "inputXmlFile");
        ParameterCheckUtils.isReadableFile(styleSheet, "xsltStyleSheet");
        ParameterCheckUtils.isWritableFile(out, "outputFile");

        // create the transformer and set any parameters
        final Transformer transformer = factory.newTransformer(new StreamSource(styleSheet));

        if (parameters != null)
        {
            for (final Entry<String, Object> entry : parameters.entrySet())
            {
                transformer.setParameter(entry.getKey(), entry.getValue());
            }
        }

        // now do the transformation
        FileOutputStream fos = null;

        try
        {
            final Source xmlSource = new StreamSource(in);

            fos = new FileOutputStream(out);
            final StreamResult result = new StreamResult(fos);

            transformer.transform(xmlSource, result);
        }
        finally
        {
            IOUtils.closeQuietly(fos);
        }
    }

    /**
     * Transforms the given XML input file using the specified XSLT style sheet and writes the result to the passed
     * output file.
     * 
     * @param inputXmlFile
     *            the source file
     * @param outputFile
     *            the target file
     * @param xsltStyleSheet
     *            the style sheet file
     * @throws FileNotFoundException
     *             if the output file cannot be (re-)created
     * @throws TransformerException
     *             if a transformation error occurs
     */
    public static void transform(final File inputXmlFile, final File outputFile, final File xsltStyleSheet)
        throws FileNotFoundException, TransformerException
    {
        transform(inputXmlFile, outputFile, xsltStyleSheet, null);
    }

    /**
     * Transforms the given XML input file using the specified XSLT style sheet and writes the result to the passed
     * output file.
     * 
     * @param inputXmlFile
     *            the source file
     * @param outputFile
     *            the target file
     * @param xsltStyleSheet
     *            the style sheet file
     * @param parameters
     *            the key/value pairs to be passed as parameters to the style sheet
     * @throws FileNotFoundException
     *             if the output file cannot be (re-)created
     * @throws TransformerException
     *             if a transformation error occurs
     */
    public static void transform(final File inputXmlFile, final File outputFile, final File xsltStyleSheet,
                                 final Map<String, Object> parameters) throws FileNotFoundException, TransformerException
    {
        instance.doTransform(inputXmlFile, outputFile, xsltStyleSheet, parameters);
    }

    /**
     * Tries to transform the given XML input file using the specified XSLT style sheet and writes the result to the
     * passed output file. Any exception occurred during transformation will be caught.
     * 
     * @param inputXmlFile
     *            the source file
     * @param outputFile
     *            the target file
     * @param xsltStyleSheet
     *            the style sheet file
     */
    public static void tryTransform(final File inputXmlFile, final File outputFile, final File xsltStyleSheet)
    {
        try
        {
            transform(inputXmlFile, outputFile, xsltStyleSheet);
        }
        catch (final TransformerException e)
        {
            // ignore
        }
        catch (final FileNotFoundException e)
        {
            // ignore
        }
    }

}
