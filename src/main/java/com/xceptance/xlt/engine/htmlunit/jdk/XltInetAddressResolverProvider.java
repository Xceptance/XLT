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
import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;
import java.util.Arrays;
import java.util.stream.Stream;

import com.xceptance.xlt.engine.dns.XltDnsResolver;

/**
 * XLT's JVM-wide DNS resolver provider. This hooks directly into the 
 * java.net.spi.InetAddressResolverProvider (JEP 418) to ensure all name 
 * resolutions performed by modern JDK networking (e.g. java.net.http.HttpClient) 
 * are routed through XLT's overriding DNS facilities cleanly.
 */
public class XltInetAddressResolverProvider extends InetAddressResolverProvider
{
    @Override
    public InetAddressResolver get(Configuration configuration)
    {
        return new XltInetAddressResolver(configuration.builtinResolver());
    }

    @Override
    public String name()
    {
        return "XltInetAddressResolverProvider";
    }

    /**
     * The actual XLT-backed resolver.
     */
    private static class XltInetAddressResolver implements InetAddressResolver
    {
        private final InetAddressResolver builtin;
        private static final ThreadLocal<Boolean> IN_PROGRESS = ThreadLocal.withInitial(() -> false);

        public XltInetAddressResolver(InetAddressResolver builtin)
        {
            this.builtin = builtin;
        }

        @Override
        public Stream<InetAddress> lookupByName(String host, LookupPolicy lookupPolicy) throws UnknownHostException
        {
            if (IN_PROGRESS.get() || !com.xceptance.xlt.engine.htmlunit.jdk.JdkWebConnection.IS_JDK_THREAD.get())
            {
                return builtin.lookupByName(host, lookupPolicy);
            }
            
            IN_PROGRESS.set(true);
            try
            {
                // Instantiate the XLT Resolver. It relies on ThreadLocals internally 
                // via RequestExecutionContext.getCurrent(), so it cleanly isolates 
                // virtual user contexts automatically.
                XltDnsResolver xltDnsResolver = new XltDnsResolver();
                InetAddress[] addresses = xltDnsResolver.resolve(host);
                return Arrays.stream(addresses);
            }
            catch (Exception e)
            {
                // Fallback to builtin or rethrow UnknownHostException if it explicitly fails
                if (e instanceof UnknownHostException)
                {
                    throw (UnknownHostException) e;
                }
                return builtin.lookupByName(host, lookupPolicy);
            }
            finally
            {
                IN_PROGRESS.set(false);
            }
        }

        @Override
        public String lookupByAddress(byte[] addr) throws UnknownHostException
        {
            return builtin.lookupByAddress(addr);
        }
    }
}
