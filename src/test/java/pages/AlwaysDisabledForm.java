package pages;

import base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.NotificationData;

/**
 * The entry form for "Notification Automation Condition".
 * This form NEVER sends the Always email (Expected_Always = NO).
 * Same fields as the Always Enabled form, but with different IDs.
 */
public class AlwaysDisabledForm extends BasePage {

    // ========== LOCATORS ==========

    @FindBy(id = "text-1790583310872")
    private WebElement referenceIdField;

    @FindBy(id = "text-1790583311380")
    private WebElement customerIdField;

    @FindBy(id = "text-1790583311881")
    private WebElement customerNameField;

    @FindBy(id = "number-1790583323931")
    private WebElement equipmentSerialField;

    @FindBy(id = "text-1790583312417")
    private WebElement equipmentNameField;

    @FindBy(id = "number-1790583324380")
    private WebElement warrantyYearsField;

    @FindBy(css = "[aria-labelledby='select2-single-select-1790583328948-container']")
    private WebElement purchasedLocationDropdown;

    @FindBy(id = "number-1790583324847")
    private WebElement totalAmountField;

    @FindBy(id = "kt_modal_add_customer_submit")
    private WebElement submitButton;


    // ========== CONSTRUCTOR ==========

    public AlwaysDisabledForm(WebDriver driver) {

        super(driver);
    }


    // ========== ACTIONS ==========

    public void fillForm(NotificationData data) {
        waitForLoader();
        type(referenceIdField, data.getReferenceId());
        type(customerIdField, data.getCustomerId());
        type(customerNameField, data.getCustomerName());
        type(equipmentSerialField, data.getEquipmentSerial());
        type(equipmentNameField, data.getEquipmentName());
        type(warrantyYearsField, data.getWarrantyYears());
        selectFromDropdown(purchasedLocationDropdown, data.getPurchasedLocation());
        type(totalAmountField, data.getTotalAmount());
    }

    public void submit() {

        click(submitButton);
    }
}
