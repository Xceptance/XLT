package com.xceptance.xlt.engine.resultbrowser;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * Container that holds all information about a transaction necessary to be processed by the results browser.
 *
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
class TransactionInfo
{
    public String user;

    public long date;

    public final List<ActionInfo> actions = new ArrayList<>();

    public final List<NameValuePair> valueLog = new ArrayList<>();
}
