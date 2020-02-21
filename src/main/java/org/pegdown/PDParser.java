package org.pegdown;

import org.parboiled.Rule;
import org.parboiled.annotations.Cached;
import org.pegdown.ast.SimpleNode;
import org.pegdown.ast.SimpleNode.Type;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.ValidEmphOrStrongCloseNode;
import org.pegdown.plugins.PegDownPlugins;

/**
 * Specialization of PegDown's Markdown parser that fixes some of its parsing issues.
 */
class PDParser extends Parser
{
    /**
     * Constructor.
     * 
     * @param options
     *            the parsing options
     * @param maxParsingTimeInMillis
     *            the parsing timeout in msec
     * @param parseRunnerProvider
     *            the parse runner provider
     * @param plugins
     *            additional plugins
     */
    public PDParser(Integer options, Long maxParsingTimeInMillis, ParseRunnerProvider parseRunnerProvider, PegDownPlugins plugins)
    {
        super(options, maxParsingTimeInMillis, parseRunnerProvider, plugins);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isLegalEmphOrStrongStartPos()
    {
        if (currentIndex() == 0)
            return true;

        Object lastItem = peek(1);
        Class<?> lastClass = lastItem.getClass();

        SuperNode supernode;
        while (SuperNode.class.isAssignableFrom(lastClass))
        {
            supernode = (SuperNode) lastItem;

            if (supernode.getChildren().size() < 1)
                return true;

            lastItem = supernode.getChildren().get(supernode.getChildren().size() - 1);
            lastClass = lastItem.getClass();
        }

        return (TextNode.class.equals(lastClass)) || (SpecialTextNode.class.equals(lastClass)) || (SimpleNode.class.equals(lastClass)) ||
               (java.lang.Integer.class.equals(lastClass));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cached
    public Rule EmphOrStrongClose(java.lang.String chars)
    {
        return Sequence(Test(isLegalEmphOrStrongClosePos()),
                        FirstOf(Sequence(Test(ValidEmphOrStrongCloseNode.class.equals(peek(0).getClass())), drop()),
                                Sequence(TestNot(Spacechar()), NotNewline(), chars)));

    }

    @Override
    public Rule NormalEndline()
    {
        return Sequence(Sp(), Newline(),
                        TestNot(FirstOf(BlankLine(), '>', '+', '-', '*', AtxStart(),
                                        Sequence(ZeroOrMore(NotNewline(), ANY), Newline(), FirstOf(NOrMore('=', 3), NOrMore('-', 3)),
                                                 Newline()),

                                        FencedCodeBlock())),
                        ext(HARDWRAPS) ? toRule(push(new SimpleNode(Type.Linebreak))) : toRule(push(new TextNode(" "))));
    }

}
