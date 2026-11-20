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
 * XLT Modern Load Report — Overview Dashboard Renderer (`index.html`)
 *
 * @fileoverview
 * Hydrates the executive landing dashboard from `window.XLT_OVERVIEW` and `window.XLT_META`:
 *   1. Scorecard Quality Gate Verdict Banner (`#sec-verdict`) when `scorecard.present` is true.
 *   2. Test Execution Window & Comment (`#sec-comment`), rendering `<comments><string>` entries
 *      directly inside `.comment-body > .paragraph` (preserving plain text, raw HTML / XTC
 *      `.xtc_comment` / `.xtc-comment` blocks, and `<div class="markdown">` output generated
 *      from `::markdown::` comments by `ConfigurationReportProvider.processComment`).
 *   3. 4-Card Executive KPIs (`#sec-kpis`) — Completed Transactions, Request Latency, Reliability, Data.
 *   4. 2x2 Static Telemetry Charts (`#sec-charts`) — Concurrent Users, Requests/s, Latency, Errors.
 *   5. 3-Column Hotspot Triage (`#sec-hotspots`) — Top Transactions, Root Cause Diagnostics, Agents.
 *   6. Searchable Scenario Load Profile (`#load-profile-table`) with proportional share bars.
 *   7. Cross-Tier Performance Summary Matrix (`#perf-summary-table`) with dynamic percentile columns.
 *
 * Consumes:
 * - `window.XLT_OVERVIEW` (`general`, `comments`, `summary`, `topTransactions`, `diagnostics`,
 *   `loadProfile`, `loadProfileTotals`, `scorecard`)
 * - `window.XLT_META` (`startTime`, `endTime`, `durationDisplay`, `loadTestName`, `chartsGenerated`,
 *   `testReportConfig`, `scorecard`, `counts`)
 * - `window.XLT_UI` (`esc`, `fmtInt`, `fmtDec`, `fmtBytes`, `fmtDurationHms`, `fmtThinkTime`, `getPercentile`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var fmtBytes = UI.fmtBytes;
  var fmtDurationHms = UI.fmtDurationHms;
  var fmtThinkTime = UI.fmtThinkTime;

  var COPY_ICON =
    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" ' +
    'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
    '<rect x="9" y="9" width="13" height="13" rx="2"/>' +
    '<path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>' +
    "</svg>";

  /**
   * Formats overview dashboard decimals with 1 fractional digit by default
   * (preserving overview KPI, latency, and summary matrix precision).
   */
  function fmtDec(n, digits) {
    return UI.fmtDec(n, digits === undefined ? 1 : digits);
  }

  /**
   * Returns `null` (not `undefined`) when a percentile is absent so callers using
   * strict `!== null` checks suppress missing P95 subtitles cleanly.
   */
  function getPct(summaryItem, pctStr) {
    var val = UI.getPercentile(summaryItem, pctStr);
    return val === undefined ? null : val;
  }

  /** Renders the automated Scorecard quality-gate verdict banner (#sec-verdict) when enabled. */
  function renderVerdict(ov, meta) {
    var sec = document.getElementById("sec-verdict");
    if (!sec) return;
    var sc = ov.scorecard || (meta && meta.scorecard);
    if (!sc || !sc.present) {
      sec.hidden = true;
      return;
    }
    sec.hidden = false;
    var res = String(sc.result || "PASSED").toUpperCase();
    var isFail = res === "FAILED" || res === "ERROR";
    sec.className = "scorecard-banner" + (isFail ? " is-failed" : "");

    var failedRules = Array.isArray(sc.failedRules) ? sc.failedRules : [];
    var chipsHtml = failedRules.slice(0, 6).map(function (r) {
      var pillStyle = r.failsTest
        ? "color:var(--status-fail-text);border-color:var(--status-fail-border);background:var(--status-fail-bg);"
        : "color:var(--status-warn-text);border-color:var(--status-warn-border);background:var(--status-warn-bg);";
      return (
        '<a href="scorecard.html#rule-' + esc(r.id) + '" class="miss-chip" style="' + pillStyle + '">' +
          (r.failsTest ? "✕ " : "! ") + esc(r.name || r.id) +
          (r.value !== undefined && r.value !== "" ? ": <strong>" + esc(r.value) + "</strong>" : "") +
          (r.condition ? " (" + esc(r.condition) + ")" : "") +
        "</a>"
      );
    }).join("");

    var titleText;
    if (res === "ERROR") {
      titleText = "Scorecard Evaluation Encountered an Error";
    } else if (isFail) {
      titleText = failedRules.length
        ? failedRules.length + " Scorecard Quality Gate Rule(s) Failed Thresholds"
        : "Scorecard Quality Gate Thresholds Breached";
    } else if (failedRules.length > 0) {
      titleText = "Quality Gates Passed (" + failedRules.length + " Non-Fatal Advisory Check(s) Missed)";
    } else {
      titleText = sc.ratingDescription || "All Scorecard Quality Gates Passed";
    }

    var tx = (ov.summary && ov.summary.transactions) || {};
    var req = (ov.summary && ov.summary.requests) || {};
    var repPctKey = UI.pickRepresentativePercentile(UI.getConfiguredPercentiles([req]));
    var repPctVal = repPctKey ? getPct(req, repPctKey) : null;
    var descHtml = sc.errorMessage
      ? esc(sc.errorMessage)
      : "Automated evaluation scored <strong>" + fmtInt(sc.points) + " out of " + fmtInt(sc.maxPoints) +
        " points</strong> (<strong>" + fmtDec(sc.pointsPercentage, 1) + "%</strong>)" +
        (sc.ratingName ? " — Rating: <strong>" + esc(sc.ratingName) + "</strong>" : "") + ". " +
        "Throughput reached <strong>" + fmtInt(tx.count || 0) + " transactions</strong> with request latency at " +
        "<strong>Mean: " + fmtInt(req.mean || 0) + " ms</strong>" +
        (repPctVal !== null ? ", <strong>P" + esc(repPctKey) + ": " + fmtInt(repPctVal) + " ms</strong>" : "") + ".";

    sec.innerHTML =
      '<div class="sc-left">' +
        '<div class="sc-badge' + (isFail ? " fail" : "") + '">' +
          "<span>" + (isFail ? "✕" : "✓") + "</span> " + esc(res) + " QUALITY GATES" +
        "</div>" +
        '<div class="sc-title">' + esc(titleText) + "</div>" +
        '<div class="sc-desc">' + descHtml + "</div>" +
        (chipsHtml
          ? '<div class="verdict-misses" style="display:flex;gap:8px;flex-wrap:wrap;margin-top:6px;">' + chipsHtml + "</div>"
          : "") +
      "</div>" +
      '<div class="sc-score-box' + (isFail ? " fail" : "") + '">' +
        '<div class="sc-score-num">' + fmtDec(sc.pointsPercentage || 0, 1) + "%</div>" +
        '<div class="sc-score-label">' + fmtInt(sc.points || 0) + " / " + fmtInt(sc.maxPoints || 0) + " PTS • " + esc(res) + "</div>" +
        '<a href="scorecard.html" class="btn-chip" style="margin-top:8px;display:inline-block;padding:3px 10px;font-size:11px;font-weight:700;">View Scorecard →</a>' +
      "</div>";
  }

  /**
   * Renders the test execution time window card (#sec-comment) and comment paragraphs.
   * Each `<comments><string>` entry (plain text, raw HTML / XTC comment snippet, or Flexmark
   * `<div class="markdown">` output) is wrapped in `<div class="paragraph">` inside `.comment-body`,
   * matching classic XSLT `sections/comment.xsl` semantics.
   */
  function renderComment(ov, meta) {
    var sec = document.getElementById("sec-comment");
    if (!sec) return;
    var rawComments = ov.comments || (meta && meta.comments) || [];
    var commentItems = Array.isArray(rawComments)
      ? rawComments.map(function (c) { return String(c == null ? "" : c).trim(); }).filter(Boolean)
      : [];

    if (!commentItems.length) {
      sec.hidden = true;
      sec.innerHTML = "";
      return;
    }

    sec.hidden = false;
    sec.innerHTML =
      '<div class="comment-card">' +
        '<div class="comment-header">' +
          '<div class="comment-title-group">' +
            '<span class="comment-badge">Test Comment</span>' +
          "</div>" +
        "</div>" +
        '<div class="comment-body">' +
          commentItems.map(function (c) {
            return '<div class="paragraph">' + c + "</div>";
          }).join("") +
        "</div>" +
      "</div>";
  }

  /** Renders the 4 top-level KPI summary cells (#sec-kpis). */
  function renderKpis(ov) {
    var sec = document.getElementById("sec-kpis");
    if (!sec) return;
    var gen = ov.general || {};
    var tx = (ov.summary && ov.summary.transactions) || {};
    var req = (ov.summary && ov.summary.requests) || {};
    var repPctKey = UI.pickRepresentativePercentile(UI.getConfiguredPercentiles([req]));
    var repPctVal = repPctKey ? getPct(req, repPctKey) : null;
    var errCount = Number(tx.errors) || 0;
    var errPct = Number(tx.errPct || 0);
    var okPct = Math.max(0, 100 - errPct);
    var totalBytes = (Number(gen.bytesSent) || 0) + (Number(gen.bytesReceived) || 0);
    var durSec = Math.max(1, Number(gen.durationSec) || 1);
    var bwPerSec = totalBytes / durSec;

    sec.innerHTML =
      '<div class="kpi-card">' +
        '<span class="kpi-label">Completed Transactions</span>' +
        '<div class="kpi-val">' + fmtInt(tx.count || 0) + " tx</div>" +
        '<div class="kpi-sub">' + fmtInt(tx.cph || 0) + " /h • " + fmtDec(tx.rate || 0, 2) + " tx/s throughput</div>" +
      "</div>" +
      '<div class="kpi-card">' +
        '<span class="kpi-label">Request Runtime</span>' +
        '<div class="kpi-val">' + fmtInt(req.mean || 0) + " ms</div>" +
        '<div class="kpi-sub">Mean: ' + fmtInt(req.mean || 0) + " ms" +
          (repPctVal !== null ? " • P" + esc(repPctKey) + ": " + fmtInt(repPctVal) + " ms" : "") +
          (tx.mean ? " (Tx mean: " + fmtInt(tx.mean) + " ms)" : "") +
        "</div>" +
      "</div>" +
      '<div class="kpi-card">' +
        '<span class="kpi-label">Errors</span>' +
        '<div class="kpi-val"' + (errCount > 0 ? ' style="color:var(--status-fail-text);"' : "") + ">" +
          fmtInt(errCount) +
        "</div>" +
        '<div class="kpi-sub">' + fmtDec(errPct, 3) + "% error rate • " + fmtDec(okPct, 3) + "% OK across " + fmtInt(tx.count || 0) + " tx</div>" +
      "</div>" +
      '<div class="kpi-card">' +
        '<span class="kpi-label">Data Transferred</span>' +
        '<div class="kpi-val">' + fmtBytes(totalBytes) + "</div>" +
        '<div class="kpi-sub">' + fmtBytes(bwPerSec) + "/s bandwidth • " + fmtInt(gen.hits || req.count || 0) + " requests</div>" +
      "</div>";
  }

  /** Renders the 2x2 overview time-series WebP chart grid (#sec-charts). */
  function renderCharts(ov, meta) {
    var sec = document.getElementById("sec-charts");
    if (!sec) return;
    if (meta && meta.chartsGenerated === false) {
      sec.innerHTML =
        '<div class="card" style="grid-column:1 / -1;">' +
          '<div class="card-body" style="text-align:center;padding:28px;color:var(--text-muted);">' +
            "<strong>Time-series charts were not generated for this report (<code>-noCharts</code>).</strong>" +
          "</div>" +
        "</div>";
      return;
    }
    var tx = (ov.summary && ov.summary.transactions) || {};
    var req = (ov.summary && ov.summary.requests) || {};
    var repPctKey = UI.pickRepresentativePercentile(UI.getConfiguredPercentiles([req]));
    var repPctVal = repPctKey ? getPct(req, repPctKey) : null;
    var lpTotals = ov.loadProfileTotals || {};

    sec.innerHTML =
      '<div class="chart-panel">' +
        '<div class="chart-panel-header">' +
          '<div class="chart-panel-title">Concurrent Users</div>' +
          '<span class="chart-header-badge">' +
            (lpTotals.usersDisplay ? "Target: " + esc(lpTotals.usersDisplay) + " Users" : "Active User Pool") +
          "</span>" +
        "</div>" +
        '<div class="chart-panel-body">' +
          '<img class="zoomable-chart" src="charts/ConcurrentUsers.webp" alt="Concurrent Users Progression" data-chart-title="Concurrent Users Progression" loading="lazy" decoding="async" />' +
        "</div>" +
      "</div>" +
      '<div class="chart-panel">' +
        '<div class="chart-panel-header">' +
          '<div class="chart-panel-title">Requests per Second</div>' +
          '<span class="chart-header-badge">Avg: ' + fmtDec(req.rate || 0, 1) + " req/s</span>" +
        "</div>" +
        '<div class="chart-panel-body">' +
          '<img class="zoomable-chart" src="charts/RequestsPerSecond.webp" alt="Requests per Second Progression" data-chart-title="Requests per Second Progression" loading="lazy" decoding="async" />' +
        "</div>" +
      "</div>" +
      '<div class="chart-panel">' +
        '<div class="chart-panel-header">' +
          '<div class="chart-panel-title">Request Runtime</div>' +
          '<span class="chart-header-badge">Mean: ' + fmtInt(req.mean || 0) + " ms" + (repPctVal !== null ? " • P" + esc(repPctKey) + ": " + fmtInt(repPctVal) + " ms" : "") + "</span>" +
        "</div>" +
        '<div class="chart-panel-body">' +
          '<img class="zoomable-chart" src="charts/RequestRuntime.webp" alt="Request Runtime Progression" data-chart-title="Request Runtime Progression" loading="lazy" decoding="async" />' +
        "</div>" +
      "</div>" +
      '<div class="chart-panel">' +
        '<div class="chart-panel-header">' +
          '<div class="chart-panel-title">Transaction Errors</div>' +
          '<span class="chart-header-badge"' + ((tx.errors || 0) > 0 ? ' style="color:var(--status-fail-text);"' : "") + ">" +
            fmtInt(tx.errors || 0) + " Errors (" + fmtDec(tx.errPct || 0, 3) + "%)" +
          "</span>" +
        "</div>" +
        '<div class="chart-panel-body">' +
          '<img class="zoomable-chart" src="charts/TransactionErrors.webp" alt="Transaction Errors Progression" data-chart-title="Transaction Errors Progression" loading="lazy" decoding="async" />' +
        "</div>" +
      "</div>";
  }

  /** Renders the 3-card hotspot triage grid: top transactions, diagnostics, and agent health. */
  function renderHotspots(ov, meta) {
    var sec = document.getElementById("sec-hotspots");
    if (!sec) return;
    var counts = (meta && meta.counts) || {};
    var topTx = Array.isArray(ov.topTransactions) ? ov.topTransactions : [];
    var diags = Array.isArray(ov.diagnostics) ? ov.diagnostics : [];
    var ag = (ov.summary && ov.summary.agents) || {};

    var txRows = topTx.length
      ? topTx.slice(0, 5).map(function (t) {
          return (
            "<tr>" +
              '<td class="hotspot-item-name"><a href="transactions.html?item=' + encodeURIComponent(t.name) + '">' + esc(t.name) + "</a></td>" +
              '<td class="cell-num">' + fmtInt(t.cph || 0) + " /h</td>" +
            "</tr>"
          );
        }).join("")
      : '<tr><td colspan="2" class="text-muted" style="text-align:center;padding:16px;">No transaction records</td></tr>';

    var diagRows = diags.length
      ? diags.slice(0, 5).map(function (d) {
          var clr = d.severity === "error"
            ? "color:var(--status-fail-text);"
            : d.severity === "warn"
              ? "color:var(--status-warn-text);"
              : "color:var(--text-muted);";
          return (
            "<tr>" +
              '<td class="hotspot-item-name" title="' + esc(d.label) + '">' + esc(d.label) + "</td>" +
              '<td class="cell-num" style="' + clr + '">' + esc(d.detail || fmtInt(d.count)) + "</td>" +
            "</tr>"
          );
        }).join("")
      : '<tr><td colspan="2" style="color:var(--success-text);font-weight:700;padding:16px;text-align:center;">✓ Zero transaction or HTTP errors recorded</td></tr>';

    var agentCount = ag.count !== undefined ? ag.count : (counts.agents || 0);
    var agentRows = agentCount > 0
      ? '<tr><td class="hotspot-item-name">Cluster Mean CPU</td><td class="cell-num">' + fmtDec(ag.totalCpuMean || 0, 2) + "%</td></tr>" +
        '<tr><td class="hotspot-item-name">Max Agent Peak CPU</td><td class="cell-num">' + fmtDec(ag.totalCpuMax || 0, 2) + "%</td></tr>" +
        '<tr><td class="hotspot-item-name">Minor GC CPU Overhead</td><td class="cell-num">' + fmtDec(ag.minorGcCpu || 0, 2) + "%</td></tr>" +
        '<tr><td class="hotspot-item-name">Full GC Pauses</td><td class="cell-num" style="color:' + ((ag.fullGcCount || 0) > 0 ? "var(--status-fail-text)" : "var(--success-text)") + ';">' +
          fmtInt(ag.fullGcCount || 0) + ((ag.fullGcCount || 0) === 0 ? " (Clean Heap)" : "") +
        "</td></tr>"
      : '<tr><td colspan="2" class="text-muted" style="text-align:center;padding:16px;">No agent telemetry recorded</td></tr>';

    sec.innerHTML =
      '<div class="hotspot-card">' +
        '<div class="hotspot-header">' +
          '<div class="hotspot-title">Top Transactions</div>' +
          '<button type="button" class="btn-copy-markdown btn-icon" onclick="copyTableAsMarkdown(this.closest(\'.hotspot-card\').querySelector(\'table\'), this)" aria-label="Copy Top Transactions table as Markdown" title="Copy Top Transactions table as Markdown">' + COPY_ICON + "</button>" +
        "</div>" +
        '<table class="hotspot-table"><thead><tr><th>Transaction</th><th class="cell-num">Throughput</th></tr></thead><tbody>' + txRows + "</tbody></table>" +
        '<div class="hotspot-footer-btn"><a href="transactions.html">All ' + fmtInt(counts.transactions || topTx.length) + " Transactions →</a></div>" +
      "</div>" +
      '<div class="hotspot-card">' +
        '<div class="hotspot-header">' +
          '<div class="hotspot-title">Top Errors</div>' +
          '<button type="button" class="btn-copy-markdown btn-icon" onclick="copyTableAsMarkdown(this.closest(\'.hotspot-card\').querySelector(\'table\'), this)" aria-label="Copy Top Errors table as Markdown" title="Copy Top Errors table as Markdown">' + COPY_ICON + "</button>" +
        "</div>" +
        '<table class="hotspot-table"><thead><tr><th>Error Message</th><th class="cell-num">Count</th></tr></thead><tbody>' + diagRows + "</tbody></table>" +
        '<div class="hotspot-footer-btn"><a href="errors.html">View Error Details →</a></div>' +
      "</div>" +
      '<div class="hotspot-card">' +
        '<div class="hotspot-header">' +
          '<div class="hotspot-title">Agents</div>' +
          '<button type="button" class="btn-copy-markdown btn-icon" onclick="copyTableAsMarkdown(this.closest(\'.hotspot-card\').querySelector(\'table\'), this)" aria-label="Copy Agents table as Markdown" title="Copy Agents table as Markdown">' + COPY_ICON + "</button>" +
        "</div>" +
        '<table class="hotspot-table"><thead><tr><th>Metric</th><th class="cell-num">Value</th></tr></thead><tbody>' + agentRows + "</tbody></table>" +
        '<div class="hotspot-footer-btn"><a href="agents.html">Inspect Agents →</a></div>' +
      "</div>";
  }

  /** Renders the interactive scenario load profile table (#load-profile-table) with share bars. */
  function renderLoadProfile(ov) {
    var tbody = document.querySelector("#load-profile-table tbody");
    var tfoot = document.querySelector("#load-profile-table tfoot");
    var subtitleEl = document.getElementById("profile-match-count");
    var searchInput = document.getElementById("profile-search");
    if (!tbody || !tfoot) return;

    var items = Array.isArray(ov.loadProfile) ? ov.loadProfile : [];
    var totals = ov.loadProfileTotals || {};

    var defaultSummary = items.length
      ? fmtInt(items.length) + " test scenario(s) configured" +
        (totals.usersDisplay ? " • Target: " + totals.usersDisplay + " concurrent users" : "") +
        (totals.arrivalDisplay && totals.arrivalDisplay !== "0" ? " • " + totals.arrivalDisplay + " /h target arrival rate" : "")
      : "No load profile configured in testreport.xml";

    if (subtitleEl) subtitleEl.textContent = defaultSummary;

    if (!items.length) {
      tbody.innerHTML = '<tr><td colspan="9" class="col-text" style="text-align:center;padding:24px;color:var(--text-muted);">No load profile entries configured for this run.</td></tr>';
      tfoot.innerHTML = "";
      return;
    }

    // Scale relative progress bars against the largest scenario share (guarded >= 1 to avoid 0/0)
    var maxUserPct = Math.max.apply(null, items.map(function (i) { return Number(i.usersPct) || 0; }).concat([1]));
    var maxArrPct = Math.max.apply(null, items.map(function (i) { return Number(i.arrivalPct) || 0; }).concat([1]));

    tbody.innerHTML = items.map(function (tc, idx) {
      var uPct = Number(tc.usersPct) || 0;
      var aPct = Number(tc.arrivalPct) || 0;
      var uBarW = Math.min(100, (uPct / maxUserPct) * 100).toFixed(1);
      var aBarW = Math.min(100, (aPct / maxArrPct) * 100).toFixed(1);
      var name = tc.name || tc.className || "Scenario";
      var arrDisp = tc.arrivalRate && tc.arrivalRate !== "0" ? tc.arrivalRate : "0";
      return (
        "<tr>" +
          '<td class="col-idx">' + (idx + 1) + "</td>" +
          '<td class="col-text scenario-name"><a href="transactions.html?item=' + encodeURIComponent(name) + '">' + esc(name) + "</a></td>" +
          '<td class="col-num cell-primary-num" data-val="' + (Number(tc.users) || 0) + '">' + esc(tc.users || "0") + "</td>" +
          '<td class="col-num" data-val="' + uPct.toFixed(4) + '"><span class="pct-bar-container"><span class="pct-bar-track"><span class="pct-bar" style="width:' + uBarW + '%"></span></span><span class="pct-text">' + fmtDec(uPct, 2) + "%</span></span></td>" +
          '<td class="col-num cell-primary-num" data-val="' + (Number(arrDisp) || 0) + '">' + esc(arrDisp) + "</td>" +
          '<td class="col-num" data-val="' + aPct.toFixed(4) + '"><span class="pct-bar-container"><span class="pct-bar-track"><span class="pct-bar bar-purple" style="width:' + aBarW + '%"></span></span><span class="pct-text">' + fmtDec(aPct, 2) + "%</span></span></td>" +
          '<td class="col-num cell-secondary-num" data-val="' + (Number(tc.rampUp) || 0) + '">' + esc(fmtDurationHms(tc.rampUp || 0)) + "</td>" +
          '<td class="col-num cell-secondary-num" data-val="' + (Number(tc.measurement) || 0) + '">' + esc(fmtDurationHms(tc.measurement || 0)) + "</td>" +
          '<td class="col-num cell-secondary-num" data-val="' + (Number(tc.thinkTime) || 0) + '">' + esc(fmtThinkTime(tc.thinkTime, tc.thinkTimeDev)) + "</td>" +
        "</tr>"
      );
    }).join("");

    tfoot.innerHTML =
      "<tr>" +
        '<td class="col-idx">Σ</td>' +
        '<td class="col-text">Totals (' + fmtInt(items.length) + " scenarios configured)</td>" +
        '<td class="col-num">' + esc(totals.usersDisplay || "0") + "</td>" +
        '<td class="col-num">100.00%</td>' +
        '<td class="col-num">' + esc(totals.arrivalDisplay || "0") + "</td>" +
        '<td class="col-num">100.00%</td>' +
        '<td class="col-num">' + esc(totals.rampUpDisplay || "—") + "</td>" +
        '<td class="col-num">' + esc(totals.measurementDisplay || "—") + "</td>" +
        '<td class="col-num">' + esc(totals.thinkTimeDisplay || "—") + "</td>" +
      "</tr>";

    if (searchInput) {
      searchInput.addEventListener("input", function () {
        var q = searchInput.value.toLowerCase().trim();
        var rows = tbody.querySelectorAll("tr");
        var count = 0;
        Array.prototype.forEach.call(rows, function (row) {
          var cell = row.querySelector(".scenario-name");
          var txt = cell ? cell.textContent.toLowerCase() : "";
          var match = !q || txt.indexOf(q) !== -1;
          row.style.display = match ? "" : "none";
          if (match) {
            count++;
            row.cells[0].textContent = String(count);
          }
        });
        if (subtitleEl) {
          subtitleEl.textContent = q
            ? count + " of " + items.length + ' scenarios match "' + searchInput.value + '"'
            : defaultSummary;
        }
      });
    }
  }

  /** Renders the cross-tier Performance Summary Matrix (#perf-summary-table). */
  function renderSummaryMatrix(ov, meta) {
    var table = document.getElementById("perf-summary-table");
    if (!table) return;
    var pctList = UI.getConfiguredPercentiles(ov && ov.summary ? [ov.summary.transactions, ov.summary.actions, ov.summary.requests, ov.summary.pageLoadTimings, ov.summary.customTimers] : undefined);

    var theadHtml =
      "<thead>" +
        '<tr class="group-header-row">' +
          '<th rowspan="2" class="col-idx">#</th>' +
          '<th rowspan="2" class="col-text">Metric Tier</th>' +
          '<th colspan="3" scope="colgroup">Counts &amp; Rates</th>' +
          '<th colspan="2" scope="colgroup">Failures</th>' +
          '<th colspan="4" scope="colgroup" class="cg-alt">Runtime Statistics [ms]</th>' +
          (pctList.length ? '<th colspan="' + pctList.length + '" scope="colgroup">Percentiles [ms]</th>' : "") +
        "</tr>" +
        "<tr>" +
          '<th class="col-num">Total Count</th>' +
          '<th class="col-num">1/s</th>' +
          '<th class="col-num">1/h</th>' +
          '<th class="col-num">Errors</th>' +
          '<th class="col-num">Events</th>' +
          '<th class="col-num cg-alt">Mean</th>' +
          '<th class="col-num cg-alt">Min</th>' +
          '<th class="col-num cg-alt">Max</th>' +
          '<th class="col-num cg-alt">Dev</th>' +
          pctList.map(function (p) { return '<th class="col-num">P' + esc(p) + "</th>"; }).join("") +
        "</tr>" +
      "</thead>";

    var sum = ov.summary || {};
    var tiers = [
      { key: "transactions", label: "Transactions", sub: "(Scenarios)", href: "transactions.html", errHref: "errors.html#sec-tx-errors", always: true },
      { key: "actions", label: "Actions", sub: "(Steps)", href: "actions.html", errHref: "errors.html#sec-act-errors", always: true },
      { key: "requests", label: "Requests", sub: "(HTTP Hits)", href: "requests.html", errHref: "errors.html#sec-req-errors", always: true },
      { key: "pageLoadTimings", label: "Page Load Timings", sub: "", href: "page-load-timings.html", errHref: "errors.html", always: false },
      { key: "customTimers", label: "Custom Timers", sub: "", href: "custom-timers.html", errHref: "errors.html", always: false }
    ];

    var rowsHtml = tiers
      .filter(function (t) {
        var s = sum[t.key];
        return t.always || (s && Number(s.count) > 0);
      })
      .map(function (t, idx) {
        var s = sum[t.key] || {};
        var errs = Number(s.errors) || 0;
        var evs = Number(s.events) || 0;
        var pctCells = pctList.map(function (p) {
          var v = getPct(s, p);
          return '<td class="col-num">' + (v !== null && v !== undefined ? fmtInt(v) : "—") + "</td>";
        }).join("");
        return (
          "<tr>" +
            '<td class="col-idx">' + (idx + 1) + "</td>" +
            '<td class="col-text"><a class="tier-link" href="' + t.href + '">' + t.label + (t.sub ? ' <span class="tier-sub">' + t.sub + "</span>" : "") + "</a></td>" +
            '<td class="col-num cell-primary-num">' + fmtInt(s.count || 0) + "</td>" +
            '<td class="col-num">' + fmtDec(s.rate || 0, 2) + "</td>" +
            '<td class="col-num">' + fmtInt(s.cph || 0) + "</td>" +
            '<td class="col-num">' + (errs > 0 ? '<a href="' + t.errHref + '" class="matrix-alert-pill pill-err">' + fmtInt(errs) + "</a>" : '<span class="cell-zero">0</span>') + "</td>" +
            '<td class="col-num">' + (evs > 0 ? '<a href="events.html" class="matrix-alert-pill pill-warn">' + fmtInt(evs) + "</a>" : '<span class="cell-zero">0</span>') + "</td>" +
            '<td class="col-num cg-alt cell-primary-num">' + fmtInt(s.mean || 0) + "</td>" +
            '<td class="col-num cg-alt">' + fmtInt(s.min || 0) + "</td>" +
            '<td class="col-num cg-alt">' + fmtInt(s.max || 0) + "</td>" +
            '<td class="col-num cg-alt">' + fmtInt(s.dev || 0) + "</td>" +
            pctCells +
          "</tr>"
        );
      })
      .join("");

    table.innerHTML = theadHtml + "<tbody>" + rowsHtml + "</tbody>";
  }

  document.addEventListener("DOMContentLoaded", function () {
    var ov = window.XLT_OVERVIEW || {};
    var meta = window.XLT_META || null;
    renderVerdict(ov, meta);
    renderComment(ov, meta);
    renderKpis(ov);
    renderCharts(ov, meta);
    renderHotspots(ov, meta);
    renderLoadProfile(ov);
    renderSummaryMatrix(ov, meta);
  });
})();
