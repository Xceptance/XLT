package org.pegdown;

import java.util.List;
import java.util.Map;

import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.VerbatimSerializer;
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
