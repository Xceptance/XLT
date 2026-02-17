/*
 *  Based on crosshair.js - v0.1.0
 *  https://github.com/eschmar/crosshair
 */
 
(function ($) {
    // constructor
    function Plugin(element){
        $(element).wrap('<div class="crosshair"></div>');
        this.element = $(element).parent();
        this.init();
    }

    Plugin.prototype = {
        init: function() {
            var app = this;
            this.spawnCrosshair();

            // hide crosshair onmouseleave
            this.element.hover(function() {
                app.element.find('.hair').show();
            }, function() {
                app.element.find('.hair').hide();
            });
        },

        spawnCrosshair: function() {
            this.element.append('<div class="hair hair-vertical"></div>');
            this.element.append('<div class="hair hair-horizontal"></div>');

            this.initCrosshair();
        },

        initCrosshair: function() {
            var app = this;
            $(this.element).on('mousemove touchmove', function(event) {
                // calculate relative position
                var offset, left, top;
                offset = app.element.offset();
                left = event.pageX - offset.left;
                top = event.pageY - offset.top;

                // update position
                app.element.find('.hair.hair-horizontal').css('top', top);
                app.element.find('.hair.hair-vertical').css('left', left);

                event.stopPropagation();
            });
        }
    }

    // lightweight plugin wrapper, preventing against multiple instantiations
    $.fn["crosshair"] = function () {
        return this.each(function() {
            if (!$.data(this, "crosshair")) {
                $.data(this, "crosshair", new Plugin(this))
            };
        });
    };
    
    $(function(){
        $('.chart').crosshair();
    });
})( jQuery );
