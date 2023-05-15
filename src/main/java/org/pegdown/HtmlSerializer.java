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
package org.pegdown;

import java.util.List;
import java.util.Map;

import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.QuotedNode;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

/**
 * Specialization of PegDown's HTML serializer used to convert double-quote characters to their HTML entity counterpart
 * and to encode inline HTML properly.
 */
class HtmlSerializer extends ToHtmlSerializer
{

    /**
     * Constructor.
     * 
     * @param linkRenderer
     * @param verbatimSerializers
     * @param plugins
     */
    public HtmlSerializer(LinkRenderer linkRenderer, Map<String, VerbatimSerializer> verbatimSerializers,
                          List<ToHtmlSerializerPlugin> plugins)
    {
        super(linkRenderer, verbatimSerializers, plugins);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(QuotedNode node)
    {
        printer.print("&quot;");
        visitChildren(node);
        printer.print("&quot;");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(InlineHtmlNode node)
    {
        printer.printEncoded(node.getText());
    }
}
