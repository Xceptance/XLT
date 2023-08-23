(function () {
    'use strict';

    /** 
     * The sub elements of the JsonViewer as a template. 
     */
    const template = document.createElement('template');

    template.innerHTML = `
        <json-tree class="tree"></json-tree>
        <div class="actions">
            <button class="expandAll">Expand All</button>
            <button class="collapseAll">Collapse All</button>
            <span class="separator"></span>
            <label>Search: <input type="text" class="search"></label><button class="previous">&bigtriangleup;</button><button class="next">&bigtriangledown;</button>
            <label><input type="checkbox" checked="checked" class="ignoreCase"> Ignore Case</label>
            <label><input type="checkbox" checked="checked" class="filter"> Filter</label>
            <span class="separator"></span>
            <span class="matches">0</span> Match(es) 
            <span class="separator"></span>
            JSON Path: <span class="jsonPath"></span>
        </div>`;

    /**
     * A viewer for JSON data that renders the JSON as a tree and offers ways to search and filter the JSON data.
     */
    class JsonViewer extends HTMLElement {

        /** The JsonTree element. */
        jsonTree = null;

        constructor() {
            super();

            // create the sub elements from the template
            this.appendChild(template.content.cloneNode(true));

            // remember the json-tree element
            this.jsonTree = this.querySelector("json-tree");

            // get some UI elements
            const searchInput = this.querySelector(".search");
            const ignoreCaseCheckBox = this.querySelector(".ignoreCase");
            const filterCheckBox = this.querySelector(".filter");
            const matchesSpan = this.querySelector(".matches");

            // the search event handler
            const search = () => {
                const searchPhrase = searchInput.value;
                const ignoreCase = !!ignoreCaseCheckBox.checked;
                const filter = !!filterCheckBox.checked;

                const matches = this.jsonTree.search(searchPhrase, ignoreCase, filter);

                matchesSpan.textContent = matches;                  
            }

            // add event handlers
            this.querySelector(".expandAll").addEventListener('click', () => this.jsonTree.expandAll());
            this.querySelector(".collapseAll").addEventListener('click', () => this.jsonTree.collapseAll());
            this.querySelector(".search").addEventListener('keyup', search);
            this.querySelector(".ignoreCase").addEventListener('click', search);
            this.querySelector(".filter").addEventListener('click', search);
            this.querySelector(".previous").addEventListener('click', () => this.jsonTree.highlightNextMatch(false));
            this.querySelector(".next").addEventListener('click', () => this.jsonTree.highlightNextMatch(true));
        }

        connectedCallback() {
            // nothing to do here for now
        }

        /**
         * Populates the viewer with the JSON data.
         *
         * @param {string} jsonData - the JSON data in string form
         */
        load(json) {
            // pass data on to the JSON tree
            this.jsonTree.load(json);

            // add a handler to the JSON tree that is called whenever the currently selected node changes
            const jsonPath = this.querySelector(".jsonPath");
            this.jsonTree.onJsonNodeSelected((s) => jsonPath.textContent = s);
        }

        /**
         * Clears any rendered JSON and resets the UI state.
         */
        clear() {
            this.jsonTree.clear();
            this.querySelector(".search").value = "";
            this.querySelector(".matches").textContent = "0";
            this.querySelector(".jsonPath").textContent = "";
        }
    }

    // register the JsonViewer web component
    window.customElements.define('json-viewer', JsonViewer);

})()

