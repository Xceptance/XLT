/*
 * Copyright (c) 2005-2024 Xceptance Software Technologies GmbH
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
package util.xlt;

/**
 * An {@link AssertionError} type whose purpose is to be thrown intentionally within unit tests for a framework in order
 * to test the frameworks's behavior in case of exceptions/errors
 * 
 * @author Deniz Altin
 */
public class IntentionalError extends AssertionError
{
    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_MESSAGE = "Intentional Error";

    public IntentionalError()
    {
        this(DEFAULT_MESSAGE);
    }

    public IntentionalError(final String detailMessage)
    {
        super(detailMessage);
    }
}
