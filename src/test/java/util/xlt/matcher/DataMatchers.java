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
package util.xlt.matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.TimerData;
import com.xceptance.xlt.api.engine.TransactionData;

/**
 * A collection of methods that create {@linkplain Matcher hamcrest matchers} for {@linkplain Data data records}
 */
public final class DataMatchers
{
    private DataMatchers() {}

    public static <D extends Data> Matcher<D> hasName(String expectedName)
    {
        return hasAttributeEqualTo(expectedName, D::getName, "name");
    }

    public static <D extends Data> Matcher<D> hasTime(Long expectedTime)
    {
        return hasAttributeEqualTo(expectedTime, D::getTime, "time");
    }

    public static <D extends Data> Matcher<D> hasAgentName(Long expectedAgentName)
    {
        return hasAttributeEqualTo(expectedAgentName, D::getAgentName, "agentName");
    }

    public static <D extends Data> Matcher<D> hasTransactionName(Long expectedTransactionName)
    {
        return hasAttributeEqualTo(expectedTransactionName, D::getTransactionName, "transactionName");
    }

    public static Matcher<EventData> hasMessage(String expectedMessage)
    {
        return hasAttributeEqualTo(expectedMessage, EventData::getMessage, "message");
    }

    public static Matcher<EventData> hasTestCaseName(String expectedTestCaseName)
    {
        return hasAttributeEqualTo(expectedTestCaseName, EventData::getTestCaseName, "testCaseName");
    }

    public static <D extends TimerData> Matcher<D> hasRunTime(Long expectedRunTime)
    {
        return hasAttributeEqualTo(expectedRunTime, D::getRunTime, "runTime");
    }

    public static <D extends TimerData> Matcher<D> hasFailed(Boolean failed)
    {
        return hasAttributeEqualTo(failed, D::hasFailed, "failed");
    }

    public static Matcher<RequestData> hasUrl(String url)
    {
        return hasAttributeEqualTo(url, d -> d.getUrl().toString(), "url");
    }

    public static Matcher<TransactionData> hasFailedActionName(String expectedFailedActionName)
    {
        return hasAttributeEqualTo(expectedFailedActionName, TransactionData::getFailedActionName, "failedActionName");
    }

    public static Matcher<TransactionData> hasFailureStackTrace(String expectedStackTrace)
    {
        return hasAttributeEqualTo(expectedStackTrace, TransactionData::getFailureStackTrace, "stackTrace");
    }

    /**
     * Returns a {@link Matcher} for {@link TransactionData} that check whether the
     * {@linkplain TransactionData#getFailureStackTrace() failure stackstrace attribute} matches a regex.<br>
     * Since the failure stacktrace attribute tends to contain backslashes (instead of newlines), and backslashes are
     * notoriously hard to cope with in java regex strings, this method allows to use
     * {@link #EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES} in the regex argument and will replace occurrences of this string
     * with a suitable expression in the regex before applying it.
     *
     * @param expectedStackTraceRegex
     * @return
     */
    public static Matcher<TransactionData> hasFailureStackTraceMatching(String expectedStackTraceRegex)
    {
        final String resolvedRegex = expectedStackTraceRegex.replace(EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES, "(?:\\r|\\n|\\r\\n)");
        return hasAttributeMatchingRegex(resolvedRegex, TransactionData::getFailureStackTrace, "stackTrace");
    }

    /**
     * A placeholder that can be used in a regex passed to {@link #hasFailureStackTraceMatching(String)} wherever an end
     * of line is to be matched. The placeholder will be replaced with a suitable expression in the regex upon invoking
     * that method.
     */
    public static final String EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES = "<EOL>";

    public static <D> Matcher<D> hasAttributeEqualTo(Object expectedValue, Function<? super D, ?> getter, final String attributeName)
    {
        return hasAttributeFulfilling(a -> Objects.equals(a, expectedValue), getter, attributeName,
                                      attributeName + " = <" + String.valueOf(expectedValue) + ">");
    }

    public static <D> Matcher<D> hasAttributeMatchingRegex(String regexForExpectedValue, Function<? super D, ?> getter,
                                                           final String attributeName)
    {
        return hasAttributeFulfilling(a -> isMatching(a, regexForExpectedValue), getter, attributeName,
                                      attributeName + " ~= /" + regexForExpectedValue + "/");
    }

    private static boolean isMatching(Object a, String regexForExpectedValue)
    {
        return a != null && RegExUtils.isMatching(String.valueOf(a), regexForExpectedValue);
    }

    public static <D, V> Matcher<D> hasAttributeFulfilling(Predicate<? super V> expectedCondition, Function<? super D, V> getter,
                                                           final String attributeName, String descriptionOfExpectedCondition)
    {
        return new TypeSafeDiagnosingMatcher<D>()
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText(descriptionOfExpectedCondition);
            }

