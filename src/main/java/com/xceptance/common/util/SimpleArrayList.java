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
package com.xceptance.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Inexpensive (partial) list implementation. Not fully implemented, just what is needed. As soon as
 * iterators and other things are involved, the memory savings we wanted are gone.
 *
 * Minimal checks for data correctness!! This is tuned for speed not elegance or safety.
 *
 * @author Rene Schwietzke
 * @since 7.0.0
 */
public class SimpleArrayList<T> implements List<T>
{
    private T[] data;
    private int size;

    /**
     * Creates a new list wrapper from an existing one. This is not copying anything rather
     * referencing it. Make sure that you understand that!
     *
     * @param list
     */
    SimpleArrayList(final SimpleArrayList<T> list)
    {
        data = list.data;
        size = list.size;
    }

    /**
     * Create a new list with a default capacity.
     * @param capacity the capacity
     */
    public SimpleArrayList(final int capacity)
    {
        data = (T[]) new Object[capacity];
    }

    /**
     * Add an element to the end of the list
     *
     * @param element the element to add
     * @return true if added and for this impl it is always true
     */
    public boolean add(T element)
    {
        final int length = data.length;
        if (size == length)
        {
            final T[] newData = (T[]) new Object[data.length << 1];
            System.arraycopy(data, 0, newData, 0, length);
            data = newData;
        }

        data[size] = element;
        size++;

        return true;
    }

    /**
     * Return an element at index. No range checks at all.
     *
     * @param index the position
     * @return the element at this position
     */
    @SuppressWarnings("unchecked")
    public T get(int index)
    {
        return (T) data[index];
    }

    /**
     * Returns the size of this list
     */
    public int size()
    {
        return size;
    }

    /**
     * Creates an array of the elements. This is a copy operation!
     *
     * @return an array of the elements
     */
    @Override
    public Object[] toArray()
    {
        return Arrays.copyOf(data, size);
    }

    /**
     * Creates an array of the elements. This is a copy operation!
     *
     * @return an array of the elements
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] array)
    {
        return (T[]) Arrays.copyOf(data, size, array.getClass());
    }

    /**
     * Clears the list by setting the size to zero. It does not release any
     * elements for performance purposes.
     */
    @Override
    public void clear()
    {
        // are are not releasing any references, this is because of speed aka less memory
        // access needed
        size = 0;
    }

    /**
     * Returns view partitions on the underlying list. If the count is larger than size
     * you get back the maximum possible list number with one element each. If count
     * is 0 or smaller, we correct it to 1.
     *
     * @param count how many list do we want
     * @return a list of lists
     */
    public List<List<T>> partition(int count)
    {
        final int _count;
        if (count > size)
        {
            _count = size;
        }
        else
        {
            _count = count <= 0 ? 1 : count;
        }

        final SimpleArrayList<List<T>> result = new SimpleArrayList<>(count);

        final int newSize = (int) Math.ceil((double) size / (double) _count);
        for (int i = 0; i < _count; i++)
        {
            int from = i * newSize;
            int to = from + newSize - 1;
            if (to >= size)
            {
                to = size - 1;
            }
            result.add(new Partition<>(this, from, to));
        }

        return result;
    }

    class Partition<K> extends SimpleArrayList<K>
    {
        private final int from;
        private final int size;

        public Partition(final SimpleArrayList<K> list, final int from, final int to)
        {
            super(list);

            this.from = from;
            this.size = to - from + 1;
        }

        public boolean add(K o)
        {
            throw new RuntimeException("Cannot modify the partition");
        }

        public K get(int index)
        {
            return (K) super.get(index + from);
        }

        public int size()
        {
            return size;
        }

        public K[] toArray()
        {
            throw new RuntimeException("Cannot modify the partition");
        }

    }

    @Override
    public boolean isEmpty()
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public boolean contains(Object o)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public Iterator<T> iterator()
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public boolean remove(Object o)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public T set(int index, T element)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public void add(int index, T element)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public T remove(int index)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public int indexOf(Object o)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public int lastIndexOf(Object o)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public ListIterator<T> listIterator()
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public ListIterator<T> listIterator(int index)
    {
        throw new IllegalArgumentException("unimplemented");
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex)
    {
        throw new IllegalArgumentException("unimplemented");
    }
}
