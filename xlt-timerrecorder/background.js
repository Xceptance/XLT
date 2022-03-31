const ContentCommunicationID = "44cbe54d-c0d5-4712-b0b0-929ca3d72c83";
const BackgroundCommunicationID = "a35329d7-3f97-44c2-8aae-88213a474ffe";
const ExternalCommunicationID = "8401bc88-ed75-4560-ae93-6854efbfbe62";

const CRLF = "\r\n";

const TimingData = {};
const TabRequestsMap = {};
const reResponseStatus = /HTTP\/\d(?:\.\d)?\s+\d{3}\s+(.*)/;

var webSocket = null;
const configuration = {
  recordIncompleted: false,
  connectParams: null
};

function isWebSocketClosed() {
  return webSocket === null || webSocket.readyState === WebSocket.CLOSED || webSocket.readyState === WebSocket.CLOSING;
}

function send(data, messageID) {
  const messageString = JSON.stringify({
    messageID: String(messageID),
    data: data
  });

  if (webSocket !== null && webSocket.readyState === WebSocket.OPEN) {
    try {
      webSocket.send(messageString);
      return;
    } catch (e) {
      console.log(e);
    }
  }

  startWebSocketConnect(function(connection) {
    try {
      connection.send(messageString);
    } catch (e) {
      console.log(e);
    }
  });
}

function startWebSocketConnect(onOpenHandler) {
  stopWebSocket();

  const connectParams = configuration.connectParams;
  if(!connectParams) {
    console.log("Could not open websocket due to missing configuration");
    return;
  }

  webSocket = new WebSocket("ws://127.0.0.1:" + connectParams.port + "/xlt/" + connectParams.clientID);
  webSocket.onopen = function(openEvent) {
    if (!!onOpenHandler) {
      onOpenHandler(webSocket);
    }
  };
  webSocket.onmessage = function(message) {
    const messageObject = JSON.parse(message.data);
    if (messageObject.data.action === "GET_DATA") {
      prepareStoreData(messageObject.data.storageTimeout, function(performanceData) {
        clearRuntimeData();
        send({action: "GET_DATA", data: JSON.stringify(filterRecordEntries(performanceData))}, messageObject.messageID);
      });
    }
  };
  webSocket.onclose = function(closeEvent) {
    setTimeout(function() {
      if (isWebSocketClosed()) {
        startWebSocketConnect();
      };
    }, 1000);
  };
  webSocket.onerror = function(event) {
    console.log(event);
  };
}

function stopWebSocket() {
  if (!isWebSocketClosed()) {
    webSocket.onclose = null;
    webSocket.close();
  }
  webSocket = null;
}

function isWebRequestOfInterest(details) {
  return !(details.tabId === -1 || isXltParametersURL(details.url) || /^fakeRequest-\d+$/.test(details.requestId));
}

function isXltParametersURL(url) {
  return !!url && url.startsWith("data:,xltParameters");
}

function connectByXltParametersURL(url) {
  const parameters = getUrlParameters(url);

  setConfiguration(parameters);

  if (parameters.xltPort && parameters.clientID) {
    startWebSocketConnect();
  } else {
    console.log("incomplete xlt start url:" + url);
  }
}

function getUrlParameters(url) {
  const parameters = {};

  let search = new URL(url).search.trim();
  if (search.startsWith("?")) {
    search = search.substring(1);
  }

  if (search.length > 0) {
    search.split("&").forEach(function(each) {
      var split = each.split("=");
      var parameter = split[0];
      if (parameter.length > 0) {
        parameters[parameter] = split[1];
      }
    });
  }
  return parameters;
}

function setConfiguration(parameters) {
  configuration.recordIncompleted = String(parameters.recordIncompleted).toLowerCase() === "true";
  configuration.connectParams = {
    port: parameters.xltPort,
    clientID: parameters.clientID
  };
}

function clearRuntimeData() {
  for (const tabId in TimingData) {
    delete TimingData[tabId];
  }
  for (const tabId2 in TabRequestsMap) {
    delete TabRequestsMap[tabId2];
  }
}

function hasTimingDataEntry(tabId) {
  return !!TimingData[tabId];
}

