/*
 * Copyright (c) 2005-2022 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.api.util;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.xceptance.xlt.api.actions.AbstractWebAction;

/**
 * Using response content processors, one can modify the content of a response body right after download but still
 * before the content is being parsed or compiled. This can be useful if the original content screws up HtmlUnit and the
 * application cannot be fixed right now.
 * 
 * @see AbstractWebAction#addResponseProcessor(ResponseProcessor)
 * @author Jörg Werner (Xceptance Software Technologies GmbH)
 */
public interface ResponseProcessor
{
    /**
     * Processes the content of a response.
     * 
     * @param webResponse
     *            the web response to modify
     * @return the (potentially) modified web response
     */
    public WebResponse processResponse(WebResponse webResponse);
}
