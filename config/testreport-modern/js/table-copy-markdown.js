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
 * XLT Modern Load Report — Table Markdown Exporter & Static Table Sorter
 *
 * @fileoverview
 * Provides two cross-page table utilities via document-level event delegation:
 *   1. `[data-copy-table]` / `.btn-copy-table[data-table]`: Serializes visible table headers,
 *      visible `<tbody>` rows (skipping hidden rows and `.drawer-row` detail panels), and
 *      visible `<tfoot>` summary rows into GitHub-Flavored Markdown (`window.copyTableAsMarkdown`).
 *   2. `table.js-static-sortable`: Client-side column sorting for static/rendered tables that
 *      are not managed by `workbench.js` (e.g., Network, Scorecard, Configuration, Errors).
 *
 * Consumes:
 * - `window.XLT_UI.collator` for natural numeric-aware lexical comparison.
 * - Optional cell attribute `data-copy` (raw unformatted export value, e.g., numeric TTFB without
 *   waterfall markup) and `data-val` (raw numeric sort key).
 * - Optional `#table-sort-status` `aria-live="polite"` element for screen-reader announcements.
 */
(function () {
  "use strict";

  var collator = window.XLT_UI.collator;

  /**
   * Extracts clean cell text for Markdown export, preferring an explicit `data-copy` attribute,
   * stripping sort indicator glyphs (`⇅▲▼`), collapsing whitespace, and escaping pipe characters.
   *
   * @param {HTMLTableCellElement} cell Table header or body cell.
   * @returns {string} Escaped Markdown cell string.
   */
  function cellText(cell) {
    return (cell.getAttribute("data-copy") || cell.textContent || "")
      .replace(/[⇅▲▼]/g, "")
      .replace(/\s+/g, " ")
      .trim()
      .replace(/\|/g, "\\|");
  }

  /**
   * Flattens a multi-row `<thead>` containing `rowspan` / `colspan` group headers into 1D
   * arrays of column titles and Markdown alignment specifiers using a 2D occupancy grid.
   *
   * @param {HTMLTableSectionElement} thead Table `<thead>` element.
   * @returns {{ headers: Array<string>, aligns: Array<string> }} Column titles and alignments.
   */
  function expandHeaderLeafMeta(thead) {
    var rows = Array.prototype.slice.call(thead.rows);
    if (!rows.length) return { headers: [], aligns: [] };
    var grid = rows.map(function () { return []; });
    var leafCells = [];
    rows.forEach(function (tr, rIdx) {
      var cIdx = 0;
      Array.prototype.forEach.call(tr.cells, function (th) {
        while (grid[rIdx][cIdx] !== undefined) cIdx++;
        var rs = Number(th.getAttribute("rowspan") || 1);
        var cs = Number(th.getAttribute("colspan") || 1);
        var txt = cellText(th);
        for (var dr = 0; dr < rs; dr++) {
          for (var dc = 0; dc < cs; dc++) {
            if (!grid[rIdx + dr]) grid[rIdx + dr] = [];
            grid[rIdx + dr][cIdx + dc] = dr === 0 ? txt : "";
          }
        }
        if (cs === 1 && rIdx + rs === rows.length) {
          leafCells[cIdx] = th;
        }
        cIdx += cs;
      });
    });
    var colCount = grid[0].length;
    var headers = [];
    var aligns = [];
    for (var c = 0; c < colCount; c++) {
      var parts = [];
      for (var r = 0; r < grid.length; r++) {
        var val = grid[r][c];
        // Avoid duplicating a rowspan label repeated down the grid
        if (val && parts[parts.length - 1] !== val) parts.push(val);
      }
      headers.push(parts.join(" "));
      var leaf = leafCells[c];
      if (leaf && leaf.classList.contains("col-idx")) {
        aligns.push(":---:");
      } else if (leaf && (leaf.classList.contains("col-text") || leaf.classList.contains("col-labels"))) {
        aligns.push(":---");
      } else if (leaf && leaf.classList.contains("col-num")) {
        aligns.push("---:");
      } else {
        aligns.push(c === 0 ? ":---" : "---:");
      }
    }
    return { headers: headers, aligns: aligns };
  }

  /**
   * Serializes a table's expanded header, visible body rows, and footer into a GFM Markdown table.
   *
   * @param {HTMLTableElement} table Source HTML table.
   * @returns {string} Multi-line Markdown table string.
   */
  function tableToMarkdown(table) {
    var meta = table.tHead ? expandHeaderLeafMeta(table.tHead) : { headers: [], aligns: [] };
    var headers = meta.headers;
    var lines = [];
    if (headers.length) {
      lines.push("| " + headers.join(" | ") + " |");
      lines.push("| " + meta.aligns.join(" | ") + " |");
    }
    if (table.tBodies[0]) {
      Array.prototype.forEach.call(table.tBodies[0].rows, function (tr) {
        if (tr.hidden || tr.classList.contains("drawer-row") || tr.style.display === "none") return;
        var vals = Array.prototype.map.call(tr.cells, cellText);
        lines.push("| " + vals.join(" | ") + " |");
      });
    }
    if (table.tFoot) {
      Array.prototype.forEach.call(table.tFoot.rows, function (tr) {
        if (tr.hidden || tr.style.display === "none") return;
        var vals = Array.prototype.map.call(tr.cells, cellText);
        lines.push("| " + vals.join(" | ") + " |");
      });
    }
    return lines.join("\n");
  }

  var ICON_CHECK =
    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" ' +
    'stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
    '<polyline points="20 6 9 17 4 12"/></svg>';

  var origMarkupMap = new WeakMap();
  var origTitleMap = new WeakMap();
  var copyTimerMap = new WeakMap();
  var liveRegion = null;
  var lastAnnouncement = "";

  function announce(msg) {
    if (!liveRegion) {
      liveRegion = document.createElement("span");
      liveRegion.className = "sr-only";
      liveRegion.setAttribute("role", "status");
      liveRegion.setAttribute("aria-live", "polite");
      document.body.appendChild(liveRegion);
    }
    var out = msg;
    if (out && out === lastAnnouncement) out += "\u00A0";
    if (out) lastAnnouncement = out;
    liveRegion.textContent = "";
    requestAnimationFrame(function () {
      if (liveRegion) liveRegion.textContent = out;
    });
  }

  function legacyCopy(text) {
    var field = document.createElement("textarea");
    field.value = text;
    field.setAttribute("readonly", "");
    field.style.cssText = "position:fixed;inset-block-start:0;opacity:0;pointer-events:none;";
    document.body.appendChild(field);
    field.select();
    try {
      return document.execCommand("copy");
    } catch (_) {
      return false;
    } finally {
      field.remove();
    }
  }

  function reportCopyResult(btn, ok) {
    var msg = ok ? "Copied table as Markdown" : "Copy failed";
    if (btn) {
      if (!origMarkupMap.has(btn)) origMarkupMap.set(btn, btn.innerHTML);
      if (!origTitleMap.has(btn)) origTitleMap.set(btn, btn.getAttribute("title") || "");
      clearTimeout(copyTimerMap.get(btn));
      btn.removeAttribute("data-copied");
      btn.removeAttribute("data-failed");
      btn.classList.remove("copied");
      void btn.offsetWidth;
      if (ok) {
        btn.innerHTML = ICON_CHECK;
        btn.setAttribute("data-copied", "");
        btn.classList.add("copied");
        btn.title = "Copied Markdown";
      } else {
        btn.innerHTML = origMarkupMap.get(btn);
        btn.setAttribute("data-failed", "");
        btn.title = "Copy failed";
      }
      copyTimerMap.set(btn, setTimeout(function () {
        btn.innerHTML = origMarkupMap.get(btn);
        btn.title = origTitleMap.get(btn);
        btn.removeAttribute("data-copied");
        btn.removeAttribute("data-failed");
        btn.classList.remove("copied");
        announce("");
      }, 1600));
    }
    announce(msg);
  }

  /**
   * Exports a table as GFM Markdown to `navigator.clipboard` (with `execCommand` fallback)
   * and shows transient icon + live-region feedback.
   *
   * @param {HTMLTableElement|string} tableOrId Table element or its DOM `id`.
   * @param {HTMLButtonElement|null} [btn] Trigger button whose SVG icon briefly switches to a checkmark.
   */
  function copyTableAsMarkdown(tableOrId, btn) {
    var table = typeof tableOrId === "string" ? document.getElementById(tableOrId) : tableOrId;
    if (!table) return;
    var md = tableToMarkdown(table);
    if (navigator.clipboard && typeof navigator.clipboard.writeText === "function") {
      navigator.clipboard.writeText(md).then(
        function () { reportCopyResult(btn, true); },
        function () { reportCopyResult(btn, legacyCopy(md)); }
      );
    } else {
      reportCopyResult(btn, legacyCopy(md));
    }
  }

  window.copyTableAsMarkdown = copyTableAsMarkdown;

  // Delegated copy-to-Markdown click handler
  document.addEventListener("click", function (e) {
    var btn = e.target.closest("[data-copy-table], [data-table]");
    if (!btn) return;
    var targetId = btn.getAttribute("data-copy-table") || btn.getAttribute("data-table");
    copyTableAsMarkdown(targetId, btn);
  });

  // Delegated sorting for static tables (.js-static-sortable) using one-shot DocumentFragment append
  document.addEventListener("click", function (e) {
    var btn = e.target.closest("table.js-static-sortable thead button.th-sort-btn");
    if (!btn) return;
    var th = btn.closest("th");
    var table = btn.closest("table.js-static-sortable");
    if (!th || th.classList.contains("col-idx") || !table || !table.tBodies[0] || !table.tHead) return;
    var tbody = table.tBodies[0];
    var statusEl = document.getElementById("table-sort-status");

    var colIdx = th.hasAttribute("data-col-idx") ? Number(th.getAttribute("data-col-idx")) : th.cellIndex;
    var type = th.getAttribute("data-type") || (th.classList.contains("col-text") ? "text" : "number");
    var dir = th.hasAttribute("aria-sort")
      ? (th.getAttribute("aria-sort") === "ascending" ? "descending" : "ascending")
      : "ascending";
    Array.prototype.forEach.call(table.tHead.querySelectorAll("th[aria-sort]"), function (h) {
      h.removeAttribute("aria-sort");
    });
    th.setAttribute("aria-sort", dir);
    var factor = dir === "ascending" ? 1 : -1;
    var rows = Array.prototype.slice.call(tbody.rows).filter(function (r) {
      return !r.classList.contains("drawer-row");
    });
    var decorated = rows.map(function (r) {
      var cell = r.cells[colIdx];
      var raw = cell ? (cell.getAttribute("data-val") !== null ? cell.getAttribute("data-val") : cell.textContent.trim()) : "";
      return { row: r, val: type === "number" ? Number(String(raw).replace(/,/g, "")) || 0 : String(raw) };
    });
    decorated.sort(function (a, b) {
      return factor * (type === "number" ? (a.val - b.val) : collator.compare(a.val, b.val));
    });
    var sortFrag = document.createDocumentFragment();
    var seq = 1;
    decorated.forEach(function (d) {
      var idxCell = d.row.cells[0];
      if (idxCell && idxCell.classList.contains("col-idx") && !d.row.hidden && d.row.style.display !== "none") {
        idxCell.textContent = String(seq++);
      }
      sortFrag.appendChild(d.row);
    });
    tbody.appendChild(sortFrag);
    if (statusEl) {
      var labelSpan = btn.querySelector("span");
      statusEl.textContent = "Sorted by " + (labelSpan ? labelSpan.textContent.trim() : btn.textContent.trim()) + ", " + dir + ", " + rows.length + " rows";
    }
  });
})();
