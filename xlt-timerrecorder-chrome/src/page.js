const ExternalCommunicationID = "8401bc88-ed75-4560-ae93-6854efbfbe62";
const ContentCommunicationID = "44cbe54d-c0d5-4712-b0b0-929ca3d72c83";
const BackgroundCommunicationID = "a35329d7-3f97-44c2-8aae-88213a474ffe";

const url = window.location.href;

chrome.runtime.onMessage.addListener(function(message, sender, sendResponse) {
  const data = getDataIfBackgroundMessage(message);
  if (data === "getTimingData") {
    sendResponse(createBackgroundMessage(getTimingData()));
  }
});

window.addEventListener("message", function(event) {
  if (!event || event.source !== window) {
    return;
  }

  const message = event.data;
  const data = getDataIfExternalMessage(message);
  if (!data) {
    return;
  }

  // currently not used
});

performance.addEventListener('resourcetimingbufferfull', function(event) {
  chrome.runtime.sendMessage(createBackgroundMessage("eventResourceTimingBufferFull", getTimingData(false)));
});

window.addEventListener("beforeunload", function(event) {
  chrome.runtime.sendMessage(createBackgroundMessage("eventBeforeUnload", getTimingData()));
});

window.addEventListener("load", function(event) {
  chrome.runtime.sendMessage(createBackgroundMessage("eventLoad"));
});

function getTimingData(includeEventTimings) {
  const data = {
    url: url,
    entries: createEntries()
  };
  if (includeEventTimings !== false) {
    data['timings'] = createTimingData();
    data['webVitals'] = getWebVitals();
  }
  return {
    timingData: data
  };
}

function createTimingData() {
  const timing = performance.timing;
  const navigationStart = timing.navigationStart;
  const paintTimings = getPaintTimings(navigationStart);

  return Object.assign({
    domComplete: {
      startTime: navigationStart,
      duration: timing.domComplete && navigationStart
        ? timing.domComplete - navigationStart
        : null
    },
    domContentLoadedEventEnd: {
      startTime: navigationStart,
      duration: timing.domContentLoadedEventEnd && navigationStart
        ? timing.domContentLoadedEventEnd - navigationStart
        : null
    },
    domContentLoadedEventStart: {
      startTime: navigationStart,
      duration: timing.domContentLoadedEventStart && navigationStart
        ? timing.domContentLoadedEventStart - navigationStart
        : null
    },
    domInteractive: {
      startTime: navigationStart,
      duration: timing.domInteractive && navigationStart
        ? timing.domInteractive - navigationStart
        : null
    },
    domLoading: {
      startTime: navigationStart,
      duration: timing.domLoading && navigationStart
        ? timing.domLoading - navigationStart
        : null
    },
    loadEventEnd: {
      startTime: navigationStart,
      duration: timing.loadEventEnd && navigationStart
        ? timing.loadEventEnd - navigationStart
        : null
    },
    loadEventStart: {
      startTime: navigationStart,
      duration: timing.loadEventStart && navigationStart
        ? timing.loadEventStart - navigationStart
        : null
    }
  }, paintTimings);
}

function createEntries() {
  const timing = performance.timing;
  const entries = performance.getEntriesByType("navigation").concat(performance.getEntriesByType("resource"));
  performance.clearResourceTimings();

  const data = {};

  entries.forEach(function(eachEntry) {

    const startTime = getRequestStartTime(eachEntry);
    const resourceEntry = {};
    resourceEntry.url = eachEntry.name;
    resourceEntry.transferSize = eachEntry.transferSize;
    resourceEntry.startTime = timing.navigationStart + startTime;
    resourceEntry.connectTime = eachEntry.connectStart && eachEntry.connectEnd
      ? eachEntry.connectEnd - eachEntry.connectStart
      : null;
    resourceEntry.sendTime = null;
    resourceEntry.busyTime = eachEntry.requestStart && eachEntry.responseStart
      ? eachEntry.responseStart - eachEntry.requestStart
      : null;
    resourceEntry.receiveTime = eachEntry.responseStart && eachEntry.responseEnd
      ? eachEntry.responseEnd - eachEntry.responseStart
      : null;
    resourceEntry.firstBytesTime = resourceEntry.connectTime + resourceEntry.sendTime + resourceEntry.busyTime;
    resourceEntry.lastBytesTime = resourceEntry.firstBytesTime + resourceEntry.receiveTime;
    resourceEntry.duration = startTime && eachEntry.responseEnd
      ? eachEntry.responseEnd - startTime
      : null;
    resourceEntry.dnsTime = eachEntry.domainLookupStart && eachEntry.domainLookupEnd
      ? eachEntry.domainLookupEnd - eachEntry.domainLookupStart
      : null;

    data[eachEntry.name] = (data[eachEntry.name] || []).concat(resourceEntry);
  });

  return data;
}

