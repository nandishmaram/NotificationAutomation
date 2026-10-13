package pages;

import base.BasePage;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * The popup shown after submitting a form.
 * The same popup is used for success and for errors.
 */
public class SuccessPopup extends BasePage {

    // ========== LOCATORS ==========

    @FindBy(css = ".swal2-popup")
    private WebElement popup;

    @FindBy(css = ".swal2-popup .swal2-title")
    private WebElement title;

    // The message text (newer and older SweetAlert versions use different classes)
    @FindBy(css = ".swal2-popup .swal2-html-container, .swal2-popup .swal2-content")
    private WebElement text;

    @FindBy(css = ".swal2-popup .swal2-error")
    private WebElement errorIcon;

    @FindBy(css = "button.swal2-confirm")
    private WebElement okButton;


    // ========== CONSTRUCTOR ==========

    public SuccessPopup(WebDriver driver) {

        super(driver);
    }


    // ========== ACTIONS ==========

    public boolean isSuccessDisplayed() {
        waitForVisible(popup);
        return !isVisibleNow(errorIcon);
    }

    // Returns the popup's title, or its message text if there is no title
    public String getMessage() {
        if (isVisibleNow(title) && !title.getText().isBlank()) {
            return title.getText().trim();
        }
        if (isVisibleNow(text)) {
            return text.getText().trim();
        }
        return "";
    }

    // Closes the popup.
    // The popup now closes by itself after 1-2 seconds, so the OK button can disappear
    // at any moment. We try ONE quick click (no 30-second wait) and, if the popup has
    // already closed itself, that's fine: we just wait until it is gone.
    public void clickOk() {
        try {
            if (isVisibleNow(okButton)) {
                try {
                    okButton.click();
                } catch (ElementClickInterceptedException e) {
                    jsClick(okButton);   // popup still animating: click it directly
                }
            }
        } catch (WebDriverException e) {
            // OK button disappeared while clicking: the popup closed by itself
        }

        waitForInvisible(popup);
        waitForLoader();
    }
}
