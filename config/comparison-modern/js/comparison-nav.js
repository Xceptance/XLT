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
 * XLT Modern Comparison & Trend Report — Shared Navigation, Run Context & Delta Helpers (`window.XLT_CMP`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI || {};
  var esc = UI.esc || function (s) {
    return String(s == null ? "" : s)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  };
  var fmtInt = UI.fmtInt || function (n, fb) {
    if (n === null || n === undefined || n === "" || Number.isNaN(Number(n))) return fb !== undefined ? fb : "0";
    return Math.round(Number(n)).toLocaleString("en-US");
  };
  var fmtCompact = UI.fmtCompact || function (n) { return fmtInt(n, "0"); };

  function dirStorageKey() {
    var p = String((window.location && window.location.pathname) || "");
    var idx = p.lastIndexOf("/");
    return "XLT_CMP_STATE:" + (idx >= 0 ? p.slice(0, idx + 1) : p);
  }

  function loadInitialState() {
    var meta = window.XLT_META || {};
    var runs = Array.isArray(meta.runs) ? meta.runs : [];
    var maxIdx = Math.max(0, runs.length - 1);
    var defBase = Math.max(0, Math.min(maxIdx, Number(meta.defaultBaselineIdx) || 0));
    var defTarget = Math.max(0, Math.min(maxIdx, meta.defaultTargetIdx !== undefined ? Number(meta.defaultTargetIdx) : maxIdx));

    var stored = {};
    try {
      var raw = sessionStorage.getItem(dirStorageKey());
      if (raw) stored = JSON.parse(raw) || {};
    } catch (_) { /* ignore file:// storage blocks if any */ }

    var params = new URLSearchParams(window.location ? window.location.search : "");
    var pBase = params.has("base") ? Number(params.get("base")) : stored.baseIdx;
    var pTarget = params.has("target") ? Number(params.get("target")) : stored.targetIdx;
    var pView = params.get("view") || stored.viewMode || "inline";
    var pBasis = params.get("sortBasis") || stored.sortBasis || "rel";

    var baseIdx = Number.isInteger(pBase) && pBase >= 0 && pBase <= maxIdx ? pBase : defBase;
    var targetIdx = Number.isInteger(pTarget) && pTarget >= 0 && pTarget <= maxIdx ? pTarget : defTarget;
    var viewMode = pView === "pct" ? "pct" : "inline";
    var sortBasis = (pBasis === "abs" || pBasis === "target" || pBasis === "rel") ? pBasis : "rel";

    return { baseIdx: baseIdx, targetIdx: targetIdx, viewMode: viewMode, sortBasis: sortBasis };
  }

  var state = loadInitialState();

  function persistState() {
    try {
      sessionStorage.setItem(dirStorageKey(), JSON.stringify(state));
    } catch (_) { /* ignore */ }
    try {
      if (window.history && window.history.replaceState) {
        var u = new URL(window.location.href);
        u.searchParams.set("base", String(state.baseIdx));
        u.searchParams.set("target", String(state.targetIdx));
        u.searchParams.set("view", state.viewMode);
        u.searchParams.set("sortBasis", state.sortBasis);
        window.history.replaceState(null, "", u.toString());
      }
    } catch (_) { /* ignore opaque origins */ }
  }

  function syncNavQueryLinks() {
    var links = document.querySelectorAll(".site-nav a.nav-btn[href], .site-nav a.nav-sublink[href]");
    Array.prototype.forEach.call(links, function (a) {
      var rawHref = a.getAttribute("data-base-href") || a.getAttribute("href");
      if (!rawHref || rawHref.charAt(0) === "#" || /^https?:/i.test(rawHref)) return;
      a.setAttribute("data-base-href", rawHref);
      var hashIdx = rawHref.indexOf("#");
      var basePart = hashIdx >= 0 ? rawHref.slice(0, hashIdx) : rawHref;
      var hashPart = hashIdx >= 0 ? rawHref.slice(hashIdx) : "";
      var qIdx = basePart.indexOf("?");
      var pathPart = qIdx >= 0 ? basePart.slice(0, qIdx) : basePart;
      var qs = new URLSearchParams(qIdx >= 0 ? basePart.slice(qIdx + 1) : "");
      qs.set("base", String(state.baseIdx));
      qs.set("target", String(state.targetIdx));
      qs.set("view", state.viewMode);
      a.setAttribute("href", pathPart + "?" + qs.toString() + hashPart);
    });
  }

  function setState(patch, silent) {
    var meta = window.XLT_META || {};
    var runs = Array.isArray(meta.runs) ? meta.runs : [];
    var maxIdx = Math.max(0, runs.length - 1);
    var changed = false;

    if (patch.baseIdx !== undefined) {
      var nb = Math.max(0, Math.min(maxIdx, Number(patch.baseIdx) || 0));
      if (nb !== state.baseIdx) { state.baseIdx = nb; changed = true; }
    }
    if (patch.targetIdx !== undefined) {
      var nt = Math.max(0, Math.min(maxIdx, Number(patch.targetIdx) || 0));
      if (nt !== state.targetIdx) { state.targetIdx = nt; changed = true; }
    }
    if (patch.viewMode === "inline" || patch.viewMode === "pct") {
      if (patch.viewMode !== state.viewMode) { state.viewMode = patch.viewMode; changed = true; }
    }
    if (patch.sortBasis === "rel" || patch.sortBasis === "abs" || patch.sortBasis === "target") {
      if (patch.sortBasis !== state.sortBasis) { state.sortBasis = patch.sortBasis; changed = true; }
    }

    if (!changed) return;
    persistState();
    syncSelectorControls();
    syncNavQueryLinks();
    if (!silent) {
      window.dispatchEvent(new CustomEvent("xlt-cmp-change", { detail: getState() }));
    }
  }

  function getState() {
    return {
      baseIdx: state.baseIdx,
      targetIdx: state.targetIdx,
      viewMode: state.viewMode,
      sortBasis: state.sortBasis
    };
  }

  function getRuns() {
    var meta = window.XLT_META || {};
    return Array.isArray(meta.runs) ? meta.runs : [];
  }

  function getBaseRun() {
    var runs = getRuns();
    return runs[state.baseIdx] || runs[0] || null;
  }

  function getTargetRun() {
    var runs = getRuns();
    return runs[state.targetIdx] || runs[runs.length - 1] || null;
  }

  function formatRunOptionLabel(run, idx) {
    if (!run) return "Run #" + (idx + 1);
    var head = run.commentHeadline ? " — " + run.commentHeadline : "";
    return "#" + (idx + 1) + " " + (run.name || ("Run " + (idx + 1))) + head;
  }

  function syncSelectorControls() {
    var runs = getRuns();
    var baseSel = document.getElementById("cmp-base-select");
    var targetSel = document.getElementById("cmp-target-select");
    var baseLink = document.getElementById("cmp-base-link");
    var targetLink = document.getElementById("cmp-target-link");

    if (baseSel) baseSel.value = String(state.baseIdx);
    if (targetSel) targetSel.value = String(state.targetIdx);

    var rA = runs[state.baseIdx];
    var rB = runs[state.targetIdx];
    if (baseLink) {
      if (rA && rA.reportHref) {
        baseLink.href = rA.reportHref;
        baseLink.hidden = false;
        baseLink.title = "Open Baseline single-run report (" + (rA.name || "") + ")";
      } else {
        baseLink.removeAttribute("href");
        baseLink.hidden = true;
      }
    }
    if (targetLink) {
      if (rB && rB.reportHref) {
        targetLink.href = rB.reportHref;
        targetLink.hidden = false;
        targetLink.title = "Open Target single-run report (" + (rB.name || "") + ")";
      } else {
        targetLink.removeAttribute("href");
        targetLink.hidden = true;
      }
    }
  }

  function renderSelectorBar() {
    var container = document.getElementById("cmp-selector-bar");
    if (!container) return;
    var runs = getRuns();
    if (!runs.length) {
      container.hidden = true;
      return;
    }
    container.hidden = false;
    var presetsHtml = runs.length >= 3
      ? '<div class="cmp-preset-group">' +
          '<button type="button" class="cmp-preset-btn" id="cmp-preset-span" title="Compare first indexed run against latest run">First → Latest</button>' +
          '<button type="button" class="cmp-preset-btn" id="cmp-preset-prev" title="Compare previous run against latest run">Prev → Latest</button>' +
        "</div>"
      : "";

    container.innerHTML =
      '<div class="cmp-run-chip">' +
        '<span class="cmp-run-badge is-base">Baseline (A)</span>' +
        '<select id="cmp-base-select" class="cmp-run-select" aria-label="Baseline run"></select>' +
        '<a id="cmp-base-link" class="cmp-run-ext-link" target="_blank" rel="noopener" hidden aria-label="Open baseline single-run report">↗</a>' +
      "</div>" +
      '<button type="button" id="cmp-swap-btn" class="cmp-swap-btn" aria-label="Swap Baseline and Target runs" title="Swap Baseline (A) and Target (B)">' +
        '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><path d="M16 3l4 4-4 4"/><path d="M20 7H4"/><path d="M8 21l-4-4 4-4"/><path d="M4 17h16"/></svg>' +
      "</button>" +
      '<div class="cmp-run-chip">' +
        '<span class="cmp-run-badge is-target">Target (B)</span>' +
        '<select id="cmp-target-select" class="cmp-run-select" aria-label="Comparison target run"></select>' +
        '<a id="cmp-target-link" class="cmp-run-ext-link" target="_blank" rel="noopener" hidden aria-label="Open target single-run report">↗</a>' +
      "</div>" +
      presetsHtml;

    var baseSel = document.getElementById("cmp-base-select");
    var targetSel = document.getElementById("cmp-target-select");
    runs.forEach(function (run, idx) {
      var label = formatRunOptionLabel(run, idx);
      var optA = document.createElement("option");
      optA.value = String(idx);
      optA.textContent = label;
      baseSel.appendChild(optA);

      var optB = document.createElement("option");
      optB.value = String(idx);
      optB.textContent = label;
      targetSel.appendChild(optB);
    });

    syncSelectorControls();

    baseSel.addEventListener("change", function () {
      setState({ baseIdx: Number(baseSel.value) });
    });
    targetSel.addEventListener("change", function () {
      setState({ targetIdx: Number(targetSel.value) });
    });

    var swapBtn = document.getElementById("cmp-swap-btn");
    if (swapBtn) {
      swapBtn.addEventListener("click", function () {
        setState({ baseIdx: state.targetIdx, targetIdx: state.baseIdx });
      });
    }

    var spanBtn = document.getElementById("cmp-preset-span");
    if (spanBtn) {
      spanBtn.addEventListener("click", function () {
        setState({ baseIdx: 0, targetIdx: runs.length - 1 });
      });
    }
    var prevBtn = document.getElementById("cmp-preset-prev");
    if (prevBtn) {
      prevBtn.addEventListener("click", function () {
        setState({ baseIdx: Math.max(0, runs.length - 2), targetIdx: runs.length - 1 });
      });
    }
  }

  function setLinkEmptyState(el, isEmpty) {
    var link = el && el.closest ? el.closest("a") : null;
    if (!link) return;
    if (isEmpty) link.classList.add("is-empty");
    else link.classList.remove("is-empty");
  }

  function populateHeaderMetadata() {
    var meta = window.XLT_META || {};
    var runs = getRuns();
    var isTrend = meta.reportKind === "trend" || runs.length > 2;
    var kindLabel = isTrend ? "Performance Trend Report" : "Performance Comparison Report";
    var titlePrefix = isTrend ? "XLT Trend Report" : "XLT Comparison Report";

    var titleEl = document.querySelector("title");
    var pageTitle = (titleEl && titleEl.getAttribute("data-page-title")) || "Overview";
    var proj = String(meta.projectName || "").trim().replace(/\s+/g, " ");
    if (proj.length >= 32) proj = proj.substring(0, 28) + "...";
    document.title = (proj ? proj + " | " : "") + titlePrefix + " - " + pageTitle;

    var projBadge = document.getElementById("meta-project-badge");
    var projSep = document.getElementById("meta-project-sep");
    if (projBadge) {
      if (meta.projectName) {
        projBadge.textContent = meta.projectName;
        projBadge.hidden = false;
        if (projSep) projSep.hidden = false;
      } else {
        projBadge.textContent = "";
        projBadge.hidden = true;
        if (projSep) projSep.hidden = true;
      }
    }

    var headingEl = document.getElementById("meta-kind-heading");
    if (headingEl) headingEl.textContent = kindLabel;

    var runsBadge = document.getElementById("meta-runs-badge");
    if (runsBadge) {
      runsBadge.textContent = runs.length + " Run" + (runs.length === 1 ? "" : "s");
      runsBadge.hidden = runs.length === 0;
    }

    var c = meta.counts || {};
    var caps = meta.capabilities || {};

    // Top nav pills
    var errPill = document.getElementById("nav-errors-pill");
    if (errPill) {
      if (caps.hasErrors === false) {
        errPill.hidden = true;
        setLinkEmptyState(errPill, true);
      } else {
        var sigs = Number(c.errors) || 0;
        errPill.textContent = fmtCompact(sigs);
        errPill.className = sigs > 0 ? "nav-pill-count fail" : "badge-tag";
        errPill.hidden = false;
        setLinkEmptyState(errPill, sigs === 0);
      }
    }

    var badgeNodes = document.querySelectorAll("[data-nav-count]");
    Array.prototype.forEach.call(badgeNodes, function (node) {
      var key = node.getAttribute("data-nav-count");
      var val = Number(c[key]) || 0;
      if (key === "events") {
        if (caps.hasEvents === false) {
          node.hidden = true;
          setLinkEmptyState(node, true);
        } else {
          node.textContent = fmtCompact(val);
          // True orange warning pill for non-zero events
          node.className = val > 0 ? "nav-pill-count warn" : "badge-tag";
          node.hidden = false;
          setLinkEmptyState(node, val === 0);
        }
      } else if (key === "hosts" || key === "responseCodes") {
        if (caps.hasNetwork === false) {
          node.hidden = true;
          setLinkEmptyState(node, true);
        } else {
          node.textContent = fmtInt(val);
          node.hidden = false;
          setLinkEmptyState(node, val === 0);
        }
      } else if (key === "properties") {
        if (caps.hasConfiguration === false) {
          node.hidden = true;
          setLinkEmptyState(node, true);
        } else {
          node.textContent = fmtInt(val) + " props";
          node.hidden = false;
          setLinkEmptyState(node, val === 0);
        }
      } else {
        node.textContent = fmtInt(val);
        node.hidden = false;
        setLinkEmptyState(node, val === 0);
      }
    });

    // Footer
    var footer = document.getElementById("report-footer");
    if (footer) {
      var divs = footer.querySelectorAll("div");
      if (divs[0]) {
        var pName = meta.productName || "Xceptance LoadTest";
        var pVer = meta.productVersion ? " " + meta.productVersion : "";
        divs[0].innerHTML =
          'Generated by <strong><a href="' + esc(meta.productUrl || "https://www.xceptance.com/en/xlt/") +
          '" target="_blank" rel="noopener" style="color:inherit;text-decoration:none">' +
          esc(pName + pVer) + "</a></strong>";
      }
      if (divs[1]) {
        divs[1].textContent = (meta.projectName ? meta.projectName + " · " : "") + kindLabel + " (" + runs.length + " Runs)";
      }
    }
  }

  function initHeaderMenus() {
    var navItems = Array.prototype.slice.call(document.querySelectorAll(".nav-item[data-has-menu]"));
    function closeAll(except) {
      navItems.forEach(function (it) {
        if (it === except) return;
        it.classList.remove("is-open", "is-pinned");
        var menu = it.querySelector(".nav-submenu");
        var btn = it.querySelector("button[aria-expanded]");
        if (menu) menu.hidden = true;
        if (btn) btn.setAttribute("aria-expanded", "false");
      });
    }
    function openItem(it, pin) {
      closeAll(it);
      var menu = it.querySelector(".nav-submenu");
      var btn = it.querySelector("button[aria-expanded]");
      if (menu) menu.hidden = false;
      if (btn) btn.setAttribute("aria-expanded", "true");
      it.classList.add("is-open");
      if (pin) it.classList.add("is-pinned");
    }

    navItems.forEach(function (it) {
      var btn = it.querySelector("button[aria-expanded]");
      var timer = null;
      it.addEventListener("mouseenter", function () {
        if (timer) { clearTimeout(timer); timer = null; }
        openItem(it, false);
      });
      it.addEventListener("mouseleave", function () {
        if (it.classList.contains("is-pinned")) return;
        timer = setTimeout(function () {
          it.classList.remove("is-open");
          var m = it.querySelector(".nav-submenu");
          if (m) m.hidden = true;
          if (btn) btn.setAttribute("aria-expanded", "false");
        }, 120);
      });
      if (btn) {
        btn.addEventListener("click", function (e) {
          e.preventDefault();
          e.stopPropagation();
          if (it.classList.contains("is-open") && it.classList.contains("is-pinned")) {
            closeAll();
          } else {
            openItem(it, true);
          }
        });
      }
      it.addEventListener("focusout", function (e) {
        var next = e.relatedTarget;
        if (!next || !it.contains(next)) {
          it.classList.remove("is-open", "is-pinned");
          var m = it.querySelector(".nav-submenu");
          if (m) m.hidden = true;
          if (btn) btn.setAttribute("aria-expanded", "false");
        }
      });
    });

    document.addEventListener("click", function (e) {
      if (!e.target.closest(".nav-item[data-has-menu]")) closeAll();
    });
    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape") closeAll();
    });
  }

  // =========================================================================
  // Delta Computation & Formatting Utilities
  // =========================================================================

  function toNum(v) {
    if (v === null || v === undefined || v === "") return null;
    var n = Number(v);
    return Number.isFinite(n) ? n : null;
  }

  function calcDelta(rawA, rawB) {
    var a = toNum(rawA);
    var b = toNum(rawB);
    if (a === null && b === null) {
      return { status: "empty", valA: null, valB: null, abs: null, rel: null, isInf: false };
    }
    if (a === null) {
      return { status: "added", valA: null, valB: b, abs: null, rel: null, isInf: false };
    }
    if (b === null) {
      return { status: "removed", valA: a, valB: null, abs: null, rel: null, isInf: false };
    }
    var abs = b - a;
    if (a === 0) {
      if (b === 0) return { status: "both", valA: 0, valB: 0, abs: 0, rel: 0, isInf: false };
      return { status: "both", valA: 0, valB: b, abs: abs, rel: abs > 0 ? Infinity : -Infinity, isInf: true };
    }
    var rel = ((b - a) / Math.abs(a)) * 100;
    return { status: "both", valA: a, valB: b, abs: abs, rel: rel, isInf: false };
  }

  function polarityClass(delta, polarity, deadbandPct) {
    if (!delta || delta.status === "empty") return "delta-flat";
    if (delta.status === "added") {
      if (polarity === "warn") return "delta-warn";
      if (polarity === "lower") return "delta-bad";
      if (polarity === "higher") return "delta-added";
      return "delta-neutral";
    }
    if (delta.status === "removed") return "delta-removed";
    if (polarity === "neutral") return "delta-neutral";
    if (polarity === "warn") {
      if (delta.abs > 0) return "delta-warn";
      if (delta.abs < 0) return "delta-good";
      return "delta-flat";
    }
    var db = deadbandPct !== undefined ? Number(deadbandPct) : 1.0;
    if (delta.abs === 0) return "delta-flat";
    if (!delta.isInf && Math.abs(delta.rel) < db) return "delta-flat";
    var improved = polarity === "higher" ? delta.abs > 0 : delta.abs < 0;
    return improved ? "delta-good" : "delta-bad";
  }

  function formatPct(delta) {
    if (!delta || delta.status === "empty") return "—";
    if (delta.status === "added") return "Added";
    if (delta.status === "removed") return "Removed";
    if (delta.isInf) return delta.abs > 0 ? "+∞" : "-∞";
    var r = delta.rel;
    if (Math.abs(r) < 0.05) return "0.0%";
    return (r > 0 ? "+" : "") + r.toFixed(1) + "%";
  }

  function formatAbsDelta(delta, fmtFn, unit) {
    if (!delta || delta.status !== "both") return "—";
    var u = unit || "";
    if (delta.abs === 0) return "0" + (u ? " " + u : "");
    var sign = delta.abs > 0 ? "+" : "-";
    return sign + fmtFn(Math.abs(delta.abs)) + (u ? " " + u : "");
  }

  function buildComparisonCellData(delta, fmtFn, unit, polarity, viewMode, opts) {
    opts = opts || {};
    var u = unit ? " " + unit : "";
    var deadband = opts.deadband !== undefined ? opts.deadband : (polarity === "lower" ? 1.0 : 1.0);
    var isEvent = Boolean(opts.isEvent);
    var isError = Boolean(opts.isError);
    var preferAbsPill = Boolean(opts.absPill);

    if (!delta || delta.status === "empty") {
      return { html: '<span class="cell-muted">—</span>', title: "", copyText: "—", tdClass: "" };
    }

    var pClass = polarityClass(delta, polarity, deadband);
    var targetCls = "cmp-cell-target";
    if (isEvent && delta.valB > 0) targetCls += " cell-warn";
    else if (isError && delta.valB > 0 && delta.abs > 0) targetCls += " cell-err";

    if (delta.status === "added") {
      var bStr = fmtFn(delta.valB) + u;
      var addedTitle = "Not in Baseline → " + bStr + " (Added)";
      var addedCopy = "— → " + bStr + " (Added)";
      if (viewMode === "pct") {
        return {
          html: '<span class="cmp-delta-pill ' + pClass + '">Added</span>',
          title: addedTitle,
          copyText: addedCopy,
          tdClass: ""
        };
      }
      return {
        html:
          '<span class="cmp-cell">' +
            '<span class="cmp-cell-base">—</span>' +
            '<span class="cmp-cell-arrow">→</span>' +
            '<span class="' + targetCls + '">' + esc(bStr) + "</span>" +
            '<span class="cmp-delta-pill ' + pClass + '">Added</span>' +
          "</span>",
        title: addedTitle,
        copyText: addedCopy,
        tdClass: ""
      };
    }

    if (delta.status === "removed") {
      var aStr = fmtFn(delta.valA) + u;
      var removedTitle = aStr + " → Not in Target (Removed)";
      var removedCopy = aStr + " → — (Removed)";
      if (viewMode === "pct") {
        return {
          html: '<span class="cmp-delta-pill delta-removed">Removed</span>',
          title: removedTitle,
          copyText: removedCopy,
          tdClass: ""
        };
      }
      return {
        html:
          '<span class="cmp-cell">' +
            '<span class="cmp-cell-base">' + esc(aStr) + "</span>" +
            '<span class="cmp-cell-arrow">→</span>' +
            '<span class="cmp-cell-target cell-muted">—</span>' +
            '<span class="cmp-delta-pill delta-removed">Removed</span>' +
          "</span>",
        title: removedTitle,
        copyText: removedCopy,
        tdClass: ""
      };
    }

    var sA = fmtFn(delta.valA);
    var sB = fmtFn(delta.valB);
    var absStr = formatAbsDelta(delta, fmtFn, unit);
    var pctStr = formatPct(delta);
    var pillText = preferAbsPill ? absStr : pctStr;
    var titleText = sA + u + " → " + sB + u + " (" + absStr + ", " + pctStr + ")";

    if (viewMode === "pct") {
      return {
        html: '<span class="cmp-delta-pill ' + pClass + '">' + esc(pillText) + "</span>",
        title: titleText,
        copyText: pillText + " (" + sA + u + " → " + sB + u + ")",
        tdClass: ""
      };
    }

    return {
      html:
        '<span class="cmp-cell">' +
          '<span class="cmp-cell-base">' + esc(sA) + "</span>" +
          '<span class="cmp-cell-arrow">→</span>' +
          '<span class="' + targetCls + '">' + esc(sB + u) + "</span>" +
          '<span class="cmp-delta-pill ' + pClass + '">' + esc(pillText) + "</span>" +
        "</span>",
      title: titleText,
      copyText: sA + u + " → " + sB + u + " (" + pillText + ")",
      tdClass: ""
    };
  }

  // =========================================================================
  // Per-Column Numeric Track Alignment (Zero-Jitter Comparison Columns)
  // =========================================================================

  var alignUidSeq = 0;
  var alignedTables = new Map(); // uid -> { el: HTMLTableElement, css: string }
  var alignedLists = new Set();  // Set<HTMLElement>
  var alignStyleEl = null;
  var alignFontsHooked = false;

  function ensureAlignStyleEl() {
    if (alignStyleEl && alignStyleEl.isConnected) return alignStyleEl;
    if (typeof document === "undefined") return null;
    alignStyleEl = document.getElementById("xlt-cmp-align-styles");
    if (!alignStyleEl) {
      alignStyleEl = document.createElement("style");
      alignStyleEl.id = "xlt-cmp-align-styles";
      (document.head || document.documentElement).appendChild(alignStyleEl);
    }
    return alignStyleEl;
  }

  function flushAlignStyles() {
    var el = ensureAlignStyleEl();
    if (!el) return;
    var chunks = [];
    alignedTables.forEach(function (entry, uid) {
      if (!entry.el || !entry.el.isConnected) {
        alignedTables.delete(uid);
        return;
      }
      if (entry.css) chunks.push(entry.css);
    });
    var nextCss = chunks.join("\n");
    if (el.textContent !== nextCss) el.textContent = nextCss;
  }

  function hookFontsReadyAlignment() {
    if (alignFontsHooked || typeof document === "undefined" || !document.fonts || !document.fonts.ready) return;
    alignFontsHooked = true;
    document.fonts.ready.then(function () {
      var tables = [];
      alignedTables.forEach(function (entry, uid) {
        if (entry.el && entry.el.isConnected) tables.push(entry.el);
        else alignedTables.delete(uid);
      });
      if (tables.length) syncTablesAlignment(tables);
      alignedLists.forEach(function (listEl) {
        if (listEl && listEl.isConnected) syncListAlignment(listEl);
        else alignedLists.delete(listEl);
      });
    });
  }

  function collectVisibleDataRows(table) {
    var rows = [];
    var pushSection = function (sec) {
      if (!sec || sec.hidden) return;
      for (var i = 0; i < sec.rows.length; i++) {
        var tr = sec.rows[i];
        if (!tr || tr.hidden || tr.classList.contains("drawer-row") || tr.style.display === "none") continue;
        rows.push(tr);
      }
    };
    for (var b = 0; b < table.tBodies.length; b++) pushSection(table.tBodies[b]);
    if (table.tFoot) pushSection(table.tFoot);
    return rows;
  }

  function measureTableColumns(table) {
    var rows = collectVisibleDataRows(table);
    if (!rows.length) return "";
    var uid = table.getAttribute("data-cmp-align-id");
    var isSubtable = table.classList.contains("cmp-subtable");
    var cols = {}; // colPos -> { base: 0, target: 0, pill: 0 }

    function ensureCol(colPos) {
      var c = cols[colPos];
      if (!c) { c = { base: 0, target: 0, pill: 0 }; cols[colPos] = c; }
      return c;
    }

    if (rows.length <= 200) {
      for (var r = 0; r < rows.length; r++) {
        var cells = rows[r].cells;
        for (var i = 0; i < cells.length; i++) {
          var td = cells[i];
          if (!td || td.colSpan > 1) continue;
          var first = td.firstElementChild;
          if (!first) continue;
          if (first.classList.contains("cmp-cell")) {
            var c = ensureCol(i + 1);
            var bEl = first.querySelector(".cmp-cell-base");
            var tEl = first.querySelector(".cmp-cell-target");
            var pEl = first.querySelector(".cmp-delta-pill");
            if (bEl) { var bw = bEl.getBoundingClientRect().width; if (bw > c.base) c.base = bw; }
            if (tEl) { var tw = tEl.getBoundingClientRect().width; if (tw > c.target) c.target = tw; }
            if (pEl) { var pw = pEl.getBoundingClientRect().width; if (pw > c.pill) c.pill = pw; }
          } else if (first.classList.contains("cmp-delta-pill") && (td.classList.contains("col-num") || isSubtable)) {
            var cp = ensureCol(i + 1);
            var spw = first.getBoundingClientRect().width;
            if (spw > cp.pill) cp.pill = spw;
          }
        }
      }
    } else {
      // Large-table optimization: pre-filter by text length so we only read layout on widest candidates + bold rows
      var cands = {}; // colPos -> { bMax, tMax, pMax, b: [], t: [], p: [] }
      function ensureCand(colPos) {
        var o = cands[colPos];
        if (!o) { o = { bMax: 0, tMax: 0, pMax: 0, b: [], t: [], p: [] }; cands[colPos] = o; }
        return o;
      }
      for (var r2 = 0; r2 < rows.length; r2++) {
        var tr = rows[r2];
        var rowBold = tr.classList.contains("is-aggregate-row") || (tr.parentNode && tr.parentNode.tagName === "TFOOT");
        var cells2 = tr.cells;
        for (var i2 = 0; i2 < cells2.length; i2++) {
          var td2 = cells2[i2];
          if (!td2 || td2.colSpan > 1) continue;
          var first2 = td2.firstElementChild;
          if (!first2) continue;
          if (first2.classList.contains("cmp-cell")) {
            var co = ensureCand(i2 + 1);
            var bEl2 = first2.querySelector(".cmp-cell-base");
            var tEl2 = first2.querySelector(".cmp-cell-target");
            var pEl2 = first2.querySelector(".cmp-delta-pill");
            if (bEl2) {
              var bl = (bEl2.textContent || "").length;
              if (bl > co.bMax) co.bMax = bl;
              co.b.push({ el: bEl2, len: bl, bold: rowBold });
            }
            if (tEl2) {
              var tl = (tEl2.textContent || "").length;
              if (tl > co.tMax) co.tMax = tl;
              co.t.push({ el: tEl2, len: tl, bold: rowBold || tEl2.classList.contains("cell-err") || tEl2.classList.contains("cell-warn") });
            }
            if (pEl2) {
              var pl = (pEl2.textContent || "").length;
              if (pl > co.pMax) co.pMax = pl;
              co.p.push({ el: pEl2, len: pl, bold: rowBold });
            }
          } else if (first2.classList.contains("cmp-delta-pill") && (td2.classList.contains("col-num") || isSubtable)) {
            var co2 = ensureCand(i2 + 1);
            var spl = (first2.textContent || "").length;
            if (spl > co2.pMax) co2.pMax = spl;
            co2.p.push({ el: first2, len: spl, bold: rowBold });
          }
        }
      }
      Object.keys(cands).forEach(function (k) {
        var co = cands[k];
        var col = ensureCol(k);
        for (var bi = 0; bi < co.b.length; bi++) {
          if (co.b[bi].bold || co.b[bi].len >= co.bMax - 3) {
            var w = co.b[bi].el.getBoundingClientRect().width;
            if (w > col.base) col.base = w;
          }
        }
        for (var ti = 0; ti < co.t.length; ti++) {
          if (co.t[ti].bold || co.t[ti].len >= co.tMax - 3) {
            var w2 = co.t[ti].el.getBoundingClientRect().width;
            if (w2 > col.target) col.target = w2;
          }
        }
        for (var pi = 0; pi < co.p.length; pi++) {
          if (co.p[pi].bold || co.p[pi].len >= co.pMax - 3) {
            var w3 = co.p[pi].el.getBoundingClientRect().width;
            if (w3 > col.pill) col.pill = w3;
          }
        }
      });
    }

    var rules = [];
    Object.keys(cols).forEach(function (colPos) {
      var c = cols[colPos];
      var decls = [];
      if (c.base > 0) decls.push("--cmp-base-w:" + Math.ceil(c.base) + "px");
      if (c.target > 0) decls.push("--cmp-target-w:" + Math.ceil(c.target) + "px");
      if (c.pill > 0) decls.push("--cmp-pill-w:" + Math.ceil(c.pill) + "px");
      if (decls.length) {
        rules.push('table[data-cmp-align-id="' + uid + '"] tr:not(.drawer-row)>td:nth-child(' + colPos + "){" + decls.join(";") + "}");
      }
    });
    return rules.join("\n");
  }

  function syncTablesAlignment(tableOrList) {
    if (typeof document === "undefined" || !tableOrList) return;
    hookFontsReadyAlignment();
    var raw = (Array.isArray(tableOrList) || (typeof tableOrList.length === "number" && !tableOrList.tagName))
      ? Array.prototype.slice.call(tableOrList)
      : [tableOrList];
    var targets = [];
    raw.forEach(function (t) {
      if (!t || !t.isConnected || t.getClientRects().length === 0) return;
      if (targets.indexOf(t) === -1) targets.push(t);
    });

    // Phase 1: clear previous constraints for target tables in a single write
    targets.forEach(function (t) {
      var uid = t.getAttribute("data-cmp-align-id");
      if (!uid) {
        uid = "cmp-t-" + (++alignUidSeq);
        t.setAttribute("data-cmp-align-id", uid);
      }
      alignedTables.set(uid, { el: t, css: "" });
    });
    flushAlignStyles();
    if (!targets.length) return;

    // Phase 2: single read-only measurement pass
    var measured = targets.map(function (t) {
      return { uid: t.getAttribute("data-cmp-align-id"), el: t, css: measureTableColumns(t) };
    });

    // Phase 3: single write pass
    measured.forEach(function (m) {
      alignedTables.set(m.uid, { el: m.el, css: m.css });
    });
    flushAlignStyles();
  }

  function syncTableAlignment(table) {
    if (table) syncTablesAlignment([table]);
  }

  function syncListAlignment(container) {
    if (typeof document === "undefined" || !container || !container.isConnected || container.getClientRects().length === 0) return;
    hookFontsReadyAlignment();
    alignedLists.add(container);
    container.style.removeProperty("--cmp-base-w");
    container.style.removeProperty("--cmp-target-w");
    container.style.removeProperty("--cmp-pill-w");

    var cells = container.querySelectorAll(".cmp-cell");
    if (!cells.length) return;
    var maxBase = 0, maxTarget = 0, maxPill = 0;
    for (var i = 0; i < cells.length; i++) {
      var cell = cells[i];
      var b = cell.querySelector(".cmp-cell-base");
      var t = cell.querySelector(".cmp-cell-target");
      var p = cell.querySelector(".cmp-delta-pill");
      if (b) { var bw = b.getBoundingClientRect().width; if (bw > maxBase) maxBase = bw; }
      if (t) { var tw = t.getBoundingClientRect().width; if (tw > maxTarget) maxTarget = tw; }
      if (p) { var pw = p.getBoundingClientRect().width; if (pw > maxPill) maxPill = pw; }
    }
    if (maxBase > 0) container.style.setProperty("--cmp-base-w", Math.ceil(maxBase) + "px");
    if (maxTarget > 0) container.style.setProperty("--cmp-target-w", Math.ceil(maxTarget) + "px");
    if (maxPill > 0) container.style.setProperty("--cmp-pill-w", Math.ceil(maxPill) + "px");
  }

  // =========================================================================
  // Shared Run Trajectory Series Selector & ECharts Builder
  // =========================================================================

  var TRAJ_PCT_LIGHT = ["#0d9488", "#059669", "#4f46e5", "#9333ea", "#0284c7", "#db2777", "#65a30d", "#b45309"];
  var TRAJ_PCT_DARK  = ["#2dd4bf", "#34d399", "#818cf8", "#c084fc", "#38bdf8", "#f472b6", "#a3e635", "#f59e0b"];

  function trajStorageKey() {
    return dirStorageKey() + ":trajSeries";
  }

  function loadTrajSelection() {
    try {
      var raw = sessionStorage.getItem(trajStorageKey());
      if (raw) {
        var parsed = JSON.parse(raw);
        if (parsed && typeof parsed === "object") return parsed;
      }
    } catch (_) { /* ignore file:// storage restrictions */ }
    return {};
  }

  var trajSelection = loadTrajSelection();

  function persistTrajSelection() {
    try {
      sessionStorage.setItem(trajStorageKey(), JSON.stringify(trajSelection));
    } catch (_) { /* ignore */ }
  }

  function getTrajectorySelection() {
    return Object.assign({}, trajSelection);
  }

  function setTrajectorySelection(patch) {
    if (!patch || typeof patch !== "object") return;
    Object.keys(patch).forEach(function (k) {
      if (patch[k] === undefined || patch[k] === null) delete trajSelection[k];
      else trajSelection[k] = Boolean(patch[k]);
    });
    persistTrajSelection();
  }

  function resetTrajectorySelection() {
    trajSelection = {};
    persistTrajSelection();
  }

  function sortPercentileKeys(pcts) {
    var seen = [];
    (pcts || []).forEach(function (p) {
      var s = String(p == null ? "" : p).trim();
      if (s && seen.indexOf(s) === -1) seen.push(s);
    });
    return seen.sort(function (a, b) {
      var na = parseFloat(a);
      var nb = parseFloat(b);
      if (!Number.isNaN(na) && !Number.isNaN(nb) && na !== nb) return na - nb;
      return a.localeCompare(b);
    });
  }

  function isDefaultPercentileOn(pctKey, sortedPcts) {
    if (sortedPcts.length <= 4) return true;
    var prefs = ["50", "90", "95", "99"];
    if (prefs.indexOf(pctKey) !== -1) return true;
    var idx = sortedPcts.indexOf(pctKey);
    return idx === 0 || idx === sortedPcts.length - 1;
  }

  function resolveSeriesActive(key, defaultOn) {
    if (Object.prototype.hasOwnProperty.call(trajSelection, key)) {
      return Boolean(trajSelection[key]);
    }
    return Boolean(defaultOn);
  }

  function buildTrajectoryConfig(opts) {
    opts = opts || {};
    var runs = Array.isArray(opts.runs) ? opts.runs : [];
    var runMetrics = Array.isArray(opts.runMetrics) ? opts.runMetrics : [];
    var sortedPcts = sortPercentileKeys(opts.pcts);
    var dark = Boolean(opts.dark);
    var valueUnit = opts.valueUnit !== undefined ? opts.valueUnit : "ms";
    var valueFmt = opts.valueFmt || fmtInt;
    var fmtDec = UI.fmtDec || function (n, d) {
      if (n === null || n === undefined || Number.isNaN(Number(n))) return "—";
      return Number(n).toFixed(d === undefined ? 2 : d);
    };
    var baseIdx = opts.baseIdx !== undefined ? opts.baseIdx : state.baseIdx;
    var targetIdx = opts.targetIdx !== undefined ? opts.targetIdx : state.targetIdx;
    var showErrors = opts.showErrors !== false;
    var showEvents = opts.showEvents !== false;
    var useHeadline = Boolean(opts.useHeadline);

    var pal = {
      text: dark ? "#e4e4e7" : "#18181b",
      muted: dark ? "#a1a1aa" : "#666e7a",
      split: dark ? "#2e2e36" : "#e5e7eb",
      axis: dark ? "#3f3f46" : "#cbd5e1",
      tipBg: dark ? "rgba(28, 28, 31, 0.96)" : "rgba(255, 255, 255, 0.97)",
      tipBorder: dark ? "#3f3f46" : "#c9d0d8",
      mean: dark ? "#fb923c" : "#e07000",
      median: dark ? "#2dd4bf" : "#0d9488",
      rateBar: dark ? "rgba(96, 165, 250, 0.28)" : "rgba(36, 75, 115, 0.22)",
      rateSwatch: dark ? "#60a5fa" : "#244b73",
      err: dark ? "#f87171" : "#dc2626",
      ev: dark ? "#fbbf24" : "#d97706",
      bandFill: dark ? "rgba(148, 163, 184, 0.20)" : "rgba(148, 163, 184, 0.24)",
      bandStroke: dark ? "#94a3b8" : "#64748b",
      pcts: dark ? TRAJ_PCT_DARK : TRAJ_PCT_LIGHT
    };

    var runLabels = runs.map(function (r, idx) {
      var tag = idx === baseIdx ? " [A]" : (idx === targetIdx ? " [B]" : "");
      var name = useHeadline
        ? (r && (r.commentHeadline || r.name) || ("Run " + (idx + 1)))
        : (r && r.name || ("Run " + (idx + 1)));
      return "#" + (idx + 1) + " " + name + tag;
    });

    var readVal = function (idx, field) {
      var rm = runMetrics[idx];
      if (!rm || rm[field] === undefined || rm[field] === null) return null;
      var n = Number(rm[field]);
      return Number.isFinite(n) ? n : null;
    };
    var readPct = function (idx, pctKey) {
      var rm = runMetrics[idx];
      if (!rm || !UI.getPercentile) return null;
      var v = UI.getPercentile(rm, pctKey);
      return v === undefined || v === null || !Number.isFinite(Number(v)) ? null : Number(v);
    };
    var hasAny = function (arr) {
      return arr.some(function (v) { return v !== null; });
    };

    var meanVals = runs.map(function (_, i) { return readVal(i, "mean"); });
    var medianVals = runs.map(function (_, i) { return readVal(i, "median"); });
    var minVals = runs.map(function (_, i) { return readVal(i, "min"); });
    var maxVals = runs.map(function (_, i) { return readVal(i, "max"); });
    var rateVals = runs.map(function (_, i) { return readVal(i, "rate"); });
    var errVals = showErrors ? runs.map(function (_, i) { return readVal(i, "errors"); }) : [];
    var evVals = showEvents ? runs.map(function (_, i) { return readVal(i, "events"); }) : [];

    var seriesDefs = [];
    var activeRuntime = false;
    var activeRate = false;
    var activeErrors = false;
    var activeEvents = false;
    var echartsSeries = [];
    var seriesMetaByName = {};

    // 1. Min-Max Band (plotted first so it sits cleanly behind curves)
    var hasMinMax = runs.some(function (_, i) { return minVals[i] !== null && maxVals[i] !== null; });
    var minMaxActive = hasMinMax && resolveSeriesActive("minmax", false);
    if (hasMinMax) {
      seriesDefs.push({
        key: "minmax",
        label: "Min–Max",
        group: "latency",
        color: pal.bandStroke,
        swatch: "band",
        active: minMaxActive
      });
      if (minMaxActive) {
        activeRuntime = true;
        var bandSpan = runs.map(function (_, i) {
          return (minVals[i] !== null && maxVals[i] !== null) ? Math.max(0, maxVals[i] - minVals[i]) : null;
        });
        echartsSeries.push({
          name: "__min_base__",
          type: "line",
          stack: "mm",
          yAxisIndex: 0,
          data: minVals,
          symbol: "none",
          lineStyle: { opacity: 0 },
          areaStyle: { opacity: 0 },
          silent: true,
          tooltip: { show: false }
        });
        echartsSeries.push({
          name: "Min–Max",
          type: "line",
          stack: "mm",
          yAxisIndex: 0,
          data: bandSpan,
          symbol: "none",
          lineStyle: { width: 1, type: "dashed", color: pal.bandStroke, opacity: 0.65 },
          itemStyle: { color: pal.bandStroke },
          areaStyle: { color: pal.bandFill }
        });
        seriesMetaByName["Min–Max"] = { kind: "minmax", color: pal.bandStroke };
      }
    }

    // 2. Throughput bars (behind line curves)
    var hasRate = hasAny(rateVals);
    var rateActive = hasRate && resolveSeriesActive("rate", true);
    if (hasRate) {
      seriesDefs.push({
        key: "rate",
        label: "1/s",
        group: "volume",
        color: pal.rateSwatch,
        swatch: "bar",
        active: rateActive
      });
      if (rateActive) {
        activeRate = true;
        echartsSeries.push({
          name: "Throughput (1/s)",
          type: "bar",
          yAxisIndex: 1,
          barMaxWidth: 28,
          data: rateVals,
          itemStyle: { color: pal.rateBar, borderColor: pal.rateSwatch, borderWidth: 0.5, borderRadius: [3, 3, 0, 0] }
        });
        seriesMetaByName["Throughput (1/s)"] = { kind: "rate", color: pal.rateSwatch };
      }
    }

    // 3. Mean Runtime / Value
    var hasMean = hasAny(meanVals);
    var meanActive = hasMean && resolveSeriesActive("mean", true);
    if (hasMean) {
      seriesDefs.push({
        key: "mean",
        label: "Mean",
        group: "latency",
        color: pal.mean,
        swatch: "line",
        active: meanActive
      });
      if (meanActive) {
        activeRuntime = true;
        echartsSeries.push({
          name: "Mean",
          type: "line",
          yAxisIndex: 0,
          data: meanVals,
          smooth: 0.12,
          symbolSize: 6,
          lineStyle: { width: 2.5, color: pal.mean },
          itemStyle: { color: pal.mean },
          z: 5
        });
        seriesMetaByName["Mean"] = { kind: "value", color: pal.mean };
      }
    }

    // 4. Median (only when P50 is not already configured in percentiles)
    var hasP50 = sortedPcts.indexOf("50") !== -1;
    var hasMedian = !hasP50 && hasAny(medianVals);
    var medianActive = hasMedian && resolveSeriesActive("median", true);
    if (hasMedian) {
      seriesDefs.push({
        key: "median",
        label: "Median",
        group: "latency",
        color: pal.median,
        swatch: "line",
        active: medianActive
      });
      if (medianActive) {
        activeRuntime = true;
        echartsSeries.push({
          name: "Median",
          type: "line",
          yAxisIndex: 0,
          data: medianVals,
          smooth: 0.12,
          symbolSize: 5,
          lineStyle: { width: 2, color: pal.median },
          itemStyle: { color: pal.median },
          z: 4
        });
        seriesMetaByName["Median"] = { kind: "value", color: pal.median };
      }
    }

    // 5. ALL Configured Percentiles (P50, P90, P95, P99, P99.9, ...)
    sortedPcts.forEach(function (pctKey, pIdx) {
      var pVals = runs.map(function (_, i) { return readPct(i, pctKey); });
      if (!hasAny(pVals)) return;
      var key = "pct:" + pctKey;
      var label = "P" + pctKey;
      var color = pal.pcts[pIdx % pal.pcts.length];
      var active = resolveSeriesActive(key, isDefaultPercentileOn(pctKey, sortedPcts));
      seriesDefs.push({
        key: key,
        label: label,
        group: "latency",
        color: color,
        swatch: "line",
        active: active
      });
      if (active) {
        activeRuntime = true;
        echartsSeries.push({
          name: label,
          type: "line",
          yAxisIndex: 0,
          data: pVals,
          smooth: 0.12,
          symbolSize: 5,
          lineStyle: { width: 1.9, color: color },
          itemStyle: { color: color },
          z: 4
        });
        seriesMetaByName[label] = { kind: "value", color: color };
      }
    });

    // Reorder latency seriesDefs so Min-Max sits at the end of the Latency group (after Mean & all Pxx)
    var latencyDefs = seriesDefs.filter(function (d) { return d.group === "latency" && d.key !== "minmax"; });
    var minMaxDef = seriesDefs.find(function (d) { return d.key === "minmax"; });
    if (minMaxDef) latencyDefs.push(minMaxDef);
    var volDefs = seriesDefs.filter(function (d) { return d.group === "volume"; });

    // 6. Errors
    var hasErrors = showErrors && hasAny(errVals);
    var hasNonZeroErrors = hasErrors && errVals.some(function (v) { return v !== null && v > 0; });
    var errorsActive = hasErrors && resolveSeriesActive("errors", hasNonZeroErrors);
    if (hasErrors) {
      volDefs.push({
        key: "errors",
        label: "Errors",
        group: "volume",
        color: pal.err,
        swatch: "dashed",
        active: errorsActive
      });
      if (errorsActive) {
        activeErrors = true;
        echartsSeries.push({
          name: "Errors",
          type: "line",
          yAxisIndex: 2,
          data: errVals,
          smooth: 0.12,
          symbol: "circle",
          symbolSize: 6,
          lineStyle: { width: 2, type: "dashed", color: pal.err },
          itemStyle: { color: pal.err },
          z: 6
        });
        seriesMetaByName["Errors"] = { kind: "count", color: pal.err };
      }
    }

    // 7. Events (shown when non-zero events occur in at least one run)
    var hasEvents = showEvents && evVals.some(function (v) { return v !== null && v > 0; });
    var eventsActive = hasEvents && resolveSeriesActive("events", false);
    if (hasEvents) {
      volDefs.push({
        key: "events",
        label: "Events",
        group: "volume",
        color: pal.ev,
        swatch: "dotted",
        active: eventsActive
      });
      if (eventsActive) {
        activeEvents = true;
        echartsSeries.push({
          name: "Events",
          type: "line",
          yAxisIndex: 2,
          data: evVals,
          smooth: 0.12,
          symbol: "diamond",
          symbolSize: 6,
          lineStyle: { width: 2, type: "dotted", color: pal.ev },
          itemStyle: { color: pal.ev },
          z: 6
        });
        seriesMetaByName["Events"] = { kind: "count", color: pal.ev };
      }
    }

    var orderedDefs = latencyDefs.concat(volDefs);
    var activeCountAxis = activeErrors || activeEvents;
    var countAxisName = (activeErrors && activeEvents) ? "Errors / Events" : (activeEvents ? "Events" : "Errors");
    var countAxisColor = (!activeErrors && activeEvents) ? pal.ev : pal.err;
    var runtimeAxisName = valueUnit ? "Runtime [" + valueUnit + "]" : "Value";
    var unitSuffix = valueUnit ? " " + valueUnit : "";

    var gridLeft = activeRuntime ? 68 : 24;
    var gridRight = (activeRate && activeCountAxis) ? 114 : ((activeRate || activeCountAxis) ? 62 : 24);
    var splitOnRuntime = activeRuntime;
    var splitOnRate = !activeRuntime && activeRate;
    var splitOnCount = !activeRuntime && !activeRate && activeCountAxis;

    var echartOption = {
      animationDuration: 180,
      tooltip: {
        trigger: "axis",
        backgroundColor: pal.tipBg,
        borderColor: pal.tipBorder,
        borderWidth: 1,
        textStyle: { color: pal.text, fontSize: 12 },
        formatter: function (params) {
          if (!params || !params.length) return "";
          var idx = params[0].dataIndex;
          var header = '<div style="font-weight:700;margin-bottom:4px">' + esc(runLabels[idx] || params[0].axisValue || "") + "</div>";
          var rowsHtml = "";
          params.forEach(function (p) {
            if (!p || p.seriesName === "__min_base__") return;
            var meta = seriesMetaByName[p.seriesName] || {};
            var valTxt = "—";
            if (meta.kind === "minmax") {
              if (minVals[idx] !== null && maxVals[idx] !== null) {
                valTxt = valueFmt(minVals[idx]) + " – " + valueFmt(maxVals[idx]) + unitSuffix;
              }
            } else if (p.value !== null && p.value !== undefined) {
              if (meta.kind === "rate") valTxt = fmtDec(p.value, 2) + " /s";
              else if (meta.kind === "count") valTxt = fmtInt(p.value);
              else valTxt = valueFmt(p.value) + unitSuffix;
            }
            rowsHtml +=
              '<div style="display:flex;align-items:center;justify-content:space-between;gap:14px;font-variant-numeric:tabular-nums">' +
                "<span>" + p.marker + " " + esc(p.seriesName) + "</span>" +
                "<strong>" + esc(valTxt) + "</strong>" +
              "</div>";
          });
          return header + rowsHtml;
        }
      },
      legend: { show: false },
      grid: { left: gridLeft, right: gridRight, top: 26, bottom: 32 },
      xAxis: {
        type: "category",
        data: runLabels,
        axisLine: { lineStyle: { color: pal.axis } },
        axisLabel: { color: pal.muted, fontSize: 11, overflow: "truncate", width: 130 }
      },
      yAxis: [
        {
          type: "value",
          position: "left",
          show: activeRuntime,
          name: runtimeAxisName,
          nameTextStyle: { color: pal.muted, fontSize: 10 },
          axisLabel: { color: pal.muted, fontSize: 11 },
          splitLine: { show: splitOnRuntime, lineStyle: { color: pal.split } }
        },
        {
          type: "value",
          position: "right",
          offset: 0,
          show: activeRate,
          name: "1/s",
          nameTextStyle: { color: pal.rateSwatch, fontSize: 10 },
          axisLabel: { color: pal.rateSwatch, fontSize: 11 },
          splitLine: { show: splitOnRate, lineStyle: { color: pal.split } }
        },
        {
          type: "value",
          position: "right",
          offset: activeRate ? 56 : 0,
          show: activeCountAxis,
          minInterval: 1,
          name: countAxisName,
          nameTextStyle: { color: countAxisColor, fontSize: 10 },
          axisLabel: { color: countAxisColor, fontSize: 11 },
          splitLine: { show: splitOnCount, lineStyle: { color: pal.split } }
        }
      ],
      series: echartsSeries,
      graphic: echartsSeries.length ? [] : [{
        type: "text",
        left: "center",
        top: "middle",
        style: {
          text: "Select one or more series above to plot",
          fill: pal.muted,
          fontSize: 12,
          fontFamily: "Roboto, sans-serif"
        }
      }]
    };

    return { seriesDefs: orderedDefs, echartOption: echartOption };
  }

  function syncToolbarTabIndex(toolbarEl) {
    if (!toolbarEl || Object.prototype.hasOwnProperty.call(HTMLElement.prototype, "focusGroup")) {
      return;
    }
    var items = Array.prototype.slice.call(
      toolbarEl.querySelectorAll('button:not([disabled]):not([hidden]), a[href]:not([hidden])')
    ).filter(function (el) {
      return !el.hidden && !(el.closest && el.closest("[hidden]"));
    });
    if (!items.length) return;
    var active = document.activeElement;
    var keepIdx = items.indexOf(active);
    var idx0 = keepIdx >= 0 ? keepIdx : 0;
    items.forEach(function (el, i) { el.tabIndex = i === idx0 ? 0 : -1; });
  }

  function renderTrajectoryToolbar(barEl, seriesDefs, opts) {
    if (!barEl) return;
    opts = opts || {};
    var onChange = typeof opts.onChange === "function" ? opts.onChange : function () {};
    var statusEl = opts.statusEl || null;

    if (!seriesDefs || !seriesDefs.length) {
      barEl.hidden = true;
      return;
    }
    barEl.hidden = false;

    var sig = seriesDefs.map(function (d) { return d.key; }).join("|");
    var prevSig = barEl.getAttribute("data-traj-sig");

    if (prevSig === sig) {
      // Update aria-pressed and swatch colors in-place so keyboard focus is preserved
      seriesDefs.forEach(function (d) {
        var btn = barEl.querySelector('button[data-traj-key="' + d.key + '"]');
        if (!btn) return;
        btn.setAttribute("aria-pressed", d.active ? "true" : "false");
        btn.style.setProperty("--swatch-color", d.color);
      });
    } else {
      var latDefs = seriesDefs.filter(function (d) { return d.group === "latency"; });
      var volDefs = seriesDefs.filter(function (d) { return d.group === "volume"; });
      var renderChip = function (d) {
        return (
          '<button type="button" class="cmp-series-btn" data-traj-key="' + esc(d.key) +
          '" aria-pressed="' + (d.active ? "true" : "false") +
          '" style="--swatch-color:' + esc(d.color) + '">' +
            '<span class="cmp-series-swatch is-' + esc(d.swatch || "line") + '" aria-hidden="true"></span>' +
            "<span>" + esc(d.label) + "</span>" +
          "</button>"
        );
      };

      var parts = ['<span class="cmp-series-label" aria-hidden="true">Series</span>'];
      if (latDefs.length) {
        parts.push('<div role="group" class="cmp-series-group" aria-label="Latency series">' + latDefs.map(renderChip).join("") + "</div>");
      }
      if (latDefs.length && volDefs.length) {
        parts.push('<span class="cmp-toolbar-sep" role="separator" aria-orientation="vertical"></span>');
      }
      if (volDefs.length) {
        parts.push('<div role="group" class="cmp-series-group" aria-label="Throughput and reliability series">' + volDefs.map(renderChip).join("") + "</div>");
      }
      parts.push('<span class="cmp-toolbar-sep" role="separator" aria-orientation="vertical"></span>');
      parts.push(
        '<div role="group" class="cmp-series-group" aria-label="Series presets">' +
          '<button type="button" class="cmp-series-action" data-traj-action="all" title="Show all available series">All</button>' +
          '<button type="button" class="cmp-series-action" data-traj-action="reset" title="Restore default series">Reset</button>' +
        "</div>"
      );

      barEl.innerHTML = parts.join("");
      barEl.setAttribute("data-traj-sig", sig);
      syncToolbarTabIndex(barEl);
    }

    barEl._trajDefs = seriesDefs;
    barEl._trajOnChange = onChange;
    barEl._trajStatusEl = statusEl;

    if (!barEl.hasAttribute("data-traj-bound")) {
      barEl.setAttribute("data-traj-bound", "1");
      barEl.addEventListener("click", function (e) {
        var btn = e.target.closest("button[data-traj-key], button[data-traj-action]");
        if (!btn) return;
        var key = btn.getAttribute("data-traj-key");
        var action = btn.getAttribute("data-traj-action");
        var defs = barEl._trajDefs || [];
        var stEl = barEl._trajStatusEl;

        if (key) {
          var nextOn = btn.getAttribute("aria-pressed") !== "true";
          var patch = {};
          patch[key] = nextOn;
          setTrajectorySelection(patch);
        } else if (action === "all") {
          var allPatch = {};
          defs.forEach(function (d) { allPatch[d.key] = true; });
          setTrajectorySelection(allPatch);
          if (stEl) stEl.textContent = "All trajectory series shown";
        } else if (action === "reset") {
          resetTrajectorySelection();
          if (stEl) stEl.textContent = "Trajectory series reset to default";
        }
        if (typeof barEl._trajOnChange === "function") barEl._trajOnChange();
      });
      installToolbarFocusgroupFallback(barEl);
    }
  }

  /**
   * Progressive roving-tabindex fallback for `role="toolbar" focusgroup="toolbar wrap"` when native
   * `HTMLElement.prototype.focusGroup` is not yet supported by the browser.
   */
  function installToolbarFocusgroupFallback(toolbarEl) {
    if (!toolbarEl || Object.prototype.hasOwnProperty.call(HTMLElement.prototype, "focusGroup")) {
      return;
    }
    if (toolbarEl.hasAttribute("data-fg-bound")) {
      syncToolbarTabIndex(toolbarEl);
      return;
    }
    toolbarEl.setAttribute("data-fg-bound", "1");
    var getItems = function () {
      return Array.prototype.slice.call(
        toolbarEl.querySelectorAll('button:not([disabled]):not([hidden]), a[href]:not([hidden])')
      ).filter(function (el) {
        return !el.hidden && !(el.closest && el.closest("[hidden]"));
      });
    };
    syncToolbarTabIndex(toolbarEl);

    toolbarEl.addEventListener("focusin", function (e) {
      var list = getItems();
      var target = e.target;
      if (list.indexOf(target) !== -1) {
        list.forEach(function (el) { el.tabIndex = el === target ? 0 : -1; });
      }
    });

    toolbarEl.addEventListener("keydown", function (e) {
      if (e.key !== "ArrowRight" && e.key !== "ArrowLeft" && e.key !== "Home" && e.key !== "End") return;
      var list = getItems();
      if (!list.length) return;
      var cur = list.indexOf(document.activeElement);
      if (cur === -1) return;
      e.preventDefault();
      var rtl = false;
      try { rtl = getComputedStyle(toolbarEl).direction === "rtl"; } catch (_) {}
      var nextIdx = cur;
      if (e.key === "Home") nextIdx = 0;
      else if (e.key === "End") nextIdx = list.length - 1;
      else {
        var step = e.key === "ArrowRight" ? (rtl ? -1 : 1) : (rtl ? 1 : -1);
        nextIdx = (cur + step + list.length) % list.length;
      }
      list.forEach(function (el, i) { el.tabIndex = i === nextIdx ? 0 : -1; });
      list[nextIdx].focus();
    });
  }

  window.XLT_CMP = {
    getState: getState,
    setState: setState,
    getRuns: getRuns,
    getBaseRun: getBaseRun,
    getTargetRun: getTargetRun,
    calcDelta: calcDelta,
    polarityClass: polarityClass,
    formatPct: formatPct,
    formatAbsDelta: formatAbsDelta,
    buildComparisonCellData: buildComparisonCellData,
    syncTableAlignment: syncTableAlignment,
    syncTablesAlignment: syncTablesAlignment,
    syncListAlignment: syncListAlignment,
    getTrajectorySelection: getTrajectorySelection,
    setTrajectorySelection: setTrajectorySelection,
    resetTrajectorySelection: resetTrajectorySelection,
    buildTrajectoryConfig: buildTrajectoryConfig,
    renderTrajectoryToolbar: renderTrajectoryToolbar,
    installToolbarFocusgroupFallback: installToolbarFocusgroupFallback
  };

  if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", function () {
      populateHeaderMetadata();
      renderSelectorBar();
      syncNavQueryLinks();
      initHeaderMenus();
    });
  }
})();
