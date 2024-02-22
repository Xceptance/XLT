/*
 * Copyright (c) 2002-2024 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.htmlunit.javascript;

import java.lang.ref.WeakReference;

import org.htmlunit.Page;

/**
 * An action triggered by a script execution but that should be executed first when the script is finished.
 * Example: when a script sets the source of an (i)frame, the request to the specified page will be first
 * triggered after the script execution.
 *
 * @author Marc Guillemot
 * @author Ronald Brill
 */
public abstract class PostponedAction {

    private final WeakReference<Page> owningPageRef_; // as weak ref in case it may allow page to be GCed
    private final String description_;

    /**
     * C'tor.
     * @param owningPage the page that initiates this action
     * @param description information making debugging easier
     */
    public PostponedAction(final Page owningPage, final String description) {
        owningPageRef_ = new WeakReference<>(owningPage);
        description_ = description;
    }

    /**
     * Gets the owning page.
     * @return the page that initiated this action or {@code null} if it has already been GCed
     */
    protected Page getOwningPage() {
        return owningPageRef_.get();
    }

    /**
     * Execute the action.
     * @throws Exception if it fails
     */
    public abstract void execute() throws Exception;

    /**
     * Indicates if the action still needs to be executed.
     * @return {@code true} if the action needs to be executed
     */
    public boolean isStillAlive() {
        final Page owningPage = getOwningPage();
        return owningPage != null && owningPage == owningPage.getEnclosingWindow().getEnclosedPage();
    }

    @Override
    public String toString() {
        if (description_ == null) {
            return super.toString();
        }

        return "PostponedAction(" + description_ + ")";
    }
}
