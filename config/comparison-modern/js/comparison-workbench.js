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
 * XLT Modern Comparison & Trend Report — Generic Timer Comparison Workbench & Directory
 * (`window.XltComparisonWorkbench`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI || {};
  var CMP = window.XLT_CMP || {};
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var fmtDec = UI.fmtDec;
  var fmtBytes = UI.fmtBytes;

  var ICON_PIN =
    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
    '<path d="M12 17v5"/>' +
    '<path d="M9 10.76a2 2 0 0 1-1.11 1.79l-1.78.9A2 2 0 0 0 5 15.24V16a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-.76a2 2 0 0 0-1.11-1.79l-1.78-.9A2 2 0 0 1 15 10.76V7a1 1 0 0 1 1-1 2 2 0 0 0 0-4H8a2 2 0 0 0 0 4 1 1 0 0 1 1 1z"/>' +
    "</svg>";

  var ICON_PIN_OFF =
    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
    '<line x1="2" y1="2" x2="22" y2="22"/>' +
    '<path d="M12 17v5"/>' +
    '<path d="M9 9v1.76a2 2 0 0 1-1.11 1.79l-1.78.9A2 2 0 0 0 5 15.24V16a1 1 0 0 0 1 1h11"/>' +
    '<path d="M15 9.34V7a1 1 0 0 1 1-1 2 2 0 0 0 0-4H7.89"/>' +
    "</svg>";

  function init(config) {
    config = config || {};
    var items = Array.isArray(config.items) ? config.items : [];
    var meta = window.XLT_META || {};
    var caps = meta.capabilities || {};
    var pcts = UI.getConfiguredPercentiles(items);
    if (!pcts.length && !(meta.testReportConfig && Array.isArray(meta.testReportConfig.runtimePercentiles))) {
      var seenPct = [];
      items.forEach(function (it) {
        (it.runs || []).forEach(function (r) {
          if (r && r.percentiles && typeof r.percentiles === "object") {
            Object.keys(r.percentiles).forEach(function (k) {
              var s = String(k).replace(/^p/i, "");
              if (s && seenPct.indexOf(s) === -1) seenPct.push(s);
            });
          }
        });
      });
      pcts = seenPct;
    }
    var repPctKey = UI.pickRepresentativePercentile(pcts);
    var polarity = config.neutralPolarity ? "neutral" : "lower";

    var wbScope = document.getElementById("wb-scope");
    var emptySec = document.getElementById("empty-section");
    if (!items.length) {
      if (wbScope) wbScope.hidden = true;
      if (emptySec) emptySec.hidden = false;
      return;
    }
    if (wbScope) wbScope.hidden = false;
    if (emptySec) emptySec.hidden = true;

    var hasLabels = items.some(function (it) { return Array.isArray(it.labels) && it.labels.length > 0; });
    var hasMethods = Boolean(config.showMethod) && items.some(function (it) { return Boolean(it.method); });
    var hasApdex = Boolean(config.showApdex) && items.some(function (it) {
      return (it.runs || []).some(function (r) { return r && r.apdex != null; });
    });
    var hasEvents = Boolean(config.showEvents) && caps.hasCounts !== false;
    var showErrors = config.showErrors !== false;
    var showCounts = caps.hasCounts !== false;

    var aggItem = null;
    var bodyItems = [];
    items.forEach(function (it) {
      if (it.isAggregate && !aggItem) aggItem = it;
      else bodyItems.push(it);
    });

    // URL query param item selection
    var urlParams = new URLSearchParams(window.location ? window.location.search : "");
    var requestedKey = urlParams.get("item") || "";

    var runs = CMP.getRuns();
    var defaultTab = runs.length >= 3 ? "trajectory" : "profile";

    var uiState = {
      selectedKey: requestedKey || (items[0] ? (items[0].key || items[0].name) : ""),
      activeTab: defaultTab,
      sortCol: "name",
      sortDir: "asc",
      filterQuery: "",
      statusFilter: "all",
      labelFilter: "ALL",
      activeList: bodyItems.slice(),
      collapsed: false,
      pinned: false
    };

    var colDefs = buildColumnDefs({
      hasLabels: hasLabels,
      hasMethods: hasMethods,
      showCounts: showCounts,
      showErrors: showErrors,
      hasEvents: hasEvents,
      hasApdex: hasApdex,
      pcts: pcts,
      polarity: polarity
    });

    var rowMap = new Map();       // key -> HTMLTableRowElement
    var openDrawers = new Map();  // key -> HTMLTableRowElement (.drawer-row)
    var echartInst = null;
    var echartRo = null;

    var dirTable = document.getElementById("dir-table");
    var tbody = dirTable ? dirTable.querySelector("tbody") : null;
    var thead = dirTable ? dirTable.querySelector("thead") : null;
    var tfoot = dirTable ? dirTable.querySelector("tfoot") : null;

    buildTableHeader();
    buildToolbar();
    buildLabelChips();
    buildAllRowElements();
    renderWorkbenchTabs();
    bindWorkbenchEvents();
    applyFilterAndSort(true);

    // Select initial item (or requested key)
    if (uiState.selectedKey) {
      var found = items.find(function (x) { return (x.key || x.name) === uiState.selectedKey; });
      if (!found && items.length) uiState.selectedKey = items[0].key || items[0].name;
    }
    selectItem(uiState.selectedKey, false);

    window.addEventListener("xlt-cmp-change", function () {
      refreshAllRowCells();
      applyFilterAndSort(false);
      updateWorkbench();
      refreshOpenDrawers();
    });
    window.addEventListener("xlt-theme-changed", function () {
      renderWorkbenchChart();
    });

    // =====================================================================
    // Column Definitions
    // =====================================================================

    function buildColumnDefs(o) {
      var cols = [
        { id: "idx", label: "#", cls: "col-idx", sortable: false },
        { id: "name", label: config.nameLabel || "Name", cls: "col-text", sortable: true, group: "Identity" }
      ];
      if (o.hasMethods) cols.push({ id: "method", label: "Method", cls: "col-cat", sortable: true, group: "Identity" });
      if (o.hasLabels) cols.push({ id: "labels", label: "Labels", cls: "col-labels", sortable: true, group: "Identity" });

      if (o.showCounts) cols.push({ id: "count", label: "Count", cls: "col-num cg-alt", sortable: true, group: "Volume", fmt: fmtInt, unit: "", polarity: "higher", deadband: 1.0 });
      cols.push({ id: "rate", label: "1/s", cls: "col-num" + (o.showCounts ? " cg-alt" : " cg-alt"), sortable: true, group: "Volume", fmt: function (n) { return fmtDec(n, 2); }, unit: "", polarity: "higher", deadband: 1.0 });
      cols.push({ id: "cph", label: "1/h", cls: "col-num cg-alt", sortable: true, group: "Volume", fmt: fmtInt, unit: "", polarity: "higher", deadband: 1.0 });

      if (o.showErrors) {
        cols.push({ id: "errors", label: "Errors", cls: "col-num", sortable: true, group: "Reliability", fmt: fmtInt, unit: "", polarity: "lower", deadband: 0, isError: true, absPill: true });
      }
      if (o.hasEvents) {
        cols.push({ id: "events", label: "Events", cls: "col-num", sortable: true, group: "Reliability", fmt: fmtInt, unit: "", polarity: "warn", deadband: 0, isEvent: true, absPill: true });
      }
      if (o.hasApdex) {
        cols.push({ id: "apdex", label: "Apdex", cls: "col-num", sortable: true, group: "Reliability", fmt: function (n) { return fmtDec(n, 2); }, unit: "", polarity: "higher", deadband: 1.0 });
      }

      var msUnit = config.valueUnit !== undefined ? config.valueUnit : "ms";
      var msFmt = config.valueFmt || fmtInt;
      cols.push({ id: "mean", label: "Mean", cls: "col-num cg-alt", sortable: true, group: msUnit ? "Runtime [" + msUnit + "]" : "Values", fmt: msFmt, unit: "", polarity: o.polarity });
      cols.push({ id: "min", label: "Min", cls: "col-num cg-alt col-extra", sortable: true, group: msUnit ? "Runtime [" + msUnit + "]" : "Values", fmt: msFmt, unit: "", polarity: o.polarity });
      cols.push({ id: "max", label: "Max", cls: "col-num cg-alt col-extra", sortable: true, group: msUnit ? "Runtime [" + msUnit + "]" : "Values", fmt: msFmt, unit: "", polarity: o.polarity });
      cols.push({ id: "median", label: "Median", cls: "col-num cg-alt col-extra", sortable: true, group: msUnit ? "Runtime [" + msUnit + "]" : "Values", fmt: msFmt, unit: "", polarity: o.polarity });

      o.pcts.forEach(function (p) {
        cols.push({
          id: "pct:" + p,
          pctKey: p,
          label: "P" + p,
          cls: "col-num",
          sortable: true,
          group: "Percentiles [" + (msUnit || "val") + "]",
          fmt: msFmt,
          unit: "",
          polarity: o.polarity
        });
      });

      return cols;
    }

    function buildTableHeader() {
      if (!thead) return;
      // Group spans
      var groups = [];
      colDefs.forEach(function (col) {
        if (col.id === "idx") return;
        var gName = col.group || "";
        var last = groups[groups.length - 1];
        if (last && last.name === gName) last.span++;
        else groups.push({ name: gName, span: 1, cls: (col.cls || "").indexOf("cg-alt") !== -1 ? "cg-alt" : "" });
      });

      var gRow = '<tr class="group-header-row"><th></th>';
      groups.forEach(function (g) {
        gRow += '<th colspan="' + g.span + '"' + (g.cls ? ' class="' + g.cls + '"' : "") + ">" + esc(g.name) + "</th>";
      });
      gRow += "</tr>";

      var cRow = "<tr>";
      colDefs.forEach(function (col) {
        var sortAttr = (col.sortable && uiState.sortCol === col.id)
          ? ' aria-sort="' + (uiState.sortDir === "asc" ? "ascending" : "descending") + '"'
          : "";
        if (!col.sortable) {
          cRow += '<th scope="col" class="' + col.cls + '">' + esc(col.label) + "</th>";
        } else {
          cRow +=
            '<th scope="col" class="' + col.cls + '" data-sort-col="' + esc(col.id) + '"' + sortAttr + ">" +
              '<button type="button" class="th-sort-btn">' +
                "<span>" + esc(col.label) + "</span>" +
                '<span class="sort-icon" aria-hidden="true"></span>' +
              "</button>" +
            "</th>";
        }
      });
      cRow += "</tr>";
      thead.innerHTML = gRow + cRow;

      thead.addEventListener("click", function (e) {
        var btn = e.target.closest("th[data-sort-col] button.th-sort-btn");
        if (!btn) return;
        var th = btn.closest("th");
        var colId = th.getAttribute("data-sort-col");
        if (!colId) return;

        if (uiState.sortCol === colId) {
          uiState.sortDir = uiState.sortDir === "asc" ? "desc" : "asc";
        } else {
          uiState.sortCol = colId;
          uiState.sortDir = (colId === "name" || colId === "method" || colId === "labels") ? "asc" : "desc";
        }
        Array.prototype.forEach.call(thead.querySelectorAll("th[aria-sort]"), function (h) {
          h.removeAttribute("aria-sort");
        });
        th.setAttribute("aria-sort", uiState.sortDir === "asc" ? "ascending" : "descending");

        applyFilterAndSort(false, true);
        var statusEl = document.getElementById("table-sort-status");
        if (statusEl) {
          var sortedCount = uiState.activeList.filter(function (x) { return !x.isAggregate; }).length;
          statusEl.textContent = "Sorted by " + (th.textContent || "").trim() + ", " +
            (uiState.sortDir === "asc" ? "ascending" : "descending") + ", " + fmtInt(sortedCount) + " items";
        }
      });
    }

    // =====================================================================
    // Toolbar Controls (View Mode, Sort Basis, Status Filter)
    // =====================================================================

    function buildToolbar() {
      var bar = document.getElementById("cmp-table-controls");
      if (!bar) return;
      var st = CMP.getState();

      bar.innerHTML =
        '<div role="group" class="cmp-toggle-group" aria-label="Cell display mode">' +
          '<button type="button" class="cmp-toggle-btn" data-cmp-view="inline" aria-pressed="' + (st.viewMode === "inline") + '">A → B (Δ%)</button>' +
          '<button type="button" class="cmp-toggle-btn" data-cmp-view="pct" aria-pressed="' + (st.viewMode === "pct") + '">Δ% Only</button>' +
        "</div>" +
        '<span class="cmp-toolbar-sep" role="separator" aria-orientation="vertical"></span>' +
        '<div role="group" class="cmp-toggle-group" aria-label="Numeric sort basis">' +
          '<button type="button" class="cmp-toggle-btn" data-sort-basis="rel" aria-pressed="' + (st.sortBasis === "rel") + '">Sort: Δ%</button>' +
          '<button type="button" class="cmp-toggle-btn" data-sort-basis="abs" aria-pressed="' + (st.sortBasis === "abs") + '">Δ Abs</button>' +
          '<button type="button" class="cmp-toggle-btn" data-sort-basis="target" aria-pressed="' + (st.sortBasis === "target") + '">Target (B)</button>' +
        "</div>" +
        '<span class="cmp-toolbar-sep" role="separator" aria-orientation="vertical"></span>' +
        '<div role="group" class="cmp-toggle-group" aria-label="Status filter">' +
          '<button type="button" class="cmp-toggle-btn" data-status-filter="all" aria-pressed="true">All</button>' +
          '<button type="button" class="cmp-toggle-btn" data-status-filter="regressed" aria-pressed="false">Regressed</button>' +
          '<button type="button" class="cmp-toggle-btn" data-status-filter="improved" aria-pressed="false">Improved</button>' +
          '<button type="button" class="cmp-toggle-btn" data-status-filter="changed" aria-pressed="false">Added / Removed</button>' +
        "</div>";

      bar.addEventListener("click", function (e) {
        var viewBtn = e.target.closest("[data-cmp-view]");
        if (viewBtn) {
          var mode = viewBtn.getAttribute("data-cmp-view");
          Array.prototype.forEach.call(bar.querySelectorAll("[data-cmp-view]"), function (b) {
            b.setAttribute("aria-pressed", b === viewBtn ? "true" : "false");
          });
          CMP.setState({ viewMode: mode });
          return;
        }
        var basisBtn = e.target.closest("[data-sort-basis]");
        if (basisBtn) {
          var basis = basisBtn.getAttribute("data-sort-basis");
          Array.prototype.forEach.call(bar.querySelectorAll("[data-sort-basis]"), function (b) {
            b.setAttribute("aria-pressed", b === basisBtn ? "true" : "false");
          });
          CMP.setState({ sortBasis: basis });
          return;
        }
        var statusBtn = e.target.closest("[data-status-filter]");
        if (statusBtn) {
          uiState.statusFilter = statusBtn.getAttribute("data-status-filter") || "all";
          Array.prototype.forEach.call(bar.querySelectorAll("[data-status-filter]"), function (b) {
            b.setAttribute("aria-pressed", b === statusBtn ? "true" : "false");
          });
          applyFilterAndSort(true);
        }
      });

      if (CMP.installToolbarFocusgroupFallback) CMP.installToolbarFocusgroupFallback(bar);

      var filterInput = document.getElementById("dir-filter-input");
      if (filterInput) {
        filterInput.addEventListener("input", function () {
          uiState.filterQuery = filterInput.value || "";
          applyFilterAndSort(true);
        });
      }
    }

    function buildLabelChips() {
      var box = document.getElementById("dir-category-chips");
      if (!box) return;
      if (!hasLabels) {
        box.hidden = true;
        box.innerHTML = "";
        return;
      }
      var counts = {};
      bodyItems.forEach(function (it) {
        (it.labels || []).forEach(function (lbl) {
          counts[lbl] = (counts[lbl] || 0) + 1;
        });
      });
      var names = Object.keys(counts).sort(function (a, b) { return UI.collator.compare(a, b); });
      if (!names.length) {
        box.hidden = true;
        return;
      }
      box.hidden = false;
      var html = '<button type="button" class="btn-chip active" data-lbl="ALL">All (' + bodyItems.length + ")</button>";
      names.forEach(function (lbl) {
        html += '<button type="button" class="btn-chip" data-lbl="' + esc(lbl) + '">' + esc(lbl) + " (" + counts[lbl] + ")</button>";
      });
      box.innerHTML = html;

      box.addEventListener("click", function (e) {
        var btn = e.target.closest("[data-lbl]");
        if (!btn) return;
        uiState.labelFilter = btn.getAttribute("data-lbl") || "ALL";
        Array.prototype.forEach.call(box.querySelectorAll("[data-lbl]"), function (b) {
          if (b === btn) b.classList.add("active");
          else b.classList.remove("active");
        });
        applyFilterAndSort(true);
      });
    }

    // =====================================================================
    // Row Creation, Cell Rendering & Identity-Preserving Sort
    // =====================================================================

    function readItemMetric(item, runIdx, colId) {
      var run = item && item.runs && item.runs[runIdx];
      if (!run) return null;
      if (colId.indexOf("pct:") === 0) {
        return UI.getPercentile(run, colId.slice(4));
      }
      var v = run[colId];
      return v === undefined ? null : v;
    }

    function computeRowStatus(item, baseIdx, targetIdx) {
      var rA = item.runs && item.runs[baseIdx];
      var rB = item.runs && item.runs[targetIdx];
      if (!rA && rB) return "added";
      if (rA && !rB) return "removed";
      if (!rA && !rB) return "empty";

      var dMean = CMP.calcDelta(rA.mean, rB.mean);
      var dErr = CMP.calcDelta(rA.errors, rB.errors);
      var errUp = dErr.status === "both" && dErr.abs > 0;
      var errDown = dErr.status === "both" && dErr.abs < 0;

      if (polarity === "neutral") return "flat";
      var meanUp = dMean.status === "both" && !dMean.isInf && dMean.rel >= 1.0;
      var meanDown = dMean.status === "both" && !dMean.isInf && dMean.rel <= -1.0;
      if (errUp || (meanUp && !errDown)) return "regressed";
      if (meanDown || errDown) return "improved";
      return "flat";
    }

    function renderLabelsCell(labels) {
      if (!labels || !labels.length) return '<span class="cell-muted">—</span>';
      if (labels.length <= 2) {
        return '<div class="labels-cell-wrap">' + labels.map(function (l) {
          return '<span class="label-chip">' + esc(l) + "</span>";
        }).join("") + "</div>";
      }
      var firstTwo = labels.slice(0, 2).map(function (l) {
        return '<span class="label-chip">' + esc(l) + "</span>";
      }).join("");
      var pop = labels.slice(2).map(function (l) {
        return '<span class="label-chip">' + esc(l) + "</span>";
      }).join("");
      return (
        '<div class="labels-cell-wrap">' +
          firstTwo +
          '<span class="label-more-wrap">' +
            '<button type="button" class="label-chip-more" aria-label="' + labels.length + ' labels">+' + (labels.length - 2) + "</button>" +
            '<span class="label-chip-popover" role="tooltip">' + pop + "</span>" +
          "</span>" +
        "</div>"
      );
    }

    function buildRowTr(item, isAgg) {
      var tr = document.createElement("tr");
      var key = item.key || item.name;
      tr.className = "wb-row" + (isAgg ? " is-aggregate-row" : "");
      tr.setAttribute("data-item-key", key);
      tr.tabIndex = -1;
      colDefs.forEach(function (col) {
        var td = document.createElement("td");
        td.className = col.cls || "";
        tr.appendChild(td);
      });
      populateRowCells(tr, item, isAgg);
      return tr;
    }

    function populateRowCells(tr, item, isAgg) {
      var st = CMP.getState();
      var key = item.key || item.name;
      var isSelected = uiState.selectedKey === key;
      if (isSelected) tr.classList.add("is-selected");
      else tr.classList.remove("is-selected");

      colDefs.forEach(function (col, idx) {
        var td = tr.cells[idx];
        if (!td) return;

        if (col.id === "idx") {
          td.textContent = isAgg ? "Σ" : "";
          return;
        }
        if (col.id === "name") {
          var subText = (item.sampleUrls && item.sampleUrls[0]) || item.description || "";
          var isExpanded = openDrawers.has(key);
          td.innerHTML =
            '<div class="name-cell-wrap">' +
              '<button type="button" class="drawer-btn" aria-expanded="' + (isExpanded ? "true" : "false") +
              '" aria-label="Toggle details for ' + esc(item.name) + '" title="Toggle full comparison &amp; run history">' +
              (isExpanded ? "▾" : "▸") + "</button>" +
              '<div class="name-cell-stack">' +
                '<span class="name-cell-text" title="' + esc(item.name) + '">' + esc(item.name) + "</span>" +
                (subText ? '<span class="name-cell-sub" title="' + esc(subText) + '">' + esc(subText) + "</span>" : "") +
              "</div>" +
            "</div>";
          td.setAttribute("data-copy", item.name);
          return;
        }
        if (col.id === "method") {
          if (item.method) {
            td.innerHTML = '<span class="wb-badge method-' + esc(item.method.toLowerCase()) + '">' + esc(item.method) + "</span>";
            td.setAttribute("data-copy", item.method);
          } else {
            td.innerHTML = '<span class="cell-muted">—</span>';
            td.setAttribute("data-copy", "—");
          }
          return;
        }
        if (col.id === "labels") {
          td.innerHTML = renderLabelsCell(item.labels);
          td.setAttribute("data-copy", (item.labels || []).join(", ") || "—");
          return;
        }

        var vA = readItemMetric(item, st.baseIdx, col.id);
        var vB = readItemMetric(item, st.targetIdx, col.id);
        var d = CMP.calcDelta(vA, vB);
        var cData = CMP.buildComparisonCellData(d, col.fmt, col.unit, col.polarity, st.viewMode, {
          deadband: col.deadband,
          isEvent: col.isEvent,
          isError: col.isError,
          absPill: col.absPill
        });
        td.innerHTML = cData.html;
        if (cData.title) td.title = cData.title;
        else td.removeAttribute("title");
        td.setAttribute("data-copy", cData.copyText);
      });
    }

    function buildAllRowElements() {
      if (!tbody) return;
      bodyItems.forEach(function (it) {
        var tr = buildRowTr(it, false);
        rowMap.set(it.key || it.name, tr);
        tbody.appendChild(tr);
      });

      // Delegated row click & drawer toggle
      tbody.addEventListener("click", function (e) {
        var dBtn = e.target.closest("button.drawer-btn");
        var tr = e.target.closest("tr.wb-row");
        if (!tr) return;
        var key = tr.getAttribute("data-item-key");
        if (!key) return;
        if (dBtn) {
          e.stopPropagation();
          toggleRowDrawer(key);
        } else if (!e.target.closest(".label-more-wrap")) {
          selectItem(key, false);
        }
      });
    }

    function refreshAllRowCells() {
      bodyItems.forEach(function (it) {
        var tr = rowMap.get(it.key || it.name);
        if (tr) populateRowCells(tr, it, false);
      });
    }

    function buildRowSearchRecord(item, st) {
      var rB = (item.runs && item.runs[st.targetIdx]) || (item.runs && item.runs[st.baseIdx]) || {};
      var rA = (item.runs && item.runs[st.baseIdx]) || {};
      var dMean = CMP.calcDelta(rA.mean, rB.mean);
      var metricsObj = {
        count: rB.count,
        rate: rB.rate,
        cph: rB.cph,
        mean: rB.mean,
        min: rB.min,
        max: rB.max,
        median: rB.median,
        errors: rB.errors,
        events: rB.events,
        dmean: dMean.status === "both" ? dMean.rel : null,
        percentiles: rB.percentiles
      };
      var textParts = [item.name || "", item.description || "", item.method || ""]
        .concat(item.labels || [])
        .concat(item.sampleUrls || []);
      return { text: textParts.join(" "), metrics: metricsObj };
    }

    function getSortValue(item, colId, st) {
      if (colId === "name") return item.name || "";
      if (colId === "method") return item.method || "";
      if (colId === "labels") return (item.labels || []).join(" ");

      var vA = readItemMetric(item, st.baseIdx, colId);
      var vB = readItemMetric(item, st.targetIdx, colId);
      var d = CMP.calcDelta(vA, vB);
      if (st.sortBasis === "target") {
        return d.valB !== null ? d.valB : -Infinity;
      }
      if (st.sortBasis === "abs") {
        return d.status === "both" ? d.abs : (d.status === "added" ? 1e14 : (d.status === "removed" ? -1e14 : -Infinity));
      }
      // "rel"
      if (d.status === "both") {
        if (d.isInf) return d.abs > 0 ? 1e12 : -1e12;
        return d.rel;
      }
      if (d.status === "added") return 1e13;
      if (d.status === "removed") return -1e13;
      return -Infinity;
    }

    function applyFilterAndSort(updatePager, skipAlign) {
      var st = CMP.getState();
      var q = uiState.filterQuery.trim();
      var hasQuery = Boolean(q);
      var hasLabel = hasLabels && uiState.labelFilter !== "ALL";
      var hasStatus = uiState.statusFilter !== "all";
      var isFiltered = hasQuery || hasLabel || hasStatus;

      var parsedQuery = hasQuery && UI.parseSearchQuery ? UI.parseSearchQuery(q) : null;

      var matched = [];
      var hiddenKeys = new Set();

      bodyItems.forEach(function (it) {
        var key = it.key || it.name;
        var okLabel = !hasLabel || (it.labels && it.labels.indexOf(uiState.labelFilter) !== -1);
        var okStatus = true;
        if (hasStatus) {
          var status = computeRowStatus(it, st.baseIdx, st.targetIdx);
          if (uiState.statusFilter === "changed") okStatus = status === "added" || status === "removed";
          else okStatus = status === uiState.statusFilter;
        }
        var okQuery = true;
        if (hasQuery && UI.matchesParsedSearchQuery) {
          var rec = buildRowSearchRecord(it, st);
          okQuery = UI.matchesParsedSearchQuery(rec.text, parsedQuery, rec.metrics);
        }
        if (okLabel && okStatus && okQuery) matched.push(it);
        else hiddenKeys.add(key);
      });

      var col = colDefs.find(function (c) { return c.id === uiState.sortCol; }) || colDefs[1];
      var isTextSort = col.id === "name" || col.id === "method" || col.id === "labels";
      var factor = uiState.sortDir === "asc" ? 1 : -1;

      matched.sort(function (a, b) {
        var va = getSortValue(a, col.id, st);
        var vb = getSortValue(b, col.id, st);
        if (isTextSort) return factor * UI.collator.compare(String(va), String(vb));
        return factor * ((Number(va) || 0) - (Number(vb) || 0));
      });

      // Reorder existing <tr> nodes without destroying them
      var frag = document.createDocumentFragment();

      var rank = 1;
      matched.forEach(function (it) {
        var key = it.key || it.name;
        var tr = rowMap.get(key);
        if (!tr) return;
        tr.hidden = false;
        tr.cells[0].textContent = String(rank++);
        frag.appendChild(tr);
        if (openDrawers.has(key)) {
          var dTr = openDrawers.get(key);
          dTr.hidden = false;
          frag.appendChild(dTr);
        }
      });

      bodyItems.forEach(function (it) {
        var key = it.key || it.name;
        if (!hiddenKeys.has(key)) return;
        var tr = rowMap.get(key);
        if (tr) {
          tr.hidden = true;
          frag.appendChild(tr);
        }
        if (openDrawers.has(key)) {
          var dTr = openDrawers.get(key);
          dTr.hidden = true;
          frag.appendChild(dTr);
        }
      });

      if (tbody) tbody.appendChild(frag);

      uiState.activeList = (!isFiltered && aggItem) ? [aggItem].concat(matched) : matched.slice();

      var cntBadge = document.getElementById("dir-visible-count");
      if (cntBadge) {
        cntBadge.textContent = isFiltered
          ? (fmtInt(matched.length) + " of " + fmtInt(bodyItems.length) + " shown")
          : (fmtInt(matched.length) + " shown");
      }

      renderTableFooter(matched, isFiltered, st);

      if (!skipAlign && dirTable && CMP.syncTableAlignment) {
        CMP.syncTableAlignment(dirTable);
      }

      if (updatePager) {
        if (!uiState.activeList.some(function (x) { return (x.key || x.name) === uiState.selectedKey; })) {
          if (uiState.activeList.length) selectItem(uiState.activeList[0].key || uiState.activeList[0].name, false);
          else updateWorkbench();
        } else {
          updatePagerControls();
        }
      }
    }

    function buildSummedFooterRow(rows, idxGlyph, label, trClass, st) {
      var sumA = { count: 0, rate: 0, cph: 0, errors: 0, events: 0, hasCount: false, hasRate: false, hasErr: false, hasEv: false };
      var sumB = { count: 0, rate: 0, cph: 0, errors: 0, events: 0, hasCount: false, hasRate: false, hasErr: false, hasEv: false };
      rows.forEach(function (it) {
        var rA = it.runs && it.runs[st.baseIdx];
        var rB = it.runs && it.runs[st.targetIdx];
        if (rA) {
          if (rA.count != null) { sumA.count += Number(rA.count); sumA.hasCount = true; }
          if (rA.rate != null) { sumA.rate += Number(rA.rate); sumA.hasRate = true; }
          if (rA.cph != null) sumA.cph += Number(rA.cph);
          if (rA.errors != null) { sumA.errors += Number(rA.errors); sumA.hasErr = true; }
          if (rA.events != null) { sumA.events += Number(rA.events); sumA.hasEv = true; }
        }
        if (rB) {
          if (rB.count != null) { sumB.count += Number(rB.count); sumB.hasCount = true; }
          if (rB.rate != null) { sumB.rate += Number(rB.rate); sumB.hasRate = true; }
          if (rB.cph != null) sumB.cph += Number(rB.cph);
          if (rB.errors != null) { sumB.errors += Number(rB.errors); sumB.hasErr = true; }
          if (rB.events != null) { sumB.events += Number(rB.events); sumB.hasEv = true; }
        }
      });

      var trHtml = trClass ? ('<tr class="' + trClass + '">') : "<tr>";
      colDefs.forEach(function (col) {
        if (col.id === "idx") { trHtml += '<td class="col-idx">' + esc(idxGlyph) + "</td>"; return; }
        if (col.id === "name") { trHtml += '<td class="col-text">' + esc(label) + "</td>"; return; }
        if (col.id === "method" || col.id === "labels") { trHtml += '<td class="' + (col.cls || "") + '">—</td>'; return; }
        var cd = null;
        if (col.id === "count" && (sumA.hasCount || sumB.hasCount)) {
          cd = CMP.buildComparisonCellData(CMP.calcDelta(sumA.hasCount ? sumA.count : null, sumB.hasCount ? sumB.count : null), fmtInt, "", "higher", st.viewMode, {});
        } else if (col.id === "rate" && (sumA.hasRate || sumB.hasRate)) {
          cd = CMP.buildComparisonCellData(CMP.calcDelta(sumA.hasRate ? sumA.rate : null, sumB.hasRate ? sumB.rate : null), function (n) { return fmtDec(n, 2); }, "", "higher", st.viewMode, {});
        } else if (col.id === "cph" && (sumA.hasRate || sumB.hasRate)) {
          cd = CMP.buildComparisonCellData(CMP.calcDelta(sumA.hasRate ? sumA.cph : null, sumB.hasRate ? sumB.cph : null), fmtInt, "", "higher", st.viewMode, {});
        } else if (col.id === "errors" && (sumA.hasErr || sumB.hasErr)) {
          cd = CMP.buildComparisonCellData(CMP.calcDelta(sumA.hasErr ? sumA.errors : null, sumB.hasErr ? sumB.errors : null), fmtInt, "", "lower", st.viewMode, { deadband: 0, isError: true, absPill: true });
        } else if (col.id === "events" && (sumA.hasEv || sumB.hasEv)) {
          cd = CMP.buildComparisonCellData(CMP.calcDelta(sumA.hasEv ? sumA.events : null, sumB.hasEv ? sumB.events : null), fmtInt, "", "warn", st.viewMode, { deadband: 0, isEvent: true, absPill: true });
        }
        trHtml += '<td class="' + (col.cls || "") + '"' + (cd ? ' data-copy="' + esc(cd.copyText) + '"' : "") + ">" + (cd ? cd.html : "") + "</td>";
      });
      trHtml += "</tr>";
      return trHtml;
    }

    function renderTableFooter(matched, isFiltered, st) {
      if (!tfoot) return;

      var totalRowHtml;
      if (aggItem) {
        totalRowHtml = "<tr>";
        colDefs.forEach(function (col) {
          if (col.id === "idx") { totalRowHtml += '<td class="col-idx">Σ</td>'; return; }
          if (col.id === "name") { totalRowHtml += '<td class="col-text">Totals (' + fmtInt(bodyItems.length) + " items)</td>"; return; }
          if (col.id === "method" || col.id === "labels") { totalRowHtml += '<td class="' + col.cls + '">—</td>'; return; }
          var vA = readItemMetric(aggItem, st.baseIdx, col.id);
          var vB = readItemMetric(aggItem, st.targetIdx, col.id);
          var d = CMP.calcDelta(vA, vB);
          var cd = CMP.buildComparisonCellData(d, col.fmt, col.unit, col.polarity, st.viewMode, {
            deadband: col.deadband, isEvent: col.isEvent, isError: col.isError, absPill: col.absPill
          });
          totalRowHtml += '<td class="' + col.cls + '" data-copy="' + esc(cd.copyText) + '">' + cd.html + "</td>";
        });
        totalRowHtml += "</tr>";
      } else {
        totalRowHtml = buildSummedFooterRow(bodyItems, "Σ", "Totals (" + fmtInt(bodyItems.length) + " items)", "", st);
      }

      var filteredRowHtml = isFiltered
        ? buildSummedFooterRow(matched, "ƒ", "Filtered Total (" + fmtInt(matched.length) + " of " + fmtInt(bodyItems.length) + ")", "filtered-totals-row", st)
        : "";

      tfoot.innerHTML = filteredRowHtml + totalRowHtml;
    }

    // =====================================================================
    // Expandable Row Drawer (Full Metrics + N-Run History)
    // =====================================================================

    function toggleRowDrawer(key) {
      var parentTr = rowMap.get(key);
      if (!parentTr) return;
      var btn = parentTr.querySelector("button.drawer-btn");
      if (openDrawers.has(key)) {
        openDrawers.get(key).remove();
        openDrawers.delete(key);
        if (btn) { btn.setAttribute("aria-expanded", "false"); btn.textContent = "▸"; }
        return;
      }
      var item = items.find(function (x) { return (x.key || x.name) === key; });
      if (!item) return;

      var dTr = document.createElement("tr");
      dTr.className = "drawer-row";
      var td = document.createElement("td");
      td.colSpan = colDefs.length;
      td.innerHTML = buildDrawerHtml(item);
      dTr.appendChild(td);
      parentTr.insertAdjacentElement("afterend", dTr);
      openDrawers.set(key, dTr);
      if (btn) { btn.setAttribute("aria-expanded", "true"); btn.textContent = "▾"; }
      if (CMP.syncTableAlignment) {
        var subTbl = dTr.querySelector(".cmp-subtable");
        if (subTbl) CMP.syncTableAlignment(subTbl);
      }
    }

    function refreshOpenDrawers() {
      var subTables = [];
      openDrawers.forEach(function (dTr, key) {
        var item = items.find(function (x) { return (x.key || x.name) === key; });
        if (item && dTr.cells[0]) {
          dTr.cells[0].innerHTML = buildDrawerHtml(item);
          var subTbl = dTr.querySelector(".cmp-subtable");
          if (subTbl) subTables.push(subTbl);
        }
      });
      if (subTables.length && CMP.syncTablesAlignment) {
        CMP.syncTablesAlignment(subTables);
      }
    }

    function buildDrawerHtml(item) {
      var st = CMP.getState();
      var rA = (item.runs && item.runs[st.baseIdx]) || {};
      var rB = (item.runs && item.runs[st.targetIdx]) || {};
      var msUnit = config.valueUnit !== undefined ? config.valueUnit : "ms";
      var msFmt = config.valueFmt || fmtInt;

      var specRows = [];
      if (showCounts) specRows.push(["Count", rA.count, rB.count, fmtInt, "", "higher", 1.0]);
      specRows.push(["Rate (1/s)", rA.rate, rB.rate, function (n) { return fmtDec(n, 2); }, "/s", "higher", 1.0]);
      specRows.push(["Hourly Rate (1/h)", rA.cph, rB.cph, fmtInt, "/h", "higher", 1.0]);
      if (showErrors) specRows.push(["Errors", rA.errors, rB.errors, fmtInt, "", "lower", 0]);
      if (hasEvents) specRows.push(["Events", rA.events, rB.events, fmtInt, "", "warn", 0]);
      if (hasApdex && (rA.apdex != null || rB.apdex != null)) specRows.push(["Apdex", rA.apdex, rB.apdex, function (n) { return fmtDec(n, 2); }, "", "higher", 1.0]);
      specRows.push(["Mean", rA.mean, rB.mean, msFmt, msUnit, polarity, 1.0]);
      specRows.push(["Min", rA.min, rB.min, msFmt, msUnit, polarity, 1.0]);
      specRows.push(["Median", rA.median, rB.median, msFmt, msUnit, polarity, 1.0]);
      specRows.push(["Max", rA.max, rB.max, msFmt, msUnit, polarity, 1.0]);
      if (rA.dev != null || rB.dev != null) specRows.push(["Std Dev", rA.dev, rB.dev, msFmt, msUnit, polarity, 1.0]);
      pcts.forEach(function (p) {
        specRows.push(["P" + p, UI.getPercentile(rA, p), UI.getPercentile(rB, p), msFmt, msUnit, polarity, 1.0]);
      });

      // Request-specific sub-metrics
      if (config.showMethod && (rA.distinctUrls != null || rB.distinctUrls != null)) {
        specRows.push(["Distinct URLs", rA.distinctUrls, rB.distinctUrls, fmtInt, "", "neutral", 0]);
      }
      if (rA.recvMean != null || rB.recvMean != null) {
        specRows.push(["Bytes Received (Mean)", rA.recvMean, rB.recvMean, fmtBytes, "", "neutral", 0]);
      }
      if (rA.sentMean != null || rB.sentMean != null) {
        specRows.push(["Bytes Sent (Mean)", rA.sentMean, rB.sentMean, fmtBytes, "", "neutral", 0]);
      }
      [["dns", "DNS Mean"], ["conn", "Connect Mean"], ["send", "Send Mean"], ["busy", "Server Busy Mean"],
       ["recv", "Receive Mean"], ["ttfb", "TTFB Mean"], ["ttlb", "TTLB Mean"]].forEach(function (pair) {
        if (rA[pair[0]] != null || rB[pair[0]] != null) {
          specRows.push([pair[1], rA[pair[0]], rB[pair[0]], function (n) { return fmtDec(n, 1); }, "ms", "lower", 1.0]);
        }
      });

      var leftTable =
        '<table class="cmp-subtable"><thead><tr><th>Metric</th><th>Baseline (A)</th><th>Target (B)</th><th>Δ Abs</th><th>Δ%</th></tr></thead><tbody>' +
        specRows.map(function (r) {
          var d = CMP.calcDelta(r[1], r[2]);
          var pc = CMP.polarityClass(d, r[5], r[6]);
          var u = r[4] ? " " + r[4] : "";
          return (
            "<tr><td>" + esc(r[0]) + "</td>" +
            "<td>" + (d.valA !== null ? esc(r[3](d.valA) + u) : "—") + "</td>" +
            "<td>" + (d.valB !== null ? esc(r[3](d.valB) + u) : "—") + "</td>" +
            "<td>" + esc(CMP.formatAbsDelta(d, r[3], r[4])) + "</td>" +
            '<td><span class="cmp-delta-pill ' + pc + '">' + esc(CMP.formatPct(d)) + "</span></td></tr>"
          );
        }).join("") +
        "</tbody></table>";

      var rightSide = "";
      if (runs.length >= 2) {
        var pctHead = repPctKey ? "<th>P" + esc(repPctKey) + "</th>" : "";
        rightSide +=
          '<table class="cmp-subtable"><thead><tr><th>Run</th><th>1/s</th><th>Mean</th>' + pctHead + "<th>Min–Max</th>" + (showErrors ? "<th>Errors</th>" : "") + "</tr></thead><tbody>" +
          runs.map(function (run, idx) {
            var rm = item.runs && item.runs[idx];
            var tag = idx === st.baseIdx ? " [A]" : (idx === st.targetIdx ? " [B]" : "");
            if (!rm) {
              return "<tr><td>#" + (idx + 1) + " " + esc(run.name) + tag + '</td><td colspan="' + (pctHead ? 5 : 4) + '" class="cell-muted">Not present</td></tr>';
            }
            var pv = repPctKey ? UI.getPercentile(rm, repPctKey) : null;
            return (
              "<tr><td>#" + (idx + 1) + " " + esc(run.name) + tag + "</td>" +
              "<td>" + (rm.rate != null ? esc(fmtDec(rm.rate, 2)) : "—") + "</td>" +
              "<td>" + (rm.mean != null ? esc(msFmt(rm.mean)) : "—") + "</td>" +
              (pctHead ? "<td>" + (pv != null ? esc(msFmt(pv)) : "—") + "</td>" : "") +
              "<td>" + esc((rm.min != null ? msFmt(rm.min) : "—") + "–" + (rm.max != null ? msFmt(rm.max) : "—")) + "</td>" +
              (showErrors ? "<td>" + (rm.errors != null ? esc(fmtInt(rm.errors)) : "—") + "</td>" : "") +
              "</tr>"
            );
          }).join("") +
          "</tbody></table>";
      }

      if (item.sampleUrls && item.sampleUrls.length) {
        rightSide += '<div style="margin-top:8px;font-size:11px;font-family:var(--font-mono);color:var(--text-muted)">Example URLs:<br/>' +
          item.sampleUrls.map(function (u) {
            return '<div style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap" title="' + esc(u) + '">' + esc(u) + "</div>";
          }).join("") + "</div>";
      }

      return '<div class="cmp-drawer-wrap"><div>' + leftTable + "</div><div>" + rightSide + "</div></div>";
    }

    // =====================================================================
    // Top Sticky Comparison Workbench
    // =====================================================================

    function selectItem(key, shouldScrollRow) {
      uiState.selectedKey = key;
      rowMap.forEach(function (tr, k) {
        if (k === key) tr.classList.add("is-selected");
        else tr.classList.remove("is-selected");
      });
      if (shouldScrollRow) {
        var tr = rowMap.get(key);
        if (tr && !tr.hidden) {
          if (tr.scrollIntoView) tr.scrollIntoView({ block: "nearest" });
          try { tr.focus({ preventScroll: true }); } catch (_) { tr.focus(); }
        }
      }
      updateWorkbench();
    }

    function updatePagerControls() {
      var list = uiState.activeList;
      var idx = list.findIndex(function (x) { return (x.key || x.name) === uiState.selectedKey; });
      var selected = idx >= 0 ? list[idx] : null;
      var regularItems = list.filter(function (x) { return !x.isAggregate; });
      var totalRegular = regularItems.length;
      var counter = document.getElementById("wb-counter");
      var prevBtn = document.getElementById("wb-prev");
      var nextBtn = document.getElementById("wb-next");
      if (counter) {
        if (!list.length) {
          counter.textContent = "0 items";
        } else if (selected && selected.isAggregate) {
          counter.textContent = totalRegular > 0
            ? ("Summary (" + fmtInt(totalRegular) + " " + (totalRegular === 1 ? "item" : "items") + ")")
            : "Summary";
        } else {
          var regIdx = selected ? regularItems.indexOf(selected) : -1;
          counter.textContent = "Item " + (regIdx >= 0 ? fmtInt(regIdx + 1) : "0") + " of " + fmtInt(totalRegular);
        }
      }
      if (prevBtn) prevBtn.disabled = idx <= 0;
      if (nextBtn) nextBtn.disabled = idx < 0 || idx >= list.length - 1;
    }

    function renderWorkbenchTabs() {
      var tabsBox = document.getElementById("wb-tabs");
      if (!tabsBox) return;
      var defs = [];
      if (runs.length >= 3) {
        defs.push({ id: "trajectory", label: "Run Trajectory (" + runs.length + " Runs)" });
        defs.push({ id: "profile", label: "Latency Profile (A vs B)" });
      } else {
        defs.push({ id: "profile", label: "Latency Profile (A vs B)" });
        defs.push({ id: "trajectory", label: "Run Comparison Trend" });
      }
      defs.push({ id: "throughput", label: "Throughput" + (showErrors ? " & Errors" : "") });
      if (config.showMethod && caps.hasNetworkDetails !== false) {
        defs.push({ id: "network", label: "Socket Timings (A vs B)" });
      }

      tabsBox.setAttribute("role", "tablist");
      tabsBox.innerHTML = defs.map(function (t) {
        var sel = t.id === uiState.activeTab;
        return '<button type="button" class="wb-tab-btn' + (sel ? " active" : "") + '" role="tab" aria-selected="' +
          (sel ? "true" : "false") + '" data-wb-tab="' + t.id + '">' + esc(t.label) + "</button>";
      }).join("");

      tabsBox.addEventListener("click", function (e) {
        var btn = e.target.closest("[data-wb-tab]");
        if (!btn) return;
        uiState.activeTab = btn.getAttribute("data-wb-tab");
        Array.prototype.forEach.call(tabsBox.querySelectorAll("[role='tab']"), function (b) {
          var active = b === btn;
          b.setAttribute("aria-selected", active ? "true" : "false");
          if (active) b.classList.add("active"); else b.classList.remove("active");
        });
        renderWorkbenchChart();
      });
    }

    function bindWorkbenchEvents() {
      var prevBtn = document.getElementById("wb-prev");
      var nextBtn = document.getElementById("wb-next");
      var linkBtn = document.getElementById("wb-link-btn");
      var pinBtn = document.getElementById("wb-pin-btn");
      var collapseBtn = document.getElementById("wb-collapse-btn");
      var wbSection = document.getElementById("workbench-section");
      var wbBody = document.getElementById("wb-body");
      var dirSection = document.getElementById("directory-section");
      var tableScroll = dirSection ? dirSection.querySelector(".table-scroll") : null;
      if (dirSection && !dirSection.hasAttribute("tabindex")) dirSection.tabIndex = -1;
      if (tableScroll && !tableScroll.hasAttribute("tabindex")) tableScroll.tabIndex = -1;

      function isWorkbenchInView() {
        if (!wbSection || wbSection.hidden) return false;
        var r = wbSection.getBoundingClientRect();
        if (r.height <= 0) return false;
        var header = document.querySelector(".top-header");
        var topOffset = header ? header.getBoundingClientRect().bottom : 0;
        var vh = window.innerHeight || document.documentElement.clientHeight || 0;
        return r.bottom > topOffset + 24 && r.top < vh - 24;
      }

      function stepItem(dir) {
        var list = uiState.activeList;
        if (!list.length) return;
        if (tbody && document.activeElement && tbody.contains(document.activeElement) && document.activeElement.tagName === "TR") {
          document.activeElement.blur();
        }
        var idx = list.findIndex(function (x) { return (x.key || x.name) === uiState.selectedKey; });
        var nextIdx = idx < 0 ? 0 : Math.max(0, Math.min(list.length - 1, idx + dir));
        selectItem(list[nextIdx].key || list[nextIdx].name, false);
      }

      if (prevBtn) prevBtn.addEventListener("click", function () { stepItem(-1); });
      if (nextBtn) nextBtn.addEventListener("click", function () { stepItem(1); });

      if (linkBtn) {
        linkBtn.addEventListener("click", function () {
          try {
            var st = CMP.getState();
            var u = new URL(window.location.href);
            u.searchParams.set("base", String(st.baseIdx));
            u.searchParams.set("target", String(st.targetIdx));
            u.searchParams.set("view", st.viewMode);
            if (uiState.selectedKey) u.searchParams.set("item", uiState.selectedKey);
            var href = u.toString();
            if (window.history && window.history.replaceState) window.history.replaceState(null, "", href);
            if (navigator.clipboard && navigator.clipboard.writeText) navigator.clipboard.writeText(href);
            linkBtn.setAttribute("data-copied", "");
            setTimeout(function () { linkBtn.removeAttribute("data-copied"); }, 1400);
          } catch (_) { /* ignore */ }
        });
      }

      function syncPinBtn() {
        if (!pinBtn || !wbSection) return;
        wbSection.classList.toggle("unpinned", !uiState.pinned);
        pinBtn.classList.toggle("active", uiState.pinned);
        pinBtn.setAttribute("aria-pressed", uiState.pinned ? "true" : "false");
        pinBtn.title = uiState.pinned ? "Pinned to top — click to unpin" : "Unpinned — click to pin to top";
        pinBtn.innerHTML = uiState.pinned ? ICON_PIN : ICON_PIN_OFF;
      }
      if (pinBtn && wbSection) {
        syncPinBtn();
        pinBtn.addEventListener("click", function () {
          uiState.pinned = !uiState.pinned;
          syncPinBtn();
        });
      }
      if (collapseBtn && wbBody) {
        collapseBtn.addEventListener("click", function () {
          uiState.collapsed = !uiState.collapsed;
          wbBody.hidden = uiState.collapsed;
          collapseBtn.setAttribute("aria-expanded", uiState.collapsed ? "false" : "true");
          if (!uiState.collapsed && echartInst && !echartInst.isDisposed()) echartInst.resize();
        });
      }

      document.addEventListener("keydown", function (e) {
        if (e.defaultPrevented) return;
        var tag = e.target && e.target.tagName;
        if (tag === "INPUT" || tag === "SELECT" || tag === "TEXTAREA") return;
        if (e.target && e.target.closest && e.target.closest('[role="toolbar"], [role="tablist"]')) return;
        var inTable = Boolean(dirSection && e.target && dirSection.contains(e.target));
        if (e.key === "ArrowLeft") {
          if (inTable || !isWorkbenchInView()) return;
          e.preventDefault();
          stepItem(-1);
        } else if (e.key === "ArrowRight") {
          if (inTable || !isWorkbenchInView()) return;
          e.preventDefault();
          stepItem(1);
        } else if (e.key === "ArrowDown") {
          e.preventDefault();
          var list = uiState.activeList;
          var idx = list.findIndex(function (x) { return (x.key || x.name) === uiState.selectedKey; });
          if (idx + 1 < list.length) selectItem(list[idx + 1].key || list[idx + 1].name, true);
        } else if (e.key === "ArrowUp") {
          e.preventDefault();
          var listU = uiState.activeList;
          var idxU = listU.findIndex(function (x) { return (x.key || x.name) === uiState.selectedKey; });
          if (idxU - 1 >= 0) selectItem(listU[idxU - 1].key || listU[idxU - 1].name, true);
        } else if (e.key === "Enter" && e.target && e.target.closest && !e.target.closest("button,a")) {
          var rowEl = e.target.closest("#dir-table tbody tr.wb-row");
          if (rowEl && !rowEl.hidden) {
            var rowKey = rowEl.getAttribute("data-item-key");
            if (rowKey) {
              e.preventDefault();
              toggleRowDrawer(rowKey);
            }
          }
        }
      });
    }

    function updateWorkbench() {
      var item = items.find(function (x) { return (x.key || x.name) === uiState.selectedKey; }) || uiState.activeList[0];
      updatePagerControls();

      var nameEl = document.getElementById("wb-name");
      var subEl = document.getElementById("wb-sub");
      var badgeEl = document.getElementById("wb-badge");
      var labelsEl = document.getElementById("wb-labels");
      var kpisEl = document.getElementById("wb-kpis");
      var fnEl = document.getElementById("wb-footnote");

      if (!item) {
        if (nameEl) nameEl.textContent = "No matching item";
        if (subEl) subEl.textContent = "";
        if (labelsEl) { labelsEl.hidden = true; labelsEl.innerHTML = ""; }
        if (kpisEl) kpisEl.innerHTML = "";
        if (fnEl) fnEl.innerHTML = "";
        if (echartInst && !echartInst.isDisposed()) echartInst.clear();
        return;
      }

      if (nameEl) { nameEl.textContent = item.name; nameEl.title = item.name; }
      if (subEl) subEl.textContent = (item.sampleUrls && item.sampleUrls[0]) || item.description || "";
      if (badgeEl) {
        var bTxt = item.isAggregate ? "ALL" : (item.method || config.badgeLabel || "");
        if (bTxt) {
          badgeEl.textContent = bTxt;
          badgeEl.hidden = false;
          badgeEl.className = "wb-badge" + (item.method ? " method-" + item.method.toLowerCase() : "");
        } else {
          badgeEl.hidden = true;
        }
      }
      if (labelsEl) {
        if (hasLabels && item.labels && item.labels.length) {
          labelsEl.hidden = false;
          labelsEl.innerHTML = item.labels.map(function (l) {
            return '<span class="label-chip">' + esc(l) + "</span>";
          }).join("");
        } else {
          labelsEl.hidden = true;
          labelsEl.innerHTML = "";
        }
      }

      var st = CMP.getState();
      var rA = (item.runs && item.runs[st.baseIdx]) || {};
      var rB = (item.runs && item.runs[st.targetIdx]) || {};
      var msUnit = config.valueUnit !== undefined ? config.valueUnit : "ms";
      var msFmt = config.valueFmt || fmtInt;

      if (kpisEl) {
        var dRate = CMP.calcDelta(rA.rate, rB.rate);
        var dMean = CMP.calcDelta(rA.mean, rB.mean);
        var pKey = repPctKey || null;
        var dTail = pKey
          ? CMP.calcDelta(UI.getPercentile(rA, pKey), UI.getPercentile(rB, pKey))
          : CMP.calcDelta(rA.median, rB.median);
        var tailLabel = pKey ? ("P" + pKey) : "Median";
        var dErr = showErrors ? CMP.calcDelta(rA.errors, rB.errors) : null;

        kpisEl.innerHTML =
          renderWbKpi("1/s", dRate, function (n) { return fmtDec(n, 2); }, "", "higher") +
          renderWbKpi("Mean", dMean, msFmt, msUnit, polarity) +
          renderWbKpi(tailLabel, dTail, msFmt, msUnit, polarity) +
          (dErr ? renderWbKpi("Errors", dErr, fmtInt, "", "lower") : "");
      }

      if (fnEl) {
        var pctSummary = pcts.length
          ? pcts.map(function (p) {
            var pa = UI.getPercentile(rA, p);
            var pb = UI.getPercentile(rB, p);
            return "P" + p + ": " + (pa != null ? msFmt(pa) : "—") + "→" + (pb != null ? msFmt(pb) : "—") + (msUnit ? " " + msUnit : "");
          }).join(" · ")
          : ("Median: " + (rA.median != null ? msFmt(rA.median) : "—") + "→" + (rB.median != null ? msFmt(rB.median) : "—") + (msUnit ? " " + msUnit : ""));

        var spreadSummary = "Min " + (rA.min != null ? msFmt(rA.min) : "—") + "→" + (rB.min != null ? msFmt(rB.min) : "—") +
          " · Max " + (rA.max != null ? msFmt(rA.max) : "—") + "→" + (rB.max != null ? msFmt(rB.max) : "—") + (msUnit ? " " + msUnit : "");

        var rateSummary = "1/h: " + (rA.cph != null ? fmtInt(rA.cph) : "—") + "→" + (rB.cph != null ? fmtInt(rB.cph) : "—");
        if (showCounts && (rA.count != null || rB.count != null)) {
          rateSummary += " · Count: " + (rA.count != null ? fmtInt(rA.count) : "—") + "→" + (rB.count != null ? fmtInt(rB.count) : "—");
        }

        var relSummary = [];
        if (showErrors) relSummary.push("Errors: " + (rA.errors != null ? fmtInt(rA.errors) : "—") + "→" + (rB.errors != null ? fmtInt(rB.errors) : "—"));
        if (hasEvents) relSummary.push("Events: " + (rA.events != null ? fmtInt(rA.events) : "—") + "→" + (rB.events != null ? fmtInt(rB.events) : "—"));
        if (hasApdex && (rA.apdex != null || rB.apdex != null)) relSummary.push("Apdex: " + fmtDec(rA.apdex, 2, "—") + "→" + fmtDec(rB.apdex, 2, "—"));

        fnEl.innerHTML =
          '<div class="fn-group"><span class="fn-label">Percentiles &amp; Median</span><span class="fn-val">' + esc(pctSummary) + "</span></div>" +
          '<div class="fn-group"><span class="fn-label">Bounds &amp; Spread</span><span class="fn-val">' + esc(spreadSummary) + "</span></div>" +
          '<div class="fn-group"><span class="fn-label">Volume &amp; Throughput</span><span class="fn-val">' + esc(rateSummary) + "</span></div>" +
          '<div class="fn-group"><span class="fn-label">Reliability</span><span class="fn-val">' + esc(relSummary.join(" · ") || "—") + "</span></div>";
      }

      renderWorkbenchChart();
    }

    function renderWbKpi(label, delta, fmtFn, unit, pol) {
      var u = unit ? " " + unit : "";
      var valStr = delta.valB !== null ? (fmtFn(delta.valB) + u) : "—";
      var pCls = CMP.polarityClass(delta, pol, label === "Errors" ? 0 : 1.0);
      return (
        '<div class="wb-kpi-item">' +
          '<span class="wb-kpi-label">' + esc(label) + "</span>" +
          '<span class="wb-kpi-val">' + esc(valStr) + ' <span class="cmp-delta-pill ' + pCls + '">' + esc(CMP.formatPct(delta)) + "</span></span>" +
        "</div>"
      );
    }

    function ensureWbTrajControls(stageDom) {
      var bar = document.getElementById("wb-traj-controls");
      var status = document.getElementById("wb-traj-status");
      if (!bar && stageDom && stageDom.parentNode) {
        bar = document.createElement("div");
        bar.id = "wb-traj-controls";
        bar.className = "cmp-series-bar";
        bar.setAttribute("role", "toolbar");
        bar.setAttribute("focusgroup", "toolbar wrap");
        bar.setAttribute("aria-label", "Trajectory chart series");
        bar.setAttribute("aria-controls", "wb-stage");
        bar.hidden = true;
        stageDom.parentNode.insertBefore(bar, stageDom);
      }
      if (!status && stageDom && stageDom.parentNode) {
        status = document.createElement("span");
        status.id = "wb-traj-status";
        status.className = "sr-only";
        status.setAttribute("role", "status");
        status.setAttribute("aria-live", "polite");
        stageDom.parentNode.insertBefore(status, stageDom);
      }
      return { bar: bar, status: status };
    }

    function renderWorkbenchChart() {
      var dom = document.getElementById("wb-stage");
      if (!dom || typeof window.echarts === "undefined") return;
      var trajEls = ensureWbTrajControls(dom);
      if (trajEls.bar && uiState.activeTab !== "trajectory") {
        trajEls.bar.hidden = true;
      }
      if (!echartInst || echartInst.isDisposed()) {
        echartInst = window.echarts.init(dom, null, { renderer: "canvas" });
        if (typeof window.ResizeObserver !== "undefined") {
          echartRo = new window.ResizeObserver(function () {
            if (echartInst && !echartInst.isDisposed()) echartInst.resize();
          });
          echartRo.observe(dom);
        }
      }

      var item = items.find(function (x) { return (x.key || x.name) === uiState.selectedKey; }) || uiState.activeList[0];
      if (!item) {
        if (trajEls.bar) trajEls.bar.hidden = true;
        echartInst.clear();
        return;
      }

      var st = CMP.getState();
      var baseRun = CMP.getBaseRun() || { name: "Baseline" };
      var targetRun = CMP.getTargetRun() || { name: "Target" };
      var rA = (item.runs && item.runs[st.baseIdx]) || {};
      var rB = (item.runs && item.runs[st.targetIdx]) || {};

      var msUnit = config.valueUnit !== undefined ? config.valueUnit : "ms";
      var yLabel = msUnit ? "Value [" + msUnit + "]" : "Value";

      var dark = document.documentElement.classList.contains("dark-theme");
      var textDark = dark ? "#e4e4e7" : "#18181b";
      var muted = dark ? "#a1a1aa" : "#666e7a";
      var split = dark ? "#2e2e36" : "#e5e7eb";
      var axisColor = dark ? "#3f3f46" : "#cbd5e1";
      var tipBg = dark ? "rgba(24, 26, 31, 0.96)" : "#fff";
      var tipBorder = dark ? "#393e47" : "#cbd5e1";
      var cBase = dark ? "#60a5fa" : "#244b73";
      var cTarget = dark ? "#fb923c" : "#e07000";
      var cErr = dark ? "#f87171" : "#dc2626";

      if (uiState.activeTab === "profile") {
        var cats = ["Min", "Mean", "Median"];
        var valsA = [rA.min != null ? rA.min : null, rA.mean != null ? rA.mean : null, rA.median != null ? rA.median : null];
        var valsB = [rB.min != null ? rB.min : null, rB.mean != null ? rB.mean : null, rB.median != null ? rB.median : null];
        pcts.forEach(function (p) {
          cats.push("P" + p);
          valsA.push(UI.getPercentile(rA, p));
          valsB.push(UI.getPercentile(rB, p));
        });
        cats.push("Max");
        valsA.push(rA.max != null ? rA.max : null);
        valsB.push(rB.max != null ? rB.max : null);

        echartInst.setOption({
          animationDuration: 180,
          tooltip: { trigger: "axis", backgroundColor: tipBg, borderColor: tipBorder, textStyle: { color: textDark, fontSize: 12 } },
          legend: { bottom: 6, textStyle: { color: textDark, fontSize: 11 } },
          grid: { left: 68, right: 24, top: 24, bottom: 52 },
          xAxis: { type: "category", data: cats, axisLine: { lineStyle: { color: axisColor } }, axisLabel: { color: muted, fontSize: 11 } },
          yAxis: { type: "value", name: yLabel, nameTextStyle: { color: muted }, axisLabel: { color: muted }, splitLine: { lineStyle: { color: split } } },
          series: [
            { name: "Baseline #" + (st.baseIdx + 1) + " (" + baseRun.name + ")", type: "bar", barMaxWidth: 26, data: valsA, itemStyle: { color: cBase, borderRadius: [3, 3, 0, 0] } },
            { name: "Target #" + (st.targetIdx + 1) + " (" + targetRun.name + ")", type: "bar", barMaxWidth: 26, data: valsB, itemStyle: { color: cTarget, borderRadius: [3, 3, 0, 0] } }
          ]
        }, true);
        return;
      }

      if (uiState.activeTab === "network") {
        var socks = [["dns", "DNS"], ["conn", "Connect"], ["send", "Send"], ["busy", "Server Busy"], ["recv", "Receive"], ["ttfb", "TTFB"], ["ttlb", "TTLB"]];
        echartInst.setOption({
          animationDuration: 180,
          tooltip: { trigger: "axis", backgroundColor: tipBg, borderColor: tipBorder, textStyle: { color: textDark, fontSize: 12 } },
          legend: { bottom: 6, textStyle: { color: textDark, fontSize: 11 } },
          grid: { left: 68, right: 24, top: 24, bottom: 52 },
          xAxis: { type: "category", data: socks.map(function (s) { return s[1]; }), axisLine: { lineStyle: { color: axisColor } }, axisLabel: { color: muted, fontSize: 11 } },
          yAxis: { type: "value", name: "Mean [ms]", nameTextStyle: { color: muted }, axisLabel: { color: muted }, splitLine: { lineStyle: { color: split } } },
          series: [
            { name: "Baseline (A)", type: "bar", barMaxWidth: 26, data: socks.map(function (s) { return rA[s[0]] != null ? rA[s[0]] : null; }), itemStyle: { color: cBase, borderRadius: [3, 3, 0, 0] } },
            { name: "Target (B)", type: "bar", barMaxWidth: 26, data: socks.map(function (s) { return rB[s[0]] != null ? rB[s[0]] : null; }), itemStyle: { color: cTarget, borderRadius: [3, 3, 0, 0] } }
          ]
        }, true);
        return;
      }

      var runLabels = runs.map(function (r, i) {
        var tag = i === st.baseIdx ? " [A]" : (i === st.targetIdx ? " [B]" : "");
        return "#" + (i + 1) + " " + (r.name || ("Run " + (i + 1))) + tag;
      });
      var runAxisLabels = runs.map(function (_, i) {
        return "#" + (i + 1);
      });

      if (uiState.activeTab === "throughput") {
        var rates = runs.map(function (_, i) { var rm = item.runs && item.runs[i]; return rm && rm.rate != null ? rm.rate : null; });
        var errs = runs.map(function (_, i) { var rm = item.runs && item.runs[i]; return rm && rm.errors != null ? rm.errors : null; });
        var tSeries = [{ name: "Throughput (1/s)", type: "bar", barMaxWidth: 32, data: rates, itemStyle: { color: cBase, borderRadius: [3, 3, 0, 0] } }];
        if (showErrors && errs.some(function (v) { return v !== null; })) {
          tSeries.push({ name: "Errors", type: "line", yAxisIndex: 1, data: errs, symbolSize: 7, lineStyle: { color: cErr, width: 2 }, itemStyle: { color: cErr } });
        }
        echartInst.setOption({
          animationDuration: 180,
          tooltip: {
            trigger: "axis",
            backgroundColor: tipBg,
            borderColor: tipBorder,
            textStyle: { color: textDark, fontSize: 12 },
            formatter: function (params) {
              if (!params || !params.length) return "";
              var idx = params[0].dataIndex;
              var html = '<div style="font-weight:700;margin-bottom:4px">' + esc(runLabels[idx] || params[0].axisValue || "") + "</div>";
              params.forEach(function (p) {
                var valTxt = "—";
                if (p.value !== null && p.value !== undefined) {
                  valTxt = p.seriesName.indexOf("Throughput") === 0 ? (fmtDec(p.value, 2) + " /s") : fmtInt(p.value);
                }
                html +=
                  '<div style="display:flex;align-items:center;justify-content:space-between;gap:14px;font-variant-numeric:tabular-nums">' +
                    "<span>" + p.marker + " " + esc(p.seriesName) + "</span>" +
                    "<strong>" + esc(valTxt) + "</strong>" +
                  "</div>";
              });
              return html;
            }
          },
          legend: { bottom: 6, textStyle: { color: textDark, fontSize: 11 } },
          grid: { left: 64, right: 56, top: 24, bottom: 52 },
          xAxis: { type: "category", data: runAxisLabels, axisLine: { lineStyle: { color: axisColor } }, axisLabel: { color: muted, fontSize: 11 } },
          yAxis: [
            { type: "value", name: "Rate [1/s]", nameTextStyle: { color: muted }, axisLabel: { color: muted }, splitLine: { lineStyle: { color: split } } },
            { type: "value", name: "Errors", show: tSeries.length > 1, nameTextStyle: { color: cErr }, axisLabel: { color: cErr }, splitLine: { show: false } }
          ],
          series: tSeries
        }, true);
        return;
      }

      // Run Trajectory across all runs (with selectable percentiles, Mean, Min-Max, 1/s, Errors, Events)
      var msFmt = config.valueFmt || fmtInt;
      var runMetrics = runs.map(function (_, i) {
        return item.runs && item.runs[i] ? item.runs[i] : null;
      });
      var traj = CMP.buildTrajectoryConfig({
        runs: runs,
        runMetrics: runMetrics,
        pcts: pcts,
        baseIdx: st.baseIdx,
        targetIdx: st.targetIdx,
        dark: dark,
        valueUnit: msUnit,
        valueFmt: msFmt,
        showErrors: showErrors,
        showEvents: hasEvents,
        useHeadline: false
      });

      if (trajEls.bar && CMP.renderTrajectoryToolbar) {
        CMP.renderTrajectoryToolbar(trajEls.bar, traj.seriesDefs, {
          statusEl: trajEls.status,
          onChange: renderWorkbenchChart
        });
      }

      echartInst.setOption(traj.echartOption, true);
    }
  }

  window.XltComparisonWorkbench = { init: init };
})();
