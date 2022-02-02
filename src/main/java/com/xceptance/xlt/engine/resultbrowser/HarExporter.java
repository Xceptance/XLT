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
package com.xceptance.xlt.engine.resultbrowser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.util.ProductInformation;
import com.xceptance.xlt.engine.har.model.HarContent;
import com.xceptance.xlt.engine.har.model.HarCreator;
import com.xceptance.xlt.engine.har.model.HarEntry;
import com.xceptance.xlt.engine.har.model.HarHeader;
import com.xceptance.xlt.engine.har.model.HarLog;
import com.xceptance.xlt.engine.har.model.HarPage;
import com.xceptance.xlt.engine.har.model.HarPageTimings;
import com.xceptance.xlt.engine.har.model.HarParam;
import com.xceptance.xlt.engine.har.model.HarPostData;
import com.xceptance.xlt.engine.har.model.HarRequest;
import com.xceptance.xlt.engine.har.model.HarResponse;
import com.xceptance.xlt.engine.har.model.HarTimings;
import com.xceptance.xlt.engine.resultbrowser.ActionInfo.PageLoadEventInfo;

class HarExporter
{
    private final TransactionInfo transaction;

    HarExporter(final TransactionInfo aTransaction)
    {
        transaction = aTransaction;
    }

    HarLog exportToHAR()
    {
        final List<HarPage> pages = new ArrayList<>();
        final List<HarEntry> entries = new ArrayList<>();

        for (int pageNo = 0; pageNo < transaction.actions.size(); pageNo++)
        {
            final String pageId = "page_" + pageNo;
            final ActionInfo axn = transaction.actions.get(pageNo);

            final HarPage.Builder pageBuilder = new HarPage.Builder();
            pageBuilder.withTitle(axn.name);
            pageBuilder.withId(pageId);

            long startTime = -1L;

            {
                PageLoadEventInfo contentLoadEnd = null;
                PageLoadEventInfo contentLoadStart = null;
                PageLoadEventInfo domInteractive = null;
                PageLoadEventInfo loadEnd = null;

                PageLoadEventInfo firstPaint = null;
                PageLoadEventInfo firstContentfulPaint = null;

                for (final PageLoadEventInfo e : axn.events)
                {
                    String name = e.name;
                    if ("DomContentLoadedEventEnd".equals(name))
                    {
                        contentLoadEnd = e;
                    }
                    else if ("DomContentLoadEventStart".equals(name))
                    {
                        contentLoadStart = e;
                    }
                    else if ("DomInteractive".equals(name))
                    {
                        domInteractive = e;
                    }
                    else if ("LoadEventEnd".equals(name))
                    {
                        loadEnd = e;
                    }
                    else if ("FirstPaint".equals(name))
                    {
                        firstPaint = e;
                    }
                    else if ("FirstContentfulPaint".equals(name))
                    {
                        firstContentfulPaint = e;
                    }

                    if (e.startTime > 0L && (startTime < 0L || e.startTime < startTime))
                    {
                        startTime = e.startTime;
                    }
                }

                final HarPageTimings.Builder timingsBuilder = new HarPageTimings.Builder();
                PageLoadEventInfo contentLoaded = contentLoadEnd;
                if (contentLoaded == null)
                {
                    contentLoaded = contentLoadStart;
                }
                if (contentLoaded == null)
                {
                    contentLoaded = domInteractive;
                }

                timingsBuilder.withOnContentLoad(contentLoaded != null && contentLoaded.startTime > 0L ? contentLoaded.duration : -1L);
                timingsBuilder.withOnLoad(loadEnd != null && loadEnd.startTime > 0L ? loadEnd.duration : -1L);

                timingsBuilder.withFirstContentfulPaint(firstContentfulPaint != null &&
                                                        firstContentfulPaint.startTime > 0L ? firstContentfulPaint.duration : null);
                timingsBuilder.withFirstPaint(firstPaint != null && firstPaint.startTime > 0L ? firstPaint.duration : null);
                pageBuilder.withPageTimings(timingsBuilder.build());
            }

            for (final RequestInfo req : axn.requests)
            {
                if (startTime < 0L || req.startTime < startTime)
                {
                    startTime = req.startTime;
                }

                final HarEntry.Builder entryBuilder = new HarEntry.Builder();
                entryBuilder.withPageref(pageId);
                entryBuilder.withTime(req.loadTime);
                entryBuilder.withStartedDateTime(new Date(req.startTime));
                entryBuilder.withTimings(requestTimings(req.timings));
                entryBuilder.withRequest(request(req));
                entryBuilder.withResponse(response(req));

                entries.add(entryBuilder.build());
            }

            if (startTime > -1L)
            {
                pageBuilder.withStartedDateTime(new Date(startTime));
                pages.add(pageBuilder.build());
            }

        }

        return new HarLog("1.2", creator(), null, pages, entries, null);
    }

