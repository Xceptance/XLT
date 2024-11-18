/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.providers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.TaskManager;
import com.xceptance.xlt.report.util.ValueSet;

/**
 *
 */
public class ResponseCodesReportProvider extends AbstractReportProvider
{
    /**
     * The colors for each response code group (0xx, 1xx, 2xx, 3xx, 4xx, 5xx).
     */
    private static final Color[] COLORS =
        {
            new Color(0xEE5A2A),            // 0xx - red-orange
            new Color(0x3BACF0),            // 1xx - blue
            new Color(0x3BB44A),            // 2xx - green
            new Color(0xD0D0D0),            // 3xx - gray
            JFreeChartUtils.COLOR_EVENT,    // 4xx - orange
            JFreeChartUtils.COLOR_ERROR     // 5xx - red
        };

    /**
     * A mapping from response codes to their corresponding {@link ResponseCodeReport} objects.
     */
    private final Map<Integer, ResponseCodeReport> responseCodeReports = new HashMap<>();

    /**
     * A mapping from response codes to their corresponding {@link ValueSet} objects.
     */
    private final Map<Integer, ValueSet> responseCodeValueSets = new TreeMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (data instanceof RequestData)
        {
            final RequestData reqData = (RequestData) data;

            final int code = reqData.getResponseCode();

            ResponseCodeReport responseCodeReport = responseCodeReports.get(code);
            if (responseCodeReport == null)
            {
                responseCodeReport = new ResponseCodeReport();
                responseCodeReport.code = code;
                responseCodeReport.statusText = getStatusText(code);

                responseCodeReports.put(code, responseCodeReport);
            }

            responseCodeReport.count++;

            // track response code occurrences over time, but only in the HTTP response code range plus 0xx
            if (code >= 0 && code <= 599)
            {
                final ValueSet responseCodeValueSet = responseCodeValueSets.computeIfAbsent(code, (__) -> new ValueSet());
                responseCodeValueSet.addOrUpdateValue(reqData.getEndTime(), 1);
            }
        }
    }

    /**
     * Returns the corresponding status text for the given HTTP status code.
     *
     * @param statusCode
     *            the status code
     * @return the status text
     */
    private String getStatusText(final int statusCode)
    {
        String statusText;

        if (statusCode == 0)
        {
            // our status code for network-related problems
            statusText = "No Response Available";
        }
        else
        {
            try
            {
                statusText = EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, null);
            }
            catch (final IllegalArgumentException e)
            {
                // status code was other than 1xx...5xx
                statusText = null;
            }

            statusText = (statusText == null) ? "???" : statusText;
        }

        return statusText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final ResponseCodesReport report = new ResponseCodesReport();

        report.responseCodes = new ArrayList<>(responseCodeReports.values());

        // create the charts if needed
        if (getConfiguration().shouldChartsGenerated())
        {
            // create charts asynchronously
            final TaskManager taskManager = TaskManager.getInstance();

            taskManager.addTask(() -> saveResponseCodesChart("Response Codes Per Second", "ResponseCodesPerSecond", responseCodeValueSets));
        }

        return report;
    }

    /**
     * Creates a chart from the encountered response codes. The chart contains a separate plot for each response code
     * that shows how the occurrences of that code are distributed over time. The chart is generated to the charts
     * directory.
     */
    private void saveResponseCodesChart(final String chartTitle, final String chartFileName,
                                        final Map<Integer, ValueSet> responseCodeValueSets)
    {
        final JFreeChart chart = createResponseCodesChart(chartTitle, responseCodeValueSets);

        // size the response codes chart according to the number of response codes encountered
        final int height = responseCodeValueSets.size() * 75 + 100;

        JFreeChartUtils.saveChart(chart, chartFileName, getConfiguration().getChartDirectory(), getConfiguration().getChartWidth(), height);
    }

    /**
     * Creates a chart from the encountered response codes. The chart contains a separate plot for each response code
     * that shows how the occurrences of that code are distributed over time.
     */
    private JFreeChart createResponseCodesChart(final String chartTitle, final Map<Integer, ValueSet> responseCodeValueSets)
    {
        final Map<String, Color> responseCodeGroups = new TreeMap<>();

        // create the combined plot
        final CombinedDomainXYPlot combinedPlot = JFreeChartUtils.createCombinedPlot(getConfiguration().getChartStartTime(),
                                                                                     getConfiguration().getChartEndTime());

        // add a sub plot for each response code to the combined plot
        for (final Entry<Integer, ValueSet> entry : responseCodeValueSets.entrySet())
        {
            final int responseCode = entry.getKey();
            final ValueSet responseCodeValueSet = entry.getValue();

            // determine response code group and color
            final String responseCodeGroup = determineResponseCodeGroup(responseCode);
            final Color responseCodeColor = determineResponseCodeColor(responseCode);

            // remember group/color for creating the legend later on
            responseCodeGroups.put(responseCodeGroup, responseCodeColor);

            // create a standard bar sub plot
            final TimeSeries responseCodeTimeSeries = JFreeChartUtils.toStandardTimeSeries(responseCodeValueSet.toMinMaxValueSet(getConfiguration().getChartWidth()),
                                                                                           responseCodeGroup);
            final XYPlot responseCodePlot = JFreeChartUtils.createBarPlot(new TimeSeriesCollection(responseCodeTimeSeries), null, "",
                                                                          responseCodeColor);

            // extend the standard bar plot with a 2nd y-axis to the right that only serves to display the response
            // code, which this plot represents, prominently
            final NumberAxis fakeAxis = new NumberAxis(String.valueOf(responseCode));
            fakeAxis.setTickMarksVisible(false);
            fakeAxis.setTickLabelsVisible(false);
            fakeAxis.setLabelAngle(4.71); // 270°
            fakeAxis.setLabelInsets(new RectangleInsets(0.0, 8.0, 0.0, 0.0));

            final ValueAxis defaultAxis = responseCodePlot.getRangeAxis();

            responseCodePlot.setRangeAxes(new ValueAxis[]
                {
                    defaultAxis, fakeAxis
                });

            // add the sub plot to the combined plot
            combinedPlot.add(responseCodePlot, 1);
        }

        // overwrite the default legend (one legend item per series) with a custom legend (one legend item per group)
        final LegendItemCollection legendItems = new LegendItemCollection();
        for (final Entry<String, Color> responseCodeGroupEntry : responseCodeGroups.entrySet())
        {
            final String responseCodeGroup = responseCodeGroupEntry.getKey();
            final Color responseCodeColor = responseCodeGroupEntry.getValue();

            legendItems.add(new LegendItem(responseCodeGroup, responseCodeColor));
        }

        combinedPlot.setFixedLegendItems(legendItems);

        // finally create the chart
        final JFreeChart jfreechart = JFreeChartUtils.createChart(chartTitle, combinedPlot);

        return jfreechart;
    }

    /**
     * Derives the response code color from the given response code.
     */
    private static Color determineResponseCodeColor(final int responseCode)
    {
        final int group = responseCode / 100;

        return (group >= 0 && group <= 5) ? COLORS[group] : null;
    }

    /**
     * Derives the response code group (e.g. "5xx") from the given response code.
     */
    private static String determineResponseCodeGroup(final int responseCode)
    {
        final int group = responseCode / 100;

        return (group >= 0 && group <= 5) ? String.valueOf(group) + "xx" : "Other";
    }
}
