<%
	response.setHeader("Cache-Control","no-cache"); 
	/*--This is used for HTTP 1.1 --*/
	response.setHeader("Pragma","no-cache");
	 /*--This is used for HTTP 1.0 --*/
	response.setDateHeader ("Expires", 0); 
	/*---- This is used to prevents caching at the proxy server */
	
	response.setHeader ("Connection", "close");
%>

<% 
	final long startTime = System.currentTimeMillis();
%>

<html>
<body id="home" class="spHomepageContent"> 

	<h1>Performance Timing Test</h1>

	<% 
		// get the time and sleep
		final String waitingTime = request.getParameter("sleep"); 
		
		final long startSleep = System.currentTimeMillis();
		
		Thread.currentThread().sleep(Long.valueOf(waitingTime));
		
		final long endSleep = System.currentTimeMillis();
	%>
 

<iframe  name="spon_vdz_countframe" width="0" height="0" frameborder="0" style="display:none;"></iframe><div id="spSZM"> 
<!-- SZM VERSION="1.3" --> 
<script type="text/javascript"> 
<!--
var IVW="http://spiegel.ivwbox.de/cgi-bin/ivw/CP/1001;/home/c-18/be-PB64-aG9tZXBhZ2UvY2VudGVy/szwprofil-1001";
document.write('<img src="'+IVW+'?r='+escape(document.referrer)+'&d='+(Math.random()*100000)+'" width="1" height="1" border="0" align="right" alt="" />'); 
// -->
</script> 
<noscript> 
<img src="http://spiegel.ivwbox.de/cgi-bin/ivw/CP/1001;/home/c-18/be-PB64-aG9tZXBhZ2UvY2VudGVy/szwprofil-1001?d=30342164" width="1" height="1" border="0" align="right" alt="" /> 
</noscript> 
<!-- /SZM --> 
<img src="http://www.spiegel.de/cgi-bin/vdz/CP/spiegel/home/c-18/be-PB64-aG9tZXBhZ2UvY2VudGVy/szwprofil-1001" width="1" height="1" border="0" align="right" alt="" /> 
<noscript> 
	<img src="https://count.spiegel.de/nm_trck.gif?sp.site=9999" width="1" height="1" border="0" align="right" alt="" /> 
</noscript> 
</div><div style="position:absolute;top: -100px;left: 0px;"> 
<script type="text/javascript"> 
<!--
spNm({
	'sp.atyp': '1000',
	'sp.site': '1',
	'sp.szw': '1001',
	'sp.aid': '18',
	'sp.channel': '18',
	'sp.be': 'homepage/center'
});
//-->
</script> 
</div> 
<script type="text/javascript"> 
<!--
OAS_url = 'http://adserv.quality-channel.de/RealMedia/ads/';
OAS_listpos = 'Sub1,Sub2,Top1,Top2,TopRight,Left,Right,Right1,Right2,Right3,Right4,Right5,Middle,Middle1,Middle2,Middle3,Bottom,Bottom1,Bottom2,Bottom3,Position1,Position2,x01,x02,x03,x04,x05,x06,x07,x08,x09,x10,x11,x12,x20,x21,x22,x23,x70,VMiddle2,VMiddle,VRight,Spezial';
OAS_allposlist  = 'Sub1,Sub2,Top1,Top2,TopRight,Left,Right,Right1,Right2,Right3,Right4,Right5,Middle,Middle1,Middle2,Middle3,Bottom,Bottom1,Bottom2,Bottom3,Position1,Position2,x01,x02,x03,x04,x05,x06,x07,x08,x09,x10,x11,x12,x20,x21,x22,x23,x70,VMiddle2,VMiddle,VRight,Spezial';
OAS_query = '';
OAS_sitepage = 'www.spiegel.de/homepage/center';
OAS_rn = '001234567890'; OAS_rns = '1234567890';
OAS_rn = new String (Math.random()); OAS_rns = OAS_rn.substring (2, 11);
 
var spHashParams=spGetHashParams();
if (spHashParams["oas.belegung"] != null) {
	OAS_belegung = spHashParams["oas.belegung"];
	OAS_sitepage = 'www.spiegel.de/' + spHashParams["oas.belegung"];
}
 
OAS_mjxscripturl = OAS_url + 'adstream_mjx.ads/' + OAS_sitepage + '/1' + OAS_rns + '@' + OAS_listpos + OAS_query;
 
function OAS_RICH(pos) { 
	document.write('<a href="' + OAS_url + 'click_nx.ads/' + OAS_sitepage + '/1' + OAS_rns + '@' + OAS_listpos + '!' + pos + OAS_query + '" target="_blank" ><img src="' + OAS_url + 'adstream_nx.ads/' + OAS_sitepage + '/1' + OAS_rns + '@' + OAS_listpos + '!' + pos + OAS_query + '" border="0" alt=""><\/a>');
}
 
 
	if (navigator.userAgent.indexOf('Mozilla/3') == -1) {
		
		// function OAS_RICH(pos) {}
		document.write('<scr' + 'ipt type="text/javascript" language="JavaScript1.1" src="' + OAS_mjxscripturl + '"><\/scr' + 'ipt' + '>');
	}
	
 
 
//-->
</script> 
 <script type="text/javascript"> 
<!--
	OAS_RICH('Top1'); 
// -->
</script><br class="spBreakNoHeight" clear="all" /> 
 
<div style="position:absolute;top:-100px;left:0px;"> 
	<script type="text/javascript"> 
	<!--
		OAS_RICH('Middle'); 
	// -->
	</script> 
</div> 
<br class="spBreakNoHeight" clear="all" /> 
<div id="spWrapper"> 
	<div id="spContentWrapper"> 
		<div id="spHeader" class="spCenterpage"> 
			<div id="spHeaderColoured"> 
			<ul id="spServiceNav"> 
				<li><a href="/schlagzeilen/">Schlagzeilen</a></li> 
						<li><a href="/dienste/0,1518,637773,00.html">Hilfe</a></li> 
						<li><a href="/dienste/0,1518,634260,00.html">RSS</a></li> 
						<li><a href="/dienste/0,1518,634551,00.html">Newsletter</a></li> 
						<li><a href="/dienste/0,1518,634549,00.html">Mobil</a></li> 
						<li><a href="http://wetter.spiegel.de/spiegel/"><img class="spIEsixPng" src="/static/sys/v9/icons/ic_sonnenwetter.png" width="18" height="18" alt="Wetter" />Wetter</a></li> 
						<li class="spLast"><a href="http://tvprogramm.spiegel.de/">TV-Programm</a></li> 
					</ul> 
			<div id="spTopNaviDate"> 
					Montag, 6. September 2010</div> 
	
			<a href="/" id="spLogo"><img class="spIEsixPng" src="/static/sys/v9/spiegelonline_logo.png" alt="SPIEGEL ONLINE" title="SPIEGEL ONLINE" width="282" height="39" /></a> 
			<h1 id="spChannelName">SPIEGEL ONLINE</h1> 
						<div> 
                        <form name="spsuchform" method="get" action="/suche/index.html" id="spSponSearch"> 
                            <input id="spSearch" name="suchbegriff" type="text"/> 
                            <script type="text/javascript"> 
                            <!--
	                            document.write('<img class="spIEsixPng" id="spSearchGo" src="/static/sys/v9/icons/ic_mainsearchbtn.png" onClick="spsuchform.submit();" width="25" height="25" />\n');
                            // -->
                            </script> 
                         </form> 
                        </div> 
					</div> 
		<ul id="spChannel"> 
			<li><a href="/" target="_self" class="spActive">NACHRICHTEN</a><ul> 
						<li> 
									<a href="/" target="_self" class="spActive">Home</a></li> 
							<li> 
									<a href="/politik/" target="_self" class="">Politik</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="/politik/deutschland/" target="_self" class="">Deutschland</a></li> 
												<li><a href="/politik/ausland/" target="_self" class="spNaviLevel2Last">Ausland</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="/wirtschaft/" target="_self" class="">Wirtschaft</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="http://boersen.manager-magazin.de/spo_mmo/" target="_blank" title="" class="">Börse</a></li> 
												<li><a href="/wirtschaft/service/" target="_self" class="">Verbraucher & Service</a></li> 
												<li><a href="/wirtschaft/unternehmen/" target="_self" class="">Unternehmen & Märkte</a></li> 
												<li><a href="/wirtschaft/soziales/" target="_self" class="spNaviLevel2Last">Staat & Soziales</a></li> 
												<li class="spDivideNaviAd"><div><div></div></div></li> 
<li><a href="http://placement24.spiegel.de/de/microsite/spiegel/" title="Anzeige">Placement24</a></li> 
<li><a href="http://immowelt.spiegel.de/" title="Anzeige">Immowelt</a></li> 
<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="/panorama/" target="_self" class="">Panorama</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="/panorama/justiz/" target="_self" class="">Justiz</a></li> 
												<li><a href="/panorama/leute/" target="_self" class="">Leute</a></li> 
												<li><a href="/panorama/gesellschaft/" target="_self" class="spNaviLevel2Last">Gesellschaft</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="/sport/" target="_self" class="">Sport</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="/sport/fussball/" target="_self" class="">Fußball</a></li> 
												<li><a href="/sport/formel1/" target="_self" class="spNaviLevel2Last">Formel 1</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="/kultur/" target="_self" class="">Kultur</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="/kultur/kino/" target="_self" class="">Kino</a></li> 
												<li><a href="/kultur/musik/" target="_self" class="">Musik</a></li> 
												<li><a href="/kultur/tv/" target="_self" class="">TV</a></li> 
												<li><a href="/kultur/literatur/" target="_self" class="">Literatur</a></li> 
												<li><a href="http://www.spiegel.de/thema/tageskarte" target="_self" title="" class="spNaviLevel2Last">KulturSPIEGEL</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="/netzwelt/" target="_self" class="">Netzwelt</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="/netzwelt/netzpolitik/" target="_self" class="">Netzpolitik</a></li> 
												<li><a href="/netzwelt/web/" target="_self" class="">Web</a></li> 
												<li><a href="/netzwelt/gadgets/" target="_self" class="">Gadgets</a></li> 
												<li><a href="/netzwelt/games/" target="_self" class="spNaviLevel2Last">Games</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="/wissenschaft/" target="_self" class="">Wissenschaft</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="/wissenschaft/mensch/" target="_self" class="">Mensch</a></li> 
												<li><a href="/wissenschaft/natur/" target="_self" class="">Natur</a></li> 
												<li><a href="/wissenschaft/technik/" target="_self" class="">Technik</a></li> 
												<li><a href="/wissenschaft/weltall/" target="_self" class="">Weltall</a></li> 
												<li><a href="/wissenschaft/medizin/" target="_self" class="spNaviLevel2Last">Medizin</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="http://einestages.spiegel.de/" target="_self" title="" class="">einestages</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="http://einestages.spiegel.de/page/SearchTopicAlbum.html" target="_self" title="" class="">Themen</a></li> 
												<li><a href="http://einestages.spiegel.de/page/SearchAuthorAlbum.html" target="_self" title="" class="">Zeitzeugen</a></li> 
												<li><a href="http://einestages.spiegel.de/page/LostAndFound.html" target="_self" title="" class="spNaviLevel2Last">Fundbüro</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="/unispiegel/" target="_self" class="">UniSPIEGEL</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="/unispiegel/studium/" target="_self" class="">Studium</a></li> 
												<li><a href="/unispiegel/jobundberuf/" target="_self" class="">Job & Beruf</a></li> 
												<li><a href="/unispiegel/wunderbar/" target="_self" class="spNaviLevel2Last">WunderBAR</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="/schulspiegel/" target="_self" class="">SchulSPIEGEL</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="/schulspiegel/abi/" target="_self" class="">Abi - und dann?</a></li> 
												<li><a href="/schulspiegel/ausland/" target="_self" class="">Querweltein</a></li> 
												<li><a href="/schulspiegel/leben/" target="_self" class="">Leben U21</a></li> 
												<li><a href="/schulspiegel/wissen/" target="_self" class="spNaviLevel2Last">Wissen</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="/reise/" target="_self" class="">Reise</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="/reise/staedte/" target="_self" class="">Städtereisen</a></li> 
												<li><a href="/reise/deutschland/" target="_self" class="">Deutschland</a></li> 
												<li><a href="/reise/europa/" target="_self" class="">Europa</a></li> 
												<li><a href="/reise/fernweh/" target="_self" class="">Fernweh</a></li> 
												<li><a href="http://km42.spiegel.de" target="_self" title="" class="spNaviLevel2Last">km42</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<li> 
									<a href="/auto/" target="_self" class="">Auto</a><ul class="spNaviLevel2" style="z-index:3;"> 
											<li><a href="/auto/fahrberichte/" target="_self" class="">Tests</a></li> 
												<li><a href="/auto/fahrkultur/" target="_self" class="spNaviLevel2Last">Fahrkultur</a></li> 
												<li class="spNaviBottom">&nbsp;</li> 
										</ul> 
									</li> 
							<script type="text/javascript"> 
						<!--
							OAS_RICH('Sub1'); 
						// -->
						</script> 
						
						<script type="text/javascript"> 
						<!--
							OAS_RICH('Sub2'); 
						// -->
						</script> 
						</ul> 
						</li> 
					<li><a href="/video/" target="_self" class="">VIDEO</a></li> 
					<li><a href="/thema/" target="_self" class="">THEMEN</a></li> 
					<li><a href="http://forum.spiegel.de/" target="_self" title="" class="">FORUM</a></li> 
					<li><a href="/international/" target="_self" class="">ENGLISH</a></li> 
					<li><a href="/spiegel/" target="_self" class="">DER SPIEGEL</a></li> 
					<li><a href="http://www.spiegel.de/sptv/magazin/" target="_self" title="" class="">SPIEGEL TV</a></li> 
					<li><a href="http://abo.spiegel.de/?et_cid=7&amp;et_lid=1946&amp;et_sub=aboreiter " target="_blank" title="" class="">ABO</a></li> 
					<li><a href="http://shop.spiegel.de/" target="_blank" title="" class="">SHOP</a></li> 
					</ul> 
	<script type="text/javascript"> 
	<!--
	spMainNaviInit();
	//-->
	</script> 
	<div id="spLoginPopup"></div> 
</div> 
	
		<div id="spContainer" class="spClearfix"> 
		 	<div id="spBreadcrumb"> 
 
<div id="spLoginArea"> 
	<ul id="spLoginLinks"> 
		<script type="text/javascript"> 
		<!--
			spWriteMSLoginLinks();
		// -->
		</script> 
		<noscript><li class="spLast"><a href="/meinspiegel/index.html">Mein SPIEGEL</a></li></noscript> 
	</ul> 
</div> 
<br clear="all" /> 
</div> 
<script type="text/javascript"> 
			<!--
				OAS_RICH('Top2'); 
			// -->
			</script> 
			<br class="spBreakNoHeight" clear="all" /> 
			<script type="text/javascript"> 
				spIPhoneMessage();
			</script>			
			<div id="spTeaserColumn"> 
				<div class="spTopThema"> 
 
<h2>München</h2> 
<h3><a href="/panorama/justiz/0,1518,715828,00.html" title="München: Angeklagter im Brunner-Prozess wegen Mordes verurteilt">Angeklagter im Brunner-Prozess wegen Mordes verurteilt</a></h3> 
		<div class="spGalleryBig"> 
 
		<div class="spGalleryBigPic"> 
					<a href="/panorama/justiz/0,1518,715828,00.html" title="München: Angeklagter im Brunner-Prozess wegen Mordes verurteilt"><img src="http://www.spiegel.de/images/image-15402-panoV9-gvop.jpg" width="520" height="250" border="0" alt="" title="" /></a><br clear="all" /> 
					<div class="spPicLayer"> 
						<a href="/fotostrecke/fotostrecke-57582.html"> 
						<span class="spPicLayerLeft"></span> 
						<span class="spPicLayerMiddle"> 
							<span class="spPicLayerText"> 
								<strong>Fotostrecke:</strong> 11 Bilder</span> 
						</span> 
						<span class="spPicLayerRight"></span> 
						</a> 
					</div> 
				</div> 
				<div class="spCredit">DDP</div> 
			</div>	
<p>Urteil im Prozess um den Tod von Dominik Brunner an einer Münchner S-Bahn-Station: Das Gericht hat beide Angeklagten zu langjährigen Haftstrafen verurteilt. Haupttäter Markus Sch. muss wegen Mordes fast zehn Jahre ins Gefängnis, Sebastian L. bekam sieben Jahre. <a href="/panorama/justiz/0,1518,715828,00.html" title="München: Angeklagter im Brunner-Prozess wegen Mordes verurteilt">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="/video/video-1075258.html" onclick="return spShowVideo(this,'1075258','1')" title="Prozess im Mordfall Brunner">Video</a> | <a href="http://forum.spiegel.de/showthread.php?t=20351" target="_self">Forum</a>&nbsp;]</span></p> 
	<ul class="spDottedLinkList"> 
	<li><a href="http://www.spiegel.de/thema/dominik_brunner/" target="_self" title="Themenseite:  Tödliche Zivilcourage ">Themenseite: <span>Tödliche Zivilcourage</span></a></li> 
	</ul> 
</div><div class="spTopThema"> 
 
<h2>Längere AKW-Laufzeiten</h2> 
<h3><a href="/politik/deutschland/0,1518,715831,00.html" title="Längere AKW-Laufzeiten: Umweltverbände zerpflücken Atomkompromiss">Umweltverbände zerpflücken Atomkompromiss</a></h3> 
		<p>Wirtschaftsminister Brüderle schwärmt nach der Einigung über längere AKW-Laufzeiten von einer "neuen Zeitrechnung". Umweltverbände jedoch sind entsetzt. Sie sprechen von einer unverantwortlichen Entscheidung, von einem Einknicken vor der Industrie - und kündigen Massenproteste an. <a href="/politik/deutschland/0,1518,715831,00.html" title="Längere AKW-Laufzeiten: Umweltverbände zerpflücken Atomkompromiss">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="/video/video-1082613.html" onclick="return spShowVideo(this,'1082613','44')" title="Koalition einigt sich auf gestaffelte Laufzeitverlängerung">Video</a> | <a href="http://forum.spiegel.de/showthread.php?t=19965">Forum</a>&nbsp;]</span></p> 
	<ul class="spDottedLinkList"> 
	<li><a href="/fotostrecke/fotostrecke-59014.html" title="Reaktionen auf Atomkompromiss:  &quot;Ein schwarzer Tag&quot; ">Reaktionen auf Atomkompromiss: <span>"Ein schwarzer Tag"</span></a></li> 
	<li><a href="/politik/deutschland/0,1518,715817,00.html" title="Einigung im Kanzleramt:  Koalition besiegelt Atomkompromiss ">Einigung im Kanzleramt: <span>Koalition besiegelt Atomkompromiss</span></a></li> 
	<li><a href="/fotostrecke/fotostrecke-56250.html" title="Grafiken:  Die wichtigsten Fakten zur Atomindustrie ">Grafiken: <span>Die wichtigsten Fakten zur Atomindustrie</span></a></li> 
	</ul> 
