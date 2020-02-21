/*
 * File: Marked.java
 * Created on: Nov 26, 2014
 * 
 * Copyright 2014
 * Xceptance Software Technologies GmbH, Germany.
 */
package com.xceptance.xlt.engine.scripting.docgen;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pegdown.Extensions;
import org.pegdown.PDProc;
import org.pegdown.PegDownProcessor;

/**
 * Utility class used for Markdown-to-HTML conversion.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class Marked
{
    /** Class logger. */
    private static final Log LOG = LogFactory.getLog(Marked.class);

    /** Parser options. */
    private final int parserOptions = Extensions.STRIKETHROUGH | Extensions.TABLES | Extensions.HARDWRAPS |
                                      Extensions.FENCED_CODE_BLOCKS | Extensions.AUTOLINKS;

    /** Parsing timeout. */
    private final long parsingTimeout = 3000L;

    /**
     * Returns the singleton instance.
     * 
     * @return singleton instance
     */
    public static Marked getInstance()
    {
        return Singleton_Holder.INSTANCE;
    }

    /**
     * Transforms the given input string into HTML.
     * 
     * @param source
     *            the input string
     * @return the generated HTML code
     */
    public String markdownToHTML(final String source)
    {
        return doGenerateHTML(source);
    }

    /**
     * Generates HTML from the given Markdown input string.
     * 
     * @param source
     *            the Markdown input string
     * @return generated HTML code
     */
    private String doGenerateHTML(final String source)
    {
        String str = source;
        if (StringUtils.isNotBlank(str))
        {
            final long startTime = System.currentTimeMillis();
            try
            {
                // create the processor
                final PegDownProcessor proc = new PDProc(parserOptions, parsingTimeout);
                // ... and convert the source
                str = StringUtils.defaultString(proc.markdownToHtml(source), source);
            }
            catch (final Exception se)
            {
                LOG.error(se);
            }
            finally
            {
                LOG.debug("HTML generation took [ms]: " + (System.currentTimeMillis() - startTime));
            }
        }

        return str;
    }

    /**
     * Singleton holder. Implementation based on the <a
     * href="http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Initialization-on-demand holder
     * idiom</a>.
     */
    private static class Singleton_Holder
    {
        private static final Marked INSTANCE = new Marked();
    }
}
