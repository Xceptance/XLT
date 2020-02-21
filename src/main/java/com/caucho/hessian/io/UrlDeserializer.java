package com.caucho.hessian.io;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A custom deserializer that deserializes {@link URL} objects from its string representation. URLs can be deserialized
 * only if they have been serialized with {@link UrlSerializer}.
 * <p>
 * To become effective, this class must be registered as custom deserializer in file
 * <code>META-INF/hessian/deserializers</code>.
 * <p>
 * This new pair of serializer/deserializer was needed to quickly work around a bug in Hessian. Due to a change in the
 * URL class in Java 1.8.0_72, the standard object deserializer in Hessian is not able any longer to deserialize URLs
 * without errors. See #2590 for more information.
 */
public class UrlDeserializer extends AbstractStringValueDeserializer
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getType()
    {
        return URL.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object create(final String value)
    {
        if (value == null)
        {
            return null;
        }
        else
        {
            try
            {
                return new URL(value);
            }
            catch (final MalformedURLException e)
            {
                throw new RuntimeException("Failed to deserialize URL from value: " + value, e);
            }
        }
    }
}
