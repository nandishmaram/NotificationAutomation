package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import steps.NotificationSteps;
import utils.ExcelReader;
import utils.NotificationData;

import java.util.List;

public class NotificationTest extends BaseTest {

    private NotificationSteps steps;

    @BeforeClass
    public void setUp() {

        steps = new NotificationSteps(driver);
    }

    @DataProvider(name = "notificationData")
    public Object[][] notificationData() {

        return ExcelReader.getTestData();
    }


    @Test
    public void verifyLogin() {
        boolean loggedIn = steps.login();

        Assert.assertTrue(loggedIn, "Login failed: dashboard not displayed");
    }

    @Test(dataProvider = "notificationData", dependsOnMethods = "verifyLogin")
    public void verifyNotificationEmails(NotificationData data) {
        steps.startTestCase(data);

        // Steps 1-4: fill and submit the form
        steps.openForm(data);
        steps.clickAdd();
        steps.fillForm(data);
        long submitTime = steps.submitForm(data);

        Assert.assertTrue(steps.isFormSaved(), "Form was not saved: " + steps.getPopupMessage());

        // Step 5: read the notification emails
        List<String> emails = steps.waitForEmails(data, submitTime);

        // Step 6: actual results
        int actualEmailCount = emails.size();
        boolean alwaysReceived = steps.isEmailReceived(emails, data.getAlwaysSubject());
        boolean conditionReceived = steps.isEmailReceived(emails, data.getConditionSubject());
        boolean advanceReceived = steps.isEmailReceived(emails, data.getAdvanceSubject());

        steps.recordResults(data, actualEmailCount, alwaysReceived, conditionReceived, advanceReceived);

        // Validate: actual vs expected (all four are checked, then any failures are reported together)
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(actualEmailCount, data.getExpectedEmailCount(), "Email count");
        softAssert.assertEquals(alwaysReceived, data.isAlwaysExpected(), "Always email");
        softAssert.assertEquals(conditionReceived, data.isConditionExpected(), "Condition email");
        softAssert.assertEquals(advanceReceived, data.isAdvanceExpected(), "Advance email");
        softAssert.assertAll();
    }


    // After each test, write its PASS/FAIL into Excel
    @AfterMethod
    public void saveStatusInExcel(ITestResult result) {

        steps.saveStatus(result);

    }
}
