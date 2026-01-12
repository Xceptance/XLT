package posters.functional.scenarios;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.OpenHomepage;
import posters.functional.modules.Search;

/**
 * <p>Simulates storefront search.</p>
 */
public class TSearch extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TSearch()
    {
        super("https://localhost:8443");
    }


    /**
     * Executes the test.
     *
     * @throws Throwable if anything went wrong
     */
    @Test
    public void test() throws Throwable
    {
        OpenHomepage.execute();

        //
        // ~~~ Search ~~~
        //
        startAction("Search");
        // Store a search phrase that gives results
        store("${searchTerm_hits}", "searchTerm");
        // Execute the search (module call)
        Search.execute("${searchTerm_hits}");

        // Validate the entered search phrase is still visible in the input
        assertText("id=searchTextValue", "${searchTerm_hits}");
        // Validate presence of the search results page headline
        assertElementPresent("id=titleSearchText");
        // Validate the headline contains the search phrase
        assertText("id=titleSearchText", "glob:*Your results for your search: '${searchTerm_hits}'*");
        // validate result counter
        assertText("id=totalProductCount", "${resultProductCount}");
        //
        // ~~~ ViewProduct ~~~
        //
        startAction("ViewProduct");
        // Assert presence of one of the product thumbnails
        assertElementPresent("id=product0");
        // Store the name of the first product
        storeText("css=#product0 .pInfo .pName", "productName");
        // Click the product ilnk to open the product detail page
        clickAndWait("css=#product0 img");
        // Validate it's the correct product detail page
        assertText("css=#titleProductName", "${productName}");

    }

}