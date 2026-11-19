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
 * XLT Modern Load Report — Quality Scorecard Renderer & Interactive Controls (`scorecard.html`)
 *
 * @fileoverview
 * Renders the automated quality gate scorecard evaluation derived from `scorecard.xml`:
 *   1. Top verdict, points attained, rules passed/failed, and test window KPI grid (`#sc-kpi-grid`).
 *   2. Optional evaluation error banner (`#sc-error-banner`).
 *   3. Failed/breached rules callout table (`#sc-breached-section` / `#sc-breached-table`).
 *   4. Rule groups matrix (`#groups-table`) with totals footer.
 *   5. Searchable, status-filterable full rules directory (`#scorecard-rules-table`) with
 *      toggleable XPath selector lines (`#sc-toggle-xpath`).
 *   6. Configured rating thresholds scale (`#sc-ratings-table`) highlighting the achieved rating.
 *
 * Consumes:
 * - `window.XLT_SCORECARD`: `{ present, result, testFailed, points, maxPoints, pointsPercentage,
 *   ratingId, ratingName, errorMessage, groups, rules, ratings }`
 * - `window.XLT_META` (`durationDisplay`, `startTime`, `counts`)
 * - `window.XLT_UI` (`esc`, `fmtInt`, `fmtDec` defaulting to 1 decimal place)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;

  /** Formats scorecard percentages/scores with 1 fractional digit by default. */
  function fmtDec(n, d) {
    return UI.fmtDec(n, d === undefined ? 1 : d);
  }

  /**
   * Maps scorecard evaluation statuses (`PASSED`, `SKIPPED`, `DISABLED`, `FAILED`, `ERROR`)
   * to semantic `.status-pill` classes.
   *
   * @param {string} res Uppercase or raw status string.
   * @returns {"pass"|"warn"|"fail"} Status pill class suffix.
   */
  function statusPillClass(res) {
    var r = String(res || "").toUpperCase();
    if (r === "PASSED") return "pass";
    if (r === "SKIPPED" || r === "DISABLED") return "warn";
    return "fail";
  }

  document.addEventListener("DOMContentLoaded", function () {
    var sc = window.XLT_SCORECARD || null;
    var meta = window.XLT_META || {};
    var kpiGrid = document.getElementById("sc-kpi-grid");
    var errorBanner = document.getElementById("sc-error-banner");
    var breachedSec = document.getElementById("sc-breached-section");
    var breachedTbody = document.querySelector("#sc-breached-table tbody");
    var breachedTitle = document.getElementById("sc-breached-title");
    var groupsTitle = document.getElementById("sc-groups-title");
    var groupsTbody = document.querySelector("#groups-table tbody");
    var groupsTfoot = document.querySelector("#groups-table tfoot");
    var rulesTable = document.getElementById("scorecard-rules-table");
    var filterInput = document.getElementById("sc-filter");
    var chipWrap = document.getElementById("sc-chips");
    var toggleXpathBtn = document.getElementById("sc-toggle-xpath");
    var countBadge = document.getElementById("sc-visible-count");
    var ratingsSec = document.getElementById("sc-ratings-section");
    var ratingsTbody = document.querySelector("#sc-ratings-table tbody");

    if (!sc || sc.present === false) {
      if (kpiGrid) kpiGrid.hidden = true;
      if (breachedSec) breachedSec.hidden = true;
      if (ratingsSec) ratingsSec.hidden = true;
      if (errorBanner) {
        errorBanner.hidden = false;
        errorBanner.innerHTML =
          '<div class="card-body" style="text-align:center;padding:36px 16px;color:var(--text-muted);">' +
            "<strong>No scorecard evaluation data (<code>scorecard.xml</code>) was generated for this report.</strong>" +
          "</div>";
      }
      return;
    }

    var verdict = String(sc.result || (sc.testFailed ? "FAILED" : "PASSED")).toUpperCase();
    var isFail = verdict === "FAILED" || verdict === "ERROR";
    var groups = Array.isArray(sc.groups) ? sc.groups : [];
    var rules = Array.isArray(sc.rules) ? sc.rules : [];
    var ratings = Array.isArray(sc.ratings) ? sc.ratings : [];

    var passedCount = 0;
    var failedCount = 0;
    var skippedCount = 0;
    rules.forEach(function (r) {
      var st = String(r.result || "").toUpperCase();
      if (st === "PASSED") passedCount++;
      else if (st === "SKIPPED" || st === "DISABLED") skippedCount++;
      else failedCount++;
    });

    // 1. KPI Summary Grid
    if (kpiGrid) {
      var durDisp = meta.durationDisplay || "—";
      var startDisp = meta.startTime || "—";
      var agentsCnt = (meta.counts && meta.counts.agents) || 0;
      kpiGrid.innerHTML =
        '<article class="kpi-card">' +
          '<span class="kpi-label">Overall Gate Verdict</span>' +
          '<div class="kpi-val" style="color:' + (isFail ? "var(--status-fail-text)" : "var(--status-pass-text)") + ';">' + esc(verdict) + "</div>" +
          '<div class="kpi-sub">' +
            (sc.errorMessage
              ? esc(sc.errorMessage)
              : (failedCount > 0
                  ? "<strong>" + fmtInt(failedCount) + " rule(s)</strong> did not meet target criteria"
                  : "All evaluated quality gate rules satisfied")) +
          "</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Attained Points</span>' +
          '<div class="kpi-val">' + fmtInt(sc.points || 0) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">/ ' + fmtInt(sc.maxPoints || 0) + " pts</span></div>" +
          '<div class="kpi-sub">Scoring ratio: <strong>' + fmtDec(sc.pointsPercentage || 0, 1) + "%</strong>" +
            (sc.ratingName ? " • Rating: <strong>" + esc(sc.ratingName) + "</strong>" : "") +
          "</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Rules Evaluation</span>' +
          '<div class="kpi-val">' +
            fmtInt(passedCount) + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">passed</span> • ' +
            fmtInt(failedCount) + ' <span style="font-size:14px;font-weight:600;color:' + (failedCount > 0 ? "var(--status-fail-text)" : "var(--text-muted)") + ';">failed</span>' +
          "</div>" +
          '<div class="kpi-sub">' + fmtInt(rules.length) + " total rule(s) across " + fmtInt(groups.length) + " group(s)" +
            (skippedCount > 0 ? " (" + fmtInt(skippedCount) + " skipped)" : "") +
          "</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Execution Window</span>' +
          '<div class="kpi-val">' + esc(durDisp) + "</div>" +
          '<div class="kpi-sub">' + esc(startDisp) + (agentsCnt ? " • " + fmtInt(agentsCnt) + " Load Agent(s)" : "") + "</div>" +
        "</article>";
    }

    // Optional Outcome Error Banner
    if (errorBanner) {
      if (sc.errorMessage) {
        errorBanner.hidden = false;
        errorBanner.innerHTML =
          '<div class="card-header"><div class="card-title" style="color:var(--status-fail-text);">✕ Scorecard Evaluation Error</div></div>' +
          '<div class="card-body"><pre style="font-family:var(--font-mono);font-size:12px;white-space:pre-wrap;">' + esc(sc.errorMessage) + "</pre></div>";
      } else {
        errorBanner.hidden = true;
      }
    }

    // 2. Breached Rules Warning Banner
    var failedRules = rules.filter(function (r) {
      var st = String(r.result || "").toUpperCase();
      return st !== "PASSED" && st !== "SKIPPED" && st !== "DISABLED";
    });
    if (breachedSec && breachedTbody) {
      if (!failedRules.length) {
        breachedSec.hidden = true;
      } else {
        breachedSec.hidden = false;
        if (breachedTitle) {
          breachedTitle.textContent = "Rule Checks Requiring Attention (" + fmtInt(failedRules.length) + (failedRules.length === 1 ? " Rule)" : " Rules)");
        }
        breachedTbody.innerHTML = failedRules.map(function (r, idx) {
          var pillCls = r.testFailed || r.failsTest ? "fail" : "warn";
          return (
            "<tr>" +
              '<td class="col-idx">' + (idx + 1) + "</td>" +
              '<td class="col-text">' + esc(r.groupName || "Ungrouped") + "</td>" +
              '<td class="col-text"><span class="status-pill ' + pillCls + '">' + esc(r.result || "FAILED") + "</span> " + esc(r.name || r.id) + "</td>" +
              '<td class="col-text"><code>' + esc(r.condition || "—") + "</code></td>" +
              '<td class="col-num cell-err">' + esc(r.actualValue != null ? r.actualValue : "—") + "</td>" +
              '<td class="col-num cell-err">' + fmtInt(r.points || 0) + " / " + fmtInt(r.maxPoints || 0) + " pts</td>" +
              '<td class="col-text">' + esc(r.message || r.description || "") + "</td>" +
            "</tr>"
          );
        }).join("");
      }
    }

    // 3. Groups Matrix
    if (groupsTitle) {
      groupsTitle.textContent = "Scorecard Evaluation Groups (" + fmtInt(groups.length) + (groups.length === 1 ? " Group)" : " Groups)");
    }
    if (groupsTbody) {
      if (!groups.length) {
        groupsTbody.innerHTML = '<tr><td colspan="9" class="col-text" style="text-align:center;padding:20px;color:var(--text-muted);">No rule groups configured in scorecard.</td></tr>';
        if (groupsTfoot) groupsTfoot.innerHTML = "";
      } else {
        groupsTbody.innerHTML = groups.map(function (g, idx) {
          var st = String(g.result || "PASSED").toUpperCase();
          var pCls = statusPillClass(st);
          var maxP = Number(g.maxPoints) || 0;
          var pts = Number(g.points) || 0;
          var ratio = maxP > 0 ? (pts * 100) / maxP : (st === "PASSED" ? 100 : 0);
          var errCls = st === "FAILED" || st === "ERROR" ? " cell-err" : "";
          return (
            "<tr>" +
              '<td class="col-idx">' + (idx + 1) + "</td>" +
              '<td class="col-text">' + esc(g.name || g.id) + "</td>" +
              '<td class="col-text">' + esc(g.description || "—") + "</td>" +
              '<td class="col-text"><span class="status-pill ' + pCls + '">' + esc(st) + "</span></td>" +
              '<td class="col-num" data-val="' + (Number(g.ruleCount) || 0) + '">' + fmtInt(g.ruleCount || 0) + "</td>" +
              '<td class="col-num" data-val="' + maxP + '">' + fmtInt(maxP) + "</td>" +
              '<td class="col-num' + (pts < maxP ? " cell-err" : "") + '" data-val="' + pts + '">' + fmtInt(pts) + "</td>" +
              '<td class="col-num' + (pts < maxP ? " cell-err" : "") + '" data-val="' + ratio.toFixed(2) + '">' + fmtDec(ratio, 1) + "%</td>" +
              '<td class="col-text' + errCls + '">' + esc(g.message || "") + "</td>" +
            "</tr>"
          );
        }).join("");

        if (groupsTfoot) {
          groupsTfoot.innerHTML =
            '<tr class="table-totals">' +
              '<td class="col-idx">Σ</td>' +
              '<td class="col-text">Totals</td>' +
              '<td class="col-text">All Groups Aggregated</td>' +
              '<td class="col-text"><span class="status-pill ' + statusPillClass(verdict) + '">' + esc(verdict) + "</span></td>" +
              '<td class="col-num">' + fmtInt(rules.length) + "</td>" +
              '<td class="col-num">' + fmtInt(sc.maxPoints || 0) + "</td>" +
              '<td class="col-num">' + fmtInt(sc.points || 0) + "</td>" +
              '<td class="col-num">' + fmtDec(sc.pointsPercentage || 0, 1) + "%</td>" +
              '<td class="col-text">' + (failedCount > 0 ? fmtInt(failedCount) + " rule(s) failed target thresholds" : "All rules passed") + "</td>" +
            "</tr>";
        }
      }
    }

    // 4. Rules Directory & Interactive Controls
    if (chipWrap) {
      chipWrap.innerHTML =
        '<button type="button" class="btn-chip active" data-sc-chip="all">All Rules (' + fmtInt(rules.length) + ")</button>" +
        '<button type="button" class="btn-chip" data-sc-chip="failed" style="color:var(--status-fail-text);">Failed (' + fmtInt(failedCount) + ")</button>" +
        '<button type="button" class="btn-chip" data-sc-chip="passed">✓ Passed (' + fmtInt(passedCount) + ")</button>";
    }

    var rowItems = [];
    if (rulesTable && rulesTable.tBodies[0]) {
      var rTbody = rulesTable.tBodies[0];
      if (!rules.length) {
        rTbody.innerHTML = '<tr><td colspan="8" class="col-text" style="text-align:center;padding:24px;color:var(--text-muted);">No scorecard rules configured.</td></tr>';
      } else {
        var frag = document.createDocumentFragment();
        rules.forEach(function (r, idx) {
          var st = String(r.result || "PASSED").toUpperCase();
          var normStatus = st === "PASSED" ? "passed" : "failed";
          var pCls = statusPillClass(st);
          var isRuleFail = normStatus === "failed";
          var tr = document.createElement("tr");
          tr.id = "rule-" + (r.id || "");
          tr.setAttribute("data-result", normStatus);
          if (isRuleFail) tr.className = "row-highlight-error";

          var selectors = Array.isArray(r.selectors) && r.selectors.length
            ? r.selectors
            : (r.selector ? [r.selector] : []);
          // XPath / JSONPath expressions stay hidden until the inspector toggle is enabled
          var xpathHtml = selectors.length
            ? '<div class="sc-xpath-line" hidden style="margin-top:4px;font-size:11px;color:var(--text-muted);word-break:break-all;">' +
                selectors.map(function (sel) { return "<div><code>" + esc(sel) + "</code></div>"; }).join("") +
              "</div>"
            : "";

          tr.innerHTML =
            '<td class="col-idx">' + (idx + 1) + "</td>" +
            '<td class="col-text"><span class="status-pill ' + pCls + '">' + esc(st) + "</span></td>" +
            '<td class="col-text">' + esc(r.groupName || "Ungrouped") + "</td>" +
            '<td class="col-text">' +
              "<span" + (isRuleFail ? ' style="color:var(--status-fail-text);"' : "") + ">" + esc(r.name || r.id) + "</span>" +
              (r.failsTest ? ' <span class="badge-tag cell-err" title="Fails entire test when breached">Fails Test</span>' : "") +
              (r.description ? '<br><span class="card-subtitle">' + esc(r.description) + "</span>" : "") +
              xpathHtml +
            "</td>" +
            '<td class="col-text"><code>' + esc(r.condition || "—") + "</code></td>" +
            '<td class="col-num' + (isRuleFail ? " cell-err" : "") + '">' + esc(r.actualValue != null ? r.actualValue : "—") + "</td>" +
            '<td class="col-num' + (isRuleFail ? " cell-err" : "") + '">' + fmtInt(r.points || 0) + " / " + fmtInt(r.maxPoints || 0) + "</td>" +
            '<td class="col-text' + (isRuleFail ? " cell-err" : "") + '">' + esc(r.message || "") + "</td>";

          frag.appendChild(tr);
          rowItems.push({ tr: tr, status: normStatus, blob: tr.textContent.toLowerCase() });
        });
        rTbody.innerHTML = "";
        rTbody.appendChild(frag);
      }
    }

    // 5. Ratings Scale Table
    if (ratingsSec && ratingsTbody) {
      if (!ratings.length) {
        ratingsSec.hidden = true;
      } else {
        ratingsSec.hidden = false;
        ratingsTbody.innerHTML = ratings.map(function (rt, idx) {
          var matched = sc.ratingId && rt.id === sc.ratingId;
          var valNum = Number(rt.value) || 0;
          var pill = valNum <= 50 ? "fail" : (valNum < 90 ? "warn" : "pass");
          return (
            "<tr" + (matched ? ' style="background:var(--bg-hover);"' : "") + ">" +
              '<td class="col-idx">' + (idx + 1) + "</td>" +
              '<td class="col-text"><span class="status-pill ' + pill + '">' + esc(rt.name || rt.id) + "</span></td>" +
              '<td class="col-text">' + esc(rt.description || "") + "</td>" +
              '<td class="col-num">' + fmtDec(valNum, 1) + "%</td>" +
              '<td class="col-text">' + esc(String(Boolean(rt.failsTest))) + "</td>" +
              '<td class="col-text">' +
                (matched
                  ? '<span class="badge-tag">Current Score: ' + fmtDec(sc.pointsPercentage || 0, 1) + "% (" + fmtInt(sc.points || 0) + " pts)</span>"
                  : '<span style="color:var(--text-muted);">&mdash;</span>') +
              "</td>" +
            "</tr>"
          );
        }).join("");
      }
    }

    // 6. Interactive Rule Search, Status Chips & XPath Selector Toggle
    var activeStatus = "all";
    var query = "";
    var showXpath = false;

    function applyFilter() {
      var q = query.toLowerCase().trim();
      var vis = 0;
      rowItems.forEach(function (item) {
        var statusOk = activeStatus === "all" || item.status === activeStatus;
        var textOk = !q || item.blob.indexOf(q) !== -1;
        var show = statusOk && textOk;
        item.tr.hidden = !show;
        if (show) {
          vis++;
          item.tr.cells[0].textContent = String(vis);
        }
      });
      if (countBadge) {
        countBadge.textContent = vis + " / " + rowItems.length + " rules";
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
        var btn = e.target.closest("[data-sc-chip]");
        if (!btn) return;
        activeStatus = btn.getAttribute("data-sc-chip");
        Array.prototype.forEach.call(chipWrap.querySelectorAll("[data-sc-chip]"), function (b) {
          b.classList.toggle("active", b === btn);
        });
        applyFilter();
      });
    }

    if (toggleXpathBtn) {
      toggleXpathBtn.addEventListener("click", function () {
        showXpath = !showXpath;
        toggleXpathBtn.classList.toggle("active", showXpath);
        toggleXpathBtn.textContent = showXpath ? "Hide XPath Selectors" : "Show XPath Selectors";
        Array.prototype.forEach.call(document.querySelectorAll(".sc-xpath-line"), function (el) {
          el.hidden = !showXpath;
        });
      });
    }

    applyFilter();
  });
})();
