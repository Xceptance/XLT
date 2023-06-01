/*
 * Copyright (c) 2005-2023 Xceptance Software Technologies GmbH
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
package com.xceptance.common.util;

/**
 * Some standard exit codes to use when quitting a program via {@link System#exit(int)}.
 */
public interface ProcessExitCodes
{
    /**
     * Success.
     */
    public static final int SUCCESS = 0;

    /**
     * General error.
     */
    public static final int GENERAL_ERROR = 1;

    /**
     * The parameters given on the command line are either unknown, incomplete, or not valid.
     */
    public static final int PARAMETER_ERROR = 2;
}
