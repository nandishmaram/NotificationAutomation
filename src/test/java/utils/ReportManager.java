package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Builds the HTML test report (ExtentReports).
 *
 * One report per run, saved in the "reports" folder:
 *   reports/NotificationReport_09-Oct-2026_10-45.html
 *
 * Every test case gets its own section with its steps, results table,
 * and (on failure) the failed step, the reason and a screenshot.
 */
public class ReportManager {

    private static ExtentReports extent;
    private static ExtentTest test;
    private static String reportPath;
    private static String currentStep = "";

    private ReportManager() {
    }


    // ========== WHOLE RUN ==========

    public static void startReport() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy_HH-mm"));
        reportPath = ConfigReader.get("report.folder") + "/NotificationReport_" + time + ".html";

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.STANDARD);
        spark.config().setDocumentTitle("Notification Automation Report");
        spark.config().setReportName("Notification Automation - " + time.replace("_", " "));
        spark.config().setTimeStampFormat("dd MMM yyyy, hh:mm:ss a");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Application", ConfigReader.get("base.url"));
        extent.setSystemInfo("Browser", "Chrome");
        extent.setSystemInfo("Run by", System.getProperty("user.name"));
    }

    public static void endReport() {
        if (extent != null) {
            extent.flush();
            System.out.println();
            System.out.println("HTML report saved: " + reportPath);
        }
    }

    public static String getReportPath()
    {
        return reportPath;
    }


    // ========== ONE TEST CASE ==========

    public static void startTest(String name) {
        if (extent != null) {
            test = extent.createTest(name);
            currentStep = "";
        }
    }

    // Groups test cases in the report, e.g. "Always Enabled"
    public static void addCategory(String category) {
        if (test != null) {
            test.assignCategory(category);
        }
    }

    // Remembers which step is running, so a failure can say where it happened
    public static void setCurrentStep(String step) {
        currentStep = step;
    }

    public static String getCurrentStep() {
        return currentStep;
    }

    public static void info(String text) {
        if (test != null) {
            test.info(escape(text));
        }
    }

    public static void pass(String text) {
        if (test != null) {
            test.pass(escape(text));
        }
    }

    public static void skip(String reason) {
        if (test != null) {
            test.skip(escape(reason));
        }
    }

    // Adds where it failed, why, and a screenshot (screenshot may be null)
    public static void fail(String reason, String screenshotBase64) {
        if (test == null) {
            return;
        }

        String where = currentStep.isEmpty() ? "" : "<b>Failed at:</b> " + escape(currentStep) + "<br>";
        test.fail(where + "<b>Reason:</b> " + escape(reason));

        if (screenshotBase64 != null) {
            test.fail("Screenshot at failure",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(screenshotBase64).build());
        }
    }


    // ========== EXPECTED VS ACTUAL TABLE ==========

    // Each row: { check name, expected, actual }
    public static void addResultsTable(List<String[]> rows) {
        if (test == null) {
            return;
        }

        StringBuilder html = new StringBuilder();
        html.append("<table style='border-collapse:collapse;font-size:13px;min-width:420px'>")
            .append("<tr style='background:#F1EFE8;color:#444441'>")
            .append(headerCell("Check")).append(headerCell("Expected"))
            .append(headerCell("Actual")).append(headerCell("Result"))
            .append("</tr>");

        for (String[] row : rows) {
            boolean ok = row[1].equals(row[2]);
            String colors = ok ? "background:#EAF3DE;color:#27500A" : "background:#FCEBEB;color:#791F1F";

            html.append("<tr>")
                .append(cell(row[0], ""))
                .append(cell(row[1], "text-align:center"))
                .append(cell(row[2], "text-align:center;" + colors + (ok ? "" : ";font-weight:bold")))
                .append(cell(ok ? "OK" : "MISMATCH", "text-align:center;" + colors))
                .append("</tr>");
        }

        html.append("</table>");
        test.info(html.toString());
    }

    private static String headerCell(String text) {
        return "<th style='padding:6px 12px;border:1px solid #D3D1C7;text-align:left'>" + text + "</th>";
    }

    private static String cell(String text, String style) {
        return "<td style='padding:6px 12px;border:1px solid #D3D1C7;" + style + "'>" + escape(text) + "</td>";
    }

    // Stops text like email subjects from being read as HTML
    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
