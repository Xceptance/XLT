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
 * XLT Modern Load Report — Modal Chart Lightbox (`<dialog closedby="any">`)
 *
 * @fileoverview
 * Delegated click handler that opens any `img.zoomable-chart` inside a shared fullscreen-capable
 * native `<dialog>` overlay (`#xlt-chart-lightbox`).
 *
 * Key Behaviors:
 * - Lazy singleton DOM creation on first chart click.
 * - Dynamic visibility filtering (`offsetParent !== null`) so Left/Right gallery navigation cycles
 *   only through charts currently visible under active tab/search filters.
 * - Captures `ArrowLeft`, `ArrowRight`, and `Escape` with `stopPropagation()` so workbench arrow
 *   shortcuts in `workbench.js` do not also step rows while the modal is open.
 * - Uses native `<dialog closedby="any">` light-dismiss with a bounding-box backdrop click
 *   polyfill for browsers lacking `HTMLDialogElement.prototype.closedBy`.
 */
(function () {
  "use strict";

  var dialog = null;
  var imgEl = null;
  var titleEl = null;
  var counterEl = null;
  var prevBtn = null;
  var nextBtn = null;
  var activeList = [];
  var activeIdx = 0;

  var crosshairOverlay = null;
  var crosshairVert = null;
  var crosshairHoriz = null;

  /**
   * Lazily constructs the shared `.chart-crosshair-overlay` element with vertical and horizontal
   * hairlines matching the classic XLT crosshair aid.
   * @returns {HTMLDivElement} The crosshair overlay element.
   */
  function ensureCrosshair() {
    if (crosshairOverlay) return crosshairOverlay;
    crosshairOverlay = document.createElement("div");
    crosshairOverlay.className = "chart-crosshair-overlay";
    crosshairOverlay.setAttribute("aria-hidden", "true");

    crosshairVert = document.createElement("div");
    crosshairVert.className = "hair hair-vertical";

    crosshairHoriz = document.createElement("div");
    crosshairHoriz.className = "hair hair-horizontal";

    crosshairOverlay.appendChild(crosshairVert);
    crosshairOverlay.appendChild(crosshairHoriz);
    return crosshairOverlay;
  }

  /** Hides the hover crosshair overlay if currently visible. */
  function hideCrosshair() {
    if (crosshairOverlay) {
      crosshairOverlay.style.display = "none";
    }
  }

  /**
   * Positions the hover crosshair over any hovered static report chart (`img.zoomable-chart`) or
   * the enlarged lightbox image (`#xlt-lightbox-img`).
   * @param {PointerEvent} e Pointer move event.
   */
  function updateCrosshair(e) {
    if (e.pointerType === "touch") return;
    var img = e.target && e.target.closest ? e.target.closest("img.zoomable-chart, #xlt-lightbox-img") : null;
    if (!img || !img.complete || img.naturalWidth === 0) {
      hideCrosshair();
      return;
    }

    var rect = img.getBoundingClientRect();
    if (rect.width <= 0 || rect.height <= 0) {
      hideCrosshair();
      return;
    }

    var overlay = ensureCrosshair();
    var host = img.closest("dialog") || document.body;
    if (overlay.parentNode !== host) {
      host.appendChild(overlay);
    }

    overlay.style.left = rect.left + "px";
    overlay.style.top = rect.top + "px";
    overlay.style.width = rect.width + "px";
    overlay.style.height = rect.height + "px";
    overlay.style.display = "block";

    crosshairVert.style.left = (e.clientX - rect.left) + "px";
    crosshairHoriz.style.top = (e.clientY - rect.top) + "px";
  }

  /**
   * Lazily constructs `#xlt-chart-lightbox` once and wires button/keyboard/backdrop listeners.
   * @returns {HTMLDialogElement} The shared lightbox dialog element.
   */
  function ensureDialog() {
    if (dialog) return dialog;
    dialog = document.createElement("dialog");
    dialog.id = "xlt-chart-lightbox";
    dialog.className = "chart-lightbox-dialog";
    dialog.setAttribute("closedby", "any");
    dialog.setAttribute("aria-labelledby", "xlt-lightbox-title");

    dialog.innerHTML =
      '<div class="lightbox-head">' +
        '<div style="display:flex;align-items:center;gap:10px;min-width:0;">' +
          '<h2 id="xlt-lightbox-title" style="font-size:13px;font-weight:800;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">Chart Preview</h2>' +
          '<span id="xlt-lightbox-counter" class="badge-tag"></span>' +
        '</div>' +
        '<div style="display:flex;align-items:center;gap:6px;">' +
          '<button type="button" class="btn-chip btn-icon" id="xlt-lb-prev" aria-label="Previous chart" title="Previous chart (←)"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><polyline points="15 18 9 12 15 6"/></svg></button>' +
          '<button type="button" class="btn-chip btn-icon" id="xlt-lb-next" aria-label="Next chart" title="Next chart (→)"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false"><polyline points="9 18 15 12 9 6"/></svg></button>' +
          '<form method="dialog" style="margin:0;">' +
            '<button type="submit" class="btn-chip" autofocus aria-label="Close chart preview">✕ Close</button>' +
          '</form>' +
        '</div>' +
      '</div>' +
      '<div class="lightbox-stage chart-canvas-light">' +
        '<img id="xlt-lightbox-img" alt="Enlarged chart" />' +
      '</div>';

    document.body.appendChild(dialog);
    imgEl = dialog.querySelector("#xlt-lightbox-img");
    titleEl = dialog.querySelector("#xlt-lightbox-title");
    counterEl = dialog.querySelector("#xlt-lightbox-counter");
    prevBtn = dialog.querySelector("#xlt-lb-prev");
    nextBtn = dialog.querySelector("#xlt-lb-next");

    prevBtn.addEventListener("click", function () { step(-1); });
    nextBtn.addEventListener("click", function () { step(1); });

    dialog.addEventListener("close", function () {
      dialog.returnValue = "";
      hideCrosshair();
    });

    dialog.addEventListener("keydown", function (e) {
      if (e.key === "ArrowLeft") {
        e.preventDefault();
        e.stopPropagation();
        step(-1);
      } else if (e.key === "ArrowRight") {
        e.preventDefault();
        e.stopPropagation();
        step(1);
      } else if (e.key === "Escape") {
        e.stopPropagation();
      }
    });

    // Legacy backdrop-click close for browsers without HTMLDialogElement.closedBy
    if (!("closedBy" in HTMLDialogElement.prototype)) {
      dialog.addEventListener("click", function (ev) {
        if (ev.target !== dialog) return;
        var r = dialog.getBoundingClientRect();
        var isDialogContent =
          r.top <= ev.clientY &&
          ev.clientY <= r.top + r.height &&
          r.left <= ev.clientX &&
          ev.clientX <= r.left + r.width;
        if (isDialogContent) return;
        dialog.close();
      });
    }

    return dialog;
  }

  /** Renders the currently selected chart entry inside the open lightbox. */
  function renderCurrent() {
    hideCrosshair();
    var item = activeList[activeIdx];
    if (!item) return;
    imgEl.src = item.src;
    imgEl.alt = item.title;
    titleEl.textContent = item.title;
    counterEl.textContent = (activeIdx + 1) + " / " + activeList.length;
    prevBtn.disabled = activeList.length <= 1;
    nextBtn.disabled = activeList.length <= 1;
  }

  /**
   * Steps circularly through `activeList` by `delta` (`+1` or `-1`).
   * @param {number} delta Index offset.
   */
  function step(delta) {
    if (activeList.length <= 1) return;
    activeIdx = (activeIdx + delta + activeList.length) % activeList.length;
    renderCurrent();
  }

  /**
   * Collects all currently visible `img.zoomable-chart` elements in document order and
   * locates the index corresponding to `clickedImg`.
   *
   * @param {HTMLImageElement} clickedImg Chart image clicked by the user.
   * @returns {{ list: Array<{ src: string, title: string }>, idx: number }} Gallery state.
   */
  function collectVisibleCharts(clickedImg) {
    var imgs = Array.prototype.slice.call(document.querySelectorAll("img.zoomable-chart"));
    var list = [];
    var idx = 0;
    imgs.forEach(function (el) {
      if (!el.src || el.offsetParent === null) return;
      var label = el.getAttribute("data-chart-title") || el.alt || "Chart";
      if (el === clickedImg) idx = list.length;
      list.push({ src: el.src, title: label });
    });
    if (!list.length && clickedImg && clickedImg.src) {
      list.push({ src: clickedImg.src, title: clickedImg.getAttribute("data-chart-title") || clickedImg.alt || "Chart" });
    }
    return { list: list, idx: idx };
  }

  document.addEventListener("pointermove", updateCrosshair, { passive: true });
  document.addEventListener("mouseleave", hideCrosshair);
  document.addEventListener("scroll", hideCrosshair, { capture: true, passive: true });
  window.addEventListener("resize", hideCrosshair, { passive: true });
  window.addEventListener("blur", hideCrosshair);

  document.addEventListener("click", function (e) {
    var img = e.target.closest("img.zoomable-chart");
    if (!img) return;
    hideCrosshair();
    var dlg = ensureDialog();
    var res = collectVisibleCharts(img);
    activeList = res.list;
    activeIdx = res.idx;
    renderCurrent();
    if (!dlg.open) dlg.showModal();
  });
})();
