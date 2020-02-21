package com.xceptance.xlt.engine.httprequest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HttpRequestTest
{
    private static final String URL = "http://www.example.org/index.html";

    @Test
    public void customParamOverwritesUrlParamWithSameName_GET() throws MalformedURLException, URISyntaxException
    {
        final WebRequest webRequest = new HttpRequest().baseUrl(URL + "?foo=url&bar=url&bum=url").method(HttpMethod.GET)
                                                       .param("bar", "code").buildWebRequest();

        // duplicate parameter removed from URL?
        // custom parameters appended to the URL?
        Assert.assertEquals(URL + "?foo=url&bum=url&bar=code", webRequest.getUrl().toString());

        // no request parameters at the web request?
        Assert.assertEquals(0, webRequest.getRequestParameters().size());
    }

    @Test
    public void customParamOverwritesUrlParamWithSameName_POST() throws MalformedURLException, URISyntaxException
    {
        customParamOverwritesUrlParamWithSameName(HttpMethod.POST);
    }

    @Test
    public void customParamOverwritesUrlParamWithSameName_PUT() throws MalformedURLException, URISyntaxException
    {
        customParamOverwritesUrlParamWithSameName(HttpMethod.PUT);
    }

    @Test
    public void customParamOverwritesUrlParamWithSameName_PATCH() throws MalformedURLException, URISyntaxException
    {
        customParamOverwritesUrlParamWithSameName(HttpMethod.PATCH);
    }

    private void customParamOverwritesUrlParamWithSameName(final HttpMethod method) throws MalformedURLException, URISyntaxException
    {
        final WebRequest webRequest = new HttpRequest().baseUrl(URL + "?foo=url&bar=url&bum=url").method(method).param("bar", "code")
                                                       .buildWebRequest();

        // duplicate parameters removed from URL?
        Assert.assertEquals(URL + "?foo=url&bum=url", webRequest.getUrl().toString());

        // custom parameters set at the web request?
        final List<NameValuePair> requestParameters = webRequest.getRequestParameters();
        Assert.assertEquals(1, requestParameters.size());
        validate(requestParameters.get(0), "bar", "code");
    }

    @Test
    public void customParamOverwritesAllUrlParamsWithSameName_GET() throws MalformedURLException, URISyntaxException
    {
        final WebRequest webRequest = new HttpRequest().baseUrl(URL + "?foo=url1&foo=url2&foo=url3").method(HttpMethod.GET)
                                                       .param("foo", "code").buildWebRequest();

        // all duplicate parameter removed from URL?
        // custom parameters appended to the URL?
        Assert.assertEquals(URL + "?foo=code", webRequest.getUrl().toString());

        // no request parameters at the web request?
        Assert.assertEquals(0, webRequest.getRequestParameters().size());
    }

    @Test
    public void customParamOverwritesAllUrlParamsWithSameName_POST() throws MalformedURLException, URISyntaxException
    {
        customParamOverwritesAllUrlParamsWithSameName(HttpMethod.POST);
    }

    @Test
    public void customParamOverwritesAllUrlParamsWithSameName_PUT() throws MalformedURLException, URISyntaxException
    {
        customParamOverwritesAllUrlParamsWithSameName(HttpMethod.PUT);
    }

    @Test
    public void customParamOverwritesAllUrlParamsWithSameName_PATCH() throws MalformedURLException, URISyntaxException
    {
        customParamOverwritesAllUrlParamsWithSameName(HttpMethod.PATCH);
    }

    private void customParamOverwritesAllUrlParamsWithSameName(final HttpMethod method) throws MalformedURLException, URISyntaxException
    {
        final WebRequest webRequest = new HttpRequest().baseUrl(URL + "?foo=url1&foo=url2&foo=url3").method(method).param("foo", "code")
                                                       .buildWebRequest();

        // all duplicate parameters removed from URL?
        Assert.assertEquals(URL, webRequest.getUrl().toString());

        // custom parameters set at the web request?
        final List<NameValuePair> requestParameters = webRequest.getRequestParameters();
        Assert.assertEquals(1, requestParameters.size());
        validate(requestParameters.get(0), "foo", "code");
    }

    private void validate(final NameValuePair requestParameter, final String name, final String value)
    {
        Assert.assertEquals(name, requestParameter.getName());
        Assert.assertEquals(value, requestParameter.getValue());
    }
}
