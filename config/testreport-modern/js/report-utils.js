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
 * XLT Modern Load Report — Shared Formatting, Escaping & Query Utilities (`window.XLT_UI`)
 *
 * @fileoverview
 * Standalone classic script loaded synchronously in `<head>` across every report HTML page
 * immediately after `js/report-meta.js` and before `js/nav.js`:
 *
 *   1. `js/theme.js`         — applies `html.dark-theme` before first paint to prevent FOUC
 *   2. `js/report-meta.js`   — generated global `window.XLT_META` bundle
 *   3. `js/report-utils.js`  — shared helpers (`window.XLT_UI`)
 *   4. `js/nav.js`           — header metadata badges, nav pills, accessible dropdowns
 *   5. Page data bundles (`window.XLT_*`) & page renderers (`*-page.js`, `workbench.js`)
 *
 * Architectural Note (`file://` Portability):
 * Generated XLT reports are self-contained static directories meant to be opened directly
 * from the filesystem (`file://`) as well as over HTTP(S). Because Chromium/Firefox block ES module
 * `<script type="module">` imports under `file://` due to opaque-origin CORS rules, all data
 * bundles and UI modules use classic IIFE scripts communicating through `window.XLT_*` globals.
 *
 * Formatting Conventions:
 * - Numeric output uses `en-US` locale grouping (`1,234.5`) paired with CSS `tabular-nums`.
 * - Missing-value fallback differs intentionally by context:
 *   * Summary/overview dashboards and count tables use `fmtInt(n)` / `fmtDec(n, d)` -> `"0"`.
 *   * Analytical workbench tables and Core Web Vitals metrics use `fmtIntOrDash(n)` /
 *     `fmtDecOrDash(n, d)` -> `"—"` so absent/unmeasured samples are visually distinct from zero.
 *
 * @global
 * @namespace XLT_UI
 */
