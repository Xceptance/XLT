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
package com.xceptance.xlt.engine.junit;

import java.lang.reflect.Method;

/**
 * A simple class for tests. It contains also some convenience methods to keep the class with the tests smaller.
 * 
 * @author Sebastian Oerding
 */
public class DummyTestClass
{
    public DummyTestClass()
    {

    }

    /**
     * Just dummy method for test invocation. Used as argument via reflection.
     */
    public void dummyMethod()
    {
    }

    /**
     * @return the {@link #dummyMethod()} from this class, this should never throw an exception as long as the method
     *         remains unchanged
     */
    Method getDummyMethod()
    {
        try
        {
            return DummyTestClass.class.getMethod("dummyMethod");
        }
        catch (final SecurityException e)
        {
            throw new IllegalStateException("Method has been changed / removed. Revert the change!");
        }
        catch (final NoSuchMethodException e)
        {
            throw new IllegalStateException("Method has been changed / removed. Revert the change!");
        }
    }
}
