package com.xceptance.xlt.engine.htmlunit;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Node;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;

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
