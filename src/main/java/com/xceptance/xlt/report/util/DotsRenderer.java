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
package com.xceptance.xlt.report.util;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.ui.RectangleEdge;

/**
 * A specialized subclass of the {@link XYDotRenderer} that requires a {@link MinMaxTimeSeriesDataItem} object and draws
 * all known values in this data item as dots.
 */
public class DotsRenderer extends XYDotRenderer
{
    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2
     *            the graphics device.
     * @param state
     *            the renderer state.
     * @param dataArea
     *            the area within which the data is being drawn.
     * @param info
     *            collects information about the drawing.
     * @param plot
     *            the plot (can be used to obtain standard color information etc).
     * @param domainAxis
     *            the domain (horizontal) axis.
     * @param rangeAxis
     *            the range (vertical) axis.
     * @param dataset
     *            the dataset.
     * @param series
     *            the series index (zero-based).
     * @param item
     *            the item index (zero-based).
     * @param crosshairState
     *            crosshair information for the plot (<code>null</code> permitted).
     * @param pass
     *            the pass index.
     */
    @Override
    public void drawItem(final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info,
                         final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset,
                         final int series, final int item, final CrosshairState crosshairState, final int pass)
    {
        // do nothing if item is not visible
        if (!getItemVisible(series, item))
        {
            return;
        }

        // get the data points
        final MinMaxTimeSeriesCollection data = (MinMaxTimeSeriesCollection) dataset;

        final double x = data.getXValue(series, item);
        final double[] yValues = data.getValues(series, item);

        if (yValues.length > 0)
        {
            // calculate the basics only once
            final int dotWidth = getDotWidth();
            final int dotHeight = getDotHeight();

            final double adjx = (dotWidth - 1) / 2.0;
            final double adjy = (dotHeight - 1) / 2.0;

            final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
            final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

            g2.setPaint(getItemPaint(series, item));

            final double transX = domainAxis.valueToJava2D(x, dataArea, xAxisLocation) - adjx;

            // draw each value as a dot
            for (final double y : yValues)
            {
                final double transY = rangeAxis.valueToJava2D(y, dataArea, yAxisLocation) - adjy;

                final PlotOrientation orientation = plot.getOrientation();
                if (orientation == PlotOrientation.HORIZONTAL)
                {
                    g2.fillRect((int) transY, (int) transX, dotHeight, dotWidth);
                }
                else if (orientation == PlotOrientation.VERTICAL)
                {
                    g2.fillRect((int) transX, (int) transY, dotWidth, dotHeight);
                }
            }
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
