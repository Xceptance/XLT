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
 * XLT Modern Load Report — Theme Manager (Light Default + Dark Override)
 *
 * @fileoverview
 * Loaded synchronously as the first script inside `<head>` on every report page so that
 * `html.dark-theme` is applied to `document.documentElement` before the browser parses or
 * paints `<body>` (preventing a light-to-dark flash / FOUC on page navigation).
 *
 * Persistence:
 * - Reads and writes `localStorage["xlt-report-theme"]` (plus legacy `"xlt-theme"` fallback).
 * - All `localStorage` calls are wrapped in `try/catch` because hardened browsers and some
 *   `file://` security policies throw `SecurityError` when storage is disabled.
 *
 * Exposes `window.toggleTheme()` and binds `#theme-toggle` / `.btn-theme-toggle` on `DOMContentLoaded`.
 */
(function () {
  "use strict";

  var STORAGE_KEY = "xlt-report-theme";

  /**
   * Resolves the stored user theme preference, defaulting to `"light"`.
   * @returns {"light"|"dark"} Preferred initial theme mode.
   */
  function getInitialTheme() {
    try {
      var saved = localStorage.getItem(STORAGE_KEY) || localStorage.getItem("xlt-theme");
      if (saved === "dark" || saved === "light") return saved;
    } catch (_) {}
    return "light";
  }

  /**
   * Locates all theme toggle buttons present in the current document.
   * @returns {NodeListOf<Element>} Matching toggle button elements.
   */
  function findToggleButtons() {
    return document.querySelectorAll("#theme-toggle, #theme-toggle-btn, #btn-theme-toggle, .btn-theme-toggle");
  }

  /**
   * Toggles the `dark-theme` class on `<html>` and synchronizes toggle button labels and `aria-pressed`.
   * @param {"light"|"dark"} theme Target theme mode.
   */
  function applyTheme(theme) {
    var root = document.documentElement;
    var isDark = theme === "dark";
    if (isDark) {
      root.classList.add("dark-theme");
    } else {
      root.classList.remove("dark-theme");
    }
    var btns = findToggleButtons();
    Array.prototype.forEach.call(btns, function (btn) {
      btn.setAttribute("aria-pressed", String(isDark));
      btn.innerHTML = isDark
        ? '<span class="theme-icon">☀️</span> Light'
        : '<span class="theme-icon">🌙</span> Dark';
      btn.title = isDark ? "Switch to Light Mode" : "Switch to Dark Mode";
    });
  }

  /**
   * Flips between light and dark themes, persists the choice, and updates the UI.
   * Exposed globally for inline or programmatic callers.
   */
  window.toggleTheme = function () {
    var next = document.documentElement.classList.contains("dark-theme") ? "light" : "dark";
    try {
      localStorage.setItem(STORAGE_KEY, next);
      localStorage.setItem("xlt-theme", next);
    } catch (_) {}
    applyTheme(next);
  };

  // Apply before first paint while <head> is still being parsed
  applyTheme(getInitialTheme());

  document.addEventListener("DOMContentLoaded", function () {
    // Re-sync button text/state once header DOM nodes exist
    applyTheme(document.documentElement.classList.contains("dark-theme") ? "dark" : "light");

    var btns = findToggleButtons();
    Array.prototype.forEach.call(btns, function (btn) {
      if (btn.hasAttribute("onclick")) return;
      btn.addEventListener("click", function () {
        window.toggleTheme();
      });
    });
  });
})();
