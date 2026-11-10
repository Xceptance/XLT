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
package com.xceptance.util.fastregex;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.junit.Assert.*;

/**
 * Test suite for FastRegexExtensiveTest.
 *
 * @author Gemini
 * @author Rene Schwietzke
 */
@RunWith(JUnitParamsRunner.class)
public class FastRegexExtensiveTest
{

    private static final String BASE_HOST =
            "https://production-test.bathandbodyworks.com";

    private static final String PRODUCT_SEARCH_URL =
            BASE_HOST +
            "/mobify/proxy/api/search/shopper-search/v1/organizations/f_ecom_abbo_prd/product-search" +
            "?expand=availability%2Cimages%2Cprices%2Crepresented_products%2Cvariations%2Cpromotions%2Ccustom_properties" +
            "&c_storeId=BBW01449&siteId=BathAndBodyWorks&refine=cgid%3Dmens-body-sprays-cologne" +
            "&refine=orderable_only%3Dfalse&limit=60&refine=c_productType%3DCologne&offset=0";

    private static final String[] NATIVE_RULES = {
            "/sessions.onsession",
            "/oauth2/([^/\\?]+)",
            "shop/v\\d\\d_\\d/([^/\\?]+).*",
            "shop/v\\d\\d_\\d/[^/\\?]+/(auth)",
            "/api/preferences",
            "custom/[^/]+/[^/?]+/[^/\\?]+/[^/\\?]+/([^/\\?]+)",
            "shop/v\\d\\d_\\d/[^/\\?]+/[^/\\?]+/[^/\\?]+/[^/\\?]+/([^/\\?]+).*"
    };

    private static final String[] FALLBACK_RULES = {
            "(shopper-([^/]+))/[\\w/]+/(\\2-suggestions)(?=\\?|([/A-z-0-9]+)\\?)",
            "(shopper-([^/]+))/[\\w/]+/(categories)(?=\\?|([/A-z-0-9]+)\\?)",
            "(shopper-([^/]+))/[\\w/]+/(categories)(?=\\?|([/A-z-0-9]+)\\?)"
    };

    private static final String[] ALL_RULES = {
            "/sessions.onsession",
            "/oauth2/([^/\\?]+)",
            "shop/v\\d\\d_\\d/([^/\\?]+).*",
            "shop/v\\d\\d_\\d/[^/\\?]+/(auth)",
            "/api/preferences",
            "custom/[^/]+/[^/?]+/[^/\\?]+/[^/\\?]+/([^/\\?]+)",
            "shop/v\\d\\d_\\d/[^/\\?]+/[^/\\?]+/[^/\\?]+/[^/\\?]+/([^/\\?]+).*",
            "(shopper-([^/]+))/[\\w/]+/(\\2-suggestions)(?=\\?|([/A-z-0-9]+)\\?)",
            "(shopper-([^/]+))/[\\w/]+/(categories)(?=\\?|([/A-z-0-9]+)\\?)",
            "(shopper-([^/]+))/[\\w/]+/(categories)(?=\\?|([/A-z-0-9]+)\\?)"
    };

    private static final String[] URL_CORPUS = {
            // Exact product-search base URL (non-matching across the 10 rules)
            PRODUCT_SEARCH_URL,
            // Endpoint variants on the same base URL exercising every rule's matching and non-matching branches
            BASE_HOST + "/on/demandware.store/Sites-BathAndBodyWorks-Site/en_US/sessions-onsession?siteId=BathAndBodyWorks",
            BASE_HOST + "/on/demandware.store/Sites-BathAndBodyWorks-Site/en_US/sessions\nonsession?siteId=BathAndBodyWorks",
            BASE_HOST + "/mobify/proxy/api/shopper/auth/v1/organizations/f_ecom_abbo_prd/oauth2/token?c_storeId=BBW01449&siteId=BathAndBodyWorks",
            BASE_HOST + "/mobify/proxy/api/shopper/auth/v1/organizations/f_ecom_abbo_prd/oauth2/?c_storeId=BBW01449",
            BASE_HOST + "/s/BathAndBodyWorks/dw/shop/v24_5/products?expand=availability%2Cimages%2Cprices&siteId=BathAndBodyWorks",
            BASE_HOST + "/s/BathAndBodyWorks/dw/shop/v24_5/customers/auth?c_storeId=BBW01449&siteId=BathAndBodyWorks",
            BASE_HOST + "/s/BathAndBodyWorks/dw/shop/v12_shop/v24_5/customers/auth?siteId=BathAndBodyWorks",
            BASE_HOST + "/mobify/proxy/api/preferences?c_storeId=BBW01449&siteId=BathAndBodyWorks",
            BASE_HOST + "/mobify/proxy/api/custom/loyalty/v1/organizations/f_ecom_abbo_prd/points?c_storeId=BBW01449&siteId=BathAndBodyWorks",
            BASE_HOST + "/s/BathAndBodyWorks/dw/shop/v24_5/baskets/b1/shipments/me/shipping_methods?siteId=BathAndBodyWorks&offset=0",
            BASE_HOST + "/mobify/proxy/api/search/shopper-search/v1/organizations/f_ecom_abbo_prd/search-suggestions?q=cologne&c_storeId=BBW01449&siteId=BathAndBodyWorks",
            BASE_HOST + "/mobify/proxy/api/search/shopper-search/v1/organizations/f_ecom_abbo_prd/search-suggestions/popular-01?q=cologne&siteId=BathAndBodyWorks",
            BASE_HOST + "/mobify/proxy/api/search/shopper-products/v1/organizations/f_ecom_abbo_prd/search-suggestions?q=cologne&siteId=BathAndBodyWorks",
            BASE_HOST + "/mobify/proxy/api/product/shopper-products/v1/organizations/f_ecom_abbo_prd/categories?c_storeId=BBW01449&siteId=BathAndBodyWorks&limit=60",
            BASE_HOST + "/mobify/proxy/api/product/shopper-products/v1/organizations/f_ecom_abbo_prd/categories/mens-body-sprays-cologne?c_storeId=BBW01449&siteId=BathAndBodyWorks",
            // Exact relative path slices to exercise full-string matches() = true
            "/sessions_onsession",
            "/oauth2/access_token",
            "shop/v24_5/products?expand=prices&limit=60",
            "shop/v24_5/customers/auth",
            "/api/preferences",
            "custom/loyalty/v1/organizations/f_ecom_abbo_prd/points",
            "shop/v24_5/baskets/b1/shipments/me/shipping_methods?offset=0",
            // Multi-URL access log entry to exercise multiple findAll() matches in one input
            PRODUCT_SEARCH_URL + " | " +
                    BASE_HOST + "/mobify/proxy/api/preferences?siteId=BathAndBodyWorks | " +
                    BASE_HOST + "/mobify/proxy/api/shopper/auth/v1/oauth2/refresh_token?siteId=BathAndBodyWorks"
    };

