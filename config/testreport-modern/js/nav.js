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
 * XLT Modern Load Report — Header/Footer Metadata & Accessible Top Navigation
 *
 * @fileoverview
 * Runs on every report page after `report-meta.js` and `report-utils.js` (`window.XLT_UI`).
 * On `DOMContentLoaded`, it:
 *   1. Populates project/test badges, subtitle execution window, `<title>`, and report footer
 *      from `window.XLT_META`.
 *   2. Hydrates header navigation count badges (`#nav-errors-pill`, `#nav-agents-pill`,
 *      `#nav-scorecard-item`, `#nav-scorecard-pill`, `#nav-ai-data-item`, `[data-nav-count]`)
 *      and dims links whose metric count is zero (`is-empty`).
 *   3. Wires accessible split/dropdown navigation menus (`.nav-item[data-has-menu]`) supporting
 *      pointer hover, click-to-pin, viewport-edge clamping, outside-click dismissal, focus-out
 *      collapse, and `Escape` closing (yielding when a modal `<dialog open>` is active).
 *
 * Consumes:
 * - `window.XLT_META` (`projectName`, `loadTestName`, `productName`, `productVersion`, `productUrl`,
 *   `startTime`, `endTime`, `durationDisplay`, `counts`, `scorecard`, `aiDataGenerated`)
 * - `window.XLT_UI` (`fmtInt`, `fmtCompact`)
 */
