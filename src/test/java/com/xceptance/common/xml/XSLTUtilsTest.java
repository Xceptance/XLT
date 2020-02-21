package com.xceptance.common.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.xceptance.xlt.AbstractXLTTestCase;

/**
 * Test the implementation of {@link XSLTUtils}.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class XSLTUtilsTest extends AbstractXLTTestCase
{
    /**
     * System dependent directory for temporary files.
     */
    protected final static File tempDir = getTempDir();

    /**
     * Test directory rooted at system dependent directory for temporary files. Uses random suffix to prevent file name
     * clashes.
     */
    protected final static String testDirName = "test" + new Random().nextInt(1000);

    /**
     * Input file.
     */
    protected File inputFile = null;

    /**
     * Output file.
     */
    protected File outputFile = null;

    /**
     * Style sheet.
     */
    protected File stylesheet = null;

    /**
     * Directory named 'test' rooted at system-specific temporary directory.
     */
    protected File testDir = null;

    /**
     * Static test fixture setup.
     * 
     * @throws Exception
     *             thrown then setup failed.
     */
    @Before
    public void intro() throws Exception
    {
        inputFile = File.createTempFile("input", ".xml");
        outputFile = File.createTempFile("output", ".xml");
        stylesheet = File.createTempFile("stylesheet", ".xsl");

        testDir = new File(tempDir, testDirName);
        Assert.assertTrue("Failed to create directory " + testDir.getName(), testDir.mkdir());
    }

    /**
     * Static test fixture cleanup.
     * 
     * @throws Exception
     *             thrown when cleanup failed.
     */
    @After
    public void outro() throws Exception
    {
        inputFile.delete();
        outputFile.delete();
        stylesheet.delete();

        testDir.delete();
    }

    /**
     * Test the implementation of {@link XSLTUtils#transform(File, File, File)} by using invalid parameters.
     */
    @Test
    public void testTransform_InvalidParameters() throws Throwable
    {
        // process all known parameters
        for (final Param p : Param.values())
        {
            checkInvalidParam(p);
        }
    }

    /**
     * Tests the implementation of {@link XSLTUtils#transform(File, File, File)} by using valid parameters.
     */
    @Test
    public void testTransform_ValidParameters()
    {
        // validate arguments
        validateArguments();
        // validate exception handling
        validateExceptionHandling();

    }

    /**
     * Validates the passed arguments.
     */
    private void validateArguments()
    {
        // mocked transformer factory
        final TransformerFactory facMock = mock(TransformerFactory.class);
        // mocked transformer
        final Transformer transformerMock = mock(Transformer.class);

        // let transformer mock return the transformer mock when
        // newInstance(Source) is called
        try
        {
            Mockito.doReturn(transformerMock).when(facMock).newTransformer((Source) Matchers.anyObject());
        }
        catch (final Throwable t)
        {
            Assert.fail("Failed to stub factory mock.");
        }

        // use an answer object to validate the arguments passed to
        // transform(Source,Result)
        try
        {
            // use doAnswer to be able to validate arguments passed to transform
            // method
            Mockito.doAnswer(new Answer<Object>()
            {
                @Override
                public Object answer(final InvocationOnMock invocation) throws Throwable
                {
                    final Object[] args = invocation.getArguments();
                    Assert.assertEquals(2, args.length);

                    final StreamSource in = (StreamSource) args[0];
                    Assert.assertEquals(new StreamSource(inputFile).getSystemId(), in.getSystemId());

                    final StreamResult out = (StreamResult) args[1];
                    Assert.assertEquals(new StreamResult(new FileOutputStream(outputFile)).getSystemId(), out.getSystemId());

                    return null;
                }
            }).when(transformerMock).transform((Source) Matchers.anyObject(), (Result) Matchers.anyObject());
        }
        catch (final Throwable t)
        {
            Assert.fail("Failed to stub transformer mock.");
        }

        // let XSLTUtils use the mocked transformer factory
        XSLTUtils.setTransformerFactory(facMock);

        // call and validate
        try
        {
            XSLTUtils.transform(inputFile, outputFile, stylesheet);
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

    }

    /**
     * Validates the exception handling of {@link XSLTUtils#transform(File, File, File)} and
     * {@link XSLTUtils#tryTransform(File, File, File)}.
     */
    private void validateExceptionHandling()
    {
        // mocked transformer factory
        final TransformerFactory facMock = mock(TransformerFactory.class);
        // mocked transformer
        final Transformer transformerMock = mock(Transformer.class);

        // stub transformer factory mock to return the transformer mock when
        // newTransformer(Source) is called
        try
        {
            Mockito.doReturn(transformerMock).when(facMock).newTransformer((Source) Matchers.anyObject());
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        // let XSLTUtils use the transformer factory mock
        XSLTUtils.setTransformerFactory(facMock);

        // validate handling of FileNotFoundExceptions
        testDir.delete();
        final File newOutFile = new File(testDir, "ghostFile");

        try
        {
            XSLTUtils.transform(inputFile, newOutFile, stylesheet);
            Assert.fail("XSLTUtils.transform(File,File,File) must throw an "
                        + "FileNotFoundException since output file does not exists and " + "its parent directory is not writable");
        }
        catch (final FileNotFoundException e)
        {
            // ignore
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        try
        {
            XSLTUtils.tryTransform(inputFile, newOutFile, stylesheet);
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        // validate handling of TransformerExceptions
        try
        {
            Mockito.doThrow(new TransformerException("")).when(transformerMock)
                   .transform((Source) Matchers.anyObject(), (Result) Matchers.anyObject());
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        try
        {
            XSLTUtils.transform(inputFile, outputFile, stylesheet);
            Assert.fail("XSLTUtils.transform(File,File,File) must throw an " + "TransformerException since "
                        + "Transformer.transform(Source,Result) throws it.");
        }
        catch (final TransformerException e)
        {
            // ignore
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

        try
        {
            XSLTUtils.tryTransform(inputFile, outputFile, stylesheet);
        }
        catch (final Throwable t)
        {
            failOnUnexpected(t);
        }

    }

    /**
     * Checks the given invalid parameter.
     * 
     * @param p
     *            invalid parameter
     */
    private void checkInvalidParam(final Param p) throws Throwable
    {
        // The parameter is invalid if it is
        // (1) the null reference
        // (2) not a file (that is a directory)
        // (3) not readable or not writable respectively

        // test (1)
        checkInvalidParam(null, p);

        // test (2)
        checkInvalidParam(testDir, p);

        // finally, test (3)
        try
        {
            // create new temporary file for test
            final File f = File.createTempFile("test", "");

            // set file to non-readable (INPUT and STYLESHEET)
            if (p == Param.OUTPUT)
            {
                f.setReadOnly();
            }
            // set file to non-writable (OUTPUT)
            else
            {
                f.delete();

            }

            // check parameter using the temporary file
            checkInvalidParam(f, p);

            // delete temporary file
            f.delete();
        }
        catch (final IOException ioe)
        {
            failOnUnexpected(ioe);
        }
    }

    /**
     * Checks the given invalid parameter using the given file.
     * 
     * @param f
     *            file to use for check
     * @param p
     *            invalid parameter
     */
    private void checkInvalidParam(final File f, final Param p) throws Throwable
    {
        // call transform() using the right parameters and expect an
        // IllegalArgumentException
        try
        {
            if (p.equals(Param.INPUT))
            {
                XSLTUtils.transform(f, outputFile, stylesheet);
            }
            else if (p.equals(Param.OUTPUT))
            {
                XSLTUtils.transform(inputFile, f, stylesheet);
            }
            else
            {
                XSLTUtils.transform(inputFile, outputFile, f);
            }

            Assert.fail("XSLTUtils.transform(File,File,File) must throw an " + "IllegalArgumentException since " + p.name + "is invalid.");
        }
        catch (final IllegalArgumentException e)
        {

        }

        // call tryTransform() using the right parameters and expect an
        // IllegalArgumentException
        try
        {
            if (p.equals(Param.INPUT))
            {
                XSLTUtils.tryTransform(f, outputFile, stylesheet);
            }
            else if (p.equals(Param.OUTPUT))
            {
                XSLTUtils.tryTransform(inputFile, f, stylesheet);
            }
            else
            {
                XSLTUtils.tryTransform(inputFile, outputFile, f);
            }

            Assert.fail("XSLTUtils.transform(File,File,File) must throw an " + "IllegalArgumentException since " + p.name + "is invalid.");
        }
        catch (final IllegalArgumentException e)
        {
        }

    }

    /**
     * Enumeration of known formal parameters of method {@link XSLTUtils#transform(File, File, File)}.
     */
    private enum Param
    {
        /**
         * Input XML file.
         */
        INPUT("inputXmlFile"),

        /**
         * Output XML file.
         */
        OUTPUT("outputFile"),

        /**
         * XSL file.
         */
        STYLESHEET("xsltStyleSheet");

        /**
         * Name of parameter (formal name).
         */
        String name;

        /**
         * Constructs a new parameter using the given name.
         * 
         * @param s
         *            name of parameter
         */
        Param(final String s)
        {
            name = s;
        }

    }
}