    private HarCreator creator()
    {
        final ProductInformation productInfo = ProductInformation.getProductInformation();
        return new HarCreator(productInfo.getProductName(), null, productInfo.getVersion());
    }

    private HarTimings requestTimings(final RequestInfo.TimingInfo timings)
    {
        final HarTimings.Builder builder = new HarTimings.Builder();
        if (timings != null)
        {
            builder.withBlocked(-1L);
            builder.withReceive((long) timings.receiveTime);
            builder.withSend((long) timings.sendTime);
            builder.withWait((long) timings.serverBusyTime);
            builder.withConnect((long) timings.connectTime);
            builder.withDns((long) timings.dnsTime);
        }

        return builder.build();
    }

    private HarRequest request(final RequestInfo request)
    {
        final HarRequest.Builder builder = new HarRequest.Builder();

        final List<HarHeader> header = request.requestHeaders.stream().map(h -> new HarHeader(h.getName(), h.getValue(), null))
                                                             .collect(Collectors.toList());

        final String url = StringUtils.substringBefore(request.url, "#");
        final String query = StringUtils.substringAfter(url, "?");
        final boolean hasRawBody = StringUtils.isNotEmpty(request.requestBodyRaw);
        final String method = StringUtils.defaultString(request.requestMethod).toUpperCase();

        builder.withHeaders(header);
        builder.withMethod(method);
        builder.withUrl(url);
        builder.withHttpVersion("HTTP/1.1");
        if (request.timings != null)
        {
            builder.withBodySize(request.timings.bytesSent);
        }

        if (hasRawBody || !request.requestParameters.isEmpty())
        {
            final HarPostData.Builder postDataBuilder = new HarPostData.Builder();
            if (hasRawBody)
            {
                postDataBuilder.withMimeType("text/plain");
                postDataBuilder.withText(request.requestBodyRaw);
            }
            else
            {
                postDataBuilder.withMimeType(request.formDataEncoding);
                postDataBuilder.withParams(request.requestParameters.stream()
                                                                    .map(p -> new HarParam(p.getName(), p.getValue(), null, null, null))
                                                                    .collect(Collectors.toList()));
            }

            builder.withPostData(postDataBuilder.build());
        }
        try
        {
            builder.withQueryString(query);
        }
        catch (final Exception e)
        {
        }

        return builder.build();
    }

    private HarResponse response(final RequestInfo request)
    {
        final HarResponse.Builder builder = new HarResponse.Builder();

        builder.withHeaders(request.responseHeaders.stream().map(h -> new HarHeader(h.getName(), h.getValue(), null))
                                                   .collect(Collectors.toList()));

        builder.withHttpVersion("HTTP/1.1");
        builder.withStatus(request.responseCode);
        builder.withStatusText(StringUtils.substringAfter(request.status, "-").trim());
        if (request.timings != null)
        {
            builder.withBodySize(request.timings.bytesReceived);
        }

        String redirectUrl = null;
        for (final com.gargoylesoftware.htmlunit.util.NameValuePair header : request.responseHeaders)
        {
            final String headerName = header.getName();
            if ("Location".equals(headerName))
            {
                redirectUrl = header.getValue();
                break;
            }
        }

        builder.withRedirectURL(StringUtils.defaultString(redirectUrl));
        builder.withContent(new HarContent.Builder().withMimeType(request.mimeType).build());

        return builder.build();
    }
}
