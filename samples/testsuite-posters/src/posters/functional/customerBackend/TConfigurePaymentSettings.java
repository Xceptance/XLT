package posters.functional.customerBackend;
import org.junit.Test;
import com.xceptance.xlt.api.engine.scripting.AbstractWebDriverScriptTestCase;

import posters.functional.modules.AssertCreditCard;
import posters.functional.modules.CreateRandomUser;
import posters.functional.modules.FillInPaymentForm;
import posters.functional.modules.Login;
import posters.functional.modules.OpenAccountOverview;
import posters.functional.modules.OpenHomepage;

/**
 * <p>Creates and deletes a credit card.</p>
 */
public class TConfigurePaymentSettings extends AbstractWebDriverScriptTestCase
{

    /**
     * Constructor.
     */
    public TConfigurePaymentSettings()
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

        store("xxxx xxxx xxxx 1111", "creditCardNumberClean");
        storeEval("new Date().getUTCFullYear()", "creditCardExpMonth");
        CreateRandomUser.execute();

        Login.execute("${generatedEmail}", "${password}", "${firstName}");

        OpenAccountOverview.execute();

        //
        // ~~~ OpenPaymentSettings ~~~
        //
        startAction("OpenPaymentSettings");
        clickAndWait("id=linkPaymentOverview");
        assertElementPresent("id=titlePaymentOverview");
        //
        // ~~~ OpenFormToAddNewCreditCard ~~~
        //
        startAction("OpenFormToAddNewCreditCard");
        clickAndWait("id=linkAddNewPayment");
        FillInPaymentForm.execute("${creditCard}", "${fullName}", "${expDateMonth}", "${expDateYear}");

        //
        // ~~~ AddNewCreditCard ~~~
        //
        startAction("AddNewCreditCard");
        clickAndWait("id=btnAddPayment");
        AssertCreditCard.execute("${fullName}", "${creditCardNumberClean}", "${expDateMonth}", "${expDateYear}");

        //
        // ~~~ EditCreditCard ~~~
        //
        startAction("EditCreditCard");
        clickAndWait("css=#btnChangePayment0");
        FillInPaymentForm.execute("4111111111121234", "David Doe", "01", "${creditCardExpMonth}");

        clickAndWait("css=#btnUpdateDelAddr");
        AssertCreditCard.execute("David Doe", "xxxx xxxx xxxx 1234", "01", "${creditCardExpMonth}");

        //
        // ~~~ DeleteCreditCard ~~~
        //
        startAction("DeleteCreditCard");
        clickAndWait("css=#btnDeletePayment0");
        type("id=password", "${password}");
        //
        // ~~~ ConfirmDeletion ~~~
        //
        startAction("ConfirmDeletion");
        clickAndWait("id=btnDeletePayment");
        assertElementPresent("id=successMessage");
        assertNotElementPresent("id=payment1");

    }

}