function getCurrentTimingDataEntry(tabId) {
  if (!TimingData[tabId]) {
    return newTimingDataEntry(tabId);
  }
  return TimingData[tabId][TimingData[tabId].length - 1];
}

function newTimingDataEntry(tabId) {
  if (!TimingData[tabId]) {
    TimingData[tabId] = [];
  }
  const dataEntry = {
    timings: null,
    entries: null,
    requests: [],
    loaded: false,
    beforeUnload: false
  };
  TimingData[tabId].push(dataEntry);
  return dataEntry;
}

function removeTimingDataEntry(tabId) {
  const entry = TimingData[tabId];
  if (entry) {
    delete TimingData[tabId];
  }
  return entry;
}

function getRequestEntry(tabId, requestId, url) {
  if (!TabRequestsMap[tabId]) {
    TabRequestsMap[tabId] = {};
  }
  if (!TabRequestsMap[tabId][requestId]) {
    TabRequestsMap[tabId][requestId] = {};
  }
  if (!TabRequestsMap[tabId][requestId][url]) {
    TabRequestsMap[tabId][requestId][url] = {
      requestId: requestId,
      url: url,
      startTime: null,
      method: null,
      statusCode: null,
      fromCache: null,
      contentType: null,
      type: null,
      error: false,
      aborted: false,
      finished: false,
      requestSize: null,
      responseSize: null,
      connectStart: null,
      connectEnd: null,
      requestStart: null,
      requestEnd: null,
      responseStart: null,
      responseEnd: null,
      header: null,
      body: {
        formData: null,
        raw: null
      },
      response: {
        header: null
      },
      statusLine: null
    };
    getCurrentTimingDataEntry(tabId).requests.push(TabRequestsMap[tabId][requestId][url]);
  }
  return TabRequestsMap[tabId][requestId][url];
}

function removeRequestEntry(tabId) {
  const entry = TabRequestsMap[tabId];
  if (entry) {
    delete TabRequestsMap[tabId];
  }
  return entry;
}

function hasRequestEntry(tabId, requestId) {
  const requestMap = TabRequestsMap[tabId];
  return requestMap && requestMap[requestId];
}

function sendTimingDataEntries(tabId, onlyFinished) {
  if (tabId === -1) {
    return;
  }

  const dataEntry = TimingData[tabId];
  if (dataEntry) {
    const atIndex = onlyFinished ? dataEntry.findIndex(function(eachData) {
      return !eachData.beforeUnload || eachData.requests.some(function(r) { return !r.finished });
    }) : dataEntry.length;
    if (atIndex > 0) {
      const removedEntries = dataEntry.splice(0, atIndex);
      removedEntries.forEach(function(eachEntry) {
        eachEntry.requests.forEach(function(eachRequest) {
          const rid = eachRequest.requestId;
          const rurl = eachRequest.url;

          const request = TabRequestsMap[tabId][rid][rurl];
          if (request) {
            if (Object.keys(TabRequestsMap[tabId][rid]).length === 1) {
             delete TabRequestsMap[tabId][rid];
            } else {
             delete TabRequestsMap[tabId][rid][rurl];
            }
          }
        });
      });

      const filteredRecords = filterRecordEntries(createRecordEntriesFromArray(removedEntries));
      if (filteredRecords.length > 0) {
        send({
          action: "DUMP_PERFORMANCE_DATA",
          performanceData: JSON.stringify(filteredRecords)
        });
      }
    }
  }
}

browser.webNavigation.onBeforeNavigate.addListener(function(details) {
  if (isXltParametersURL(details.url)) {
    connectByXltParametersURL(details.url);
  }
}, {
   url: [{ urlContains: "xltParameters" }, { schemes: ["data"] }]
});

browser.webNavigation.onCommitted.addListener(function(details) {
  sendTimingDataEntries(details.tabId, true);
});

browser.tabs.onRemoved.addListener(function(tabId, removeInfo) {
  sendTimingDataEntries(tabId, false);

  removeTimingDataEntry(tabId);
  removeRequestEntry(tabId);
});