            @Override
            protected boolean matchesSafely(D item, Description mismatchDescription)
            {
                final V actualValue = getter.apply(item);

                if (!expectedCondition.test(actualValue))
                {
                    mismatchDescription.appendText(attributeName).appendText(" was ").appendValue(actualValue);
                    return false;
                }

                return true;
            }
        };
    }

    /**
     * Creates a {@link Matcher} that returns {@code true} iff the argument's class equals the specified expected class
     * <b>and</b> all specified additional {@linkplain Matcher}s (if any) are matched by the argument
     *
     * @param expectedClass
     * @param additionalMatchers
     * @return a {@link Matcher}
     */
    private static <D> Matcher<D> has(final Class<D> expectedClass, final Collection<Matcher<? super D>> additionalMatchers)
    {
        return new BaseMatcher<D>()
        {
            @Override
            public void describeTo(Description description)
            {
                description.appendText(expectedClass.getSimpleName());
                String matcherDescriptions = additionalMatchers.stream().map(Object::toString).collect(Collectors.joining(", "));
                if (!matcherDescriptions.isEmpty())
                {
                    description.appendText(" [" + matcherDescriptions + "]");
                }
            }

            @Override
            public boolean matches(Object item)
            {
                if (item == null || item.getClass() != expectedClass)
                {
                    return false;
                }

                for (Matcher<?> matcher : additionalMatchers)
                {
                    if (!matcher.matches(item))
                    {
                        return false;
                    }
                }
                return true;
            }

        };
    }

    /**
     * Creates a {@link Matcher} that returns {@code true} iff the argument's class equals the specified expected class
     * <b>and</b> all specified additional {@linkplain Matcher}s (if any) are matched by the argument
     *
     * @param expectedClass
     * @param additionalMatchers
     * @return a {@link Matcher}
     */
    @SafeVarargs
    public static <D> Matcher<D> has(final Class<D> expectedClass, final Matcher<? super D>... matchers)
    {
        return has(expectedClass, Arrays.asList(matchers));
    }

    public static class DataRecordExpectation<D extends Data>
    {
        public final int expectedCount;

        public final Class<D> expectedClassOfDataRecord;

        public final Collection<Matcher<? super D>> additionalMatchers;

        private DataRecordExpectation(int expectedCount, Class<D> expectedClassOfDataRecord,
                                      Collection<Matcher<? super D>> additionalMatchers)
        {
            super();
            this.expectedCount = expectedCount;
            this.expectedClassOfDataRecord = expectedClassOfDataRecord;
            this.additionalMatchers = new ArrayList<>(additionalMatchers);
        }

    }

    @SafeVarargs
    public static <D extends Data> DataRecordExpectation<D> expect(final int expectedCount, final Class<D> expectedClassOfDataRecord,
                                                                   final Matcher<? super D>... additionalMatchers)
    {
        return new DataRecordExpectation<D>(expectedCount, expectedClassOfDataRecord, Arrays.asList(additionalMatchers));
    }

    @SafeVarargs
    public static <D extends Data> DataRecordExpectation<D> expect(final Class<D> expectedClass, final Matcher<? super D>... matchers)
    {
        return expect(1, expectedClass, matchers);
    }

    public static <D extends Data> Matcher<D> meets(DataRecordExpectation<D> expectation)
    {
        return has(expectation.expectedClassOfDataRecord, expectation.additionalMatchers);
    }

    public static Matcher<Iterable<?>> hasInstanceCounts(Map<Class<?>, Integer> expectedCountsPerClass)
    {
        return new BaseMatcher<Iterable<?>>()
        {
            private int[] actualCounts = null;

            @Override
            public void describeTo(Description description)
            {
                for (Entry<Class<?>, Integer> entry : expectedCountsPerClass.entrySet())
                {
                    description.appendText(entry.getValue() + " occurrences of " + entry.getKey().getSimpleName() + "; ");
                }
            }

            @Override
            public boolean matches(Object item)
            {
                if (item instanceof Iterable == false)
                {
                    return false;
                }

                actualCounts = countInstancesOf(expectedCountsPerClass.keySet(), (Iterable<?>) item);

                Iterator<Integer> expectedCounts = expectedCountsPerClass.values().iterator();
                for (int actualCount : actualCounts)
                {
                    if (actualCount != expectedCounts.next())
                    {
                        return false;
                    }
                }

                return true;
            }

            private int[] countInstancesOf(Collection<Class<?>> classes, Iterable<?> collection)
            {
                int[] counts = new int[classes.size()];
                for (Object object : collection)
                {
                    int i = 0;
                    for (Class<?> klass : classes)
                    {
                        if (klass.isInstance(object))
                        {
                            ++counts[i];
                        }
                        ++i;
                    }
                }
                return counts;
            }

        };
    }
}
