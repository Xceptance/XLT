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
  // 4. Network Comparison
  // =========================================================================

  function initNetwork() {
    var net = window.XLT_NETWORK || { available: false };
    var toolbar = document.getElementById("cmp-table-controls");
    wireViewToggleGroup(toolbar);

    function draw() {
      var st = CMP.getState();
      var baseRun = CMP.getBaseRun() || {};
      var targetRun = CMP.getTargetRun() || {};

      var kpis = document.getElementById("net-kpis");
      if (kpis) {
        var dRx = CMP.calcDelta(baseRun.bytesReceived, targetRun.bytesReceived);
        var dTx = CMP.calcDelta(baseRun.bytesSent, targetRun.bytesSent);
        var dHits = CMP.calcDelta(baseRun.hits, targetRun.hits);
        kpis.innerHTML =
          renderNetKpi("Bytes Received", dRx, fmtBytes, "neutral") +
          renderNetKpi("Bytes Sent", dTx, fmtBytes, "neutral") +
          renderNetKpi("Total Hits", dHits, fmtInt, "higher");
      }

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

    function renderNetKpi(label, delta, fmtFn, pol) {
      var pc = CMP.polarityClass(delta, pol, 1.0);
      return (
        '<article class="kpi-card">' +
          '<div class="cmp-kpi-head"><span class="kpi-label">' + esc(label) + '</span><span class="cmp-delta-pill ' + pc + '">' + esc(CMP.formatPct(delta)) + "</span></div>" +
          '<div class="cmp-kpi-pair"><span class="cmp-kpi-curr">' + (delta.valB !== null ? esc(fmtFn(delta.valB)) : "—") + '</span><span class="cmp-kpi-prev">from ' + (delta.valA !== null ? esc(fmtFn(delta.valA)) : "—") + "</span></div>" +
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

    draw();
    window.addEventListener("xlt-cmp-change", draw);
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
