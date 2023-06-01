/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.webdriver;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.api.webdriver.XltDriver;
import com.xceptance.xlt.engine.XltWebClient;

/**
 * Tests the implementation of {@link XltDriver}.
 */
public class XltDriverImplTest
{
    /**
     * Tests the implementation of {@link XltDriver#getWebClient()}.
     */
    @Test
    public void testGetWebClient()
    {
        Assert.assertTrue("The return type of XltDriverImpl#getWebClient() is not XltWebClient",
                          new XltDriver().getWebClient() instanceof XltWebClient);
    }

    @Test
    public void ensureCurrentURLIsNullAfterInit()
    {
        Assert.assertNull("Current URL should be <null> right after construction of XltDriver", new XltDriver().getCurrentUrl());
    }
}
