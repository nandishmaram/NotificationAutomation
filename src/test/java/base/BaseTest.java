package base;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import utils.ConfigReader;
import utils.EmailSender;
import utils.Log;
import utils.PdfReport;
import utils.ReportManager;
import utils.RunSummary;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;

/**
 * Parent of every test class.
 * - Creates the HTML report at the start of the run and saves it at the end
 * - Opens and closes the browser
 * - Adds each test to the report, with a screenshot when it fails
 */
public class BaseTest {

    protected WebDriver driver;


    // ========== REPORT AND EMAIL (once per run) ==========

    @BeforeSuite(alwaysRun = true)
    public void startReport() {
        RunSummary.startRun();
        ReportManager.startReport();
    }

    @AfterSuite(alwaysRun = true)
    public void saveAndSendReport() {
        ReportManager.endReport();
        String pdfReport = PdfReport.create(ReportManager.getReportPath());
        EmailSender.sendReport(pdfReport, ReportManager.getReportPath());
    }


    // ========== BROWSER (once per test class) ==========

    @BeforeClass
    public void openBrowser() {
        ChromeOptions options = new ChromeOptions();

        if (ConfigReader.getBoolean("browser.headless")) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("page.load.timeout")));
        driver.manage().window().maximize();
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }


    // ========== EACH TEST ==========

    // Adds the test to the report, e.g. "TC_01 | AUTO_REG_001" or "verifyLogin"
    @BeforeMethod
    public void addTestToReport(Method method, Object[] testData) {
        String name = (testData.length > 0) ? testData[0].toString() : method.getName();
        ReportManager.startTest(name);
    }

    // Shows the final result in the console and the report
    @AfterMethod
    public void updateReport(ITestResult result) {
        if (result.getStatus() == ITestResult.SUCCESS) {
            Log.testPassed();

        } else if (result.getStatus() == ITestResult.FAILURE) {
            String reason = Log.failureReason(result.getThrowable());
            String screenshot = takeScreenshot(result.getMethod().getMethodName());
            Log.testFailed(reason, screenshot);

        } else if (result.getStatus() == ITestResult.SKIP) {
            Log.testSkipped();
        }
    }

    // Saves a screenshot in the "screenshots" folder and returns it for the report
    private String takeScreenshot(String testName) {
        try {
            String base64 = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);

            Path folder = Paths.get("screenshots");
            Files.createDirectories(folder);
            String fileName = testName + "_" + System.currentTimeMillis() + ".png";
            Files.write(folder.resolve(fileName), Base64.getDecoder().decode(base64));

            Log.console("Screenshot saved: screenshots/" + fileName);
            return base64;

        } catch (Exception e) {
            System.out.println("Could not take screenshot: " + e.getMessage());
            return null;
        }
    }
}
