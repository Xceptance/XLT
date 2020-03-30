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
package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.report.AbstractReportProvider;
import com.xceptance.xlt.api.report.ReportProvider;
import com.xceptance.xlt.api.report.ReportProviderConfiguration;
import com.xceptance.xlt.report.ReportGeneratorConfiguration;
import com.xceptance.xlt.report.util.JFreeChartUtils;
import com.xceptance.xlt.report.util.TaskManager;
import com.xceptance.xlt.report.util.ValueSet;

/**
 * An implementation of {@link ReportProvider} that is responsible to process all the {@link EventData} records and to
 * create the Events section in the test report XML.
 */
public class EventsReportProvider extends AbstractReportProvider
{
    /**
     * The events value set for all transactions.
     */
    private final ValueSet eventsPerSecondValueSet = new ValueSet();

    /**
     * A mapping from event types to {@link EventReport} instances.
     */
    private final Map<String, EventReport> eventReports = new HashMap<String, EventReport>();

    /**
     * A mapping from {@link EventReport} instances to the respective event messages/count.
     */
    private final Map<EventReport, Map<String, EventMessageInfo>> messageInfos = new HashMap<EventReport, Map<String, EventMessageInfo>>();

    /**
     * Whether or not to group events by test case.
     */
    private boolean groupEventsByTestCase;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(ReportProviderConfiguration config)
    {
        super.setConfiguration(config);

        // read provider-specific configuration
        groupEventsByTestCase = ((ReportGeneratorConfiguration) config).getGroupEventsByTestCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createReportFragment()
    {
        if (getConfiguration().shouldChartsGenerated())
        {
            // create charts asynchronously
            final TaskManager taskManager = TaskManager.getInstance();

            taskManager.addTask(new Runnable()
            {
                @Override
                public void run()
                {
                    final int minMaxValueSetSize = getConfiguration().getChartWidth();

                    // post-process the time series
                    final TimeSeries eventsPerSecondTimeSeries = JFreeChartUtils.toStandardTimeSeries(eventsPerSecondValueSet.toMinMaxValueSet(minMaxValueSetSize),
                                                                                                      "Events/s");

                    createEventChart(eventsPerSecondTimeSeries);
                }
            });
        }

        final EventsReport eventsReport = new EventsReport();

        // now add the collected message infos to the respective event report
        for (final EventReport eventReport : eventReports.values())
        {
            final Map<String, EventMessageInfo> messageInfosPerEvent = messageInfos.get(eventReport);
            for (final EventMessageInfo messageInfo : messageInfosPerEvent.values())
            {
                eventReport.messages.add(messageInfo);
            }
        }

        // finally fill in the events report
        eventsReport.events = new ArrayList<EventReport>(eventReports.values());

        return eventsReport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data stat)
    {
        if (stat instanceof EventData)
        {
            final EventData eventStat = (EventData) stat;

            // remember the event
            eventsPerSecondValueSet.addOrUpdateValue(eventStat.getTime(), 1);

            // store and count the event types
            final String eventKey = groupEventsByTestCase ? eventStat.getName() + "-" + eventStat.getTestCaseName() : eventStat.getName();

            EventReport eventReport = eventReports.get(eventKey);
            if (eventReport == null)
            {
                eventReport = new EventReport();
                eventReport.name = eventStat.getName();
                eventReport.testCaseName = groupEventsByTestCase ? eventStat.getTestCaseName() : "(ignored)";
                eventReport.totalCount = 1;

                eventReports.put(eventKey, eventReport);

                messageInfos.put(eventReport, new HashMap<String, EventMessageInfo>());
            }
            else
            {
                eventReport.totalCount++;
            }

            // store and count the event messages per event type
            final Map<String, EventMessageInfo> messageInfosPerEvent = messageInfos.get(eventReport);
            EventMessageInfo messageInfo = messageInfosPerEvent.get(eventStat.getMessage());
            if (messageInfo == null)
            {
                messageInfo = new EventMessageInfo();
                messageInfo.info = eventStat.getMessage();
                messageInfo.count = 1;

                messageInfosPerEvent.put(eventStat.getMessage(), messageInfo);
            }
            else
            {
                messageInfo.count++;
            }
        }
    }

    /**
     * Creates a chart where the passed events time series is drawn as bar plot. The chart is generated to the charts
     * directory.
     * 
     * @param eventsPerSecondTimeSeries
     *            the events
     */
    private void createEventChart(final TimeSeries eventsPerSecondTimeSeries)
    {
        // System.out.println("Creating events chart ... ");

        // final long start = TimerUtils.getTime();

        // convert the time series
        TimeSeriesCollection events = new TimeSeriesCollection(eventsPerSecondTimeSeries);

        // finally create and save the chart
        final ReportProviderConfiguration config = getConfiguration();

        final JFreeChart chart = JFreeChartUtils.createBarChart("Events", events, "Events", JFreeChartUtils.COLOR_EVENT,
                                                                config.getChartStartTime(), config.getChartEndTime());

        JFreeChartUtils.saveChart(chart, "Events", config.getChartDirectory(), config.getChartWidth(), config.getChartHeight());

        // System.out.printf("OK (%,d values, %,d ms)\n", runTimeTimeSeries.getItemCount(), TimerUtils.getTime() -
        // start);
    }
}
