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
 * XLT Modern Load Report — Executive Summary Renderer (`executive.html`)
 *
 * @fileoverview
 * Renders a concise, strictly factual summary for non-engineering stakeholders using
 * `window.XLT_META`, `window.XLT_OVERVIEW`, and `window.XLT_EXECUTIVE`:
 *   1. Scorecard verdict & group summary (only when `scorecard.xml` was evaluated).
 *   2. Test context / comment.
 *   3. Run facts & integrity strip (window, ramp-up, load config, think time, agent peaks, events).
 *   4. 4 headline factual KPI cards (Executed Flows, Flow Reliability, Server Request Timing, HTTP Status).
 *   5. Visual distribution bars (Server Request runtime buckets, HTTP status classes, Browser Web Vitals).
 *   6. Timeline progression with a 3-mode chart switcher (Interactive ECharts vs Static Key Charts vs Static 4-Up).
 *   7. Sortable Executed Flows (Transactions) table.
 *   8. 2-column triage: Failure & Event Concentration | Slowest Measured Items (Server Requests / Actions).
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var fmtDurationHms = UI.fmtDurationHms;

  var chartInstance = null;
  var chartResizeObserver = null;
  var activeTsTier = "tx";

  function fmtDec(n, digits) {
    return UI.fmtDec(n, digits === undefined ? 1 : digits);
  }

  /**
   * Formats a success percentage factually so non-zero failures never round up to "100.00%".
   */
  function fmtSuccessPct(okPct, errorCount) {
    var v = Number(okPct);
    if (Number.isNaN(v)) return "—";
    if ((Number(errorCount) || 0) > 0 && v >= 99.995) {
      return v >= 99.9995 ? ">99.999%" : fmtDec(v, 3) + "%";
    }
    return fmtDec(v, 2) + "%";
  }

  /**
   * Formats a non-zero error percentage so tiny rates never round down to "0.00%".
   */
  function fmtErrorPct(errPct, errorCount) {
    var v = Number(errPct);
    if (Number.isNaN(v)) return "—";
    if ((Number(errorCount) || 0) > 0 && v < 0.005) {
      return v < 0.0005 ? "<0.001%" : fmtDec(v, 3) + "%";
    }
    return fmtDec(v, 2) + "%";
  }

  /**
   * Formats milliseconds cleanly: sub-second values stay in `ms` (e.g. `133 ms`),
   * values >= 1,000 ms display in seconds (e.g. `1.88 s`, `28.5 s`).
   */
  function fmtMsReadable(ms) {
    if (ms === null || ms === undefined || ms === "" || Number.isNaN(Number(ms))) return "—";
    var v = Number(ms);
    if (v >= 10000) return fmtDec(v / 1000, 1) + " s";
    if (v >= 1000) return fmtDec(v / 1000, 2) + " s";
    return fmtInt(v) + " ms";
  }

  function getPct(item, pctStr) {
    var val = UI.getPercentile(item, pctStr);
    return val === undefined ? null : val;
  }

  function apdexBand(val) {
    var v = Number(val);
    if (Number.isNaN(v)) return { label: "", cls: "" };
    if (v >= 0.94) return { label: "Excellent", cls: "apdex-excellent" };
    if (v >= 0.85) return { label: "Good", cls: "apdex-good" };
    if (v >= 0.70) return { label: "Fair", cls: "apdex-fair" };
    if (v >= 0.50) return { label: "Poor", cls: "apdex-poor" };
    return { label: "Unacceptable", cls: "apdex-unacceptable" };
  }

  /** Renders an accessible toggletip (`popover="hint"` + `interestfor` + `popovertarget`). */
  function renderToggletip(id, label, bodyText) {
    return (
      '<button type="button" class="exec-tip-btn" ' +
        'interestfor="' + esc(id) + '" ' +
        'popovertarget="' + esc(id) + '" ' +
        'popovertargetaction="show" ' +
        'aria-label="' + esc(label) + '">?</button>' +
      '<div id="' + esc(id) + '" class="exec-tip-popover" popover="hint" role="tooltip">' +
        esc(bodyText) +
      "</div>"
    );
  }

  /**
   * Wires explicit ARIA (`aria-expanded`) and progressive fallback for `popover="hint"`
   * on browsers that only support `popover="auto"`/`"manual"`.
   */
  function initToggletips() {
    var ownedTriggers = [];
    var triggers = document.querySelectorAll(".exec-tip-btn[interestfor]");
    Array.prototype.forEach.call(triggers, function (btn) {
      var id = btn.getAttribute("interestfor");
      var tip = id ? document.getElementById(id) : null;
      if (!tip) return;
      if (!tip.hasAttribute("role")) tip.setAttribute("role", "tooltip");
      if (!btn.hasAttribute("aria-expanded")) {
        btn.setAttribute("aria-expanded", "false");
        ownedTriggers.push({ btn: btn, id: id });
      }
    });

    // Feature-detect `popover="hint"` support; fall back to `auto` in `beforetoggle` (capture phase)
    var probe = document.createElement("div");
    if ("popover" in probe) {
      probe.popover = "hint";
      if (probe.popover !== "hint") {
        document.addEventListener("beforetoggle", function (e) {
          var target = e.target;
          if (!target || !target.id) return;
          if (e.newState === "open" && target.getAttribute("popover") === "hint") {
            var hasBtn = document.querySelector('[popovertarget="' + CSS.escape(target.id) + '"]');
            if (hasBtn) target.popover = "auto";
          }
        }, true);
      }
    }

    // Keep `aria-expanded` synchronized from the popover's non-bubbling `toggle` event
    document.addEventListener("toggle", function (e) {
      var target = e.target;
      if (!target || !target.id) return;
      var isOpen = e.newState === "open";
      ownedTriggers.forEach(function (pair) {
        if (pair.id === target.id) {
          pair.btn.setAttribute("aria-expanded", String(isOpen));
        }
      });
    }, true);
  }

  /**
   * Initializes accessible tablists following WAI-ARIA + `focusgroup` / `hidden="until-found"`
   * with keyboard Arrow/Home/End fallback and `beforematch` sync for Find-in-Page.
   */
  function initTabGroup(tablistEl, tabAttr, panelAttr, onSelect) {
    if (!tablistEl) return null;
    var tabs = Array.prototype.slice.call(tablistEl.querySelectorAll('button[role="tab"][' + tabAttr + "]"));
    var scope = tablistEl.closest(".card") || document;
    var panels = Array.prototype.slice.call(scope.querySelectorAll('[role="tabpanel"][' + panelAttr + "]"));
    var nativeFocusgroup = Object.hasOwn && Object.hasOwn(HTMLElement.prototype, "focusGroup");

    function selectKey(key, focusTab) {
      var matchedTab = null;
      tabs.forEach(function (tab) {
        var isSel = tab.getAttribute(tabAttr) === key;
        tab.setAttribute("aria-selected", String(isSel));
        if (isSel) {
          tab.setAttribute("focusgroupstart", "");
          if (!nativeFocusgroup) tab.tabIndex = 0;
          matchedTab = tab;
        } else {
          tab.removeAttribute("focusgroupstart");
          if (!nativeFocusgroup) tab.tabIndex = -1;
        }
      });
      panels.forEach(function (panel) {
        var isSel = panel.getAttribute(panelAttr) === key;
        if (isSel) {
          panel.removeAttribute("hidden");
        } else {
          panel.hidden = "until-found";
        }
      });
      if (focusTab && matchedTab) {
        matchedTab.focus();
      }
      if (matchedTab && typeof matchedTab.scrollIntoView === "function") {
        try {
          matchedTab.scrollIntoView({ block: "nearest", inline: "nearest", container: "nearest" });
        } catch (_) {
          matchedTab.scrollIntoView({ block: "nearest", inline: "nearest" });
        }
      }
      if (typeof onSelect === "function") onSelect(key);
    }

    tabs.forEach(function (tab, idx) {
      if (!nativeFocusgroup) {
        tab.tabIndex = tab.getAttribute("aria-selected") === "true" ? 0 : -1;
      }
      tab.addEventListener("click", function () {
        selectKey(tab.getAttribute(tabAttr), false);
      });
      tab.addEventListener("keydown", function (e) {
        var nextIdx = -1;
        if (e.key === "ArrowRight") nextIdx = (idx + 1) % tabs.length;
        else if (e.key === "ArrowLeft") nextIdx = (idx - 1 + tabs.length) % tabs.length;
        else if (e.key === "Home") nextIdx = 0;
        else if (e.key === "End") nextIdx = tabs.length - 1;
        if (nextIdx >= 0) {
          e.preventDefault();
          selectKey(tabs[nextIdx].getAttribute(tabAttr), true);
        }
      });
    });

    panels.forEach(function (panel) {
      panel.addEventListener("beforematch", function () {
        selectKey(panel.getAttribute(panelAttr), false);
      });
    });

    return { select: selectKey };
  }

  // =========================================================================
  // 1. Scope Line, Scorecard Verdict, and Test Context
  // =========================================================================

  function renderScopeLine(ov, ex, meta) {
    var el = document.getElementById("exec-scope-line");
    if (!el) return;
    var parts = [];
    if (meta && meta.startTime) parts.push("<strong>" + esc(meta.startTime) + "</strong>");
    if (meta && meta.durationDisplay) parts.push("Duration: <strong>" + esc(meta.durationDisplay) + "</strong>");
    var flows = (ex && Array.isArray(ex.flows)) ? ex.flows : [];
    if (flows.length) parts.push("<strong>" + fmtInt(flows.length) + "</strong> transactions");
    var agCnt = ex && ex.agentHealth ? Number(ex.agentHealth.agentCount) || 0 : 0;
    if (agCnt > 0) parts.push("<strong>" + fmtInt(agCnt) + "</strong> load agents");
    el.innerHTML = parts.join(' <span class="exec-dot" aria-hidden="true">·</span> ');
  }

  function renderScorecard(ov, ex, meta) {
    var sec = document.getElementById("exec-verdict");
    if (!sec) return;
    var sc = (ov && ov.scorecard) || (meta && meta.scorecard);
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

    var tx = (ov && ov.summary && ov.summary.transactions) || {};
    var req = (ov && ov.summary && ov.summary.requests) || {};
    var repPctKey = (ex && ex.repPercentile) || UI.pickRepresentativePercentile(UI.getConfiguredPercentiles([req]));
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

  function renderComment(ov, meta) {
    var sec = document.getElementById("exec-comment");
    if (!sec) return;
    var raw = (ov && ov.comments) || (meta && meta.comments) || [];
    var items = Array.isArray(raw)
      ? raw.map(function (c) { return String(c == null ? "" : c).trim(); }).filter(Boolean)
      : [];
    if (!items.length) {
      sec.hidden = true;
      sec.innerHTML = "";
      return;
    }
    sec.hidden = false;
    sec.innerHTML =
      '<div class="comment-card">' +
        '<div class="comment-header"><div class="comment-title-group"><span class="comment-badge">Test Context</span></div></div>' +
        '<div class="comment-body">' +
          items.map(function (c) { return '<div class="paragraph">' + c + "</div>"; }).join("") +
        "</div>" +
      "</div>";
  }

  // =========================================================================
  // 2. Run Facts & Integrity Strip
  // =========================================================================

  function renderFactsStrip(ov, ex, meta) {
    var sec = document.getElementById("exec-facts-strip");
    if (!sec) return;

    var lpTotals = (ov && ov.loadProfileTotals) || {};
    var ag = (ex && ex.agentHealth) || {};
    var counts = (meta && meta.counts) || {};
    var totalEvents = Number(counts.events) || 0;

    var rampDisp = lpTotals.rampUpDisplay && lpTotals.rampUpDisplay !== "00:00:00" && lpTotals.rampUpDisplay !== "—"
      ? lpTotals.rampUpDisplay
      : null;
    var thinkDisp = lpTotals.thinkTimeDisplay && lpTotals.thinkTimeDisplay !== "—" && lpTotals.thinkTimeDisplay !== "0 ms"
      ? lpTotals.thinkTimeDisplay
      : null;
    var targetArrDisp = lpTotals.arrivalDisplay && lpTotals.arrivalDisplay !== "0" && lpTotals.arrivalDisplay !== "–"
      ? lpTotals.arrivalDisplay + " /h"
      : null;

    var cells = [];

    // Load Target
    var loadVal = (lpTotals.usersDisplay ? esc(lpTotals.usersDisplay) + " users" : "—");
    var loadSub = targetArrDisp ? "Arrival target: " + esc(targetArrDisp) : "User-driven load";
    cells.push(
      '<div class="exec-fact-cell">' +
        '<div class="exec-fact-k">Load Profile</div>' +
        '<div class="exec-fact-v">' + loadVal + "</div>" +
        '<div class="exec-fact-sub">' + loadSub + "</div>" +
      "</div>"
    );

    // Measurement & Ramp-up
    var measVal = esc((meta && meta.durationDisplay) || lpTotals.measurementDisplay || "—");
    var measSub = rampDisp ? "Includes ramp-up: " + esc(rampDisp) : "Steady window (no ramp-up listed)";
    cells.push(
      '<div class="exec-fact-cell">' +
        '<div class="exec-fact-k">Test Window</div>' +
        '<div class="exec-fact-v">' + measVal + "</div>" +
        '<div class="exec-fact-sub">' + measSub + "</div>" +
      "</div>"
    );

    // Think Time (factual note so transaction duration is read accurately)
    cells.push(
      '<div class="exec-fact-cell">' +
        '<div class="exec-fact-k">Configured Think Time</div>' +
        '<div class="exec-fact-v">' + esc(thinkDisp || "None / 0 ms") + "</div>" +
        '<div class="exec-fact-sub">' + (thinkDisp ? "Included in transaction durations" : "Back-to-back execution") + "</div>" +
      "</div>"
    );

    // Load Generator Integrity
    var agCount = Number(ag.agentCount) || Number(counts.agents) || 0;
    var peakAgentCpu = Number(ag.peakSingleAgentCpu) || 0;
    var peakHostCpu = Number(ag.peakSingleHostCpu) || 0;
    var fullGcs = Number(ag.fullGcCount) || 0;
    var agentCaution = peakAgentCpu >= 80 || peakHostCpu >= 90 || fullGcs > 0;
    var agValHtml = agCount > 0
      ? (agentCaution
          ? '<span class="status-pill warn">Caution</span> ' + fmtInt(agCount) + " agents"
          : fmtInt(agCount) + " agents")
      : "No agent telemetry";
    var agSubHtml = agCount > 0
      ? "Agent CPU max: " + fmtDec(peakAgentCpu, 1) + "% · Host peak: " + fmtDec(peakHostCpu, 1) + "%" +
        (fullGcs > 0 ? " · Full GCs: " + fmtInt(fullGcs) : " · 0 Full GCs")
      : "Agent metrics not recorded";
    cells.push(
      '<a href="agents.html" class="exec-fact-cell exec-fact-link">' +
        '<div class="exec-fact-k">Load Generators</div>' +
        '<div class="exec-fact-v">' + agValHtml + "</div>" +
        '<div class="exec-fact-sub">' + agSubHtml + "</div>" +
      "</a>"
    );

    // Events
    var evValHtml = totalEvents > 0
      ? '<span class="status-pill warn">' + fmtInt(totalEvents) + "</span>"
      : "0";
    cells.push(
      '<a href="events.html" class="exec-fact-cell exec-fact-link">' +
        '<div class="exec-fact-k">Logged Events ' +
          renderToggletip("tip-events", "About logged events", "Non-fatal warnings or custom anomalies logged by test code during execution. Events do not mark a transaction as failed.") +
        "</div>" +
        '<div class="exec-fact-v">' + evValHtml + "</div>" +
        '<div class="exec-fact-sub">' + (totalEvents > 0 ? "Non-fatal test warnings" : "No events recorded") + "</div>" +
      "</a>"
    );

    sec.innerHTML = cells.join("");
  }

  // =========================================================================
  // 3. Headline Factual KPI Cards
  // =========================================================================

  function renderKpis(ov, ex, meta) {
    var sec = document.getElementById("exec-kpis");
    if (!sec) return;

    var tx = (ov && ov.summary && ov.summary.transactions) || {};
    var act = (ov && ov.summary && ov.summary.actions) || {};
    var req = (ov && ov.summary && ov.summary.requests) || {};
    var lpTotals = (ov && ov.loadProfileTotals) || {};
    var flows = (ex && Array.isArray(ex.flows)) ? ex.flows : [];
    var http = (ex && ex.httpStatus) || {};

    var repPctKey = (ex && ex.repPercentile) || UI.pickRepresentativePercentile(UI.getConfiguredPercentiles([req])) || "95";
    var reqP50 = getPct(req, "50");
    var reqRep = getPct(req, repPctKey);
    var reqP99 = getPct(req, "99");

    // Flow Reliability + Error concentration
    var txCount = Number(tx.count) || 0;
    var txErrors = Number(tx.errors) || 0;
    var txErrPct = Number(tx.errPct) || 0;
    var txOkPct = txCount > 0 ? Math.max(0, 100 - txErrPct) : 0;

    var topErrFlow = null;
    flows.forEach(function (f) {
      var e = Number(f.errors) || 0;
      if (e > 0 && (!topErrFlow || e > (Number(topErrFlow.errors) || 0))) {
        topErrFlow = f;
      }
    });
    var errConcText = "0 failed transactions recorded";
    if (txErrors > 0) {
      errConcText = fmtInt(txErrors) + " failed (" + fmtDec(txErrPct, 3) + "%)";
      if (topErrFlow) {
        var topShare = (Number(topErrFlow.errors) * 100) / txErrors;
        errConcText += " · Top: " + esc(topErrFlow.name) + " (" + fmtDec(topShare, 0) + "%)";
      }
    }

    // HTTP 2xx+3xx share
    var httpTot = Number(http.total) || 0;
    var httpOk = (Number(http.c2xx) || 0) + (Number(http.c3xx) || 0);
    var httpOkPct = httpTot > 0 ? (httpOk * 100) / httpTot : 100;
    var c4xx = Number(http.c4xx) || 0;
    var c5xx = Number(http.c5xx) || 0;
    var c0 = Number(http.c0) || 0;

    var apdexObj = act.apdex || null;
    var apdexHtml = "";
    if (apdexObj && apdexObj.value !== undefined) {
      var b = apdexBand(apdexObj.value);
      apdexHtml =
        '<div class="exec-kpi-extra">Action Apdex: <span class="apdex-pill ' + b.cls + '">' +
          esc(apdexObj.longValue || fmtDec(apdexObj.value, 2)) + (b.label ? " · " + b.label : "") +
        "</span> " +
        renderToggletip("tip-apdex", "About Action Apdex", "Application Performance Index (0.00–1.00) computed on measured Actions against the configured threshold T shown in brackets.") +
        "</div>";
    }

    var targetArrSub = lpTotals.arrivalDisplay && lpTotals.arrivalDisplay !== "0" && lpTotals.arrivalDisplay !== "–"
      ? " · Target: " + esc(lpTotals.arrivalDisplay) + " /h"
      : "";

    sec.innerHTML =
      '<article class="kpi-card">' +
        '<span class="kpi-label">Transactions ' +
          renderToggletip("tip-flows", "About Transactions", "Completed end-to-end test scenarios (Transactions).") +
        "</span>" +
        '<div class="kpi-val">' + fmtInt(txCount) + "</div>" +
        '<div class="kpi-sub">' + fmtInt(tx.cph || 0) + " /h (" + fmtDec(tx.rate || 0, 2) + " /s)" + targetArrSub + "</div>" +
      "</article>" +

      '<article class="kpi-card">' +
        '<span class="kpi-label">Transaction Success Rate</span>' +
        '<div class="kpi-val"' + (txErrors === 0 && txCount > 0 ? ' style="color:var(--status-pass-text);"' : (txOkPct < 99 ? ' style="color:var(--status-fail-text);"' : "")) + ">" +
          (txCount > 0 ? fmtSuccessPct(txOkPct, txErrors) : "—") +
        "</div>" +
        '<div class="kpi-sub">' + errConcText + "</div>" +
      "</article>" +

      '<article class="kpi-card">' +
        '<span class="kpi-label">Server Request Time ' +
          renderToggletip("tip-req-time", "About Server Request Time", "Mean and percentiles across all HTTP server requests. P" + repPctKey + " means " + repPctKey + "% of requests completed faster than this value.") +
        "</span>" +
        '<div class="kpi-val">' + fmtMsReadable(req.mean) + ' <span class="exec-kpi-unit">mean</span></div>' +
        '<div class="kpi-sub">' +
          (reqP50 !== null ? "P50: " + fmtMsReadable(reqP50) + " · " : "") +
          (reqRep !== null ? "P" + esc(repPctKey) + ": <strong>" + fmtMsReadable(reqRep) + "</strong>" : "") +
          (reqP99 !== null ? " · P99: " + fmtMsReadable(reqP99) : "") +
          "<br>" + fmtInt(req.count || 0) + " requests (" + fmtDec(req.rate || 0, 1) + " req/s)" +
        "</div>" +
      "</article>" +

      '<article class="kpi-card">' +
        '<span class="kpi-label">HTTP 2xx / 3xx Responses</span>' +
        '<div class="kpi-val">' +
          (httpTot > 0 ? fmtSuccessPct(httpOkPct, c4xx + c5xx + c0) : "—") +
        "</div>" +
        '<div class="kpi-sub">' +
          "4xx: " + fmtInt(c4xx) +
          " · 5xx: " + (c5xx > 0 ? '<strong class="cell-err">' + fmtInt(c5xx) + "</strong>" : "0") +
          (c0 > 0 ? ' · Timeout/0: <strong class="cell-err">' + fmtInt(c0) + "</strong>" : "") +
        "</div>" +
        apdexHtml +
      "</article>";
  }

  // =========================================================================
  // 4. Distribution Bars (Request Runtime Thresholds + HTTP Status [+ Web Vitals])
  // =========================================================================

  function renderDistributions(ov, ex, meta) {
    var sec = document.getElementById("exec-distributions");
    if (!sec) return;

    var cards = [];

    // Card 1: Server Request Runtime Distribution (from runtimeIntervals & cumulative countPerInterval)
    var req = (ov && ov.summary && ov.summary.requests) || {};
    var intervals = (meta && meta.testReportConfig && Array.isArray(meta.testReportConfig.runtimeIntervals))
      ? meta.testReportConfig.runtimeIntervals
      : [];
    var seg = Array.isArray(req.segmentation) ? req.segmentation : [];
    var reqTotal = Number(req.count) || 0;

    if (intervals.length && seg.length === intervals.length && reqTotal > 0) {
      var palette = ["#059669", "#244b73", "#d97706", "#dc2626", "#7c2d12"];
      var slices = [];
      var prevCum = 0;
      for (var i = 0; i < intervals.length; i++) {
        var iv = intervals[i];
        var rawCnt = Number(seg[i].count) || 0;
        var isLastOpen = iv.to === null || iv.to === undefined;
        var sliceCnt = isLastOpen ? rawCnt : Math.max(0, rawCnt - prevCum);
        if (!isLastOpen) prevCum = rawCnt;
        var pct = (sliceCnt * 100) / reqTotal;
        var label;
        if (i === 0 && (iv.from === 0 || iv.from === null || iv.from === undefined) && !isLastOpen) {
          label = "≤ " + fmtMsReadable(iv.to);
        } else if (isLastOpen) {
          label = "> " + fmtMsReadable(iv.from);
        } else {
          label = fmtMsReadable(iv.from) + " – " + fmtMsReadable(iv.to);
        }
        slices.push({
          label: label,
          count: sliceCnt,
          pct: pct,
          color: palette[Math.min(i, palette.length - 1)]
        });
      }

      var barSegs = slices.map(function (s) {
        if (s.count <= 0) return "";
        var w = Math.max(s.pct, 0.4);
        return '<span class="exec-stack-seg" style="flex-grow:' + w.toFixed(4) + ";background:" + s.color + ';" title="' + esc(s.label) + ": " + fmtInt(s.count) + " (" + fmtDec(s.pct, 2) + '%)"></span>';
      }).join("");

      var legendRows = slices.map(function (s) {
        return (
          '<div class="exec-dist-row">' +
            '<span class="exec-dist-swatch" style="background:' + s.color + ';"></span>' +
            '<span class="exec-dist-label">' + esc(s.label) + "</span>" +
            '<span class="exec-dist-val">' + fmtDec(s.pct, 2) + '% <span class="exec-dist-cnt">(' + fmtInt(s.count) + ")</span></span>" +
          "</div>"
        );
      }).join("");

      cards.push(
        '<div class="card exec-dist-card">' +
          '<div class="exec-card-header">' +
            '<div class="profile-title-block">' +
              '<div class="profile-title">Server Request Runtime Distribution</div>' +
              '<div class="profile-subtitle">Configured runtime intervals across ' + fmtInt(reqTotal) + " HTTP requests</div>" +
            "</div>" +
            '<a href="requests.html" class="exec-mini-link">Requests →</a>' +
          "</div>" +
          '<div class="exec-dist-body">' +
            '<div class="exec-stack-bar" role="img" aria-label="Server request runtime distribution">' + barSegs + "</div>" +
            '<div class="exec-dist-legend">' + legendRows + "</div>" +
          "</div>" +
        "</div>"
      );
    }

    // Card 2: HTTP Status Breakdown
    var http = (ex && ex.httpStatus) || {};
    var hTot = Number(http.total) || 0;
    if (hTot > 0) {
      var hSlices = [
        { label: "2xx Success", count: Number(http.c2xx) || 0, color: "#059669" },
        { label: "3xx Redirect", count: Number(http.c3xx) || 0, color: "#244b73" },
        { label: "4xx Client", count: Number(http.c4xx) || 0, color: "#d97706" },
        { label: "5xx Server", count: Number(http.c5xx) || 0, color: "#dc2626" },
        { label: "0 Conn / Timeout", count: Number(http.c0) || 0, color: "#991b1b" }
      ];
      var hBar = hSlices.map(function (s) {
        if (s.count <= 0) return "";
        var pct = (s.count * 100) / hTot;
        return '<span class="exec-stack-seg" style="flex-grow:' + Math.max(pct, 0.4).toFixed(4) + ";background:" + s.color + ';" title="' + esc(s.label) + ": " + fmtInt(s.count) + " (" + fmtDec(pct, 2) + '%)"></span>';
      }).join("");

      var hLegend = hSlices.filter(function (s) { return s.count > 0 || s.label.indexOf("2xx") === 0 || s.label.indexOf("5xx") === 0; }).map(function (s) {
        var pct = (s.count * 100) / hTot;
        return (
          '<div class="exec-dist-row">' +
            '<span class="exec-dist-swatch" style="background:' + s.color + ';"></span>' +
            '<span class="exec-dist-label">' + esc(s.label) + "</span>" +
            '<span class="exec-dist-val">' + fmtDec(pct, 2) + '% <span class="exec-dist-cnt">(' + fmtInt(s.count) + ")</span></span>" +
          "</div>"
        );
      }).join("");

      var topNonOk = Array.isArray(http.topNonOk) ? http.topNonOk : [];
      var nonOkPills = topNonOk.length
        ? '<div class="exec-nonok-strip">' +
            topNonOk.slice(0, 5).map(function (rc) {
              var code = Number(rc.code) || 0;
              var cls = (code === 0 || code >= 500) ? "fail" : "warn";
              return '<span class="status-pill ' + cls + '">HTTP ' + code + ": " + fmtInt(rc.count) + "</span>";
            }).join("") +
          "</div>"
        : "";

      cards.push(
        '<div class="card exec-dist-card">' +
          '<div class="exec-card-header">' +
            '<div class="profile-title-block">' +
              '<div class="profile-title">HTTP Response Status Breakdown</div>' +
              '<div class="profile-subtitle">All recorded HTTP status classes (' + fmtInt(hTot) + " responses)</div>" +
            "</div>" +
            '<a href="network.html#sec-status" class="exec-mini-link">Network →</a>' +
          "</div>" +
          '<div class="exec-dist-body">' +
            '<div class="exec-stack-bar" role="img" aria-label="HTTP status code distribution">' + hBar + "</div>" +
            '<div class="exec-dist-legend">' + hLegend + "</div>" +
            nonOkPills +
          "</div>" +
        "</div>"
      );
    }

    // Card 3 (conditional): Browser Core Web Vitals (only when browser vitals were recorded)
    var wv = (ex && ex.webVitals) || {};
    if (wv.present) {
      var wvMetrics = wv.metrics || {};
      var keys = ["lcp", "inp", "cls", "fcp", "ttfb"].filter(function (k) { return wvMetrics[k]; });
      var wvRows = keys.map(function (k) {
        var m = wvMetrics[k];
        var tot = (Number(m.good) || 0) + (Number(m.improve) || 0) + (Number(m.poor) || 0);
        var gPct = tot > 0 ? (m.good * 100) / tot : 0;
        var iPct = tot > 0 ? (m.improve * 100) / tot : 0;
        var pPct = tot > 0 ? (m.poor * 100) / tot : 0;
        return (
          '<div class="exec-wv-row">' +
            '<span class="exec-wv-name">' + k.toUpperCase() + "</span>" +
            '<div class="exec-stack-bar exec-stack-bar-sm">' +
              (m.good > 0 ? '<span class="exec-stack-seg" style="flex-grow:' + m.good + ';background:#059669;" title="Good: ' + fmtDec(gPct, 1) + '%"></span>' : "") +
              (m.improve > 0 ? '<span class="exec-stack-seg" style="flex-grow:' + m.improve + ';background:#d97706;" title="Needs Improvement: ' + fmtDec(iPct, 1) + '%"></span>' : "") +
              (m.poor > 0 ? '<span class="exec-stack-seg" style="flex-grow:' + m.poor + ';background:#dc2626;" title="Poor: ' + fmtDec(pPct, 1) + '%"></span>' : "") +
            "</div>" +
            '<span class="exec-dist-val">' + fmtDec(gPct, 0) + "% good</span>" +
          "</div>"
        );
      }).join("");

      cards.push(
        '<div class="card exec-dist-card">' +
          '<div class="exec-card-header">' +
            '<div class="profile-title-block">' +
              '<div class="profile-title">Browser Core Web Vitals</div>' +
              '<div class="profile-subtitle">' + fmtInt(wv.actionCount) + " browser actions (" + fmtInt(wv.goodActions) + " good · " + fmtInt(wv.improveActions) + " improve · " + fmtInt(wv.poorActions) + " poor)</div>" +
            "</div>" +
            '<a href="web-vitals.html" class="exec-mini-link">Web Vitals →</a>' +
          "</div>" +
          '<div class="exec-dist-body">' +
            '<div class="exec-wv-list">' + wvRows + "</div>" +
          "</div>" +
        "</div>"
      );
    }

    if (!cards.length) {
      sec.hidden = true;
      return;
    }
    sec.hidden = false;
    sec.className = "exec-dist-grid" + (cards.length === 3 ? " is-three" : (cards.length === 1 ? " is-one" : ""));
    sec.innerHTML = cards.join("");
  }

  // =========================================================================
  // 5. Timeline Progression (Interactive ECharts + Static 2-Up + Static 4-Up)
  // =========================================================================

  function themePalette() {
    var dark = document.documentElement.classList.contains("dark-theme");
    return {
      dark: dark,
      text: dark ? "#d4d4d8" : "#3f444d",
      muted: dark ? "#9ca3af" : "#666e7a",
      grid: dark ? "#272a30" : "#e5e7eb",
      axis: dark ? "#393e47" : "#c9d0d8",
      tooltipBg: dark ? "#181a1f" : "#ffffff",
      tooltipBorder: dark ? "#393e47" : "#c9d0d8",
      blue: dark ? "#60a5fa" : "#244b73",
      blueFill: dark ? "rgba(96, 165, 250, 0.16)" : "rgba(36, 75, 115, 0.14)",
      amber: dark ? "#fbbf24" : "#d97706",
      teal: dark ? "#34d399" : "#059669",
      red: dark ? "#f87171" : "#dc2626"
    };
  }

  function formatAxisTime(row) {
    if (row && row.time) {
      var m = String(row.time).match(/(\d{2}:\d{2})(?::\d{2})?/);
      if (m) return m[1];
    }
    return fmtDurationHms((row && row.elapsed) || 0);
  }

  function renderEchartTimeline(ex) {
    var stage = document.getElementById("exec-echart-stage");
    var controls = document.getElementById("exec-echart-controls");
    var metaEl = document.getElementById("exec-echart-meta");
    if (!stage) return;

    var ts = ex && ex.timeSeries;
    if (!ts || !Array.isArray(ts.rows) || !ts.rows.length || !window.echarts) {
      if (controls) controls.hidden = true;
      stage.innerHTML =
        '<div class="exec-empty-box">No time-series bucket data available in this report. Use the Static chart tabs to view generated WebP charts.</div>';
      return;
    }

    if (controls) controls.hidden = false;
    var rows = ts.rows;
    var intervalSec = Number(ts.intervalSec) || 60;
    var bucketLabel = intervalSec === 60 ? "min" : (intervalSec + "s");
    var hasErrorsSeries = Boolean(ts.hasErrorSeries);

    if (metaEl) {
      metaEl.textContent =
        fmtInt(rows.length) + " intervals (" + intervalSec + " s/bucket" +
        (!hasErrorsSeries ? " · legacy XML without <timeSeries> error data" : "") + ")";
    }

    var tierCfg = {
      tx: { rateKey: "txRate", rateLabel: "Transactions /s" },
      act: { rateKey: "actRate", rateLabel: "Actions /s" },
      req: { rateKey: "reqRate", rateLabel: "Requests /s" }
    };
    var cfg = tierCfg[activeTsTier] || tierCfg.tx;
    var p = themePalette();

    var labels = rows.map(formatAxisTime);
    var rateVals = rows.map(function (r) { return Number(r[cfg.rateKey]) || 0; });
    var reqMeanVals = rows.map(function (r) { return Number(r.reqMean) || 0; });
    var actMeanVals = rows.map(function (r) { return Number(r.actMean) || 0; });
    var txMeanVals = rows.map(function (r) { return Number(r.txMean) || 0; });

    // Convert error rates (/s) to per-bucket error counts so small error bursts stay legible
    var txErrCounts = rows.map(function (r) { return Math.round((Number(r.txErr) || 0) * intervalSec); });
    var actErrCounts = rows.map(function (r) { return Math.round((Number(r.actErr) || 0) * intervalSec); });
    var reqErrCounts = rows.map(function (r) { return Math.round((Number(r.reqErr) || 0) * intervalSec); });

    var hasTxErr = hasErrorsSeries && txErrCounts.some(function (v) { return v > 0; });
    var hasActErr = hasErrorsSeries && actErrCounts.some(function (v) { return v > 0; });
    var hasReqErr = hasErrorsSeries && reqErrCounts.some(function (v) { return v > 0; });

    // Band 1: Throughput
    var series = [
      {
        name: cfg.rateLabel,
        type: "line",
        xAxisIndex: 0,
        yAxisIndex: 0,
        smooth: 0.15,
        showSymbol: false,
        lineStyle: { width: 2, color: p.blue },
        itemStyle: { color: p.blue },
        areaStyle: { color: p.blueFill },
        data: rateVals
      }
    ];

    // Band 2: Mean Response Time
    if (activeTsTier === "req") {
      series.push({
        name: "Server Request Mean",
        type: "line",
        xAxisIndex: 1,
        yAxisIndex: 1,
        smooth: 0.15,
        showSymbol: false,
        lineStyle: { width: 2, color: p.teal },
        itemStyle: { color: p.teal },
        data: reqMeanVals
      });
    } else if (activeTsTier === "act") {
      series.push({
        name: "Action Mean",
        type: "line",
        xAxisIndex: 1,
        yAxisIndex: 1,
        smooth: 0.15,
        showSymbol: false,
        lineStyle: { width: 2, color: p.amber },
        itemStyle: { color: p.amber },
        data: actMeanVals
      });
      series.push({
        name: "Server Request Mean",
        type: "line",
        xAxisIndex: 1,
        yAxisIndex: 1,
        smooth: 0.15,
        showSymbol: false,
        lineStyle: { width: 1.5, color: p.teal, type: "dashed" },
        itemStyle: { color: p.teal },
        data: reqMeanVals
      });
    } else {
      series.push({
        name: "Server Request Mean",
        type: "line",
        xAxisIndex: 1,
        yAxisIndex: 1,
        smooth: 0.15,
        showSymbol: false,
        lineStyle: { width: 2, color: p.teal },
        itemStyle: { color: p.teal },
        data: reqMeanVals
      });
      series.push({
        name: "Action Mean",
        type: "line",
        xAxisIndex: 1,
        yAxisIndex: 1,
        smooth: 0.15,
        showSymbol: false,
        lineStyle: { width: 1.75, color: p.amber, type: "dashed" },
        itemStyle: { color: p.amber },
        data: actMeanVals
      });
    }

    // Band 3: Errors (always shown independently of throughput tier selection)
    if (hasErrorsSeries) {
      series.push({
        name: "Tx Errors",
        type: "bar",
        xAxisIndex: 2,
        yAxisIndex: 2,
        barMaxWidth: 10,
        itemStyle: { color: p.red, opacity: 0.9 },
        data: txErrCounts
      });
      if (hasActErr) {
        series.push({
          name: "Action Errors",
          type: "bar",
          xAxisIndex: 2,
          yAxisIndex: 2,
          barMaxWidth: 10,
          itemStyle: { color: p.amber, opacity: 0.85 },
          data: actErrCounts
        });
      }
      if (hasReqErr) {
        series.push({
          name: "Request Errors",
          type: "bar",
          xAxisIndex: 2,
          yAxisIndex: 2,
          barMaxWidth: 10,
          itemStyle: { color: "#9333ea", opacity: 0.85 },
          data: reqErrCounts
        });
      }
    }

    if (!chartInstance) {
      chartInstance = window.echarts.init(stage);
      if (typeof ResizeObserver !== "undefined") {
        chartResizeObserver = new ResizeObserver(function () {
          if (chartInstance && !chartInstance.isDisposed()) chartInstance.resize();
        });
        chartResizeObserver.observe(stage);
      }
    }

    var errMax = hasErrorsSeries
      ? Math.max.apply(null, [1].concat(hasTxErr ? txErrCounts : []).concat(hasActErr ? actErrCounts : []).concat(hasReqErr ? reqErrCounts : []))
      : 1;

    chartInstance.setOption({
      animation: false,
      tooltip: {
        trigger: "axis",
        axisPointer: { type: "line", link: [{ xAxisIndex: "all" }] },
        backgroundColor: p.tooltipBg,
        borderColor: p.tooltipBorder,
        borderWidth: 1,
        textStyle: { color: p.text, fontSize: 12 },
        formatter: function (params) {
          if (!params || !params.length) return "";
          var idx = params[0].dataIndex;
          var r = rows[idx] || {};
          var header = '<div style="font-weight:700;margin-bottom:4px;">' + esc(r.time || labels[idx]) + "</div>";
          var lines = params.map(function (pt) {
            var valStr;
            if (pt.seriesName.indexOf("Mean") !== -1) {
              valStr = fmtMsReadable(pt.value);
            } else if (pt.seriesName.indexOf("Errors") !== -1) {
              valStr = fmtInt(pt.value) + " / " + bucketLabel;
            } else {
              valStr = fmtDec(pt.value, 2) + " /s";
            }
            return (
              '<div style="display:flex;justify-content:space-between;gap:14px;">' +
                "<span>" + pt.marker + esc(pt.seriesName) + "</span>" +
                "<strong>" + esc(valStr) + "</strong>" +
              "</div>"
            );
          });
          if (activeTsTier === "tx" && txMeanVals[idx] > 0) {
            lines.push(
              '<div style="display:flex;justify-content:space-between;gap:14px;margin-top:3px;padding-top:3px;border-top:1px solid ' + p.grid + ';color:' + p.muted + ';">' +
                "<span>Transaction Mean</span><strong>" + esc(fmtMsReadable(txMeanVals[idx])) + "</strong>" +
              "</div>"
            );
          }
          return header + lines.join("");
        }
      },
      legend: {
        top: 0,
        left: 0,
        textStyle: { color: p.muted, fontSize: 11.5 },
        itemWidth: 14,
        itemHeight: 8
      },
      axisPointer: { link: [{ xAxisIndex: "all" }] },
      grid: [
        { left: 58, right: 20, top: 44, height: "22%" },
        { left: 58, right: 20, top: "39%", height: "22%" },
        { left: 58, right: 20, top: "73%", height: "16%" }
      ],
      xAxis: [
        {
          type: "category",
          gridIndex: 0,
          data: labels,
          boundaryGap: false,
          axisLine: { lineStyle: { color: p.axis } },
          axisLabel: { show: false }
        },
        {
          type: "category",
          gridIndex: 1,
          data: labels,
          boundaryGap: false,
          axisLine: { lineStyle: { color: p.axis } },
          axisLabel: { show: false }
        },
        {
          type: "category",
          gridIndex: 2,
          data: labels,
          boundaryGap: true,
          axisLine: { lineStyle: { color: p.axis } },
          axisLabel: { color: p.muted, fontSize: 11 }
        }
      ],
      yAxis: [
        {
          type: "value",
          gridIndex: 0,
          name: cfg.rateLabel,
          nameGap: 8,
          nameTextStyle: { color: p.muted, fontSize: 11, align: "left" },
          splitNumber: 2,
          splitLine: { lineStyle: { color: p.grid } },
          axisLabel: { color: p.muted, fontSize: 11 }
        },
        {
          type: "value",
          gridIndex: 1,
          name: "Response Time (ms)",
          nameGap: 8,
          nameTextStyle: { color: p.muted, fontSize: 11, align: "left" },
          splitNumber: 2,
          splitLine: { lineStyle: { color: p.grid } },
          axisLabel: { color: p.muted, fontSize: 11 }
        },
        {
          type: "value",
          gridIndex: 2,
          min: 0,
          max: (!hasTxErr && !hasActErr && !hasReqErr) ? 1 : Math.ceil(errMax * 1.15),
          minInterval: 1,
          name: hasErrorsSeries ? ("Errors / " + bucketLabel) : "Errors (unavailable in legacy XML)",
          nameGap: 8,
          nameTextStyle: { color: hasErrorsSeries ? p.red : p.muted, fontSize: 11, align: "left" },
          splitNumber: 2,
          splitLine: { lineStyle: { color: p.grid } },
          axisLabel: { color: p.muted, fontSize: 11 }
        }
      ],
      graphic: !hasErrorsSeries
        ? [{
            type: "text",
            left: "center",
            top: "79%",
            style: {
              text: "Error time series not present in this older testreport.xml — use Static: Key Charts for Errors.webp",
              fill: p.muted,
              fontSize: 11.5
            }
          }]
        : [],
      dataZoom: [
        { type: "inside", xAxisIndex: [0, 1, 2] },
        {
          type: "slider",
          xAxisIndex: [0, 1, 2],
          height: 16,
          bottom: 4,
          borderColor: p.grid,
          textStyle: { color: p.muted, fontSize: 10 }
        }
      ],
      series: series
    }, true);
  }

  function renderStaticCharts(ov, ex, meta) {
    var grid2 = document.getElementById("exec-static2-grid");
    var grid4 = document.getElementById("exec-static4-grid");
    if (!grid2 && !grid4) return;

    if (meta && meta.chartsGenerated === false) {
      var msg = '<div class="exec-empty-box">Static WebP charts were not generated for this report (<code>-noCharts</code>).</div>';
      if (grid2) grid2.innerHTML = msg;
      if (grid4) grid4.innerHTML = msg;
      return;
    }

    var tx = (ov && ov.summary && ov.summary.transactions) || {};
    var req = (ov && ov.summary && ov.summary.requests) || {};
    var lpTotals = (ov && ov.loadProfileTotals) || {};
    var repPctKey = (ex && ex.repPercentile) || UI.pickRepresentativePercentile(UI.getConfiguredPercentiles([req])) || "95";
    var repPctVal = getPct(req, repPctKey);
    var hasErrors = (Number(tx.errors) || 0) > 0;

    var cardUsers =
      '<div class="chart-panel">' +
        '<div class="chart-panel-header"><div class="chart-panel-title">Concurrent Users</div><span class="chart-header-badge">' +
          (lpTotals.usersDisplay ? "Target: " + esc(lpTotals.usersDisplay) : "Active Users") +
        "</span></div>" +
        '<div class="chart-panel-body"><img class="zoomable-chart" src="charts/ConcurrentUsers.webp" alt="Concurrent Users Progression" data-chart-title="Concurrent Users Progression" loading="lazy" decoding="async" /></div>' +
      "</div>";

    var cardRps =
      '<div class="chart-panel">' +
        '<div class="chart-panel-header"><div class="chart-panel-title">Server Requests per Second</div><span class="chart-header-badge">Avg: ' + fmtDec(req.rate || 0, 1) + " req/s</span></div>" +
        '<div class="chart-panel-body"><img class="zoomable-chart" src="charts/RequestsPerSecond.webp" alt="Requests per Second Progression" data-chart-title="Requests per Second Progression" loading="lazy" decoding="async" /></div>' +
      "</div>";

    var cardRuntime =
      '<div class="chart-panel">' +
        '<div class="chart-panel-header"><div class="chart-panel-title">Server Request Runtime</div><span class="chart-header-badge">Mean: ' + fmtMsReadable(req.mean) +
          (repPctVal !== null ? " · P" + esc(repPctKey) + ": " + fmtMsReadable(repPctVal) : "") +
        "</span></div>" +
        '<div class="chart-panel-body"><img class="zoomable-chart" src="charts/RequestRuntime.webp" alt="Request Runtime Progression" data-chart-title="Request Runtime Progression" loading="lazy" decoding="async" /></div>' +
      "</div>";

    var cardErrors =
      '<div class="chart-panel">' +
        '<div class="chart-panel-header"><div class="chart-panel-title">Transaction Errors</div><span class="chart-header-badge"' + (hasErrors ? ' style="color:var(--status-fail-text);"' : "") + ">" +
          fmtInt(tx.errors || 0) + " (" + fmtDec(tx.errPct || 0, 3) + "%)" +
        "</span></div>" +
        '<div class="chart-panel-body"><img class="zoomable-chart" src="charts/TransactionErrors.webp" alt="Transaction Errors Progression" data-chart-title="Transaction Errors Progression" loading="lazy" decoding="async" /></div>' +
      "</div>";

    if (grid2) {
      grid2.className = "exec-static-grid is-three";
      grid2.innerHTML = cardUsers + cardRuntime + cardErrors;
    }
    if (grid4) {
      grid4.innerHTML = cardUsers + cardRps + cardRuntime + cardErrors;
    }
  }

  // =========================================================================
  // 6. Reliability & Failures: Flows with Errors (no transaction runtimes)
  // =========================================================================

  function renderFlowsReliability(ov, ex) {
    var titleEl = document.getElementById("exec-flows-title");
    var subEl = document.getElementById("exec-flows-subtitle");
    var tabErrBtn = document.getElementById("tab-flows-err");
    var tabAllBtn = document.getElementById("tab-flows-all");
    var errBody = document.getElementById("exec-flows-err-body");
    var allBody = document.getElementById("exec-flows-all-body");
    if (!errBody || !allBody) return;

    var flows = (ex && Array.isArray(ex.flows)) ? ex.flows : [];
    var failingFlows = flows.filter(function (f) { return (Number(f.errors) || 0) > 0; });
    failingFlows.sort(function (a, b) {
      var diff = (Number(b.errors) || 0) - (Number(a.errors) || 0);
      return diff !== 0 ? diff : (Number(b.count) || 0) - (Number(a.count) || 0);
    });

    var cleanCount = flows.length - failingFlows.length;
    var hasTargets = flows.some(function (f) {
      return f.targetArrivalRate && String(f.targetArrivalRate) !== "0" && String(f.targetArrivalRate) !== "–";
    });

    if (titleEl) {
      titleEl.textContent = failingFlows.length > 0 ? "Transactions with Errors" : "Transaction Reliability";
    }
    if (subEl) {
      subEl.textContent = flows.length
        ? fmtInt(failingFlows.length) + " of " + fmtInt(flows.length) + " transaction(s) recorded errors"
        : "No transaction records in this report";
    }
    if (tabErrBtn) tabErrBtn.textContent = "With Errors (" + fmtInt(failingFlows.length) + ")";
    if (tabAllBtn) tabAllBtn.textContent = "All (" + fmtInt(flows.length) + ")";

    function buildFlowsList(list, showCleanNote) {
      if (!list.length) {
        return '<div class="exec-clean-note">All ' + fmtInt(flows.length) + " transaction(s) completed with 0 errors.</div>";
      }
      var rowsHtml = list.map(function (f) {
        var errs = Number(f.errors) || 0;
        var errPct = Number(f.errPct) || 0;
        var okPct = Math.max(0, 100 - errPct);
        var rateStr = fmtInt(f.cph || 0) + " /h";
        var targetSub = hasTargets && f.targetArrivalRate && String(f.targetArrivalRate) !== "0"
          ? '<div class="exec-sub-meta">Target: ' + esc(f.targetArrivalRate) + " /h</div>"
          : "";
        return (
          "<tr>" +
            '<td class="hotspot-item-name" title="' + esc(f.name) + '"><a href="transactions.html?item=' + encodeURIComponent(f.name) + '" class="exec-trunc">' + esc(f.name) + "</a>" + targetSub + "</td>" +
            '<td class="cell-num text-muted">' + fmtInt(f.count || 0) + "</td>" +
            '<td class="cell-num text-muted">' + rateStr + "</td>" +
            '<td class="cell-num">' +
              (errs > 0
                ? '<a href="errors.html#sec-incidents" class="matrix-alert-pill pill-err">' + fmtInt(errs) + "</a>"
                : '<span class="cell-zero">0</span>') +
            "</td>" +
            '<td class="cell-num">' +
              (errs > 0
                ? '<span class="status-pill ' + (okPct < 99 ? "fail" : "warn") + '">' + fmtErrorPct(errPct, errs) + "</span>"
                : '<span class="exec-ok-num">0.00%</span>') +
            "</td>" +
          "</tr>"
        );
      }).join("");

      var noteHtml = showCleanNote && cleanCount > 0
        ? '<div class="exec-inline-note">' + fmtInt(cleanCount) + " other transaction(s) completed with 0 errors.</div>"
        : "";

      return (
        '<div class="exec-compact-scroll" tabindex="0">' +
          '<table class="hotspot-table exec-triage-table exec-flows-compact">' +
            '<thead><tr><th>Transaction</th><th class="cell-num">Completed</th><th class="cell-num">Rate</th><th class="cell-num">Errors</th><th class="cell-num">Error %</th></tr></thead>' +
            "<tbody>" + rowsHtml + "</tbody>" +
          "</table>" +
        "</div>" +
        noteHtml
      );
    }

    errBody.innerHTML = buildFlowsList(failingFlows, true);
    allBody.innerHTML = buildFlowsList(flows, false);
  }

  // =========================================================================
  // 7. Error Causes & Events + Simplified Slowest Server Requests / Actions
  // =========================================================================

  function renderErrorsAndEvents(ov, ex, meta) {
    var card = document.getElementById("exec-errors-card");
    if (!card) return;

    var topErrors = (ex && Array.isArray(ex.topErrors)) ? ex.topErrors : [];
    var topEvents = (ex && Array.isArray(ex.topEvents)) ? ex.topEvents : [];
    var counts = (meta && meta.counts) || {};
    var totalTxErr = Number(counts.transactionErrors !== undefined ? counts.transactionErrors : counts.errors) || 0;
    var totalEvents = Number(counts.events) || 0;

    var errBody;
    if (topErrors.length) {
      errBody =
        '<table class="hotspot-table exec-triage-table">' +
          '<thead><tr><th>Error Message</th><th class="cell-num">Count</th><th class="cell-num">Share</th></tr></thead>' +
          "<tbody>" +
            topErrors.slice(0, 6).map(function (e) {
              var flows = Array.isArray(e.affectedFlows) && e.affectedFlows.length
                ? '<div class="exec-sub-meta">Transactions: ' + esc(e.affectedFlows.join(", ")) + "</div>"
                : "";
              return (
                "<tr>" +
                  '<td class="hotspot-item-name" title="' + esc(e.message) + '"><div class="exec-trunc">' + esc(e.message) + "</div>" + flows + "</td>" +
                  '<td class="cell-num cell-err">' + fmtInt(e.count || 0) + "</td>" +
                  '<td class="cell-num text-muted">' + fmtDec(e.sharePct || 0, 1) + "%</td>" +
                "</tr>"
              );
            }).join("") +
          "</tbody>" +
        "</table>";
    } else {
      errBody = '<div class="exec-clean-note">0 transaction errors recorded.</div>';
    }

    var evSection = "";
    if (topEvents.length) {
      evSection =
        '<div class="exec-sub-block">' +
          '<div class="exec-sub-header">' +
            '<span class="exec-sub-title">Top Logged Events (' + fmtInt(totalEvents) + " total)</span>" +
            '<a href="events.html" class="exec-mini-link">All Events →</a>' +
          "</div>" +
          '<table class="hotspot-table exec-triage-table">' +
            '<thead><tr><th>Event</th><th>Transaction</th><th class="cell-num">Count</th></tr></thead>' +
            "<tbody>" +
              topEvents.slice(0, 5).map(function (ev) {
                return (
                  "<tr>" +
                    '<td class="hotspot-item-name" title="' + esc(ev.name) + '"><div class="exec-trunc">' + esc(ev.name) + "</div></td>" +
                    '<td class="text-muted">' + esc(ev.testCase || "—") + "</td>" +
                    '<td class="cell-num" style="color:var(--status-warn-text);">' + fmtInt(ev.totalCount || 0) + "</td>" +
                  "</tr>"
                );
              }).join("") +
            "</tbody>" +
          "</table>" +
        "</div>";
    }

    card.innerHTML =
      '<div class="exec-card-header">' +
        '<div class="profile-title-block">' +
          '<div class="profile-title">Error Causes &amp; Events</div>' +
          '<div class="profile-subtitle">' + fmtInt(totalTxErr) + " transaction error(s) · " + fmtInt(totalEvents) + " event(s)</div>" +
        "</div>" +
        '<a href="errors.html" class="exec-mini-link">Error Details →</a>' +
      "</div>" +
      errBody +
      evSection;
  }

  function renderSlowestCards(ex) {
    var reqCard = document.getElementById("exec-slow-requests-card");
    var actCard = document.getElementById("exec-slow-actions-card");
    var repPctKey = (ex && ex.repPercentile) || "95";

    function buildCardHtml(title, subtitle, items, hrefBase, linkLabel, showApdex) {
      var bodyHtml;
      if (!items || !items.length) {
        bodyHtml = '<div class="exec-empty-box">No items recorded in this tier.</div>';
      } else {
        bodyHtml =
          '<table class="hotspot-table exec-triage-table">' +
            "<thead><tr>" +
              "<th>Name</th>" +
              '<th class="cell-num">Count</th>' +
              '<th class="cell-num">Mean</th>' +
              '<th class="cell-num">P' + esc(repPctKey) + "</th>" +
              (showApdex ? '<th class="cell-num">Apdex</th>' : '<th class="cell-num">Errors</th>') +
            "</tr></thead>" +
            "<tbody>" +
              items.map(function (it) {
                var pVal = getPct(it, repPctKey);
                var lastCell;
                if (showApdex) {
                  var ap = it.apdex;
                  if (ap && ap.value !== undefined) {
                    var b = apdexBand(ap.value);
                    lastCell = '<td class="cell-num"><span class="apdex-pill ' + b.cls + '">' + fmtDec(ap.value, 2) + "</span></td>";
                  } else {
                    lastCell = '<td class="cell-num text-muted">—</td>';
                  }
                } else {
                  var err = Number(it.errors) || 0;
                  lastCell = '<td class="cell-num">' + (err > 0 ? '<span class="cell-err">' + fmtInt(err) + "</span>" : '<span class="cell-zero">0</span>') + "</td>";
                }
                return (
                  "<tr>" +
                    '<td class="hotspot-item-name" title="' + esc(it.name) + '"><a href="' + hrefBase + "?item=" + encodeURIComponent(it.name) + '" class="exec-trunc">' + esc(it.name) + "</a></td>" +
                    '<td class="cell-num text-muted">' + fmtInt(it.count || 0) + "</td>" +
                    '<td class="cell-num">' + fmtMsReadable(it.mean) + "</td>" +
                    '<td class="cell-num cell-primary-num">' + fmtMsReadable(pVal) + "</td>" +
                    lastCell +
                  "</tr>"
                );
              }).join("") +
            "</tbody>" +
          "</table>";
      }

      return (
        '<div class="exec-card-header">' +
          '<div class="profile-title-block">' +
            '<div class="profile-title">' + esc(title) + "</div>" +
            '<div class="profile-subtitle">' + esc(subtitle) + "</div>" +
          "</div>" +
          '<a href="' + hrefBase + '" class="exec-mini-link">' + esc(linkLabel) + "</a>" +
        "</div>" +
        bodyHtml
      );
    }

    if (reqCard) {
      reqCard.innerHTML = buildCardHtml(
        "Slowest Server Requests",
        "Top 8 HTTP endpoints ranked by P" + repPctKey + " response time",
        (ex && ex.topSlowRequests) || [],
        "requests.html",
        "All Requests →",
        false
      );
    }

    if (actCard) {
      var actItems = (ex && ex.topSlowActions) || [];
      var hasApdex = actItems.some(function (a) { return a.apdex && a.apdex.value !== undefined; });
      actCard.innerHTML = buildCardHtml(
        "Slowest Actions",
        "Top 8 actions ranked by P" + repPctKey + " duration",
        actItems,
        "actions.html",
        "All Actions →",
        hasApdex
      );
    }
  }

  // =========================================================================
  // 8. Copy Executive Summary (Markdown) & Print
  // =========================================================================

  function buildMarkdownSummary(ov, ex, meta) {
    var tx = (ov && ov.summary && ov.summary.transactions) || {};
    var req = (ov && ov.summary && ov.summary.requests) || {};
    var act = (ov && ov.summary && ov.summary.actions) || {};
    var lpTotals = (ov && ov.loadProfileTotals) || {};
    var http = (ex && ex.httpStatus) || {};
    var ag = (ex && ex.agentHealth) || {};
    var sc = (ov && ov.scorecard) || (meta && meta.scorecard);
    var repPctKey = (ex && ex.repPercentile) || "95";

    var lines = [];
    var title = (meta && meta.projectName ? meta.projectName + " — " : "") +
      "Load Test Executive Summary" +
      (meta && meta.loadTestName ? " (" + meta.loadTestName + ")" : "");
    lines.push("## " + title);
    lines.push("");
    lines.push("- **Window:** " + ((meta && meta.startTime) || "—") + " – " + ((meta && meta.endTime) || "—") +
      " (" + ((meta && meta.durationDisplay) || "—") + ")");
    if (lpTotals.usersDisplay) {
      lines.push("- **Load Profile:** " + lpTotals.usersDisplay + " users" +
        (lpTotals.arrivalDisplay && lpTotals.arrivalDisplay !== "0" ? " · Target: " + lpTotals.arrivalDisplay + " /h" : "") +
        (lpTotals.rampUpDisplay && lpTotals.rampUpDisplay !== "00:00:00" ? " · Ramp-up: " + lpTotals.rampUpDisplay : ""));
    }
    if (sc && sc.present) {
      lines.push("- **Scorecard:** " + String(sc.result || "").toUpperCase() + " — " +
        fmtInt(sc.points || 0) + "/" + fmtInt(sc.maxPoints || 0) + " pts (" + fmtDec(sc.pointsPercentage || 0, 1) + "%)" +
        (sc.ratingName ? " · " + sc.ratingName : ""));
    }

    var txCount = Number(tx.count) || 0;
    var txOk = txCount > 0 ? Math.max(0, 100 - (Number(tx.errPct) || 0)) : 0;
    lines.push("- **Transactions:** " + fmtInt(txCount) + " (" + fmtInt(tx.cph || 0) + " /h) · **Success Rate:** " +
      fmtSuccessPct(txOk, tx.errors) + " (" + fmtInt(tx.errors || 0) + " errors)");
    lines.push("- **Server Requests:** " + fmtInt(req.count || 0) + " (" + fmtDec(req.rate || 0, 1) + " req/s) · Mean: " +
      fmtMsReadable(req.mean) + " · P" + repPctKey + ": " + fmtMsReadable(getPct(req, repPctKey)));
    lines.push("- **HTTP Responses:** 2xx=" + fmtInt(http.c2xx || 0) + ", 3xx=" + fmtInt(http.c3xx || 0) +
      ", 4xx=" + fmtInt(http.c4xx || 0) + ", 5xx=" + fmtInt(http.c5xx || 0) +
      ((Number(http.c0) || 0) > 0 ? ", Timeout/0=" + fmtInt(http.c0) : ""));
    if (act.apdex && act.apdex.longValue) {
      lines.push("- **Action Apdex:** " + act.apdex.longValue);
    }
    lines.push("- **Load Generators:** " + fmtInt(ag.agentCount || 0) + " agents · Max agent CPU: " +
      fmtDec(ag.peakSingleAgentCpu || 0, 1) + "% · Full GCs: " + fmtInt(ag.fullGcCount || 0));
    lines.push("- **Events:** " + fmtInt((meta && meta.counts && meta.counts.events) || 0));

    var flows = (ex && Array.isArray(ex.flows)) ? ex.flows : [];
    var failingFlows = flows.filter(function (f) { return (Number(f.errors) || 0) > 0; });
    if (failingFlows.length) {
      failingFlows.sort(function (a, b) { return (Number(b.errors) || 0) - (Number(a.errors) || 0); });
      lines.push("");
      lines.push("### Transactions with Errors (" + fmtInt(failingFlows.length) + " of " + fmtInt(flows.length) + ")");
      lines.push("| Transaction | Completed | Rate (/h) | Errors | Error % |");
      lines.push("| --- | ---: | ---: | ---: | ---: |");
      failingFlows.forEach(function (f) {
        lines.push("| " + f.name + " | " + fmtInt(f.count || 0) + " | " + fmtInt(f.cph || 0) + " | " +
          fmtInt(f.errors || 0) + " | " + fmtErrorPct(f.errPct || 0, f.errors) + " |");
      });
    }

    var errs = (ex && Array.isArray(ex.topErrors)) ? ex.topErrors : [];
    if (errs.length) {
      lines.push("");
      lines.push("### Top Errors");
      errs.slice(0, 5).forEach(function (e) {
        lines.push("- **" + fmtInt(e.count || 0) + "× (" + fmtDec(e.sharePct || 0, 1) + "%):** " + e.message);
      });
    }

    return lines.join("\n") + "\n";
  }

  function wireActions(ov, ex, meta) {
    var copyBtn = document.getElementById("btn-copy-summary");
    if (copyBtn) {
      copyBtn.addEventListener("click", function () {
        var md = buildMarkdownSummary(ov, ex, meta);
        var done = function () {
          var prev = copyBtn.textContent;
          copyBtn.textContent = "Copied ✓";
          copyBtn.classList.add("is-copied");
          setTimeout(function () {
            copyBtn.textContent = prev;
            copyBtn.classList.remove("is-copied");
          }, 1800);
        };
        if (navigator.clipboard && navigator.clipboard.writeText) {
          navigator.clipboard.writeText(md).then(done, function () { fallbackCopy(md, done); });
        } else {
          fallbackCopy(md, done);
        }
      });
    }

    var printBtn = document.getElementById("btn-print-summary");
    if (printBtn) {
      printBtn.addEventListener("click", function () { window.print(); });
    }
  }

  function fallbackCopy(text, cb) {
    var ta = document.createElement("textarea");
    ta.value = text;
    ta.style.position = "fixed";
    ta.style.opacity = "0";
    document.body.appendChild(ta);
    ta.select();
    try { document.execCommand("copy"); } catch (_) {}
    document.body.removeChild(ta);
    if (typeof cb === "function") cb();
  }

  // =========================================================================
  // Bootstrap
  // =========================================================================

  document.addEventListener("DOMContentLoaded", function () {
    var ov = window.XLT_OVERVIEW || {};
    var ex = window.XLT_EXECUTIVE || {};
    var meta = window.XLT_META || null;

    renderScopeLine(ov, ex, meta);
    renderScorecard(ov, ex, meta);
    renderComment(ov, meta);
    renderFactsStrip(ov, ex, meta);
    renderKpis(ov, ex, meta);
    renderDistributions(ov, ex, meta);
    renderStaticCharts(ov, ex, meta);
    renderFlowsReliability(ov, ex);
    renderErrorsAndEvents(ov, ex, meta);
    renderSlowestCards(ex);
    wireActions(ov, ex, meta);
    initToggletips();

    // Wire Chart Switcher tabs
    var chartTablist = document.querySelector("#exec-timeline-section [role='tablist']");
    var chartTabs = initTabGroup(chartTablist, "data-chart-tab", "data-chart-panel", function (key) {
      if (key === "echarts") {
        renderEchartTimeline(ex);
        if (chartInstance && !chartInstance.isDisposed()) {
          setTimeout(function () { chartInstance.resize(); }, 20);
        }
      }
    });

    // Wire ECharts tier switcher pills
    var tierBtns = document.querySelectorAll("#exec-echart-controls [data-ts-tier]");
    Array.prototype.forEach.call(tierBtns, function (btn) {
      btn.addEventListener("click", function () {
        activeTsTier = btn.getAttribute("data-ts-tier") || "tx";
        Array.prototype.forEach.call(tierBtns, function (b) {
          b.classList.toggle("is-active", b === btn);
        });
        renderEchartTimeline(ex);
      });
    });

    // Auto-select Static Key Charts if no XML timeSeries with errors is available (and static charts exist)
    var hasTs = Boolean(ex && ex.timeSeries && Array.isArray(ex.timeSeries.rows) && ex.timeSeries.rows.length);
    var hasFullTs = hasTs && Boolean(ex.timeSeries.hasErrorSeries);
    if (hasFullTs || (hasTs && meta && meta.chartsGenerated === false)) {
      renderEchartTimeline(ex);
    } else if (chartTabs) {
      chartTabs.select("static2", false);
    }

    // Wire Flows filter tabs (With Errors / All Flows)
    var flowsTablist = document.querySelector("#exec-flows-card [role='tablist']");
    initTabGroup(flowsTablist, "data-flows-tab", "data-flows-panel");

    // Re-theme ECharts live when dark/light mode changes
    if (typeof MutationObserver !== "undefined") {
      var themeObs = new MutationObserver(function () {
        if (chartInstance && !chartInstance.isDisposed()) {
          renderEchartTimeline(ex);
        }
      });
      themeObs.observe(document.documentElement, { attributes: true, attributeFilter: ["class"] });
    }
  });
})();
