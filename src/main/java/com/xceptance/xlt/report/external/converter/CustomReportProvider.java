/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.external.converter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.jfree.data.time.TimeSeries;

import com.xceptance.xlt.api.util.XltLogger;
import com.xceptance.xlt.report.external.config.ChartConfig;
import com.xceptance.xlt.report.external.config.SeriesConfig;
import com.xceptance.xlt.report.external.config.TableConfig;
import com.xceptance.xlt.report.external.config.TableConfig.TableType;
import com.xceptance.xlt.report.external.config.ValueConfig;
import com.xceptance.xlt.report.external.reportObject.GenericReport;
import com.xceptance.xlt.report.external.reportObject.Row;
import com.xceptance.xlt.report.external.reportObject.Table;
import com.xceptance.xlt.report.external.util.StatsValueSet;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.TaskManager;
import com.xceptance.xlt.report.util.TimeSeriesConfiguration;
import com.xceptance.xlt.report.util.TimeSeriesConfiguration.Style;

/**
 * Parse values and convert to report. Meta data are taken from configuration file.
 *
 * @author Matthias Ullrich (Xceptance Software Technologies GmbH)
 */
public class CustomReportProvider extends AbstractDataConverter
{
    protected static final Color DEFAULT_COLOR_BLUE = Color.BLUE;

    protected static final Color DEFAULT_COLOR_GRAY = Color.DARK_GRAY;

    protected static final String NA = "n/a";

    protected static final String SHARP = "#";

    /**
     * The value sets used to populate min-max-avg tables.
     */
    protected Map<String, StatsValueSet> vSets = new HashMap<String, StatsValueSet>();

    /**
     * The list of value sets used to populate plain data tables.
     */
    private List<Map<String, Object>> plainValueSets = new ArrayList<Map<String, Object>>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        final GenericReport report = new GenericReport();

        report.headline = getConfiguration().getDataFile().getHeadline();
        report.description = getConfiguration().getDataFile().getDescription();

        // process table(s)
        final List<TableConfig> tableConfigs = getConfiguration().getDataFile().getTables();
        for (final TableConfig tableConfig : tableConfigs)
        {
            // get table configuration properties
            final TableType tableType = tableConfig.getType();
            final String tableTitle = tableConfig.getTitle();

            boolean transposeTable = false;

            List<ValueConfig> valueConfigs = tableConfig.getColumns();
            if (valueConfigs.isEmpty())
            {
                // no column configurations found, check for row configurations instead
                valueConfigs = tableConfig.getRows();

                // By default, tables are created with a column per value.
                // If rows were configured, we have to transpose the table.
                transposeTable = true;
            }

            // create the table
            Table table = null;

            if (tableType.equals(TableType.minmaxavg))
            {
                table = createMinMaxAvgTable(tableTitle, valueConfigs, vSets);
            }
            else if (tableType.equals(TableType.plain))
            {
                table = createPlainTable(tableTitle, valueConfigs, plainValueSets);
            }
            else
            {
                String message = String.format("Table '%s' specifies the unknown table type '%s'. Only '%s' and '%s' are supported types.",
                                               tableConfig.getTitle(), tableType, TableType.minmaxavg, TableType.plain);
                XltLogger.reportLogger.error(message);
            }

            // transpose and publish the table if needed
            if (table != null)
            {
                if (transposeTable)
                {
                    table = transposeTable(table);
                }

                report.tables.add(table);
            }
        }

