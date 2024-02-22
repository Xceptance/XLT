/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package util.xlt.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Rule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.xceptance.xlt.api.util.XltProperties;

/**
 * An annotation that can be used on JUnit test methods to specify {@link XltProperties} values that need to be set for
 * the test method. Test classes that use this annotation need to specify a {@link Rule} of type
 * {@link AdjustXltProperties.MethodRule}, which will see to it that the property values are changed accordingly before
 * the test method is invoked
 * 
 * @author Deniz Altin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AdjustXltProperties
{
    /**
     * Annotation to be used to specify a single key/value pair in the value of an {@link AdjustXltProperties}
     * annotation.
     */
    public @interface SetProperty
    {
        String key();

        String value();
    }

    /**
     * the array of key/value pairs required by the annotated test method
     */
    SetProperty[] value();

    /**
     * A {@link org.junit.rules.MethodRule MethodRule} that will take care of setting the specified properties before
     * executing a test method annotated with {@link AdjustXltProperties}
     */
    public static class MethodRule implements org.junit.rules.MethodRule
    {
        @Override
        public Statement apply(final Statement base, final FrameworkMethod method, final Object target)
        {
            final AdjustXltProperties anno = method.getAnnotation(AdjustXltProperties.class);
            final SetProperty[] propOverrides = anno != null ? anno.value() : null;
            if (propOverrides == null || propOverrides.length == 0)
            {
                return base;
            }

            return new Statement()
            {
                @Override
                public void evaluate() throws Throwable
                {
                    final XltProperties xltProperties = XltProperties.getInstance();
                    final List<String> previousValues = retrieveValuesForKeysIn(propOverrides, xltProperties);

                    changeProperties(propOverrides, xltProperties);

                    try
                    {
                        base.evaluate();
                    }
                    finally
                    {
                        restoreProperties(propOverrides, previousValues, xltProperties);
                    }
                }
            };
        }

        private static List<String> retrieveValuesForKeysIn(final SetProperty[] specifiedProperties, final XltProperties xltProperties)
        {
            final List<String> result = new ArrayList<>(specifiedProperties.length);
            for (final SetProperty entry : specifiedProperties)
            {
                result.add(xltProperties.getProperty(entry.key()));
            }
            return result;
        }

        private static void changeProperty(final String key, final String value, final XltProperties xltProperties)
        {
            if (value == null)
            {
                xltProperties.removeProperty(key);
            }
            else
            {
                xltProperties.setProperty(key, value);
            }
        }

        private static void changeProperties(final SetProperty[] specifiedProperties, final XltProperties xltProperties)
        {
            for (final SetProperty property : specifiedProperties)
            {
                changeProperty(property.key(), property.value(), xltProperties);
            }
        }

        private static void restoreProperties(final SetProperty[] specifiedProperties, final List<String> previousValues,
                                              final XltProperties xltProperties)
        {
            final Iterator<String> previousValuesIterator = previousValues.iterator();
            for (final SetProperty property : specifiedProperties)
            {
                changeProperty(property.key(), previousValuesIterator.next(), xltProperties);
            }
        }
    }
}
