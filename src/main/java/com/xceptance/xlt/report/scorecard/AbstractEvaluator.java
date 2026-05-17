package com.xceptance.xlt.report.scorecard;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.thoughtworks.xstream.XStream;
import com.xceptance.xlt.common.XltConstants;
import com.xceptance.xlt.report.util.xstream.SanitizingDomDriver;

import net.sf.saxon.s9api.Processor;

/**
 * Base class for scorecard evaluators.
 */
public abstract class AbstractEvaluator
{
    protected final File configFile;

    protected final Processor processor;

    protected AbstractEvaluator(final File configFile, final Processor processor)
    {
        this.configFile = Objects.requireNonNull(configFile);
        this.processor = Objects.requireNonNull(processor);
    }

    public abstract Scorecard evaluate(final File documentFile);

    /**
     * Writes the given scorecard as serialized XML to the given destination writer.
     *
     * @param scorecard
     *                      the scorecard to be written
     * @param writer
     *                      the destination to write serialized XML to
     * @throws IOException
     *                         thrown if scorecard could not be written
     */
    public void writeScorecard(final Scorecard scorecard, final Writer writer) throws IOException
    {
        // writer the XML preamble
        writer.write(XltConstants.XML_HEADER);

        // create and configure XStream instance
        final XStream xstream = new XStream(new SanitizingDomDriver());
        xstream.autodetectAnnotations(true);
        xstream.aliasSystemAttribute(null, "class");
        xstream.setMode(XStream.NO_REFERENCES);

        // Important for backward compatibility/consistency
        xstream.alias("scorecard", Scorecard.class);
        xstream.alias("outcome", Scorecard.Result.class);
        xstream.alias("error", Scorecard.Error.class);
        xstream.alias("group", Scorecard.Group.class);
        xstream.alias("rule", Scorecard.Rule.class);
        xstream.alias("check", Scorecard.Rule.Check.class);

        // let XStream do its job
        xstream.toXML(scorecard, writer);
    }

    /**
     * Returns the ratio in percent rounded to one decimal place.
     *
     * @param numerator
     *                        the numerator value
     * @param denominator
     *                        the denominator value
     * @return given ratio in percent
     */
    protected static double getPercentage(final int numerator, final int denominator)
    {
        return denominator > 0 ? (Math.round((numerator * 1000.0) / denominator) / 10.0) : 0.0;
    }

    /**
     * Formats the given value using the specified formatter string.
     *
     * @param value
     *                      the value to format
     * @param formatter
     *                      the Java string formatter syntax
     * @return the formatted value
     */
    protected String formatValue(final String value, final String formatter)
    {
        if (value == null || formatter == null)
        {
            return value;
        }

        try
        {
            // try to parse as double first
            try
            {
                return String.format(java.util.Locale.US, formatter, Double.valueOf(value));
            }
            catch (final NumberFormatException e1)
            {
                // try to parse as long
                try
                {
                    return String.format(java.util.Locale.US, formatter, Long.valueOf(value));
                }
                catch (final NumberFormatException e2)
                {
                    // continue as string
                    return String.format(java.util.Locale.US, formatter, value);
                }
            }
        }
        catch (final Exception e)
        {
            // fallback to original value on formatting error
            return value;
        }
    }

