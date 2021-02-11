/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package test.com.xceptance.xlt.engine;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.Assert;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.xceptance.xlt.engine.XltWebClient;

import util.xlt.properties.ReversibleChangePipeline;

/**
 * Tests authentication. Thus requires a web application which has access controls been set. The class has to be weird
 * to work around a class loading problem in Hudson/Jenkins.
 * 
 * @author Sebastian Oerding
 */
public class XltWebClientWithAnotherClassNameTest
{
    /**
     * Tests for issue #1405
     * 
     * @throws IOException
     * @throws MalformedURLException
     * @throws FailingHttpStatusCodeException
     */
    @Test
    public void testBasicAuthWithXltWebClient() throws FailingHttpStatusCodeException, MalformedURLException, IOException
    {
        /* 1. Ensure that the request fails with authentication */
        final XltWebClient client = new XltWebClient();
        client.setTimerName("TEST");
        Page p = client.getPage("http://localhost:8080/testpages/auth/basic/");
        Assert.assertEquals("Unauthorized, request should fail!, request should work!", 401, p.getWebResponse().getStatusCode());

        /*
         * 2. Ensure that the request is successful with authentication. It has to be basic authentication due to Jetty
         * configuration for the accessed resource.
         */
        ReversibleChangePipeline rcp = new ReversibleChangePipeline();
        rcp.addAndApply("com.xceptance.xlt.auth.userName", "JohnXLTCustomer");
        rcp.addAndApply("com.xceptance.xlt.auth.password", "testUser");
        try (final XltWebClient client2 = new XltWebClient())
        {
            client2.setTimerName("TEST");
            p = client2.getPage("http://localhost:8080/testpages/auth/basic/");
            Assert.assertEquals("Authorized, request should work!", 200, p.getWebResponse().getStatusCode());
        }
        finally
        {
            rcp.reverseAll();

            // clean up
            client.close();
        }
    }
}
