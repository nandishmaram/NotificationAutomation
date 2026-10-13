package pages;

import base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.NotificationData;

/**
 * The entry form for "Notification Automation".
 * This form ALWAYS sends the Always email (Expected_Always = YES).
 */
public class AlwaysEnabledForm extends BasePage {

    // ========== LOCATORS ==========

    @FindBy(id = "text-1790254404444")
    private WebElement referenceIdField;

    @FindBy(id = "text-1790253292200")
    private WebElement customerIdField;

    @FindBy(id = "text-1790253299565")
    private WebElement customerNameField;

    @FindBy(id = "number-1790253506182")
    private WebElement equipmentSerialField;

    @FindBy(id = "text-1790253304459")
    private WebElement equipmentNameField;

    @FindBy(id = "number-1790253307699")
    private WebElement warrantyYearsField;

    @FindBy(css = "[aria-labelledby='select2-single-select-1790253313280-container']")
    private WebElement purchasedLocationDropdown;

    @FindBy(id = "number-1790254775505")
    private WebElement totalAmountField;

    @FindBy(id = "kt_modal_add_customer_submit")
    private WebElement submitButton;


    // ========== CONSTRUCTOR ==========

    public AlwaysEnabledForm(WebDriver driver) {

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
