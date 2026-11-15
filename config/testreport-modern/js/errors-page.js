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
 *   2. Static error progression charts (`#err-charts-grid`) hidden when `-noCharts` was used.
 *   3. Aggregated Error Signatures table (`#err-overview-table`) grouped by failure message.
 *   4. Captured `custom_data_logs/` attachments (`#err-custom-logs-table`).
 *   5. Expandable Incident Forensics cards (`#err-incidents-list`) with Java exception extraction,
 *      Result Browser dump links (`resultBrowsers/`), stack trace copy buttons, and live filtering.
 *
 * Accessibility / Find-in-Page Design:
 * Collapsed stack-trace wrappers use `hidden="until-found"` with a `beforematch` listener so
 * native browser search (`Ctrl+F` / `Cmd+F`) automatically expands matching trace blocks; older
 * browsers lacking `onbeforematch` reveal all trace blocks upfront as progressive fallback.
 *
 * Consumes:
 * - `window.XLT_ERRORS` (`incidents`, `summary`, `overviewByMessage`, `requestErrorCharts`,
 *   `transactionErrorCharts`, `customLogs`, `resultsPathPrefix`)
 * - `window.XLT_META` (`counts`, `linkToResultBrowser`, `chartsGenerated`)
 * - `window.XLT_UI` (`esc`, `fmtInt`, `fmtDec`, `fmtBytes`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var fmtDec = UI.fmtDec;
  var fmtBytes = UI.fmtBytes;

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

  document.addEventListener("DOMContentLoaded", function () {
    var raw = window.XLT_ERRORS || {};
    var meta = window.XLT_META || {};
    var incidents = Array.isArray(raw) ? raw : (raw.incidents || []);
    var summary = raw.summary || {};
    var reqCharts = raw.requestErrorCharts || [];
    var txCharts = raw.transactionErrorCharts || [];
    var customLogs = raw.customLogs || [];
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
    var txChartsWrap = document.getElementById("err-tx-charts-wrap");
    var txChartsGrid = document.getElementById("err-tx-charts-grid");
    var txChartsCount = document.getElementById("err-tx-charts-count");

    if (chartsSec) {
      if (!chartsGenerated) {
        chartsSec.hidden = true;
        if (reqChartsWrap) {
          reqChartsWrap.id = "err-req-charts-sub";
          reqChartsWrap.hidden = true;
        }
        if (txChartsWrap) txChartsWrap.hidden = true;
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
        if (txChartsWrap && txChartsGrid) {
          if (txCharts.length > 0) {
            txChartsGrid.innerHTML = txCharts.map(function (tc) {
              var src = tc.src || ("charts/errors/t" + tc.id + ".webp");
              var label = tc.title ? (tc.title + " (t" + tc.id + ")") : ("Transaction Errors (t" + tc.id + ")");
              return renderStripItem({
                id: "err-chart-" + tc.id,
                src: src,
                alt: label,
                title: label,
                backHref: "#err-row-" + tc.id
              });
            }).join("");
            if (txChartsCount) {
              txChartsCount.textContent = txCharts.length + (txCharts.length === 1 ? " chart" : " charts") + " · Click chart to enlarge";
            }
            txChartsWrap.hidden = false;
          } else {
            txChartsWrap.hidden = true;
          }
        }
      }
    }

    // 3. Overview by Error Message Table (group client-side if generator omitted overviewByMessage)
    var overviewRows = Array.isArray(raw.overviewByMessage) ? raw.overviewByMessage : null;
    if (!overviewRows) {
      var byMsg = {};
      incidents.forEach(function (inc) {
        var msg = inc.message || "(No error message)";
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

    var ovTbody = document.querySelector("#err-overview-table tbody");
    var ovTfoot = document.querySelector("#err-overview-table tfoot");
    var totalIncCount = overviewRows.reduce(function (s, r) { return s + (Number(r.count) || 0); }, 0);
    if (ovTbody) {
      if (!overviewRows.length) {
        ovTbody.innerHTML =
          '<tr><td colspan="5" class="col-text" style="text-align:center;padding:28px;color:var(--status-pass-text);font-weight:700;">' +
            "✓ Zero transaction errors recorded during this test run." +
          "</td></tr>";
        if (ovTfoot) ovTfoot.innerHTML = "";
      } else {
        ovTbody.innerHTML = overviewRows.map(function (r, idx) {
          var share = totalIncCount > 0 ? ((Number(r.count) || 0) * 100) / totalIncCount : 0;
          var msgHtml = r.chartId && chartsGenerated
            ? '<a href="#err-chart-' + esc(r.chartId) + '">' + esc(r.message) + "</a>"
            : esc(r.message);
          var rowAttr = (r.chartId && chartsGenerated) ? ' id="err-row-' + esc(r.chartId) + '"' : "";
          return (
            "<tr" + rowAttr + ">" +
              '<td class="col-idx">' + (idx + 1) + "</td>" +
              '<td class="col-text">' + msgHtml + "</td>" +
              '<td class="col-num cell-err" data-val="' + (Number(r.count) || 0) + '">' + fmtInt(r.count) + "</td>" +
              '<td class="col-num" data-val="' + share.toFixed(4) + '">' + fmtDec(share, 1) + "%</td>" +
              '<td class="col-text"><code>' + esc(r.exceptionClass || "—") + "</code></td>" +
            "</tr>"
          );
        }).join("");
        if (ovTfoot) {
          ovTfoot.innerHTML =
            "<tr>" +
              '<td class="col-idx">Σ</td>' +
              '<td class="col-text">Total Failures (' + fmtInt(overviewRows.length) + " signatures)</td>" +
              '<td class="col-num cell-err">' + fmtInt(totalIncCount) + "</td>" +
              '<td class="col-num">100.0%</td>' +
              '<td class="col-text"></td>' +
            "</tr>";
        }
      }
    }

    // 4. Custom Data Logs Section (custom_data_logs/)
    var logsSec = document.getElementById("sec-custom-logs");
    var logsTbody = document.querySelector("#err-custom-logs-table tbody");
    if (logsSec && logsTbody) {
      if (!customLogs.length) {
        logsSec.hidden = true;
      } else {
        logsSec.hidden = false;
        logsTbody.innerHTML = customLogs.map(function (lg, idx) {
          var path = lg.path || ("custom_data_logs/" + (lg.scope || "log"));
          var fname = path.split("/").pop();
          return (
            "<tr>" +
              '<td class="col-idx">' + (idx + 1) + "</td>" +
              '<td class="col-text">' + esc(fname) + "</td>" +
              '<td class="col-text"><span class="badge-tag">' + esc(lg.scope || "default") + "</span></td>" +
              '<td class="col-num">' + fmtBytes(lg.size || 0) + "</td>" +
              '<td class="col-text"><a href="' + esc(path) + '" class="btn-chip" target="_blank" rel="noopener">⬇ Open / Download <code>' + esc(fname) + "</code></a></td>" +
            "</tr>"
          );
        }).join("");
      }
    }

    // 5. Incident Forensics & Result Browser Dumps Table (traces collapsed by default, searchable via hidden="until-found")
    var incTable = document.getElementById("err-incidents-table");
    var incTbody = incTable && incTable.querySelector("tbody");
    var incTfoot = incTable && incTable.querySelector("tfoot");
    var countBadge = document.getElementById("err-visible-count");
    var filterInput = document.getElementById("err-details-filter");
    var toggleAllBtn = document.getElementById("err-toggle-all-traces");
    var cards = [];

    function setTraceExpanded(wrap, btn, open) {
      if (!wrap) return;
      if (open) {
        wrap.removeAttribute("hidden");
      } else {
        wrap.setAttribute("hidden", "until-found");
      }
      if (btn) {
        btn.setAttribute("aria-expanded", open ? "true" : "false");
        var caret = btn.querySelector(".incident-trace-caret");
        if (caret) caret.textContent = open ? "\u25BC" : "\u25B6";
      }
    }

    function syncToggleAllBtn() {
      if (!toggleAllBtn || !cards.length) return;
      var visibleCards = cards.filter(function (c) { return !c.el.hidden; });
      var targetCards = visibleCards.length ? visibleCards : cards;
      var allOpen = visibleCards.length > 0 && targetCards.every(function (c) {
        return !c.traceWrap || !c.traceWrap.hasAttribute("hidden");
      });
      toggleAllBtn.setAttribute("aria-expanded", allOpen ? "true" : "false");
      toggleAllBtn.textContent = allOpen ? "▲ Collapse All Traces" : "▼ Expand All Traces";
    }

    if (incTbody) {
      if (!incidents.length) {
        incTbody.innerHTML =
          '<tr><td colspan="6" class="col-text" style="text-align:center;padding:28px;color:var(--status-pass-text);font-weight:700;">' +
            "✓ No error incidents or stack traces recorded during this test run." +
          "</td></tr>";
        if (incTfoot) incTfoot.innerHTML = "";
        if (toggleAllBtn) toggleAllBtn.hidden = true;
        if (countBadge) countBadge.textContent = "0 incident groups";
      } else {
        var frag = document.createDocumentFragment();
        var totalDetailCount = 0;

        incidents.forEach(function (inc, idx) {
          var n = idx + 1;
          var tr = document.createElement("tr");
          var cnt = Number(inc.count) || 0;
          totalDetailCount += cnt;
          var exClass = inc.exceptionClass || extractExceptionClass(inc.trace, inc.message);
          var summaryLabel = inc.message || exClass || "Error";
          var dirs = Array.isArray(inc.directories) ? inc.directories : [];
          var dirsHtml = '<span style="color:var(--text-muted);">—</span>';

          if (dirs.length) {
            dirsHtml = '<div class="incident-dir-list">' + dirs.map(function (d) {
              var href = linkResultBrowsers ? resolveResultBrowserHref(resultsPrefix, d) : null;
              var item = href
                ? '<a href="' + esc(href) + '" target="_blank" rel="noopener">' + esc(d) + " ↗</a>"
                : "<code>" + esc(d) + "</code>";
              return '<div class="incident-dir-item">' + item + "</div>";
            }).join("") + "</div>";
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
          var overviewLinkHtml = (chartsGenerated && overviewChartId && String(overviewChartId) !== "0")
            ? '<div class="incident-chart-links"><a class="incident-overview-link" href="#err-chart-' + esc(overviewChartId) + '">&#8593; Error overview chart</a></div>'
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

          tr.innerHTML =
            '<td class="col-idx">' + n + "</td>" +
            '<td class="col-num cell-err incident-count-cell" data-val="' + cnt + '">' + fmtInt(cnt) + "</td>" +
            '<td class="col-text incident-testcase-cell" data-val="' + esc(inc.testCaseName || "") + '">' + txHtml + "</td>" +
            '<td class="col-text incident-action-cell" data-val="' + esc(inc.actionName || "") + '" data-copy="' + esc(actCopy) + '">' + actMainHtml + reqSubHtml + "</td>" +
            '<td class="col-text incident-dirs-cell" data-copy="' + esc(dirs.join(", ")) + '">' + dirsHtml + "</td>" +
            '<td class="col-text incident-info-cell" data-val="' + esc(summaryLabel) + '" data-copy="' + esc(summaryLabel) + '">' +
              '<div class="incident-trace-header">' +
                '<button type="button" class="incident-trace-toggle" data-toggle-trace="trace-wrap-' + n + '" aria-expanded="false" aria-controls="trace-wrap-' + n + '">' +
                  '<span class="incident-trace-caret" aria-hidden="true">&#9654;</span>' +
                  '<span class="incident-trace-summary">' + esc(summaryLabel) + "</span>" +
                "</button>" +
                '<button type="button" class="btn-chip incident-copy-btn" data-copy-trace="trace-pre-' + n + '" data-orig-label="📋 Copy Trace">📋 Copy Trace</button>' +
              "</div>" +
              '<div id="trace-wrap-' + n + '" class="incident-trace-wrap" data-trace-wrap hidden="until-found">' +
                '<pre id="trace-pre-' + n + '" class="incident-trace-pre">' + esc(inc.trace || inc.message || "") + "</pre>" +
              "</div>" +
              detailChartHtml +
              overviewLinkHtml +
            "</td>";

          frag.appendChild(tr);
          cards.push({
            el: tr,
            traceWrap: tr.querySelector("[data-trace-wrap]"),
            toggleBtn: tr.querySelector("[data-toggle-trace]"),
            blob: ((inc.testCaseName || "") + " " + (inc.actionName || "") + " " + (inc.requestName || "") + " " + dirs.join(" ") + " " + (inc.message || "") + " " + (inc.trace || "")).toLowerCase()
          });
        });

        incTbody.innerHTML = "";
        incTbody.appendChild(frag);
        if (incTfoot) {
          incTfoot.innerHTML =
            "<tr>" +
              '<td class="col-idx">Σ</td>' +
              '<td class="col-num cell-err">' + fmtInt(totalDetailCount) + "</td>" +
              '<td class="col-text">Total Incident Occurrences (' + fmtInt(incidents.length) + " groups)</td>" +
              '<td class="col-text"></td>' +
              '<td class="col-text"></td>' +
              '<td class="col-text"></td>' +
            "</tr>";
        }
        if (countBadge) {
          countBadge.textContent = fmtInt(cards.length) + " incident group(s)";
        }
        syncToggleAllBtn();
      }
    }

    if (filterInput && cards.length) {
      filterInput.addEventListener("input", function () {
        var q = filterInput.value.toLowerCase().trim();
        var vis = 0;
        cards.forEach(function (c) {
          var ok = !q || c.blob.indexOf(q) !== -1;
          c.el.hidden = !ok;
          if (ok) vis++;
        });
        if (incTbody) {
          var seq = 1;
          Array.prototype.forEach.call(incTbody.rows, function (row) {
            if (!row.hidden && row.cells[0]) {
              row.cells[0].textContent = String(seq++);
            }
          });
        }
        if (countBadge) {
          countBadge.textContent = vis + " / " + cards.length + " incident groups";
        }
        syncToggleAllBtn();
      });
    }

    if (toggleAllBtn && cards.length) {
      toggleAllBtn.addEventListener("click", function () {
        var anyClosed = cards.some(function (c) {
          return !c.el.hidden && c.traceWrap && c.traceWrap.hasAttribute("hidden");
        });
        var hasVisible = cards.some(function (c) { return !c.el.hidden; });
        cards.forEach(function (c) {
          if (hasVisible && c.el.hidden) return;
          setTraceExpanded(c.traceWrap, c.toggleBtn, anyClosed);
        });
        syncToggleAllBtn();
      });
    }

    // Keep toggle button UI synchronized when browser Ctrl+F reveals hidden="until-found" traces
    document.addEventListener("beforematch", function (e) {
      var wrapper = e.target.closest("[data-trace-wrap]");
      if (!wrapper) return;
      var btn = document.querySelector('[data-toggle-trace="' + wrapper.id + '"]');
      setTraceExpanded(wrapper, btn, true);
      syncToggleAllBtn();
    });

    if (!("onbeforematch" in HTMLElement.prototype)) {
      Array.prototype.forEach.call(document.querySelectorAll('[hidden="until-found"]'), function (el) {
        var btn = document.querySelector('[data-toggle-trace="' + el.id + '"]');
        setTraceExpanded(el, btn, true);
      });
      syncToggleAllBtn();
    }

    document.addEventListener("click", function (e) {
      var toggleBtn = e.target.closest("[data-toggle-trace]");
      if (toggleBtn) {
        var targetId = toggleBtn.getAttribute("data-toggle-trace");
        var wrap = document.getElementById(targetId);
        if (wrap) {
          setTraceExpanded(wrap, toggleBtn, wrap.hasAttribute("hidden"));
          syncToggleAllBtn();
        }
        return;
      }

      var copyBtn = e.target.closest("[data-copy-trace]");
      if (copyBtn) {
        var preId = copyBtn.getAttribute("data-copy-trace");
        var preEl = document.getElementById(preId);
        if (preEl && navigator.clipboard) {
          var orig = copyBtn.getAttribute("data-orig-label") || copyBtn.textContent;
          navigator.clipboard.writeText(preEl.textContent).then(function () {
            copyBtn.textContent = "✓ Copied Trace";
            setTimeout(function () { copyBtn.textContent = orig; }, 1500);
          });
        }
      }
    });
  });
})();
