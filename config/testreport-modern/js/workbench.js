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
 * @fileoverview Core chart workbench, sortable directory table, and visual stream engine.
 *
 * Requires `window.XLT_UI` (`js/report-utils.js`) loaded before this file and optionally
 * consumes `window.XLT_META` for chart availability, configured percentiles, runtime
 * segmentation boundaries, and `requestTableColorization` threshold rules.
 *
 * Architecture highlights:
 * - Zero-reflow table updates: `buildTableDom()` creates each `<tr>` once and caches it in
 *   `rowMap` (`key -> tr`); sorting and filtering reorder `<tbody>` via a `DocumentFragment`
 *   without rebuilding row markup or destroying open expandable drawers (`drawerMap`).
 * - Dynamic column expansion: `dynamicPercentiles` and `dynamicSegmentation` placeholders
 *   expand at init/table-view-switch time from report metadata.
 * - Multi-criteria filter engine (`UI.matchesFilter`): supports classic XLT space-AND,
 *   `-negation`, and `|` OR clauses across item names, labels, and optional metadata fields.
 * - Dual inspection modes: pinned/collapsible top Workbench (`#workbench-section`) synchronized
 *   with URL query parameters (`?item=...&tab=...&tview=...&q=...&sort=...&view=...`) and an
 *   IntersectionObserver-batched Visual Chart Stream (`#stream-section`).
 *
 * Exposes `window.XltWorkbench` (`init` plus shared formatters and colorization helpers).
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var collator = UI.collator;
  var escHtml = UI.esc;
  var fmtInt = UI.fmtIntOrDash;
  var fmtDec = UI.fmtDecOrDash;

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

  var ICON_LINK =
    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
    '<path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>' +
    '<path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>' +
    "</svg>";

  var ICON_CHECK =
    '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">' +
    '<polyline points="20 6 9 17 4 12"/>' +
    "</svg>";

  var lastWbStatus = "";
  var wbStatusRegion = null;

  function announceWbStatus(msg) {
    if (!wbStatusRegion) {
      wbStatusRegion = document.createElement("span");
      wbStatusRegion.className = "sr-only";
      wbStatusRegion.setAttribute("role", "status");
      wbStatusRegion.setAttribute("aria-live", "polite");
      document.body.appendChild(wbStatusRegion);
    }
    var out = msg;
    if (out && out === lastWbStatus) out += "\u00A0";
    if (out) lastWbStatus = out;
    wbStatusRegion.textContent = "";
    requestAnimationFrame(function () {
      if (wbStatusRegion) wbStatusRegion.textContent = out;
    });
  }

  function legacyCopyText(text) {
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

  function writeClipboardText(text) {
    if (navigator.clipboard && typeof navigator.clipboard.writeText === "function") {
      return navigator.clipboard.writeText(text).catch(function () {
        if (!legacyCopyText(text)) throw new Error("Copy failed");
      });
    }
    return legacyCopyText(text) ? Promise.resolve() : Promise.reject(new Error("Copy failed"));
  }

  function encodePathSegment(seg) {
    return encodeURIComponent(seg).replace(/%24/gi, "$");
  }

  var labelPopoverSeq = 0;

  function normalizeLabels(raw) {
    if (Array.isArray(raw)) {
      return raw.map(function (s) { return String(s == null ? "" : s).trim(); }).filter(Boolean);
    }
    if (typeof raw === "string" && raw.trim()) {
      return raw.trim().split(/\s+/).filter(Boolean);
    }
    return [];
  }

  function renderLabelPills(labels) {
    if (!labels || !labels.length) return "";
    var visible = labels.slice(0, 2).map(function (lbl) {
      return '<span class="label-chip">' + escHtml(lbl) + '</span>';
    }).join("");
    var more = "";
    if (labels.length >= 3) {
      var rest = labels.slice(2);
      var popId = "lbl-pop-" + (++labelPopoverSeq);
      more =
        '<span class="label-more-wrap">' +
          '<button type="button" class="label-chip-more" aria-describedby="' + popId + '">+' + rest.length + '</button>' +
          '<span role="tooltip" id="' + popId + '" class="label-chip-popover">' +
            rest.map(function (l) { return '<span class="label-chip">' + escHtml(l) + '</span>'; }).join("") +
          '</span>' +
        '</span>';
    }
    return '<div class="labels-cell-wrap">' + visible + more + '</div>';
  }

  // Accessible WCAG 1.4.13 Escape dismissal & reset for +(N-2) label popovers
  document.addEventListener("keydown", function (e) {
    if (e.key !== "Escape") return;
    var activeWraps = document.querySelectorAll(".label-more-wrap:hover:not(.is-dismissed), .label-more-wrap:focus-within:not(.is-dismissed)");
    if (activeWraps.length) {
      e.stopPropagation();
      Array.prototype.forEach.call(activeWraps, function (w) { w.classList.add("is-dismissed"); });
    }
  }, true);

  document.addEventListener("mouseout", function (e) {
    var w = e.target && e.target.closest ? e.target.closest(".label-more-wrap.is-dismissed") : null;
    if (w && !w.contains(e.relatedTarget)) w.classList.remove("is-dismissed");
  });

  document.addEventListener("focusout", function (e) {
    var w = e.target && e.target.closest ? e.target.closest(".label-more-wrap.is-dismissed") : null;
    if (w && !w.contains(e.relatedTarget)) w.classList.remove("is-dismissed");
  });

  document.addEventListener("click", function (e) {
    var moreBtn = e.target && e.target.closest ? e.target.closest(".label-chip-more") : null;
    if (moreBtn) {
      e.stopPropagation();
      var wrap = moreBtn.closest(".label-more-wrap");
      if (wrap) wrap.classList.remove("is-dismissed");
      moreBtn.focus();
    }
  });

  function formatMeanPill(val, entityLabel) {
    if (val == null || isNaN(val)) return "—";
    var n = Number(val);
    var cls = "lat-good";
    var kind = String(entityLabel || "").toLowerCase();
    if (kind.indexOf("transaction") !== -1) {
      if (n >= 180000) cls = "lat-bad";
      else if (n >= 60000) cls = "lat-warn";
    } else if (kind.indexOf("action") !== -1) {
      if (n >= 3000) cls = "lat-bad";
      else if (n >= 1000) cls = "lat-warn";
    } else {
      if (n >= 800) cls = "lat-bad";
      else if (n >= 250) cls = "lat-warn";
    }
    return '<span class="lat-pill ' + cls + '">' + fmtInt(n) + '</span>';
  }

  function renderTtfbWaterfall(item) {
    if (item.ttfb === undefined || item.ttfb === null) return "—";
    var ttfb = Number(item.ttfb || 0);
    var recv = Number(item.recv || 0);
    var total = Math.max(ttfb + recv, 1);
    var scaleMax = 500;
    var ttfbPct = Math.min(85, Math.max(8, Math.round((ttfb / Math.max(total, scaleMax)) * 100)));
    var recvPct = Math.min(100 - ttfbPct, Math.max(4, Math.round((recv / Math.max(total, scaleMax)) * 100)));
    return (
      '<div class="wf-cell" title="TTFB: ' + fmtInt(ttfb) + ' ms • Receive: ' + fmtInt(recv) + ' ms">' +
        '<span class="wf-bar-track">' +
          '<span class="wf-bar-ttfb" style="width:' + ttfbPct + '%"></span>' +
          '<span class="wf-bar-recv" style="width:' + recvPct + '%"></span>' +
        '</span>' +
        '<span>' + fmtInt(ttfb) + '</span>' +
      '</div>'
    );
  }

  // Real SVG sparkline from moving-average time-series buckets (0-anchored for non-negative values)
  function renderRealSparkline(spark, unitSuffix) {
    if (!spark || !spark.length) {
      return '<span class="cell-muted">—</span>';
    }
    var w = 72, h = 14, pad = 2;
    var minV = Infinity, maxV = -Infinity, sumV = 0;
    for (var i = 0; i < spark.length; i++) {
      var val = Number(spark[i]);
      if (val < minV) minV = val;
      if (val > maxV) maxV = val;
      sumV += val;
    }
    var meanV = sumV / spark.length;
    var unit = unitSuffix === undefined ? " ms" : unitSuffix;
    var nonNegative = minV >= 0;
    var yMin = 0, yMax = 0, span = 0;
    if (nonNegative) {
      yMin = 0;
      yMax = Math.max(maxV * 1.15, meanV * 1.75);
      span = yMax - yMin;
    } else {
      span = maxV - minV;
      var yPad = span < 0.0001 ? 0 : span * 0.15;
      yMin = minV - yPad;
      yMax = maxV + yPad;
      span = yMax - yMin;
    }

    var pts = [];
    for (var j = 0; j < spark.length; j++) {
      var v = Number(spark[j]);
      var x = spark.length === 1 ? pad : pad + (j / (spark.length - 1)) * (w - pad * 2);
      var y;
      if (span <= 0.0001) {
        y = nonNegative && yMax <= 0 ? (h - pad) : (h / 2);
      } else {
        y = (h - pad) - ((v - yMin) / span) * (h - pad * 2);
      }
      pts.push({ x: x, y: y });
    }
    if (pts.length === 1) {
      pts.push({ x: w - pad, y: pts[0].y });
    }

    var polyPts = pts.map(function (p) { return p.x.toFixed(1) + "," + p.y.toFixed(1); }).join(" ");
    var baseY = (h - pad).toFixed(1);
    var areaHtml = "";
    var baseLineHtml = "";
    if (nonNegative && yMax > 0) {
      var firstX = pts[0].x.toFixed(1);
      var lastX = pts[pts.length - 1].x.toFixed(1);
      var areaPts = firstX + "," + baseY + " " + polyPts + " " + lastX + "," + baseY;
      baseLineHtml = '<line x1="2" y1="12" x2="70" y2="12" stroke="var(--border-subtle)" stroke-width="0.75"/>';
      areaHtml = '<polygon fill="var(--brand-blue)" fill-opacity="0.10" points="' + areaPts + '"/>';
    }

    var dec = (Math.max(Math.abs(minV), Math.abs(maxV)) < 10 && unit === "") ? 2 : 0;
    var title = "Moving avg (5%): " + fmtDec(minV, dec) + " – " + fmtDec(maxV, dec) + unit;
    return (
      '<svg width="' + w + '" height="' + h + '" viewBox="0 0 ' + w + ' ' + h + '" style="display:block;margin-inline:auto;" aria-label="' + escHtml(title) + '" role="img">' +
      '<title>' + escHtml(title) + '</title>' +
      baseLineHtml +
      areaHtml +
      '<polyline fill="none" stroke="var(--brand-blue)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" points="' + polyPts + '"/>' +
      '</svg>'
    );
  }

  /**
   * Resolves the ordered list of configured percentile suffixes from `XLT_META` or sample data.
   * @param {Array<Object>=} sampleItems
   * @returns {string[]}
   */
  function getConfiguredPercentiles(sampleItems) {
    return UI.getConfiguredPercentiles(sampleItems);
  }

  /**
   * Extracts a numeric percentile value from an item's `percentiles` map or flat `p<N>` fields.
   * @param {Object} item
   * @param {string|number} pctStr
   * @returns {number|undefined}
   */
  function getItemPercentile(item, pctStr) {
    return UI.getPercentile(item, pctStr);
  }

  /**
   * Resolves runtime segmentation interval boundaries `[from, to]` in milliseconds.
   * Falls back to `[0,1000], [1000,3000], [3000,5000], [5000,null]` when legacy `b1..b4` fields appear.
   * @param {Array<Object>=} sampleItems
   * @returns {Array<{from: number, to: (number|null)}>}
   */

  function getConfiguredIntervals(sampleItems) {
    var metaCfg = window.XLT_META && window.XLT_META.testReportConfig;
    if (metaCfg && Array.isArray(metaCfg.runtimeIntervals)) {
      return metaCfg.runtimeIntervals.map(function (iv) {
        var fromNum = iv && iv.from != null && iv.from !== "" ? Number(iv.from) : 0;
        var toNum = (iv && iv.to != null && iv.to !== "") ? Number(iv.to) : null;
        return {
          from: isNaN(fromNum) ? 0 : fromNum,
          to: (toNum !== null && !isNaN(toNum)) ? toNum : null
        };
      });
    }
    if (Array.isArray(sampleItems)) {
      for (var i = 0; i < sampleItems.length; i++) {
        var it = sampleItems[i];
        if (it && it.b1 !== undefined && it.b4 !== undefined) {
          return [
            { from: 0, to: 1000 },
            { from: 1000, to: 3000 },
            { from: 3000, to: 5000 },
            { from: 5000, to: null }
          ];
        }
      }
    }
    return [];
  }

  function getItemSegmentation(item, idx) {
    if (!item || idx == null || idx < 0) return undefined;
    if (Array.isArray(item.segmentation) && item.segmentation[idx] !== undefined) {
      var seg = item.segmentation[idx];
      if (seg && typeof seg === "object") {
        var c = Number(seg.count != null ? seg.count : 0);
        var p = Number(seg.pct != null ? seg.pct : (seg.percentage != null ? seg.percentage : 0));
        return { count: isNaN(c) ? 0 : c, pct: isNaN(p) ? 0 : p };
      }
    }
    if (Array.isArray(item.countPerInterval) && item.countPerInterval[idx] !== undefined) {
      var cnt = Number(item.countPerInterval[idx] || 0);
      var pct = (Array.isArray(item.percentagePerInterval) && item.percentagePerInterval[idx] !== undefined)
        ? Number(item.percentagePerInterval[idx] || 0)
        : 0;
      return { count: isNaN(cnt) ? 0 : cnt, pct: isNaN(pct) ? 0 : pct };
    }
    var bKey = "b" + (idx + 1);
    if (item[bKey] !== undefined) {
      return {
        count: Number(item[bKey] || 0),
        pct: Number(item[bKey + "Pct"] || 0)
      };
    }
    return undefined;
  }

  function formatIntervalLabel(iv, idx, total) {
    if (idx < total - 1 && iv.to !== null) {
      return "≤ " + fmtInt(iv.to);
    }
    if (iv.to !== null) {
      return "≤ " + fmtInt(iv.to);
    }
    return "> " + fmtInt(iv.from || 0);
  }

  function formatIntervalRange(iv) {
    if (iv.to !== null) {
      return (iv.from > 0)
        ? (fmtInt(iv.from) + " < x ≤ " + fmtInt(iv.to) + " ms")
        : ("x ≤ " + fmtInt(iv.to) + " ms");
    }
    return "x > " + fmtInt(iv.from || 0) + " ms";
  }

  function formatPercentileSummaryHtml(item, pcts, decimals, unitSuffix) {
    if (!pcts || !pcts.length) return "";
    var dec = decimals !== undefined ? decimals : 0;
    var unit = unitSuffix === undefined ? " ms" : unitSuffix;
    return pcts.map(function (p) {
      var v = getItemPercentile(item, p);
      var txt = v === undefined ? "—" : ((dec > 0 ? fmtDec(v, dec) : fmtInt(v)) + unit);
      return "P" + escHtml(p) + ": <strong>" + txt + "</strong>";
    }).join(" • ");
  }

  /**
   * Computes Apdex classification and tooltip metadata from an item's `apdex` field.
   * Note: marks scores with `*` when `count < 100` (statistically small sample).
   */
  function getApdexInfo(item) {
    if (!item || item.apdex == null || item.apdex === "") return null;
    var raw = item.apdex;
    var val;
    var longVal = "";
    var thresh = "";
    if (typeof raw === "object") {
      val = Number(raw.value);
      longVal = raw.longValue != null ? String(raw.longValue).trim() : "";
      thresh = raw.threshold != null ? String(raw.threshold).trim() : "";
    } else {
      val = Number(raw);
    }
    if (isNaN(val)) return null;
    if (!thresh && longVal) {
      var m = longVal.match(/\[([^\]]+)\]/);
      if (m && m[1]) thresh = m[1].trim();
    }
    var lowSample = /\*/.test(longVal);
    var rating, cls;
    if (val >= 0.94) { rating = "Excellent"; cls = "apdex-excellent"; }
    else if (val >= 0.85) { rating = "Good"; cls = "apdex-good"; }
    else if (val >= 0.70) { rating = "Fair"; cls = "apdex-fair"; }
    else if (val >= 0.50) { rating = "Poor"; cls = "apdex-poor"; }
    else { rating = "Unacceptable"; cls = "apdex-unacceptable"; }

    var scoreStr = fmtDec(val, 2) + (lowSample ? "*" : "");
    var copyText = longVal || (thresh ? (scoreStr + " [" + thresh + "]") : scoreStr);
    var tipParts = ["Apdex: " + copyText + " — " + rating];
    if (thresh) tipParts.push("Threshold T = " + thresh + " s");
    if (lowSample) tipParts.push("Small sample size (< 100)");
    return {
      value: val,
      threshold: thresh,
      longValue: longVal,
      lowSample: lowSample,
      scoreStr: scoreStr,
      copyText: copyText,
      rating: rating,
      cls: cls,
      tooltip: tipParts.join(" • ")
    };
  }

  function renderApdexCell(item) {
    var info = getApdexInfo(item);
    if (!info) return '<span class="cell-muted">—</span>';
    var threshHtml = info.threshold ? ' <span class="apdex-thresh">[' + escHtml(info.threshold) + ']</span>' : "";
    return (
      '<span class="apdex-pill ' + info.cls + '" title="' + escHtml(info.tooltip) + '">' +
        '<span>' + escHtml(info.scoreStr) + '</span>' +
        threshHtml +
      '</span>'
    );
  }

  /**
   * Builds a lookup of request-table colorization rules indexed by `<groupName>` -> `<type>:<id>`.
   * Reads `XLT_META.testReportConfig.requestTableColorization` unless an explicit config is passed.
   */
  function buildColorizationMap(rawConfig) {
    var src = rawConfig !== undefined
      ? rawConfig
      : (window.XLT_META && window.XLT_META.testReportConfig && window.XLT_META.testReportConfig.requestTableColorization);
    var groups = Object.create(null);
    var hasAny = false;
    if (!src) return { hasAny: false, groups: groups };

    function ingestRules(groupName, rulesArr) {
      if (!groupName) return;
      var gMap = groups[groupName] || Object.create(null);
      groups[groupName] = gMap;
      hasAny = true;
      if (!Array.isArray(rulesArr)) return;
      rulesArr.forEach(function (r) {
        if (!r || typeof r !== "object" || !r.type) return;
        var fromNum = Number(r.from);
        var targetNum = Number(r.target);
        var toNum = Number(r.to);
        if (isNaN(fromNum) || isNaN(targetNum) || isNaN(toNum)) return;
        var idStr = r.id == null ? "" : String(r.id);
        gMap[r.type + ":" + idStr] = {
          id: idStr,
          type: String(r.type),
          from: fromNum,
          target: targetNum,
          to: toNum
        };
      });
    }

    if (Array.isArray(src)) {
      src.forEach(function (entry) {
        if (!entry || typeof entry !== "object") return;
        var gName = entry.groupName || entry.name;
        ingestRules(gName, entry.rules);
      });
    } else if (typeof src === "object") {
      Object.keys(src).forEach(function (gName) {
        var val = src[gName];
        ingestRules(gName, Array.isArray(val) ? val : (val && val.rules));
      });
    }
    return { hasAny: hasAny, groups: groups };
  }

  /**
   * Evaluates a table cell against its group's colorization rule and returns a `p0..p100`
   * (positive/target-met) or `n0..n100` (degraded) CSS heat-map step code.
   */
  function evaluateCellColorization(item, col, colorMap) {
    if (!item || item.isAggregate || !col || !col.colorizeType || !colorMap || !colorMap.hasAny) {
      return null;
    }
    if (item.colorizationGroup === null || item.colorizationGroupName === null) {
      return null;
    }
    var groupName = item.colorizationGroupName || item.colorizationGroup || "default";
    var gRules = colorMap.groups[groupName];
    if (!gRules) return null;

    var ruleId = col.colorizeId !== undefined ? String(col.colorizeId) : String(col.key);
    var rule = gRules[col.colorizeType + ":" + ruleId];
    if (!rule) return null;

    var val;
    if (col.colorizeType === "mean") val = Number(item.mean);
    else if (col.colorizeType === "min") val = Number(item.min);
    else if (col.colorizeType === "max") val = Number(item.max);
    else if (col.colorizeType === "percentile") {
      var pv = getItemPercentile(item, col.pctStr !== undefined ? col.pctStr : ruleId.replace(/^p/i, ""));
      val = pv === undefined ? NaN : Number(pv);
    } else if (col.colorizeType === "segmentation") {
      var seg = getItemSegmentation(item, col.segIndex);
      val = seg ? Number(seg.pct) : NaN;
    } else {
      val = col.getValue ? Number(col.getValue(item)) : Number(item[col.key]);
    }
    if (val === undefined || isNaN(val)) return null;

    var inverted = !!col.colorizeInverted;
    var lo = Math.min(rule.from, rule.to);
    var hi = Math.max(rule.from, rule.to);
    var target = rule.target;
    var posPrefix = inverted ? "n" : "p";
    var negPrefix = inverted ? "p" : "n";
    var prefix, pct;

    if (val > target) {
      prefix = negPrefix;
      pct = hi === target ? 100 : Math.floor((val - target) * (100 / (hi - target)));
    } else {
      prefix = posPrefix;
      pct = val === target ? 0 : (lo === target ? 100 : Math.floor((val - target) * (100 / (lo - target))));
    }
    if (pct < 0) pct = 0;
    if (pct > 100) pct = 100;

    return {
      groupName: groupName,
      prefix: prefix,
      pct: pct,
      code: prefix + pct,
      rule: rule
    };
  }

  var matchesXltFilter = UI.matchesFilter;

  /**
   * Initializes the Workbench, Directory Table, and Visual Stream views on a report page.
   *
   * @param {Object} cfg Page configuration object (`entityLabel`, `items`, `columns`/`tableViews`,
   *   `tabs`, `totalsRow`, `defaultSort`, `renderWbKpis`, `renderDrawer`, etc.).
   */
  function initWorkbenchPage(cfg) {
    var allItems = (cfg.items || []).slice(); // includes optional summary item with isAggregate=true at [0]
    allItems.forEach(function (it) {
      it.labels = normalizeLabels(it.labels);
    });
    var tableItems = allItems.filter(function (it) { return !it.isAggregate; });
    var hasPageLabels = tableItems.some(function (it) { return it.labels.length > 0; });
    var configuredPcts = getConfiguredPercentiles(allItems);
    var configuredIntervals = getConfiguredIntervals(allItems);
    var hasPageSegData = allItems.some(function (it) { return getItemSegmentation(it, 0) !== undefined; });
    var chartsAvailable = !(window.XLT_META && window.XLT_META.chartsGenerated === false);
    var colorMap = cfg.colorizeRequests ? buildColorizationMap(cfg.colorizationConfig) : null;

    function expandPercentileColumns(colDef) {
      var dec = colDef && colDef.decimals !== undefined ? colDef.decimals : 0;
      var grp = (colDef && colDef.group) || "Percentiles [ms]";
      var alt = !!(colDef && colDef.altGroup);
      return configuredPcts.map(function (pctStr) {
        return {
          key: "p" + pctStr,
          pctStr: pctStr,
          colorizeType: "percentile",
          colorizeId: "p" + pctStr,
          label: "P" + pctStr,
          title: "The " + pctStr + "th percentile of the data series.",
          group: grp,
          altGroup: alt,
          sortable: true,
          isExtraStat: Number(pctStr) > 99,
          getValue: function (it) {
            var v = getItemPercentile(it, pctStr);
            return v === undefined ? NaN : v;
          },
          getCopyText: function (it) {
            var v = getItemPercentile(it, pctStr);
            if (v === undefined) return "—";
            return dec > 0 ? fmtDec(v, dec) : fmtInt(v);
          },
          cellClass: colDef && colDef.cellClass ? function (it) {
            return colDef.cellClass(it, pctStr, getItemPercentile(it, pctStr));
          } : undefined,
          render: function (it) {
            var v = getItemPercentile(it, pctStr);
            if (v === undefined) return "—";
            return dec > 0 ? fmtDec(v, dec) : fmtInt(v);
          },
          renderTotal: function (val, totObj) {
            var v = (val !== undefined && val !== null && !isNaN(val)) ? Number(val) : getItemPercentile(totObj, pctStr);
            if (v === undefined || isNaN(v)) return "";
            return dec > 0 ? fmtDec(v, dec) : fmtInt(v);
          }
        };
      });
    }

    function expandSegmentationColumns(colDef) {
      if (colDef && colDef.onlyIfData && !hasPageSegData) return [];
      if (!configuredIntervals.length) return [];
      var grp = (colDef && colDef.group) || "Runtime Segmentation [ms]";
      var alt = colDef && colDef.altGroup !== undefined ? !!colDef.altGroup : false;
      return configuredIntervals.map(function (iv, idx) {
        var isLast = idx === configuredIntervals.length - 1;
        var lbl = formatIntervalLabel(iv, idx, configuredIntervals.length);
        var rng = formatIntervalRange(iv);
        return {
          key: "seg_" + idx,
          segIndex: idx,
          segInterval: iv,
          segIsLast: isLast,
          colorizeType: "segmentation",
          colorizeId: (iv.to === null || isLast) ? "" : String(iv.to),
          colorizeInverted: !isLast,
          label: lbl,
          title: "Runtime segment (" + rng + ") and percentage of requests within.",
          group: grp,
          altGroup: alt,
          sortable: true,
          agg: "segPct",
          getValue: function (it) {
            var seg = getItemSegmentation(it, idx);
            return seg ? seg.pct : NaN;
          },
          getCopyText: function (it) {
            var seg = getItemSegmentation(it, idx);
            return seg ? (fmtDec(seg.pct, 2) + "%") : "—";
          },
          render: function (it) {
            var seg = getItemSegmentation(it, idx);
            if (!seg) return "—";
            var tip = fmtInt(seg.count) + " (" + fmtDec(seg.pct, 2) + "%)";
            return '<span title="' + escHtml(tip) + '">' + fmtDec(seg.pct, 2) + '%</span>';
          },
          renderTotal: function (val, totObj) {
            var seg = (totObj && totObj._segTotals && totObj._segTotals[idx])
              ? totObj._segTotals[idx]
              : getItemSegmentation(totObj, idx);
            if (!seg) return "";
            var tip = fmtInt(seg.count) + " (" + fmtDec(seg.pct, 2) + "%)";
            return '<span title="' + escHtml(tip) + '">' + fmtDec(seg.pct, 2) + '%</span>';
          }
        };
      });
    }

    function buildExpandedColumns(rawColumns) {
      var expandedCols = [];
      var sawDynamicPctPlaceholder = false;
      (rawColumns || []).forEach(function (c) {
        if (!c) return;
        if (c.type === "dynamicPercentiles") {
          sawDynamicPctPlaceholder = true;
          Array.prototype.push.apply(expandedCols, expandPercentileColumns(c));
        } else if (c.type === "dynamicSegmentation") {
          Array.prototype.push.apply(expandedCols, expandSegmentationColumns(c));
        } else if (hasPageLabels || c.key !== "labels") {
          expandedCols.push(c);
        }
      });
      if (cfg.dynamicPercentiles && !sawDynamicPctPlaceholder) {
        Array.prototype.push.apply(expandedCols, expandPercentileColumns({
          group: cfg.percentilesGroup || "Percentiles [ms]",
          decimals: cfg.percentileDecimals || 0
        }));
      }

      var cols = expandedCols.slice();
      if (hasPageLabels && !cols.some(function (c) { return c.key === "labels"; })) {
        var nameIdx = cols.findIndex(function (c) { return c.key === "name"; });
        var labelCol = { key: "labels", label: "Labels", type: "labels", sortable: true };
        if (nameIdx >= 0) cols.splice(nameIdx + 1, 0, labelCol);
        else cols.unshift(labelCol);
      }
      return cols;
    }

    var tableViews = Array.isArray(cfg.tableViews) && cfg.tableViews.length ? cfg.tableViews : null;
    var tabs = cfg.tabs || [];
    var params = new URLSearchParams(window.location.search);

    var initialTableViewObj = tableViews
      ? (tableViews.find(function (v) { return v.key === params.get("tview"); }) || tableViews[0])
      : null;
    var columns = buildExpandedColumns(initialTableViewObj ? initialTableViewObj.columns : (cfg.columns || []));
    var initialSort = (initialTableViewObj && initialTableViewObj.defaultSort) || cfg.defaultSort;

    var state = {
      activeList: allItems.slice(), // items navigable in workbench
      activeIdx: 0,
      activeTab: tabs.length ? tabs[0].key : "overview",
      tableView: initialTableViewObj ? initialTableViewObj.key : null,
      query: "",
      label: "ALL",
      sortKey: initialSort ? initialSort.key : null,
      sortDir: initialSort ? initialSort.dir : "descending",
      view: "workbench",
      streamTab: tabs.length ? tabs[0].key : "overview",
      collapsed: false,
      pinned: true
    };

    // Hydrate remaining state from URL query params
    if (params.get("tab") && tabs.some(function (t) { return t.key === params.get("tab"); })) {
      state.activeTab = params.get("tab");
      state.streamTab = params.get("tab");
    }
    if (params.get("q")) state.query = params.get("q");
    if (hasPageLabels && (params.get("label") || params.get("cat"))) {
      state.label = params.get("label") || params.get("cat");
    }
    if (params.get("view") === "stream") state.view = "stream";
    if (params.get("sort")) {
      var sp = params.get("sort").split(":");
      if (sp[0]) state.sortKey = sp[0];
      if (sp[1] === "ascending" || sp[1] === "descending") state.sortDir = sp[1];
    }

    var wbRoot = document.getElementById("workbench-section");
    var streamRoot = document.getElementById("stream-section");
    var dirRoot = document.getElementById("directory-section");
    var wbBadge = document.getElementById("wb-badge");
    var wbName = document.getElementById("wb-name");
    var wbCounter = document.getElementById("wb-counter");
    var wbPrev = document.getElementById("wb-prev");
    var wbNext = document.getElementById("wb-next");
    var wbKpis = document.getElementById("wb-kpis");
    var wbBody = document.getElementById("wb-body");
    var wbTabs = document.getElementById("wb-tabs");
    var wbStage = document.getElementById("wb-stage");
    var wbFootnote = document.getElementById("wb-footnote");
    var btnPin = document.getElementById("wb-pin-btn");
    var btnCollapse = document.getElementById("wb-collapse-btn");
    var btnCopyLink = document.getElementById("wb-link-btn");
    var btnZoom = document.getElementById("wb-zoom-btn");
    var btnUnpin = document.getElementById("wb-unpin-btn");
    var btnViewWb = document.getElementById("btn-view-workbench");
    var btnViewStream = document.getElementById("btn-view-stream");
    var filterInput = document.getElementById("dir-filter-input");
    var tableTabsBar = document.getElementById("dir-table-tabs");
    var catBar = document.getElementById("dir-category-chips");
    var table = document.getElementById("dir-table");
    var tbody = table ? table.tBodies[0] : null;
    var tfoot = table ? table.tFoot : null;
    var statusEl = document.getElementById("table-sort-status");

    var rowMap = new Map(); // item.key -> <tr>
    var drawerMap = new Map(); // item.key -> <tr class="drawer-row">

    function syncUrl() {
      var p = new URLSearchParams();
      var cur = state.activeList[state.activeIdx];
      if (cur && cur.key) p.set("item", cur.key);
      if (state.activeTab && tabs[0] && state.activeTab !== tabs[0].key) p.set("tab", state.activeTab);
      if (tableViews && state.tableView && state.tableView !== tableViews[0].key) p.set("tview", state.tableView);
      if (state.query) p.set("q", state.query);
      if (hasPageLabels && state.label && state.label !== "ALL") p.set("label", state.label);
      if (state.sortKey) p.set("sort", state.sortKey + ":" + state.sortDir);
      if (state.view === "stream") p.set("view", "stream");
      var qs = p.toString();
      var nextUrl = window.location.pathname + (qs ? "?" + qs : "") + window.location.hash;
      try {
        window.history.replaceState(null, "", nextUrl);
      } catch (_) {}
    }

    function buildChartUrl(item, tabObj) {
      return tabObj.url(item, encodePathSegment);
    }

    var EXTRA_STAT_KEYS = { min: true, max: true, dev: true, p999: true, "p99.9": true };

    // Build Table Header (2-row grouped headers when group labels exist)
    function buildTableDom() {
      if (!table) return;
      var thead = table.tHead || table.createTHead();
      thead.innerHTML = "";

      var hasGroups = columns.some(function (c) { return !!c.group; });
      if (hasGroups) {
        var trGroup = document.createElement("tr");
        trGroup.className = "group-header-row";
        var trLeaf = document.createElement("tr");

        var thIdx = document.createElement("th");
        thIdx.rowSpan = 2;
        thIdx.scope = "col";
        thIdx.className = "col-idx";
        thIdx.textContent = "#";
        trGroup.appendChild(thIdx);

        var i = 0;
        while (i < columns.length) {
          var col = columns[i];
          if (!col.group) {
            var thSingle = createHeaderCell(col, 2);
            trGroup.appendChild(thSingle);
            i++;
          } else {
            var grp = col.group;
            var span = 0;
            var alt = !!col.altGroup;
            while (i + span < columns.length && columns[i + span].group === grp) {
              trLeaf.appendChild(createHeaderCell(columns[i + span], 1));
              span++;
            }
            var thGrp = document.createElement("th");
            thGrp.colSpan = span;
            thGrp.scope = "colgroup";
            if (alt) thGrp.className = "cg-alt";
            thGrp.textContent = grp;
            trGroup.appendChild(thGrp);
            i += span;
          }
        }
        thead.appendChild(trGroup);
        thead.appendChild(trLeaf);
      } else {
        var tr = document.createElement("tr");
        var thIdxSingle = document.createElement("th");
        thIdxSingle.scope = "col";
        thIdxSingle.className = "col-idx";
        thIdxSingle.textContent = "#";
        tr.appendChild(thIdxSingle);
        columns.forEach(function (col) {
          tr.appendChild(createHeaderCell(col, 1));
        });
        thead.appendChild(tr);
      }

      // Build rows once into a DocumentFragment to avoid 1,000+ live reflows
      tbody.innerHTML = "";
      var initFrag = document.createDocumentFragment();
      if (cfg.includeAggregateRowInTable) {
        allItems.filter(function (x) { return x.isAggregate; }).forEach(function (agg) {
          var aggRow = createRowElement(agg, "Σ");
          rowMap.set(agg.key, aggRow);
          initFrag.appendChild(aggRow);
        });
      }
      tableItems.forEach(function (item, idx) {
        var tr = createRowElement(item, String(idx + 1));
        rowMap.set(item.key, tr);
        initFrag.appendChild(tr);
      });
      tbody.appendChild(initFrag);

      // Build static Totals + dynamic Filtered Totals rows in <tfoot>
      if (tfoot && cfg.totalsRow) {
        tfoot.innerHTML = "";
        tfoot.appendChild(buildTotalsTr(cfg.totalsRow, "Totals (" + tableItems.length + " entries)", false));
        var ftr = document.createElement("tr");
        ftr.className = "filtered-totals-row";
        ftr.hidden = true;
        tfoot.appendChild(ftr);
      }
    }

    function createHeaderCell(col, rowSpan) {
      var th = document.createElement("th");
      th.scope = "col";
      if (rowSpan > 1) th.rowSpan = rowSpan;
      var classes = [];
      if (col.type === "text") classes.push("col-text");
      else if (col.type === "labels") classes.push("col-labels");
      if (col.altGroup) classes.push("cg-alt");
      if (EXTRA_STAT_KEYS[col.key] || col.isExtraStat) classes.push("col-extra");
      if (classes.length) th.className = classes.join(" ");
      th.setAttribute("data-col-key", col.key);
      if (col.title) th.title = col.title;

      if (col.sortable !== false) {
        if (state.sortKey === col.key) {
          th.setAttribute("aria-sort", state.sortDir);
        }
        var btn = document.createElement("button");
        btn.type = "button";
        btn.className = "th-sort-btn";
        btn.innerHTML = '<span>' + escHtml(col.label) + '</span><span class="sort-icon" aria-hidden="true"></span>';
        btn.addEventListener("click", function () {
          if (state.sortKey === col.key) {
            state.sortDir = state.sortDir === "ascending" ? "descending" : "ascending";
          } else {
            state.sortKey = col.key;
            state.sortDir = "ascending";
          }
          applySortAndFilter(false);
          syncUrl();
        });
        th.appendChild(btn);
      } else {
        th.textContent = col.label;
      }
      return th;
    }

    function createRowElement(item, rowIndexStr) {
      var tr = document.createElement("tr");
      tr.className = "wb-row" + (item.isAggregate ? " is-aggregate-row" : "");
      tr.setAttribute("data-key", item.key);
      tr.tabIndex = -1;

      var tdIdx = document.createElement("td");
      tdIdx.className = "col-idx";
      tdIdx.textContent = rowIndexStr || "";
      tr.appendChild(tdIdx);

      columns.forEach(function (col) {
        var td = document.createElement("td");
        var cls = [];
        if (col.type === "text") cls.push("col-text");
        else if (col.type === "labels") cls.push("col-labels");
        else if (col.type === "spark") cls.push("col-spark");
        else {
          cls.push("col-num");
          if (col.type === "apdex") cls.push("col-apdex");
        }
        if (col.key === "cph") cls.push("col-cph");
        if (col.altGroup) cls.push("cg-alt");
        if (EXTRA_STAT_KEYS[col.key] || col.isExtraStat) cls.push("col-extra");
        if (col.cellClass) {
          var extra = col.cellClass(item);
          if (extra) cls.push(extra);
        }
        var colorRes = colorMap ? evaluateCellColorization(item, col, colorMap) : null;
        if (colorRes) {
          cls.push("colorized");
          cls.push("colorized-" + colorRes.prefix);
          cls.push(colorRes.code);
          td.setAttribute("data-colorize", colorRes.code);
          td.setAttribute("data-colorize-group", colorRes.groupName);
          td.style.setProperty("--colorize-pct", colorRes.pct + "%");
        }
        td.className = cls.join(" ");

        if (col.type === "labels") {
          td.setAttribute("data-copy", (item.labels || []).join(", "));
          if (item.isAggregate) {
            td.textContent = "";
          } else if (!item.labels || !item.labels.length) {
            td.innerHTML = '<span class="cell-muted">—</span>';
          } else {
            td.innerHTML = renderLabelPills(item.labels);
          }
        } else if (col.type === "apdex") {
          var apdexInfo = getApdexInfo(item);
          td.setAttribute("data-copy", apdexInfo ? apdexInfo.copyText : "");
          if (apdexInfo) {
            td.setAttribute("data-apdex-rating", apdexInfo.rating.toLowerCase());
            td.setAttribute("data-apdex-value", fmtDec(apdexInfo.value, 2));
            td.title = apdexInfo.tooltip;
            td.innerHTML = renderApdexCell(item);
          } else if (item.isAggregate) {
            td.textContent = "";
          } else {
            td.innerHTML = '<span class="cell-muted">—</span>';
          }
        } else if (col.key === "name") {
          td.setAttribute("data-copy", item.name);
          td.innerHTML =
            '<div class="name-cell-wrap">' +
              '<button type="button" class="drawer-btn" aria-expanded="false" aria-label="Toggle row details" title="Details (Enter)">▸</button>' +
              '<div class="name-cell-stack">' +
                '<span class="name-cell-text" title="' + escHtml(item.name) + '">' + escHtml(item.name) + '</span>' +
              '</div>' +
            '</div>';
          var dBtn = td.querySelector(".drawer-btn");
          dBtn.addEventListener("click", function (ev) {
            ev.stopPropagation();
            selectByKey(item.key, false);
            toggleDrawer(item);
          });
        } else if (col.type === "spark") {
          td.setAttribute("data-copy", "");
          td.innerHTML = renderRealSparkline(item.spark, cfg.sparkUnit);
        } else if (col.key === "ttfb" && item.ttfb !== undefined && !col.disableTtfbWaterfall) {
          td.setAttribute("data-copy", fmtInt(item.ttfb));
          td.innerHTML = renderTtfbWaterfall(item);
        } else {
          if (col.getCopyText) {
            td.setAttribute("data-copy", col.getCopyText(item));
          } else if (col.key === "mean" && item.mean !== undefined) {
            td.setAttribute("data-copy", (cfg.meanDecimals && cfg.meanDecimals > 0) ? fmtDec(item.mean, cfg.meanDecimals) : fmtInt(item.mean));
          }
          td.innerHTML = col.render ? col.render(item) : escHtml(item[col.key]);
        }
        tr.appendChild(td);
      });

      tr.addEventListener("click", function () {
        selectByKey(item.key, false);
      });

      return tr;
    }

    function buildTotalsTr(totObj, label, isFiltered) {
      var tr = document.createElement("tr");
      if (isFiltered) tr.className = "filtered-totals-row";
      var tdIdx = document.createElement("td");
      tdIdx.className = "col-idx";
      tdIdx.textContent = isFiltered ? "ƒ" : "Σ";
      tr.appendChild(tdIdx);

      columns.forEach(function (col) {
        var td = document.createElement("td");
        if (col.type === "text") td.className = "col-text";
        else if (col.type === "labels") td.className = "col-labels";
        else if (col.type === "apdex") td.className = "col-apdex";
        if (col.altGroup) td.classList.add("cg-alt");
        if (EXTRA_STAT_KEYS[col.key] || col.isExtraStat) td.classList.add("col-extra");
        if (!isFiltered && totObj && col.cellClass) {
          var totExtra = col.cellClass(totObj);
          if (totExtra) td.classList.add(totExtra);
        }
        if (col.type === "labels") {
          td.textContent = "";
        } else if (col.key === "name") {
          td.textContent = label;
        } else if (col.type === "spark") {
          td.textContent = "";
        } else if (col.type === "apdex") {
          if (!isFiltered && totObj && getApdexInfo(totObj)) {
            td.innerHTML = renderApdexCell(totObj);
          } else {
            td.textContent = "";
          }
        } else if (col.renderTotal) {
          td.innerHTML = col.renderTotal(totObj ? totObj[col.key] : undefined, totObj);
        } else if (totObj && totObj[col.key] !== undefined && totObj[col.key] !== null) {
          td.innerHTML = col.render ? col.render(totObj) : escHtml(totObj[col.key]);
        } else {
          td.textContent = "";
        }
        tr.appendChild(td);
      });
      return tr;
    }

    function computeFilteredTotals(visibleItems) {
      var out = { _segTotals: {} };
      var totalCount = 0;
      visibleItems.forEach(function (it) { totalCount += Number(it.count || it.totalTx || 0); });

      columns.forEach(function (col) {
        var agg = col.agg;
        if (!agg || !visibleItems.length) return;
        if (agg === "sum") {
          var s = 0;
          visibleItems.forEach(function (it) { s += Number(it[col.key] || 0); });
          out[col.key] = s;
        } else if (agg === "min") {
          var mn = Infinity;
          visibleItems.forEach(function (it) {
            var v = Number(it[col.key]);
            if (!isNaN(v) && v < mn) mn = v;
          });
          out[col.key] = mn === Infinity ? null : mn;
        } else if (agg === "max") {
          var mx = -Infinity;
          visibleItems.forEach(function (it) {
            var v = Number(it[col.key]);
            if (!isNaN(v) && v > mx) mx = v;
          });
          out[col.key] = mx === -Infinity ? null : mx;
        } else if (agg === "avg") {
          var av = 0;
          visibleItems.forEach(function (it) { av += Number(it[col.key] || 0); });
          out[col.key] = av / visibleItems.length;
        } else if (agg === "weightedMean") {
          if (totalCount > 0) {
            var wm = 0;
            visibleItems.forEach(function (it) {
              wm += Number(it[col.key] || 0) * (Number(it.count || it.totalTx || 0) / totalCount);
            });
            out[col.key] = wm;
          }
        } else if (agg === "errPct") {
          var errs = 0;
          visibleItems.forEach(function (it) { errs += Number(it.errors || 0); });
          out[col.key] = totalCount > 0 ? (errs / totalCount) * 100 : 0;
        } else if (agg === "segPct" && col.segIndex !== undefined) {
          var segSum = 0;
          visibleItems.forEach(function (it) {
            var seg = getItemSegmentation(it, col.segIndex);
            if (seg) segSum += Number(seg.count || 0);
          });
          var segPctVal = totalCount > 0 ? (segSum / totalCount) * 100 : 0;
          out._segTotals[col.segIndex] = { count: segSum, pct: segPctVal };
          out[col.key] = segPctVal;
        }
      });
      return out;
    }

    // --- Expandable Row Drawers & Zero-Reflow Table Sorting / Filtering ---

    /**
     * Toggles the detail `<tr class="drawer-row">` immediately after an item's `<tr>`.
     */
    function toggleDrawer(item) {
      var row = rowMap.get(item.key);
      if (!row || !cfg.renderDrawer) return;
      var btn = row.querySelector(".drawer-btn");
      var existing = drawerMap.get(item.key);
      if (existing) {
        existing.remove();
        drawerMap.delete(item.key);
        if (btn) { btn.textContent = "▸"; btn.setAttribute("aria-expanded", "false"); }
        return;
      }
      var dTr = document.createElement("tr");
      dTr.className = "drawer-row";
      var dTd = document.createElement("td");
      dTd.colSpan = columns.length + 1;
      dTd.innerHTML = cfg.renderDrawer(item, escHtml, fmtInt, fmtDec);
      dTr.appendChild(dTd);
      row.after(dTr);
      drawerMap.set(item.key, dTr);
      if (btn) { btn.textContent = "▾"; btn.setAttribute("aria-expanded", "true"); }
    }

    /**
     * Sorts `tableItems` by active column, filters via query/label, reorders cached DOM nodes
     * in a single DocumentFragment (keeping open drawers attached), and updates Filtered Totals.
     */
    function applySortAndFilter(initialLoad) {
      // Update aria-sort on headers
      if (table && table.tHead) {
        Array.prototype.forEach.call(table.tHead.querySelectorAll("th[data-col-key]"), function (th) {
          if (th.getAttribute("data-col-key") === state.sortKey) {
            th.setAttribute("aria-sort", state.sortDir);
          } else {
            th.removeAttribute("aria-sort");
          }
        });
      }

      var sortCol = columns.find(function (c) { return c.key === state.sortKey; });
      var factor = state.sortDir === "ascending" ? 1 : -1;

      var sortedTableItems = tableItems.slice();
      if (sortCol) {
        sortedTableItems.sort(function (a, b) {
          if (sortCol.type === "labels") {
            var la = (a.labels || []).join(" ");
            var lb = (b.labels || []).join(" ");
            if (!la && lb) return 1;
            if (la && !lb) return -1;
            var cmp = collator.compare(la, lb);
            return factor * (cmp !== 0 ? cmp : collator.compare(String(a.name || ""), String(b.name || "")));
          }
          if (sortCol.type === "text") {
            return factor * collator.compare(String(a[sortCol.key] || ""), String(b[sortCol.key] || ""));
          }
          if (sortCol.type === "apdex") {
            var aa = getApdexInfo(a);
            var ab = getApdexInfo(b);
            var ava = aa ? aa.value : -Infinity;
            var avb = ab ? ab.value : -Infinity;
            var acmp = ava === avb ? 0 : (ava < avb ? -1 : 1);
            return factor * (acmp !== 0 ? acmp : collator.compare(String(a.name || ""), String(b.name || "")));
          }
          var va = sortCol.getValue ? Number(sortCol.getValue(a)) : Number(a[sortCol.key]);
          var vb = sortCol.getValue ? Number(sortCol.getValue(b)) : Number(b[sortCol.key]);
          if (isNaN(va)) va = -Infinity;
          if (isNaN(vb)) vb = -Infinity;
          return factor * (va - vb);
        });
      }

      var visibleTableItems = [];
      var sortFrag = document.createDocumentFragment();
      if (cfg.includeAggregateRowInTable) {
        allItems.filter(function (x) { return x.isAggregate; }).forEach(function (agg) {
          var aggTr = rowMap.get(agg.key);
          if (aggTr) sortFrag.appendChild(aggTr);
        });
      }
      sortedTableItems.forEach(function (item) {
        var labelMatch = !hasPageLabels || state.label === "ALL" || item.labels.indexOf(state.label) !== -1;
        var searchHaystack = [item.name || ""]
          .concat(item.labels || [])
          .concat(item.searchBlob ? [item.searchBlob] : [])
          .join(" ");
        var textMatch = matchesXltFilter(searchHaystack, state.query, item);
        var show = labelMatch && textMatch;
        var tr = rowMap.get(item.key);
        var dTr = drawerMap.get(item.key);
        if (tr) {
          tr.hidden = !show;
          if (show) {
            visibleTableItems.push(item);
            var idxCell = tr.firstElementChild;
            if (idxCell) idxCell.textContent = String(visibleTableItems.length);
          }
          sortFrag.appendChild(tr);
          if (dTr) {
            dTr.hidden = !show;
            sortFrag.appendChild(dTr);
          }
        }
      });
      tbody.appendChild(sortFrag);

      // Update active navigable list (Aggregate first when unfiltered, plus visible items)
      var aggItems = allItems.filter(function (x) { return x.isAggregate; });
      var isFiltered = !!(state.query.trim() || (hasPageLabels && state.label !== "ALL"));
      state.activeList = (!isFiltered ? aggItems.concat(visibleTableItems) : visibleTableItems.slice());
      if (!state.activeList.length && aggItems.length) state.activeList = aggItems.slice();

      // Update Filtered Totals row
      if (tfoot && tfoot.rows.length >= 2) {
        var fRow = tfoot.rows[1];
        if (isFiltered) {
          var fTotals = computeFilteredTotals(visibleTableItems);
          var newFRow = buildTotalsTr(fTotals, "Filtered (" + visibleTableItems.length + " of " + tableItems.length + ")", true);
          tfoot.replaceChild(newFRow, fRow);
        } else {
          fRow.hidden = true;
        }
      }

      var countLabel = document.getElementById("dir-visible-count");
      if (countLabel) {
        countLabel.textContent = visibleTableItems.length.toLocaleString("en-US") + " of " + tableItems.length.toLocaleString("en-US") + " shown";
      }

      if (statusEl && !initialLoad && sortCol) {
        statusEl.textContent = "Sorted by " + sortCol.label + ", " + state.sortDir + ", " + visibleTableItems.length + " rows";
      }

      // Preserve current item selection if still in activeList
      if (initialLoad && params.get("item")) {
        var idx = state.activeList.findIndex(function (it) { return it.key === params.get("item"); });
        state.activeIdx = idx >= 0 ? idx : 0;
      } else {
        var cur = state.activeList[state.activeIdx];
        if (!cur) state.activeIdx = 0;
      }

      renderWorkbench();
      renderStream();
    }

    function selectByKey(key, shouldScrollRow) {
      var idx = state.activeList.findIndex(function (it) { return it.key === key; });
      if (idx === -1) return;
      state.activeIdx = idx;
      renderWorkbench();
      if (shouldScrollRow) {
        var tr = rowMap.get(key);
        if (tr) tr.scrollIntoView({ block: "nearest" });
      }
      syncUrl();
    }

    function stepWorkbench(delta, scrollRow) {
      if (!state.activeList.length) return;
      var next = state.activeIdx + delta;
      if (next < 0 || next >= state.activeList.length) return;
      state.activeIdx = next;
      renderWorkbench();
      if (scrollRow) {
        var item = state.activeList[state.activeIdx];
        var tr = item ? rowMap.get(item.key) : null;
        if (tr) tr.scrollIntoView({ block: "nearest" });
      }
      syncUrl();
    }

    function buildStructuredFootnote(item) {
      var hasAnyPct = configuredPcts.some(function (p) { return getItemPercentile(item, p) !== undefined; });
      if (!hasAnyPct && cfg.renderWbFootnote) {
        return cfg.renderWbFootnote(item, escHtml, fmtInt, fmtDec);
      }
      var pctDec = cfg.percentileDecimals !== undefined ? cfg.percentileDecimals : 0;
      var unit = cfg.sparkUnit !== undefined ? cfg.sparkUnit : " ms";
      var pctSummary = formatPercentileSummaryHtml(item, configuredPcts, pctDec, unit);
      var col1 = pctSummary
        ? ('<div class="fn-group">' +
            '<span class="fn-label">Percentiles Distribution</span>' +
            '<span class="fn-val">' + pctSummary + '</span>' +
          '</div>')
        : "";
      var col2 = "";
      if (item.ttfb !== undefined) {
        col2 =
          '<div class="fn-group">' +
            '<span class="fn-label">Network Waterfall &amp; Spread</span>' +
            '<span class="fn-val">TTFB: <strong>' + fmtInt(item.ttfb) + ' ms</strong> • Recv: <strong>' + fmtInt(item.recv) + ' ms</strong> • Range: <strong>' + fmtInt(item.min) + '–' + fmtInt(item.max) + ' ms</strong></span>' +
          '</div>';
      } else {
        col2 =
          '<div class="fn-group">' +
            '<span class="fn-label">Latency Range &amp; Spread</span>' +
            '<span class="fn-val">Min: <strong>' + (pctDec > 0 ? fmtDec(item.min, pctDec) : fmtInt(item.min)) + unit + '</strong> • Max: <strong>' + (pctDec > 0 ? fmtDec(item.max, pctDec) : fmtInt(item.max)) + unit + '</strong> • Std Dev: <strong>' + (pctDec > 0 ? fmtDec(item.dev, 2) : fmtInt(item.dev)) + unit + '</strong></span>' +
          '</div>';
      }
      var col3 = "";
      if (item.recvMean !== undefined && item.recvMean > 0) {
        col3 =
          '<div class="fn-group">' +
            '<span class="fn-label">Payload &amp; Throughput</span>' +
            '<span class="fn-val">Count: <strong>' + fmtInt(item.count) + '</strong> • Mean Recv: <strong>' + fmtInt(item.recvMean) + ' B</strong> • Rate: <strong>' + fmtDec(item.rate, 2) + ' /s</strong></span>' +
          '</div>';
      } else {
        col3 =
          '<div class="fn-group">' +
            '<span class="fn-label">Throughput &amp; Volume</span>' +
            '<span class="fn-val">Executions: <strong>' + fmtInt(item.count) + '</strong> • Rate: <strong>' + fmtDec(item.rate, 2) + ' /s</strong> • Hourly: <strong>' + fmtInt(item.cph) + ' /h</strong></span>' +
          '</div>';
      }
      var col4 = "";
      if (item.errors !== undefined) {
        var errTxt = item.errors > 0
          ? ('<strong style="color:var(--status-fail-text);">' + fmtInt(item.errors) + ' Errors (' + fmtDec(item.errPct || 0, 2) + '%)</strong>')
          : '<strong style="color:var(--status-pass-text);">0 Errors (100% Pass)</strong>';
        var apdexVal = (item.apdex && typeof item.apdex === "object") ? item.apdex.value : item.apdex;
        var extraSignal = item.events !== undefined
          ? (' • Events: <strong style="' + (item.events > 0 ? 'color:var(--warning);' : '') + '">' + fmtInt(item.events) + '</strong>')
          : (apdexVal !== undefined && apdexVal !== null ? (' • Apdex: <strong>' + fmtDec(apdexVal, 2) + '</strong>') : (item.distinctUrls ? (' • Distinct URLs: <strong>' + fmtInt(item.distinctUrls) + '</strong>') : ''));
        col4 =
          '<div class="fn-group">' +
            '<span class="fn-label">Reliability &amp; Telemetry</span>' +
            '<span class="fn-val">' + errTxt + extraSignal + '</span>' +
          '</div>';
      }
      return col1 + col2 + col3 + col4;
    }

    // --- Workbench Hero Stage & Footnote Rendering ---

    function renderWorkbench() {
      var item = state.activeList[state.activeIdx];
      rowMap.forEach(function (tr, k) {
        tr.classList.toggle("is-selected", !!(item && item.key === k));
      });

      if (!item) {
        if (wbName) wbName.textContent = "No matching items";
        if (wbCounter) wbCounter.textContent = "0 of 0";
        if (wbPrev) wbPrev.disabled = true;
        if (wbNext) wbNext.disabled = true;
        if (wbStage) wbStage.innerHTML = '<div class="cell-muted" style="padding:24px;">No items match the current filter.</div>';
        return;
      }

      if (wbBadge) {
        wbBadge.hidden = true;
      }
      var wbLabelsEl = document.getElementById("wb-labels");
      if (!wbLabelsEl && wbBadge && wbBadge.parentElement) {
        wbLabelsEl = document.createElement("div");
        wbLabelsEl.id = "wb-labels";
        wbLabelsEl.className = "wb-labels-wrap";
        wbBadge.parentElement.insertBefore(wbLabelsEl, wbBadge.nextSibling);
      }
      if (wbLabelsEl) {
        if (hasPageLabels && item.labels && item.labels.length) {
          wbLabelsEl.hidden = false;
          wbLabelsEl.innerHTML = renderLabelPills(item.labels);
        } else {
          wbLabelsEl.hidden = true;
          wbLabelsEl.innerHTML = "";
        }
      }
      if (wbName) {
        // Ensure 2-line title stack with metadata subtitle
        var titleArea = wbName.parentElement;
        if (titleArea && !titleArea.classList.contains("wb-title-stack")) {
          var stack = document.createElement("div");
          stack.className = "wb-title-stack";
          titleArea.insertBefore(stack, wbName);
          stack.appendChild(wbName);
          var subEl = document.createElement("span");
          subEl.className = "workbench-sub";
          subEl.id = "wb-sub";
          stack.appendChild(subEl);
        }
        wbName.textContent = item.name;
        wbName.title = item.name;
        var wbSub = document.getElementById("wb-sub");
        if (wbSub) {
          var parts = [];
          if (item.labels && item.labels.length) parts.push("Labels: " + item.labels.join(", "));
          if (item.method) parts.push("Method: " + item.method);
          if (item.urlPattern) parts.push("Rule: " + item.urlPattern);
          else if (item.min !== undefined && item.max !== undefined) parts.push("Range: " + fmtInt(item.min) + " – " + fmtInt(item.max) + " ms");
          if (item.distinctUrls) parts.push(fmtInt(item.distinctUrls) + " distinct URLs");
          wbSub.textContent = parts.join(" • ") || (cfg.entityLabel + " Analytics");
        }
      }
      if (wbCounter) {
        wbCounter.textContent = (cfg.entityLabel || "Item") + " " + (state.activeIdx + 1).toLocaleString("en-US") + " of " + state.activeList.length.toLocaleString("en-US");
      }
      if (wbPrev) wbPrev.disabled = state.activeIdx <= 0;
      if (wbNext) wbNext.disabled = state.activeIdx >= state.activeList.length - 1;

      if (wbKpis && cfg.renderWbKpis) {
        wbKpis.innerHTML = cfg.renderWbKpis(item, escHtml, fmtInt, fmtDec);
        var firstVal = wbKpis.querySelector(".wb-kpi-val");
        if (firstVal) firstVal.classList.add("highlight-cph");
      }

      // Tabs available for this item
      var availTabs = tabs.filter(function (t) { return !t.available || t.available(item); });
      if (!availTabs.some(function (t) { return t.key === state.activeTab; }) && availTabs.length) {
        state.activeTab = availTabs[0].key;
      }

      if (wbTabs) {
        wbTabs.innerHTML = "";
        availTabs.forEach(function (t) {
          var b = document.createElement("button");
          b.type = "button";
          b.className = "wb-tab-btn" + (t.key === state.activeTab ? " active" : "");
          b.textContent = t.label;
          b.addEventListener("click", function () {
            state.activeTab = t.key;
            renderWorkbench();
            syncUrl();
          });
          wbTabs.appendChild(b);
        });
      }

      var activeTabObj = availTabs.find(function (t) { return t.key === state.activeTab; }) || availTabs[0];
      if (wbStage && activeTabObj && !state.collapsed) {
        if (!chartsAvailable) {
          wbStage.innerHTML = '<div class="cell-muted" style="padding:28px;text-align:center;">Charts were not generated for this report (<code>-noCharts</code>).</div>';
        } else if (activeTabObj.type === "echart") {
          wbStage.innerHTML = '<div class="wb-echart-box" id="wb-echart-canvas"></div>';
          var canvas = document.getElementById("wb-echart-canvas");
          if (window.XltDynamicChart) {
            window.XltDynamicChart.render(canvas, buildChartUrl(item, activeTabObj), item.name);
          }
        } else {
          var src = buildChartUrl(item, activeTabObj);
          var title = item.name + " — " + activeTabObj.label;
          wbStage.innerHTML = '<img class="zoomable-chart" src="' + escHtml(src) + '" alt="' + escHtml(title) + '" data-chart-title="' + escHtml(title) + '" />';
        }
      }

      if (wbFootnote) {
        wbFootnote.innerHTML = buildStructuredFootnote(item);
      }
    }

    function buildStreamStatsHtml(it) {
      var stats = [];
      if (it.totalCpuMean !== undefined) {
        stats.push('<span class="stream-stat-item">Tx: <strong>' + fmtInt(it.totalTx) + '</strong></span>');
        stats.push('<span class="stream-stat-item">CPU: <strong>' + fmtDec(it.totalCpuMean, 1) + '%</strong></span>');
        stats.push('<span class="stream-stat-item">Minor GC: <strong>' + fmtDec(it.minorGcCpu, 2) + '%</strong></span>');
        stats.push('<span class="stream-stat-item">Errors: <strong style="color:' + (it.errors > 0 ? 'var(--status-fail-text)' : 'var(--status-pass-text)') + ';">' + fmtInt(it.errors) + '</strong></span>');
        stats.push('<span class="stream-stat-item">Full GC: <strong>' + fmtInt(it.fullGcCount) + '</strong></span>');
      } else {
        var unit = cfg.sparkUnit !== undefined ? cfg.sparkUnit : (cfg.entityLabel === "Custom Value" ? "" : " ms");
        var pctKey = configuredPcts.indexOf("95") !== -1 ? "95" : (configuredPcts.length ? configuredPcts[Math.min(1, configuredPcts.length - 1)] : null);
        var pctVal = pctKey ? getItemPercentile(it, pctKey) : undefined;
        if (it.cph !== undefined) stats.push('<span class="stream-stat-item">1/h: <strong style="color:var(--brand-blue);">' + fmtInt(it.cph) + '</strong></span>');
        if (it.rate !== undefined) stats.push('<span class="stream-stat-item">1/s: <strong>' + fmtDec(it.rate, 2) + '</strong></span>');
        if (it.mean !== undefined) stats.push('<span class="stream-stat-item">Mean: <strong>' + ((cfg.meanDecimals && cfg.meanDecimals > 0) ? fmtDec(it.mean, cfg.meanDecimals) : fmtInt(it.mean)) + unit + '</strong></span>');
        if (pctVal !== undefined) stats.push('<span class="stream-stat-item">P' + escHtml(pctKey) + ': <strong>' + (cfg.percentileDecimals > 0 ? fmtDec(pctVal, cfg.percentileDecimals) : fmtInt(pctVal)) + unit + '</strong></span>');
        if (it.ttfb !== undefined) stats.push('<span class="stream-stat-item">TTFB: <strong>' + fmtInt(it.ttfb) + ' ms</strong></span>');
        if (it.errors !== undefined) {
          stats.push('<span class="stream-stat-item">Errors: <strong style="color:' + (it.errors > 0 ? 'var(--status-fail-text)' : 'var(--status-pass-text)') + ';">' + fmtInt(it.errors) + '</strong></span>');
        }
      }
      return stats.join("");
    }

    // Always-visible Continuous Chart Stream below the directory table with lazy IntersectionObserver batching
    var streamObserver = null;
    function renderStream() {
      if (!streamRoot) return;
      if (streamObserver) { streamObserver.disconnect(); streamObserver = null; }
      if (!chartsAvailable) {
        streamRoot.hidden = true;
        streamRoot.innerHTML = "";
        return;
      }
      streamRoot.hidden = false;

      var imgTabs = tabs.filter(function (t) { return t.type !== "echart"; });
      if (!imgTabs.some(function (t) { return t.key === state.streamTab; }) && imgTabs.length) {
        state.streamTab = imgTabs[0].key;
      }
      var tabObj = imgTabs.find(function (t) { return t.key === state.streamTab; }) || imgTabs[0];

      var tabsHtml = imgTabs.map(function (t) {
        return '<button type="button" class="wb-tab-btn' + (t.key === state.streamTab ? ' active' : '') + '" data-stream-tab="' + escHtml(t.key) + '">' + escHtml(t.label) + '</button>';
      }).join("");

      var isFiltered = !!(state.query.trim() || (hasPageLabels && state.label !== "ALL"));
      var countText = state.activeList.length.toLocaleString("en-US") + (isFiltered ? " matching charts" : " charts");

      streamRoot.innerHTML =
        '<div class="card">' +
          '<div class="card-header stream-sticky-header">' +
            '<div class="card-title">' +
              '<span>Continuous ' + escHtml(cfg.entityLabel || "Chart") + ' Stream</span>' +
              '<span class="badge-tag">' + escHtml(countText) + '</span>' +
            '</div>' +
            '<div class="stream-toolbar-controls">' +
              '<span class="stream-glance-label">Glance Metric:</span>' +
              '<div class="wb-tabs">' + tabsHtml + '</div>' +
              '<button type="button" class="btn-chip" id="btn-stream-to-top" title="Scroll back up to Directory Table">↑ Table</button>' +
            '</div>' +
          '</div>' +
          '<div class="card-body">' +
            '<div class="stream-grid" id="stream-cards-grid"></div>' +
            '<div class="stream-load-bar" id="stream-load-bar" hidden></div>' +
            '<div id="stream-sentinel" style="height:16px;"></div>' +
          '</div>' +
        '</div>';

      Array.prototype.forEach.call(streamRoot.querySelectorAll("[data-stream-tab]"), function (b) {
        b.addEventListener("click", function () {
          state.streamTab = b.getAttribute("data-stream-tab");
          state.activeTab = state.streamTab;
          renderStream();
          renderWorkbench();
          syncUrl();
        });
      });

      var grid = document.getElementById("stream-cards-grid");
      var loadBar = document.getElementById("stream-load-bar");
      var sentinel = document.getElementById("stream-sentinel");

      var btnTop = document.getElementById("btn-stream-to-top");
      if (btnTop) {
        btnTop.addEventListener("click", function () {
          var target = dirRoot || wbRoot;
          if (target) target.scrollIntoView({ behavior: "smooth", block: "start" });
        });
      }

      if (!state.activeList.length) {
        grid.innerHTML = '<div class="cell-muted" style="grid-column:1/-1;padding:24px;text-align:center;">No charts match the current filter.</div>';
        return;
      }

      var itemByKey = new Map();
      state.activeList.forEach(function (it) { itemByKey.set(it.key, it); });

      var cursor = 0;
      var BATCH = 24;

      function updateLoadBar() {
        if (!loadBar) return;
        var remaining = state.activeList.length - cursor;
        if (remaining <= 0) {
          loadBar.hidden = true;
          loadBar.innerHTML = "";
          return;
        }
        loadBar.hidden = false;
        loadBar.innerHTML =
          '<span>Showing <strong>' + cursor.toLocaleString("en-US") + '</strong> of <strong>' + state.activeList.length.toLocaleString("en-US") + '</strong> charts</span>' +
          '<button type="button" class="btn-chip" id="btn-stream-next">Load Next ' + Math.min(BATCH, remaining) + ' Charts</button>' +
          (remaining > BATCH ? '<button type="button" class="btn-chip" id="btn-stream-all">Load All Remaining (' + remaining.toLocaleString("en-US") + ')</button>' : '');
        var btnNext = document.getElementById("btn-stream-next");
        var btnAll = document.getElementById("btn-stream-all");
        if (btnNext) btnNext.addEventListener("click", function () { appendBatch(BATCH); });
        if (btnAll) btnAll.addEventListener("click", function () { appendBatch(remaining); });
      }

      function appendBatch(count) {
        var step = count || BATCH;
        var end = Math.min(cursor + step, state.activeList.length);
        var frag = document.createDocumentFragment();
        for (var i = cursor; i < end; i++) {
          var it = state.activeList[i];
          var card = document.createElement("article");
          card.className = "stream-card";
          card.setAttribute("data-stream-key", it.key);
          var cardLabelsHtml = (hasPageLabels && it.labels && it.labels.length)
            ? renderLabelPills(it.labels)
            : "";
          var src = buildChartUrl(it, tabObj);
          var title = it.name + " — " + tabObj.label;
          var subHtml = (!it.isAggregate && it.urlPattern)
            ? '<span class="stream-card-sub" title="' + escHtml(it.urlPattern) + '">' + escHtml(it.urlPattern) + '</span>'
            : '';
          var perCardTabs = imgTabs.map(function (t) {
            return '<button type="button" class="stream-card-tab' + (t.key === tabObj.key ? ' active' : '') + '" data-card-tab="' + escHtml(t.key) + '">' + escHtml(t.label) + '</button>';
          }).join("");

          card.innerHTML =
            '<header class="stream-card-head">' +
              '<div class="stream-card-top-row">' +
                '<div class="stream-card-title-group">' +
                  '<span class="stream-card-rank">#' + (i + 1) + '</span>' +
                  cardLabelsHtml +
                  '<div class="stream-card-name-stack">' +
                    '<span class="stream-card-name" title="' + escHtml(it.name) + '">' + escHtml(it.name) + '</span>' +
                    subHtml +
                  '</div>' +
                '</div>' +
                '<div class="stream-card-actions">' +
                  (imgTabs.length > 1 ? '<div class="stream-card-tabs">' + perCardTabs + '</div>' : '') +
                  '<button type="button" class="btn-chip" data-table-key="' + escHtml(it.key) + '" title="Jump to this row in Directory Table">↑ Table</button>' +
                  '<button type="button" class="btn-chip" data-inspect-key="' + escHtml(it.key) + '" title="Load in Top Workbench">Inspect ↗</button>' +
                '</div>' +
              '</div>' +
              '<div class="stream-card-stats">' + buildStreamStatsHtml(it) + '</div>' +
            '</header>' +
            '<div class="stream-card-body chart-canvas-light">' +
              '<img class="zoomable-chart" loading="lazy" decoding="async" src="' + escHtml(src) + '" alt="' + escHtml(title) + '" data-chart-title="' + escHtml(title) + '"/>' +
            '</div>';
          frag.appendChild(card);
        }
        grid.appendChild(frag);
        cursor = end;
        updateLoadBar();
        if (cursor >= state.activeList.length && streamObserver) {
          streamObserver.disconnect();
        }
      }

      grid.addEventListener("click", function (e) {
        var cardTabBtn = e.target.closest("[data-card-tab]");
        if (cardTabBtn) {
          var card = cardTabBtn.closest(".stream-card");
          var key = card ? card.getAttribute("data-stream-key") : "";
          var it = itemByKey.get(key);
          var tKey = cardTabBtn.getAttribute("data-card-tab");
          var targetTab = imgTabs.find(function (t) { return t.key === tKey; });
          if (card && it && targetTab) {
            Array.prototype.forEach.call(card.querySelectorAll("[data-card-tab]"), function (b) {
              b.classList.toggle("active", b === cardTabBtn);
            });
            var img = card.querySelector("img.zoomable-chart");
            if (img) {
              var nextSrc = buildChartUrl(it, targetTab);
              var nextTitle = it.name + " — " + targetTab.label;
              img.src = nextSrc;
              img.alt = nextTitle;
              img.setAttribute("data-chart-title", nextTitle);
            }
          }
          return;
        }
        var tblBtn = e.target.closest("[data-table-key]");
        if (tblBtn) {
          selectByKey(tblBtn.getAttribute("data-table-key"), true);
          return;
        }
        var inspectBtn = e.target.closest("[data-inspect-key]");
        if (inspectBtn) {
          selectByKey(inspectBtn.getAttribute("data-inspect-key"), false);
          if (wbRoot) wbRoot.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      });

      appendBatch(BATCH);
      if (cursor < state.activeList.length && "IntersectionObserver" in window) {
        streamObserver = new IntersectionObserver(function (entries) {
          if (entries.some(function (en) { return en.isIntersecting; })) {
            appendBatch(BATCH);
          }
        }, { rootMargin: "800px" });
        streamObserver.observe(sentinel);
      }
    }

    function setView(v, scrollBehavior) {
      state.view = v;
      if (wbRoot) wbRoot.hidden = false;
      if (streamRoot) streamRoot.hidden = !chartsAvailable;
      if (btnViewWb) btnViewWb.classList.toggle("active", v === "workbench");
      if (btnViewStream) {
        btnViewStream.hidden = !chartsAvailable;
        btnViewStream.classList.toggle("active", v === "stream");
      }
      if (scrollBehavior) {
        var beh = scrollBehavior === "auto" ? "auto" : "smooth";
        if (v === "stream" && streamRoot) {
          streamRoot.scrollIntoView({ behavior: beh, block: "start" });
        } else if (v === "workbench" && wbRoot) {
          wbRoot.scrollIntoView({ behavior: beh, block: "start" });
        }
      }
      syncUrl();
    }

    // Wire Controls
    if (wbPrev) wbPrev.addEventListener("click", function () { stepWorkbench(-1, false); });
    if (wbNext) wbNext.addEventListener("click", function () { stepWorkbench(1, false); });

    function syncCollapseBtn() {
      if (!btnCollapse || !wbBody) return;
      wbBody.hidden = state.collapsed;
      btnCollapse.setAttribute("aria-expanded", state.collapsed ? "false" : "true");
      btnCollapse.title = state.collapsed ? "Show chart" : "Hide chart";
    }

    if (btnCollapse && wbBody) {
      syncCollapseBtn();
      btnCollapse.addEventListener("click", function () {
        state.collapsed = !state.collapsed;
        syncCollapseBtn();
        if (!state.collapsed) renderWorkbench();
      });
    }

    function syncPinBtn() {
      if (!btnPin || !wbRoot) return;
      wbRoot.classList.toggle("unpinned", !state.pinned);
      btnPin.classList.toggle("active", state.pinned);
      btnPin.setAttribute("aria-pressed", state.pinned ? "true" : "false");
      btnPin.title = state.pinned ? "Pinned to top — click to unpin" : "Unpinned — click to pin to top";
      btnPin.innerHTML = state.pinned ? ICON_PIN : ICON_PIN_OFF;
    }

    if (btnPin && wbRoot) {
      syncPinBtn();
      btnPin.addEventListener("click", function () {
        state.pinned = !state.pinned;
        syncPinBtn();
      });
    }

    var linkTimer = null;
    if (btnCopyLink) {
      var origLinkTitle = btnCopyLink.getAttribute("title") || "Copy shareable link with current item & chart tab";
      btnCopyLink.addEventListener("click", function () {
        syncUrl();
        function reportLinkResult(ok) {
          clearTimeout(linkTimer);
          btnCopyLink.removeAttribute("data-copied");
          btnCopyLink.removeAttribute("data-failed");
          btnCopyLink.classList.remove("copied");
          void btnCopyLink.offsetWidth;
          if (ok) {
            btnCopyLink.innerHTML = ICON_CHECK;
            btnCopyLink.setAttribute("data-copied", "");
            btnCopyLink.classList.add("copied");
            btnCopyLink.title = "Link copied";
            announceWbStatus("Shareable link copied");
          } else {
            btnCopyLink.innerHTML = ICON_LINK;
            btnCopyLink.setAttribute("data-failed", "");
            btnCopyLink.title = "Copy failed";
            announceWbStatus("Copy failed");
          }
          linkTimer = setTimeout(function () {
            btnCopyLink.innerHTML = ICON_LINK;
            btnCopyLink.removeAttribute("data-copied");
            btnCopyLink.removeAttribute("data-failed");
            btnCopyLink.classList.remove("copied");
            btnCopyLink.title = origLinkTitle;
            announceWbStatus("");
          }, 1400);
        }
        writeClipboardText(window.location.href).then(
          function () { reportLinkResult(true); },
          function () { reportLinkResult(false); }
        );
      });
    }

    if (btnZoom && wbStage) {
      btnZoom.addEventListener("click", function () {
        var img = wbStage.querySelector("img.zoomable-chart");
        if (img) img.click();
      });
    }

    if (btnUnpin) {
      btnUnpin.addEventListener("click", function () {
        state.activeIdx = 0;
        renderWorkbench();
        syncUrl();
      });
    }

    if (btnViewWb) btnViewWb.addEventListener("click", function () { setView("workbench", "smooth"); });
    if (btnViewStream) btnViewStream.addEventListener("click", function () { setView("stream", "smooth"); });

    if (filterInput) {
      filterInput.title = "Filter by text, or expressions like: has errors, count > 100, < 500 ms, p95 > 10,000 (space = AND, | = OR, - = exclude)";
      if (filterInput.placeholder && filterInput.placeholder.indexOf("has errors") === -1) {
        filterInput.placeholder = filterInput.placeholder.replace(/\.\.\.\s*$/, "") + " (e.g. has errors, < 500 ms, p95 > 10,000)...";
      }
      filterInput.value = state.query;
      filterInput.addEventListener("input", function () {
        state.query = filterInput.value;
        state.activeIdx = 0;
        applySortAndFilter(false);
        syncUrl();
      });
    }

    if (tableTabsBar) {
      tableTabsBar.innerHTML = "";
      if (!tableViews || tableViews.length < 2) {
        tableTabsBar.hidden = true;
      } else {
        tableTabsBar.hidden = false;
        tableViews.forEach(function (v) {
          var b = document.createElement("button");
          b.type = "button";
          b.setAttribute("role", "tab");
          var isActive = v.key === state.tableView;
          b.className = "wb-tab-btn" + (isActive ? " active" : "");
          b.setAttribute("aria-selected", isActive ? "true" : "false");
          b.setAttribute("data-table-view", v.key);
          b.textContent = v.label;
          b.addEventListener("click", function () {
            if (state.tableView === v.key) return;
            var openKeys = Array.from(drawerMap.keys());
            drawerMap.clear();
            rowMap.clear();
            state.tableView = v.key;
            Array.prototype.forEach.call(tableTabsBar.querySelectorAll("[data-table-view]"), function (btn) {
              var activeNow = btn === b;
              btn.classList.toggle("active", activeNow);
              btn.setAttribute("aria-selected", activeNow ? "true" : "false");
            });
            columns = buildExpandedColumns(v.columns);
            if (!columns.some(function (c) { return c.key === state.sortKey; })) {
              state.sortKey = (v.defaultSort && v.defaultSort.key) || (cfg.defaultSort && cfg.defaultSort.key) || "name";
              state.sortDir = (v.defaultSort && v.defaultSort.dir) || (cfg.defaultSort && cfg.defaultSort.dir) || "ascending";
            }
            buildTableDom();
            openKeys.forEach(function (k) {
              var it = allItems.find(function (x) { return x.key === k; });
              if (it) toggleDrawer(it);
            });
            applySortAndFilter(false);
            syncUrl();
          });
          tableTabsBar.appendChild(b);
        });
      }
    }

    if (catBar) {
      catBar.innerHTML = "";
      if (!hasPageLabels) {
        catBar.hidden = true;
      } else {
        catBar.hidden = false;
        var labelCounts = new Map();
        tableItems.forEach(function (it) {
          var seen = {};
          (it.labels || []).forEach(function (lbl) {
            if (!seen[lbl]) {
              seen[lbl] = true;
              labelCounts.set(lbl, (labelCounts.get(lbl) || 0) + 1);
            }
          });
        });
        var sortedLabels = Array.from(labelCounts.keys()).sort(function (a, b) {
          return collator.compare(a, b);
        });
        if (state.label !== "ALL" && !labelCounts.has(state.label)) {
          state.label = "ALL";
        }
        var chips = [{ key: "ALL", label: "All (" + fmtInt(tableItems.length) + ")" }].concat(
          sortedLabels.map(function (lbl) {
            return { key: lbl, label: lbl + " (" + fmtInt(labelCounts.get(lbl)) + ")" };
          })
        );
        chips.forEach(function (chip) {
          var b = document.createElement("button");
          b.type = "button";
          b.className = "btn-chip" + (state.label === chip.key ? " active" : "");
          b.setAttribute("data-label-chip", chip.key);
          b.textContent = chip.label;
          b.addEventListener("click", function () {
            state.label = chip.key;
            state.activeIdx = 0;
            Array.prototype.forEach.call(catBar.children, function (ch) { ch.classList.remove("active"); });
            b.classList.add("active");
            applySortAndFilter(false);
            syncUrl();
          });
          catBar.appendChild(b);
        });
      }
    }

    // Keyboard ergonomics (never hijack when dialog[open] or typing in input!)
    document.addEventListener("keydown", function (e) {
      if (document.querySelector("dialog[open]")) return;
      var tag = e.target && e.target.tagName ? e.target.tagName.toLowerCase() : "";
      if (tag === "input" || tag === "textarea" || tag === "select") {
        if (e.key === "Escape" && filterInput && document.activeElement === filterInput && filterInput.value) {
          e.preventDefault();
          filterInput.value = "";
          state.query = "";
          applySortAndFilter(false);
          syncUrl();
        }
        return;
      }

      if (e.key === "ArrowRight") {
        e.preventDefault();
        stepWorkbench(1, false);
      } else if (e.key === "ArrowLeft") {
        e.preventDefault();
        stepWorkbench(-1, false);
      } else if (e.key === "ArrowDown") {
        e.preventDefault();
        stepWorkbench(1, true);
      } else if (e.key === "ArrowUp") {
        e.preventDefault();
        stepWorkbench(-1, true);
      } else if (e.key === "Enter" && !e.target.closest("button,a")) {
        var cur = state.activeList[state.activeIdx];
        if (cur && !cur.isAggregate) {
          e.preventDefault();
          toggleDrawer(cur);
        }
      }
    });

    buildTableDom();
    applySortAndFilter(true);
    setView(state.view, state.view === "stream" ? "auto" : false);
  }

  /**
   * Public workbench API used by page initialization scripts (`transactions.html`,
   * `actions.html`, `requests.html`, `page-load-timings.html`, `custom-timers.html`,
   * `custom-values.html`, and `agents.html`).
   */
  window.XltWorkbench = {
    init: initWorkbenchPage,
    fmtInt: fmtInt,
    fmtDec: fmtDec,
    escHtml: escHtml,
    getConfiguredPercentiles: getConfiguredPercentiles,
    getItemPercentile: getItemPercentile,
    getConfiguredIntervals: getConfiguredIntervals,
    getItemSegmentation: getItemSegmentation,
    formatIntervalLabel: formatIntervalLabel,
    formatIntervalRange: formatIntervalRange,
    formatPercentileSummaryHtml: formatPercentileSummaryHtml,
    getApdexInfo: getApdexInfo,
    buildColorizationMap: buildColorizationMap,
    evaluateCellColorization: evaluateCellColorization
  };
})();