    private void assertMatchesMatchJdk(String pattern, String... inputs)
    {
        Pattern jdk = Pattern.compile(pattern);
        FastRegex.Pattern fast = FastRegex.compileStrict(pattern);
        var fastMatcher = fast.matcher();
        var res = fast.newMatchResult();

        for (String input : inputs)
        {
            var jdkMatcher = jdk.matcher(input);
            boolean jdkMatches = jdkMatcher.matches();
            boolean fastMatches = fastMatcher.matches(input, res);
            assertEquals("matches() mismatch for pattern '" + pattern + "' on input: '" + input + "'", jdkMatches, fastMatches);

            if (jdkMatches)
            {
                assertEquals("start() mismatch for '" + input + "'", jdkMatcher.start(), res.start());
                assertEquals("end() mismatch for '" + input + "'", jdkMatcher.end(), res.end());
                assertEquals("groupCount() mismatch", jdkMatcher.groupCount(), res.groupCount());
                for (int g = 0; g <= jdkMatcher.groupCount(); g++)
                {
                    assertEquals("start(" + g + ") mismatch", jdkMatcher.start(g), res.start(g));
                    assertEquals("end(" + g + ") mismatch", jdkMatcher.end(g), res.end(g));
                    assertEquals("group(" + g + ") mismatch", jdkMatcher.group(g), res.group(g));
                }
            }
        }
    }

    private void assertFindMatchesMatchJdk(String pattern, String... inputs)
    {
        Pattern jdk = Pattern.compile(pattern);
        FastRegex.Pattern fast = FastRegex.compileStrict(pattern);
        var fastMatcher = fast.matcher();
        var res = fast.newMatchResult();

        for (String input : inputs)
        {
            var jdkMatcher = jdk.matcher(input);
            List<String> jdkMatches = new ArrayList<>();
            while (jdkMatcher.find())
            {
                StringBuilder sb = new StringBuilder();
                sb.append(jdkMatcher.start()).append(":").append(jdkMatcher.end()).append("=").append(jdkMatcher.group());
                for (int g = 1; g <= jdkMatcher.groupCount(); g++)
                {
                    sb.append(",g").append(g).append("=").append(jdkMatcher.group(g));
                }
                jdkMatches.add(sb.toString());
            }

            List<String> fastMatches = new ArrayList<>();
            for (var m : fastMatcher.findAll(input, res))
            {
                StringBuilder sb = new StringBuilder();
                sb.append(m.start()).append(":").append(m.end()).append("=").append(m.group());
                for (int g = 1; g <= m.groupCount(); g++)
                {
                    sb.append(",g").append(g).append("=").append(m.group(g));
                }
                fastMatches.add(sb.toString());
            }

            assertEquals("findAll mismatch for pattern '" + pattern + "' on input: '" + input + "'", jdkMatches, fastMatches);

            for (int from = 0; from <= input.length(); from++)
            {
                boolean jFound = jdkMatcher.find(from);
                boolean fFound = fastMatcher.find(input, from, res);
                assertEquals("find(" + from + ") mismatch for pattern '" + pattern + "' on input: '" + input + "'", jFound, fFound);
                if (jFound)
                {
                    assertEquals(jdkMatcher.start(), res.start());
                    assertEquals(jdkMatcher.end(), res.end());
                    assertEquals(jdkMatcher.group(), res.group());
                }
            }
        }
    }

    // Alternation and Greedy Priority Tests

    @Test
    public void testAlternationLongestFirst()
    {
        assertMatchesMatchJdk("foo|foobar|bar", "foo", "foobar", "bar", "foobaz", "baz");
        assertFindMatchesMatchJdk("cat|cattle|dog", "the cattle and the cat");
        assertFindMatchesMatchJdk("(ab|a)(bc|c)", "abc");
    }

