(function($){

    var $navigation     = null,
        $transaction    = null;
        $actionlist     = null,
        $header         = null,
        $content        = null,
        $requestContent = null,
        $requestText    = null,
        $actionContent  = null,
        $errorContent   = null,
        $postRequestParam = null,
        $requestBodySmall = null,
        $window         = null,
        $leftSideMenu   = null,
        navTopOffset    = 0,
        localTimeZone   = null,
        extras          = {
            highlight : true,
            beautify  : {
                js  : true,
                css : true,
                html: true
            }
        },
        $menu = null,
        $menuIcon = null;

        $transactionContent = null,
        $valueLog = null;

    function init() {

        function cachedScript(url, options){
            options = $.extend(options || {}, {
                dataType: 'script',
                cache: true,
                url: url
            });

            return $.ajax(options);
        }

        $navigation = $('#navigation');
        $transaction = $('#transaction');
        $actionlist = $('#actionlist');
        $header = $('#header');
        $leftSideMenu = $('#leftSideMenu');

        $content = $('#content');

        $requestContent = $('#requestcontent');
        $requestText = $('#requesttext');
        $actionContent = $('#actioncontent');
        $errorContent = $('#errorcontent');
        $postRequestParam = $('#postrequestparameters');
        $requestBodySmall = $('#requestBodySmall');

        $transactionContent = $('#transactionContent');
        $valueLog = $('#valueLog');

        $window = $(window);

        $menu = $('#menu');
        $menuIcon = $('#menu-icon');

        navTopOffset = parseInt($navigation.css('top').replace(/px/,'')) + 2;

        var protocol = /^https?/.test(location.protocol) ? location.protocol : 'http:';
        $('<link href="'+protocol+'//xlt.xceptance.com/static/highlightjs/7.5/styles/xc.min.css" rel="stylesheet" type="text/css" />').appendTo('head');
        cachedScript(protocol+'//xlt.xceptance.com/static/highlightjs/7.5/highlight.min.js').fail(function(){
            extras.highlight = false;
        });
        cachedScript(protocol+'//xlt.xceptance.com/static/beautify/20140610-bdf3c2e743/beautify-min.js').fail(function(){
            extras.beautify.js = false;
        });
        cachedScript(protocol+'//xlt.xceptance.com/static/beautify/20140610-bdf3c2e743/beautify-html-min.js').fail(function(){
            extras.beautify.html = false;
        });
        cachedScript(protocol+'//xlt.xceptance.com/static/beautify/20140610-bdf3c2e743/beautify-css-min.js').fail(function(){
            extras.beautify.css = false;
        });

        localTimeZone = (function(){
            var dateString = new Date().toString(),
                zone = dateString.match(/\(([^\(]+)\)$/) || dateString.match(/(GMT[-+0-9]*)/);

            if (zone && zone.length > 1) {
                return zone[1];
            }

            return null;
        })();

        // Check for presence of HAR file by simply loading it via AJAX
        // -> In case AJAX call fails, HAR file is assumed to be missing
        //    and 'View as HAR' link will be visually hidden
        $.ajax({ url: 'data.har', dataType: 'json' }).fail(function(){
          $('.har', $transaction).hide();
        });

        initEvents();
    }

    function initEvents() {
        var $highlight = $('#highlightSyntax'),
            $beautify  = $('#beautify');

        if(extras.highlight) {
            $highlight.click(function(){
                $requestText.each(function(i, e) {
                    hljs.highlightBlock(e)
                });
            }).removeAttr('disabled');
        }

        if(extras.beautify.js || extras.beautify.css || extras.beautify.html) {
            $beautify.click(function(){
                var s = $requestText.text();
                // CSS
                if($requestText.hasClass('css')) {
                    try {
                        s = css_beautify(s);
                    }
                    catch(e){}
                }
                // Javascript / JSON
                else if($requestText.hasClass('javascript')) {
                    try {
                        s = js_beautify(s);
                    }
                    catch(e){}
                }
                // HTML
                else if($requestText.hasClass('html') || $requestText.hasClass('xml')) {
                    try {
                        s = html_beautify(s, {
                            preserve_newlines : false,
                            wrap_line_length : 0
                        });
                    }
                    catch(e){}
                }
                $requestText.text(s);
            }).removeAttr('disabled');
        }

        $('#selectResponseContent').unbind("click").click(function() {
          document.getSelection().selectAllChildren($requestText.get(0));
        });


        // menu button
        $menuIcon.click(showMenu);
        $(document).click(function(e){
            var x = e.target;
            if($(x).parents('#menu').length === 0 && x.id != "menu-icon") {
                if($menu.hasClass("open")) {
                    showMenu();
                }
            }
        });

        $('#contentTypeFilter input').change(function(event, handler){
            var checkbox = event.target;
            var type = checkbox.getAttribute('name');
            filterRequestsByContentType(type);
        });

        $('#requestMethodFilter input').change(function(event, handler){
            var checkbox = event.target;
            var type = checkbox.getAttribute('name');
            filterRequestsByMethod(type);
        });

        $('#protocolFilter input').change(function(event, handler){
            var checkbox = event.target;
            var type = checkbox.getAttribute('name');
            filterRequestsByProtocol(type);
        });

        // transaction page
        $transaction.click(showTransaction);

        // JSON viewer
        $('#jsonViewerActions .expandAll').click(function(event, handler) { jsonView.expandAll(); });
        $('#jsonViewerActions .collapseAll').click(function(event, handler) { jsonView.collapseAll(); });
        $('#jsonViewerActions .search').keyup(search);
        $('#jsonViewerActions .ignoreCase').click(search);
        $('#jsonViewerActions .filter').click(search);
        $('#jsonViewerActions .previous').click(function(event, handler) { jsonView.highlightNextMatch(false); });
        $('#jsonViewerActions .next').click(function(event, handler) { jsonView.highlightNextMatch(true); });
    }

    function search() {
        var searchPhrase = $('#jsonViewerActions .search').val();
        var ignoreCase = $('#jsonViewerActions .ignoreCase').is(":checked");
        var filter = $('#jsonViewerActions .filter').is(":checked");

        jsonView.search(searchPhrase, ignoreCase, filter);
    }

    function toggleContent($element) {
        // show the given content pane and hide the others
        $element.show();
        $element.siblings().hide();
    }

    function showTransaction() {
        toggleContent($transactionContent);

        // unselect any selected action/request in the navigation
        $('li', $actionlist).removeClass('current active');
    }

    function htmlEncode(value) {
        return $('<div/>').text(value).html();
    }

    function trim(str){
        str = str || '';
        return str.replace(/^\s+|\s+$/g, '');
    }

    function showAction(element) {
        var $element = $(element);
        // only show this action if not shown yet
        if (!$element.hasClass("active")) {
            // switch active state of navigation
            $(".active", $actionlist).removeClass("active");
            $element.addClass("active");

            // update and show action content iframe
            var data       = $element.data("json"),
                actionFile = data.fileName;
            if(actionFile) {
                $actionContent.attr('src', actionFile);
                toggleContent($actionContent);
            }
            else {
                toggleContent($errorContent);
            }
        }

        if(!$element.hasClass("current")) {
            $(".current", $actionlist).removeClass("current");
            $element.addClass("current");
        }
    }

    function expandCollapseAction(element) {
        // lazily create the requests
        if ($('ul.requests', element).length == 0) {
            createRequestsForAction(element);
        }

        // show/hide the requests
        $('ul.requests', element).slideToggle(200, resizeContent);

        // show/hide the requests
        $('.expander', element).toggleClass("expanded");
    }

    function createRequestsForAction(actionElement) {
        // build requests element
        var requests       = $('<ul class="requests"></ul>'),
            $actionElement = $(actionElement),
            action         = $actionElement.data('json'),
            actionRequests = action && action.requests || [];

        $actionElement.append(requests);

        // make sure, we do not see it building up
        requests.hide();

        // ok, we have to add the data from the json object to it
        for ( var i = 0, l = actionRequests.length; i < l; i++) {
            var request = actionRequests[i];
            var name = request.name;
            var contentTypeClass = determineContentTypeClass(request.mimeType, request.responseCode);
            var protocolClass = determineProtocolClass(request.url);
            var title = "[" + request.responseCode + "] " + request.url;

            var requestElement = $('<li class="request" title="' + htmlEncode(title) + '"><span class="name ' + htmlEncode(contentTypeClass)
                                    + ' ' + htmlEncode(request.requestMethod) + ' ' + htmlEncode(protocolClass) +'">' + htmlEncode(name) + '</span></li>');

            // store the json object for later
            requestElement.data("json", request)
            // attach listeners at action's name
            .children('.name')
            // setup onclick to show request content
            .click( function(event) {
                showRequest(this.parentNode);
                event.stopPropagation();
            })
            // setup ondblclick to do nothing
            .dblclick( function(event) {
                event.stopPropagation();
            });

            // insert into DOM
            requests.append(requestElement);
        }

        filterRequestsByContentType();
        filterRequestsByMethod();
        filterRequestsByProtocol();
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
        if(url.lastIndexOf('https', 0) === 0) {
            return "HTTPS";
        }
        else if(url.lastIndexOf('http', 0) === 0) {
            return "HTTP";
        }
        else {
            return "protocolOther";
        }
    }

    function populateKeyValueTable(table, keyValueArray) {
        var isRequestHeaderTable = table.attr('id') === 'requestheaders',
            kvLength = keyValueArray.length;

        // Clear table contents first.
        table.empty();

        if (kvLength == 0) {
            var tableRow = $('<tr><td class="empty" colspan="2">None.</td></tr>');
            table.append(tableRow);
        }
        else {
            for(var i = 0; i < kvLength; i++) {
                var kv    = keyValueArray[i],
                    name  = htmlEncode(kv.name_),
                    value = kv.value_ || '';
                if(isRequestHeaderTable && name.toLowerCase() === "cookie") {
                    value = value.split(";").map(function(e){
                        var idx = e.indexOf('='),
                            cname = idx < 0 ? e : e.substring(0, idx),
                            cvalue = idx < 0 || idx > e.length-1 ? '' : e.substring(idx+1);
                        return [ cname, cvalue ].map(trim).map(htmlEncode);
                    })
                    .sort(function(a,b){ return a=a[0].toLowerCase(),b=b[0].toLowerCase(),a<b?-1:b<a?1:0 })
                    .map(function(e){
                        return '<div class="crow"><span class="cname">'+e[0]+'</span><span class="csep">=</span><span class="cvalue">'+e[1]+'</span></div>';
                    })
                    .join('');
                }
                else {
                    value = htmlEncode(value);
                }

                table.append($('<tr><td class="key">' + name + '</td><td class="value">' + value + '</td></tr>'));
            }
        }

        return table;
    }

    function activateTab(element) {
        // switch active tab header
        $('.selected', $requestContent).removeClass('selected');
        $(element).addClass('selected');

        // switch active tab panel
        $('#requestcontent > div').hide();
        var index = $('#requestcontent li').index(element);

        $('#requestcontent > div').eq(index).show();
    }

    function showRequest(element) {
        var $element = $(element),
            $action = $element.parents(".action");
        // only show this request if not shown yet
        if (!$element.hasClass("active")) {
            // switch active state of navigation
            $(".active", $actionlist).removeClass("active");
            $element.addClass("active");

            $('#errorMessage').hide();

            $('#jsonViewerActions .search').val('');
            $('#jsonViewerContent').text('');

            // retrieve the request data
            var requestData = $element.data("json");

            // update content view tab based on the mime type
            if (requestData.mimeType.indexOf('image/') == 0) {
                // update the image
                $('#requestimage').attr('src', requestData.fileName).show();
                $requestText.hide();
            }
            else {
                $('#requestimage').hide();

                $('#beautify, #selectResponseContent, #highlightSyntax').prop('disabled', true);

                // check if we have no response or it was empty
                if(requestData._noContent) {
                    $requestText.text('').show();
                }
                else {
                    // update the text, load it from file
                    $.ajax({
                        url : requestData.fileName,
                        dataType : 'text',
                        success : function(data) {
                            var subMime  = requestData.mimeType.substring(requestData.mimeType.indexOf('/')+1),
                                lang = /x?html/.test(subMime) ? 'html' : /xml/.test(subMime) ? 'xml' : /(javascript|json)$/.test(subMime) ? 'javascript' : /^css$/.test(subMime) ? 'css' : undefined,
                                canBeautify = lang && (( /(ht|x)ml/.test(lang) && extras.beautify.html ) || ( 'javascript' === lang && extras.beautify.js) || ('css' === lang && extras.beautify.css));

                            $('#beautify').prop('disabled', !canBeautify);
                            $('#selectResponseContent').prop('disabled', false);
                            $('#highlightSyntax').prop('disabled', !extras.highlight);
                            $requestText.text(data).removeClass().addClass(lang ? ('language-'+lang+' '+lang) : 'text').show();

                            // feed the json viewer if the mime type indicates json-ish content (e.g. "application/json" or "application/<...>+json")
                            if (/^application\/(.+\+)?json$/.test(requestData.mimeType)) {
                                jsonView.format(data, '#jsonViewerContent');
                            }
                        },
                        error : function(xhr,textStatus,errorThrown) {
                            $requestText.hide();
                            $('#errorMessage .filename').text(requestData.fileName);
                            $('#errorMessage').show();
                            centerErrorMessage();
                        }
                    });
                }
            }

            // update the request information tab
            $("#url").empty().append($('<a>').attr('href', requestData.url).attr('target','_blank').text(requestData.url));
            $("#requestmethod").text(requestData.requestMethod);

            // start time
            var startDate = new Date(requestData.startTime);
            $("#time-start-gmt").text(formatDate(startDate, true));
            $("#time-start-local").text(formatDate(startDate));

            // headers and parameters
            populateKeyValueTable($("#requestheaders"), requestData.requestHeaders);
            populateKeyValueTable($("#requestparameters"), requestData.requestParameters);
            populateKeyValueTable($("#queryparameters"), requestData.queryParameters);

            // show either the request body or the POST parameters
            var bodyRaw = requestData.requestBodyRaw || '';
            if(bodyRaw.length > 0) {
                // request body
                $('textarea', $requestBodySmall).text(bodyRaw);
                $requestBodySmall.show();
                $postRequestParam.hide();
            }
            else {
                // POST parameters  
                var isPost = requestData.requestMethod === "POST";
                $postRequestParam.toggle(isPost);
                $requestBodySmall.hide();
            } 

            // update the request content tab
            $("#requestbody").text(requestData.requestBodyRaw || '');

            // update the response information tab
            $("#protocol").text(requestData.protocol);
            $("#status").text(parseStatusLine(requestData.status));
            $("#loadtime").text(requestData.loadTime + " ms");
            populateKeyValueTable($("#responseheaders"), requestData.responseHeaders);

            // finally show the request content
            toggleContent($requestContent);
        }

        if(!$action.hasClass("current")) {
            $(".current", $actionlist).removeClass("current");
            $action.addClass("current");
        }
    }

    function parseStatusLine(status){
      var statusMessage = "n/a";

      if(status) {
        var match = status.match(/(\d{3})[- ]+(.+)/);

        if(match) {
          statusMessage = (match.length > 1 ? match[1] : "n/a");
          statusMessage += (" - "+ (match.length > 2 ? match[2] : "n/a"));
        }
      }

      return statusMessage;
    }

    function formatDate(date, toGmt) {
        var d  = moment(date),
            tz = toGmt ? "UTC" : localTimeZone;

        if(toGmt) {
            d.utc();
        }

        var result = d.format();

        result = result.replace("T", " ");
        result = result.replace(/\+.*/, ("."+d.format("SSS")));

        if(tz) {
            result = result + " [" + tz + "]";
        }

        return result;
    }

    function centerErrorMessage() {
        var content = $content,
            height  = Math.floor(0.333 * content.height()),
            width   = content.width() - 700;

        $('#errorMessage, #errorNoPage').css({ position: 'absolute', left: width/2 + 'px', top: height/2 + 'px' });
    }

    /*
     * Resize the action content area
     */
    function resizeContent() {
        var height = $window.height(), // get the current viewport size
            leftPos = parseInt($content.css('left').replace(/px/,'')); // and left position of content area

        // resize navigation
        resizeNav(height);
        // .. and content area
        $content.height(height).width($window.width()-leftPos);

        // finally, center error message
        centerErrorMessage();
    }

    function resizeNav(winHeight) {
        winHeight = winHeight || $window.height();
        $actionlist.height(winHeight-navTopOffset-15 - $transaction.height());
        $leftSideMenu.height(winHeight);
        $('.vsplitbar').height(winHeight);
    }

    function preprocessRequests(requests) {
        function kvSort(a,b) {
            var aName = a.name_, bName = b.name_;
            if(aName < bName) return -1;
            if(aName > bName) return 1;
            return 0;
        }

        function checkHasNoContent(rqData) {
            rqData = rqData || {};

            var headers  = rqData.responseHeaders || [],
                respCode = rqData.responseCode || 0;

            // check for redirect (response file is empty and will cause an error when trying to be read in)
            // and zero content length response header as well
            if(/30[0-8]|20[45]/.test(respCode) || !rqData.fileName) {
                return true;
            }

            for(var i = 0, l = headers.length, h; i < l; i++) {
                h = headers[i];
                if(h.name_ === "Content-Length") {
                    return h.value_ === "0";
                }
            }

            return false;
        }

        function decodeQueryParam(param) {
            param = param || '';

            var kv = param.split('=').map(decodeQPNameOrValue),
                r  = null;
             if(kv && kv.length > 0) {
                 r = {
                     name_  : kv[0],
                     value_ : kv.length > 1 ? kv.slice(1).join('=') : ''
                 };
             }

             return r;
        }

        function decodeQPNameOrValue(nameOrValue) {
            nameOrValue = (nameOrValue || '').replace(/[+]/g, ' ');
            try {
                nameOrValue = decodeURIComponent(nameOrValue);
            }
            catch(e) {
                if(typeof(window.unescape) === 'function') {
                    try {
                        nameOrValue = window.unescape(nameOrValue);
                    }
                    catch(e2){}
                }
            }
            return nameOrValue;
        }

        function parseParams(str) {
            str = str || '';

            var params = [];
            if(str.length > 0) {
                params = str.split('&')
                // transform into decoded name/value pairs
                .map(decodeQueryParam)
                // filter out nulls and empty names
                .filter(function(e) {
                    return !!e && e.name_.length;
                })
                // and sort it
                .sort(kvSort);
            }
            return params;
        }

        function parsePostBodyIfNecessary(rqData) {
            rqData = rqData || {};

            var body = rqData.requestBodyRaw || '',
                method = rqData.requestMethod,
                params = rqData.requestParameters,
                isUrlEncoded = rqData.requestHeaders.some(function(e){
                    var n = e.name_.toLowerCase(),
                        v = (e.value_ || '').toLowerCase();
                    return n === 'content-type' && v === 'application/x-www-form-urlencoded';
                });
            if(method === 'POST' && isUrlEncoded && body.length > 0) {
                body = body.split('\n');
                body = body[body.length-1];

                parseParams(body).forEach(function(p){
                    params.push(p)
                });
            }
        }

        var l = requests && requests.length;
        for(var i = 0, r; i < l; i++) {
            r = requests[i];
            r._noContent = checkHasNoContent(r);
            parsePostBodyIfNecessary(r);
            r.requestHeaders.sort(kvSort);
            r.responseHeaders.sort(kvSort);
            r.requestParameters.sort(kvSort);

            // parse request query string
            var url = r.url || '',
                idx = url.indexOf('?'),
                hIdx = url.indexOf('#'),
                params = [];
            if(idx > 0 && (hIdx < 0 || idx < hIdx)) {
                var qs = url.substring(idx+1, (hIdx < 0 ? url.length : hIdx));
                params = parseParams(qs);
            }

            params.sort(kvSort);
            r.queryParameters = params;
        }
    }

    var filters = {
        type : {
            variants : ["contentTypeCSS", "contentTypeImage", "httpError", "contentTypeJS", "httpRedirect", "contentTypeOther"],
            category : "#contentTypeFilter",
            requestMarker : "contentTypeFiltered",
            all : "all"
        },
        method : {
            variants : ["GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE", "CONNECT", "PATCH"],
            category : "#requestMethodFilter",
            requestMarker : "methodFiltered",
            all : "all"
        },
        protocol : {
            variants : ["HTTP", "HTTPS"],
            category : "#protocolFilter",
            requestMarker : "protocolFiltered",
            all : "all"
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
        if(!selection) {
            filter.variants.forEach(function(type) {
                filterRequests(type, filter);
            });
        }
        else {
            var checked = $(filter.category+" .filter-"+selection+" input").prop('checked');
            if(filter.all && selection==filter.all) {
                filter.variants.forEach(function(type) {
                    // set all other checkboxes accordingly
                    $(filter.category+" .filter-"+type+" input").prop('checked', checked);

                    // update requests
                    filterRequests(type, filter);
                });
            }
            else {
                var requests = $("#actionlist .requests .request ."+selection).parent();
                if(checked) {
                    requests.removeClass(filter.requestMarker);
                }
                else {
                    requests.addClass(filter.requestMarker);
                }
            }

            if(filter.all) {
                var checkALL = checked && !filter.variants.some(function(variant){
                  return !$(filter.category + " .filter-" + variant + " input").prop('checked');
                });
                $(filter.category+" .filter-"+filter.all+" input").prop('checked', checkALL);
            }
        }
    }

    function showMenu() {
        var open = "open";

        if($menu.hasClass(open)) {
            $menu.hide();
        }
        else {
          var menuIconPos = $menuIcon.position();
          $menu.css({
              position: "absolute",
              top: (menuIconPos.top + $menuIcon.outerHeight())+"px",
              left: (menuIconPos.left + 7)+"px",
              "z-index": 200001,
          })
          .show();
        }

        $menu.toggleClass(open);
    }

    function loadJSON() {
        // get the json data from the external file
        var transaction = jsonData,
            actions     = transaction.actions;

        document.title = transaction.user + " - XLT Result Browser";

        $('#transaction > .name').text(transaction.user);

        var $actions = $('<ul class="actions"></ul>');
        $actions.hide();

        for ( var i = 0, l = actions.length; i < l; i++) {
            var action = actions[i];
            var $actionElement = $('<li class="action" title="Double-click to show/hide this action\'s requests."><span class="expander" title="Single-click to show/hide this action\'s requests."/><span class="name">' + htmlEncode(action.name) + '</span></li>');

            // store the json object for later
            $actionElement.data("json", action)
            // attach listeners at action's name
            .children('.name')
            // setup onclick to show action content
            .click( function(event) {
                showAction(this.parentNode);
            })
            // setup ondblclick to show/hide requests
            .dblclick( function(event) {
                expandCollapseAction(this.parentNode);
            });

            // setup click to show/hide requests
            $('.expander', $actionElement).click( function(event) {
                expandCollapseAction(this.parentNode);
            })
            // setup ondblclick to do nothing since a dblclick causes the following event sequence to be dispatched:
            // dblclick ::= click -> click -> dblclick
            .dblclick( function(event) {
                event.stopPropagation();
            });

            // insert into DOM
            $actions.append($actionElement);

            // preprocess action's requests
            preprocessRequests(action.requests);
        }

        // insert the actions into the DOM
        $actionlist.append($actions);

        // show them
        $actions.slideDown(200);

        // test parameters and results
        populateKeyValueTable($valueLog, transaction.valueLog);
    }

    // the on load setup
    $(document).ready( function() {
        init();

        var $progress = $('#progressmeter');

        try {
            $progress.show(100);

            loadJSON();

            // take care of the size of the content display area to
            // adjust it to the window size
            $(window).bind("resize", function(event) {
                resizeContent();
            });

            // setup onclick for the tabbed panel in the request content
            // area
            $('.tabs-nav li', $requestContent).click( function(event) {
                activateTab(this);
            });

            $('#wrapper').splitter({ orientation: 'horizontal', limit: 150 });

            // resize in the beginning already
            resizeContent();

            // activate first request-tab
            activateTab($('.tabs-nav li', $requestContent).get(0));

            // open the first action
            $('li.action > span.name', $actionlist).eq(0).click();
        }
        finally {
             $progress.hide(200);
        }
    });

})(jQuery);