browser.webRequest.onBeforeRequest.addListener(function(details) {
  if (!isWebRequestOfInterest(details))
    return;

  browser.tabs.get(details.tabId, function(tab) {
    if (!browser.runtime.lastError) {
      const request = getRequestEntry(details.tabId, details.requestId, details.url);
      request.startTime = details.timeStamp;
      request.connectStart = details.timeStamp;

      request.method = details.method;
      request.type = details.type;
      if (details.requestBody) {
        request.body.formData = details.requestBody.formData || null;
        request.body.raw = details.requestBody.raw || null;
      }
      request.requestSize = (request.requestSize || 0) + getRequestBodySize(details);
    }
  });
}, {
  urls: ["<all_urls>"]
}, ["requestBody"]);

browser.webRequest.onBeforeSendHeaders.addListener(function(details) {
  if (!isWebRequestOfInterest(details))
    return;

  browser.tabs.get(details.tabId, function(tab) {
    if (!browser.runtime.lastError) {
      const request = getRequestEntry(details.tabId, details.requestId, details.url);
      request.method = details.method;
      request.type = details.type;
      request.header = details.requestHeaders || null;
    }
  });
}, {
  urls: ["<all_urls>"]
}, ["requestHeaders"]);

browser.webRequest.onSendHeaders.addListener(function(details) {
  if (!isWebRequestOfInterest(details))
    return;

  browser.tabs.get(details.tabId, function(tab) {
    if (!browser.runtime.lastError) {
      const request = getRequestEntry(details.tabId, details.requestId, details.url);

      request.connectEnd = details.timeStamp;
      request.requestStart = details.timeStamp;
      request.requestEnd = details.timeStamp;

      request.method = details.method;
      request.type = details.type;
      request.header = details.requestHeaders || null;
      request.requestSize = (request.requestSize || 0) + getHeaderSize(details.requestHeaders) + getStatusLineSize(details.statusLine);
    }
  });
}, {
  urls: ["<all_urls>"]
}, ["requestHeaders"]);

browser.webRequest.onHeadersReceived.addListener(function(details) {
  if (!isWebRequestOfInterest(details))
    return;

  browser.tabs.get(details.tabId, function(tab) {
    if (!browser.runtime.lastError) {
      const request = getRequestEntry(details.tabId, details.requestId, details.url);

      request.method = details.method;
      request.type = details.type;
      request.statusCode = details.statusCode;
      request.statusLine = details.statusLine;
      request.response.header = details.responseHeaders || null;
      request.responseSize = getHeaderSize(details.responseHeaders) + getStatusLineSize(details.statusLine);
      request.contentType = getContentType(details.responseHeaders);
    }
  });
}, {
  urls: ["<all_urls>"]
}, ["responseHeaders"]);

browser.webRequest.onBeforeRedirect.addListener(function(details) {
  if (!isWebRequestOfInterest(details))
    return;

  browser.tabs.get(details.tabId, function(tab) {
    if (!browser.runtime.lastError) {
      const request = getRequestEntry(details.tabId, details.requestId, details.url);
      request.responseStart = details.timeStamp;
      request.responseEnd = details.timeStamp;

      request.method = details.method;
      request.type = details.type;
      request.fromCache = details.fromCache;
      request.statusCode = details.statusCode;
      request.statusLine = details.statusLine;
      request.response.header = details.responseHeaders || null;
      request.finished = true;
      request.responseSize = getHeaderSize(details.responseHeaders) + getStatusLineSize(details.statusLine);
      request.contentType = getContentType(details.responseHeaders);
    }
  });
}, {
  urls: ["<all_urls>"]
}, ["responseHeaders"]);

browser.webRequest.onResponseStarted.addListener(function(details) {
  if (!isWebRequestOfInterest(details))
    return;

  browser.tabs.get(details.tabId, function(tab) {
    if (!browser.runtime.lastError) {
      const request = getRequestEntry(details.tabId, details.requestId, details.url);
      request.responseStart = details.timeStamp;

      request.method = details.method;
      request.type = details.type;
      request.fromCache = details.fromCache;
      request.statusCode = details.statusCode;
      request.statusLine = details.statusLine;
      request.response.header = details.responseHeaders || null;
      request.responseSize = getHeaderSize(details.responseHeaders) + getStatusLineSize(details.statusLine);
      request.contentType = getContentType(details.responseHeaders);
    }
  });
}, {
  urls: ["<all_urls>"]
}, ["responseHeaders"]);