</div><div class="spTopThema"> 
 
<div class="spArticleImageBox spAssetAlignleft" style="width: 182px"> 
	<a href="/panorama/justiz/0,1518,715616,00.html" title="Fall Kachelmann: Ein Prozess, sieben Fragen"><img src="/images/image-127450-thumb-qoeq.jpg" width="180" height="180" border="0" align="left" alt="Fall Kachelmann: Ein Prozess, sieben Fragen" title="Fall Kachelmann: Ein Prozess, sieben Fragen" /></a></div><h2>Fall Kachelmann</h2> 
<h3><a href="/panorama/justiz/0,1518,715616,00.html" title="Fall Kachelmann: Ein Prozess, sieben Fragen">Ein Prozess, sieben Fragen</a></h3> 
		<p>Der Fall Kachelmann beschäftigt seit Monaten die Öffentlichkeit. Jetzt hat der Prozess begonnen - und wurde nach wenigen Minuten vertagt. Das Gericht muss klären, ob der Moderator tatsächlich eine Freundin vergewaltigt hat. Doch in Wahrheit reicht das Verfahren weit über den Gerichtssaal hinaus. <span class="spAutorenzeile">Von Simone Utler</span> <a href="/panorama/justiz/0,1518,715616,00.html" title="Fall Kachelmann: Ein Prozess, sieben Fragen">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="/video/video-1077305.html" onclick="return spShowVideo(this,'1077305','11')" title="Kachelmann wieder frei">Video</a> | <a href="http://forum.spiegel.de/showthread.php?t=20342" target="_self">Forum</a>&nbsp;]</span></p> 
	<ul class="spDottedLinkList"> 
	<li><a href="http://www.spiegel.de/thema/joerg_kachelmann/" target="_self" title="Themenseite:  Das ist Jörg Kachelmann ">Themenseite: <span>Das ist Jörg Kachelmann</span></a></li> 
	<li><a href="http://www.lto.de/de/html/nachrichten/1365/staatsanwaelte-kachelmann-Kavallerie-der-Justiz-statt-objektivster-BehC3B6rde-der-Welt-/" target="_blank" title="Der Fall Kachelmann:  Die Kavallerie der Justiz (Legal Tribune Online) ">Der Fall Kachelmann: <span>Die Kavallerie der Justiz (Legal Tribune Online)</span></a></li> 
	</ul> 
</div><div class="spTopThema"> 
 
<h2>Einwanderung</h2> 
<h3><a href="/politik/deutschland/0,1518,715820,00.html" title="Einwanderung: CDU-Politiker wollen Integrationsverweigerer bestrafen">CDU-Politiker wollen Integrationsverweigerer bestrafen</a></h3> 
		<p>Die CDU nutzt die Sarrazin-Debatte, um ihr Profil zu schärfen. Nach Innenminister de Maizière fordern zahlreiche Unionspolitiker, Integrationsverweigerer härter zu bestrafen. Wer sich Integrationskursen entziehe, müsse mit "konsequent angewandten" Strafen rechnen. <a href="/politik/deutschland/0,1518,715820,00.html" title="Einwanderung: CDU-Politiker wollen Integrationsverweigerer bestrafen">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="http://forum.spiegel.de/showthread.php?t=20291">Forum</a>&nbsp;]</span></p> 
	<ul class="spDottedLinkList"> 
	<li><a href="/politik/deutschland/0,1518,715784,00.html" title="SPD in Sorge:  Gabriel fürchtet den Sarrazin-Malus ">SPD in Sorge: <span>Gabriel fürchtet den Sarrazin-Malus</span></a></li> 
	<li><a href="/politik/deutschland/0,1518,715807,00.html" title="Kritik an Bundespräsident:  Wulff gerät zwischen die Sarrazin-Fronten ">Kritik an Bundespräsident: <span>Wulff gerät zwischen die Sarrazin-Fronten</span></a></li> 
	<li><a href="/politik/deutschland/0,1518,715751,00.html" title="Sarrazin-Debatte:  Umfrage sieht großes Potential für Protestpartei ">Sarrazin-Debatte: <span>Umfrage sieht großes Potential für Protestpartei</span></a></li> 
	<li><a href="http://www.spiegel.de/thema/thilo_sarrazin/" target="_self" title="Themenseite Thilo Sarrazin:  Der Querulant ">Themenseite Thilo Sarrazin: <span>Der Querulant</span></a></li> 
	</ul> 
</div><div class="spTopThema"> 
 
<div class="spArticleImageBox spAssetAlignleft" style="width: 182px"> 
	<a href="/wissenschaft/technik/0,1518,715804,00.html" title="Energieforschung: Solarzellen erneuern sich selbst"><img src="/images/image-3784-thumb-zasu.jpg" width="180" height="180" border="0" align="left" alt="Energieforschung: Solarzellen erneuern sich selbst" title="Energieforschung: Solarzellen erneuern sich selbst" /></a></div><h2>Energieforschung</h2> 
<h3><a href="/wissenschaft/technik/0,1518,715804,00.html" title="Energieforschung: Solarzellen erneuern sich selbst">Solarzellen erneuern sich selbst</a></h3> 
		<p>Kein Verschleiß: Forscher haben Moleküle entwickelt, die Sonnenlicht speichern und sich zugleich regenerieren. Nun hoffen sie auf die Entwicklung von Solarzellen, die nicht altern. Normalerweise sinkt nämlich im Lauf der Jahre deren Wirkungsgrad. <a href="/wissenschaft/technik/0,1518,715804,00.html" title="Energieforschung: Solarzellen erneuern sich selbst">mehr...</a> </p> 
	</div><div class="spTopThema"> 
 
<div class="spArticleImageBox spAssetAlignleft" style="width: 182px"> 
	<a href="/unispiegel/jobundberuf/0,1518,715572,00.html" title="Zugewanderte Akademiker: &quot;Deutschland lässt mich nicht arbeiten&quot;"><img src="/images/image-82852-thumb-qdbq.jpg" width="180" height="180" border="0" align="left" alt="Zugewanderte Akademiker: &quot;Deutschland lässt mich nicht arbeiten&quot;" title="Zugewanderte Akademiker: &quot;Deutschland lässt mich nicht arbeiten&quot;" /></a></div><h2>Zugewanderte Akademiker</h2> 
<h3><a href="/unispiegel/jobundberuf/0,1518,715572,00.html" title="Zugewanderte Akademiker: &quot;Deutschland lässt mich nicht arbeiten&quot;">"Deutschland lässt mich nicht arbeiten"</a></h3> 
		<p>Hunderttausende ausländische Akademiker leben in Deutschland - und können nicht arbeiten, weil Chefs und Behörden ihre Abschlüsse nicht anerkennen. Die Wirtschaft jammert über das verschenkte Potential. Und doch mangelt es an Versuchen, das Chaos zu bekämpfen. <span class="spAutorenzeile">Von Massimo Bognanni und Lenz Jacobsen</span> <a href="/unispiegel/jobundberuf/0,1518,715572,00.html" title="Zugewanderte Akademiker: Deutschland lässt mich nicht arbeiten">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="http://forum.spiegel.de/showthread.php?t=20348" target="_self">Forum</a>&nbsp;]</span></p> 
	</div><div class="spTopThema"> 
 
<div class="spArticleImageBox spAssetAlignleft" style="width: 182px"> 
	<a href="/reise/fernweh/0,1518,709851,00.html" title="Chili-Hochburg Diamante: Viagra des armen Mannes"><img src="/images/image-116953-thumb-gegn.jpg" width="180" height="180" border="0" align="left" alt="Chili-Hochburg Diamante: Viagra des armen Mannes" title="Chili-Hochburg Diamante: Viagra des armen Mannes" /></a></div><h2>Chili-Hochburg Diamante</h2> 
<h3><a href="/reise/fernweh/0,1518,709851,00.html" title="Chili-Hochburg Diamante: Viagra des armen Mannes">Viagra des armen Mannes</a></h3> 
		<p>Von wegen Dolce Vita: Ganz Kalabrien hegt eine brennende Leidenschaft für Chili. Im Örtchen Diamante beginnt am Mittwoch zu Ehren der scharfen Schote das Peperoncino-Festival - Höhepunkt des Programms ist einer der fiesesten Esswettbewerbe der Welt. <span class="spAutorenzeile">Von Dominik Fehrmann</span> <a href="/reise/fernweh/0,1518,709851,00.html" title="Chili-Hochburg Diamante: Viagra des armen Mannes">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="http://forum.spiegel.de/showthread.php?t=20341" target="_self">Forum</a>&nbsp;]</span></p> 
	<ul class="spDottedLinkList"> 
	<li><a href="/fotostrecke/fotostrecke-57791.html" title="Fotostrecke:  Teufelchen für feuerfeste Gaumen ">Fotostrecke: <span>Teufelchen für feuerfeste Gaumen</span></a></li> 
	<li><a href="http://www.spiegel.de/thema/reisespeisen/" target="_self" title="Themenseite Reisespeisen:  Geschmack der Welt ">Themenseite Reisespeisen: <span>Geschmack der Welt</span></a></li> 
	</ul> 
