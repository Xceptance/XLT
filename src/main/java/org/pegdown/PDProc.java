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
package org.pegdown;

import java.util.List;
import java.util.Map;

import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.pegdown.Parser.ParseRunnerProvider;
import org.pegdown.ast.Node;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

/**
 * Specialization of PegDown's Markdown-to-HTML processor that uses our custom parser and HTML serializer.
 */
public class PDProc extends PegDownProcessor
{
    /** Default parse runner. */
    private static final ParseRunnerProvider DEFAULT = new ParseRunnerProvider()
    {
        @Override
        public ParseRunner<Node> get(Rule rule)
        {
            return new ReportingParseRunner<Node>(rule);
        }
    };

    /**
     * Constructor.
     * 
     * @param options
     *            the parsing options
     * @param parseTimeout
     *            the parsing timeout in msec
     */
    public PDProc(final int options, final long parseTimeout)
    {
        super(Parboiled.createParser(PDParser.class, options, parseTimeout, DEFAULT, PegDownPlugins.NONE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String markdownToHtml(char[] markdownSource, LinkRenderer linkRenderer, Map<String, VerbatimSerializer> verbatimSerializerMap,
                                 List<ToHtmlSerializerPlugin> plugins)
    {
        try
        {
            final RootNode ast = parseMarkdown(markdownSource);
            return new HtmlSerializer(linkRenderer, verbatimSerializerMap, plugins).toHtml(ast);
        }
        catch (ParsingTimeoutException e)
        {
            return null;
        }

    }

}
