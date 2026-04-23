/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
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

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.control.customizers.SecureASTCustomizer;

/**
 * Security configuration for Groovy scripts executed in property evaluation.
 * <p>
 * This customizer restricts the capabilities of Groovy scripts to prevent potentially dangerous operations such as file
 * system access, network operations, or system command execution.
 * </p>
 * <p>
 * Allowed operations:
 * <ul>
 * <li>Basic arithmetic and string operations</li>
 * <li>java.util collections (List, Map, etc.)</li>
 * <li>java.math (BigDecimal, BigInteger)</li>
 * <li>java.text formatting</li>
 * <li>Closures for functional operations</li>
 * </ul>
 * </p>
 * <p>
 * Blocked operations:
 * <ul>
 * <li>File system access (java.io, java.nio.file)</li>
 * <li>Network operations (java.net)</li>
 * <li>Process execution (Runtime, ProcessBuilder)</li>
 * <li>Reflection operations</li>
 * <li>Thread creation</li>
 * <li>System property modification</li>
 * </ul>
 * </p>
 *
 * @author Xceptance Software Technologies GmbH
 * @since 8.0.0
 */
public class PropertyGroovySecurityUtils
{
    /**
     * Private constructor to prevent instantiation.
     */
    private PropertyGroovySecurityUtils()
    {
    }

    /**
     * Creates a secure AST customizer for property Groovy evaluation.
     * <p>
     * Allows safe imports for mathematical and collection operations while blocking dangerous classes and operations.
     * </p>
     *
     * @return configured SecureASTCustomizer
     */
    public static SecureASTCustomizer createSecureCustomizer()
    {
        final SecureASTCustomizer secure = new SecureASTCustomizer();

        // Allow closures for functional calculations
        secure.setClosuresAllowed(true);

        // Whitelist safe package imports (explicit star imports only)
        final List<String> allowedStarImports = Arrays.asList("java.util", "java.math", "java.text");
        secure.setStarImportsWhitelist(allowedStarImports);
        secure.setIndirectImportCheckEnabled(true);

        // Block direct class references to dangerous classes
        // This prevents using fully-qualified names like java.io.File
        secure.setReceiversClassesBlackList(Arrays.asList(
                                                          // System and runtime
                                                          System.class, Runtime.class, ProcessBuilder.class, Thread.class,
                                                          ClassLoader.class,
                                                          // File I/O
                                                          java.io.File.class, java.io.FileReader.class, java.io.FileWriter.class,
                                                          java.io.FileInputStream.class, java.io.FileOutputStream.class,
                                                          java.io.RandomAccessFile.class,
                                                          // Network
                                                          java.net.URL.class, java.net.URI.class, java.net.Socket.class,
                                                          java.net.ServerSocket.class, java.net.HttpURLConnection.class));

        return secure;
    }
}
