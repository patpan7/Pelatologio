package org.easytech.pelatologio.batches;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;

public class myposctraper {

    public static void main(String[] args) {
        // Configure ChromeOptions to connect to an existing browser session
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Prepare CSV file
        try (PrintWriter writer = new PrintWriter(new FileWriter("mypos_export.csv"))) {
            // Write CSV Header
            writer.println("Company Name,VAT ID (AFM),Client ID,Phone,Email");

            System.out.println("Attached to existing Chrome session. Starting data extraction...");
            System.out.println("Output will be saved to mypos_export.csv");

            String originalTab = driver.getWindowHandle();

            // Find all the details buttons on the current page
            List<WebElement> detailsButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("td.sticky-col.miw_60.text-right.last_col > button > i"))
            );
            int customersOnPage = detailsButtons.size();
            System.out.println("Found " + customersOnPage + " customers on this page.");

            // Loop through each customer on the current page
            for (int i = 0; i < customersOnPage; i++) {
                List<WebElement> currentButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.cssSelector("td.sticky-col.miw_60.text-right.last_col > button > i"))
                );
                
                System.out.println("Processing customer " + (i + 1) + " of " + customersOnPage + "...");
                WebElement detailsButton = currentButtons.get(i);
                
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", detailsButton);

                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!originalTab.contentEquals(windowHandle)) {
                        driver.switchTo().window(windowHandle);
                        break;
                    }
                }

                // --- Extract Data from Details Page ---
                String companyName = findElementText(wait, By.cssSelector("#main > div > div.row > div.column-xl > div > div > div > div.tab-content > div > div:nth-child(2) > div > div.row > div:nth-child(1) > ul > li:nth-child(1) > span"));
                String vatId = findElementText(wait, By.cssSelector("#main > div > div.row > div.column-xl > div > div > div > div.tab-content > div > div:nth-child(1) > div > div.row > div:nth-child(1) > ul > li:nth-child(2) > span"));
                String clientId = findElementText(wait, By.cssSelector("#main > div > div.row > div.column-sm > div > div:nth-child(2) > div > div > div.list-box.list-box-bg.br_5 > ul > li:nth-child(1) > dl > dd"));
                String phone = findElementText(wait, By.cssSelector("#main > div > div.row > div.column-xl > div > div > div > div.tab-content > div > div:nth-child(2) > div > div.row > div:nth-child(2) > ul > li:nth-child(2) > span"));
                String email = findElementText(wait, By.cssSelector("#main > div > div.row > div.column-xl > div > div > div > div.tab-content > div > div:nth-child(2) > div > div.row > div:nth-child(1) > ul > li:nth-child(2) > span > a"));
                String status = findElementText(wait, By.cssSelector("#main > div > div.row > div.column-sm > div > div:nth-child(2) > div > div > div.list-box.list-box-bg.br_5 > div > dl > dt > span"));
                String accountStatus = findElementText(wait, By.cssSelector("#main > div > div.main-head > div > div.company-wrap > span"));


                // Write data to CSV
                writer.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    escapeCsv(companyName), escapeCsv(vatId), escapeCsv(clientId), escapeCsv(phone), escapeCsv(email), escapeCsv(status), escapeCsv(accountStatus)));

                System.out.println("  -> Extracted: " + companyName);

                driver.close();
                driver.switchTo().window(originalTab);

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#main > div.report > div > div > div > table")));
            }

            System.out.println("\nExtraction complete for the current page. Data saved to mypos_export.csv");

        } catch (Exception e) {
            System.err.println("An error occurred during the scraping test.");
            e.printStackTrace();
        } finally {
            System.out.println("Scraper has finished. You can now change the page manually and re-run.");
        }
    }

    private static String findElementText(WebDriverWait wait, By by) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).getText();
        } catch (Exception e) {
            return "N/A"; // Return N/A if element is not found
        }
    }

    private static String escapeCsv(String data) {
        if (data == null) {
            return "";
        }
        // If data contains a comma, quote, or newline, wrap it in double quotes.
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }
}
