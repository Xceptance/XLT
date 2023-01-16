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
package com.xceptance.xlt.report.providers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.xceptance.common.collection.FastHashMap;
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
     * Map from test case to event name to event report data
     */
    private final FastHashMap<String, Map<String, EventReport>> testCaseToEventMap = new FastHashMap<>(23, 0.5f);

    /**
     * Whether or not to group events by test case.
     */
    private boolean groupEventsByTestCase;

    /**
     * Count the events dropped. if the event concept was not abused, this should not happen aka event names are
     * not containing any data
     */
    private int eventsDropped;

    /**
     * Limit per event per test case for message infos
     */
    private int messageLimit = 250;

    /**
     * Limit per test case per event
     */
    private int eventLimitPerTestCase = 100;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(ReportProviderConfiguration config)
    {
        super.setConfiguration(config);

        // read provider-specific configuration
        this.groupEventsByTestCase = ((ReportGeneratorConfiguration) config).getGroupEventsByTestCase();
        this.messageLimit = ((ReportGeneratorConfiguration) config).getEventMessageLimitPerEvent();
        this.eventLimitPerTestCase = ((ReportGeneratorConfiguration) config).getEventLimitPerTestCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processDataRecord(final Data data)
    {
        if (!(data instanceof EventData))
        {
            // leave early, we don't want that data
            return;
        }

        final EventData eventData = (EventData) data;

        // remember the event on the timeline
        eventsPerSecondValueSet.addOrUpdateValue(eventData.getTime(), 1);

        // in case we don't want to count anything in detail
        if (eventLimitPerTestCase <= 0)
        {
            // count that we dropped it
            eventsDropped++;

            return;
        }

        // find the info by test case name
        Map<String, EventReport> stat = this.testCaseToEventMap.get(eventData.getTestCaseName());

        // unknown?
        if (stat == null)
        {
            // store test case -> event name -> counter and info
            final EventReport eventReport = new EventReport(eventData.getTestCaseName(), eventData.getName());
            eventReport.addMessage(eventData.getMessage(), messageLimit);

            stat = new HashMap<>();
            stat.put(eventData.getName(), eventReport);

            this.testCaseToEventMap.put(eventData.getTestCaseName(), stat);
        }
        else
        {
            // get the event by name from the test case data
            EventReport eventReport = stat.get(eventData.getName());
            if (eventReport == null)
            {
                // it is unknown, do we want to add it aka do we have room?
                if (stat.size() >= eventLimitPerTestCase)
                {
                    // ok, nothing new to add, just record that we dropped something
                    // we don't count dropped events by test case (yet)
                    eventsDropped++;
                }
                else
                {
                    // new event name
                    eventReport = new EventReport(eventData.getTestCaseName(), eventData.getName());
                    eventReport.addMessage(eventData.getMessage(), messageLimit);

                    // store
                    stat.put(eventData.getName(), eventReport);
                }
            }
            else
            {
                // record data, let the method decide based on the limit if this is stored or just counted as dropped
                eventReport.addMessage(eventData.getMessage(), messageLimit);
            }
        }

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

        // for later when we reduce the count of events in case we don't want grouping
        int moreDroppedMessages = 0;

        // ok, if we want to group by test case, we have not much to do
        final List<EventReport> eventReports = new ArrayList<>();
        if (this.groupEventsByTestCase)
        {
            this.testCaseToEventMap.values().forEach(e -> eventReports.addAll(e.values() /* the values of the map */));
        }
        else
        {
            // ok, we don't want the default grouping, so we have to resolve that now
            final Map<String, EventReport> events = new HashMap<>(101);

            this.testCaseToEventMap.values().forEach(e -> {
                // we don't care about the name of the test case, so this is dropped

                // we just sum up the values
                for (var entry : e.entrySet())
                {
                    final String eventName = entry.getKey();
                    final EventReport stats = entry.getValue();
                    stats.setTestCaseName("(ignored)");

                    // add data to the existing data or use the stats as new entry
                    events.compute(eventName, (n, r) -> (r == null) ? stats : r.merge(stats));
                }
            });

            // ok, we also want to limit the count
            var comperator = new Comparator<EventReport>()
            {
                @Override
                public int compare(EventReport o1, EventReport o2)
                {
                    var t = o2.totalCount - o1.totalCount;
                    return t != 0 ? t : o2.droppedCount - o1.droppedCount;
                }

            };

            // sort desc
            var sortedEvents = events.values().stream().sorted(comperator).collect(Collectors.toList());

            // add remainder and count dropped for the totals
            for (int i = 0; i < sortedEvents.size(); i++)
            {
                final EventReport e = sortedEvents.get(i);
                if (i < this.eventLimitPerTestCase)
                {
                    eventReports.add(e);
                }
                else
                {
                    eventsDropped += e.totalCount;
                    moreDroppedMessages += e.droppedCount;
                }
            }
        }

        final EventsReport eventsReport = new EventsReport();

        // now add the collected message infos to the respective event report
        eventReports.forEach(EventReport::prepareSerialization);

        // in case we have eventsDropped > 0, we insert a virtual event
        if (eventsDropped > 0)
        {
            final EventReport dropped = new EventReport("XLT Internal", ">> XLT::EventsDropped - Reached event limit <<");
            dropped.totalCount = eventsDropped;
            dropped.droppedCount = moreDroppedMessages;
            eventReports.add(dropped);
        }

        // finally fill in the events report
        eventsReport.events = eventReports;

        return eventsReport;
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
