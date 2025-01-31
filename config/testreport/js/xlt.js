(function($){

    function navigate(target) {
        // does it contain a #
        var pos = target.lastIndexOf("#");

        if (pos >= 0) {
            // is it the current document?
            var targetDocument = target.slice(0, pos);
            var targetHashText = target.slice(pos);

            var path = window.location.pathname.split( '/' );
            var currentDocument = path[path.length - 1];

            var hashObj = splitHash(targetHashText);
            targetHashText = hashObj.navigation;

            if(targetHashText != undefined){
                if (targetDocument == currentDocument || targetDocument == "") {
                    // before we run it, check that this exists 
                    if (targetHashText.length > 0) {
                        // quote any "." in the hash, otherwise JQuery interprets the following chars as class 
                        targetHashText = targetHashText.replace(/\./g, "\\.");
                        $.scrollTo(targetHashText, 250, {easing:'swing', offset: {top: -35}}); 
                        return false;
                    }
                }
            }            
        }

        return true;
    }

    function scrollTo() {
        navigate(window.location.hash);
    }

    // the filter function, returns true if the value is to be shown
    function doFilter(value, filterPhrase) {
        // request table cells contain the URLs as well, so cut them off
        value = value.trim().split('\n')[0];

        // split filter phrase into filters
        var filters = filterPhrase.split('|');

        // check each filter
        var visible = false;
        for (var f = 0; f < filters.length && !visible; f++) {
            var substrings = filters[f].split(' ');
            var substring;
            var filterResult = true;

            // try each filter substring
            for (var s = 0; s < substrings.length; s++) {
                substring = substrings[s];
                if (substring.length > 0) {
                    if (substring.charAt(0) == '-') {
                        // substrings that start with minus must NOT be present
                        substring = substring.slice(1);
                        if (substring.length > 0 && value.indexOf(substring) != -1) {
                            filterResult = false;
                            break;
                        }
                    }
                    else {
                        // substring must be present
                        if (value.indexOf(substring) == -1) {
                            filterResult = false;
                            break;
                        }
                    }
                }
            }

            visible = visible || filterResult;
        }

        return visible;
    }

    // recalculateFilteredFooter: computes the content of the footer to be displayed when tables are filtered.
    // It is expected that the row to be filled is already part of the DOM (but potentially not displayed),
    // see config/xsl/common/util/filtered-footer-row.xsl
    function recalculateFilteredFooter(table, filteredFooterRow, staticFooterRow) {
        // Go through the table header (with expanded rowspans and colspans)
        // and join the text contents of the <th>'s in each column with '\n' in between
        // to determine column descriptions
        function determineColumnDescriptions() {
            function joinNonEmptyTextsInColumn(columnIndex, array, separator) {
                return (array || []).map(function(a) { return a[columnIndex] })
                       .filter(function(a){ return a !== ''})
                       .join(separator);
            }

            // Return the text contents of <th>'s in the header of a table while expanding rowspans and colspans
            // (rowspans are expanded by inserting empty strings in following rows,
            //  colspans are expanded by duplicating the text content in following columns).
            // The results are returned in a two-dimensional array of strings
            function textContentsOfExpandedHeaderCells() {
                function spanAttributeToNumber(spanAttribute) {
                    return spanAttribute ? Number(spanAttribute) : 1;
                }

                var headerRows = table.find('thead > tr');
                var numberOfHeaderRows = headerRows.length;

                var resultArray = [];
                for(var i = 0; i < numberOfHeaderRows; i++) {
                    resultArray.push([]);
                }

                for (var rowIndex = 0; rowIndex < numberOfHeaderRows; ++rowIndex) {
                    var currentResultRow = resultArray[rowIndex];
                    var columnIndex = 0;

                    // iterate through the <th> cells in the current row
                    headerRows.eq(rowIndex).children('th').each( function() {
                        var textContent = this.textContent.trim();

                        // skip over result entries that have already been filled
                        // (while expanding rowspans in previous rows)
                        while (currentResultRow[columnIndex] !== undefined) ++columnIndex;

                        var rowSpanEnd = rowIndex + spanAttributeToNumber(this.getAttribute('rowspan'));
                        var colSpanEnd = columnIndex + spanAttributeToNumber(this.getAttribute('colspan'));

                        // expand rowspans with empty strings in the following rows (if applicable)
                        for(var i = rowIndex+1; i < rowSpanEnd; i++) {
                            for(var j = columnIndex; j < colSpanEnd; j++) {
                                resultArray[i][j] = '';
                            }
                        }

                        // expand colspans by duplicating the textContent
                        while (columnIndex < colSpanEnd) {
                            currentResultRow[columnIndex] = textContent;
                            ++columnIndex;
                        }
                    });
                }

                return resultArray;
            }

            var headerTexts = textContentsOfExpandedHeaderCells(table);
            var numberOfColumns = headerTexts[0].length;
            var columnDescriptions = [];

            for (var columnIndex = 0; columnIndex < numberOfColumns; ++columnIndex) {
                columnDescriptions.push(joinNonEmptyTextsInColumn(columnIndex, headerTexts, '\n'));
            }

            return columnDescriptions;
        }

        // build htmlString for the description (i.e. leftmost) cell in the filtered footer row
        function buildFilteredFooterDescriptionCell(staticFooterDescriptionCell, skipTotalsCalculation, numberOfMatchingEntries) {
            var clazz = staticFooterDescriptionCell.getAttribute('class');
            if(!/\bkey\b/.test(clazz)) {
                clazz += ' key';
            }

            var text = '(filtered, ' + numberOfMatchingEntries + " matching entries)";
            if (skipTotalsCalculation ) {
                if(numberOfMatchingEntries === 1) {
                    text = '(filtered, 1 matching entry)';
                }
            }
            else {
                // take the static footer description text (usually 'Totals (# entries)')
                // and replace the text in parentheses to reflect the number of matching entries
                // (plus add 'filtered, ' - so there is no chance of misinterpreting the totals values)
                text = staticFooterDescriptionCell.textContent.trim().replace(/\(\d+\s\w+\)|$/, text);
            }

            return '<td class="' + clazz.trim() + '">' + text + '</td>';
        }

        // build htmlString for a cell in the filtered footer
        // (containing the filtered totals value for that column, if applicable)
        function buildTotalsCellForColumn(columnIndex, columnDescription, displayedRows, staticFooterCell, totalsColumnIdx) {

            // reduce each substring consisting of whitespace characters to a single space
            function normalizeClassAttributeValue(classAttributeValue) {
                return classAttributeValue.replace(/\s+/g, ' ').trim();
            }

            function decimalPlaces(numberString) {
                var indexOfDecimalPoint = numberString.lastIndexOf('.');
                return indexOfDecimalPoint < 0 ? 0 : numberString.length - indexOfDecimalPoint - 1;
            }

            function numberWithCommas(numberString) {
                var parts = numberString.split('.');
                parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ','); // thanks to http://stackoverflow.com/a/2901298
                return parts.join('.');
            }

            function determineTotalsFunctionBy(aColumnDescription, aMean) {
                var regexForColumnsWithMinTotal = /Min\.$/;
                var regexForColumnsWithMaxTotal = /Max\.$/;
                var regexForColumnsWithSumTotal = /(Total|Errors|1\/(s|min|h|d)\*?)$/;
                var regexForColumnsWithAverageTotal = /\sGC\s|CPU\s\[%\]/; // matches columns in Agents table

                function minimum(total, current) {
                    return Math.min(total, current);
                }

                function maximum(total, current) {
                    return Math.max(total, current);
                }

                function sum(total, current) {
                    return total + current;
                }

                var func;
                if (aMean || regexForColumnsWithSumTotal.test(aColumnDescription)) {
                    func = function(array) { return array.reduce(sum, 0); };
                }
                else if (regexForColumnsWithMinTotal.test(aColumnDescription)) {
                    func = function(array) { return array.reduce(minimum, Number.POSITIVE_INFINITY); };
                }
                else if (regexForColumnsWithMaxTotal.test(aColumnDescription)) {
                    func = function(array) { return array.reduce(maximum, Number.NEGATIVE_INFINITY); };
                }
                else if (regexForColumnsWithAverageTotal.test(aColumnDescription)) {
                    func = function(array) { return array.reduce(sum, 0) / array.length; };
                }
                return func;
            }

            function cleanCellText($cell) {
                return $cell.text().replace(/,|\s/g, '');
            }

            // Take the class attribute of the static footer by default
            var clazz = staticFooterCell.attr('class') || '';
            var isMean = /\bMean\b/.test(columnDescription) && totalsColumnIdx > -1;
            var totalsFunction = determineTotalsFunctionBy(columnDescription, isMean);

            var maxDecimalPlaces = 0;
            var additionalClass = '';
            var displayedColumnValues = [];

            // But remove 'error'/'event' and coloring for now
            // (we will only include those if there are actual errors / events currently displayed)
            clazz = normalizeClassAttributeValue(clazz.replace(/\s*(error|event|[np]\d+|infinity)\s*/g, ' '));

            if (totalsFunction === undefined) {
                // if we cannot calculate a totals value, just use an empty cell
                return '<td class="' + clazz + '"/>';
            }

            var count, countsPerRow;
            if(isMean) {
                count = 0;
                countsPerRow = new Array(displayedRows.length);
                displayedRows.each( function(i) {
                    var val = Number(cleanCellText($(this).find('td').eq(totalsColumnIdx)));
                    countsPerRow[i] = val;
                    count = count + val;
                });
            }

            displayedRows.each( function(i) {
                var cell = $(this).find('td').eq(columnIndex),
                    cellTextWithoutCommasAndSpaces = cleanCellText(cell),
                    cellVal = Number(cellTextWithoutCommasAndSpaces);

                if(isMean) {
                    cellVal = (countsPerRow[i] / count) * cellVal;
                }

                displayedColumnValues.push(cellVal);
                maxDecimalPlaces = Math.max(maxDecimalPlaces, decimalPlaces(cellTextWithoutCommasAndSpaces));

                // if any of the displayed cells is colored using the 'error' or 'event' class,
                // we want the same to be true for the filtered total cell
                if (cell.hasClass('error')) {
                    additionalClass = 'error';
                }
                else if (cell.hasClass('event')) {
                    additionalClass = 'event';
                }
            });

            var totalsValue = totalsFunction(displayedColumnValues);

            // need to get rid off decimal places introduced through floating point arithmetic errors
            totalsValue = totalsValue.toFixed(maxDecimalPlaces);

            if(additionalClass) {
                clazz += (' ' + additionalClass);
            }

            return '<td class="' + clazz + '">' + numberWithCommas(totalsValue) + '</td>';
        }

        function lastIndexOf(arr, predicate, startIdx) {
            var idx;

            arr = arr || [];
            idx = arr.length - 1;

            if(typeof startIdx === 'number' && Number.isFinite(startIdx)){
                idx = Math.min(idx, startIdx);
            }

            while(idx > -1 && !predicate(arr[idx], idx, arr)) {
                --idx;
            }

            return idx;
        }

        var displayedRows = table.find('tbody > tr:not([style*="display: none"])');
        var numberOfMatchingEntries = displayedRows.length;

        // totals cannot be calculated for diff or trend reports AND we need at least two rows
        var skipTotalsCalculation =  numberOfMatchingEntries < 2 || !isLoadTestReport;

        var staticFooterCells = staticFooterRow.find('td');

        // start with the <td> element for the description (i.e. the leftmost cell)
        var filteredFooterRowContents = buildFilteredFooterDescriptionCell(staticFooterCells.get(0), skipTotalsCalculation, numberOfMatchingEntries);

        // now add the <td> elements for the value columns
        var columnDescriptions = skipTotalsCalculation ? [] // don't need column descriptions if we don't calculate totals
            : determineColumnDescriptions();
        var numberOfColumns = staticFooterCells.length;
        var descDenotesTotalCol = function(desc) {
              return /(Bytes\s+(Received|Sent)|Count)\s+Total$/.test(desc);
        };

        // Skip column 0, which contains row descriptions
        for (var columnIndex = 1; columnIndex < numberOfColumns; ++columnIndex) {
            var totalsColIdx = lastIndexOf(columnDescriptions, descDenotesTotalCol, columnIndex-1);
            filteredFooterRowContents +=
                buildTotalsCellForColumn(columnIndex, columnDescriptions[columnIndex], displayedRows, staticFooterCells.eq(columnIndex), totalsColIdx);
        }

        // Replace the contents of the filtered footer row with the newly calculated stuff
        filteredFooterRow.html(filteredFooterRowContents);
    }

    function filter (input) {
        var $input       = $(input),
            table        = $input.parents("table"), // get the target/foreground table
            filterPhrase = $input.val();

        var filterFunc = function(value) { return doFilter(value, filterPhrase) };

        // actually perform filtering a table by a filter phrase
        var filterTable = function(table) {
            table.find("input.filter").each(function() {
                Table.filter(this, { 'filter': filterFunc });
            });
        };

        // shows/hides the footer row of a table
        var showTableFooter = function(table, footerVisible) {
            var footer = table.find('tfoot');
            var staticFooterRow = footer.find('tr:not(.filtered)');
            var filteredFooterRow = footer.find('tr.filtered');

            staticFooterRow.toggle(footerVisible);

            if (filteredFooterRow.length == 0) {
                return;
            }

            if (!footerVisible) {
                recalculateFilteredFooter(table, filteredFooterRow, staticFooterRow);
                filteredFooterRow.toggle(true);
            }
            else {
                filteredFooterRow.toggle(false);
                filteredFooterRow.empty();
            }
        };

        var footerVisible;

        // let the table filter the rows
        filterTable(table);

        // show the table footer only if no body rows have been filtered out
        footerVisible = table.find('tbody tr:hidden').length == 0;

        showTableFooter(table, footerVisible);

        // now process any hidden table (Requests page only)
        $('table:hidden').each(function() {
            var $this = $(this);
            filterTable($this);
            showTableFooter($this, footerVisible);
            // set the current filter phrase as the filter input's value
            $this.find('input.filter').val(filterPhrase);
        });

        // now filter the charts
        $('.charts:not(.overview) .chart-group.no-print').each(function() {
            var value = this.getAttribute('data-name');
            var visible = filterFunc(value);

            $(this).toggle(visible);
        });
    }

    var timeouter;
    function throttleFilter(input){
        if (!timeouter){
            timeouter = window.setTimeout(function(){
                filter(input);
                timeouter = undefined;
            }, 200);
        }
    }

    var isLoadTestReport;

    // start with on document ready and prep the functionality
    $(function() {

        isLoadTestReport = !!document.getElementById('loadtestreport');

        // setup scrolling magic for navigation and summary tables
        (function setupScrollingMagic() {
            $('table a, nav a, .chart .error .backLink').click( function() {
                navigate(this.getAttribute('href'));
            });
        })();

        // setup click handler to scroll to the top of the page when clicking the navigation bar
        (function setupBackToTopHandler() {
            $('nav').click( function(event) {
                $.scrollTo(0, 250, {easing:'swing'});
                // if there is an anchor remove it from the hash 
                if(window.location.hash != '')
                {
                    var newHashObj = splitHash(window.location.hash);
                    newHashObj.navigation = '';
                    updateHash(newHashObj);
                }
            });
            // stop stopPropagation
            $('nav li a').click( function(event) {
                // avoid that the back to top handler kicks in
                event.stopPropagation();
            });
        })();

        // setup the tables
        (function setupTables() {
            Table.auto();

            // hide loader and show content after HTML for table is build (only if present)
            const progressmeter = document.getElementById("progressmeter");
            if (progressmeter) {
                progressmeter.classList.add("hidden");
            }

            const hiddenContent = document.querySelector(".content.hidden");
            if (hiddenContent) {
                hiddenContent.classList.remove("hidden");
            }

            // We have to wait for the table.js to finish processing before registering events and
            // may be calling switchToTargetTabIfRequired in sort
            if (document.readyState === 'complete') {
                // console.log("load already done");
                registerSortAndFilterListeners();
            }
            else {
                // console.log("attach event listener");
                window.addEventListener("load", registerSortAndFilterListeners);
            }
        })();

        // the table filters
        (function setupTableFilters() {
            var filterInputs = $('table:not(.copy) input.filter');

            filterInputs.click(function(event) {
                // ensure that clicking the input will not resort the table
                event.stopPropagation();
            }).keyup(function() {
                throttleFilter(this);
            });

            // clear the input
           filterInputs.each(function() {
               var input = this,
                   $input = $(this);
               $input.next(".clear-input").click(function(){
                   // neither perform any default button click handling nor propagate click event any further
                   event.preventDefault();
                   event.stopPropagation();
                   $input.val("");
                   throttleFilter(input);
               });
           });
        })();

        //simulate click on Requestspage
        (function setupStickyTableHeads() {
            $("#tabletabbies ul > li:first").click();
        })();

        // get tabs
        (function setupTabs() {
            $('div.c-tabs').each( function() {
                var id = this.getAttribute('id');
                tabs({
                    el: '#'+id,
                    tabNavigationLinks: '.c-tabs-nav-link',
                    tabContentContainers: '.c-tab'
                })
                .init();
            });
        })();

        // lazy load the chart images to speed up the site
        (function setupChartGroups() {
            $('div.charts div.chart-group').each( function() {
                var $this = $(this),
                    $tabLines = $this.find("ul > li"),
                    $images = $this.find("img");

                $tabLines.each( function(index) {
                    // get the corresponding image
                    var img = $images.eq(index);

                    if (index != 0) {
                        $(this).click( function() {
                            img.attr('src', img.attr('alt'));
                        });
                    }
                });

                // set click handler of back links in order to scroll to right position in document
                $('a.backlink', this).click( function() {
                    var targetId = this.getAttribute('href').substring(1),
                        selector = '.content a[data-id=' + targetId + ']:visible',
                        target   = $(selector).get(0);

                    $.scrollTo(target, 250, {easing:'swing', offset: {top: -120}});
                });
            });
        })();

        //lazy load the print images to speed up the site
        //they will only be loaded when printing is triggered
        (function setupPrintImages() {
            // selector
            var unloadedPrintImgs = ".chart-group.print > .chart > img:not(.load)";
            var parallelExecution = 8;

            // lazy load one image after the other
            function lazyLoadPrintImgs() {
                var elem = $(unloadedPrintImgs).first();

                elem.addClass("load").on("load", function(event) {
                    $(this).addClass("done");
                    lazyLoadPrintImgs();
                }).on("error", function(event) {
                    $(this).addClass("error");
                    lazyLoadPrintImgs();
                }).attr('src', elem.attr('alt'));
            }

            // load all remaining images when printing is triggered
            $(window).on('beforeprint', function() {
                // trigger parallel execution
                for(var i=0; i<parallelExecution; i++) {
                    lazyLoadPrintImgs();
                }
                if ($(unloadedPrintImgs).length > 0) {
                    alert("Required images for printing are still loading. Please close print dialog and try again later.");
                }
            });
        })();

        // the tool tips
        // #request-summary .section > div .content > div .data > table #TABLE_1 .table-autosort:0 table-autostripe table-stripeclass:odd > tbody > tr . > td .key > a
        (function setupUrlLists() {
            //mouseover handler on cluetip anchor to show tooltip on hover, does nothing on mouseout
            $("#request-summary table td.key a.cluetip").hoverIntent({
                over: function(e) {
                    //clone and append the tooltip with the corresponding data-rel attribute to hovered element
                    var dataRel = $(this).attr('data-rel');
                    var tooltip = $(dataRel).clone();
                    if (!$(this).parent().children('.cluetip-data').length) {
                        tooltip.appendTo($(this).parent());
                    };
                    $(this).siblings('.cluetip-data').css('left', e.pageX + 10);
                    $(this).siblings('.cluetip-data').addClass("is-active");
                },
                out: function() {},
                timeout: 250,
                interval: 250,
                sensitivity: 1
            });
            //seperate mouseout handler on parent element of cluetip anchor to remove "is-active" class, does nothing on mouseover
            $("#request-summary table td.key a.cluetip").parent().hoverIntent({
                over: function() {},
                out: function() {
                    $(this).children('.cluetip-data').removeClass("is-active");
                },
                timeout: 250,
                sensitivity: 1
            });
        })();

        // the tool tips for trendReport
        (function setupUrlLists_trendReport() {
            $('table th.cluetip').hoverIntent({
                //append tooltip div to table th on mouseover
                over: function(e){
                    var dataRel = $(this).attr('data-rel');
                    var tooltip = $(dataRel).clone();
                    tooltip.appendTo($(this));
                    $(this).children('.cluetip-data').css('left', e.pageX + 10);
                    $(this).children('.cluetip-data').addClass("is-active");
                },
                //remove tooltip div on mouseout
                out: function(){
                    $(this).children('.cluetip-data').remove();
                },
                timeout: 250,
                interval: 250,
                sensitivity: 1
            });
        })();

        // the collapsible stack traces
        (function setupCollapsibles() {
            $(".collapsible").each( function() {
                // the first child is the expand/collapse trigger
                $(this).children(".collapse").addClass("collapsible-collapsed").click( function() {
                    // restyle the trigger element
                    $(this).toggleClass("collapsible-collapsed");
                    $(this).toggleClass("collapsible-expanded");

                    // the next sibling is the element to show/hide
                    $(this).next().toggle();
                });
            });
        })();

        // add double-click handler to tab headers which switches all tabs at once
        (function setupDoubleclickHandlers() {
            $(".charts div.tabs > ul").each( function() {
                $(this).find("> li").each( function(index) {
                    // find the index of the current tab in the current tab pane
                    var i = index + 1;

                    // add a handler that switches all tabs with the same index
                    $(this).dblclick(function() {
                      $(".charts div.tabs ul li:nth-child(" + i + ")").click();
                      $(this).scrollTop();
                    });
                });
            });
        })();

        // transform URLs in event messages to anchors
        (function transformEventUrls() {
            $('#event-summary #TABLE_2 tbody tr td.text').each(function() {
                var $this = $(this);
                $this.html( $this.html().replace(/(https?:\/\/[^\s]+)/gi, "<a href='$1' target='_blank'>$1</a>") )
            });
        })();

        // see if we jumped and now have to scroll
        scrollTo();
    });

    // method that is called when the hash is updated. This happens if the user clicks on local anchors (table, charts), sorts tables by sortable table rows, updates the hash
    // directly in the URL or if the hash is updated via code
    function hashChanged(event){
        if(window.ignoreNextHashChange){
            window.ignoreNextHashChange = false;
        }
        else{
            // in some cases we have create a new hash out of a combination (old + new hash). For example, clicking on a request to get to the request charts totally wipes the hash.
            // therefore we have to restore the sorting option and filter if there were any provided previously
            var oldHashObj = splitHash(event.oldURL);
            var newHashObj = splitHash(event.newURL);

            // hashes might contain a sorting option
            if(oldHashObj.sort != undefined && newHashObj.sort == undefined){
                newHashObj.sort = oldHashObj.sort;
            }

            // hashes might contain a filter
            if(oldHashObj.filter != undefined && newHashObj.filter == undefined){
                newHashObj.filter = oldHashObj.filter;
            }

            updateHash(newHashObj);
        }
    }

    // splits the given hash - automatically tries to detect the current format. the returned hash object might contain a "navigation", "sort" and "filter" option
    function splitHash(hash){
        var hashObj = {};

        if(hash !== ""){
            // hash format: http://...#abc
            var pos = hash.lastIndexOf('#');
            if (pos >= 0) {
                hash = hash.slice(pos);
                // hash format: #abc
                if(hash.startsWith('#')){
                    hash = hash.split('#')[1];
                }

                var split = hash.split('&');
                for(var i = 0; i < split.length; i++){
                    var param = split[i];
                    if(param.startsWith('sort')){
                        hashObj.sort = param;
                    }
                    else if(param.startsWith('filter')){
                        hashObj.filter = param;
                    }
                    else{
                        hashObj.navigation = '#' + param;
                    }
                }
            }
        }

        return hashObj;
    }

    // updates the URL hash to the parameters passed in updateHashObj. This change is only applied if the hashObj is different from the current hash.
    // if the update is applied a hashchanged event is fired which then calls "hashChanged"
    function updateHash(updatedHashObj){
        var newHash = [];

        // check the possible parameters for the hash
        if(updatedHashObj.navigation != undefined){
            newHash.push(updatedHashObj.navigation);
        }

        if(updatedHashObj.sort != undefined){
            newHash.push(updatedHashObj.sort);
        }

        if(updatedHashObj.filter != undefined){
            newHash.push(updatedHashObj.filter);
        }

        // check if we have a hash to process
        if(newHash.length > 0){
            // create the new hash string -> filter out empty elements (required for removal of anchors in the hash)
            var newJoinedHash = newHash.filter(n => n).join('&');
            if(newJoinedHash.startsWith('#') == false){
                newJoinedHash = '#' + newJoinedHash;
            }

            // check if the hash is different: only then update it
            if(window.location.hash != newJoinedHash){
                // updated hash to params
                window.location.hash = newJoinedHash;
            }
        }
    }

    // eventlistener that fires if a sortable table row gets clicked
    function updateHashAfterSort(sortingEvent){
        if(sortingEvent.target.classList.contains('table-sortable') && sortingEvent.target.id != undefined){
            var sortingRule = sortingEvent.target.classList.contains('table-sorted-asc') ? 'asc' : 'desc';
            // Get the current hash (if one exists)
            var hashObj = splitHash(window.location.hash);

            // update the sorting option of the hash
            hashObj.sort = sortingEvent.target.id + '=' + sortingRule;

            // After sorting we update the hash manually, so we disable executing the next event
            window.ignoreNextHashChange = true;

            // trigger the hash change
            updateHash(hashObj);
        }
    }

    // switches to the given tab if the current one is different
    function switchToTargetTabIfRequired(targetTab){
        var requestPageActiveTab = document.querySelector('#tabletabbies > .c-tab[id].c-is-active');
        if(requestPageActiveTab != null){
            var currentTabId = requestPageActiveTab.getAttribute('id');

            var targetTabId = targetTab.getAttribute('id');

            // if the current tab is different from the target tab containing the sorting option switch tabs
            if(currentTabId != targetTabId){
                document.querySelector('#tabletabbies .c-tabs-nav-link a[href=\'#' + targetTabId + '\']').click()
            }
        }
    }

    // sorts the passed table row by the given rule -> either ascending (asc) or descending (desc). Invalid options or elements trigger an alert and are ignored
    function sort(elem, rule) {
        if(elem != null){
            if(rule == 'asc' || rule == 'desc'){
                // if users are on the request page we need to check in which tab the target sorting option is located
                var targetTab = elem.closest('.c-tab[id]');
                switchToTargetTabIfRequired(targetTab);

                var classList = elem.classList;
                // only sort if the sorting rule is not already applied on the element
                if(classList.contains("table-sorted-" + rule) == false){
                    while(elem.classList.contains("table-sorted-" + rule) == false){
                        // Click sorting
                        elem.click();
                    }
                }
            }
            else{
                alert('Sorting only suppports \'asc\' or \'desc\' as parameter')
            }
        }
        else{
            alert('Target element for sorting does not exist');
        }
    }

    // method that gets triggered when the user enters some input to apply a filter
    function updateHashAfterFilter(filterEvent){
        var filter = filterEvent.target.value;
        var encodedFilter = encodeURIComponent(filterEvent.target.value);
        // console.log('filter change:' + filter + ' to ' + encodedFilter);

        var newHashObj = splitHash(window.location.hash);
        newHashObj.filter = 'filter=' + encodedFilter;

        updateHash(newHashObj);
    }

    // $(window).on( "load", function(){
    function registerSortAndFilterListeners(){
        // Prepare Hash Monitoring

        // once everything is loaded check whether there is a sorting rule passed
        var hashObj = splitHash(window.location.hash);
        if(hashObj.sort != undefined){
            // Perform initial sorting
            var sortParam = hashObj.sort.split('=');
            var sortingElem = document.getElementById(sortParam[0]);
            var sortingRule = sortParam[1];
            sort(sortingElem, sortingRule);
        }

        // check for existing filter to apply
        if(hashObj.filter != undefined){
            // Apply initial filter
            var filterParam = hashObj.filter.split('=');
            var encodedFilter = filterParam[1];

            if(encodedFilter.length > 0){
                var decodedFilter = decodeURIComponent(encodedFilter);
                // console.log('filter value: ' + decodedFilter);
                var filterInputFields = document.querySelectorAll('input.filter');
                for(var i = 0; i < filterInputFields.length; i++){
                    var filterField = filterInputFields[i];
                    filterField.value = decodedFilter;
                    filter(filterField);
                }
            }
        }

        // Register sorting listeners
        var sortableTableRows = document.getElementsByClassName('table-sortable');
        for(var i = 0; i < sortableTableRows.length; i++){
            sortableTableRows[i].addEventListener('click', updateHashAfterSort);
        }

        // Register filter listeners
        var filterInputFields = document.querySelectorAll('input.filter');
        for(var i = 0; i < filterInputFields.length; i++){
            // filterInputFields[i].addEventListener('input', updateHashAfterFilter);
            filterInputFields[i].addEventListener('focusout', updateHashAfterFilter);
        }

        // Variable that prevents triggering the hash update twice because of hash modifications inside updateHash
        window.ignoreNextHashChange = false;

        // Listeners applied, hook on the hashchange event
        window.addEventListener('hashchange', hashChanged);
    //});
    }

})(jQuery)

/*
 Attach a scroll listener for our read header that changes size when
 scrolled to make room but preserve information
 */
document.addEventListener ('scroll', function() {
    // this works, because we have an element header in the HTML which
    // is automatically selected here as the variable header, fancy!

    // the two values are meant to avoid flickering in case of edge case
    var sticky = header.classList.contains("sticky") ? 70 : 120;

    if (document.documentElement.scrollTop > sticky) {
        header.classList.add("sticky");
    }
    else {
        header.classList.remove("sticky");
    }
});