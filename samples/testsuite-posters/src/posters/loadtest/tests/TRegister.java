/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package posters.loadtest.tests;

import org.junit.Test;

import posters.loadtest.actions.Homepage;
import posters.loadtest.actions.account.GoToRegistrationForm;
import posters.loadtest.actions.account.GoToSignIn;
import posters.loadtest.actions.account.Login;
import posters.loadtest.actions.account.Logout;
import posters.loadtest.actions.account.Register;
import posters.loadtest.util.Account;

import com.xceptance.xlt.api.tests.AbstractTestCase;

/**
 * Open landing page and navigate to the registration form. Register a new customer, log in with new account and log out
 * afterwards.
 */
public class TRegister extends AbstractTestCase
{
    /**
     * Main test method.
     * 
     * @throws Throwable
     */
    @Test
    public void register() throws Throwable
    {
        // Create new account data. This account data will be used to create a new account.
        final Account account = new Account();

        // Read the store URL from properties.
        final String url = getProperty("store-url", "http://localhost:8080/posters/");

        // Go to poster store homepage
        final Homepage homepage = new Homepage(url);
        // Disable JavaScript for the complete test case to reduce client side resource consumption.
        // If JavaScript executed functionality is needed to proceed with the scenario (i.e. AJAX calls)
        // we will simulate this in the related actions.
        homepage.getWebClient().getOptions().setJavaScriptEnabled(false);
        homepage.run();

        // go to sign in
        final GoToSignIn goToSignIn = new GoToSignIn(homepage);
        goToSignIn.run();

        // go to registration form
        final GoToRegistrationForm goToRegistrationForm = new GoToRegistrationForm(goToSignIn);
        goToRegistrationForm.run();

        // register
        final Register register = new Register(goToRegistrationForm, account);
        register.run();

        // log in
        final Login login = new Login(register, account);
        login.run();

        // log out
        final Logout logout = new Logout(login);
        logout.run();
    }
}
