/*
 * Copyright (c) 2005-2021 Xceptance Software Technologies GmbH
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
package com.xceptance.xlt.report.mergerules;

/**
 * Exception used to indicate an invalid request processing rule.
 * 
 * @author Hartmut Arlt (Xceptance Software Technologies GmbH)
 */
public class InvalidRequestProcessingRuleException extends Exception
{
    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = 7981238688848743328L;

    /**
     * 
     */
    public InvalidRequestProcessingRuleException()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public InvalidRequestProcessingRuleException(String message)
    {
        super(message);
        // TODO Auto-generated constructor stub
    }
}
