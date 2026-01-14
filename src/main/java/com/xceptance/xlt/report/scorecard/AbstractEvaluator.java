package com.xceptance.xlt.report.scorecard;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
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
        xstream.alias("group", Scorecard.Group.class);
        xstream.alias("rule", Scorecard.Rule.class);
        xstream.alias("check", Scorecard.Rule.Check.class);

        // let XStream do its job
        xstream.toXML(scorecard, writer);
    }

    protected static double getPercentage(final int numerator, final int denominator)
    {
        return denominator > 0 ? (Math.round((numerator * 1000.0) / denominator) / 10.0) : 0.0;
    }
}
