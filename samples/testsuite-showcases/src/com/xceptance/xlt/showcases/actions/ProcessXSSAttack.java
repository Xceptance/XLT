/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.showcases.actions;

import java.util.LinkedList;
import java.util.List;

import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.junit.Assert;

import com.xceptance.xlt.api.actions.AbstractHtmlPageAction;

/**
 * Here the perform the check of the inputs and the validation. First we check the form and backup the values of the
 * inputs. In the post validation we will use the backup to restore the input values to get the original form. Depending
 * on the call the check is done for one input or for all inputs of a form. After the page load, we check if we can find
 * the attack string on the page. If we found the string we have to check if a real xss vulnerability exists.
 */
public class ProcessXSSAttack extends AbstractHtmlPageAction
{
    /**
     * The timer name to use. The timer name is used to log measurements associated with this action. It can be passed
     * to the super class by the constructor.
     */
    private static final String TIMERNAME = "ProcessXSSAttack";

    /**
     * This will be the used attack string.
     */
    private final String attackString;

    /**
     * This will be the relevant input, if we want to check one input individual. Otherwise the input will be null.
     */
    private final HtmlInput input;

    /**
     * This will be the submit button.
     */
    private final HtmlElement clickable;

    /**
     * This will be our relevant form.
     */
    private final HtmlForm form;

    /**
     * This will be the inputs of the form.
     */
    private List<HtmlElement> inputs;

    /**
     * This fetch the values of the inputs.
     */
    private final List<String> inputValues = new LinkedList<String>();

    /**
     * XSSCheck for attacking all inputs of provided form.
     * 
     * @param previousAction
     *            the action we come from
     * @param formId
     *            the id of the relevant form
     * @param attackString
     *            the string which is used for the attack
     * @param clickableElement
     *            the clickable element for page load
     */
    public ProcessXSSAttack(final AbstractHtmlPageAction previousAction, final HtmlForm form, final String attackString,
                            final HtmlElement clickableElement)
    {
        super(previousAction, TIMERNAME);

        this.form = form;
        this.attackString = attackString;
        clickable = clickableElement;
        input = null;
    }

    /**
     * XSSCheck for attacking given inputs of provided form.
     * 
     * @param previousAction
     *            the action we come from
     * @param formId
     *            the id of the relevant form
     * @param attackString
     *            the string which is used for the attack
     * @param clickableElement
     *            the clickable element for page load
     * @param input
     *            the input which should be tested
     */
    public ProcessXSSAttack(final AbstractHtmlPageAction previousAction, final HtmlForm form, final String attackString,
                            final HtmlElement clickableElement, final HtmlInput input)
    {
        super(previousAction, TIMERNAME);

        this.form = form;
        this.attackString = attackString;
        clickable = clickableElement;
        this.input = input;
    }

    /**
     * Verify all preconditions. The prevalidate method is a used to ensure that everything that is needed to execute
     * this action is present on the page. So we check if the form, input and submit button are available. And enter the
     * attack string to the input(s)
     */
    @Override
    public void preValidate() throws Exception
    {
        // we should have it
        Assert.assertNotNull("Missing form for processing", form);

        // let's get the inputs
        inputs = form.getElementsByTagName("input");

        // we should have at least 1 input
        Assert.assertFalse("No inputs found to process", inputs.isEmpty());

        // pebbles hack
        // changes in entry & comment cause an internal server error
        // and title has a XSS vulnerability
        // comment out title to have a correct xss check
        final List<HtmlInput> removeInputs = new LinkedList<HtmlInput>();
        for (final HtmlElement element : inputs)
        {
            final HtmlInput input = (HtmlInput) element;

            if (input.getNameAttribute().equals("entry") || input.getNameAttribute().equals("comment") ||
                input.getNameAttribute().equals("title"))
            {
                removeInputs.add(input);
            }
        }
        if (!removeInputs.isEmpty())
        {
            inputs.removeAll(removeInputs);
        }
        // now check if we have to do a single check or if all input should be
        // tested
        if (input == null)
        {
            // enter the attack string into each input
            for (final HtmlElement element : inputs)
            {
                final HtmlInput input = (HtmlInput) element;

                // backup input values for cleanup after test
                inputValues.add(input.getValue());
                // now enter attack string
                input.setValue(attackString);
            }
        }
        else
        {
            // backup input value
            inputValues.add(input.getValue());

            // we make a single check
            // we got the correct input and so we only have to set the value
            input.setValue(attackString);
        }

        Assert.assertNotNull("No clickable found to submit form", clickable);
    }

    /**
     * Execute the request. Once pre-execution conditions have been meet, the execute method load the new page
     */
    @Override
    protected void execute() throws Exception
    {
        // load the page simply by firing the url
        // always make sure that loadPage* methods are used
        loadPageByClick(clickable, 10000);
    }

    /**
     * Validate the correctness of the result. Check if the page contains the attack string If it is so the risk is high
     * that we have an XSS vulnerability
     */
    @Override
    protected void postValidate() throws Exception
    {
        // get the result of the last action
        final HtmlPage page = getHtmlPage();

        // First, we check all common criteria. This code can be bundled and
        // reused
        // if needed. For the purpose of the programming example, we leave it
        // here as
        // detailed as possible.

        // StandardValidator.getInstance().validate(page);

        // check if we found the string on the resulting page
        final boolean check = page.getWebResponse().getContentAsString().contains(attackString);
        Assert.assertFalse("Page contains attack string '" + attackString + "'.", check);

        if (input == null)
        {
            // clean up form because we need the original form for further tests
            // this will be performed on the old page
            for (final HtmlElement input : inputs)
            {
                // check if we have at least one value
                Assert.assertFalse(inputValues.isEmpty());
                // and when restore value of input
                ((HtmlInput) input).setValue(inputValues.remove(0));
            }
        }
        else
        {
            // we should have only one value in our list
            Assert.assertTrue(inputValues.size() == 1);
            // restore value of the input
            input.setValue(inputValues.get(0));
        }
    }
}
