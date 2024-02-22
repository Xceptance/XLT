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
package org.htmlunit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.htmlunit.html.HtmlPage;
import org.junit.Assert;
import org.junit.Test;

public class NPEDomNodeTest
{
    @Test
    public void npe() throws FailingHttpStatusCodeException, MalformedURLException, IOException
    {
        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection conn = new MockWebConnection();
            conn.setDefaultResponse("<p>");
            webClient.setWebConnection(conn);

            final HtmlPage page = (HtmlPage) webClient.getPage("http://mootools.net/");

            final List<?> elements = page.getByXPath("//p");

            Assert.assertFalse("There should be elements", elements.isEmpty());

            // clone the page
            final HtmlPage clone = page.cloneNode(true);

            // this will cause the NPE
            // Caused by: java.lang.NullPointerException
            // at com.gargoylesoftware.htmlunit.html.DomNode.getScriptObject(DomNode.java:854)
            // at com.gargoylesoftware.htmlunit.html.DomNode.getScriptObject(DomNode.java:854)
            // at com.gargoylesoftware.htmlunit.javascript.NamedNodeMap.<init>(NamedNodeMap.java:87)
            // at com.gargoylesoftware.htmlunit.html.HtmlElement.getAttributes(HtmlElement.java:551)
            // at org.apache.xml.dtm.ref.dom2dtm.DOM2DTM.nextNode(DOM2DTM.java:533)
            // at org.apache.xml.dtm.ref.DTMDefaultBase._firstch(DTMDefaultBase.java:533)
            // at
            // org.apache.xml.dtm.ref.DTMDefaultBaseTraversers$DescendantFromRootTraverser.getFirstPotential(DTMDefaultBaseTraversers.java:1690)
            // at
            // org.apache.xml.dtm.ref.DTMDefaultBaseTraversers$DescendantFromRootTraverser.first(DTMDefaultBaseTraversers.java:1734)
            // at org.apache.xpath.axes.DescendantIterator.nextNode(DescendantIterator.java:214)
            // at org.apache.xpath.axes.NodeSequence.nextNode(NodeSequence.java:335)
            // at org.apache.xpath.axes.NodeSequence.runTo(NodeSequence.java:494)
            // at org.apache.xml.dtm.ref.DTMNodeList.<init>(DTMNodeList.java:81)
            // at org.apache.xpath.objects.XNodeSet.nodelist(XNodeSet.java:346)
            // at com.gargoylesoftware.htmlunit.html.xpath.XPathUtils.getByXPath(XPathUtils.java:89)
            // ... 24 more
            clone.getByXPath("//img");
        }
    }
}
