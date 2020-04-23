/*
 * Copyright (c) 2005-2020 Xceptance Software Technologies GmbH
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
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.util.ShapeUtils;

/**
 * Enhances the {@link LineAndShapeRenderer} to draw a continuous line even if some categories do not have a value
 * assigned. The implementation is basically copied from the super class and modified a little.
 */
public class CategoryItemRenderer extends LineAndShapeRenderer implements Serializable
{
    /** For serialization. */
    private static final long serialVersionUID = -7793786349384231896L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawItem(final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot,
                         final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row,
                         final int column, final int pass)
    {
        // do nothing if item is not visible
        if (!getItemVisible(row, column))
        {
            return;
        }

        // do nothing if both the line and shape are not visible
        if (!getItemLineVisible(row, column) && !getItemShapeVisible(row, column))
        {
            return;
        }

        // nothing is drawn for null...
        final Number v = dataset.getValue(row, column);
        if (v == null)
        {
            return;
        }

        final int visibleRow = state.getVisibleSeriesIndex(row);
        if (visibleRow < 0)
        {
            return;
        }
        final int visibleRowCount = state.getVisibleSeriesCount();

        final PlotOrientation orientation = plot.getOrientation();

        // current data point...
        double x1;
        if (getUseSeriesOffset())
        {
            x1 = domainAxis.getCategorySeriesMiddle(column, dataset.getColumnCount(), visibleRow, visibleRowCount, getItemMargin(),
                                                    dataArea, plot.getDomainAxisEdge());
        }
        else
        {
            x1 = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot.getDomainAxisEdge());
        }
        final double value = v.doubleValue();
        final double y1 = rangeAxis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());

        if (pass == 0 && getItemLineVisible(row, column))
        {
            if (column != 0)
            {
                Number previousValue = null;
                int previousValueColumn = column;
                while (previousValueColumn > 0 && previousValue == null)
                {
                    previousValue = dataset.getValue(row, --previousValueColumn);
                }

                if (previousValue != null)
                {
                    // previous data point...
                    final double previous = previousValue.doubleValue();

                    double x0;
                    if (getUseSeriesOffset())
                    {
                        x0 = domainAxis.getCategorySeriesMiddle(previousValueColumn, dataset.getColumnCount(), visibleRow, visibleRowCount,
                                                                getItemMargin(), dataArea, plot.getDomainAxisEdge());
                    }
                    else
                    {
                        x0 = domainAxis.getCategoryMiddle(previousValueColumn, getColumnCount(), dataArea, plot.getDomainAxisEdge());
                    }

                    final double y0 = rangeAxis.valueToJava2D(previous, dataArea, plot.getRangeAxisEdge());

                    Line2D line = null;
                    if (orientation == PlotOrientation.HORIZONTAL)
                    {
                        line = new Line2D.Double(y0, x0, y1, x1);
                    }
                    else if (orientation == PlotOrientation.VERTICAL)
                    {
                        line = new Line2D.Double(x0, y0, x1, y1);
                    }

                    g2.setPaint(getItemPaint(row, column));
                    g2.setStroke(getItemStroke(row, column));
                    g2.draw(line);
                }
            }
        }

        if (pass == 1)
        {
            Shape shape = getItemShape(row, column);
            if (orientation == PlotOrientation.HORIZONTAL)
            {
                shape = ShapeUtils.createTranslatedShape(shape, y1, x1);
            }
            else if (orientation == PlotOrientation.VERTICAL)
            {
                shape = ShapeUtils.createTranslatedShape(shape, x1, y1);
            }

            if (getItemShapeVisible(row, column))
            {
                if (getItemShapeFilled(row, column))
                {
                    if (getUseFillPaint())
                    {
                        g2.setPaint(getItemFillPaint(row, column));
                    }
                    else
                    {
                        g2.setPaint(getItemPaint(row, column));
                    }
                    g2.fill(shape);
                }
                if (getDrawOutlines())
                {
                    if (getUseOutlinePaint())
                    {
                        g2.setPaint(getItemOutlinePaint(row, column));
                    }
                    else
                    {
                        g2.setPaint(getItemPaint(row, column));
                    }
                    g2.setStroke(getItemOutlineStroke(row, column));
                    g2.draw(shape);
                }
            }

            // draw the item label if there is one...
            if (isItemLabelVisible(row, column))
            {
                if (orientation == PlotOrientation.HORIZONTAL)
                {
                    drawItemLabel(g2, orientation, dataset, row, column, y1, x1, (value < 0.0));
                }
                else if (orientation == PlotOrientation.VERTICAL)
                {
                    drawItemLabel(g2, orientation, dataset, row, column, x1, y1, (value < 0.0));
                }
            }

            // submit the current data point as a crosshair candidate
            final int datasetIndex = plot.indexOf(dataset);
            updateCrosshairValues(state.getCrosshairState(), dataset.getRowKey(row), dataset.getColumnKey(column), value, datasetIndex, x1,
                                  y1, orientation);

            // add an item entity, if this information is being collected
            final EntityCollection entities = state.getEntityCollection();
            if (entities != null)
            {
                addItemEntity(entities, dataset, row, column, shape);
            }
        }
    }
}
