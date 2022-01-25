/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.InputStream;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The standard XLT chart theme.
 */
public class XltChartTheme extends StandardChartTheme
{
    private static final Color _DEFAULT_OUTLINE_PAINT = new Color(0xBFBFBF);

    private static final Stroke _DEFAULT_OUTLINE_STROKE = new BasicStroke(1.0f);

    private static final boolean AXIS_LINE_VISIBLE = false;

    private static final Paint AXIS_TICK_MARK_PAINT = _DEFAULT_OUTLINE_PAINT;

    private static final Paint CHART_BACKGROUND_PAINT = new Color(0xFAFAFA);

    private static final RectangleInsets CHART_PADDING = new RectangleInsets(8, 0, 8, 2);

    private static final Font FONT_EXTRA_LARGE;

    private static final Font FONT_LARGE;

    private static final Font FONT_REGULAR;

    private static final Font FONT_SMALL;

    private static final LineBorder LEGEND_BORDER = new LineBorder(_DEFAULT_OUTLINE_PAINT, _DEFAULT_OUTLINE_STROKE,
                                                                   new RectangleInsets(1, 1, 1, 1));

    private static final RectangleInsets LEGEND_PADDING = new RectangleInsets(1, 4, 1, 4);

    private static final Paint PLOT_BACKGROUND_PAINT = Color.WHITE;

    private static final Paint PLOT_GRID_LINE_PAINT = Color.LIGHT_GRAY;

    private static final Paint TEXT_PAINT = new Color(0x333333);

    private static final XYBarPainter YX_BAR_PAINTER = new StandardXYBarPainter();

    private static final Logger LOG = LoggerFactory.getLogger(XltChartTheme.class);

    private static final int TEXTTITLE_MAXLINES = 1;

    private static final RectangleInsets TEXTTITLE_PADDING = new RectangleInsets(1, 100, 1, 100);

    /*
     * Initialize the fonts.
     */
    static
    {
        Font extraLargeFont;
        Font largeFont;
        Font regularFont;
        Font smallFont;

        try
        {
            // load base fonts
            final Font boldFont = loadFont("Roboto-Medium.ttf");
            final Font plainFont = loadFont("Roboto-Regular.ttf");

            // derive fonts from base fonts
            extraLargeFont = boldFont.deriveFont(13f);
            largeFont = plainFont.deriveFont(12f);
            regularFont = plainFont.deriveFont(12f);
            smallFont = plainFont.deriveFont(10f);
        }
        catch (final Exception e)
        {
            LOG.error("Failed to load chart text font", e);

            // use fall-back fonts
            extraLargeFont = new Font("SansSerif", Font.BOLD, 12);
            largeFont = new Font("SansSerif", Font.PLAIN, 12);
            regularFont = new Font("SansSerif", Font.PLAIN, 12);
            smallFont = new Font("SansSerif", Font.PLAIN, 10);
        }

        // initialize font constants
        FONT_EXTRA_LARGE = extraLargeFont;
        FONT_LARGE = largeFont;
        FONT_REGULAR = regularFont;
        FONT_SMALL = smallFont;
    }

    /**
     * Loads the font with the given file name from the class path, relative to this class.
     * 
     * @param fontFileName
     *            the name of the font file
     * @return the font
     * @throws Exception
     *             if anything goes wrong
     */
    private static Font loadFont(final String fontFileName) throws Exception
    {
        try (final InputStream fontStream = XltChartTheme.class.getResourceAsStream(fontFileName))
        {
            final Font font = Font.createFont(Font.TRUETYPE_FONT, fontStream);

            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);

            return font;
        }
    }

    /**
     * Constructor.
     */
    public XltChartTheme()
    {
        super("XLT");

        // text settings
        setExtraLargeFont(FONT_EXTRA_LARGE);
        setLargeFont(FONT_LARGE);
        setRegularFont(FONT_REGULAR);
        setSmallFont(FONT_SMALL);

        // chart settings
        setTitlePaint(TEXT_PAINT);
        setChartBackgroundPaint(CHART_BACKGROUND_PAINT);

        // plot settings
        setPlotBackgroundPaint(PLOT_BACKGROUND_PAINT);
        setPlotOutlinePaint(_DEFAULT_OUTLINE_PAINT);
        setAxisOffset(RectangleInsets.ZERO_INSETS);
        setDomainGridlinePaint(PLOT_GRID_LINE_PAINT);
        setRangeGridlinePaint(PLOT_GRID_LINE_PAINT);

        // bar plot settings
        setXYBarPainter(YX_BAR_PAINTER);

        // axis settings
        setAxisLabelPaint(TEXT_PAINT);
        setTickLabelPaint(TEXT_PAINT);

        // legend settings
        setLegendItemPaint(TEXT_PAINT);

        // annotations/renderers
        setItemLabelPaint(TEXT_PAINT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(final JFreeChart chart)
    {
        super.apply(chart);

        TextTitle title = chart.getTitle();
        if (title != null)
        {
            title.setMaximumLinesToDisplay(TEXTTITLE_MAXLINES);
            title.setPadding(TEXTTITLE_PADDING);
        }

        // set some additional properties
        chart.setPadding(CHART_PADDING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyToPlot(final Plot plot)
    {
        super.applyToPlot(plot);

        // set some additional properties
        plot.setOutlineStroke(_DEFAULT_OUTLINE_STROKE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyToAbstractRenderer(final AbstractRenderer renderer)
    {
        // implemented empty -> we do not want the theme to overwrite our programmatically set colors and strokes
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyToTitle(final Title title)
    {
        super.applyToTitle(title);

        // set some additional properties
        if (title instanceof LegendTitle)
        {
            final LegendTitle legendTitle = (LegendTitle) title;

            legendTitle.setFrame(LEGEND_BORDER);
            legendTitle.setPadding(LEGEND_PADDING);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyToValueAxis(final ValueAxis axis)
    {
        super.applyToValueAxis(axis);

        // set some additional properties
        axis.setTickLabelFont(FONT_SMALL);
        axis.setTickMarkPaint(AXIS_TICK_MARK_PAINT);
        axis.setAxisLineVisible(AXIS_LINE_VISIBLE);
    }
}
