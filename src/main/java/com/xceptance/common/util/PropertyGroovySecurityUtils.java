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
package com.xceptance.common.util;

import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
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
 * @since 10.0.0
 */
public class PropertyGroovySecurityUtils
{
    /**
     * Type-name prefixes whose construction is blocked in scripts.
     * <p>
     * This list is checked by the custom {@link SecureASTCustomizer.ExpressionChecker} against every
     * {@link ConstructorCallExpression} found in the AST. Note that {@code setDisallowedReceiversClasses} only covers
     * <em>method calls</em> on an existing receiver; it does not intercept {@code new Foo(...)} constructor
     * expressions, so both mechanisms are needed.
     * </p>
     */
    private static final List<String> BLOCKED_CONSTRUCTOR_PREFIXES = List.of("java.io.", "java.nio.", "java.net.", "java.lang.Runtime",
                                                                             "java.lang.ProcessBuilder", "java.lang.Thread",
                                                                             "java.lang.ClassLoader",
                                                                             // Groovy scripting infrastructure — block GroovyShell inception
                                                                             "groovy.lang.", "org.codehaus.groovy.");

    /**
     * The list of packages star-imports are allowed for.
     */
    private static final List<String> ALLOWED_STAR_IMPORT_PACKAGES = List.of("java.util", "java.math", "java.text");

    /**
     * The list of dangerous classes for which method calls are blocked if the class is the explicit receiver (e.g.
     * Runtime.getRuntime().exec(...), System.exit(...)).
     */
    @SuppressWarnings("rawtypes")
    private static final List<Class> DISALLOWED_RECEIVERS_CLASSES = List.of(
                                                                            // System and runtime
                                                                            System.class, Runtime.class, ProcessBuilder.class, Thread.class,
                                                                            ClassLoader.class,
                                                                            // Reflection
                                                                            Class.class,
                                                                            // File I/O
                                                                            java.io.File.class, java.io.FileReader.class,
                                                                            java.io.FileWriter.class, java.io.FileInputStream.class,
                                                                            java.io.FileOutputStream.class, java.io.RandomAccessFile.class,
                                                                            // Network
                                                                            java.net.URL.class, java.net.URI.class, java.net.Socket.class,
                                                                            java.net.ServerSocket.class, java.net.HttpURLConnection.class);

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
     * <p>
     * Two complementary mechanisms are used:
     * <ol>
     * <li><b>Constructor check</b> — a custom {@link SecureASTCustomizer.ExpressionChecker} rejects
     * {@code new dangerous.Type(...)} expressions for packages listed in {@link #BLOCKED_CONSTRUCTOR_PREFIXES}.</li>
     * <li><b>Receiver check</b> — {@code setDisallowedReceiversClasses} rejects method calls where a dangerous class is
     * the explicit receiver (e.g. {@code Runtime.getRuntime().exec(...)}).</li>
     * </ol>
     * {@code setIndirectImportCheckEnabled} is intentionally left at its default ({@code false}). When enabled it
     * inspects every method-call receiver's static type and rejects anything not on the imports whitelist — including
     * {@code java.lang.Object}, which is the compile-time type of all Groovy binding variables ({@code props},
     * {@code ctx}, etc.), causing false positives for legitimate property access.
     * </p>
     *
     * @return configured SecureASTCustomizer
     */
    public static SecureASTCustomizer createSecureCustomizer()
    {
        final SecureASTCustomizer secure = new SecureASTCustomizer();

        // Allow closures for functional calculations
        secure.setClosuresAllowed(true);

        // Allow star-imports from safe packages only
        secure.setAllowedStarImports(ALLOWED_STAR_IMPORT_PACKAGES);

        // Block constructor calls to dangerous types (e.g. new java.io.File(...), new java.net.URL(...)).
        // setDisallowedReceiversClasses does NOT cover ConstructorCallExpression, so we need a separate check.
        //
        // Additionally block method calls by name that are known Groovy GDK escape vectors.
        // 'execute' is added by DefaultGroovyMethods to String/String[] at runtime and calls Runtime.exec()
        // 'getClass' and '.class' allow bypassing the receiver checker by getting Class<?> dynamically.
        final Set<String> blockedMethodNames = Set.of("execute", "getClass");

        secure.addExpressionCheckers(final expr -> {
            if (expr instanceof ConstructorCallExpression)
            {
                final String typeName = expr.getType().getName();
                for (final String prefix : BLOCKED_CONSTRUCTOR_PREFIXES)
                {
                    if (typeName.startsWith(prefix))
                    {
                        return false;
                    }
                }
            }
            else if (expr instanceof MethodCallExpression)
            {
                final String methodName = ((MethodCallExpression) expr).getMethodAsString();
                if (methodName != null && blockedMethodNames.contains(methodName))
                {
                    return false;
                }
            }
            else if (expr instanceof org.codehaus.groovy.ast.expr.PropertyExpression)
            {
                final String propertyName = ((org.codehaus.groovy.ast.expr.PropertyExpression) expr).getPropertyAsString();
                if ("class".equals(propertyName))
                {
                    return false;
                }
            }
            return true;
        });

        // Block method calls where a dangerous class is the explicit receiver
        // (e.g. Runtime.getRuntime().exec(...), System.exit(...))
        secure.setDisallowedReceiversClasses(DISALLOWED_RECEIVERS_CLASSES);

        return secure;
    }
}