browser.webRequest.onCompleted.addListener(function(details) {
  if (!isWebRequestOfInterest(details))
    return;

  browser.tabs.get(details.tabId, function(tab) {
    if (!browser.runtime.lastError) {
      const request = getRequestEntry(details.tabId, details.requestId, details.url);
      request.responseEnd = details.timeStamp;

      request.method = details.method;
      request.type = details.type;
      request.fromCache = details.fromCache;
      request.statusCode = details.statusCode;
      request.statusLine = details.statusLine;
      request.response.header = details.responseHeaders || null;
      request.finished = true;
      request.responseSize = getResponseSize(details);
      request.contentType = getContentType(details.responseHeaders);
    }
  });
}, {
  urls: ["<all_urls>"]
}, ["responseHeaders"]);

browser.webRequest.onErrorOccurred.addListener(function(details) {
  if (!isWebRequestOfInterest(details))
    return;

  browser.tabs.get(details.tabId, function(tab) {
    if (!browser.runtime.lastError) {
      const request = getRequestEntry(details.tabId, details.requestId, details.url);
      request.responseEnd = details.timeStamp;

      request.error = true;
      request.finished = true;
      request.aborted = details.error === "net::ERR_ABORTED";
      request.method = details.method;
      request.type = details.type;
      request.fromCache = details.fromCache;
      request.responseSize = getResponseSize(details);

      if (request.startTime === null) {
        request.startTime = details.timeStamp;
      }
    }
  });
}, {urls: ["<all_urls>"]});

browser.runtime.onMessage.addListener(function(message, sender, sendResponse) {
  const data = getDataIfContentMessage(message);
  if (!data)
    return;

  const tabId = sender.tab.id;
  if (data === "eventBeforeUnload") {
    if (hasTimingDataEntry(tabId)) {
      const currentDataEntry = getCurrentTimingDataEntry(tabId);
      currentDataEntry.beforeUnload = true;
      setTimingData(currentDataEntry, message.value);
    }
    newTimingDataEntry(tabId);
  } else if (data === "eventLoad") {
    if (hasTimingDataEntry(tabId)) {
      getCurrentTimingDataEntry(tabId).loaded = true;
    }
  }
  else if(data === "eventResourceTimingBufferFull") {
    if(hasTimingDataEntry(tabId)) {
      setTimingData(getCurrentTimingDataEntry(tabId), message.value);
    }
  }
});

function getRequestBodySize(details) {
  let size = 0;
  if (details.requestBody) {
    if (details.requestBody.formData) {
      for (const key in details.requestBody.formData) {
        size += key.length;
        details.requestBody.formData[key].forEach(function(eachData) {
          size += (eachData.length || eachData.byteLength) || 0;
        });
      }
    }
    if (details.requestBody.raw && details.requestBody.raw.length > 0) {
      details.requestBody.raw.forEach(function(eachData) {
        if(eachData.bytes){
          size += eachData.bytes.byteLength;
        }
        else if(eachData.file) {
          size += eachData.file.length;
        }
      });
    }
  }
  return size;
}

function getResponseSize(details) {
  let size = getHeaderSize(details.responseHeaders) + getStatusLineSize(details.statusLine);
  if (!details.fromCache) {
    size += getContentLength(details.responseHeaders);
  }
  return size;
}

function getStatusLineSize(statusLine) {
  if (statusLine) {
    return statusLine.length + CRLF.length;
  }
  return 0;
}

function getStatusText(statusLine) {
  const m = (statusLine || '').match(reResponseStatus);
  if (m !== null && m.length === 2) {
    return m[1];
  }
  return null;
}

function getHeaderSize(headerArray) {
  let size = 0;
  if (headerArray && headerArray.length) {
    headerArray.forEach(function(eachHeader) {
      size += (eachHeader.name.length + ": ".length + eachHeader.value.length + CRLF.length);
    });
    size += CRLF.length;
  }
  return size;
}

function getContentLength(headerArray) {
  const val = getHeaderValue(headerArray, 'content-length');
  return val ? parseInt(val) : 0;
}

function getContentType(headerArray) {
  return getHeaderValue(headerArray, 'content-type');
}

function getHeaderValue(headerArray, headerName) {
  if (headerArray) {
    for (const header of headerArray) {
      if ((header.name || '').toLowerCase() === headerName) {
        return header.value;
      }
    }
  }
  return null;
}

