package com.xceptance.common.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SimpleCacheAwareArrayList<T> implements List<T>
{
    private static final int SHIFT = 4;
    public static final int SUBARRAYSIZE = 2 << SHIFT;
    
    Object[] base;
    Object[] currentSlot;
    
    int size;

    int slot;
    int slotPos;
    
    SimpleCacheAwareArrayList(final SimpleCacheAwareArrayList<T> list)
    {
        base = list.base;
        size = list.size;
        slot = list.slot;
        slotPos = list.slotPos;
    }
    
    public SimpleCacheAwareArrayList(int capacity)
    {
        base = new Object[Math.max(1,  capacity >> SHIFT)];
        base[0] = new Object[SUBARRAYSIZE];
        currentSlot = (Object[]) base[0];
    }
    
    @Override
    public boolean add(T o)
    {
        if (slotPos == SUBARRAYSIZE)
        {
            slotPos = 0;
            slot++; 

            // worst case, we are totally full
            if (slot == base.length)
            {
                final int length = base.length;

                final Object[] newData = new Object[length << 1];
                System.arraycopy(base, 0, newData, 0, length);
                
                base = newData;
            }
            
            base[slot] = new Object[SUBARRAYSIZE];
            currentSlot = (Object[]) base[slot];
        }

        currentSlot[slotPos] = o;
        
        slotPos++;
        
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public T get(int index)
    {
        int slotNo = index >> (SHIFT + 1);
        int slotPos = index & (SUBARRAYSIZE - 1);
        
        final Object[] slot = (Object[]) base[slotNo];
        return (T) slot[slotPos];
    }

    @Override
    public int size()
    {
        return (this.slot << (this.SHIFT + 1)) + slotPos;
    }
    
    /**
     * Creates an array of the elements. This is a copy operation!
     * 
     * @return an array of the elements
     */
    @Override
    public Object[] toArray() 
    {
        throw new IllegalArgumentException("unimplemented");
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
        throw new IllegalArgumentException("unimplemented");
    }


    @Override
    public void clear()
    {
        throw new IllegalArgumentException("unimplemented"); 
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