function getRequestStartTime(timingEntry) {
  return (timingEntry.domainLookupStart && timingEntry.domainLookupEnd - timingEntry.domainLookupStart) > 0
    ? timingEntry.domainLookupStart
    : (timingEntry.connectStart && timingEntry.connectEnd - timingEntry.connectStart) > 0
      ? timingEntry.connectStart
      : (timingEntry.requestStart && timingEntry.responseStart - timingEntry.requestStart) > 0
        ? timingEntry.requestStart
        : (timingEntry.responseStart && timingEntry.responseEnd - timingEntry.responseStart) > 0
          ? timingEntry.responseStart
          : timingEntry.fetchStart;
}

function createBackgroundMessage(data, value) {
  return {
    communicationID: ContentCommunicationID,
    data: data,
    value: value
  };
}

function getDataIfExternalMessage(message) {
  if (!message || message.communicationID !== ExternalCommunicationID) {
    return;
  }

  return message.data;
}

function getDataIfBackgroundMessage(message) {
  if (!message || message.communicationID !== BackgroundCommunicationID) {
    return;
  }

  return message.data;
}

function getPaintTimings(navigationStart) {
  const timings = {};
  if (window.PerformancePaintTiming) {
    performance.getEntriesByType('paint').forEach(function (entry) {
      // fix name (replace any hyphen followed by a lower-case character with its upper-case counterpart)
      const name = entry.name.replace(/-[a-z]/g, function (match) {
        return match.substring(1).toUpperCase();
      });
      // limit to known paint events
      if (name === 'firstPaint' || name === 'firstContentfulPaint') {
        timings[name] = {
          startTime: navigationStart,
          duration: Math.round(entry.startTime)
        };
      }
    });
  } else {
    const loadTimes = chrome.loadTimes();
    timings['firstPaint'] = {
      startTime: navigationStart,
      duration: loadTimes && loadTimes.firstPaintTime
        ? Math.round(loadTimes.firstPaintTime * 1000 - navigationStart)
        : null
    };
  }

  return timings;
}

// --- Web Vitals --------------------------------------------------------------

/**
 * Store for Web Vital metrics recorded during the lifespan of a page.
 * The entries are not the original metrics, but simplified ones with only the data we need.
 */
const webVitalsMetrics = [];

/**
 * Returns the entries in the metrics store. For now, no further processing needed.
 */
function getWebVitals() {
  return webVitalsMetrics;
}

/**
 * Adds a metric value to the metrics store.
 */
function addMetric(metric) {
  webVitalsMetrics.push({ time: Date.now(), name: metric.name, value: metric.value });
}

/**
 * Rounds a metric value and adds it to the metrics store.
 * Typically used for metrics representing millisecond measurements as we don't need the fractional part here.
 */
function addMetricRounded(metric) {
  webVitalsMetrics.push({ time: Date.now(), name: metric.name, value: Math.round(metric.value) });
}

// attach handlers for all known metric events, some may fire multiple times
webVitals.onCLS(addMetric, { reportAllChanges: true });
webVitals.onFCP(addMetricRounded);
webVitals.onFID(addMetricRounded);
webVitals.onINP(addMetricRounded, { reportAllChanges: true });
webVitals.onLCP(addMetricRounded, { reportAllChanges: true });
webVitals.onTTFB(addMetricRounded);
