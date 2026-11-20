/*
 * Copyright (c) 2005-2026 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * XLT Modern Load Report — Events Explorer Renderer & Interactive Controls (`events.html`)
 *
 * @fileoverview
 * Renders non-fatal programmatic events logged via XLT's Event API (`window.XLT_EVENTS`):
 *   1. Summary KPI strip (`#ev-kpi-grid`) — logged count, event categories, primary share, dropped messages.
 *   2. Classification Summary table (`#events-summary-table`) — aggregated by event name.
 *   3. Event category filter chips (`#events-chips`) + multi-token search (`#events-filter`).
 *   4. Per-scenario/per-event detail cards (`#event-details-list`) using `hidden="until-found"`
 *      and `beforematch` so browser Ctrl+F expands collapsed message tables automatically.
 *
 * Visual Semantics:
 * Events represent warning/informational test signals (never fatal errors), so badges and non-zero
 * count cells consistently use orange warning styling (`warn` / `.cell-warn`), never red error colors.
 *
 * Consumes:
 * - `window.XLT_EVENTS` (`Array<{ name, testCase, totalCount, droppedCount, messages }>`)
 * - `window.XLT_META` (`chartsGenerated`)
 * - `window.XLT_UI` (`esc`, `fmtInt`, `fmtDec`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var fmtDec = UI.fmtDec;

  /**
   * HTML-escapes plain event info strings BEFORE wrapping `http://` / `https://` URLs
   * in clickable external `<a>` tags to prevent HTML injection.
   *
   * @param {string} text Raw event message info string.
   * @returns {string} Escaped HTML string with clickable external links.
   */
  function linkifyUrls(text) {
    var escaped = esc(text);
    return escaped.replace(/(https?:\/\/[^\s<")]+)/g, '<a href="$1" target="_blank" rel="noopener">$1 ↗</a>');
  }

  document.addEventListener("DOMContentLoaded", function () {
    var raw = window.XLT_EVENTS || [];
    var meta = window.XLT_META || {};
    var eventsData = Array.isArray(raw) ? raw : (raw.items || []);

    var kpiGrid = document.getElementById("ev-kpi-grid");
    var summarySec = document.getElementById("ev-summary-section");
    var chartWrap = document.getElementById("ev-chart-wrap");
    var summaryTbody = document.querySelector("#events-summary-table tbody");
    var summaryTfoot = document.querySelector("#events-summary-table tfoot");
    var filterBar = document.getElementById("ev-filter-bar");
    var container = document.getElementById("event-details-list");
    var filterInput = document.getElementById("events-filter");
    var chipWrap = document.getElementById("events-chips");
    var countBadge = document.getElementById("events-visible-count");

    var totalEvents = 0;
    var totalDropped = 0;
    var scenariosMap = {};
    var byNameMap = {};

    // Aggregate per-scenario event records by event category name
    eventsData.forEach(function (ev) {
      var cnt = Number(ev.totalCount) || 0;
      var drp = Number(ev.droppedCount) || 0;
      totalEvents += cnt;
      totalDropped += drp;
      if (ev.testCase) scenariosMap[ev.testCase] = true;
      var nm = ev.name || "Event";
      if (!byNameMap[nm]) {
        byNameMap[nm] = { name: nm, totalCount: 0, droppedCount: 0, groupCount: 0, scenarios: {} };
      }
      byNameMap[nm].totalCount += cnt;
      byNameMap[nm].droppedCount += drp;
      byNameMap[nm].groupCount += 1;
      if (ev.testCase) byNameMap[nm].scenarios[ev.testCase] = true;
    });

    var byNameList = Object.keys(byNameMap).map(function (k) {
      var g = byNameMap[k];
      g.scenarioCount = Object.keys(g.scenarios).length;
      return g;
    }).sort(function (a, b) {
      return b.totalCount - a.totalCount;
    });

    var uniqueScenarioCount = Object.keys(scenariosMap).length;
    var topClass = byNameList[0] || null;
    var secondClass = byNameList[1] || null;

    // 1. KPI Strip
    if (kpiGrid) {
      kpiGrid.innerHTML =
        '<article class="kpi-card">' +
          '<span class="kpi-label">Logged Test Events</span>' +
          '<div class="kpi-val' + (totalEvents > 0 ? ' cell-warn' : '') + '">' + fmtInt(totalEvents) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">events</span></div>' +
          '<div class="kpi-sub">Across <strong>' + fmtInt(uniqueScenarioCount) + " scenario(s)</strong> • <strong>" + fmtInt(eventsData.length) + " group(s)</strong></div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Distinct Event Types</span>' +
          '<div class="kpi-val">' + fmtInt(byNameList.length) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">categories</span></div>' +
          '<div class="kpi-sub">' + (topClass ? "Top: <strong>" + esc(topClass.name) + "</strong> (" + fmtInt(topClass.totalCount) + ")" : "Zero events recorded") + "</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Primary Event Share</span>' +
          '<div class="kpi-val" style="color:' + (topClass ? "var(--status-warn-text)" : "var(--text-muted)") + ';">' +
            (topClass && totalEvents > 0 ? fmtDec((topClass.totalCount * 100) / totalEvents, 1) + "%" : "0.0%") +
          "</div>" +
          '<div class="kpi-sub">' + (secondClass ? "2nd: " + esc(secondClass.name) + " (" + fmtInt(secondClass.totalCount) + ")" : "Single or zero event types") + "</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Dropped Messages</span>' +
          '<div class="kpi-val' + (totalDropped > 0 ? ' cell-warn' : '') + '">' + fmtInt(totalDropped) + "</div>" +
          '<div class="kpi-sub">' + (totalDropped > 0 ? "Rate-limited detail strings beyond buffer cap" : "Zero event detail messages dropped") + "</div>" +
        "</article>";
    }

    if (chartWrap && meta.chartsGenerated === false) {
      chartWrap.hidden = true;
    }

    // Empty State when 0 events
    if (!eventsData.length) {
      if (summarySec) summarySec.hidden = true;
      if (filterBar) filterBar.hidden = true;
      if (container) {
        container.innerHTML =
          '<section class="card"><div class="card-body" style="text-align:center;padding:40px 16px;color:var(--text-muted);">' +
            "<strong>No custom events were logged during this test run.</strong>" +
          "</div></section>";
      }
      return;
    }

    // 2. Summary Classification Table (orange warning theme across all event cells)
    if (summaryTbody) {
      summaryTbody.innerHTML = byNameList.map(function (c, idx) {
        var pct = totalEvents > 0 ? (c.totalCount * 100) / totalEvents : 0;
        var pillCls = "warn";
        return (
          "<tr>" +
            '<td class="col-idx">' + (idx + 1) + "</td>" +
            '<td class="col-text"><span class="status-pill ' + pillCls + '">' + esc(c.name) + "</span></td>" +
            '<td class="col-num' + (c.totalCount > 0 ? ' cell-warn' : '') + '" data-val="' + c.totalCount + '">' + fmtInt(c.totalCount) + "</td>" +
            '<td class="col-num" data-val="' + pct.toFixed(4) + '">' + fmtDec(pct, 2) + "%</td>" +
            '<td class="col-num" data-val="' + c.scenarioCount + '">' + fmtInt(c.scenarioCount) + "</td>" +
            '<td class="col-num' + (c.droppedCount > 0 ? ' cell-warn' : '') + '" data-val="' + c.droppedCount + '">' + fmtInt(c.droppedCount) + "</td>" +
          "</tr>"
        );
      }).join("");
      if (summaryTfoot) {
        summaryTfoot.innerHTML =
          '<tr class="table-totals">' +
            '<td class="col-idx">Σ</td>' +
            '<td class="col-text">Total Logged Events (' + fmtInt(byNameList.length) + " types)</td>" +
            '<td class="col-num' + (totalEvents > 0 ? ' cell-warn' : '') + '">' + fmtInt(totalEvents) + "</td>" +
            '<td class="col-num">100.00%</td>' +
            '<td class="col-num">' + fmtInt(uniqueScenarioCount) + "</td>" +
            '<td class="col-num' + (totalDropped > 0 ? ' cell-warn' : '') + '">' + fmtInt(totalDropped) + "</td>" +
          "</tr>";
      }
    }

    // 3. Dynamic Event Chips
    if (chipWrap) {
      var chipsHtml = ['<button type="button" class="btn-chip active" data-ev-chip="all">All Events (' + fmtInt(eventsData.length) + ")</button>"];
      byNameList.forEach(function (c) {
        chipsHtml.push('<button type="button" class="btn-chip" data-ev-chip="' + esc(c.name) + '">' + esc(c.name) + " (" + fmtInt(c.groupCount) + ")</button>");
      });
      chipWrap.innerHTML = chipsHtml.join("");
    }

    // 4. Event Detail Cards (first 4 cards expanded initially; remaining use hidden="until-found")
    if (!container) return;
    var activeEventName = "all";
    var query = "";
    var cards = [];
    var frag = document.createDocumentFragment();

    eventsData.forEach(function (ev, idx) {
      var card = document.createElement("article");
      card.className = "card";
      var txHref = "transactions.html?item=" + encodeURIComponent(ev.testCase || "");
      var msgRows = (ev.messages || []).map(function (m, mIdx) {
        return (
          "<tr>" +
            '<td class="col-idx">' + (mIdx + 1) + "</td>" +
            '<td class="col-num" style="width:90px;">' + fmtInt(m.count) + "</td>" +
            '<td class="col-text" style="white-space:normal;word-break:break-word;">' + linkifyUrls(m.info) + "</td>" +
          "</tr>"
        );
      }).join("");

      var bodyId = "ev-msg-body-" + idx;
      var defaultOpen = idx < 4;
      var msgCount = ev.messages ? ev.messages.length : 0;

      card.innerHTML =
        '<div class="card-header" style="cursor:pointer;" data-toggle-body="' + bodyId + '">' +
          '<div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap;">' +
            '<span class="status-pill warn">' + esc(ev.name) + "</span>" +
            '<a href="' + esc(txHref) + '" style="font-weight:800;font-size:12.5px;" onclick="event.stopPropagation()">' + esc(ev.testCase) + "</a>" +
            '<span class="card-subtitle">• ' + fmtInt(msgCount) + " distinct message pattern(s)</span>" +
          "</div>" +
          '<div style="display:flex;align-items:center;gap:8px;">' +
            (ev.droppedCount > 0 ? '<span class="badge-tag cell-warn">Dropped: ' + fmtInt(ev.droppedCount) + "</span>" : "") +
            '<span class="badge-tag">Total: <strong>' + fmtInt(ev.totalCount) + "</strong></span>" +
            '<button type="button" class="btn-chip js-ev-toggle" aria-expanded="' + (defaultOpen ? "true" : "false") + '" aria-controls="' + bodyId + '">' +
              (defaultOpen ? "▲ Hide Messages" : "▼ Show Messages (" + fmtInt(msgCount) + ")") +
            "</button>" +
          "</div>" +
        "</div>" +
        '<div id="' + bodyId + '"' + (defaultOpen ? "" : ' hidden="until-found"') + ">" +
          '<div class="table-scroll" style="max-height:280px;overflow-y:auto;">' +
            '<table class="data-table">' +
              '<thead><tr><th scope="col" class="col-idx">#</th><th scope="col" class="col-num" style="width:90px;">Occurrences</th><th scope="col" class="col-text">Message / Detail</th></tr></thead>' +
              "<tbody>" + msgRows + "</tbody>" +
            "</table>" +
          "</div>" +
        "</div>";

      var headerEl = card.querySelector("[data-toggle-body]");
      var toggleBtn = card.querySelector(".js-ev-toggle");
      var bodyEl = card.querySelector("#" + bodyId);
      function setOpen(open) {
        if (open) {
          bodyEl.removeAttribute("hidden");
        } else {
          bodyEl.setAttribute("hidden", "until-found");
        }
        toggleBtn.setAttribute("aria-expanded", open ? "true" : "false");
        toggleBtn.textContent = open ? "▲ Hide Messages" : "▼ Show Messages (" + fmtInt(msgCount) + ")";
      }
      headerEl.addEventListener("click", function () {
        setOpen(bodyEl.hasAttribute("hidden"));
      });
      bodyEl.addEventListener("beforematch", function () {
        setOpen(true);
      });

      var msgBlob = (ev.messages || []).map(function (m) { return m.info; }).join(" ");
      cards.push({
        ev: ev,
        el: card,
        blob: ((ev.name || "") + " " + (ev.testCase || "") + " " + msgBlob).toLowerCase()
      });
      frag.appendChild(card);
    });
    container.innerHTML = "";
    container.appendChild(frag);

    function applyFilter() {
      var vis = 0;
      var visOccurrences = 0;
      cards.forEach(function (c) {
        var nameOk = activeEventName === "all" || c.ev.name === activeEventName;
        var textOk = UI.matchesFilter(c.blob, query, { count: c.ev.totalCount, events: c.ev.totalCount });
        var show = nameOk && textOk;
        c.el.hidden = !show;
        if (show) {
          vis++;
          visOccurrences += Number(c.ev.totalCount) || 0;
        }
      });
      if (countBadge) {
        countBadge.textContent = fmtInt(vis) + " / " + fmtInt(cards.length) + " groups (" + fmtInt(visOccurrences) + " events)";
      }
    }

    if (filterInput) {
      filterInput.addEventListener("input", function () {
        query = filterInput.value;
        applyFilter();
      });
    }

    if (chipWrap) {
      chipWrap.addEventListener("click", function (e) {
        var btn = e.target.closest("[data-ev-chip]");
        if (!btn) return;
        activeEventName = btn.getAttribute("data-ev-chip");
        Array.prototype.forEach.call(chipWrap.querySelectorAll("[data-ev-chip]"), function (b) {
          b.classList.toggle("active", b === btn);
        });
        applyFilter();
      });
    }

    applyFilter();

    if (!("onbeforematch" in HTMLElement.prototype)) {
      Array.prototype.forEach.call(container.querySelectorAll('[hidden="until-found"]'), function (el) {
        el.removeAttribute("hidden");
        var btn = container.querySelector('[aria-controls="' + el.id + '"]');
        if (btn) btn.setAttribute("aria-expanded", "true");
      });
    }
  });
})();