</div><div class="spLine"></div> 
<script type="text/javascript"> 
<!--
var spMMLBrowsespGalleryBoxX40560=new spEnhPaginator("spGalleryBoxX40560", [ ['/images/image-126933-videothumbmultimedia-tzmb.jpg','/images/image-127504-videothumbmultimedia-bwrb.jpg','/img/0,1020,3024826,00.jpg'],['/images/image-126979-videothumbmultimedia-xfkd.jpg','/images/image-127429-videothumbmultimedia-twfn.jpg','/images/image-125640-videothumbmultimedia-oglw.jpg'],['/images/image-127241-videothumbmultimedia-utld.jpg','/images/image-127183-videothumbmultimedia-ktya.jpg','/images/image-126272-videothumbmultimedia-odjf.jpg'],['/images/image-7957-videothumbmultimedia-yyyb.jpg','/images/image-126965-videothumbmultimedia-smzb.jpg','/images/image-126805-videothumbmultimedia-kgcl.jpg'],['/images/image-126687-videothumbmultimedia-mjqd.jpg','/images/image-126536-videothumbmultimedia-tyzf.jpg','/images/image-126231-videothumbmultimedia-pxmw.jpg'],['/images/image-126463-videothumbmultimedia-puij.jpg','/images/image-125820-videothumbmultimedia-dhys.jpg','/images/image-126424-videothumbmultimedia-ybqq.jpg'] ]);
// -->
</script> 
<div class="spPaginatorControlArrows"> 
<div class="spArrowLeft"> 
<a onclick="spMMLBrowsespGalleryBoxX40560.showPrev(); return false;" href="/extra/0,1518,267802,00.html" onfocus="blur();"> 
<img class="spIEsixPng" src="/static/sys/v9/buttons/pfeilmume_li.png" border="0" alt="" /> 
</a> 
</div> 
<div class="spArrowRight"> 
<a onclick="spMMLBrowsespGalleryBoxX40560.showNext(); return false;" href="/extra/0,1518,267802,00.html" onfocus="blur();"> 
<img class="spIEsixPng" src="/static/sys/v9/buttons/pfeilmume_re.png" border="0" alt="" /> 
</a> 
</div> 
</div> 
<div class="spMuMeLei spClearfix" id="spGalleryBoxX40560"> 
<h4><a href="/video/">VIDEOS</a></h4> 
<div class="spPaginatorControlPanel"> 
<div class="spPaginatorControl spActive"><a onclick="spMMLBrowsespGalleryBoxX40560.showNum('0'); return false;" href="/extra/0,1518,267802,00.html">1</a></div><div class="spPaginatorControl">|<a onclick="spMMLBrowsespGalleryBoxX40560.showNum('1'); return false;" href="/extra/0,1518,267802,00.html">2</a></div><div class="spPaginatorControl">|<a onclick="spMMLBrowsespGalleryBoxX40560.showNum('2'); return false;" href="/extra/0,1518,267802,00.html">3</a></div><div class="spPaginatorControl">|<a onclick="spMMLBrowsespGalleryBoxX40560.showNum('3'); return false;" href="/extra/0,1518,267802,00.html">4</a></div><div class="spPaginatorControl">|<a onclick="spMMLBrowsespGalleryBoxX40560.showNum('4'); return false;" href="/extra/0,1518,267802,00.html">5</a></div><div class="spPaginatorControl">|<a onclick="spMMLBrowsespGalleryBoxX40560.showNum('5'); return false;" href="/extra/0,1518,267802,00.html">6</a></div><div class="spPaginatorControl">|<a onclick="spMMLBrowsespGalleryBoxX40560.showNum('6'); return false;" href="/extra/0,1518,267802,00.html">7</a></div> 
</div> 
<div class="spPaginatorPage" style="display: block;"> 
<ul> 
<li class="spFirst"> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1081547.html"  onclick="return spShowVideo(this,'1081547','10')"><img src="/images/image-125031-videothumbmultimedia-hlwi.jpg" width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1081547','10')" href="/video/video-1081547.html">Attentate in Russland: <span class="blk"> Hass auf den deutschen Hightech-<span class="spOptiBreak"> </span>Zug</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/flash/flash-24255.html" ><img src="/images/image-127401-videothumbmultimedia-djfu.jpg" width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a href="/flash/flash-24255.html">Video-<span class="spOptiBreak"> </span>Spezial zu Duisburg-<span class="spOptiBreak"> </span>Marxloh: <span class="blk"> "Hier müssen wir Deutsche uns integrieren"</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082294.html"  onclick="return spShowVideo(this,'1082294','1')"><img src="/images/image-127283-videothumbmultimedia-bips.jpg" width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082294','1')" href="/video/video-1082294.html">IFA 2010: <span class="blk"> Das Galaxy Tablet von Samsung im Videotest</span></a> 
</li> 
</ul> 
</div> 
<div class="spPaginatorPage" style="display: none;"> 
<ul> 
<li class="spFirst"> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082086.html"  onclick="return spShowVideo(this,'1082086','1')"><img id="spGalleryBoxX4056000"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082086','1')" href="/video/video-1082086.html">Neu im Kino: <span class="blk"> Paradiesische Liebschaften und kämpfende Magier</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082419.html"  onclick="return spShowVideo(this,'1082419','1')"><img id="spGalleryBoxX4056001"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082419','1')" href="/video/video-1082419.html">kicker.tv: <span class="blk"> Klose schießt Deutschland zum Arbeitssieg (3D)</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/flash/flash-23594.html" >><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a href="/flash/flash-23594.html">Video-<span class="spOptiBreak"> </span>Spezial: <span class="blk"> Görings Prachtbau, Schäubles Sparzimmer</span></a> 
</li> 
</ul> 
</div> 
<div class="spPaginatorPage" style="display: none;"> 
<ul> 
<li class="spFirst"> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082186.html"  onclick="return spShowVideo(this,'1082186','1')"><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082186','1')" href="/video/video-1082186.html">Türkische Männergruppe in Berlin: <span class="blk"> "Sarrazin soll uns besuchen"</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082331.html"  onclick="return spShowVideo(this,'1082331','1')"><img id="spGalleryBoxX4056011"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082331','1')" href="/video/video-1082331.html">Glosse: <span class="blk"> Neustart für Sarrazin</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1081252.html"  onclick="return spShowVideo(this,'1081252','10')"><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1081252','10')" href="/video/video-1081252.html">Über den Himalaya: <span class="blk"> Härtetest für revolutionäre Fahrrad-<span class="spOptiBreak"> </span>Schaltung</span></a> 
</li> 
</ul> 
</div> 
<div class="spPaginatorPage" style="display: none;"> 
<ul> 
<li class="spFirst"> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082276.html"  onclick="return spShowVideo(this,'1082276','11')"><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082276','11')" href="/video/video-1082276.html">Massen-<span class="spOptiBreak"> </span>Attacke: <span class="blk"> 100 Tigerhaie jagen riesigen Fischschwarm</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082286.html"  onclick="return spShowVideo(this,'1082286','1')"><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082286','1')" href="/video/video-1082286.html">kicker.tv: <span class="blk"> Lahm - "Ich bin jetzt der Kapitän"</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082143.html"  onclick="return spShowVideo(this,'1082143','11')"><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082143','11')" href="/video/video-1082143.html">Hollywood-<span class="spOptiBreak"> </span>Star Michael Douglas: <span class="blk"> "Ja, ich habe Krebs"</span></a> 
</li> 
</ul> 
</div> 
<div class="spPaginatorPage" style="display: none;"> 
<ul> 
<li class="spFirst"> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082100.html"  onclick="return spShowVideo(this,'1082100','1')"><img id="spGalleryBoxX4056030"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082100','1')" href="/video/video-1082100.html">Erster Beweis:<span class="blk"> FCKW-<span class="spOptiBreak"> </span>Verbot heilt Ozonschicht</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082132.html"  onclick="return spShowVideo(this,'1082132','1')"><img id="spGalleryBoxX4056031"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082132','1')" href="/video/video-1082132.html">Drogenboss im Verhör: <span class="blk"> Mexikanische Polizei veröffentlicht Video</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082138.html"  onclick="return spShowVideo(this,'1082138','1')"><img id="spGalleryBoxX4056032"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082138','1')" href="/video/video-1082138.html">kicker.tv: <span class="blk"> Mladen Petric - gefrustet von Transfer-<span class="spOptiBreak"> </span>Posse</span></a> 
</li> 
</ul> 
</div> 
<div class="spPaginatorPage" style="display: none;"> 
<ul> 
<li class="spFirst"> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1082080.html"  onclick="return spShowVideo(this,'1082080','1')"><img id="spGalleryBoxX4056040"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1082080','1')" href="/video/video-1082080.html">Flammendes Spektakel: <span class="blk"> Schlacht mit Feuerbällen</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1081953.html"  onclick="return spShowVideo(this,'1081953','1')"><img id="spGalleryBoxX4056041"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1081953','1')" href="/video/video-1081953.html">Pubertierender Hund: <span class="blk"> Ein Selbstversuch im Dackelclub</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1081797.html"  onclick="return spShowVideo(this,'1081797','1')"><img id="spGalleryBoxX4056042"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1081797','1')" href="/video/video-1081797.html">Angefasst: <span class="blk"> Konkurrenz für das iPhone - das Streak von Dell</span></a> 
</li> 
</ul> 
</div> 
<div class="spPaginatorPage" style="display: none;"> 
<ul> 
<li class="spFirst"> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1081927.html"  onclick="return spShowVideo(this,'1081927','1')"><img id="spGalleryBoxX4056050"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1081927','1')" href="/video/video-1081927.html">Wahlkampf in Tschechien: <span class="blk"> Pin-<span class="spOptiBreak"> </span>up-<span class="spOptiBreak"> </span>Politikerinnen</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1081879.html"  onclick="return spShowVideo(this,'1081879','1')"><img id="spGalleryBoxX4056051"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1081879','1')" href="/video/video-1081879.html">Brutaler Überfall: <span class="blk"> Hooligan-<span class="spOptiBreak"> </span>Angriff auf Rockfestival</span></a> 
</li> 
<li> 
<div class="spArticleImageBox spAssetAlign" style="width: 162px"> 
<div class="spVideoPic" style="width:162px; height:122px;"><a href="/video/video-1081903.html"  onclick="return spShowVideo(this,'1081903','11')"><img id="spGalleryBoxX4056052"  width="160" height="120" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
</div> 
<br /> 
<a  onclick="return spShowVideo(this,'1081903','11')" href="/video/video-1081903.html">Filmfest in Venedig: <span class="blk"> Tarantino und Portman sind schon da</span></a> 
</li> 
</ul> 
</div> 
</div> 
<div class="spRessortTeaserBox spiegel"> 
<div class="spRessortBoxHeader"> 
<h3 class="spNoBullet"> 
<a href="/spiegel/">DER SPIEGEL</a> 
</h3> 
<div class="spRessortBoxNav"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/spiegel/0,1518,ausg-4748,00.html">Inhalt</a></li> 
<li><a href="/spiegel/vorab/">Vorabmeldungen</a></li> 
<li><a href="http://abo.spiegel.de/go/place">Abo</a></li> 
<li><a href="http://wissen.spiegel.de/wissen/epaper/SP/2010/36/start.html">E-Paper</a></li> 
<li><a href="http://abo.spiegel.de/micro_issue" target="_blank">Heft kaufen</a></li> 
</ul> 
</div> 
</div> 
<div style="width:297px; padding-right:10px; float:left;"> 
<div style="width:98px; float:left;"> 
<a href="/spiegel/0,1518,ausg-4748,00.html" ><img src="/img/0,1020,3039359,00.jpg" width="90" height="116" border="0" alt="" align="left" hspace="0" class="spImgNoBorder spImgNoBorder" /></a> </div> 
<div> 
<a href="/spiegel/0,1518,ausg-4748,00.html" style="color:black;"> 
<p style="margin:0 8px 6px 0;">36/2010</p> 
<p style="margin:0 8px 6px 0; font-weight:bold;">Volksheld Sarrazin</p> 
<p style="margin:0 8px 6px 0;">Warum so viele Deutsche einem Provokateur verfallen</p> 
</a> 
<ul class="spArrowList" style="margin:4px 0 0 0;"><li><a href="http://forum.spiegel.de/showthread.php?t=20291">Diskutieren Sie über das aktuelle Titelthema</a></li></ul> </div> 
</div> 
<div style="width:210px; height:123px; margin:0; padding:0; float:left; overflow:hidden;"> 
<script type="text/javascript"> 
<!--
OAS_RICH('x20');
// -->
</script><br class="spBreakNoHeight" clear="all" /> </div> 
</div> 
<br clear="all" /> 
<div class="spRessortTeaserBox panorama spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="/panorama"> PANORAMA</a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_panorama"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/panorama/justiz/">Justiz</a></li> 
<li><a href="/panorama/leute/">Leute</a></li> 
<li><a href="/panorama/gesellschaft/">Gesellschaft</a></li> 
<li><a onclick="spHpTopicBoxToggle('panorama');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_panorama">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('panorama');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_panorama"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_panorama" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="/thema/hohlspiegel">Hohlspiegel</a></li> 
<li><a href="/thema/abgekocht">Abgekocht</a></li> 
<li><a href="/thema/eine_meldung_und_ihre_geschichte">Meldung und Geschichte</a></li> 
<li><a href="/thema/gisela_friedrichsen">Gisela Friedrichsen</a></li> 
<li><a href="/thema/uups_et_orbi">Uups et orbi
</a></li> 
<li><a href="/thema/"><strong>alle Themen</strong></a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<div class="spArticleImageBox spAssetAlignleft" style="width: 92px;"> 
												<a href="/panorama/0,1518,715822,00.html" title="Kurz & krass: Polizei streicht Ponys an"><img src="/images/image-127697-thumbsmall-lwaj.jpg" width="90" height="90" border="0" align="left" alt="Kurz & krass: Polizei streicht Ponys an" title="Kurz & krass: Polizei streicht Ponys an" /> 
												</a></div> 
										<h4> 
								<a href="/panorama/0,1518,715822,00.html" title="Kurz & krass: Polizei streicht Ponys an">Kurz & krass: <span class="spBlk">Polizei streicht Ponys an</span></a></h4> 
							<p>Verkehrserziehung auf Russisch: "Aktion Zebrastreifen" hieß die eigentlich harmlos erscheinende Maßnahme. Doch in Ermangelung echter Zebras sahen sich einige Moskauer Beamte dazu veranlasst, Pferde anzumalen. SPIEGEL ONLINE präsentiert die skurrilsten Kurzmeldungen der Woche. <a href="/panorama/0,1518,715822,00.html" title="Kurz & krass: Polizei streicht Ponys an">mehr...</a> </p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="/panorama/0,1518,715843,00.html" title="Electric Light Orchestra: Heuballen tötet Musiker">Electric Light Orchestra: <span class="spBlk">Heuballen tötet Musiker</span></a></li><li> 
							<a href="/panorama/leute/0,1518,715775,00.html" title="Stürmerstar: Prostituierte plaudert über angebliche Affäre mit Wayne Rooney">Stürmerstar: <span class="spBlk">Prostituierte plaudert über angebliche Affäre mit Wayne Rooney</span></a></li><li> 
							<a href="/panorama/0,1518,715769,00.html" title="&quot;Schlag den Raab&quot;-Sieger: Von wegen Jetset-Leben">"Schlag den Raab"-Sieger: <span class="spBlk">Von wegen Jetset-Leben</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_panorama"><a onclick="spHPTeaserToggle('panorama','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_panorama"> 
		<ul> 
			<li> 
							<a href="/panorama/0,1518,715834,00.html" title="Guatemala: Viele Menschen sterben bei Erdrutschen">Guatemala: <span class="spBlk">Viele Menschen sterben bei Erdrutschen</span></a></li><li> 
							<a href="/panorama/0,1518,715808,00.html" title="Guatemala: Dutzende Menschen sterben durch Erdrutsche">Guatemala: <span class="spBlk">Dutzende Menschen sterben durch Erdrutsche</span></a></li><li> 
							<a href="/panorama/0,1518,715793,00.html" title="Flugschau in Bayern: Doppeldecker rast in Zuschauermenge">Flugschau in Bayern: <span class="spBlk">Doppeldecker rast in Zuschauermenge</span></a></li><li> 
							<a href="/panorama/justiz/0,1518,715788,00.html" title="El Salvador: Polizei findet mehrere Millionen Dollar in Ölfässern">El Salvador: <span class="spBlk">Polizei findet mehrere Millionen Dollar in Ölfässern</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('panorama','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<div class="spRessortTeaserBox politik spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="/politik"> POLITIK</a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_politik"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/politik/deutschland/">Deutschland</a></li> 
<li><a href="/politik/ausland/">Ausland</a></li> 
<li><a href="/thema/cdu_csu_fdp_koalition_2009/">Schwarz-gelbe Koalition</a></li> 
<li><a onclick="spHpTopicBoxToggle('politik');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_politik">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('politik');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_politik"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_politik" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="/thema/regierung_obama">Regierung Obama</a></li> 
<li><a href="/thema/afghanistan_krieg">Afghanistan-Krieg</a></li> 
<li><a href="/thema/iran_konflikt">Iran-Krise</a></li> 
<li><a href="/thema/nordkorea_atomkonflikt">Nordkorea-Konflikt</a></li> 
<li><a href="/thema/deutschland_rechtsextremismus">Rechtsextremismus</a></li> 
<li><a href="/thema/"><strong>alle Themen</strong></a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<h4> 
								<a href="/politik/deutschland/0,1518,715807,00.html" title="Kritik an Bundespräsident: Wulff gerät zwischen die Sarrazin-Fronten">Kritik an Bundespräsident: <span class="spBlk">Wulff gerät zwischen die Sarrazin-Fronten</span></a></h4> 
							<p>Verliert Thilo Sarrazin seinen Job als Bundesbanker? Darüber muss der Bundespräsident entscheiden. Doch weil Christian Wulff sich früh öffentlich über den umstrittenen Sozialdemokraten äußerte, wird nun scharfe Kritik laut. Nicht nur aus der SPD: CSU-Mann Gauweiler nennt das Verfahren eine "Farce". <a href="/politik/deutschland/0,1518,715807,00.html" title="Kritik an Bundespräsident: Wulff gerät zwischen die Sarrazin-Fronten">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="http://forum.spiegel.de/showthread.php?t=20291">Forum</a>&nbsp;]</span></p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="/politik/ausland/0,1518,715511,00.html" title="Vuvuzela-Protest gegen Sarkozy: Tröten für die Rente">Vuvuzela-Protest gegen Sarkozy: <span class="spBlk">Tröten für die Rente</span></a></li><li> 
							<a href="/politik/ausland/0,1518,715829,00.html" title="Anschlag in Pakistan: Extremisten töten ein Dutzend Menschen">Anschlag in Pakistan: <span class="spBlk">Extremisten töten ein Dutzend Menschen</span></a></li><li> 
							<a href="/politik/ausland/0,1518,715760,00.html" title="Fall Aschtiani: Peitschenhiebe für das falsche Foto">Fall Aschtiani: <span class="spBlk">Peitschenhiebe für das falsche Foto</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_politik"><a onclick="spHPTeaserToggle('politik','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_politik"> 
		<ul> 
			<li> 
							<a href="/politik/ausland/0,1518,715818,00.html" title="Italien: Berlusconi-Kontrahent Fini rechnet ab">Italien: <span class="spBlk">Berlusconi-Kontrahent Fini rechnet ab</span></a></li><li> 
							<a href="/politik/ausland/0,1518,715816,00.html" title="Moldau: Referendum scheitert an zu geringer Beteiligung">Moldau: <span class="spBlk">Referendum scheitert an zu geringer Beteiligung</span></a></li><li> 
							<a href="/politik/deutschland/0,1518,715789,00.html" title="SMS-Indiskretion: Merkel und Gabriel beenden Funkstille">SMS-Indiskretion: <span class="spBlk">Merkel und Gabriel beenden Funkstille</span></a></li><li> 
							<a href="/politik/ausland/0,1518,715803,00.html" title="Bagdad: Mehrere Tote bei Selbstmordanschlag auf Armee-Hauptquartier">Bagdad: <span class="spBlk">Mehrere Tote bei Selbstmordanschlag auf Armee-Hauptquartier</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('politik','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<div class="spRessortTeaserBox kultur spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="/kultur"> KULTUR</a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_kultur"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/kultur/kino/">Kino</a></li> 
<li><a href="/kultur/musik/">Musik</a></li> 
<li><a href="/kultur/tv/">TV</a></li> 
<li><a href="/thema/kulturhauptstadt_ruhr_2010/">Ruhr.2010</a></li> 
<li><a onclick="spHpTopicBoxToggle('kultur');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_kultur">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('kultur');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_kultur"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_kultur" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="/thema/rezensionen">Rezensionen</a></li> 
<li><a href="/thema/abgehoert">Abgehört</a></li> 
<li><a href="/kultur/literatur/">Literatur</a></li> 
<li><a href="/thema/tageskarte">KulturSPIEGEL-Tageskarte</a></li> 
<li><a href="/thema/matusseks_kulturtipp">Matusseks Kulturtipp</a></li> 
<li><a href="/thema/verstehen_sie_haas">Verstehen Sie Haas?</a></li> 
<li><a href="/thema/"><strong>alle Themen</strong></a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<div class="spArticleImageBox spAssetAlignleft" style="width: 92px;"> 
												<a href="/kultur/literatur/0,1518,715552,00.html" title="Harbour Front Festival: Leinen los für die Literatur"><img src="/images/image-126693-thumbsmall-ydaq.jpg" width="90" height="90" border="0" align="left" alt="Harbour Front Festival: Leinen los für die Literatur" title="Harbour Front Festival: Leinen los für die Literatur" /> 
												</a></div> 
										<h4> 
								<a href="/kultur/literatur/0,1518,715552,00.html" title="Harbour Front Festival: Leinen los für die Literatur">Harbour Front Festival: <span class="spBlk">Leinen los für die Literatur</span></a></h4> 
							<p>Mit einem Vorlesefestival soll das "kulturelle Angebot der Hafencity bereichert" werden: 114 Schriftsteller stellen bei 87 Veranstaltungen ihre Werke vor, unter anderem in Hamburgs umstrittenem Edelstadtteil. <span class="spAutorenzeile">Von Tobias Becker</span> <a href="/kultur/literatur/0,1518,715552,00.html" title="Harbour Front Festival: Leinen los für die Literatur">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="http://forum.spiegel.de/showthread.php?t=20346" target="_self">Forum</a>&nbsp;]</span></p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="/kultur/gesellschaft/0,1518,715855,00.html" title="Heute in den Feuilletons: &quot;Slum, Museum oder Bühne?&quot;">Heute in den Feuilletons: <span class="spBlk">"Slum, Museum oder Bühne?"</span></a></li><li> 
							<a href="/kultur/gesellschaft/0,1518,714005,00.html" title="Stadtentwicklung: Lob der Ramschmeile">Stadtentwicklung: <span class="spBlk">Lob der Ramschmeile</span></a></li><li> 
							<a href="/kultur/tv/0,1518,715749,00.html" title="Neue RTL-Show: Abwärts geht's immer">Neue RTL-Show: <span class="spBlk">Abwärts geht's immer</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_kultur"><a onclick="spHPTeaserToggle('kultur','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_kultur"> 
		<ul> 
			<li> 
							<a href="/kultur/gesellschaft/0,1518,715542,00.html" title="Dal-Bhat-Curry: Mein Fleisch ist Gemüse">Dal-Bhat-Curry: <span class="spBlk">Mein Fleisch ist Gemüse</span></a></li><li> 
							<a href="/kultur/tv/0,1518,715544,00.html" title="Abschied beim Frankfurter &quot;Tatort&quot;: Porzellanblass im Rotlichtsumpf">Abschied beim Frankfurter "Tatort": <span class="spBlk">Porzellanblass im Rotlichtsumpf</span></a></li><li> 
							<a href="/kultur/literatur/0,1518,715746,00.html" title="Pulitzerpreisträger: US-Karikaturist Paul Conrad gestorben">Pulitzerpreisträger: <span class="spBlk">US-Karikaturist Paul Conrad gestorben</span></a></li><li> 
							<a href="/kultur/gesellschaft/0,1518,715397,00.html" title="Hafencity-Aufführung: Punk oder Bank, das ist hier die Frage">Hafencity-Aufführung: <span class="spBlk">Punk oder Bank, das ist hier die Frage</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('kultur','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<script type="text/javascript"> 
				<!--
				OAS_RICH('Middle2');
				// -->
				</script><br class="spBreakNoHeight" clear="all" /> 
			<div class="spRessortTeaserBox wirtschaft spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="/wirtschaft/"> WIRTSCHAFT</a></h3> 
				 <h3 class="spNoBullet">&nbsp; &nbsp; <a href="http://boersen.manager-magazin.de/spo_mmo/"> BÖRSE</a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_wirtschaft"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/thema/finanzkrise_2007">Finanzkrise</a></li> 
<li><a href="/wirtschaft/service/">Service</a></li> 
<li><a onclick="spHpTopicBoxToggle('wirtschaft');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_wirtschaft">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('wirtschaft');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_wirtschaft"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_wirtschaft" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="/wirtschaft/unternehmen/">Unternehmen und Märkte</a></li> 
<li><a href="/wirtschaft/soziales/">Staat und Soziales</a></li> 
<li><a href="/thema/lebensmittelindustrie">Lebensmittelindustrie</a></li> 
<li><a href="/thema/gesundheitssystem_deutschland">Gesundheitssystem</a></li> 
<li><a href="/thema/energieversorgung">Energieversorgung</a></li> 
<li><a href="/thema/"><strong>alle Themen</strong></a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<h4> 
								<a href="/wirtschaft/soziales/0,1518,715827,00.html" title="Organisationsprobleme: Straßen-Maut für LKW verzögert sich">Organisationsprobleme: <span class="spBlk">Straßen-Maut für LKW verzögert sich</span></a></h4> 
							<p>Die Erweiterung der Lkw-Maut auf vierspurige Bundesstraßen wird offenbar nicht wie geplant zum 1. Januar 2011 eingeführt. Noch existiert nicht einmal eine vollständige Liste der Straßen, die davon erfasst werden sollen.  <a href="/wirtschaft/soziales/0,1518,715827,00.html" title="Organisationsprobleme: Straßen-Maut für LKW verzögert sich">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="http://forum.spiegel.de/showthread.php?t=20345" target="_self">Forum</a>&nbsp;]</span></p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="/wirtschaft/soziales/0,1518,715845,00.html" title="Konjunkturhilfe: Obama plant Steuersegen für forschende Firmen">Konjunkturhilfe: <span class="spBlk">Obama plant Steuersegen für forschende Firmen</span></a></li><li> 
							<a href="/wirtschaft/unternehmen/0,1518,715498,00.html" title="Wirtschaft in der Hauptstadt: Wie Berlin wieder boomen kann">Wirtschaft in der Hauptstadt: <span class="spBlk">Wie Berlin wieder boomen kann</span></a></li><li> 
							<a href="/wirtschaft/0,1518,715830,00.html" title="Wirtschaftsdebatte: Am Geld gesundet die Welt">Wirtschaftsdebatte: <span class="spBlk">Am Geld gesundet die Welt</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_wirtschaft"><a onclick="spHPTeaserToggle('wirtschaft','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_wirtschaft"> 
		<ul> 
			<li> 
							<a href="/wirtschaft/service/0,1518,715756,00.html" title="Immobilienmarkt: Mieten in Deutschland steigen rapide">Immobilienmarkt: <span class="spBlk">Mieten in Deutschland steigen rapide</span></a></li><li> 
							<a href="/wirtschaft/0,1518,715794,00.html" title="Projekt &quot;Stuttgart 21&quot;: Bahnhof-Abriss soll während Krisengipfel ruhen">Projekt "Stuttgart 21": <span class="spBlk">Bahnhof-Abriss soll während Krisengipfel ruhen</span></a></li><li> 
							<a href="/wirtschaft/unternehmen/0,1518,715795,00.html" title="Highstreet-Konsortium: Vermieter will Karstadt-Immobilien verkaufen">Highstreet-Konsortium: <span class="spBlk">Vermieter will Karstadt-Immobilien verkaufen</span></a></li><li> 
							<a href="/wirtschaft/service/0,1518,715750,00.html" title="Verbrauchertäuschung: Foodwatch-Chef wirft Ministerin Aigner Versagen vor">Verbrauchertäuschung: <span class="spBlk">Foodwatch-Chef wirft Ministerin Aigner Versagen vor</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('wirtschaft','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<div class="spRessortTeaserBox sport spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="/sport"> SPORT</a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_sport"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/sport/fussball/">Fußball</a></li> 
<li><a href="/sport/formel1/">Formel 1</a></li> 
<li><a href="/thema/achilles_verse">Achilles</a></li> 
<li><a onclick="spHpTopicBoxToggle('sport');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_sport">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('sport');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_sport"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_sport" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="/thema/flush_hour">Flush Hour</a></li> 
<li><a href="/thema/boxen">Boxen</a></li> 
<li><a href="/thema/doping">Doping</a></li> 
<li><a href="/thema/tennis">Tennis</a></li> 
<li><a href="/thema/bitte_laecheln">Bitte lächeln</a></li> 
<li><a href="/thema/"><strong>alle Themen</strong></a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<div class="spArticleImageBox spAssetAlignleft" style="width: 92px;"> 
												<a href="/sport/sonst/0,1518,715832,00.html" title="Mixed Zone: Überraschung bei den US Open, Hockey-Damen müssen zittern"><img src="/images/image-127732-thumbsmall-ibgw.jpg" width="90" height="90" border="0" align="left" alt="Mixed Zone: Überraschung bei den US Open, Hockey-Damen müssen zittern" title="Mixed Zone: Überraschung bei den US Open, Hockey-Damen müssen zittern" /> 
												</a></div> 
										<h4> 
								<a href="/sport/sonst/0,1518,715832,00.html" title="Mixed Zone: Überraschung bei den US Open, Hockey-Damen müssen zittern">Mixed Zone: <span class="spBlk">Überraschung bei den US Open, Hockey-Damen müssen zittern</span></a></h4> 
							<p>Favoritensturz bei den US Open: Der Schotte Andy Murray unterlag dem Schweizer Stanislaw Wawrinka. Die deutschen Hockey-Damen mussten eine unglückliche Niederlage hinnehmen, die Eisbären Berlin triumphierten bei der European-Trophy. <a href="/sport/sonst/0,1518,715832,00.html" title="Mixed Zone: Überraschung bei den US Open, Hockey-Damen müssen zittern">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="http://forum.spiegel.de/showthread.php?t=20352" target="_self">Forum</a>&nbsp;]</span></p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="/sport/sonst/0,1518,715802,00.html" title="Basketball-WM: Türkei und Slowenien erreichen das Viertelfinale">Basketball-WM: <span class="spBlk">Türkei und Slowenien erreichen das Viertelfinale</span></a></li><li> 
							<a href="/sport/sonst/0,1518,715806,00.html" title="US Open: Clijsters schlägt Ivanovic, Nadal locker weiter">US Open: <span class="spBlk">Clijsters schlägt Ivanovic, Nadal locker weiter</span></a></li><li> 
							<a href="/sport/fussball/0,1518,715779,00.html" title="DFB-Angreifer Podolski: Aufbaugegner für Löws Lieblingsschüler">DFB-Angreifer Podolski: <span class="spBlk">Aufbaugegner für Löws Lieblingsschüler</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_sport"><a onclick="spHPTeaserToggle('sport','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_sport"> 
		<ul> 
			<li> 
							<a href="/sport/sonst/0,1518,715811,00.html" title="Mixed Zone: Löwen in der Champions League, Sinkewitz gewinnt">Mixed Zone: <span class="spBlk">Löwen in der Champions League, Sinkewitz gewinnt</span></a></li><li> 
							<a href="/sport/fussball/0,1518,715797,00.html" title="Kurzpässe: Domenech gefeuert, Frankfurt macht Miese">Kurzpässe: <span class="spBlk">Domenech gefeuert, Frankfurt macht Miese</span></a></li><li> 
							<a href="/sport/sonst/0,1518,715783,00.html" title="Tödlicher Unfall: Motorradpilot Shoya Tomizawa gestorben">Tödlicher Unfall: <span class="spBlk">Motorradpilot Shoya Tomizawa gestorben</span></a></li><li> 
							<a href="/sport/sonst/0,1518,715416,00.html" title="Pokerweltmeister Eskeland: &quot;Ich wache jeden Morgen mit einem Lächeln auf&quot;">Pokerweltmeister Eskeland: <span class="spBlk">"Ich wache jeden Morgen mit einem Lächeln auf"</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('sport','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<div class="spRessortTeaserBox netzwelt spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="/netzwelt"> NETZWELT</a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_netzwelt"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/netzwelt/netzpolitik/">Netzpolitik</a></li> 
<li><a href="/netzwelt/web/">Web</a></li> 
<li><a href="/netzwelt/gadgets/">Gadgets</a></li> 
<li><a onclick="spHpTopicBoxToggle('netzwelt');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_netzwelt">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('netzwelt');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_netzwelt"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_netzwelt" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="/thema/umbruch_der_medienwelt">Medienwandel</a></li> 
<li><a href="/thema/angefasst">Angefasst</a></li> 
<li><a href="/thema/fehlfunktion">Fehlfunktion</a></li> 
<li><a href="/netzwelt/games/">Games</a></li> 
<li><a href="/thema/bilderwelten">Bilderwelten</a></li> 
<li><a href="/thema/silberscheiben">Silberscheiben</a></li> 
<li><a href="/thema/"><strong>alle Themen</strong></a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<h4> 
								<a href="/netzwelt/netzpolitik/0,1518,715805,00.html" title="Vorwürfe der Konkurrenz: US-Justiz prüft Reihenfolge von Google-Treffern">Vorwürfe der Konkurrenz: <span class="spBlk">US-Justiz prüft Reihenfolge von Google-Treffern</span></a></h4> 
							<p>Benachteiligt Google seine Konkurrenten in Trefferlisten? Nach mehreren Beschwerden von Wettbewerbern untersucht die US-Justiz nun die Reihenfolge der Suchergebnisse bei dem Internetriesen. Google jedoch weist alle Schuld von sich - und vermutet Microsoft hinter den Anschuldigungen. <a href="/netzwelt/netzpolitik/0,1518,715805,00.html" title="Vorwürfe der Konkurrenz: US-Justiz prüft Reihenfolge von Google-Treffern">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="http://forum.spiegel.de/showthread.php?t=20337" target="_self">Forum</a>&nbsp;]</span></p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="/netzwelt/web/0,1518,715774,00.html" title="Kleinanzeigen-Portal: Craigslist blockiert Rubrik mit Erotik-Dienstleistungen">Kleinanzeigen-Portal: <span class="spBlk">Craigslist blockiert Rubrik mit Erotik-Dienstleistungen</span></a></li><li> 
							<a href="/netzwelt/games/0,1518,714307,00.html" title="&quot;Half-Life&quot;: Der Science-Fiction-Klassiker der Spielgeschichte">"Half-Life": <span class="spBlk">Der Science-Fiction-Klassiker der Spielgeschichte</span></a></li><li> 
							<a href="/netzwelt/gadgets/0,1518,715492,00.html" title="Weiße Ware auf der Ifa: Ein Bügeleisen mit vier PS">Weiße Ware auf der Ifa: <span class="spBlk">Ein Bügeleisen mit vier PS</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_netzwelt"><a onclick="spHPTeaserToggle('netzwelt','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_netzwelt"> 
		<ul> 
			<li> 
							<a href="/spiegelwissen/0,1518,715348,00.html" title="Schatzkammern des Wissens: Künstliches Gedächtnis">Schatzkammern des Wissens: <span class="spBlk">Künstliches Gedächtnis</span></a></li><li> 
							<a href="/video/video-1082294.html" title="IFA 2010: Das Galaxy Tablet von Samsung im Videotest">IFA 2010: <span class="spBlk">Das Galaxy Tablet von Samsung im Videotest</span></a></li><li> 
							<a href="/netzwelt/web/0,1518,715478,00.html" title="Netzwelt-Ticker: Telekom verliert angeblich iPhone-Exklusivrechte">Netzwelt-Ticker: <span class="spBlk">Telekom verliert angeblich iPhone-Exklusivrechte</span></a></li><li> 
							<a href="/netzwelt/web/0,1518,715558,00.html" title="Konkurrenz unter Medien: AFP will Nachrichten auch direkt anbieten">Konkurrenz unter Medien: <span class="spBlk">AFP will Nachrichten auch direkt anbieten</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('netzwelt','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<div class="spRessortTeaserBox einestages spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="http://einestages.spiegel.de/"><img src="/static/sys/v9/bg/bg_einestagesheader.gif"  border="0" alt="einestages" /></a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_einestages"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="http://einestages.spiegel.de/page/TimeMachine.html">Zeitmaschine</a></li> 
<li><a href="http://einestages.spiegel.de/rubrik/Deutsche-Heiligtuemer">Deutsche Heiligtümer</a></li> 
<li><a onclick="spHpTopicBoxToggle('einestages');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_einestages">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('einestages');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_einestages"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_einestages" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="http://einestages.spiegel.de/rubrik/60-Jahre-Bundesrepublik">60 Jahre BRD</a></li> 
<li><a href="http://einestages.spiegel.de/rubrik/20-Jahre-Mauerfall">20 Jahre Mauerfall</a></li> 
<li><a href="http://einestages.spiegel.de/rubrik/Film">Film</a></li> 
<li><a href="http://einestages.spiegel.de/rubrik/Fernsehen">Fernsehen</a></li> 
<li><a href="http://einestages.spiegel.de/rubrik/Fotografie">Fotografie</a></li> 
<li><a href="http://einestages.spiegel.de/page/LostAndFound.html">Fundbüro</a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<div class="spArticleImageBox spAssetAlignleft" style="width: 92px;"> 
												<a href="http://einestages.spiegel.de/static/topicalbumbackground/14321/_weltmeister_oder_tot.html" target="_self" title="Formel-1-Legende Jochen Rindt: Todesfahrt zum Titel"><img src="/images/image-127333-thumbsmall-jnwv.jpg" width="90" height="90" border="0" align="left" alt="Formel-1-Legende Jochen Rindt: Todesfahrt zum Titel" title="Formel-1-Legende Jochen Rindt: Todesfahrt zum Titel" /> 
												</a></div> 
										<h4> 
								<a href="http://einestages.spiegel.de/static/topicalbumbackground/14321/_weltmeister_oder_tot.html" target="_self" title="Formel-1-Legende Jochen Rindt: Todesfahrt zum Titel">Formel-1-Legende Jochen Rindt: <span class="spBlk">Todesfahrt zum Titel</span></a></h4> 
							<p>Er fuhr wie ein Gott und liebte das Risiko - am Ende zu sehr: Vor 40 Jahren endete in den Leitplanken von Monza das Leben und die kometenhafte Karriere von Jochen Rindt. e<i>inestages </i>erinnert an den Ausnahmefahrer einer Zeit, als die Formel 1 noch Abenteuer war. <span class="spAutorenzeile">Von Martin Rupps</span> <a href="http://einestages.spiegel.de/static/topicalbumbackground/14321/_weltmeister_oder_tot.html" target="_self" title="Formel-1-Legende Jochen Rindt: Todesfahrt zum Titel">mehr...</a> </p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="http://einestages.spiegel.de/static/topicalbumbackground/14281/die_geburt_des_groessten.html" target="_self" title="Cassius Clays Olympiasieg: Geburt des Größten">Cassius Clays Olympiasieg: <span class="spBlk">Geburt des Größten</span></a></li><li> 
							<a href="http://einestages.spiegel.de/static/topicalbumbackground/14301/die_vergessenen_haudegen.html" target="_self" title="Kriegsende in der Arktis: Die vergessenen Haudegen">Kriegsende in der Arktis: <span class="spBlk">Die vergessenen Haudegen</span></a></li><li> 
							<a href="http://einestages.spiegel.de/static/topicalbumbackground/13881/_echte_germanen.html" target="_self" title="Lektüre der Top-Nazis: Echte Germanen">Lektüre der Top-Nazis: <span class="spBlk">"Echte Germanen"</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_einestages"><a onclick="spHPTeaserToggle('einestages','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_einestages"> 
		<ul> 
			<li> 
							<a href="http://einestages.spiegel.de/static/topicalbumbackground/14181/swingerclub_im_grunewald.html" target="_self" title="Polizeiprotokolle aus der Kaiserzeit: Swingerclub im Grunewald">Polizeiprotokolle aus der Kaiserzeit: <span class="spBlk">Swingerclub im Grunewald</span></a></li><li> 
							<a href="http://einestages.spiegel.de/static/topicalbumbackground/14221/schatz_das_haus_ist_fertig.html" target="_self" title="Aufstieg des Fertighauses: Budenzauber aus der Konserve">Aufstieg des Fertighauses: <span class="spBlk">Budenzauber aus der Konserve</span></a></li><li> 
							<a href="http://einestages.spiegel.de/static/topicalbumbackground/13821/brust_raus_bauch_rein.html" target="_self" title="TV-Serie Baywatch: Brust raus, Bauch rein!">TV-Serie "Baywatch": <span class="spBlk">Brust raus, Bauch rein!</span></a></li><li> 
							<a href="http://einestages.spiegel.de/static/authoralbumbackground/13861/die_goldene_nase.html" target="_self" title="Showlegende Mike Krüger: Die Goldene Nase">Showlegende Mike Krüger: <span class="spBlk">Die Goldene Nase</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('einestages','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<script type="text/javascript"> 
				<!--
				OAS_RICH('Middle3');
				// -->
				</script><br class="spBreakNoHeight" clear="all" /> 
			<div class="spRessortTeaserBox reise spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="/reise"> REISE</a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_reise"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/reise/staedte/">Städte</a></li> 
<li><a href="/reise/deutschland/">Deutschland</a></li> 
<li><a href="/reise/fernweh/">Fernweh</a></li> 
<li><a onclick="spHpTopicBoxToggle('reise');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_reise">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('reise');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_reise"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_reise" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="/reise/europa/">Europa</a></li> 
<li><a href="http://km42.spiegel.de/">km42</a></li> 
<li><a href="/thema/reiserecht">Reiserecht</a></li> 
<li><a href="/thema/familienreisen">Familienreisen</a></li> 
<li><a href="/thema/warum_wir_reisen">Warum wir reisen</a></li> 
<li><a href="/thema/"><strong>alle Themen</strong></a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<h4> 
								<a href="/reise/aktuell/0,1518,713981,00.html" title="Moskau-St.Petersburg: Mit Tempo 250 durch Russlands Dörfer">Moskau-St.Petersburg: <span class="spBlk">Mit Tempo 250 durch Russlands Dörfer</span></a></h4> 
							<p>Russland ist stolz auf seinen Hochgeschwindigkeitszug deutscher Bauart - doch der Hightech-Flitzer gefährdet das Leben von Menschen an der Strecke Moskau-St. Petersburg. Die Bahnanlagen ihrer Dörfer sind veraltet. Wenn Moderne auf Marodes trifft: SPIEGEL TV besuchte die Betroffenen. <a href="/reise/aktuell/0,1518,713981,00.html" title="Moskau-St.Petersburg: Mit Tempo 250 durch Russlands Dörfer">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="/video/video-1081547.html" onclick="return spShowVideo(this,'1081547','10')" title="Hass auf den deutschen Hightech-Zug">Video</a> | <a href="http://forum.spiegel.de/showthread.php?t=20326" target="_self">Forum</a>&nbsp;]</span></p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="/reise/aktuell/0,1518,715800,00.html" title="Neue Steuer: Deutsche Fluglinien geben Ticketabgabe an Kunden weiter">Neue Steuer: <span class="spBlk">Deutsche Fluglinien geben Ticketabgabe an Kunden weiter</span></a></li><li> 
							<a href="/reise/aktuell/0,1518,715781,00.html" title="Neuseeland: Neun Menschen sterben bei Flugzeugabsturz">Neuseeland: <span class="spBlk">Neun Menschen sterben bei Flugzeugabsturz</span></a></li><li> 
							<a href="/reise/fernweh/0,1518,715383,00.html" title="Südseeinsel Tuvalu: Trip mit Katastrophen-Flair">Südseeinsel Tuvalu: <span class="spBlk">Trip mit Katastrophen-Flair</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_reise"><a onclick="spHPTeaserToggle('reise','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_reise"> 
		<ul> 
			<li> 
							<a href="/reise/fernweh/0,1518,715026,00.html" title="Antarktis-Kreuzfahrt: Pinguinautobahn durchs ewige Eis">Antarktis-Kreuzfahrt: <span class="spBlk">Pinguinautobahn durchs ewige Eis</span></a></li><li> 
							<a href="/reise/aktuell/0,1518,715588,00.html" title="Flugsteuer: Mallorcas Behörden protestieren gegen Abgabe">Flugsteuer: <span class="spBlk">Mallorcas Behörden protestieren gegen Abgabe</span></a></li><li> 
							<a href="/reise/aktuell/0,1518,715338,00.html" title="Auszeichnung: Wanderverband ernennt vier neue Qualitätswege">Auszeichnung: <span class="spBlk">Wanderverband ernennt vier neue Qualitätswege</span></a></li><li> 
							<a href="/reise/fernweh/0,1518,713737,00.html" title="Harry-Potter-Freizeitpark: Zauberhaft speisen">Harry-Potter-Freizeitpark: <span class="spBlk">Zauberhaft speisen</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('reise','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<div class="spRessortTeaserBox wissenschaft spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="/wissenschaft"> WISSENSCHAFT</a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_wissenschaft"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/wissenschaft/natur/">Natur</a></li> 
<li><a href="/wissenschaft/technik/">Technik</a></li> 
<li><a href="/thema/klimawandel">Klimawandel</a></li> 
<li><a onclick="spHpTopicBoxToggle('wissenschaft');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_wissenschaft">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('wissenschaft');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_wissenschaft"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_wissenschaft" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="/wissenschaft/weltall/">Weltall</a></li> 
<li><a href="/wissenschaft/medizin/">Medizin</a></li> 
<li><a href="/thema/schweinegrippe">Schweinegrippe</a></li> 
<li><a href="/thema/satellitenbild_der_woche">Satellitenbilder</a></li> 
<li><a href="/thema/graf_seismo">Graf Seismo</a></li> 
<li><a href="/thema/archaeologie">Archäologie</a></li> 
<li><a href="/thema/"><strong>alle Themen</strong></a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<div class="spArticleImageBox spAssetAlignleft" style="width: 92px;"> 
												<a href="/wissenschaft/medizin/0,1518,715846,00.html" title="Medizin: Probleme mit künstlichen Gelenken nehmen zu"><img src="/images/image-119779-thumbsmall-vrxt.jpg" width="90" height="90" border="0" align="left" alt="Medizin: Probleme mit künstlichen Gelenken nehmen zu" title="Medizin: Probleme mit künstlichen Gelenken nehmen zu" /> 
												</a></div> 
										<h4> 
								<a href="/wissenschaft/medizin/0,1518,715846,00.html" title="Medizin: Probleme mit künstlichen Gelenken nehmen zu">Medizin: <span class="spBlk">Probleme mit künstlichen Gelenken nehmen zu</span></a></h4> 
							<p>Mit zunehmendem Alter machen die Gelenke immer mehr Probleme. Oft kann eine Hüft- oder Knieprothese den Patienten helfen. Doch die dabei auftretenden Komplikationen häufen sich. Bei Entzündungen oder Materialverschleiß muss der Gelenkersatz ausgetauscht werden. <a href="/wissenschaft/medizin/0,1518,715846,00.html" title="Medizin: Probleme mit künstlichen Gelenken nehmen zu">mehr...</a> </p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="/wissenschaft/mensch/0,1518,715762,00.html" title="Countdown bei Bornholm: Dänische Bastlerrakete verpatzt Flugpremiere">Countdown bei Bornholm: <span class="spBlk">Dänische Bastlerrakete verpatzt Flugpremiere</span></a></li><li> 
							<a href="/wissenschaft/mensch/0,1518,715765,00.html" title="Fund in Vorpommern: Archäologen heben Silberschatz aus dem Frühmittelalter">Fund in Vorpommern: <span class="spBlk">Archäologen heben Silberschatz aus dem Frühmittelalter</span></a></li><li> 
							<a href="/wissenschaft/natur/0,1518,715755,00.html" title="Christchurch: 30 Nachbeben erschüttern neuseeländische Stadt">Christchurch: <span class="spBlk">30 Nachbeben erschüttern neuseeländische Stadt</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_wissenschaft"><a onclick="spHPTeaserToggle('wissenschaft','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_wissenschaft"> 
		<ul> 
			<li> 
							<a href="/wissenschaft/mensch/0,1518,715169,00.html" title="Samoa: Die Illusion von der Südsee-Idylle">Samoa: <span class="spBlk">Die Illusion von der Südsee-Idylle</span></a></li><li> 
							<a href="/wissenschaft/natur/0,1518,715767,00.html" title="Ölpest im Golf von Mexiko: Ingenieure bergen defektes Absperrventil">Ölpest im Golf von Mexiko: <span class="spBlk">Ingenieure bergen defektes Absperrventil</span></a></li><li> 
							<a href="/wissenschaft/natur/0,1518,715698,00.html" title="Erdstöße in Neuseeland: Warum die Menschen dem Beben entkamen">Erdstöße in Neuseeland: <span class="spBlk">Warum die Menschen dem Beben entkamen</span></a></li><li> 
							<a href="/wissenschaft/natur/0,1518,715515,00.html" title="Satellitenbild der Woche: Größter Eisberg des Nordens rammt Insel">Satellitenbild der Woche: <span class="spBlk">Größter Eisberg des Nordens rammt Insel</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('wissenschaft','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<div class="spRessortTeaserBox uniundschule spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="/unispiegel/"> UNISPIEGEL</a></h3> 
			
				<h3 class="spNoBullet">&nbsp; &nbsp; <a href="/schulspiegel/"> SCHULSPIEGEL</a></h3> 
				
			<div class="spRessortBoxNav" id="spRessortBoxNav_uniundschule"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/unispiegel/jobundberuf/">Job &amp; Beruf</a></li> 
<li><a onclick="spHpTopicBoxToggle('uniundschule');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_uniundschule">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('uniundschule');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_uniundschule"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_uniundschule" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="/unispiegel/studium/">Studium</a></li> 
<li><a href="/unispiegel/wunderbar/">Wunderbar</a></li> 
<li><a href="/schulspiegel/abi/">Abi - und dann?</a></li> 
<li><a href="/schulspiegel/ausland/">Querweltein</a></li> 
<li><a href="/schulspiegel/leben/">Leben U21</a></li> 
<li><a href="/schulspiegel/wissen/">Wissen</a></li> 
<li><a href="/thema/"><strong>alle Themen</strong></a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<h4> 
								<a href="/unispiegel/jobundberuf/0,1518,714247,00.html" title="Exzellenzinitiative: So fördern Deutschlands Wettbewerber die Forscher-Elite">Exzellenzinitiative: <span class="spBlk">So fördern Deutschlands Wettbewerber die Forscher-Elite</span></a></h4> 
							<p>Um sehr viel Geld geht es den deutschen Unis in der Exzellenzinitiative. Doch auch andere Forschernationen fördern ihre besten Hochschulen. Das Magazin "duz" zeigt, wie anderswo mit Milliarden aufgerüstet wird. Manche Länder gehen dabei ganz eigene Wege. <span class="spAutorenzeile">Von Christine Xuan Müller</span> <a href="/unispiegel/jobundberuf/0,1518,714247,00.html" title="Exzellenzinitiative: So fördern Deutschlands Wettbewerber die Forscher-Elite">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="http://forum.spiegel.de/showthread.php?t=20350" target="_self">Forum</a>&nbsp;]</span></p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="/schulspiegel/leben/0,1518,715362,00.html" title="Jugendliche beim Sorgentelefon: Hier werden sie geholfen">Jugendliche beim Sorgentelefon: <span class="spBlk">Hier werden sie geholfen</span></a></li><li> 
							<a href="/deinspiegel/0,1518,712071,00.html" title="Kinderleben auf der Kirmes: An Abschiede habe ich mich mittlerweile gewöhnt">Kinderleben auf der Kirmes: <span class="spBlk">"An Abschiede habe ich mich mittlerweile gewöhnt"</span></a></li><li> 
							<a href="/unispiegel/studium/0,1518,715422,00.html" title="Studieren in Sibirien: Im Land der schönsten Frau und des größten Diamanten">Studieren in Sibirien: <span class="spBlk">Im Land der schönsten Frau und des größten Diamanten</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_uniundschule"><a onclick="spHPTeaserToggle('uniundschule','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_uniundschule"> 
		<ul> 
			<li> 
							<a href="/unispiegel/studium/0,1518,715590,00.html" title="Start der Exzellenzinitiative: Deutschland sucht die Super-Unis">Start der Exzellenzinitiative: <span class="spBlk">Deutschland sucht die Super-Unis</span></a></li><li> 
							<a href="/unispiegel/studium/0,1518,715567,00.html" title="Richtig bewerben: Ein Stipendium, das zu mir passt">Richtig bewerben: <span class="spBlk">Ein Stipendium, das zu mir passt</span></a></li><li> 
							<a href="/schulspiegel/wissen/0,1518,715103,00.html" title="Jugendsex-Studie: Keusche Kuschler">Jugendsex-Studie: <span class="spBlk">Keusche Kuschler</span></a></li><li> 
							<a href="/schulspiegel/wissen/0,1518,715271,00.html" title="Längeres gemeinsames Lernen: Saar-SPD sagt nein zum fünften Grundschuljahr">Längeres gemeinsames Lernen: <span class="spBlk">Saar-SPD sagt nein zum fünften Grundschuljahr</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('uniundschule','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<div class="spRessortTeaserBox auto spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="/auto"> AUTO</a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_auto"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="/auto/fahrberichte/">Tests</a></li> 
<li><a href="/thema/pariser_autosalon/">Pariser Salon 2010</a></li> 
<li><a href="/thema/schraege_schilder">Schräge Schilder</a></li> 
<li><a onclick="spHpTopicBoxToggle('auto');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_auto">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('auto');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_auto"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_auto" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="/thema/abgewuergt">Abgewürgt</a></li> 
<li><a href="/thema/altmetall">Altmetall</a></li> 
<li><a href="/thema/schoene_autorouten">Schöne Autorouten</a></li> 
<li><a href="/auto/fahrkultur/">Fahrkultur</a></li> 
<li><a href="/thema/"><strong>alle Themen</strong></a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<div class="spArticleImageBox spAssetAlignleft" style="width: 92px;"> 
												<a href="/auto/fahrberichte/0,1518,714957,00.html" title="VW Phaeton: Luxuriöser Ladenhüter"><img src="/images/image-125726-thumbsmall-kdgk.jpg" width="90" height="90" border="0" align="left" alt="VW Phaeton: Luxuriöser Ladenhüter" title="VW Phaeton: Luxuriöser Ladenhüter" /> 
												</a></div> 
										<h4> 
								<a href="/auto/fahrberichte/0,1518,714957,00.html" title="VW Phaeton: Luxuriöser Ladenhüter">VW Phaeton: <span class="spBlk">Luxuriöser Ladenhüter</span></a></h4> 
							<p>Der VW Phaeton sollte nicht weniger werden als das beste Auto der Welt. Allerdings erwartete kein Mensch eine derartige Luxuslimousine von Volkswagen. Doch weil der Prunkwagen ein Lieblingsprojekt von VW-Patriarch Ferdinand Piëch ist, wurde er jetzt weiter aufpoliert. <span class="spAutorenzeile">Von Tom Grünweg</span> <a href="/auto/fahrberichte/0,1518,714957,00.html" title="VW Phaeton: Luxuriöser Ladenhüter">mehr...</a> <span class="spInteractionMarks">[&nbsp;<a href="http://forum.spiegel.de/showthread.php?t=20347" target="_self">Forum</a>&nbsp;]</span></p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="/auto/fahrberichte/0,1518,715516,00.html" title="Volvo V60: Der Power-Riegel">Volvo V60: <span class="spBlk">Der Power-Riegel</span></a></li><li> 
							<a href="/auto/aktuell/0,1518,715245,00.html" title="Mazda-Studie Shinari: Speedy gibt die Form vor">Mazda-Studie Shinari: <span class="spBlk">Speedy gibt die Form vor</span></a></li><li> 
							<a href="/auto/aktuell/0,1518,715526,00.html" title="Bundesverfassungsgericht: Video-Verkehrskontrollen bei konkretem Verdacht erlaubt">Bundesverfassungsgericht: <span class="spBlk">Video-Verkehrskontrollen bei konkretem Verdacht erlaubt</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_auto"><a onclick="spHPTeaserToggle('auto','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_auto"> 
		<ul> 
			<li> 
							<a href="/spiegel/0,1518,714838,00.html" title="Neuerfindung der Fahrradschaltung: Revolution in 18 Gängen">Neuerfindung der Fahrradschaltung: <span class="spBlk">Revolution in 18 Gängen</span></a></li><li> 
							<a href="/auto/aktuell/0,1518,714930,00.html" title="Modellauto Lamborghini Reventon: Rasen auf Raten">Modellauto Lamborghini Reventon: <span class="spBlk">Rasen auf Raten</span></a></li><li> 
							<a href="/auto/aktuell/0,1518,715357,00.html" title="Chinas Verkehrsprobleme: Der Superstau ist wieder da">Chinas Verkehrsprobleme: <span class="spBlk">Der Superstau ist wieder da</span></a></li><li> 
							<a href="/auto/aktuell/0,1518,715246,00.html" title="Flambierte Flitzer: Ferrari ruft Hunderte Supersportwagen zurück">Flambierte Flitzer: <span class="spBlk">Ferrari ruft Hunderte Supersportwagen zurück</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('auto','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<div class="spRessortTeaserBox merian spClearfix"> 
	<div class="spRessortBoxHeader"> 
		
		<h3 class="spBlockBullet"><a href="http://www.merian.de/"><img src="/static/sys/v9/bg/bg_merianheader.gif"  border="0" alt="Merian" /></a></h3> 
			<div class="spRessortBoxNav" id="spRessortBoxNav_merian"> 
<ul class="spTabs"> 
<li class="spFirstLink"><a href="http://www.merian.de/reiseziele#ref=reiseziele">Reiseziele</a></li> 
<li><a href="http://www.merian.de/fotos#ref=fotos">Fotos</a></li> 
<li><a href="http://www.merian.de/video#ref=videos">Videos</a></li> 
<li><a onclick="spHpTopicBoxToggle('merian');" href="javascript:void(0);" id="spTopicBoxToggleLinkMore_merian">mehr</a></li> 
</ul> 
<a onclick="spHpTopicBoxToggle('merian');" href="javascript:void(0);" class="spSubjectLink"> 
<img alt="" src="/static/sys/v9/icons/ic_morestories.gif" id="spTopicBoxToggleLink_merian"/> 
</a> 
<div class="spSubjectBox" id="spSubjectBox_merian" style="display: none;"> 
<div class="spSubjectBGTop"></div> 
<div class="spSubjectBG"> 
<ul> 
<li><a href="http://www.merian.de/reiseziele/ziel/new_york#ref=newyork">New York</a></li> 
<li><a href="http://www.merian.de/reiseziele/ziel/kanarische_inseln#ref=kanaren">Kanaren</a></li> 
<li><a href="http://www.merian.de/reiseziele/ziel/hollywood#ref=hollywood">Hollywood</a></li> 
<li><a href="http://www.merian.de/reiseziele/ziel/marokko#ref=marokko">Marokko</a></li> 
</ul> 
<br /> 
</div> 
</div> 
</div></div> 
	
	<div class="spRessortTeaserBoxTop"> 
							<div class="spArticleImageBox spAssetAlignleft" style="width: 92px;"> 
												<a href="http://www.merian.de/reiseziele/artikel/a-714561.html" title="Barcelona: Heißes Pflaster hinterm Strand"><img src="/images/image-125567-thumbsmall-pdtg.jpg" width="90" height="90" border="0" align="left" alt="Barcelona: Heißes Pflaster hinterm Strand" title="Barcelona: Heißes Pflaster hinterm Strand" /> 
												</a></div> 
										<h4> 
								<a href="http://www.merian.de/reiseziele/artikel/a-714561.html" title="Barcelona: Heißes Pflaster hinterm Strand">Barcelona: <span class="spBlk">Heißes Pflaster hinterm Strand</span></a></h4> 
							<p>Luxus-Yachten, riesige Palmen, teure Mietswohnungen: Das einst schäbige Fischerviertel Barceloneta wandelt sich zum Miami Europas. Die Alteingesessenen sehen den Veränderungen mit Wut und Wehmut zu. <i>Von MERIAN-Autorin Dorothea Massmann</i> 
 <a href="http://www.merian.de/reiseziele/artikel/a-714561.html" title="Barcelona: Heißes Pflaster hinterm Strand">mehr...</a> </p> 
						</div><div class="spRessortTeaserBoxList"> 
		<ul> 
			<li> 
							<a href="http://www.merian.de/reiseziele/artikel/a-714201.html" title="US-Spukhotel: Wahnsinn in der Endlosschleife">US-Spukhotel: <span class="spBlk">Wahnsinn in der Endlosschleife</span></a></li><li> 
							<a href="http://www.merian.de/kolumnen/mit-stil/a-715474.html" title="Mit Stil: Aufs falsche Pferd gesetzt">Mit Stil: <span class="spBlk">Aufs falsche Pferd gesetzt</span></a></li><li> 
							<a href="http://www.merian.de/reiseziele/artikel/a-715140.html" title="Deutsche Inseln: Schicken Sie uns Ihre Bilder!">Deutsche Inseln: <span class="spBlk">Schicken Sie uns Ihre Bilder!</span></a></li></ul> 
	</div> 
	<div class="spRessortTeaserMore" style="display:none;" id="spRTless_merian"><a onclick="spHPTeaserToggle('merian','more',1); return false;" href="#">mehr Artikel</a></div> 
	<div class="spRessortTeaserBoxList" style="display:none;" id="spRTmore_merian"> 
		<ul> 
			<li> 
							<a href="http://www.merian.de/reiseziele/artikel/a-714150.html" title="Fehmarn in Fotos: Die schönste Stadt im Norden">Fehmarn in Fotos: <span class="spBlk">Die schönste Stadt im Norden</span></a></li><li> 
							<a href="http://www.merian.de/reiseziele/artikel/a-712517.html" title="Schwarzwaldthermen: Durchlauferhitzer mit Geschichte">Schwarzwaldthermen: <span class="spBlk">Durchlauferhitzer mit Geschichte</span></a></li><li> 
							<a href="http://www.merian.de/kolumnen/fast-lane/a-715011.html" title="Fast Lane: Der totale Freizeitstress">Fast Lane: <span class="spBlk">Der totale Freizeitstress</span></a></li><li> 
							<a href="http://www.merian.de/reiseziele/artikel/a-714094.html" title="Segeln in Dänemark: Südsee mit Fachwerk-Häusern">Segeln in Dänemark: <span class="spBlk">Südsee mit Fachwerk-Häusern</span></a></li></ul> 
		<div class="spRessortTeaserLess"><a onclick="spHPTeaserToggle('merian','less',1); return false;" href="#">weniger Artikel</a></div> 
	</div> 
		
</div> 
<div class="spRessortTeaserBox spam spClearfix"> 
	<div class="spRessortTeaserBoxHPHeadline"> 
		<h3><a href="/spam/"><img src="/static/sys/v9/bg/bg_spamheader.jpg" alt="SPAM - SATIRE @ SPIEGEL ONLINESPAM - SATIRE @ SPIEGEL ONLINE"> </a></h3> 
	</div> 
 
	<div class="spArticleImageBox spAssetAlignleft" style="width: 92px"> 
					<a href="/spam/"><img src="http://www.spiegel.de/img/0,1020,3041515,00.jpg" width="90" height="90" border="0" hspace="0" alt="" title="" /></a> 
				</div> 
			<div class="spRessortTeaserBoxList"> 
		<ul> 
			<li><a href="/spam/">ZDF plant Verfilmung</a></li> 
					<li><a href="/spam/">Die besten Hauck&Bauer-Witze</a></li> 
					<li><a href="/spam/">Ganz neue Mathematik entdeckt!</a></li> 
					<li><a href="/spam/">Ein Schild sagt mehr als 1000 Worte...</a></li> 
					</ul> 
	</div> 
</div><script type="text/javascript"> 
	<!--
		spHPTeaserInit('panorama politik kultur wirtschaft sport netzwelt einestages reise wissenschaft uniundschule auto merian');
	// -->
	</script> 
</div> 
			<div id="spBoxColumn"> 
 
				<div class="spInfoBox"> 
<h4><a href="/video/">VIDEO</a></h4> 
<div class="spHPVideoNewsBox"> 
<div class="spVideoNewsTop">	<div class="spColumnBoxPic"> 
<div class="spVideoPic" style="width:114px; height:86px;"><a href="/video/video-1082620.html#oas.videobelegung=news" onclick="return spShowVideo(this,'1082620','44')"><img src="/images/image-127701-videothumbplayer-ipbb.jpg" width="112" height="84" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
<div style="width:114px;"> 
<div align="right" class="spCredit">dpa</div> 
</div> 
</div> 
<a onclick="return spShowVideo(this,'1082620','44')" href="/video/video-1082620.html#oas.videobelegung=news"><strong>Videobotschaft:</strong> <span>Eta will den bewaffneten Kampf beenden</span></a><br clear="all" /></div> 
<ul class="spHPVideoNewsList"> 
<li><a onclick="return spShowVideo(this,'1082615','')" href="/video/video-1082615.html#oas.videobelegung=news"><strong>Absturz eines Doppeldeckers:</strong> 
<span>Unglück bei Flugshow</span> 
</a></li> 
<li><a onclick="return spShowVideo(this,'1082613','')" href="/video/video-1082613.html#oas.videobelegung=news"><strong>Atom-Konsens:</strong> 
<span>Koalition einigt sich auf gestaffelte Laufzeitverlängerung</span> 
</a></li> 
</ul> 
<div class="spHPVideoNewsLink"><a href="/video/">alle Videos</a></div> 
</div> 
</div> 
<script type="text/javascript"> 
<!--
OAS_RICH('Right1');
// -->
</script><br class="spBreakNoHeight" clear="all" /><div class="spInfoBox"> 
<h4><a href="/video/">KICKER TV</a></h4> 
<div class="spHPVideoNewsBox"> 
<div class="spVideoNewsTop">	<div class="spColumnBoxPic"> 
<div class="spVideoPic" style="width:114px; height:86px;"><a href="/video/video-1082630.html" onclick="return spShowVideo(this,'1082630','1')"><img src="/images/image-127702-videothumbplayer-vhjb.jpg" width="112" height="84" border="0" /><img class="spIEsixPng" src="http://www.spiegel.de/static/sys/v8/buttons/play-trans_37x37.png" title="Video abspielen..." border="0" height="37" width="37" alt="" /></a></div> 
<div style="width:114px;"> 
<div align="right" class="spCredit">dpa</div> 
</div> 
</div> 
<a onclick="return spShowVideo(this,'1082630','1')" href="/video/video-1082630.html"><strong>kicker.tv:</strong> <span>Miroslav Klose jagt Gerd Müller</span></a><br clear="all" /></div> 
<ul class="spHPVideoNewsList"> 
</ul> 
<div class="spHPVideoNewsLink"><a href="/video/">alle Videos</a></div> 
</div> 
</div> 
<div class="spInfoBox"> 
<h4><a href="http://www.spiegel.de/home/seite2/">SEITE 2</a></h4> 
<div class="spArticleImageBox spAssetAlignright" style="width: 122px"> 
<a href="http://www.spiegel.de/home/seite2/" target="_top"><img src="/images/image-127332-hpcpleftcolumn-naou.jpg" width="120" border="0" alt="" hspace="0" align="right" /></a> 
</div> 
<p> 
<a href="http://www.spiegel.de/home/seite2/"><strong>Formel-<span class="spOptiBreak"> </span>1-Legende Jochen Rindt:</strong> <span>Todesfahrt zum Titel</span></a> 
</p> 
<p> 
<a href="/home/seite2/"> 
<span class="blk">Außerdem: Wie der Bundespräsident zwischen die </span><b>Sarrazin-Fronten</b><span class="blk"> gerät - und das Erfolgsrezept von </span><b>Poker-Weltmeister </b><span class="blk">Sigurd Eskeland</span><b></b><span class="blk"></span></a></p> 
<br clear="all" /> 
<div class="spSeite2"> 
<b> 
<a href="http://www.spiegel.de/home/seite2/">Das Beste aus 24 Stunden SPIEGEL ONLINE</a> 
</b> 
</div> 
</div> 
<div class="spInfoBox"> 
<h4><a href="/dienste/0,1518,675533,00.html">DER SPIEGEL ALS APP</a></h4> 
<a href="/dienste/0,1518,675533,00.html" target="_top"><img src="/img/0,1020,2642013,00.jpg" width="136" height="109" border="0" alt="" align="right" hspace="0" class=" spImgNoBorder" /></a> 
<a href="/dienste/0,1518,675533,00.html"><strong>Deutschlands Nachrichten-<span class="spOptiBreak"> </span>Magazin für iPad und iPhone:</strong> <span>Entdecken Sie ein neues Lesevergnügen - jede Woche</span></a> 
</div> 
<div class="spInfoBox"> 
<h4>TOP</h4> 
<div class="spMPCBoxAutoHeight"> 
<div class="spMPCBox spMPCBoxHintergrund"> 
<ul class="spMPCTabs"> 
<li id="spMPCTab-40276-1" class="spMPCTab"><a href="javascript:void(0);" onclick="spToggleMPC('40276',1)" onfocus="blur()">Gelesen</a></li><li id="spMPCTab-40276-2" class="spMPCTab spMPCTabAktiv"><a href="javascript:void(0);" onclick="spToggleMPC('40276',2)" onfocus="blur()">Verschickt</a></li><li id="spMPCTab-40276-3" class="spMPCTab"><a href="javascript:void(0);" onclick="spToggleMPC('40276',3)" onfocus="blur()">Gesehen</a></li> 
</ul> 
<div class="spMPCContent"> 
<div id="spMPCContent-40276-1" style="display:none;"><ul class="spRankingList"> 
<li><div class="spItemNumber">1</div> 
<div class="spItemText">	<div class="spColumnBoxPic"> 
<a href="/panorama/0,1518,715769,00.html#ref=top" ><img src="/images/image-127607-hpcpleftcolumn-mmdw.jpg" width="120" border="0" alt="" hspace="0" align="right" /></a> 
<div style="width:122px;"> 
<div align="right" class="spCredit">DDP</div> 
</div> 
</div> 
<a href="/panorama/0,1518,715769,00.html#ref=top"><strong>"Schlag den Raab"-<span class="spOptiBreak"> </span>Sieger: </strong> <span>Von wegen Jetset-<span class="spOptiBreak"> </span>Leben</span></a></div> 
</li> 
<li><div class="spItemNumber">2</div> 
<div class="spItemText"><a href="/kultur/tv/0,1518,715749,00.html#ref=top"><strong>Neue RTL-<span class="spOptiBreak"> </span>Show: </strong> <span>Abwärts geht's immer</span></a></div> 
</li> 
<li><div class="spItemNumber">3</div> 
<div class="spItemText"><a href="/panorama/justiz/0,1518,715616,00.html#ref=top"><strong>Fall Kachelmann: </strong> <span>Ein Prozess, sieben Fragen</span></a></div> 
</li> 
<li><div class="spItemNumber">4</div> 
<div class="spItemText"><a href="/netzwelt/games/0,1518,714307,00.html#ref=top"><strong>"Half-<span class="spOptiBreak"> </span>Life": </strong> <span>Der Science-<span class="spOptiBreak"> </span>Fiction-<span class="spOptiBreak"> </span>Klassiker der Spielgeschichte</span></a></div> 
</li> 
<li><div class="spItemNumber">5</div> 
<div class="spItemText"><a href="/politik/deutschland/0,1518,715807,00.html#ref=top"><strong>Kritik an Bundespräsident: </strong> <span>Wulff gerät zwischen die Sarrazin-<span class="spOptiBreak"> </span>Fronten</span></a></div> 
</li> 
</ul><br clear="all" /> 
</div><div id="spMPCContent-40276-2" style="display:block;"><ul class="spRankingList"> 
<li><div class="spItemNumber">1</div> 
<div class="spItemText">	<div class="spColumnBoxPic"> 
<a href="/wirtschaft/service/0,1518,715756,00.html#ref=top" ><img src="/images/image-91710-hpcpleftcolumn-kimd.jpg" width="120" border="0" alt="" hspace="0" align="right" /></a> 
<div style="width:122px;"> 
<div align="right" class="spCredit">AP</div> 
</div> 
</div> 
<a href="/wirtschaft/service/0,1518,715756,00.html#ref=top"><strong>Immobilienmarkt: </strong> <span>Mieten in Deutschland steigen rapide</span></a></div> 
</li> 
<li><div class="spItemNumber">2</div> 
<div class="spItemText"><a href="/reise/fernweh/0,1518,709851,00.html#ref=top"><strong>Chili-<span class="spOptiBreak"> </span>Hochburg Diamante: </strong> <span>Viagra des armen Mannes</span></a></div> 
</li> 
<li><div class="spItemNumber">3</div> 
<div class="spItemText"><a href="/kultur/gesellschaft/0,1518,714005,00.html#ref=top"><strong>Stadtentwicklung: </strong> <span>Lob der Ramschmeile</span></a></div> 
</li> 
<li><div class="spItemNumber">4</div> 
<div class="spItemText"><a href="/spam/0,1518,715825,00.html#ref=top"><strong>SATIRE@SPIEGEL ONLINE:</strong> <span>ZDF plant Verfilmung</span></a></div> 
</li> 
<li><div class="spItemNumber">5</div> 
<div class="spItemText"><a href="/reise/aktuell/0,1518,713981,00.html#ref=top"><strong>Moskau-<span class="spOptiBreak"> </span>St.Petersburg: </strong> <span>Mit Tempo 250 durch Russlands Dörfer</span></a></div> 
</li> 
</ul><br clear="all" /> 
</div><div id="spMPCContent-40276-3" style="display:none;"><ul class="spRankingList"> 
<li><div class="spItemNumber">1</div> 
<div class="spItemText"><a onclick="return spShowVideo(this,'1081547','10')" href="/video/video-1081547.html#ref=top"><strong>Attentate in Russland: </strong> <span>Hass auf den deutschen Hightech-<span class="spOptiBreak"> </span>Zug</span></a></div> 
</li><li><div class="spItemNumber">2</div> 
<div class="spItemText"><a onclick="return spShowVideo(this,'1082111','10')" href="/video/video-1082111.html#ref=top"><strong>Eher Ghetto als Integration: </strong> <span>Duisburg Marxloh (1)</span></a></div> 
</li><li><div class="spItemNumber">3</div> 
<div class="spItemText"><a onclick="return spShowVideo(this,'1082294','1')" href="/video/video-1082294.html#ref=top"><strong>IFA 2010: </strong> <span>Das Galaxy Tablet von Samsung im Test</span></a></div> 
</li><li><div class="spItemNumber">4</div> 
<div class="spItemText"><a onclick="return spShowVideo(this,'1082086','1')" href="/video/video-1082086.html#ref=top"><strong>Neu im Kino: </strong> <span>Paradiesische Liebschaften und kämpfende Magier</span></a></div> 
</li><li><div class="spItemNumber">5</div> 
<div class="spItemText"><a onclick="return spShowVideo(this,'1082102','10')" href="/video/video-1082102.html#ref=top"><strong>Eher Ghetto als Integration: </strong> <span>Duisburg Marxloh (2)</span></a></div> 
</li> 
</ul><br clear="all" /></div> 
</div> 
</div> 
</div> 
</div> 
<div class="spInfoBox"> 
<h4><a href="http://www.spiegel.de/international/">ENGLISH SITE</a></h4> 
<ul class="spInfoBoxList"> 
<li>	<div class="spColumnBoxPic"> 
<a href="/international/zeitgeist/0,1518,715485,00.html" ><img src="/images/image-125613-hpcpleftcolumn-ubsf.jpg" width="120" border="0" alt="" hspace="0" align="right" /></a> 
<div style="width:122px;"> 
<div align="right" class="spCredit">Jesco Denzel</div> 
</div> 
</div> 
<a href="/international/zeitgeist/0,1518,715485,00.html"><strong>Roma Photo Exhibition: </strong> <span>Exploring the World of Gitans in France</span></a></li> 
<li><a href="/international/germany/0,1518,715167,00.html"><strong>Counterculture Vs. Capitalism: </strong> <span>Iconic Berlin Squat Fights Its Last Battle</span></a></li> 
<li><a href="/international/zeitgeist/0,1518,715278,00.html"><strong>Austrian Anti-<span class="spOptiBreak"> </span>Muslim Video Game: </strong> <span>'We'd Rather Have Sarrazin than a Muezzin'</span></a></li> 
</ul> 
</div> 
<script type="text/javascript"> 
<!--
OAS_RICH('x23');
// -->
</script><br class="spBreakNoHeight" clear="all" /><div class="spInfoBox"> 
<h4><a  target="_blank" href="http://boersen.manager-magazin.de/spo_mmo/">BÖRSE</a></h4> 
<div class="spBoerseBox"> 
<div class="spMPCBox spMPCBoxHintergrund"> 
<ul class="spMPCTabs"> 
<li id="spMPCTab-46875-1" class="spMPCTab spMPCTabAktiv"><a href="javascript:void(0);" onclick="spToggleMPC('46875',1)" onfocus="blur()">DAX</a></li><li id="spMPCTab-46875-2" class="spMPCTab"><a href="javascript:void(0);" onclick="spToggleMPC('46875',2)" onfocus="blur()">TECDAX</a></li><li id="spMPCTab-46875-3" class="spMPCTab"><a href="javascript:void(0);" onclick="spToggleMPC('46875',3)" onfocus="blur()">DOW</a></li> 
</ul> 
<div class="spMPCContent"> 
<div id="spMPCContent-46875-1" style="display: block;"> 
<div class="spBoersenChart"> 
<ul> 
<li><a target="Boerse" href="http://boersen.manager-magazin.de/spo_mmo/index.htm">Chart</a></li> 
<li><a target="Boerse" href="http://boersen.manager-magazin.de/spo_mmo/news.htm">News</a></li> 
<li><a target="Boerse" href="http://boersen.manager-magazin.de/spo_mmo/sms.htm">Kurse auf Handy</a></li> 
<li><a target="SPONflash19276" href="http://www.spiegel.de/flash/0,5532,19276,00.html" onclick="window.open('http://www.spiegel.de/flash/0,5532,19276,00.html','SPONflash19276','width=730,height=600'+SpOnENV_FlashPopupParams).focus(); return false;">Parkettkamera</a></li> 
</ul> 
<a href="http://boersen.manager-magazin.de/spo_mmo/kurse_listen.htm?sektion=dax"><img src="http://www.spiegel.de/staticgen/boerse/spv9dax161x96.gif" width="161" border="0" height="96"></a> 
</div> 
</div> 
<div id="spMPCContent-46875-2" style="display: none;"> 
<div class="spBoersenChart"> 
<ul> 
<li><a target="Boerse" href="http://boersen.manager-magazin.de/spo_mmo/index.htm">Chart</a></li> 
<li><a target="Boerse" href="http://boersen.manager-magazin.de/spo_mmo/news.htm">News</a></li> 
<li><a target="Boerse" href="http://boersen.manager-magazin.de/spo_mmo/sms.htm">Kurse auf Handy</a></li> 
<li><a target="SPONflash19276" href="http://www.spiegel.de/flash/0,5532,19276,00.html" onclick="window.open('http://www.spiegel.de/flash/0,5532,19276,00.html','SPONflash19276','width=730,height=600'+SpOnENV_FlashPopupParams).focus(); return false;">Parkettkamera</a></li> 
</ul> 
<a href="http://boersen.manager-magazin.de/spo_mmo/kurse_listen.htm?sektion=tecdax"><img src="http://www.spiegel.de/staticgen/boerse/spv9tecdax161x96.gif" width="161" border="0" height="96"></a></div> 
</div> 
<div id="spMPCContent-46875-3" style="display: none;"> 
<div class="spBoersenChart"> 
<ul> 
<li><a target="Boerse" href="http://boersen.manager-magazin.de/spo_mmo/index.htm">Chart</a></li> 
<li><a target="Boerse" href="http://boersen.manager-magazin.de/spo_mmo/news.htm">News</a></li> 
<li><a target="Boerse" href="http://boersen.manager-magazin.de/spo_mmo/sms.htm">Kurse auf Handy</a></li> 
<li><a target="SPONflash19276" href="http://www.spiegel.de/flash/0,5532,19276,00.html" onclick="window.open('http://www.spiegel.de/flash/0,5532,19276,00.html','SPONflash19276','width=730,height=600'+SpOnENV_FlashPopupParams).focus(); return false;">Parkettkamera</a></li> 
</ul> 
<a href="http://boersen.manager-magazin.de/spo_mmo/kurse_listen.htm?sektion=dj"><img src="http://www.spiegel.de/staticgen/boerse/spv9dow161x96.gif" width="161" border="0" height="96"></a></div> 
</div> 
</div> 
</div> 
 
<div class="spBoerseForm"> 
<form target="Boerse" action="http://boersen.manager-magazin.de/spo_mmo/kurse_einzelkurs_suche.htm" method="post"> 
<input type="hidden" value="suche" name="sektion"/> 
<input type="hidden" value="Alles" name="button"/> 
<input type="image" class="spBoerseSubmit" name="kursesuchen" height="17" border="0" width="17" src="/static/sys/v8/icons/ic_submit.gif" alt=">>" title="Kurssuche starten" /> 
<input type="text" class="spBoerseInput" name="suchbegriff" value="" /> 
<label for="suchbegriff">KURSE ABFRAGEN</label> 
</form> 
</div> 
<table class="spBoerseTable" cellpadding="0" cellspacing="0" border="0"> 
<tr class="spRow1"> 
<th class="spBoerseUhrzeit">10:40 Uhr</th> 
<th align="right"><b>Kurs</b></th> 
<th align="right"><b>absolut</b></th> 
<th align="right" width="40"><b>in %</b></th> 
</tr> 
<tr class="spRow1"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_listen.htm?sektion=dax" target="Boerse">DAX</a></td> 
<td align="right">6.146,24</td> 
<td align="right">+11,62</td> 
<td align="right" class="spNumberPositive">0,19</td> 
</tr> 
<tr class="spRow2"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_listen.htm?sektion=mdax" target="Boerse">MDax</a></td> 
<td align="right">8.495,04</td> 
<td align="right">-12,79</td> 
<td align="right" class="spNumberNegative">-0,15</td> 
</tr> 
<tr class="spRow1"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_listen.htm?sektion=tecdax" target="Boerse">TecDax</a></td> 
<td align="right">767,48</td> 
<td align="right">+1,31</td> 
<td align="right" class="spNumberPositive">0,17</td> 
</tr> 
<tr class="spRow2"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_listen.htm?sektion=estoxx" target="Boerse">E-Stoxx</a></td> 
<td align="right">2.750,69</td> 
<td align="right">+4,46</td> 
<td align="right" class="spNumberPositive">0,16</td> 
</tr> 
<tr class="spRow1"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_listen.htm?sektion=dj" target="Boerse">Dow</a></td> 
<td align="right">10.447,90</td> 
<td align="right">+127,83</td> 
<td align="right" class="spNumberPositive">1,24</td> 
</tr> 
<tr class="spRow2"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_listen.htm?sektion=nasdaq100" target="Boerse">Nasdaq 100</a></td> 
<td align="right">1.870,31</td> 
<td align="right">+29,73</td> 
<td align="right" class="spNumberPositive">1,62</td> 
</tr> 
<tr class="spRow1"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_listen.htm?sektion=nikkei" target="Boerse">Nikkei</a> <a href="http://www.spiegel.de/wirtschaft/0,1518,583400,00.html">(late)</a><a></a></td> 
<td align="right">9.301,32</td> 
<td align="right">+187,19</td> 
<td align="right" class="spNumberPositive">2,05</td> 
</tr> 
<tr class="spRow2"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_einzelkurs_uebersicht.htm?s=EUR&amp;b=691&amp;l=276" target="Boerse">&euro; in $</a></td> 
<td align="right">1,2910</td> 
<td align="right">+0,0027</td> 
<td align="right" class="spNumberPositive">0,21</td> 
</tr> 
<tr class="spRow1"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_einzelkurs_uebersicht.htm?s=EURGBP&amp;b=691&amp;l=276" target="Boerse">&euro; in £</a></td> 
<td align="right">0,8354</td> 
<td align="right">+0,0015</td> 
<td align="right" class="spNumberPositive">0,18</td> 
</tr> 
<tr class="spRow2"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_einzelkurs_uebersicht.htm?s=EURCHF&amp;b=691&amp;l=276" target="Boerse">&euro; in sfr</a></td> 
<td align="right">1,3108</td> 
<td align="right">+0,0004</td> 
<td align="right" class="spNumberPositive">0,03</td> 
</tr> 
<tr class="spRow1"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_einzelkurs_uebersicht.htm?s=BRENTDAT.RSM&amp;b=400&amp;l=276&amp;n=OIL%20in%20USD" target="Boerse">Öl ($)</a> <a href="http://www.spiegel.de/wirtschaft/0,1518,583400,00.html">(late)</a><a></a></td> 
<td align="right">75,35</td> 
<td align="right">-0,12</td> 
<td align="right" class="spNumberNegative">-0,16</td> 
</tr> 
<tr class="spRow2"> 
<td><a href="http://boersen.manager-magazin.de/spo_mmo/kurse_einzelkurs_uebersicht.htm?s=USDGOLPM.LFIX&amp;b=401&amp;l=826&amp;n=GOLD%20in%20USD" target="Boerse">Gold ($)</a> <a href="http://www.spiegel.de/wirtschaft/0,1518,583400,00.html">(late)</a><a></a></td> 
<td align="right">1.240,50</td> 
<td align="right">-8,00</td> 
<td align="right" class="spNumberNegative">-0,64</td> 
</tr> 
</table> 
 
</div> 
</div> 
<div class="spInfoBox"> 
<h4><a href="http://www.spiegel.de/spiegel/spiegelwissen/index-2010-3.html">SPIEGEL WISSEN</a></h4> 
<div class="spArticleImageBox spAssetAlignright" style="width: 122px"> 
<a href="http://www.spiegel.de/spiegel/spiegelwissen/index-2010-3.html" target="_top"><img src="/img/0,1020,3020329,00.jpg" width="120" height="158" border="0" alt="" align="right" hspace="0" /></a> 
</div> 
<p><b><a href="http://www.spiegel.de/spiegel/spiegelwissen/index-2010-3.html"><strong>Heft 3/2010:</strong> <span></span></a> 
<br/>Alles im Kopf</b><br /> 
Was wir heute alles wissen müssen
</p> 
<p><a href="http://www.spiegel.de/spiegel/spiegelwissen/index-2010-3.html">Inhaltsverzeichnis</a></p> 
<p><a  target="_blank" href="http://abo.spiegel.de/go/place!ABOSSPSC">Hier können Sie das Heft im Abo bestellen</a></p> 
<p><a  target="_blank" href="http://shop.spiegel.de/shop/action/productDetails/12546565/3_2010_alles_im_kopf.html?aUrl=90009999">Hier können Sie das Heft kaufen</a></p> 
<p><a  target="_blank" href="http://abo.spiegel.de/?et_cid=7&amp;et_lid=1946&amp;et_sub=heftkasten">Hier finden Sie weitere Abo-<span class="spOptiBreak"> </span>Angebote</a></p> 
</div> 
<div class="spInfoBox"> 
<h4><a href="http://wetter.spiegel.de/spiegel/">WETTER</a></h4> 
 
<table border="0" cellpadding="0" cellspacing="0" class="spWetterbox"> 
<tr class="spRow1"> 
<td><a href="http://wetter.spiegel.de/cgi-bin/wettersearch.cgi?pn=14193">Berlin</a></td> 
<td width="40">18°C</td> 
<td><img src="http://www.spiegel.de/static/wetter/heiter26x20.gif" alt="heiter" width="26" height="20" /></td> 
</tr> 
<tr class="spRow2"> 
<td><a href="http://wetter.spiegel.de/spiegel/europe/896.html">London</a></td> 
<td width="40">19°C</td> 
<td><img src="http://www.spiegel.de/static/wetter/regen26x20.gif" alt="regen" width="26" height="20" /></td> 
</tr> 
<tr class="spRow1"> 
<td><a href="http://wetter.spiegel.de/spiegel/world/1106.html">New York</a></td> 
<td width="40">24°C</td> 
<td><img src="http://www.spiegel.de/static/wetter/heiter26x20.gif" alt="heiter" width="26" height="20" /></td> 
</tr> 
<tr class="spRow2"> 
<td><a href="http://wetter.spiegel.de/spiegel/world/1333.html">Rio de Janeiro</a></td> 
<td width="40">22°C</td> 
<td><img src="http://www.spiegel.de/static/wetter/regenschauer26x20.gif" alt="regenschauer" width="26" height="20" /></td> 
</tr> 
<tr class="spRow1"> 
<td><a href="http://wetter.spiegel.de/spiegel/europe/1756.html">Rom</a></td> 
<td width="40">27°C</td> 
<td><img src="http://www.spiegel.de/static/wetter/heiter26x20.gif" alt="heiter" width="26" height="20" /></td> 
</tr> 
<tr class="spRow2"> 
<td><a href="http://wetter.spiegel.de/spiegel/world/1573.html">Tokio</a></td> 
<td width="40">30°C</td> 
<td><img src="http://www.spiegel.de/static/wetter/wolkig26x20.gif" alt="wolkig" width="26" height="20" /></td> 
</tr> 
</table> 
 
<ul class="spWetterList"> 
<li><a href="http://wetter.spiegel.de/spiegel/">Aktuelle Vorhersagen</a></li> 
<li><a href="http://wetter.spiegel.de/spiegel/html/deutschland_temp.html">Temperaturen in Deutschland</a></li> 
<li><a href="http://wetter.spiegel.de/spiegel/html/euro_temp.html">Temperaturen in Europa</a></li> 
<li><a href="http://wetter.spiegel.de/spiegel/satellite/sat_europa.html">Satellitenbilder</a></li> 
<li><a href="http://unwetterzentrale.spiegel.de/">Unwetterwarnungen</a></li> 
</ul> 
</div> 
<div class="spInfoBox"> 
<style type="text/css"> 
<!--
h4.spLogoHeadline img{
margin:0 0 -4px 2px;
}
.spInfoBox h4.spLogoHeadline a{
background: 0 none;
padding:0;
}
-->
</style> 
<h4 class="spLogoHeadline"> 
<a target="_blank" href="http://www.lto.de"><img src="/img/0,1020,2538158,00.jpg" alt="" hspace="0" border="0"/> 
</a> 
</h4> 
<b>Rechtliche Hintergründe - jeden Tag</b><br clear="all"/><br/>	<ul class="spInfoBoxList"> 
<li>	<div class="spColumnBoxPic"> 
<a href="http://www.lto.de/de/html/nachrichten/1365/staatsanwaelte-kachelmann-Kavallerie-der-Justiz-statt-objektivster-BehC3B6rde-der-Welt-/"  target="_blank"><img src="/images/image-116341-hpcpleftcolumn-danc.jpg" width="120" border="0" alt="" hspace="0" align="right" /></a> 
<div style="width:122px;"> 
<div align="right" class="spCredit">dpa</div> 
</div> 
</div> 
<a  target="_blank" href="http://www.lto.de/de/html/nachrichten/1365/staatsanwaelte-kachelmann-Kavallerie-der-Justiz-statt-objektivster-BehC3B6rde-der-Welt-/"><strong>Die Staatsanwälte und der Fall Kachelmann:</strong> <span>Die Kavallerie der Justiz </span></a></li> 
<li><a  target="_blank" href="http://www.lto.de/de/html/nachrichten/1360/Vorgaben-des-Bundesverfassungsgerichts-bleiben-gewahrt/ "><strong>Aussetzung der Wehrpflicht:</strong> <span>Vorgaben des Verfassungsgerichts bleiben gewahrt</span></a></li> 
<li><a  target="_blank" href="http://www.lto.de/de/html/nachrichten/1353/Thilo-Sarrazin-und-die-SPD-hohe-huerden-fuer-parteiausschluss/ "><strong>Thilo Sarrazin und die SPD:</strong> <span>Hohe Hürden für den Parteiausschluss</span></a></li> 
</ul> 
</div> 
<div class="spInfoBox"> 
<h4><a href="http://www.spiegel.de/panorama/0,1518,k-1447,00.html">AUGENBLICK</a></h4> 
<ul class="spInfoBoxList"> 
<li>	<div class="spColumnBoxPic"> 
<a href="/reise/aktuell/0,1518,715796,00.html" ><img src="/images/image-127673-hpcpleftcolumn-poga.jpg" width="120" border="0" alt="" hspace="0" align="right" /></a> 
<div style="width:122px;"> 
<div align="right" class="spCredit">AP</div> 
</div> 
</div> 
<a href="/reise/aktuell/0,1518,715796,00.html">Hoch das Bein</a></li> 
</ul> 
</div> 
<div class="spInfoBox"> 
<h4><a  target="_blank" href="http://www.manager-magazin.de/">MANAGER MAGAZIN</a></h4> 
<ul class="spInfoBoxList"> 
<li><a  target="_blank" href="http://www.manager-magazin.de/unternehmen/artikel/0,2828,715819,00.html"><strong>GEZ-<span class="spOptiBreak"> </span>Gebühr:</strong> <span>Wie sich die Lasten für Zahler verschieben</span></a></li> 
<li><a  target="_blank" href="http://www.manager-magazin.de/magazin/artikel/0,2828,708035,00.html"><strong>Bankenrettung:</strong> <span>Hilfe kann auch lukrativ sein</span></a></li> 
<li><a  target="_blank" href="http://www.manager-magazin.de/unternehmen/artikel/0,2828,715324,00.html"><strong>Wohlstandskrankheiten:</strong> <span>Deutsche Firmen dick im Geschäft</span></a></li> 
</ul> 
</div> 
<div class="spInfoBox"> 
<h4><a  target="_blank" href="http://www.dctp.tv">DAS DCTP  WEB TV</a></h4> 
<ul class="spInfoBoxList"> 
<li>	<div class="spColumnBoxPic"> 
<a href="http://www.dctp.tv/bildung/helge-schneider-beinahe-waeren-wir-roemer-geworden/?ref=spiegel"  target="_blank"><img src="/img/0,1020,2440010,00.jpg" width="120" border="0" alt="" hspace="0" align="right" /></a> 
</div> 
<a  target="_blank" href="http://www.dctp.tv/bildung/helge-schneider-beinahe-waeren-wir-roemer-geworden/?ref=spiegel"><strong>Beinahe wären wir Römer geworden: </strong> <span>Was wäre gewesen, wenn der römische Feldherr Varus gesiegt hätte? Mit Helge Schneider</span></a></li> 
</ul> 
</div> 
<div class="spInfoBox"> 
<h4><a href="http://seen.by.spiegel.de/">SEEN.BY</a></h4> 
<ul class="spInfoBoxList"> 
<li>	<div class="spColumnBoxPic"> 
<a href="http://seen.by.spiegel.de/" ><img src="/img/0,1020,2874604,00.jpg" width="120" border="0" alt="" hspace="0" align="right" /></a> 
</div> 
<a href="http://seen.by.spiegel.de/"><strong>seen.by Fotokunst & Fotolabor:</strong> <span>Finden Sie Ihre Lieblingsmotive oder drucken und präsentieren Sie Ihre Bilder wie die Profis. Das alles zu unschlagbar günstigen Preisen</span></a></li> 
</ul> 
</div> 
<div class="spInfoBox"> 
<h4><a href="/kultur/charts/0,1518,458623,00.html">SPIEGEL-<span class="spOptiBreak"> </span>BESTSELLER</a></h4> 
<style type="text/css"> 
<!--
.spBestsellerLists .spArticleImageBox.spAssetAligncenter{ margin-bottom:1px; }
.spBestsellerLists ul,
.spBestsellerLists h4,
.spBestsellerLists p.spLinkAlle{ float:left; clear:both; width:100%; }
.spBestsellerLists ul{ margin-bottom:7px; padding:1px 0; border-bottom:1px solid #CCC; }
.spBestsellerLists h4{ margin:0; padding:7px 0 4px 0; font-size:1em; text-transform:none; }
.spBestsellerLists h4:first-child{ padding-top:1px; }
.spBestsellerLists h4 a{ color:#900; }
.spBestsellerLists li{ float:left; width:100%; margin:0; padding:0.5em 0; border-top:1px solid #CCC; }
.spBestsellerLists li span{ float:left; width:130px; padding:0 0 0 10px; }
.spBestsellerLists li span.no{ width:20px; padding:0; font-weight:bold; }
.spBestsellerLists li span.auth{ width:130px; padding:0; }
.spBestsellerLists li span.dvd{ width:260px; padding:0; }
.spBestsellerLists p.spLinkAlle { float:left; width: 100%; margin:0; padding:0; }
.spBestsellerLists p.spLinkAlle a {
float:right; clear:both; display:inline-block; width:auto;
margin:1px; padding:1px 16px 2px 0;
background:transparent url("http://www.spiegel.de/static/sys/v9/icons/ic_forward.gif") no-repeat scroll right center;
text-align:right;
font-weight:bold;
}
-->
</style> 
<div class="spBestsellerLists"> 
<div class="spArticleImageBox spAssetAligncenter" style="width: 293px"> 
<a href="/kultur/charts/0,1518,458623,00.html" target="_top"><img src="/img/0,1020,2221510,00.jpg" width="291" height="106" border="0" alt="" align="middle" hspace="0" /></a> 
<div style="width:293px;"> 
<div align="right" class="spCredit">AP</div> 
</div> 
</div> 
<!-- Taschenbücher Belletristik 48905 --> 
<h4><a href="/kultur/charts/0,1518,458992,00.html">Taschenbücher Belletristik 36/2010</a></h4> 
<ul> 
<li><span class="no">1.</span> 
<span class="auth"> 
Beckett, Simon
</span><span> 
Leichenblässe
</span></li> 
<li><span class="no">2.</span> 
<span class="auth"> 
Mankell, Henning
</span><span> 
Der Chinese
</span></li> 
<li><span class="no">3.</span> 
<span class="auth"> 
Morton, Kate
</span><span> 
Der verborgene Garten
</span></li> 
</ul> 
 
<!-- Hardcover Belletristik 48904 --> 
<!-- DVD Spielfilme 48908 --> 
<h4><a href="/kultur/charts/0,1518,680126,00.html">DVDs Spielfilme September 2010</a></h4> 
<ul> 
<li><span class="no">1.</span> 
<span class="dvd"> 
Twilight - New Moon - Bis(s) zur Mittagsstunde
</span></li> 
<li><span class="no">2.</span> 
<span class="dvd"> 
Alice im Wunderland
</span></li> 
<li><span class="no">3.</span> 
<span class="dvd"> 
Verblendung
</span></li> 
</ul> 
<p class="spLinkAlle"><a href="/kultur/charts/0,1518,458623,00.html">Alle Bestseller</a></p> 
</div> 
</div> 
<script type="text/javascript"> 
<!--
OAS_RICH('Position1');
// -->
</script><br class="spBreakNoHeight" clear="all" /><div class="spInfoBox"> 
<h4>SPIEGEL ONLINE BESSER NUTZEN</h4> 
<p>Vernetzen Sie sich mit uns:</p> 
<ul class="spHelpBoxList2"> 
<li> 
<a href="/dienste/0,1518,634608,00.html" target="_top"><img src="/img/0,1020,1600747,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634608,00.html">Facebook</a></span> 
</li> 
<li> 
<a href="/dienste/0,1518,634522,00.html" target="_top"><img src="/img/0,1020,1600749,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634522,00.html">Twitter</a></span> 
</li> 
<li class="spHelpBoxListClear"> 
<a href="/dienste/0,1518,634703,00.html" target="_top"><img src="/img/0,1020,1600750,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634703,00.html">StudiVZ &amp; Co</a></span> 
</li> 
<li> 
<a href="/dienste/0,1518,634708,00.html" target="_top"><img src="/img/0,1020,1600757,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634708,00.html">MySpace</a></span> 
</li> 
<!--
<li class="spHelpBoxListClear">
<a href="/dienste/0,1518,689058,00.html" target="_top"><img src="/img/0,1020,2493670,00.jpg" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a>
<span class="spHelpLLTxt"><a href="/dienste/0,1518,689058,00.html">Google Buzz</a></span>
</li>
<li>
</li>
--> 
</ul> 
<h5 class="spHelpBox"><br />SERVICES</h5> 
<div style="margin-bottom:10px;">Nutzen Sie unsere praktischen Angebote:</div> 
<ul class="spHelpBoxList2"> 
<li> 
<a href="/dienste/0,1518,675525,00.html" target="_top"><img src="/img/0,1020,1600751,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,675525,00.html">MOBIL</a></span> 
</li> 
<li> 
<a href="/dienste/0,1518,634562,00.html" target="_top"><img src="/img/0,1020,1600752,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634562,00.html">Eilmeldungen</a></span> 
</li> 
<li class="spHelpBoxListClear"> 
<a href="/dienste/0,1518,634260,00.html" target="_top"><img src="/img/0,1020,1600754,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634260,00.html">RSS</a></span> 
</li> 
<li> 
<a href="/dienste/0,1518,634551,00.html" target="_top"><img src="/img/0,1020,1600753,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634551,00.html">Newsletter</a></span> 
</li> 
</ul> 
<h5 class="spHelpBox"><br />WIDGETS</h5> 
<div style="margin-bottom:10px;">Lassen Sie sich auf dem Laufenden halten:</div> 
<ul class="spHelpBoxList2"> 
<li> 
<a href="/dienste/0,1518,634571,00.html" target="_top"><img src="/img/0,1020,1600740,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634571,00.html">Windows</a></span> 
</li> 
<li> 
<a href="/dienste/0,1518,634582,00.html" target="_top"><img src="/img/0,1020,1600741,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634582,00.html">Mac</a></span> 
</li> 
<li class="spHelpBoxListClear"> 
<a href="/dienste/0,1518,638859,00.html" target="_top"><img src="/img/0,1020,1600744,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,638859,00.html">iGoogle</a></span> 
</li> 
<li> 
<a href="/dienste/0,1518,638859,00.html" target="_top"><img src="/img/0,1020,2345629,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,681110,00.html">Google Chrome</a></span> 
</li> 
<li class="spHelpBoxListClear"> 
<a href="/dienste/0,1518,634585,00.html" target="_top"><img src="/img/0,1020,1600748,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634585,00.html">Netvibes</a></span> 
</li> 
<li> 
<a href="/dienste/0,1518,634594,00.html" target="_top"><img src="/img/0,1020,1600742,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634594,00.html">Opera</a></span> 
</li> 
<li class="spHelpBoxListClear"> 
<a href="/dienste/0,1518,634595,00.html" target="_top"><img src="/img/0,1020,1600743,00.gif" width="20" height="20" border="0" alt="" align="left" hspace="0" class=" spImgNoBorder" /></a> 
<span class="spHelpLLTxt"><a href="/dienste/0,1518,634595,00.html">Embedding</a></span> 
</li> 
<li> 
</li> 
</ul> 
</div> 
<div class="spInfoBox"> 
<h4><a  target="_blank" href="http://www.harvardbusinessmanager.de/extra/artikel/a-596491.html">HARVARD BUSINESS MANAGER</a></h4> 
<ul class="spInfoBoxList"> 
<li><a  target="_blank" href="http://www.harvardbusinessmanager.de/heft/artikel/a-697655.html"><strong>Personalgespräche: </strong> <span>Führen wie Gott in Frankreich</span></a></li> 
<li><a  target="_blank" href="http://www.harvardbusinessmanager.de/heft/artikel/a-604002.html"><strong>Organisation: </strong> <span>Wann sich Innovationen lohnen</span></a></li> 
<li><a  target="_blank" href="http://www.harvardbusinessmanager.de/heft/artikel/a-697649.html"><strong>Forschung: </strong> <span>"Wir können die Macht von Charisma messen"</span></a></li> 
</ul> 
</div> 
</div> 
			<div class="spTop"> 
				<a href="#"><span>TOP</span></a> 
			</div> 
		</div> 
	</div> 
	<div id="spColumnAd"> 
	<script type="text/javascript"> 
	<!--
		OAS_RICH('Right'); 
	// -->
	</script><br class="spBreakNoHeight" clear="all" /> 
	<script type="text/javascript"> 
	<!--
		OAS_RICH('Bottom1'); 
	// -->
	</script><br class="spBreakNoHeight" clear="all" /> 
	<script type="text/javascript"> 
	<!--
		OAS_RICH('TopRight'); 
	// -->
	</script><br class="spBreakNoHeight" clear="all" /> 
</div></div> 
<script type="text/javascript"> 
    <!--
		if (typeof(qLoaded) == 'undefined') document.write('<img src="http://count.spiegel.de/nm_trck.gif?sp.site=9997&ua='+escape(navigator.userAgent)+'" width="1" height="1" border="0" align="right" alt="" />');
	//-->
</script> 
<div id="spPageFooter"> 
 
	<script type="text/javascript"> 
				<!--
					OAS_RICH('Bottom'); 
					// -->
				</script> 
			<div id="spPartnerBar"> 
<h4>Service von SPIEGEL-ONLINE-Partnern</h4> 
<!-- AUTO UND FREIZEIT 1 --> 
<ul class="spFirst"> 
<li><h5>AUTO UND FREIZEIT</h5></li> 
<li><span class="spPartnerBar1Zeile"> 
<a href="http://map24.spiegel.de/" style="background-position:0 -1px;"> 
Routenplaner
</a></span></li> 
<li><span> 
<a href="/auto/aktuell/0,1518,185826,00.html" style="background-position:1px -31px;"> 
Benzinpreis-<br>vergleich
</a></span></li> 
<li><span> 
<a title="Anzeige" href="http://einsurance.spiegel.de/versicherungsvergleich/versicherung/kfzversicherung/spiegelonline/SpiegelLayout.htm" style="background-position:0 -60px;"> 
Kfz-<br/>Versicherung
</a></span></li> 
<li><span> 
<a href="/auto/aktuell/0,1518,244772,00.html" style="background-position:0 -90px;"> 
Bußgeld-<br>rechner
</a></span></li> 
<li><span class="spPartnerBar1Zeile"> 
<a href="http://kliniksuche.spiegel.de/" style="background-position:0 -810px;"> 
Kliniksuche
</a></span></li> 
<li><span> 
<a href="http://www.spiegel.de/shop" style="background-position:0 -180px;"> 
Bücher<br>bestellen
</a></span></li> 
</ul> 
<!-- AUTO UND FREIZEIT 2 --> 
<ul> 
<li><h5 style="color: #f6f6f6;">AUTO UND FREIZEIT</h5></li> 
<li><span> 
<a title="Anzeige" href="http://www.libri.de/shop/action/magazine/4277/hoerbuch_download.html" style="background-position:0 -210px;"> 
Hörbuch-<br/>Downloads
</a></span></li> 
<li><span class="spPartnerBar1Zeile"> 
<a title="Anzeige" href="http://arztsuche.spiegel.de/" style="background-position:0 -240px;"> 
Arztsuche
</a></span></li> 
<li><span class="spPartnerBar1Zeile"> 
<a title="Anzeige" href="http://www.buchaktuell.de/" style="background-position:0 -270px;"> 
buch aktuell
</a></span></li> 
<li><span class="spPartnerBar1Zeile"> 
<a title="Anzeige" href="http://parship.spiegel.de/?source=nav" style="background-position:-2px -301px;"> 
Partnersuche
</a></span></li> 
<li><span> 
<a title="Anzeige" href="http://www.dastelefonbuch.de" target="_blank" style="background-position:0 -330px;"> 
Das<br>Telefonbuch
</a></span></li> 
<li><span class="spPartnerBar1Zeile"> 
<a title="Anzeige" href="http://hotels.spiegel.de" style="background-position:0 -360px;"> 
Hotels
</a></span></li> 
</ul> 
<!-- ENERGIE --> 
<ul> 
<li><h5>ENERGIE</h5></li> 
<li><span> 
<a title="Anzeige" href="http://check24.spiegel.de/energie/gas/index.html" style="background-position:0 -390px;"> 
Gasanbieter-<br>vergleich
</a></span></li> 
<li><span> 
<a title="Anzeige" href="http://check24.spiegel.de/energie/strom/index.html" style="background-position:0 -419px;"> 
Stromanbieter-<br>vergleich
</a></span></li> 
<li><span> 
<a title="Anzeige" href="https://ratgeber.co2online.de/index.php?berater=ratgeberauswahl&amp;portal_id=spiegel_online" style="background-position:-1px -450px;"> 
Energiespar-<br>ratgeber
</a></span></li> 
<li><span> 
<a title="Anzeige" href="http://check24.spiegel.de/energie/" style="background-position:-1px -479px;"> 
Energie-<br>vergleiche
</a></span></li> 
</ul> 
<!-- JOB --> 
<ul> 
<li><h5>JOB</h5></li> 
<li><span class="spPartnerBar1Zeile"> 
<a title="Anzeige" href="http://www.personalmarkt.de/source-links/spiegel/serviceangebotebox.html" style="background-position:0 -510px;"> 
Gehaltscheck
</a></span></li> 
<li><span> 
<a href="/wirtschaft/0,1518,223811,00.html" style="background-position:0 -90px;"> 
Brutto-Netto-<br/>Rechner
</a></span></li> 
<li><span class="spPartnerBar1Zeile"> 
<a href="/unispiegel/studium/0,1518,640620,00.html" style="background-position:0 -540px;"> 
Uni-Tools
</a></span></li> 
<li><span class="spPartnerBar1Zeile"> 
<a href="/schulspiegel/0,1518,193925,00.html" style="background-position:0 -150px;"> 
Ferientermine
</a></span></li> 
<li><span> 
<a title="Anzeige" href="http://placement24.spiegel.de/" style="background-position:0 -569px;"> 
2650<br>Headhunter
</a></span></li> 
</ul> 
<!-- FINANZEN 1 --> 
<ul> 
<li><h5>FINANZEN UND RECHT</h5></li> 
<li><span> 
<a title="Anzeige" href="http://einsurance.spiegel.de/versicherungsvergleich/versicherung/banken/spiegelonline/SpiegelLayout.htm" style="background-position:0 -601px;"> 
Banken-<br>vergleiche
</a></span></li> 
<li><span> 
<a title="Anzeige" href="http://einsurance.spiegel.de/versicherungsvergleich/versicherung/ratenkredit/spiegelonline/SpiegelLayout.htm" style="background-position:0 -629px;"> 
Kredite<br>vergleichen
</a></span></li> 
<li><span> 
<a title="Anzeige" href="http://einsurance.spiegel.de/versicherungsvergleich/versicherung/privatekranken/spiegelonline/SpiegelLayout.htm" style="background-position:-1px -659px;"> 
Kranken-<br>versicherung
</a></span></li> 
<li><span> 
<a href="http://boersen.manager-magazin.de/spo_mmo/kurse_crossrates.htm?p=0" style="background-position:0 -90px;"> 
Währungs-<br>rechner
</a></span></li> 
<li><span> 
<a title="Anzeige" href="http://einsurance.spiegel.de/versicherungsvergleich/versicherung/versicherungen/spiegelonline/SpiegelLayout.htm" style="background-position:0 -689px;"> 
Versicherungs-<br>vergleiche
</a></span></li> 
</ul> 
<!-- FINANZEN 2 --> 
<ul> 
<li><h5 style="color: #f6f6f6;">FINANZEN UND RECHT</h5></li> 
<!-- /static/sys/v9/icons/icon_gehaltscheck.png --> 
<li><span> 
<a title="Anzeige" href="http://einsurance.spiegel.de/telko/dsl" style="background-position:0 -510px;"> 
DSL-Anbieter-<br/>Vergleich
</a></span></li> 
<li><span class="spPartnerBar1Zeile"> 
<a title="Anzeige" href="http://telfish.spiegel.de/" style="background-position:0 -720px;"> 
Handytarife
</a></span></li> 
<li><span> 
<a title="Anzeige" href="http://immowelt.spiegel.de/" style="background-position:0 -749px;"> 
Immobilien-<br/>Börse
</a></span></li> 
<li><span> 
<a href="/wirtschaft/0,1518,237919,00.html" style="background-position:0 -779px;"> 
Prozesskosten-<br/>Rechner
</a></span></li> 
<li><span> 
<a title="Anzeige" href="http://arenonet.spiegel.de/" style="background-position:0 -120px;"> 
Rechts-<br>beratung
</a></span></li> 
</ul> 
</div><div id="spPageFooterMainNav"> 
		<ul> 
			<li><a href="/">Home</a></li> 
			<li><a href="/politik/">Politik</a></li> 
			<li><a href="/wirtschaft/">Wirtschaft</a></li> 
			<li><a href="/panorama/">Panorama</a></li> 
			<li><a href="/sport/">Sport</a></li> 
			<li><a href="/kultur/">Kultur</a></li> 
			<li><a href="/netzwelt/">Netzwelt</a></li> 
			<li><a href="/wissenschaft/">Wissenschaft</a></li> 
			<li><a href="/unispiegel/">UniSPIEGEL</a></li> 
			<li><a href="/schulspiegel/">SchulSPIEGEL</a></li> 
			<li><a href="/reise/">Reise</a></li> 
			<li><a href="/auto/">Auto</a></li> 
			<li><a href="http://wetter.spiegel.de/spiegel/">Wetter</a></li> 
		</ul> 
	</div> 
	<div id="spPageFooterSubNav"> 
		<ul class="spFirst">				  
			<li><strong>DIENSTE</strong></li> 
			<li><a href="/schlagzeilen/">Schlagzeilen</a></li> 
			<li><a href="/dienste/0,1518,271804,00.html">RSS</a></li> 
			<li><a href="/dienste/0,1518,352020,00.html">Newsletter</a></li> 
			<li><a href="/dienste/0,1518,419947,00.html">Mobil</a></li> 
		</ul> 
		
		<ul> 
			<li><strong>VIDEO</strong></li> 
			<li><a href="/video/">Nachrichten Videos</a></li> 
			<li><a href="/sptv/magazin/">SPIEGEL TV Magazin</a></li> 
			<li><a href="/sptv/programm/">SPIEGEL TV Programm</a></li> 
		</ul> 
		
		<ul> 
			<li><strong>MEDIA</strong></li> 
			<li><a target="_blank" href="http://www.spiegel-qc.de/">SPIEGEL QC</a></li> 
			<li><a target="_blank" href="/mediadaten">Mediadaten</a></li> 
			<li><a target="_blank" href="http://www.spiegel-qc.de/selbstbuchungstool">Selbstbuchungstool</a></li> 
			<li><a target="_blank" href="http://www.buchreport.de/">buchreport</a></li> 
			<li><a target="_blank" href="http://www.quality-abo.de/">weitere Zeitschriften</a></li> 
		</ul> 
		
		
		<ul>				  
			<li><strong>MAGAZINE</strong> 
			<li><a href="/spiegel/">DER SPIEGEL</a></li> 
			<li><a href="/deinspiegel/">Dein SPIEGEL</a></li> 
			<li><a href="/spiegelgeschichte/">SPIEGEL GESCHICHTE</a></li> 
			<li><a href="/spiegelwissen/">SPIEGEL WISSEN</a></li> 
			<li><a href="/kulturspiegel/">KulturSPIEGEL</a></li> 
			<li><a href="/spiegel/unispiegel/">UniSPIEGEL</a></li> 
		</ul> 
		<ul>  
			<li><strong>SPIEGEL GRUPPE</strong></li> 
			 <li><a href="http://abo.spiegel.de/?et_cid=7&amp;et_lid=1946&amp;et_sub=footer">Abo</a></li> 
			 <li class="first"><a href="http://shop.spiegel.de/" target="_blank">Shop</a></li> 
			 <li><a href="/sptv/">SPIEGEL&nbsp;TV</a></li> 
			 <li><a target="_blank" href="http://www.manager-magazin.de/">manager&nbsp;magazin</a></li> 
			<li><a target="_blank" href="http://www.harvardbusinessmanager.de/">Harvard Business Man.</a></li> 
			<li class="first"><a href="http://www.spiegelgruppe.de/" target="_blank">SPIEGEL-Gruppe</a></li> 
			
		</ul> 
		<ul>  
			<li><strong>WEITERE</strong> 
			<li><a href="/hilfe">Hilfe</a></li> 
			<li><a href="/kontakt">Kontakt</a></li> 
			<li><a target="_blank" href="http://www.spiegelgruppe-nachdrucke.de">Nachdrucke</a></li> 
			<li><a href="/dienste/0,1518,639126,00.html">Datenschutz</a></li> 
			<li><a href="/impressum">Impressum</a></li> 
		</ul>		
	</div>	
	<div class="spTop"> 
		<a href="#"><span>TOP</span></a> 
	</div> 
 
	<script type="text/javascript"> 
<!-- 
	spFramebuster();
// -->
</script><script type="text/javascript"> 
	<!--
		OAS_RICH('x70');
		// -->
	</script> 
</div> 

	<% 
		final long endTime = System.currentTimeMillis();
		final String runtime = String.valueOf(endTime - startTime);
	%>

	<div>Waiting time planned: <span id="waitingTimePlanned"><%=waitingTime%></span> ms</div>
	<div>Waiting time actual: <span id="waitingTimeActual"><%=runtime%></span> ms</div>
	<div>Runtime: <span id="runtime"><%=runtime%></span> ms</div>


</body>
</html>