(function () {
  "use strict";

  /**
   * Shared natural numeric-aware collator for consistent lexical table sorting
   * (e.g., `"Agent-2"` sorts before `"Agent-10"`).
   * @type {Intl.Collator}
   */
  var collator = new Intl.Collator(undefined, { numeric: true, sensitivity: "base" });

  /**
   * Escapes HTML metacharacters for safe interpolation into text nodes and
   * double-quoted HTML attributes (`data-*`, `title`, `href`, etc.).
   *
   * @param {*} s Raw value (`null` and `undefined` serialize as `""`).
   * @returns {string} Escaped string safe for `innerHTML`.
   */
  function esc(s) {
    return String(s == null ? "" : s)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  /**
   * Formats a number rounded to the nearest integer with `en-US` thousands separators.
   *
   * @param {number|string|null|undefined} n Input numeric value.
   * @param {string} [fallback="0"] String returned when `n` is `null`, `undefined`, or `NaN`.
   * @returns {string} Formatted integer string or `fallback`.
   */
  function fmtInt(n, fallback) {
    var fb = fallback !== undefined ? fallback : "0";
    if (n === null || n === undefined || n === "" || Number.isNaN(Number(n))) return fb;
    return Math.round(Number(n)).toLocaleString("en-US");
  }

  /**
   * Formats an integer or returns `"—"` when absent (used by dense workbench tables
   * where an unrecorded metric must not be mistaken for a literal `0`).
   *
   * @param {number|string|null|undefined} n Input numeric value.
   * @returns {string} Formatted integer string or `"—"`.
   */
  function fmtIntOrDash(n) {
    return fmtInt(n, "—");
  }

  /**
   * Formats a number with fixed fractional precision and `en-US` thousands separators.
   *
   * @param {number|string|null|undefined} n Input numeric value.
   * @param {number} [digits=2] Fractional digit count (applied to both min and max digits).
   * @param {string} [fallback="0"] String returned when `n` is `null`, `undefined`, or `NaN`.
   * @returns {string} Formatted decimal string or `fallback`.
   */
  function fmtDec(n, digits, fallback) {
    var fb = fallback !== undefined ? fallback : "0";
    if (n === null || n === undefined || n === "" || Number.isNaN(Number(n))) return fb;
    var d = digits === undefined ? 2 : digits;
    return Number(n).toLocaleString("en-US", {
      minimumFractionDigits: d,
      maximumFractionDigits: d
    });
  }

  /**
   * Formats a decimal number or returns `"—"` when absent.
   *
   * @param {number|string|null|undefined} n Input numeric value.
   * @param {number} [digits=1] Fractional digit count.
   * @returns {string} Formatted decimal string or `"—"`.
   */
  function fmtDecOrDash(n, digits) {
    return fmtDec(n, digits === undefined ? 1 : digits, "—");
  }

  /**
   * Formats large integer counts compactly for header navigation pills (e.g., `1.2M`, `12.3k`).
   * Values under `10,000` render as full integers (`9,850`).
   *
   * @param {number|string|null|undefined} n Input count.
   * @returns {string} Compact formatted count string.
   */
  function fmtCompact(n) {
    if (n === null || n === undefined || n === "" || Number.isNaN(Number(n))) return "0";
    var v = Number(n);
    if (Math.abs(v) >= 1000000) {
      return (v / 1000000).toFixed(1).replace(/\.0$/, "") + "M";
    }
    if (Math.abs(v) >= 10000) {
      return (v / 1000).toFixed(1).replace(/\.0$/, "") + "k";
    }
    return fmtInt(v, "0");
  }

  /**
   * Splits a byte count into `{ val, unit }` using binary 1024-stepped units (`B`..`TB`),
   * matching Classic XLT bandwidth reporting conventions (`0` decimals for `B`, `1` for `KB`+).
   *
   * @param {number|string|null|undefined} bytes Total byte count.
   * @returns {{ val: string, unit: string }} Separated numeric value string and unit label.
   */
  function fmtBytesParts(bytes) {
    var b = Number(bytes) || 0;
    if (b >= 1099511627776) return { val: fmtDec(b / 1099511627776, 1, "0"), unit: "TB" };
    if (b >= 1073741824) return { val: fmtDec(b / 1073741824, 1, "0"), unit: "GB" };
    if (b >= 1048576) return { val: fmtDec(b / 1048576, 1, "0"), unit: "MB" };
    if (b >= 1024) return { val: fmtDec(b / 1024, 1, "0"), unit: "KB" };
    return { val: fmtInt(b, "0"), unit: "B" };
  }

  /**
   * Formats a byte count into a single human-readable string (e.g., `"46.8 GB"`).
   *
   * @param {number|string|null|undefined} bytes Total byte count.
   * @returns {string} Combined formatted value and unit.
   */
  function fmtBytes(bytes) {
    var p = fmtBytesParts(bytes);
    return p.val + " " + p.unit;
  }

  /**
   * Formats a duration in seconds as zero-padded `HH:MM:SS` (e.g., `01:05:09`).
   *
   * @param {number|string|null|undefined} sec Duration in seconds.
   * @returns {string} Zero-padded `HH:MM:SS` string.
   */
  function fmtDurationHms(sec) {
    var s = Math.max(0, Math.round(Number(sec) || 0));
    var h = Math.floor(s / 3600);
    var m = Math.floor((s % 3600) / 60);
    var rem = s % 60;
    return (
      String(h).padStart(2, "0") +
      ":" +
      String(m).padStart(2, "0") +
      ":" +
      String(rem).padStart(2, "0")
    );
  }

  /**
   * Formats an XLT load-profile think time + deviation pair into a human-readable range.
   *
   * @param {number|string|null|undefined} thinkMs Configured base think time in milliseconds.
   * @param {number|string|null|undefined} devMs Configured symmetric deviation in milliseconds.
   * @returns {string} Formatted think-time string (`"0 ms"`, `"min...max ms"`, or `"t ms"`).
   */
  function fmtThinkTime(thinkMs, devMs) {
    var t = Number(thinkMs) || 0;
    var d = Number(devMs) || 0;
    if (t === 0 && d === 0) return "0 ms";
    if (d > 0) {
      return fmtInt(Math.max(0, t - d), "0") + "..." + fmtInt(t + d, "0") + " ms";
    }
    return fmtInt(t, "0") + " ms";
  }

  /**
   * Looks up a configured percentile value from a summary or item record, accommodating
   * all `ModernReportGenerator` JSON shapes (`percentiles.p95`, `percentiles["95"]`,
   * `percentiles.p999`, and legacy flat `p95` / `p999` keys).
   *
   * @param {Object|null|undefined} item Metric record containing `percentiles` or flat `p*` keys.
   * @param {string|number|null|undefined} pctStr Percentile identifier (e.g., `"95"` or `"99.9"`).
   * @returns {number|undefined} Parsed finite numeric percentile value, or `undefined` if absent.
   */
  function getPercentile(item, pctStr) {
    if (!item || pctStr == null) return undefined;
    var s = String(pctStr).trim();
    if (!s) return undefined;
    var noDot = s.replace(/\./g, "");
    var val;
    if (item.percentiles && typeof item.percentiles === "object") {
      if (item.percentiles["p" + s] !== undefined) val = item.percentiles["p" + s];
      else if (item.percentiles[s] !== undefined) val = item.percentiles[s];
      else if (item.percentiles["p" + noDot] !== undefined) val = item.percentiles["p" + noDot];
    }
    if (val === undefined) {
      if (item["p" + s] !== undefined) val = item["p" + s];
      else if (item["p" + noDot] !== undefined) val = item["p" + noDot];
    }
    if (val === undefined || val === null || val === "") return undefined;
    var num = Number(val);
    return Number.isNaN(num) ? undefined : num;
  }

  /**
   * Resolves the ordered list of configured percentile labels (e.g. `["50", "95", "99", "99.9"]`),
   * preferring `window.XLT_META.testReportConfig.runtimePercentiles`, falling back to keys
   * inspected on sample items, and finally defaulting to XLT's standard percentile set.
   *
   * @param {Array<Object>} [sampleItems] Optional array of metric records for key auto-detection.
   * @returns {Array<string>} Ordered list of percentile key suffixes without leading `"p"`.
   */
  function getConfiguredPercentiles(sampleItems) {
    var metaCfg = window.XLT_META && window.XLT_META.testReportConfig;
    if (metaCfg && Array.isArray(metaCfg.runtimePercentiles)) {
      return metaCfg.runtimePercentiles
        .map(function (p) { return String(p == null ? "" : p).trim(); })
        .filter(Boolean);
    }
    if (Array.isArray(sampleItems)) {
      for (var i = 0; i < sampleItems.length; i++) {
        var it = sampleItems[i];
        if (it && it.percentiles && typeof it.percentiles === "object") {
          var keys = Object.keys(it.percentiles);
          if (keys.length) {
            return keys.map(function (k) { return k.replace(/^p/i, ""); });
          }
        }
      }
    }
    return ["50", "95", "99", "99.9"];
  }

  /**
   * Evaluates a search string using the classic XLT table filter DSL:
   * - `|` separates OR branches (a match in any branch returns `true`).
   * - Whitespace separates AND tokens within each branch.
   * - Leading `-` negates a token (substring must NOT appear).
   * - Matching is case-insensitive substring comparison.
   *
   * @param {string} haystack Concatenated searchable text for a row or card.
   * @param {string} query User filter input string.
   * @returns {boolean} `true` if `haystack` satisfies the filter query.
   */
  function matchesFilter(haystack, query) {
    var q = String(query == null ? "" : query).trim().toLowerCase();
    if (!q) return true;
    var text = String(haystack == null ? "" : haystack).toLowerCase();
    var branches = q.split("|");
    for (var b = 0; b < branches.length; b++) {
      var tokens = branches[b].trim().split(/\s+/);
      var ok = true;
      for (var t = 0; t < tokens.length; t++) {
        var tok = tokens[t];
        if (!tok) continue;
        if (tok.charAt(0) === "-" && tok.length > 1) {
          if (text.indexOf(tok.slice(1)) !== -1) {
            ok = false;
            break;
          }
        } else {
          if (text.indexOf(tok) === -1) {
            ok = false;
            break;
          }
        }
      }
      if (ok) return true;
    }
    return false;
  }

  window.XLT_UI = {
    collator: collator,
    esc: esc,
    fmtInt: fmtInt,
    fmtIntOrDash: fmtIntOrDash,
    fmtDec: fmtDec,
    fmtDecOrDash: fmtDecOrDash,
    fmtCompact: fmtCompact,
    fmtBytesParts: fmtBytesParts,
    fmtBytes: fmtBytes,
    fmtDurationHms: fmtDurationHms,
    fmtThinkTime: fmtThinkTime,
    getPercentile: getPercentile,
    getConfiguredPercentiles: getConfiguredPercentiles,
    matchesFilter: matchesFilter
  };
})();
