package com.xceptance.xlt.engine.dns;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;

import org.junit.Ignore;

import com.xceptance.common.lang.ThreadUtils;
import com.xceptance.xlt.engine.RequestExecutionContext;
import com.xceptance.xlt.engine.socket.SocketMonitor;

@Ignore("Not a test case, but rather an analysis and debugging tool")
public class XltDnsResolverTest
{
    public static void main(final String[] args)
    {
        final String name = "dwlp2.coachoutlet.com";
        // final String name = "production-functional22-qa.demandware.net";
        // String name = "www.youtube.com";

        new Thread(() -> doIt(name)).start();
        // new Thread(() -> doIt(name)).start();
        // new Thread(() -> doIt(name)).start();
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
