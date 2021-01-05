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
package util.lang;

import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import com.xceptance.xlt.api.util.XltLogger;

/**
 * This class extends {@link SimpleJavaFileObject} to have a convenience class for working with strings that represent
 * Java source files. This is due to the Java 6 compiler API which accepts {@link JavaFileObject}s.
 * 
 * @author Sebastian Oerding
 */
final class JavaObjectFromString extends SimpleJavaFileObject
{
    private final String content;

    /**
     * Returns a new instance with the argument name and content.
     * 
     * @param name
     *            the name of the (pseudo) file
     * @param content
     *            the content of the (pseudo) file
     * @throws IllegalArgumentException
     *             if content is <code>null</code> or empty
     */
    JavaObjectFromString(final String name, final String content) throws URISyntaxException
    {
        super(new URI(name), Kind.SOURCE);
        if (content == null || content.isEmpty())
        {
            throw new IllegalArgumentException("Content may neither be \"null\" nor empty!");
        }
        this.content = content;
    }

    /**
     * Returns the contents of this instance, ignores the argument flag.
     * 
     * @param ignoreEncodingErrors
     *            is ignored
     * @throws UnsupportedOperationException
     *             if calling this method with <code>true</code> as argument value
     */
    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors)
    {
        if (!ignoreEncodingErrors)
        {
            XltLogger.runTimeLogger.warn("\"ignoreEncodingErrors\" is ignored! Call this method with \"true\"!");
            // throw new UnsupportedOperationException("This implementation can only be called with \"true\"!");
        }
        return content;
    }
}
