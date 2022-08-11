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
package com.xceptance.xlt.engine.dns;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;

import org.junit.Ignore;

import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.RequestExecutionContext;
import com.xceptance.xlt.engine.socket.SocketMonitor;

@Ignore("Not a test case, but rather an analysis and debugging tool")
public class XltDnsResolverTest
{
    public static void main(final String[] args)
    {
        // final String name = "host1.htmlunit.org";
        final String name = "www.tapestry.com";
        // final String name = "www.carters.com";
        // final String name = "www.soliver.com";
        // final String name = "www.microsoft.com";
        // String name = "www.youtube.com";

        new Thread(() -> doIt(name)).start();
        // new Thread(() -> doIt(name)).start();
        // new Thread(() -> doIt(name)).start();

        XltProperties props = XltProperties.getInstance();

        props.setProperty("xlt.dns.provider", "dnsjava");
        //props.setProperty("xlt.dns.providers.dnsjava.resolver.servers", "8.8.8.8");
    }

    private static void doIt(final String name)
    {
        ThreadUtils.sleep(new Random().nextInt(1000));

        final SocketMonitor socketMonitor = RequestExecutionContext.getCurrent().getSocketMonitor();

        final XltDnsResolver dns = new XltDnsResolver();

        for (int i = 0; i < 500; i++)
        {
            InetAddress[] addresses = null;

            try
            {
                socketMonitor.reset();

                addresses = dns.resolve(name);
            }
            catch (final Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                System.out.printf("%3d: %s - %d ms\n", i, Arrays.toString(addresses),
                                  socketMonitor.getSocketStatistics().getDnsLookupTime());
            }

            ThreadUtils.sleep(1000);
        }
    }
}
