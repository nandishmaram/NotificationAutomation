package utils;

import org.testng.Reporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Writes test progress to:
 *  - the ExtentReports HTML report (always)
 *  - TestNG's report, as "Reporter output" (always)
 *  - the console (only when console.logs=true)
 *
 * Example:
 * ==================================================================
 *  TC_01  |  Reference ID: AUTO_REG_001  |  Form: Always Enabled
 * ==================================================================
 * [Step 1] Open form ................................ done
 */
public class Log {

    private static final String DOUBLE_LINE = "==================================================================";
    private static final String SINGLE_LINE = "---------------------------------------------------";
    private static final String INDENT = "         ";
    private static final int STEP_WIDTH = 50;

    // console.logs=true in config.properties prints the logs in the console too.
    // The HTML report is always written, whatever this is set to.
    private static final boolean CONSOLE_ENABLED = ConfigReader.getBoolean("console.logs");

    // Rows of the results table, collected until the table is finished
    private static final List<String[]> resultRows = new ArrayList<>();


    // ========== TEST HEADER ==========

    public static void testHeader(String testId, String referenceId, String formName) {
        console();
        console(DOUBLE_LINE);
        console(" " + testId + "  |  Reference ID: " + referenceId + "  |  Form: " + formName);
        console(DOUBLE_LINE);

        ReportManager.addCategory(formName);
        ReportManager.info("Reference ID: " + referenceId + "  |  Form: " + formName);
    }


    // ========== STEPS ==========

    // Call at the START of a step. Prints nothing; the report uses it
    // to show which step was running if the test fails.
    public static void startStep(String stepName) {
        ReportManager.setCurrentStep(stepName);
    }

    // [Step 1] Open form ................................ done
    public static void stepDone(int number, String name) {
        String text = "[Step " + number + "] " + name + " ";
        String dots = ".".repeat(Math.max(3, STEP_WIDTH - text.length()));
        console(text + dots + " done");
        ReportManager.pass("Step " + number + ": " + name);
        RunSummary.addLog("PASS", "Step " + number + ": " + name);
    }

    // [Step 5] Wait for emails (expected 1)    <- for steps with more lines below
    public static void step(int number, String name) {
        console("[Step " + number + "] " + name);
        startStep("Step " + number + ": " + name);
        ReportManager.info("Step " + number + ": " + name);
        RunSummary.addLog("INFO", "Step " + number + ": " + name);
    }

    // Indented line under a step
    public static void detail(String text) {
        console(INDENT + text);
        ReportManager.info(text.trim());
        RunSummary.addLog("INFO", text.trim());
    }


    // ========== RESULTS TABLE ==========

    public static void resultTableStart() {
        resultRows.clear();
        print(SINGLE_LINE);
        print(String.format("%-18s %-10s %-8s %s", "Check", "Expected", "Actual", "Result"));
        print(SINGLE_LINE);
    }

    public static void resultRow(String check, int expected, int actual) {
        printRow(check, String.valueOf(expected), String.valueOf(actual));
    }

    public static void resultRow(String check, boolean expected, boolean actual) {
        printRow(check, yesNo(expected), yesNo(actual));
    }

    public static void resultTableEnd() {
        print(SINGLE_LINE);
        ReportManager.addResultsTable(resultRows);
    }

    private static void printRow(String check, String expected, String actual) {
        String result = expected.equals(actual) ? "OK" : "MISMATCH";
        print(String.format("%-18s %-10s %-8s %s", check, expected, actual, result));
        resultRows.add(new String[]{check, expected, actual});
    }

    // Console only (the report shows the table as a real table instead)
    private static void print(String text) {
        console(INDENT + text);
    }


    // ========== FINAL RESULT OF EACH TEST ==========

    public static void testPassed() {
        console(INDENT + "RESULT: PASS");
        ReportManager.pass("Test passed");
    }

    public static void testFailed(String reason, String screenshotBase64) {
        String step = ReportManager.getCurrentStep();
        console(INDENT + "RESULT: FAIL");
        if (!step.isEmpty()) {
            console(INDENT + "Failed at: " + step);
        }
        console(INDENT + "Reason   : " + reason);
        ReportManager.fail(reason, screenshotBase64);
        RunSummary.addScreenshot(screenshotBase64);
    }

    public static void testSkipped() {
        console(INDENT + "RESULT: SKIPPED");
        ReportManager.skip("Test skipped");
    }


    // ========== HELPERS ==========

    // A short, readable reason from an error:
    //  - Selenium errors: only the first line (they are very long)
    //  - soft assert failures: all the mismatches on one line
    public static String failureReason(Throwable error) {
        if (error == null || error.getMessage() == null) {
            return error == null ? "" : error.getClass().getSimpleName();
        }

        String message = error.getMessage();
        if (message.startsWith("The following asserts failed:")) {
            return message.replace("The following asserts failed:", "")
                    .replace("[true]", "[YES]")
                    .replace("[false]", "[NO]")
                    .replaceAll("\\s+", " ")
                    .trim();
        }
        return message.split("\n")[0];
    }

    // Writes the line to TestNG's report (Reporter output) always,
    // and prints it in the console only when console.logs=true
    public static void console(String text) {
        Reporter.log(text, CONSOLE_ENABLED);
    }

    public static void console() {
        console("");
    }


    // "1 email" or "3 emails"
    public static String emails(int count) {
        return count == 1 ? "1 email" : count + " emails";
    }

    private static String yesNo(boolean value) {
        return value ? "YES" : "NO";
    }
}