function createRecordEntry(dataEntry) {

  function enrichRequestEntry(reqEntry, timingEntry) {
    reqEntry.startTime = timingEntry.startTime
      ? Math.round(timingEntry.startTime)
      : reqEntry.startTime;
    reqEntry.connectTime = timingEntry.connectTime
      ? Math.round(timingEntry.connectTime)
      : timingEntry.connectTime;
    reqEntry.sendTime = timingEntry.sendTime
      ? Math.round(timingEntry.sendTime)
      : timingEntry.sendTime;
    reqEntry.busyTime = timingEntry.busyTime
      ? Math.round(timingEntry.busyTime)
      : timingEntry.busyTime;
    reqEntry.receiveTime = timingEntry.receiveTime
      ? Math.round(timingEntry.receiveTime)
      : timingEntry.receiveTime;
    reqEntry.firstBytesTime = timingEntry.firstBytesTime
      ? Math.round(timingEntry.firstBytesTime)
      : timingEntry.firstBytesTime;
    reqEntry.lastBytesTime = timingEntry.lastBytesTime
      ? Math.round(timingEntry.lastBytesTime)
      : timingEntry.lastBytesTime;
    reqEntry.duration = timingEntry.duration
      ? Math.round(timingEntry.duration)
      : reqEntry.duration;
    reqEntry.dnsTime = timingEntry.dnsTime
      ? Math.round(timingEntry.dnsTime)
      : reqEntry.dnsTime;
    reqEntry.responseSize = timingEntry.transferSize
      ? timingEntry.transferSize
      : reqEntry.responseSize
  }

  const leftovers = {};
  const record = {
    timings: dataEntry.timings,
    requests: []
  };

  const entriesCopy = {};
  for (const i in dataEntry.entries) {
    for (const u in dataEntry.entries[i]) {
      entriesCopy[u] = Array.from(dataEntry.entries[i][u]);
    }
  }

  dataEntry.requests.forEach(function(eachRequest) {
    const url = eachRequest.url;
    const requestEntry = {
      url: url,
      requestId: eachRequest.requestId,
      requestSize: eachRequest.requestSize,
      responseSize: eachRequest.responseSize,
      statusCode: eachRequest.statusCode,
      contentType: eachRequest.contentType || getRequestContentType(eachRequest.type),
      fromCache: eachRequest.fromCache,
      error: eachRequest.error,
      aborted: eachRequest.aborted,
      finished: eachRequest.finished,
      startTime: eachRequest.startTime,
      duration: null,
      dnsTime: null,
      connectTime: null,
      sendTime: null,
      firstBytesTime: null,
      lastBytesTime: null,
      receiveTime: null,
      busyTime: null,
      method: eachRequest.method,
      header: eachRequest.header,
      body: getRequestBody(eachRequest),
      response: {
        header: eachRequest.response.header
      },
      statusText: getStatusText(eachRequest.statusLine)
    };

    const startTime = getRequestStartTime(eachRequest);

    requestEntry.startTime = startTime ? Math.round(startTime) : startTime;
    requestEntry.connectTime = eachRequest.connectEnd && eachRequest.connectStart
      ? Math.round(eachRequest.connectEnd - eachRequest.connectStart)
      : null;
    requestEntry.sendTime = eachRequest.requestEnd && eachRequest.requestStart
      ? Math.round(eachRequest.requestEnd - eachRequest.requestStart)
      : null;
    requestEntry.busyTime = eachRequest.responseStart && eachRequest.requestEnd
      ? Math.round(eachRequest.responseStart - eachRequest.requestEnd)
      : null;
    requestEntry.receiveTime = eachRequest.responseEnd && eachRequest.responseStart
      ? Math.round(eachRequest.responseEnd - eachRequest.responseStart)
      : null;
    requestEntry.firstBytesTime = requestEntry.connectTime + requestEntry.sendTime + requestEntry.busyTime;
    requestEntry.lastBytesTime = requestEntry.firstBytesTime + requestEntry.receiveTime;
    requestEntry.duration = requestEntry.startTime && eachRequest.responseEnd
      ? Math.round(eachRequest.responseEnd - requestEntry.startTime)
      : null;
    requestEntry.dnsTime = eachRequest.domainLookupStart && eachRequest.domainLookupEnd
      ? Math.round(eachRequest.domainLookupEnd - eachRequest.domainLookupStart)
      : null;

    const resourceEntries = entriesCopy[url];
    if (resourceEntries && resourceEntries.length > 0) {
      // multiple requests to same URL -> schedule request-entry for post-processing
      if (resourceEntries.length > 1) {
        leftovers[url] = (leftovers[url] || []).concat(requestEntry);
        return;
      }
      // make sure we don't process those resource entries again
      delete entriesCopy[url];

      enrichRequestEntry(requestEntry, resourceEntries[0]);
    }

    record.requests.push(requestEntry);
  });

  for (const url in leftovers) {
    const worklist = leftovers[url];
    // sort request-entries by startTime in ascending order
    worklist.sort(function(x, y) {
      const z = x.startTime - y.startTime;
      return z < 0 ? (-1) : (z > 0 ? 1 : 0);
    });

    // get corresponding timing entries (already sorted)
    const timingEntries = entriesCopy[url];
    // post-process request entries one by one
    // -> as both lists are in chronological order, each request entry at index j
    //    should match to the timing entry at index j (if it exists)
    while (worklist.length > 0) {
      const requestEntry = worklist.shift();
      // there might be no more timing entries left for whatever reason
      if (timingEntries.length > 0) {
        enrichRequestEntry(requestEntry, timingEntries.shift());
      }

      record.requests.push(requestEntry);
    }
  }

  return record;
}

