package posters.loadtest.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

public class AjaxUtils
{
    /**
     * Performs an XHR call for the given URL and parameters with method GET.
     * 
     * @param page
     *            the current page
     * @param urlString
     *            the URL to call
     * @param method
     *            HTTP method
     * @param params
     *            the URL parameters
     * @return the XHR response
     * @throws Exception
     *             if something bad happens
     */
    public static WebResponse callGet(final HtmlPage page, final String urlString, final List<NameValuePair> params) throws Exception
    {
        // Build a fully qualified URL
        final URL pageURL = page.getFullyQualifiedUrl(urlString);
        // Decide if the given parameters are additional parameters and append
        // them properly encoded to the given
        // URL and build the request.
        String requestUrlString = pageURL.toExternalForm();
        if (params != null)
        {
            requestUrlString += (pageURL.getQuery() == null ? "?" : "&") + paramsToQueryString(params);
        }
        final WebRequest request = new WebRequest(new URL(requestUrlString), HttpMethod.GET);

        // XHR requests must not get cached internally. This is achieved by
        // setting the XHR flag.
        request.setXHR();
        // XHR request have an additional header.
        request.getAdditionalHeaders().put("X-Requested-With", "XMLHttpRequest");
        request.getAdditionalHeaders().put("Referer", page.getUrl().toExternalForm());

        // Perform the updatePrice call and return the result.
        return page.getWebClient().loadWebResponse(request);
    }

    /**
     * Performs an XHR call for the given URL and parameters with method POST.
     * 
     * @param page
     *            the current page
     * @param urlString
     *            the URL to call
     * @param params
     *            the URL parameters
     * @return the XHR response
     * @throws Exception
     *             if something bad happens
     */
    public static WebResponse callPost(final HtmlPage page, final String urlString, final List<NameValuePair> params) throws Exception
    {
        // Often an URL string is (relative or absolute) not full qualified
        // (e.g. '/foo/bar.html'). So this example shows how to build a full
        // qualified URL first from a url string.
        final URL pageURL = page.getFullyQualifiedUrl(urlString);

        // We create a web request and set the parameters
        final WebRequest request = new WebRequest(pageURL, HttpMethod.POST);
        request.setRequestParameters(params);

        // XHR requests must not get cached internally. This is achieved by
        // setting the XHR flag.
        request.setXHR();
        // XHR request have an additional header.
        request.getAdditionalHeaders().put("X-Requested-With", "XMLHttpRequest");
        request.getAdditionalHeaders().put("Referer", page.getUrl().toExternalForm());

        // Perform the updatePrice call and return the result.
        return page.getWebClient().loadWebResponse(request);
    }

    /**
     * Transform the given parameter list to an URL conform parameter string.
     * 
     * @param parameters
     *            parameters to transform
     * @return an URL parameter string
     */
    public static String paramsToQueryString(final List<NameValuePair> parameters) throws Exception
    {
        final ArrayList<org.apache.http.NameValuePair> arr = new ArrayList<org.apache.http.NameValuePair>();
        for (final org.apache.http.NameValuePair nvp : NameValuePair.toHttpClient(parameters))
        {
            arr.add(nvp);
        }

        return URLEncodedUtils.format(arr, "UTF-8");
    }
}
