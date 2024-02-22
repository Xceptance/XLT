/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.javascript.host;

import java.lang.reflect.Method;

import org.htmlunit.corejs.javascript.Context;
import org.htmlunit.corejs.javascript.Function;
import org.htmlunit.corejs.javascript.Scriptable;
import org.htmlunit.javascript.HtmlUnitScriptable;
import org.htmlunit.javascript.JavaScriptEngine;

/**
 * An implementation of native ActiveX components using <a href="http://jacob-project.wiki.sourceforge.net/">Jacob</a>.
 *
 * @author Ahmed Ashour
 */
public class ActiveXObjectImpl extends HtmlUnitScriptable {

    private static final Class<?> activeXComponentClass_;

    /** ActiveXComponent.getProperty(String) */
    private static final Method METHOD_getProperty_;
    private final Object object_;

    /** Dispatch.callN(Dispatch, String, Object[]) */
    private static final Method METHOD_callN_;

    /** Variant.getvt() */
    private static final Method METHOD_getvt_;

    /** Variant.getDispatch() */
    private static final Method METHOD_getDispatch_;

    static {
        try {
            activeXComponentClass_ = Class.forName("com.jacob.activeX.ActiveXComponent");
            METHOD_getProperty_ = activeXComponentClass_.getMethod("getProperty", String.class);
            final Class<?> dispatchClass = Class.forName("com.jacob.com.Dispatch");
            METHOD_callN_ = dispatchClass.getMethod("callN", dispatchClass, String.class, Object[].class);
            final Class<?> variantClass = Class.forName("com.jacob.com.Variant");
            METHOD_getvt_ = variantClass.getMethod("getvt");
            METHOD_getDispatch_ = variantClass.getMethod("getDispatch");
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs a new instance.
     *
     * @param activeXName ActiveX object name
     * @throws Exception if failed to initiate Jacob
     */
    public ActiveXObjectImpl(final String activeXName) throws Exception {
        this(activeXComponentClass_.getConstructor(String.class).newInstance(activeXName));
    }

    private ActiveXObjectImpl(final Object object) {
        object_ = object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(final String name, final Scriptable start) {
        try {
            final Object variant = METHOD_getProperty_.invoke(object_, name);
            return wrapIfNecessary(variant);
        }
        catch (final Exception e) {
            return new Function() {
                @Override
                public Object call(final Context arg0, final Scriptable arg1, final Scriptable arg2,
                    final Object[] arg3) {
                    try {
                        final Object rv = METHOD_callN_.invoke(null, object_, name, arg3);
                        return wrapIfNecessary(rv);
                    }
                    catch (final Exception ex) {
                        throw JavaScriptEngine.throwAsScriptRuntimeEx(ex);
                    }
                }

                @Override
                public Scriptable construct(final Context arg0, final Scriptable arg1, final Object[] arg2) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void delete(final String arg0) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void delete(final int arg0) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Object get(final String arg0, final Scriptable arg1) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Object get(final int arg0, final Scriptable arg1) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getClassName() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Object getDefaultValue(final Class<?> arg0) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Object[] getIds() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Scriptable getParentScope() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Scriptable getPrototype() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean has(final String arg0, final Scriptable arg1) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean has(final int arg0, final Scriptable arg1) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean hasInstance(final Scriptable arg0) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void put(final String arg0, final Scriptable arg1, final Object arg2) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void put(final int arg0, final Scriptable arg1, final Object arg2) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setParentScope(final Scriptable arg0) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setPrototype(final Scriptable arg0) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /**
     * Wrap the specified variable into {@link ActiveXObjectImpl} if its type is Variant.VariantDispatch.
     * @param variant the variant to potentially wrap
     * @return either the variant if it is basic type or wrapped {@link ActiveXObjectImpl}
     */
    static Object wrapIfNecessary(final Object variant) throws Exception {
        if (((Short) METHOD_getvt_.invoke(variant)) == 9) { //Variant.VariantDispatch
            return new ActiveXObjectImpl(METHOD_getDispatch_.invoke(variant));
        }
        return variant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void put(final String name, final Scriptable start, final Object value) {
        try {
            final Method setMethod = activeXComponentClass_.getMethod("setProperty", String.class, value.getClass());
            setMethod.invoke(object_, name, JavaScriptEngine.toString(value));
        }
        catch (final Exception e) {
            throw JavaScriptEngine.throwAsScriptRuntimeEx(e);
        }
    }
}
