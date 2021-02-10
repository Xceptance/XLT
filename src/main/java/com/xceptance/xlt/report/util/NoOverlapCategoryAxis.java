/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.xceptance.xlt.report.util;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryTick;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.ui.RectangleEdge;

/**
 * This class implements X-axis label skipping algorithm to avoid drawing overlapping labels.
 * 
 * @author Kohsuke Kawaguchi
 */
public class NoOverlapCategoryAxis extends CategoryAxis
{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5323337986483547113L;

    public NoOverlapCategoryAxis(final String label)
    {
        super(label);
    }

    @Override
    protected AxisState drawCategoryLabels(final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea,
                                           final RectangleEdge edge, final AxisState state, final PlotRenderingInfo plotState)
    {

        if (state == null)
        {
            throw new IllegalArgumentException("Null 'state' argument.");
        }

        if (isTickLabelsVisible())
        {
            final java.util.List<?> ticks = refreshTicks(g2, state, plotArea, edge);
            state.setTicks(ticks);

            // remember the last drawn label so that we can avoid drawing overlapping labels.
            Rectangle2D r = null;

            int categoryIndex = 0;
            @SuppressWarnings("unchecked")
            final Iterator<CategoryTick> iterator = (Iterator<CategoryTick>) ticks.iterator();
            while (iterator.hasNext())
            {

                final CategoryTick tick = iterator.next();
                g2.setFont(getTickLabelFont(tick.getCategory()));
                g2.setPaint(getTickLabelPaint(tick.getCategory()));

                final CategoryLabelPosition position = getCategoryLabelPositions().getLabelPosition(edge);
                double x0 = 0.0;
                double x1 = 0.0;
                double y0 = 0.0;
                double y1 = 0.0;
                if (edge == RectangleEdge.TOP)
                {
                    x0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
                    x1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
                    y1 = state.getCursor() - getCategoryLabelPositionOffset();
                    y0 = y1 - state.getMax();
                }
                else if (edge == RectangleEdge.BOTTOM)
                {
                    x0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
                    x1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
                    y0 = state.getCursor() + getCategoryLabelPositionOffset();
                    y1 = y0 + state.getMax();
                }
                else if (edge == RectangleEdge.LEFT)
                {
                    y0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
                    y1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
                    x1 = state.getCursor() - getCategoryLabelPositionOffset();
                    x0 = x1 - state.getMax();
                }
                else if (edge == RectangleEdge.RIGHT)
                {
                    y0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, edge);
                    y1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, edge);
                    x0 = state.getCursor() + getCategoryLabelPositionOffset();
                    x1 = x0 - state.getMax();
                }
                final Rectangle2D area = new Rectangle2D.Double(x0, y0, (x1 - x0), (y1 - y0));
                if (r == null || !r.intersects(area))
                {
                    final Point2D anchorPoint = position.getCategoryAnchor().getAnchorPoint(area);
                    final TextBlock block = tick.getLabel();
                    block.draw(g2, (float) anchorPoint.getX(), (float) anchorPoint.getY(), position.getLabelAnchor(),
                               (float) anchorPoint.getX(), (float) anchorPoint.getY(), position.getAngle());
                    final Shape bounds = block.calculateBounds(g2, (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                                                               position.getLabelAnchor(), (float) anchorPoint.getX(),
                                                               (float) anchorPoint.getY(), position.getAngle());
                    if (plotState != null && plotState.getOwner() != null)
                    {
                        final EntityCollection entities = plotState.getOwner().getEntityCollection();
                        if (entities != null)
                        {
                            final String tooltip = getCategoryLabelToolTip(tick.getCategory());
                            entities.add(new CategoryLabelEntity(tick.getCategory(), bounds, tooltip, null));
                        }
                    }
                    r = bounds.getBounds2D();
                }

                categoryIndex++;
            }

            if (edge.equals(RectangleEdge.TOP))
            {
                final double h = state.getMax();
                state.cursorUp(h);
            }
            else if (edge.equals(RectangleEdge.BOTTOM))
            {
                final double h = state.getMax();
                state.cursorDown(h);
            }
            else if (edge == RectangleEdge.LEFT)
            {
                final double w = state.getMax();
                state.cursorLeft(w);
            }
            else if (edge == RectangleEdge.RIGHT)
            {
                final double w = state.getMax();
                state.cursorRight(w);
            }
        }
        return state;
    }
}
