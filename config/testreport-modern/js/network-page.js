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
 * XLT Modern Load Report — Network Analytics Renderer (`network.html`)
 *
 * @fileoverview
 * Populates the network traffic and HTTP protocol telemetry page from `window.XLT_NETWORK`:
 *   1. Top 4-card bandwidth & throughput KPI grid (`#sec-network`).
 *   2. Static chart visibility toggle (`#net-charts-section` hidden if `chartsGenerated === false`).
 *   3. Sortable breakdown tables for Hosts (`#hosts-table`), IP Addresses (`#ips-table`),
 *      HTTP Methods (`#methods-table`), HTTP Response Codes (`#status-table`), and Content
 *      Types (`#content-types-table`), each emitting numeric `data-val` attributes for sorting.
 *
 * Consumes:
 * - `window.XLT_NETWORK` (`bytesSent`, `bytesReceived`, `hits`, `durationSec`, `sentPerSec`,
 *   `receivedPerSec`, `reqPerSec`, `hosts`, `ips`, `methods`, `responseCodes`, `contentTypes`)
 * - `window.XLT_META` (`durationMs`, `chartsGenerated`)
 * - `window.XLT_UI` (`esc`, `fmtInt`, `fmtDec`, `fmtBytesParts`, `fmtBytes`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var esc = UI.esc;
  var fmtInt = UI.fmtInt;
  var fmtDec = UI.fmtDec;
  var fmtBytesParts = UI.fmtBytesParts;
  var fmtBytes = UI.fmtBytes;

  /**
   * Formats a row's percentage share relative to `total`, displaying `"<0.01%"` for tiny
   * non-zero slices that would otherwise round down to `"0.00%"`.
   *
   * @param {number} count Category count.
   * @param {number} total Aggregate total across all rows.
   * @returns {string} Formatted percentage string.
   */
  function fmtPct(count, total) {
    if (!total) return "0.00%";
    var pct = (Number(count) * 100) / Number(total);
    if (pct > 0 && pct < 0.01) return "<0.01%";
    return fmtDec(pct, 2) + "%";
  }

  /**
   * Maps HTTP status codes to semantic pill severity classes (`2xx/3xx` -> pass green,
   * `4xx` -> warn orange, `5xx` or `0` network failure -> fail red).
   *
   * @param {number|string} code HTTP status code (`0` indicates no response / connection failure).
   * @returns {"pass"|"warn"|"fail"} Status pill class suffix.
   */
  function statusPillClass(code) {
    var c = Number(code);
    if (c >= 200 && c < 400) return "pass";
    if (c >= 400 && c < 500) return "warn";
    return "fail";
  }

  document.addEventListener("DOMContentLoaded", function () {
    var net = window.XLT_NETWORK || {};
    var meta = window.XLT_META || {};

    var sent = Number(net.bytesSent) || 0;
    var recv = Number(net.bytesReceived) || 0;
    var totalBytes = sent + recv;
    var hits = Number(net.hits) || 0;
    var dur = Math.max(1, Number(net.durationSec) || ((meta.durationMs || 1000) / 1000));
    var sentRate = net.sentPerSec !== undefined ? Number(net.sentPerSec) : sent / dur;
    var recvRate = net.receivedPerSec !== undefined ? Number(net.receivedPerSec) : recv / dur;
    var totalRate = sentRate + recvRate;
    var reqRate = net.reqPerSec !== undefined ? Number(net.reqPerSec) : hits / dur;

    var hosts = Array.isArray(net.hosts) ? net.hosts : [];
    var ips = Array.isArray(net.ips) ? net.ips : [];
    var methods = Array.isArray(net.methods) ? net.methods : [];
    var codes = Array.isArray(net.responseCodes) ? net.responseCodes : [];
    var types = Array.isArray(net.contentTypes) ? net.contentTypes : [];

    // 1. KPI Grid (Bandwidth & Hit Rate Overview)
    var kpiGrid = document.getElementById("sec-network");
    if (kpiGrid) {
      var totP = fmtBytesParts(totalBytes);
      var recvP = fmtBytesParts(recv);
      var sentP = fmtBytesParts(sent);
      var recvShare = totalBytes > 0 ? fmtDec((recv * 100) / totalBytes, 1) : "0.0";
      var sentShare = totalBytes > 0 ? fmtDec((sent * 100) / totalBytes, 1) : "0.0";

      kpiGrid.innerHTML =
        '<article class="kpi-card">' +
          '<span class="kpi-label">Total Bandwidth Transferred</span>' +
          '<div class="kpi-val">' + totP.val + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">' + totP.unit + "</span></div>" +
          '<div class="kpi-sub">Mean: <strong>' + fmtBytes(totalRate) + "/s</strong> combined network load</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Bytes Received (Download)</span>' +
          '<div class="kpi-val">' + recvP.val + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">' + recvP.unit + "</span></div>" +
          '<div class="kpi-sub">Mean: <strong>' + fmtBytes(recvRate) + "/s</strong> (" + recvShare + "% of traffic)</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Bytes Sent (Upload)</span>' +
          '<div class="kpi-val">' + sentP.val + ' <span style="font-size:14px;font-weight:600;color:var(--text-muted);">' + sentP.unit + "</span></div>" +
          '<div class="kpi-sub">Mean: <strong>' + fmtBytes(sentRate) + "/s</strong> (" + sentShare + "% of traffic)</div>" +
        "</article>" +
        '<article class="kpi-card">' +
          '<span class="kpi-label">Total HTTP Requests</span>' +
          '<div class="kpi-val">' + fmtInt(hits) + "</div>" +
          '<div class="kpi-sub"><strong>' + fmtDec(reqRate, 2) + " req/s</strong> across " + fmtInt(hosts.length) + " target host(s)</div>" +
        "</article>";
    }

    var chartsSec = document.getElementById("net-charts-section");
    if (chartsSec && meta.chartsGenerated === false) {
      chartsSec.hidden = true;
    }

    // 2. Hosts Table
    var hostsBadge = document.getElementById("net-hosts-badge");
    if (hostsBadge) hostsBadge.textContent = fmtInt(hosts.length) + " Target Domain(s)";
    var hostsTbody = document.querySelector("#hosts-table tbody");
    var hostsTfoot = document.querySelector("#hosts-table tfoot");
    if (hostsTbody) {
      var hostsTotal = hosts.reduce(function (s, h) { return s + (Number(h.count) || 0); }, 0);
      if (!hosts.length) {
        hostsTbody.innerHTML = '<tr><td colspan="4" class="col-text" style="text-align:center;padding:24px;color:var(--text-muted);">No network hosts recorded for this test run.</td></tr>';
        if (hostsTfoot) hostsTfoot.innerHTML = "";
      } else {
        hostsTbody.innerHTML = hosts.map(function (h, i) {
          var cnt = Number(h.count) || 0;
          var pct = hostsTotal > 0 ? (cnt * 100) / hostsTotal : 0;
          return (
            "<tr>" +
              '<td class="col-idx">' + (i + 1) + "</td>" +
              '<td class="col-text">' + esc(h.name || "(unknown)") + "</td>" +
              '<td class="col-num" data-val="' + cnt + '">' + fmtInt(cnt) + "</td>" +
              '<td class="col-num" data-val="' + pct.toFixed(4) + '">' + fmtPct(cnt, hostsTotal) + "</td>" +
            "</tr>"
          );
        }).join("");
        if (hostsTfoot) {
          hostsTfoot.innerHTML =
            '<tr><td class="col-idx">Σ</td><td class="col-text">Totals (' + fmtInt(hosts.length) + " hosts)</td>" +
            '<td class="col-num">' + fmtInt(hostsTotal) + "</td>" +
            '<td class="col-num">100.00%</td></tr>';
        }
      }
    }

    // 3. IP Addresses Table
    var ipsBadge = document.getElementById("net-ips-badge");
    if (ipsBadge) ipsBadge.textContent = fmtInt(ips.length) + " Entries";
    var ipsTbody = document.querySelector("#ips-table tbody");
    var ipsTfoot = document.querySelector("#ips-table tfoot");
    if (ipsTbody) {
      var ipsTotal = ips.reduce(function (s, ip) { return s + (Number(ip.count) || 0); }, 0);
      if (!ips.length) {
        ipsTbody.innerHTML = '<tr><td colspan="5" class="col-text" style="text-align:center;padding:24px;color:var(--text-muted);">No IP address statistics recorded for this test run.</td></tr>';
        if (ipsTfoot) ipsTfoot.innerHTML = "";
      } else {
        ipsTbody.innerHTML = ips.map(function (ip, i) {
          var cnt = Number(ip.count) || 0;
          var pct = ipsTotal > 0 ? (cnt * 100) / ipsTotal : 0;
          return (
            "<tr>" +
              '<td class="col-idx">' + (i + 1) + "</td>" +
              '<td class="col-text">' + esc(ip.ip || "(unknown)") + "</td>" +
              '<td class="col-text">' + esc(ip.host || "—") + "</td>" +
              '<td class="col-num" data-val="' + cnt + '">' + fmtInt(cnt) + "</td>" +
              '<td class="col-num" data-val="' + pct.toFixed(4) + '">' + fmtPct(cnt, ipsTotal) + "</td>" +
            "</tr>"
          );
        }).join("");
        if (ipsTfoot) {
          ipsTfoot.innerHTML =
            '<tr><td class="col-idx">Σ</td><td class="col-text">Totals (' + fmtInt(ips.length) + " entries)</td>" +
            '<td class="col-text">—</td>' +
            '<td class="col-num">' + fmtInt(ipsTotal) + "</td>" +
            '<td class="col-num">100.00%</td></tr>';
        }
      }
    }

    // 4. HTTP Methods Table
    var methodsTbody = document.querySelector("#methods-table tbody");
    var methodsTfoot = document.querySelector("#methods-table tfoot");
    if (methodsTbody) {
      var mTotal = methods.reduce(function (s, m) { return s + (Number(m.count) || 0); }, 0);
      if (!methods.length) {
        methodsTbody.innerHTML = '<tr><td colspan="4" class="col-text" style="text-align:center;padding:24px;color:var(--text-muted);">No HTTP methods recorded.</td></tr>';
        if (methodsTfoot) methodsTfoot.innerHTML = "";
      } else {
        methodsTbody.innerHTML = methods.map(function (m, i) {
          var cnt = Number(m.count) || 0;
          var pct = mTotal > 0 ? (cnt * 100) / mTotal : 0;
          return (
            "<tr>" +
              '<td class="col-idx">' + (i + 1) + "</td>" +
              '<td class="col-text">' + esc(m.method || "UNKNOWN") + "</td>" +
              '<td class="col-num" data-val="' + cnt + '">' + fmtInt(cnt) + "</td>" +
              '<td class="col-num" data-val="' + pct.toFixed(4) + '">' + fmtPct(cnt, mTotal) + "</td>" +
            "</tr>"
          );
        }).join("");
        if (methodsTfoot) {
          methodsTfoot.innerHTML =
            '<tr><td class="col-idx">Σ</td><td class="col-text">Totals (' + fmtInt(methods.length) + " methods)</td>" +
            '<td class="col-num">' + fmtInt(mTotal) + "</td>" +
            '<td class="col-num">100.00%</td></tr>';
        }
      }
    }

    // 5. Response Codes Table
    var codesTbody = document.querySelector("#status-table tbody");
    var codesTfoot = document.querySelector("#status-table tfoot");
    if (codesTbody) {
      var cTotal = codes.reduce(function (s, c) { return s + (Number(c.count) || 0); }, 0);
      if (!codes.length) {
        codesTbody.innerHTML = '<tr><td colspan="4" class="col-text" style="text-align:center;padding:24px;color:var(--text-muted);">No HTTP response codes recorded.</td></tr>';
        if (codesTfoot) codesTfoot.innerHTML = "";
      } else {
        codesTbody.innerHTML = codes.map(function (c, i) {
          var cnt = Number(c.count) || 0;
          var pct = cTotal > 0 ? (cnt * 100) / cTotal : 0;
          var codeNum = Number(c.code) || 0;
          var pillCls = statusPillClass(codeNum);
          var label = codeNum + (c.statusText ? " – " + c.statusText : (codeNum === 0 ? " – No Response Available" : ""));
          var errCls = pillCls === "fail" ? " cell-err" : "";
          return (
            "<tr>" +
              '<td class="col-idx">' + (i + 1) + "</td>" +
              '<td class="col-text" data-val="' + codeNum + '"><span class="status-pill ' + pillCls + '">' + esc(label) + "</span></td>" +
              '<td class="col-num' + errCls + '" data-val="' + cnt + '">' + fmtInt(cnt) + "</td>" +
              '<td class="col-num' + errCls + '" data-val="' + pct.toFixed(4) + '">' + fmtPct(cnt, cTotal) + "</td>" +
            "</tr>"
          );
        }).join("");
        if (codesTfoot) {
          codesTfoot.innerHTML =
            '<tr><td class="col-idx">Σ</td><td class="col-text">Totals (' + fmtInt(codes.length) + " response codes)</td>" +
            '<td class="col-num">' + fmtInt(cTotal) + "</td>" +
            '<td class="col-num">100.00%</td></tr>';
        }
      }
    }

    // 6. Content Types Table
    var typesTbody = document.querySelector("#content-types-table tbody");
    var typesTfoot = document.querySelector("#content-types-table tfoot");
    if (typesTbody) {
      var tTotal = types.reduce(function (s, t) { return s + (Number(t.count) || 0); }, 0);
      if (!types.length) {
        typesTbody.innerHTML = '<tr><td colspan="4" class="col-text" style="text-align:center;padding:24px;color:var(--text-muted);">No content types recorded.</td></tr>';
        if (typesTfoot) typesTfoot.innerHTML = "";
      } else {
        typesTbody.innerHTML = types.map(function (t, i) {
          var cnt = Number(t.count) || 0;
          var pct = tTotal > 0 ? (cnt * 100) / tTotal : 0;
          var ct = t.contentType ? t.contentType : "(empty / not specified)";
          return (
            "<tr>" +
              '<td class="col-idx">' + (i + 1) + "</td>" +
              '<td class="col-text">' + esc(ct) + "</td>" +
              '<td class="col-num" data-val="' + cnt + '">' + fmtInt(cnt) + "</td>" +
              '<td class="col-num" data-val="' + pct.toFixed(4) + '">' + fmtPct(cnt, tTotal) + "</td>" +
            "</tr>"
          );
        }).join("");
        if (typesTfoot) {
          typesTfoot.innerHTML =
            '<tr><td class="col-idx">Σ</td><td class="col-text">Totals (' + fmtInt(types.length) + " content types)</td>" +
            '<td class="col-num">' + fmtInt(tTotal) + "</td>" +
            '<td class="col-num">100.00%</td></tr>';
        }
      }
    }
  });
})();
