/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.ui.RectangleEdge;

/**
 * A specialized subclass of the {@link XYLineAndShapeRenderer} that requires an {@link IntervalXYDataset} and
 * represents the y-interval as a vertical line.
 * 
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public class MinMaxRenderer extends XYLineAndShapeRenderer
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4097521103727872464L;

    /**
     * Creates a new renderer.
     * 
     * @param lines
     *            show lines between data items?
     * @param shapes
     *            show a shape for each data item?
     */
    public MinMaxRenderer(final boolean lines, final boolean shapes)
    {
        super(lines, shapes);

        // tell the super class to draw the graph as a whole
        setDrawSeriesLineAsPath(true);
    }

    /**
     * Draws the item (first pass). This method draws the lines connecting the items. Instead of drawing separate lines,
     * a GeneralPath is constructed and drawn at the end of the series painting.
     * 
     * @param g2
     *            the graphics device.
     * @param state
     *            the renderer state.
     * @param plot
     *            the plot (can be used to obtain standard color information etc).
     * @param dataset
     *            the dataset.
     * @param pass
     *            the pass.
     * @param series
     *            the series index (zero-based).
     * @param item
     *            the item index (zero-based).
     * @param domainAxis
     *            the domain axis.
     * @param rangeAxis
     *            the range axis.
     * @param dataArea
     *            the area within which the data is being drawn.
     */
    @Override
    protected void drawPrimaryLineAsPath(final XYItemRendererState state, final Graphics2D g2, final XYPlot plot, final XYDataset dataset,
                                         final int pass, final int series, final int item, final ValueAxis domainAxis,
                                         final ValueAxis rangeAxis, final Rectangle2D dataArea)
    {
        final IntervalXYDataset data = (IntervalXYDataset) dataset;

        final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        // get the data points
        final double x = data.getXValue(series, item);
        final double yLow = data.getStartYValue(series, item);
        final double yHigh = data.getEndYValue(series, item);

        // get the translated coordinates
        final double xx = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
        final double yyLow = rangeAxis.valueToJava2D(yLow, dataArea, yAxisLocation);
        final double yyHigh = rangeAxis.valueToJava2D(yHigh, dataArea, yAxisLocation);

        // update path to reflect latest point
        final State s = (State) state;
        if (!Double.isNaN(xx) && !Double.isNaN(yyLow) && !Double.isNaN(yyHigh))
        {
            final PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL)
            {
                final float xxxLow = (float) yyHigh;
                final float xxxHigh = (float) yyLow;
                final float yyy = (float) xx;

                if (s.isLastPointGood())
                {
                    s.seriesPath.lineTo(xxxLow, yyy);
                }
                else
                {
                    s.seriesPath.moveTo(xxxLow, yyy);
                }

                if (xxxHigh != xxxLow)
                {
                    s.seriesPath.lineTo(xxxHigh, yyy);
                }
            }
            else
            {
                final float xxx = (float) xx;
                final float yyyLow = (float) yyLow;
                final float yyyHigh = (float) yyHigh;

                if (s.isLastPointGood())
                {
                    s.seriesPath.lineTo(xxx, yyyHigh);
                }
                else
                {
                    s.seriesPath.moveTo(xxx, yyyHigh);
                }

                if (yyyHigh != yyyLow)
                {
                    s.seriesPath.lineTo(xxx, yyyLow);
                }
            }

            s.setLastPointGood(true);
        }
        else
        {
            s.setLastPointGood(false);
        }

        // if this is the last item, draw the path now
        if (item == s.getLastItemIndex())
        {
            drawFirstPassShape(g2, pass, series, item, s.seriesPath);
        }
    }

    /**
     * Returns the range of values the renderer requires to display all the items from the specified dataset.
     * 
     * @param dataset
     *            the dataset (<code>null</code> permitted).
     * @return the range, or <code>null</code> if the dataset is <code>null</code> or empty
     */
    @Override
    public Range findRangeBounds(final XYDataset dataset)
    {
        if (dataset != null)
        {
            return DatasetUtils.findRangeBounds(dataset, true);
        }
        else
        {
            return null;
        }
    }
}
