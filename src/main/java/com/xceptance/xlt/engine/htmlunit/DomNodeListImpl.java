/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.htmlunit;

import java.util.ArrayList;
import java.util.Collection;

import org.htmlunit.html.DomNode;
import org.htmlunit.html.DomNodeList;
import org.w3c.dom.Node;

/**
 * 
 *
 * @param <E>
 */
public class DomNodeListImpl<E extends DomNode> extends ArrayList<E> implements DomNodeList<E>
{
    /**
     * 
     */
    public DomNodeListImpl()
    {
        super();
    }

    /**
     * @param c
     */
    public DomNodeListImpl(Collection<E> c)
    {
        super(c);
    }

    /**
     * @param initialCapacity
     */
    public DomNodeListImpl(int initialCapacity)
    {
        super(initialCapacity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node item(int index)
    {
        return get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLength()
    {
        return size();
    }
}