(function () {
  "use strict";

  var UI = window.XLT_UI;
  var fmtInt = UI.fmtInt;
  var fmtCompact = UI.fmtCompact;

  /**
   * Marks the enclosing `<a>` element with `.is-empty` when its section has zero items
   * so navigation menus visually de-emphasize unpopulated sections without hiding them.
   *
   * @param {Element|null} el Badge element inside a navigation link.
   * @param {boolean} isEmpty Whether the corresponding dataset has 0 entries.
   */
  function setLinkEmptyState(el, isEmpty) {
    var link = el && el.closest ? el.closest("a") : null;
    if (!link) return;
    if (isEmpty) link.classList.add("is-empty");
    else link.classList.remove("is-empty");
  }

  /**
   * Hydrates the top header badges, browser tab title, navigation count pills,
   * scorecard status indicator, and report footer from `window.XLT_META`.
   */
  function populateMetadata() {
    var meta = window.XLT_META;
    if (!meta) return;

    // 1. Compose browser <title> matching classic format: "<ProjectName> | XLT Report - <Page>"
    var titleEl = document.querySelector("title");
    var pageTitle =
      (titleEl && titleEl.getAttribute("data-page-title")) ||
      document.title
        .replace(/^.*?\|\s*/, "")
        .replace(/^XLT Report\s*[—-]\s*/, "") ||
      "Overview";

    var proj = String(meta.projectName || "").trim().replace(/\s+/g, " ");
    if (proj.length >= 32) {
      proj = proj.substring(0, 28) + "...";
    }
    document.title = (proj ? proj + " | " : "") + "XLT Report - " + pageTitle;

    // 2. Report context strip (project, test name, execution window, duration, XLT version)
    var projectSep = document.getElementById("meta-project-sep");
    var projectBadge = document.getElementById("meta-project-badge");
    if (projectBadge) {
      if (meta.projectName) {
        projectBadge.textContent = meta.projectName;
        projectBadge.hidden = false;
        if (projectSep) projectSep.hidden = false;
      } else {
        projectBadge.textContent = "";
        projectBadge.hidden = true;
        if (projectSep) projectSep.hidden = true;
      }
    }

    var testBadge = document.getElementById("meta-test-badge");
    if (testBadge) {
      if (meta.loadTestName) {
        testBadge.textContent = meta.loadTestName;
        testBadge.hidden = false;
      } else {
        testBadge.textContent = "";
        testBadge.hidden = true;
      }
    }

    var subtitleEl = document.getElementById("meta-subtitle");
    if (subtitleEl) {
      var timeWindow = "";
      if (meta.startTime && meta.endTime) {
        timeWindow = meta.startTime + " – " + meta.endTime;
      } else if (meta.startTime) {
        timeWindow = meta.startTime;
      }
      var prodName = meta.productName || "Xceptance LoadTest";
      var prodShort = prodName === "Xceptance LoadTest" ? "XLT" : prodName;
      var prodStr = meta.productVersion ? prodShort + " " + meta.productVersion : prodShort;

      subtitleEl.textContent = "";
      var appendSep = function () {
        var sep = document.createElement("span");
        sep.className = "ctx-meta-sep";
        sep.textContent = "·";
        subtitleEl.appendChild(sep);
      };
      if (timeWindow) {
        var winEl = document.createElement("span");
        winEl.textContent = timeWindow;
        subtitleEl.appendChild(winEl);
      }
      if (meta.durationDisplay) {
        if (subtitleEl.childNodes.length) appendSep();
        var durEl = document.createElement("span");
        durEl.className = "ctx-meta-duration";
        durEl.textContent = meta.durationDisplay;
        subtitleEl.appendChild(durEl);
      }
      if (prodStr) {
        if (subtitleEl.childNodes.length) appendSep();
        var prodEl = document.createElement("span");
        prodEl.textContent = prodStr;
        subtitleEl.appendChild(prodEl);
      }
    }

    // 2b. Optional XTC run metadata line (matches classic header.xsl when com.xceptance.xtc.organization is set)
    var contextInner = document.querySelector(".report-context-inner");
    var existingXtc = document.getElementById("meta-xtc-strip");
    var xtc = meta.xtc && meta.xtc.organization ? meta.xtc : null;
    if (contextInner) {
      if (xtc) {
        var xtcStrip = existingXtc || document.createElement("div");
        xtcStrip.id = "meta-xtc-strip";
        xtcStrip.className = "report-context-xtc";
        xtcStrip.textContent = "";
        var xtcFields = [
          { key: "Organization:", val: xtc.organization },
          { key: "Project:", val: xtc.project },
          { key: "Load Test:", val: xtc.loadTestId },
          { key: "Result:", val: xtc.resultId },
          { key: "Report:", val: xtc.reportId }
        ];
        xtcFields.forEach(function (field) {
          var item = document.createElement("span");
          item.className = "ctx-xtc-item";
          var k = document.createElement("span");
          k.className = "ctx-xtc-key";
          k.textContent = field.key;
          var v = document.createElement("span");
          v.className = "ctx-xtc-val";
          v.textContent = field.val ? String(field.val) : "—";
          item.appendChild(k);
          item.appendChild(document.createTextNode(" "));
          item.appendChild(v);
          xtcStrip.appendChild(item);
        });
        if (!existingXtc) {
          contextInner.appendChild(xtcStrip);
        }
      } else if (existingXtc) {
        existingXtc.remove();
      }
    }

    // 3. Top-level nav pills (Errors, Agents, Scorecard, AI Markdown link)
    var c = meta.counts || {};

    var errPill = document.getElementById("nav-errors-pill");
    if (errPill && c.errors !== undefined && c.errors !== null) {
      var errs = Number(c.errors) || 0;
      errPill.textContent = fmtCompact(errs);
      errPill.className = errs > 0 ? "nav-pill-count fail" : "badge-tag";
      errPill.hidden = false;
    }

    var agentsPill = document.getElementById("nav-agents-pill");
    if (agentsPill && c.agents !== undefined && c.agents !== null) {
      agentsPill.textContent = fmtInt(c.agents);
      agentsPill.hidden = false;
    }

    var scItem = document.getElementById("nav-scorecard-item");
    var scPill = document.getElementById("nav-scorecard-pill");
    if (scItem) {
      if (meta.scorecard && meta.scorecard.present) {
        scItem.hidden = false;
        if (scPill) {
          var res = String(meta.scorecard.result || "EVALUATED").toUpperCase();
          scPill.textContent = res;
          scPill.className =
            "nav-pill-count " +
            (res === "PASSED"
              ? "pass"
              : res === "FAILED" || res === "ERROR"
                ? "fail"
                : "warn");
        }
      } else {
        scItem.hidden = true;
      }
    }

    var aiItem = document.getElementById("nav-ai-data-item");
    if (aiItem) {
      aiItem.hidden = meta.aiDataGenerated === false;
    }

    // 4. Dropdown sub-link badges annotated with [data-nav-count]
    var badgeNodes = Array.prototype.slice.call(document.querySelectorAll("[data-nav-count]"));
    badgeNodes.forEach(function (node) {
      var key = node.getAttribute("data-nav-count");
      var val;
      if (key === "requests") {
        val = Number(c.requests) || 0;
        node.textContent = fmtInt(val);
        node.hidden = false;
        setLinkEmptyState(node, val === 0);
      } else if (key === "slowestRequests") {
        val = Number(c.slowestRequests) || 0;
        node.textContent = val > 0 ? "Top " + fmtInt(val) : "0";
        node.className = val > 0 ? "badge-tag cell-warn" : "badge-tag";
        node.hidden = false;
        setLinkEmptyState(node, val === 0);
      } else if (key === "hosts") {
        if (c.hosts !== undefined && c.hosts !== null) {
          node.textContent = fmtInt(c.hosts);
          node.hidden = false;
        }
      } else if (key === "ips") {
        if (c.ips !== undefined && c.ips !== null) {
          node.textContent = fmtInt(c.ips);
          node.hidden = false;
        }
      } else if (key === "requestErrors") {
        if (c.requestErrors !== undefined && c.requestErrors !== null) {
          node.textContent = fmtInt(c.requestErrors);
          node.hidden = false;
        }
      } else if (key === "actionErrors") {
        if (c.actionErrors !== undefined && c.actionErrors !== null) {
          node.textContent = fmtInt(c.actionErrors);
          node.hidden = false;
        }
      } else if (key === "transactionErrors") {
        var txErr = c.transactionErrors !== undefined ? c.transactionErrors : c.errors;
        if (txErr !== undefined && txErr !== null) {
          node.textContent = fmtInt(txErr);
          node.hidden = false;
        }
      } else if (key === "events") {
        val = Number(c.events) || 0;
        node.textContent = fmtCompact(val);
        // Events use orange warning styling rather than red error styling
        node.className = val > 0 ? "nav-pill-count warn" : "badge-tag";
        node.hidden = false;
        setLinkEmptyState(node, val === 0);
      } else if (key === "properties") {
        if (c.properties !== undefined && c.properties !== null) {
          node.textContent = fmtInt(c.properties) + " props";
          node.hidden = false;
        }
      } else if (key === "customTimers") {
        val = Number(c.customTimers) || 0;
        node.textContent = fmtCompact(val);
        node.hidden = false;
        setLinkEmptyState(node, val === 0);
      } else if (key === "customValues") {
        var cv = Number(c.customValues) || 0;
        var cl = Number(c.customLogs) || 0;
        if (cv > 0 && cl > 0) {
          node.textContent = fmtInt(cv) + " + " + fmtInt(cl) + (cl === 1 ? " log" : " logs");
        } else if (cv > 0) {
          node.textContent = fmtInt(cv);
        } else if (cl > 0) {
          node.textContent = fmtInt(cl) + (cl === 1 ? " log" : " logs");
        } else {
          node.textContent = "0";
        }
        node.hidden = false;
        setLinkEmptyState(node, cv === 0 && cl === 0);
      } else if (key === "pageLoadTimings") {
        val = Number(c.pageLoadTimings) || 0;
        node.textContent = fmtInt(val);
        node.hidden = false;
        setLinkEmptyState(node, val === 0);
      } else if (key === "webVitals") {
        val = Number(c.webVitals) || 0;
        node.textContent = fmtInt(val);
        node.hidden = false;
        setLinkEmptyState(node, val === 0);
      } else if (key === "externalCharts") {
        val = Number(c.externalCharts) || 0;
        node.textContent = fmtInt(val);
        node.hidden = false;
        setLinkEmptyState(node, val === 0);
      }
    });

    // 5. Report footer attribution and test time window
    var footer = document.getElementById("report-footer") || document.querySelector(".report-footer, footer.footer");
    if (footer) {
      footer.className = "report-footer";
      footer.textContent = "";

      var line1 = document.createElement("div");
      line1.appendChild(document.createTextNode("Generated by "));
      var prodStrong = document.createElement("strong");
      prodStrong.textContent =
        (meta.productName || "Xceptance LoadTest") +
        (meta.productVersion ? " " + meta.productVersion : "");
      if (meta.productUrl) {
        var prodLink = document.createElement("a");
        prodLink.href = meta.productUrl;
        prodLink.target = "_blank";
        prodLink.rel = "noopener";
        prodLink.style.color = "inherit";
        prodLink.style.textDecoration = "none";
        prodLink.appendChild(prodStrong);
        line1.appendChild(prodLink);
      } else {
        line1.appendChild(prodStrong);
      }
      var footerContext = [meta.projectName, meta.loadTestName || meta.testName].filter(Boolean).join(" — ");
      if (footerContext) {
        line1.appendChild(document.createTextNode(" • " + footerContext));
      }
      footer.appendChild(line1);

      var line2 = document.createElement("div");
      var parts = [];
      if (meta.startTime && meta.endTime) {
        parts.push("Test Window: " + meta.startTime + " – " + meta.endTime);
      } else if (meta.startTime) {
        parts.push("Test Start: " + meta.startTime);
      }
      if (meta.durationDisplay) {
        parts.push("Duration: " + meta.durationDisplay);
      }
      line2.textContent = parts.join(" • ");
      footer.appendChild(line2);
    }
  }

  var hashTargetTimer = null;

  /**
   * Smoothly scrolls a URL hash target into view (respecting sticky-header scroll-margin-top)
   * and applies a temporary `.is-hash-target` visual highlight class.
   */
  function scrollToHashTarget(hashStr) {
    var raw = typeof hashStr === "string" && hashStr ? hashStr : window.location.hash;
    if (!raw) return false;
    var clean = raw.charAt(0) === "#" ? raw.slice(1) : raw;
    if (!clean) return false;
    var id = clean;
    try {
      id = decodeURIComponent(clean);
    } catch (err) {
      // Keep raw fragment if URI decoding fails
    }
    var target = document.getElementById(id);
    if (!target) return false;

    if (hashTargetTimer) {
      clearTimeout(hashTargetTimer);
      hashTargetTimer = null;
    }
    Array.prototype.forEach.call(document.querySelectorAll(".is-hash-target"), function (el) {
      el.classList.remove("is-hash-target");
    });

    var reduceMotion = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    target.scrollIntoView({ behavior: reduceMotion ? "auto" : "smooth", block: "start" });
    target.classList.add("is-hash-target");
    hashTargetTimer = setTimeout(function () {
      target.classList.remove("is-hash-target");
      hashTargetTimer = null;
    }, 1800);
    return true;
  }

  document.addEventListener("DOMContentLoaded", function () {
    try {
      populateMetadata();
    } catch (err) {
      console.error("XLT nav metadata init error:", err);
    }

    // Wire split-button and dropdown navigation menus with hover + click-pin semantics
    var navItems = Array.prototype.slice.call(document.querySelectorAll(".nav-item[data-has-menu]"));
    var canHover = function () {
      return window.matchMedia && window.matchMedia("(hover: hover)").matches;
    };

    function closeAll(exceptItem) {
      navItems.forEach(function (item) {
        if (item === exceptItem) return;
        var trigger = item.querySelector("[aria-controls]");
        var menu = item.querySelector(".nav-submenu");
        if (menu) menu.hidden = true;
        if (trigger) trigger.setAttribute("aria-expanded", "false");
        item.dataset.pinned = "";
      });
    }

    /**
     * Nudges an opened submenu horizontally so it stays within 8px of either viewport edge
     * (important for narrow viewports or right-aligned menus like `More`).
     */
    function clampMenuToViewport(menu) {
      menu.style.transform = "";
      var r = menu.getBoundingClientRect();
      if (r.right > window.innerWidth - 8) {
        menu.style.transform = "translateX(" + Math.round(window.innerWidth - 8 - r.right) + "px)";
      } else if (r.left < 8) {
        menu.style.transform = "translateX(" + Math.round(8 - r.left) + "px)";
      }
    }

    function openItem(item, pinned) {
      closeAll(item);
      var trigger = item.querySelector("[aria-controls]");
      var menu = item.querySelector(".nav-submenu");
      if (!menu || !trigger) return;
      menu.hidden = false;
      trigger.setAttribute("aria-expanded", "true");
      item.dataset.pinned = pinned ? "1" : "";
      clampMenuToViewport(menu);
    }

    function closeItem(item) {
      var trigger = item.querySelector("[aria-controls]");
      var menu = item.querySelector(".nav-submenu");
      if (menu) menu.hidden = true;
      if (trigger) trigger.setAttribute("aria-expanded", "false");
      item.dataset.pinned = "";
    }

    navItems.forEach(function (item) {
      var trigger = item.querySelector("[aria-controls]");
      var menu = item.querySelector(".nav-submenu");
      if (!trigger || !menu) return;

      trigger.addEventListener("click", function (e) {
        e.preventDefault();
        var isOpen = !menu.hidden;
        // Clicking a hover-opened menu promotes it to pinned rather than closing immediately
        if (isOpen && !item.dataset.pinned) {
          item.dataset.pinned = "1";
          return;
        }
        if (isOpen) {
          closeItem(item);
        } else {
          openItem(item, true);
        }
      });

      item.addEventListener("pointerenter", function (e) {
        if (!canHover() || e.pointerType === "touch") return;
        if (menu.hidden) openItem(item, false);
      });

      item.addEventListener("pointerleave", function (e) {
        if (!canHover() || e.pointerType === "touch") return;
        if (!item.dataset.pinned) closeItem(item);
      });

      item.addEventListener("focusout", function (e) {
        if (e.relatedTarget && !item.contains(e.relatedTarget)) {
          closeItem(item);
        }
      });

      menu.addEventListener("click", function (e) {
        var link = e.target.closest("a.nav-sublink, a");
        if (!link) return;
        closeItem(item);
        if (link.hash) {
          var currentFile = (window.location.pathname || "").split("/").pop() || "index.html";
          var targetFile = (link.pathname || "").split("/").pop() || "index.html";
          if (currentFile === targetFile && scrollToHashTarget(link.hash)) {
            e.preventDefault();
            if (window.location.hash !== link.hash) {
              if (window.history && window.history.pushState) {
                try {
                  window.history.pushState(null, "", link.hash);
                } catch (err) {
                  window.location.hash = link.hash;
                }
              } else {
                window.location.hash = link.hash;
              }
            }
          }
        }
      });
    });

    document.addEventListener("pointerdown", function (e) {
      if (!e.target.closest(".nav-item[data-has-menu]")) {
        closeAll(null);
      }
    });

    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape") {
        // Yield Escape to open modals (e.g., chart lightbox) before closing menus
        var openDialog = document.querySelector("dialog[open]");
        if (openDialog) return;
        var anyOpen = navItems.some(function (it) {
          var m = it.querySelector(".nav-submenu");
          return m && !m.hidden;
        });
        if (anyOpen) {
          e.stopPropagation();
          closeAll(null);
        }
      }
    });

    if (window.location.hash) {
      setTimeout(function () {
        scrollToHashTarget(window.location.hash);
      }, 60);
    }
    window.addEventListener("hashchange", function () {
      scrollToHashTarget(window.location.hash);
    });
  });
})();