function getRequestStartTime(timingEntry) {
  const startTime = timingEntry.startTime;
  return timingEntry.connectStart > startTime
    ? timingEntry.connectStart
    : timingEntry.connectEnd > startTime
      ? timingEntry.connectEnd
      : timingEntry.requestStart > startTime
        ? timingEntry.requestStart
        : timingEntry.requestEnd > startTime
          ? timingEntry.requestEnd
          : timingEntry.responseStart > startTime
            ? timingEntry.responseStart
            : startTime
}

function getRequestContentType(type) {
  const ResourceType = browser.webRequest.ResourceType;
  if (ResourceType.MAIN_FRAME === type || ResourceType.SUB_FRAME === type || ResourceType.PING === type || ResourceType.OTHER === type || ResourceType.XMLHTTPREQUEST === type) {
    return null;
  }
  return type;
}

function setTimingData(dataEntry, data) {
  if (data.timingData.timings) {
    dataEntry.timings = data.timingData.timings;
  }
  dataEntry.entries = (dataEntry.entries || []).concat(data.timingData.entries);
}

function updateTimingData(tabId, dataEntry) {
  browser.tabs.sendMessage(parseInt(tabId), createContentMessage("getTimingData"), function(response) {
    if (!browser.runtime.lastError) {
      const data = getDataIfContentMessage(response);
      if (!data)
        return;

      setTimingData(dataEntry, data);
    }
    dataEntry.waitLonger = false;
  });
}

function prepareStoreData(timeout, onDataCollectedHandler) {
  function dataCollected() {
    onDataCollectedHandler(createRecordEntries(TimingData));
  }

  const waitForEntries = [];
  for (const eachTabId in TimingData) {
    const dataEntry = getCurrentTimingDataEntry(eachTabId);
    // we're not interested in tabs that haven't made any requests
    if(dataEntry.requests.length > 0) {
      waitForEntries.push({
        tabId: eachTabId,
        entry: dataEntry
      });
    }
  }

  const checkCompletedDelay = 250;
  const updateCheckDelay = 500;
  // don't spend more than 75% of allowed time with waiting for pending requests to complete
  const safeToScheduleCompletedCheck = (function () {
    const endTime = Date.now() + Math.floor(timeout * 0.75);
    return function(){
      return Date.now() < endTime;
    }
  })();

  const scheduleAllowed = (function() {
    const endTime = Date.now() + timeout;
    return function(){
      return Date.now() < endTime;
    }
  })();

  setTimeout(function check_completed() {
    // check if some tab's requests are still pending
    if (safeToScheduleCompletedCheck() && waitForEntries.some(function(e) { return e.entry.requests.some(function(r){ return !r.finished }) })) {
      // schedule another check
      return setTimeout(check_completed, checkCompletedDelay);
    }
    // something to do?
    if (waitForEntries.length > 0) {
      // assume that all tabs are loaded and all requests have completed
      // -> request a timing data update
      waitForEntries.forEach(function(e) {
        e.entry.waitLonger = true;
        updateTimingData(e.tabId, e.entry);
      });

      // schedule a task that checks for the update to be complete
      return setTimeout(function check_updateCompleted() {
        // schedule another check if timeout wasn't reached yet and update is still not complete
        if (scheduleAllowed() && waitForEntries.some(function(e) { return e.entry.waitLonger })) {
          return setTimeout(check_updateCompleted, updateCheckDelay);
        }
        // timeout reached or update is complete
        dataCollected();
      }, updateCheckDelay);
    }

    // timeout reached
    // OR nothing to do at all
    dataCollected();
  }, 10);
}

