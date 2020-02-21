package com.caucho.hessian.client;

/**
 * A special {@link HessianProxyFactory} which uses an {@link EasyHessianURLConnectionFactory} to make communication
 * with servers possible that use an invalid/self-signed certificate.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class EasyHessianProxyFactory extends HessianProxyFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected HessianConnectionFactory createHessianConnectionFactory()
    {
        return new EasyHessianURLConnectionFactory();
    }
}
