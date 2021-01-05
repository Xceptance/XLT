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
package com.gargoylesoftware.htmlunit;

import java.net.URI;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URIUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class _3661_UrlPathSegmentTest
{
    /*
     * "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "=" / "@" / ":" / "/" / "?" / "#" / "[" / "]"
     */
    private final Integer special;

    public _3661_UrlPathSegmentTest(final Integer aSpecial)
    {
        this.special = aSpecial;
    }

    @Parameters(name = "charCode={0} (dec)")
    public static Iterable<Object[]> data()
    {
        return "!$&'()*+,;=@:/?#[]".chars().boxed().map(c -> new Object[] { c }).collect(Collectors.toList());
    }

    @Test
    public void testEncodedCharacter() throws Throwable
    {
        final String path = String.format("/some/path%%%xwith/encoded/segments", special);
        final URI uri = new URI("http://example.org" + path);
        final URI uri2 = URIUtils.rewriteURI(uri, null);

        Assert.assertEquals(new URI(path), uri2);
    }
}
