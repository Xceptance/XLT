/**
 * XLT Guided Result Explorer - Diagnostic Storyboard & Synchronized Drill-Down UI
 */
(function () {
  "use strict";

  const SERIES_COLORS = [
    "#0284c7", "#d97706", "#059669", "#7c3aed",
    "#e11d48", "#0891b2", "#ca8a04", "#db2777",
    "#65a30d", "#4f46e5", "#0d9488", "#ea580c"
  ];

  const MAX_DATALIST_OPTIONS = 200;
  const REQUEST_ONLY_GROUP_BYS = ["USED_IP", "STATUS_CODE", "CONTENT_TYPE", "HTTP_METHOD"];

  // Application State
  const state = {
    meta: null,
    fromTime: 0,
    toTime: 0,
    zoomed: false,
    hasExecutedQuery: false,
    hasPendingChanges: false,
    activeTab: "summaryTab",
    summaryGroupBy: "TIMER",
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
    lastSessionResponse: null,
    usedIpFilter: null,
    httpMethodFilter: null,
    contentTypeFilter: null,
    exactStatusCodeFilter: null,
    lastDatalistKey: "",
    // Baseline comparison snapshot
    baseline: null
  };

  // DOM References
  const el = {
    datasetMeta: document.getElementById("datasetMeta"),
    themeToggleBtn: document.getElementById("themeToggleBtn"),
    statusDot: document.getElementById("statusDot"),
    statusText: document.getElementById("statusText"),
    funnelTrail: document.getElementById("funnelTrail"),
    baselineBadge: document.getElementById("baselineBadge"),
    pinBaselineBtn: document.getElementById("pinBaselineBtn"),
    clearBaselineBtn: document.getElementById("clearBaselineBtn"),
    recordTypeSelect: document.getElementById("recordTypeSelect"),
    layerQuickPills: document.getElementById("layerQuickPills"),
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
    activeScopeSubtitle: document.getElementById("activeScopeSubtitle"),
    // KPI Scorecard
    kpiVolumeVal: document.getElementById("kpiVolumeVal"),
    kpiVolumeSub: document.getElementById("kpiVolumeSub"),
    kpiReliabilityCard: document.getElementById("kpiReliabilityCard"),
    kpiErrorVal: document.getElementById("kpiErrorVal"),
    kpiErrorSub: document.getElementById("kpiErrorSub"),
    kpiLatencyVal: document.getElementById("kpiLatencyVal"),
    kpiLatencySub: document.getElementById("kpiLatencySub"),
    kpiTailVal: document.getElementById("kpiTailVal"),
    kpiTailSub: document.getElementById("kpiTailSub"),
    kpiPhaseVal: document.getElementById("kpiPhaseVal"),
    kpiPhaseBar: document.getElementById("kpiPhaseBar"),
    kpiPhaseSub: document.getElementById("kpiPhaseSub"),
    kpiBandwidthVal: document.getElementById("kpiBandwidthVal"),
    kpiBandwidthSub: document.getElementById("kpiBandwidthSub"),
    findingsGrid: document.getElementById("findingsGrid"),
    // Golden Signals Grid
    timeWindowBadge: document.getElementById("timeWindowBadge"),
    bucketBadge: document.getElementById("bucketBadge"),
    chartGroupBySelect: document.getElementById("chartGroupBySelect"),
    bucketSizeSelect: document.getElementById("bucketSizeSelect"),
    brushActionBanner: document.getElementById("brushActionBanner"),
    brushBannerText: document.getElementById("brushBannerText"),
    applyBrushZoomBtn: document.getElementById("applyBrushZoomBtn"),
    compareBrushWithBaselineBtn: document.getElementById("compareBrushWithBaselineBtn"),
    cancelBrushBtn: document.getElementById("cancelBrushBtn"),
    canvasThroughput: document.getElementById("canvasThroughput"),
    canvasErrors: document.getElementById("canvasErrors"),
    canvasLatency: document.getElementById("canvasLatency"),
    canvasPhases: document.getElementById("canvasPhases"),
    panel4Title: document.getElementById("panel4Title"),
    readoutThroughput: document.getElementById("readoutThroughput"),
    readoutErrors: document.getElementById("readoutErrors"),
    readoutLatency: document.getElementById("readoutLatency"),
    readoutPhases: document.getElementById("readoutPhases"),
    // Stage 3 Tabs
    tabBtnSummary: document.getElementById("tabBtnSummary"),
    tabBtnForensic: document.getElementById("tabBtnForensic"),
    summaryTab: document.getElementById("summaryTab"),
    forensicTab: document.getElementById("forensicTab"),
    dimPillGroup: document.getElementById("dimPillGroup"),
    summarySearchInput: document.getElementById("summarySearchInput"),
    copyMarkdownBtn: document.getElementById("copyMarkdownBtn"),
    exportSummaryCsvBtn: document.getElementById("exportSummaryCsvBtn"),
    summaryTable: document.getElementById("summaryTable"),
    summaryTableBody: document.getElementById("summaryTableBody"),
    inspectModeSelect: document.getElementById("inspectModeSelect"),
    inspectLimitSelect: document.getElementById("inspectLimitSelect"),
    exportForensicCsvBtn: document.getElementById("exportForensicCsvBtn"),
    scatterWrap: document.getElementById("scatterWrap"),
    scatterCanvas: document.getElementById("scatterCanvas"),
    scatterReadout: document.getElementById("scatterReadout"),
    forensicTableBody: document.getElementById("forensicTableBody"),
    // Native <dialog> Modal
    sessionModalDialog: document.getElementById("sessionModalDialog"),
    sessionMetaBar: document.getElementById("sessionMetaBar"),
    sessionModalBody: document.getElementById("sessionModalBody"),
    prevIterationBtn: document.getElementById("prevIterationBtn"),
    nextIterationBtn: document.getElementById("nextIterationBtn"),
    openResultDumpLink: document.getElementById("openResultDumpLink"),
    closeSessionModalBtn: document.getElementById("closeSessionModalBtn")
  };

  // Formatting Utilities
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

  function fmtBytes(bytes) {
    if (!bytes || bytes <= 0) return "0 B";
    const units = ["B", "KB", "MB", "GB", "TB"];
    let val = bytes;
    let u = 0;
    while (val >= 1024 && u < units.length - 1) {
      val /= 1024;
      u++;
    }
    return `${val >= 100 ? Math.round(val) : val.toFixed(1)} ${units[u]}`;
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

  function parseInputUtc(rawStr, fallbackMs) {
    if (!rawStr || !rawStr.trim()) return fallbackMs;
    const s = rawStr.trim();
    if (/^\d{10,15}$/.test(s)) {
      return parseInt(s, 10);
    }
    if (/^\d{2}:\d{2}(:\d{2}(\.\d{1,3})?)?$/.test(s)) {
      const baseDate = new Date(fallbackMs || (state.meta ? state.meta.minTime : Date.now()))
        .toISOString()
        .substring(0, 10);
      const fullStr = `${baseDate}T${s.length === 5 ? s + ":00" : s}Z`;
      const parsed = Date.parse(fullStr);
      return Number.isNaN(parsed) ? fallbackMs : parsed;
    }
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

  function isDarkSchemeActive() {
    const meta = document.querySelector('meta[name="color-scheme"]');
    const content = meta ? meta.content : "light dark";
    if (content === "dark") return true;
    if (content === "light") return false;
    return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
  }

  function getLayerLabel(code) {
    switch (code) {
      case "R": return "Requests (HTTP)";
      case "T": return "Transactions";
      case "A": return "Actions";
      case "P": return "Page Loads";
      case "C": return "Custom Timers";
      default: return code;
    }
  }

  // Sync Time Inputs & Funnel Breadcrumb
  function syncTimeInputsFromState() {
    if (el.fromTimeInput) el.fromTimeInput.value = fmtInputUtc(state.fromTime);
    if (el.toTimeInput) el.toTimeInput.value = fmtInputUtc(state.toTime);
    const spanMs = Math.max(1, state.toTime - state.fromTime);
    el.timeWindowBadge.textContent = state.zoomed
      ? `Zoomed: ${fmtUtc(state.fromTime)} → ${fmtUtc(state.toTime)} (${fmtDuration(spanMs)})`
      : `Full Window: ${fmtUtc(state.fromTime)} → ${fmtUtc(state.toTime)} (${fmtDuration(spanMs)})`;
    el.resetZoomBtn.classList.toggle("hidden", !state.zoomed);
    renderFunnelTrail();
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

  function markPendingChanges(reason = "Filters modified — click ▶ Analyze to update") {
    state.hasPendingChanges = true;
    if (el.runQueryBtn) {
      el.runQueryBtn.classList.add("has-pending");
      el.runQueryBtn.textContent = "▶ Analyze ●";
    }
    if (!state.queryInFlight) {
      el.statusDot.className = "status-dot pending";
      el.statusText.textContent = reason;
    }
    renderFunnelTrail();
  }

  function clearPendingChanges() {
    state.hasPendingChanges = false;
    if (el.runQueryBtn) {
      el.runQueryBtn.classList.remove("has-pending");
      el.runQueryBtn.textContent = "▶ Analyze";
    }
  }

  // Status Indicator
  function startQueryFeedback(label) {
    if (state.timerInterval) clearInterval(state.timerInterval);
    const t0 = performance.now();
    state.feedbackLabel = label;
    el.statusDot.className = "status-dot loading";
    el.statusText.textContent = `${label}...`;
    state.timerInterval = setInterval(() => {
      const elapsedSec = ((performance.now() - t0) / 1000).toFixed(1);
      el.statusText.textContent = `${state.feedbackLabel} (${elapsedSec}s)...`;
    }, 100);
    return t0;
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

  // Interactive Drill-Down Funnel Trail
  function renderFunnelTrail() {
    if (!el.funnelTrail) return;
    const steps = [];

    steps.push({
      label: "🌐 All Traffic",
      action: "resetAll",
      removable: false
    });

    const type = el.recordTypeSelect.value;
    steps.push({
      label: `📑 Layer: ${getLayerLabel(type)}`,
      action: null,
      removable: false
    });

    if (el.scenarioSelect.value) {
      steps.push({
        label: `🎬 Scenario: ${el.scenarioSelect.value}`,
        action: "clearScenario",
        removable: true
      });
    }

    if (el.agentSelect.value) {
      steps.push({
        label: `🖥️ Agent: ${el.agentSelect.value}`,
        action: "clearAgent",
        removable: true
      });
    }

    if (state.zoomed) {
      steps.push({
        label: `⏱️ Window: ${fmtUtc(state.fromTime)} → ${fmtUtc(state.toTime)}`,
        action: "clearZoom",
        removable: true
      });
    }

    if (el.nameFilterInput.value.trim()) {
      steps.push({
        label: `🎯 Timer: ${el.nameFilterInput.value.trim()}`,
        action: "clearTimer",
        removable: true
      });
    }

    if (type === "R" && state.exactStatusCodeFilter !== null) {
      steps.push({
        label: `🔢 HTTP ${state.exactStatusCodeFilter}`,
        action: "clearExactStatus",
        removable: true
      });
    } else if (type === "R" && el.statusPresetSelect.value !== "ALL") {
      steps.push({
        label: `🔢 Status: ${el.statusPresetSelect.value}`,
        action: "clearStatusPreset",
        removable: true
      });
    }

    if (el.failedOnlyCheckbox.checked) {
      steps.push({
        label: `❌ Failed Only`,
        action: "clearFailed",
        removable: true
      });
    }

    if (type === "R" && state.usedIpFilter) {
      steps.push({
        label: `🖧 IP: ${state.usedIpFilter}`,
        action: "clearUsedIp",
        removable: true
      });
    }

    if (type === "R" && state.httpMethodFilter) {
      steps.push({
        label: `📡 Method: ${state.httpMethodFilter}`,
        action: "clearMethod",
        removable: true
      });
    }

    if (type === "R" && state.contentTypeFilter) {
      steps.push({
        label: `📦 Type: ${state.contentTypeFilter}`,
        action: "clearContentType",
        removable: true
      });
    }

    el.funnelTrail.innerHTML = steps.map((s, idx) => {
      const isLast = idx === steps.length - 1;
      const arrow = idx > 0 ? `<span class="funnel-arrow">&rarr;</span>` : "";
      const closeBtn = s.removable ? `<button type="button" title="Remove filter">&times;</button>` : "";
      return `${arrow}<span class="funnel-step ${isLast ? "active" : ""}" data-funnel-action="${escapeHtml(s.action || "")}">${escapeHtml(s.label)}${closeBtn}</span>`;
    }).join("");

    el.funnelTrail.querySelectorAll(".funnel-step[data-funnel-action]").forEach(node => {
      const act = node.getAttribute("data-funnel-action");
      if (!act) return;
      node.addEventListener("click", () => {
        handleFunnelStepAction(act);
      });
    });

    // Update Scope Subtitle
    const scopeParts = [getLayerLabel(type)];
    if (el.scenarioSelect.value) scopeParts.push(`Scenario ${el.scenarioSelect.value}`);
    if (el.nameFilterInput.value.trim()) scopeParts.push(`Timer "${el.nameFilterInput.value.trim()}"`);
    if (state.zoomed) scopeParts.push(`Zoomed (${fmtDuration(state.toTime - state.fromTime)})`);
    else scopeParts.push("Full Test Window");
    if (el.activeScopeSubtitle) {
      el.activeScopeSubtitle.textContent = scopeParts.join(" · ");
    }
  }

  function handleFunnelStepAction(action) {
    if (!state.meta) return;
    switch (action) {
      case "resetAll":
        el.resetFiltersBtn.click();
        refreshDashboard("both");
        return;
      case "clearScenario":
        el.scenarioSelect.value = "";
        break;
      case "clearAgent":
        el.agentSelect.value = "";
        break;
      case "clearZoom":
        state.fromTime = state.meta.minTime;
        state.toTime = state.meta.maxTime;
        state.zoomed = false;
        syncTimeInputsFromState();
        break;
      case "clearTimer":
        el.nameFilterInput.value = "";
        el.clearNameFilterBtn.classList.add("hidden");
        populateTimerNamesForType("");
        break;
      case "clearExactStatus":
        state.exactStatusCodeFilter = null;
        break;
      case "clearStatusPreset":
        el.statusPresetSelect.value = "ALL";
        break;
      case "clearFailed":
        el.failedOnlyCheckbox.checked = false;
        break;
      case "clearUsedIp":
        state.usedIpFilter = null;
        break;
      case "clearMethod":
        state.httpMethodFilter = null;
        break;
      case "clearContentType":
        state.contentTypeFilter = null;
        break;
      default:
        return;
    }
    renderFunnelTrail();
    refreshDashboard("both");
  }

  function getStatusCodeBounds() {
    if (el.recordTypeSelect.value !== "R") {
      return { minStatusCode: null, maxStatusCode: null };
    }
    if (state.exactStatusCodeFilter !== null && state.exactStatusCodeFilter !== undefined) {
      return {
        minStatusCode: state.exactStatusCodeFilter,
        maxStatusCode: state.exactStatusCodeFilter
      };
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

  function populateTimerNamesForType(filterText = "") {
    if (!state.meta || !state.meta.timers) return;
    const type = el.recordTypeSelect.value;
    const names = state.meta.timers[type] || [];
    const q = (filterText || "").trim().toLowerCase();

    let selected;
    if (!q || names.length <= MAX_DATALIST_OPTIONS) {
      selected = names.slice(0, MAX_DATALIST_OPTIONS);
    } else {
      const prefixMatches = [];
      const substringMatches = [];
      for (let i = 0; i < names.length; i++) {
        const name = names[i];
        const lower = name.toLowerCase();
        if (lower.startsWith(q)) {
          prefixMatches.push(name);
          if (prefixMatches.length >= MAX_DATALIST_OPTIONS) break;
        } else if (
          prefixMatches.length + substringMatches.length < MAX_DATALIST_OPTIONS &&
          lower.includes(q)
        ) {
          substringMatches.push(name);
        }
      }
      selected = prefixMatches.concat(substringMatches).slice(0, MAX_DATALIST_OPTIONS);
    }

    const cacheKey = `${type}:${selected.length}:${selected[0] || ""}:${selected[selected.length - 1] || ""}`;
    if (state.lastDatalistKey === cacheKey) return;
    state.lastDatalistKey = cacheKey;

    el.timerNameList.innerHTML = selected
      .map(n => `<option value="${escapeHtml(n)}"></option>`)
      .join("");
  }

  function syncRecordTypeDependentControls(syncInspectMode = true) {
    const type = el.recordTypeSelect.value;
    const isReq = type === "R";
    el.statusFilterGroup.classList.toggle("hidden", !isReq);

    // Sync Layer Quick Pills
    if (el.layerQuickPills) {
      el.layerQuickPills.querySelectorAll(".layer-pill").forEach(btn => {
        btn.classList.toggle("active", btn.dataset.layer === type);
      });
    }

    // Sync Panel 4 GroupBy options
    if (el.chartGroupBySelect) {
      Array.from(el.chartGroupBySelect.options).forEach(opt => {
        if (REQUEST_ONLY_GROUP_BYS.includes(opt.value)) {
          opt.disabled = !isReq;
        }
      });
      if (!isReq && REQUEST_ONLY_GROUP_BYS.includes(el.chartGroupBySelect.value)) {
        el.chartGroupBySelect.value = "NONE";
      }
    }

    // Sync Dimension Breakdown Pills
    if (el.dimPillGroup) {
      el.dimPillGroup.querySelectorAll(".dim-pill").forEach(btn => {
        if (btn.dataset.reqOnly === "true") {
          btn.disabled = !isReq;
        }
      });
      if (!isReq && REQUEST_ONLY_GROUP_BYS.includes(state.summaryGroupBy)) {
        state.summaryGroupBy = "TIMER";
      }
      el.dimPillGroup.querySelectorAll(".dim-pill").forEach(btn => {
        btn.classList.toggle("active", btn.dataset.dim === state.summaryGroupBy);
      });
    }

    if (syncInspectMode && el.inspectModeSelect) {
      const curMode = el.inspectModeSelect.value;
      const preferFailed = el.failedOnlyCheckbox && el.failedOnlyCheckbox.checked;
      if (type === "R" && !curMode.endsWith("_REQUESTS")) {
        el.inspectModeSelect.value = preferFailed ? "FAILED_REQUESTS" : "SLOWEST_REQUESTS";
      } else if (type === "T" && !curMode.endsWith("_TRANSACTIONS")) {
        el.inspectModeSelect.value = preferFailed ? "FAILED_TRANSACTIONS" : "SLOWEST_TRANSACTIONS";
      } else if (type === "A" && !curMode.endsWith("_ACTIONS")) {
        el.inspectModeSelect.value = preferFailed ? "FAILED_ACTIONS" : "SLOWEST_ACTIONS";
      }
    }

    renderFunnelTrail();
  }

  // Load Metadata & Run Initial Guided Overview
  async function loadMetadata() {
    const t0 = startQueryFeedback("Loading dataset metadata");
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
      const modeLabel = meta.forensicMode === "full" ? "FULL" : "SPARSE";
      const memLabel = meta.memoryMode || "DISK";
      el.datasetMeta.innerHTML = `
        <span class="meta-pill">Records: <strong>${fmtInt(meta.totalRecords)}</strong></span>
        <span class="meta-pill">Chunks: <strong>${fmtInt(meta.totalChunks)}</strong></span>
        <span class="meta-pill">Span: <strong>${fmtUtc(meta.minTime, true)} &rarr; ${fmtUtc(meta.maxTime, false)} (${durationStr})</strong></span>
        <span class="meta-pill">Forensics: <strong>${escapeHtml(modeLabel)}</strong></span>
        <span class="meta-pill">Mode: <strong>${escapeHtml(memLabel)}</strong></span>
      `;

      el.scenarioSelect.innerHTML = `<option value="">All Scenarios (${(meta.scenarios || []).length})</option>` +
        (meta.scenarios || []).map(s => `<option value="${escapeHtml(s)}">${escapeHtml(s)}</option>`).join("");

      el.agentSelect.innerHTML = `<option value="">All Agents (${(meta.agents || []).length})</option>` +
        (meta.agents || []).map(a => `<option value="${escapeHtml(a)}">${escapeHtml(a)}</option>`).join("");

      syncRecordTypeDependentControls(true);
      populateTimerNamesForType(el.nameFilterInput.value);
      stopQueryFeedback(t0, `${fmtInt(meta.totalRecords)} records`);

      // Automatically execute initial guided scan so the dashboard greets the user with insights
      await refreshDashboard("both");
    } catch (err) {
      stopQueryFeedback(t0, err.message, true);
    }
  }

  // Single-Flight Sequential Dashboard Refresh
  async function refreshDashboard(requestedScope = "both") {
    const scope = typeof requestedScope === "string" ? requestedScope : "both";
    if (!state.meta) return;

    if (state.queryInFlight) {
      state.pendingScope = state.pendingScope === scope ? scope : "both";
      return;
    }

    state.queryInFlight = true;
    let nextScope = scope;

    try {
      while (nextScope) {
        state.pendingScope = null;
        await executeGuidedPass(nextScope);
        nextScope = state.pendingScope;
      }
    } finally {
      state.queryInFlight = false;
    }
  }

  async function executeGuidedPass(scope) {
    state.hasExecutedQuery = true;
    const doChart = scope === "both" || scope === "chart";
    const doBottom = scope === "both" || scope === "bottom";

    const t0 = startQueryFeedback("Scanning telemetry & computing guided insights");

    const isReq = el.recordTypeSelect.value === "R";
    const { minStatusCode, maxStatusCode } = getStatusCodeBounds();
    const timerQuery = el.nameFilterInput.value.trim() || null;
    const agents = el.agentSelect.value ? [el.agentSelect.value] : null;
    const scenarios = el.scenarioSelect.value ? [el.scenarioSelect.value] : null;
    const failedOnly = el.failedOnlyCheckbox.checked || null;
    const usedIps = isReq && state.usedIpFilter ? [state.usedIpFilter] : null;
    const httpMethods = isReq && state.httpMethodFilter ? [state.httpMethodFilter] : null;
    const contentTypes = isReq && state.contentTypeFilter ? [state.contentTypeFilter] : null;

    const commonFilter = {
      recordType: el.recordTypeSelect.value,
      fromTime: state.fromTime,
      toTime: state.toTime,
      timerQuery,
      agents,
      scenarios,
      usedIps,
      httpMethods,
      contentTypes,
      failedOnly,
      minStatusCode,
      maxStatusCode
    };

    clearPendingChanges();
    hideBrushBanner();
    syncTimeInputsFromState();

    try {
      // Always use single-pass /api/v1/dashboard when updating both chart and summary
      if (doChart || (doBottom && state.activeTab === "summaryTab")) {
        const dashResp = await fetch("/api/v1/dashboard", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            ...commonFilter,
            bucketWidthMs: parseInt(el.bucketSizeSelect.value, 10) || 0,
            chartGroupBy: el.chartGroupBySelect.value,
            summaryGroupBy: state.summaryGroupBy
          })
        });
        if (!dashResp.ok) throw new Error(`HTTP ${dashResp.status}`);
        const dashData = await dashResp.json();
        const tsData = dashData.timeSeries;
        const bottomData = dashData.summaryTable;

        state.lastTimeSeries = tsData;
        state.summaryRows = (bottomData && bottomData.rows) || [];

        if (tsData) {
          el.bucketBadge.textContent = `Bucket: ${fmtDuration(tsData.bucketWidthMs)}`;
        }
        renderKpiScorecard(tsData, state.summaryRows);
        renderGuidedFindings(tsData, state.summaryRows);
        scheduleAllSignalsRender();
        renderSummaryTable();
      }

      // If Forensic Tab is active, also fetch the forensic records + scatter plot
      if (state.activeTab === "forensicTab") {
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
            usedIps,
            httpMethods,
            contentTypes,
            minStatusCode,
            maxStatusCode,
            failedOnly
          })
        });
        if (!inspResp.ok) throw new Error(`HTTP ${inspResp.status}`);
        const inspData = await inspResp.json();
        state.forensicRecords = inspData.records || [];
        state.expandedForensicIdx = null;
        renderForensicTable();
        renderScatterPlot();
      }

      const ts = state.lastTimeSeries;
      const info = ts
        ? `${fmtInt(ts.rowsMatched)} rows · ${ts.chunksScanned} chunks · ${state.summaryRows.length} groups`
        : `${state.summaryRows.length} groups`;
      stopQueryFeedback(t0, info);
    } catch (err) {
      stopQueryFeedback(t0, err.message, true);
    }
  }

  // Stage 1A: Render 6-Card Executive KPI Scorecard
  function renderKpiScorecard(tsData, rows) {
    if (!tsData || !tsData.timestamps || tsData.rowsMatched === 0) {
      el.kpiVolumeVal.textContent = "0";
      el.kpiVolumeSub.textContent = "No matching records in window";
      el.kpiErrorVal.textContent = "0 (0.0%)";
      el.kpiErrorSub.textContent = "No errors";
      el.kpiReliabilityCard.className = "kpi-card";
      el.kpiLatencyVal.textContent = "—";
      el.kpiLatencySub.textContent = "No observations";
      el.kpiTailVal.textContent = "—";
      el.kpiTailSub.textContent = "No observations";
      el.kpiPhaseVal.textContent = "—";
      el.kpiPhaseBar.innerHTML = "";
      el.kpiBandwidthVal.textContent = "0 B";
      el.kpiBandwidthSub.textContent = "Sent: 0 B · Recv: 0 B";
      return;
    }

    const totalCount = tsData.rowsMatched || 0;
    const spanSec = Math.max(0.001, (tsData.toTime - tsData.fromTime) / 1000.0);
    const avgRps = totalCount / spanSec;
    let peakRps = 0;
    let peakRpsTime = 0;
    let totalErrors = 0;
    let totalSent = 0;
    let totalRecv = 0;
    let maxObs = 0;

    const n = tsData.timestamps.length;
    for (let i = 0; i < n; i++) {
      const r = (tsData.rps && tsData.rps[i]) || 0;
      if (r > peakRps) {
        peakRps = r;
        peakRpsTime = tsData.timestamps[i];
      }
      totalErrors += (tsData.errors && tsData.errors[i]) || 0;
      totalSent += (tsData.bytesSent && tsData.bytesSent[i]) || 0;
      totalRecv += (tsData.bytesReceived && tsData.bytesReceived[i]) || 0;
      const m = (tsData.max && tsData.max[i]) || 0;
      if (m > maxObs) maxObs = m;
    }

    // Compute volume-weighted percentiles and network phases across summary rows (or buckets fallback)
    let wMean = 0, wP50 = 0, wP95 = 0, wP99 = 0;
    let wConn = 0, wSend = 0, wBusy = 0, wRecv = 0;
    let rowSumCount = 0;
    for (const r of rows) {
      const c = r.count || 0;
      rowSumCount += c;
      wMean += (r.mean || 0) * c;
      wP50 += (r.p50 || 0) * c;
      wP95 += (r.p95 || 0) * c;
      wP99 += (r.p99 || 0) * c;
      wConn += (r.avgConnect || 0) * c;
      wSend += (r.avgSend || 0) * c;
      wBusy += (r.avgServerBusy || 0) * c;
      wRecv += (r.avgReceive || 0) * c;
    }
    const denom = Math.max(1, rowSumCount);
    wMean /= denom;
    wP50 /= denom;
    wP95 /= denom;
    wP99 /= denom;
    wConn /= denom;
    wSend /= denom;
    wBusy /= denom;
    wRecv /= denom;

    const errPct = totalCount > 0 ? (totalErrors / totalCount) * 100.0 : 0;

    // 1. Volume & Throughput
    el.kpiVolumeVal.textContent = `${fmtDec(avgRps, 1)} /s`;
    el.kpiVolumeSub.textContent = `${fmtInt(totalCount)} records · Peak ${fmtDec(peakRps, 1)}/s @ ${fmtUtc(peakRpsTime)}`;

    // 2. Reliability & Errors
    el.kpiErrorVal.textContent = `${fmtInt(totalErrors)} (${fmtDec(errPct, 2)}%)`;
    if (totalErrors === 0) {
      el.kpiReliabilityCard.className = "kpi-card kpi-status-ok";
      el.kpiErrorSub.textContent = "✓ 100% clean in selected window";
    } else if (errPct < 1.0) {
      el.kpiReliabilityCard.className = "kpi-card kpi-status-warn";
      el.kpiErrorSub.textContent = "⚠️ Minor failure rate detected";
    } else {
      el.kpiReliabilityCard.className = "kpi-card kpi-status-danger";
      el.kpiErrorSub.textContent = "🔴 Elevated failure rate — inspect burst";
    }

    // 3. Latency Pulse (p50 -> p95)
    el.kpiLatencyVal.textContent = `${fmtInt(wP50)} → ${fmtInt(wP95)} ms`;
    el.kpiLatencySub.textContent = `Mean: ${fmtDec(wMean, 1)} ms across ${rows.length} groups`;

    // 4. Tail Amplification (p99 / Max)
    const tailRatio = wP50 > 0 ? (wP99 / wP50) : 1;
    el.kpiTailVal.textContent = `${fmtInt(wP99)} ms (p99)`;
    el.kpiTailSub.textContent = `Max: ${fmtInt(maxObs)} ms · Tail skew ${fmtDec(tailRatio, 1)}× median`;

    // 5. Dominant Time Phase
    const isReq = el.recordTypeSelect.value === "R";
    const phaseSum = wConn + wSend + wBusy + wRecv;
    if (isReq && phaseSum > 0) {
      const pConn = (wConn / phaseSum) * 100;
      const pSend = (wSend / phaseSum) * 100;
      const pBusy = (wBusy / phaseSum) * 100;
      const pRecv = (wRecv / phaseSum) * 100;
      let domName = "Server Busy";
      let domPct = pBusy;
      if (pConn >= domPct && pConn >= pRecv) { domName = "TCP/TLS Connect"; domPct = pConn; }
      else if (pRecv >= domPct && pRecv >= pConn) { domName = "Network Receive"; domPct = pRecv; }

      el.kpiPhaseVal.textContent = `${domName} (${fmtDec(domPct, 0)}%)`;
      el.kpiPhaseBar.innerHTML = `
        <div class="phase-seg phase-connect" style="width:${pConn.toFixed(1)}%" title="Connect: ${fmtDec(wConn, 1)} ms"></div>
        <div class="phase-seg phase-send" style="width:${pSend.toFixed(1)}%" title="Send: ${fmtDec(wSend, 1)} ms"></div>
        <div class="phase-seg phase-busy" style="width:${pBusy.toFixed(1)}%" title="Server Busy: ${fmtDec(wBusy, 1)} ms"></div>
        <div class="phase-seg phase-recv" style="width:${pRecv.toFixed(1)}%" title="Receive: ${fmtDec(wRecv, 1)} ms"></div>
      `;
      el.kpiPhaseSub.textContent = `Busy ${fmtDec(wBusy, 0)}ms · Conn ${fmtDec(wConn, 0)}ms · Recv ${fmtDec(wRecv, 0)}ms`;
    } else {
      el.kpiPhaseVal.textContent = getLayerLabel(el.recordTypeSelect.value);
      el.kpiPhaseBar.innerHTML = `<div class="phase-seg phase-connect" style="width:100%"></div>`;
      el.kpiPhaseSub.textContent = `Switch to Requests (R) for network phases`;
    }

    // 6. Network Bandwidth
    const totalBandwidth = totalSent + totalRecv;
    const avgBytes = totalCount > 0 ? totalRecv / totalCount : 0;
    el.kpiBandwidthVal.textContent = fmtBytes(totalBandwidth);
    el.kpiBandwidthSub.textContent = `In: ${fmtBytes(totalRecv)} · Out: ${fmtBytes(totalSent)} · Avg ${fmtBytes(avgBytes)}/req`;
  }

  // Stage 1B: Automated Guided Findings ("What to Investigate First")
  function renderGuidedFindings(tsData, rows) {
    if (!tsData || !tsData.timestamps || tsData.rowsMatched === 0) {
      el.findingsGrid.innerHTML = `
        <div class="finding-card finding-info">
          <div class="finding-badge">ℹ️ Empty Window</div>
          <h4>No Records Match Current Filters</h4>
          <p>Try restoring the full time window or clearing active dimension filters in the funnel bar above.</p>
          <div class="finding-actions">
            <button type="button" class="btn btn-primary btn-xs" data-guide-action="resetAll">Reset All Filters</button>
          </div>
        </div>
      `;
      bindFindingActions();
      return;
    }

    const cards = [];
    const n = tsData.timestamps.length;
    const bw = tsData.bucketWidthMs || 1000;
    const totalCount = tsData.rowsMatched || 1;
    let totalErrors = 0;
    for (let i = 0; i < n; i++) totalErrors += (tsData.errors && tsData.errors[i]) || 0;

    // 1. Error Burst & Top Failing Group Detection
    if (totalErrors > 0) {
      // Find peak 3-bucket sliding window of errors
      let bestErrSum = 0;
      let bestStartIdx = 0;
      let bestEndIdx = 0;
      const winLen = Math.min(n, 4);
      for (let i = 0; i < n; i++) {
        let s = 0;
        const end = Math.min(n - 1, i + winLen - 1);
        for (let j = i; j <= end; j++) s += (tsData.errors && tsData.errors[j]) || 0;
        if (s > bestErrSum) {
          bestErrSum = s;
          bestStartIdx = i;
          bestEndIdx = end;
        }
      }
      const burstStart = tsData.timestamps[bestStartIdx];
      const burstEnd = tsData.timestamps[bestEndIdx] + bw;
      const burstShare = ((bestErrSum / totalErrors) * 100).toFixed(0);

      const topErrRow = [...rows].sort((a, b) => (b.errors || 0) - (a.errors || 0))[0];
      const topErrDesc = topErrRow && topErrRow.errors > 0
        ? ` Top failing ${state.summaryGroupBy.toLowerCase()}: <strong>${escapeHtml(topErrRow.key)}</strong> (${fmtInt(topErrRow.errors)} errors, ${fmtDec(topErrRow.errorPct, 1)}%).`
        : "";

      cards.push(`
        <div class="finding-card finding-danger">
          <div>
            <span class="finding-badge">🔴 Reliability Hotspot</span>
            <h4>${burstShare}% of Errors Concentrated in ${fmtDuration(burstEnd - burstStart)} Window</h4>
            <p>${fmtInt(bestErrSum)} of ${fmtInt(totalErrors)} failures occurred between <strong>${fmtUtc(burstStart)}</strong> and <strong>${fmtUtc(burstEnd)}</strong>.${topErrDesc}</p>
          </div>
          <div class="finding-actions">
            <button type="button" class="btn btn-primary btn-xs" data-guide-action="zoomBurst" data-from="${burstStart}" data-to="${burstEnd}">
              🔍 Zoom to Error Burst
            </button>
            <button type="button" class="btn btn-secondary btn-xs" data-guide-action="inspectFailures" data-key="${escapeHtml(topErrRow && topErrRow.errors > 0 ? topErrRow.key : "")}">
              🔬 Inspect Failed Records
            </button>
          </div>
        </div>
      `);
    } else {
      cards.push(`
        <div class="finding-card finding-ok">
          <div>
            <span class="finding-badge">🟢 Zero Failures</span>
            <h4>100% Error-Free Across ${fmtInt(totalCount)} Observations</h4>
            <p>No failed records were observed in this ${getLayerLabel(el.recordTypeSelect.value)} window. You can focus on tail latency (p95/p99) or inspect other telemetry layers.</p>
          </div>
          <div class="finding-actions">
            <button type="button" class="btn btn-secondary btn-xs" data-guide-action="switchLayer" data-layer="${el.recordTypeSelect.value === "R" ? "T" : "R"}">
              📑 Check ${el.recordTypeSelect.value === "R" ? "Transactions (T)" : "Requests (R)"}
            </button>
          </div>
        </div>
      `);
    }

    // 2. Tail-Latency Suspect & Root Cause Phase Diagnosis
    if (rows.length > 0) {
      // Pick row with highest p95 among rows with meaningful count (or overall highest p95)
      const minThreshold = Math.max(1, Math.floor(totalCount * 0.005));
      const candidates = rows.filter(r => r.count >= minThreshold);
      const pool = candidates.length > 0 ? candidates : rows;
      const slowestRow = [...pool].sort((a, b) => (b.p95 || 0) - (a.p95 || 0))[0];

      if (slowestRow) {
        const skew = slowestRow.p50 > 0 ? (slowestRow.p99 / slowestRow.p50).toFixed(1) : "1.0";
        let causeText = "";
        if (el.recordTypeSelect.value === "R") {
          const phaseTot = Math.max(1, slowestRow.avgConnect + slowestRow.avgSend + slowestRow.avgServerBusy + slowestRow.avgReceive);
          const busyPct = Math.round((slowestRow.avgServerBusy / phaseTot) * 100);
          const connPct = Math.round((slowestRow.avgConnect / phaseTot) * 100);
          const recvPct = Math.round((slowestRow.avgReceive / phaseTot) * 100);
          if (busyPct >= connPct && busyPct >= recvPct) {
            causeText = ` Driven primarily by <strong>Server Busy wait (${busyPct}%)</strong>.`;
          } else if (connPct >= busyPct && connPct >= recvPct) {
            causeText = ` Driven by <strong>TCP/TLS Connect (${connPct}%)</strong>.`;
          } else {
            causeText = ` Driven by <strong>Payload Receive (${recvPct}%)</strong>.`;
          }
        }

        cards.push(`
          <div class="finding-card finding-warn">
            <div>
              <span class="finding-badge">🟠 Tail-Latency Bottleneck</span>
              <h4>Slowest p95: ${escapeHtml(slowestRow.key)} (${fmtInt(slowestRow.p95)} ms)</h4>
              <p>Median is <strong>${fmtInt(slowestRow.p50)} ms</strong> while p99 reaches <strong>${fmtInt(slowestRow.p99)} ms</strong> (${skew}&times; tail skew, max ${fmtInt(slowestRow.max)} ms across ${fmtInt(slowestRow.count)} calls).${causeText}</p>
            </div>
            <div class="finding-actions">
              <button type="button" class="btn btn-primary btn-xs" data-guide-action="inspectSlowest" data-key="${escapeHtml(slowestRow.key)}">
                🔬 Inspect Slowest Outliers
              </button>
              <button type="button" class="btn btn-secondary btn-xs" data-guide-action="filterKey" data-key="${escapeHtml(slowestRow.key)}">
                🎯 Filter to ${escapeHtml(slowestRow.key.substring(0, 22))}
              </button>
            </div>
          </div>
        `);
      }
    }

    // 3. Peak Latency Window vs. Baseline Comparison Guide
    let maxP95BucketIdx = 0;
    let maxP95Val = 0;
    for (let i = 0; i < n; i++) {
      const v = (tsData.p95 && tsData.p95[i]) || 0;
      if (v > maxP95Val) {
        maxP95Val = v;
        maxP95BucketIdx = i;
      }
    }
    if (maxP95Val > 0 && n >= 3) {
      const i1 = Math.max(0, maxP95BucketIdx - 1);
      const i2 = Math.min(n - 1, maxP95BucketIdx + 1);
      const spikeFrom = tsData.timestamps[i1];
      const spikeTo = tsData.timestamps[i2] + bw;
      cards.push(`
        <div class="finding-card finding-info">
          <div>
            <span class="finding-badge">📈 Temporal Spike Analysis</span>
            <h4>Peak p95 Latency (${fmtInt(maxP95Val)} ms) at ${fmtUtc(tsData.timestamps[maxP95BucketIdx])}</h4>
            <p>Pin the current window as a baseline and zoom into the peak latency spike to see exact <strong>&Delta; RPS</strong> and <strong>&Delta; p95</strong> changes per timer.</p>
          </div>
          <div class="finding-actions">
            <button type="button" class="btn btn-accent btn-xs" data-guide-action="pinAndCompareSpike" data-from="${spikeFrom}" data-to="${spikeTo}">
              ⚖️ Pin Baseline &amp; Zoom to Spike
            </button>
            <button type="button" class="btn btn-secondary btn-xs" data-guide-action="zoomBurst" data-from="${spikeFrom}" data-to="${spikeTo}">
              🔍 Zoom to Spike Only
            </button>
          </div>
        </div>
      `);
    }

    // 4. Hierarchical Drill-Down Suggestion (T -> A -> R or Backend IP / Status split)
    const curType = el.recordTypeSelect.value;
    if (curType === "T") {
      cards.push(`
        <div class="finding-card finding-info">
          <div>
            <span class="finding-badge">🧭 Hierarchy Guide</span>
            <h4>Drill Down: Transactions &rarr; Actions &rarr; Requests</h4>
            <p>You are viewing top-level Scenario Transactions. Drill down into <strong>Actions (A)</strong> or <strong>HTTP Requests (R)</strong> to isolate the exact step or endpoint responsible.</p>
          </div>
          <div class="finding-actions">
            <button type="button" class="btn btn-primary btn-xs" data-guide-action="switchLayer" data-layer="A">
              ⤵️ Drill into Actions (A)
            </button>
            <button type="button" class="btn btn-secondary btn-xs" data-guide-action="switchLayer" data-layer="R">
              ⤵️ Drill into Requests (R)
            </button>
          </div>
        </div>
      `);
    } else if (curType === "R" && state.summaryGroupBy === "TIMER") {
      cards.push(`
        <div class="finding-card finding-info">
          <div>
            <span class="finding-badge">🧭 Dimension Guide</span>
            <h4>Check Backend IP Balance &amp; HTTP Status Codes</h4>
            <p>Pivot the breakdown table or Panel ④ chart by <strong>Backend IP</strong> or <strong>HTTP Status</strong> to spot load-balancer skew or specific error codes.</p>
          </div>
          <div class="finding-actions">
            <button type="button" class="btn btn-secondary btn-xs" data-guide-action="pivotDim" data-dim="USED_IP">
              🖧 Pivot by Backend IP
            </button>
            <button type="button" class="btn btn-secondary btn-xs" data-guide-action="pivotDim" data-dim="STATUS_CODE">
              🔢 Pivot by HTTP Status
            </button>
          </div>
        </div>
      `);
    }

    el.findingsGrid.innerHTML = cards.join("");
    bindFindingActions();
  }

  function bindFindingActions() {
    el.findingsGrid.querySelectorAll("button[data-guide-action]").forEach(btn => {
      btn.addEventListener("click", () => {
        const act = btn.getAttribute("data-guide-action");
        if (act === "resetAll") {
          handleFunnelStepAction("resetAll");
        } else if (act === "zoomBurst") {
          const from = parseInt(btn.getAttribute("data-from"), 10);
          const to = parseInt(btn.getAttribute("data-to"), 10);
          if (to > from) {
            state.fromTime = from;
            state.toTime = to;
            state.zoomed = true;
            syncTimeInputsFromState();
            refreshDashboard("both");
          }
        } else if (act === "pinAndCompareSpike") {
          pinCurrentAsBaseline();
          const from = parseInt(btn.getAttribute("data-from"), 10);
          const to = parseInt(btn.getAttribute("data-to"), 10);
          if (to > from) {
            state.fromTime = from;
            state.toTime = to;
            state.zoomed = true;
            syncTimeInputsFromState();
            refreshDashboard("both");
          }
        } else if (act === "inspectFailures") {
          const key = btn.getAttribute("data-key");
          if (key) applyDimensionFilter(state.summaryGroupBy, key, false);
          const t = el.recordTypeSelect.value;
          el.inspectModeSelect.value = t === "T" ? "FAILED_TRANSACTIONS" : t === "A" ? "FAILED_ACTIONS" : "FAILED_REQUESTS";
          switchTab("forensicTab", true);
        } else if (act === "inspectSlowest") {
          const key = btn.getAttribute("data-key");
          if (key) applyDimensionFilter(state.summaryGroupBy, key, false);
          const t = el.recordTypeSelect.value;
          el.inspectModeSelect.value = t === "T" ? "SLOWEST_TRANSACTIONS" : t === "A" ? "SLOWEST_ACTIONS" : "SLOWEST_REQUESTS";
          switchTab("forensicTab", true);
        } else if (act === "filterKey") {
          const key = btn.getAttribute("data-key");
          if (key) {
            applyDimensionFilter(state.summaryGroupBy, key, false);
            refreshDashboard("both");
          }
        } else if (act === "switchLayer") {
          const layer = btn.getAttribute("data-layer");
          if (layer) {
            el.recordTypeSelect.value = layer;
            syncRecordTypeDependentControls(true);
            populateTimerNamesForType(el.nameFilterInput.value);
            refreshDashboard("both");
          }
        } else if (act === "pivotDim") {
          const dim = btn.getAttribute("data-dim");
          if (dim) {
            state.summaryGroupBy = dim;
            el.chartGroupBySelect.value = dim;
            syncRecordTypeDependentControls(false);
            refreshDashboard("both");
          }
        }
      });
    });
  }

  // Baseline Pinning & Comparison
  function pinCurrentAsBaseline() {
    if (!state.summaryRows || state.summaryRows.length === 0) return;
    const rowMap = new Map();
    for (const r of state.summaryRows) {
      rowMap.set(r.key, { ...r });
    }
    state.baseline = {
      fromTime: state.fromTime,
      toTime: state.toTime,
      groupBy: state.summaryGroupBy,
      recordType: el.recordTypeSelect.value,
      rowMap
    };
    el.baselineBadge.textContent = `📌 Baseline: ${fmtUtc(state.fromTime)} → ${fmtUtc(state.toTime)} (${fmtDuration(state.toTime - state.fromTime)})`;
    el.baselineBadge.classList.remove("hidden");
    el.clearBaselineBtn.classList.remove("hidden");
    renderSummaryTable();
  }

  function clearBaseline() {
    state.baseline = null;
    el.baselineBadge.classList.add("hidden");
    el.clearBaselineBtn.classList.add("hidden");
    renderSummaryTable();
  }

  // Stage 2: Synchronized 2x2 Golden Signals Canvas Grid
  const syncChartState = {
    hoverIdx: null,
    dragging: false,
    dragStartRatio: 0,
    dragCurRatio: 0,
    pendingFromTime: null,
    pendingToTime: null,
    rafId: null,
    padLeft: 48,
    padRight: 12,
    padTop: 10,
    padBottom: 20
  };

  function scheduleAllSignalsRender() {
    if (syncChartState.rafId) cancelAnimationFrame(syncChartState.rafId);
    syncChartState.rafId = requestAnimationFrame(() => {
      syncChartState.rafId = null;
      renderAllSignalCharts();
    });
  }

  function renderAllSignalCharts() {
    const ts = state.lastTimeSeries;
    const bucketSec = ts ? Math.max(0.001, (ts.bucketWidthMs || 1000) / 1000.0) : 1;

    // Panel 1: Load & Throughput
    renderSingleSignalCanvas(
      el.canvasThroughput,
      el.readoutThroughput,
      ts,
      "req/s",
      ts ? [
        { label: "Throughput", color: "#0284c7", fill: "rgba(2, 132, 199, 0.14)", values: ts.rps || [] }
      ] : []
    );

    // Panel 2: Error Rate & Bursts
    const errRate = ts && ts.errors ? ts.errors.map(e => e / bucketSec) : [];
    renderSingleSignalCanvas(
      el.canvasErrors,
      el.readoutErrors,
      ts,
      "err/s",
      ts ? [
        { label: "Errors/s", color: "#e11d48", fill: "rgba(225, 29, 72, 0.18)", values: errRate }
      ] : []
    );

    // Panel 3: Latency Percentiles
    renderSingleSignalCanvas(
      el.canvasLatency,
      el.readoutLatency,
      ts,
      "ms",
      ts ? [
        { label: "p99", color: "#e11d48", values: ts.p99 || [] },
        { label: "p95", color: "#d97706", values: ts.p95 || [] },
        { label: "p50", color: "#059669", values: ts.p50 || [] }
      ] : []
    );

    // Panel 4: Network Phases OR Split Series By Dimension
    const groupBy = el.chartGroupBySelect.value;
    const isGrouped = groupBy !== "NONE" && ts && ts.series && ts.series.length > 0;
    if (isGrouped) {
      el.panel4Title.textContent = `④ p95 Split by ${groupBy} (ms)`;
      renderSingleSignalCanvas(
        el.canvasPhases,
        el.readoutPhases,
        ts,
        "ms",
        ts.series.map((s, idx) => ({
          label: s.key,
          color: SERIES_COLORS[idx % SERIES_COLORS.length],
          values: s.p95 || []
        }))
      );
    } else if (el.recordTypeSelect.value === "R") {
      el.panel4Title.textContent = "④ Network Phase Breakdown (ms)";
      renderSingleSignalCanvas(
        el.canvasPhases,
        el.readoutPhases,
        ts,
        "ms",
        ts ? [
          { label: "Busy", color: "#d97706", values: ts.avgServerBusy || [] },
          { label: "Connect", color: "#0284c7", values: ts.avgConnect || [] },
          { label: "Receive", color: "#059669", values: ts.avgReceive || [] },
          { label: "Send", color: "#7c3aed", values: ts.avgSend || [] }
        ] : []
      );
    } else {
      el.panel4Title.textContent = "④ Mean vs. Max Runtime (ms)";
      renderSingleSignalCanvas(
        el.canvasPhases,
        el.readoutPhases,
        ts,
        "ms",
        ts ? [
          { label: "Max", color: "#e11d48", values: ts.max || [] },
          { label: "Mean", color: "#0284c7", values: ts.mean || [] }
        ] : []
      );
    }
  }

  function renderSingleSignalCanvas(canvas, readoutEl, tsData, unit, lines) {
    if (!canvas) return;
    const wrap = canvas.parentElement;
    const rect = wrap.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    const width = Math.max(240, Math.floor(rect.width));
    const height = Math.max(120, Math.floor(rect.height));
    if (canvas.width !== width * dpr || canvas.height !== height * dpr) {
      canvas.width = width * dpr;
      canvas.height = height * dpr;
    }

    const isDark = isDarkSchemeActive();
    const gridColor = isDark ? "#1e293b" : "#e2e8f0";
    const axisTextColor = isDark ? "#94a3b8" : "#475569";

    const ctx = canvas.getContext("2d");
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.save();
    ctx.scale(dpr, dpr);

    const { padLeft, padRight, padTop, padBottom } = syncChartState;
    const plotW = width - padLeft - padRight;
    const plotH = height - padTop - padBottom;

    if (!tsData || !tsData.timestamps || tsData.timestamps.length === 0 || lines.length === 0) {
      ctx.fillStyle = axisTextColor;
      ctx.font = "11.5px sans-serif";
      ctx.textAlign = "center";
      ctx.fillText("No telemetry in selected window", width / 2, height / 2);
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
    if (maxVal <= 0) maxVal = 1;
    else maxVal *= 1.1;

    // Horizontal grid lines
    ctx.strokeStyle = gridColor;
    ctx.fillStyle = axisTextColor;
    ctx.font = "10px monospace";
    ctx.textAlign = "right";
    const yTicks = 3;
    for (let i = 0; i <= yTicks; i++) {
      const ratio = i / yTicks;
      const y = padTop + plotH - ratio * plotH;
      const val = ratio * maxVal;
      ctx.beginPath();
      ctx.moveTo(padLeft, y);
      ctx.lineTo(padLeft + plotW, y);
      ctx.stroke();
      ctx.fillText(val >= 100 ? Math.round(val).toString() : val.toFixed(1), padLeft - 5, y + 3);
    }

    // X-axis labels
    ctx.textAlign = "center";
    const xTicks = Math.min(4, n);
    for (let i = 0; i < xTicks; i++) {
      const idx = Math.min(n - 1, Math.floor((i / Math.max(1, xTicks - 1)) * (n - 1)));
      const x = padLeft + (n === 1 ? plotW / 2 : (idx / (n - 1)) * plotW);
      ctx.fillText(fmtUtc(timestamps[idx]), x, height - 5);
    }

    // Draw series lines & optional area fill
    for (const line of lines) {
      if (line.fill && n > 1) {
        ctx.fillStyle = line.fill;
        ctx.beginPath();
        for (let i = 0; i < n; i++) {
          const x = padLeft + (i / (n - 1)) * plotW;
          const y = padTop + plotH - ((line.values[i] || 0) / maxVal) * plotH;
          if (i === 0) ctx.moveTo(x, y);
          else ctx.lineTo(x, y);
        }
        ctx.lineTo(padLeft + plotW, padTop + plotH);
        ctx.lineTo(padLeft, padTop + plotH);
        ctx.closePath();
        ctx.fill();
      }

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

    // Synchronized Brush Selection Overlay
    if (syncChartState.dragging || (syncChartState.pendingFromTime !== null && syncChartState.pendingToTime !== null)) {
      const r1 = Math.max(0, Math.min(1, Math.min(syncChartState.dragStartRatio, syncChartState.dragCurRatio)));
      const r2 = Math.max(0, Math.min(1, Math.max(syncChartState.dragStartRatio, syncChartState.dragCurRatio)));
      const x1 = padLeft + r1 * plotW;
      const x2 = padLeft + r2 * plotW;
      ctx.fillStyle = "rgba(2, 132, 199, 0.22)";
      ctx.fillRect(x1, padTop, Math.max(2, x2 - x1), plotH);
      ctx.strokeStyle = "#0284c7";
      ctx.strokeRect(x1, padTop, Math.max(2, x2 - x1), plotH);
    }

    // Synchronized Crosshair & Readout
    if (syncChartState.hoverIdx !== null && syncChartState.hoverIdx >= 0 && syncChartState.hoverIdx < n) {
      const idx = syncChartState.hoverIdx;
      const snapX = padLeft + (n === 1 ? plotW / 2 : (idx / (n - 1)) * plotW);

      ctx.strokeStyle = isDark ? "rgba(148, 163, 184, 0.65)" : "rgba(71, 85, 105, 0.55)";
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.moveTo(snapX, padTop);
      ctx.lineTo(snapX, padTop + plotH);
      ctx.stroke();

      if (readoutEl) {
        const parts = lines.slice(0, 3).map(l => `${l.label}: ${fmtDec(l.values[idx] || 0, 1)}${unit}`);
        readoutEl.textContent = `${fmtUtc(timestamps[idx])} · ${parts.join(" · ")}`;
      }
    } else if (readoutEl) {
      const legendStr = lines.slice(0, 4).map(l => l.label).join(" · ");
      readoutEl.textContent = legendStr;
    }

    ctx.restore();
  }

  function hideBrushBanner() {
    syncChartState.pendingFromTime = null;
    syncChartState.pendingToTime = null;
    if (el.brushActionBanner) el.brushActionBanner.classList.add("hidden");
  }

  function setupSynchronizedCharts() {
    const wraps = document.querySelectorAll(".signal-canvas-wrap");
    wraps.forEach(wrap => {
      const canvas = wrap.querySelector("canvas");
      if (!canvas) return;

      canvas.addEventListener("mousedown", e => {
        const rect = canvas.getBoundingClientRect();
        const plotW = Math.max(1, rect.width - syncChartState.padLeft - syncChartState.padRight);
        const relX = Math.max(0, Math.min(plotW, e.clientX - rect.left - syncChartState.padLeft));
        syncChartState.dragging = true;
        syncChartState.dragStartRatio = relX / plotW;
        syncChartState.dragCurRatio = syncChartState.dragStartRatio;
        scheduleAllSignalsRender();
      });

      canvas.addEventListener("mousemove", e => {
        const ts = state.lastTimeSeries;
        if (!ts || !ts.timestamps || ts.timestamps.length === 0) return;
        const rect = canvas.getBoundingClientRect();
        const plotW = Math.max(1, rect.width - syncChartState.padLeft - syncChartState.padRight);
        const relX = Math.max(0, Math.min(plotW, e.clientX - rect.left - syncChartState.padLeft));
        const ratio = relX / plotW;

        if (syncChartState.dragging) {
          syncChartState.dragCurRatio = ratio;
        }
        const n = ts.timestamps.length;
        syncChartState.hoverIdx = n === 1 ? 0 : Math.round(ratio * (n - 1));
        scheduleAllSignalsRender();
      });

      canvas.addEventListener("mouseleave", () => {
        if (!syncChartState.dragging && syncChartState.hoverIdx !== null) {
          syncChartState.hoverIdx = null;
          scheduleAllSignalsRender();
        }
      });

      canvas.addEventListener("dblclick", () => {
        if (state.meta && state.zoomed) {
          state.fromTime = state.meta.minTime;
          state.toTime = state.meta.maxTime;
          state.zoomed = false;
          hideBrushBanner();
          syncTimeInputsFromState();
          refreshDashboard("both");
        }
      });
    });

    window.addEventListener("mouseup", () => {
      if (!syncChartState.dragging) return;
      syncChartState.dragging = false;
      const ts = state.lastTimeSeries;
      if (!ts || !ts.timestamps || ts.timestamps.length < 2) {
        scheduleAllSignalsRender();
        return;
      }
      const r1 = Math.min(syncChartState.dragStartRatio, syncChartState.dragCurRatio);
      const r2 = Math.max(syncChartState.dragStartRatio, syncChartState.dragCurRatio);
      if (r2 - r1 > 0.02) {
        const n = ts.timestamps.length;
        const idx1 = Math.max(0, Math.floor(r1 * (n - 1)));
        const idx2 = Math.min(n - 1, Math.ceil(r2 * (n - 1)));
        const newFrom = ts.timestamps[idx1];
        const newTo = ts.timestamps[idx2] + (ts.bucketWidthMs || 1000);
        if (newTo > newFrom) {
          syncChartState.pendingFromTime = newFrom;
          syncChartState.pendingToTime = newTo;
          el.brushBannerText.textContent = `Selected Window: ${fmtUtc(newFrom)} → ${fmtUtc(newTo)} (${fmtDuration(newTo - newFrom)})`;
          el.brushActionBanner.classList.remove("hidden");
        }
      }
      scheduleAllSignalsRender();
    });

    window.addEventListener("resize", () => {
      scheduleAllSignalsRender();
      if (state.activeTab === "forensicTab") renderScatterPlot();
    });
  }

  // Stage 3A: Render Visual Dimension Breakdown Table
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

  function renderDeltaPill(curVal, baseVal, invertGoodBad = false) {
    if (baseVal === undefined || baseVal === null || baseVal <= 0) return "";
    const diffPct = ((curVal - baseVal) / baseVal) * 100;
    if (Math.abs(diffPct) < 1.0) return "";
    const sign = diffPct > 0 ? "+" : "";
    const isWorse = invertGoodBad ? diffPct > 5 : diffPct < -5;
    const cls = isWorse ? "delta-worse" : "delta-better";
    return `<span class="delta-pill ${cls}" title="Baseline: ${fmtDec(baseVal, 1)}">${sign}${Math.round(diffPct)}%</span>`;
  }

  function renderSummaryTable() {
    const rows = getSortedFilteredSummaryRows();
    if (rows.length === 0) {
      el.summaryTableBody.innerHTML = `<tr><td colspan="12" class="empty-cell">No matching records for current filter criteria.</td></tr>`;
      return;
    }

    let totalVolume = 0;
    let maxP99 = 1;
    for (const r of rows) {
      totalVolume += r.count || 0;
      if ((r.p99 || 0) > maxP99) maxP99 = r.p99;
    }

    const isReq = el.recordTypeSelect.value === "R";
    const curType = el.recordTypeSelect.value;
    const hasBaseline = state.baseline &&
      state.baseline.groupBy === state.summaryGroupBy &&
      state.baseline.recordType === curType;

    el.summaryTableBody.innerHTML = rows.map((r, idx) => {
      const sharePct = totalVolume > 0 ? ((r.count / totalVolume) * 100) : 0;
      const errBadge = r.errors > 0
        ? `<span class="badge badge-err">${fmtInt(r.errors)}</span>`
        : `<span class="badge badge-ok">0</span>`;

      const baseRow = hasBaseline ? state.baseline.rowMap.get(r.key) : null;
      const rpsDelta = baseRow ? renderDeltaPill(r.rps, baseRow.rps, false) : "";
      const p95Delta = baseRow ? renderDeltaPill(r.p95, baseRow.p95, true) : "";

      // Latency 3-tier bar (p50, p95, p99 relative to table maxP99)
      const p50W = Math.min(100, ((r.p50 || 0) / maxP99) * 100).toFixed(1);
      const p95W = Math.max(0, Math.min(100 - p50W, (((r.p95 || 0) - (r.p50 || 0)) / maxP99) * 100)).toFixed(1);
      const p99W = Math.max(0, Math.min(100 - p50W - p95W, (((r.p99 || 0) - (r.p95 || 0)) / maxP99) * 100)).toFixed(1);

      // Network Phase Split Bar
      const phaseSum = Math.max(1, (r.avgConnect || 0) + (r.avgSend || 0) + (r.avgServerBusy || 0) + (r.avgReceive || 0));
      let phaseCellHtml;
      if (isReq && phaseSum > 1) {
        const cPct = ((r.avgConnect / phaseSum) * 100).toFixed(1);
        const sPct = ((r.avgSend / phaseSum) * 100).toFixed(1);
        const bPct = ((r.avgServerBusy / phaseSum) * 100).toFixed(1);
        const rPct = ((r.avgReceive / phaseSum) * 100).toFixed(1);
        phaseCellHtml = `
          <div class="phase-micro-wrap" title="Conn: ${fmtDec(r.avgConnect, 1)}ms | Send: ${fmtDec(r.avgSend, 1)}ms | Busy: ${fmtDec(r.avgServerBusy, 1)}ms | Recv: ${fmtDec(r.avgReceive, 1)}ms">
            <div class="phase-micro-bar">
              <div class="phase-seg phase-connect" style="width:${cPct}%"></div>
              <div class="phase-seg phase-send" style="width:${sPct}%"></div>
              <div class="phase-seg phase-busy" style="width:${bPct}%"></div>
              <div class="phase-seg phase-recv" style="width:${rPct}%"></div>
            </div>
            <span class="phase-micro-label">${Math.round(bPct)}% busy</span>
          </div>
        `;
      } else {
        phaseCellHtml = `<span class="phase-micro-label">Mean ${fmtDec(r.mean, 1)} ms</span>`;
      }

      // Hierarchy drill-down button when viewing Transactions or Scenarios
      let hierarchyBtn = "";
      if (curType === "T" || state.summaryGroupBy === "SCENARIO") {
        hierarchyBtn = `
          <button type="button" class="btn btn-secondary btn-xs drill-hierarchy-btn" data-key="${escapeHtml(r.key)}" title="Drill down into HTTP Requests for this Scenario">
            ⤵️ Requests
          </button>
        `;
      }

      return `
        <tr class="data-row" data-idx="${idx}">
          <th scope="row" title="${escapeHtml(r.key)}">${escapeHtml(r.key)}</th>
          <td class="num">
            <div class="cell-with-bar">
              <span>${fmtInt(r.count)} <small style="color:var(--text-muted)">(${fmtDec(sharePct, 1)}%)</small></span>
              <div class="share-bar-track"><div class="share-bar-fill" style="width:${Math.min(100, sharePct).toFixed(1)}%"></div></div>
            </div>
          </td>
          <td class="num">${fmtDec(r.rps, 2)}${rpsDelta}</td>
          <td class="num">${errBadge}</td>
          <td class="num">${fmtDec(r.errorPct, 2)}%</td>
          <td class="num">${fmtInt(r.p50)}</td>
          <td class="num"><strong>${fmtInt(r.p95)}</strong>${p95Delta}</td>
          <td class="num">${fmtInt(r.p99)}</td>
          <td class="num">${fmtInt(r.max)}</td>
          <td>
            <div class="latency-micro-wrap" title="p50: ${fmtInt(r.p50)}ms · p95: ${fmtInt(r.p95)}ms · p99: ${fmtInt(r.p99)}ms">
              <div class="latency-micro-track">
                <div class="latency-seg-p50" style="width:${p50W}%"></div>
                <div class="latency-seg-p95" style="left:${p50W}%;width:${p95W}%"></div>
                <div class="latency-seg-p99" style="left:${(parseFloat(p50W) + parseFloat(p95W)).toFixed(1)}%;width:${p99W}%"></div>
              </div>
            </div>
          </td>
          <td>${phaseCellHtml}</td>
          <td>
            <div class="row-action-group">
              <button type="button" class="btn btn-secondary btn-xs filter-dim-btn" data-key="${escapeHtml(r.key)}" title="Filter dashboard to this group">
                🎯 Filter
              </button>
              ${hierarchyBtn}
              <button type="button" class="btn btn-accent btn-xs inspect-dim-btn" data-key="${escapeHtml(r.key)}" title="Inspect forensic outliers &amp; scatter plot for this group">
                🔬 Outliers
              </button>
            </div>
          </td>
        </tr>
      `;
    }).join("");

    el.summaryTableBody.querySelectorAll(".filter-dim-btn").forEach(btn => {
      btn.addEventListener("click", e => {
        e.stopPropagation();
        applyDimensionFilter(state.summaryGroupBy, btn.dataset.key, false);
        refreshDashboard("both");
      });
    });

    el.summaryTableBody.querySelectorAll(".drill-hierarchy-btn").forEach(btn => {
      btn.addEventListener("click", e => {
        e.stopPropagation();
        const key = btn.dataset.key;
        if (key) el.scenarioSelect.value = key;
        el.recordTypeSelect.value = "R";
        state.summaryGroupBy = "TIMER";
        syncRecordTypeDependentControls(true);
        populateTimerNamesForType(el.nameFilterInput.value);
        refreshDashboard("both");
      });
    });

    el.summaryTableBody.querySelectorAll(".inspect-dim-btn").forEach(btn => {
      btn.addEventListener("click", e => {
        e.stopPropagation();
        applyDimensionFilter(state.summaryGroupBy, btn.dataset.key, true);
      });
    });
  }

  function applyDimensionFilter(groupBy, key, switchToForensics) {
    if (!key || key === "(none)") return;
    if (groupBy === "TIMER") {
      el.nameFilterInput.value = key;
      el.clearNameFilterBtn.classList.remove("hidden");
      populateTimerNamesForType(key);
    } else if (groupBy === "AGENT") {
      el.agentSelect.value = key;
    } else if (groupBy === "SCENARIO") {
      el.scenarioSelect.value = key;
    } else if (groupBy === "USED_IP") {
      state.usedIpFilter = key;
    } else if (groupBy === "HTTP_METHOD") {
      state.httpMethodFilter = key;
    } else if (groupBy === "CONTENT_TYPE") {
      state.contentTypeFilter = key;
    } else if (groupBy === "STATUS_CODE") {
      const m = String(key).match(/(\d+)/);
      if (m) {
        state.exactStatusCodeFilter = parseInt(m[1], 10);
      }
    }
    renderFunnelTrail();
    if (switchToForensics) {
      switchTab("forensicTab", true);
    }
  }

  // Stage 3B: Interactive Outlier Scatter Plot & Forensic Table
  const scatterPoints = [];

  function renderScatterPlot() {
    const canvas = el.scatterCanvas;
    if (!canvas) return;
    const rect = el.scatterWrap.getBoundingClientRect();
    const dpr = window.devicePixelRatio || 1;
    const width = Math.max(280, Math.floor(rect.width));
    const height = Math.max(120, Math.floor(rect.height));
    if (canvas.width !== width * dpr || canvas.height !== height * dpr) {
      canvas.width = width * dpr;
      canvas.height = height * dpr;
    }

    const isDark = isDarkSchemeActive();
    const gridColor = isDark ? "#1e293b" : "#e2e8f0";
    const axisTextColor = isDark ? "#94a3b8" : "#475569";

    const ctx = canvas.getContext("2d");
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.save();
    ctx.scale(dpr, dpr);

    scatterPoints.length = 0;
    const records = state.forensicRecords || [];
    if (records.length === 0) {
      ctx.fillStyle = axisTextColor;
      ctx.font = "12px sans-serif";
      ctx.textAlign = "center";
      ctx.fillText("No outlier records in current selection", width / 2, height / 2);
      ctx.restore();
      return;
    }

    const padLeft = 54, padRight = 18, padTop = 12, padBottom = 22;
    const plotW = width - padLeft - padRight;
    const plotH = height - padTop - padBottom;

    let minT = state.fromTime || records[0].time;
    let maxT = state.toTime || records[0].time;
    let maxRT = 10;
    for (const r of records) {
      if (r.time < minT) minT = r.time;
      if (r.time > maxT) maxT = r.time;
      if (r.runTime > maxRT) maxRT = r.runTime;
    }
    maxRT *= 1.08;
    const spanT = Math.max(1, maxT - minT);

    // Grid lines
    ctx.strokeStyle = gridColor;
    ctx.fillStyle = axisTextColor;
    ctx.font = "10px monospace";
    ctx.textAlign = "right";
    for (let i = 0; i <= 3; i++) {
      const ratio = i / 3;
      const y = padTop + plotH - ratio * plotH;
      ctx.beginPath();
      ctx.moveTo(padLeft, y);
      ctx.lineTo(padLeft + plotW, y);
      ctx.stroke();
      ctx.fillText(`${Math.round(ratio * maxRT)}ms`, padLeft - 5, y + 3);
    }

    // Plot points
    records.forEach((rec, idx) => {
      const x = padLeft + ((rec.time - minT) / spanT) * plotW;
      const y = padTop + plotH - ((rec.runTime || 0) / maxRT) * plotH;
      const isErr = rec.failed || rec.statusCode >= 500 || rec.statusCode === 0;
      const isWarn = !isErr && rec.statusCode >= 400;
      const color = isErr ? "#e11d48" : isWarn ? "#d97706" : "#0284c7";
      const isSelected = state.expandedForensicIdx === idx;

      ctx.beginPath();
      ctx.arc(x, y, isSelected ? 6.5 : 4.2, 0, Math.PI * 2);
      ctx.fillStyle = color;
      ctx.globalAlpha = isSelected ? 1.0 : 0.78;
      ctx.fill();
      if (isSelected) {
        ctx.lineWidth = 2;
        ctx.strokeStyle = isDark ? "#ffffff" : "#0f172a";
        ctx.stroke();
      }
      ctx.globalAlpha = 1.0;

      scatterPoints.push({ x, y, idx, rec });
    });

    ctx.restore();
  }

  function setupScatterInteractions() {
    if (!el.scatterCanvas) return;
    el.scatterCanvas.addEventListener("mousemove", e => {
      const rect = el.scatterCanvas.getBoundingClientRect();
      const mx = e.clientX - rect.left;
      const my = e.clientY - rect.top;
      let nearest = null;
      let bestDist = 144; // 12px radius squared
      for (const p of scatterPoints) {
        const d2 = (p.x - mx) ** 2 + (p.y - my) ** 2;
        if (d2 < bestDist) {
          bestDist = d2;
          nearest = p;
        }
      }
      if (nearest && el.scatterReadout) {
        const r = nearest.rec;
        el.scatterReadout.textContent = `${fmtUtc(r.time)} · ${r.timerName} · ${fmtInt(r.runTime)} ms (${r.recordType === "R" ? "HTTP " + r.statusCode : (r.failed ? "FAILED" : "OK")})`;
      }
    });

    el.scatterCanvas.addEventListener("click", e => {
      const rect = el.scatterCanvas.getBoundingClientRect();
      const mx = e.clientX - rect.left;
      const my = e.clientY - rect.top;
      let nearest = null;
      let bestDist = 196;
      for (const p of scatterPoints) {
        const d2 = (p.x - mx) ** 2 + (p.y - my) ** 2;
        if (d2 < bestDist) {
          bestDist = d2;
          nearest = p;
        }
      }
      if (nearest) {
        state.expandedForensicIdx = nearest.idx;
        renderForensicTable();
        renderScatterPlot();
        const rowEl = el.forensicTableBody.querySelector(`tr.forensic-row[data-idx="${nearest.idx}"]`);
        if (rowEl) {
          rowEl.scrollIntoView({ block: "center", behavior: "smooth" });
        }
      }
    });
  }

  function renderForensicTable() {
    const records = state.forensicRecords;
    if (!records || records.length === 0) {
      el.forensicTableBody.innerHTML = `<tr><td colspan="9" class="empty-cell">No forensic records found in current time window and filters.</td></tr>`;
      return;
    }

    const htmlRows = [];
    records.forEach((rec, idx) => {
      const isTx = rec.recordType === "T";
      const isAction = rec.recordType === "A";
      const isReq = rec.recordType === "R";

      let statusBadge;
      if (isTx) {
        statusBadge = rec.failed ? `<span class="badge badge-err">FAILED TX</span>` : `<span class="badge badge-ok">OK (TX)</span>`;
      } else if (isAction) {
        statusBadge = rec.failed ? `<span class="badge badge-err">FAILED ACTION</span>` : `<span class="badge badge-ok">OK (ACTION)</span>`;
      } else if (isReq) {
        statusBadge = (rec.failed || rec.statusCode >= 400 || rec.statusCode === 0)
          ? `<span class="badge badge-err">HTTP ${rec.statusCode}</span>`
          : `<span class="badge badge-ok">HTTP ${rec.statusCode}</span>`;
      } else {
        statusBadge = rec.failed ? `<span class="badge badge-err">FAILED</span>` : `<span class="badge badge-ok">OK</span>`;
      }

      let summaryDetail;
      if (isTx) {
        summaryDetail = `<span class="url-preview">${escapeHtml(rec.failedActionName ? "Action: " + rec.failedActionName + " — " : "")}${escapeHtml(rec.stackTrace || (rec.failed ? "Transaction failed" : "Transaction completed"))}</span>`;
      } else if (isAction) {
        summaryDetail = `<span class="url-preview">${escapeHtml(rec.stackTrace || (rec.failed ? "Action failed" : "Action completed"))}</span>`;
      } else if (isReq) {
        summaryDetail = `<span class="badge badge-info">${escapeHtml(rec.httpMethod || "GET")}</span> <span class="url-preview" title="${escapeHtml(rec.url)}">${escapeHtml(rec.url || "—")}</span>`;
      } else {
        summaryDetail = `<span class="url-preview">${escapeHtml(rec.timerName || "—")}</span>`;
      }

      htmlRows.push(`
        <tr class="data-row forensic-row" data-idx="${idx}">
          <th scope="row" title="${escapeHtml(rec.timerName)}">${escapeHtml(rec.timerName)}</th>
          <td class="num">${fmtUtc(rec.time, true)}</td>
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
        renderScatterPlot();
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

    if (rec.recordType === "A" || rec.recordType === "C" || rec.recordType === "P") {
      const typeLabel = rec.recordType === "A" ? "Action" : rec.recordType === "C" ? "Custom Timer" : "Page Load";
      return `
        <tr class="drawer-row">
          <td colspan="9">
            <div class="forensic-drawer">
              <div class="drawer-card">
                <h4>${typeLabel} Context</h4>
                <div class="kv-grid">
                  <span class="kv-label">${typeLabel} Name:</span><span class="kv-val">${escapeHtml(rec.timerName)}</span>
                  <span class="kv-label">Agent / VU:</span><span class="kv-val">${escapeHtml(rec.agent)} / VU #${escapeHtml(rec.userNumber || "0")}</span>
                  <span class="kv-label">Scenario:</span><span class="kv-val">${escapeHtml(rec.scenario || "—")}</span>
                  <span class="kv-label">Status:</span><span class="kv-val">${rec.failed ? "FAILED" : "OK"}</span>
                  <span class="kv-label">Duration:</span><span class="kv-val">${fmtInt(rec.runTime)} ms</span>
                </div>
              </div>
              <div class="drawer-card">
                <h4>Execution Details</h4>
                <pre class="stacktrace-pre">${escapeHtml(rec.stackTrace || (rec.failed ? "Marked as failed without stack trace" : "Completed successfully"))}</pre>
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
      : `<span class="badge badge-warn">ℹ️ Representative Timer URL (Sparse Mode)</span>`;

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

  // Native <dialog> Modal for Virtual User Session Reconstruction
  async function openSessionModal(rec) {
    state.sessionContext = {
      agent: rec.agent,
      scenario: rec.scenario,
      userNumber: rec.userNumber || "0",
      targetTime: rec.time,
      targetName: rec.timerName,
      targetType: rec.recordType,
      targetRequestId: rec.requestId,
      paddingMs: 30000
    };
    if (el.sessionModalDialog && !el.sessionModalDialog.open) {
      el.sessionModalDialog.showModal();
    }
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
          const widthPct = Math.max(0.6, Math.min(100 - leftPct, (Math.max(1, n.runTime) / spanMs) * 100)).toFixed(1);

          let barClass = "wf-bar-req";
          if (n.typeCode === "T") barClass = n.failed ? "wf-bar-err" : "wf-bar-tx";
          else if (n.typeCode === "A") barClass = n.failed ? "wf-bar-err" : "wf-bar-action";
          else if (n.typeCode === "E") barClass = "wf-bar-event";
          else if (n.failed || n.statusCode >= 400) barClass = "wf-bar-err";

          const isTarget = state.sessionContext && (
            (state.sessionContext.targetRequestId && n.requestId === state.sessionContext.targetRequestId) ||
            (Math.abs(n.time - state.sessionContext.targetTime) <= 1 && n.name === state.sessionContext.targetName)
          );

          const targetPill = isTarget ? `<span class="badge badge-warn">🎯 Target</span> ` : "";
          const rowClass = isTarget ? "wf-row wf-target-row" : "wf-row";
          const rowIdAttr = isTarget ? 'id="wfTargetRow"' : "";

          const badge = n.typeCode === "R"
            ? `<span class="badge ${n.failed || n.statusCode >= 400 ? "badge-err" : "badge-ok"}">${escapeHtml(n.httpMethod || "GET")} ${n.statusCode}</span>`
            : `<span class="badge ${n.failed ? "badge-err" : "badge-info"}">${escapeHtml(n.typeCode)}</span>`;

          const detail = n.typeCode === "R"
            ? `<span class="url-preview" title="${escapeHtml(n.url)}">${escapeHtml(n.url || "")}</span>`
            : `<span class="url-preview" title="${escapeHtml(n.message)}">${escapeHtml(n.message || "")}</span>`;

          rowsHtml.push(`
            <tr class="${rowClass}" ${rowIdAttr}>
              <td class="wf-indent-${Math.min(2, depth)}">${targetPill}${escapeHtml(n.name)}</td>
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
              <th>Iteration Waterfall (0 &rarr; ${fmtDuration(spanMs)})</th>
              <th>URL / Event Message / Stack Trace</th>
            </tr>
          </thead>
          <tbody>${rowsHtml.join("")}</tbody>
        </table>
      `;

      const targetEl = el.sessionModalBody.querySelector("#wfTargetRow");
      if (targetEl) {
        setTimeout(() => {
          targetEl.scrollIntoView({ block: "center", behavior: "smooth" });
        }, 60);
      }
    } catch (err) {
      el.sessionModalBody.innerHTML = `<div class="empty-cell">Failed to reconstruct session: ${escapeHtml(err.message)}</div>`;
    }
  }

  // Export & Markdown Helpers
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
      el.copyMarkdownBtn.textContent = "✓ Copied!";
      setTimeout(() => { el.copyMarkdownBtn.textContent = orig; }, 1600);
    } catch (_) {}
  }

  function downloadCsv(filename, headers, rows) {
    function escapeCsvCell(val) {
      if (val === null || val === undefined) return '""';
      const str = String(val);
      if (str.includes('"') || str.includes(',') || str.includes('\n') || str.includes('\r')) {
        return `"${str.replace(/"/g, '""')}"`;
      }
      return `"${str}"`;
    }
    const lines = [headers.map(escapeCsvCell).join(",")];
    for (const row of rows) {
      lines.push(row.map(escapeCsvCell).join(","));
    }
    const blob = new Blob([lines.join("\r\n")], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  function exportSummaryCsv() {
    const rows = getSortedFilteredSummaryRows();
    const headers = [
      "Dimension Key", "Count", "RPS", "Errors", "Error %",
      "Mean (ms)", "Min (ms)", "p50 (ms)", "p90 (ms)", "p95 (ms)", "p99 (ms)", "Max (ms)",
      "TTFB Mean (ms)", "Connect Mean (ms)", "Busy Mean (ms)", "Distinct URLs"
    ];
    const data = rows.map(r => [
      r.key, r.count, r.rps, r.errors, r.errorPct,
      r.mean, r.min, r.p50, r.p90, r.p95, r.p99, r.max,
      r.avgTtfb, r.avgConnect, r.avgServerBusy, r.distinctUrls
    ]);
    downloadCsv(`chunkdb-guided-summary-${state.summaryGroupBy.toLowerCase()}-${Date.now()}.csv`, headers, data);
  }

  function exportForensicCsv() {
    const records = state.forensicRecords || [];
    const headers = [
      "Time (UTC)", "Record Type", "Timer Name", "Runtime (ms)", "Status",
      "Failed", "Scenario", "Agent", "VU", "Method", "URL",
      "Used IP", "Request ID", "Failed Action", "Stack Trace / Message"
    ];
    const data = records.map(r => [
      fmtUtc(r.time, true),
      r.recordType,
      r.timerName,
      r.runTime,
      r.recordType === "R" ? `HTTP ${r.statusCode}` : (r.failed ? "FAILED" : "OK"),
      r.failed ? "true" : "false",
      r.scenario || "",
      r.agent || "",
      r.userNumber || "",
      r.httpMethod || "",
      r.url || "",
      r.usedIp || "",
      r.requestId || "",
      r.failedActionName || "",
      r.stackTrace || ""
    ]);
    downloadCsv(`chunkdb-guided-forensic-${Date.now()}.csv`, headers, data);
  }

  function switchTab(tabId, autoFetch = false) {
    state.activeTab = tabId;
    el.tabBtnSummary.classList.toggle("active", tabId === "summaryTab");
    el.tabBtnForensic.classList.toggle("active", tabId === "forensicTab");
    el.summaryTab.classList.toggle("active", tabId === "summaryTab");
    el.forensicTab.classList.toggle("active", tabId === "forensicTab");
    if (autoFetch || (tabId === "forensicTab" && state.forensicRecords.length === 0)) {
      refreshDashboard("bottom");
    } else if (tabId === "forensicTab") {
      renderScatterPlot();
    }
  }

  // Event Listeners & Modern Web Guidance Fallbacks
  function initEventListeners() {
    // Theme toggle with <meta name="color-scheme"> and localStorage persistence
    if (el.themeToggleBtn) {
      el.themeToggleBtn.addEventListener("click", () => {
        const meta = document.querySelector('meta[name="color-scheme"]');
        const nextScheme = isDarkSchemeActive() ? "light" : "dark";
        if (meta) meta.content = nextScheme;
        try {
          localStorage.setItem("color-scheme", nextScheme);
        } catch (_) {}
        scheduleAllSignalsRender();
        if (state.activeTab === "forensicTab") renderScatterPlot();
      });
    }

    window.addEventListener("storage", e => {
      if (e.key === "color-scheme") {
        const meta = document.querySelector('meta[name="color-scheme"]');
        if (meta) meta.content = e.newValue ?? "light dark";
        scheduleAllSignalsRender();
        if (state.activeTab === "forensicTab") renderScatterPlot();
      }
    });

    if (window.matchMedia) {
      window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", () => {
        scheduleAllSignalsRender();
        if (state.activeTab === "forensicTab") renderScatterPlot();
      });
    }

    // Native <dialog closedby="any"> fallback for browsers without closedBy support
    const dialog = el.sessionModalDialog;
    if (dialog && !("closedBy" in HTMLDialogElement.prototype)) {
      dialog.addEventListener("click", event => {
        if (event.target !== dialog) return;
        const rect = dialog.getBoundingClientRect();
        const isDialogContent = (
          rect.top <= event.clientY &&
          event.clientY <= rect.top + rect.height &&
          rect.left <= event.clientX &&
          event.clientX <= rect.left + rect.width
        );
        if (isDialogContent) return;
        dialog.close();
      });
    }

    if (el.closeSessionModalBtn && dialog) {
      el.closeSessionModalBtn.addEventListener("click", () => dialog.close());
    }

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

    // Baseline Pin / Clear
    el.pinBaselineBtn.addEventListener("click", pinCurrentAsBaseline);
    el.clearBaselineBtn.addEventListener("click", clearBaseline);

    // Brush Action Banner Buttons
    el.applyBrushZoomBtn.addEventListener("click", () => {
      if (syncChartState.pendingFromTime && syncChartState.pendingToTime) {
        state.fromTime = syncChartState.pendingFromTime;
        state.toTime = syncChartState.pendingToTime;
        state.zoomed = true;
        hideBrushBanner();
        syncTimeInputsFromState();
        refreshDashboard("both");
      }
    });

    el.compareBrushWithBaselineBtn.addEventListener("click", () => {
      if (syncChartState.pendingFromTime && syncChartState.pendingToTime) {
        if (!state.baseline) pinCurrentAsBaseline();
        state.fromTime = syncChartState.pendingFromTime;
        state.toTime = syncChartState.pendingToTime;
        state.zoomed = true;
        hideBrushBanner();
        syncTimeInputsFromState();
        refreshDashboard("both");
      }
    });

    el.cancelBrushBtn.addEventListener("click", () => {
      hideBrushBanner();
      scheduleAllSignalsRender();
    });

    // Quick Layer Pills
    if (el.layerQuickPills) {
      el.layerQuickPills.querySelectorAll(".layer-pill").forEach(btn => {
        btn.addEventListener("click", () => {
          const layer = btn.dataset.layer;
          if (layer && el.recordTypeSelect.value !== layer) {
            el.recordTypeSelect.value = layer;
            syncRecordTypeDependentControls(true);
            populateTimerNamesForType(el.nameFilterInput.value);
            refreshDashboard("both");
          }
        });
      });
    }

    // Dimension Breakdown Pills
    if (el.dimPillGroup) {
      el.dimPillGroup.querySelectorAll(".dim-pill").forEach(btn => {
        btn.addEventListener("click", () => {
          if (btn.disabled) return;
          const dim = btn.dataset.dim;
          if (dim && state.summaryGroupBy !== dim) {
            state.summaryGroupBy = dim;
            syncRecordTypeDependentControls(false);
            refreshDashboard("both");
          }
        });
      });
    }

    // Analyze & Filter Bar Controls
    el.runQueryBtn.addEventListener("click", () => {
      applyTimeInputsToState();
      clearPendingChanges();
      refreshDashboard("both");
    });

    el.recordTypeSelect.addEventListener("change", () => {
      syncRecordTypeDependentControls(true);
      populateTimerNamesForType(el.nameFilterInput.value);
      markPendingChanges();
    });

    el.nameFilterInput.addEventListener("input", () => {
      el.clearNameFilterBtn.classList.toggle("hidden", !el.nameFilterInput.value);
      populateTimerNamesForType(el.nameFilterInput.value);
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
      inp.addEventListener("change", () => {
        applyTimeInputsToState();
        markPendingChanges();
      });
    });

    el.clearNameFilterBtn.addEventListener("click", () => {
      el.nameFilterInput.value = "";
      el.clearNameFilterBtn.classList.add("hidden");
      populateTimerNamesForType("");
      markPendingChanges();
    });

    if (el.statusPresetSelect) {
      el.statusPresetSelect.addEventListener("change", () => {
        state.exactStatusCodeFilter = null;
        markPendingChanges();
      });
    }

    [el.scenarioSelect, el.agentSelect, el.failedOnlyCheckbox].forEach(ctrl => {
      if (ctrl) ctrl.addEventListener("change", () => markPendingChanges());
    });

    [el.chartGroupBySelect, el.bucketSizeSelect].forEach(ctrl => {
      if (ctrl) ctrl.addEventListener("change", () => refreshDashboard("both"));
    });

    [el.inspectModeSelect, el.inspectLimitSelect].forEach(ctrl => {
      if (ctrl) {
        ctrl.addEventListener("change", () => {
          const mode = el.inspectModeSelect.value;
          let targetType = null;
          if (mode.endsWith("_REQUESTS")) targetType = "R";
          else if (mode.endsWith("_TRANSACTIONS")) targetType = "T";
          else if (mode.endsWith("_ACTIONS")) targetType = "A";
          if (targetType && el.recordTypeSelect.value !== targetType) {
            el.recordTypeSelect.value = targetType;
            syncRecordTypeDependentControls(false);
            populateTimerNamesForType(el.nameFilterInput.value);
          }
          refreshDashboard("bottom");
        });
      }
    });

    el.summarySearchInput.addEventListener("input", () => renderSummaryTable());
    el.copyMarkdownBtn.addEventListener("click", copyTableAsMarkdown);
    el.exportSummaryCsvBtn.addEventListener("click", exportSummaryCsv);
    el.exportForensicCsvBtn.addEventListener("click", exportForensicCsv);

    el.resetZoomBtn.addEventListener("click", () => {
      if (!state.meta) return;
      state.fromTime = state.meta.minTime;
      state.toTime = state.meta.maxTime;
      state.zoomed = false;
      hideBrushBanner();
      syncTimeInputsFromState();
      refreshDashboard("both");
    });

    el.resetFiltersBtn.addEventListener("click", () => {
      if (!state.meta) return;
      el.recordTypeSelect.value = "R";
      el.nameFilterInput.value = "";
      el.clearNameFilterBtn.classList.add("hidden");
      el.scenarioSelect.value = "";
      el.agentSelect.value = "";
      el.statusPresetSelect.value = "ALL";
      el.failedOnlyCheckbox.checked = false;
      el.chartGroupBySelect.value = "NONE";
      el.bucketSizeSelect.value = "0";
      el.summarySearchInput.value = "";
      state.summaryGroupBy = "TIMER";
      state.usedIpFilter = null;
      state.httpMethodFilter = null;
      state.contentTypeFilter = null;
      state.exactStatusCodeFilter = null;
      state.fromTime = state.meta.minTime;
      state.toTime = state.meta.maxTime;
      state.zoomed = false;
      hideBrushBanner();
      syncRecordTypeDependentControls(true);
      syncTimeInputsFromState();
      populateTimerNamesForType("");
      refreshDashboard("both");
    });

    el.tabBtnSummary.addEventListener("click", () => switchTab("summaryTab", false));
    el.tabBtnForensic.addEventListener("click", () => switchTab("forensicTab", true));

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
  }

  // Bootstrap
  initEventListeners();
  setupSynchronizedCharts();
  setupScatterInteractions();
  loadMetadata();
})();
