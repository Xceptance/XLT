package com.gargoylesoftware.htmlunit;

import org.junit.Assert;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.css.ComputedCSSStyleDeclaration;
import com.gargoylesoftware.htmlunit.javascript.host.css.StyleAttributes.Definition;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;

/**
 * See http://sourceforge.net/tracker/?func=detail&aid=3136642&group_id=47038&atid=448266 and #566.
 */
public class ParsingInvalidCssTest
{
    @Test
    public void test() throws Throwable
    {
        try (final WebClient wc = new WebClient(BrowserVersion.CHROME))
        {
            final HtmlPage page = wc.getPage(getClass().getResource(getClass().getSimpleName() + ".html"));

            final HtmlElement e = (HtmlElement) page.getByXPath("//h1").get(0);
            final ComputedCSSStyleDeclaration styleDec = ((HTMLElement) e.getScriptableObject()).getCurrentStyle();
            Assert.assertEquals("url(someFile.jpg) top right", styleDec.getStyleAttribute(Definition.BACKGROUND));
        }
    }
}
