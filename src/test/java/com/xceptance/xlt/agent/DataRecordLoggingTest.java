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
package com.xceptance.xlt.agent;

import static org.mockito.Mockito.times;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static util.xlt.matcher.DataMatchers.EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES;
import static util.xlt.matcher.DataMatchers.expect;
import static util.xlt.matcher.DataMatchers.has;
import static util.xlt.matcher.DataMatchers.hasFailed;
import static util.xlt.matcher.DataMatchers.hasFailedActionName;
import static util.xlt.matcher.DataMatchers.hasFailureStackTrace;
import static util.xlt.matcher.DataMatchers.hasFailureStackTraceMatching;
import static util.xlt.matcher.DataMatchers.hasInstanceCounts;
import static util.xlt.matcher.DataMatchers.hasMessage;
import static util.xlt.matcher.DataMatchers.hasName;
import static util.xlt.matcher.DataMatchers.hasTestCaseName;
import static util.xlt.matcher.DataMatchers.hasTime;
import static util.xlt.matcher.DataMatchers.hasUrl;
import static util.xlt.matcher.DataMatchers.meets;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestName;
import org.junit.runner.Request;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.exceptions.verification.junit.ArgumentsAreDifferent;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.reflect.Whitebox;

import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.xlt.agentcontroller.TestUserConfiguration;
import com.xceptance.xlt.api.engine.ActionData;
import com.xceptance.xlt.api.engine.Data;
import com.xceptance.xlt.api.engine.DataManager;
import com.xceptance.xlt.api.engine.EventData;
import com.xceptance.xlt.api.engine.GlobalClock;
import com.xceptance.xlt.api.engine.RequestData;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.TransactionData;
import com.xceptance.xlt.api.tests.AbstractTestCase;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.engine.DataManagerImpl;
import com.xceptance.xlt.engine.SessionImpl;
import com.xceptance.xlt.engine.XltWebClient;

import util.xlt.IntentionalError;
import util.xlt.actions.TestAction;
import util.xlt.actions.TestHtmlPageAction;
import util.xlt.matcher.DataMatchers.DataRecordExpectation;
import util.xlt.properties.AdjustXltProperties;
import util.xlt.properties.AdjustXltProperties.SetProperty;

