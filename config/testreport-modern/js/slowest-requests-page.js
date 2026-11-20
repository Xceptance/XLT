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
 * XLT Modern Load Report — Slowest Individual Requests Explorer (`slowest-requests.html`)
 *
 * @fileoverview
 * Renders the top individual slowest HTTP request outlier table from `window.XLT_SLOWEST_REQUESTS`.
 *
 * DOM Performance Architecture:
 * Builds each `[mainTr, drawerTr]` pair once up front in `buildInitialRows()` and stores them in
 * `records`. Interactive sorting and filtering (`applyFilterAndSort`) toggle `hidden` and append
 * existing paired `<tr>` + `.drawer-row` nodes via a single `DocumentFragment` so expanded URL/DNS
 * drawers remain attached to their parent rows without full `innerHTML` rebuilds.
 *
 * Consumes:
 * - `window.XLT_SLOWEST_REQUESTS` (`Array<{ rank, runtime, baseMean, baseP95, time, name,
 *   method, status, usedIp, ips, dns, conn, send, busy, recv, ttfb, ttlb, bytesSent,
 *   bytesReceived, url, requestId, contentType }>`)
 * - `window.XLT_UI` (`esc`, `fmtInt`, `collator`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var collator = UI.collator;

  document.addEventListener("DOMContentLoaded", function () {
    var raw = window.XLT_SLOWEST_REQUESTS || [];
    var rowsData = Array.isArray(raw) ? raw : (raw.items || []);
    var kpiGrid = document.getElementById("slowest-kpi-grid");
    var titleEl = document.getElementById("slowest-title-text");
    var tbody = document.querySelector("#slowest-table tbody");
    var thead = document.querySelector("#slowest-table thead");
    var filterInput = document.getElementById("slowest-filter");
    var countBadge = document.getElementById("slowest-visible-count");
    var chipWrap = document.getElementById("slowest-chips");
    var statusLive = document.getElementById("table-sort-status");

    var metaHasPctConfig = Boolean(
      window.XLT_META && window.XLT_META.testReportConfig && Array.isArray(window.XLT_META.testReportConfig.runtimePercentiles)
    );
    var sampleForPcts = rowsData.map(function (r) {
      if (r && r.basePercentiles) return { percentiles: r.basePercentiles };
      if (!metaHasPctConfig && r && r.baseP95 != null) return { percentiles: { p95: r.baseP95 } };
      return null;
    }).filter(Boolean);
    var configuredPcts = UI.getConfiguredPercentiles(sampleForPcts);
    var basePctKey = UI.pickRepresentativePercentile(configuredPcts);
    var hasBasePct = Boolean(basePctKey);
    var colCount = hasBasePct ? 19 : 18;

    var baseColgroup = document.getElementById("slowest-base-colgroup");
    var basePctTh = document.getElementById("slowest-base-pct-th");
    if (basePctTh) {
      if (hasBasePct) {
        var basePctSpan = basePctTh.querySelector(".th-sort-btn > span");
        if (basePctSpan) basePctSpan.textContent = "P" + basePctKey;
      } else {
        if (baseColgroup) baseColgroup.colSpan = 1;
        if (basePctTh.parentNode) basePctTh.parentNode.removeChild(basePctTh);
      }
    }

    // 1. Compute outlier summary KPIs (status === 0 denotes socket/timeout failure without HTTP code)
    var sortedByRt = rowsData.slice().sort(function (a, b) { return (Number(b.runtime) || 0) - (Number(a.runtime) || 0); });
    var topItem = sortedByRt[0] || null;
    var medianItem = sortedByRt.length ? sortedByRt[Math.floor(sortedByRt.length / 2)] : null;
    var errStatusCount = rowsData.filter(function (r) { return (Number(r.status) >= 400) || Number(r.status) === 0; }).length;

    if (titleEl) {
      titleEl.textContent = rowsData.length
        ? "Top " + fmtInt(rowsData.length) + " Slowest Individual Requests"
        : "Slowest Individual Requests";
    }

    if (kpiGrid) {
      kpiGrid.innerHTML =
        '<article class="kpi-card">' +
          '<span class="kpi-label">Slowest Requests</span>' +
          '<div class="kpi-val">' + fmtInt(rowsData.length) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">requests</span></div>' +
          '<div class="kpi-sub">Individual slowest HTTP request executions</div>' +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Max Request Runtime</span>' +
          '<div class="kpi-val ' + (topItem ? "cell-warn" : "") + '">' + (topItem ? fmtInt(topItem.runtime) : "0") + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">ms</span></div>' +
          '<div class="kpi-sub">' + (topItem ? "Top rank: <code>" + esc(topItem.name || "") + "</code>" : "No slow requests exceeded threshold") + "</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Median Request Runtime</span>' +
          '<div class="kpi-val">' + (medianItem ? fmtInt(medianItem.runtime) : "0") + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">ms</span></div>' +
          '<div class="kpi-sub">50th percentile across slowest requests</div>' +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">HTTP Error Requests</span>' +
          '<div class="kpi-val ' + (errStatusCount > 0 ? "cell-err" : "") + '">' + fmtInt(errStatusCount) + "</div>" +
          '<div class="kpi-sub">4xx / 5xx / status-0 responses among slowest requests</div>' +
        "</article>";
    }

    if (!tbody || !thead) return;

    if (!rowsData.length) {
      if (chipWrap) chipWrap.innerHTML = "";
      if (countBadge) countBadge.textContent = "0 requests";
      tbody.innerHTML =
        '<tr><td colspan="' + colCount + '" class="col-text" style="text-align:center;padding:36px 16px;color:var(--text-muted);">' +
          "<strong>No individual requests exceeded the configured slowest-request threshold during this test run.</strong>" +
        "</td></tr>";
      return;
    }

    // 2. Quick-filter chips by HTTP method, error status, >= 15s runtime, or >= 1s connect latency
    if (chipWrap) {
      var getCnt = rowsData.filter(function (r) { return r.method === "GET"; }).length;
      var postCnt = rowsData.filter(function (r) { return r.method === "POST"; }).length;
      var gt15Cnt = rowsData.filter(function (r) { return (Number(r.runtime) || 0) >= 15000; }).length;
      var connCnt = rowsData.filter(function (r) { return (Number(r.conn) || 0) >= 1000; }).length;
      chipWrap.innerHTML =
        '<button type="button" class="btn-chip active" data-chip="all">All (' + fmtInt(rowsData.length) + ")</button>" +
        (getCnt ? '<button type="button" class="btn-chip" data-chip="GET">GET (' + fmtInt(getCnt) + ")</button>" : "") +
        (postCnt ? '<button type="button" class="btn-chip" data-chip="POST">POST (' + fmtInt(postCnt) + ")</button>" : "") +
        (errStatusCount ? '<button type="button" class="btn-chip" data-chip="4xx5xx">4xx / 5xx (' + fmtInt(errStatusCount) + ")</button>" : "") +
        (gt15Cnt ? '<button type="button" class="btn-chip" data-chip="gt15s">&ge; 15,000 ms (' + fmtInt(gt15Cnt) + ")</button>" : "") +
        (connCnt ? '<button type="button" class="btn-chip" data-chip="connSpike">Connect &ge; 1,000 ms (' + fmtInt(connCnt) + ")</button>" : "");
    }

    var activeChip = "all";
    var query = "";
    var sortKey = "rank";
    var sortDir = "ascending";
    var records = [];

    /** Builds cached main + expandable detail drawer row pairs once during initialization. */
    function buildInitialRows() {
      var frag = document.createDocumentFragment();
      rowsData.forEach(function (r) {
        var tr = document.createElement("tr");
        tr.className = "wb-row";
        tr.tabIndex = 0;

        var statusCls = r.status >= 500 ? "cell-err" : r.status >= 400 ? "cell-warn" : "";
        var urlStr = String(r.url || "");
        var shortUrl = urlStr.length > 58 ? urlStr.slice(0, 58) + "…" : urlStr;
        var reqHref = "requests.html?item=" + encodeURIComponent(r.name || "");
        var bMean = r.baseMean != null ? fmtInt(r.baseMean) : "—";
        var basePctVal = hasBasePct
          ? (r.basePercentiles
              ? UI.getPercentile({ percentiles: r.basePercentiles }, basePctKey)
              : (basePctKey === "95" && r.baseP95 != null ? Number(r.baseP95) : undefined))
          : undefined;
        var bPctCell = hasBasePct
          ? ('<td class="col-num cg-alt">' + (basePctVal !== undefined ? fmtInt(basePctVal) : "—") + "</td>")
          : "";
        var dnsCount = r.ips ? r.ips.length : 0;

        var mCls = "method-" + String(r.method || "get").toLowerCase();
        tr.innerHTML =
          '<td class="col-num">' + fmtInt(r.rank) + "</td>" +
          '<td class="col-num cell-warn">' + fmtInt(r.runtime) + "</td>" +
          '<td class="col-num cg-alt">' + bMean + "</td>" +
          bPctCell +
          '<td class="col-text">' + esc(String(r.time || "").replace(" UTC", "")) + "</td>" +
          '<td class="col-text"><div class="name-cell-wrap">' +
            '<button type="button" class="drawer-btn" aria-expanded="false" title="Toggle full URL &amp; socket details">▸</button>' +
            '<a class="name-cell-text" href="' + esc(reqHref) + '" title="' + esc(r.name) + '">' + esc(r.name) + "</a>" +
          "</div></td>" +
          '<td class="col-cat"><span class="cat-badge ' + esc(mCls) + '">' + esc(r.method || "—") + "</span></td>" +
          '<td class="col-num ' + statusCls + '">' + (r.status !== undefined && r.status !== null ? r.status : "—") + "</td>" +
          '<td class="col-text cg-alt" title="DNS pool (' + dnsCount + '): ' + esc((r.ips || []).join(", ")) + '">' + esc(r.usedIp || "—") + (dnsCount > 1 ? ' <span class="cell-muted">(' + dnsCount + ")</span>" : "") + "</td>" +
          '<td class="col-num">' + fmtInt(r.dns) + "</td>" +
          '<td class="col-num">' + fmtInt(r.conn) + "</td>" +
          '<td class="col-num">' + fmtInt(r.send) + "</td>" +
          '<td class="col-num">' + fmtInt(r.busy) + "</td>" +
          '<td class="col-num">' + fmtInt(r.recv) + "</td>" +
          '<td class="col-num cg-alt">' + fmtInt(r.ttfb) + "</td>" +
          '<td class="col-num cg-alt">' + fmtInt(r.ttlb) + "</td>" +
          '<td class="col-num">' + fmtInt(r.bytesSent) + "</td>" +
          '<td class="col-num">' + fmtInt(r.bytesReceived) + "</td>" +
          '<td class="col-text" title="' + esc(urlStr) + '">' + esc(shortUrl) + "</td>";

        var drawerTr = document.createElement("tr");
        drawerTr.className = "drawer-row";
        drawerTr.hidden = true;
        var drawerTd = document.createElement("td");
        drawerTd.colSpan = colCount;
        drawerTd.innerHTML =
          '<div class="drawer-grid">' +
            '<div class="drawer-box" style="grid-column: span 2;">' +
              "<h4>Captured Full Request URL &amp; Request ID</h4>" +
              '<div style="font-family:var(--font-mono);font-size:11.5px;word-break:break-all;margin-bottom:6px;">' +
                (urlStr ? '<a href="' + esc(urlStr) + '" target="_blank" rel="noopener">' + esc(urlStr) + " ↗</a>" : "—") +
              "</div>" +
              '<div class="cell-muted" style="font-size:11px;">Request ID: <code>' + esc(r.requestId || "—") + "</code> • Content-Type: <code>" + esc(r.contentType || "—") + "</code></div>" +
            "</div>" +
            '<div class="drawer-box">' +
              "<h4>Resolved &amp; Used IP Addresses</h4>" +
              '<div style="font-family:var(--font-mono);font-size:11px;">Used IP: <strong>' + esc(r.usedIp || "—") + "</strong></div>" +
              '<div class="cell-muted" style="font-family:var(--font-mono);font-size:11px;margin-top:4px;">DNS Pool (' + (r.ips ? r.ips.length : 0) + "): " + esc((r.ips || []).join(", ") || "—") + "</div>" +
            "</div>" +
          "</div>";
        drawerTr.appendChild(drawerTd);

        var btn = tr.querySelector(".drawer-btn");
        function toggleDrawer() {
          var open = drawerTr.hidden;
          drawerTr.hidden = !open;
          btn.setAttribute("aria-expanded", open ? "true" : "false");
          btn.textContent = open ? "▾" : "▸";
        }
        btn.addEventListener("click", function (ev) {
          ev.stopPropagation();
          toggleDrawer();
        });
        tr.addEventListener("click", function (ev) {
          if (ev.target.closest("a, button")) return;
          toggleDrawer();
        });
        tr.addEventListener("keydown", function (ev) {
          if (ev.key === "Enter") {
            ev.preventDefault();
            toggleDrawer();
          }
        });

        frag.appendChild(tr);
        frag.appendChild(drawerTr);
        records.push({
          item: r,
          basePct: basePctVal,
          tr: tr,
          drawerTr: drawerTr,
          blob: ((r.name || "") + " " + (r.method || "") + " " + (r.status || "") + " " + (r.usedIp || "") + " " + urlStr + " " + (r.requestId || "")).toLowerCase()
        });
      });
      tbody.innerHTML = "";
      tbody.appendChild(frag);
    }

    /**
     * Filters by active chip and space-separated AND search tokens, sorts `records`,
     * and re-appends paired `[tr, drawerTr]` nodes in one fragment pass.
     */
    function applyFilterAndSort() {
      var visibleCount = 0;

      records.forEach(function (rec) {
        var r = rec.item;
        var chipOk = true;
        if (activeChip === "GET" || activeChip === "POST") {
          chipOk = r.method === activeChip;
        } else if (activeChip === "4xx5xx") {
          chipOk = r.status >= 400 || r.status === 0;
        } else if (activeChip === "gt15s") {
          chipOk = r.runtime >= 15000;
        } else if (activeChip === "connSpike") {
          chipOk = r.conn >= 1000;
        }

        var textOk = UI.matchesFilter(rec.blob, query, r);

        var show = chipOk && textOk;
        rec.tr.hidden = !show;
        // Collapse drawer when its parent row is filtered out
        if (!show) rec.drawerTr.hidden = true;
        if (show) visibleCount++;
      });

      var factor = sortDir === "ascending" ? 1 : -1;
      var isText = sortKey === "name" || sortKey === "method" || sortKey === "usedIp" || sortKey === "time" || sortKey === "url";
      records.sort(function (a, b) {
        var va = sortKey === "basePct" ? a.basePct : a.item[sortKey];
        var vb = sortKey === "basePct" ? b.basePct : b.item[sortKey];
        if (isText) {
          return factor * collator.compare(String(va || ""), String(vb || ""));
        }
        return factor * ((Number(va) || 0) - (Number(vb) || 0));
      });

      var frag = document.createDocumentFragment();
      records.forEach(function (rec) {
        frag.appendChild(rec.tr);
        frag.appendChild(rec.drawerTr);
      });
      tbody.appendChild(frag);

      if (countBadge) {
        countBadge.textContent = fmtInt(visibleCount) + " / " + fmtInt(records.length) + " requests";
      }
    }

    thead.addEventListener("click", function (e) {
      var btn = e.target.closest("button.th-sort-btn");
      if (!btn) return;
      var th = btn.closest("th");
      var key = th.getAttribute("data-key");
      if (!key) return;
      if (th.hasAttribute("aria-sort")) {
        sortDir = th.getAttribute("aria-sort") === "ascending" ? "descending" : "ascending";
      } else {
        sortDir = "ascending";
      }
      sortKey = key;
      Array.prototype.forEach.call(thead.querySelectorAll("th[aria-sort]"), function (h) {
        h.removeAttribute("aria-sort");
      });
      th.setAttribute("aria-sort", sortDir);
      applyFilterAndSort();
      if (statusLive) {
        var labelSpan = btn.querySelector("span");
        statusLive.textContent = "Sorted by " + (labelSpan ? labelSpan.textContent.trim() : btn.textContent.trim()) + ", " + sortDir + ", " + records.length + " rows";
      }
    });

    if (filterInput) {
      filterInput.addEventListener("input", function () {
        query = filterInput.value;
        applyFilterAndSort();
      });
    }

    if (chipWrap) {
      chipWrap.addEventListener("click", function (e) {
        var btn = e.target.closest("[data-chip]");
        if (!btn) return;
        activeChip = btn.getAttribute("data-chip");
        Array.prototype.forEach.call(chipWrap.querySelectorAll("[data-chip]"), function (b) {
          b.classList.toggle("active", b === btn);
        });
        applyFilterAndSort();
      });
    }

    buildInitialRows();
    applyFilterAndSort();
  });
})();