function filterRecordEntries(dataArray) {
  const entries = [];
  dataArray.forEach(function(eachData) {
    const filteredRequests = eachData.requests.filter(function(eachRequest) {
      return !eachRequest.fromCache && (configuration.recordIncompleted || (eachRequest.finished && !eachRequest.aborted));
    });
    if(filteredRequests.length > 0) {
      entries.push(Object.assign({}, eachData, { requests: filteredRequests}));
    }
  });

  return entries;
}

function createRecordEntries(timingDataEntries) {
  let records = [];
  for (const eachTabId in timingDataEntries) {
    records = records.concat(createRecordEntriesFromArray(timingDataEntries[eachTabId]));
  }
  return records;
}

function createRecordEntriesFromArray(timingDataEntryArray) {
  return timingDataEntryArray.map(createRecordEntry);
}

function createContentMessage(data) {
  return {communicationID: BackgroundCommunicationID, data: data};
}

function getDataIfContentMessage(message) {
  if (!message || message.communicationID !== ContentCommunicationID)
    return;

  return message.data;
}

function getDataIfExternalMessage(message) {
  if (!message || message.communicationID !== ExternalCommunicationID)
    return;

  return message.data;
}

function toBase64(arrBuf) {
  const bmap = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".split('');
  const arr = [];

  let l = arrBuf.byteLength;
  let truncate = false;

  // limit to 8kB
  if (l > 8192) {
    l = 8190;
    truncate = true;
  }

  const view = new DataView(arrBuf);

  const bytesLeft = l % 3;
  const firstPadIdx = l - bytesLeft;
  for (let i = 0; i < firstPadIdx; i = i+3 ) {
    let byte1 = view.getUint8(i);
    let byte2 = view.getUint8(i+1);
    let byte3 = view.getUint8(i+2);


    let b1 = byte1 >> 2;
    let b2 = ((byte1 & 3) << 4) + (byte2 >> 4);
    let b3 = ((byte2 & 15) << 2) + (byte3 >> 6);
    let b4 = byte3 & 63;

    arr.push(bmap[b1], bmap[b2], bmap[b3], bmap[b4]);
  }

  if (bytesLeft > 0) {
    let byte1 = view.getUint8(firstPadIdx);
    let byte2 = bytesLeft > 1 ? view.getUint8(firstPadIdx+1) : 0;

    let b1 = byte1 >> 2;
    let b2 = ((byte1 & 3) << 4) + (byte2 >> 4);
    let b3 = ((byte2 & 15) << 2);

    arr.push(bmap[b1], bmap[b2], bytesLeft === 1 ? '=' : bmap[b3], '=');
  }

  if(truncate) {
    arr.push('...');
  }

  return arr.join('');
}

function urlEncode(data) {
  function _encodeField(strOrBuffer) {
    if (strOrBuffer instanceof ArrayBuffer) {
      strOrBuffer = String.fromCharCode.apply(null, new Uint8Array(strOrBuffer));
    }
    return encodeURIComponent(strOrBuffer).replace(/%20/g, '+');
  }

  const str = [];
  for (const name in data){
    const key = _encodeField(name);
    for (const val of data[name]){
      const value = _encodeField(val);
      str.push([key, value].join('='));
    }
  }

  return str.join('&');
}

function decodeBytes(byteBuffer, encoding) {
  const decoder = new TextDecoder(encoding || 'utf-8', { fatal: true });
  return decoder.decode(byteBuffer);
}