        // process chart(s)
        if (getConfiguration().shouldChartsGenerated())
        {
            final List<ChartConfig> chartConfigs = getConfiguration().getDataFile().getCharts();
            final TaskManager manager = TaskManager.getInstance();

            for (final ChartConfig chartConfig : chartConfigs)
            {
                final String fileName = chartConfig.getTitle();
                report.chartFileNames.add(fileName);

                manager.addTask(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        createChart(chartConfig, fileName);
                    }
                });
            }
        }

        return report;
    }

    /**
     * Creates a min-max-avg table from the passed value sets. The value sets contain the min/max/mean values, which
     * will populate the respective columns in the table:
     *
     * <pre>
     *             | Value Name 1 | Value Name 2 | ... | Value Name n
     * ---------------------------------------------------------------
     *  Mean       |    Mean 1    |    Mean 2    | ... |    Mean n
     *  Minimum    |     Min 1    |     Min 2    | ... |     Min n
     *  Maximum    |     Max 1    |     Max 2    | ... |     Max n
     * </pre>
     *
     * @param tableTitle
     *            the table title
     * @param valueConfigs
     *            the value configurations specifying the values of interest
     * @param valueSets
     *            the value sets keyed by value name
     * @return the table
     */
    private Table createMinMaxAvgTable(String tableTitle, List<ValueConfig> valueConfigs, Map<String, StatsValueSet> valueSets)
    {
        Table table = new Table();
        table.setTitle(tableTitle);

        // prepare the table rows, always four
        Row headRow = new Row();
        Row meanRow = new Row();
        Row minRow = new Row();
        Row maxRow = new Row();

        table.setHeadRow(headRow);
        table.addRow(meanRow);
        table.addRow(minRow);
        table.addRow(maxRow);

        // fill the table rows column by column
        headRow.addCell("Value Name");
        meanRow.addCell("Mean");
        minRow.addCell("Minimum");
        maxRow.addCell("Maximum");

        for (final ValueConfig valueConfig : valueConfigs)
        {
            // set the value name (including the unit if available)
            String title = valueConfig.getTitle();
            if (StringUtils.isNotBlank(valueConfig.getUnit()))
            {
                title = title + " [" + valueConfig.getUnit() + "]";
            }

            headRow.addCell(title);

            // set the mean/min/max values (if available)
            final StatsValueSet vSet = valueSets.get(valueConfig.getValueName());
            if (vSet != null)
            {
                meanRow.addCell(vSet.getAvg());
                minRow.addCell(vSet.getMin());
                maxRow.addCell(vSet.getMax());
            }
            else
            {
                meanRow.addCell(NA);
                minRow.addCell(NA);
                maxRow.addCell(NA);
            }
        }

        table.finish();

        return table;
    }

    /**
     * Creates a plain data table from the passed list of value sets. Each value set in the list will be transformed
     * into exactly one table row as outlined below:
     *
     * <pre>
     *  Value Name 1 | Value Name 2 | ... | Value Name n
     * --------------------------------------------------
     *    Value 1.1  |   Value 2.1  | ... |   Value n.1
     *    Value 1.2  |   Value 2.2  | ... |   Value n.2
     *       ...     |      ...     | ... |      ...
     *    Value 1.m  |   Value 2.m  | ... |   Value n.m
     * </pre>
     *
     * @param tableTitle
     *            the table title
     * @param valueConfigs
     *            the value configurations specifying the values of interest
     * @param valueSets
     *            the list of value sets
     * @return the table
     */
    private Table createPlainTable(String tableTitle, List<ValueConfig> valueConfigs, List<Map<String, Object>> valueSets)
    {
        Table table = new Table();
        table.setTitle(tableTitle);

        // create a table header with the value names/titles
        {
            final Row row = new Row();

            for (final ValueConfig valueConfig : valueConfigs)
            {
                String title = valueConfig.getTitle();
                if (StringUtils.isNotBlank(valueConfig.getUnit()))
                {
                    title = title + " [" + valueConfig.getUnit() + "]";
                }

                row.addCell(title);
            }

            table.setHeadRow(row);
        }

        // create body rows and fill them with the corresponding values row by row
        {
            final List<Row> rows = new ArrayList<>();

            // if valueSets is empty insert a dummy value set with N/A values
            if (valueSets.isEmpty())
            {
                final Map<String, Object> dummyValueSet = new HashMap<>();
                valueSets.add(dummyValueSet);

                for (final ValueConfig valueConfig : valueConfigs)
                {
                    dummyValueSet.put(valueConfig.getValueName(), NA);
                }
            }

            // now render the value sets
            for (Map<String, Object> valueSet : valueSets)
            {
                final Row row = new Row();

                for (final ValueConfig valueConfig : valueConfigs)
                {
                    final Object value = valueSet.get(valueConfig.getValueName());
                    row.addCell(value);
                }

                rows.add(row);
            }

            table.setBodyRows(rows);
        }

        table.finish();

        return table;
    }

    /**
     * Transposes the given table, meaning that rows and columns will be inverted.
     *
     * @param table
     *            the table
     * @return the transposed table
     */
    private Table transposeTable(Table table)
    {
        Table newTable = new Table();
        newTable.setTitle(table.getTitle());

        Row headRow = table.getHeadRow();
        List<Row> bodyRows = table.getBodyRows();

        for (int c = 0; c < table.getMaxCols(); c++)
        {
            Row newRow = new Row();

            newRow.addCell(headRow.getCells().get(c));
            for (int r = 0; r < bodyRows.size(); r++)
            {
                newRow.addCell(bodyRows.get(r).getCells().get(c));
            }

            if (c == 0)
            {
                newTable.setHeadRow(newRow);
            }
            else
            {
                newTable.addRow(newRow);
            }
        }

        newTable.finish();

        return newTable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parse(final long time, final Map<String, Object> input) throws IllegalArgumentException
    {
        if (input == null)
        {
            throw new IllegalArgumentException("Input must not be NULL");
        }

        // check whether we have to process the value set
        if (time == -1)
        {
            // no, we will output it later as is
            plainValueSets.add(input);
        }
        else
        {
            // yes, update the statistics

            // is the current data set in wanted time range
            if (getConfiguration().getChartStartTime() <= time && time <= getConfiguration().getChartEndTime())
            {
                for (final Entry<String, Object> entry : input.entrySet())
                {
                    String name = entry.getKey();
                    Double value = (Double) entry.getValue();

                    // update data set
                    StatsValueSet vSet = vSets.get(name);
                    if (vSet == null)
                    {
                        vSet = new StatsValueSet();
                        vSets.put(name, vSet);
                    }
                    vSet.addOrUpdate(time, value);
                }
            }
        }
    }

    protected Color getColor(String color)
    {
        Color resultColor = null;

        if (color != null)
        {
            color = color.trim();
            // remove leading sharp (usually used to mark a hex string)
            if (color.startsWith(SHARP))
            {
                color = color.substring(1);
            }

            try
            {
                final int colorValue = Integer.valueOf(color, 16).intValue();
                resultColor = new Color(colorValue);
            }
            catch (final Exception e)
            {
                if (XltLogger.reportLogger.isInfoEnabled())
                {
                    XltLogger.reportLogger.info("Color '" + color + "' is not valid.");
                }
            }
        }

        return resultColor;
    }

    private void createChart(final ChartConfig config, final String fileName)
    {
        // initialize time series collections
        final List<List<TimeSeriesConfiguration>> axisCollections = new ArrayList<List<TimeSeriesConfiguration>>(2);
        axisCollections.add(new ArrayList<TimeSeriesConfiguration>());
        axisCollections.add(new ArrayList<TimeSeriesConfiguration>());
        for (final SeriesConfig seriesConfig : config.getSeriesCollection())
        {
            final String valueName = seriesConfig.getValueName();
            final StatsValueSet vSet = vSets.get(valueName);
            if (vSet != null)
            {
                final String seriesTitle = seriesConfig.getTitle() != null ? seriesConfig.getTitle() : valueName;
                final TimeSeries timeSeries = vSet.toTimeSeries(seriesTitle);
                final Color color = getColor(seriesConfig.getColor());
                final TimeSeriesConfiguration tsConfig = new TimeSeriesConfiguration(timeSeries, color, Style.LINE);

                if (seriesConfig.getAverage() != null)
                {
                    try
                    {
                        final int percentage = Integer.parseInt(seriesConfig.getAverage());
                        final TimeSeries avgTimeSeries = JFreeChartUtils.createMovingAverageTimeSeries(timeSeries, percentage);
                        final Color avgColor = getColor(seriesConfig.getAverageColor());
                        final TimeSeriesConfiguration avgTsConfig = new TimeSeriesConfiguration(avgTimeSeries, avgColor, Style.LINE);
                        axisCollections.get(seriesConfig.getAxis() - 1).add(avgTsConfig);
                    }
                    catch (final NumberFormatException e)
                    {
                        XltLogger.reportLogger.warn("Skip moving average for series " + valueName + ". Can not parse average '" +
                                                     seriesConfig.getAverage() + "' to integer value");
                    }
                }

                axisCollections.get(seriesConfig.getAxis() - 1).add(tsConfig);
            }
            else
            {
                if (XltLogger.reportLogger.isInfoEnabled())
                {
                    XltLogger.reportLogger.info("no data for series " + valueName);
                }
            }
        }

        createFloatingChart(axisCollections, config, fileName);

        System.out.printf("Creating chart '%s' ...\n", fileName);
    }
}