/**
 * Integration tests for the {@linkplain DataManager#logDataRecord(Data) logging of data records} during load test
 * execution
 *
 * @author Deniz Altin
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest(
    {
        SessionImpl.class, DataManagerImpl.class, GlobalClock.class, AbstractExecutionTimer.class
})
@PowerMockIgnore({"javax.*", "org.xml.*", "org.w3c.dom.*"})
public class DataRecordLoggingTest
{
    /**
     * The {@link XltProperties} key to enable offline mode in {@link XltWebClient}
     */
    public static final String XLT_OFFLINE_MODE = "com.xceptance.xlt.http.offline";

    /**
     * This {@link MethodRule} will take care of changing XLT properties for test methods annotated with
     * {@link AdjustXltProperties}
     */
    @Rule
    public final MethodRule adjustXltPropertiesRule = new AdjustXltProperties.MethodRule();

    /**
     * Whether the load test class should be derived from {@link AbstractTestCase} or not
     */
    @Parameter(0)
    public KindOfLoadTestClass kindOfLoadTestClass;

    /**
     * What kind of thread should be used to execute the load test (e.g. {@link LoadTestRunner})
     */
    @Parameter(1)
    public TestExecutionThreadStrategy testExecutionThreadStrategy;

    @Parameters(name = "{0}/{1}")
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]
            {
                {
                    KindOfLoadTestClass.XltDerived, TestExecutionThreadStrategy.LoadTestRunner
                },
                {
                    KindOfLoadTestClass.NotDerived, TestExecutionThreadStrategy.LoadTestRunner
                },
                {
                    KindOfLoadTestClass.XltDerived, TestExecutionThreadStrategy.JUnitClassRequestRunner
                },
                {
                    KindOfLoadTestClass.NotDerived, TestExecutionThreadStrategy.JUnitClassRequestRunner
                },
            });
    }

    @Before
    public void initMocks() throws Exception
    {
        mockDataManagerCreation();
    }

    @After
    public void clear()
    {
        dataRecordCaptors.clear();
        mockDataManagers.clear();
    }

    @Test
    @AdjustXltProperties(
        {
            @SetProperty(key = XLT_OFFLINE_MODE, value = "true"),
            @SetProperty(key = "com.xceptance.xlt.socket.collectNetworkData", value = "false")
    })
    public void test_HtmlPageActionsAndEvents() throws Exception
    {
        final String url1 = "http://localhost:8081/";
        final String url2 = "https://localhost:8082/";

        final Thread testExecutionThread = createLoadTestExecutionThread(new LoadTestImplementation()
        {
            @Override
            public void test() throws Throwable
            {
                new TestHtmlPageAction("Action1", new URL(url1)).run();

                Session.logEvent("Event 1", "Message 1");

                new TestHtmlPageAction("Action2", new URL(url2))
                {
                    @Override
                    protected void postValidate() throws Exception
                    {
                        Session.logEvent("Event 2", "Message 2");
                    }
                }.run();

                Session.logEvent("Event 3", "Message 3");
            }
        });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();

        startAndWaitFor(testExecutionThread);

        verifyDataRecordsLoggedWith(mockDataManagerFor(threadGroup),
                                    expect(RequestData.class, hasName("Action1.1"), hasFailed(false), hasUrl(url1)),
                                    expect(ActionData.class, hasName("Action1"), hasFailed(false)),
                                    expect(EventData.class, hasName("Event 1"), hasMessage("Message 1"),
                                           hasTestCaseName(expectedUserName())),
                                    expect(RequestData.class, hasName("Action2.1"), hasFailed(false), hasUrl(url2)),
                                    expect(EventData.class, hasName("Event 2"), hasMessage("Message 2"),
                                           hasTestCaseName(expectedUserName())),
                                    expect(ActionData.class, hasName("Action2"), hasFailed(false)),
                                    expect(EventData.class, hasName("Event 3"), hasMessage("Message 3"),
                                           hasTestCaseName(expectedUserName())),
                                    expect(TransactionData.class, hasName(expectedUserName()), hasFailed(false), hasFailedActionName(null),
                                           hasFailureStackTrace(null)));
    }

    @Test
    public void test_CaughtAndUncaughtActionErrors() throws Exception
    {
        final Thread testExecutionThread = createLoadTestExecutionThread(new LoadTestImplementation()
        {
            @Override
            public void test() throws Throwable
            {
                try
                {
                    new FailedAction("FailedAction-Caught").run();
                }
                catch (IntentionalError e)
                {
                }

                new FailedAction_postValidate("FailedAction-Uncaught").run();
            }
        });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();

        startAndWaitFor(testExecutionThread);

        verifyDataRecordsLoggedWith(mockDataManagerFor(threadGroup),
                                    expect(ActionData.class, hasName("FailedAction-Caught"), hasFailed(true)),
                                    expect(ActionData.class, hasName("FailedAction-Uncaught"), hasFailed(true)),
                                    // failedActionName is only set once
                                    expect(TransactionData.class, hasFailed(true), hasFailedActionName("FailedAction-Caught")));
    }

    @Test
    public void test_ActionsExecutedOrNot_DependingOn_preValidateSafe() throws Exception
    {
        final Thread testExecutionThread = createLoadTestExecutionThread(new LoadTestImplementation()
        {
            @Override
            public void test() throws Throwable
            {
                new TestAction("FirstAction").runIfPossible();

                // the following action will not actually be run, because preValidateSafe will return false
                new FailedAction_preValidate("NotApplicableAction").runIfPossible();

                new TestAction("LastAction").runIfPossible();
            }
        });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();

        startAndWaitFor(testExecutionThread);

        verifyDataRecordsLoggedWith(mockDataManagerFor(threadGroup), expect(1, ActionData.class, hasName("FirstAction"), hasFailed(false)),
                                    expect(0, ActionData.class, hasName("NotApplicableAction")),
                                    expect(1, ActionData.class, hasName("LastAction"), hasFailed(false)),
                                    expect(1, TransactionData.class, hasFailed(false), hasFailedActionName(null),
                                           hasFailureStackTrace(null)));
    }

    @Test
    public void test_CaughtError_IntermediateAction() throws Exception
    {
        final Thread testExecutionThread = createLoadTestExecutionThread(new LoadTestImplementation()
        {
            @Override
            public void test() throws Throwable
            {
                try
                {
                    new FailedAction("FailedAction-Caught").run();
                }
                catch (IntentionalError e)
                {
                    new TestAction("LastAction").run();
                }
            }
        });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();

        startAndWaitFor(testExecutionThread);

        verifyDataRecordsLoggedWith(mockDataManagerFor(threadGroup),
                                    expect(ActionData.class, hasName("FailedAction-Caught"), hasFailed(true)),
                                    expect(ActionData.class, hasName("LastAction"), hasFailed(false)),
                                    expect(TransactionData.class, hasFailed(false), hasFailureStackTrace(null),
                                           hasFailedActionName("FailedAction-Caught")));
    }

    @Test
    public void test_CaughtError_LastAction() throws Exception
    {
        final Thread testExecutionThread = createLoadTestExecutionThread(new LoadTestImplementation()
        {
            @Override
            public void test() throws Throwable
            {
                try
                {
                    new FailedAction("FailedAction-Caught").run();
                }
                catch (IntentionalError e)
                {
                }
            }
        });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();

        startAndWaitFor(testExecutionThread);

        verifyDataRecordsLoggedWith(mockDataManagerFor(threadGroup),
                                    expect(ActionData.class, hasName("FailedAction-Caught"), hasFailed(true)),
                                    expect(TransactionData.class, hasFailed(false), hasFailureStackTrace(null),
                                           hasFailedActionName("FailedAction-Caught")));
    }

    @Test
    public void test_ErrorBeforeActions() throws Exception
    {
        final Thread testExecutionThread = createLoadTestExecutionThread(new LoadTestImplementation()
        {
            @Override
            public void test() throws Throwable
            {
                throwIntentionalError();
                new TestAction("FirstAction").run();
            }
        });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();

        startAndWaitFor(testExecutionThread);

        final DataManagerImpl instance = mockDataManagerFor(threadGroup);
        if (kindOfLoadTestClass.isXltDerived() || testExecutionThreadStrategy.usesLoadTestRunner)
        {
            verifyDataRecordsLoggedWith(instance, expect(0, ActionData.class),
                                        expect(TransactionData.class, hasName(expectedUserName()), hasFailed(true), hasFailedActionName(""),
                                               hasFailureStackTraceMatching(expectedFailureStacktraceRegex(STACKTRACE_REGEX_FOR_THROW_INTENTIONAL_ERROR,
                                                                                                           defaultUserId()))));
        }
        else
        {
            Assert.assertNull("No action -> no session -> no data manager", instance);
        }
    }

    @Test
    public void test_ErrorBetweenActions() throws Exception
    {
        final Thread testExecutionThread = createLoadTestExecutionThread(new LoadTestImplementation()
        {
            @Override
            public void test() throws Throwable
            {
                new TestAction("FirstAction").run();
                throwIntentionalError();
                new TestAction("LastAction").run();
            }
        });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();

        startAndWaitFor(testExecutionThread);
        final DataManagerImpl instance = mockDataManagerFor(threadGroup);

        verifyDataRecordsLoggedWith(instance, expect(1, ActionData.class, hasName("FirstAction"), hasFailed(false)),
                                    expect(TransactionData.class, hasName(expectedUserName()), hasFailed(true), hasFailedActionName(""),
                                           hasFailureStackTraceMatching(expectedFailureStacktraceRegex(STACKTRACE_REGEX_FOR_THROW_INTENTIONAL_ERROR,
                                                                                                       defaultUserId()))));
    }

    private String expectedFailureStacktraceRegex(String beginningOfStacktraceRegex, String userId)
    {
        return beginningOfStacktraceRegex + ".*" + STACKTRACE_REGEX_FOR_AT +
               kindOfLoadTestClass.getGenericLoadTestClassObject().getName().replace(".", "\\.").replace("$", "\\$") +
               "\\.test\\([^)]*\\)" + EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES + "\\t...$";
    }

    @Test
    public void test_ErrorDuringAction_preValidate() throws Exception
    {
        final Thread testExecutionThread = createLoadTestExecutionThread(new LoadTestImplementation()
        {
            @Override
            public void test() throws Throwable
            {
                new TestAction("FirstAction").run();
                new FailedAction_preValidate("FailedAction-preValidate").run();
                new TestAction("LastAction").run();
            }
        });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();

        startAndWaitFor(testExecutionThread);

        verifyDataRecordsLoggedWith(mockDataManagerFor(threadGroup), expect(ActionData.class, hasName("FirstAction"), hasFailed(false)),
                                    expect(ActionData.class, hasName("FailedAction-preValidate"), hasFailed(true)),
                                    expect(TransactionData.class, hasName(expectedUserName()), hasFailed(true),
                                           hasFailedActionName("FailedAction-preValidate"),
                                           hasFailureStackTraceMatching(expectedFailureStacktraceRegex(FailedAction_preValidate.STACKTRACE_REGEX,
                                                                                                       defaultUserId()))));
    }

    @Test
    public void test_ErrorDuringAction_execute() throws Exception
    {
        final Thread testExecutionThread = createLoadTestExecutionThread(new LoadTestImplementation()
        {
            @Override
            public void test() throws Throwable
            {
                new TestAction("FirstAction").run();
                new FailedAction("FailedAction-execute").run();
                new TestAction("LastAction").run();
            }
        });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();

        startAndWaitFor(testExecutionThread);

        verifyDataRecordsLoggedWith(mockDataManagerFor(threadGroup), expect(ActionData.class, hasName("FirstAction"), hasFailed(false)),
                                    expect(ActionData.class, hasName("FailedAction-execute"), hasFailed(true)),
                                    expect(TransactionData.class, hasName(expectedUserName()), hasFailed(true),
                                           hasFailedActionName("FailedAction-execute"),
                                           hasFailureStackTraceMatching(expectedFailureStacktraceRegex(FailedAction.STACKTRACE_REGEX,
                                                                                                       defaultUserId()))));
    }

    @Test
    public void test_ErrorDuringAction_postValidate() throws Exception
    {
        final Thread testExecutionThread = createLoadTestExecutionThread(new LoadTestImplementation()
        {
            @Override
            public void test() throws Throwable
            {
                new TestAction("FirstAction").run();
                new FailedAction_postValidate("FailedAction-postValidate").run();
                new TestAction("LastAction").run();
            }
        });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();

        startAndWaitFor(testExecutionThread);

        verifyDataRecordsLoggedWith(mockDataManagerFor(threadGroup), expect(ActionData.class, hasName("FirstAction"), hasFailed(false)),
                                    expect(ActionData.class, hasName("FailedAction-postValidate"), hasFailed(true)),
                                    expect(TransactionData.class, hasName(expectedUserName()), hasFailed(true),
                                           hasFailedActionName("FailedAction-postValidate"),
                                           hasFailureStackTraceMatching(expectedFailureStacktraceRegex(FailedAction_postValidate.STACKTRACE_REGEX,
                                                                                                       defaultUserId()))));
    }

    private String defaultUserId()
    {
        return String.valueOf(expectedUserName()) + "-0";
    }

    @Test
    public void test_LoggingPeriod_SetByLoadTestRunner_BeforeExecutingLoadTest() throws Exception
    {
        Assume.assumeTrue(testExecutionThreadStrategy.usesLoadTestRunner());

        final long startTime = 314159265;
        final int initialDelay = 1234;
        final int warmUpPeriod = 567890;
        final int measurementPeriod = 987654;
        final long eventTime = startTime + initialDelay + warmUpPeriod + measurementPeriod / 2;
        final String eventName = "Event in beforeClass";

        final TestUserConfiguration testUserConfiguration = minimumTestUserConfigurationFor(kindOfLoadTestClass.getGenericLoadTestClassObject(),
                                                                                            this);
        testUserConfiguration.setInitialDelay(initialDelay);
        testUserConfiguration.setWarmUpPeriod(warmUpPeriod);
        testUserConfiguration.setMeasurementPeriod(measurementPeriod);

        final Thread testExecutionThread = createLoadTestExecutionThread(testUserConfiguration, defaultAgentInfo(),
                                                                         new LoadTestImplementation()
                                                                         {
                                                                             @Override
                                                                             public void beforeClass()
                                                                             {
                                                                                 GlobalClock.installFixed(eventTime);
                                                                                 Session.logEvent(eventName, "...");
                                                                                 GlobalClock.installFixed(eventTime + 100);
                                                                             }

                                                                             @Override
                                                                             public void test() throws Throwable
                                                                             {
                                                                                 GlobalClock.installFixed(eventTime + 1000);
                                                                                 Session.logEvent("Event 1", "Message 1");
                                                                             }
                                                                         });

        final ThreadGroup threadGroup = testExecutionThread.getThreadGroup();
        GlobalClock.installFixed(startTime);

        startAndWaitFor(testExecutionThread);

        final DataManagerImpl dataManager = mockDataManagerFor(threadGroup);
        final InOrder inOrder = Mockito.inOrder(dataManager);
        inOrder.verify(dataManager).setStartOfLoggingPeriod(startTime + initialDelay + warmUpPeriod);
        inOrder.verify(dataManager).setEndOfLoggingPeriod(startTime + initialDelay + warmUpPeriod + measurementPeriod);
        inOrder.verify(dataManager).logDataRecord(argThat(has(EventData.class, hasTime(eventTime), hasName(eventName))));
    }

    static final Pattern EOL_PLACEHOLDER_PATTERN = Pattern.compile(EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES);

    /**
     * A {@link TestAction} that will throw an {@link IntentionalError} during {@link #execute()}
     */
    public static class FailedAction extends TestAction
    {
        private final Long timeOfFailure;

        public FailedAction(String timerName)
        {
            this(timerName, null);
        }

        public FailedAction(String timerName, Long timeOfFailure)
        {
            super(timerName);
            this.timeOfFailure = timeOfFailure;
        }

        @Override
        public void execute() throws Exception
        {
            throwIntentionalError(timeOfFailure);
        }

        public static String STACKTRACE_REGEX = STACKTRACE_REGEX_FOR_THROW_INTENTIONAL_ERROR + STACKTRACE_REGEX_FOR_AT +
                                                FailedAction.class.getName().replace(".", "\\.").replace("$", "\\$") +
                                                "\\.execute\\([^)]*\\)" + EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES;
    }

    /**
     * A {@link TestAction} that will throw an {@link IntentionalError} during {@link #preValidate()}
     */
    public static final class FailedAction_preValidate extends TestAction
    {
        private final Long timeOfFailure;

        public FailedAction_preValidate(String timerName)
        {
            this(timerName, null);
        }

        public FailedAction_preValidate(String timerName, Long timeOfFailure)
        {
            super(timerName);
            this.timeOfFailure = timeOfFailure;
        }

        @Override
        public void preValidate() throws Exception
        {
            throwIntentionalError(timeOfFailure);
        }

        public static String STACKTRACE_REGEX = STACKTRACE_REGEX_FOR_THROW_INTENTIONAL_ERROR + STACKTRACE_REGEX_FOR_AT +
                                                FailedAction_preValidate.class.getName().replace(".", "\\.").replace("$", "\\$") +
                                                "\\.preValidate\\([^)]*\\)" + EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES;
    }

    /**
     * A {@link TestAction} that will throw an {@link IntentionalError} during {@link #postValidate()}
     */
    public static final class FailedAction_postValidate extends TestAction
    {
        private final Long timeOfFailure;

        public FailedAction_postValidate(String timerName)
        {
            this(timerName, null);
        }

        public FailedAction_postValidate(String timerName, Long timeOfFailure)
        {
            super(timerName);
            this.timeOfFailure = timeOfFailure;
        }

        @Override
        public void postValidate() throws Exception
        {
            throwIntentionalError(timeOfFailure);
        }

        public static String STACKTRACE_REGEX = STACKTRACE_REGEX_FOR_THROW_INTENTIONAL_ERROR + STACKTRACE_REGEX_FOR_AT +
                                                FailedAction_postValidate.class.getName().replace(".", "\\.").replace("$", "\\$") +
                                                "\\.postValidate\\([^)]*\\)" + EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES;
    }

    private static final String STACKTRACE_REGEX_FOR_AT = "\\tat ";

    private static final String STACKTRACE_REGEX_FOR_THROW_INTENTIONAL_ERROR = "^" + IntentionalError.class.getName().replace("$", "\\$") +
                                                                               ": " + IntentionalError.DEFAULT_MESSAGE +
                                                                               EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES +
                                                                               STACKTRACE_REGEX_FOR_AT +
                                                                               DataRecordLoggingTest.class.getName().replace(".", "\\.") +
                                                                               "\\." + "throwIntentionalError\\([^)]*\\)" +
                                                                               EOL_PLACEHOLDER_IN_STACKTRACE_REGEXES;

    public static void throwIntentionalError()
    {
        throw new IntentionalError();
    }

    public static void throwIntentionalError(Long timeOfFailure)
    {
        if (timeOfFailure != null)
        {
            GlobalClock.installFixed(timeOfFailure);
        }
        throw new IntentionalError();
    }

    private Map<ThreadGroup, DataManagerImpl> mockDataManagers = createThreadSafeWeakHashMap();

    private Map<DataManagerImpl, ArgumentCaptor<Data>> dataRecordCaptors = createThreadSafeWeakHashMap();

    /**
     * Make it so that SessionImpl() will use a mock {@link DataManagerImpl} instead of creating a new one. The mock
     * object can be accessed using {@link #mockDataManagerFor(Thread)}
     *
     * @throws Exception
     */
    private void mockDataManagerCreation() throws Exception
    {
        PowerMockito.whenNew(DataManagerImpl.class).withAnyArguments().thenAnswer(new Answer<DataManagerImpl>()
        {
            @Override
            public DataManagerImpl answer(InvocationOnMock invocation) throws Throwable
            {
                // limit to constructor new DataManagerImpl(Session) and avoid using (Session, Metrics)
                final DataManagerImpl instance = Whitebox.invokeConstructor(DataManagerImpl.class, invocation.getArguments()[0]);
                return mockDataManagers.computeIfAbsent(Thread.currentThread().getThreadGroup(), __ -> createMockDataManager(instance));
            }
        });
    }

    /**
     * Returns the mock {@link DataManagerImpl} that will be used by the {@link SessionImpl} object for the specified
     * thread group.
     * <p>
     * <b>ATTENTION:</b> If using this in a test, it needs to be called <i>before</i> the thread has finished
     *
     * @param thread
     * @param session
     * @return mock {@link DataManagerImpl} object used by {@link SessionImpl} for the specified thread
     * @see #mockDataManagerCreation()
     */
    private DataManagerImpl mockDataManagerFor(ThreadGroup threadGroup)
    {
        return mockDataManagers.get(threadGroup);
    }

    private DataManagerImpl createMockDataManager(final DataManagerImpl instance)
    {
        final ArgumentCaptor<Data> dataRecordCaptor = ArgumentCaptor.forClass(Data.class);
        final DataManagerImpl mock = Mockito.spy(instance);

        Mockito.doNothing().when(mock).logDataRecord(dataRecordCaptor.capture());

        // We want to see the logging of EventData records, so we'll have to let logEvent do its job
        Mockito.doCallRealMethod().when(mock).logEvent(Mockito.any(), Mockito.any());

        dataRecordCaptors.put(mock, dataRecordCaptor);

        return mock;
    }

    public List<Data> getDataRecordsCapturedFor(DataManager mockDataManager)
    {
        return dataRecordCaptors.get(mockDataManager).getAllValues();
    }

    /**
     * Creates a mock {@linkplain AbstractExecutionTimer execution timer} that does nothing.
     */
    private static AbstractExecutionTimer dummyExecutionTimer()
    {
        return PowerMockito.mock(AbstractExecutionTimer.class);
    }

    private static final String DEFAULT_AGENT_ID = "agentID";

    private static void startAndWaitFor(Thread... threads) throws InterruptedException
    {
        for (Thread thread : threads)
        {
            thread.start();
        }

        for (Thread thread : threads)
        {
            thread.join();
        }
    }

    @SuppressWarnings("serial")
    static class CountingMap<Key> extends LinkedHashMap<Key, Integer>
    {
        public void increaseValueFor(final Key key, final int increaseBy)
        {
            super.compute(key, (__, value) -> value == null ? increaseBy : (value + increaseBy));
        }
    }

    @SafeVarargs
    final void verifyDataRecordsLoggedWith(DataManager mockDataManager, DataRecordExpectation<? extends Data>... expectations)
    {
        final List<Data> capturedDataRecords = getDataRecordsCapturedFor(mockDataManager);
        final InOrder inOrder = Mockito.inOrder(mockDataManager);
        final CountingMap<Class<?>> expectedClassCounts = new CountingMap<Class<?>>();
        try
        {
            for (DataRecordExpectation<? extends Data> expectation : expectations)
            {
                // skip verification of transaction data expectation when neither load test runner nor XLT derived
                if (TransactionData.class.equals(expectation.expectedClassOfDataRecord))
                {
                    if (!kindOfLoadTestClass.isXltDerived() && !testExecutionThreadStrategy.usesLoadTestRunner)
                    {
                        continue;
                    }
                }
                inOrder.verify(mockDataManager, times(expectation.expectedCount)).logDataRecord(argThat(meets(expectation)));
                expectedClassCounts.increaseValueFor(expectation.expectedClassOfDataRecord, expectation.expectedCount);
            }

            MatcherAssert.assertThat(capturedDataRecords, hasInstanceCounts(expectedClassCounts));
        }
        catch (AssertionError e)
        {
            addCapturedDataRecordsToErrorMessage(e, capturedDataRecords);
            throw e;
        }
    }

    public static void addCapturedDataRecordsToErrorMessage(Throwable error, final List<Data> capturedDataRecords)
    {
        final String originalMessage = ThrowableUtils.getMessage(error);
        final String extendedMessage = originalMessage + "\nLogged data records:\n - " +
                                       capturedDataRecords.stream().map(Object::toString).collect(Collectors.joining("\n - "));

        ThrowableUtils.setMessage(error, extendedMessage);

        // Stupid ArgumentsAreDifferent has its own duplicate message field,
        // which it's using instead of Throwable.detailMessage.
        if (error instanceof ArgumentsAreDifferent)
        {
            setMessageFieldInArgumentsAreDifferentError(error, extendedMessage);
        }
    }

    static void setMessageFieldInArgumentsAreDifferentError(Throwable error, String msg)
    {
        if (MESSAGE_FIELD_IN_ARGUMENTSAREDIFFERENT_ERROR == null)
        {
            return;
        }

        try
        {
            MESSAGE_FIELD_IN_ARGUMENTSAREDIFFERENT_ERROR.set(error, msg);
        }
        catch (final Exception x)
        {
        }
    }

    private static final Field MESSAGE_FIELD_IN_ARGUMENTSAREDIFFERENT_ERROR;
    static
    {
        // HACK: make the private field "message" of class ArgumentsAreDifferent accessible
        Field field = null;
        try
        {
            field = ArgumentsAreDifferent.class.getDeclaredField("message");
            field.setAccessible(true);
        }
        catch (final Exception e)
        {
            field = null;
        }
        MESSAGE_FIELD_IN_ARGUMENTSAREDIFFERENT_ERROR = field;
    }

    interface LoadTestImplementation
    {
        default void beforeClass() throws Throwable
        {
        }

        default void before() throws Throwable
        {
        }

        void test() throws Throwable;

        default void after() throws Throwable
        {
        }

        default void afterClass() throws Throwable
        {
        }
    }

    static class GenericLoadTestClasses
    {
        public static class XltDerived extends AbstractTestCase
        {
            @Before
            public void before() throws Throwable
            {
                loadTestImplementations.get(Thread.currentThread()).before();
            }

            @BeforeClass
            static public void beforeClass() throws Throwable
            {
                loadTestImplementations.get(Thread.currentThread()).beforeClass();
            }

            @After
            public void after() throws Throwable
            {
                loadTestImplementations.get(Thread.currentThread()).after();
            }

            @AfterClass
            static public void afterClass() throws Throwable
            {
                loadTestImplementations.get(Thread.currentThread()).afterClass();
            }

            @Test
            public void test() throws Throwable
            {
                loadTestImplementations.get(Thread.currentThread()).test();
            }
        }

        public static class NotDerived
        {
            @Before
            public void before() throws Throwable
            {
                loadTestImplementations.get(Thread.currentThread()).before();
            }

            @BeforeClass
            static public void beforeClass() throws Throwable
            {
                loadTestImplementations.get(Thread.currentThread()).beforeClass();
            }

            @After
            public void after() throws Throwable
            {
                loadTestImplementations.get(Thread.currentThread()).after();
            }

            @AfterClass
            static public void afterClass() throws Throwable
            {
                loadTestImplementations.get(Thread.currentThread()).afterClass();
            }

            @Test
            public void test() throws Throwable
            {
                loadTestImplementations.get(Thread.currentThread()).test();
            }
        }

        private static final Map<Thread, LoadTestImplementation> loadTestImplementations = createThreadSafeWeakHashMap();

        public static void setGenericLoadTestImplementationFor(final Thread thread, final LoadTestImplementation behavior)
        {
            loadTestImplementations.put(thread, behavior);
        }
    }

    enum KindOfLoadTestClass
    {
     /**
      * Use a load test class that is derived from {@link AbstractTestCase}
      */
     XltDerived(GenericLoadTestClasses.XltDerived.class, true),

     /**
      * Use a load test class that is not derived from anything except Object
      */
     NotDerived(GenericLoadTestClasses.NotDerived.class, false);

        private KindOfLoadTestClass(Class<?> genericTestClassObject, boolean isXltDerived)
        {
            this.genericTestClassObject = genericTestClassObject;
            this.isXltDerived = isXltDerived;
        }

        private final Class<?> genericTestClassObject;

        private final boolean isXltDerived;

        public Class<?> getGenericLoadTestClassObject()
        {
            return genericTestClassObject;
        }

        /**
         * Tells if the load test execution thread will be a {@link LoadTestRunner}. This method can be used in
         * {@linkplain Assume assumptions} if a test only make sense with a load test class derived from
         * {@link AbstractTestCase}.
         *
         * @return {@code true} if and only if the load test class will be derived from {@link AbstractTestCase}
         */
        public boolean isXltDerived()
        {
            return isXltDerived;
        }
    }

    enum TestExecutionThreadStrategy
    {
     /**
      * Use a {@link LoadTestRunner} thread to execute the load test class
      */
     LoadTestRunner(true)
     {
         @Override
         public Thread createThreadFor(Class<?> loadTestClassObject, TestUserConfiguration testUserConfiguration, AgentInfo agentInfo,
                                       DataRecordLoggingTest thisTestInstance)
         {
             return new LoadTestRunner(testUserConfiguration, agentInfo, dummyExecutionTimer());
         }

     },

     /**
      * Use a simple thread that will just call JUnit's
      * <code>{@linkplain Request#aClass(Class) Request.aClass(Class)}.getRunner().run(RunNotifier)</code> to execute
      * the load test class
      */
     JUnitClassRequestRunner(false)
     {
         @Override
         public Thread createThreadFor(Class<?> loadTestClassObject, TestUserConfiguration testUserConfiguration, AgentInfo agentInfo,
                                       DataRecordLoggingTest thisTestInstance)
         {
             final Runnable r = () -> Request.aClass(loadTestClassObject).getRunner().run(new RunNotifier());
             return new Thread(new ThreadGroup("JUnitRequestRunner"), r);
         }
     };

        private TestExecutionThreadStrategy(final boolean usesLoadTestRunner)
        {
            this.usesLoadTestRunner = usesLoadTestRunner;
        }

        private final boolean usesLoadTestRunner;

        /**
         * Tells if the load test execution thread will be a {@link LoadTestRunner}. This method can be used in
         * {@linkplain Assume assumptions} if a test only make sense with a LoadTestRunner.
         *
         * @return {@code true} if and only if the load test execution thread will be a {@link LoadTestRunner}
         */
        public boolean usesLoadTestRunner()
        {
            return usesLoadTestRunner;
        }

        public abstract Thread createThreadFor(Class<?> loadTestClassObject, TestUserConfiguration testUserConfiguration,
                                               AgentInfo agentInfo, DataRecordLoggingTest thisTestInstance);
    }

    private String configuredUserName = null;

    private Thread createLoadTestExecutionThread(TestUserConfiguration testUserConfiguration, AgentInfo agentInfo,
                                                 LoadTestImplementation loadTestImplementation)
    {
        final Thread testExecutionThread = testExecutionThreadStrategy.createThreadFor(kindOfLoadTestClass.getGenericLoadTestClassObject(),
                                                                                       testUserConfiguration, agentInfo, this);
        GenericLoadTestClasses.setGenericLoadTestImplementationFor(testExecutionThread, loadTestImplementation);
        configuredUserName = testUserConfiguration.getUserName();
        return testExecutionThread;
    }

    private Thread createLoadTestExecutionThread(LoadTestImplementation loadTestImplementation)
    {
        return createLoadTestExecutionThread(minimumTestUserConfigurationFor(kindOfLoadTestClass.genericTestClassObject, this),
                                             new AgentInfo(DEFAULT_AGENT_ID, null), loadTestImplementation);
    }

    private static TestUserConfiguration minimumTestUserConfigurationFor(Class<?> loadTestClassObject,
                                                                         DataRecordLoggingTest thisTestInstance)
    {
        final TestUserConfiguration userConfig = new TestUserConfiguration();
        userConfig.setTestCaseClassName(loadTestClassObject.getName());
        userConfig.setUserName(thisTestInstance.defaultUserNameForTestUserConfigurations());
        userConfig.setNumberOfIterations(1);
        return userConfig;
    }

    @Rule
    public final TestName testName = new TestName();

    private String defaultUserNameForTestUserConfigurations()
    {
        return testName.getMethodName().replace("[", "/").replace("]", "");
    }

    private String expectedUserName()
    {
        if (testExecutionThreadStrategy.usesLoadTestRunner())
        {
            return configuredUserName;
        }
        if (kindOfLoadTestClass.isXltDerived())
        {
            return kindOfLoadTestClass.getGenericLoadTestClassObject().getSimpleName();
        }
        return "UnknownUser";
    }

    private static AgentInfo defaultAgentInfo()
    {
        return new AgentInfo("agentID", null);
    }

    private static <K, V> Map<K, V> createThreadSafeWeakHashMap()
    {
        // return new com.google.common.collect.MapMaker().concurrencyLevel(1).weakKeys().makeMap();
        return java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    }
}
