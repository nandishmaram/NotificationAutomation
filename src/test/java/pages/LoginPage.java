package pages;

import base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.ConfigReader;

public class LoginPage extends BasePage {

    // ========== LOCATORS ==========

    @FindBy(name = "email")
    private WebElement emailField;

    @FindBy(name = "password")
    private WebElement passwordField;

    @FindBy(xpath = "//button[.//span[normalize-space()='Continue']]")
    private WebElement continueButton;


    // ========== CONSTRUCTOR ==========

    public LoginPage(WebDriver driver) {

        super(driver);
    }


    // ========== ACTIONS ==========

    public void open() {
        openUrl(ConfigReader.getUrl("login.path"));
    }

    public void login(String email, String password) {
        type(emailField, email);
        type(passwordField, password);
        click(continueButton);
    }

    public boolean isDashboardDisplayed() {

        return waitForUrlContains(ConfigReader.get("dashboard.path"));
    }
}
