package utils;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Sends the test report by email at the end of the run:
 *  - subject with the pass/fail counts
 *  - summary, failed test cases and all test cases in the body
 *  - the PDF report and the Excel file as attachments
 *
 * Sent through Gmail. Settings are in config.properties (report.email.*).
 */
public class EmailSender {

    private static final String GMAIL_HOST = "smtp.gmail.com";
    private static final String GMAIL_PORT = "587";

    private EmailSender() {
    }


    // ========== SEND ==========

    public static void sendReport(String pdfReportPath, String htmlReportPath) {
        if (!ConfigReader.getBoolean("report.email.enabled")) {
            System.out.println("Report email NOT sent: it is turned off (report.email.enabled=false)");
            return;
        }

        String password = System.getenv("REPORT_EMAIL_PASSWORD");
        if (password == null || password.isBlank()) {
            System.out.println("Report email NOT sent: set the REPORT_EMAIL_PASSWORD environment variable");
            return;
        }

        try {
            String from = ConfigReader.get("report.email.from");
            String to = ConfigReader.get("report.email.to");

            MimeMessage email = new MimeMessage(createSession(from, password));
            email.setFrom(new InternetAddress(from, "Notification Automation"));
            email.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            email.setSubject(buildSubject(), "UTF-8");
            email.setContent(buildContent(pdfReportPath, htmlReportPath));

            Transport.send(email);
            System.out.println("Report email sent to: " + to);

        } catch (Exception e) {
            // A problem sending the email should never fail the test run
            System.out.println("Report email NOT sent: " + e.getMessage());
        }
    }

    private static Session createSession(String from, String password) {
        Properties settings = new Properties();
        settings.put("mail.smtp.host", GMAIL_HOST);
        settings.put("mail.smtp.port", GMAIL_PORT);
        settings.put("mail.smtp.auth", "true");
        settings.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(settings, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });
    }

    // Body + attachments
    // If the PDF could not be made, the HTML report is attached instead
    // Body + attachments: PDF report, HTML report and the Excel file
    private static Multipart buildContent(String pdfReportPath, String htmlReportPath) throws Exception {
        Multipart content = new MimeMultipart();

        MimeBodyPart body = new MimeBodyPart();
        body.setContent(buildBody(), "text/html; charset=UTF-8");
        content.addBodyPart(body);

        attachIfExists(content, pdfReportPath);
        attachIfExists(content, htmlReportPath);
        attachIfExists(content, ConfigReader.get("excel.path"));
        return content;
    }

    private static void attachIfExists(Multipart content, String path) throws Exception {
        if (path != null && new File(path).exists()) {
            MimeBodyPart attachment = new MimeBodyPart();
            attachment.attachFile(new File(path));
            content.addBodyPart(attachment);
        }
    }


    // ========== SUBJECT ==========

    // e.g. "Notification Automation - 7 passed, 1 failed - 09 Oct 2026"
    private static String buildSubject() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        return "Notification Automation - " + RunSummary.passedCount() + " passed, "
                + RunSummary.failedCount() + " failed - " + date;
    }


    // ========== BODY ==========

    private static String buildBody() {
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family:Arial,sans-serif;font-size:14px;color:").append(ReportHtml.TEXT).append(";max-width:760px'>");
        html.append("<p>Hi team,</p>");
        html.append("<p>The notification automation run has finished. Here is the summary.</p>");

        html.append(ReportHtml.summaryCards());
        html.append(ReportHtml.failedTestCases());
        html.append(ReportHtml.allTestCases());

        html.append("<p>The attached PDF report has the step-by-step details of every test case and screenshots of failed ones.</p>");
        html.append("<p style='color:").append(ReportHtml.MUTED).append(";font-size:12px'>This email was sent automatically by the "
                + "Notification Automation framework. Application: ").append(ConfigReader.get("base.url"))
            .append(" &middot; Browser: Chrome</p>");
        html.append("</div>");
        return html.toString();
    }
}
