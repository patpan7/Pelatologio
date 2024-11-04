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
    public LoginAutomator() throws IOException {
        String brave = "C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe";
        System.out.println(brave);
        String browserType = AppSettings.loadSetting("browser");

        switch (browserType.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--incognito");
                driver = new ChromeDriver(chromeOptions);
                break;
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("-private");
                driver = new FirefoxDriver(firefoxOptions);
                break;
            case "brave":
                ChromeOptions braveOptions = new ChromeOptions();
                braveOptions.setBinary(brave); // ορίστε το path προς τον Brave browser
                braveOptions.addArguments("--incognito");
                driver = new ChromeDriver(braveOptions);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browserType);
        }
    }

    // Method to open a URL and fill the login form
    public void openAndFillLoginForm(String url, String username, String password, String usernameSelector, String passwordSelector) {
        driver.get(url);

        // Locate and fill username and password fields
        WebElement usernameField = driver.findElement(By.cssSelector(usernameSelector));
        WebElement passwordField = driver.findElement(By.cssSelector(passwordSelector));

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);

        // Submit the form
        passwordField.submit();
    }

    // Close the driver
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
}
