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
 * XLT Modern Load Report — External Data Renderer (`external.html`)
 *
 * @fileoverview
 * Renders third-party / external infrastructure & APM metrics imported into the report.
 *
 * Consumes:
 * - `window.XLT_EXTERNAL`: `{ sections: Array<{ headline, description, tables, charts }> }`
 * - `window.XLT_UI`: `esc`, `fmtInt`
 *
 * Target DOM Containers:
 * - `#ext-kpi-grid` — top KPI strip (ingestion status, table count, chart count, correlation)
 * - `#ext-empty-section` — informational fallback when no external sources exist
 * - `#ext-sections-container` — dynamically generated cards with sortable `.js-static-sortable`
 *   tables, Markdown copy triggers, and `.zoomable-chart` 2x2 image grids
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;

  /**
   * Formats external metric table cell values cleanly (up to 3 fractional digits for numbers,
   * preserving non-numeric strings verbatim).
   *
   * @param {number|string|null|undefined} val Raw cell value.
   * @returns {string} Formatted cell display text.
   */
  function fmtNumber(val) {
    var num = Number(val);
    if (Number.isNaN(num)) return String(val == null ? "" : val);
    return num.toLocaleString("en-US", { maximumFractionDigits: 3 });
  }

  document.addEventListener("DOMContentLoaded", function () {
    var data = window.XLT_EXTERNAL || {};
    var sections = Array.isArray(data) ? data : (data.sections || []);
    var kpiGrid = document.getElementById("ext-kpi-grid");
    var emptySec = document.getElementById("ext-empty-section");
    var contentSec = document.getElementById("ext-sections-container");

    var totalTables = 0;
    var totalCharts = 0;
    sections.forEach(function (s) {
      totalTables += Array.isArray(s.tables) ? s.tables.length : 0;
      totalCharts += Array.isArray(s.charts) ? s.charts.length : 0;
    });

    // 1. Summary KPI strip (always rendered even when external sections are absent)
    if (kpiGrid) {
      var active = sections.length > 0;
      kpiGrid.innerHTML =
        '<article class="kpi-card">' +
          '<span class="kpi-label">External Data Ingestion</span>' +
          '<div class="kpi-val" style="color:' + (active ? "var(--status-pass-text)" : "var(--text-muted)") + ';">' + (active ? "Active" : "None") + "</div>" +
          '<div class="kpi-sub">' + (active ? fmtInt(sections.length) + " external report section(s) imported" : "No external metric sources configured for this run") + "</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">External Data Tables</span>' +
          '<div class="kpi-val">' + fmtInt(totalTables) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">tables</span></div>' +
          '<div class="kpi-sub">Imported tabular infrastructure &amp; APM summaries</div>' +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">External Charts</span>' +
          '<div class="kpi-val">' + fmtInt(totalCharts) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">charts</span></div>' +
          '<div class="kpi-sub">Time-synchronized WebP telemetry plots</div>' +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Report Correlation</span>' +
          '<div class="kpi-val">Time-Synced</div>' +
          '<div class="kpi-sub">Aligned to load test measurement window</div>' +
        "</article>";
    }

    if (!sections.length) {
      if (emptySec) emptySec.hidden = false;
      if (contentSec) contentSec.innerHTML = "";
      return;
    }

    if (emptySec) emptySec.hidden = true;
    if (!contentSec) return;

    // 2. Render each external section card with sortable tables and lightbox-enabled charts
    contentSec.innerHTML = sections.map(function (sec, sIdx) {
      var tablesHtml = (sec.tables || []).map(function (tbl, tIdx) {
        var tableId = "ext-table-" + sIdx + "-" + tIdx;
        var headers = Array.isArray(tbl.headers) ? tbl.headers : [];
        var rows = Array.isArray(tbl.rows) ? tbl.rows : [];
        var theadHtml = headers.length
          ? "<thead><tr>" + headers.map(function (h, cIdx) {
              var firstRowCell = rows[0] && rows[0][cIdx];
              // Treat column 0 as label text unless the cell object explicitly marks it numeric
              var isNum = firstRowCell && typeof firstRowCell === "object" ? Boolean(firstRowCell.numeric) : cIdx > 0;
              return (
                '<th scope="col" class="' + (isNum ? "col-num" : "col-text") + '" data-col-idx="' + cIdx + '" data-type="' + (isNum ? "number" : "text") + '">' +
                  '<button type="button" class="th-sort-btn"><span>' + esc(h) + '</span><span class="sort-icon"></span></button>' +
                "</th>"
              );
            }).join("") + "</tr></thead>"
          : "";
        var tbodyHtml = "<tbody>" + rows.map(function (row) {
          return "<tr>" + (row || []).map(function (cell) {
            var isObj = cell !== null && typeof cell === "object";
            var rawVal = isObj ? cell.value : cell;
            var isNum = isObj ? Boolean(cell.numeric) : typeof rawVal === "number";
            if (isNum) {
              return '<td class="col-num" data-val="' + esc(rawVal) + '">' + esc(fmtNumber(rawVal)) + "</td>";
            }
            return '<td class="col-text">' + esc(rawVal) + "</td>";
          }).join("") + "</tr>";
        }).join("") + "</tbody>";

        return (
          '<div style="margin-bottom:14px;">' +
            '<div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px;">' +
              '<h3 style="font-size:13px;font-weight:800;margin:0;">' + esc(tbl.title || "Metrics") + "</h3>" +
              '<button type="button" class="btn-chip" data-copy-table="' + tableId + '">📋 Copy as Markdown</button>' +
            "</div>" +
            '<div class="table-scroll"><table class="data-table js-static-sortable" id="' + tableId + '">' + theadHtml + tbodyHtml + "</table></div>" +
          "</div>"
        );
      }).join("");

      var chartsHtml = (sec.charts || []).length
        ? '<div class="charts-2x2" style="margin-top:12px;">' +
            sec.charts.map(function (ch) {
              var src = typeof ch === "string" ? ch : ch.src;
              var title = (typeof ch === "object" && ch.title) || sec.headline || "External Chart";
              return (
                '<div class="chart-panel">' +
                  '<div class="chart-panel-header"><strong>' + esc(title) + "</strong></div>" +
                  '<div class="chart-panel-body chart-canvas-light">' +
                    '<img class="zoomable-chart" src="' + esc(src) + '" alt="' + esc(title) + '" data-chart-title="' + esc(title) + '" loading="lazy" decoding="async" />' +
                  "</div>" +
                "</div>"
              );
            }).join("") +
          "</div>"
        : "";

      return (
        '<section class="card" style="margin-bottom:16px;">' +
          '<div class="card-header">' +
            "<div>" +
              '<div class="card-title">' + esc(sec.headline || "External Data Section") + "</div>" +
              (sec.description ? '<p class="card-subtitle">' + esc(sec.description) + "</p>" : "") +
            "</div>" +
          "</div>" +
          '<div class="card-body">' + tablesHtml + chartsHtml + "</div>" +
        "</section>"
      );
    }).join("");
  });
})();