    @Test
    public void testEmptyBranches()
    {
        assertFindMatchesMatchJdk("a|", "a", "b", "aa");
        assertFindMatchesMatchJdk("|a", "a", "b", "");
    }

    // Character Classes Complex Tests

    @Test
    public void testRangesAndNegation()
    {
        assertMatchesMatchJdk("[^a-z]", "A", "1", "a", "z", "_", "ü", "€");
        assertMatchesMatchJdk("[a-zA-Z0-9_.-]+", "foo.bar-baz_123", "foo@bar");
        assertMatchesMatchJdk("[\\d\\w]+", "abc123_", "!");
        assertMatchesMatchJdk("[^\\d]+", "abc!@#", "abc1", "123");
        assertMatchesMatchJdk("[-a-z]", "-", "a", "m", "z", "_");
        assertMatchesMatchJdk("[a-z-]", "-", "a", "m", "z", "_");
        assertMatchesMatchJdk("[a\\-b]", "a", "b", "-", "c");
    }

    // Quantifier Edge Cases

    @Test
    public void testZeroRepeats()
    {
        assertMatchesMatchJdk("a{0}", "", "a");
        assertMatchesMatchJdk("a{0,0}", "", "a");
        assertMatchesMatchJdk("a{0,2}", "", "a", "aa", "aaa");
    }

    @Test
    public void testGreedyVsReluctantFind()
    {
        assertFindMatchesMatchJdk("x*?", "xxxx");
        assertFindMatchesMatchJdk("x+?", "xxxx");
        assertFindMatchesMatchJdk("x??", "xxxx");
        assertFindMatchesMatchJdk("x{1,3}?", "xxxx");
        assertFindMatchesMatchJdk("<.*?>", "<html><head><title>hello</title></head></html>");
    }

    // Submatch Captures in Loops

    @Test
    public void testRepeatedCaptureGroups()
    {
        // (a)+ on "aaa": JDK sets group 1 to the LAST matched repetition "a"
        assertMatchesMatchJdk("(a)+", "a", "aa", "aaa");
        assertMatchesMatchJdk("([0-9])+", "12345");
        assertMatchesMatchJdk("(a|b)+", "ababb");
    }

    @Test
    public void testOptionalGroupsInsideRepeats()
    {
        assertMatchesMatchJdk("(a(b)?)+", "a", "ab", "aba", "abab", "abaa");
    }

    // Stress and Cache Reset Tests

    @Test
    public void testRepeatedMassiveMatchesWithoutLeak()
    {
        var regex = FastRegex.compileStrict("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}");
        var matcher = regex.matcher();
        var res = regex.newMatchResult();

        String valid = "user.name+tag@domain.co.uk";
        for (int i = 0; i < 10_000; i++)
        {
            assertTrue(matcher.matches(valid, res));
        }
    }

