package utils;

import org.openqa.selenium.Pdf;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.print.PrintOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Makes a PDF version of the report, which email apps like Gmail can preview.
 *
 * It builds a simple report page (summary, all test cases, then the details of
 * each test case with its steps and screenshots), opens it in a hidden Chrome,
 * and prints it to PDF.
 */
public class PdfReport {

    private PdfReport() {
    }

    // Creates the PDF next to the HTML report and returns its path (null if it failed)
    public static String create(String htmlReportPath) {
        String pdfPath = htmlReportPath.replace(".html", ".pdf");
        WebDriver chrome = null;

        try {
            Path page = Files.createTempFile("notification-report", ".html");
            Files.writeString(page, buildPage());

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            chrome = new ChromeDriver(options);
            chrome.get(page.toUri().toString());

            PrintOptions print = new PrintOptions();
            print.setBackground(true);   // keep the colors

            Pdf pdf = ((ChromeDriver) chrome).print(print);
            Files.write(Paths.get(pdfPath), Base64.getDecoder().decode(pdf.getContent()));
            Files.deleteIfExists(page);

            System.out.println("PDF report saved: " + pdfPath);
            return pdfPath;

        } catch (Exception e) {
            System.out.println("PDF report NOT created: " + e.getMessage());
            return null;

        } finally {
            if (chrome != null) {
                chrome.quit();
            }
        }
    }


    // ========== THE REPORT PAGE ==========

    private static String buildPage() {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset='utf-8'>")
            .append("<style>")
            .append("body{font-family:Arial,sans-serif;font-size:12px;color:").append(ReportHtml.TEXT).append(";margin:0 3px}")
            .append("h1{font-size:20px;margin:0 0 2px}")
            .append("h2{font-size:15px;margin:22px 0 8px}")
            .append(".muted{color:").append(ReportHtml.MUTED).append("}")
            .append(".testcase{border:1px solid ").append(ReportHtml.BORDER).append(";border-radius:6px;margin:0 0 14px;break-inside:avoid}")
            .append(".head{padding:8px 12px;font-weight:bold;display:flex;justify-content:space-between}")
            .append(".body{padding:8px 12px}")
            .append(".steps{width:100%;border-collapse:collapse;font-size:11px}")
            .append(".steps td{padding:3px 6px;border-bottom:1px solid #EEEDE8;vertical-align:top}")
            .append(".badge{display:inline-block;padding:1px 6px;border-radius:3px;font-size:10px;font-weight:bold}")
            .append("img{max-width:100%;border:1px solid ").append(ReportHtml.BORDER).append(";margin-top:8px}")
            .append("</style></head><body>");

        html.append("<h1>Notification Automation report</h1>")
            .append("<div class='muted' style='margin:0 0 14px'>")
            .append(ReportHtml.escape(ConfigReader.get("base.url"))).append(" &middot; Chrome &middot; Run by ")
            .append(ReportHtml.escape(System.getProperty("user.name"))).append("</div>");

        html.append(ReportHtml.summaryCards());
        html.append(ReportHtml.failedTestCases());
        html.append(ReportHtml.allTestCases());

        html.append("<h2 style='break-before:page'>Test case details</h2>");
        for (RunSummary.TestCase testCase : RunSummary.getTestCases()) {
            html.append(testCaseDetails(testCase));
        }

        html.append("</body></html>");
        return html.toString();
    }

    // One box per test case: header, failure reason, steps, screenshot
    private static String testCaseDetails(RunSummary.TestCase testCase) {
        boolean passed = testCase.passed();
        String headColors = passed
                ? "background:" + ReportHtml.GREEN_BG + ";color:" + ReportHtml.GREEN_TEXT
                : "background:" + ReportHtml.RED_BG + ";color:" + ReportHtml.RED_TEXT;

        StringBuilder html = new StringBuilder();
        html.append("<div class='testcase'>")
            .append("<div class='head' style='").append(headColors).append("'>")
            .append("<span>").append(ReportHtml.escape(testCase.testId));
        if (!testCase.referenceId.isEmpty()) {
            html.append(" &nbsp;|&nbsp; ").append(ReportHtml.escape(testCase.referenceId))
                .append(" &nbsp;|&nbsp; ").append(ReportHtml.escape(testCase.form));
        }
        html.append("</span><span>").append(passed ? "PASS" : "FAIL").append("</span></div>")
            .append("<div class='body'>");

        if (!passed) {
            html.append("<p style='margin:0 0 8px;color:").append(ReportHtml.RED_TEXT).append("'>")
                .append("<b>Failed at:</b> ").append(ReportHtml.escape(testCase.failedAt)).append("<br>")
                .append("<b>Reason:</b> ").append(ReportHtml.escape(testCase.reason)).append("</p>");
        }

        html.append("<table class='steps'>");
        for (String[] log : testCase.logs) {
            boolean isPass = log[1].equals("PASS");
            String badgeColors = isPass
                    ? "background:" + ReportHtml.GREEN_BG + ";color:" + ReportHtml.GREEN_TEXT
                    : "background:" + ReportHtml.HEADER_BG + ";color:" + ReportHtml.MUTED;
            html.append("<tr>")
                .append("<td class='muted' style='width:80px;white-space:nowrap'>").append(log[0]).append("</td>")
                .append("<td style='width:44px'><span class='badge' style='").append(badgeColors).append("'>")
                .append(isPass ? "DONE" : "INFO").append("</span></td>")
                .append("<td>").append(ReportHtml.escape(log[2])).append("</td>")
                .append("</tr>");
        }
        html.append("</table>");

        if (testCase.screenshotBase64 != null) {
            html.append("<div class='muted' style='margin-top:8px'>Screenshot at failure</div>")
                .append("<img src='data:image/png;base64,").append(testCase.screenshotBase64).append("'>");
        }

        html.append("</div></div>");
        return html.toString();
    }
}
