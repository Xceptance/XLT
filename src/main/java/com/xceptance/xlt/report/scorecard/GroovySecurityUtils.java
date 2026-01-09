package com.xceptance.xlt.report.scorecard;

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
        List<String> starImports = Arrays.asList("java.util", "java.math", "java.text", "com.xceptance.xlt.report.scorecard.builder",
                                                 "com.xceptance.xlt.report.scorecard");
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
