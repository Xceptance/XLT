/**
 * Inspired by the json-view library (https://github.com/pgrabovets/json-view)
 * created by Pavel Grabovets and published under ISC license.
 *
 * ISC License
 *
 * Copyright (c) 2018, Pavel Grabovets
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
(function () {
    'use strict';

    /**
     * The TreeNode constructor.
     */
    function TreeNode() {
        this.parent = null;
        this.key = null;
        this.value = null;
        this.type = null;
        this.expanded = false;
        this.children = null;
        this.elem = null;
        this.depth = 0;

        this.isLeaf = function () {
            return this.value !== null;
        };

        this.hasChildren = function () {
            return this.children !== null && this.children.length > 0;
        };

        this.forEachChildNode = function (callback) {
            if (this.hasChildren()) {
                this.children.forEach((child) => callback(child));
            }
        };

        this.hide = function () {
            this.expanded && this.forEachChildNode((child) => child.hide());
            this.setVisible(false);
        };

        this.show = function () {
            this.setVisible(true);
            this.expanded && this.forEachChildNode((child) => child.show());
        };

        this.setVisible = function (visible) {
            visible ? this.elem.classList.remove('json-hide') : this.elem.classList.add('json-hide');
        };

        this.setExpanded = function (expanded) {
            if (this.hasChildren()) {
                this.expanded = expanded;

                const icon = this.elem.querySelector('.json-caret');
                expanded ? icon.classList.add('json-caret-expanded') : icon.classList.remove('json-caret-expanded');
            }
        };

        this.toggle = function () {
            this.expanded ? this.collapse() : this.expand();
        };

        this.expand = function () {
            if (this.hasChildren()) {
                this.setExpanded(true);
                this.forEachChildNode((child) => child.show());
            }
        };

        this.expandAll = function () {
            this.expand();
            this.forEachChildNode((child) => child.expandAll());
        };

        this.collapse = function () {
            if (this.hasChildren()) {
                this.setExpanded(false);
                this.forEachChildNode((child) => child.hide());
            }
        };

        this.collapseAll = function () {
            this.forEachChildNode((child) => child.collapseAll());
            this.collapse();
        };

        this.getJsonPath = function () {
            let path = '';
            let node = this;

            do {
                let p;
                if (node.parent && node.parent.type === 'array') {
                    p = '[' + node.key + ']';
                } else {
                    p = node.key;
                }

                path = (path === '') ? p : (node.type === 'array') ? p + path : p + '.' + path;
                node = node.parent;
            } while (node != null);

            return path;
        };

        this.render = function (targetElem) {
            targetElem.appendChild(this.elem);
            this.forEachChildNode((child) => child.render(targetElem));
        };

        this.search = function (searchPhrase, searchState) {
            let found = false;

            // key
            if (findAndMarkMatches(this.key, this.elem.childNodes.item(1), searchPhrase, searchState)) {
                found = true;
            }

            // value
            if (this.isLeaf() && findAndMarkMatches(this.value, this.elem.childNodes.item(3), searchPhrase, searchState)) {
                found = true;
            }

            // children
            this.forEachChildNode((child) => found = child.search(searchPhrase, searchState) || found);

            // change appearance of this node accordingly
            this.setVisible(found || !searchState.filter);
            found && this.setExpanded(true);

            return found;
        };

        function findAndMarkMatches(value, elem, searchPhrase, searchState) {
            const empty = searchPhrase.length == 0;
            const escapedSearchPhrase = escapeRegExp(searchPhrase);
            const regex = (searchState.ignoreCase) ? new RegExp(escapedSearchPhrase, 'i') : new RegExp(escapedSearchPhrase);
            const found = (empty) ? false : value.search(regex) != -1;

            if (found) {
                while (elem.firstChild) {
                    elem.removeChild(elem.lastChild);
                }

                const parts = value.split(regex);
                let index = 0;
                parts.forEach((part, i) => {
                    if (i != 0) {
                        const matchedText = value.substr(index, searchPhrase.length);
                        const span = createElement('span', { className: 'json-match', content: matchedText });
                        elem.appendChild(span);

                        searchState.matches.push(span);
                        index += searchPhrase.length;
                    }

                    const text = document.createTextNode(part);
                    elem.appendChild(text);

                    index += part.length;
                });
            }
            else {
                elem.textContent = value;
            }

            return empty || found;
        }

        function escapeRegExp(s) {
            return s.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
        }
    }

    /**
     * Creates a tree that resembles the given data object.
     *
     * @param {*} data - the data object
     * @return {TreeNode} the tree
     */
    function createTree(data) {
        return createTreeNode('$', data, null);
    }

    /**
     * Creates a tree node for the given key and value. If the value is complex, child nodes will be created recursively.
     *
     * @param {string} key - the name of the new node
     * @param {*} value - the value of the new node
     * @param {TreeNode} parentNode - the parent node of the new node
     * @return {TreeNode} the new node
     */
    function createTreeNode(key, value, parentNode) {
        const node = new TreeNode();
        node.parent = parentNode;
        node.key = key;
        node.type = getType(value);
        node.depth = parentNode ? parentNode.depth + 1 : 0;

        if ((node.type === 'object' || node.type === 'array') && value !== null) {
            node.children = [];

            for (let key in value) {
                const childNode = createTreeNode(key, value[key], node);
                node.children.push(childNode);
            }
        }
        else if (node.type === 'string') {
            node.value = JSON.stringify(value);
        }
        else {
            node.value = String(value);
        }

        node.elem = createLineElement(node);

        return node;
    }

    /**
     * Returns the type of the given value.
     *
     * @param {*} value - the value
     * @returns {string} the type name, one of ['object', 'array', 'number', 'string', 'boolean', 'null']
     */
    function getType(value) {
        return (value === null) ? 'null' : Array.isArray(value) ? 'array' : typeof value;
    }

    /**
     * Creates an HTML element that represents a node in the tree.
     *
     * @param {TreeNode} node - a tree node
     * @returns {HTMLElement}
     */
    function createLineElement(node) {
        const childElements = [];

        // caret
        const caretClasses = node.hasChildren() ? 'json-caret' : 'json-caret json-caret-empty';
        const caretElement = createElement('div', { className: caretClasses });
        childElements.push(caretElement);

        // key or index
        const keyOrIndexClasses = (node.parent && node.parent.type === 'array') ? 'json-index' : 'json-key';
        const keyOrIndexElement = createElement('div', { className: keyOrIndexClasses, content: node.key });
        childElements.push(keyOrIndexElement);

        // size or separator/value
        if (node.type === 'object' || node.type === 'array') {
            // size
            const content = (node.type === 'array') ? '[' + node.children.length + ']' : '{' + node.children.length + '}';
            const sizeElement = createElement('div', { className: 'json-size', content: content });
            childElements.push(sizeElement);
        }
        else { // node.type is one out of ['null', 'number', 'string', 'boolean']
            // separator
            const separatorElement = createElement('div', { className: 'json-separator', content: ':' });
            childElements.push(separatorElement);

            // value
            const valueClasses = 'json-value json-' + node.type;
            const valueElement = createElement('div', { className: valueClasses, content: node.value });
            childElements.push(valueElement);
        }

        // compose a line from the children
        const lineElement = createElement('div', { className: 'json-line', children: childElements });
        lineElement.style = 'margin-left: ' + node.depth * 24 + 'px;';

        return lineElement;
    }

    /**
     * Creates an HTML element.
     *
     * @param {string} type - the tag name
     * @param {object} config - additional options
     * @returns {HTMLElement} the new HTML element
     */
    function createElement(type, config) {
        const htmlElement = document.createElement(type);

        if (config) {
            if (config.className) {
                htmlElement.className = config.className;
            }

            if (config.content) {
                htmlElement.textContent = config.content;
            }

            if (config.children) {
                config.children.forEach((el) => htmlElement.appendChild(el));
            }
        }

        return htmlElement;
    }

    /**
     * Attaches an event handler to the HTML element that represents the given node. The same handler is attached to all child nodes recursively.
     *
     * @param {TreeNode} node - a tree node
     * @param {(TreeNode) => void} handler - the handler to attach 
     */
    function attachEventHandlers(node, handler) {
        node.elem.addEventListener('click', () => handler(node));
        node.forEachChildNode((child) => attachEventHandlers(child, handler));
    }

    /**
     * Renders JSON data in a tree-like view.
     */
    class JsonTree extends HTMLElement {

        /** The tree. */
        tree = null;

        /**  */
        searchState = null;

        /**
         * Clears any rendered JSON.
         */
        clear() {
            this.tree = null;
            this.searchState = null;
            this.innerHTML = "";
        }

        /**
         * Creates a tree from the JSON data and renders it into the JsonTree element.
         *
         * @param {string} jsonData - the JSON data in string form
         */
        load(jsonData) {
            this.clear();

            try {
                const parsedData = JSON.parse(jsonData);

                this.tree = createTree(parsedData);
                this.tree.render(this);

                attachEventHandlers(this.tree, (node) => node.toggle());

                this.tree.collapseAll();
                this.tree.expand();
            }
            catch (error) {
                this.textContent = error;
            }
        }

        /**
         * Collapses all JSON nodes recursively.
         */
        collapseAll() {
            this.tree && this.tree.collapseAll();
        }

        /**
         * Expands all JSON nodes recursively.
         */
        expandAll() {
            this.tree && this.tree.expandAll();
        }

        /**
         * Searches the JSON tree for entries matching the search phrase and marks any match.
         *
         * @param {string} searchPhrase - the text to search for
         * @param {boolean} ignoreCase - wehther the search is case insensitive
         * @param {boolean} filter - whether non-matching lines should be filtered out
         * @return {number} the number of matches found
         */
        search(searchPhrase, ignoreCase, filter) {
            if (this.tree) {
                searchPhrase = searchPhrase || '';
                this.searchState = { matches: [], currentMatch: undefined, ignoreCase: ignoreCase, filter: filter, };

                this.tree.search(searchPhrase, this.searchState);
                this.highlightNextMatch(true);

                return this.searchState.matches.length;
            }
            else {
                return 0;
            }
        }

        /**
         * Highlights the next occurrence of the search phrase in the JSON tree.
         *
         * @param {boolean} forward - whether to go forward or backward
         */
        highlightNextMatch(forward) {
            if (this.searchState && this.searchState.matches.length > 0) {
                if (this.searchState.currentMatch === undefined) {
                    this.searchState.currentMatch = 0;
                }
                else {
                    this.searchState.matches[this.searchState.currentMatch].classList.remove('json-match-current');

                    if (forward) {
                        this.searchState.currentMatch = (this.searchState.currentMatch == this.searchState.matches.length - 1) ? 0 : this.searchState.currentMatch + 1;
                    }
                    else {
                        this.searchState.currentMatch = (this.searchState.currentMatch == 0) ? this.searchState.matches.length - 1 : this.searchState.currentMatch - 1;
                    }
                }

                this.searchState.matches[this.searchState.currentMatch].scrollIntoView(!forward);
                this.searchState.matches[this.searchState.currentMatch].classList.add('json-match-current');
            }
        }

        /**
         * Adds an event handler that is called with the JSON path of a node whenever a node in the JSON tree is clicked.
         *
         * @param {(string) => void} handler - consumes the JSON path of the target node
         */
        onJsonNodeSelected(handler) {
            if (this.tree) {
                attachEventHandlers(this.tree, (node) => handler(node.getJsonPath()));
            }
        }   
    }

    // register the JsonTree web component
    window.customElements.define('json-tree', JsonTree);

})();
