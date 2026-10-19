/**
 * XLT ChunkDB Explorer - Interactive Telemetry & Forensic UI
 */
(function () {
  "use strict";

  const SERIES_COLORS = [
    "#38bdf8", "#f59e0b", "#10b981", "#a855f7",
    "#f43f5e", "#eab308", "#06b6d4", "#ec4899",
    "#84cc16", "#6366f1", "#14b8a6", "#f97316"
  ];

  // Application State
  const state = {
    theme: "light",
    meta: null,
    fromTime: 0,
    toTime: 0,
    zoomed: false,
    hasExecutedQuery: false,
    hasPendingChanges: false,
    activeTab: "summaryTab",
    summaryRows: [],
    summarySortCol: "p95",
    summarySortAsc: false,
    forensicRecords: [],
    expandedForensicIdx: null,
    lastTimeSeries: null,
    queryInFlight: false,
    pendingScope: null,
    feedbackLabel: "",
    timerInterval: null,
    sessionContext: null,
    lastSessionResponse: null
  };

  // DOM Elements
  const el = {
    datasetMeta: document.getElementById("datasetMeta"),
    themeToggleBtn: document.getElementById("themeToggleBtn"),
    statusDot: document.getElementById("statusDot"),
    statusText: document.getElementById("statusText"),
    recordTypeSelect: document.getElementById("recordTypeSelect"),
    nameFilterInput: document.getElementById("nameFilterInput"),
    clearNameFilterBtn: document.getElementById("clearNameFilterBtn"),
    timerNameList: document.getElementById("timerNameList"),
    fromTimeInput: document.getElementById("fromTimeInput"),
    toTimeInput: document.getElementById("toTimeInput"),
    scenarioSelect: document.getElementById("scenarioSelect"),
    agentSelect: document.getElementById("agentSelect"),
    statusFilterGroup: document.getElementById("statusFilterGroup"),
    statusPresetSelect: document.getElementById("statusPresetSelect"),
    failedOnlyCheckbox: document.getElementById("failedOnlyCheckbox"),
    runQueryBtn: document.getElementById("runQueryBtn"),
    resetZoomBtn: document.getElementById("resetZoomBtn"),
    resetFiltersBtn: document.getElementById("resetFiltersBtn"),
    timeWindowBadge: document.getElementById("timeWindowBadge"),
    bucketBadge: document.getElementById("bucketBadge"),
    chartMetricSelect: document.getElementById("chartMetricSelect"),
    chartGroupBySelect: document.getElementById("chartGroupBySelect"),
    bucketSizeSelect: document.getElementById("bucketSizeSelect"),
    chartContainer: document.getElementById("chartContainer"),
    canvas: document.getElementById("timeSeriesCanvas"),
    chartTooltip: document.getElementById("chartTooltip"),
    chartLegend: document.getElementById("chartLegend"),
    tabBtnSummary: document.getElementById("tabBtnSummary"),
    tabBtnForensic: document.getElementById("tabBtnForensic"),
    summaryToolbar: document.getElementById("summaryToolbar"),
    forensicToolbar: document.getElementById("forensicToolbar"),
    summaryTab: document.getElementById("summaryTab"),
    forensicTab: document.getElementById("forensicTab"),
    summaryGroupBySelect: document.getElementById("summaryGroupBySelect"),
    summarySearchInput: document.getElementById("summarySearchInput"),
    copyMarkdownBtn: document.getElementById("copyMarkdownBtn"),
    summaryTable: document.getElementById("summaryTable"),
    summaryTableBody: document.getElementById("summaryTableBody"),
    inspectModeSelect: document.getElementById("inspectModeSelect"),
    inspectLimitSelect: document.getElementById("inspectLimitSelect"),
    forensicTableBody: document.getElementById("forensicTableBody"),
    sessionModalBackdrop: document.getElementById("sessionModalBackdrop"),
    sessionMetaBar: document.getElementById("sessionMetaBar"),
    sessionModalBody: document.getElementById("sessionModalBody"),
    prevIterationBtn: document.getElementById("prevIterationBtn"),
    nextIterationBtn: document.getElementById("nextIterationBtn"),
    openResultDumpLink: document.getElementById("openResultDumpLink"),
    closeSessionModalBtn: document.getElementById("closeSessionModalBtn")
  };

  // Formatting Helpers
  function fmtInt(n) {
    if (n === null || n === undefined || Number.isNaN(n)) return "—";
    return Math.round(n).toLocaleString("en-US");
  }

  function fmtDec(n, digits = 1) {
    if (n === null || n === undefined || Number.isNaN(n)) return "—";
    return Number(n).toLocaleString("en-US", {
      minimumFractionDigits: digits,
      maximumFractionDigits: digits
    });
  }

  function fmtUtc(ms, includeDate = false) {
    if (!ms || ms <= 0) return "—";
    const d = new Date(ms);
    const iso = d.toISOString();
    return includeDate ? iso.replace("T", " ").substring(0, 23) + " UTC" : iso.substring(11, 23);
  }

  function fmtInputUtc(ms) {
    if (!ms || ms <= 0) return "";
    return new Date(ms).toISOString().replace("T", " ").substring(0, 19);
  }

  function parseInputUtc(rawStr, fallbackMs) {
    if (!rawStr || !rawStr.trim()) return fallbackMs;
    const s = rawStr.trim();
    if (/^\d{10,15}$/.test(s)) {
      return parseInt(s, 10);
    }
    // Time-only HH:mm or HH:mm:ss(.SSS) -> combine with date of fallbackMs
    if (/^\d{2}:\d{2}(:\d{2}(\.\d{1,3})?)?$/.test(s)) {
      const baseDate = new Date(fallbackMs || (state.meta ? state.meta.minTime : Date.now()))
        .toISOString()
        .substring(0, 10);
      const fullStr = `${baseDate}T${s.length === 5 ? s + ":00" : s}Z`;
      const parsed = Date.parse(fullStr);
      return Number.isNaN(parsed) ? fallbackMs : parsed;
    }
    // YYYY-MM-DD HH:mm(:ss) -> normalize to ISO UTC
    let norm = s.replace(" ", "T");
    if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(norm)) {
      norm += ":00";
    }
    if (!norm.endsWith("Z") && !/[+-]\d{2}:?\d{2}$/.test(norm)) {
      norm += "Z";
    }
    const parsed = Date.parse(norm);
    return Number.isNaN(parsed) ? fallbackMs : parsed;
  }

  function syncTimeInputsFromState() {
    if (el.fromTimeInput) el.fromTimeInput.value = fmtInputUtc(state.fromTime);
    if (el.toTimeInput) el.toTimeInput.value = fmtInputUtc(state.toTime);
    const spanMs = Math.max(1, state.toTime - state.fromTime);
    el.timeWindowBadge.textContent = state.zoomed
      ? `Selected: ${fmtUtc(state.fromTime)} → ${fmtUtc(state.toTime)} (${fmtDuration(spanMs)})`
      : `Full Window: ${fmtUtc(state.fromTime)} → ${fmtUtc(state.toTime)} (${fmtDuration(spanMs)})`;
    el.resetZoomBtn.classList.toggle("hidden", !state.zoomed);
  }

  function applyTimeInputsToState() {
    if (!state.meta) return;
    const parsedFrom = parseInputUtc(el.fromTimeInput.value, state.fromTime);
    const parsedTo = parseInputUtc(el.toTimeInput.value, state.toTime);
    if (parsedTo > parsedFrom) {
      state.fromTime = parsedFrom;
      state.toTime = parsedTo;
    } else {
      state.fromTime = Math.min(parsedFrom, parsedTo);
      state.toTime = Math.max(parsedFrom + 1000, parsedTo);
    }
    state.zoomed = state.fromTime > state.meta.minTime || state.toTime < state.meta.maxTime;
    syncTimeInputsFromState();
  }

  function markPendingChanges(reason = "Filters modified — click ▶ Update to run") {
    state.hasPendingChanges = true;
    if (el.runQueryBtn) {
      el.runQueryBtn.classList.add("has-pending");
      el.runQueryBtn.textContent = "▶ Update ●";
    }
    if (!state.queryInFlight) {
      el.statusDot.className = "status-dot pending";
      el.statusText.textContent = reason;
    }
  }

  function clearPendingChanges() {
    state.hasPendingChanges = false;
    if (el.runQueryBtn) {
      el.runQueryBtn.classList.remove("has-pending");
      el.runQueryBtn.textContent = "▶ Update";
    }
  }

  function fmtDuration(ms) {
    if (ms < 1000) return ms + " ms";
    const sec = ms / 1000;
    if (sec < 60) return sec.toFixed(1) + " s";
    const min = Math.floor(sec / 60);
    const remSec = Math.round(sec % 60);
    return `${min}m ${remSec}s`;
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  // Status Indicator & Elapsed Timer
  function startQueryFeedback(label) {
    if (state.timerInterval) clearInterval(state.timerInterval);
    const t0 = performance.now();
    state.feedbackLabel = label;
    el.statusDot.className = "status-dot loading";
    el.statusText.textContent = `${label}...`;
    state.timerInterval = setInterval(() => {
      const elapsedSec = ((performance.now() - t0) / 1000).toFixed(1);
      const queuedSuffix = state.pendingScope ? " · next update queued" : "";
      el.statusText.textContent = `${state.feedbackLabel} (${elapsedSec}s)${queuedSuffix}...`;
    }, 100);
    return t0;
  }

  function updateQueryFeedbackStep(label) {
    state.feedbackLabel = label;
  }

  function stopQueryFeedback(t0, extraInfo, isError = false) {
    if (state.timerInterval) {
      clearInterval(state.timerInterval);
      state.timerInterval = null;
    }
    const elapsedMs = Math.round(performance.now() - t0);
    el.statusDot.className = isError ? "status-dot error" : "status-dot idle";
    el.statusText.textContent = isError
      ? `Error: ${extraInfo}`
      : `Ready · ${elapsedMs} ms${extraInfo ? " · " + extraInfo : ""}`;
  }

  function mergeScopes(existing, incoming) {
    if (!existing) return incoming;
    if (existing === incoming) return existing;
    return "both";
  }

  function getStatusCodeBounds() {
    if (el.recordTypeSelect.value !== "R") {
      return { minStatusCode: null, maxStatusCode: null };
    }
    switch (el.statusPresetSelect.value) {
      case "4XX_5XX": return { minStatusCode: 400, maxStatusCode: 599 };
      case "5XX":     return { minStatusCode: 500, maxStatusCode: 599 };
      case "4XX":     return { minStatusCode: 400, maxStatusCode: 499 };
      case "2XX":     return { minStatusCode: 200, maxStatusCode: 299 };
      case "0":       return { minStatusCode: 0, maxStatusCode: 0 };
      default:        return { minStatusCode: null, maxStatusCode: null };
    }
  }

  function populateTimerNamesForType() {
    if (!state.meta || !state.meta.timers) return;
    const type = el.recordTypeSelect.value;
    const names = state.meta.timers[type] || [];
    el.timerNameList.innerHTML = names
      .slice(0, 500)
      .map(n => `<option value="${escapeHtml(n)}"></option>`)
      .join("");
  }

  // Load Dataset Metadata
  async function loadMetadata() {
    const t0 = startQueryFeedback("Loading metadata");
    try {
      const resp = await fetch("/api/v1/metadata");
      if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
      const meta = await resp.json();
      state.meta = meta;
      state.fromTime = meta.minTime;
      state.toTime = meta.maxTime;
      state.zoomed = false;
      syncTimeInputsFromState();

      const durationStr = fmtDuration(Math.max(0, meta.maxTime - meta.minTime));
      const modeLabel = meta.forensicMode === "full" ? "FULL (100% Forensics)" : "SPARSE (Tail + Failures)";
      const memLabel = meta.memoryMode || "DISK";
      el.datasetMeta.innerHTML = `
        <span class="meta-pill">Records: <strong>${fmtInt(meta.totalRecords)}</strong></span>
        <span class="meta-pill">Chunks: <strong>${fmtInt(meta.totalChunks)}</strong></span>
        <span class="meta-pill">Span: <strong>${fmtUtc(meta.minTime, true)} &rarr; ${fmtUtc(meta.maxTime, false)} (${durationStr})</strong></span>
        <span class="meta-pill">Forensics: <strong>${escapeHtml(modeLabel)}</strong></span>
        <span class="meta-pill">Memory: <strong>${escapeHtml(memLabel)}</strong></span>
      `;

      el.scenarioSelect.innerHTML = `<option value="">All Scenarios (${(meta.scenarios || []).length})</option>` +
        (meta.scenarios || []).map(s => `<option value="${escapeHtml(s)}">${escapeHtml(s)}</option>`).join("");

      el.agentSelect.innerHTML = `<option value="">All Agents (${(meta.agents || []).length})</option>` +
        (meta.agents || []).map(a => `<option value="${escapeHtml(a)}">${escapeHtml(a)}</option>`).join("");

      populateTimerNamesForType();
      stopQueryFeedback(t0, `${fmtInt(meta.totalRecords)} records`);
      renderTimeSeriesChart(null);
      markPendingChanges(`Ready (${fmtInt(meta.totalRecords)} records) — configure filters and click ▶ Update to run`);
    } catch (err) {
      stopQueryFeedback(t0, err.message, true);
    }
  }

  // Strictly Sequential, Single-Flight Dashboard Refresh
  // Never runs two backend analyses concurrently; queues any user interaction until the active analysis finishes.
  async function refreshDashboard(requestedScope = "both") {
    const scope = typeof requestedScope === "string" ? requestedScope : "both";
    if (!state.meta) return;

    // If an analysis is already running, queue/coalesce the requested scope and wait for the active one to finish.
    if (state.queryInFlight) {
      state.pendingScope = mergeScopes(state.pendingScope, scope);
      return;
    }

    state.queryInFlight = true;
    let nextScope = scope;

    try {
      while (nextScope) {
        state.pendingScope = null;
        await executeSingleSequentialPass(nextScope);
        nextScope = state.pendingScope;
      }
    } finally {
      state.queryInFlight = false;
    }
  }

  async function executeSingleSequentialPass(scope) {
    state.hasExecutedQuery = true;
    const doChart = scope === "both" || scope === "chart";
    const doBottom = scope === "both" || scope === "bottom";

    const t0 = startQueryFeedback(
      doChart && doBottom
        ? "1/2 · Querying time-series"
        : doChart
          ? "Querying time-series"
          : "Querying table"
    );

    const { minStatusCode, maxStatusCode } = getStatusCodeBounds();
    const timerQuery = el.nameFilterInput.value.trim() || null;
    const agents = el.agentSelect.value ? [el.agentSelect.value] : null;
    const scenarios = el.scenarioSelect.value ? [el.scenarioSelect.value] : null;
    const failedOnly = el.failedOnlyCheckbox.checked || null;

    const commonFilter = {
      recordType: el.recordTypeSelect.value,
      fromTime: state.fromTime,
      toTime: state.toTime,
      timerQuery,
      agents,
      scenarios,
      failedOnly,
      minStatusCode,
      maxStatusCode
    };

    // Update Time Window Badge & inputs
    clearPendingChanges();
    chartInteraction.pendingBrushX1 = null;
    chartInteraction.pendingBrushX2 = null;
    syncTimeInputsFromState();

    try {
      let summaryBadgeInfo = "";

      // Step 1: Time-Series Query (if needed)
      if (doChart) {
        const tsResp = await fetch("/api/v1/timeseries", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            ...commonFilter,
            bucketWidthMs: parseInt(el.bucketSizeSelect.value, 10) || 0,
            groupBy: el.chartGroupBySelect.value
          })
        });
        if (!tsResp.ok) throw new Error(`HTTP ${tsResp.status}`);
        const tsData = await tsResp.json();
        state.lastTimeSeries = tsData;
        el.bucketBadge.textContent = `Bucket: ${fmtDuration(tsData.bucketWidthMs)}`;
        renderTimeSeriesChart(tsData);
        summaryBadgeInfo = `${fmtInt(tsData.rowsMatched)} rows · ${tsData.chunksScanned} chunks`;
      }

      // If the user triggered a new global filter while Step 1 was running, skip old Step 2 and let the outer loop run the fresh state
      if (state.pendingScope === "both") {
        stopQueryFeedback(t0, summaryBadgeInfo);
        return;
      }

      // Step 2: Bottom Table Query (runs strictly AFTER Step 1 completes)
      if (doBottom) {
        if (doChart) {
          updateQueryFeedbackStep(
            state.activeTab === "summaryTab"
              ? "2/2 · Aggregating summary table"
              : "2/2 · Inspecting forensic outliers"
          );
        }

        if (state.activeTab === "summaryTab") {
          const sumResp = await fetch("/api/v1/summary-table", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              ...commonFilter,
              groupBy: el.summaryGroupBySelect.value
            })
          });
          if (!sumResp.ok) throw new Error(`HTTP ${sumResp.status}`);
          const bottomData = await sumResp.json();
          state.summaryRows = bottomData.rows || [];
          renderSummaryTable();
          if (!summaryBadgeInfo) {
            summaryBadgeInfo = `${fmtInt(bottomData.totalRowsMatched)} rows · ${state.summaryRows.length} groups`;
          }
        } else {
          const inspResp = await fetch("/api/v1/inspect", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              mode: el.inspectModeSelect.value,
              limit: parseInt(el.inspectLimitSelect.value, 10) || 100,
              fromTime: state.fromTime,
              toTime: state.toTime,
              timerQuery,
              agents,
              scenarios,
              minStatusCode,
              maxStatusCode,
              failedOnly
            })
          });
          if (!inspResp.ok) throw new Error(`HTTP ${inspResp.status}`);
          const bottomData = await inspResp.json();
          state.forensicRecords = bottomData.records || [];
          state.expandedForensicIdx = null;
          renderForensicTable();
          if (!summaryBadgeInfo) {
            summaryBadgeInfo = `${state.forensicRecords.length} forensic records`;
          }
        }
      }

      stopQueryFeedback(t0, summaryBadgeInfo);
    } catch (err) {
      stopQueryFeedback(t0, err.message, true);
    }
  }

  // Interactive Canvas Time-Series Renderer + Brush-to-Zoom
  const chartInteraction = {
    dragging: false,
    dragStartX: 0,
    dragCurX: 0,
    pendingBrushX1: null,
    pendingBrushX2: null,
    hoverX: null,
    padLeft: 56,
    padRight: 18,
    padTop: 14,
    padBottom: 24
  };

  function buildChartDatasets(tsData) {
    if (!tsData || !tsData.timestamps || tsData.timestamps.length === 0) return { unit: "ms", lines: [] };
    const metricMode = el.chartMetricSelect.value;
    const isGrouped = el.chartGroupBySelect.value !== "NONE" && tsData.series && tsData.series.length > 0;

    if (isGrouped) {
      return {
        unit: metricMode === "THROUGHPUT" ? "req/s" : "ms",
        lines: tsData.series.map((s, idx) => ({
          label: s.key,
          color: SERIES_COLORS[idx % SERIES_COLORS.length],
          values: metricMode === "THROUGHPUT" ? s.rps : s.p95
        }))
      };
    }

    if (metricMode === "THROUGHPUT") {
      const bucketSec = Math.max(0.001, (tsData.bucketWidthMs || 1000) / 1000.0);
      return {
        unit: "rate/s",
        lines: [
          { label: "Throughput (RPS)", color: "#0284c7", values: tsData.rps || [] },
          { label: "Errors / sec", color: "#e11d48", values: (tsData.errors || []).map(e => e / bucketSec) }
        ]
      };
    } else if (metricMode === "NETWORK_PHASES") {
      return {
        unit: "ms",
        lines: [
          { label: "Server Busy Mean", color: "#d97706", values: tsData.avgServerBusy || [] },
          { label: "Connect Mean", color: "#0284c7", values: tsData.avgConnect || [] },
          { label: "Receive Mean", color: "#059669", values: tsData.avgReceive || [] },
          { label: "Send Mean", color: "#7c3aed", values: tsData.avgSend || [] }
        ]
      };
    } else {
      return {
        unit: "ms",
        lines: [
          { label: "p99", color: "#e11d48", values: tsData.p99 || [] },
          { label: "p95", color: "#d97706", values: tsData.p95 || [] },
          { label: "p90", color: "#7c3aed", values: tsData.p90 || [] },
          { label: "p50 (Median)", color: "#059669", values: tsData.p50 || [] },
          { label: "Mean", color: "#0284c7", values: tsData.mean || [] }
        ]
      };
    }
  }

  function renderTimeSeriesChart(tsData) {
    const canvas = el.canvas;
    const rect = el.chartContainer.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    const width = Math.max(300, Math.floor(rect.width));
    const height = Math.max(140, Math.floor(rect.height));
    if (canvas.width !== width * dpr || canvas.height !== height * dpr) {
      canvas.width = width * dpr;
      canvas.height = height * dpr;
    }

    const isDark = state.theme === "dark";
    const gridColor = isDark ? "#1e293b" : "#e2e8f0";
    const axisTextColor = isDark ? "#64748b" : "#475569";

    const ctx = canvas.getContext("2d");
    ctx.save();
    ctx.scale(dpr, dpr);
    ctx.clearRect(0, 0, width, height);

    const { padLeft, padRight, padTop, padBottom } = chartInteraction;
    const plotW = width - padLeft - padRight;
    const plotH = height - padTop - padBottom;

    const { unit, lines } = buildChartDatasets(tsData);
    el.chartLegend.innerHTML = lines.map(l => `
      <span class="legend-item">
        <span class="legend-swatch" style="background:${l.color}"></span>
        ${escapeHtml(l.label)}
      </span>
    `).join("");

    if (!tsData || !tsData.timestamps || tsData.timestamps.length === 0 || lines.length === 0) {
      ctx.fillStyle = axisTextColor;
      ctx.font = "12.5px sans-serif";
      ctx.textAlign = "center";
      const emptyMsg = state.hasExecutedQuery
        ? "No matching time-series data in selected window"
        : "Configure filters or time range above and click ▶ Update (or press Enter) to run analysis";
      ctx.fillText(emptyMsg, width / 2, height / 2);
      ctx.restore();
      return;
    }

    const timestamps = tsData.timestamps;
    const n = timestamps.length;
    let maxVal = 0;
    for (const line of lines) {
      for (let i = 0; i < n; i++) {
        const v = line.values[i] || 0;
        if (v > maxVal) maxVal = v;
      }
    }
    if (maxVal <= 0) maxVal = 10;
    else maxVal *= 1.08;

    // Grid lines & Y-axis labels
    ctx.strokeStyle = gridColor;
    ctx.fillStyle = axisTextColor;
    ctx.font = "10.5px monospace";
    ctx.textAlign = "right";
    const yTicks = 4;
    for (let i = 0; i <= yTicks; i++) {
      const ratio = i / yTicks;
      const y = padTop + plotH - ratio * plotH;
      const val = ratio * maxVal;
      ctx.beginPath();
      ctx.moveTo(padLeft, y);
      ctx.lineTo(padLeft + plotW, y);
      ctx.stroke();
      ctx.fillText(val >= 100 ? Math.round(val).toString() : val.toFixed(1), padLeft - 6, y + 3);
    }

    // X-axis time ticks
    ctx.textAlign = "center";
    const xTicks = Math.min(6, n);
    for (let i = 0; i < xTicks; i++) {
      const idx = Math.min(n - 1, Math.floor((i / Math.max(1, xTicks - 1)) * (n - 1)));
      const x = padLeft + (n === 1 ? plotW / 2 : (idx / (n - 1)) * plotW);
      ctx.fillText(fmtUtc(timestamps[idx]), x, height - 6);
    }

    // Draw series lines
    for (const line of lines) {
      ctx.strokeStyle = line.color;
      ctx.lineWidth = 1.6;
      ctx.beginPath();
      for (let i = 0; i < n; i++) {
        const x = padLeft + (n === 1 ? plotW / 2 : (i / (n - 1)) * plotW);
        const y = padTop + plotH - ((line.values[i] || 0) / maxVal) * plotH;
        if (i === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.stroke();
    }

    // Draw brush selection if dragging or if a pending brush selection exists
    if (chartInteraction.dragging) {
      const x1 = Math.max(padLeft, Math.min(chartInteraction.dragStartX, chartInteraction.dragCurX));
      const x2 = Math.min(padLeft + plotW, Math.max(chartInteraction.dragStartX, chartInteraction.dragCurX));
      ctx.fillStyle = "rgba(2, 132, 199, 0.22)";
      ctx.fillRect(x1, padTop, x2 - x1, plotH);
      ctx.strokeStyle = "#0284c7";
      ctx.strokeRect(x1, padTop, x2 - x1, plotH);
    } else if (chartInteraction.pendingBrushX1 !== null && chartInteraction.pendingBrushX2 !== null) {
      const x1 = Math.max(padLeft, chartInteraction.pendingBrushX1);
      const x2 = Math.min(padLeft + plotW, chartInteraction.pendingBrushX2);
      ctx.fillStyle = "rgba(217, 119, 6, 0.18)";
      ctx.fillRect(x1, padTop, x2 - x1, plotH);
      ctx.strokeStyle = "#d97706";
      ctx.strokeRect(x1, padTop, x2 - x1, plotH);
    }

    // Draw hover crosshair & tooltip
    if (chartInteraction.hoverX !== null && !chartInteraction.dragging) {
      const relX = Math.max(0, Math.min(plotW, chartInteraction.hoverX - padLeft));
      const idx = n === 1 ? 0 : Math.round((relX / plotW) * (n - 1));
      const snapX = padLeft + (n === 1 ? plotW / 2 : (idx / (n - 1)) * plotW);

      ctx.strokeStyle = "rgba(148, 163, 184, 0.5)";
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(snapX, padTop);
      ctx.lineTo(snapX, padTop + plotH);
      ctx.stroke();

      const tBucket = timestamps[idx];
      let html = `<div style="margin-bottom:4px;font-weight:600">${fmtUtc(tBucket, true)}</div>`;
      for (const line of lines) {
        const v = line.values[idx] || 0;
        html += `<div style="display:flex;justify-content:space-between;gap:12px">
          <span style="color:${line.color}">● ${escapeHtml(line.label)}</span>
          <strong>${fmtDec(v, 1)} ${unit}</strong>
        </div>`;
      }
      el.chartTooltip.innerHTML = html;
      el.chartTooltip.classList.remove("hidden");
      const tooltipLeft = snapX + 195 < width ? snapX + 12 : snapX - 195;
      el.chartTooltip.style.left = `${Math.max(8, tooltipLeft)}px`;
      el.chartTooltip.style.top = `${padTop + 6}px`;
    } else {
      el.chartTooltip.classList.add("hidden");
    }

    ctx.restore();
  }

  function setupChartInteractions() {
    el.canvas.addEventListener("mousedown", e => {
      const rect = el.canvas.getBoundingClientRect();
      chartInteraction.dragging = true;
      chartInteraction.dragStartX = e.clientX - rect.left;
      chartInteraction.dragCurX = chartInteraction.dragStartX;
    });

    window.addEventListener("mousemove", e => {
      const rect = el.canvas.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;
      if (chartInteraction.dragging) {
        chartInteraction.dragCurX = x;
        renderTimeSeriesChart(state.lastTimeSeries);
      } else if (x >= chartInteraction.padLeft && x <= rect.width - chartInteraction.padRight && y >= 0 && y <= rect.height) {
        chartInteraction.hoverX = x;
        renderTimeSeriesChart(state.lastTimeSeries);
      } else if (chartInteraction.hoverX !== null) {
        chartInteraction.hoverX = null;
        renderTimeSeriesChart(state.lastTimeSeries);
      }
    });

    window.addEventListener("mouseup", () => {
      if (!chartInteraction.dragging) return;
      chartInteraction.dragging = false;
      const tsData = state.lastTimeSeries;
      if (!tsData || !tsData.timestamps || tsData.timestamps.length < 2) {
        renderTimeSeriesChart(tsData);
        return;
      }
      const rect = el.canvas.getBoundingClientRect();
      const plotW = rect.width - chartInteraction.padLeft - chartInteraction.padRight;
      const rawX1 = Math.min(chartInteraction.dragStartX, chartInteraction.dragCurX);
      const rawX2 = Math.max(chartInteraction.dragStartX, chartInteraction.dragCurX);
      const x1 = Math.max(0, Math.min(plotW, rawX1 - chartInteraction.padLeft));
      const x2 = Math.max(0, Math.min(plotW, rawX2 - chartInteraction.padLeft));

      if (x2 - x1 > 12) {
        const n = tsData.timestamps.length;
        const idx1 = Math.max(0, Math.floor((x1 / plotW) * (n - 1)));
        const idx2 = Math.min(n - 1, Math.ceil((x2 / plotW) * (n - 1)));
        const newFrom = tsData.timestamps[idx1];
        const newTo = tsData.timestamps[idx2] + tsData.bucketWidthMs;
        if (newTo > newFrom) {
          state.fromTime = newFrom;
          state.toTime = newTo;
          state.zoomed = true;
          chartInteraction.pendingBrushX1 = chartInteraction.padLeft + x1;
          chartInteraction.pendingBrushX2 = chartInteraction.padLeft + x2;
          syncTimeInputsFromState();
          markPendingChanges("Time range selected — click ▶ Update to run");
          renderTimeSeriesChart(tsData);
          return;
        }
      }
      renderTimeSeriesChart(tsData);
    });

    el.canvas.addEventListener("dblclick", () => {
      if (state.meta && state.zoomed) {
        state.fromTime = state.meta.minTime;
        state.toTime = state.meta.maxTime;
        state.zoomed = false;
        chartInteraction.pendingBrushX1 = null;
        chartInteraction.pendingBrushX2 = null;
        syncTimeInputsFromState();
        markPendingChanges("Full time window restored — click ▶ Update to run");
        renderTimeSeriesChart(state.lastTimeSeries);
      }
    });

    window.addEventListener("resize", () => {
      renderTimeSeriesChart(state.lastTimeSeries);
    });
  }

  // Render Tab 1: Multi-Dimensional Summary Table
  function getSortedFilteredSummaryRows() {
    const q = el.summarySearchInput.value.trim().toLowerCase();
    const rows = state.summaryRows.filter(r => !q || (r.key && r.key.toLowerCase().includes(q)));
    const col = state.summarySortCol;
    const dir = state.summarySortAsc ? 1 : -1;

    rows.sort((a, b) => {
      const va = a[col];
      const vb = b[col];
      if (typeof va === "string") return va.localeCompare(vb) * dir;
      return ((va || 0) - (vb || 0)) * dir;
    });
    return rows;
  }

  function renderSummaryTable() {
    const rows = getSortedFilteredSummaryRows();
    if (rows.length === 0) {
      el.summaryTableBody.innerHTML = `<tr><td colspan="17" class="empty-cell">No matching records for current filter criteria.</td></tr>`;
      return;
    }

    el.summaryTableBody.innerHTML = rows.map((r, idx) => {
      const errBadge = r.errors > 0
        ? `<span class="badge badge-err">${fmtInt(r.errors)} (${fmtDec(r.errorPct, 1)}%)</span>`
        : `<span class="badge badge-ok">0</span>`;
      return `
        <tr class="data-row" data-idx="${idx}">
          <td class="key-cell" title="${escapeHtml(r.key)}">${escapeHtml(r.key)}</td>
          <td class="num">${fmtInt(r.count)}</td>
          <td class="num">${fmtDec(r.rps, 2)}</td>
          <td class="num">${errBadge}</td>
          <td class="num">${fmtDec(r.errorPct, 2)}%</td>
          <td class="num">${fmtDec(r.mean, 1)}</td>
          <td class="num">${fmtInt(r.min)}</td>
          <td class="num">${fmtInt(r.p50)}</td>
          <td class="num">${fmtInt(r.p90)}</td>
          <td class="num"><strong>${fmtInt(r.p95)}</strong></td>
          <td class="num">${fmtInt(r.p99)}</td>
          <td class="num">${fmtInt(r.max)}</td>
          <td class="num">${fmtDec(r.avgTtfb, 1)}</td>
          <td class="num">${fmtDec(r.avgConnect, 1)}</td>
          <td class="num">${fmtDec(r.avgServerBusy, 1)}</td>
          <td class="num">${fmtInt(r.distinctUrls)}</td>
          <td>
            <button type="button" class="btn btn-secondary btn-xs inspect-dim-btn" data-key="${escapeHtml(r.key)}" title="Inspect forensic outliers for this group">
              🔬 Outliers
            </button>
          </td>
        </tr>
      `;
    }).join("");

    el.summaryTableBody.querySelectorAll("tr.data-row").forEach(tr => {
      tr.addEventListener("click", e => {
        if (e.target.closest(".inspect-dim-btn")) return;
        const row = rows[parseInt(tr.dataset.idx, 10)];
        if (!row) return;
        applyDimensionFilter(el.summaryGroupBySelect.value, row.key, false);
      });
    });

    el.summaryTableBody.querySelectorAll(".inspect-dim-btn").forEach(btn => {
      btn.addEventListener("click", e => {
        e.stopPropagation();
        applyDimensionFilter(el.summaryGroupBySelect.value, btn.dataset.key, true);
      });
    });
  }

  function applyDimensionFilter(groupBy, key, switchToForensics) {
    if (groupBy === "TIMER") {
      el.nameFilterInput.value = key;
      el.clearNameFilterBtn.classList.remove("hidden");
    } else if (groupBy === "AGENT") {
      el.agentSelect.value = key;
    } else if (groupBy === "SCENARIO") {
      el.scenarioSelect.value = key;
    }
    if (switchToForensics) {
      switchTab("forensicTab");
    }
    markPendingChanges("Filter selected from table — click ▶ Update to run");
  }

  // Copy Summary Table as GitHub Markdown
  async function copyTableAsMarkdown() {
    const rows = getSortedFilteredSummaryRows();
    const lines = [
      "| Dimension | Count | RPS | Errors | Error % | Mean (ms) | p50 | p90 | p95 | p99 | Max |",
      "| :--- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |"
    ];
    for (const r of rows.slice(0, 100)) {
      lines.push(`| \`${r.key}\` | ${fmtInt(r.count)} | ${fmtDec(r.rps, 2)} | ${fmtInt(r.errors)} | ${fmtDec(r.errorPct, 2)}% | ${fmtDec(r.mean, 1)} | ${fmtInt(r.p50)} | ${fmtInt(r.p90)} | ${fmtInt(r.p95)} | ${fmtInt(r.p99)} | ${fmtInt(r.max)} |`);
    }
    try {
      await navigator.clipboard.writeText(lines.join("\n"));
      const orig = el.copyMarkdownBtn.textContent;
      el.copyMarkdownBtn.textContent = "✓ Copied Markdown!";
      setTimeout(() => { el.copyMarkdownBtn.textContent = orig; }, 1800);
    } catch (_) {
      // Fallback ignored
    }
  }

  // Render Tab 2: Forensic Outlier & Failure Inspector
  function renderForensicTable() {
    const records = state.forensicRecords;
    if (!records || records.length === 0) {
      el.forensicTableBody.innerHTML = `<tr><td colspan="9" class="empty-cell">No forensic records found in current time window and filters.</td></tr>`;
      return;
    }

    const htmlRows = [];
    records.forEach((rec, idx) => {
      const isTx = rec.recordType === "T";
      const statusBadge = isTx
        ? (rec.failed ? `<span class="badge badge-err">FAILED TX</span>` : `<span class="badge badge-ok">OK</span>`)
        : (rec.failed || rec.statusCode >= 400
            ? `<span class="badge badge-err">HTTP ${rec.statusCode}</span>`
            : `<span class="badge badge-ok">HTTP ${rec.statusCode}</span>`);

      const summaryDetail = isTx
        ? `<span class="url-preview">${escapeHtml(rec.failedActionName ? "Action: " + rec.failedActionName + " — " : "")}${escapeHtml(rec.stackTrace || "")}</span>`
        : `<span class="badge badge-info">${escapeHtml(rec.httpMethod || "GET")}</span> <span class="url-preview" title="${escapeHtml(rec.url)}">${escapeHtml(rec.url || "—")}</span>`;

      htmlRows.push(`
        <tr class="data-row forensic-row" data-idx="${idx}">
          <td class="num">${fmtUtc(rec.time, true)}</td>
          <td class="key-cell" title="${escapeHtml(rec.timerName)}">${escapeHtml(rec.timerName)}</td>
          <td class="num"><strong>${fmtInt(rec.runTime)} ms</strong></td>
          <td>${statusBadge}</td>
          <td>${escapeHtml(rec.scenario)} / ${escapeHtml(rec.agent)}${rec.userNumber ? " #" + escapeHtml(rec.userNumber) : ""}</td>
          <td>${summaryDetail}</td>
          <td class="num">${escapeHtml(rec.usedIp || "—")}</td>
          <td class="num">${escapeHtml(rec.requestId || "—")}</td>
          <td>
            <button type="button" class="btn btn-accent btn-xs trace-session-btn" data-idx="${idx}">
              🔍 Session
            </button>
          </td>
        </tr>
      `);

      if (state.expandedForensicIdx === idx) {
        htmlRows.push(renderForensicDrawerRow(rec));
      }
    });

    el.forensicTableBody.innerHTML = htmlRows.join("");

    el.forensicTableBody.querySelectorAll("tr.forensic-row").forEach(tr => {
      tr.addEventListener("click", e => {
        if (e.target.closest(".trace-session-btn")) return;
        const idx = parseInt(tr.dataset.idx, 10);
        state.expandedForensicIdx = state.expandedForensicIdx === idx ? null : idx;
        renderForensicTable();
      });
    });

    el.forensicTableBody.querySelectorAll(".trace-session-btn").forEach(btn => {
      btn.addEventListener("click", e => {
        e.stopPropagation();
        const rec = state.forensicRecords[parseInt(btn.dataset.idx, 10)];
        if (rec) openSessionModal(rec);
      });
    });
  }

  function renderForensicDrawerRow(rec) {
    if (rec.recordType === "T") {
      return `
        <tr class="drawer-row">
          <td colspan="9">
            <div class="forensic-drawer">
              <div class="drawer-card">
                <h4>Transaction Context</h4>
                <div class="kv-grid">
                  <span class="kv-label">Transaction:</span><span class="kv-val">${escapeHtml(rec.timerName)}</span>
                  <span class="kv-label">Agent / User:</span><span class="kv-val">${escapeHtml(rec.agent)} / User #${escapeHtml(rec.userNumber || "0")}</span>
                  <span class="kv-label">Failed Action:</span><span class="kv-val">${escapeHtml(rec.failedActionName || "—")}</span>
                  <span class="kv-label">Duration:</span><span class="kv-val">${fmtInt(rec.runTime)} ms</span>
                </div>
              </div>
              <div class="drawer-card">
                <h4>Failure Stack Trace</h4>
                <pre class="stacktrace-pre">${escapeHtml(rec.stackTrace || "No stack trace recorded")}</pre>
              </div>
            </div>
          </td>
        </tr>
      `;
    }

    const totalPhase = Math.max(1, rec.connectTime + rec.sendTime + rec.serverBusyTime + rec.receiveTime);
    const pConn = ((rec.connectTime / totalPhase) * 100).toFixed(1);
    const pSend = ((rec.sendTime / totalPhase) * 100).toFixed(1);
    const pBusy = ((rec.serverBusyTime / totalPhase) * 100).toFixed(1);
    const pRecv = ((rec.receiveTime / totalPhase) * 100).toFixed(1);

    const exactBadge = rec.forensicExact
      ? `<span class="badge badge-ok">✓ Exact Per-Row Forensic Record</span>`
      : `<span class="badge badge-warn" title="Fast 200 OK request in sparse cache mode; showing representative sample URL for this timer.">ℹ️ Representative Timer URL (Sparse Mode)</span>`;

    return `
      <tr class="drawer-row">
        <td colspan="9">
          <div class="forensic-drawer">
            <div class="drawer-card">
              <h4>Request Forensics ${exactBadge}</h4>
              <div class="kv-grid">
                <span class="kv-label">Full URL:</span><span class="kv-val">${escapeHtml(rec.url || "—")}</span>
                <span class="kv-label">Raw Timer Name:</span><span class="kv-val">${escapeHtml(rec.rawName || rec.timerName)}</span>
                <span class="kv-label">HTTP Method / Status:</span><span class="kv-val">${escapeHtml(rec.httpMethod || "GET")} &rarr; HTTP ${rec.statusCode}</span>
                <span class="kv-label">Request / Resp ID:</span><span class="kv-val">${escapeHtml(rec.requestId || "—")} / #${escapeHtml(rec.responseId || "0")}</span>
                <span class="kv-label">Used Backend IP:</span><span class="kv-val">${escapeHtml(rec.usedIp || "—")}</span>
                <span class="kv-label">Content-Type:</span><span class="kv-val">${escapeHtml(rec.contentType || "—")}</span>
                <span class="kv-label">Bytes Sent / Recv:</span><span class="kv-val">${fmtInt(rec.bytesSent)} B / ${fmtInt(rec.bytesReceived)} B</span>
                <span class="kv-label">Form Data:</span><span class="kv-val">${escapeHtml(rec.formData || "—")}</span>
              </div>
            </div>
            <div class="drawer-card">
              <h4>Network Phase Waterfall (Total: ${fmtInt(rec.runTime)} ms · TTFB: ${fmtInt(rec.ttfb)} ms · TTLB: ${fmtInt(rec.ttlb)} ms)</h4>
              <div class="phase-waterfall-bar">
                <div class="phase-seg phase-connect" style="width:${pConn}%" title="Connect: ${rec.connectTime} ms"></div>
                <div class="phase-seg phase-send" style="width:${pSend}%" title="Send: ${rec.sendTime} ms"></div>
                <div class="phase-seg phase-busy" style="width:${pBusy}%" title="Server Busy: ${rec.serverBusyTime} ms"></div>
                <div class="phase-seg phase-recv" style="width:${pRecv}%" title="Receive: ${rec.receiveTime} ms"></div>
              </div>
              <div class="phase-legend">
                <span><span class="legend-swatch phase-connect"></span> Connect: <strong>${fmtInt(rec.connectTime)} ms</strong></span>
                <span><span class="legend-swatch phase-send"></span> Send: <strong>${fmtInt(rec.sendTime)} ms</strong></span>
                <span><span class="legend-swatch phase-busy"></span> Server Busy: <strong>${fmtInt(rec.serverBusyTime)} ms</strong></span>
                <span><span class="legend-swatch phase-recv"></span> Receive: <strong>${fmtInt(rec.receiveTime)} ms</strong></span>
              </div>
            </div>
          </div>
        </td>
      </tr>
    `;
  }

  // Virtual User Session Reconstruction Modal
  async function openSessionModal(rec) {
    state.sessionContext = {
      agent: rec.agent,
      scenario: rec.scenario,
      userNumber: rec.userNumber || "0",
      targetTime: rec.time,
      paddingMs: 30000
    };
    el.sessionModalBackdrop.classList.remove("hidden");
    await loadSessionIteration();
  }

  async function loadSessionIteration() {
    if (!state.sessionContext) return;
    el.sessionModalBody.innerHTML = `<div class="empty-cell">Reconstructing Virtual User iteration...</div>`;
    el.openResultDumpLink.classList.add("hidden");

    try {
      const resp = await fetch("/api/v1/session", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(state.sessionContext)
      });
      const sess = await resp.json();
      state.lastSessionResponse = sess;

      el.prevIterationBtn.disabled = !sess.prevTransactionTime;
      el.nextIterationBtn.disabled = !sess.nextTransactionTime;

      const spanMs = Math.max(1, sess.windowEnd - sess.windowStart);
      el.sessionMetaBar.innerHTML = `
        <span>Scenario: <strong>${escapeHtml(sess.scenario)}</strong></span>
        <span>Agent: <strong>${escapeHtml(sess.agent)}</strong></span>
        <span>User: <strong>#${escapeHtml(sess.userNumber || "0")}</strong></span>
        <span>Window: <strong>${fmtUtc(sess.windowStart, true)} &rarr; ${fmtUtc(sess.windowEnd)} (${fmtDuration(spanMs)})</strong></span>
      `;

      // Show Result Browser dump link if resolved by backend
      const dumpUrl = sess.transaction && sess.transaction.dumpUrl;
      if (dumpUrl) {
        el.openResultDumpLink.href = dumpUrl;
        el.openResultDumpLink.classList.remove("hidden");
      }

      const rootNodes = [];
      if (sess.transaction) {
        rootNodes.push({ ...sess.transaction, children: sess.items || [] });
      } else if (sess.items) {
        rootNodes.push(...sess.items);
      }

      if (rootNodes.length === 0) {
        el.sessionModalBody.innerHTML = `<div class="empty-cell">No session records found in this window.</div>`;
        return;
      }

      const rowsHtml = [];
      function walkNodes(nodes, depth) {
        for (const n of nodes) {
          const relStart = Math.max(0, n.time - sess.windowStart);
          const leftPct = Math.min(98, (relStart / spanMs) * 100).toFixed(1);
          const widthPct = Math.max(0.8, Math.min(100 - leftPct, (Math.max(1, n.runTime) / spanMs) * 100)).toFixed(1);

          let barClass = "wf-bar-req";
          if (n.typeCode === "T") barClass = n.failed ? "wf-bar-err" : "wf-bar-tx";
          else if (n.typeCode === "A") barClass = n.failed ? "wf-bar-err" : "wf-bar-action";
          else if (n.typeCode === "E") barClass = "wf-bar-event";
          else if (n.failed || n.statusCode >= 400) barClass = "wf-bar-err";

          const badge = n.typeCode === "R"
            ? `<span class="badge ${n.failed || n.statusCode >= 400 ? "badge-err" : "badge-ok"}">${escapeHtml(n.httpMethod || "GET")} ${n.statusCode}</span>`
            : `<span class="badge ${n.failed ? "badge-err" : "badge-info"}">${escapeHtml(n.typeCode)}</span>`;

          const detail = n.typeCode === "R"
            ? `<span class="url-preview" title="${escapeHtml(n.url)}">${escapeHtml(n.url || "")}</span>`
            : `<span class="url-preview" title="${escapeHtml(n.message)}">${escapeHtml(n.message || "")}</span>`;

          rowsHtml.push(`
            <tr>
              <td class="wf-indent-${Math.min(2, depth)}">${escapeHtml(n.name)}</td>
              <td>${badge}</td>
              <td class="num">+${fmtInt(relStart)} ms</td>
              <td class="num"><strong>${fmtInt(n.runTime)} ms</strong></td>
              <td class="wf-timeline-cell">
                <div class="wf-track">
                  <div class="wf-bar ${barClass}" style="left:${leftPct}%;width:${widthPct}%"></div>
                </div>
              </td>
              <td>${detail}</td>
            </tr>
          `);

          if (n.children && n.children.length > 0) {
            walkNodes(n.children, depth + 1);
          }
        }
      }

      walkNodes(rootNodes, 0);

      el.sessionModalBody.innerHTML = `
        <table class="waterfall-table">
          <thead>
            <tr>
              <th>Step / Timer Hierarchy</th>
              <th>Type / Status</th>
              <th class="num">Offset</th>
              <th class="num">Duration</th>
              <th>Iteration Waterfall</th>
              <th>URL / Event Message / Stack Trace</th>
            </tr>
          </thead>
          <tbody>${rowsHtml.join("")}</tbody>
        </table>
      `;
    } catch (err) {
      el.sessionModalBody.innerHTML = `<div class="empty-cell">Failed to reconstruct session: ${escapeHtml(err.message)}</div>`;
    }
  }

  function switchTab(tabId) {
    state.activeTab = tabId;
    el.tabBtnSummary.classList.toggle("active", tabId === "summaryTab");
    el.tabBtnForensic.classList.toggle("active", tabId === "forensicTab");
    el.summaryTab.classList.toggle("active", tabId === "summaryTab");
    el.forensicTab.classList.toggle("active", tabId === "forensicTab");
    el.summaryToolbar.classList.toggle("hidden", tabId !== "summaryTab");
    el.forensicToolbar.classList.toggle("hidden", tabId !== "forensicTab");
    if (tabId === "forensicTab" && state.forensicRecords.length === 0) {
      el.forensicTableBody.innerHTML = `<tr><td colspan="9" class="empty-cell">Click <strong>▶ Update</strong> to query forensic outliers for the current filter selection.</td></tr>`;
      markPendingChanges("Forensic tab active — click ▶ Update to query");
    } else if (tabId === "summaryTab" && state.summaryRows.length === 0) {
      markPendingChanges("Summary tab active — click ▶ Update to query");
    }
  }

  // Wire Event Listeners
  function initEventListeners() {
    // Light (default) / Dark theme toggle
    if (el.themeToggleBtn) {
      el.themeToggleBtn.addEventListener("click", () => {
        state.theme = state.theme === "dark" ? "light" : "dark";
        if (state.theme === "dark") {
          document.documentElement.setAttribute("data-theme", "dark");
          el.themeToggleBtn.textContent = "☀️ Light";
        } else {
          document.documentElement.removeAttribute("data-theme");
          el.themeToggleBtn.textContent = "🌙 Dark";
        }
        renderTimeSeriesChart(state.lastTimeSeries);
      });
    }

    // Explicit Update Button: only execute backend query when clicked (or Enter pressed)
    el.runQueryBtn.addEventListener("click", () => {
      applyTimeInputsToState();
      clearPendingChanges();
      refreshDashboard("both");
    });

    el.recordTypeSelect.addEventListener("change", () => {
      const isReq = el.recordTypeSelect.value === "R";
      el.statusFilterGroup.classList.toggle("hidden", !isReq);
      populateTimerNamesForType();
      markPendingChanges();
    });

    el.nameFilterInput.addEventListener("input", () => {
      el.clearNameFilterBtn.classList.toggle("hidden", !el.nameFilterInput.value);
      markPendingChanges();
    });

    [el.nameFilterInput, el.fromTimeInput, el.toTimeInput].forEach(inp => {
      if (!inp) return;
      inp.addEventListener("keydown", e => {
        if (e.key === "Enter") {
          e.preventDefault();
          el.runQueryBtn.click();
        }
      });
    });

    [el.fromTimeInput, el.toTimeInput].forEach(inp => {
      if (!inp) return;
      inp.addEventListener("input", () => markPendingChanges("Time selection modified — click ▶ Update to run"));
      inp.addEventListener("change", () => {
        applyTimeInputsToState();
        markPendingChanges("Time selection modified — click ▶ Update to run");
      });
    });

    el.clearNameFilterBtn.addEventListener("click", () => {
      el.nameFilterInput.value = "";
      el.clearNameFilterBtn.classList.add("hidden");
      markPendingChanges();
    });

    // Global filter controls mark pending so user can combine filters before clicking ▶ Update
    [
      el.scenarioSelect, el.agentSelect, el.statusPresetSelect, el.failedOnlyCheckbox,
      el.chartGroupBySelect, el.bucketSizeSelect,
      el.summaryGroupBySelect, el.inspectModeSelect, el.inspectLimitSelect
    ].forEach(ctrl => {
      if (ctrl) ctrl.addEventListener("change", () => markPendingChanges());
    });

    // Chart metric mode is a pure client-side redraw when time-series data is already loaded
    el.chartMetricSelect.addEventListener("change", () => {
      if (state.lastTimeSeries) {
        renderTimeSeriesChart(state.lastTimeSeries);
      } else {
        markPendingChanges();
      }
    });

    el.summarySearchInput.addEventListener("input", () => renderSummaryTable());
    el.copyMarkdownBtn.addEventListener("click", copyTableAsMarkdown);

    el.resetZoomBtn.addEventListener("click", () => {
      if (!state.meta) return;
      state.fromTime = state.meta.minTime;
      state.toTime = state.meta.maxTime;
      state.zoomed = false;
      chartInteraction.pendingBrushX1 = null;
      chartInteraction.pendingBrushX2 = null;
      syncTimeInputsFromState();
      if (state.lastTimeSeries) renderTimeSeriesChart(state.lastTimeSeries);
      markPendingChanges("Full time window restored — click ▶ Update to run");
    });

    el.resetFiltersBtn.addEventListener("click", () => {
      if (!state.meta) return;
      el.recordTypeSelect.value = "R";
      el.statusFilterGroup.classList.remove("hidden");
      el.nameFilterInput.value = "";
      el.clearNameFilterBtn.classList.add("hidden");
      el.scenarioSelect.value = "";
      el.agentSelect.value = "";
      el.statusPresetSelect.value = "ALL";
      el.failedOnlyCheckbox.checked = false;
      el.chartGroupBySelect.value = "NONE";
      el.bucketSizeSelect.value = "0";
      el.summarySearchInput.value = "";
      state.fromTime = state.meta.minTime;
      state.toTime = state.meta.maxTime;
      state.zoomed = false;
      chartInteraction.pendingBrushX1 = null;
      chartInteraction.pendingBrushX2 = null;
      syncTimeInputsFromState();
      populateTimerNamesForType();
      if (state.lastTimeSeries) renderTimeSeriesChart(state.lastTimeSeries);
      markPendingChanges("All filters reset — click ▶ Update to run");
    });

    el.tabBtnSummary.addEventListener("click", () => switchTab("summaryTab"));
    el.tabBtnForensic.addEventListener("click", () => switchTab("forensicTab"));

    // Table column sorting
    el.summaryTable.querySelectorAll("th.sortable").forEach(th => {
      th.addEventListener("click", () => {
        const col = th.dataset.sort;
        if (state.summarySortCol === col) {
          state.summarySortAsc = !state.summarySortAsc;
        } else {
          state.summarySortCol = col;
          state.summarySortAsc = col === "key";
        }
        el.summaryTable.querySelectorAll("th.sortable").forEach(h => {
          h.classList.remove("sort-asc", "sort-desc");
        });
        th.classList.add(state.summarySortAsc ? "sort-asc" : "sort-desc");
        renderSummaryTable();
      });
    });

    // Modal navigation using prevTransactionTime / nextTransactionTime
    el.prevIterationBtn.addEventListener("click", () => {
      if (!state.sessionContext || !state.lastSessionResponse || !state.lastSessionResponse.prevTransactionTime) return;
      state.sessionContext.targetTime = state.lastSessionResponse.prevTransactionTime;
      loadSessionIteration();
    });
    el.nextIterationBtn.addEventListener("click", () => {
      if (!state.sessionContext || !state.lastSessionResponse || !state.lastSessionResponse.nextTransactionTime) return;
      state.sessionContext.targetTime = state.lastSessionResponse.nextTransactionTime;
      loadSessionIteration();
    });
    el.closeSessionModalBtn.addEventListener("click", () => {
      el.sessionModalBackdrop.classList.add("hidden");
    });
    el.sessionModalBackdrop.addEventListener("click", e => {
      if (e.target === el.sessionModalBackdrop) {
        el.sessionModalBackdrop.classList.add("hidden");
      }
    });
  }

  // Bootstrap
  initEventListeners();
  setupChartInteractions();
  loadMetadata();
})();
