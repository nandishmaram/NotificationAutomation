package pages;

import base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.ConfigReader;

/**
 * The form screen that shows the Add button.
 * The form itself lives inside an iframe.
 */
public class FormListPage extends BasePage {

    // ========== LOCATORS ==========

    @FindBy(tagName = "iframe")
    private WebElement formFrame;

    @FindBy(id = "addButton")
    private WebElement addButton;


    // ========== CONSTRUCTOR ==========

    public FormListPage(WebDriver driver) {

        super(driver);
    }


    // ========== ACTIONS ==========

    public void open(boolean alwaysEnabledForm) {
        if (alwaysEnabledForm) {
            openUrl(ConfigReader.getUrl("form.always.enabled.path"));
        } else {
            openUrl(ConfigReader.getUrl("form.always.disabled.path"));
        }

        switchToFrame(formFrame);
        waitForLoader();
    }

    public void clickAdd() {
        waitForLoader();
        click(addButton);
        waitForLoader();
    }
}
