package posters.loadtest.flows;

import posters.loadtest.actions.catalog.Paging;
import posters.loadtest.actions.catalog.ProductDetailView;
import posters.loadtest.actions.catalog.SelectCategory;
import posters.loadtest.actions.catalog.SelectTopCategory;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;
import com.xceptance.xlt.api.util.XltRandom;

/**
 * Browse the catalog by selecting a random top category followed by a sub category. If paging is configured in the
 * project properties we perform one or more paging actions and finally loading a random product detail page. The
 * browsing is encapsulated in a flow that combines a sequence of several XLT actions. Different test cases can call
 * this method now to reuse the flow. This is a concept for code structuring you can implement if needed, yet explicit
 * support is neither available in the XLT framework nor necessary when you manually create a flow.
 */
public class BrowsingFlow
{
    /**
     * The previous action
     */
    private AbstractHtmlPageAction previousAction;

    /**
     * The probability to perform a paging
     */
    private final int pagingProbability;

    /**
     * The minimum number of paging rounds
     */
    private final int pagingMin;

    /**
     * The maximum number of paging rounds
     */
    private final int pagingMax;

    /**
     * The probability to select a Top Category
     */
    private final int topCategoryProbability;

    /**
     * Constructor
     *
     * @param previousAction
     *            The previously performed action
     * @param topCategoryProbability
     *            Probability to browse top categories instead of sub categories
     * @param pagingProbability
     *            The paging probability
     * @param pagingMin
     *            The minimum number of paging rounds
     * @param pagingMax
     *            The maximum number of paging rounds
     */
    public BrowsingFlow(final AbstractHtmlPageAction previousAction, final int topCategoryProbability, final int pagingProbability,
                        final int pagingMin, final int pagingMax)
    {
        this.previousAction = previousAction;
        this.topCategoryProbability = topCategoryProbability;
        this.pagingProbability = pagingProbability;
        this.pagingMin = pagingMin;
        this.pagingMax = pagingMax;
    }

    /**
     * {@inheritDoc}
     */
    public AbstractHtmlPageAction run() throws Throwable
    {
        if (XltRandom.nextBoolean(topCategoryProbability))
        {
            // Select a random top category from side navigation.
            final SelectTopCategory selectTopCategory = new SelectTopCategory(previousAction);
            selectTopCategory.run();
            previousAction = selectTopCategory;
        }
        else
        {
            // Select a random level-1 category from side navigation.
            final SelectCategory selectCategory = new SelectCategory(previousAction);
            selectCategory.run();
            previousAction = selectCategory;
        }

        // According to the configured probability perform the paging or not.
        if (XltRandom.nextBoolean(pagingProbability))
        {
            // Get current number of paging rounds determined from the configured
            // min and max value for paging.
            final int pagingRounds = XltRandom.nextInt(pagingMin, pagingMax);
            for (int round = 0; round < pagingRounds; round++)
            {
                // Perform a paging if possible.
                final Paging paging = new Paging(previousAction);

                if (paging.preValidateSafe())
                {
                    paging.run();
                }
                else
                {
                    break;
                }

                previousAction = paging;
            }
        }

        // Select a random poster from product overview and show product detail page.
        final ProductDetailView productDetailView = new ProductDetailView(previousAction);
        productDetailView.run();
        previousAction = productDetailView;

        // Return the result of the last action in this flow.
        return previousAction;
    }
}
