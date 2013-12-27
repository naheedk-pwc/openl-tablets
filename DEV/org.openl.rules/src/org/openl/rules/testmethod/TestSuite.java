package org.openl.rules.testmethod;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openl.base.INamedThing;
import org.openl.engine.OpenLSystemProperties;
import org.openl.types.IOpenMethod;
import org.openl.vm.IRuntimeEnv;

public class TestSuite implements INamedThing {
    private static final int QUEUE_SIZE = 2000;

    static ThreadPoolExecutor threadPoolExecutor;
    static {
        try {
            int threadCount = OpenLSystemProperties.getTestRunThreadCount(null);
            threadPoolExecutor = new ThreadPoolExecutor(threadCount,
                threadCount,
                1l,
                TimeUnit.MINUTES,
                new PriorityBlockingQueue<Runnable>(QUEUE_SIZE),
                new ThreadPoolExecutor.CallerRunsPolicy());
        } catch (Exception e) {
            threadPoolExecutor = new ThreadPoolExecutor(4,
                4,
                1l,
                TimeUnit.MINUTES,
                new PriorityBlockingQueue<Runnable>(QUEUE_SIZE),
                new ThreadPoolExecutor.CallerRunsPolicy());
        }
    }

    public static String VIRTUAL_TEST_SUITE = "Virtual test suite";
    private TestSuiteMethod testSuiteMethod;
    private TestDescription[] tests;

    public TestSuite(TestSuiteMethod testSuiteMethod) {
        this.testSuiteMethod = testSuiteMethod;
        tests = testSuiteMethod.getTests();
    }

    public TestSuite(TestSuiteMethod testSuiteMethod, int... testIndices) {
        this.testSuiteMethod = testSuiteMethod;
        tests = new TestDescription[testIndices.length];
        for (int i = 0; i < testIndices.length; i++) {
            tests[i] = testSuiteMethod.getTest(testIndices[i]);
        }
    }

    public TestSuite(TestDescription... tests) {
        this.tests = tests;
    }

    public TestSuiteMethod getTestSuiteMethod() {
        return testSuiteMethod;
    }

    public TestDescription[] getTests() {
        return tests;
    }

    public TestDescription getTest(int testNumber) {
        return tests[testNumber];
    }

    public TestUnitsResults invokeParallel(final Object target, final IRuntimeEnvFactory envFactory, final long ntimes) {
        final TestUnitsResults testUnitResults = new TestUnitsResults(this);
        final CountDownLatch countDownLatch = new CountDownLatch(getNumberOfTests());
        final TestUnit[] testUnitResultsArray = new TestUnit[getNumberOfTests()];
        for (int i = 0; i < getNumberOfTests(); i++) {
            final TestDescription currentTest = getTest(i);
            final int index = i;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        testUnitResultsArray[index] = currentTest.runTest(target, envFactory.buildIRuntimeEnv(), ntimes);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            };
            threadPoolExecutor.execute(runnable);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
        }
        for (int i = 0; i < getNumberOfTests(); i++) {
            testUnitResults.addTestUnit(testUnitResultsArray[i]);
        }
        return testUnitResults;
    }

    public TestUnitsResults invoke(Object target, IRuntimeEnv env, long ntimes) {
        TestUnitsResults testUnitResults = new TestUnitsResults(this);

        for (int i = 0; i < getNumberOfTests(); i++) {
            TestDescription currentTest = getTest(i);
            testUnitResults.addTestUnit(currentTest.runTest(target, env, ntimes));
        }

        return testUnitResults;
    }

    public int getNumberOfTests() {
        return tests.length;
    }

    public String getName() {
        if (testSuiteMethod != null) {
            return testSuiteMethod.getName();
        } else {
            return VIRTUAL_TEST_SUITE;
        }
    }

    /**
     * @return <code>true</code> in case this test suite is virtual, and
     *         <code>false</code> if this test suite corresponds to particular
     *         test table.
     */
    public boolean isVirtualTestSuite() {
        return testSuiteMethod == null;
    }

    public IOpenMethod getTestedMethod() {
        if (testSuiteMethod != null) {
            return testSuiteMethod.getTestedMethod();
        } else {
            return tests[0].getTestedMethod();
        }
    }

    public String getDisplayName(int mode) {
        if (testSuiteMethod != null) {
            return testSuiteMethod.getDisplayName(mode);
        } else {
            return getName();
        }
    }

    public String getUri() {
        if (testSuiteMethod != null) {
            return testSuiteMethod.getSourceUrl();
        } else {
            return getTestedMethod().getInfo().getSourceUrl();
        }
    }

    public static interface IRuntimeEnvFactory {
        public IRuntimeEnv buildIRuntimeEnv();
    }

}
