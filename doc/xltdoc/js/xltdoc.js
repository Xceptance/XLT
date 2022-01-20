// Generate the TOC elements with http://projects.jga.me/toc/
function createToc (selectedHeadlines, classesTarget, classes ) {
	$('#toc').toc({
		'selectors': selectedHeadlines, //elements to use as headings
		'container': 'body', //element to find all selectors in
		'prefix': 'toc', //prefix for anchor tags and class names
		'scrollToOffset': 80, //offset for the scrollTo target
	    'highlightOnScroll': false, //add class to heading that is currently in focus
		'highlightOffset': 0 //offset to trigger the next headline
	});

	// add classes to newly generated toc to enable bootstrap styling
	$( classesTarget ).addClass( classes );
}

// calculate the visible height of an element
// http://stackoverflow.com/a/29944927/33229
$.fn.visibleHeight = function() {
    var elBottom, elTop, scrollBot, scrollTop, visibleBottom, visibleTop;
    scrollTop = $(window).scrollTop();
    scrollBot = scrollTop + $(window).height();
    elTop = this.offset().top;
    elBottom = elTop + this.outerHeight();
    visibleTop = elTop < scrollTop ? scrollTop : elTop;
    visibleBottom = elBottom > scrollBot ? scrollBot : elBottom;
    return visibleBottom - visibleTop
}

// Resize side navigation height
function resizeSidenav () {
	var headerHeight;

	if ($('#sidenav').hasClass("headroom--top")) {
		headerHeight =
					$("#breadcrumb").visibleHeight() +
		 			$("#header").visibleHeight();
	}
	else {
		headerHeight = document.getElementById("breadcrumb").scrollHeight;
	}

	//calculate and set the new sidenav height
	var sidenavHeight = $(window).height() - headerHeight;
	if (document.getElementById("sidenav")) {
		document.getElementById("sidenav").style.height = sidenavHeight + "px";
	}


}


// Add attributes to prepare images for lightbox (http://lokeshdhakar.com/projects/lightbox2/)
function prepareImg () {
	$( "p.illustration > a > img" ).each(function() {

		var title = $( this ).attr('title');

		$( this.parentNode ).attr('data-title', title).attr('data-lightbox', "XLT");
		});
}


// When document is ready
$(document).ready(function() {
	prepareImg ();

	// bootstrap table styles
	$("table").addClass("table");

	// http://wicky.nillia.ms/headroom.js - Add classes to hide header on scroll down
	(function() {
		var breadcrumb = new Headroom(document.querySelector("#breadcrumb"), {
			offset : 82,
			classes : {
				notTop : 'navbar-fixed-top'
			}
		});
		breadcrumb.init();

		if (document.getElementById("sidenav")) {
			var sidenav = new Headroom(document.querySelector("#sidenav"), {
				offset : 82,
				classes : {
				  top : 'headroom--top',
				  notTop : 'headroom--not-top',
				},
				onTop : resizeSidenav,
				onNotTop : resizeSidenav
			});
			sidenav.init();
		}

	}());

	resizeSidenav ();

	// fix legacy plain in the code with bash until completely edited out
	// $(".plain").removeClass("plain").addClass("bash");

	hljs.configure({
	});
	hljs.initHighlighting();

	// fix the scrolling with scrollspy, did not work... left like that for now
	/*$('#toc').on('activate.bs.scrollspy', function () {
		var element = $('#toc ul li.active');
		$(element).offset({ top: 10 });

		document.title = $(element).offset().top + '/' + $(element).position().top;
	});*/
});

$(window).resize(function() {
	resizeSidenav ();
});

$(window).scroll(function() {
	resizeSidenav ();
});
