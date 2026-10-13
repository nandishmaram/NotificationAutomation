package utils;

/**
 * One row of test data from the Excel file.
 */
public class NotificationData {

    private final String testId;
    private final String referenceId;
    private final String customerId;
    private final String customerName;
    private final String equipmentSerial;
    private final String equipmentName;
    private final String warrantyYears;
    private final String purchasedLocation;
    private final String totalAmount;
    private final String expectedAlways;
    private final String expectedCondition;
    private final String expectedAdvance;
    private final int expectedEmailCount;
    private final String alwaysSubject;
    private final String conditionSubject;
    private final String advanceSubject;

    public NotificationData(String testId, String referenceId, String customerId, String customerName,
                            String equipmentSerial, String equipmentName, String warrantyYears,
                            String purchasedLocation, String totalAmount, String expectedAlways,
                            String expectedCondition, String expectedAdvance, int expectedEmailCount,
                            String alwaysSubject, String conditionSubject, String advanceSubject) {
        this.testId = testId;
        this.referenceId = referenceId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.equipmentSerial = equipmentSerial;
        this.equipmentName = equipmentName;
        this.warrantyYears = warrantyYears;
        this.purchasedLocation = purchasedLocation;
        this.totalAmount = totalAmount;
        this.expectedAlways = expectedAlways;
        this.expectedCondition = expectedCondition;
        this.expectedAdvance = expectedAdvance;
        this.expectedEmailCount = expectedEmailCount;
        this.alwaysSubject = alwaysSubject;
        this.conditionSubject = conditionSubject;
        this.advanceSubject = advanceSubject;
    }


    // ========== FORM INPUT ==========

    public String getTestId() { return testId; }
    public String getReferenceId() { return referenceId; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getEquipmentSerial() { return equipmentSerial; }
    public String getEquipmentName() { return equipmentName; }
    public String getWarrantyYears() { return warrantyYears; }
    public String getPurchasedLocation() { return purchasedLocation; }
    public String getTotalAmount() { return totalAmount; }


    // ========== EXPECTED RESULTS ==========

    public int getExpectedEmailCount() { return expectedEmailCount; }
    public boolean isAlwaysExpected() { return expectedAlways.equalsIgnoreCase("YES"); }
    public boolean isConditionExpected() { return expectedCondition.equalsIgnoreCase("YES"); }
    public boolean isAdvanceExpected() { return expectedAdvance.equalsIgnoreCase("YES"); }

    // Rows that expect the Always email use the Always Enabled form; the others use the Always Disabled form
    public boolean isAlwaysEnabledForm() {
        return isAlwaysExpected();
    }

    // Shown in the logs
    public String getFormName() {
        return isAlwaysEnabledForm() ? "Always Enabled" : "Always Disabled";
    }


    // ========== EMAIL SUBJECTS (from Excel) ==========

    public String getAlwaysSubject() { return alwaysSubject; }
    public String getConditionSubject() { return conditionSubject; }
    public String getAdvanceSubject() { return advanceSubject; }


    // Shown in the TestNG report
    @Override
    public String toString() {
        return testId + " | " + referenceId;
    }
}