function getContentTypeCharset(contentType) {
  const idx = contentType ? contentType.indexOf(';') : -1;
  let charset = null;
  if (idx > -1) {
    const params = contentType.substring(idx+1).toLowerCase();
    const match = params.match(/charset=([a-z0-9\-]+)/);
    if (match && match.length > 1) {
      charset = match[1];
    }
  }
  return charset;
}

function getRequestBody(request) {

  function stripFileContent(text, contentType) {
    const match = (contentType||'').match(/multipart\/form-data;\s*boundary=(\S+)/);
    if(match && match.length > 0){
      const boundary = match[1];
      const boundLength = boundary.length;

      const s = [];
      let idx = -1, lastIdx = 0;
      while((idx = text.indexOf(boundary, idx + boundLength)) > -1) {
        const startIdx = text.indexOf(CRLF + CRLF, idx);
        if(startIdx > -1){
          const headerPart = text.substring(idx, startIdx);
          if (/[Cc]ontent-[Dd]isposition:\s*form-data;.*filename=.+/.test(headerPart)) {
            let endIdx = text.indexOf(boundary, idx + boundLength);
            if (endIdx < 0) {
              endIdx = text.length;
            }

            s.push(text.substring(lastIdx, startIdx + 4));

            lastIdx = endIdx;
            idx = endIdx;
          }
        }
      }

      s.push(text.substring(lastIdx));

      return s.join('');
    }
    return text;
  }

  const contentType = getContentType(request.header);

  const multiPart = /multipart\/form-data/.test(contentType);

  const isText = /text\/.+|application\/(json|(java|ecma)script|x-www-form-urlencoded|.+\+xml|xml\b)/.test(contentType);
  const uploadDataArr = request.body.raw || [];

  let encoding = getContentTypeCharset(contentType);
  if (!encoding) {
    if (/text\/plain/.test(contentType)) {
      encoding = 'us-ascii';
    }
    // forms might define an hidden input named '_charset_' that contains the charset encoding used by the browser for transmission
    else {
      if (request.body.formData) {
        const _arr = (request.body.formData['_charset_']) || [];
        if(_arr.length > 0) {
          encoding = _arr[0];
        }
      }
      else if (multiPart) {
        uploadDataArr.some(function(d){
          if ('bytes' in d) {
            try {
              const text = decodeBytes(d.bytes, 'us-ascii');
              const match = text.match(/[Cc]ontent\-[Dd]isposition:\s*form\-data;\s*name="_charset_"\s+([a-z0-9\-]+)\s+/);
              if(match && match.length > 1){
                encoding = match[1];
                return true;
              }
            }
            catch (e) {
              // we did our best
            }
          }
          return false;
        });
      }
    }
  }

  let rawData = null;
  if (request.body.raw) {
    rawData = uploadDataArr.map(function(d) {
      // check if 'file' property is set
      if ('file' in d){
        return { file: d.file };
      }
      // check if 'bytes' property is set
      if ('bytes' in d) {
        // check if contentType denotes text or multipart form-data
        if (isText || multiPart) {
          try {
            // Firefox gives us the entire body (including file content) as byte array in case of file upload
            // -> get rid of the file content in order to be consistent w/ Chrome
            return { text: stripFileContent(decodeBytes(d.bytes, encoding), contentType) };
          }
          catch (e){
            // decoding failed -> fall back to base64
          }
        }
        return { base64: toBase64(d.bytes) };
      }
      return null;
    });
  }

  let formData = null;
  if (request.body.formData) {
    let encodeAsRawData = false;

    formData = {};
    for (const name in request.body.formData) {
      formData[name] = (request.body.formData[name] || []).map(function(val) {
        // val is given as ArrayBuffer when not UTF-8
        if (val instanceof ArrayBuffer) {
          if (encoding) {
            try {
              return decodeBytes(val, encoding);
            }
            catch (e) {
            }
          }

          encodeAsRawData = true;
        }
        return val;
      });

      if (encodeAsRawData) {
        break;
      }
    }

    if (encodeAsRawData) {
      formData = null;
      rawData = (rawData || []).concat([{ text: urlEncode(request.body.formData) }]);
    }
  }

  return { formData: formData, raw: rawData };
}
