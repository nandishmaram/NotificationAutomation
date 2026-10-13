# Notification Automation

Tests that submitting the notification forms sends the right emails.

## Project structure

| Folder | What it contains |
|---|---|
| `src/test/java/base` | `BaseTest` (opens/closes the browser) and `BasePage` (safe click, type and wait) |
| `src/test/java/pages` | One class per screen. Locators at the top, actions below |
| `src/test/java/steps` | The steps of a test: each step uses the pages and prints its log line |
| `src/test/java/tests` | Test classes. Only the list of steps, no Selenium or logging code |
| `src/test/java/utils` | Config, Excel and Gmail helpers, and the test data class |
| `src/test/resources` | `config.properties` and `credentials.json` |
| `testdata` | The Excel file with test cases and results |

## Before running

1. Put `credentials.json` (Gmail API) in `src/test/resources`.
2. Put `NotificationTestData.xlsx` in `testdata` and keep it closed in Excel while tests run.
3. Set the environment variable `UNTANGLED_PASSWORD` (in IntelliJ: Run > Edit Configurations > Environment variables).
4. To email the report, set `REPORT_EMAIL_PASSWORD` (an app password for the `report.email.from` account). Sender and recipients are in `config.properties` (sent through Gmail); set `report.email.enabled=false` to turn emailing off.

## Running

Right-click `testng.xml` > Run, or run `mvn test` in the terminal.

Results are written back to the Excel file. An HTML report and a PDF report are saved in `reports` (one of each per run, with steps, results and screenshots of failures). The PDF is the one attached to the email, because email apps can preview it. Screenshots are also saved in `screenshots`.

## Adding a new page

1. Create a class in `pages` that `extends BasePage`.
2. Add `@FindBy` locators at the top (no `= null`).
3. Add a constructor that calls `super(driver)`.
4. Add action methods using `click()`, `type()` and the other `BasePage` methods.

## Rules

- No `Thread.sleep()` and no `driver.findElement()` in pages or tests. Use the `BasePage` methods.
- No locators in tests. No assertions in pages.
- No hardcoded data. Use Excel or `config.properties`.
- On slow networks, increase the timeouts in `config.properties` instead of changing code.
