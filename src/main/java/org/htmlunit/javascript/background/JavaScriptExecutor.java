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
package org.htmlunit.javascript.background;

import org.htmlunit.WebWindow;

/**
 * An event loop to execute all the JavaScript jobs.
 *
 * @author Amit Manjhi
 * @author Kostadin Chikov
 * @author Ronald Brill
 */
public interface JavaScriptExecutor extends Runnable {

    /**
     * Register a window with the eventLoop.
     * @param newWindow the new web window
     */
    void addWindow(WebWindow newWindow);

    /**
     * Notes that this thread has been shutdown.
     */
    void shutdown();
}
