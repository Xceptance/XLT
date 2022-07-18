(function ($) {

    let navigation = null,
        transaction = null,
        actionlist = null,
        header = null,
        content = null,
        requestContent = null,
        requestText = null,
        actionContent = null,
        errorContent = null,
        postRequestParam = null,
        requestBodySmall = null,
        leftSideMenu = null,
        navTopOffset = 0,
        localTimeZone = null,
        extras = {
            highlight: true,
            beautify: {
                js: true,
                css: true,
                html: true
            }
        },
        menu = null,
        menuIcon = null,
        transactionContent = null,
        valueLog = null;


    function ajax(url, options) {
        return fetch(url, options || {})
    }

    function show(element) {
        if (element) {
            element.style.display = "block";
        }
    }

    function hide(element) {
        if (element) {
            element.style.display = "none";
        }
    }

    function toggle(element) {
        if (element) {
            element.style.display = element.style.display === "none" ? "block" : "none";
        }
    }

    function getParents(element) {
        let result = [];
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
            $(element).text(); // TODO replace with native js
        }
    }

    function setText(element, text) {
        if (element) {
            $(element).text(text); // TODO replace with native js
        }
    }

    function getElementById(id) {
        return document.getElementById(id);
    }

    function getElementByQuery(query) {
        return document.querySelector(query);
    }

    function getJSElement(jquery_element) {
        return jquery_element[0];
    }

    function getPixelPropertyAsNumber(element, propertyName) {
        if (!element) {
            return null;
        }

        return parseInt(getComputedStyle(element)[propertyName].replace(/px/, ''));
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

        function cachedScript(url, options) {

            options = {
                ...(options || {}),
                ...{
                    dataType: 'script',
                    cache: true
                }
            }

            return ajax(url, options);
        }

        navigation = document.getElementById("navigation");
        transaction = document.getElementById("transaction");
        actionlist = document.getElementById("actionlist");
        header = document.getElementById("header");
        leftSideMenu = document.getElementById("leftSideMenu");
        content = document.getElementById("content");
        requestContent = document.getElementById("requestcontent");
        requestText = document.getElementById("requesttext");
        actionContent = document.getElementById("actioncontent");
        errorContent = document.getElementById("errorcontent");
        postRequestParam = document.getElementById("postrequestparameters");
        requestBodySmall = document.getElementById("requestBodySmall");

        transactionContent = document.getElementById("transactionContent");
        valueLog = document.getElementById("valueLog");

        menu = document.getElementById("menu");
        menuIcon = document.getElementById("menu-icon");

        navTopOffset = parseInt(getComputedStyle(navigation).top.replace(/px/, '')) + 2;

        let protocol = /^https?/.test(location.protocol) ? location.protocol : 'http:';
        $('<link href="' + protocol + '//xlt.xceptance.com/static/highlightjs/7.5/styles/xc.min.css" rel="stylesheet" type="text/css" />').appendTo('head');
        cachedScript(protocol + '//xlt.xceptance.com/static/highlightjs/7.5/highlight.min.js').catch(() => extras.highlight = false)
        cachedScript(protocol + '//xlt.xceptance.com/static/beautify/20140610-bdf3c2e743/beautify-min.js').catch(() => extras.beautify.js = false)
        cachedScript(protocol + '//xlt.xceptance.com/static/beautify/20140610-bdf3c2e743/beautify-html-min.js').catch(() => extras.beautify.html = false)
        cachedScript(protocol + '//xlt.xceptance.com/static/beautify/20140610-bdf3c2e743/beautify-css-min.js').catch(() => extras.beautify.css = false)

        localTimeZone = (function () {
            let dateString = new Date().toString(),
                zone = dateString.match(/\(([^\(]+)\)$/) || dateString.match(/(GMT[-+0-9]*)/);

            if (zone && zone.length > 1) {
                return zone[1];
            }

            return null;
        })();

        // Check for presence of HAR file by simply loading it via AJAX
        // -> In case AJAX call fails, HAR file is assumed to be missing
        //    and 'View as HAR' link will be visually hidden
        ajax(url, { dataType: 'json' }).catch(() => transaction.querySelectorAll(":scope .har").forEach(hide))

        initEvents();
    }

    function initEvents() {
        let $highlight = $('#highlightSyntax'),
            $beautify = $('#beautify');

        if (extras.highlight) {
            $highlight.click(function () {
                requestText.each(function (i, e) {
                    hljs.highlightBlock(e)
                });
            }).removeAttr('disabled');
        }

        if (extras.beautify.js || extras.beautify.css || extras.beautify.html) {
            $beautify.click(function () {
                let s = requestText.text();
                // CSS
                if (requestText.hasClass('css')) {
                    try {
                        s = css_beautify(s);
                    }
                    catch (e) { }
                }
                // Javascript / JSON
                else if (requestText.hasClass('javascript')) {
                    try {
                        s = js_beautify(s);
                    }
                    catch (e) { }
                }
                // HTML
                else if (requestText.hasClass('html') || requestText.hasClass('xml')) {
                    try {
                        s = html_beautify(s, {
                            preserve_newlines: false,
                            wrap_line_length: 0
                        });
                    }
                    catch (e) { }
                }
                requestText.text(s);
            }).removeAttr('disabled');
        }

        $('#selectResponseContent').unbind("click").click(function () {
            document.getSelection().selectAllChildren(requestText.get(0));
        });


        // menu button
        menuIcon.click(showMenu);
        $(document).click(function (e) {
            let x = e.target;
            if ($(x).parents('#menu').length === 0 && x.id != "menu-icon") {
                if (menu.classList.contains("open")) {
                    showMenu();
                }
            }
        });

        $('#contentTypeFilter input').change(function (event, handler) {
            let checkbox = event.target;
            let type = checkbox.getAttribute('name');
            filterRequestsByContentType(type);
        });

        $('#requestMethodFilter input').change(function (event, handler) {
            let checkbox = event.target;
            let type = checkbox.getAttribute('name');
            filterRequestsByMethod(type);
        });

        $('#protocolFilter input').change(function (event, handler) {
            let checkbox = event.target;
            let type = checkbox.getAttribute('name');
            filterRequestsByProtocol(type);
        });

        // transaction page
        transaction.addEventListener("click", showTransaction);

        // JSON viewer
        $('#jsonViewerActions .expandAll').click(function (event, handler) { jsonView.expandAll(); });
        $('#jsonViewerActions .collapseAll').click(function (event, handler) { jsonView.collapseAll(); });
        $('#jsonViewerActions .search').keyup(search);
        $('#jsonViewerActions .ignoreCase').click(search);
        $('#jsonViewerActions .filter').click(search);
        $('#jsonViewerActions .previous').click(function (event, handler) { jsonView.highlightNextMatch(false); });
        $('#jsonViewerActions .next').click(function (event, handler) { jsonView.highlightNextMatch(true); });
    }

    function search() {
        let searchPhrase = $('#jsonViewerActions .search').val();
        let ignoreCase = $('#jsonViewerActions .ignoreCase').is(":checked");
        let filter = $('#jsonViewerActions .filter').is(":checked");

        jsonView.search(searchPhrase, ignoreCase, filter);
    }

    function toggleContent(element) {
        // show the given content pane and hide the others
        show(element);

        // get all siblings of element
        const otherContentPanes = [...element.parentElement.children].filter(current => current != element);

        for (const otherContentPane of otherContentPanes) {
            hide(otherContentPane);
        }
    }

    function showTransaction() {
        toggleContent(transactionContent);

        // unselect any selected action/request in the navigation
        actionlist.querySelectorAll(":scope li").forEach((el) => el.classList.remove("current", "active"))
    }

    function htmlEncode(value) {
        // TODO
        return $('<div/>').text(value).html();
    }

    function trim(str) {
        str = str || '';
        return str.replace(/^\s+|\s+$/g, '');
    }

    function showAction(element) {
        // only show this action if not shown yet
        if (!element.classList.contains("active")) {
            // switch active state of navigation
            actionlist.querySelectorAll(":scope .active").forEach((el) => el.classList.remove("active"))
            element.classList.add("active");

            // update and show action content iframe
            const data = dataStore.fetchData(element),
                actionFile = data.fileName;
            if (actionFile) {
                actionContent.src = actionFile
                toggleContent(actionContent);
            }
            else {
                toggleContent(errorContent);
            }
        }

        if (!element.classList.contains("current")) {
            actionlist.querySelectorAll(":scope .current").forEach((el) => el.classList.remove("current"))
            element.classList.add("current");
        }
    }

    function expandCollapseAction(element) {
        // lazily create the requests
        if (!element.querySelector("ul.requests")) {
            createRequestsForAction(element);
        }

        // show/hide the requests
        toggle(element.querySelector("ul.requests")); // TODO former $('ul.requests', element).slideToggle(200, resizeContent)
        resizeContent();

        // show/hide the requests
        element.querySelector(".expander").classList.toggle("expanded"); // TODO former $('.expander', element).toggleClass("expanded")
    }

    function createRequestsForAction(actionElement) {
        // build requests element
        let requests = document.createElement("ul");
        requests.classList.add("requests");
        let action = dataStore.fetchData(actionElement);
        let actionRequests = action && action.requests || [];

        actionElement.appendChild(requests)

        // make sure, we do not see it building up
        hide(requests);

        // ok, we have to add the data from the json object to it
        for (const request of actionRequests) {
            let name = request.name;
            let contentTypeClass = determineContentTypeClass(request.mimeType, request.responseCode);
            let protocolClass = determineProtocolClass(request.url);
            let title = "[" + request.responseCode + "] " + request.url;

            let requestElement = document.createElement("li");
            requestElement.classList.add("request");
            requestElement.title = htmlEncode(title);

            let nameElement = document.createElement("span");
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

        // TODO IMPORTANT
        debugger;
        // filterRequestsByContentType();
        // filterRequestsByMethod();
        // filterRequestsByProtocol();
    }

    function determineContentTypeClass(mimeType, responseCode) {
        if (responseCode >= 400 || responseCode == 0) {
            return "httpError";
        }
        if (responseCode == 301 || responseCode == 302) {
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
        $('.selected', requestContent).removeClass('selected');
        $(element).addClass('selected');

        // switch active tab panel
        $('#requestcontent > div').hide();
        let index = $('#requestcontent li').index(element);

        $('#requestcontent > div').eq(index).show();
    }

    function showRequest(element) {

        // get action parent element
        let action = getParents(element).filter(parent => parent.classList.contains("action"))[0];

        // only show this request if not shown yet
        if (!element.classList.contains("active")) {
            // switch active state of navigation
            actionlist.querySelectorAll(":scope .active").forEach((el) => el.classList.remove("active"))
            element.classList.add("active");

            hide(getElementById("errorMessage"))

            document.querySelector("#jsonViewerActions .search").value = "";

            setText(getElementById(jsonViewerContent), "");

            // retrieve the request data
            let requestData = dataStore.fetchData(element);

            // update content view tab based on the mime type
            let requestImage = document.getElementById("requestimage");

            if (requestData.mimeType.indexOf('image/') == 0) {
                // update the image
                requestImage.src = requestData.fileName;
                show(requestImage);
                requestText.hide();
            }
            else {
                hide(requestImage);

                document.querySelector("#beautify, #selectResponseContent, #highlightSyntax").disabled = true;

                // check if we have no response or it was empty
                if (requestData._noContent) {
                    requestText.text('').show();
                }
                else {
                    // update the text, load it from file
                    $.ajax({
                        url: requestData.fileName,
                        dataType: 'text',
                        success: function (data) {
                            let subMime = requestData.mimeType.substring(requestData.mimeType.indexOf('/') + 1),
                                lang = /x?html/.test(subMime) ? 'html' : /xml/.test(subMime) ? 'xml' : /(javascript|json)$/.test(subMime) ? 'javascript' : /^css$/.test(subMime) ? 'css' : undefined,
                                canBeautify = lang && ((/(ht|x)ml/.test(lang) && extras.beautify.html) || ('javascript' === lang && extras.beautify.js) || ('css' === lang && extras.beautify.css));

                            document.getElementById("beautify").disabled = !canBeautify;
                            document.getElementById("selectResponseContent").disabled = false;
                            document.getElementById("highlightSyntax").disabled = !extras.highlight;

                            requestText.text(data).removeClass().addClass(lang ? ('language-' + lang + ' ' + lang) : 'text').show();

                            // feed the json viewer if the mime type indicates json-ish content (e.g. "application/json" or "application/<...>+json")
                            if (/^application\/(.+\+)?json$/.test(requestData.mimeType)) {
                                jsonView.format(data, '#jsonViewerContent');
                            }
                        },
                        error: function (xhr, textStatus, errorThrown) {
                            requestText.hide();
                            document.querySelector("#errorMessage .filename").disabled = true;
                            setText(document.querySelector("#errorMessage .filename"), requestData.fileName);
                            show(document.getElementById("errorMessage"));
                            centerErrorMessage();
                        }
                    });
                }
            }

            // update the request information tab
            let urlElement = getElementById("url");
            empty(urlElement);

            let linkElement = document.createElement("a");
            linkElement.href = requestData.url;
            linkElement.target = "_blank";

            urlElement.appendChild(linkElement);

            setText(urlElement, requestData.url);

            setText(getElementById("requestmethod"), requestData.requestMethod);

            // start time
            let startDate = new Date(requestData.startTime);
            setText(getElementById("time-start-gmt"), formatDate(startDate, true));
            setText(getElementById("time-start-local"), formatDate(startDate));

            // headers and parameters
            populateKeyValueTable(getElementById("requestheaders"), requestData.requestHeaders);
            populateKeyValueTable(getElementById("requestparameters"), requestData.requestParameters);
            populateKeyValueTable(getElementById("queryparameters"), requestData.queryParameters);

            // show either the request body or the POST parameters
            let bodyRaw = requestData.requestBodyRaw || '';
            if (bodyRaw.length > 0) {
                // request body
                setText(requestBodySmall.querySelector("textarea"), bodyRaw);
                hide(requestBodySmall);
                hide(postRequestParam);
            }
            else {
                // POST parameters  
                let isPost = requestData.requestMethod === "POST";

                isPost ? show(postRequestParam) : hide(postRequestParam);

                hide(requestBodySmall);
            }

            // update the request content tab
            setText(getElementById("requestbody"), requestData.requestBodyRaw || '');

            // update the response information tab
            setText(getElementById("protocol"), requestData.protocol);
            setText(getElementById("status"), parseStatusLine(requestData.status));
            setText(getElementById("loadtime"), `${requestData.loadTime} ms`);

            populateKeyValueTable(getElementById("responseheaders"), requestData.responseHeaders);

            // finally show the request content
            toggleContent(requestContent);
        }

        if (!action.classList.contains("current")) {
            actionlist.querySelectorAll(":scope .current").forEach((el) => el.classList.remove("current"))
            action.classList.add("current");
        }
    }

    function parseStatusLine(status) {
        let statusMessage = "n/a";

        if (status) {
            let match = status.match(/(\d{3})[- ]+(.+)/);

            if (match) {
                statusMessage = (match.length > 1 ? match[1] : "n/a");
                statusMessage += (" - " + (match.length > 2 ? match[2] : "n/a"));
            }
        }

        return statusMessage;
    }

    // TODO solved in moment issue
    function formatDate(date, toGmt) {
        let d = moment(date),
            tz = toGmt ? "UTC" : localTimeZone;

        if (toGmt) {
            d.utc();
        }

        let result = d.format();

        result = result.replace("T", " ");
        result = result.replace(/\+.*/, ("." + d.format("SSS")));

        if (tz) {
            result = result + " [" + tz + "]";
        }

        return result;
    }

    function centerErrorMessage() {
        let height = Math.floor(0.333 * getPixelPropertyAsNumber(content, "height")),
            width = getPixelPropertyAsNumber(content, "height") - 700;

        let errorMessage = document.querySelector("#errorMessage, #errorNoPage");
        errorMessage.style.position = "absolute";
        errorMessage.style.left = `${width / 2} px`;
        errorMessage.style.top = `${height / 2} px`;
    }

    /*
     * Resize the action content area
     */
    function resizeContent() {
        let height = window.innerHeight, // get the current viewport size
            leftPos = parseInt(getComputedStyle(content).left.replace(/px/, '')); // and left position of content area

        // resize navigation
        resizeNav(height);
        // .. and content area
        content.style.height = `${height} px`;
        content.style.width = `${window.innerWidth - leftPos} px`;

        // finally, center error message
        centerErrorMessage();
    }

    function resizeNav(winHeight) {
        winHeight = winHeight || window.height();
        actionlist.style.height = `${winHeight - navTopOffset - 15 - getComputedStyle(transaction).height.replace(/px/, "")}px`;
        leftSideMenu.style.height = winHeight;

        const vsplitbar = getElementByQuery(".vsplitbar");
        if (vsplitbar) {
            vsplitbar.style.height = winHeight;
        }
    }

    function preprocessRequests(requests) {
        function kvSort(a, b) {
            let aName = a.name_, bName = b.name_;
            if (aName < bName) return -1;
            if (aName > bName) return 1;
            return 0;
        }

        function checkHasNoContent(rqData) {
            rqData = rqData || {};

            let headers = rqData.responseHeaders || [],
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

            let kv = param.split('=').map(decodeQPNameOrValue),
                r = null;
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

            let body = rqData.requestBodyRaw || '',
                method = rqData.requestMethod,
                params = rqData.requestParameters,
                isUrlEncoded = rqData.requestHeaders.some(function (e) {
                    let n = e.name_.toLowerCase(),
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

        let l = requests && requests.length;
        for (let i = 0, r; i < l; i++) {
            r = requests[i];
            r._noContent = checkHasNoContent(r);
            parsePostBodyIfNecessary(r);
            r.requestHeaders.sort(kvSort);
            r.responseHeaders.sort(kvSort);
            r.requestParameters.sort(kvSort);

            // parse request query string
            let url = r.url || '',
                idx = url.indexOf('?'),
                hIdx = url.indexOf('#'),
                params = [];
            if (idx > 0 && (hIdx < 0 || idx < hIdx)) {
                let qs = url.substring(idx + 1, (hIdx < 0 ? url.length : hIdx));
                params = parseParams(qs);
            }

            params.sort(kvSort);
            r.queryParameters = params;
        }
    }

    let filters = {
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
            let checked = $(filter.category + " .filter-" + selection + " input").prop('checked');
            if (filter.all && selection == filter.all) {
                filter.variants.forEach(function (type) {
                    // set all other checkboxes accordingly
                    $(filter.category + " .filter-" + type + " input").prop('checked', checked);

                    // update requests
                    filterRequests(type, filter);
                });
            }
            else {
                let requests = $("#actionlist .requests .request ." + selection).parent();
                if (checked) {
                    requests.removeClass(filter.requestMarker);
                }
                else {
                    requests.addClass(filter.requestMarker);
                }
            }

            if (filter.all) {
                let checkALL = checked && !filter.variants.some(function (variant) {
                    return !$(filter.category + " .filter-" + variant + " input").prop('checked');
                });
                $(filter.category + " .filter-" + filter.all + " input").prop('checked', checkALL);
            }
        }
    }

    function showMenu() {
        let open = "open";

        if (menu.classList.contains(open)) {
            hide(menu);
        }
        else {
            let menuIconPos = menuIcon.position();
            menu.css({
                position: "absolute",
                top: (menuIconPos.top + menuIcon.outerHeight()) + "px",
                left: (menuIconPos.left + 7) + "px",
                "z-index": 200001,
            })
                .show();
        }

        menu.toggleClass(open);
    }

    function loadJSON() {
        // get the json data from the external file
        let transaction = jsonData,
            actions = transaction.actions;

        document.title = transaction.user + " - XLT Result Browser";

        $('#transaction > .name').text(transaction.user);

        let $actions = document.createElement("ul");
        $actions.classList.add("actions")
        hide($actions);

        for (let i = 0, l = actions.length; i < l; i++) {
            let action = actions[i];
            let $actionElement = document.createElement("li");
            $actionElement.classList.add("action");
            $actionElement.title = "Double-click to show/hide this action\'s requests.";
            let $expander = document.createElement("span");
            $expander.classList.add("expander");
            $expander.title = "Single-click to show/hide this action\'s requests.";
            let $name = document.createElement("span");
            $name.classList.add("name");
            $name.innerHTML = `${htmlEncode(action.name)}`;
            $actionElement.appendChild($expander);
            $actionElement.appendChild($name);

            // store the json object for later
            dataStore.storeData($actionElement, action);
            // attach listeners at action's name
            let $nameElement = $actionElement.querySelector(".name");
            // setup onclick to show action content
            $nameElement.addEventListener(
                "click",
                function () {
                    showAction(this.parentNode);
                }
            );
            // setup ondblclick to show/hide requests
            $nameElement.addEventListener(
                "dblclick",
                function () {
                    expandCollapseAction(this.parentNode);
                }
            );

            let $expanderElement = $actionElement.querySelector(".expander");

            // setup click to show/hide requests
            $expanderElement.addEventListener(
                "click",
                function () {
                    expandCollapseAction(this.parentNode);
                }
            );

            // setup ondblclick to do nothing since a dblclick causes the following event sequence to be dispatched:
            // dblclick ::= click -> click -> dblclick
            $expanderElement.addEventListener(
                "dblclick",
                function (event) {
                    event.stopPropagation();
                }
            );

            // insert into DOM
            $actions.appendChild($actionElement)

            // preprocess action's requests
            preprocessRequests(action.requests);
        }

        // insert the actions into the DOM
        actionlist.appendChild($actions);

        // show them
        show($actions); // TODO former $actions.slideDown(200) (replace animation with CSS transition)

        // test parameters and results
        populateKeyValueTable(valueLog, transaction.valueLog);
    }

    // the on load setup
    $(document).ready(function () {
        init();

        let $progress = $('#progressmeter');

        try {
            $progress.show(100);

            loadJSON();

            // take care of the size of the content display area to
            // adjust it to the window size
            $(window).bind("resize", function (event) {
                resizeContent();
            });

            // setup onclick for the tabbed panel in the request content
            // area
            $('.tabs-nav li', requestContent).click(function (event) {
                activateTab(this);
            });

            $('#wrapper').splitter({ orientation: 'horizontal', limit: 150 });

            // resize in the beginning already
            resizeContent();

            // activate first request-tab
            activateTab($('.tabs-nav li', requestContent).get(0));

            // open the first action
            actionlist.querySelector(":scope li.action > span.name").click();
        }
        finally {
            $progress.hide(200);
        }
    });

})(jQuery);
