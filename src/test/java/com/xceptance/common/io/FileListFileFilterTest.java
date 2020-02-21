package com.xceptance.common.io;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the FileListFileFilter implementation.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class FileListFileFilterTest
{

    /** FileListFileFilter instance to be tested. */
    private FileListFileFilter filter;

    @Before
    public void init()
    {
        final File[] files = new File[]
            {
                new File("test1.txt"), new File("test2.txt")
            };
        filter = new FileListFileFilter(files);
    }

    /**
     * Tests if files named "test1.txt" and "test2.txt" will be accepted by filter, which should be the case.
     */
    @Test
    public void testAcceptAll()
    {
        Assert.assertTrue(filter.accept(new File("test1.txt")));
        Assert.assertTrue(filter.accept(new File("test2.txt")));
    }

    /**
     * Tests, if files named "test3.txt" and "anyFile" will be accepted by filter, which shouldn't be.
     */
    @Test
    public void testAcceptNone()
    {
        Assert.assertFalse(filter.accept(new File("test3.txt")));
        Assert.assertFalse(filter.accept(new File("anyFile")));
    }

}
