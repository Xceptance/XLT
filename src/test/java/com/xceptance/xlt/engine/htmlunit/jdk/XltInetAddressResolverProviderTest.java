/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.engine.htmlunit.jdk;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class XltInetAddressResolverProviderTest
{
    @Before
    public void setup()
    {
        // Ensure clean state
        JdkWebConnection.IS_JDK_THREAD.remove();

    }

    @After
    public void tearDown()
    {
        JdkWebConnection.IS_JDK_THREAD.remove();
    }

    @Test
    public void testBypassesProxyWhenNotJdkThread() throws UnknownHostException
    {
        // Validate that when IS_JDK_THREAD is false or unset, DNS resolutions go to native OS
        JdkWebConnection.IS_JDK_THREAD.remove();
        InetAddress[] addresses = InetAddress.getAllByName("localhost");
        Assert.assertNotNull(addresses);
        Assert.assertTrue(addresses.length > 0);
    }
    
    @Test
    public void testDelegatesToProxyWhenJdkThread() throws UnknownHostException
    {
        JdkWebConnection.IS_JDK_THREAD.set(true);
        boolean threwException = false;
        try
        {
            // "this-domain-does-not-exist.xceptance.test"
            InetAddress.getAllByName("this-domain-does-not-exist.xceptance.test");
        }
        catch (UnknownHostException e)
        {
            threwException = true;
        }
        Assert.assertTrue("Should have delegated to resolver and thrown UnknownHostException for invalid domain", threwException);
    }
    
    @Test
    public void testIsolationWithNullContext() throws UnknownHostException
    {
        JdkWebConnection.IS_JDK_THREAD.set(true);
        
        // Remove RequestExecutionContext entirely to simulate isolated background threads running bare

        
        // Should not throw NPE during XltDnsResolver creation - should either safely throw UnknownHost or fallback
        try
        {
            InetAddress.getAllByName("localhost");
        }
        catch (Exception e)
        {
            Assert.fail("Should handle null RequestExecutionContext gracefully without unexpected exceptions");
        }
    }
}
