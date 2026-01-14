package com.xceptance.xlt.report.labelingrules;

import com.xceptance.xlt.report.providers.ActionReport;
import com.xceptance.xlt.report.providers.PageLoadTimingReport;
import com.xceptance.xlt.report.providers.RequestReport;
import com.xceptance.xlt.report.providers.TransactionReport;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class LabelingRule_Type_Test extends LabelingRuleTestBase
{
    @Test
    public void singleType()
    {
        final LabelingRule rule = getTypeLabelingRule("test", "R");
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        assertEquals(Set.of("R"), rule.getTypeCodes());

        // match
        final RequestReport requestReport = new RequestReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(requestReport));
        assertEquals("test", requestReport.label);

        // no match
        final TransactionReport transactionReport = new TransactionReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(transactionReport));
        assertEquals(null, transactionReport.label);
    }

    @Test
    public void multipleTypes()
    {
        final LabelingRule rule = getTypeLabelingRule("test", "T,A");
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        assertEquals(Set.of("T", "A"), rule.getTypeCodes());

        // match
        final TransactionReport transactionReport = new TransactionReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(transactionReport));
        assertEquals("test", transactionReport.label);

        // match
        final ActionReport actionReport = new ActionReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(actionReport));
        assertEquals("test", actionReport.label);

        // no match
        final RequestReport requestReport = new RequestReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(requestReport));
        assertEquals(null, requestReport.label);
    }

    @Test
    public void duplicateTypeCodes()
    {
        final LabelingRule rule = getTypeLabelingRule("test", "A,T,A");
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        assertEquals(Set.of("A", "T"), rule.getTypeCodes());

        // match
        final TransactionReport transactionReport = new TransactionReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(transactionReport));
        assertEquals("test", transactionReport.label);

        // match
        final ActionReport actionReport = new ActionReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(actionReport));
        assertEquals("test", actionReport.label);

        // no match
        final RequestReport requestReport = new RequestReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(requestReport));
        assertEquals(null, requestReport.label);
    }

    @Test
    public void allSupportedTypeCodes()
    {
        final LabelingRule rule = getTypeLabelingRule("test", "A;R T");
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        assertEquals(Set.of("A", "R", "T"), rule.getTypeCodes());

        // match
        final TransactionReport transactionReport = new TransactionReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(transactionReport));
        assertEquals("test", transactionReport.label);

        // match
        final ActionReport actionReport = new ActionReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(actionReport));
        assertEquals("test", actionReport.label);

        // match
        final RequestReport requestReport = new RequestReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(requestReport));
        assertEquals("test", requestReport.label);

        // no match
        final PageLoadTimingReport pageLoadTimingReport = new PageLoadTimingReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(pageLoadTimingReport));
        assertEquals(null, pageLoadTimingReport.label);
    }

    @Test
    public void nullTypeString()
    {
        // if no specific types are defined, match all types that support labeling (Transactions, Actions and Requests)
        final LabelingRule rule = getTypeLabelingRule("test", null);
        assertEquals(0, rule.getIncludeConditions().length);
        assertEquals(0, rule.getExcludeConditions().length);
        assertEquals(Set.of("T", "A", "R"), rule.getTypeCodes());

        // match
        final TransactionReport transactionReport = new TransactionReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(transactionReport));
        assertEquals("test", transactionReport.label);

        // match
        final ActionReport actionReport = new ActionReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(actionReport));
        assertEquals("test", actionReport.label);

        // match
        final RequestReport requestReport = new RequestReport();
        assertEquals(LabelingRule.ReturnState.STOP, rule.process(requestReport));
        assertEquals("test", requestReport.label);

        // no match
        final PageLoadTimingReport pageLoadTimingReport = new PageLoadTimingReport();
        assertEquals(LabelingRule.ReturnState.CONTINUE, rule.process(pageLoadTimingReport));
        assertEquals(null, pageLoadTimingReport.label);
    }
}
