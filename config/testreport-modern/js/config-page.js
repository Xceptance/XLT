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
 * XLT Modern Load Report — Configuration, Load Profile & JVM Renderer (`configuration.html`)
 *
 * @fileoverview
 * Populates the test suite configuration reference page from `window.XLT_CONFIG`:
 *   1. Top KPI strip (`#cfg-kpi-grid`) — property count, scenarios, agents/JVM args, XLT engine.
 *   2. Load Profile table (`#profile-config-table`) — scenario users, arrival rates, phases, think times.
 *   3. Searchable XLT Properties table (`#props-table`) — live multi-token filter + namespace chips.
 *   4. Custom JVM Arguments table (`#jvm-table`) — regex-classified agent JVM flags (`jvmargs.cfg`).
 *   5. Runtime Environment & Report Generator settings (`#env-table`).
 *
 * Consumes:
 * - `window.XLT_CONFIG` (`properties`, `loadProfile`, `loadProfileTotals`, `customJvmArgs`,
 *   `version`, `reportGeneratorConfig`) + legacy fallback `window.XLT_PROPERTIES`
 * - `window.XLT_META` (`counts`, `xltVersion`, `testReportConfig`)
 * - `window.XLT_UI` (`esc`, `fmtInt`, `fmtDec`, `fmtDurationHms`, `fmtThinkTime`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var fmtDec = UI.fmtDec;
  var fmtDurationHms = UI.fmtDurationHms;
  var fmtThinkTime = UI.fmtThinkTime;

  /**
   * Classifies a raw JVM command-line option from `jvmargs.cfg` into a readable category
   * and explanation badge for the cluster JVM section.
   *
   * @param {string} arg Raw JVM flag string (e.g., `"-XX:+UseG1GC"`, `"-Xmx4g"`).
   * @returns {{ cat: string, desc: string }} Category title and description.
   */
  function classifyJvmArg(arg) {
    var a = String(arg || "");
    if (/GC|Survivor|Tenuring|ReservePercent/i.test(a)) return { cat: "Garbage Collection", desc: "Garbage collector selection, pause target, or region sizing parameter." };
    if (/RAMPercentage|Xmx|Xms|Xmn|Metaspace|DirectMemory/i.test(a)) return { cat: "Memory & Heap", desc: "JVM heap or native memory allocation limit." };
    if (/Deduplication|Optimize|Inline|Tiered/i.test(a)) return { cat: "Optimization", desc: "JIT compiler or runtime memory deduplication flag." };
    if (/^-D/.test(a)) return { cat: "System Property", desc: "Java system property override passed on the agent command line." };
    return { cat: "JVM Argument", desc: "Custom Java Virtual Machine runtime option." };
  }

  document.addEventListener("DOMContentLoaded", function () {
    var cfg = window.XLT_CONFIG || {};
    var meta = window.XLT_META || {};
    var props = Array.isArray(cfg.properties) ? cfg.properties : (window.XLT_PROPERTIES || []);
    var loadProfile = Array.isArray(cfg.loadProfile) ? cfg.loadProfile : [];
    var lpTotals = cfg.loadProfileTotals || {};
    var jvmArgs = Array.isArray(cfg.customJvmArgs) ? cfg.customJvmArgs : [];
    var ver = cfg.version || {};
    var rgCfg = cfg.reportGeneratorConfig || {};

    // Bucket properties by top-level prefix (combining all com.xceptance.* properties into one chip)
    var prefixCounts = {};
    props.forEach(function (p) {
      var k = String(p.name || "");
      var pref = k.indexOf("com.xceptance") === 0 ? "com.xceptance" : (k.split(".")[0] || "other");
      prefixCounts[pref] = (prefixCounts[pref] || 0) + 1;
    });
    var prefixKeys = Object.keys(prefixCounts).sort(function (a, b) {
      return prefixCounts[b] - prefixCounts[a];
    });

    // 1. KPI Grid
    var kpiGrid = document.getElementById("cfg-kpi-grid");
    if (kpiGrid) {
      var agentsCnt = (meta.counts && meta.counts.agents) || 0;
      kpiGrid.innerHTML =
        '<article class="kpi-card">' +
          '<span class="kpi-label">Configured Properties</span>' +
          '<div class="kpi-val">' + fmtInt(props.length) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">props</span></div>' +
          '<div class="kpi-sub">Across <strong>' + fmtInt(prefixKeys.length) + " property namespace(s)</strong></div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Scenario Load Profile</span>' +
          '<div class="kpi-val">' + fmtInt(loadProfile.length) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">scenarios</span></div>' +
          '<div class="kpi-sub">' +
            (lpTotals.usersDisplay ? "<strong>" + esc(lpTotals.usersDisplay) + " users</strong>" : "0 users") +
            (lpTotals.arrivalDisplay && lpTotals.arrivalDisplay !== "0" ? " • " + esc(lpTotals.arrivalDisplay) + " /h arrival" : "") +
          "</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Agent Cluster Fleet</span>' +
          '<div class="kpi-val">' + fmtInt(agentsCnt) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">JVM node(s)</span></div>' +
          '<div class="kpi-sub">' + fmtInt(jvmArgs.length) + " custom JVM argument(s) configured</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">XLT Engine Version</span>' +
          '<div class="kpi-val">' + esc(ver.version || meta.xltVersion || "XLT") + "</div>" +
          '<div class="kpi-sub">Timezone: <strong>' + esc(rgCfg.timeZoneLabel || "UTC") + "</strong></div>" +
        "</article>";
    }

    // 2. Load Profile Table
    var lpTitle = document.getElementById("cfg-profile-title");
    if (lpTitle) {
      lpTitle.textContent = "Load Profile Specification (" + fmtInt(loadProfile.length) + (loadProfile.length === 1 ? " Scenario)" : " Scenarios)");
    }
    var lpTbody = document.querySelector("#profile-config-table tbody");
    var lpTfoot = document.querySelector("#profile-config-table tfoot");
    if (lpTbody) {
      if (!loadProfile.length) {
        lpTbody.innerHTML = '<tr><td colspan="11" class="col-text" style="text-align:center;padding:24px;color:var(--text-muted);">No load profile entries recorded in <code>testreport.xml</code>.</td></tr>';
        if (lpTfoot) lpTfoot.innerHTML = "";
      } else {
        lpTbody.innerHTML = loadProfile.map(function (tc, idx) {
          var name = tc.name || tc.userName || tc.className || "Scenario";
          var iters = tc.iterations && String(tc.iterations) !== "0" ? esc(tc.iterations) : "&ndash;";
          var arr = tc.arrivalRate && String(tc.arrivalRate) !== "0" ? esc(tc.arrivalRate) : "0";
          return (
            "<tr>" +
              '<td class="col-idx">' + (idx + 1) + "</td>" +
              '<td class="col-text" title="' + esc(tc.className || "") + '"><a href="transactions.html?item=' + encodeURIComponent(name) + '">' + esc(name) + "</a></td>" +
              '<td class="col-num">' + esc(tc.users || "0") + "</td>" +
              '<td class="col-num">' + fmtDec(tc.usersPct || 0, 2) + "%</td>" +
              '<td class="col-num">' + iters + "</td>" +
              '<td class="col-num">' + arr + "</td>" +
              '<td class="col-num">' + fmtDec(tc.arrivalPct || 0, 2) + "%</td>" +
              '<td class="col-text">' + esc(fmtDurationHms(tc.rampUp || 0)) + "</td>" +
              '<td class="col-text">' + esc(fmtDurationHms(tc.measurement || 0)) + "</td>" +
              '<td class="col-text">' + esc(fmtDurationHms(tc.shutdown || 0)) + "</td>" +
              '<td class="col-text">' + esc(fmtThinkTime(tc.thinkTime, tc.thinkTimeDev)) + "</td>" +
            "</tr>"
          );
        }).join("");

        if (lpTfoot) {
          lpTfoot.innerHTML =
            '<tr class="table-totals">' +
              '<td class="col-idx">Σ</td>' +
              '<td class="col-text">Totals (' + fmtInt(loadProfile.length) + " Scenarios)</td>" +
              '<td class="col-num">' + esc(lpTotals.usersDisplay || "0") + "</td>" +
              '<td class="col-num">100.00%</td>' +
              '<td class="col-num">' + esc(lpTotals.iterationsDisplay || "–") + "</td>" +
              '<td class="col-num">' + esc(lpTotals.arrivalDisplay || "0") + "</td>" +
              '<td class="col-num">100.00%</td>' +
              '<td class="col-text">' + esc(lpTotals.rampUpDisplay || "—") + "</td>" +
              '<td class="col-text">' + esc(lpTotals.measurementDisplay || "—") + "</td>" +
              '<td class="col-text">' + esc(lpTotals.shutdownDisplay || "—") + "</td>" +
              '<td class="col-text">' + esc(lpTotals.thinkTimeDisplay || "—") + "</td>" +
            "</tr>";
        }
      }
    }

    // 3. Searchable Configuration Properties Table & Dynamic Prefix Chips
    var tbody = document.querySelector("#props-table tbody");
    var filterInput = document.getElementById("props-filter");
    var chipWrap = document.getElementById("props-chips");
    var countBadge = document.getElementById("props-visible-count");

    if (filterInput) {
      filterInput.placeholder = "Search " + fmtInt(props.length) + " properties by key or value...";
    }

    if (chipWrap) {
      var chipBtns = ['<button type="button" class="btn-chip active" data-prop-chip="all">All Props (' + fmtInt(props.length) + ")</button>"];
      prefixKeys.slice(0, 8).forEach(function (pref) {
        chipBtns.push('<button type="button" class="btn-chip" data-prop-chip="' + esc(pref) + '">' + esc(pref) + " (" + fmtInt(prefixCounts[pref]) + ")</button>");
      });
      chipWrap.innerHTML = chipBtns.join("");
    }

    var activePrefix = "all";
    var query = "";
    var rows = [];

    if (tbody) {
      if (!props.length) {
        tbody.innerHTML = '<tr><td colspan="3" class="col-text" style="text-align:center;padding:24px;color:var(--text-muted);">No configuration properties recorded.</td></tr>';
      } else {
        var frag = document.createDocumentFragment();
        props.forEach(function (p, idx) {
          var tr = document.createElement("tr");
          var k = String(p.name || "");
          var pref = k.indexOf("com.xceptance") === 0 ? "com.xceptance" : (k.split(".")[0] || "other");
          tr.innerHTML =
            '<td class="col-idx">' + (idx + 1) + "</td>" +
            '<td class="col-text">' + esc(p.name) + "</td>" +
            '<td class="col-text" style="white-space:normal;word-break:break-all;">' + esc(p.value) + "</td>";
          frag.appendChild(tr);
          rows.push({
            name: k,
            prefix: pref,
            tr: tr,
            blob: (k + " " + (p.value || "")).toLowerCase()
          });
        });
        tbody.innerHTML = "";
        tbody.appendChild(frag);
      }
    }

    // Filter toggles tr.hidden across cached property rows without regenerating HTML
    function applyFilter() {
      var tokens = query.toLowerCase().trim().split(/\s+/).filter(Boolean);
      var vis = 0;
      rows.forEach(function (r) {
        var prefOk = activePrefix === "all" || r.prefix === activePrefix || r.name.indexOf(activePrefix) === 0;
        var textOk = true;
        for (var i = 0; i < tokens.length; i++) {
          if (r.blob.indexOf(tokens[i]) === -1) {
            textOk = false;
            break;
          }
        }
        var show = prefOk && textOk;
        r.tr.hidden = !show;
        if (show) {
          vis++;
          r.tr.cells[0].textContent = String(vis);
        }
      });
      if (countBadge) {
        countBadge.textContent = fmtInt(vis) + " / " + fmtInt(rows.length) + " properties";
      }
    }

    if (filterInput) {
      filterInput.addEventListener("input", function () {
        query = filterInput.value;
        applyFilter();
      });
    }

    if (chipWrap) {
      chipWrap.addEventListener("click", function (e) {
        var btn = e.target.closest("[data-prop-chip]");
        if (!btn) return;
        activePrefix = btn.getAttribute("data-prop-chip");
        Array.prototype.forEach.call(chipWrap.querySelectorAll("[data-prop-chip]"), function (b) {
          b.classList.toggle("active", b === btn);
        });
        applyFilter();
      });
    }
    applyFilter();

    // 4. Custom JVM Arguments Table
    var jvmTbody = document.querySelector("#jvm-table tbody");
    var jvmSub = document.getElementById("cfg-jvm-subtitle");
    if (jvmSub) {
      var agCnt = (meta.counts && meta.counts.agents) || 0;
      jvmSub.textContent = jvmArgs.length
        ? fmtInt(jvmArgs.length) + " custom Java Virtual Machine option(s)" + (agCnt ? " passed to " + fmtInt(agCnt) + " load agent node(s)" : "")
        : "No custom JVM options recorded in jvmargs.cfg for this test run";
    }
    if (jvmTbody) {
      if (!jvmArgs.length) {
        jvmTbody.innerHTML = '<tr><td colspan="4" class="col-text" style="text-align:center;padding:20px;color:var(--text-muted);">Default JVM runtime flags used (no custom <code>customJvmArgs</code> entries).</td></tr>';
      } else {
        jvmTbody.innerHTML = jvmArgs.map(function (arg, idx) {
          var info = classifyJvmArg(arg);
          return (
            "<tr>" +
              '<td class="col-idx">' + (idx + 1) + "</td>" +
              '<td class="col-text">' + esc(arg) + "</td>" +
              '<td class="col-text"><span class="badge-tag">' + esc(info.cat) + "</span></td>" +
              '<td class="col-text">' + esc(info.desc) + "</td>" +
            "</tr>"
          );
        }).join("");
      }
    }

    // 5. Runtime Environment & Report Generator Config Table
    var envTbody = document.querySelector("#env-table tbody");
    if (envTbody) {
      var envRows = [
        {
          label: "XLT Framework Version",
          val: (ver.productName || "Xceptance LoadTest") + " " + (ver.version || meta.xltVersion || ""),
          note: ver.revisionNumber ? "Revision " + ver.revisionNumber + (ver.buildDate ? " (" + ver.buildDate + ")" : "") : (ver.vendorName || "")
        },
        {
          label: "Report Timezone",
          val: rgCfg.timeZoneLabel || "UTC",
          note: rgCfg.timeZoneOffset !== undefined ? "Offset: " + rgCfg.timeZoneOffset + " ms" : "Standard report timestamps"
        },
        {
          label: "Slowest Requests Tracking",
          val: rgCfg.slowestRequestsTotal !== undefined ? ("Top " + fmtInt(rgCfg.slowestRequestsTotal) + " outliers (max " + fmtInt(rgCfg.slowestRequestsPerBucket || 0) + " / bucket)") : "Configured in reportgenerator.properties",
          note: rgCfg.slowestRequestsMinRuntime !== undefined ? ("Threshold range: " + fmtInt(rgCfg.slowestRequestsMinRuntime) + " ms – " + fmtInt(rgCfg.slowestRequestsMaxRuntime || 0) + " ms") : ""
        },
        {
          label: "Configured Percentiles",
          val: (meta.testReportConfig && Array.isArray(meta.testReportConfig.runtimePercentiles))
            ? meta.testReportConfig.runtimePercentiles.map(function (p) { return "P" + p; }).join(", ")
            : "Default",
          note: "Runtime quantile columns rendered across timer tables"
        }
      ];
      envTbody.innerHTML = envRows.map(function (r, idx) {
        return (
          "<tr>" +
            '<td class="col-idx">' + (idx + 1) + "</td>" +
            '<td class="col-text">' + esc(r.label) + "</td>" +
            '<td class="col-text"><code>' + esc(r.val) + "</code></td>" +
            '<td class="col-text">' + esc(r.note) + "</td>" +
          "</tr>"
        );
      }).join("");
    }
  });
})();
