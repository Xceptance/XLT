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
 * XLT Modern Comparison & Trend Report — Overview Dashboard Controller (`index.html`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI || {};
  var CMP = window.XLT_CMP || {};
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var fmtDec = UI.fmtDec;
  var fmtCompact = UI.fmtCompact;
  var fmtBytes = UI.fmtBytes;

  var activeTrendTier = "transactions";
  var activeProfileTier = "transactions";
  var activeMoverScope = "all";

  var trendChart = null;
  var profileChart = null;
  var scenarioChart = null;
  var resizeObservers = [];

  function tierVal(run, tier, field) {
    if (!run || !run.summary || !run.summary[tier]) return null;
    var v = run.summary[tier][field];
    return v === undefined ? null : v;
  }

  function tierPct(run, tier, pctKey) {
    if (!run || !run.summary || !run.summary[tier] || !pctKey) return null;
    return UI.getPercentile(run.summary[tier], pctKey);
  }

  function renderRunPanel(run, idx, roleClass, roleLabel) {
    if (!run) return "";
    var meta = window.XLT_META || {};
    var caps = meta.capabilities || {};
    var txCount = tierVal(run, "transactions", "count");
    var txRate = tierVal(run, "transactions", "rate");
    var txErr = tierVal(run, "transactions", "errors");

    var metrics = [];
    if (run.startTime) {
      metrics.push("<span>Start: <strong>" + esc(run.startTime) + "</strong></span>");
    }
    if (run.durationDisplay) {
      metrics.push("<span>Duration: <strong>" + esc(run.durationDisplay) + "</strong></span>");
    }
    if (run.configuredUsers != null && run.configuredUsers > 0) {
      metrics.push("<span>Users: <strong>" + fmtInt(run.configuredUsers) + "</strong></span>");
    }
    if (caps.hasCounts !== false && txCount != null) {
      metrics.push("<span>Transactions: <strong>" + fmtCompact(txCount) + "</strong></span>");
    } else if (txRate != null) {
      metrics.push("<span>Tx Rate: <strong>" + fmtDec(txRate, 1) + "/s</strong></span>");
    }
    if (run.hits != null) {
      metrics.push("<span>Hits: <strong>" + fmtCompact(run.hits) + "</strong></span>");
    }
    if (run.bytesReceived != null) {
      metrics.push("<span>Received: <strong>" + esc(fmtBytes(run.bytesReceived)) + "</strong></span>");
    }
    if (txErr != null) {
      var errCls = txErr > 0 ? ' class="cell-err"' : "";
      metrics.push("<span>Tx Errors: <strong" + errCls + ">" + fmtInt(txErr) + "</strong></span>");
    }

    var scHtml = "";
    if (run.scorecard && run.scorecard.present) {
      var res = String(run.scorecard.result || "EVALUATED").toUpperCase();
      var pillCls = res === "PASSED" ? "pass" : (res === "FAILED" || res === "ERROR" ? "fail" : "warn");
      scHtml = '<span class="status-pill ' + pillCls + '">Scorecard: ' + esc(res) + "</span>";
    }

    var headActionsHtml = scHtml
      ? '<div style="display:inline-flex;align-items:center;gap:6px">' + scHtml + "</div>"
      : "";

    var commentHtml = "";
    if (run.commentHeadline || run.commentPlain) {
      commentHtml =
        '<div class="cmp-run-comment">' +
          (run.commentHeadline ? '<div class="cmp-run-comment-head">' + esc(run.commentHeadline) + "</div>" : "") +
          (run.commentPlain ? "<div>" + esc(run.commentPlain) + "</div>" : "") +
        "</div>";
    }

    return (
      '<article class="cmp-run-panel ' + roleClass + '">' +
        '<div class="cmp-run-panel-head">' +
          '<div class="cmp-run-panel-title">' +
            '<span class="cmp-run-badge ' + roleClass + '">' + esc(roleLabel) + "</span>" +
            "<span>#" + (idx + 1) + " " + esc(run.name || ("Run " + (idx + 1))) + "</span>" +
          "</div>" +
          headActionsHtml +
        "</div>" +
        '<div class="cmp-run-panel-meta">' + metrics.join("") + "</div>" +
        commentHtml +
      "</article>"
    );
  }

  function renderSideBySideRuns(baseRun, targetRun, st) {
    var sec = document.getElementById("sec-runs-compare");
    if (!sec || !baseRun || !targetRun) return;
    sec.innerHTML =
      '<div class="cmp-runs-card">' +
        renderRunPanel(baseRun, st.baseIdx, "is-base", "Baseline (A)") +
        '<div class="cmp-runs-vs" aria-hidden="true">vs</div>' +
        renderRunPanel(targetRun, st.targetIdx, "is-target", "Target (B)") +
      "</div>";
  }

  function renderTimelineAndTrajectory(st) {
    var sec = document.getElementById("sec-trend");
    if (!sec) return;
    var runs = CMP.getRuns();
    if (runs.length < 3) {
      sec.hidden = true;
      disposeChart(trendChart);
      trendChart = null;
      return;
    }
    sec.hidden = false;

    var strip = document.getElementById("cmp-timeline-strip");
    if (strip) {
      strip.innerHTML = "";
      runs.forEach(function (run, idx) {
        var isBase = idx === st.baseIdx;
        var isTarget = idx === st.targetIdx;
        var btn = document.createElement("button");
        btn.type = "button";
        btn.className = "cmp-timeline-node" + (isBase ? " is-base" : "") + (isTarget ? " is-target" : "");
        btn.title = "Click to compare as Target (B) · Shift+Click to set Baseline (A)";

        var rolesHtml = "";
        if (isBase) rolesHtml += '<span class="cmp-node-role base">Base A</span>';
        if (isTarget) rolesHtml += '<span class="cmp-node-role target">Target B</span>';

        var txRate = tierVal(run, "transactions", "rate");
        var txMean = tierVal(run, "transactions", "mean");
        var txErr = tierVal(run, "transactions", "errors");
        var statParts = [];
        if (txRate != null) statParts.push(fmtDec(txRate, 1) + " tx/s");
        if (txMean != null) statParts.push(fmtInt(txMean) + " ms");
        if (txErr != null) statParts.push(fmtInt(txErr) + " err");

        btn.innerHTML =
          '<div class="cmp-node-top">' +
            '<span class="cmp-node-idx">Run #' + (idx + 1) + "</span>" +
            '<span style="display:inline-flex;gap:4px">' + rolesHtml + "</span>" +
          "</div>" +
          '<div class="cmp-node-name">' + esc(run.commentHeadline || run.name || ("Run " + (idx + 1))) + "</div>" +
          (run.commentHeadline && run.name ? '<div class="cmp-node-date">' + esc(run.name) + "</div>" : "") +
          (run.startTime ? '<div class="cmp-node-date">' + esc(run.startTime) + "</div>" : "") +
          '<div class="cmp-node-stats">' + esc(statParts.join(" · ")) + "</div>";

        btn.addEventListener("click", function (e) {
          if (e.shiftKey) {
            CMP.setState({ baseIdx: idx });
          } else {
            CMP.setState({ targetIdx: idx });
          }
        });
        strip.appendChild(btn);
      });
    }

    renderTrendTrajectoryChart();
  }

  function renderKpiCardHtml(label, valA, valB, fmtFn, unit, polarity, subHtml, opts) {
    opts = opts || {};
    var delta = CMP.calcDelta(valA, valB);
    var pClass = CMP.polarityClass(delta, polarity, opts.deadband !== undefined ? opts.deadband : 1.0);
    var pillText = opts.absPill ? CMP.formatAbsDelta(delta, fmtFn, unit) : CMP.formatPct(delta);
    var u = unit ? " " + unit : "";
    var currText = delta.valB !== null ? fmtFn(delta.valB) + u : "—";
    var prevText = delta.valA !== null ? "from " + fmtFn(delta.valA) + u : "not in baseline";
    var currClass = "cmp-kpi-curr" + (opts.warnAccent && delta.valB > 0 ? " cell-warn" : "");

    return (
      '<article class="kpi-card">' +
        '<div class="cmp-kpi-head">' +
          '<span class="kpi-label">' + esc(label) + "</span>" +
          '<span class="cmp-delta-pill ' + pClass + '">' + esc(pillText) + "</span>" +
        "</div>" +
        '<div class="cmp-kpi-pair">' +
          '<span class="' + currClass + '">' + esc(currText) + "</span>" +
          '<span class="cmp-kpi-prev">' + esc(prevText) + "</span>" +
        "</div>" +
        '<div class="kpi-sub">' + subHtml + "</div>" +
      "</article>"
    );
  }

  function renderKpiCards(baseRun, targetRun) {
    var grid = document.getElementById("sec-kpis");
    if (!grid || !baseRun || !targetRun) return;
    var meta = window.XLT_META || {};
    var caps = meta.capabilities || {};
    var pcts = UI.getConfiguredPercentiles();
    var part = UI.partitionPercentilesForKpis(pcts);

    // 1. Transactions Throughput
    var txRateA = tierVal(baseRun, "transactions", "rate");
    var txRateB = tierVal(targetRun, "transactions", "rate");
    var txCphA = tierVal(baseRun, "transactions", "cph");
    var txCphB = tierVal(targetRun, "transactions", "cph");
    var txSub = txCphB != null
      ? fmtInt(txCphA) + " → " + fmtInt(txCphB) + " /h"
      : "Completed scenarios/s";
    var card1 = renderKpiCardHtml("Transaction Rate", txRateA, txRateB, function (n) { return fmtDec(n, 1); }, "tx/s", "higher", txSub);

    // 2. Transaction Mean Runtime
    var txMeanA = tierVal(baseRun, "transactions", "mean");
    var txMeanB = tierVal(targetRun, "transactions", "mean");
    var meanDelta = CMP.calcDelta(txMeanA, txMeanB);
    var medA = tierVal(baseRun, "transactions", "median");
    var medB = tierVal(targetRun, "transactions", "median");
    var meanSub = "Δ " + CMP.formatAbsDelta(meanDelta, fmtInt, "ms") +
      (medA != null && medB != null ? " · Median " + fmtInt(medA) + " → " + fmtInt(medB) + " ms" : "");
    var card2 = renderKpiCardHtml("Transaction Mean", txMeanA, txMeanB, fmtInt, "ms", "lower", meanSub);

    // 3. Tail Percentile (dynamic across 0..N configured percentiles!)
    var tailKey = part.tailKeys.length ? part.tailKeys[0] : part.subKey;
    var card3;
    if (tailKey) {
      var pTailA = tierPct(baseRun, "transactions", tailKey);
      var pTailB = tierPct(targetRun, "transactions", tailKey);
      var tailExtras = [];
      if (part.headKey) {
        var phA = tierPct(baseRun, "transactions", part.headKey);
        var phB = tierPct(targetRun, "transactions", part.headKey);
        if (phA != null || phB != null) tailExtras.push("P" + part.headKey + ": " + fmtInt(phA, "—") + " → " + fmtInt(phB, "—"));
      }
      var extraSecond = part.subKey || (part.tailKeys.length > 1 ? part.tailKeys[1] : null);
      if (extraSecond && extraSecond !== tailKey) {
        var psA = tierPct(baseRun, "transactions", extraSecond);
        var psB = tierPct(targetRun, "transactions", extraSecond);
        if (psA != null || psB != null) tailExtras.push("P" + extraSecond + ": " + fmtInt(psA, "—") + " → " + fmtInt(psB, "—"));
      }
      card3 = renderKpiCardHtml("Transaction P" + tailKey, pTailA, pTailB, fmtInt, "ms", "lower",
        tailExtras.length ? tailExtras.join(" · ") : "Configured tail latency");
    } else {
      var minA = tierVal(baseRun, "transactions", "min");
      var minB = tierVal(targetRun, "transactions", "min");
      var maxA = tierVal(baseRun, "transactions", "max");
      var maxB = tierVal(targetRun, "transactions", "max");
      var mmSub = "Min " + fmtInt(minA, "—") + "→" + fmtInt(minB, "—") + " · Max " + fmtInt(maxA, "—") + "→" + fmtInt(maxB, "—") + " ms";
      card3 = renderKpiCardHtml("Transaction Median", medA, medB, fmtInt, "ms", "lower", mmSub);
    }

    // 4. Requests Throughput & Mean
    var reqRateA = tierVal(baseRun, "requests", "rate");
    var reqRateB = tierVal(targetRun, "requests", "rate");
    var reqMeanA = tierVal(baseRun, "requests", "mean");
    var reqMeanB = tierVal(targetRun, "requests", "mean");
    var reqRepPctKey = UI.pickRepresentativePercentile(pcts);
    var reqSubParts = [];
    if (reqMeanA != null || reqMeanB != null) {
      reqSubParts.push("Mean " + fmtInt(reqMeanA, "—") + " → " + fmtInt(reqMeanB, "—") + " ms");
    }
    if (reqRepPctKey) {
      var rpA = tierPct(baseRun, "requests", reqRepPctKey);
      var rpB = tierPct(targetRun, "requests", reqRepPctKey);
      if (rpA != null || rpB != null) reqSubParts.push("P" + reqRepPctKey + " " + fmtInt(rpA, "—") + " → " + fmtInt(rpB, "—") + " ms");
    }
    var card4 = renderKpiCardHtml("Request Throughput", reqRateA, reqRateB, function (n) { return fmtDec(n, 1); }, "req/s", "higher",
      reqSubParts.length ? reqSubParts.join(" · ") : "HTTP request rate");

    // 5. Errors & Events
    var errA = tierVal(baseRun, "transactions", "errors");
    var errB = tierVal(targetRun, "transactions", "errors");
    var evA = tierVal(baseRun, "transactions", "events");
    var evB = tierVal(targetRun, "transactions", "events");
    var errSub;
    if (caps.hasCounts !== false && (evA != null || evB != null)) {
      var evDelta = CMP.calcDelta(evA, evB);
      var evClass = (evB || 0) > 0 || (evA || 0) > 0 ? ' class="cell-warn"' : "";
      errSub = '<span' + evClass + ">Events: " + fmtInt(evA, "0") + " → " + fmtInt(evB, "0") +
        " (" + CMP.formatAbsDelta(evDelta, fmtInt, "") + ")</span>";
    } else {
      var reqErrA = tierVal(baseRun, "requests", "errors");
      var reqErrB = tierVal(targetRun, "requests", "errors");
      errSub = reqErrA != null || reqErrB != null
        ? "Request errors: " + fmtInt(reqErrA, "0") + " → " + fmtInt(reqErrB, "0")
        : "Transaction errors delta";
    }
    var card5 = renderKpiCardHtml("Transaction Errors", errA, errB, fmtInt, "err", "lower", errSub, { deadband: 0, absPill: true });

    grid.innerHTML = card1 + card2 + card3 + card4 + card5;
  }

  // =========================================================================
  // ECharts Visuals (Theme-aware, Offline)
  // =========================================================================

  function themePalette() {
    var dark = document.documentElement.classList.contains("dark-theme");
    return {
      dark: dark,
      text: dark ? "#e4e4e7" : "#18181b",
      muted: dark ? "#a1a1aa" : "#666e7a",
      split: dark ? "#2e2e36" : "#e5e7eb",
      axis: dark ? "#3f3f46" : "#cbd5e1",
      tipBg: dark ? "rgba(28, 28, 31, 0.96)" : "rgba(255, 255, 255, 0.96)",
      tipBorder: dark ? "#3f3f46" : "#c9d0d8",
      baseColor: dark ? "#60a5fa" : "#244b73",
      targetColor: dark ? "#fb923c" : "#e07000",
      errColor: dark ? "#f87171" : "#dc2626",
      throughputColor: dark ? "#3b82f6" : "#244b73"
    };
  }

  function ensureChart(domId, existing) {
    var dom = document.getElementById(domId);
    if (!dom || typeof window.echarts === "undefined") return null;
    if (existing && !existing.isDisposed()) return existing;
    var chart = window.echarts.init(dom, null, { renderer: "canvas" });
    if (typeof window.ResizeObserver !== "undefined") {
      var ro = new window.ResizeObserver(function () {
        if (!chart.isDisposed()) chart.resize();
      });
      ro.observe(dom);
      resizeObservers.push(ro);
    }
    return chart;
  }

  function disposeChart(ch) {
    if (ch && !ch.isDisposed()) ch.dispose();
  }

  function ensureTrendSeriesBar(chartDom) {
    var bar = document.getElementById("cmp-trend-series-bar");
    var status = document.getElementById("cmp-trend-series-status");
    if (!bar && chartDom && chartDom.parentNode) {
      bar = document.createElement("div");
      bar.id = "cmp-trend-series-bar";
      bar.className = "cmp-series-bar";
      bar.setAttribute("role", "toolbar");
      bar.setAttribute("focusgroup", "toolbar wrap");
      bar.setAttribute("aria-label", "Trajectory chart series");
      bar.setAttribute("aria-controls", "chart-trend-trajectory");
      chartDom.parentNode.insertBefore(bar, chartDom);
    }
    if (!status && chartDom && chartDom.parentNode) {
      status = document.createElement("span");
      status.id = "cmp-trend-series-status";
      status.className = "sr-only";
      status.setAttribute("role", "status");
      status.setAttribute("aria-live", "polite");
      chartDom.parentNode.insertBefore(status, chartDom);
    }
    return { bar: bar, status: status };
  }

  function renderTrendTrajectoryChart() {
    var dom = document.getElementById("chart-trend-trajectory");
    if (!dom) return;
    var runs = CMP.getRuns();
    if (runs.length < 3) return;
    trendChart = ensureChart("chart-trend-trajectory", trendChart);
    if (!trendChart) return;

    var pal = themePalette();
    var st = CMP.getState();
    var runMetrics = runs.map(function (r) {
      return r && r.summary && r.summary[activeTrendTier] ? r.summary[activeTrendTier] : null;
    });
    var pcts = UI.getConfiguredPercentiles(runMetrics.filter(Boolean));
    var traj = CMP.buildTrajectoryConfig({
      runs: runs,
      runMetrics: runMetrics,
      pcts: pcts,
      baseIdx: st.baseIdx,
      targetIdx: st.targetIdx,
      dark: pal.dark,
      valueUnit: "ms",
      valueFmt: fmtInt,
      showErrors: true,
      showEvents: activeTrendTier === "transactions",
      useHeadline: true
    });

    var els = ensureTrendSeriesBar(dom);
    if (els.bar && CMP.renderTrajectoryToolbar) {
      CMP.renderTrajectoryToolbar(els.bar, traj.seriesDefs, {
        statusEl: els.status,
        onChange: renderTrendTrajectoryChart
      });
    }

    trendChart.setOption(traj.echartOption, true);
  }

  function renderProfileChart(baseRun, targetRun, st) {
    profileChart = ensureChart("chart-profile-overlay", profileChart);
    if (!profileChart || !baseRun || !targetRun) return;

    var pal = themePalette();
    var pcts = UI.getConfiguredPercentiles();
    var cats = ["Mean", "Median"];
    var valsA = [tierVal(baseRun, activeProfileTier, "mean"), tierVal(baseRun, activeProfileTier, "median")];
    var valsB = [tierVal(targetRun, activeProfileTier, "mean"), tierVal(targetRun, activeProfileTier, "median")];

    if (pcts.length) {
      pcts.forEach(function (p) {
        cats.push("P" + p);
        valsA.push(tierPct(baseRun, activeProfileTier, p));
        valsB.push(tierPct(targetRun, activeProfileTier, p));
      });
    } else {
      cats.unshift("Min");
      valsA.unshift(tierVal(baseRun, activeProfileTier, "min"));
      valsB.unshift(tierVal(targetRun, activeProfileTier, "min"));
      cats.push("Max");
      valsA.push(tierVal(baseRun, activeProfileTier, "max"));
      valsB.push(tierVal(targetRun, activeProfileTier, "max"));
    }

    var nameA = "Baseline #" + (st.baseIdx + 1) + " (" + (baseRun.name || "A") + ")";
    var nameB = "Target #" + (st.targetIdx + 1) + " (" + (targetRun.name || "B") + ")";

    profileChart.setOption({
      animationDuration: 220,
      tooltip: {
        trigger: "axis",
        backgroundColor: pal.tipBg,
        borderColor: pal.tipBorder,
        borderWidth: 1,
        textStyle: { color: pal.text, fontSize: 12 },
        formatter: function (params) {
          if (!params || !params.length) return "";
          var s = "<strong>" + esc(params[0].axisValue) + " Runtime</strong><br/>";
          params.forEach(function (p) {
            s += p.marker + " " + esc(p.seriesName) + ": <strong>" + fmtInt(p.value, "—") + " ms</strong><br/>";
          });
          if (params.length >= 2 && params[0].value != null && params[1].value != null) {
            var d = CMP.calcDelta(params[0].value, params[1].value);
            s += "Δ: <strong>" + esc(CMP.formatAbsDelta(d, fmtInt, "ms")) + " (" + esc(CMP.formatPct(d)) + ")</strong>";
          }
          return s;
        }
      },
      legend: {
        bottom: 4,
        textStyle: { color: pal.muted, fontSize: 11 }
      },
      grid: { left: 60, right: 20, top: 20, bottom: 48 },
      xAxis: {
        type: "category",
        data: cats,
        axisLine: { lineStyle: { color: pal.axis } },
        axisLabel: { color: pal.muted, fontSize: 11 }
      },
      yAxis: {
        type: "value",
        name: "Runtime [ms]",
        nameTextStyle: { color: pal.muted, fontSize: 10 },
        axisLabel: { color: pal.muted, fontSize: 11 },
        splitLine: { lineStyle: { color: pal.split } }
      },
      series: [
        {
          name: nameA,
          type: "bar",
          barGap: "18%",
          barMaxWidth: 28,
          data: valsA,
          itemStyle: { color: pal.baseColor, borderRadius: [3, 3, 0, 0] }
        },
        {
          name: nameB,
          type: "bar",
          barMaxWidth: 28,
          data: valsB,
          itemStyle: { color: pal.targetColor, borderRadius: [3, 3, 0, 0] }
        }
      ]
    }, true);
  }

  function renderTopScenarioChart(baseRun, targetRun, st) {
    scenarioChart = ensureChart("chart-scenario-shift", scenarioChart);
    if (!scenarioChart || !baseRun || !targetRun) return;

    var ov = window.XLT_OVERVIEW || {};
    var pool = (ov.transactions && ov.transactions.length) ? ov.transactions
      : ((ov.actions && ov.actions.length) ? ov.actions : (ov.requests || []));

    var ranked = pool.map(function (it) {
      var rA = it.runs && it.runs[st.baseIdx];
      var rB = it.runs && it.runs[st.targetIdx];
      var mA = rA && rA.mean != null ? Number(rA.mean) : 0;
      var mB = rB && rB.mean != null ? Number(rB.mean) : 0;
      return { name: it.name, mA: rA ? rA.mean : null, mB: rB ? rB.mean : null, rank: Math.max(mA, mB) };
    }).sort(function (a, b) { return b.rank - a.rank; }).slice(0, 10).reverse();

    var pal = themePalette();
    scenarioChart.setOption({
      animationDuration: 220,
      tooltip: {
        trigger: "axis",
        axisPointer: { type: "shadow" },
        backgroundColor: pal.tipBg,
        borderColor: pal.tipBorder,
        borderWidth: 1,
        textStyle: { color: pal.text, fontSize: 12 }
      },
      legend: {
        bottom: 4,
        textStyle: { color: pal.muted, fontSize: 11 }
      },
      grid: { left: 170, right: 24, top: 14, bottom: 46 },
      xAxis: {
        type: "value",
        name: "Mean [ms]",
        nameTextStyle: { color: pal.muted, fontSize: 10 },
        axisLabel: { color: pal.muted, fontSize: 11 },
        splitLine: { lineStyle: { color: pal.split } }
      },
      yAxis: {
        type: "category",
        data: ranked.map(function (x) { return x.name; }),
        axisLine: { lineStyle: { color: pal.axis } },
        axisLabel: { color: pal.muted, fontSize: 11, width: 155, overflow: "truncate" }
      },
      series: [
        {
          name: "Baseline (A)",
          type: "bar",
          data: ranked.map(function (x) { return x.mA; }),
          barMaxWidth: 12,
          itemStyle: { color: pal.baseColor, borderRadius: [0, 3, 3, 0] }
        },
        {
          name: "Target (B)",
          type: "bar",
          data: ranked.map(function (x) { return x.mB; }),
          barMaxWidth: 12,
          itemStyle: { color: pal.targetColor, borderRadius: [0, 3, 3, 0] }
        }
      ]
    }, true);
  }

  // =========================================================================
  // Top Regressions & Improvements
  // =========================================================================

  function renderMovers(st) {
    var ov = window.XLT_OVERVIEW || {};
    var buckets = [];
    if (activeMoverScope === "all" || activeMoverScope === "transactions") {
      (ov.transactions || []).forEach(function (it) { buckets.push({ type: "TX", href: "transactions.html", item: it }); });
    }
    if (activeMoverScope === "all" || activeMoverScope === "actions") {
      (ov.actions || []).forEach(function (it) { buckets.push({ type: "ACT", href: "actions.html", item: it }); });
    }
    if (activeMoverScope === "all" || activeMoverScope === "requests") {
      (ov.requests || []).forEach(function (it) { buckets.push({ type: "REQ", href: "requests.html", item: it }); });
    }

    var regressions = [];
    var improvements = [];

    buckets.forEach(function (b) {
      var rA = b.item.runs && b.item.runs[st.baseIdx];
      var rB = b.item.runs && b.item.runs[st.targetIdx];
      if (!rA || !rB) return;

      var dMean = CMP.calcDelta(rA.mean, rB.mean);
      var dErr = CMP.calcDelta(rA.errors, rB.errors);
      var rel = dMean.status === "both" && Number.isFinite(dMean.rel) ? dMean.rel : 0;
      var absMs = dMean.status === "both" ? dMean.abs : 0;
      var absErr = dErr.status === "both" ? dErr.abs : 0;

      var score = (rel > 0 ? rel : -rel) + (Math.abs(absMs) > 25 ? Math.log10(Math.abs(absMs)) * 4 : 0);

      if ((rel >= 2.0 && absMs >= 3) || absErr > 0) {
        var useErr = absErr > 0 && rel < 5.0;
        regressions.push({
          type: b.type,
          href: b.href,
          name: b.item.name,
          key: b.item.key || b.item.name,
          isErrorMover: useErr,
          valA: useErr ? fmtInt(rA.errors) : fmtInt(rA.mean),
          valB: useErr ? (fmtInt(rB.errors) + " err") : (fmtInt(rB.mean) + " ms"),
          pillText: useErr ? CMP.formatAbsDelta(dErr, fmtInt, "err") : CMP.formatPct(dMean),
          rank: score + (absErr > 0 ? absErr * 5 : 0)
        });
      } else if ((rel <= -2.0 && absMs <= -3) || absErr < 0) {
        var useErrImp = absErr < 0 && rel > -5.0;
        improvements.push({
          type: b.type,
          href: b.href,
          name: b.item.name,
          key: b.item.key || b.item.name,
          valA: useErrImp ? fmtInt(rA.errors) : fmtInt(rA.mean),
          valB: useErrImp ? (fmtInt(rB.errors) + " err") : (fmtInt(rB.mean) + " ms"),
          pillText: useErrImp ? CMP.formatAbsDelta(dErr, fmtInt, "err") : CMP.formatPct(dMean),
          rank: score + (absErr < 0 ? Math.abs(absErr) * 5 : 0)
        });
      }
    });

    regressions.sort(function (a, b) { return b.rank - a.rank; });
    improvements.sort(function (a, b) { return b.rank - a.rank; });

    renderMoversList("cmp-regressions-list", "cmp-regressions-count", regressions.slice(0, 8), "delta-bad", st,
      "No notable regressions or error increases detected between these runs.");
    renderMoversList("cmp-improvements-list", "cmp-improvements-count", improvements.slice(0, 8), "delta-good", st,
      "No notable runtime speedups or error reductions detected between these runs.");
  }

  function renderMoversList(listId, countId, list, pillCls, st, emptyMsg) {
    var ul = document.getElementById(listId);
    var cntEl = document.getElementById(countId);
    if (cntEl) cntEl.textContent = list.length + " shown";
    if (!ul) return;

    if (!list.length) {
      ul.innerHTML = '<li class="cmp-empty-panel" style="padding:18px 14px">' + esc(emptyMsg) + "</li>";
      return;
    }

    ul.innerHTML = list.map(function (m) {
      var href = m.href + "?base=" + st.baseIdx + "&target=" + st.targetIdx + "&item=" + encodeURIComponent(m.key);
      return (
        '<li class="cmp-mover-row">' +
          '<div class="cmp-mover-left">' +
            '<span class="cmp-mover-type">' + esc(m.type) + "</span>" +
            '<a class="cmp-mover-link" href="' + esc(href) + '" title="' + esc(m.name) + '">' + esc(m.name) + "</a>" +
          "</div>" +
          '<div class="cmp-mover-right cmp-cell">' +
            '<span class="cmp-cell-base">' + esc(m.valA) + "</span>" +
            '<span class="cmp-cell-arrow">→</span>' +
            '<span class="cmp-cell-target">' + esc(m.valB) + "</span>" +
            '<span class="cmp-delta-pill ' + pillCls + '">' + esc(m.pillText) + "</span>" +
          "</div>" +
        "</li>"
      );
    }).join("");
    if (CMP.syncListAlignment) CMP.syncListAlignment(ul);
  }

  function wireTablist(containerId, onSelect) {
    var box = document.getElementById(containerId);
    if (!box) return;
    var btns = Array.prototype.slice.call(box.querySelectorAll('[role="tab"]'));
    box.addEventListener("click", function (e) {
      var btn = e.target.closest('[role="tab"]');
      if (!btn) return;
      btns.forEach(function (b) { b.setAttribute("aria-selected", b === btn ? "true" : "false"); });
      onSelect(btn);
    });
  }

  function renderAll() {
    var st = CMP.getState();
    var baseRun = CMP.getBaseRun();
    var targetRun = CMP.getTargetRun();
    renderSideBySideRuns(baseRun, targetRun, st);
    renderTimelineAndTrajectory(st);
    renderKpiCards(baseRun, targetRun);
    renderProfileChart(baseRun, targetRun, st);
    renderTopScenarioChart(baseRun, targetRun, st);
    renderMovers(st);
  }

  document.addEventListener("DOMContentLoaded", function () {
    wireTablist("cmp-trend-tier-tabs", function (btn) {
      activeTrendTier = btn.getAttribute("data-tier") || "transactions";
      renderTrendTrajectoryChart();
    });
    wireTablist("cmp-profile-tier-tabs", function (btn) {
      activeProfileTier = btn.getAttribute("data-tier") || "transactions";
      renderProfileChart(CMP.getBaseRun(), CMP.getTargetRun(), CMP.getState());
    });
    wireTablist("cmp-movers-scope-tabs", function (btn) {
      activeMoverScope = btn.getAttribute("data-scope") || "all";
      renderMovers(CMP.getState());
    });

    renderAll();

    window.addEventListener("xlt-cmp-change", renderAll);
    window.addEventListener("xlt-theme-changed", function () {
      renderTrendTrajectoryChart();
      renderProfileChart(CMP.getBaseRun(), CMP.getTargetRun(), CMP.getState());
      renderTopScenarioChart(CMP.getBaseRun(), CMP.getTargetRun(), CMP.getState());
    });
  });
})();
