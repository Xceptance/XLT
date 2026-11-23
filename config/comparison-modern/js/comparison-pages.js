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
 * XLT Modern Comparison & Trend Report — Secondary Page Renderers
 * (`web-vitals`, `errors`, `events`, `network`, `configuration`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI || {};
  var CMP = window.XLT_CMP || {};
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var fmtDec = UI.fmtDec;
  var fmtBytes = UI.fmtBytes;
  var fmtDurationHms = UI.fmtDurationHms;
  var fmtThinkTime = UI.fmtThinkTime;

  function clearTableSortState(tableId) {
    var table = document.getElementById(tableId);
    if (!table || !table.tHead) return;
    Array.prototype.forEach.call(table.tHead.querySelectorAll("th[aria-sort]"), function (th) {
      th.removeAttribute("aria-sort");
    });
  }

  function renderUnavailableNotice(containerId, title, detail) {
    var el = document.getElementById(containerId);
    if (!el) return;
    el.hidden = false;
    el.innerHTML =
      '<div class="card"><div class="cmp-empty-panel">' +
        "<strong>" + esc(title) + "</strong>" +
        "<p>" + esc(detail) + "</p>" +
      "</div></div>";
  }

  function wireViewToggleGroup(toolbarEl) {
    if (!toolbarEl) return;
    var st = CMP.getState();
    Array.prototype.forEach.call(toolbarEl.querySelectorAll("[data-cmp-view]"), function (b) {
      b.setAttribute("aria-pressed", b.getAttribute("data-cmp-view") === st.viewMode ? "true" : "false");
    });
    toolbarEl.addEventListener("click", function (e) {
      var btn = e.target.closest("[data-cmp-view]");
      if (!btn) return;
      var mode = btn.getAttribute("data-cmp-view");
      Array.prototype.forEach.call(toolbarEl.querySelectorAll("[data-cmp-view]"), function (b) {
        b.setAttribute("aria-pressed", b === btn ? "true" : "false");
      });
      CMP.setState({ viewMode: mode });
    });
    if (CMP.installToolbarFocusgroupFallback) CMP.installToolbarFocusgroupFallback(toolbarEl);
  }

  // =========================================================================
  // 1. Web Vitals
  // =========================================================================

  function initWebVitals() {
    var payload = window.XLT_WEB_VITALS || { items: [] };
    var items = Array.isArray(payload.items) ? payload.items : [];
    var content = document.getElementById("wv-content");
    if (!items.length) {
      if (content) content.hidden = true;
      renderUnavailableNotice("wv-empty", "No Web Vitals recorded",
        "None of the indexed runs contain Core Web Vitals measurements.");
      return;
    }

    var toolbar = document.getElementById("cmp-table-controls");
    wireViewToggleGroup(toolbar);
    var searchEl = document.getElementById("dir-filter-input");
    var q = "";
    if (searchEl) searchEl.addEventListener("input", function () { q = searchEl.value || ""; draw(); });

    var metrics = [
      { key: "fcp", label: "FCP [ms]", fmt: fmtInt, unit: "" },
      { key: "lcp", label: "LCP [ms]", fmt: fmtInt, unit: "" },
      { key: "cls", label: "CLS", fmt: function (n) { return fmtDec(n, 3); }, unit: "" },
      { key: "fid", label: "FID [ms]", fmt: fmtInt, unit: "" },
      { key: "inp", label: "INP [ms]", fmt: fmtInt, unit: "" },
      { key: "ttfb", label: "TTFB [ms]", fmt: fmtInt, unit: "" }
    ];

    function draw() {
      clearTableSortState("wv-table");
      var st = CMP.getState();
      var tbody = document.querySelector("#wv-table tbody");
      var cntBadge = document.getElementById("dir-visible-count");
      if (!tbody) return;

      var parsed = q.trim() && UI.parseSearchQuery ? UI.parseSearchQuery(q) : null;
      var visible = items.filter(function (it) {
        return !parsed || UI.matchesParsedSearchQuery(it.name || "", parsed, {});
      });

      if (cntBadge) cntBadge.textContent = fmtInt(visible.length) + " of " + fmtInt(items.length) + " shown";

      tbody.innerHTML = visible.map(function (it, idx) {
        var rA = (it.runs && it.runs[st.baseIdx]) || {};
        var rB = (it.runs && it.runs[st.targetIdx]) || {};
        var cells = metrics.map(function (m) {
          var d = CMP.calcDelta(rA[m.key], rB[m.key]);
          var cd = CMP.buildComparisonCellData(d, m.fmt, m.unit, "lower", st.viewMode, { deadband: 2.0 });
          var sortVal = d.valB !== null ? d.valB : (d.valA !== null ? d.valA : -1);
          return '<td class="col-num" data-val="' + sortVal + '" title="' + esc(cd.title) + '" data-copy="' + esc(cd.copyText) + '">' + cd.html + "</td>";
        }).join("");
        return (
          "<tr>" +
            '<td class="col-idx">' + (idx + 1) + "</td>" +
            '<td class="col-text" data-copy="' + esc(it.name) + '"><strong>' + esc(it.name) + "</strong></td>" +
            cells +
          "</tr>"
        );
      }).join("");
      if (CMP.syncTableAlignment) CMP.syncTableAlignment(document.getElementById("wv-table"));
    }

    draw();
    window.addEventListener("xlt-cmp-change", draw);
  }

  // =========================================================================
  // 2. Errors Comparison
  // =========================================================================

  function initErrors() {
    var payload = window.XLT_ERRORS || { available: false, items: [] };
    var wrap = document.getElementById("err-content");
    if (!payload.available) {
      if (wrap) wrap.hidden = true;
      renderUnavailableNotice("err-empty", "Error Signatures Unavailable",
        "Detailed error stack-trace signatures are captured when comparing testreport.xml directories via create_diff_report / create_trend_report. Legacy diffreport.xml / trendreport.xml files only store aggregate error counts per transaction/action/request.");
      return;
    }

    var items = Array.isArray(payload.items) ? payload.items : [];
    var statusFilter = "all";
    var q = "";

    var toolbar = document.getElementById("cmp-table-controls");
    wireViewToggleGroup(toolbar);
    if (toolbar) {
      toolbar.addEventListener("click", function (e) {
        var btn = e.target.closest("[data-status-filter]");
        if (!btn) return;
        statusFilter = btn.getAttribute("data-status-filter") || "all";
        Array.prototype.forEach.call(toolbar.querySelectorAll("[data-status-filter]"), function (b) {
          b.setAttribute("aria-pressed", b === btn ? "true" : "false");
        });
        draw();
      });
    }
    var searchEl = document.getElementById("dir-filter-input");
    if (searchEl) searchEl.addEventListener("input", function () { q = searchEl.value || ""; draw(); });
    var errHead = document.querySelector("#err-table thead");
    if (errHead) {
      errHead.addEventListener("click", function (e) {
        if (!e.target.closest("button.th-sort-btn")) return;
        Array.prototype.forEach.call(document.querySelectorAll("#err-table tbody tr.drawer-row"), function (dr) { dr.remove(); });
        Array.prototype.forEach.call(document.querySelectorAll("#err-table tbody button.drawer-btn"), function (b) {
          b.setAttribute("aria-expanded", "false");
          b.textContent = "▸";
        });
      });
    }

    function draw() {
      clearTableSortState("err-table");
      var st = CMP.getState();
      var totalA = 0, totalB = 0, newSigs = 0, resolvedSigs = 0;

      var enriched = items.map(function (it) {
        var cA = Number((it.runs && it.runs[st.baseIdx]) || 0);
        var cB = Number((it.runs && it.runs[st.targetIdx]) || 0);
        totalA += cA;
        totalB += cB;
        var stTag;
        if (cA === 0 && cB > 0) { stTag = "new"; newSigs++; }
        else if (cA > 0 && cB === 0) { stTag = "resolved"; resolvedSigs++; }
        else if (cB > cA) stTag = "regressed";
        else if (cB < cA) stTag = "improved";
        else stTag = "unchanged";
        return { item: it, cA: cA, cB: cB, delta: cB - cA, status: stTag };
      });

      var kpis = document.getElementById("err-kpis");
      if (kpis) {
        var dTot = CMP.calcDelta(totalA, totalB);
        var pc = CMP.polarityClass(dTot, "lower", 0);
        kpis.innerHTML =
          '<article class="kpi-card"><span class="kpi-label">Target Errors (B)</span><div class="kpi-val' + (totalB > 0 ? " cell-err" : "") + '">' + fmtInt(totalB) + '</div><div class="kpi-sub">Baseline (A): ' + fmtInt(totalA) + "</div></article>" +
          '<article class="kpi-card"><span class="kpi-label">Net Error Delta</span><div class="kpi-val">' + esc(CMP.formatAbsDelta(dTot, fmtInt, "")) + ' <span class="cmp-delta-pill ' + pc + '">' + esc(CMP.formatPct(dTot)) + '</span></div><div class="kpi-sub">Across matched signatures</div></article>' +
          '<article class="kpi-card"><span class="kpi-label">New Signatures in B</span><div class="kpi-val' + (newSigs > 0 ? " cell-err" : "") + '">' + fmtInt(newSigs) + '</div><div class="kpi-sub">Absent in Baseline (A)</div></article>' +
          '<article class="kpi-card"><span class="kpi-label">Resolved Signatures</span><div class="kpi-val" style="color:var(--status-pass-text)">' + fmtInt(resolvedSigs) + '</div><div class="kpi-sub">Cleared since Baseline (A)</div></article>';
      }

      var parsed = q.trim() && UI.parseSearchQuery ? UI.parseSearchQuery(q) : null;
      var visible = enriched.filter(function (row) {
        if (row.cA === 0 && row.cB === 0) return false;
        if (statusFilter !== "all" && row.status !== statusFilter) return false;
        if (parsed && UI.matchesParsedSearchQuery) {
          var txt = [row.item.testCaseName, row.item.actionName, row.item.requestName, row.item.message, row.status].join(" ");
          if (!UI.matchesParsedSearchQuery(txt, parsed, { count: row.cB, errors: row.cB })) return false;
        }
        return true;
      }).sort(function (a, b) { return Math.abs(b.delta) - Math.abs(a.delta) || (b.cB - a.cB); });

      var cntBadge = document.getElementById("dir-visible-count");
      if (cntBadge) cntBadge.textContent = fmtInt(visible.length) + " shown";

      var tbody = document.querySelector("#err-table tbody");
      if (!tbody) return;
      if (!visible.length) {
        tbody.innerHTML = '<tr><td colspan="6" class="cmp-empty-panel">No errors match the active runs or filters.</td></tr>';
        return;
      }

      tbody.innerHTML = "";
      visible.forEach(function (row, idx) {
        var it = row.item;
        var pillCls = row.status === "resolved" || row.status === "improved" ? "delta-good"
          : (row.status === "new" ? "delta-added" : (row.status === "regressed" ? "delta-bad" : "delta-flat"));
        var d = CMP.calcDelta(row.cA, row.cB);
        var cd = CMP.buildComparisonCellData(d, fmtInt, "", "lower", st.viewMode, { deadband: 0, isError: true, absPill: true });

        var tr = document.createElement("tr");
        var scopeText = it.actionName + (it.requestName ? " / " + it.requestName : "");
        tr.innerHTML =
          '<td class="col-idx">' + (idx + 1) + "</td>" +
          '<td class="col-cat"><span class="cmp-delta-pill ' + pillCls + '">' + esc(row.status.toUpperCase()) + "</span></td>" +
          '<td class="col-text"><strong>' + esc(it.testCaseName || "—") + '</strong><div class="name-cell-sub">' + esc(scopeText) + "</div></td>" +
          '<td class="col-text" style="white-space:normal;max-width:460px">' + esc(it.message || "(no message)") + "</td>" +
          '<td class="col-num" data-val="' + row.delta + '" title="' + esc(cd.title) + '" data-copy="' + esc(cd.copyText) + '">' + cd.html + "</td>" +
          '<td class="col-idx"><button type="button" class="drawer-btn" aria-expanded="false" aria-label="Toggle trace">▸</button></td>';

        var dBtn = tr.querySelector("button.drawer-btn");
        if (dBtn) {
          dBtn.addEventListener("click", function () {
            var next = tr.nextElementSibling;
            if (next && next.classList.contains("drawer-row")) {
              next.remove();
              dBtn.setAttribute("aria-expanded", "false");
              dBtn.textContent = "▸";
            } else {
              var dr = document.createElement("tr");
              dr.className = "drawer-row";
              dr.innerHTML = '<td colspan="6"><pre class="trace-box" style="margin:0;white-space:pre-wrap;font-size:11px">' +
                esc(it.traceSample || "No stack trace recorded.") + "</pre></td>";
              tr.insertAdjacentElement("afterend", dr);
              dBtn.setAttribute("aria-expanded", "true");
              dBtn.textContent = "▾";
            }
          });
        }
        tbody.appendChild(tr);
      });
      if (CMP.syncTableAlignment) CMP.syncTableAlignment(document.getElementById("err-table"));
    }

    draw();
    window.addEventListener("xlt-cmp-change", draw);
  }

  // =========================================================================
  // 3. Events Comparison (True Orange Accent)
  // =========================================================================

  function initEvents() {
    var payload = window.XLT_EVENTS || { available: false, items: [] };
    var wrap = document.getElementById("ev-content");
    if (!payload.available) {
      if (wrap) wrap.hidden = true;
      renderUnavailableNotice("ev-empty", "Event Breakdown Unavailable",
        "Per-event counts are captured when comparing source testreport.xml directories via create_diff_report / create_trend_report.");
      return;
    }

    var items = Array.isArray(payload.items) ? payload.items : [];
    var q = "";
    var toolbar = document.getElementById("cmp-table-controls");
    wireViewToggleGroup(toolbar);
    var searchEl = document.getElementById("dir-filter-input");
    if (searchEl) searchEl.addEventListener("input", function () { q = searchEl.value || ""; draw(); });

    function draw() {
      clearTableSortState("ev-table");
      var st = CMP.getState();
      var totA = 0, totB = 0;
      items.forEach(function (it) {
        totA += Number((it.runs && it.runs[st.baseIdx]) || 0);
        totB += Number((it.runs && it.runs[st.targetIdx]) || 0);
      });

      var kpis = document.getElementById("ev-kpis");
      if (kpis) {
        var dTot = CMP.calcDelta(totA, totB);
        var pc = CMP.polarityClass(dTot, "warn", 0);
        kpis.innerHTML =
          '<article class="kpi-card"><span class="kpi-label">Target Events (B)</span><div class="kpi-val' + (totB > 0 ? " cell-warn" : "") + '">' + fmtInt(totB) + '</div><div class="kpi-sub">Baseline (A): ' + fmtInt(totA) + "</div></article>" +
          '<article class="kpi-card"><span class="kpi-label">Net Events Delta</span><div class="kpi-val' + (totB > 0 ? " cell-warn" : "") + '">' + esc(CMP.formatAbsDelta(dTot, fmtInt, "")) + ' <span class="cmp-delta-pill ' + pc + '">' + esc(CMP.formatPct(dTot)) + '</span></div><div class="kpi-sub">Distinct event groups: ' + fmtInt(items.length) + "</div></article>";
      }

      var parsed = q.trim() && UI.parseSearchQuery ? UI.parseSearchQuery(q) : null;
      var visible = items.filter(function (it) {
        var cA = Number((it.runs && it.runs[st.baseIdx]) || 0);
        var cB = Number((it.runs && it.runs[st.targetIdx]) || 0);
        if (cA === 0 && cB === 0) return false;
        if (parsed && UI.matchesParsedSearchQuery) {
          var txt = [it.testCaseName, it.name].concat(it.sampleMessages || []).join(" ");
          if (!UI.matchesParsedSearchQuery(txt, parsed, { count: cB })) return false;
        }
        return true;
      });

      var cntBadge = document.getElementById("dir-visible-count");
      if (cntBadge) cntBadge.textContent = fmtInt(visible.length) + " shown";

      var tbody = document.querySelector("#ev-table tbody");
      if (!tbody) return;
      if (!visible.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="cmp-empty-panel">No events match the selected runs or filter.</td></tr>';
        return;
      }

      tbody.innerHTML = visible.map(function (it, idx) {
        var cA = Number((it.runs && it.runs[st.baseIdx]) || 0);
        var cB = Number((it.runs && it.runs[st.targetIdx]) || 0);
        var d = CMP.calcDelta(cA, cB);
        var cd = CMP.buildComparisonCellData(d, fmtInt, "", "warn", st.viewMode, { deadband: 0, isEvent: true, absPill: true });
        var sample = (it.sampleMessages && it.sampleMessages[0]) || "";
        return (
          "<tr>" +
            '<td class="col-idx">' + (idx + 1) + "</td>" +
            '<td class="col-text">' + esc(it.testCaseName || "—") + "</td>" +
            '<td class="col-text"><strong class="cell-warn">' + esc(it.name || "—") + "</strong></td>" +
            '<td class="col-text" style="white-space:normal;max-width:440px">' + esc(sample || "—") + "</td>" +
            '<td class="col-num" data-val="' + (cB - cA) + '" title="' + esc(cd.title) + '" data-copy="' + esc(cd.copyText) + '">' + cd.html + "</td>" +
          "</tr>"
        );
      }).join("");
      if (CMP.syncTableAlignment) CMP.syncTableAlignment(document.getElementById("ev-table"));
    }

    draw();
    window.addEventListener("xlt-cmp-change", draw);
  }

  // =========================================================================
  // 4. Network Comparison & Multi-Run Traffic Charts
  // =========================================================================

  function initNetwork() {
    var net = window.XLT_NETWORK || { available: false };
    var toolbar = document.getElementById("cmp-table-controls");
    wireViewToggleGroup(toolbar);

    var runs = CMP.getRuns ? CMP.getRuns() : [];
    var hasDuration = runs.some(function (r) { return r && Number(r.durationSec) > 0; });
    var hasTrafficData = runs.some(function (r) {
      return r && (r.bytesReceived != null || r.bytesSent != null || r.hits != null);
    });
    var responseCodes = Array.isArray(net.responseCodes) ? net.responseCodes : [];
    var errorCodeRows = responseCodes.filter(function (rc) {
      var code = Number(rc.code);
      return code === 0 || code >= 400;
    });
    var hasHttpErrorRows = Boolean(net.available) && errorCodeRows.some(function (rc) {
      return Array.isArray(rc.runs) && rc.runs.some(function (v) { return Number(v) > 0; });
    });

    var trafficMode = hasDuration ? "rate" : "total";
    var codesMode = "all";
    var mixMode = "contentTypes";
    var trafficSeriesState = {
      recv: true,
      sent: true,
      hits: true,
      httpErr: hasHttpErrorRows
    };

    var trafficChart = null;
    var codesChart = null;
    var mixChart = null;
    var resizeObservers = [];

    function netThemePalette() {
      var dark = document.documentElement.classList.contains("dark-theme");
      return {
        dark: dark,
        text: dark ? "#e4e4e7" : "#18181b",
        muted: dark ? "#a1a1aa" : "#666e7a",
        split: dark ? "#2e2e36" : "#e5e7eb",
        axis: dark ? "#3f3f46" : "#cbd5e1",
        tipBg: dark ? "rgba(28, 28, 31, 0.96)" : "rgba(255, 255, 255, 0.97)",
        tipBorder: dark ? "#3f3f46" : "#c9d0d8",
        baseColor: dark ? "#60a5fa" : "#244b73",
        targetColor: dark ? "#fb923c" : "#e07000",
        recv: dark ? "#34d399" : "#059669",
        sent: dark ? "#fb923c" : "#e07000",
        hitsBar: dark ? "rgba(96, 165, 250, 0.28)" : "rgba(36, 75, 115, 0.22)",
        hitsSwatch: dark ? "#60a5fa" : "#244b73",
        err: dark ? "#f87171" : "#dc2626",
        mixPalette: dark
          ? ["#60a5fa", "#34d399", "#fb923c", "#c084fc", "#38bdf8", "#fbbf24", "#f472b6", "#94a3b8"]
          : ["#244b73", "#059669", "#e07000", "#7c3aed", "#0284c7", "#d97706", "#db2777", "#64748b"]
      };
    }

    function ensureNetChart(domId, existing) {
      var dom = document.getElementById(domId);
      if (!dom || typeof window.echarts === "undefined") return null;
      if (existing && !existing.isDisposed()) return existing;
      var chart = window.echarts.init(dom, null, { renderer: "canvas" });
      if (typeof window.ResizeObserver !== "undefined") {
        var ro = new window.ResizeObserver(function () {
          if (!chart.isDisposed()) chart.resize();
        });
        ro.observe(dom);
        resizeObservers.push(ro);
      }
      return chart;
    }

    function pickByteScale(maxBytes) {
      var v = Math.max(0, Number(maxBytes) || 0);
      if (v >= 1099511627776) return { div: 1099511627776, unit: "TB" };
      if (v >= 1073741824) return { div: 1073741824, unit: "GB" };
      if (v >= 1048576) return { div: 1048576, unit: "MB" };
      if (v >= 1024) return { div: 1024, unit: "KB" };
      return { div: 1, unit: "B" };
    }

    function fmtCompactCount(n) {
      var v = Number(n);
      if (!Number.isFinite(v)) return "0";
      if (Math.abs(v) >= 1000000) return (v / 1000000).toFixed(1).replace(/\.0$/, "") + "M";
      if (Math.abs(v) >= 1000) return (v / 1000).toFixed(1).replace(/\.0$/, "") + "k";
      return fmtInt(v);
    }

    function sumHttpErrorsForRun(runIdx) {
      if (!net.available || !errorCodeRows.length) return null;
      var sum = 0;
      errorCodeRows.forEach(function (rc) {
        sum += Number((rc.runs && rc.runs[runIdx]) || 0);
      });
      return sum;
    }

    function buildRunLabels(st) {
      return runs.map(function (r, idx) {
        var tag = idx === st.baseIdx ? " [A]" : (idx === st.targetIdx ? " [B]" : "");
        return "#" + (idx + 1) + " " + (r.commentHeadline || r.name || ("Run " + (idx + 1))) + tag;
      });
    }

    function buildCompactRunLabels(st) {
      if (runs.length <= 6) return buildRunLabels(st);
      return runs.map(function (_, idx) {
        var tag = idx === st.baseIdx ? " [A]" : (idx === st.targetIdx ? " [B]" : "");
        return "#" + (idx + 1) + tag;
      });
    }

    function statusCodeColor(code, idx, dark) {
      var c = Number(code);
      var greens = dark ? ["#34d399", "#2dd4bf", "#6ee7b7"] : ["#059669", "#0d9488", "#10b981"];
      var blues = dark ? ["#60a5fa", "#38bdf8", "#818cf8"] : ["#2563eb", "#0284c7", "#4f46e5"];
      var ambers = dark ? ["#fbbf24", "#fb923c", "#f59e0b"] : ["#d97706", "#ea580c", "#ca8a04"];
      var reds = dark ? ["#f87171", "#fb7185", "#c084fc"] : ["#dc2626", "#e11d48", "#9333ea"];
      if (c >= 200 && c < 300) return greens[idx % greens.length];
      if (c >= 300 && c < 400) return blues[idx % blues.length];
      if (c >= 400 && c < 500) return ambers[idx % ambers.length];
      return reds[idx % reds.length];
    }

    function syncTabRovingState(box) {
      if (!box) return;
      var btns = Array.prototype.slice.call(box.querySelectorAll('[role="tab"]'));
      var hasNativeFg = typeof HTMLElement !== "undefined" && "focusGroup" in HTMLElement.prototype;
      btns.forEach(function (b) {
        var active = b.getAttribute("aria-selected") === "true" && !b.hidden;
        if (active) b.setAttribute("focusgroupstart", "");
        else b.removeAttribute("focusgroupstart");
        if (!hasNativeFg) b.tabIndex = active ? 0 : -1;
      });
    }

    function wireTablist(containerId, attrName, onSelect) {
      var box = document.getElementById(containerId);
      if (!box) return;
      var btns = Array.prototype.slice.call(box.querySelectorAll('[role="tab"]'));
      function activate(btn, shouldFocus) {
        if (!btn || btn.hidden) return;
        btns.forEach(function (b) { b.setAttribute("aria-selected", b === btn ? "true" : "false"); });
        syncTabRovingState(box);
        if (shouldFocus) btn.focus();
        onSelect(btn.getAttribute(attrName));
      }
      box.addEventListener("click", function (e) {
        var btn = e.target.closest('[role="tab"]');
        if (!btn) return;
        activate(btn, false);
      });
      if (typeof HTMLElement === "undefined" || !("focusGroup" in HTMLElement.prototype)) {
        box.addEventListener("keydown", function (e) {
          var visible = btns.filter(function (b) { return !b.hidden; });
          var idx = visible.indexOf(e.target.closest('[role="tab"]'));
          if (idx < 0 || visible.length < 2) return;
          var rtl = getComputedStyle(box).direction === "rtl";
          var nextIdx = -1;
          if (e.key === "ArrowRight") nextIdx = (idx + (rtl ? -1 : 1) + visible.length) % visible.length;
          else if (e.key === "ArrowLeft") nextIdx = (idx + (rtl ? 1 : -1) + visible.length) % visible.length;
          else if (e.key === "Home") nextIdx = 0;
          else if (e.key === "End") nextIdx = visible.length - 1;
          else return;
          e.preventDefault();
          activate(visible[nextIdx], true);
        });
      }
      syncTabRovingState(box);
    }

    function initTabVisibility() {
      var trafficTabs = document.getElementById("net-traffic-mode-tabs");
      if (trafficTabs && !hasDuration) {
        var rateBtn = trafficTabs.querySelector('[data-net-mode="rate"]');
        var totalBtn = trafficTabs.querySelector('[data-net-mode="total"]');
        if (rateBtn) {
          rateBtn.hidden = true;
          rateBtn.setAttribute("aria-selected", "false");
        }
        if (totalBtn) totalBtn.setAttribute("aria-selected", "true");
        syncTabRovingState(trafficTabs);
      }
      var codesTabs = document.getElementById("net-codes-mode-tabs");
      if (codesTabs && !hasHttpErrorRows) {
        var errBtn = codesTabs.querySelector('[data-codes-mode="errors"]');
        if (errBtn) errBtn.hidden = true;
        syncTabRovingState(codesTabs);
      }
    }

    function renderTrafficSeriesBar(seriesDefs) {
      var bar = document.getElementById("net-traffic-series-bar");
      var statusEl = document.getElementById("net-traffic-series-status");
      if (!bar) return;
      if (!seriesDefs.length) {
        bar.hidden = true;
        return;
      }
      bar.hidden = false;

      var sig = seriesDefs.map(function (d) { return d.key + ":" + d.label; }).join("|");
      if (bar.getAttribute("data-net-sig") === sig) {
        seriesDefs.forEach(function (d) {
          var btn = bar.querySelector('button[data-net-series="' + d.key + '"]');
          if (!btn) return;
          btn.setAttribute("aria-pressed", d.active ? "true" : "false");
          btn.style.setProperty("--swatch-color", d.color);
        });
      } else {
        var bwDefs = seriesDefs.filter(function (d) { return d.group === "bw"; });
        var volDefs = seriesDefs.filter(function (d) { return d.group === "vol"; });
        var mkBtn = function (d) {
          return (
            '<button type="button" class="cmp-series-btn" data-net-series="' + esc(d.key) +
            '" aria-pressed="' + (d.active ? "true" : "false") +
            '" style="--swatch-color:' + esc(d.color) + '">' +
              '<span class="cmp-series-swatch is-' + esc(d.swatch) + '" aria-hidden="true"></span>' +
              "<span>" + esc(d.label) + "</span>" +
            "</button>"
          );
        };
        var html = '<span class="cmp-series-label" aria-hidden="true">Series</span>';
        if (bwDefs.length) {
          html += '<div role="group" class="cmp-series-group" aria-label="Bandwidth series">' + bwDefs.map(mkBtn).join("") + "</div>";
        }
        if (bwDefs.length && volDefs.length) {
          html += '<span class="cmp-toolbar-sep" role="separator" aria-orientation="vertical"></span>';
        }
        if (volDefs.length) {
          html += '<div role="group" class="cmp-series-group" aria-label="Hits and error series">' + volDefs.map(mkBtn).join("") + "</div>";
        }
        html +=
          '<span class="cmp-toolbar-sep" role="separator" aria-orientation="vertical"></span>' +
          '<div role="group" class="cmp-series-group" aria-label="Series presets">' +
            '<button type="button" class="cmp-series-action" data-net-action="all" title="Show all network series">All</button>' +
            '<button type="button" class="cmp-series-action" data-net-action="reset" title="Restore default series">Reset</button>' +
          "</div>";
        bar.innerHTML = html;
        bar.setAttribute("data-net-sig", sig);
        if (CMP.installToolbarFocusgroupFallback) CMP.installToolbarFocusgroupFallback(bar);
      }

      if (!bar.hasAttribute("data-net-bound")) {
        bar.setAttribute("data-net-bound", "1");
        bar.addEventListener("click", function (e) {
          var btn = e.target.closest("button[data-net-series], button[data-net-action]");
          if (!btn) return;
          var key = btn.getAttribute("data-net-series");
          var action = btn.getAttribute("data-net-action");
          if (key) {
            trafficSeriesState[key] = btn.getAttribute("aria-pressed") !== "true";
          } else if (action === "all") {
            trafficSeriesState.recv = true;
            trafficSeriesState.sent = true;
            trafficSeriesState.hits = true;
            trafficSeriesState.httpErr = true;
            if (statusEl) statusEl.textContent = "All network traffic series shown";
          } else if (action === "reset") {
            trafficSeriesState.recv = true;
            trafficSeriesState.sent = true;
            trafficSeriesState.hits = true;
            trafficSeriesState.httpErr = hasHttpErrorRows;
            if (statusEl) statusEl.textContent = "Network traffic series reset to default";
          }
          renderTrafficChart(CMP.getState());
        });
      }
    }

    function renderTrafficChart(st) {
      var card = document.getElementById("net-traffic-card");
      if (!card) return;
      if (!hasTrafficData || !runs.length) {
        card.hidden = true;
        return;
      }
      card.hidden = false;
      trafficChart = ensureNetChart("chart-net-traffic", trafficChart);
      if (!trafficChart) return;

      var pal = netThemePalette();
      var isRate = trafficMode === "rate" && hasDuration;
      var labels = buildRunLabels(st);

      var rawRecv = runs.map(function (r) {
        if (!r || r.bytesReceived == null) return null;
        var b = Number(r.bytesReceived);
        if (!isRate) return b;
        var d = Number(r.durationSec);
        return d > 0 ? b / d : null;
      });
      var rawSent = runs.map(function (r) {
        if (!r || r.bytesSent == null) return null;
        var b = Number(r.bytesSent);
        if (!isRate) return b;
        var d = Number(r.durationSec);
        return d > 0 ? b / d : null;
      });
      var rawHits = runs.map(function (r) {
        if (!r || r.hits == null) return null;
        var h = Number(r.hits);
        if (!isRate) return h;
        var d = Number(r.durationSec);
        return d > 0 ? h / d : null;
      });
      var rawErr = hasHttpErrorRows ? runs.map(function (_, i) { return sumHttpErrorsForRun(i); }) : [];

      var hasRecv = rawRecv.some(function (v) { return v !== null; });
      var hasSent = rawSent.some(function (v) { return v !== null; });
      var hasHits = rawHits.some(function (v) { return v !== null; });

      var seriesDefs = [];
      if (hasRecv) seriesDefs.push({ key: "recv", label: isRate ? "Received/s" : "Received", group: "bw", color: pal.recv, swatch: "line", active: Boolean(trafficSeriesState.recv) });
      if (hasSent) seriesDefs.push({ key: "sent", label: isRate ? "Sent/s" : "Sent", group: "bw", color: pal.sent, swatch: "line", active: Boolean(trafficSeriesState.sent) });
      if (hasHits) seriesDefs.push({ key: "hits", label: isRate ? "Hits/s" : "Hits", group: "vol", color: pal.hitsSwatch, swatch: "bar", active: Boolean(trafficSeriesState.hits) });
      if (hasHttpErrorRows) seriesDefs.push({ key: "httpErr", label: "4xx / 5xx", group: "vol", color: pal.err, swatch: "dashed", active: Boolean(trafficSeriesState.httpErr) });
      renderTrafficSeriesBar(seriesDefs);

      var activeRecv = hasRecv && trafficSeriesState.recv;
      var activeSent = hasSent && trafficSeriesState.sent;
      var activeBw = activeRecv || activeSent;
      var activeHits = hasHits && trafficSeriesState.hits;
      var activeErr = hasHttpErrorRows && trafficSeriesState.httpErr;

      var maxBw = 0;
      if (activeRecv) rawRecv.forEach(function (v) { if (v !== null && v > maxBw) maxBw = v; });
      if (activeSent) rawSent.forEach(function (v) { if (v !== null && v > maxBw) maxBw = v; });
      var scale = pickByteScale(maxBw);
      var bwUnitLabel = isRate ? (scale.unit + "/s") : scale.unit;

      var series = [];
      if (activeHits) {
        series.push({
          name: isRate ? "Hits/s" : "Total Hits",
          type: "bar",
          yAxisIndex: 1,
          barMaxWidth: 32,
          data: rawHits,
          itemStyle: { color: pal.hitsBar, borderColor: pal.hitsSwatch, borderWidth: 0.5, borderRadius: [3, 3, 0, 0] }
        });
      }
      if (activeRecv) {
        series.push({
          name: isRate ? "Download (Received/s)" : "Bytes Received",
          type: "line",
          yAxisIndex: 0,
          smooth: 0.12,
          symbolSize: 6,
          data: rawRecv.map(function (v) { return v !== null ? Number((v / scale.div).toFixed(3)) : null; }),
          lineStyle: { width: 2.5, color: pal.recv },
          itemStyle: { color: pal.recv },
          z: 5
        });
      }
      if (activeSent) {
        series.push({
          name: isRate ? "Upload (Sent/s)" : "Bytes Sent",
          type: "line",
          yAxisIndex: 0,
          smooth: 0.12,
          symbolSize: 6,
          data: rawSent.map(function (v) { return v !== null ? Number((v / scale.div).toFixed(3)) : null; }),
          lineStyle: { width: 2, color: pal.sent },
          itemStyle: { color: pal.sent },
          z: 4
        });
      }
      if (activeErr) {
        series.push({
          name: "HTTP Errors (4xx/5xx)",
          type: "line",
          yAxisIndex: 2,
          smooth: 0.12,
          symbol: "circle",
          symbolSize: 6,
          data: rawErr,
          lineStyle: { width: 2, type: "dashed", color: pal.err },
          itemStyle: { color: pal.err },
          z: 6
        });
      }

      var gridLeft = activeBw ? 66 : 24;
      var gridRight = (activeHits && activeErr) ? 116 : ((activeHits || activeErr) ? 66 : 24);
      var splitBw = activeBw;
      var splitHits = !activeBw && activeHits;
      var splitErr = !activeBw && !activeHits && activeErr;

      trafficChart.setOption({
        animationDuration: 180,
        tooltip: {
          trigger: "axis",
          backgroundColor: pal.tipBg,
          borderColor: pal.tipBorder,
          borderWidth: 1,
          textStyle: { color: pal.text, fontSize: 12 },
          formatter: function (params) {
            if (!params || !params.length) return "";
            var idx = params[0].dataIndex;
            var html = '<div style="font-weight:700;margin-bottom:4px">' + esc(labels[idx] || "") + "</div>";
            params.forEach(function (p) {
              var valStr = "—";
              if (p.seriesName.indexOf("Download") === 0 || p.seriesName === "Bytes Received") {
                if (rawRecv[idx] !== null) valStr = fmtBytes(rawRecv[idx]) + (isRate ? "/s" : "");
              } else if (p.seriesName.indexOf("Upload") === 0 || p.seriesName === "Bytes Sent") {
                if (rawSent[idx] !== null) valStr = fmtBytes(rawSent[idx]) + (isRate ? "/s" : "");
              } else if (p.seriesName.indexOf("Hits") !== -1) {
                if (rawHits[idx] !== null) valStr = isRate ? (fmtDec(rawHits[idx], 1) + " /s") : fmtInt(rawHits[idx]);
              } else if (p.value !== null && p.value !== undefined) {
                valStr = fmtInt(p.value);
              }
              html +=
                '<div style="display:flex;align-items:center;justify-content:space-between;gap:14px;font-variant-numeric:tabular-nums">' +
                  "<span>" + p.marker + " " + esc(p.seriesName) + "</span>" +
                  "<strong>" + esc(valStr) + "</strong>" +
                "</div>";
            });
            return html;
          }
        },
        legend: { show: false },
        grid: { left: gridLeft, right: gridRight, top: 26, bottom: 32 },
        xAxis: {
          type: "category",
          data: labels,
          axisLine: { lineStyle: { color: pal.axis } },
          axisLabel: { color: pal.muted, fontSize: 11, overflow: "truncate", width: 130 }
        },
        yAxis: [
          {
            type: "value",
            position: "left",
            show: activeBw,
            name: (isRate ? "Bandwidth" : "Volume") + " [" + bwUnitLabel + "]",
            nameTextStyle: { color: pal.muted, fontSize: 10 },
            axisLabel: { color: pal.muted, fontSize: 11 },
            splitLine: { show: splitBw, lineStyle: { color: pal.split } }
          },
          {
            type: "value",
            position: "right",
            offset: 0,
            show: activeHits,
            name: isRate ? "Hits/s" : "Hits",
            nameTextStyle: { color: pal.hitsSwatch, fontSize: 10 },
            axisLabel: { color: pal.hitsSwatch, fontSize: 11, formatter: function (v) { return isRate ? fmtDec(v, 0) : fmtCompactCount(v); } },
            splitLine: { show: splitHits, lineStyle: { color: pal.split } }
          },
          {
            type: "value",
            position: "right",
            offset: activeHits ? 58 : 0,
            show: activeErr,
            minInterval: 1,
            name: "4xx/5xx",
            nameTextStyle: { color: pal.err, fontSize: 10 },
            axisLabel: { color: pal.err, fontSize: 11 },
            splitLine: { show: splitErr, lineStyle: { color: pal.split } }
          }
        ],
        series: series,
        graphic: series.length ? [] : [{
          type: "text",
          left: "center",
          top: "middle",
          style: { text: "Select one or more series above to plot", fill: pal.muted, fontSize: 12, fontFamily: "Roboto, sans-serif" }
        }]
      }, true);
    }

    function renderResponseCodesChart(st) {
      codesChart = ensureNetChart("chart-net-codes", codesChart);
      if (!codesChart || !net.available) return;
      var pal = netThemePalette();
      var labels = buildRunLabels(st);
      var axisLabels = buildCompactRunLabels(st);

      if (codesMode === "ab") {
        var activeRows = responseCodes.filter(function (rc) {
          return Number((rc.runs && rc.runs[st.baseIdx]) || 0) > 0 || Number((rc.runs && rc.runs[st.targetIdx]) || 0) > 0;
        }).slice().sort(function (a, b) {
          var va = Math.max(Number((a.runs && a.runs[st.baseIdx]) || 0), Number((a.runs && a.runs[st.targetIdx]) || 0));
          var vb = Math.max(Number((b.runs && b.runs[st.baseIdx]) || 0), Number((b.runs && b.runs[st.targetIdx]) || 0));
          return va - vb;
        });
        var cats = activeRows.map(function (rc) { return "HTTP " + rc.code + (rc.statusText ? " " + rc.statusText : ""); });
        var valsA = activeRows.map(function (rc) { return Number((rc.runs && rc.runs[st.baseIdx]) || 0); });
        var valsB = activeRows.map(function (rc) { return Number((rc.runs && rc.runs[st.targetIdx]) || 0); });

        codesChart.setOption({
          animationDuration: 180,
          tooltip: {
            trigger: "axis",
            axisPointer: { type: "shadow" },
            backgroundColor: pal.tipBg,
            borderColor: pal.tipBorder,
            borderWidth: 1,
            textStyle: { color: pal.text, fontSize: 12 },
            formatter: function (params) {
              if (!params || !params.length) return "";
              var idx = params[0].dataIndex;
              var d = CMP.calcDelta(valsA[idx], valsB[idx]);
              return (
                '<div style="font-weight:700;margin-bottom:4px">' + esc(cats[idx]) + "</div>" +
                params.map(function (p) {
                  var rawVal = p.seriesIndex === 0 ? valsA[idx] : valsB[idx];
                  return '<div style="display:flex;justify-content:space-between;gap:14px;font-variant-numeric:tabular-nums"><span>' + p.marker + " " + esc(p.seriesName) + "</span><strong>" + fmtInt(rawVal) + "</strong></div>";
                }).join("") +
                '<div style="margin-top:4px;padding-top:4px;border-top:1px solid ' + pal.split + ';font-variant-numeric:tabular-nums">Δ: <strong>' + esc(CMP.formatAbsDelta(d, fmtInt, "")) + " (" + esc(CMP.formatPct(d)) + ")</strong></div>"
              );
            }
          },
          legend: { bottom: 4, textStyle: { color: pal.muted, fontSize: 11 } },
          grid: { left: 155, right: 24, top: 14, bottom: 42 },
          xAxis: {
            type: "value",
            axisLabel: { color: pal.muted, fontSize: 11, formatter: fmtCompactCount },
            splitLine: { lineStyle: { color: pal.split } }
          },
          yAxis: {
            type: "category",
            data: cats,
            axisLine: { lineStyle: { color: pal.axis } },
            axisLabel: { color: pal.muted, fontSize: 11, width: 142, overflow: "truncate" }
          },
          series: [
            { name: "Baseline (A)", type: "bar", barMaxWidth: 14, barMinHeight: 2, data: valsA.map(function (v) { return v > 0 ? v : null; }), itemStyle: { color: pal.baseColor, borderRadius: [0, 3, 3, 0] } },
            { name: "Target (B)", type: "bar", barMaxWidth: 14, barMinHeight: 2, data: valsB.map(function (v) { return v > 0 ? v : null; }), itemStyle: { color: pal.targetColor, borderRadius: [0, 3, 3, 0] } }
          ]
        }, true);
        return;
      }

      if (codesMode === "errors" && hasHttpErrorRows) {
        var errSeries = errorCodeRows.filter(function (rc) {
          return Array.isArray(rc.runs) && rc.runs.some(function (v) { return Number(v) > 0; });
        }).map(function (rc, idx) {
          var clr = statusCodeColor(rc.code, idx, pal.dark);
          return {
            name: "HTTP " + rc.code + (rc.statusText ? " " + rc.statusText : ""),
            type: "line",
            smooth: 0.12,
            symbolSize: 6,
            data: runs.map(function (_, i) { return Number((rc.runs && rc.runs[i]) || 0); }),
            lineStyle: { width: 2, color: clr },
            itemStyle: { color: clr }
          };
        });
        codesChart.setOption({
          animationDuration: 180,
          tooltip: {
            trigger: "axis",
            backgroundColor: pal.tipBg,
            borderColor: pal.tipBorder,
            borderWidth: 1,
            textStyle: { color: pal.text, fontSize: 12 },
            formatter: function (params) {
              if (!params || !params.length) return "";
              var idx = params[0].dataIndex;
              var html = '<div style="font-weight:700;margin-bottom:4px">' + esc(labels[idx] || "") + "</div>";
              params.forEach(function (p) {
                html +=
                  '<div style="display:flex;justify-content:space-between;gap:14px;font-variant-numeric:tabular-nums">' +
                    "<span>" + p.marker + " " + esc(p.seriesName) + "</span>" +
                    "<strong>" + fmtInt(p.value) + "</strong>" +
                  "</div>";
              });
              return html;
            }
          },
          legend: { bottom: 4, type: "scroll", textStyle: { color: pal.muted, fontSize: 11 } },
          grid: { left: 56, right: 20, top: 20, bottom: 48 },
          xAxis: {
            type: "category",
            data: axisLabels,
            axisLine: { lineStyle: { color: pal.axis } },
            axisLabel: { color: pal.muted, fontSize: 11, overflow: "truncate", width: 110 }
          },
          yAxis: {
            type: "value",
            minInterval: 1,
            name: "Responses",
            nameTextStyle: { color: pal.muted, fontSize: 10 },
            axisLabel: { color: pal.muted, fontSize: 11, formatter: fmtCompactCount },
            splitLine: { lineStyle: { color: pal.split } }
          },
          series: errSeries
        }, true);
        return;
      }

      // Default: All Codes stacked bars across runs
      var allSeries = responseCodes.filter(function (rc) {
        return Array.isArray(rc.runs) && rc.runs.some(function (v) { return Number(v) > 0; });
      }).map(function (rc, idx) {
        var clr = statusCodeColor(rc.code, idx, pal.dark);
        return {
          name: "HTTP " + rc.code,
          type: "bar",
          stack: "codes",
          barMaxWidth: 34,
          data: runs.map(function (_, i) { return Number((rc.runs && rc.runs[i]) || 0); }),
          itemStyle: { color: clr }
        };
      });

      codesChart.setOption({
        animationDuration: 180,
        tooltip: {
          trigger: "axis",
          axisPointer: { type: "shadow" },
          backgroundColor: pal.tipBg,
          borderColor: pal.tipBorder,
          borderWidth: 1,
          textStyle: { color: pal.text, fontSize: 12 },
          formatter: function (params) {
            if (!params || !params.length) return "";
            var idx = params[0].dataIndex;
            var total = params.reduce(function (s, p) { return s + (Number(p.value) || 0); }, 0);
            var html = '<div style="font-weight:700;margin-bottom:4px">' + esc(labels[idx] || "") + "</div>";
            params.forEach(function (p) {
              var val = Number(p.value) || 0;
              var share = total > 0 ? " (" + fmtDec((val * 100) / total, 2) + "%)" : "";
              html +=
                '<div style="display:flex;justify-content:space-between;gap:14px;font-variant-numeric:tabular-nums">' +
                  "<span>" + p.marker + " " + esc(p.seriesName) + "</span>" +
                  "<strong>" + fmtInt(val) + esc(share) + "</strong>" +
                "</div>";
            });
            return html;
          }
        },
        legend: { bottom: 4, type: "scroll", textStyle: { color: pal.muted, fontSize: 11 } },
        grid: { left: 58, right: 20, top: 20, bottom: 48 },
        xAxis: {
          type: "category",
          data: axisLabels,
          axisLine: { lineStyle: { color: pal.axis } },
          axisLabel: { color: pal.muted, fontSize: 11, overflow: "truncate", width: 110 }
        },
        yAxis: {
          type: "value",
          name: "Hits",
          nameTextStyle: { color: pal.muted, fontSize: 10 },
          axisLabel: { color: pal.muted, fontSize: 11, formatter: fmtCompactCount },
          splitLine: { lineStyle: { color: pal.split } }
        },
        series: allSeries
      }, true);
    }

    function renderMixChart(st) {
      mixChart = ensureNetChart("chart-net-mix", mixChart);
      if (!mixChart || !net.available) return;
      var pal = netThemePalette();
      var labels = buildRunLabels(st);
      var axisLabels = buildCompactRunLabels(st);

      var rawRows = [];
      var labelFn;
      if (mixMode === "methods") {
        rawRows = Array.isArray(net.methods) ? net.methods : [];
        labelFn = function (r) { return r.method || "(unknown)"; };
      } else if (mixMode === "hosts") {
        rawRows = Array.isArray(net.hosts) ? net.hosts : [];
        labelFn = function (r) { return r.name || "(unknown)"; };
      } else {
        rawRows = Array.isArray(net.contentTypes) ? net.contentTypes : [];
        labelFn = function (r) { return r.contentType || "(unspecified)"; };
      }

      var ranked = rawRows.map(function (r) {
        var vals = runs.map(function (_, i) { return Number((r.runs && r.runs[i]) || 0); });
        var total = vals.reduce(function (s, v) { return s + v; }, 0);
        return { name: labelFn(r), vals: vals, total: total };
      }).filter(function (x) { return x.total > 0; })
        .sort(function (a, b) { return b.total - a.total; });

      var MAX_SERIES = 7;
      var top = ranked.slice(0, MAX_SERIES);
      if (ranked.length > MAX_SERIES) {
        var rest = ranked.slice(MAX_SERIES);
        var otherVals = runs.map(function (_, i) {
          return rest.reduce(function (s, x) { return s + x.vals[i]; }, 0);
        });
        top.push({ name: "Other (" + rest.length + ")", vals: otherVals, total: 1 });
      }

      var series = top.map(function (item, idx) {
        return {
          name: item.name,
          type: "bar",
          stack: "mix",
          barMaxWidth: 34,
          data: item.vals,
          itemStyle: { color: pal.mixPalette[idx % pal.mixPalette.length] }
        };
      });

      mixChart.setOption({
        animationDuration: 180,
        tooltip: {
          trigger: "axis",
          axisPointer: { type: "shadow" },
          backgroundColor: pal.tipBg,
          borderColor: pal.tipBorder,
          borderWidth: 1,
          textStyle: { color: pal.text, fontSize: 12 },
          formatter: function (params) {
            if (!params || !params.length) return "";
            var idx = params[0].dataIndex;
            var total = params.reduce(function (s, p) { return s + (Number(p.value) || 0); }, 0);
            var html = '<div style="font-weight:700;margin-bottom:4px">' + esc(labels[idx] || "") + "</div>";
            params.forEach(function (p) {
              var val = Number(p.value) || 0;
              var share = total > 0 ? " (" + fmtDec((val * 100) / total, 1) + "%)" : "";
              html +=
                '<div style="display:flex;justify-content:space-between;gap:14px;font-variant-numeric:tabular-nums">' +
                  "<span>" + p.marker + " " + esc(p.seriesName) + "</span>" +
                  "<strong>" + fmtInt(val) + esc(share) + "</strong>" +
                "</div>";
            });
            return html;
          }
        },
        legend: { bottom: 4, type: "scroll", textStyle: { color: pal.muted, fontSize: 11 } },
        grid: { left: 58, right: 20, top: 20, bottom: 48 },
        xAxis: {
          type: "category",
          data: axisLabels,
          axisLine: { lineStyle: { color: pal.axis } },
          axisLabel: { color: pal.muted, fontSize: 11, overflow: "truncate", width: 110 }
        },
        yAxis: {
          type: "value",
          name: "Hits",
          nameTextStyle: { color: pal.muted, fontSize: 10 },
          axisLabel: { color: pal.muted, fontSize: 11, formatter: fmtCompactCount },
          splitLine: { lineStyle: { color: pal.split } }
        },
        series: series
      }, true);
    }

    function renderAllCharts(st) {
      renderTrafficChart(st);
      var breakdownGrid = document.getElementById("net-breakdown-charts");
      if (breakdownGrid) breakdownGrid.hidden = !net.available;
      if (net.available) {
        renderResponseCodesChart(st);
        renderMixChart(st);
      }
    }

    function draw() {
      var st = CMP.getState();
      var baseRun = CMP.getBaseRun() || {};
      var targetRun = CMP.getTargetRun() || {};

      var kpis = document.getElementById("net-kpis");
      if (kpis) {
        var dRx = CMP.calcDelta(baseRun.bytesReceived, targetRun.bytesReceived);
        var dTx = CMP.calcDelta(baseRun.bytesSent, targetRun.bytesSent);
        var dHits = CMP.calcDelta(baseRun.hits, targetRun.hits);
        var durA = Number(baseRun.durationSec) || 0;
        var durB = Number(targetRun.durationSec) || 0;
        var rxRateSub = (durA > 0 && durB > 0 && dRx.valA !== null && dRx.valB !== null)
          ? (fmtBytes(dRx.valA / durA) + "/s → " + fmtBytes(dRx.valB / durB) + "/s")
          : "";
        var txRateSub = (durA > 0 && durB > 0 && dTx.valA !== null && dTx.valB !== null)
          ? (fmtBytes(dTx.valA / durA) + "/s → " + fmtBytes(dTx.valB / durB) + "/s")
          : "";
        var hitRateSub = (durA > 0 && durB > 0 && dHits.valA !== null && dHits.valB !== null)
          ? (fmtDec(dHits.valA / durA, 1) + " → " + fmtDec(dHits.valB / durB, 1) + " hits/s")
          : "";

        var fourthCard = "";
        if (net.available && responseCodes.length) {
          var errA = sumHttpErrorsForRun(st.baseIdx);
          var errB = sumHttpErrorsForRun(st.targetIdx);
          var dErr = CMP.calcDelta(errA, errB);
          var errPctA = (baseRun.hits > 0 && errA !== null) ? fmtDec((errA * 100) / baseRun.hits, 3) + "%" : "";
          var errPctB = (targetRun.hits > 0 && errB !== null) ? fmtDec((errB * 100) / targetRun.hits, 3) + "%" : "";
          var errSub = (errPctA && errPctB) ? (errPctA + " → " + errPctB + " of hits") : "4xx, 5xx & status 0 responses";
          fourthCard = renderNetKpi("HTTP Errors (4xx/5xx)", dErr, fmtInt, "lower", errSub, { deadband: 0, absPill: true });
        } else if (durA > 0 && durB > 0 && dRx.valA !== null && dRx.valB !== null) {
          var dBw = CMP.calcDelta(dRx.valA / durA, dRx.valB / durB);
          fourthCard = renderNetKpi("Avg Download Rate", dBw, function (v) { return fmtBytes(v) + "/s"; }, "neutral", "");
        }

        kpis.className = fourthCard ? "kpi-grid-4" : "kpi-grid-3";
        kpis.innerHTML =
          renderNetKpi("Bytes Received", dRx, fmtBytes, "neutral", rxRateSub) +
          renderNetKpi("Bytes Sent", dTx, fmtBytes, "neutral", txRateSub) +
          renderNetKpi("Total Hits", dHits, fmtInt, "higher", hitRateSub) +
          fourthCard;
      }

      renderAllCharts(st);

      var tablesBox = document.getElementById("net-tables-wrap");
      var emptyBox = document.getElementById("net-empty");
      if (!net.available) {
        if (tablesBox) tablesBox.hidden = true;
        renderUnavailableNotice("net-empty", "HTTP Breakdown Tables Unavailable",
          "Response codes, methods, content types, and host counts are available when generated from source testreport.xml directories.");
        return;
      }
      if (tablesBox) tablesBox.hidden = false;
      if (emptyBox) emptyBox.hidden = true;

      renderNetTable("net-codes-table", net.responseCodes || [], function (r) {
        return "HTTP " + r.code + (r.statusText ? " — " + r.statusText : "");
      }, st);
      renderNetTable("net-methods-table", net.methods || [], function (r) { return r.method; }, st);
      renderNetTable("net-types-table", net.contentTypes || [], function (r) { return r.contentType; }, st);
      renderNetTable("net-hosts-table", net.hosts || [], function (r) { return r.name; }, st);
      if (CMP.syncTablesAlignment) {
        CMP.syncTablesAlignment([
          document.getElementById("net-codes-table"),
          document.getElementById("net-methods-table"),
          document.getElementById("net-types-table"),
          document.getElementById("net-hosts-table")
        ]);
      }
    }

    function renderNetKpi(label, delta, fmtFn, pol, subText, opts) {
      opts = opts || {};
      var pc = CMP.polarityClass(delta, pol, opts.deadband !== undefined ? opts.deadband : 1.0);
      var pillTxt = opts.absPill ? CMP.formatAbsDelta(delta, fmtFn, "") : CMP.formatPct(delta);
      return (
        '<article class="kpi-card">' +
          '<div class="cmp-kpi-head"><span class="kpi-label">' + esc(label) + '</span><span class="cmp-delta-pill ' + pc + '">' + esc(pillTxt) + "</span></div>" +
          '<div class="cmp-kpi-pair"><span class="cmp-kpi-curr">' + (delta.valB !== null ? esc(fmtFn(delta.valB)) : "—") + '</span><span class="cmp-kpi-prev">from ' + (delta.valA !== null ? esc(fmtFn(delta.valA)) : "—") + "</span></div>" +
          (subText ? '<div class="kpi-sub">' + esc(subText) + "</div>" : "") +
        "</article>"
      );
    }

    function renderNetTable(tableId, rows, labelFn, st) {
      clearTableSortState(tableId);
      var tbody = document.querySelector("#" + tableId + " tbody");
      if (!tbody) return;
      var active = rows.filter(function (r) {
        var cA = Number((r.runs && r.runs[st.baseIdx]) || 0);
        var cB = Number((r.runs && r.runs[st.targetIdx]) || 0);
        return cA > 0 || cB > 0;
      });
      if (!active.length) {
        tbody.innerHTML = '<tr><td colspan="3" class="cell-muted" style="text-align:center">No data</td></tr>';
        return;
      }
      tbody.innerHTML = active.map(function (r, idx) {
        var cA = Number((r.runs && r.runs[st.baseIdx]) || 0);
        var cB = Number((r.runs && r.runs[st.targetIdx]) || 0);
        var d = CMP.calcDelta(cA, cB);
        var cd = CMP.buildComparisonCellData(d, fmtInt, "", "neutral", st.viewMode, {});
        var lbl = labelFn(r);
        return (
          "<tr>" +
            '<td class="col-idx">' + (idx + 1) + "</td>" +
            '<td class="col-text" data-copy="' + esc(lbl) + '">' + esc(lbl) + "</td>" +
            '<td class="col-num" data-val="' + cB + '" title="' + esc(cd.title) + '" data-copy="' + esc(cd.copyText) + '">' + cd.html + "</td>" +
          "</tr>"
        );
      }).join("");
    }

    initTabVisibility();
    wireTablist("net-traffic-mode-tabs", "data-net-mode", function (mode) {
      trafficMode = mode || "rate";
      renderTrafficChart(CMP.getState());
    });
    wireTablist("net-codes-mode-tabs", "data-codes-mode", function (mode) {
      codesMode = mode || "all";
      renderResponseCodesChart(CMP.getState());
    });
    wireTablist("net-mix-mode-tabs", "data-mix-mode", function (mode) {
      mixMode = mode || "contentTypes";
      renderMixChart(CMP.getState());
    });

    draw();
    window.addEventListener("xlt-cmp-change", draw);
    window.addEventListener("xlt-theme-changed", function () {
      renderAllCharts(CMP.getState());
    });
  }

  // =========================================================================
  // 5. Configuration & Load Profile Diff
  // =========================================================================

  function initConfiguration() {
    var cfg = window.XLT_CONFIG || { available: false };
    var wrap = document.getElementById("cfg-content");
    if (!cfg.available) {
      if (wrap) wrap.hidden = true;
      renderUnavailableNotice("cfg-empty", "Configuration & Load Profile Unavailable",
        "This report does not contain stored load profile or property snapshots (for example, legacy trendreport.xml files).");
      return;
    }

    var diffOnly = true;
    var q = "";
    var diffBtn = document.getElementById("cfg-diff-only-btn");
    if (diffBtn) {
      diffBtn.setAttribute("aria-pressed", "true");
      diffBtn.addEventListener("click", function () {
        diffOnly = !diffOnly;
        diffBtn.setAttribute("aria-pressed", diffOnly ? "true" : "false");
        draw();
      });
      if (CMP.installToolbarFocusgroupFallback && diffBtn.parentElement) {
        CMP.installToolbarFocusgroupFallback(diffBtn.parentElement);
      }
    }
    var searchEl = document.getElementById("dir-filter-input");
    if (searchEl) searchEl.addEventListener("input", function () { q = searchEl.value || ""; draw(); });

    function fmtValPair(a, b) {
      var sa = a == null ? "—" : String(a);
      var sb = b == null ? "—" : String(b);
      if (sa === sb) return esc(sb);
      return '<span class="cmp-cell-base">' + esc(sa) + '</span> <span class="cmp-cell-arrow">→</span> <strong class="cell-warn">' + esc(sb) + "</strong>";
    }

    function draw() {
      clearTableSortState("cfg-lp-table");
      clearTableSortState("cfg-props-table");
      var st = CMP.getState();

      // Load Profile table
      var lpBody = document.querySelector("#cfg-lp-table tbody");
      var profiles = cfg.loadProfiles || [];
      if (lpBody) {
        if (!profiles.length) {
          lpBody.innerHTML = '<tr><td colspan="7" class="cell-muted" style="text-align:center">No load profile recorded</td></tr>';
        } else {
          lpBody.innerHTML = profiles.map(function (lp) {
            var a = lp.runs && lp.runs[st.baseIdx];
            var b = lp.runs && lp.runs[st.targetIdx];
            if (!a && !b) return "";
            var tag = !a ? '<span class="cmp-delta-pill delta-added">Added</span>'
              : (!b ? '<span class="cmp-delta-pill delta-removed">Removed</span>'
                : (JSON.stringify(a) !== JSON.stringify(b)
                  ? '<span class="cmp-delta-pill delta-warn">Changed</span>'
                  : '<span class="cmp-delta-pill delta-flat">Same</span>'));
            return (
              "<tr>" +
                '<td class="col-text"><strong>' + esc(lp.name) + '</strong><div class="name-cell-sub">' + esc(lp.className) + "</div></td>" +
                '<td class="col-num">' + fmtValPair(a && a.users, b && b.users) + "</td>" +
                '<td class="col-num">' + fmtValPair(a && a.arrivalRate, b && b.arrivalRate) + "</td>" +
                '<td class="col-num">' + fmtValPair(a ? fmtDurationHms(a.rampUp) : null, b ? fmtDurationHms(b.rampUp) : null) + "</td>" +
                '<td class="col-num">' + fmtValPair(a ? fmtDurationHms(a.measurement) : null, b ? fmtDurationHms(b.measurement) : null) + "</td>" +
                '<td class="col-num">' + fmtValPair(a ? fmtThinkTime(a.thinkTime, a.thinkTimeDev) : null, b ? fmtThinkTime(b.thinkTime, b.thinkTimeDev) : null) + "</td>" +
                '<td class="col-cat">' + tag + "</td>" +
              "</tr>"
            );
          }).join("");
        }
      }

      // Properties table
      var props = cfg.properties || [];
      var propBody = document.querySelector("#cfg-props-table tbody");
      var propCount = document.getElementById("cfg-prop-count");
      if (!propBody) return;

      var parsed = q.trim() && UI.parseSearchQuery ? UI.parseSearchQuery(q) : null;
      var diffTotal = 0;
      var filtered = [];

      props.forEach(function (p) {
        var vA = p.runs ? p.runs[st.baseIdx] : null;
        var vB = p.runs ? p.runs[st.targetIdx] : null;
        if (vA == null && vB == null) return;
        var status = vA == null ? "added" : (vB == null ? "removed" : (vA !== vB ? "changed" : "same"));
        if (status !== "same") diffTotal++;
        if (diffOnly && status === "same") return;
        if (parsed && UI.matchesParsedSearchQuery) {
          if (!UI.matchesParsedSearchQuery([p.key, vA || "", vB || "", status].join(" "), parsed, {})) return;
        }
        filtered.push({ key: p.key, vA: vA, vB: vB, status: status });
      });

      if (propCount) propCount.textContent = fmtInt(filtered.length) + " shown · " + fmtInt(diffTotal) + " differing";

      if (!filtered.length) {
        propBody.innerHTML = '<tr><td colspan="4" class="cmp-empty-panel">' +
          (diffTotal === 0 ? "All test properties match identically between Baseline (A) and Target (B)." : "No properties match the active filter.") +
          "</td></tr>";
        return;
      }

      propBody.innerHTML = filtered.map(function (r) {
        var pCls = r.status === "changed" ? "delta-warn" : (r.status === "added" ? "delta-added" : (r.status === "removed" ? "delta-removed" : "delta-flat"));
        return (
          '<tr class="cmp-prop-row' + (r.status !== "same" ? " is-diff" : "") + '">' +
            '<td class="col-text" style="font-family:var(--font-mono);font-size:11.5px">' + esc(r.key) + "</td>" +
            '<td class="col-text" style="white-space:pre-wrap;word-break:break-word;font-family:var(--font-mono);font-size:11px">' + (r.vA != null ? esc(r.vA) : '<span class="cell-muted">—</span>') + "</td>" +
            '<td class="col-text" style="white-space:pre-wrap;word-break:break-word;font-family:var(--font-mono);font-size:11px">' + (r.vB != null ? esc(r.vB) : '<span class="cell-muted">—</span>') + "</td>" +
            '<td class="col-cat"><span class="cmp-delta-pill ' + pCls + '">' + esc(r.status.toUpperCase()) + "</span></td>" +
          "</tr>"
        );
      }).join("");
    }

    draw();
    window.addEventListener("xlt-cmp-change", draw);
  }

  window.XltComparisonPages = {
    init: function (page) {
      document.addEventListener("DOMContentLoaded", function () {
        if (page === "web-vitals") initWebVitals();
        else if (page === "errors") initErrors();
        else if (page === "events") initEvents();
        else if (page === "network") initNetwork();
        else if (page === "configuration") initConfiguration();
      });
    }
  };
})();
