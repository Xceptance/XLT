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
package com.caucho.hessian.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;

import com.caucho.hessian.io.HessianRemoteObject;

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

//    public Object create(Class<?> api, URL url, ClassLoader loader)
//    {
//        if (api == null)
//            throw new NullPointerException("api must not be null for HessianProxyFactory.create()");
//        InvocationHandler handler = null;
//
//        handler = new XltProxy(url, this, api);
//
//        return Proxy.newProxyInstance(loader, new Class[]
//            {
//                api, HessianRemoteObject.class
//            }, handler);
//    }
//
//    private static class XltProxy extends HessianProxy
//    {
//        public XltProxy(URL url, HessianProxyFactory factory, Class<?> type)
//        {
//            super(url, factory, type);
//        }
//
//        @Override
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
//        {
//            System.out.printf("Calling method: %s\n", method.getName());
//
//            Object result = super.invoke(proxy, method, args);
//            if (result != null)
//            {
//                System.out.printf("Result type: %s\n", result.getClass().getSimpleName());
//            }
//            
//            return result;
//        }
//    }
}