    /**
     * Checks if the given sequence starts with any of the provided search strings.
     *
     * @param sequence
     *                          the sequence to check
     * @param searchStrings
     *                          the search strings to look for
     * @return <code>true</code> if the sequence starts with any of the search strings, <code>false</code> otherwise
     */
    protected boolean startsWithAny(final String sequence, final String... searchStrings)
    {
        if (sequence != null && searchStrings != null)
        {
            for (final String searchString : searchStrings)
            {
                if (sequence.startsWith(searchString))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Concludes the evaluation of a rule by determining its final status based on its checks. Sets the rule's status,
     * message, and points accordingly.
     *
     * @param rule
     *                 the rule to conclude
     */
    protected void conclude(final Scorecard.Rule rule)
    {
        // nothing to do for disabled rules
        if (!rule.isEnabled())
        {
            return;
        }

        Status lastStatus = Status.PASSED;
        // loop through rule's checks
        for (final Scorecard.Rule.Check c : rule.getChecks())
        {
            final Status checkStatus = c.getStatus();
            // ignore skipped checks
            if (checkStatus.isSkipped())
            {
                continue;
            }

            // remember most recent check status that doesn't indicate a passed check
            // or just the very first check status if all checks did pass
            if (!checkStatus.isPassed())
            {
                lastStatus = checkStatus;
                // encountered erroneous check -> set rule message to the check's error message and stop looping
                if (checkStatus.isError())
                {
                    rule.setMessage(String.format("[Check #%d] %s", c.getIndex(), c.getErrorMessage()));
                    break;
                }
            }
        }

        // negate rule status if desired
        if (rule.getDefinition().isNegateResult())
        {
            lastStatus = lastStatus.negate();
        }
        rule.setStatus(lastStatus);
        if (lastStatus.isPassed())
        {
            rule.setMessage(rule.getDefinition().getSuccessMessage());
            rule.setPoints(rule.getDefinition().getPoints());
        }
        else if (lastStatus.isFailed())
        {
            rule.setMessage(rule.getDefinition().getFailMessage());
        }
    }

    /**
     * Concludes the evaluation of a group by determining its final status based on its rules. Sets the group's status,
     * message, points, and determines if the test should fail.
     *
     * @param group
     *                  the group to conclude
     * @return true if the test should fail due to this group's evaluation
     */
    protected boolean conclude(final Scorecard.Group group)
    {
        // nothing to do for disabled groups
        if (!group.isEnabled())
        {
            return false;
        }

        Scorecard.Rule firstMatch = null, lastMatch = null;
        int maxPoints = 0, sumPointsTotal = 0, sumPointsMatching = 0;
        boolean somePassed = false, someFailed = false, someError = false;

        final List<Scorecard.Rule> rules = group.getRules();
        for (final Scorecard.Rule rule : rules)
        {
            if (!rule.getDefinition().isEnabled())
            {
                continue;
            }

            final int pointsAchieved = rule.getPoints();
            final int rulePoints = rule.getDefinition().getPoints();
            final Status ruleStatus = rule.getStatus();

            if (ruleStatus.isError())
            {
                someError = true;
                break;
            }

            maxPoints = Math.max(maxPoints, rulePoints);
            sumPointsTotal += rulePoints;

            if (ruleStatus.isPassed())
            {
                somePassed = true;
                if (firstMatch == null)
                {
                    firstMatch = rule;
                }
                lastMatch = rule;
                sumPointsMatching += pointsAchieved;
            }
            else if (ruleStatus.isFailed())
            {
                someFailed = true;
            }
        }

        if (someError)
        {
            group.setStatus(Status.ERROR);
            group.setPoints(0);
            group.setTotalPoints(0);
            return false;
        }

        final int points, totalPoints;
        final Status groupStatus;
        final List<Scorecard.Rule> rulesThatMayFailTest;
        final GroupDefinition.Mode mode = group.getDefinition().getMode();

        if (mode == GroupDefinition.Mode.allPassed)
        {
            groupStatus = someFailed ? Status.FAILED : somePassed ? Status.PASSED : Status.SKIPPED;
            points = sumPointsMatching;
            totalPoints = sumPointsTotal;
            rulesThatMayFailTest = rules;
        }
        else if (mode == GroupDefinition.Mode.firstPassed || mode == GroupDefinition.Mode.lastPassed)
        {
            final Scorecard.Rule triggerRule = (mode == GroupDefinition.Mode.firstPassed) ? firstMatch : lastMatch;
            final int idx = triggerRule != null ? rules.indexOf(triggerRule) : -1;

            groupStatus = somePassed ? Status.PASSED : someFailed ? Status.FAILED : Status.SKIPPED;
            points = triggerRule != null ? triggerRule.getPoints() : 0;
            totalPoints = maxPoints;
            rulesThatMayFailTest = idx < 0 ? rules : rules.subList(0, idx + 1);
        }
        else
        {
            groupStatus = Status.ERROR;
            points = 0;
            totalPoints = 0;
            rulesThatMayFailTest = Collections.emptyList();
        }

        boolean testFailed = rulesThatMayFailTest.stream().filter((rule) -> {
            final boolean ruleFailedTest = rule.mayFailTest();
            if (ruleFailedTest)
            {
                rule.setTestFailed();
            }
            return ruleFailedTest;
        }).count() > 0L;

        if (group.mayFailTest())
        {
            group.setTestFailed();
            testFailed = true;
        }

        group.setStatus(groupStatus);
        group.setPoints(points);
        group.setTotalPoints(totalPoints);

        final String groupMessage = groupStatus.isPassed() ? group.getDefinition().getSuccessMessage()
                                                           : groupStatus.isFailed() ? group.getDefinition().getFailMessage() : null;
        if (groupMessage != null)
        {
            group.setMessage(groupMessage);
        }

        return testFailed;
    }
}
