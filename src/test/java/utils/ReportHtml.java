package utils;

/**
 * HTML pieces shared by the email body and the PDF report:
 * summary boxes, failed test cases and the all-test-cases table.
 * Styles are written on each element, because email apps ignore style sheets.
 */
public class ReportHtml {

    // Fixed colors written on each element (email apps ignore style sheets)
    public static final String GREEN_BG = "#EAF3DE", GREEN_TEXT = "#27500A";
    public static final String RED_BG = "#FCEBEB", RED_TEXT = "#791F1F";
    public static final String HEADER_BG = "#F1EFE8", BORDER = "#D3D1C7";
    public static final String TEXT = "#2C2C2A", MUTED = "#5F5E5A";

    private ReportHtml() {
    }


    public static String summaryCards() {
        int total = RunSummary.getTestCases().size();
        return "<table style='border-collapse:separate;border-spacing:8px 0;margin:0 0 20px -8px'><tr>"
                + card("Total", String.valueOf(total), HEADER_BG, TEXT)
                + card("Passed", String.valueOf(RunSummary.passedCount()), GREEN_BG, GREEN_TEXT)
                + card("Failed", String.valueOf(RunSummary.failedCount()), RED_BG, RED_TEXT)
                + card("Duration", RunSummary.duration(), HEADER_BG, TEXT)
                + "</tr></table>";
    }

    private static String card(String label, String value, String background, String color) {
        return "<td style='background:" + background + ";color:" + color + ";padding:10px 16px;border-radius:6px;min-width:90px'>"
                + "<div style='font-size:12px'>" + label + "</div>"
                + "<div style='font-size:22px;font-weight:bold'>" + value + "</div></td>";
    }

    public static String failedTestCases() {
        StringBuilder rows = new StringBuilder();
        for (RunSummary.TestCase testCase : RunSummary.getTestCases()) {
            if (!testCase.passed()) {
                rows.append("<tr>")
                    .append(td("<b>" + escape(testCase.testId) + "</b><br><span style='color:" + MUTED
                            + ";font-size:12px'>" + escape(testCase.referenceId) + "</span>", ""))
                    .append(td(escape(testCase.failedAt), ""))
                    .append(td(escape(testCase.reason), "color:" + RED_TEXT))
                    .append("</tr>");
            }
        }

        if (rows.length() == 0) {
            return "<p style='background:" + GREEN_BG + ";color:" + GREEN_TEXT
                    + ";padding:10px 14px;border-radius:6px'><b>All test cases passed.</b></p>";
        }

        return "<p style='font-weight:bold;margin:0 0 6px'>Failed test cases</p>"
                + "<table style='border-collapse:collapse;width:100%;font-size:13px;margin:0 0 20px'>"
                + "<tr>" + th("Test ID", "16%") + th("Failed at", "24%") + th("Reason", "60%") + "</tr>"
                + rows + "</table>";
    }

    public static String allTestCases() {
        StringBuilder html = new StringBuilder();
        html.append("<p style='font-weight:bold;margin:0 0 6px'>All test cases</p>")
            .append("<table style='border-collapse:collapse;width:100%;font-size:12px;margin:0 0 6px'>")
            .append("<tr>")
            .append(th("Test ID", "11%")).append(th("Form", "15%"))
            .append(th("Email count<br><span style='font-weight:normal'>exp &rarr; act</span>", "15%"))
            .append(th("Always<br><span style='font-weight:normal'>exp &rarr; act</span>", "15%"))
            .append(th("Condition<br><span style='font-weight:normal'>exp &rarr; act</span>", "15%"))
            .append(th("Advance<br><span style='font-weight:normal'>exp &rarr; act</span>", "15%"))
            .append(th("Status", "14%"))
            .append("</tr>");

        for (RunSummary.TestCase testCase : RunSummary.getTestCases()) {
            String rowColor = testCase.passed() ? "" : "background:" + RED_BG + ";";
            html.append("<tr style='" + rowColor + "'>")
                .append(td("<b>" + escape(testCase.testId) + "</b>", ""))
                .append(td(escape(testCase.form), ""))
                .append(check(testCase.expectedCount, testCase.actualCount))
                .append(check(testCase.expectedAlways, testCase.actualAlways))
                .append(check(testCase.expectedCondition, testCase.actualCondition))
                .append(check(testCase.expectedAdvance, testCase.actualAdvance))
                .append(statusCell(testCase.passed()))
                .append("</tr>");
        }

        html.append("</table>")
            .append("<p style='color:").append(MUTED).append(";font-size:12px;margin:0 0 20px'>")
            .append("Green = matches expected &middot; Red = mismatch &middot; &mdash; = not reached (test stopped earlier)</p>");
        return html.toString();
    }

    // One "expected -> actual" cell, green if it matches, red if not
    private static String check(Object expected, Object actual) {
        if (expected == null || actual == null) {
            return td("<span style='color:" + MUTED + "'>&mdash;</span>", "text-align:center");
        }

        boolean ok = expected.equals(actual);
        String colors = ok ? "background:" + GREEN_BG + ";color:" + GREEN_TEXT
                           : "background:" + RED_BG + ";color:" + RED_TEXT + ";font-weight:bold";
        String text = show(expected) + " &rarr; " + show(actual);

        return td("<span style='display:inline-block;padding:2px 8px;border-radius:4px;" + colors + "'>"
                + text + "</span>", "text-align:center");
    }

    private static String statusCell(boolean passed) {
        String badge = passed
                ? "<span style='background:" + GREEN_BG + ";color:" + GREEN_TEXT + ";padding:3px 10px;border-radius:4px'>PASS</span>"
                : "<span style='background:#FFFFFF;color:" + RED_TEXT + ";border:1px solid " + RED_TEXT
                  + ";padding:3px 10px;border-radius:4px'>FAIL</span>";
        return td(badge, "text-align:center");
    }

    public static String th(String text, String width) {
        return "<th style='background:" + HEADER_BG + ";color:" + MUTED + ";text-align:left;padding:7px 8px;"
                + "border:1px solid " + BORDER + ";width:" + width + "'>" + text + "</th>";
    }

    public static String td(String html, String style) {
        return "<td style='padding:7px 8px;border:1px solid " + BORDER + ";vertical-align:top;" + style + "'>"
                + html + "</td>";
    }

    // true/false shown as YES/NO
    public static String show(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? "YES" : "NO";
        }
        return String.valueOf(value);
    }

    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
