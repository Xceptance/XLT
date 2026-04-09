var Site = {
	
	start: function(){
		
		if ($('kwick')) Site.parseKwicks();
		
		if ($('download')) Download.start();
		
		if ($('sidebar')){
			Site.appearText();
			if (!window.ie6) Site.makeShadow();
		}
	},
	
	parseKwicks: function(){
		var kwicks = $$('#kwick .kwick');
		var fx = new Fx.Elements(kwicks, {wait: false, duration: 200, transition: Fx.Transitions.quadOut});
		kwicks.each(function(kwick, i){
			kwick.addEvent('mouseenter', function(e){
				var obj = {};
				obj[i] = {
					'width': [kwick.getStyle('width').toInt(), 185]
				};
				kwicks.each(function(other, j){
					if (other != kwick){
						var w = other.getStyle('width').toInt();
						if (w != 105) obj[j] = {'width': [w, 105]};
					}
				});
				fx.start(obj);
			});
		});
		
		$('kwick').addEvent('mouseleave', function(e){
			var obj = {};
			kwicks.each(function(other, j){
				obj[j] = {'width': [other.getStyle('width').toInt(), 125]};
			});
			fx.start(obj);
		});
	},
	
	appearText: function(){
		var timer = 0;
		var sideblocks = $$('#sidebar li');
		
		var slidefxs = [];
		var colorfxs = [];
		
		sideblocks.each(function(el, i){
			el.setStyle('margin-left', '-155px');
			timer += 150;
			slidefxs[i] = new Fx.Style(el, 'margin-left', {
				duration: 400,
				transition: Fx.Transitions.backOut,
				wait: false,
				onComplete: Site.createOver.pass([el, i])
			});
			slidefxs[i].start.delay(timer, slidefxs[i], 0);

		}, this);
	},
	
	createOver: function(el, i){
		var first = el.getFirst();
		if (!first || first.getTag() != 'a') return;
		var overfxs = new Fx.Styles(first, {'duration': 200, 'wait': false});
		var tocolor, fromcolor;
		if (first.hasClass('big')){
			tocolor = '333';
			fromcolor = 'fff';
		} else {
			tocolor = 'faec8f';
			fromcolor = '595965';
		}
		el.mouseouted = true;
		el.addEvent('mouseenter', function(e){
			overfxs.start({
				'color': tocolor,
				'margin-left': 10
			});
		});
		el.addEvent('mouseleave', function(e){
			overfxs.start({
				'color': fromcolor,
				'margin-left': 0
			});
		});
	},
	
	makeShadow: function(){
		new Element('img').injectInside('container').setStyles({
			'position': 'absolute', 'top': '0', 'margin-top': '-30px', 'left': '644px', 'z-index': '999999'
		}).setProperties({
			'height': $('sidebar').offsetHeight + 70, 'width': '10', 'src': '/assets/images/menubig_shadow.png'
		});
	}
	
};

var Download = {

	start: function(){
		
		var compSlide = new Fx.Slide('compression', {duration: 500, transition: Fx.Transitions.quadOut, wait: false}).hide();
		
		$('compression-tog').addEvent('click', function(e){
			compSlide.toggle();
			new Event(e).stop();
		});
		
		Download.trs = $$('tr.option');
		
		Download.chks = $$('#download div.check');
		
		Download.radios = $$('#compression-options div.check');
		
		Download.prefs = $$('#preferences-options div.check');
		
		
		Download.fx = [];
		Download.parse();
		
		var allinputs = $$(Download.prefs, Download.chks, Download.radios);
		
		allinputs.each(function(chk){
			chk.inputElement = chk.getElement('input');
			chk.inputElement.setStyle('display', 'none');
		});
		
		allinputs.each(function(chk){
			if (chk.inputElement.checked) Download.select(chk);
		});
		
		Download.select(Download.chks[0]);
		
		Download.select(Download.radios[0]);
	},

	select: function(chk){
		
		chk.inputElement.checked = 'checked';
		
		Download.fx[chk.index].start({
			'background-color': '#161619',
			'color': '#FFF'
		});
		
		chk.addClass('selected');
		
		if (chk.deps){
			chk.deps.each(function(id){
				if (!$(id).hasClass('selected')) Download.select($(id));
			});
		} else if (chk.inputElement.type == 'radio'){
			Download.radios.each(function(other){
				if (other == chk) return;
				Download.deselect(other);
			});
		}
	},
	
	all: function(){
		Download.chks.each(function(chk){
			Download.select(chk);
		});
	},
	
	none: function(){
		Download.chks.each(function(chk){
			Download.deselect(chk);
		});
	},

	deselect: function(chk){
		chk.inputElement.checked = false;
		Download.fx[chk.index].start({
			'background-color': '#1d1d20',
			'color': '#595965'
		});
		chk.removeClass('selected');
		
		if (chk.deps){
			Download.chks.each(function(other){
				if (other == chk) return;
				if (other.deps.test(chk.id) && other.hasClass('selected')) Download.deselect(other);
			});
		}
	},

	parse: function(){
		Download.trs.each(function(tr, i){
			Download.fx[i] = new Fx.Styles(tr, {wait: false, duration: 300});

			var chk = tr.getElement('div.check');

			chk.index = i;
			var dp = chk.getProperty('deps');
			if (dp) chk.deps = dp.split(',');

			tr.addEvent('click', function(){
				
				if (!chk.hasClass('selected')) Download.select(chk);
				else if (tr.hasClass('check')) Download.deselect(chk);
			});
			
			tr.addEvent('mouseenter', function(){
				if (!chk.hasClass('selected')){
					Download.fx[i].start({
						'background-color': '#18181b',
						'color': '#b3b3bb'
					});
				}
			});
			
			tr.addEvent('mouseleave', function(){
				if (!chk.hasClass('selected')){
					Download.fx[i].start({
						'background-color': '#1d1d20',
						'color': '#595965'
					});
				}
			});

		});
	}

};

window.addEvent('load', Site.start);