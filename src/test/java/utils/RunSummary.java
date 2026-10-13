package utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects the result of every test case during the run,
 * so the email can show a summary at the end.
 */
public class RunSummary {

    /** The result of one test case. Values stay null if the test stopped before them. */
    public static class TestCase {
        public String testId;
        public String referenceId;
        public String form;

        public Integer expectedCount, actualCount;
        public Boolean expectedAlways, actualAlways;
        public Boolean expectedCondition, actualCondition;
        public Boolean expectedAdvance, actualAdvance;

        public String status = "FAIL";
        public String failedAt = "";
        public String reason = "";

        // Every log line of this test: { time, PASS/INFO, text }
        public final List<String[]> logs = new ArrayList<>();
        public String screenshotBase64;

        public boolean passed() {

            return "PASS".equals(status);
        }
    }

    private static final List<TestCase> testCases = new ArrayList<>();
    private static TestCase current;
    private static long runStartTime = System.currentTimeMillis();

    private RunSummary() {
    }


    // ========== DURING THE RUN ==========

    public static void startRun() {
        runStartTime = System.currentTimeMillis();
    }

    public static void startTestCase(NotificationData data) {
        current = new TestCase();
        current.testId = data.getTestId();
        current.referenceId = data.getReferenceId();
        current.form = data.getFormName();
        current.expectedCount = data.getExpectedEmailCount();
        current.expectedAlways = data.isAlwaysExpected();
        current.expectedCondition = data.isConditionExpected();
        current.expectedAdvance = data.isAdvanceExpected();
        testCases.add(current);
    }

    public static void recordActualResults(int count, boolean always, boolean condition, boolean advance) {
        if (current != null) {
            current.actualCount = count;
            current.actualAlways = always;
            current.actualCondition = condition;
            current.actualAdvance = advance;
        }
    }

    public static void finishTestCase(String status, String failedAt, String reason) {
        if (current != null) {
            current.status = status;
            current.failedAt = failedAt;
            current.reason = reason;
        }
    }

    // Login is not an Excel row, but if it fails the email should say so
    public static void recordLoginFailure(String reason) {
        TestCase login = new TestCase();
        login.testId = "Login";
        login.referenceId = "";
        login.form = "";
        login.failedAt = "Login";
        login.reason = reason;
        testCases.add(login);
        current = login;
    }

    // Called by Log for every step/detail line of the current test case
    public static void addLog(String type, String text) {
        if (current != null) {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
            current.logs.add(new String[]{time, type, text});
        }
    }

    public static void addScreenshot(String screenshotBase64) {
        if (current != null) {
            current.screenshotBase64 = screenshotBase64;
        }
    }


    // ========== FOR THE EMAIL ==========

    public static List<TestCase> getTestCases() {
        return testCases;
    }

    public static int passedCount() {
        int count = 0;
        for (TestCase testCase : testCases) {
            if (testCase.passed()) {
                count++;
            }
        }
        return count;
    }

    public static int failedCount() {
        return testCases.size() - passedCount();
    }

    // e.g. "14m 32s"
    public static String duration() {
        long seconds = (System.currentTimeMillis() - runStartTime) / 1000;
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }
}
