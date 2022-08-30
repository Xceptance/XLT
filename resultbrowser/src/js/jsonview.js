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

            // TODO
            document.querySelector('#jsonPath').textContent = path;
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
            found ? this.setExpanded(true) : this.collapse();

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

        // attach event handlers to the line element
        const handleClick = node.toggle.bind(node);
        const handleClick2 = node.getJsonPath.bind(node);
        lineElement.addEventListener('click', handleClick);
        lineElement.addEventListener('click', handleClick2);

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

    /** The tree. */
    let tree = null;

    /**  */
    let searchState = null;

    /* Export jsonView object */
    window.jsonView = {
        /**
           * Creates a tree from the JSON data and renders it into a DOM container.
           *
           * @param {string} jsonData - the JSON data in string form
           * @param {string} targetElementSelector - the CSS selector specifying the target element
           */
        format: function (jsonData, targetElementSelector) {
            const targetElement = document.querySelector(targetElementSelector) || document.body;

            try {
                const parsedData = JSON.parse(jsonData);

                tree = createTree(parsedData);

                tree.render(targetElement);
                tree.collapseAll();
                tree.expand();
            }
            catch (error) {
                targetElement.textContent = error;
            }
        },

        /**
         * Collapses all JSON nodes recursively.
         */
        collapseAll: function () {
            tree && tree.collapseAll();
        },

        /**
         * Expands all JSON nodes recursively.
         */
        expandAll: function () {
            tree && tree.expandAll();
        },

        /**
         * The CSS selector for the DOM element which holds the number of matches found.
         */
        matchesElementSelector: '#matches',

        /**
         * Searches the JSON tree for entries matching the search phrase and marks any match.
         *
         * @param {string} searchPhrase - the text to search for
         * @param {boolean} ignoreCase - wehther the search is case insensitive
         * @param {boolean} filter - whether non-matching lines should be filtered out
         */
        search: function (searchPhrase, ignoreCase, filter) {
            if (tree) {
                searchPhrase = searchPhrase || '';
                searchState = { matches: [], currentMatch: undefined, ignoreCase: ignoreCase, filter: filter, };

                tree.search(searchPhrase, searchState);
                this.highlightNextMatch(true);

                // update number of matches found
                document.querySelector(this.matchesElementSelector).textContent = searchState.matches.length;
            }
        },

        /**
         * Highlights the next occurrence of the search phrase in the JSON tree.
         *
         * @param {boolean} forward - whether to go forward or backward
         */
        highlightNextMatch: function (forward) {
            if (searchState && searchState.matches.length > 0) {
                if (searchState.currentMatch === undefined) {
                    searchState.currentMatch = 0;
                }
                else {
                    searchState.matches[searchState.currentMatch].classList.remove('json-match-current');

                    if (forward) {
                        searchState.currentMatch = (searchState.currentMatch == searchState.matches.length - 1) ? 0 : searchState.currentMatch + 1;
                    }
                    else {
                        searchState.currentMatch = (searchState.currentMatch == 0) ? searchState.matches.length - 1 : searchState.currentMatch - 1;
                    }
                }

                searchState.matches[searchState.currentMatch].scrollIntoView(!forward);
                searchState.matches[searchState.currentMatch].classList.add('json-match-current');
            }
        },
    }

})();