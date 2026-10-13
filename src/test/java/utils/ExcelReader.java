package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads test data from the Excel file and writes the results back.
 */
public class ExcelReader {

    // Column numbers in the sheet (0 = column A)
    private static final int TEST_ID = 0;
    private static final int REFERENCE_ID = 1;
    private static final int CUSTOMER_ID = 2;
    private static final int CUSTOMER_NAME = 3;
    private static final int EQUIPMENT_SERIAL = 4;
    private static final int EQUIPMENT_NAME = 5;
    private static final int WARRANTY_YEARS = 6;
    private static final int PURCHASED_LOCATION = 7;
    private static final int TOTAL_AMOUNT = 8;
    private static final int EXPECTED_ALWAYS = 9;
    private static final int EXPECTED_CONDITION = 10;
    private static final int EXPECTED_ADVANCE = 11;
    private static final int EXPECTED_EMAIL_COUNT = 12;
    private static final int ACTUAL_EMAIL_COUNT = 13;
    private static final int ACTUAL_ALWAYS = 14;
    private static final int ACTUAL_CONDITION = 15;
    private static final int ACTUAL_ADVANCE = 16;
    private static final int STATUS = 17;
    private static final int ALWAYS_SUBJECT = 18;
    private static final int CONDITION_SUBJECT = 19;
    private static final int ADVANCE_SUBJECT = 20;

    private static final DataFormatter formatter = new DataFormatter();


    // ========== READ ==========

    public static Object[][] getTestData() {
        List<NotificationData> rows = new ArrayList<>();
        Set<String> referenceIds = new HashSet<>();

        try (Workbook workbook = openWorkbook()) {
            Sheet sheet = workbook.getSheetAt(0);

            // Row 0 is the header, so start from row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || getCell(row, TEST_ID).isEmpty()) {
                    continue;
                }

                String testId = getCell(row, TEST_ID);
                String referenceId = getCell(row, REFERENCE_ID);

                // Each row needs its own Reference ID, because emails are found by it
                if (referenceId.isEmpty()) {
                    throw new RuntimeException("Reference ID is empty for " + testId);
                }
                if (!referenceIds.add(referenceId)) {
                    throw new RuntimeException("Reference ID " + referenceId + " is used more than once");
                }

                // A subject is needed whenever that email is expected
                String alwaysSubject = getCell(row, ALWAYS_SUBJECT);
                String conditionSubject = getCell(row, CONDITION_SUBJECT);
                String advanceSubject = getCell(row, ADVANCE_SUBJECT);

                checkSubject(row, EXPECTED_ALWAYS, alwaysSubject, "Always_Subject", testId);
                checkSubject(row, EXPECTED_CONDITION, conditionSubject, "Condition_Subject", testId);
                checkSubject(row, EXPECTED_ADVANCE, advanceSubject, "Advance_Subject", testId);

                rows.add(new NotificationData(
                        testId,
                        referenceId,
                        getCell(row, CUSTOMER_ID),
                        getCell(row, CUSTOMER_NAME),
                        getCell(row, EQUIPMENT_SERIAL),
                        getCell(row, EQUIPMENT_NAME),
                        getCell(row, WARRANTY_YEARS),
                        getCell(row, PURCHASED_LOCATION),
                        getCell(row, TOTAL_AMOUNT),
                        getYesNo(row, EXPECTED_ALWAYS, testId),
                        getYesNo(row, EXPECTED_CONDITION, testId),
                        getYesNo(row, EXPECTED_ADVANCE, testId),
                        getNumber(row, EXPECTED_EMAIL_COUNT, testId),
                        alwaysSubject,
                        conditionSubject,
                        advanceSubject
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read the Excel file", e);
        }

        Log.console("Test cases loaded from Excel: " + rows.size());

        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) {
            data[i][0] = rows.get(i);
        }
        return data;
    }


    // ========== WRITE ==========

    // Writes the actual results for one test case
    public static void saveActualResults(String testId, int emailCount, boolean always,
                                         boolean condition, boolean advance) {
        try (Workbook workbook = openWorkbook()) {
            Row row = findRow(workbook, testId);

            setCell(row, ACTUAL_EMAIL_COUNT, String.valueOf(emailCount));
            setCell(row, ACTUAL_ALWAYS, toYesNo(always));
            setCell(row, ACTUAL_CONDITION, toYesNo(condition));
            setCell(row, ACTUAL_ADVANCE, toYesNo(advance));

            saveWorkbook(workbook);

        } catch (IOException e) {
            throw new RuntimeException("Could not update Excel. Is the file open in Excel?", e);
        }
    }

    // Writes PASS or FAIL for one test case (decided by the test's assertions)
    public static void saveStatus(String testId, String status) {
        try (Workbook workbook = openWorkbook()) {
            setCell(findRow(workbook, testId), STATUS, status);
            saveWorkbook(workbook);
        } catch (Exception e) {
            System.out.println("Could not save status for " + testId + " in Excel: " + e.getMessage());
        }
    }


    // ========== HELPERS ==========

    private static Workbook openWorkbook() throws IOException {
        try (FileInputStream file = new FileInputStream(ConfigReader.get("excel.path"))) {
            return new XSSFWorkbook(file);
        }
    }

    private static void saveWorkbook(Workbook workbook) throws IOException {
        try (FileOutputStream file = new FileOutputStream(ConfigReader.get("excel.path"))) {
            workbook.write(file);
        }
    }

    private static Row findRow(Workbook workbook, String testId) {
        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && getCell(row, TEST_ID).equalsIgnoreCase(testId)) {
                return row;
            }
        }
        throw new RuntimeException("Test ID not found in Excel: " + testId);
    }

    private static String getCell(Row row, int column) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    private static void setCell(Row row, int column, String value) {
        Cell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        cell.setCellValue(value);
    }

    private static String getYesNo(Row row, int column, String testId) {
        String value = getCell(row, column);
        if (!value.equalsIgnoreCase("YES") && !value.equalsIgnoreCase("NO")) {
            throw new RuntimeException("Expected YES or NO but found '" + value + "' for " + testId);
        }
        return value;
    }

    private static int getNumber(Row row, int column, String testId) {
        String value = getCell(row, column);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Expected a number but found '" + value + "' for " + testId);
        }
    }

    private static void checkSubject(Row row, int expectedColumn, String subject, String columnName, String testId) {
        if (getCell(row, expectedColumn).equalsIgnoreCase("YES") && subject.isEmpty()) {
            throw new RuntimeException(columnName + " is empty for " + testId + ", but that email is expected");
        }
    }

    private static String toYesNo(boolean value) {
        return value ? "YES" : "NO";
    }
}
