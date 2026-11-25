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
 * XLT Modern Load Report — Error Forensics Renderer & Interactive Controls (`errors.html`)
 *
 * @fileoverview
 * Populates the error diagnostics and stack-trace forensics page from `window.XLT_ERRORS`:
 *   1. Summary KPI strip (`#err-kpi-grid`) — Transaction, Action, and Request error tallies.
 *   2. Static error progression charts (`#err-charts-section`) hidden when `-noCharts` was used.
 *   3. Unified Transaction Errors card (`#sec-tx-errors`):
 *      - Aggregated Error Signatures table (`#err-overview-table`) grouped by failure message,
 *        with on-demand inline per-row overview chart drawers and "↓ Details" jump links.
 *      - Connected Incident Forensics table (`#sec-incidents` / `#err-incidents-table`) with
 *        Java exception extraction, Result Browser dump links (`resultBrowsers/`), stack trace
 *        copy buttons, and live filtering.
 *
 * Accessibility / Find-in-Page Design:
 * Collapsed raw-path drawers use `hidden="until-found"` with a `beforematch` listener so
 * native browser search (`Ctrl+F` / `Cmd+F`) automatically expands matching blocks; older
 * browsers lacking `onbeforematch` reveal all path drawers upfront as progressive fallback.
 * Programmatic row jumps await `scrollIntoView()` promises (with a `scrollend`/idle fallback)
 * before highlighting and focusing the target row with `{ preventScroll: true }`.
 *
 * Consumes:
 * - `window.XLT_ERRORS` (`incidents`, `summary`, `overviewByMessage`, `requestErrorCharts`,
 *   `transactionErrorCharts`, `resultsPathPrefix`)
 * - `window.XLT_META` (`counts`, `linkToResultBrowser`, `chartsGenerated`)
 * - `window.XLT_UI` (`esc`, `fmtInt`, `fmtDec`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var fmtDec = UI.fmtDec;

  /**
   * Normalizes an error message to the shared grouping key used across overview and details.
   *
   * @param {string} msg Raw error message.
   * @returns {string} Non-empty error message key.
   */
  function normalizeErrorMsg(msg) {
    return msg ? String(msg) : "(No error message)";
  }

  /**
   * Awaits programmatic `scrollIntoView()` completion using native Scroll method promises when
   * supported, falling back to `scrollend` with an idle timer for browsers returning `undefined`.
   *
   * @param {Element} element Target DOM element to scroll into view.
   * @param {ScrollIntoViewOptions} options Scroll options.
   * @returns {Promise<void>} Resolves once scrolling completes (or immediately if already in view).
   */
  function scrollIntoViewAsync(element, options) {
    var result = element.scrollIntoView(options);
    if (result && typeof result.then === "function") {
      return result;
    }
    return new Promise(function (resolve) {
      var timeoutId;
      function finish() {
        clearTimeout(timeoutId);
        window.removeEventListener("scrollend", finish);
        window.removeEventListener("scroll", onScroll);
        resolve();
      }
      function onScroll() {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(finish, 100);
      }
      window.addEventListener("scrollend", finish, { once: true });
      window.addEventListener("scroll", onScroll, { passive: true });
      timeoutId = setTimeout(finish, 100);
    });
  }

  /**
   * Resolves the clickable Result Browser HTML dump URL for a captured directory entry.
   * Ignores omitted `"..."` placeholder entries and avoids double-prepending `resultsPathPrefix`
   * when `dir` already contains `resultBrowsers/` or points directly to an `.html` file.
   *
   * @param {string} prefix Configured `resultsPathPrefix` (defaults to `"resultBrowsers/"`).
   * @param {string} dir Dump directory name or relative path.
   * @returns {string|null} Resolved relative `href`, or `null` if non-linkable.
   */
  function resolveResultBrowserHref(prefix, dir) {
    var cleanDir = String(dir || "").trim();
    if (!cleanDir || cleanDir === "...") return null;
    if (/\.html?$/i.test(cleanDir)) return cleanDir;
    var p = String(prefix || "resultBrowsers/").trim();
    if (p && p.charAt(p.length - 1) !== "/") p += "/";
    if (cleanDir.indexOf("resultBrowsers/") === 0 || cleanDir.indexOf("./resultBrowsers/") === 0) {
      return cleanDir.replace(/\/+$/, "") + "/index.html";
    }
    return p + cleanDir.replace(/^\/+/, "").replace(/\/+$/, "") + "/index.html";
  }

  /**
   * Parses the top exception class/type name from a Java stack trace's first line
   * (fallback when `inc.exceptionClass` is not pre-extracted by the generator).
   *
   * @param {string} trace Raw Java stack trace text.
   * @param {string} msg Fallback error message.
   * @returns {string} Exception class name, `"Error"`, or `"—"`.
   */
  function extractExceptionClass(trace, msg) {
    if (trace) {
      var firstLine = String(trace).trim().split(/\r?\n/)[0] || "";
      var colonIdx = firstLine.indexOf(":");
      if (colonIdx > 0) return firstLine.slice(0, colonIdx).trim();
      if (firstLine) return firstLine.trim();
    }
    return msg ? "Error" : "—";
  }

  /**
   * Computes the report display timezone offset (in ms) relative to UTC from
   * `XLT_META.startTime` (`YYYY-MM-DD HH:MM:SS ...`) and `XLT_META.startTimeMs`,
   * so dump directory epoch timestamps match report chart axes.
   *
   * @param {Object} meta Report metadata object.
   * @returns {number|null} Offset in milliseconds, or `null` if unavailable.
   */
  function computeReportTzOffsetMs(meta) {
    if (!meta || !meta.startTime || meta.startTimeMs === undefined) return null;
    var startMs = Number(meta.startTimeMs);
    if (!Number.isFinite(startMs) || startMs <= 0) return null;
    var m = /^(\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2}):(\d{2})/.exec(String(meta.startTime).trim());
    if (!m) return null;
    var utcFromWallClock = Date.UTC(
      Number(m[1]),
      Number(m[2]) - 1,
      Number(m[3]),
      Number(m[4]),
      Number(m[5]),
      Number(m[6])
    );
    return utcFromWallClock - startMs;
  }

  function pad2(n) {
    return n < 10 ? "0" + n : String(n);
  }

  /**
   * Parses a Result Browser dump directory string (e.g.
   * `ac0010_us-west1_00/TRegisteredOrder/802/output/1770070129968`) into a
   * full date-time label (`YYYY-MM-DD HH:MM:SS`), epoch sort key, and full raw path.
   *
   * @param {string} dirRaw Raw directory string.
   * @param {number|null} tzOffsetMs Report timezone offset in ms from UTC.
   * @returns {{ label: string, dateTimeStr: string, timeStr: string, agent: string, raw: string, epochMs: number|null, isPlaceholder: boolean }} Parsed dump metadata.
   */
  function parseDumpHint(dirRaw, tzOffsetMs) {
    var clean = String(dirRaw || "").trim();
    if (!clean || clean === "...") {
      return { label: clean || "…", dateTimeStr: "", timeStr: "", agent: "", raw: clean, epochMs: null, isPlaceholder: true };
    }
    var parts = clean.split("/");
    var agentSeg = parts[0] || "";
    var runSeg = parts.length >= 3 ? parts[2] : "";
    var lastSeg = parts[parts.length - 1] || "";
    var shortAgent = agentSeg ? (agentSeg.split("_")[0] || agentSeg.slice(0, 12)) : "";
    var timeStr = "";
    var dateTimeStr = "";
    var epochMs = null;

    if (/^\d{12,14}$/.test(lastSeg)) {
      var parsedEpoch = Number(lastSeg);
      if (Number.isFinite(parsedEpoch)) {
        epochMs = parsedEpoch;
        if (tzOffsetMs !== null) {
          var d = new Date(parsedEpoch + tzOffsetMs);
          if (!Number.isNaN(d.getTime())) {
            timeStr = pad2(d.getUTCHours()) + ":" + pad2(d.getUTCMinutes()) + ":" + pad2(d.getUTCSeconds());
            dateTimeStr =
              d.getUTCFullYear() + "-" + pad2(d.getUTCMonth() + 1) + "-" + pad2(d.getUTCDate()) +
              " " + timeStr;
          }
        }
      }
    }

    var label;
    if (dateTimeStr) {
      label = dateTimeStr;
    } else if (shortAgent) {
      label = runSeg ? (shortAgent + "/" + runSeg) : shortAgent;
    } else {
      label = clean.length > 18 ? clean.slice(-18) : clean;
    }

    return {
      label: label,
      dateTimeStr: dateTimeStr,
      timeStr: timeStr,
      agent: shortAgent,
      raw: clean,
      epochMs: epochMs,
      isPlaceholder: false
    };
  }

  var FRAMEWORK_FRAME_RE = /^\s+at\s+(org\.junit\.|java\.|javax\.|jdk\.|sun\.|com\.sun\.|org\.htmlunit\.|com\.xceptance\.xlt\.api\.actions\.Abstract|com\.xceptance\.xlt\.engine\.)/;
  var STACK_FRAME_RE = /^\s+at\s+/;

  /**
   * Escapes a Java stack trace and highlights application/test stack frames
   * while leaving JDK, JUnit, and HtmlUnit frames visually secondary.
   *
   * @param {string} traceStr Raw trace string.
   * @returns {string} HTML string safe for insertion inside `<pre>`.
   */
  function formatStackTraceHtml(traceStr) {
    var lines = String(traceStr || "").replace(/\r\n/g, "\n").split("\n");
    return lines.map(function (line) {
      var escaped = esc(line);
      if (STACK_FRAME_RE.test(line) && !FRAMEWORK_FRAME_RE.test(line)) {
        return '<span class="trace-frame-focus">' + escaped + "</span>";
      }
      return escaped;
    }).join("\n");
  }

  document.addEventListener("DOMContentLoaded", function () {
    var raw = window.XLT_ERRORS || {};
    var meta = window.XLT_META || {};
    var incidents = Array.isArray(raw) ? raw : (raw.incidents || []);
    var summary = raw.summary || {};
    var reqCharts = raw.requestErrorCharts || [];
    var txCharts = raw.transactionErrorCharts || [];
    var resultsPrefix = raw.resultsPathPrefix || "";
    var linkResultBrowsers =
      meta.linkToResultBrowser !== undefined
        ? Boolean(meta.linkToResultBrowser)
        : Boolean(resultsPrefix);
    var chartsGenerated = meta.chartsGenerated !== false;

    var txErrors = summary.transactionErrors !== undefined
      ? Number(summary.transactionErrors)
      : incidents.reduce(function (s, i) { return s + (Number(i.count) || 0); }, 0);
    var actErrors = summary.actionErrors !== undefined ? Number(summary.actionErrors) : 0;
    var reqErrors = summary.requestErrors !== undefined ? Number(summary.requestErrors) : 0;

    // 1. KPI Strip
    var kpiGrid = document.getElementById("err-kpi-grid");
    if (kpiGrid) {
      var affectedTx = {};
      incidents.forEach(function (inc) {
        if (inc.testCaseName) affectedTx[inc.testCaseName] = true;
      });
      var affectedCount = Object.keys(affectedTx).length;
      var totalScenarios = (meta.counts && meta.counts.transactions) || affectedCount;

      kpiGrid.innerHTML =
        '<article class="kpi-card" id="sec-tx-errors-kpi">' +
          '<span class="kpi-label">Transaction Errors</span>' +
          '<div class="kpi-val ' + (txErrors > 0 ? "cell-err" : "") + '">' + fmtInt(txErrors) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">failed runs</span></div>' +
          '<div class="kpi-sub"><strong>' + fmtDec(summary.transactionErrPct || 0, 3) + "%</strong> failure rate across " + fmtInt(summary.transactionCount || 0) + " tx</div>" +
        "</article>" +
        '<article class="kpi-card" id="sec-act-errors">' +
          '<span class="kpi-label">Action Errors</span>' +
          '<div class="kpi-val ' + (actErrors > 0 ? "cell-err" : "") + '">' + fmtInt(actErrors) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">failed steps</span></div>' +
          '<div class="kpi-sub">Out of ' + fmtInt(summary.actionCount || 0) + " total action executions</div>" +
        "</article>" +
        '<article class="kpi-card" id="' + ((chartsGenerated && reqCharts.length) ? "sec-req-errors-kpi" : "sec-req-errors") + '">' +
          '<span class="kpi-label">Request Errors</span>' +
          '<div class="kpi-val ' + (reqErrors > 0 ? "cell-err" : "") + '">' + fmtInt(reqErrors) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">HTTP errors</span></div>' +
          '<div class="kpi-sub">Out of ' + fmtInt(summary.requestCount || 0) + " total HTTP requests</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Affected Scenarios</span>' +
          '<div class="kpi-val">' + fmtInt(affectedCount) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">of ' + fmtInt(totalScenarios) + "</span></div>" +
          '<div class="kpi-sub">' + fmtInt(incidents.length) + " distinct incident group(s)</div>" +
        "</article>";
    }

    function renderStripItem(opts) {
      return (
        '<div class="error-strip-item chart-canvas-light" id="' + esc(opts.id) + '">' +
          '<img class="zoomable-chart" src="' + esc(opts.src) + '" alt="' + esc(opts.alt) + '" data-chart-title="' + esc(opts.title) + '" loading="lazy" decoding="async" />' +
          (opts.backHref ? '<a class="error-strip-backlink" href="' + esc(opts.backHref) + '">&#8593; Back to Table</a>' : "") +
        "</div>"
      );
    }

    // 2. Error Progression Charts
    var chartsSec = document.getElementById("err-charts-section");
    var mainChartWrap = document.getElementById("err-main-chart-wrap");
    var reqChartsWrap = document.getElementById("sec-req-errors");
    var reqChartsGrid = document.getElementById("err-req-charts-grid");
    var reqChartsCount = document.getElementById("err-req-charts-count");

    if (chartsSec) {
      if (!chartsGenerated) {
        chartsSec.hidden = true;
        if (reqChartsWrap) {
          reqChartsWrap.id = "err-req-charts-sub";
          reqChartsWrap.hidden = true;
        }
      } else {
        if (mainChartWrap) {
          mainChartWrap.innerHTML =
            '<div class="chart-panel">' +
              '<div class="chart-panel-header"><strong>Errors Progression</strong></div>' +
              '<div class="chart-panel-body chart-canvas-light">' +
                '<img class="zoomable-chart" src="charts/Errors.webp" alt="Errors Progression" data-chart-title="Errors Progression" loading="lazy" decoding="async" />' +
              "</div>" +
            "</div>";
        }
        if (reqChartsWrap && reqChartsGrid) {
          if (reqCharts.length > 0) {
            var sortedReqCharts = reqCharts.slice().sort(function (a, b) {
              var na = Number(a.id), nb = Number(b.id);
              if (!isNaN(na) && !isNaN(nb) && na !== nb) return na - nb;
              return String(a.id).localeCompare(String(b.id));
            });
            reqChartsGrid.innerHTML = sortedReqCharts.map(function (rc) {
              var src = rc.src || ("charts/errors/r" + rc.id + ".webp");
              var title = "Request Errors (r" + rc.id + ")";
              return renderStripItem({
                id: "err-req-chart-" + rc.id,
                src: src,
                alt: "Request Errors by Code (" + rc.id + ")",
                title: title
              });
            }).join("");
            if (reqChartsCount) {
              reqChartsCount.textContent = reqCharts.length + (reqCharts.length === 1 ? " response code" : " response codes") + " · Click chart to enlarge";
            }
            reqChartsWrap.hidden = false;
          } else {
            reqChartsWrap.id = "err-req-charts-sub";
            reqChartsWrap.hidden = true;
          }
        }
      }
    }

    // Index overview charts by ID for inline per-row drawers
    var txChartById = {};
    txCharts.forEach(function (tc) {
      if (tc && tc.id !== undefined && tc.id !== null) {
        txChartById[String(tc.id)] = tc;
      }
    });

    // Pre-index first incident row ID by normalized error message for progressive anchor hrefs
    var firstIncidentIdByMsg = {};
    incidents.forEach(function (inc, idx) {
      var msgKey = normalizeErrorMsg(inc.message);
      if (!firstIncidentIdByMsg[msgKey]) {
        firstIncidentIdByMsg[msgKey] = "err-incident-" + (idx + 1);
      }
    });

    // 3. Overview by Error Message Table (group client-side if generator omitted overviewByMessage)
    var overviewRows = Array.isArray(raw.overviewByMessage) ? raw.overviewByMessage : null;
    if (!overviewRows) {
      var byMsg = {};
      incidents.forEach(function (inc) {
        var msg = normalizeErrorMsg(inc.message);
        if (!byMsg[msg]) {
          byMsg[msg] = {
            message: msg,
            count: 0,
            exceptionClass: inc.exceptionClass || extractExceptionClass(inc.trace, msg),
            chartId: inc.overviewChartId || null
          };
        }
        byMsg[msg].count += Number(inc.count) || 0;
      });
      overviewRows = Object.keys(byMsg).map(function (k) { return byMsg[k]; });
      overviewRows.sort(function (a, b) { return b.count - a.count; });
    }

    var sigBadge = document.getElementById("err-signature-count");
    if (sigBadge) {
      sigBadge.textContent = fmtInt(overviewRows.length) + (overviewRows.length === 1 ? " Signature" : " Signatures");
    }

    var CHART_ICON_SVG =
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" ' +
      'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
      '<path d="M3 3v18h18"/><path d="M7 15l4-4 3 3 5-6"/></svg>';
    var ARROW_DOWN_SVG =
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" ' +
      'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
      '<path d="M12 5v14"/><path d="m19 12-7 7-7-7"/></svg>';
    var ARROW_UP_SVG =
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" ' +
      'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
      '<path d="M12 19V5"/><path d="m5 12 7-7 7 7"/></svg>';
    var COPY_ICON_SVG =
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" ' +
      'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
      '<rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>';
    var CHECK_ICON_SVG =
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" ' +
      'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
      '<polyline points="20 6 9 17 4 12"/></svg>';
    var CHEVRON_DOWN_SVG =
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" ' +
      'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
      '<polyline points="6 9 12 15 18 9"/></svg>';
    var CHEVRON_UP_SVG =
      '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" ' +
      'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
      '<polyline points="18 15 12 9 6 15"/></svg>';

    var selectAllCb = document.getElementById("err-select-all");
    var ovClearSelBtn = document.getElementById("err-ov-clear-selection");
    var detailsClearSelBtn = document.getElementById("err-details-clear-selection");
    var selectedMsgs = new Set();

    var ovTbody = document.querySelector("#err-overview-table tbody");
    var ovTfoot = document.querySelector("#err-overview-table tfoot");
    var totalIncCount = overviewRows.reduce(function (s, r) { return s + (Number(r.count) || 0); }, 0);
    if (ovTbody) {
      if (!overviewRows.length) {
        if (selectAllCb) selectAllCb.disabled = true;
        ovTbody.innerHTML =
          '<tr><td colspan="7" class="col-text" style="text-align:center;padding:28px;color:var(--status-pass-text);font-weight:700;">' +
            "✓ Zero transaction errors recorded during this test run." +
          "</td></tr>";
        if (ovTfoot) ovTfoot.innerHTML = "";
      } else {
        ovTbody.innerHTML = overviewRows.map(function (r, idx) {
          var share = totalIncCount > 0 ? ((Number(r.count) || 0) * 100) / totalIncCount : 0;
          var msgKey = normalizeErrorMsg(r.message);
          var hasChart = Boolean(r.chartId && chartsGenerated);
          var chartIdStr = hasChart ? String(r.chartId) : "";
          var chartMeta = hasChart ? txChartById[chartIdStr] : null;
          var chartSrc = hasChart ? ((chartMeta && chartMeta.src) || ("charts/errors/t" + chartIdStr + ".webp")) : "";
          var chartLabel = hasChart
            ? ((chartMeta && chartMeta.title ? chartMeta.title : r.message) + " (t" + chartIdStr + ")")
            : "";
          var drawerId = "ov-chart-row-" + idx;
          var rowId = hasChart ? ("err-row-" + chartIdStr) : ("err-ov-row-" + idx);
          var firstIncId = firstIncidentIdByMsg[msgKey] || "";

          var chartBtnHtml = hasChart
            ? '<button type="button" class="btn-chip btn-icon err-ov-action-btn err-ov-chart-btn" data-toggle-ov-chart="' + esc(drawerId) + '" aria-expanded="false" aria-controls="' + esc(drawerId) + '" aria-label="Toggle overview error chart" title="Toggle overview error chart">' + CHART_ICON_SVG + "</button>"
            : "";
          var jumpBtnHtml = firstIncId
            ? '<a class="btn-chip btn-icon err-ov-action-btn err-jump-details-btn" href="#' + esc(firstIncId) + '" data-jump-details-msg="' + esc(msgKey) + '" aria-label="Jump to first matching detail for ' + esc(r.message) + '" title="Jump to first matching entry in Transaction Error Details">' + ARROW_DOWN_SVG + "</a>"
            : "";

          var mainRow =
            '<tr id="' + esc(rowId) + '" class="err-ov-selectable-row" data-error-msg="' + esc(msgKey) + '" aria-selected="false" tabindex="-1">' +
              '<td class="col-select"><label class="err-cb-wrap"><input type="checkbox" class="err-ov-checkbox" data-ov-select aria-label="Filter details to ' + esc(r.message) + '" /><span class="err-cb-box" aria-hidden="true"></span></label></td>' +
              '<td class="col-idx">' + (idx + 1) + "</td>" +
              '<td class="col-text">' + esc(r.message) + "</td>" +
              '<td class="col-num cell-err" data-val="' + (Number(r.count) || 0) + '">' + fmtInt(r.count) + "</td>" +
              '<td class="col-num" data-val="' + share.toFixed(4) + '">' + fmtDec(share, 1) + "%</td>" +
              '<td class="col-text"><code>' + esc(r.exceptionClass || "—") + "</code></td>" +
              '<td class="col-actions"><div class="err-ov-actions">' + chartBtnHtml + jumpBtnHtml + "</div></td>" +
            "</tr>";

          if (!hasChart) {
            return mainRow;
          }

          var drawerRow =
            '<tr id="' + esc(drawerId) + '" class="drawer-row err-ov-chart-drawer" data-ov-chart-id="' + esc(chartIdStr) + '" hidden>' +
              '<td colspan="7">' +
                '<div class="err-ov-chart-box chart-canvas-light">' +
                  '<img class="zoomable-chart" src="' + esc(chartSrc) + '" alt="' + esc(chartLabel) + '" data-chart-title="' + esc(chartLabel) + '" loading="lazy" decoding="async" />' +
                "</div>" +
              "</td>" +
            "</tr>";

          return mainRow + drawerRow;
        }).join("");
        if (ovTfoot) {
          ovTfoot.innerHTML =
            '<tr class="filtered-totals-row" id="err-ov-filtered-totals" hidden>' +
              '<td class="col-select"></td>' +
              '<td class="col-idx">ƒ</td>' +
              '<td class="col-text"></td>' +
              '<td class="col-num"></td>' +
              '<td class="col-num"></td>' +
              '<td class="col-text"></td>' +
              '<td class="col-actions"></td>' +
            "</tr>" +
            "<tr>" +
              '<td class="col-select"></td>' +
              '<td class="col-idx">Σ</td>' +
              '<td class="col-text">Total Failures (' + fmtInt(overviewRows.length) + " signatures)</td>" +
              '<td class="col-num cell-err">' + fmtInt(totalIncCount) + "</td>" +
              '<td class="col-num">100.0%</td>' +
              '<td class="col-text"></td>' +
              '<td class="col-actions"></td>' +
            "</tr>";
        }
      }
    }

    // 4. Incident Forensics & Result Browser Dumps Table (Connected inside #sec-tx-errors)
    var incTable = document.getElementById("err-incidents-table");
    var incTbody = incTable && incTable.querySelector("tbody");
    var incTfoot = incTable && incTable.querySelector("tfoot");
    var countBadge = document.getElementById("err-visible-count");
    var filterInput = document.getElementById("err-details-filter");
    var toggleAllBtn = document.getElementById("err-toggle-all-traces");
    var tzOffsetMs = computeReportTzOffsetMs(meta);
    var cards = [];
    var totalDetailCount = 0;

    function setTraceExpanded(wrap, btn, open) {
      if (!wrap) return;
      wrap.classList.toggle("is-expanded", open);
      if (btn) {
        btn.setAttribute("aria-expanded", open ? "true" : "false");
        btn.classList.toggle("active", open);
        btn.innerHTML = open ? CHEVRON_UP_SVG : CHEVRON_DOWN_SVG;
        var lbl = open ? "Collapse trace" : "Expand full trace";
        btn.setAttribute("aria-label", lbl);
        btn.title = lbl;
      }
    }

    function setPathsExpanded(wrap, btn, open) {
      if (!wrap) return;
      if (open) {
        wrap.removeAttribute("hidden");
      } else {
        wrap.setAttribute("hidden", "until-found");
      }
      if (btn) {
        btn.setAttribute("aria-expanded", open ? "true" : "false");
        btn.classList.toggle("active", open);
        btn.textContent = open ? "▾ Raw" : "Raw";
        btn.title = open ? "Hide raw Result Browser paths" : "Show raw Result Browser paths";
      }
    }

    function syncToggleAllBtn() {
      if (!toggleAllBtn || !cards.length) return;
      var visibleCards = cards.filter(function (c) { return !c.el.hidden; });
      var targetCards = visibleCards.length ? visibleCards : cards;
      var allOpen = visibleCards.length > 0 && targetCards.every(function (c) {
        return c.traceWrap && c.traceWrap.classList.contains("is-expanded");
      });
      toggleAllBtn.setAttribute("aria-expanded", allOpen ? "true" : "false");
      toggleAllBtn.textContent = allOpen ? "▲ Collapse All Traces" : "▼ Expand All Traces";
    }

    if (incTbody) {
      if (!incidents.length) {
        incTbody.innerHTML =
          '<tr><td colspan="5" class="col-text" style="text-align:center;padding:28px;color:var(--status-pass-text);font-weight:700;">' +
            "✓ No error incidents or stack traces recorded during this test run." +
          "</td></tr>";
        if (incTfoot) incTfoot.innerHTML = "";
        if (toggleAllBtn) toggleAllBtn.hidden = true;
        if (countBadge) countBadge.textContent = "0 incident groups";
      } else {
        var frag = document.createDocumentFragment();
        totalDetailCount = incidents.reduce(function (s, i) { return s + (Number(i.count) || 0); }, 0);

        incidents.forEach(function (inc, idx) {
          var n = idx + 1;
          var msgKey = normalizeErrorMsg(inc.message);
          var tr = document.createElement("tr");
          tr.id = "err-incident-" + n;
          tr.setAttribute("data-error-msg", msgKey);
          tr.setAttribute("tabindex", "-1");
          var cnt = Number(inc.count) || 0;
          var sharePct = totalDetailCount > 0 ? (cnt * 100) / totalDetailCount : 0;
          var exClass = inc.exceptionClass || extractExceptionClass(inc.trace, inc.message);
          var summaryLabel = inc.message || exClass || "Error";
          var dirs = Array.isArray(inc.directories) ? inc.directories : [];
          var dumpHints = [];
          var dumpsPanelHtml = "";
          var rawPathsDrawerHtml = "";

          if (dirs.length) {
            var parsedDirs = dirs.map(function (d, origIdx) {
              var info = parseDumpHint(d, tzOffsetMs);
              dumpHints.push(info.raw + " " + info.dateTimeStr + " " + info.agent);
              return {
                dir: d,
                info: info,
                origIdx: origIdx,
                href: linkResultBrowsers ? resolveResultBrowserHref(resultsPrefix, d) : null
              };
            });

            // Sort chronologically (earliest first) so timestamps align with the incident timeline chart
            parsedDirs.sort(function (a, b) {
              if (a.info.isPlaceholder !== b.info.isPlaceholder) {
                return a.info.isPlaceholder ? 1 : -1;
              }
              if (a.info.epochMs !== null && b.info.epochMs !== null && a.info.epochMs !== b.info.epochMs) {
                return a.info.epochMs - b.info.epochMs;
              }
              if (a.info.epochMs !== null && b.info.epochMs === null) return -1;
              if (a.info.epochMs === null && b.info.epochMs !== null) return 1;
              return a.origIdx - b.origIdx;
            });

            var renderedDirs = parsedDirs.map(function (entry, dIdx) {
              var info = entry.info;
              var href = entry.href;
              var labelTxt = info.dateTimeStr || info.label;
              var listItemHtml = href
                ? '<li><a class="incident-dump-link" href="' + esc(href) + '" target="_blank" rel="noopener" title="' + esc(info.raw) + '"><span>' + esc(labelTxt) + '</span><span class="dump-link-ext" aria-hidden="true">&#8599;</span></a></li>'
                : '<li><span class="incident-dump-link is-static" title="' + esc(info.raw) + '"><span>' + esc(labelTxt) + "</span></span></li>";
              var rawRow = href
                ? '<div class="incident-path-item"><span class="incident-path-idx">#' + (dIdx + 1) + '</span><a href="' + esc(href) + '" target="_blank" rel="noopener"><code>' + esc(info.raw) + "</code> &#8599;</a></div>"
                : '<div class="incident-path-item"><span class="incident-path-idx">#' + (dIdx + 1) + "</span><code>" + esc(info.raw) + "</code></div>";
              return { listItem: listItemHtml, rawRow: rawRow };
            });

            dumpsPanelHtml =
              '<aside class="incident-dumps-panel" aria-label="Result Browsers">' +
                '<div class="incident-dumps-header">' +
                  '<span class="incident-dumps-label">Result Browsers</span>' +
                  '<span class="badge-tag incident-dumps-count">' + fmtInt(dirs.length) + "</span>" +
                  '<button type="button" class="incident-paths-toggle" data-toggle-paths="paths-wrap-' + n + '" aria-expanded="false" aria-controls="paths-wrap-' + n + '" title="Show raw Result Browser paths">Raw</button>' +
                "</div>" +
                '<ul class="incident-dumps-list">' +
                  renderedDirs.map(function (item) { return item.listItem; }).join("") +
                "</ul>" +
              "</aside>";

            rawPathsDrawerHtml =
              '<div id="paths-wrap-' + n + '" class="incident-paths-drawer" data-paths-wrap hidden="until-found">' +
                '<div class="incident-paths-list">' +
                  renderedDirs.map(function (item) { return item.rawRow; }).join("") +
                "</div>" +
              "</div>";
          }

          var detailChartId = inc.detailChartId !== undefined ? inc.detailChartId : inc.detailChartID;
          var detailChartHtml = "";
          if (chartsGenerated && detailChartId && String(detailChartId) !== "0") {
            var dSrc = inc.detailChartSrc || ("charts/errors/d" + detailChartId + ".webp");
            detailChartHtml =
              '<div class="incident-detail-chart chart-canvas-light">' +
                '<img class="zoomable-chart" src="' + esc(dSrc) + '" alt="Incident Detail Chart" data-chart-title="' + esc((inc.testCaseName || "") + " — " + (inc.actionName || "")) + '" loading="lazy" decoding="async" />' +
              "</div>";
          }

          var overviewChartId = inc.overviewChartId !== undefined ? inc.overviewChartId : inc.overviewChartID;
          var overviewChipHtml = (chartsGenerated && overviewChartId && String(overviewChartId) !== "0")
            ? '<a class="btn-chip btn-icon incident-overview-chip" href="#err-row-' + esc(overviewChartId) + '" data-jump-ov-chart="' + esc(overviewChartId) + '" aria-label="Jump to overview chart" title="Jump to overview chart">' + ARROW_UP_SVG + "</a>"
            : "";

          var txHtml = inc.testCaseName
            ? '<a href="transactions.html?item=' + encodeURIComponent(inc.testCaseName) + '"><strong>' + esc(inc.testCaseName) + "</strong></a>"
            : '<span style="color:var(--text-muted);">—</span>';
          var actMainHtml = inc.actionName
            ? '<a href="actions.html?item=' + encodeURIComponent(inc.actionName) + '">' + esc(inc.actionName) + "</a>"
            : '<span style="color:var(--text-muted);">—</span>';
          var reqSubHtml = inc.requestName
            ? ' <div class="incident-req-sub"><a class="badge-tag" href="requests.html?item=' + encodeURIComponent(inc.requestName) + '">' + esc(inc.requestName) + "</a></div>"
            : "";
          var actCopy = inc.requestName
            ? ((inc.actionName || "") + (inc.actionName ? " " : "") + inc.requestName)
            : (inc.actionName || "—");
          var exBadgeHtml = (exClass && exClass !== "—" && exClass !== summaryLabel)
            ? ' <code class="incident-ex-badge">' + esc(exClass) + "</code>"
            : "";
          var traceText = inc.trace || inc.message || "";

          tr.innerHTML =
            '<td class="col-idx">' + n + "</td>" +
            '<td class="col-num cell-err incident-count-cell" data-val="' + cnt + '">' +
              '<div class="incident-count-val">' + fmtInt(cnt) + "</div>" +
              '<div class="incident-count-share">' + fmtDec(sharePct, 1) + "%</div>" +
            "</td>" +
            '<td class="col-text incident-testcase-cell" data-val="' + esc(inc.testCaseName || "") + '">' + txHtml + "</td>" +
            '<td class="col-text incident-action-cell" data-val="' + esc(inc.actionName || "") + '" data-copy="' + esc(actCopy) + '">' + actMainHtml + reqSubHtml + "</td>" +
            '<td class="col-text incident-info-cell" data-val="' + esc(summaryLabel) + '" data-copy="' + esc(summaryLabel) + '">' +
              '<div class="incident-trace-header">' +
                '<div class="incident-error-headline">' +
                  '<span class="incident-error-msg">' + esc(summaryLabel) + "</span>" +
                  exBadgeHtml +
                "</div>" +
                '<div class="incident-trace-actions">' +
                  overviewChipHtml +
                  '<button type="button" class="btn-chip btn-icon incident-copy-btn" data-copy-trace="trace-pre-' + n + '" aria-label="Copy stack trace" title="Copy stack trace">' + COPY_ICON_SVG + "</button>" +
                  '<button type="button" class="btn-chip btn-icon incident-expand-btn" data-toggle-trace="trace-wrap-' + n + '" aria-expanded="false" aria-controls="trace-wrap-' + n + '" aria-label="Expand full trace" title="Expand full trace">' + CHEVRON_DOWN_SVG + "</button>" +
                "</div>" +
              "</div>" +
              '<div class="incident-body-container">' +
                '<div class="incident-body-layout' + (dumpsPanelHtml ? " has-dumps" : "") + '">' +
                  '<div class="incident-main-col">' +
                    detailChartHtml +
                    '<div id="trace-wrap-' + n + '" class="incident-trace-wrap" data-trace-wrap>' +
                      '<pre id="trace-pre-' + n + '" class="incident-trace-pre">' + formatStackTraceHtml(traceText) + "</pre>" +
                    "</div>" +
                  "</div>" +
                  dumpsPanelHtml +
                "</div>" +
                rawPathsDrawerHtml +
              "</div>" +
            "</td>";

          frag.appendChild(tr);
          cards.push({
            el: tr,
            item: inc,
            msgKey: msgKey,
            traceWrap: tr.querySelector("[data-trace-wrap]"),
            toggleBtn: tr.querySelector("[data-toggle-trace]"),
            blob: ((inc.testCaseName || "") + " " + (inc.actionName || "") + " " + (inc.requestName || "") + " " + dumpHints.join(" ") + " " + (inc.message || "") + " " + traceText).toLowerCase()
          });
        });

        incTbody.innerHTML = "";
        incTbody.appendChild(frag);
        if (incTfoot) {
          incTfoot.innerHTML =
            '<tr class="filtered-totals-row" id="err-inc-filtered-totals" hidden>' +
              '<td class="col-idx">ƒ</td>' +
              '<td class="col-num"></td>' +
              '<td class="col-text"></td>' +
              '<td class="col-text"></td>' +
              '<td class="col-text"></td>' +
            "</tr>" +
            "<tr>" +
              '<td class="col-idx">Σ</td>' +
              '<td class="col-num cell-err">' + fmtInt(totalDetailCount) + "</td>" +
              '<td class="col-text"></td>' +
              '<td class="col-text"></td>' +
              '<td class="col-text">Total Incident Occurrences (' + fmtInt(incidents.length) + " groups)</td>" +
            "</tr>";
        }
        if (countBadge) {
          countBadge.textContent = fmtInt(cards.length) + " incident group(s)";
        }
        syncToggleAllBtn();
      }
    }

    function applyDetailsFilter() {
      if (!cards.length) return;
      var q = filterInput ? filterInput.value : "";
      var hasSel = selectedMsgs.size > 0;
      var hasQuery = Boolean(q && q.trim());
      var isFiltered = hasSel || hasQuery;
      var vis = 0;
      var visOccurrences = 0;
      cards.forEach(function (c) {
        var selOk = !hasSel || selectedMsgs.has(c.msgKey);
        var txtOk = UI.matchesFilter(c.blob, q, c.item);
        var ok = selOk && txtOk;
        c.el.hidden = !ok;
        if (ok) {
          vis++;
          visOccurrences += Number(c.item && c.item.count) || 0;
        }
      });
      if (incTbody) {
        var seq = 1;
        Array.prototype.forEach.call(incTbody.rows, function (row) {
          if (!row.hidden && row.cells[0]) {
            row.cells[0].textContent = String(seq++);
          }
        });
      }
      var incFilteredRow = document.getElementById("err-inc-filtered-totals");
      if (incFilteredRow) {
        if (isFiltered) {
          var visPct = totalDetailCount > 0 ? (visOccurrences * 100) / totalDetailCount : 0;
          incFilteredRow.hidden = false;
          incFilteredRow.cells[1].innerHTML =
            '<div class="incident-count-val">' + fmtInt(visOccurrences) + "</div>" +
            '<div class="incident-count-share">' + fmtDec(visPct, 1) + "%</div>";
          incFilteredRow.cells[4].textContent = "Filtered Total (" + fmtInt(vis) + " of " + fmtInt(cards.length) + " groups)";
        } else {
          incFilteredRow.hidden = true;
        }
      }
      if (countBadge) {
        countBadge.textContent = isFiltered
          ? (vis + " / " + cards.length + " incident groups")
          : (fmtInt(cards.length) + " incident group(s)");
      }
      syncToggleAllBtn();
    }

    function syncSelectionUi() {
      var ovRows = ovTbody ? Array.prototype.slice.call(ovTbody.querySelectorAll("tr.err-ov-selectable-row")) : [];
      var selectedCount = 0;
      var selectedOccurrences = 0;
      ovRows.forEach(function (row) {
        var msg = row.getAttribute("data-error-msg");
        var isSel = selectedMsgs.has(msg);
        if (isSel) {
          selectedCount++;
          var countCell = row.cells[3];
          selectedOccurrences += countCell ? (Number(countCell.getAttribute("data-val")) || 0) : 0;
        }
        row.classList.toggle("is-selected", isSel);
        row.setAttribute("aria-selected", isSel ? "true" : "false");
        var cb = row.querySelector("[data-ov-select]");
        if (cb) cb.checked = isSel;
      });

      var ovFilteredRow = document.getElementById("err-ov-filtered-totals");
      if (ovFilteredRow) {
        if (selectedCount > 0) {
          var selShare = totalIncCount > 0 ? (selectedOccurrences * 100) / totalIncCount : 0;
          ovFilteredRow.hidden = false;
          ovFilteredRow.cells[2].textContent = "Selected Total (" + fmtInt(selectedCount) + " of " + fmtInt(ovRows.length) + " signatures)";
          ovFilteredRow.cells[3].textContent = fmtInt(selectedOccurrences);
          ovFilteredRow.cells[4].textContent = fmtDec(selShare, 1) + "%";
        } else {
          ovFilteredRow.hidden = true;
        }
      }

      if (selectAllCb) {
        selectAllCb.checked = ovRows.length > 0 && selectedCount === ovRows.length;
        selectAllCb.indeterminate = selectedCount > 0 && selectedCount < ovRows.length;
      }
      if (ovClearSelBtn) {
        ovClearSelBtn.hidden = selectedCount === 0;
        ovClearSelBtn.textContent = "Clear Filter (" + selectedCount + ") ✕";
      }
      if (detailsClearSelBtn) {
        detailsClearSelBtn.hidden = selectedCount === 0;
        detailsClearSelBtn.textContent = "Selected: " + selectedCount + (selectedCount === 1 ? " error" : " errors") + " ✕";
      }
      applyDetailsFilter();
    }

    function clearOverviewSelection() {
      if (!selectedMsgs.size) return;
      selectedMsgs.clear();
      syncSelectionUi();
      if (statusEl) {
        statusEl.textContent = "Cleared error selection filter";
      }
    }

    if (selectAllCb) {
      selectAllCb.addEventListener("change", function () {
        var ovRows = ovTbody ? Array.prototype.slice.call(ovTbody.querySelectorAll("tr.err-ov-selectable-row")) : [];
        if (selectAllCb.checked) {
          ovRows.forEach(function (row) {
            var msg = row.getAttribute("data-error-msg");
            if (msg !== null) selectedMsgs.add(msg);
          });
        } else {
          selectedMsgs.clear();
        }
        syncSelectionUi();
      });
    }

    if (ovClearSelBtn) {
      ovClearSelBtn.addEventListener("click", clearOverviewSelection);
    }
    if (detailsClearSelBtn) {
      detailsClearSelBtn.addEventListener("click", clearOverviewSelection);
    }

    if (ovTbody) {
      ovTbody.addEventListener("change", function (e) {
        var cb = e.target.closest("[data-ov-select]");
        if (!cb) return;
        var row = cb.closest("tr.err-ov-selectable-row");
        if (!row) return;
        var msg = row.getAttribute("data-error-msg");
        if (cb.checked) selectedMsgs.add(msg);
        else selectedMsgs.delete(msg);
        syncSelectionUi();
      });

      ovTbody.addEventListener("click", function (e) {
        if (e.target.closest("a, button, input, label")) return;
        if (window.getSelection && String(window.getSelection()).trim()) return;
        var row = e.target.closest("tr.err-ov-selectable-row");
        if (!row) return;
        var msg = row.getAttribute("data-error-msg");
        if (selectedMsgs.has(msg)) selectedMsgs.delete(msg);
        else selectedMsgs.add(msg);
        syncSelectionUi();
      });
    }

    if (filterInput && cards.length) {
      filterInput.addEventListener("input", applyDetailsFilter);
    }

    if (toggleAllBtn && cards.length) {
      toggleAllBtn.addEventListener("click", function () {
        var anyCollapsed = cards.some(function (c) {
          return !c.el.hidden && c.traceWrap && !c.traceWrap.classList.contains("is-expanded");
        });
        var hasVisible = cards.some(function (c) { return !c.el.hidden; });
        cards.forEach(function (c) {
          if (hasVisible && c.el.hidden) return;
          setTraceExpanded(c.traceWrap, c.toggleBtn, anyCollapsed);
        });
        syncToggleAllBtn();
      });
    }

    var currentScrollId = 0;
    var highlightedRow = null;
    var highlightTimer = null;
    var statusEl = document.getElementById("table-sort-status");
    var toggleAllOvChartsBtn = document.getElementById("err-toggle-all-ov-charts");

    function getOverviewChartDrawers() {
      return ovTbody ? Array.prototype.slice.call(ovTbody.querySelectorAll("tr.err-ov-chart-drawer")) : [];
    }

    function syncToggleAllOvChartsBtn() {
      if (!toggleAllOvChartsBtn) return;
      var drawers = getOverviewChartDrawers();
      if (!drawers.length) {
        toggleAllOvChartsBtn.hidden = true;
        return;
      }
      toggleAllOvChartsBtn.hidden = false;
      var allOpen = drawers.every(function (dr) { return !dr.hidden; });
      toggleAllOvChartsBtn.setAttribute("aria-expanded", allOpen ? "true" : "false");
      toggleAllOvChartsBtn.classList.toggle("active", allOpen);
      var labelSpan = toggleAllOvChartsBtn.querySelector("span");
      if (labelSpan) {
        labelSpan.textContent = allOpen ? "Hide All Charts" : "Show All Charts";
      }
    }

    function setOverviewChartExpanded(drawerRow, open) {
      if (!drawerRow) return;
      drawerRow.hidden = !open;
      var btn = document.querySelector('[data-toggle-ov-chart="' + drawerRow.id + '"]');
      if (btn) {
        btn.setAttribute("aria-expanded", open ? "true" : "false");
        btn.classList.toggle("active", open);
      }
    }

    if (toggleAllOvChartsBtn) {
      syncToggleAllOvChartsBtn();
      toggleAllOvChartsBtn.addEventListener("click", function () {
        var drawers = getOverviewChartDrawers();
        if (!drawers.length) return;
        var anyClosed = drawers.some(function (dr) { return dr.hidden; });
        drawers.forEach(function (dr) {
          setOverviewChartExpanded(dr, anyClosed);
        });
        syncToggleAllOvChartsBtn();
      });
    }

    async function jumpToRow(targetRow, blockMode, announceMsg) {
      if (!targetRow) return;
      var scrollId = ++currentScrollId;
      if (highlightedRow) {
        highlightedRow.classList.remove("is-jump-target");
        highlightedRow = null;
      }
      clearTimeout(highlightTimer);

      var reduceMotion = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
      await scrollIntoViewAsync(targetRow, {
        behavior: reduceMotion ? "auto" : "smooth",
        block: blockMode || "center"
      });

      if (scrollId !== currentScrollId) return;

      void targetRow.offsetWidth;
      targetRow.classList.add("is-jump-target");
      highlightedRow = targetRow;
      highlightTimer = setTimeout(function () {
        if (highlightedRow === targetRow) {
          targetRow.classList.remove("is-jump-target");
          highlightedRow = null;
        }
      }, 2200);

      targetRow.focus({ preventScroll: true });
      if (statusEl && announceMsg) {
        statusEl.textContent = announceMsg;
      }
    }

    // Reveal raw path drawers automatically when browser Ctrl+F matches inside hidden="until-found"
    document.addEventListener("beforematch", function (e) {
      var pathsWrap = e.target.closest("[data-paths-wrap]");
      if (!pathsWrap) return;
      var btn = document.querySelector('[data-toggle-paths="' + pathsWrap.id + '"]');
      setPathsExpanded(pathsWrap, btn, true);
    });

    if (!("onbeforematch" in HTMLElement.prototype)) {
      Array.prototype.forEach.call(document.querySelectorAll('[data-paths-wrap][hidden="until-found"]'), function (el) {
        var btn = document.querySelector('[data-toggle-paths="' + el.id + '"]');
        setPathsExpanded(el, btn, true);
      });
    }

    document.addEventListener("click", function (e) {
      var ovChartBtn = e.target.closest("[data-toggle-ov-chart]");
      if (ovChartBtn) {
        var drawerId = ovChartBtn.getAttribute("data-toggle-ov-chart");
        var drawerRow = document.getElementById(drawerId);
        if (drawerRow) {
          setOverviewChartExpanded(drawerRow, drawerRow.hidden);
          syncToggleAllOvChartsBtn();
        }
        return;
      }

      var jumpDetailsBtn = e.target.closest("[data-jump-details-msg]");
      if (jumpDetailsBtn && incTbody) {
        e.preventDefault();
        var msgKey = jumpDetailsBtn.getAttribute("data-jump-details-msg");
        var rows = Array.prototype.slice.call(incTbody.rows);
        var matchRow = rows.find(function (r) {
          return !r.hidden && r.getAttribute("data-error-msg") === msgKey;
        });
        var adjustedFilter = false;
        if (!matchRow && selectedMsgs.size > 0 && !selectedMsgs.has(msgKey)) {
          selectedMsgs.add(msgKey);
          syncSelectionUi();
          adjustedFilter = true;
          matchRow = rows.find(function (r) {
            return !r.hidden && r.getAttribute("data-error-msg") === msgKey;
          });
        }
        if (!matchRow && filterInput && filterInput.value) {
          filterInput.value = "";
          applyDetailsFilter();
          adjustedFilter = true;
          matchRow = rows.find(function (r) {
            return !r.hidden && r.getAttribute("data-error-msg") === msgKey;
          });
        }
        if (matchRow) {
          jumpToRow(
            matchRow,
            "center",
            (adjustedFilter ? "Updated details filter and jumped" : "Jumped") +
              " to first matching detail for " + msgKey
          );
        }
        return;
      }

      var jumpOvBtn = e.target.closest("[data-jump-ov-chart]");
      if (jumpOvBtn) {
        e.preventDefault();
        var chartId = jumpOvBtn.getAttribute("data-jump-ov-chart");
        var ovRow = document.getElementById("err-row-" + chartId);
        var drawers = ovTbody ? ovTbody.querySelectorAll("tr.err-ov-chart-drawer") : [];
        var targetDrawer = null;
        Array.prototype.forEach.call(drawers, function (dr) {
          if (dr.getAttribute("data-ov-chart-id") === chartId) targetDrawer = dr;
        });
        if (targetDrawer) {
          setOverviewChartExpanded(targetDrawer, true);
          syncToggleAllOvChartsBtn();
        }
        if (ovRow) {
          jumpToRow(ovRow, "start", "Jumped to error overview chart");
        }
        return;
      }

      var toggleBtn = e.target.closest("[data-toggle-trace]");
      if (toggleBtn) {
        var targetId = toggleBtn.getAttribute("data-toggle-trace");
        var wrap = document.getElementById(targetId);
        if (wrap) {
          setTraceExpanded(wrap, toggleBtn, !wrap.classList.contains("is-expanded"));
          syncToggleAllBtn();
        }
        return;
      }

      var pathsBtn = e.target.closest("[data-toggle-paths]");
      if (pathsBtn) {
        var pathsId = pathsBtn.getAttribute("data-toggle-paths");
        var pathsWrap = document.getElementById(pathsId);
        if (pathsWrap) {
          setPathsExpanded(pathsWrap, pathsBtn, pathsWrap.hasAttribute("hidden"));
        }
        return;
      }

      var copyBtn = e.target.closest("[data-copy-trace]");
      if (copyBtn) {
        var preId = copyBtn.getAttribute("data-copy-trace");
        var preEl = document.getElementById(preId);
        if (preEl && navigator.clipboard) {
          clearTimeout(copyBtn._copyTimer);
          navigator.clipboard.writeText(preEl.textContent).then(function () {
            copyBtn.innerHTML = CHECK_ICON_SVG;
            copyBtn.classList.add("copied");
            copyBtn.title = "Copied stack trace";
            copyBtn._copyTimer = setTimeout(function () {
              copyBtn.innerHTML = COPY_ICON_SVG;
              copyBtn.classList.remove("copied");
              copyBtn.title = "Copy stack trace";
            }, 1500);
          });
        }
      }
    });
  });
})();
