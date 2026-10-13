package base;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;
import utils.Log;

import java.time.Duration;
import java.util.List;

/**
 * Parent of every page class.
 *
 * Gives every page safe versions of click, type and wait.
 * These wait for the element and try again if the page reloads,
 * so tests keep working on slow networks.
 *
 * Note for page classes: declare @FindBy fields WITHOUT a value
 * (write "private WebElement button;" not "private WebElement button = null;").
 */
public class BasePage {

    private static final int RETRIES = 3;

    protected WebDriver driver;
    protected WebDriverWait wait;

    // The app's loading spinner, which can appear on any page
    @FindBy(css = ".page-loader")
    private List<WebElement> loaders;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("wait.timeout")));
        this.wait.ignoring(StaleElementReferenceException.class);
        PageFactory.initElements(driver, this);
    }


    // ========== OPENING PAGES ==========

    protected void openUrl(String url) {
        for (int attempt = 1; attempt <= RETRIES; attempt++) {
            try {
                driver.get(url);   // waits for the page to load (up to page.load.timeout)
                return;
            } catch (TimeoutException e) {
                Log.detail("Page is loading slowly, trying again");
                if (attempt == RETRIES) {
                    throw e;
                }
            }
        }
    }

    protected boolean waitForUrlContains(String text) {
        try {
            return wait.until(ExpectedConditions.urlContains(text));
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected void switchToFrame(WebElement frame) {
        driver.switchTo().defaultContent();
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frame));
    }


    // ========== ACTIONS ==========

    protected void click(WebElement element) {
        for (int attempt = 1; attempt <= RETRIES; attempt++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(element)).click();
                return;
            } catch (StaleElementReferenceException | ElementClickInterceptedException e) {
                // The click did not happen (page reloaded or something was covering it)
                if (attempt == RETRIES) {
                    jsClick(element);
                    return;
                }
                waitForLoader();
            }
        }
    }

    protected void type(WebElement field, String text) {
        for (int attempt = 1; attempt <= RETRIES; attempt++) {
            try {
                wait.until(ExpectedConditions.visibilityOf(field));
                field.clear();
                field.sendKeys(text);

                // Check the text really went in
                if (text.equals(field.getDomProperty("value"))) {
                    return;
                }
            } catch (StaleElementReferenceException e) {
                Log.detail("Field reloaded, typing again");
            }
        }
        throw new RuntimeException("Could not type into the field");
    }

    // For Select2 dropdowns: open the dropdown, click the option, check it was selected
    protected void selectFromDropdown(WebElement dropdown, String optionText) {
        click(dropdown);

        By option = By.xpath("//li[contains(@class,'select2-results__option') and normalize-space()='" + optionText + "']");
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();

        wait.until(ExpectedConditions.textToBePresentInElement(dropdown, optionText));
    }

    protected void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }


    // ========== WAITS AND CHECKS ==========

    protected void waitForVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected void waitForInvisible(WebElement element) {
        wait.until(ExpectedConditions.invisibilityOf(element));
    }

    protected void waitForLoader() {
        wait.until(ExpectedConditions.invisibilityOfAllElements(loaders));
    }

    // Checks right now, without waiting
    protected boolean isVisibleNow(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
}
