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
package com.xceptance.xlt.report.scorecard.groovy;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

/**
 * Utility to provide secure Groovy configuration.
 */
public class GroovySecurityUtils
{

    public static SecureASTCustomizer createSecureCustomizer()
    {
        SecureASTCustomizer secure = new SecureASTCustomizer();

        // Disallow closures? No, we need them for data building.
        secure.setClosuresAllowed(true);

        // Imports
        List<String> starImports = Arrays.asList("java.util", "java.math", "java.text", "com.xceptance.xlt.report.scorecard.groovy.builder",
                                                 "com.xceptance.xlt.report.scorecard",
                                                 "com.xceptance.xlt.report.scorecard.groovy.ScorecardLogger");
        secure.setStarImportsWhitelist(starImports);

        // allow static imports if needed, for now none

        // Tokens - Block Threading, System, etc.
        // Actually, token restriction is tricky. Better to restrict Types/Receivers.

        // Block dangerous classes via package restriction mechanism implies we whitelist expected types.
        // But whitelisting types is very restrictive.
        // Let's use checking of imports + receivers if possible, or simple Disallowed implementations.

        return secure;
    }
}