    @Test
    public void testLargeAlternationLazyDfaOverflow()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++)
        {
            if (i > 0) sb.append('|');
            sb.append("token_").append(i);
        }
        var regex = FastRegex.compileStrict(sb.toString());
        var matcher = regex.matcher();
        var res = regex.newMatchResult();

        for (int i = 0; i < 300; i++)
        {
            assertTrue(matcher.matches("token_" + i, res));
        }
        assertFalse(matcher.matches("token_301", res));
    }

    /**
     * Asserts complete parity between a compiled {@link java.util.regex.Pattern} and a
     * compiled {@link FastRegex.Pattern} across {@code matches()}, {@code find()}, {@code findAll()},
     * and {@code find(fromIndex)} for all supplied input sequences.
     *
     * @param jdk compiled standard JDK pattern
     * @param fast compiled FastRegex.Pattern instance (strict or fallback)
     * @param inputs character sequences to compare against
     */
    private void assertFullJdkParity(Pattern jdk, FastRegex.Pattern fast, CharSequence... inputs)
    {
        var fastMatcher = fast.matcher();
        var res = fast.newMatchResult();
        String pat = jdk.pattern();
        assertEquals(pat, fast.pattern());

        for (CharSequence input : inputs)
        {
            Matcher jdkMatcher = jdk.matcher(input);
            FastRegex.Matcher stateful = fast.matcher(input);
            assertSame(fast, stateful.pattern());

            // 1. Full-sequence matches() parity (stateless ReusableMatchResult + stateful Matcher)
            boolean jdkMatches = jdkMatcher.matches();
            boolean fastMatches = fastMatcher.matches(input, res);
            boolean statefulMatches = stateful.matches();
            assertEquals("matches() mismatch for pattern '" + pat + "' on input: '" + input + "'",
                    jdkMatches, fastMatches);
            assertEquals("stateful matches() mismatch for pattern '" + pat + "' on input: '" + input + "'",
                    jdkMatches, statefulMatches);
            assertEquals(jdkMatches, res.matched());
            assertEquals(jdkMatches, res.hasMatch());
            assertEquals(jdkMatches, stateful.matched());
            assertEquals(jdkMatches, stateful.hasMatch());
            if (jdkMatches)
            {
                assertMatchDetailsMatchJdk(jdkMatcher, res, pat, input);
                assertMatchDetailsMatchJdk(jdkMatcher, stateful, pat, input);
            }

            // 2. Initial find() parity
            boolean jdkFound = jdkMatcher.reset(input).find();
            boolean fastFound = fastMatcher.find(input, res);
            boolean statefulFound = stateful.reset().find();
            assertEquals("find() mismatch for pattern '" + pat + "' on input: '" + input + "'",
                    jdkFound, fastFound);
            assertEquals("stateful find() mismatch for pattern '" + pat + "' on input: '" + input + "'",
                    jdkFound, statefulFound);
            if (jdkFound)
            {
                assertMatchDetailsMatchJdk(jdkMatcher, res, pat, input);
                assertMatchDetailsMatchJdk(jdkMatcher, stateful, pat, input);
            }

            // 3. Iterative findAll() & stateful while(find()) parity vs while (jdkMatcher.find())
            jdkMatcher.reset(input);
            List<String> jdkAll = new ArrayList<>();
            while (jdkMatcher.find())
            {
                jdkAll.add(formatJdkMatch(jdkMatcher));
            }

            List<String> fastAll = new ArrayList<>();
            for (var m : fastMatcher.findAll(input, res))
            {
                fastAll.add(formatFastMatch(m));
            }
            assertEquals("findAll() mismatch for pattern '" + pat + "' on input: '" + input + "'",
                    jdkAll, fastAll);

            stateful.reset(input);
            List<String> statefulAll = new ArrayList<>();
            while (stateful.find())
            {
                statefulAll.add(formatFastMatch(stateful));
            }
            assertEquals("stateful while(find()) mismatch for pattern '" + pat + "' on input: '" + input + "'",
                    jdkAll, statefulAll);

            // 4. Offset find(fromIndex) parity across every valid index
            int len = input.length();
            for (int from = 0; from <= len; from++)
            {
                boolean jAt = jdkMatcher.find(from);
                boolean fAt = fastMatcher.find(input, from, res);
                boolean sAt = stateful.find(from);
                assertEquals("find(" + from + ") mismatch for pattern '" + pat + "' on input: '" + input + "'",
                        jAt, fAt);
                assertEquals("stateful find(" + from + ") mismatch for pattern '" + pat + "' on input: '" + input + "'",
                        jAt, sAt);
                if (jAt)
                {
                    assertMatchDetailsMatchJdk(jdkMatcher, res, pat, input);
                    assertMatchDetailsMatchJdk(jdkMatcher, stateful, pat, input);
                }
            }
        }
    }

    private void assertMatchDetailsMatchJdk(
            Matcher jdkMatcher,
            FastRegex.FastMatchResult res,
            String pat,
            CharSequence input)
    {
        assertTrue(res.matched());
        assertTrue(res.hasMatch());
        assertEquals("start() mismatch for '" + pat + "' on '" + input + "'", jdkMatcher.start(), res.start());
        assertEquals("end() mismatch for '" + pat + "' on '" + input + "'", jdkMatcher.end(), res.end());
        assertEquals("group() mismatch for '" + pat + "' on '" + input + "'", jdkMatcher.group(), res.group());
        assertEquals("groupView() mismatch for '" + pat + "'", jdkMatcher.group(), res.groupView().toString());
        assertEquals("groupCount() mismatch for '" + pat + "'", jdkMatcher.groupCount(), res.groupCount());

        FastRegex.MatchSnapshot snap = res.snapshot();
        assertTrue(snap.matched());
        assertTrue(snap.hasMatch());
        assertEquals(jdkMatcher.group(), snap.groupView().toString());
        assertEquals(jdkMatcher.groupCount(), snap.groupCount());

        for (int g = 0; g <= jdkMatcher.groupCount(); g++)
        {
            String expectedGroup = jdkMatcher.group(g);
            assertEquals("start(" + g + ") mismatch for '" + pat + "'", jdkMatcher.start(g), res.start(g));
            assertEquals("end(" + g + ") mismatch for '" + pat + "'", jdkMatcher.end(g), res.end(g));
            assertEquals("group(" + g + ") mismatch for '" + pat + "'", expectedGroup, res.group(g));
            assertEquals("snapshot.start(" + g + ") mismatch", jdkMatcher.start(g), snap.start(g));
            assertEquals("snapshot.end(" + g + ") mismatch", jdkMatcher.end(g), snap.end(g));
            assertEquals("snapshot.group(" + g + ") mismatch", expectedGroup, snap.group(g));

            CharSequence view = res.groupView(g);
            if (expectedGroup == null)
            {
                assertNull("Expected null groupView(" + g + ") for '" + pat + "'", view);
                assertNull(snap.groupView(g));
            }
            else
            {
                assertNotNull("Expected non-null groupView(" + g + ") for '" + pat + "'", view);
                assertEquals("groupView(" + g + ") mismatch for '" + pat + "'", expectedGroup, view.toString());
                assertEquals(expectedGroup, snap.groupView(g).toString());
            }
        }
    }

    private static String formatJdkMatch(Matcher m)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(m.start()).append(':').append(m.end()).append('=').append(m.group());
        for (int g = 1; g <= m.groupCount(); g++)
        {
            sb.append("|g").append(g).append('@').append(m.start(g)).append(':').append(m.end(g))
              .append('=').append(m.group(g));
        }
        return sb.toString();
    }

    private static String formatFastMatch(FastRegex.FastMatchResult m)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(m.start()).append(':').append(m.end()).append('=').append(m.group());
        for (int g = 1; g <= m.groupCount(); g++)
        {
            sb.append("|g").append(g).append('@').append(m.start(g)).append(':').append(m.end(g))
              .append('=').append(m.group(g));
        }
        return sb.toString();
    }

    // E-Commerce URL Routing Rules Differential Suite

    /**
     * Verifies that evaluating all 10 e-commerce rules on the exact product-search URL
     * produces identical results to {@link java.util.regex.Pattern} for both {@link String}
     * and non-String {@link CharSequence} inputs, while enforcing that rules 8-10 trigger fallback.
     */
    @Test
    public void testProductSearchBaseUrlDifferentialAgainstAllRules()
    {
        StringBuilder charSeqUrl = new StringBuilder(PRODUCT_SEARCH_URL);

        for (String rule : NATIVE_RULES)
        {
            Pattern jdk = Pattern.compile(rule);
            FastRegex.Pattern strictFast = FastRegex.compileStrict(rule);
            assertFalse("Expected native execution for: " + rule, strictFast.isFallback());
            assertFullJdkParity(jdk, strictFast, PRODUCT_SEARCH_URL, charSeqUrl);
        }

        for (String fallbackRule : FALLBACK_RULES)
        {
            assertThrows("Expected rule to be rejected in strict mode and act as fallback trigger: " + fallbackRule,
                    FastRegex.UnsupportedRegexFeatureException.class,
                    () -> FastRegex.compileStrict(fallbackRule));

            Pattern jdk = Pattern.compile(fallbackRule);
            FastRegex.Pattern fallbackFast = FastRegex.compile(fallbackRule);
            assertTrue("Expected JDK fallback mode for: " + fallbackRule, fallbackFast.isFallback());
            assertFullJdkParity(jdk, fallbackFast, PRODUCT_SEARCH_URL, charSeqUrl);
        }
    }

    /**
     * Verifies compilation mode classification, exact unsupported-feature trigger offsets for rules 8-10,
     * and deterministic SIMD literal prefix extraction for rules 1-7.
     */
    @Test
    public void testRuleCompilationModesAndLiteralPrefixes()
    {
        String[] expectedPrefixes = {
                "/sessions",
                "/oauth2/",
                "shop/v",
                "shop/v",
                "/api/preferences",
                "custom/",
                "shop/v"
        };

        for (int i = 0; i < NATIVE_RULES.length; i++)
        {
            FastRegex.Pattern strict = FastRegex.compileStrict(NATIVE_RULES[i]);
            assertFalse(strict.isFallback());
            assertEquals(expectedPrefixes[i], strict.literalPrefix());
        }

        int[] expectedTriggerPositions = {
                26, // '\2' backreference in rule 8
                37, // '(?=' positive lookahead in rule 9
                37  // '(?=' positive lookahead in rule 10
        };

        for (int i = 0; i < FALLBACK_RULES.length; i++)
        {
            String fallbackRule = FALLBACK_RULES[i];
            var ex = assertThrows(FastRegex.UnsupportedRegexFeatureException.class,
                    () -> FastRegex.compileStrict(fallbackRule));
            assertEquals(fallbackRule, ex.pattern());
            assertEquals(expectedTriggerPositions[i], ex.position());

            FastRegex.Pattern withFallback = FastRegex.compile(fallbackRule);
            assertTrue(withFallback.isFallback());
            assertEquals("", withFallback.literalPrefix());

            FastRegex.Pattern defaultCompiled = FastRegex.compile(fallbackRule);
            assertTrue(defaultCompiled.isFallback());
        }
    }

    public Object[][] parametersForTestNativeRulesDifferentialAcrossEcommerceUrlSuite()
    {
        Object[][] params = new Object[NATIVE_RULES.length][1];
        for (int i = 0; i < NATIVE_RULES.length; i++)
        {
            params[i][0] = NATIVE_RULES[i];
        }
        return params;
    }

    /**
     * Parameterized differential test comparing each supported native e-commerce regex (rules 1-7)
     * against {@link java.util.regex.Pattern} across the full Bath &amp; Body Works URL corpus.
     *
     * @param nativeRule linear-time regular expression rule under test
     */
    @Test
    @Parameters
    public void testNativeRulesDifferentialAcrossEcommerceUrlSuite(String nativeRule)
    {
        Pattern jdk = Pattern.compile(nativeRule);

        FastRegex.Pattern strict = FastRegex.compileStrict(nativeRule);
        assertFalse(strict.isFallback());
        assertFullJdkParity(jdk, strict, URL_CORPUS);

        FastRegex.Pattern hybrid = FastRegex.compile(nativeRule);
        assertFalse(hybrid.isFallback());
        assertFullJdkParity(jdk, hybrid, URL_CORPUS);
    }

    public Object[][] parametersForTestFallbackTriggerRulesDifferentialAcrossEcommerceUrlSuite()
    {
        Object[][] params = new Object[FALLBACK_RULES.length][1];
        for (int i = 0; i < FALLBACK_RULES.length; i++)
        {
            params[i][0] = FALLBACK_RULES[i];
        }
        return params;
    }

    /**
     * Parameterized differential test verifying that rules 8-10 are rejected by {@code compileStrict},
     * trigger automatic JDK fallback in {@code compileWithFallback}/{@code compile}, and preserve
     * full match and subgroup parity against {@link java.util.regex.Pattern}.
     *
     * @param fallbackRule unsupported backreference/lookahead rule acting as fallback trigger
     */
    @Test
    @Parameters
    public void testFallbackTriggerRulesDifferentialAcrossEcommerceUrlSuite(String fallbackRule)
    {
        assertThrows(FastRegex.UnsupportedRegexFeatureException.class,
                () -> FastRegex.compileStrict(fallbackRule));

        Pattern jdk = Pattern.compile(fallbackRule);

        FastRegex.Pattern fallback = FastRegex.compile(fallbackRule);
        assertTrue(fallback.isFallback());
        assertFullJdkParity(jdk, fallback, URL_CORPUS);

        FastRegex.Pattern defaultCompiled = FastRegex.compile(fallbackRule);
        assertTrue(defaultCompiled.isFallback());
        assertFullJdkParity(jdk, defaultCompiled, URL_CORPUS);
    }

    /**
     * Simulates a production URL classifier reusing a single {@link FastRegex.ReusableMatchResult}
     * across all 10 compiled matchers over multiple warm-up passes, comparing every result to JDK.
     */
    @Test
    public void testSharedReusableResultAcrossEcommerceRuleChain()
    {
        Matcher[] jdkMatchers = new Matcher[ALL_RULES.length];
        FastRegex.Matcher[] fastMatchers = new FastRegex.Matcher[ALL_RULES.length];
        for (int i = 0; i < ALL_RULES.length; i++)
        {
            jdkMatchers[i] = Pattern.compile(ALL_RULES[i]).matcher("");
            fastMatchers[i] = FastRegex.compile(ALL_RULES[i]).matcher();
        }

        var sharedResult = new FastRegex.ReusableMatchResult(1);
        for (int pass = 0; pass < 5; pass++)
        {
            for (String url : URL_CORPUS)
            {
                for (int r = 0; r < ALL_RULES.length; r++)
                {
                    boolean jFound = jdkMatchers[r].reset(url).find();
                    boolean fFound = fastMatchers[r].find(url, sharedResult);
                    assertEquals("Pass " + pass + " rule " + r + " mismatch on " + url, jFound, fFound);
                    if (jFound)
                    {
                        assertMatchDetailsMatchJdk(jdkMatchers[r], sharedResult, ALL_RULES[r], url);
                    }
                }
            }
        }
    }

    /**
     * Verifies character-by-character parity for the {@code [/A-z-0-9]+} character class
     * and zero-width {@code findAll()} cursor progression on the Bath &amp; Body Works URL.
     */
    @Test
    public void testCharacterClassAndZeroWidthFindAllParityOnUrl()
    {
        Pattern jdkClass = Pattern.compile("[/A-z-0-9]+");
        FastRegex.Pattern fastClass = FastRegex.compileStrict("[/A-z-0-9]+");
        for (int ch = 0; ch < 256; ch++)
        {
            String single = String.valueOf((char) ch);
            assertFullJdkParity(jdkClass, fastClass, single);
        }
        assertFullJdkParity(jdkClass, fastClass, PRODUCT_SEARCH_URL);

        // Verify zero-width matches occurring well past cursor 0 (e.g. '$' at URL end and '(?=\?)' at query '?')
        assertFullJdkParity(Pattern.compile("$"), FastRegex.compileStrict("$"), PRODUCT_SEARCH_URL);
        assertFullJdkParity(Pattern.compile("(?=\\?)"), FastRegex.compile("(?=\\?)"), PRODUCT_SEARCH_URL);
    }

    /**
     * Differential parity tests for the production {@code ariat} SFCC rule
     * ({@code -Site/[^/]+/([^/\?]+).*}) and {@code .+} variant across String and StringBuilder URLs,
     * including queryless paths, multi-line log lines with {@code \n}, and false-prefix hits.
     */
    @Test
    public void testAriatSfccDifferentialParity()
    {
        String[] rules = {
                "-Site/[^/]+/([^/\\?]+).*",
                "-Site/[^/]+/([^/\\?]+).+"
        };
        String[] urls = {
                "https://loadtest.ariat.com/on/demandware.store/Sites-AriatUS-Site/en_US/Product-Show?pid=10040352&dwvar_10040352_color=BROWN&dwvar_10040352_width=D_Medium&cgid=mens- footwear-boots",
                "https://loadtest.ariat.com/on/demandware.store/Sites-AriatUS-Site/en_US/Cart-Show",
                "https://loadtest.ariat.com/on/demandware.store/Sites-AriatUS-Site/en_US/X?a=1\nhttps://loadtest.ariat.com/on/demandware.store/Sites-AriatUK-Site/en_GB/Home-Show?src=nav",
                "https://loadtest.ariat.com/on/demandware.store/Sites-AriatUS-Site//Product-Show?pid=1",
                "https://loadtest.ariat.com/on/demandware.store/Sites-AriatUS-Site/en_US/?pid=1",
                "https://loadtest.ariat.com/static/-Site/broken?x=1",
                "https://loadtest.ariat.com/static/assets/main.css"
        };

        for (String rule : rules)
        {
            Pattern jdk = Pattern.compile(rule);
            FastRegex.Pattern fast = FastRegex.compileStrict(rule);
            assertFalse(fast.isFallback());
            for (String url : urls)
            {
                assertFullJdkParity(jdk, fast, url, new StringBuilder(url));
            }
        }
    }

    /**
     * Differential parity tests for the 6-rule {@code aritzia} SCAPI chain, exercising
     * reluctant captures, shared {@code /v} prefixes, overlap terminal {@code ([^/]+)\?} loops,
     * single-character segments, empty segments, multiple {@code ?} in query strings, and sequential
     * rule-chain evaluation with {@code stopOnMatch = false} on rule 1.
     */
    @Test
    public void testAritziaScapiChainDifferentialParity()
    {
        String[] scapiRules = {
                "/scapi/([^/]+?)/([^/]+?)/v1",
                "/shopper-products/[^/]+/[^/]+/[^/]+/products/[^\\?]+",
                "/v[0-9]/[^/]+?/[^/]+?/([^/]+)\\?",
                "/v[0-9]/[^/]+?/[^/]+?/([^/]+)/[^/]+/([^/]+)\\?",
                "/v[0-9]/[^/]+?/[^/]+?/([^/]+)/[^/]+\\?",
                "/v[0-9]/[^/]+?/[^/]+?/([^/]+)/[^/]+/([^/]+)/[^/]+/([^/]+)\\?"
        };
        String[] urls = {
                "https://api.aritzia.com/mobify/proxy/api/scapi/shopper-products/v1/organizations/f_ecom_aritzia/products?ids=101234,101235,101236&locale=en-CA&siteId=Aritzia_CA",
                "https://api.aritzia.com/mobify/proxy/api/scapi/shopper-search/v1/organizations/f_ecom_aritzia/product-search?refine=cgid%3Dclothing-dresses&limit=24&offset=0&locale=en-CA",
                "https://api.aritzia.com/mobify/proxy/api/scapi/shopper-products/v1/organizations/f_ecom_aritzia/products/204881?locale=en-CA&siteId=Aritzia_CA",
                "https://api.aritzia.com/mobify/proxy/api/scapi/shopper-baskets/v1/organizations/f_ecom_aritzia/baskets/b123/items?siteId=Aritzia_CA",
                "https://api.aritzia.com/mobify/proxy/api/scapi/shopper-baskets/v1/organizations/f_ecom_aritzia/baskets/b123/shipments/me/shipping-methods?siteId=Aritzia_CA",
                "https://api.aritzia.com/mobify/proxy/api/scapi/shopper-customers/v1/organizations/f_ecom_aritzia/customers/c999?siteId=Aritzia_CA",
                // 1-char segment, empty segment, repeated '?' in query, and '/v' false prefixes
                "/v1/a/b/x?foo=1?bar=2",
                "/v1/a/b/?foo=1",
                "/velocity/v/v2/org/f_ecom/items/sub?a=1&b=2",
                "/scapi/a/b/v1/organizations/org/products/p1"
        };

        Pattern[] jdkPatterns = new Pattern[scapiRules.length];
        FastRegex.Pattern[] fastPatterns = new FastRegex.Pattern[scapiRules.length];
        Matcher[] jdkMatchers = new Matcher[scapiRules.length];
        FastRegex.Matcher[] fastMatchers = new FastRegex.Matcher[scapiRules.length];
        for (int i = 0; i < scapiRules.length; i++)
        {
            jdkPatterns[i] = Pattern.compile(scapiRules[i]);
            fastPatterns[i] = FastRegex.compileStrict(scapiRules[i]);
            assertFalse(fastPatterns[i].isFallback());
            jdkMatchers[i] = jdkPatterns[i].matcher("");
            fastMatchers[i] = fastPatterns[i].matcher();
            for (String url : urls)
            {
                assertFullJdkParity(jdkPatterns[i], fastPatterns[i], url, new StringBuilder(url));
            }
        }

        // Sequential rule chain check (rule 0 has stopOnMatch = false; rules 1..5 stop on first match)
        for (String url : urls)
        {
            boolean j0 = jdkMatchers[0].reset(url).find();
            boolean f0 = fastMatchers[0].reset(url).find();
            assertEquals(j0, f0);
            if (j0)
            {
                assertEquals(jdkMatchers[0].group(1), fastMatchers[0].groupView(1).toString());
                assertEquals(jdkMatchers[0].group(2), fastMatchers[0].groupView(2).toString());
            }
            for (int r = 1; r < scapiRules.length; r++)
            {
                boolean jr = jdkMatchers[r].reset(url).find();
                boolean fr = fastMatchers[r].reset(url).find();
                assertEquals(jr, fr);
                if (jr)
                {
                    for (int g = 1; g <= jdkMatchers[r].groupCount(); g++)
                    {
                        assertEquals(jdkMatchers[r].group(g), fastMatchers[r].groupView(g).toString());
                    }
                    break;
                }
            }
        }
    }

    /**
     * Differential parity tests for all 7 {@code bbw} production rules (including {@code $}-anchored
     * basket rule and 3-segment {@code shop/v\d\d_\d/...}) plus subtle leftmost-first / greedy-overlap
     * patterns.
     */
    @Test
    public void testBbwSevenRuleChainAndPriorityEdgeCases()
    {
        String[] bbwRules = {
                "shop/v\\d\\d_\\d/([^/\\?]+).*",
                "shop/v\\d\\d_\\d/[^/\\?]+/(auth)",
                "shop/v\\d\\d_\\d/baskets/[^/\\?]+$",
                "shop/v\\d\\d_\\d/[^/\\?]+/[^/\\?]+/([^/\\?]+).*",
                "shop/v\\d\\d_\\d/[^/\\?]+/[^/\\?]+/[^/\\?]+/[^/\\?]+/([^/\\?]+).*",
                "custom/[^/]+/[^/?]+/[^/\\?]+/[^/\\?]+/([^/\\?]+)",
                "(shopper-([^/]+))/[\\w/]+/(categories)(?=\\?|([/A-z-0-9]+)\\?)"
        };
        String[] extraBbwUrls = {
                BASE_HOST + "/s/BathAndBodyWorks/dw/shop/v24_5/baskets/b12345",
                BASE_HOST + "/s/BathAndBodyWorks/dw/shop/v24_5/baskets/b12345?locale=en_US",
                BASE_HOST + "/s/BathAndBodyWorks/dw/shop/v24_5/baskets/b12345/items?locale=en_US"
        };

        for (int r = 0; r < bbwRules.length; r++)
        {
            Pattern jdk = Pattern.compile(bbwRules[r]);
            FastRegex.Pattern fast = FastRegex.compile(bbwRules[r]);
            assertEquals(r == 6, fast.isFallback());
            assertFullJdkParity(jdk, fast, URL_CORPUS);
            assertFullJdkParity(jdk, fast, extraBbwUrls);
        }

        // Tricky leftmost-first optional/reluctant/overlap patterns
        String[][] trickyCases = {
                { "(?:a)?(?:ab)?", "ab", "b", "aab", "" },
                { "(a)?(ab)?", "ab", "b", "aab", "" },
                { "(a.?)+?b", "abb", "axb", "ab", "aaab" },
                { "a([^/]*)\\?", "a?", "ax?", "a/x?", "a?b?c" },
                { "([^/]+)\\?.*", "x?a=1&b=2\nnext", "abc?foo?bar", "/?" },
                { "([a-z]+).+", "abc", "ab", "a", "abc\ndef" }
        };
        for (String[] tc : trickyCases)
        {
            String pat = tc[0];
            Pattern jdk = Pattern.compile(pat);
            FastRegex.Pattern fast = FastRegex.compileStrict(pat);
            for (int i = 1; i < tc.length; i++)
            {
                assertFullJdkParity(jdk, fast, tc[i], new StringBuilder(tc[i]));
            }
        }
    }

    /**
     * Multi-threaded concurrent compilation stress test verifying that {@link FastRegex.Pattern}
     * compilation is lock-free, race-free, and deterministic across concurrent threads.
     */
    @Test
    public void testConcurrentMultiThreadedCompilation() throws Exception
    {
        String[] validStrict = {
                "-Site/[^/]+/([^/\\?]+).*",
                "/scapi/([^/]+?)/([^/]+?)/v1",
                "/v[0-9]/[^/]+?/[^/]+?/([^/]+)/[^/]+/([^/]+)\\?",
                "shop/v\\d\\d_\\d/baskets/[^/\\?]+$",
                "^(https?)://([^/:]+)(?::(\\d+))?(/.*)?$",
                "(?:foo|bar|baz)_[0-9]{2,5}_[a-z]+"
        };
        String[] validFallback = {
                "(shopper-([^/]+))/[\\w/]+/(categories)(?=\\?|([/A-z-0-9]+)\\?)",
                "\\bword\\b",
                "(?<=prefix_)\\d+"
        };
        String[] invalidSyntax = {
                "unclosed([a-z]+",
                "[z-a]",
                "*dangling"
        };
        String sampleUrl =
                "https://loadtest.ariat.com/on/demandware.store/Sites-AriatUS-Site/en_US/Product-Show?pid=10040352";

        int threadCount = 12;
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try
        {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (int t = 0; t < threadCount; t++)
            {
                final int tid = t;
                tasks.add(() ->
                {
                    barrier.await();
                    for (int iter = 0; iter < 40; iter++)
                    {
                        String sPat = validStrict[(tid + iter) % validStrict.length];
                        FastRegex.Pattern fr = FastRegex.compileStrict(sPat);
                        Pattern jr = Pattern.compile(sPat);
                        assertFalse(fr.isFallback());
                        assertEquals(jr.matcher(sampleUrl).find(), fr.matcher(sampleUrl).find());

                        String fbPat = validFallback[(tid + iter) % validFallback.length];
                        FastRegex.Pattern fb = (iter % 2 == 0)
                                ? FastRegex.compile(fbPat)
                                : FastRegex.compile(fbPat);
                        assertTrue(fb.isFallback());
                        assertEquals(Pattern.compile(fbPat).matcher(sampleUrl).find(), fb.matcher(sampleUrl).find());

                        String bad = invalidSyntax[(tid + iter) % invalidSyntax.length];
                        assertThrows(PatternSyntaxException.class, () -> FastRegex.compile(bad));
                    }
                    return null;
                });
            }

            List<Future<Void>> futures = pool.invokeAll(tasks);
            for (Future<Void> f : futures)
            {
                f.get();
            }
        }
        finally
        {
            pool.shutdownNow();
        }
    }
}
