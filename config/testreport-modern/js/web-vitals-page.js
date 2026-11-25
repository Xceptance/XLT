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
 * XLT Modern Load Report — Google Core Web Vitals Renderer (`web-vitals.html`)
 *
 * @fileoverview
 * Renders real-browser Core Web Vitals telemetry captured by `XltChromeDriver`, following the
 * Google Chrome User Experience (CrUX) 75th-percentile rating methodology (`good` / `improve` / `poor`)
 * with stacked distribution progress bars per action and metric.
 *
 * Consumes:
 * - `window.XLT_WEB_VITALS`: `{ items: Array<{ name, lcp, cls, inp, fcp, fid, ttfb }> }`
 *   where each vital object contains `{ score, rating, goodCount, improveCount, poorCount }`.
 * - `window.XLT_UI`: `esc`, `fmtInt`, `fmtDecOrDash` (absent metrics render as `"—"`).
 *
 * Target DOM Containers:
 * - `#wv-kpi-grid` — top 4-card summary strip
 * - `#wv-toolbar`, `#wv-filter`, `#wv-chips`, `#wv-visible-count` — filter controls
 * - `#wv-table tbody` — sortable matrix linking each action back to `actions.html?item=...`
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  // Unrecorded vitals must render "—" rather than "0"
  var fmtMetric = UI.fmtDecOrDash;

  /** Ordered Core Web Vitals + secondary timing metrics columns (CLS is unitless with 3 decimals). */
  var METRICS = [
    { key: "lcp", label: "LCP [ms]", digits: 0, unit: "ms" },
    { key: "cls", label: "CLS", digits: 3, unit: "" },
    { key: "inp", label: "INP [ms]", digits: 0, unit: "ms" },
    { key: "fcp", label: "FCP [ms]", digits: 0, unit: "ms" },
    { key: "fid", label: "FID [ms]", digits: 0, unit: "ms" },
    { key: "ttfb", label: "TTFB [ms]", digits: 0, unit: "ms" }
  ];

  /**
   * Maps CrUX rating keys to semantic `.status-pill` modifier classes.
   * @param {"good"|"improve"|"poor"|string} rating Rating keyword.
   * @returns {string} CSS modifier class (`"pass"`, `"warn"`, `"fail"`, or `""`).
   */
  function pillClass(rating) {
    if (rating === "good") return "pass";
    if (rating === "improve") return "warn";
    if (rating === "poor") return "fail";
    return "";
  }

  /**
   * Determines the worst CrUX rating across all recorded vitals on an action row
   * so rating filter chips (`All` / `Poor` / `Needs Improvement` / `Good`) surface problem actions.
   *
   * @param {Object} item Action web vitals entry.
   * @returns {"good"|"improve"|"poor"|"none"} Worst rating observed on the action.
   */
  function worstRating(item) {
    var rank = { good: 1, improve: 2, poor: 3 };
    var maxR = 0;
    var label = "none";
    METRICS.forEach(function (m) {
      var v = item[m.key];
      if (v && rank[v.rating] && rank[v.rating] > maxR) {
        maxR = rank[v.rating];
        label = v.rating;
      }
    });
    return label;
  }

  /**
   * Renders a single vital `<td>` with its numeric `data-val` sort key, CrUX rating pill,
   * and optional stacked Good / Needs Improvement / Poor sample distribution bar.
   *
   * @param {Object|null|undefined} v Vital metric object (`score`, `rating`, bucket counts).
   * @param {{ key: string, digits: number, unit: string }} m Metric descriptor from `METRICS`.
   * @returns {string} HTML `<td>` element string.
   */
  function renderVitalCell(v, m) {
    if (!v || v.score === undefined || v.score === null) {
      return '<td class="col-num cell-muted" data-val="-1">—</td>';
    }
    var good = Number(v.goodCount) || 0;
    var imp = Number(v.improveCount) || 0;
    var poor = Number(v.poorCount) || 0;
    var tot = good + imp + poor;
    var gPct = tot > 0 ? ((good * 100) / tot).toFixed(1) : "0.0";
    var iPct = tot > 0 ? ((imp * 100) / tot).toFixed(1) : "0.0";
    var pPct = tot > 0 ? ((poor * 100) / tot).toFixed(1) : "0.0";
    var pCls = pillClass(v.rating);
    var scoreTxt = fmtMetric(v.score, m.digits) + (m.unit ? " " + m.unit : "");

    var barHtml = tot > 0
      ? '<div style="display:flex;height:5px;border-radius:3px;overflow:hidden;margin-top:4px;background:var(--bg-subtle);" title="Good: ' + fmtInt(good) + " (" + gPct + "%) • Needs Improvement: " + fmtInt(imp) + " (" + iPct + "%) • Poor: " + fmtInt(poor) + " (" + pPct + '%)">' +
          (good > 0 ? '<span style="flex-grow:' + good + ';background:#10b981;"></span>' : "") +
          (imp > 0 ? '<span style="flex-grow:' + imp + ';background:#f59e0b;"></span>' : "") +
          (poor > 0 ? '<span style="flex-grow:' + poor + ';background:#ef4444;"></span>' : "") +
        "</div>"
      : "";

    return (
      '<td class="col-num" data-val="' + Number(v.score) + '">' +
        '<span class="status-pill ' + pCls + '">' + esc(scoreTxt) + "</span>" +
        barHtml +
      "</td>"
    );
  }

  document.addEventListener("DOMContentLoaded", function () {
    var raw = window.XLT_WEB_VITALS || {};
    var items = Array.isArray(raw) ? raw : (raw.items || []);
    var kpiGrid = document.getElementById("wv-kpi-grid");
    var toolbar = document.getElementById("wv-toolbar");
    var tbody = document.querySelector("#wv-table tbody");
    var countBadge = document.getElementById("wv-visible-count");
    var filterInput = document.getElementById("wv-filter");
    var chipWrap = document.getElementById("wv-chips");

    // Summarize total sample count and action-level pass/poor tallies
    var totalSamples = 0;
    var poorActions = 0;
    var goodActions = 0;
    items.forEach(function (it) {
      var w = worstRating(it);
      if (w === "poor") poorActions++;
      else if (w === "good") goodActions++;
      var ref = it.lcp || it.fcp || it.cls || it.inp || it.ttfb;
      if (ref) {
        totalSamples += (Number(ref.goodCount) || 0) + (Number(ref.improveCount) || 0) + (Number(ref.poorCount) || 0);
      }
    });

    if (kpiGrid) {
      var active = items.length > 0;
      kpiGrid.innerHTML =
        '<article class="kpi-card">' +
          '<span class="kpi-label">Core Web Vitals Status</span>' +
          '<div class="kpi-val" style="color:' + (active ? "var(--status-pass-text)" : "var(--text-muted)") + ';">' + (active ? "Recorded" : "Inactive") + "</div>" +
          '<div class="kpi-sub">' + (active ? fmtInt(items.length) + " instrumented browser action(s)" : "No browser Web Vitals recorded for this run") + "</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">CWV Samples Collected</span>' +
          '<div class="kpi-val">' + fmtInt(totalSamples) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">samples</span></div>' +
          '<div class="kpi-sub">' + (active ? fmtInt(goodActions) + " all-good • " + fmtInt(poorActions) + " poor action(s)" : "Requires WebDriver browser profile") + "</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Evaluation Quantile</span>' +
          '<div class="kpi-val">75th <span style="font-size:14px;font-weight:600;color:var(--text-muted);">Percentile</span></div>' +
          '<div class="kpi-sub">Google Chrome User Experience (CrUX) standard</div>' +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Core Vitals Metrics</span>' +
          '<div class="kpi-val">LCP • CLS • INP</div>' +
          '<div class="kpi-sub">Plus secondary FCP, FID, and TTFB telemetry</div>' +
        "</article>";
    }

    if (!tbody) return;
    if (!items.length) {
      if (toolbar) toolbar.hidden = true;
      if (countBadge) countBadge.textContent = "0 actions";
      tbody.innerHTML =
        "<tr>" +
          '<td colspan="8" class="col-text" style="text-align:center;padding:40px 16px;color:var(--text-muted);">' +
            "<strong>No Web Vitals records available for this test run.</strong><br>" +
            '<span style="font-size:12px;">Web Vitals telemetry requires real-browser execution (e.g. <code>XltChromeDriver</code>).</span>' +
          "</td>" +
        "</tr>";
      return;
    }

    var sortedItems = items.slice().sort(function (a, b) {
      return UI.collator.compare(a.name || "", b.name || "");
    });
    if (toolbar) toolbar.hidden = false;
    var records = [];
    var frag = document.createDocumentFragment();
    sortedItems.forEach(function (it, idx) {
      var tr = document.createElement("tr");
      var worst = worstRating(it);
      tr.innerHTML =
        '<td class="col-idx">' + (idx + 1) + "</td>" +
        '<td class="col-text"><a href="actions.html?item=' + encodeURIComponent(it.name || "") + '">' + esc(it.name || "") + "</a></td>" +
        METRICS.map(function (m) { return renderVitalCell(it[m.key], m); }).join("");
      frag.appendChild(tr);
      records.push({ tr: tr, name: (it.name || "").toLowerCase(), worst: worst });
    });
    tbody.innerHTML = "";
    tbody.appendChild(frag);

    // Interactive action-name + worst-rating filter (toggles tr.hidden without rebuilding DOM)
    var activeChip = "all";
    var query = "";
    function applyFilter() {
      var q = query.toLowerCase().trim();
      var vis = 0;
      records.forEach(function (r) {
        var chipOk = activeChip === "all" || r.worst === activeChip;
        var textOk = !q || r.name.indexOf(q) !== -1;
        var show = chipOk && textOk;
        r.tr.hidden = !show;
        if (show) {
          vis++;
          r.tr.cells[0].textContent = String(vis);
        }
      });
      if (countBadge) countBadge.textContent = vis + " / " + records.length + " actions";
    }

    if (filterInput) {
      filterInput.addEventListener("input", function () {
        query = filterInput.value;
        applyFilter();
      });
    }
    if (chipWrap) {
      chipWrap.addEventListener("click", function (e) {
        var btn = e.target.closest("[data-wv-chip]");
        if (!btn) return;
        activeChip = btn.getAttribute("data-wv-chip");
        Array.prototype.forEach.call(chipWrap.querySelectorAll("[data-wv-chip]"), function (b) {
          b.classList.toggle("active", b === btn);
        });
        applyFilter();
      });
    }
    applyFilter();
  });
})();
