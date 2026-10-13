package steps;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import pages.AlwaysDisabledForm;
import pages.AlwaysEnabledForm;
import pages.FormListPage;
import pages.LoginPage;
import pages.SuccessPopup;
import utils.ConfigReader;
import utils.ExcelReader;
import utils.GmailReader;
import utils.Log;
import utils.NotificationData;
import utils.ReportManager;
import utils.RunSummary;

import java.util.List;

/**
 * The steps of a notification test.
 * Each method does one step using the pages, and prints its log line.
 * This keeps NotificationTest short and free of logging code.
 */
public class NotificationSteps {

    private final LoginPage loginPage;
    private final FormListPage formListPage;
    private final AlwaysEnabledForm alwaysEnabledFormPage;
    private final AlwaysDisabledForm alwaysDisabledFormPage;
    private final SuccessPopup successPopup;

    // Result of the last form submission (checked by the test)
    private boolean formSaved;
    private String popupMessage = "";

    public NotificationSteps(WebDriver driver) {
        loginPage = new LoginPage(driver);
        formListPage = new FormListPage(driver);
        alwaysEnabledFormPage = new AlwaysEnabledForm(driver);
        alwaysDisabledFormPage = new AlwaysDisabledForm(driver);
        successPopup = new SuccessPopup(driver);
    }


    // ========== LOGIN ==========

    // Returns true if the dashboard opened after logging in
    public boolean login() {
        Log.startStep("Login");
        loginPage.open();
        loginPage.login(ConfigReader.get("username"), ConfigReader.getPassword());

        boolean loggedIn = loginPage.isDashboardDisplayed();
        Log.detail(loggedIn ? "Login successful" : "Login failed: dashboard not displayed");
        return loggedIn;
    }


    // ========== TEST CASE STEPS ==========

    public void startTestCase(NotificationData data) {
        RunSummary.startTestCase(data);
        Log.testHeader(data.getTestId(), data.getReferenceId(), data.getFormName());
    }

    // Step 1
    public void openForm(NotificationData data) {
        Log.startStep("Step 1: Open form");
        formListPage.open(data.isAlwaysEnabledForm());
        Log.stepDone(1, "Open form");
    }

    // Step 2
    public void clickAdd() {
        Log.startStep("Step 2: Click Add");
        formListPage.clickAdd();
        Log.stepDone(2, "Click Add");
    }

    // Step 3
    public void fillForm(NotificationData data) {
        Log.startStep("Step 3: Fill form");
        if (data.isAlwaysEnabledForm()) {
            alwaysEnabledFormPage.fillForm(data);
        } else {
            alwaysDisabledFormPage.fillForm(data);
        }
        Log.stepDone(3, "Fill form");
    }

    // Step 4: returns the time of submitting, used to find the emails
    public long submitForm(NotificationData data) {
        Log.startStep("Step 4: Submit form");
        if (data.isAlwaysEnabledForm()) {
            alwaysEnabledFormPage.submit();
        } else {
            alwaysDisabledFormPage.submit();
        }

        // Taken right after clicking Submit
        long submitTime = System.currentTimeMillis();

        formSaved = successPopup.isSuccessDisplayed();
        popupMessage = successPopup.getMessage();

        if (formSaved) {
            Log.stepDone(4, "Submit form");
            Log.detail("Popup: \"" + popupMessage + "\"");
            successPopup.clickOk();
        } else {
            Log.detail("Error popup: \"" + popupMessage + "\"");
        }
        return submitTime;
    }

    public boolean isFormSaved() {

        return formSaved;
    }

    public String getPopupMessage() {

        return popupMessage;
    }

    // Step 5
    public List<String> waitForEmails(NotificationData data, long submitTime) {
        Log.step(5, "Wait for emails (expected " + data.getExpectedEmailCount() + ")");

        List<String> emails = GmailReader.waitForEmails(
                data.getReferenceId(), data.getExpectedEmailCount(), submitTime);

        Log.detail("Emails received:" + (emails.isEmpty() ? " none" : ""));
        for (int i = 0; i < emails.size(); i++) {
            Log.detail("  " + (i + 1) + ". " + emails.get(i));
        }
        return emails;
    }

    // Step 6: was an email with this subject received?
    public boolean isEmailReceived(List<String> emails, String subject) {
        return GmailReader.hasSubject(emails, subject);
    }

    // Step 6: shows the expected vs actual table and saves the actual values to Excel.
    // It does not decide PASS/FAIL; the test's assertions do that.
    public void recordResults(NotificationData data, int emailCount, boolean alwaysReceived,
                              boolean conditionReceived, boolean advanceReceived) {
        Log.step(6, "Results");
        Log.resultTableStart();
        Log.resultRow("Email count", data.getExpectedEmailCount(), emailCount);
        Log.resultRow("Always email", data.isAlwaysExpected(), alwaysReceived);
        Log.resultRow("Condition email", data.isConditionExpected(), conditionReceived);
        Log.resultRow("Advance email", data.isAdvanceExpected(), advanceReceived);
        Log.resultTableEnd();

        ExcelReader.saveActualResults(data.getTestId(), emailCount,
                alwaysReceived, conditionReceived, advanceReceived);
        RunSummary.recordActualResults(emailCount, alwaysReceived, conditionReceived, advanceReceived);
    }


    // ========== AFTER EACH TEST ==========

    // Writes the test's own result (PASS if all assertions passed) into Excel
    // and into the run summary used for the email
    public void saveStatus(ITestResult result) {
        boolean passed = result.getStatus() == ITestResult.SUCCESS;
        String reason = passed ? "" : Log.failureReason(result.getThrowable());

        if (result.getParameters().length == 0) {
            // verifyLogin has no Excel row, but a login failure should appear in the email
            if (!passed) {
                RunSummary.recordLoginFailure(reason);
            }
            return;
        }

        NotificationData data = (NotificationData) result.getParameters()[0];
        String status = passed ? "PASS" : "FAIL";

        ExcelReader.saveStatus(data.getTestId(), status);
        RunSummary.finishTestCase(status, passed ? "" : ReportManager.getCurrentStep(), reason);
        Log.detail(data.getTestId() + " -> " + status + "  (saved to Excel)");
    }
}
