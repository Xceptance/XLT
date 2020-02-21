package com.xceptance.common.collection;

import java.util.LinkedList;

/**
 * <p>
 * Linked list of fixed size supporting LRU-behavior. This is not a full implementation that overwrites all list
 * behavior. Only the methods add and get should be used when LRU-behavior including the size limit is needed.
 * </p>
 * <p>
 * All other methods are still available but their use will disturb the lru order and potentially violate the set size
 * limit.
 * </p>
 * 
 * @param <E>
 *            type of list elements
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class LRUList<E> extends LinkedList<E>
{
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = -6722448569778711452L;

    /**
     * Size of list.
     */
    private final int size;

    /**
     * Creates a new LRU list.
     * 
     * @param maxSize
     *            the maximum size of the list
     */
    public LRUList(final int maxSize)
    {
        super();
        this.size = maxSize;
    }

    /**
     * Adds the given element to the list.
     * <p>
     * If the maximum size of the list has been reached, the head of the list will be removed before the given element
     * will be added to the list.
     * </p>
     * 
     * @param element
     *            the element to add
     * @return <tt>true</tt> if the element was successfully added to the list, <tt>false</tt> otherwise
     */
    @Override
    public boolean add(final E element)
    {
        while (super.size() >= this.size)
        {
            super.poll();
        }

        return super.add(element);
    }

    /**
     * Gets the element at the given index.
     * <p>
     * To guarantee LRU behavior this element will be removed and added to the end of the list.
     * </p>
     * 
     * @param idx
     *            the index of the element to get
     * @return element at given index
     */
    @Override
    public E get(final int idx)
    {
        final E element = super.get(idx);

        super.remove(idx);
        super.add(element);

        return element;
    }
}
