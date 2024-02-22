/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.html;

import org.htmlunit.SgmlPage;
import org.w3c.dom.DocumentFragment;

/**
 * A DOM object for DocumentFragment.
 *
 * @author Ahmed Ashour
 */
public class DomDocumentFragment extends DomNode implements DocumentFragment {

    /** The symbolic node name. */
    public static final String NODE_NAME = "#document-fragment";

    /**
     * Creates a new instance.
     * @param page the page which contains this node
     */
    public DomDocumentFragment(final SgmlPage page) {
        super(page);
    }

    /**
     * {@inheritDoc}
     * @return the node name, in this case {@link #NODE_NAME}
     */
    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    /**
     * {@inheritDoc}
     * @return the node type constant, in this case {@link org.w3c.dom.Node#DOCUMENT_FRAGMENT_NODE}
     */
    @Override
    public short getNodeType() {
        return org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String asXml() {
        final StringBuilder sb = new StringBuilder();
        for (final DomNode node : getChildren()) {
            sb.append(node.asXml());
        }
        return sb.toString();
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br>
     *
     * @return {@code false}
     */
    @Override
    public boolean isAttachedToPage() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNodeValue(final String value) {
        // Default behavior is to do nothing, overridden in some subclasses
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPrefix(final String prefix) {
        // Empty.
    }
}
