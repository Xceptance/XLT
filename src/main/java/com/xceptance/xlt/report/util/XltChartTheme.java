/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
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
import java.util.Collection;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xceptance.xlt.report.util.JFreeChartUtils.MoreColors;

/**
 * The standard XLT chart theme, supporting both the classic appearance and the modern clean style.
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

    // Modern theme surface, typography, and grid tokens
    private static final Color MODERN_CHART_BACKGROUND_PAINT = Color.WHITE;

    private static final Color MODERN_TITLE_PAINT = new Color(0x0F172A);

    private static final Color MODERN_AXIS_LABEL_PAINT = new Color(0x1E293B);

    private static final Color MODERN_TICK_LABEL_PAINT = new Color(0x334155);

    private static final Color MODERN_GRID_LINE_PAINT = new Color(0xCBD5E1);

    private static final Color MODERN_BASELINE_PAINT = new Color(0x94A3B8);

    private static final Stroke MODERN_GRID_LINE_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
                                                                         new float[]
                                                                             {
                                                                                 3.0f, 3.0f
                                                                             }, 0.0f);

    private static final Stroke MODERN_BASELINE_STROKE = new BasicStroke(1.0f);

    private static final RectangleInsets MODERN_CHART_PADDING = new RectangleInsets(6, 4, 6, 6);

    private static final RectangleInsets MODERN_TEXTTITLE_PADDING = new RectangleInsets(2, 8, 6, 56);

    private static final RectangleInsets MODERN_LEGEND_PADDING = new RectangleInsets(4, 8, 2, 8);

    private static final double MODERN_COMBINED_PLOT_GAP = 10.0;

    // Modern data series palette
    private static final Color MODERN_COLOR_MOVING_AVERAGE = new Color(0x1D4ED8);

    private static final Color MODERN_COLOR_DOTS = new Color(168, 168, 168, 190);

    private static final Color MODERN_COLOR_LINE = new Color(0x64748B);

    private static final Color MODERN_COLOR_LINE_DIMMED = new Color(224, 224, 224, 90);

    private static final Color MODERN_COLOR_HISTOGRAM_FILL = new Color(59, 130, 246, 85);

    private static final Color MODERN_COLOR_HISTOGRAM_OUTLINE = new Color(0x3B82F6);

    private static final Color MODERN_COLOR_ERROR_BAR = new Color(0xDC2626);

    private static final Color MODERN_COLOR_ERROR_LINE = new Color(0xEF4444);

    private static final Color MODERN_COLOR_EVENT = new Color(0xF59E0B);

    private static final Color MODERN_COLOR_CAP = new Color(0xE11D48);

    private static final Color MODERN_COLOR_MEAN = new Color(0x0EA5E9);

    private static final Color MODERN_COLOR_MEDIAN = new Color(0x475569);

    private static final Stroke MODERN_CAP_STROKE = new BasicStroke(2.0f);

    private static final Stroke MODERN_HISTOGRAM_OUTLINE_STROKE = new BasicStroke(0.75f);

    private static final Stroke MODERN_MOVING_AVERAGE_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private static final Stroke MODERN_ERROR_LINE_STROKE = new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private static final int MAX_REMAP_SERIES_INDEX = 16;

    private final boolean modern;

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
     * Lazy holder for classic-to-modern series color mappings to avoid static initialization cycles with
     * {@link JFreeChartUtils}.
     */
    private static final class ColorRemapHolder
    {
        private static final Map<Integer, Color> LINE_COLORS = Map.ofEntries(
            Map.entry(JFreeChartUtils.COLOR_MOVING_AVERAGE.getRGB(), MODERN_COLOR_MOVING_AVERAGE),
            Map.entry(JFreeChartUtils.COLOR_LINE.getRGB(), MODERN_COLOR_LINE),
            Map.entry(JFreeChartUtils.COLOR_LINE_DIMMED.getRGB(), MODERN_COLOR_LINE_DIMMED),
            Map.entry(JFreeChartUtils.COLOR_ERROR.getRGB(), MODERN_COLOR_ERROR_LINE),
            Map.entry(JFreeChartUtils.COLOR_EVENT.getRGB(), MODERN_COLOR_EVENT),
            Map.entry(JFreeChartUtils.COLOR_MEAN.getRGB(), MODERN_COLOR_MEAN),
            Map.entry(JFreeChartUtils.COLOR_MEDIAN.getRGB(), MODERN_COLOR_MEDIAN),
            Map.entry(JFreeChartUtils.COLOR_MOVING_AVERAGE_ADDITIONAL_1.getRGB(), new Color(0x10B981)),
            Map.entry(JFreeChartUtils.COLOR_MOVING_AVERAGE_ADDITIONAL_2.getRGB(), new Color(0xF59E0B)),
            Map.entry(JFreeChartUtils.COLOR_MOVING_AVERAGE_ADDITIONAL_3.getRGB(), new Color(0x8B5CF6)),
            Map.entry(JFreeChartUtils.COLOR_MOVING_AVERAGE_ADDITIONAL_4.getRGB(), new Color(0x06B6D4)),
            Map.entry(JFreeChartUtils.COLOR_MOVING_AVERAGE_ADDITIONAL_5.getRGB(), new Color(0xF43F5E)),
            Map.entry(Color.BLUE.getRGB(), new Color(0x2563EB)),
            Map.entry(Color.MAGENTA.getRGB(), new Color(0xA855F7)),
            Map.entry(MoreColors.GREEN.getColor().getRGB(), new Color(0x10B981)),
            Map.entry(Color.BLACK.getRGB(), new Color(0x334155)),
            Map.entry(MoreColors.LIGHT_GREEN.getColor().getRGB(), new Color(0x84CC16)),
            Map.entry(MoreColors.BROWN.getColor().getRGB(), new Color(0xB45309)),
            Map.entry(MoreColors.STEEL_BLUE.getColor().getRGB(), new Color(0x0EA5E9)),
            Map.entry(MoreColors.LIGHT_GRAY.getColor().getRGB(), new Color(0x94A3B8)),
            Map.entry(MoreColors.ORANGE.getColor().getRGB(), new Color(0xF59E0B)),
            Map.entry(Color.CYAN.getRGB(), new Color(0x06B6D4)),
            Map.entry(Color.PINK.getRGB(), new Color(0xEC4899)),
            Map.entry(MoreColors.LILAC.getColor().getRGB(), new Color(0x8B5CF6)),
            Map.entry(Color.YELLOW.getRGB(), new Color(0xEAB308)));
    }

    /**
     * Constructs the classic XLT chart theme.
     */
    public XltChartTheme()
    {
        this(false);
    }

    /**
     * Constructs an XLT chart theme.
     *
     * @param modern
     *            {@code true} for the modern clean theme, {@code false} for the classic theme
     */
    public XltChartTheme(final boolean modern)
    {
        super(modern ? "XLT Modern" : "XLT");
        this.modern = modern;

        // text settings
        setExtraLargeFont(FONT_EXTRA_LARGE);
        setLargeFont(FONT_LARGE);
        setRegularFont(FONT_REGULAR);
        setSmallFont(FONT_SMALL);

        if (modern)
        {
            setTitlePaint(MODERN_TITLE_PAINT);
            setChartBackgroundPaint(MODERN_CHART_BACKGROUND_PAINT);
            setPlotBackgroundPaint(PLOT_BACKGROUND_PAINT);
            setPlotOutlinePaint(MODERN_BASELINE_PAINT);
            setAxisOffset(RectangleInsets.ZERO_INSETS);
            setDomainGridlinePaint(MODERN_GRID_LINE_PAINT);
            setRangeGridlinePaint(MODERN_GRID_LINE_PAINT);
            setBaselinePaint(MODERN_BASELINE_PAINT);
            setXYBarPainter(YX_BAR_PAINTER);
            setAxisLabelPaint(MODERN_AXIS_LABEL_PAINT);
            setTickLabelPaint(MODERN_TICK_LABEL_PAINT);
            setLegendBackgroundPaint(MODERN_CHART_BACKGROUND_PAINT);
            setLegendItemPaint(MODERN_AXIS_LABEL_PAINT);
            setItemLabelPaint(MODERN_AXIS_LABEL_PAINT);
        }
        else
        {
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(final JFreeChart chart)
    {
        super.apply(chart);

        final TextTitle title = chart.getTitle();
        if (modern)
        {
            chart.setAntiAlias(true);
            chart.setTextAntiAlias(true);
            chart.setPadding(MODERN_CHART_PADDING);

            if (title != null)
            {
                title.setMaximumLinesToDisplay(TEXTTITLE_MAXLINES);
                title.setHorizontalAlignment(HorizontalAlignment.LEFT);
                title.setTextAlignment(HorizontalAlignment.LEFT);
                title.setPadding(MODERN_TEXTTITLE_PADDING);
            }
        }
        else
        {
            if (title != null)
            {
                title.setMaximumLinesToDisplay(TEXTTITLE_MAXLINES);
                title.setPadding(TEXTTITLE_PADDING);
            }

            // set some additional properties
            chart.setPadding(CHART_PADDING);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyToPlot(final Plot plot)
    {
        super.applyToPlot(plot);

        if (!modern)
        {
            // set some additional properties
            plot.setOutlineStroke(_DEFAULT_OUTLINE_STROKE);
            return;
        }

        plot.setOutlineVisible(false);

        if (plot instanceof CombinedDomainXYPlot combinedPlot)
        {
            combinedPlot.setGap(MODERN_COMBINED_PLOT_GAP);
        }
        else if (plot instanceof XYPlot xyPlot && !(plot instanceof CombinedRangeXYPlot))
        {
            applyModernXYPlotStyling(xyPlot);
        }
    }

    private void applyModernXYPlotStyling(final XYPlot xyPlot)
    {
        // Skip invisible spacer plots used to pad right-hand combined histogram layouts
        if (xyPlot.getBackgroundAlpha() <= 0.0f)
        {
            return;
        }

        xyPlot.setDomainGridlinesVisible(false);
        xyPlot.setRangeGridlinesVisible(true);
        xyPlot.setRangeGridlinePaint(MODERN_GRID_LINE_PAINT);
        xyPlot.setRangeGridlineStroke(MODERN_GRID_LINE_STROKE);
        xyPlot.setRangeZeroBaselineVisible(true);
        xyPlot.setRangeZeroBaselinePaint(MODERN_BASELINE_PAINT);
        xyPlot.setRangeZeroBaselineStroke(MODERN_BASELINE_STROKE);

        final ValueAxis rangeAxis = xyPlot.getRangeAxis();
        if (rangeAxis instanceof NumberAxis && !(rangeAxis instanceof LogarithmicAxis) && rangeAxis.isAutoRange()
            && rangeAxis.getLowerBound() < 0.0 && rangeAxis.getUpperBound() <= 1.0)
        {
            // Anchor empty/all-zero subplots so the zero baseline sits cleanly at the bottom
            rangeAxis.setRange(0.0, 1.0);
        }

        // Add a clean left vertical spine for the right-hand distribution histogram
        final ValueAxis domainAxis = xyPlot.getDomainAxis();
        if (domainAxis != null && !domainAxis.isVisible() && rangeAxis != null && !rangeAxis.isVisible())
        {
            xyPlot.setDomainZeroBaselineVisible(true);
            xyPlot.setDomainZeroBaselinePaint(MODERN_BASELINE_PAINT);
            xyPlot.setDomainZeroBaselineStroke(MODERN_BASELINE_STROKE);
        }

        remapCapMarkersAndLegend(xyPlot);
    }

    private void remapCapMarkersAndLegend(final XYPlot xyPlot)
    {
        final Collection<Marker> rangeMarkers = xyPlot.getRangeMarkers(Layer.FOREGROUND);
        if (rangeMarkers != null)
        {
            for (final Marker marker : rangeMarkers)
            {
                if (JFreeChartUtils.COLOR_CAP.equals(marker.getPaint()))
                {
                    marker.setPaint(MODERN_COLOR_CAP);
                    marker.setStroke(MODERN_CAP_STROKE);
                }
            }
        }

        final LegendItemCollection fixedLegendItems = xyPlot.getFixedLegendItems();
        if (fixedLegendItems != null)
        {
            for (int i = 0; i < fixedLegendItems.getItemCount(); i++)
            {
                final LegendItem item = fixedLegendItems.get(i);
                if ("Cap".equals(item.getLabel()) && JFreeChartUtils.COLOR_CAP.equals(item.getFillPaint()))
                {
                    item.setFillPaint(MODERN_COLOR_CAP);
                    item.setOutlinePaint(MODERN_COLOR_CAP);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyToAbstractRenderer(final AbstractRenderer renderer)
    {
        if (!modern)
        {
            // implemented empty -> we do not want the theme to overwrite our programmatically set colors and strokes
            return;
        }

        if (renderer instanceof DotsRenderer dotsRenderer)
        {
            dotsRenderer.setSeriesPaint(0, MODERN_COLOR_DOTS);
        }
        else if (renderer instanceof XYBarRenderer barRenderer)
        {
            remapBarRendererColors(barRenderer);
        }
        else if (renderer instanceof XYLineAndShapeRenderer lineRenderer)
        {
            remapLineRendererColors(lineRenderer);
        }
    }

    private void remapBarRendererColors(final XYBarRenderer barRenderer)
    {
        for (int i = 0; i < MAX_REMAP_SERIES_INDEX; i++)
        {
            final Paint paint = barRenderer.getSeriesPaint(i);
            if (!(paint instanceof Color color))
            {
                continue;
            }

            if (JFreeChartUtils.COLOR_HISTOGRAM.equals(color))
            {
                barRenderer.setSeriesPaint(i, MODERN_COLOR_HISTOGRAM_FILL);
                barRenderer.setDrawBarOutline(true);
                barRenderer.setSeriesOutlinePaint(i, MODERN_COLOR_HISTOGRAM_OUTLINE);
                barRenderer.setSeriesOutlineStroke(i, MODERN_HISTOGRAM_OUTLINE_STROKE);
            }
            else if (JFreeChartUtils.COLOR_ERROR.equals(color))
            {
                barRenderer.setSeriesPaint(i, MODERN_COLOR_ERROR_BAR);
            }
            else if (JFreeChartUtils.COLOR_EVENT.equals(color))
            {
                barRenderer.setSeriesPaint(i, MODERN_COLOR_EVENT);
            }
            else
            {
                final Color remapped = ColorRemapHolder.LINE_COLORS.get(color.getRGB());
                if (remapped != null)
                {
                    barRenderer.setSeriesPaint(i, remapped);
                }
            }
        }
    }

    private void remapLineRendererColors(final XYLineAndShapeRenderer lineRenderer)
    {
        for (int i = 0; i < MAX_REMAP_SERIES_INDEX; i++)
        {
            final Paint paint = lineRenderer.getSeriesPaint(i);
            if (!(paint instanceof Color color))
            {
                continue;
            }

            if (lineRenderer.getSeriesStroke(i) == null)
            {
                if (JFreeChartUtils.COLOR_MOVING_AVERAGE.equals(color))
                {
                    lineRenderer.setSeriesStroke(i, MODERN_MOVING_AVERAGE_STROKE);
                }
                else if (JFreeChartUtils.COLOR_ERROR.equals(color))
                {
                    lineRenderer.setSeriesStroke(i, MODERN_ERROR_LINE_STROKE);
                }
            }

            final Color remapped = ColorRemapHolder.LINE_COLORS.get(color.getRGB());
            if (remapped != null)
            {
                lineRenderer.setSeriesPaint(i, remapped);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyToTitle(final Title title)
    {
        super.applyToTitle(title);

        if (title instanceof LegendTitle legendTitle)
        {
            if (modern)
            {
                legendTitle.setFrame(BlockBorder.NONE);
                legendTitle.setPadding(MODERN_LEGEND_PADDING);
                legendTitle.setItemPaint(MODERN_AXIS_LABEL_PAINT);
            }
            else
            {
                legendTitle.setFrame(LEGEND_BORDER);
                legendTitle.setPadding(LEGEND_PADDING);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyToValueAxis(final ValueAxis axis)
    {
        super.applyToValueAxis(axis);

        axis.setTickLabelFont(FONT_SMALL);

        if (modern)
        {
            axis.setTickMarkPaint(MODERN_BASELINE_PAINT);
            axis.setAxisLineVisible(true);
            axis.setAxisLinePaint(MODERN_BASELINE_PAINT);
            axis.setAxisLineStroke(MODERN_BASELINE_STROKE);
        }
        else
        {
            axis.setTickMarkPaint(AXIS_TICK_MARK_PAINT);
            axis.setAxisLineVisible(AXIS_LINE_VISIBLE);
        }
    }
}
