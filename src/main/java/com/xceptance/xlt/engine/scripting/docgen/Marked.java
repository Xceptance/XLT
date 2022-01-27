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
package com.xceptance.xlt.engine.scripting.docgen;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.Extensions;
import org.pegdown.PDProc;
import org.pegdown.PegDownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used for Markdown-to-HTML conversion.
 *
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public final class Marked
{
    /** Class logger. */
    private static final Logger LOG = LoggerFactory.getLogger(Marked.class);

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
                LOG.error("Failed to generate HTML", se);
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
