/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.common.lang;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the ThreadUtils constructor only.
 * 
 * @author Rene Schwietzke
 * @see ThreadUtils
 */
public class ThreadUtilsTest
{
    /**
     * Check private constructor
     */
    @Test
    public final void testConstructor()
    {
        Assert.assertTrue(ReflectionUtils.classHasOnlyPrivateConstructors(ThreadUtils.class));
    }
}
