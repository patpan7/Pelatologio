package org.easytech.pelatologio;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import java.io.IOException;

public class LoginAutomator {
    private WebDriver driver = null;

    // Constructor to set up the driver based on the browser type in the properties file
    public LoginAutomator(boolean incognito) throws IOException {
        String brave = System.getProperty("user.home") + "\\AppData\\Local\\BraveSoftware\\Brave-Browser\\Application\\brave.exe";
        System.out.println(brave);
        System.setProperty("webdriver.chrome.driver", "C:\\web_driver\\chromedriver.exe");
        String browserType = AppSettings.loadSetting("browser");

        switch (browserType.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                if (incognito) {
                    chromeOptions.addArguments("--incognito");
                }
                driver = new ChromeDriver(chromeOptions);
                break;
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (incognito) {
                    firefoxOptions.addArguments("-private");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;
            case "brave":
                ChromeOptions braveOptions = new ChromeOptions();
                braveOptions.setBinary(brave); // ορίστε το path προς τον Brave browser
                if (incognito) {
                    braveOptions.addArguments("--incognito");
                }
                driver = new ChromeDriver(braveOptions);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browserType);
        }
    }

    // Method to open a URL and fill the login form
    public void openAndFillLoginForm(String url, String username, String password, By usernameLocator, By passwordLocator, By submitButtonLocator) {
        driver.get(url);

        // Εντοπισμός πεδίων username και password και εισαγωγή τιμών
            WebElement usernameField = driver.findElement(usernameLocator);
            WebElement passwordField = driver.findElement(passwordLocator);

            usernameField.sendKeys(username);
            passwordField.sendKeys(password);

        // Υποβολή φόρμας ή πάτημα κουμπιού αν χρειάζεται
        driver.findElement(submitButtonLocator).click();
    }

    public void openPage(String url) {
        driver.get(url);
    }

    // Close the driver
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}
