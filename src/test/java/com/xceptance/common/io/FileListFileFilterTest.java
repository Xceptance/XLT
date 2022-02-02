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
