(function () {

    let transaction = null,
        actionlist = null,
        requestContent = null,
        requestText = null,
        actionContent = null,
        actionContentImage = null,
        actionImage = null,
        errorContent = null,
        postRequestParam = null,
        requestBodySmall = null,
        localTimeZone = null,
        extras = {
            highlight: false,
            beautify: {
                js: false,
                css: false,
                html: false
            }
        },
        menu = null,
        menuIcon = null,
        transactionContent = null,
        valueLog = null,
        zoomLevel = 100;

    // function aliases
    const getElementById = id => document.getElementById(id),
        queryFirst = query => document.querySelector(query),
        queryAll = query => document.querySelectorAll(query),
        forEachElement = (collection, fn) => Array.prototype.forEach.call(collection, fn),
        filterElements = (collection, fn) => Array.prototype.filter.call(collection, fn),
        mapElements = (collection, fn) => Array.prototype.map.call(collection, fn);

    function ajax(url, options) {
        return fetch(url, options || {})
    }

    function show(element) {
        if (element) {
            element.classList.remove("hide");
        }
    }

    function hide(element) {
        if (element) {
            element.classList.add("hide");
        }
    }

    function toggle(element) {
        if (element) {
            element.classList.toggle("hide")
        }
    }

    function getParents(element) {
        const result = [];
        for (let parent = element && element.parentElement; parent; parent = parent.parentElement) {
            result.push(parent);
        }
        return result;
    }

    function empty(element) {
        if (element) {
            while (element.firstChild) {
                element.removeChild(element.lastChild);
            }
        }
    }

    function getText(element) {
        if (element) {
            return element.innerText;
        }
    }

    function setText(element, text) {
        if (element) {
            element.innerText = text || "";
        }
    }

    /**
     * Returns the index of the elment in list
     * @param {Array} list
     * @param {HTMLElement} element
     * @returns Index of element in list, if not found then -1
     */
    function getIndexOfElementInList(list, element) {
        let elementFound = false;
        let index = 0;
        for (const el of list) {
            if (el === element) {
                elementFound = true;
                break;
            }
            index++;
        }

        return elementFound ? index : -1;
    }

    function disableButton(buttonID, disable) {
        const button = getElementById(buttonID);
        if (button) {
            if (disable) {
                button.setAttribute("disabled", "true");
            } else {
                button.removeAttribute("disabled")
            }
        }
    }

    const dataStore = {
        store: new WeakMap(),
        storeData: function (element, value) {
            this.store.set(element, value);
        },
        fetchData: function (element) {
            return this.store.get(element)
        }
    };

    function init() {
        transaction = getElementById("transaction");
        actionlist = getElementById("actionlist");
        requestContent = getElementById("requestcontent");
        requestText = getElementById("requesttext");
        actionContent = getElementById("actioncontent");
        actionContentImage = getElementById("actioncontent_image");
        actionImage = getElementById("actionimage");
        errorContent = getElementById("errorcontent");
        postRequestParam = getElementById("postrequestparameters");
        requestBodySmall = getElementById("requestBodySmall");

        requestBodyJsonViewer = getElementById("requestBodyJson");
        responseBodyJsonViewer = getElementById("responseBodyJson");

        transactionContent = getElementById("transactionContent");
        valueLog = getElementById("valueLog");

        menu = getElementById("menu");
        menuIcon = getElementById("menu-icon");

        extras.highlight = !!window.hljs;
        extras.beautify.js = !!window.js_beautify;
        extras.beautify.html = !!window.html_beautify;
        extras.beautify.css = !!window.css_beautify;

        localTimeZone = (function () {
            const dateString = new Date().toString(),
                zone = dateString.match(/\(([^\(]+)\)$/) || dateString.match(/(GMT[-+0-9]*)/);

            if (zone && zone.length > 1) {
                return zone[1];
            }

            return null;
        })();

        // Check for presence of HAR file by simply loading it via AJAX
        // -> In case AJAX call fails, HAR file is assumed to be missing
        //    and 'View as HAR' link will be visually hidden
        ajax("data.har", { dataType: 'json' }).then((response) => {
            if (!response.ok) {
                forEachElement(transaction.querySelectorAll(".har"), hide);
            }
        }).catch((error) => {
            forEachElement(transaction.querySelectorAll(".har"), hide);
        });

        initEvents();
    }

    function initEvents() {
        const highlight = getElementById("highlightSyntax");
        const beautify = getElementById("beautify");

        if (extras.highlight) {
            // init listeners for highlight button
            highlight.addEventListener("click", function () {
                hljs.highlightBlock(requestText);
            });
        }

        if (extras.beautify.js || extras.beautify.html || extras.beautify.css) {
            // init listeners for beautify button
            beautify.addEventListener("click", function () {
                let s = getText(requestText);
                // CSS
                if (requestText.classList.contains("css")) {
                    try {
                        s = css_beautify(s);
                    }
                    catch (e) { }
                }
                // Javascript / JSON
                else if (requestText.classList.contains("javascript")) {
                    try {
                        s = js_beautify(s);
                    }
                    catch (e) { }
                }
                // HTML
                else if (requestText.classList.contains("html") || requestText.classList.contains('xml')) {
                    try {
                        s = html_beautify(s, {
                            preserve_newlines: false,
                            wrap_line_length: 0
                        });
                    }
                    catch (e) { }
                }
                setText(requestText, s);
            });
            
        	const actionimgSwitch = getElementById("actionimgSwitch");
        	actionimgSwitch.addEventListener("click", function () {
                zoomImage(actionimgSwitch);
            });
        }

        const selectResponseContent = getElementById("selectResponseContent");

        const listener = function () {
            document.getSelection().selectAllChildren(requestText);
        }

        selectResponseContent.removeEventListener("click", listener);
        selectResponseContent.addEventListener("click", listener);

        // menu button
        menuIcon.addEventListener("click", showMenu);
        document.addEventListener("click", function (e) {
            const x = e.target;
            if (filterElements(getParents(x), parent => parent.id == "menu").length === 0 && x.id != "menu-icon") {
                if (menu.classList.contains("open")) {
                    showMenu();
                }
            }
        });

        forEachElement(queryAll("#contentTypeFilter input"), (el) => el.addEventListener("change", function (event) {
            const checkbox = event.target;
            const type = checkbox.getAttribute('name');
            filterRequestsByContentType(type);
        }));

        forEachElement(queryAll("#requestMethodFilter input"), (el) => el.addEventListener("change", function (event) {
            const checkbox = event.target;
            const type = checkbox.getAttribute('name');
            filterRequestsByMethod(type);
        }));

        forEachElement(queryAll("#protocolFilter input"), (el) => el.addEventListener("change", function (event) {
            const checkbox = event.target;
            const type = checkbox.getAttribute('name');
            filterRequestsByProtocol(type);
        }));

        // transaction page
        transaction.addEventListener("click", showTransaction);
    }
    
    function zoomImage(element) {

		if (element.id == "actionimgSwitch")
		{
			if (element.classList.contains('fit'))
			{				
				element.classList.remove('fit');
				element.classList.add('full');
				element.title = "Click to enlarge to 100%";
				actionImage.style = "width: 100%;"
			}
			else if (element.classList.contains('full'))
			{
				element.classList.remove('full');
				element.classList.add('fit');
				element.title = "Click to fit image to screen width";
				actionImage.style = "size: 100%;"
				zoomLevel = 100;
			}
		}
	}

    function toggleContent(element) {
        // show the given content pane and hide the others
        show(element);

        // get all siblings of element
        const otherContentPanes = filterElements(element.parentElement.children, current => current != element);
        otherContentPanes.forEach(hide);
    }

    function showTransaction() {
        toggleContent(transactionContent);

        // unselect any selected action/request in the navigation
        forEachElement(actionlist.querySelectorAll("li"), (el) => el.classList.remove("current", "active"));
    }

    function htmlEncode(value) {
        const div = document.createElement("div");
        setText(div, value);
        return div.innerHTML;
    }

    function trim(str) {
        str = str || '';
        return str.replace(/^\s+|\s+$/g, '');
    }

    function showAction(element) {
        // only show this action if not shown yet
        if (!element.classList.contains("active")) {
            // switch active state of navigation
            forEachElement(actionlist.querySelectorAll(".active"), (el) => el.classList.remove("active"));

            element.classList.add("active");

            // update and show action content iframe
            const data = dataStore.fetchData(element),
                actionFile = data.fileName;
            if (actionFile) {
				if (actionFile.indexOf(".png") > -1)
				{
					// put page screenshots in div
					actionImage.setAttribute("src", actionFile);
                	toggleContent(actionContentImage);
				}
				else
				{
					// put html pages in iframe
                	actionContent.setAttribute("src", actionFile);
                	toggleContent(actionContent);
                }
            }
            else {
                toggleContent(errorContent);
            }
        }

        if (!element.classList.contains("current")) {
            forEachElement(actionlist.querySelectorAll(".current"), (el) => el.classList.remove("current"));
            element.classList.add("current");
        }
    }
    
    function showHideMenu() {
        // show/hide the menu
        document.getElementById("leftSideMenu").classList.toggle("expanded");
        document.getElementById("content").classList.toggle("expanded");
        // switch the menu toggle
        document.getElementById("mExpander").classList.toggle("expanded");
    }

    function expandCollapseAction(element) {
        // lazily create the requests
        if (!element.querySelector("ul.requests")) {
            createRequestsForAction(element);
        }

        // show/hide the requests
        toggle(element.querySelector("ul.requests"));

        // show/hide the requests
        element.querySelector(".expander").classList.toggle("expanded");
    }

    function createRequestsForAction(actionElement) {
        // build requests element
        const requests = document.createElement("ul");
        requests.classList.add("requests");
        const action = dataStore.fetchData(actionElement);
        const actionRequests = action && action.requests || [];

        actionElement.appendChild(requests)

        // make sure, we do not see it building up
        hide(requests);

        // ok, we have to add the data from the json object to it
        for (const request of actionRequests) {
            const name = request.name;
            const contentTypeClass = determineContentTypeClass(request.mimeType, request.responseCode);
            const protocolClass = determineProtocolClass(request.url);
            const title = "[" + request.responseCode + "] " + request.url;

            const requestElement = document.createElement("li");
            requestElement.classList.add("request");
            requestElement.title = htmlEncode(title);

            const nameElement = document.createElement("span");
            nameElement.classList.add("name", htmlEncode(contentTypeClass), htmlEncode(request.requestMethod), htmlEncode(protocolClass));
            nameElement.innerHTML = htmlEncode(name);

            // attach listeners at action's name
            // setup onclick to show request content
            nameElement.addEventListener(
                "click",
                function (event) {
                    showRequest(this.parentNode);
                    event.stopPropagation();
                }
            );

            // setup ondblclick to do nothing
            nameElement.addEventListener(
                "dblclick",
                function (event) {
                    event.stopPropagation();
                }
            );

            requestElement.appendChild(nameElement);

            // store the json object for later
            dataStore.storeData(requestElement, request);

            // insert into DOM
            requests.appendChild(requestElement);
        }

        filterRequestsByContentType();
        filterRequestsByMethod();
        filterRequestsByProtocol();
    }

    function determineContentTypeClass(mimeType, responseCode) {
        if (responseCode >= 400 || responseCode == 0) {
            return "httpError";
        }
        if (responseCode == 301 || responseCode == 302 || responseCode == 303 || responseCode == 307 || responseCode == 308) {
            return "httpRedirect";
        }
        if (mimeType.indexOf("image/") == 0) {
            return "contentTypeImage";
        }
        if (mimeType.indexOf("text/css") == 0) {
            return "contentTypeCSS";
        }
        if (mimeType.indexOf("javascript") >= 0 || mimeType.indexOf("application/json") == 0) {
            return "contentTypeJS";
        }
        return "contentTypeOther";
    }

    function determineProtocolClass(url) {
        // url starts with 'https'
        if (url.lastIndexOf('https', 0) === 0) {
            return "HTTPS";
        }
        else if (url.lastIndexOf('http', 0) === 0) {
            return "HTTP";
        }
        else {
            return "protocolOther";
        }
    }

    function populateKeyValueTable(table, keyValueArray) {
        const isRequestHeaderTable = table.id === 'requestheaders',
            kvLength = keyValueArray.length;

        // Clear table contents first.
        empty(table);

        if (kvLength == 0) {
            const tableRow = document.createElement("tr");
            const tableCell = document.createElement("td");

            tableCell.classList.add("empty");
            tableCell.colSpan = "2";
            tableCell.innerHTML = "None.";

            tableRow.appendChild(tableCell);

            table.appendChild(tableRow);
        }
        else {
            for (let i = 0; i < kvLength; i++) {
                let kv = keyValueArray[i],
                    name = htmlEncode(kv.name_),
                    value = kv.value_ || '';
                if (isRequestHeaderTable && name.toLowerCase() === "cookie") {
                    value = value.split(";").map(function (e) {
                        let idx = e.indexOf('='),
                            cname = idx < 0 ? e : e.substring(0, idx),
                            cvalue = idx < 0 || idx > e.length - 1 ? '' : e.substring(idx + 1);
                        return [cname, cvalue].map(trim).map(htmlEncode);
                    })
                        .sort(function (a, b) { return a = a[0].toLowerCase(), b = b[0].toLowerCase(), a < b ? -1 : b < a ? 1 : 0 })
                        .map(function (e) {
                            return '<div class="crow"><span class="cname">' + e[0] + '</span><span class="csep">=</span><span class="cvalue">' + e[1] + '</span></div>';
                        })
                        .join('');
                }
                else {
                    value = htmlEncode(value);
                }

                const tableRow = document.createElement("tr");
                const tableCell1 = document.createElement("td");
                const tableCell2 = document.createElement("td");

                tableCell1.classList.add("key");
                tableCell1.innerHTML = name;

                tableCell2.classList.add("value");
                tableCell2.innerHTML = value;

                tableRow.appendChild(tableCell1);
                tableRow.appendChild(tableCell2);

                table.appendChild(tableRow);
            }
        }

        return table;
    }

    function activateTab(element) {
        // switch active tab header
        const selected = requestContent.querySelector(".selected");
        if (selected) {
            selected.classList.remove("selected");
        }
        element.classList.add("selected");

        // switch active tab panel
        forEachElement(queryAll("#requestcontent > div"), hide);

        // filter of elements
        const index = getIndexOfElementInList(queryAll('#requestcontent li'), element);

        if (index > -1) {
            show(queryAll('#requestcontent > div')[index]);
        }
    }

    function isJsonContent(contentType) {
        //  e.g. "application/json" or "application/<...>+json"
        return /^application\/(.+\+)?json$/.test(contentType);
    }

    function showRequest(element) {
        // get action parent element
        const action = filterElements(getParents(element), parent => parent.classList.contains("action"))[0];

        // only show this request if not shown yet
        if (!element.classList.contains("active")) {
            // switch active state of navigation
            forEachElement(actionlist.querySelectorAll(".active"), (el) => el.classList.remove("active"));
            element.classList.add("active");

            hide(getElementById("loadErrorContent"));

            // clear JSON viewers
            requestBodyJsonViewer.clear();
            responseBodyJsonViewer.clear();

            // retrieve the request data
            const requestData = dataStore.fetchData(element);

            // update content view tab based on the mime type
            const requestImage = getElementById("requestimage");

            if (requestData.mimeType.indexOf('image/') == 0) {
                // update the image
                requestImage.src = requestData.fileName;
                show(requestImage);
                hide(requestText);
            }
            else {
                hide(requestImage);

                forEachElement(queryAll("#beautify, #selectResponseContent, #highlightSyntax"), (el) => el.setAttribute('disabled', ''));

                // check if we have no response or it was empty
                if (requestData._noContent) {
                    setText(requestText);
                    show(requestText);
                }
                else {
                    const options = {
                        dataType: 'text'
                    }

                    ajax(requestData.fileName, options)
                        .then((response) => {
                            if (!response.ok) {
                                throw new Error('Network response was not OK');
                            }
                            return response.text();
                        })
                        .then((data) => {
                            const subMime = requestData.mimeType.substring(requestData.mimeType.indexOf('/') + 1),
                                lang = /x?html/.test(subMime) ? 'html' : /xml/.test(subMime) ? 'xml' : /(javascript|json)$/.test(subMime) ? 'javascript' : /^css$/.test(subMime) ? 'css' : undefined,
                                canBeautify = lang && ((/(ht|x)ml/.test(lang) && extras.beautify.html) || ('javascript' === lang && extras.beautify.js) || ('css' === lang && extras.beautify.css));

                            disableButton("beautify", !canBeautify);
                            disableButton("highlightSyntax", !extras.highlight);
                            disableButton("selectResponseContent", false);

                            setText(requestText, data);
                            requestText.classList.remove(...requestText.classList);
                            requestText.classList.add(...(lang ? [`language-${lang}`, lang] : ['text']));
                            show(requestText);

                            // feed the response body json viewer if the mime type indicates json-ish content
                            if (isJsonContent(requestData.mimeType)) {
                                responseBodyJsonViewer.load(data);
                            }
                        }).catch(() => {
                            hide(requestText);

                            const errorMessageFileName = queryFirst("#errorMessage .filename");

                            errorMessageFileName.setAttribute('disabled', '');
                            setText(errorMessageFileName, requestData.fileName);
                            show(getElementById("loadErrorContent"));
                        });
                }
            }

            // update the request information tab
            const urlElement = getElementById("url");
            empty(urlElement);

            const linkElement = document.createElement("a");
            linkElement.href = requestData.url;
            linkElement.target = "_blank";
            setText(linkElement, requestData.url);

            urlElement.appendChild(linkElement);

            setText(getElementById("requestmethod"), requestData.requestMethod);

            // start time
            const startDate = new Date(requestData.startTime);
            setText(getElementById("time-start-gmt"), formatDate(startDate, true));
            setText(getElementById("time-start-local"), formatDate(startDate));

            // headers and parameters
            populateKeyValueTable(getElementById("requestheaders"), requestData.requestHeaders);
            populateKeyValueTable(getElementById("requestparameters"), requestData.requestParameters);
            populateKeyValueTable(getElementById("queryparameters"), requestData.queryParameters);

            // show either the request body or the POST parameters
            const bodyRaw = requestData.requestBodyRaw || '';
            if (bodyRaw.length > 0) {
                // request body
                setText(requestBodySmall.querySelector("textarea"), bodyRaw);
                show(requestBodySmall);
                hide(postRequestParam);
            }
            else {
                // POST parameters
                const isPost = requestData.requestMethod === "POST";

                isPost ? show(postRequestParam) : hide(postRequestParam);

                hide(requestBodySmall);
            }

            // update the request content tab
            setText(getElementById("requestbody"), requestData.requestBodyRaw || '');

            // feed the request body json viewer if the content type indicates json-ish content
            const requestContentTypeHeader = requestData.requestHeaders.find(e => e.name_.toLowerCase() === "content-type");
            const requestContentType = requestContentTypeHeader ? requestContentTypeHeader.value_ : "";
            if (isJsonContent(requestContentType)) {
                requestBodyJsonViewer.load(requestData.requestBodyRaw);
            }

            // update the response information tab
            setText(getElementById("protocol"), requestData.protocol);
            setText(getElementById("status"), parseStatusLine(requestData.status));
            setText(getElementById("loadtime"), `${requestData.loadTime} ms`);

            populateKeyValueTable(getElementById("responseheaders"), requestData.responseHeaders);

            // finally show the request content
            toggleContent(requestContent);
        }

        if (!action.classList.contains("current")) {
            forEachElement(actionlist.querySelectorAll(".current"), (el) => el.classList.remove("current"));
            action.classList.add("current");
        }
    }

    function parseStatusLine(status) {
        let statusMessage = "n/a";

        if (status) {
            const match = status.match(/(\d{3})[- ]+(.+)/);

            if (match) {
                statusMessage = (match.length > 1 ? match[1] : "n/a");
                statusMessage += (" - " + (match.length > 2 ? match[2] : "n/a"));
            }
        }

        return statusMessage;
    }

    function formatDate(date, toGmt) {
        let year, month, day, hours, minutes, seconds, appendix;

        if (toGmt) {
            year = date.getUTCFullYear();
            month = String(date.getUTCMonth() + 1).padStart(2, 0);
            day = String(date.getUTCDate()).padStart(2, 0);
            hours = String(date.getUTCHours()).padStart(2, 0);
            minutes = String(date.getUTCMinutes()).padStart(2, 0);
            seconds = String(date.getUTCSeconds()).padStart(2, 0);
            appendix = `.${date.getUTCMilliseconds()} [GMT]`;
        }
        else {
            year = date.getFullYear();
            month = String(date.getMonth() + 1).padStart(2, 0);
            day = String(date.getDate()).padStart(2, 0);
            hours = String(date.getHours()).padStart(2, 0);
            minutes = String(date.getMinutes()).padStart(2, 0);
            seconds = String(date.getSeconds()).padStart(2, 0);
            appendix = `.${date.getMilliseconds()} [${localTimeZone}]`;
        }

        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}${appendix}`;
    }

    function preprocessRequests(requests) {
        function kvSort(a, b) {
            const aName = a.name_, bName = b.name_;
            if (aName < bName) return -1;
            if (aName > bName) return 1;
            return 0;
        }

        function checkHasNoContent(rqData) {
            rqData = rqData || {};

            const headers = rqData.responseHeaders || [],
                respCode = rqData.responseCode || 0;

            // check for redirect (response file is empty and will cause an error when trying to be read in)
            // and zero content length response header as well
            if (/30[0-8]|20[45]/.test(respCode) || !rqData.fileName) {
                return true;
            }

            for (let i = 0, l = headers.length, h; i < l; i++) {
                h = headers[i];
                if (h.name_ === "Content-Length") {
                    return h.value_ === "0";
                }
            }

            return false;
        }

        function decodeQueryParam(param) {
            param = param || '';

            const kv = param.split('=').map(decodeQPNameOrValue);

            let r = null;

            if (kv && kv.length > 0) {
                r = {
                    name_: kv[0],
                    value_: kv.length > 1 ? kv.slice(1).join('=') : ''
                };
            }

            return r;
        }

        function decodeQPNameOrValue(nameOrValue) {
            nameOrValue = (nameOrValue || '').replace(/[+]/g, ' ');
            try {
                nameOrValue = decodeURIComponent(nameOrValue);
            }
            catch (e) {
                if (typeof (window.unescape) === 'function') {
                    try {
                        nameOrValue = window.unescape(nameOrValue);
                    }
                    catch (e2) { }
                }
            }
            return nameOrValue;
        }

        function parseParams(str) {
            str = str || '';

            let params = [];
            if (str.length > 0) {
                params = str.split('&')
                    // transform into decoded name/value pairs
                    .map(decodeQueryParam)
                    // filter out nulls and empty names
                    .filter(function (e) {
                        return !!e && e.name_.length;
                    })
                    // and sort it
                    .sort(kvSort);
            }
            return params;
        }

        function parsePostBodyIfNecessary(rqData) {
            rqData = rqData || {};

            let body = rqData.requestBodyRaw || '';

            const method = rqData.requestMethod,
                params = rqData.requestParameters,
                isUrlEncoded = rqData.requestHeaders.some(function (e) {
                    const n = e.name_.toLowerCase(),
                        v = (e.value_ || '').toLowerCase();
                    return n === 'content-type' && v === 'application/x-www-form-urlencoded';
                });
            if (method === 'POST' && isUrlEncoded && body.length > 0) {
                body = body.split('\n');
                body = body[body.length - 1];

                parseParams(body).forEach(function (p) {
                    params.push(p)
                });
            }
        }

        const l = requests && requests.length;
        for (let i = 0, r; i < l; i++) {
            r = requests[i];
            r._noContent = checkHasNoContent(r);
            parsePostBodyIfNecessary(r);
            r.requestHeaders.sort(kvSort);
            r.responseHeaders.sort(kvSort);
            r.requestParameters.sort(kvSort);

            // parse request query string
            const url = r.url || '',
                idx = url.indexOf('?'),
                hIdx = url.indexOf('#');

            let params = [];

            if (idx > 0 && (hIdx < 0 || idx < hIdx)) {
                const qs = url.substring(idx + 1, (hIdx < 0 ? url.length : hIdx));
                params = parseParams(qs);
            }

            params.sort(kvSort);
            r.queryParameters = params;
        }
    }

    const filters = {
        type: {
            variants: ["contentTypeCSS", "contentTypeImage", "httpError", "contentTypeJS", "httpRedirect", "contentTypeOther"],
            category: "#contentTypeFilter",
            requestMarker: "contentTypeFiltered",
            all: "all"
        },
        method: {
            variants: ["GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE", "CONNECT", "PATCH"],
            category: "#requestMethodFilter",
            requestMarker: "methodFiltered",
            all: "all"
        },
        protocol: {
            variants: ["HTTP", "HTTPS"],
            category: "#protocolFilter",
            requestMarker: "protocolFiltered",
            all: "all"
        }
    };

    function filterRequestsByContentType(selection) {
        filterRequests(selection, filters.type);
    }

    function filterRequestsByMethod(selection) {
        filterRequests(selection, filters.method);
    }

    function filterRequestsByProtocol(selection) {
        filterRequests(selection, filters.protocol);
    }

    function filterRequests(selection, filter) {
        if (!selection) {
            filter.variants.forEach(function (type) {
                filterRequests(type, filter);
            });
        }
        else {
            const checked = !!queryFirst(`${filter.category} .filter-${selection} input`).checked
            if (filter.all && selection == filter.all) {
                filter.variants.forEach(function (type) {
                    // set all other checkboxes accordingly
                    forEachElement(queryAll(`${filter.category} .filter-${type} input`), (el) => el.checked = checked);

                    // update requests
                    filterRequests(type, filter);
                });
            }
            else {
                const requests = mapElements(queryAll(`#actionlist .requests .request .${selection}`), el => el.parentElement);

                if (checked) {
                    if (requests) {
                        forEachElement(requests, el => el.classList.remove(filter.requestMarker));
                    }
                }
                else {
                    if (requests) {
                        forEachElement(requests, el => el.classList.add(filter.requestMarker));
                    }
                }
            }

            if (filter.all) {
                const checkALL = checked && !filter.variants.some(function (variant) {
                    return !queryFirst(`${filter.category} .filter-${variant} input`).checked;
                });
                queryFirst(`${filter.category} .filter-${filter.all} input`).checked = checkALL;
            }
        }
    }

    function showMenu() {
        const open = "open";

        if (menu.classList.contains(open)) {
            hide(menu);
        }
        else {
            menu.style.position = "absolute";
            menu.style.top = `${menuIcon.offsetTop}px`;
            menu.style.left = `${menuIcon.offsetLeft + 17}px`;
            menu.style.zIndex = "200001";

            show(menu);
        }

        menu.classList.toggle(open);
    }

    function loadJSON() {
        // get the json data from the external file
        const transactionData = jsonData,
            transactionActions = transactionData.actions;

        document.title = transactionData.user + " - XLT Result Browser";

        setText(queryFirst("#transaction > .name"), transactionData.user);

        const actions = document.createElement("ul");
        actions.classList.add("actions")

        for (let i = 0, l = transactionActions.length; i < l; i++) {
            const action = transactionActions[i];
            const actionElement = document.createElement("li");
            actionElement.classList.add("action");
            actionElement.title = "Double-click to show/hide this action\'s requests.";
            const expander = document.createElement("span");
            expander.classList.add("expander");
            expander.title = "Single-click to show/hide this action\'s requests.";
            const name = document.createElement("span");
            name.classList.add("name");
            name.innerHTML = `${htmlEncode(action.name)}`;
            actionElement.appendChild(expander);
            actionElement.appendChild(name);

            // store the json object for later
            dataStore.storeData(actionElement, action);
            // attach listeners at action's name
            const nameElement = actionElement.querySelector(".name");
            // setup onclick to show action content
            nameElement.addEventListener(
                "click",
                function () {
                    showAction(this.parentNode);
                }
            );
            // setup ondblclick to show/hide requests
            nameElement.addEventListener(
                "dblclick",
                function () {
                    expandCollapseAction(this.parentNode);
                }
            );

            const expanderElement = actionElement.querySelector(".expander");

            // setup click to show/hide requests
            expanderElement.addEventListener(
                "click",
                function () {
                    expandCollapseAction(this.parentNode);
                }
            );

            // setup ondblclick to do nothing since a dblclick causes the following event sequence to be dispatched:
            // dblclick ::= click -> click -> dblclick
            expanderElement.addEventListener(
                "dblclick",
                function (event) {
                    event.stopPropagation();
                }
            );

            // insert into DOM
            actions.appendChild(actionElement)

            // preprocess action's requests
            preprocessRequests(action.requests);
        }

        // insert the actions into the DOM
        actionlist.appendChild(actions);

        // show them
        show(actions);

        // test parameters and results
        populateKeyValueTable(valueLog, transactionData.valueLog);
    }

    window.addEventListener("DOMContentLoaded", function () {
        init();

        const progress = getElementById("progressmeter");

        try {
            show(progress);

            loadJSON();

            // setup onclick for the tabbed panel in the request content
            // area
            forEachElement(requestContent.querySelectorAll(".tabs-nav li"), (li) => li.addEventListener("click", function () {
                activateTab(this);
            }));

			function splitContent(mm) {
				if (mm.matches) { // If media query matches / is large screen
			    	Split(['#leftSideMenu', '#content'], {
			        	sizes: [15, 85],
			            minSize: [300, 600],
			            gutterSize: 3
			        });
				} 
				else { //on small screens/mobile: revert changes made by Split
					document.getElementById("leftSideMenu").removeAttribute("style");
					document.getElementById("content").removeAttribute("style");
					var gutter = document.getElementsByClassName('gutter-horizontal');
					while(gutter[0]) {
    					gutter[0].parentNode.removeChild(gutter[0]);
					}
					
					if (document.getElementById("mExpander") === null)
					{
						const mExpander = document.createElement("span");
	        			mExpander.classList.add("expander");
	        			mExpander.classList.add("expanded");
	        			mExpander.id = "mExpander";
	        			mExpander.title = "Single-click to show/hide menu.";
	        			document.getElementById("header").appendChild(mExpander);
	        
				        // setup click to show/hide requests
				        mExpander.addEventListener(
				        	"click",
				            function () {
				                showHideMenu();
				            }
				        );
				        
				        // content click also shows/hides menu
				        /*content.addEventListener(
				        	"click",
				            function () {
				                showHideMenu();
				            }
						);*/
			        }
				}
			}		
			// Create a MediaQueryList object
			var mm = window.matchMedia("(min-width: 700px)")
			// Call listener function at run time
			splitContent(mm);
			// Attach listener function on state changes
			mm.addEventListener("change", function() {
				splitContent(mm);
			}); 

            // activate first request-tab
            activateTab(requestContent.querySelector(".tabs-nav li"));

            // open the first action, if available
            const firstAction = actionlist.querySelector("li.action > span.name");
            if (firstAction) {
                firstAction.click();
            }
        }
        finally {
            hide(progress);
        }
    });

    // show content, as soon as the page is fully loaded to prevent unstyled flashing content
    window.addEventListener("load", () => getElementById("container").classList.remove("visibilityHidden"));
})();
