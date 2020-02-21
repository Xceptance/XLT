(function(){

    function navigate(target) {
        // does it contain a #
        var pos = target.lastIndexOf("#");

        if (pos >= 0)
        {   
            // is it the current document?
            var targetDocument = target.slice(0, pos);
            var targetHashText = target.slice(pos);

            var path = window.location.pathname.split( '/' );
            var currentDocument = path[path.length - 1];

            if (targetDocument == currentDocument || targetDocument == "")
            {
                // before we run it, check that this exists 
                if (targetHashText.length > 0) {
                    // quote any "." in the hash, otherwise JQuery interprets the following chars as class 
                    targetHashText = targetHashText.replace(/\./g, "\\.");
                    $.scrollTo(targetHashText, 250, {easing:'swing', offset: {top: -35}}); 
                    return false;
                }
            }
        }

        return true;
    }

    function scrollTo() {
        navigate(window.location.hash);
    }

    // start with on document ready and prep the functionality
    $(document).ready( function () {
        // setup menu
        $('#superfish').superfish({delay:0, autoArrows:false, speed:'fast'}); 

        // setup scrolling magic for navigation and summary tables
        $('#navigation a, table a').click( function() {
                navigate($(this).attr('href'))
        });

        // setup click handler to scroll to the top of the page when clicking the navigation bar
        $('#navigation').click( function(event) {
            // handle direct click events only, but not events that bubbled up 
            if (event.target.id == this.id) {
                $.scrollTo(0, 250, {easing:'swing'});
            }
        });

        // setup the tables
        Table.auto();

        // the table filters
        $('table input.filter').click(function(event)
        {
            // ensure that clicking the input will not resort the table
            event.stopPropagation();
        }).keyup(function(event)
        {
            var filterPhrase = event.target.value;

            // the filter function, returns true if the value is to be shown
            var doFilter = function(value, filterPhrase)
            {
                // request table cells contain the URLs as well, so cut them off
                value = value.trim().split('\n')[0];

                // split filter phrase into filters
                var filters = filterPhrase.split('|');

                // check each filter                    
                var visible = false;
                for (var f = 0; f < filters.length && !visible; f++)
                {
                    var substrings = filters[f].split(' ');
                    var substring;
                    var filterResult = true;

                    // try each filter substring
                    for (var s = 0; s < substrings.length; s++)
                    {
                        substring = substrings[s];
                        if (substring.length > 0)
                        {
                            if (substring.charAt(0) == '-')
                            {
                                // substrings that start with minus must NOT be present 
                                substring = substring.slice(1);
                                if (substring.length > 0 && value.indexOf(substring) != -1)
                                {
                                    filterResult = false;
                                    break;
                                }
                            }
                            else
                            {
                                // substring must be present
                                if (value.indexOf(substring) == -1)
                                {
                                    filterResult = false;
                                    break;
                                }
                            }
                        }
                    }
                    
                    visible = visible || filterResult;
                }

                return visible;
            };

            // actually perform filtering a table by a filter phrase
            var filterTable = function(table, filterPhrase) {
                $('input.filter', table).each(function() {
                    Table.filter(this, { 'filter': function(value) { return doFilter(value, filterPhrase); }});
                });
            };

            // shows/hides the footer row of a table
            var showTableFooter = function(table, footerVisible) {
                $('tfoot', table).toggle(footerVisible);
            };

            // get the target/foreground table
            var table = $(this).parents("table");

            // let the table filter the rows
            filterTable(table, filterPhrase);

            // show the table footer only if no body rows have been filtered out
            var footerVisible = $('tbody tr:hidden', table).size() == 0;
            showTableFooter(table, footerVisible);

            // now process any hidden table (Requests page only)
            $('table:hidden').each(function() {
                filterTable(this, filterPhrase);
                showTableFooter(this, footerVisible);
                // set the current filter phrase as the filter input's value
                $('input.filter', this).val(filterPhrase);
            });

            // now filter the charts
            $('.charts .chart-group').each(function()
            {
                var value = $(this).attr('data-name');
                var visible = doFilter(value, filterPhrase);

                $(this).toggle(visible);
            });
        });

        // get the tabs set up
        try {
            $('.tabs').tabs();
        }
        catch (e) {
            // in case we so not have tabs
        }

        // lazy load the chart images to speed up the site
        $('div.charts div.chart-group').each( function() {
            var chartGroup = this;

            $('ul', this).each( function() {
                var counter = 0;

                $('a', this).each( function() {
                    // get the href
                    var id = $(this).attr('href');

                    // get the original image img
                    var img = $(id + '> div.chart > img', chartGroup);

                    if (counter != 0) {
                        $(this).click( function() {
                                img.attr('src', img.attr('alt'));
                        });
                    } else {
                        // first tab, show immediately
                        img.attr('src', img.attr('alt'));
                    }

                    counter++;
                });
            });

            // set click handler of back links in order to scroll to right position in document
            $('a.backlink', this).click( function() {
                var timerId  = $(this).attr('href').substring(7),
                    selector = '.content a[data-timer-id=' + timerId + ']:visible',
                    target   = $(selector).get(0);

                $.scrollTo(target, 250, {easing:'swing', offset: {top: -35}}); 
            });
        });

        // the tool tips
        // #request-summary .section > div .content > div .data > table #TABLE_1 .table-autosort:0 table-autostripe table-stripeclass:odd > tbody > tr . > td .key > a
        $('#request-summary table td.key a.cluetip').cluetip( {
                hoverIntent: {
                    sensitivity:  1,
                    interval:     250,
                    timeout:      250
                },
                mouseOutClose: true,
                closeText: 'x',
                closePosition: 'title',
                sticky: true,
                local: true,
                showTitle: false,
                dropShadow: false,
                positionBy: 'mouse',
                clickThrough: true,
                attribute: 'data-rel'
        });

        // the tool tips
        $('table th.cluetip').cluetip( {
            hoverIntent: {
                sensitivity:  1,
                interval:     250,
                timeout:      250
            },
            mouseOutClose: true,
            closeText: 'x',
            closePosition: 'title',
            sticky: true,
            local: true,
            hideLocal: true,
            showTitle: false,
            dropShadow: false,
            positionBy: 'mouse',
            titleAttribute: '',
            attribute: 'data-rel'
        } );

        // the collapsible stack traces
        $(".collapsible").each( function() {
            // the first child is the expand/collapse trigger 
            $(this).children(":first").addClass("collapsible-collapsed").click( function() {
                // restyle the trigger element
                $(this).toggleClass("collapsible-collapsed");
                $(this).toggleClass("collapsible-expanded");

                // the next sibling is the element to show/hide 
                $(this).next().toggle();
            });
        });

        // add double-click handler to tab headers which switches all tabs at once
        $(".charts div.tabs ul li").each( function() {
            // find the index of the current tab in the current tab pane
            var i = $("li", $(this).parent()).index(this) + 1;

            // add a handler that switches all tabs with the same index
            $("a", $(this)).dblclick(function() 
            {
                // activate all tabs with the same index by simulating a single click
                $(".charts div.tabs ul li:nth-child(" + i + ") a").click(); 
            });
        });

        // transform URLs in event messages to anchors
        $('#event-summary #TABLE_3 tbody tr td.text').each(function(){ 
            var $this = $(this);
            $this.html( $this.html().replace(/(https?:\/\/[^\s]+)/gi, "<a href='$1' target='_blank'>$1</a>") )
        });

        // see if we jumped and now have to scroll
        scrollTo();
    });  
})()