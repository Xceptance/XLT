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
